package com.emi.plugins.admob.rewardedinterstitial;

import android.app.Activity;
import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.emi.plugins.admob.AdMobNextGenPlugin;
import com.emi.plugins.admob.ActionCallback;

import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem;
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener;

public class RewardedInterstitialExecutor {

    private final AdMobNextGenPlugin plugin;
    private RewardedInterstitialAd mRewardedInterstitialAd;
    private String currentAdUnitId = "";
    private long lastLoadTime = 0; 

    public RewardedInterstitialExecutor(AdMobNextGenPlugin plugin) {
        this.plugin = plugin;
    }

    public void load(Activity activity, PluginCall call, final ActionCallback callback) {
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

        this.currentAdUnitId = adUnitId;

        activity.runOnUiThread(() -> {
            RewardedInterstitialAd.load(
                    new AdRequest.Builder(adUnitId).build(),
                    new AdLoadCallback<RewardedInterstitialAd>() {
                        @Override
                        public void onAdLoaded(@NonNull RewardedInterstitialAd ad) {
                            mRewardedInterstitialAd = ad;
                            lastLoadTime = System.currentTimeMillis(); 

                            JSObject ret = new JSObject();
                            ret.put("adUnitId", currentAdUnitId);
                            plugin.notifyPluginListeners("onRewardedInterstitialAdLoaded", ret);

                            callback.onSuccess();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            mRewardedInterstitialAd = null;

                            JSObject ret = new JSObject();
                            ret.put("error", adError.getMessage());
                            plugin.notifyPluginListeners("onRewardedInterstitialAdFailedToLoad", ret);
                            callback.onError(adError.getMessage());
                        }
                    }
            );
        });
    }

    public void show(Activity activity, ActionCallback callback) {
        if (mRewardedInterstitialAd == null) {
            callback.onError("The rewarded interstitial ad is not ready yet.");
            return;
        }

        activity.runOnUiThread(() -> {
            final boolean[] isRewardEarned = {false};
            mRewardedInterstitialAd.setAdEventCallback(new RewardedInterstitialAdEventCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    plugin.notifyPluginListeners("onRewardedInterstitialAdShowed", new JSObject());
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    mRewardedInterstitialAd = null; 
                    if (!isRewardEarned[0]) {
                        plugin.notifyPluginListeners("onRewardedInterstitialAdSkip", new JSObject());
                    }
                    plugin.notifyPluginListeners("onRewardedInterstitialAdDismissed", new JSObject());
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {
                    mRewardedInterstitialAd = null; 
                    JSObject ret = new JSObject();
                    ret.put("error", error.getMessage());
                    plugin.notifyPluginListeners("onRewardedInterstitialAdFailedToShow", ret);
                }

                @Override
                public void onAdImpression() {
                    plugin.notifyPluginListeners("onRewardedInterstitialAdImpression", new JSObject());
                }

                @Override
                public void onAdClicked() {
                    plugin.notifyPluginListeners("onRewardedInterstitialAdClicked", new JSObject());
                }

                @Override
                public void onAdPaid(@NonNull AdValue value) {
                    JSObject ret = new JSObject();
                    ret.put("adUnitId", currentAdUnitId);
                    ret.put("valueMicros", value.getValueMicros());
                    ret.put("currencyCode", value.getCurrencyCode());
                    ret.put("precisionType", value.getPrecisionType().name());
                    plugin.notifyPluginListeners("onRewardedInterstitialAdPaid", ret);
                }
            });

            mRewardedInterstitialAd.show(activity, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    isRewardEarned[0] = true;
                    JSObject ret = new JSObject();
                    ret.put("amount", rewardItem.getAmount());
                    ret.put("type", rewardItem.getType());
                    plugin.notifyPluginListeners("onRewardedInterstitialAdReward", ret);
                }
            });

            callback.onSuccess();
        });
    }
}
