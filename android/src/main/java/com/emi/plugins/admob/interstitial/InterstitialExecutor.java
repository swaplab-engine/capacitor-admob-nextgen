package com.emi.plugins.admob.interstitial;

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
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd;
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback;

public class InterstitialExecutor {

    private final AdMobNextGenPlugin plugin;
    private InterstitialAd mInterstitialAd;
    private String currentAdUnitId = "";
    private long lastLoadTime = 0; 

    public InterstitialExecutor(AdMobNextGenPlugin plugin) {
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

        if (mInterstitialAd != null && currentAdUnitId.equals(adUnitId)) {

            JSObject ret = new JSObject();
            ret.put("adUnitId", currentAdUnitId);
            ret.put("message", "Ad already loaded (Cached)");
            plugin.notifyPluginListeners("onInterstitialAdLoaded", ret);
            callback.onSuccess();
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
            InterstitialAd.load(
                    new AdRequest.Builder(adUnitId).build(),
                    new AdLoadCallback<InterstitialAd>() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            mInterstitialAd = interstitialAd;
                            lastLoadTime = System.currentTimeMillis(); 

                            JSObject ret = new JSObject();
                            ret.put("adUnitId", currentAdUnitId);
                            plugin.notifyPluginListeners("onInterstitialAdLoaded", ret);

                            callback.onSuccess();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {

                            JSObject ret = new JSObject();
                            ret.put("error", adError.getMessage());
                            plugin.notifyPluginListeners("onInterstitialAdFailedToLoad", ret);

                            callback.onError(adError.getMessage());
                        }
                    }
            );
        });
    }

    public void show(Activity activity, ActionCallback callback) {
        if (mInterstitialAd == null) {
            callback.onError("The interstitial ad is not ready yet.");
            return;
        }

        activity.runOnUiThread(() -> {
            mInterstitialAd.setAdEventCallback(new InterstitialAdEventCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    plugin.notifyPluginListeners("onInterstitialAdShowed", new JSObject());
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    mInterstitialAd = null; 
                    plugin.notifyPluginListeners("onInterstitialAdDismissed", new JSObject());
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {
                    mInterstitialAd = null; 
                    JSObject ret = new JSObject();
                    ret.put("error", error.getMessage());
                    plugin.notifyPluginListeners("onInterstitialAdFailedToShow", ret);
                }

                @Override
                public void onAdImpression() {
                    plugin.notifyPluginListeners("onInterstitialAdImpression", new JSObject());
                }

                @Override
                public void onAdClicked() {
                    plugin.notifyPluginListeners("onInterstitialAdClicked", new JSObject());
                }

                @Override
                public void onAdPaid(@NonNull AdValue value) {
                    JSObject ret = new JSObject();
                    ret.put("adUnitId", currentAdUnitId);
                    ret.put("valueMicros", value.getValueMicros());
                    ret.put("currencyCode", value.getCurrencyCode());
                    ret.put("precisionType", value.getPrecisionType().name());
                    plugin.notifyPluginListeners("onInterstitialAdPaid", ret);
                }
            });

            mInterstitialAd.show(activity);
            callback.onSuccess();
        });
    }
}
