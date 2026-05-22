package com.emi.plugins.admob.preloading;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.emi.plugins.admob.AdMobNextGenPlugin;
import com.emi.plugins.admob.ActionCallback;

import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdPreloader;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration;
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo;

public class AppOpenPreloadExecutor {

    private static final String TAG = "AdMobPreload";
    private final AdMobNextGenPlugin plugin;
    private String currentAdUnitId = "";

    private long lastLoadTime = 0;

    public AppOpenPreloadExecutor(AdMobNextGenPlugin plugin) {
        this.plugin = plugin;
    }

    public void startPreload(PluginCall call, ActionCallback callback) {
        if (!plugin.isInitialized()) {
            callback.onError("Google Mobile Ads SDK has not been initialized. Please call initialize() first.");
            return;
        }
        String adUnitId = call.getString("adUnitId");
        if (adUnitId == null || adUnitId.isEmpty()) {
            callback.onError("Ad Unit ID is required.");
            return;
        }

        Double retryOpt = call.getDouble("retryInterval");
        long minLoadInterval = (retryOpt != null) ? retryOpt.longValue() : 5000L;
        long currentTime = System.currentTimeMillis();

        if ((currentTime - lastLoadTime) < minLoadInterval) {
            callback.onError("Request too fast. Please wait " + minLoadInterval + "ms to prevent invalid traffic.");
            return;
        }

        this.lastLoadTime = currentTime;

        this.currentAdUnitId = adUnitId;

        AdRequest adRequest = new AdRequest.Builder(adUnitId).build();
        PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest);

        PreloadCallback preloadCallback = new PreloadCallback() {
            @Override
            public void onAdFailedToPreload(@NonNull String preloadId, @NonNull LoadAdError loadAdError) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                ret.put("error", loadAdError.getMessage());
                plugin.notifyPluginListeners("onAppOpenPreloadFailed", ret);
            }

            @Override
            public void onAdsExhausted(@NonNull String preloadId) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                plugin.notifyPluginListeners("onAppOpenPreloadExhausted", ret);
            }

            @Override
            public void onAdPreloaded(@NonNull String preloadId, @NonNull ResponseInfo responseInfo) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                plugin.notifyPluginListeners("onAppOpenPreloaded", ret);
            }
        };

        AppOpenAdPreloader.start(adUnitId, preloadConfig, preloadCallback);

        callback.onSuccess();
    }

    public void pollAndShow(Activity activity, ActionCallback callback) {
        if (!plugin.isInitialized()) {
            callback.onError("Google Mobile Ads SDK has not been initialized. Please call initialize() first.");
            return;
        }
        if (currentAdUnitId.isEmpty()) {
            callback.onError("Preload has not been started. No AdUnit ID found.");
            return;
        }

        AppOpenAd ad = AppOpenAdPreloader.pollAd(currentAdUnitId);

        if (ad == null) {
            callback.onError("No preloaded app open ads available. Wait for 'onAppOpenPreloaded' event.");
            return;
        }

        activity.runOnUiThread(() -> {
            ad.setAdEventCallback(new AppOpenAdEventCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    plugin.notifyPluginListeners("onAppOpenAdShowed", new JSObject());
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    plugin.notifyPluginListeners("onAppOpenAdDismissed", new JSObject());
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {
                    JSObject ret = new JSObject();
                    ret.put("error", error.getMessage());
                    plugin.notifyPluginListeners("onAppOpenAdFailedToShow", ret);
                }

                @Override
                public void onAdImpression() {
                    plugin.notifyPluginListeners("onAppOpenAdImpression", new JSObject());
                }

                @Override
                public void onAdClicked() {
                    plugin.notifyPluginListeners("onAppOpenAdClicked", new JSObject());
                }

                @Override
                public void onAdPaid(@NonNull AdValue value) {
                    JSObject ret = new JSObject();
                    ret.put("adUnitId", currentAdUnitId);
                    ret.put("valueMicros", value.getValueMicros());
                    ret.put("currencyCode", value.getCurrencyCode());
                    ret.put("precisionType", value.getPrecisionType().name());
                    plugin.notifyPluginListeners("onAppOpenAdPaid", ret);
                }
            });

            ad.show(activity);
            callback.onSuccess();
        });
    }

    public void checkAvailability(PluginCall call) {
        String adUnitId = call.getString("adUnitId", currentAdUnitId);
        if (adUnitId == null || adUnitId.isEmpty()) {
            call.reject("Ad Unit ID is required to check availability.");
            return;
        }

        boolean isAvailable = AppOpenAdPreloader.isAdAvailable(adUnitId);
        JSObject ret = new JSObject();
        ret.put("isAvailable", isAvailable);
        call.resolve(ret);
    }
}
