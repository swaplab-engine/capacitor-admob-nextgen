package com.emi.plugins.admob.preloading;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.emi.plugins.admob.AdMobNextGenPlugin;
import com.emi.plugins.admob.ActionCallback;

import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdPreloader;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration;
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo;

public class BannerPreloadExecutor {

    private final AdMobNextGenPlugin plugin;

    private BannerAd currentBannerAd;
    private View adView;
    private FrameLayout capacitorAdLayout;

    private String currentAdUnitId = "";
    private String currentPosition = "BOTTOM";

    private boolean isBannerVisible = false;
    private boolean isOverlapping = true;
    private boolean isCollapsible = false;

    private int lastAdHeight = 0;
    private int systemSafeTop = 0;
    private int systemSafeBottom = 0;

    private long lastLoadTime = 0;

    public BannerPreloadExecutor(AdMobNextGenPlugin plugin) {
        this.plugin = plugin;
    }

    public void startPreload(PluginCall call, ActionCallback callback) {
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

        String posOpt = call.getString("position", "BOTTOM");
        this.currentPosition = (posOpt != null) ? posOpt.toUpperCase() : "BOTTOM";

        Boolean overlapOpt = call.getBoolean("isOverlap", true);
        this.isOverlapping = (overlapOpt != null) ? overlapOpt : true;

        Boolean collapsibleOpt = call.getBoolean("isCollapsible", false);
        this.isCollapsible = (collapsibleOpt != null) ? collapsibleOpt : false;

        String requestSizeStr = call.getString("adSize", "ADAPTIVE");
        if (requestSizeStr == null) requestSizeStr = "ADAPTIVE";

        Integer bufferSize = call.getInt("bufferSize");

        Activity activity = plugin.getActivity();
        AdSize adSize = getAdSize(activity, requestSizeStr);
        BannerAdRequest.Builder builder = new BannerAdRequest.Builder(adUnitId, adSize);

        if (isCollapsible) {
            Bundle extras = new Bundle();
            extras.putString("collapsible", currentPosition.toLowerCase());
            builder.setGoogleExtrasBundle(extras);
        }

        PreloadConfiguration preloadConfig;
        if (bufferSize != null && bufferSize > 0) {
            preloadConfig = new PreloadConfiguration(builder.build(), bufferSize);
        } else {
            preloadConfig = new PreloadConfiguration(builder.build());
        }

        PreloadCallback preloadCallback = new PreloadCallback() {
            @Override
            public void onAdFailedToPreload(@NonNull String preloadId, @NonNull LoadAdError adError) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                ret.put("error", adError.getMessage());
                plugin.notifyListeners("onBannerPreloadFailed", ret);
            }

            @Override
            public void onAdsExhausted(@NonNull String preloadId) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                plugin.notifyListeners("onBannerPreloadExhausted", ret);
            }

            @Override
            public void onAdPreloaded(@NonNull String preloadId, @NonNull ResponseInfo responseInfo) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", adUnitId);
                plugin.notifyListeners("onBannerPreloaded", ret);
            }
        };

        BannerAdPreloader.start(adUnitId, preloadConfig, preloadCallback);
        callback.onSuccess();
    }

    public void pollAndShow(Activity activity, ActionCallback callback) {
        if (currentAdUnitId.isEmpty()) {
            callback.onError("Preload has not been started. No AdUnit ID found.");
            return;
        }

        activity.runOnUiThread(() -> {
            BannerAd ad = BannerAdPreloader.pollAd(currentAdUnitId);
            if (ad == null) {
                callback.onError("No preloaded banner ads available.");
                return;
            }

            destroyCurrentAdInternal();

            currentBannerAd = ad;
            adView = currentBannerAd.getView(activity);

            adView.post(() -> lastAdHeight = adView.getHeight());

            setupBannerCallbacks(currentBannerAd);
            setupLayout(activity);

            isBannerVisible = true;
            updateBannerLayout();
            updateWebViewMargins();

            if (adView.getParent() == null) {
                capacitorAdLayout.addView(adView);
            }
            adView.setVisibility(View.VISIBLE);
            capacitorAdLayout.setVisibility(View.VISIBLE);
            capacitorAdLayout.bringToFront();

            JSObject ret = new JSObject();
            ret.put("adUnitId", currentAdUnitId);
            ret.put("isCollapsible", currentBannerAd.isCollapsible());
            plugin.notifyListeners("onBannerPreloadShown", ret);

            callback.onSuccess();
        });
    }

    private void setupLayout(Activity activity) {
        if (capacitorAdLayout == null) {
            capacitorAdLayout = new FrameLayout(activity);
            capacitorAdLayout.setTag("emi_banner_preload_layout");
            capacitorAdLayout.setBackgroundColor(Color.TRANSPARENT);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.addView(capacitorAdLayout, layoutParams);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                capacitorAdLayout.setOnApplyWindowInsetsListener((v, insets) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        android.graphics.Insets sysInsets = insets.getInsets(android.view.WindowInsets.Type.systemBars());
                        systemSafeTop = sysInsets.top;
                        systemSafeBottom = sysInsets.bottom;
                    } else {
                        systemSafeTop = insets.getSystemWindowInsetTop();
                        systemSafeBottom = insets.getSystemWindowInsetBottom();
                    }
                    if (isBannerVisible) {
                        updateBannerLayout();
                        updateWebViewMargins();
                    }
                    return insets;
                });
                capacitorAdLayout.requestApplyInsets();
            }
        }
    }

    private void updateBannerLayout() {
        if (adView == null || capacitorAdLayout == null) return;

        FrameLayout.LayoutParams bannerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        if ("TOP".equals(currentPosition)) {
            bannerParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            bannerParams.setMargins(0, systemSafeTop, 0, 0);
        } else {
            bannerParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            bannerParams.setMargins(0, 0, 0, systemSafeBottom);
        }

        adView.setLayoutParams(bannerParams);
        capacitorAdLayout.requestLayout();
    }

    private void updateWebViewMargins() {
        View webViewView = plugin.getBridge().getWebView();
        if (webViewView == null) return;

        ViewGroup.LayoutParams lp = webViewView.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) lp;

            int screenHeightInPx = getScreenHeightInPx();

            if (!isBannerVisible || isOverlapping) {
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                params.topMargin = 0;
                params.bottomMargin = 0;
            } else {
                if ("TOP".equals(currentPosition)) {
                    params.topMargin = lastAdHeight;
                    params.bottomMargin = 0;
                    params.height = screenHeightInPx;
                } else {
                    int finalBottom = lastAdHeight;
                    params.topMargin = 0;
                    params.bottomMargin = 0;
                    params.height = screenHeightInPx - finalBottom;
                }
            }

            webViewView.setLayoutParams(params);
            webViewView.requestLayout();
        }
    }

    public void hidePreloadedBanner(PluginCall call) {
        plugin.getActivity().runOnUiThread(() -> {
            isBannerVisible = false;
            if (adView != null) adView.setVisibility(View.GONE);
            if (capacitorAdLayout != null) capacitorAdLayout.setVisibility(View.GONE);
            updateWebViewMargins();
            if (call != null) call.resolve();
        });
    }

    public void destroyPreloadedBanner(PluginCall call) {
        plugin.getActivity().runOnUiThread(() -> {
            isBannerVisible = false;
            updateWebViewMargins();
            destroyCurrentAdInternal();
            if (call != null) call.resolve();
        });
    }

    private void destroyCurrentAdInternal() {
        if (adView != null) {
            if (adView.getParent() != null) {
                ((ViewGroup) adView.getParent()).removeView(adView);
            }
            adView = null;
        }
        if (currentBannerAd != null) {
            currentBannerAd.destroy();
            currentBannerAd = null;
        }
        if (capacitorAdLayout != null) {
            if (capacitorAdLayout.getParent() != null) {
                ((ViewGroup) capacitorAdLayout.getParent()).removeView(capacitorAdLayout);
            }
            capacitorAdLayout = null;
        }
    }

    public void checkAvailability(PluginCall call) {
        String adUnitId = call.getString("adUnitId", currentAdUnitId);
        boolean isAvailable = BannerAdPreloader.isAdAvailable(adUnitId);
        JSObject ret = new JSObject();
        ret.put("isAvailable", isAvailable);
        call.resolve(ret);
    }

    private void setupBannerCallbacks(BannerAd bannerAd) {
        bannerAd.setAdEventCallback(new BannerAdEventCallback() {
            @Override public void onAdImpression() { plugin.notifyListeners("onBannerAdImpression", new JSObject()); }
            @Override public void onAdClicked() { plugin.notifyListeners("onBannerAdClicked", new JSObject()); }
            @Override public void onAdShowedFullScreenContent() { plugin.notifyListeners("onBannerAdShowedFullScreen", new JSObject()); }
            @Override public void onAdDismissedFullScreenContent() { plugin.notifyListeners("onBannerAdDismissedFullScreen", new JSObject()); }
            @Override public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {
                JSObject ret = new JSObject();
                ret.put("error", error.getMessage());
                plugin.notifyListeners("onBannerAdFailedToShowFullScreen", ret);
            }
            @Override public void onAdPaid(@NonNull AdValue value) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", currentAdUnitId);
                ret.put("valueMicros", value.getValueMicros());
                plugin.notifyListeners("onBannerAdPaid", ret);
            }
        });
        bannerAd.setBannerAdRefreshCallback(new BannerAdRefreshCallback() {
            @Override public void onAdRefreshed() { plugin.notifyListeners("onBannerAdRefreshed", new JSObject()); }
            @Override public void onAdFailedToRefresh(@NonNull LoadAdError error) {
                JSObject ret = new JSObject();
                ret.put("error", error.getMessage());
                plugin.notifyListeners("onBannerAdFailedToRefresh", ret);
            }
        });
    }

    private int getScreenHeightInPx() {
        Activity activity = plugin.getActivity();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.view.WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            return windowMetrics.getBounds().height();
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.heightPixels;
        }
    }

    private int getAdWidth(Activity activity) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.view.WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            android.graphics.Insets insets = windowMetrics.getWindowInsets().getInsetsIgnoringVisibility(
                    android.view.WindowInsets.Type.systemBars() | android.view.WindowInsets.Type.displayCutout()
            );
            int widthPixels = windowMetrics.getBounds().width() - insets.left - insets.right;
            return (int) (widthPixels / displayMetrics.density);
        } else {
            return (int) (displayMetrics.widthPixels / displayMetrics.density);
        }
    }

    private AdSize getAdSize(Activity activity, String sizeStr) {
        if ("BANNER".equalsIgnoreCase(sizeStr)) return AdSize.BANNER;
        if ("LARGE_BANNER".equalsIgnoreCase(sizeStr)) return AdSize.LARGE_BANNER;
        if ("MEDIUM_RECTANGLE".equalsIgnoreCase(sizeStr)) return AdSize.MEDIUM_RECTANGLE;
        if ("FULL_BANNER".equalsIgnoreCase(sizeStr)) return AdSize.FULL_BANNER;
        if ("LEADERBOARD".equalsIgnoreCase(sizeStr)) return AdSize.LEADERBOARD;
        if ("LARGE_LANDSCAPE_ANCHORED_ADAPTIVE".equalsIgnoreCase(sizeStr)) return AdSize.getLargeLandscapeAnchoredAdaptiveBannerAdSize(activity, getAdWidth(activity));
        if ("LARGE_PORTRAIT_ANCHORED_ADAPTIVE".equalsIgnoreCase(sizeStr)) return AdSize.getLargePortraitAnchoredAdaptiveBannerAdSize(activity, getAdWidth(activity));
        if ("CURRENT_ORIENTATION_INLINE_ADAPTIVE".equalsIgnoreCase(sizeStr)) return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(activity, getAdWidth(activity));
        if ("LARGE_ANCHORED_ADAPTIVE".equalsIgnoreCase(sizeStr)) return AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, getAdWidth(activity));
        if ("PORTRAIT_INLINE_ADAPTIVE".equalsIgnoreCase(sizeStr)) return AdSize.getPortraitInlineAdaptiveBannerAdSize(activity, getAdWidth(activity));
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, getAdWidth(activity));
    }
}
