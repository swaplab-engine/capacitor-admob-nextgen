package com.emi.plugins.admob.appopen;

import android.app.Activity;
import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.emi.plugins.admob.AdMobNextGenPlugin;
import com.emi.plugins.admob.ActionCallback;

import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd;
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;

import java.util.Date;

public class AppOpenExecutor {

    private final AdMobNextGenPlugin plugin;
    private AppOpenAd appOpenAd;
    private String currentAdUnitId = "";

    private boolean isLoadingAd = false;
    private boolean isShowingAd = false;
    private long loadTime = 0;

    private long lastRequestTime = 0;

    public AppOpenExecutor(AdMobNextGenPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = new Date().getTime() - loadTime;
        long numMilliSecondsPerHour = 3600000L;
        return dateDifference < (numMilliSecondsPerHour * numHours);
    }

    private boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }

    public void load(Activity activity, PluginCall call, final ActionCallback callback) {
        String adUnitId = call.getString("adUnitId");
        if (adUnitId == null || adUnitId.isEmpty()) {
            callback.onError("Ad Unit ID is required.");
            return;
        }

        Double retryOpt = call.getDouble("retryInterval");
        long minLoadInterval = (retryOpt != null) ? retryOpt.longValue() : 5000L;
        long currentTime = System.currentTimeMillis();

        if ((currentTime - lastRequestTime) < minLoadInterval) {
            callback.onError("Request too fast. Please wait " + minLoadInterval + "ms to prevent invalid traffic.");
            return;
        }

        if (isLoadingAd || isAdAvailable()) {
            callback.onError("App open ad is already loading or available.");
            return;
        }

        this.currentAdUnitId = adUnitId;
        this.lastRequestTime = currentTime;
        isLoadingAd = true;

        activity.runOnUiThread(() -> {
            AppOpenAd.load(
                    new AdRequest.Builder(adUnitId).build(),
                    new AdLoadCallback<AppOpenAd>() {
                        @Override
                        public void onAdLoaded(@NonNull AppOpenAd ad) {
                            appOpenAd = ad;
                            isLoadingAd = false;
                            loadTime = new Date().getTime(); 

                            JSObject ret = new JSObject();
                            ret.put("adUnitId", currentAdUnitId);
                            plugin.notifyPluginListeners("onAppOpenAdLoaded", ret);

                            callback.onSuccess();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            isLoadingAd = false;
                            appOpenAd = null;

                            JSObject ret = new JSObject();
                            ret.put("error", adError.getMessage());
                            plugin.notifyPluginListeners("onAppOpenAdFailedToLoad", ret);

                            callback.onError(adError.getMessage());
                        }
                    }
            );
        });
    }

    public void show(Activity activity, ActionCallback callback) {
        if (isShowingAd) {
            callback.onError("App open ad is already showing.");
            return;
        }

        if (!isAdAvailable()) {
            callback.onError("App open ad is not ready or has expired.");
            return;
        }

        activity.runOnUiThread(() -> {
            appOpenAd.setAdEventCallback(new AppOpenAdEventCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    plugin.notifyPluginListeners("onAppOpenAdShowed", new JSObject());
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    appOpenAd = null; 
                    isShowingAd = false;
                    plugin.notifyPluginListeners("onAppOpenAdDismissed", new JSObject());
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {
                    appOpenAd = null; 
                    isShowingAd = false;

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

            isShowingAd = true;
            appOpenAd.show(activity);
            callback.onSuccess();
        });
    }
}
