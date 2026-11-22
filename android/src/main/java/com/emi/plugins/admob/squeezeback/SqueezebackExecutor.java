package com.emi.plugins.admob.squeezeback;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emi.plugins.admob.AdMobNextGenPlugin;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezebackAd;
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezebackAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezebackAdOptions;
import com.google.android.libraries.ads.mobile.sdk.squeezeback.SqueezebackAdRequest;
import androidx.annotation.OptIn;
import com.google.android.libraries.ads.mobile.sdk.common.ExperimentalApi;

import java.util.HashMap;
import java.util.Map;

@OptIn(markerClass = ExperimentalApi.class)
public class SqueezebackExecutor {

    private static final String TAG = "SqueezebackExecutor";
    private final AdMobNextGenPlugin plugin;

    private SqueezebackAd squeezebackAd;
    private String currentAdUnitId = "";
    private boolean isLoading = false;

    private final Map<String, Long> lastLoadTimes = new HashMap<>();

    public SqueezebackExecutor(AdMobNextGenPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAd(PluginCall call) {
        if (!plugin.isInitialized()) {
            call.reject("Google Mobile Ads SDK has not been initialized. Please call initialize() first.");
            return;
        }

        final String adUnitId = call.getString("adUnitId", "");
        if (adUnitId == null || adUnitId.isEmpty()) {
            call.reject("Ad Unit ID is required.");
            return;
        }

        final boolean isAutoShow = Boolean.TRUE.equals(call.getBoolean("isAutoShow", false));
        final String fallbackBackgroundColor = call.getString("fallbackBackgroundColor", null);

        if (squeezebackAd != null && currentAdUnitId.equals(adUnitId)) {
            JSObject ret = new JSObject();
            ret.put("adUnitId", currentAdUnitId);
            ret.put("message", "Ad already loaded (Cached)");
            plugin.notifyPluginListeners("onSqueezebackAdLoaded", ret);

            if (isAutoShow) {
                showInternal(plugin.getActivity(), fallbackBackgroundColor, call);
            } else {
                call.resolve(ret);
            }
            return;
        }

        if (isLoading) {
            call.reject("A squeezeback ad is already loading.");
            return;
        }

        Integer retryOpt = call.getInt("retryInterval", 5000);
        long retryInterval = (retryOpt != null) ? retryOpt : 5000L;
        long currentTime = System.currentTimeMillis();
        Long lastTimeObj = lastLoadTimes.get(adUnitId);
        long lastLoadTime = (lastTimeObj != null) ? lastTimeObj : 0L;

        if (retryInterval > 0 && (currentTime - lastLoadTime) < retryInterval) {
            call.reject("Request too fast for this Ad Unit. Please wait " + retryInterval + "ms.");
            return;
        }

        isLoading = true;
        lastLoadTimes.put(adUnitId, currentTime);

        SqueezebackAdRequest request = new SqueezebackAdRequest.Builder(adUnitId).build();

        SqueezebackAd.load(request, new AdLoadCallback<SqueezebackAd>() {
            @Override
            public void onAdLoaded(@NonNull SqueezebackAd ad) {
                isLoading = false;

                if (squeezebackAd != null) {
                    squeezebackAd.destroy();
                }

                squeezebackAd = ad;
                currentAdUnitId = adUnitId;
                lastLoadTimes.put(adUnitId, System.currentTimeMillis());

                setAdEventCallback(ad, adUnitId);

                JSObject ret = new JSObject();
                ret.put("adUnitId", currentAdUnitId);
                plugin.notifyPluginListeners("onSqueezebackAdLoaded", ret);

                if (isAutoShow) {
                    showInternal(plugin.getActivity(), fallbackBackgroundColor, call);
                } else {
                    call.resolve(ret);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                isLoading = false;
                Log.w(TAG, "Squeezeback ad failed to load: " + adError.getMessage());

                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                ret.put("code", adError.getCode());
                ret.put("message", adError.getMessage());
                plugin.notifyPluginListeners("onSqueezebackAdFailedToLoad", ret);

                call.reject(adError.getMessage());
            }
        });
    }

    public void showAd(PluginCall call) {
        String fallbackBackgroundColor = call.getString("fallbackBackgroundColor", null);
        showInternal(plugin.getActivity(), fallbackBackgroundColor, call);
    }

    private void showInternal(Activity activity, @Nullable String fallbackBackgroundColor, PluginCall call) {
        if (activity == null) {
            call.reject("Activity is null.");
            return;
        }

        activity.runOnUiThread(() -> {
            if (squeezebackAd == null) {
                call.reject("No squeezeback ad loaded. Call loadSqueezebackAd() first.");
                return;
            }

            if (fallbackBackgroundColor != null && !fallbackBackgroundColor.isEmpty()) {
                try {
                    int parsedColor = Color.parseColor(fallbackBackgroundColor);
                    SqueezebackAdOptions options = new SqueezebackAdOptions.Builder()
                            .setFallbackBackgroundColor(parsedColor)
                            .build();
                    squeezebackAd.show(activity, options);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Invalid fallbackBackgroundColor: " + fallbackBackgroundColor + ". Using SDK default.");
                    squeezebackAd.show(activity);
                }
            } else {
                squeezebackAd.show(activity);
            }

            call.resolve();
        });
    }

    public void hideAd(PluginCall call) {
        Activity activity = plugin.getActivity();
        if (activity == null) {
            call.reject("Activity is null.");
            return;
        }

        activity.runOnUiThread(() -> {
            if (squeezebackAd != null) {
                squeezebackAd.hide();
                call.resolve();
            } else {
                call.reject("No squeezeback ad to hide.");
            }
        });
    }

    public void destroyAd(PluginCall call) {
        Activity activity = plugin.getActivity();
        if (activity == null) {
            cleanup();
            call.resolve();
            return;
        }

        activity.runOnUiThread(() -> {
            cleanup();
            call.resolve();
        });
    }

    public void onDestroy() {
        cleanup();
    }

    private void cleanup() {
        if (squeezebackAd != null) {
            squeezebackAd.destroy();
            squeezebackAd = null;
        }
        currentAdUnitId = "";
        isLoading = false;
    }

    private void setAdEventCallback(@NonNull SqueezebackAd ad, @NonNull String adUnitId) {
        ad.setAdEventCallback(new SqueezebackAdEventCallback() {
            @Override
            public void onAdShown() {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                plugin.notifyPluginListeners("onSqueezebackAdShown", ret);
            }

            @Override
            public void onAdHidden() {

                cleanup();
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                plugin.notifyPluginListeners("onSqueezebackAdHidden", ret);
            }

            @Override
            public void onAdFailedToShow(@NonNull FullScreenContentError error) {
                cleanup();
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                ret.put("code", error.getCode());
                ret.put("message", error.getMessage());
                plugin.notifyPluginListeners("onSqueezebackAdFailedToShow", ret);
            }

            @Override
            public void onAdImpression() {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                plugin.notifyPluginListeners("onSqueezebackAdImpression", ret);
            }

            @Override
            public void onAdClicked() {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                plugin.notifyPluginListeners("onSqueezebackAdClicked", ret);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                plugin.notifyPluginListeners("onSqueezebackAdShowedFull", ret);
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                plugin.notifyPluginListeners("onSqueezebackAdDismissedFull", ret);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                ret.put("code", error.getCode());
                ret.put("message", error.getMessage());
                plugin.notifyPluginListeners("onSqueezebackAdFailedToShowFull", ret);
            }

            @Override
            public void onAdPaid(@NonNull AdValue value) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                ret.put("valueMicros", String.valueOf(value.getValueMicros()));
                ret.put("currencyCode", value.getCurrencyCode());
                ret.put("precisionType", value.getPrecisionType().ordinal());
                plugin.notifyPluginListeners("onSqueezebackAdPaid", ret);
            }
        });
    }
}
