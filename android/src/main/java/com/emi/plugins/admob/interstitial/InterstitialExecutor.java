package com.emi.plugins.admob.interstitial;

import android.app.Activity;
import androidx.annotation.NonNull;

import com.emi.plugins.admob.ActionCallback;
import com.emi.plugins.admob.AdMobNextGenPlugin;
import com.getcapacitor.JSObject;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd;
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback;

public class InterstitialExecutor {

    private InterstitialAd mInterstitialAd;
    private final AdMobNextGenPlugin pluginInstance;

    public InterstitialExecutor(AdMobNextGenPlugin pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    public void load(Activity activity, String adUnitId, final ActionCallback callback) {
        activity.runOnUiThread(() -> {
            InterstitialAd.load(
                    new AdRequest.Builder(adUnitId).build(),
                    new AdLoadCallback<InterstitialAd>() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

                            mInterstitialAd = interstitialAd;
                            setEventCallbacks();
                            callback.onSuccess();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {

                            mInterstitialAd = null;
                            callback.onError(adError.getMessage());
                        }
                    }
            );
        });
    }

    public void show(Activity activity, final ActionCallback callback) {
        activity.runOnUiThread(() -> {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(activity);
                callback.onSuccess();
            } else {
                callback.onError("The interstitial ad is not ready yet.");
            }
        });
    }

    private void setEventCallbacks() {
        if (mInterstitialAd == null) return;

        mInterstitialAd.setAdEventCallback(new InterstitialAdEventCallback() {
            @Override
            public void onAdShowedFullScreenContent() {

                pluginInstance.notifyListeners("onAdShowed", new JSObject());
            }

            @Override
            public void onAdDismissedFullScreenContent() {

                mInterstitialAd = null;
                pluginInstance.notifyListeners("onAdDismissed", new JSObject());
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {

                mInterstitialAd = null;
                JSObject ret = new JSObject();
                ret.put("error", error.getMessage());
                pluginInstance.notifyListeners("onAdFailedToShow", ret);
            }

            @Override
            public void onAdImpression() {

                pluginInstance.notifyListeners("onAdImpression", new JSObject());
            }

            @Override
            public void onAdClicked() {

                pluginInstance.notifyListeners("onAdClicked", new JSObject());
            }
        });
    }
}
