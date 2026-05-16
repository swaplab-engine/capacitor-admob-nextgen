package com.emi.plugins.admob.rewarded;

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
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback;

public class RewardedExecutor {

    private final AdMobNextGenPlugin plugin;
    private RewardedAd mRewardedAd;
    private String currentAdUnitId = "";
    private long lastLoadTime = 0;

    public RewardedExecutor(AdMobNextGenPlugin plugin) {
        this.plugin = plugin;
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

        if ((currentTime - lastLoadTime) < minLoadInterval) {
            callback.onError("Request too fast. Please wait " + minLoadInterval + "ms to prevent invalid traffic.");
            return;
        }

        this.currentAdUnitId = adUnitId;

        activity.runOnUiThread(() -> {
            RewardedAd.load(
                    new AdRequest.Builder(adUnitId).build(),
                    new AdLoadCallback<RewardedAd>() {
                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            mRewardedAd = rewardedAd;
                            lastLoadTime = System.currentTimeMillis(); 

                            JSObject ret = new JSObject();
                            ret.put("adUnitId", currentAdUnitId);
                            plugin.notifyListeners("onRewardedAdLoaded", ret);

                            callback.onSuccess();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            mRewardedAd = null;

                            JSObject ret = new JSObject();
                            ret.put("error", adError.getMessage());
                            plugin.notifyListeners("onRewardedAdFailedToLoad", ret);

                            callback.onError(adError.getMessage());
                        }
                    }
            );
        });
    }

    public void show(Activity activity, ActionCallback callback) {
        if (mRewardedAd == null) {
            callback.onError("The rewarded ad is not ready yet.");
            return;
        }

        activity.runOnUiThread(() -> {
            mRewardedAd.setAdEventCallback(new RewardedAdEventCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    plugin.notifyListeners("onRewardedAdShowed", new JSObject());
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    mRewardedAd = null; 
                    plugin.notifyListeners("onRewardedAdDismissed", new JSObject());
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {
                    mRewardedAd = null; 
                    JSObject ret = new JSObject();
                    ret.put("error", error.getMessage());
                    plugin.notifyListeners("onRewardedAdFailedToShow", ret);
                }

                @Override
                public void onAdImpression() {
                    plugin.notifyListeners("onRewardedAdImpression", new JSObject());
                }

                @Override
                public void onAdClicked() {
                    plugin.notifyListeners("onRewardedAdClicked", new JSObject());
                }

                @Override
                public void onAdPaid(@NonNull AdValue value) {
                    JSObject ret = new JSObject();
                    ret.put("adUnitId", currentAdUnitId);
                    ret.put("valueMicros", value.getValueMicros());
                    ret.put("currencyCode", value.getCurrencyCode());
                    ret.put("precisionType", value.getPrecisionType().name());
                    plugin.notifyListeners("onRewardedAdPaid", ret);
                }
            });

            mRewardedAd.show(activity, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    JSObject ret = new JSObject();
                    ret.put("amount", rewardItem.getAmount());
                    ret.put("type", rewardItem.getType());
                    plugin.notifyListeners("onRewardedAdReward", ret);
                }
            });
            callback.onSuccess();
        });
    }
}
