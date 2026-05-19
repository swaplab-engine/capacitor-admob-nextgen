package com.emi.plugins.admob.banner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.emi.plugins.admob.AdMobNextGenPlugin;

import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.banner.AdView;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd;
import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;

public class BannerExecutor {

    private final AdMobNextGenPlugin plugin;

    private AdView adView;
    private FrameLayout capacitorAdLayout;

    private String currentAdUnitId = "";
    private String currentPosition = "BOTTOM";

    private boolean isBannerVisible = false;
    private boolean isLoading = false;
    private boolean isOverlapping = true;
    private boolean isAutoShow = true;
    private boolean isCollapsible = false;

    private int lastAdHeight = 0;
    private int systemSafeTop = 0;
    private int systemSafeBottom = 0;

    private String lastSizeStr = "";
    private AdSize lastAdSize = null;
    private String lastPosition = "BOTTOM";
    private long lastLoadTime = 0;

    public BannerExecutor(AdMobNextGenPlugin plugin) {
        this.plugin = plugin;
    }

    public void createBanner(final PluginCall call) {
        String adUnitId = call.getString("adUnitId");
        if (adUnitId == null || adUnitId.isEmpty()) {
            call.reject("Ad Unit ID is required", "ERR_MISSING_ID");
            return;
        }

        Activity activity = plugin.getActivity();

        activity.runOnUiThread(() -> {
            if (isLoading) {
                call.reject("A banner is already loading.");
                return;
            }

            Boolean autoShowOpt = call.getBoolean("isAutoShow", true);
            boolean requestAutoShow = (autoShowOpt != null) ? autoShowOpt : true;

            String posOpt = call.getString("position", "BOTTOM");
            String requestPosition = (posOpt != null) ? posOpt.toUpperCase() : "BOTTOM";

            Boolean overlapOpt = call.getBoolean("isOverlap", true);
            this.isOverlapping = (overlapOpt != null) ? overlapOpt : true;

            Boolean collapsibleOpt = call.getBoolean("isCollapsible", false);
            this.isCollapsible = (collapsibleOpt != null) ? collapsibleOpt : false;

            String requestSizeStr = call.getString("adSize", "ADAPTIVE");
            if (requestSizeStr == null) requestSizeStr = "ADAPTIVE";

            Double retryOpt = call.getDouble("retryInterval");
            long minLoadInterval = (retryOpt != null) ? retryOpt.longValue() : 5000L;

            long currentTime = System.currentTimeMillis();

            boolean isSameId = currentAdUnitId.equals(adUnitId);
            boolean isSameSize = lastSizeStr.equals(requestSizeStr);
            boolean isSamePos = lastPosition.equals(requestPosition);

            if (adView != null && isSameId && isSameSize) {
                this.currentPosition = requestPosition;
                this.isAutoShow = requestAutoShow;

                if (this.isAutoShow) {
                    if (!isBannerVisible) {
                        showBanner(null);
                        JSObject ret = new JSObject(); ret.put("message", "Banner Shown (Cached)");
                        call.resolve(ret);
                    } else {
                        if (!isSamePos) {
                            updateBannerLayout();
                            updateWebViewMargins();
                            JSObject ret = new JSObject(); ret.put("message", "Banner Repositioned");
                            call.resolve(ret);
                        } else {
                            JSObject ret = new JSObject(); ret.put("message", "Banner Already Visible");
                            call.resolve(ret);
                        }
                    }
                } else {
                    hideBanner(null);
                    JSObject ret = new JSObject(); ret.put("message", "Banner Hidden (Cached)");
                    call.resolve(ret);
                }

                this.lastPosition = this.currentPosition;
                return; 
            }

            if ((currentTime - lastLoadTime) < minLoadInterval) {
                call.reject("Request too fast. Please wait " + minLoadInterval + "ms to prevent invalid traffic.");
                return;
            }

            this.currentAdUnitId = adUnitId;
            this.currentPosition = requestPosition;
            this.lastPosition = requestPosition;
            this.isAutoShow = requestAutoShow;
            this.lastSizeStr = requestSizeStr;
            this.lastLoadTime = currentTime; 

            isLoading = true;

            if (capacitorAdLayout == null) {
                capacitorAdLayout = new FrameLayout(activity);
                capacitorAdLayout.setTag("emi_banner_layout");
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

            AdView pendingAdView = new AdView(activity);

            AdSize adSize = getAdSize(activity, requestSizeStr);

            FrameLayout.LayoutParams hiddenParams = new FrameLayout.LayoutParams(1, 1);
            capacitorAdLayout.addView(pendingAdView, hiddenParams);
            pendingAdView.setVisibility(View.INVISIBLE);

            BannerAdRequest.Builder builder = new BannerAdRequest.Builder(adUnitId, adSize);
            if (isCollapsible) {
                Bundle extras = new Bundle();
                extras.putString("collapsible", currentPosition.toLowerCase());
                builder.setGoogleExtrasBundle(extras);
            }

            pendingAdView.loadAd(builder.build(), new AdLoadCallback<BannerAd>() {
                @Override
                public void onAdLoaded(@NonNull BannerAd bannerAd) {
                    activity.runOnUiThread(() -> {
                        isLoading = false;

                        if (adView != null) {
                            if (adView.getParent() != null) {
                                ((ViewGroup) adView.getParent()).removeView(adView);
                            }
                            adView.destroy();
                        }

                        adView = pendingAdView;
                        lastAdHeight = adSize.getHeightInPixels(activity);

                        setupBannerCallbacks(bannerAd);

                        if (isAutoShow) {
                            isBannerVisible = true;
                            updateBannerLayout();
                            updateWebViewMargins();
                            adView.setVisibility(View.VISIBLE);
                            capacitorAdLayout.bringToFront();
                        } else {
                            isBannerVisible = false;
                            adView.setVisibility(View.GONE);
                        }

                        JSObject ret = new JSObject();
                        ret.put("adUnitId", adUnitId);
                        ret.put("width", adSize.getWidth());
                        ret.put("height", adSize.getHeight());
                        ret.put("widthPixels", adSize.getWidthInPixels(activity));
                        ret.put("heightPixels", adSize.getHeightInPixels(activity));
                        ret.put("isCollapsible", bannerAd.isCollapsible());

                        plugin.notifyPluginListeners("onBannerAdLoaded", ret);
                        call.resolve();
                    });
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                    activity.runOnUiThread(() -> {
                        isLoading = false;
                        if (pendingAdView.getParent() != null) {
                            ((ViewGroup) pendingAdView.getParent()).removeView(pendingAdView);
                        }
                        pendingAdView.destroy();

                        JSObject ret = new JSObject();
                        ret.put("error", adError.getMessage());
                        plugin.notifyPluginListeners("onBannerAdFailedToLoad", ret);
                        call.reject("Banner failed to load: " + adError.getMessage());
                    });
                }
            });
        });
    }

    private void updateBannerLayout() {

        if (Build.VERSION.SDK_INT < 35) {
            updateBannerLayoutLegacy();
            return;
        }

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

    private void updateBannerLayoutLegacy() {

        if (adView == null || capacitorAdLayout == null) return;

        FrameLayout.LayoutParams bannerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        Activity activity = plugin.getActivity();
        int topMargin = 0;
        int bottomMargin = 0;

        if (activity != null && activity.getWindow() != null) {
            View decorView = activity.getWindow().getDecorView();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                android.view.WindowInsets insets = decorView.getRootWindowInsets();
                if (insets != null) {
                    boolean isStatusVisible = insets.isVisible(android.view.WindowInsets.Type.statusBars());
                    boolean isNavVisible = insets.isVisible(android.view.WindowInsets.Type.navigationBars());

                    topMargin = isStatusVisible ? insets.getInsets(android.view.WindowInsets.Type.statusBars()).top : 0;
                    bottomMargin = isNavVisible ? insets.getInsets(android.view.WindowInsets.Type.navigationBars()).bottom : 0;
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                android.view.WindowInsets insets = decorView.getRootWindowInsets();
                if (insets != null) {
                    topMargin = insets.getSystemWindowInsetTop();
                    bottomMargin = insets.getSystemWindowInsetBottom();

                    int uiOptions = decorView.getSystemUiVisibility();
                    if ((uiOptions & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0) topMargin = 0;
                    if ((uiOptions & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0) bottomMargin = 0;
                }
            } else {

                int uiOptions = decorView.getSystemUiVisibility();
                if ((uiOptions & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) topMargin = getRealStatusBarHeight();
                if ((uiOptions & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) bottomMargin = getRealNavigationBarHeight();
            }
        }

        if ("TOP".equals(currentPosition)) {
            bannerParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            bannerParams.setMargins(0, topMargin, 0, 0);
        } else {
            bannerParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            bannerParams.setMargins(0, 0, 0, bottomMargin);
        }

        adView.setLayoutParams(bannerParams);
        capacitorAdLayout.requestLayout();
    }

    private void updateWebViewMarginsLegacy() {

        View webViewView = plugin.getBridge().getWebView();
        if (webViewView == null) return;

        ViewGroup.LayoutParams lp = webViewView.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) lp;

            if (!isBannerVisible || isOverlapping) {
                params.topMargin = 0;
                params.bottomMargin = 0;
            } else {

                if ("TOP".equals(currentPosition)) {
                    params.topMargin = lastAdHeight; 
                    params.bottomMargin = 0;
                } else {
                    params.topMargin = 0;
                    params.bottomMargin = lastAdHeight; 
                }
            }

            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            webViewView.setLayoutParams(params);
            webViewView.requestLayout();
        }
    }

    private void updateWebViewMargins() {

        if (Build.VERSION.SDK_INT < 35) {
            updateWebViewMarginsLegacy();
            return;
        }

        View webViewView = plugin.getBridge().getWebView();
        if (webViewView == null) return;

        ViewGroup.LayoutParams lp = webViewView.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) lp;

            if (!isBannerVisible || isOverlapping) {
                params.topMargin = 0;
                params.bottomMargin = 0;
            } else {
                if ("TOP".equals(currentPosition)) {
                    params.topMargin = lastAdHeight;
                    params.bottomMargin = 0;
                } else {
                    params.topMargin = 0;
                    params.bottomMargin = lastAdHeight;
                }
            }

            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            webViewView.setLayoutParams(params);
            webViewView.requestLayout();
        }
    }

    public void showBanner(final PluginCall call) {
        plugin.getActivity().runOnUiThread(() -> {
            if (adView == null) {
                if (call != null) call.reject("Banner not created yet.");
                return;
            }
            isBannerVisible = true;
            updateBannerLayout();
            updateWebViewMargins();
            adView.setVisibility(View.VISIBLE);
            if (capacitorAdLayout != null) {
                capacitorAdLayout.setVisibility(View.VISIBLE);
                capacitorAdLayout.bringToFront();
            }
            if (call != null) call.resolve();
        });
    }

    public void hideBanner(final PluginCall call) {
        plugin.getActivity().runOnUiThread(() -> {
            isBannerVisible = false;
            if (adView != null) adView.setVisibility(View.GONE);
            if (capacitorAdLayout != null) capacitorAdLayout.setVisibility(View.GONE);
            updateWebViewMargins();
            if (call != null) call.resolve();
        });
    }

    public void destroyBanner(final PluginCall call) {
        plugin.getActivity().runOnUiThread(() -> {
            isBannerVisible = false;
            updateWebViewMargins();

            if (adView != null) {
                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }
                adView.destroy();
                adView = null;
            }

            if (capacitorAdLayout != null) {
                if (capacitorAdLayout.getParent() != null) {
                    ((ViewGroup) capacitorAdLayout.getParent()).removeView(capacitorAdLayout);
                }
                capacitorAdLayout = null;
            }

            if (call != null) call.resolve();
        });
    }

    private void setupBannerCallbacks(BannerAd bannerAd) {
        bannerAd.setAdEventCallback(new BannerAdEventCallback() {
            @Override public void onAdImpression() {
                plugin.notifyPluginListeners("onBannerAdImpression", new JSObject());
            }
            @Override public void onAdClicked() {
                plugin.notifyPluginListeners("onBannerAdClicked", new JSObject());
            }
            @Override public void onAdShowedFullScreenContent() {
                plugin.notifyPluginListeners("onBannerAdShowedFullScreen", new JSObject());
            }
            @Override public void onAdDismissedFullScreenContent() {
                plugin.notifyPluginListeners("onBannerAdDismissedFullScreen", new JSObject());
            }
            @Override public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {
                JSObject ret = new JSObject();
                ret.put("error", error.getMessage());
                plugin.notifyPluginListeners("onBannerAdFailedToShowFullScreen", ret);
            }
            @Override public void onAdPaid(@NonNull AdValue value) {
                JSObject ret = new JSObject();
                ret.put("adUnitId", currentAdUnitId);
                ret.put("valueMicros", value.getValueMicros());
                ret.put("currencyCode", value.getCurrencyCode());
                ret.put("precisionType", value.getPrecisionType().name());
                plugin.notifyPluginListeners("onBannerAdPaid", ret);
            }
        });
        bannerAd.setBannerAdRefreshCallback(new BannerAdRefreshCallback() {
            @Override public void onAdRefreshed() {
                plugin.notifyPluginListeners("onBannerAdRefreshed", new JSObject());
            }
            @Override public void onAdFailedToRefresh(@NonNull LoadAdError error) {
                JSObject ret = new JSObject();
                ret.put("error", error.getMessage());
                plugin.notifyPluginListeners("onBannerAdFailedToRefresh", ret);
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

    @SuppressLint({"DiscouragedApi", "InternalInsetResource"})
    private int getRealNavigationBarHeight() {
        Activity activity = plugin.getActivity();
        if (activity == null) return 0;
        int resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return activity.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    @SuppressLint({"DiscouragedApi", "InternalInsetResource"})
    private int getRealStatusBarHeight() {
        Activity activity = plugin.getActivity();
        if (activity == null) return 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return activity.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
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

    public void onPause() {

    }

    public void onResume() {

    }

    public void onDestroy() {
        if (adView != null) {
            plugin.getActivity().runOnUiThread(() -> {
                adView.destroy();
                adView = null;
            });
        }
        if (capacitorAdLayout != null) {
            plugin.getActivity().runOnUiThread(() -> {
                if (capacitorAdLayout.getParent() != null) {
                    ((ViewGroup) capacitorAdLayout.getParent()).removeView(capacitorAdLayout);
                }
                capacitorAdLayout = null;
            });
        }
    }

}
