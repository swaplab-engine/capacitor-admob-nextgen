package com.emi.plugins.admob.preloading;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.emi.plugins.admob.AdMobNextGenPlugin;
import com.emi.plugins.admob.ActionCallback;

import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration;
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo;
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd;
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdPreloader;

public class InterstitialPreloadExecutor {

    private final AdMobNextGenPlugin plugin;
    private String currentAdUnitId = "";

    private long lastLoadTime = 0;

    public InterstitialPreloadExecutor(AdMobNextGenPlugin plugin) {
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

        Integer bufferSizeOpt = call.getInt("bufferSize");
        int finalBufferSize = 1; 

        if (bufferSizeOpt != null) {
            if (bufferSizeOpt > 3) {
                finalBufferSize = 3; 
            } else if (bufferSizeOpt < 1) {
                finalBufferSize = 1; 
            } else {
                finalBufferSize = bufferSizeOpt;
            }
        }

        AdRequest adRequest = new AdRequest.Builder(adUnitId).build();

        PreloadConfiguration preloadConfig = new PreloadConfiguration(adRequest, finalBufferSize);

        PreloadCallback preloadCallback = new PreloadCallback() {
            @Override
            public void onAdFailedToPreload(@NonNull String preloadId, @NonNull LoadAdError adError) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                ret.put("error", adError.getMessage());
                ret.put("source", "preloader");
                plugin.notifyPluginListeners("onInterstitialAdFailedToLoad", ret);
            }

            @Override
            public void onAdsExhausted(@NonNull String preloadId) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                ret.put("source", "preloader");
                plugin.notifyPluginListeners("onInterstitialPreloadExhausted", ret);
            }

            @Override
            public void onAdPreloaded(@NonNull String preloadId, @NonNull ResponseInfo responseInfo) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                ret.put("source", "preloader");
                plugin.notifyPluginListeners("onInterstitialAdLoaded", ret);
            }
        };

        InterstitialAdPreloader.start(adUnitId, preloadConfig, preloadCallback);

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

        InterstitialAd ad = InterstitialAdPreloader.pollAd(currentAdUnitId);

        if (ad == null) {
            callback.onError("No preloaded interstitial ads available. Wait for 'onInterstitialPreloaded' event.");
            return;
        }

        activity.runOnUiThread(() -> {
            ad.setAdEventCallback(new InterstitialAdEventCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    JSObject ret = new JSObject();
                    ret.put("source", "preloader");
                    plugin.notifyPluginListeners("onInterstitialAdShowed", ret);
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    JSObject ret = new JSObject();
                    ret.put("source", "preloader");
                    plugin.notifyPluginListeners("onInterstitialAdDismissed", ret);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {
                    JSObject ret = new JSObject();
                    ret.put("error", error.getMessage());
                    ret.put("source", "preloader");
                    plugin.notifyPluginListeners("onInterstitialAdFailedToShow", ret);
                }

                @Override
                public void onAdImpression() {
                    JSObject ret = new JSObject();
                    ret.put("source", "preloader");
                    plugin.notifyPluginListeners("onInterstitialAdImpression", ret);
                }

                @Override
                public void onAdClicked() {
                    JSObject ret = new JSObject();
                    ret.put("source", "preloader");
                    plugin.notifyPluginListeners("onInterstitialAdClicked", ret);
                }

                @Override
                public void onAdPaid(@NonNull AdValue value) {
                    JSObject ret = new JSObject();
                    ret.put("adUnitId", currentAdUnitId);
                    ret.put("valueMicros", value.getValueMicros());
                    ret.put("currencyCode", value.getCurrencyCode());
                    ret.put("precisionType", value.getPrecisionType().name());
                    ret.put("source", "preloader");
                    plugin.notifyPluginListeners("onInterstitialAdPaid", ret);
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

        boolean isAvailable = InterstitialAdPreloader.isAdAvailable(adUnitId);
        JSObject ret = new JSObject();
        ret.put("isAvailable", isAvailable);
        call.resolve(ret);
    }
}
