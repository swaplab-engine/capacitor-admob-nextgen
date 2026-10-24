package com.emi.plugins.admob.pip;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;

import com.emi.plugins.admob.ActionCallback;
import com.emi.plugins.admob.AdMobNextGenPlugin;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.ExperimentalApi;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAd;
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdOptions;
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdPosition;
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdPresentationScope;
import com.google.android.libraries.ads.mobile.sdk.pip.PictureInPictureAdRequest;

@OptIn(markerClass = ExperimentalApi.class)
public class PictureInPictureExecutor {
    private final AdMobNextGenPlugin plugin;
    private PictureInPictureAd pipAd;
    private PictureInPictureAdOptions pipOptions;
    private long lastRequestTime = 0;

    public PictureInPictureExecutor(AdMobNextGenPlugin plugin) {
        this.plugin = plugin;
    }

    public void load(Activity activity, PluginCall call, ActionCallback callback) {
        if (!plugin.isInitialized()) {
            callback.onError("Google Mobile Ads SDK has not been initialized. Please call initialize() first.");
            return;
        }
        String adUnitId = call.getString("adUnitId");
        if (adUnitId == null || adUnitId.isEmpty()) {
            callback.onError("adUnitId is missing or empty.");
            return;
        }

        if (activity == null) {
            callback.onError("Activity is null. Cannot create Picture-in-Picture.");
            return;
        }

        Boolean autoShowOpt = call.getBoolean("isAutoShow", false);
        boolean isAutoShow = autoShowOpt != null ? autoShowOpt : false;

        Integer retryOpt = call.getInt("retryInterval", 5000);
        int retryInterval = retryOpt != null ? retryOpt : 5000;

        PictureInPictureAdPosition position = PictureInPictureAdPosition.DEFAULT;
        String posString = call.getString("position", "DEFAULT");
        if ("TOP_LEFT".equals(posString)) position = PictureInPictureAdPosition.TOP_LEFT;
        else if ("TOP_RIGHT".equals(posString)) position = PictureInPictureAdPosition.TOP_RIGHT;
        else if ("BOTTOM_LEFT".equals(posString)) position = PictureInPictureAdPosition.BOTTOM_LEFT;
        else if ("BOTTOM_RIGHT".equals(posString))
            position = PictureInPictureAdPosition.BOTTOM_RIGHT;

        PictureInPictureAdPresentationScope scope = PictureInPictureAdPresentationScope.SCREEN;
        if ("APPLICATION".equals(call.getString("presentationScope", "SCREEN"))) {
            scope = PictureInPictureAdPresentationScope.APPLICATION;
        }

        this.pipOptions = new PictureInPictureAdOptions.Builder()
                .setPosition(position)
                .setPresentationScope(scope)
                .build();

        if (pipAd != null) {
            if (isAutoShow) {
                activity.runOnUiThread(() -> pipAd.show(activity, pipOptions));
            }
            callback.onSuccess();
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRequestTime < retryInterval) {
            callback.onError("Request too fast. Please wait " + retryInterval + "ms to prevent invalid traffic.");
            return;
        }
        lastRequestTime = currentTime;

        activity.runOnUiThread(() -> {
            PictureInPictureAdRequest request = new PictureInPictureAdRequest.Builder(adUnitId).build();
            PictureInPictureAd.load(request, new AdLoadCallback<PictureInPictureAd>() {
                @Override
                public void onAdLoaded(@NonNull PictureInPictureAd ad) {
                    if (pipAd != null) {
                        pipAd.destroy();
                    }
                    pipAd = ad;
                    setAdEventCallback(pipAd);

                    JSObject event = new JSObject();
                    event.put("adUnitId", adUnitId);
                    plugin.notifyPluginListeners("onPipAdLoaded", event);
                    if (isAutoShow) {
                        pipAd.show(activity, pipOptions);
                    }
                    callback.onSuccess();
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    JSObject event = new JSObject();
                    event.put("error", loadAdError.getMessage());
                    plugin.notifyPluginListeners("onPipAdFailedToLoad", event);
                    callback.onError(loadAdError.getMessage());
                }
            });
        });
    }

    public void show(Activity activity, ActionCallback callback) {
        if (pipAd == null) {
            callback.onError("Picture-in-Picture Ad is not loaded yet.");
            return;
        }
        if (activity == null) {
            callback.onError("Activity is null. Cannot create Picture-in-Picture.");
            return;
        }
        activity.runOnUiThread(() -> {
            pipAd.show(activity, pipOptions);
            callback.onSuccess();
        });
    }

    public void hide(ActionCallback callback) {
        if (pipAd != null) {
            pipAd.hide();
        }
        callback.onSuccess();
    }

    public void destroy() {
        if (pipAd != null) {
            pipAd.destroy();
            pipAd = null;
        }
    }

    private void setAdEventCallback(PictureInPictureAd ad) {
        ad.setAdEventCallback(new PictureInPictureAdEventCallback() {
            @Override
            public void onAdShown() {
                plugin.notifyPluginListeners("onPipAdShown", new JSObject());
            }

            @Override
            public void onAdHidden() {
                plugin.notifyPluginListeners("onPipAdHidden", new JSObject());
            }

            @Override
            public void onAdImpression() {
                plugin.notifyPluginListeners("onPipAdImpression", new JSObject());
            }

            @Override
            public void onAdClicked() {
                plugin.notifyPluginListeners("onPipAdClicked", new JSObject());
            }

            @Override
            public void onAdShowedFullScreenContent() {
                plugin.notifyPluginListeners("onPipAdShowed", new JSObject());
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                plugin.notifyPluginListeners("onPipAdDismissed", new JSObject());
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {
                JSObject event = new JSObject();
                event.put("error", error.getMessage());
                plugin.notifyPluginListeners("onPipAdFailedToShow", event);
            }

            @Override
            public void onAdPaid(@NonNull AdValue adValue) {
                JSObject event = new JSObject();
                event.put("valueMicros", adValue.getValueMicros());
                event.put("currencyCode", adValue.getCurrencyCode());
                event.put("precisionType", adValue.getPrecisionType());
                plugin.notifyPluginListeners("onPipAdPaid", event);
            }
        });
    }
}
