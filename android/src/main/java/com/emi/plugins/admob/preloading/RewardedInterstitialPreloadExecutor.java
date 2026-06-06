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
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem;
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener;
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAdPreloader;

public class RewardedInterstitialPreloadExecutor {

    private final AdMobNextGenPlugin plugin;
    private String currentAdUnitId = "";
    private long lastLoadTime = 0;

    public RewardedInterstitialPreloadExecutor(AdMobNextGenPlugin plugin) {
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
                plugin.notifyPluginListeners("onRewardedInterstitialAdFailedToLoad", ret);
            }

            @Override
            public void onAdsExhausted(@NonNull String preloadId) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                ret.put("source", "preloader");
                plugin.notifyPluginListeners("onRewardedInterstitialPreloadExhausted", ret);
            }

            @Override
            public void onAdPreloaded(@NonNull String preloadId, @NonNull ResponseInfo responseInfo) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                ret.put("source", "preloader");
                plugin.notifyPluginListeners("onRewardedInterstitialAdLoaded", ret);
            }
        };

        RewardedInterstitialAdPreloader.start(adUnitId, preloadConfig, preloadCallback);
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

        RewardedInterstitialAd ad = RewardedInterstitialAdPreloader.pollAd(currentAdUnitId);

        if (ad == null) {
            callback.onError("No preloaded rewarded interstitial ads available. Wait for 'onRewardedInterstitialPreloaded' event.");
            return;
        }

        activity.runOnUiThread(() -> {
            final boolean[] isRewardEarned = {false};
            ad.setAdEventCallback(new RewardedInterstitialAdEventCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    JSObject ret = new JSObject();
                    ret.put("source", "preloader");
                    plugin.notifyPluginListeners("onRewardedInterstitialAdShowed", ret);
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    JSObject ret = new JSObject();
                    ret.put("source", "preloader");
                    if (!isRewardEarned[0]) {
                        plugin.notifyPluginListeners("onRewardedInterstitialAdSkip", new JSObject());
                    }
                    plugin.notifyPluginListeners("onRewardedInterstitialAdDismissed", ret);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {
                    JSObject ret = new JSObject();
                    ret.put("error", error.getMessage());
                    ret.put("source", "preloader");
                    plugin.notifyPluginListeners("onRewardedInterstitialAdFailedToShow", ret);
                }

                @Override
                public void onAdImpression() {
                    JSObject ret = new JSObject();
                    ret.put("source", "preloader");
                    plugin.notifyPluginListeners("onRewardedInterstitialAdImpression", ret);
                }

                @Override
                public void onAdClicked() {
                    JSObject ret = new JSObject();
                    ret.put("source", "preloader");
                    plugin.notifyPluginListeners("onRewardedInterstitialAdClicked", ret);
                }

                @Override
                public void onAdPaid(@NonNull AdValue value) {
                    JSObject ret = new JSObject();
                    ret.put("adUnitId", currentAdUnitId);
                    ret.put("valueMicros", value.getValueMicros());
                    ret.put("currencyCode", value.getCurrencyCode());
                    ret.put("precisionType", value.getPrecisionType().name());
                    ret.put("source", "preloader");
                    plugin.notifyPluginListeners("onRewardedInterstitialAdPaid", ret);
                }
            });

            ad.show(activity, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    isRewardEarned[0] = true;
                    JSObject ret = new JSObject();
                    ret.put("amount", rewardItem.getAmount());
                    ret.put("type", rewardItem.getType());
                    ret.put("source", "preloader");
                    plugin.notifyPluginListeners("onRewardedInterstitialAdReward", ret);
                }
            });
            callback.onSuccess();
        });
    }

    public void checkAvailability(PluginCall call) {
        String adUnitId = call.getString("adUnitId", currentAdUnitId);
        if (adUnitId == null || adUnitId.isEmpty()) {
            call.reject("Ad Unit ID is required to check availability.");
            return;
        }

        boolean isAvailable = RewardedInterstitialAdPreloader.isAdAvailable(adUnitId);
        JSObject ret = new JSObject();
        ret.put("isAvailable", isAvailable);
        call.resolve(ret);
    }
}
