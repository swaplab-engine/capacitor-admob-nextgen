package com.emi.plugins.admob.nativead;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.emi.plugins.admob.AdMobNextGenPlugin;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import com.google.android.libraries.ads.mobile.sdk.common.AdValue;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd.NativeAdType;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView;

import java.util.Collections;

public class NativeExecutor {
    private static final String TAG = "AdMobNextGenNative";

    private final AdMobNextGenPlugin plugin;
    private FrameLayout adContainer;
    private NativeAd currentNativeAd;
    private ViewTreeObserver.OnScrollChangedListener scrollListener;
    private long lastShowTime = 0;

    private float adAbsoluteX = 0;
    private float adAbsoluteY = 0;

    public NativeExecutor(AdMobNextGenPlugin plugin) {
        this.plugin = plugin;
    }

    public void showNativeAd(PluginCall call) {
        if (!plugin.isInitialized()) {
            call.reject("Google Mobile Ads SDK has not been initialized. Please call initialize() first.");
            return;
        }

        final Activity activity = plugin.getActivity();
        if (activity == null) {
            call.reject("Activity is null");
            return;
        }

        String adUnitId = call.getString("adUnitId", "");
        if (adUnitId.trim().isEmpty()) {
            call.reject("adUnitId is required");
            return;
        }

        Double retryOpt = call.getDouble("retryInterval");
        long minLoadInterval = (retryOpt != null) ? retryOpt.longValue() : 5000L;
        long currentTime = System.currentTimeMillis();

        if ((currentTime - lastShowTime) < minLoadInterval) {
            call.reject("Request too fast. Please wait " + minLoadInterval + "ms to prevent invalid traffic.");
            return;
        }
        lastShowTime = currentTime;

        String templateName = call.getString("template", "small");

        Double xVal = call.getDouble("x", 0.0);
        Double yVal = call.getDouble("y", 0.0);
        Double wVal = call.getDouble("width", (double) ViewGroup.LayoutParams.MATCH_PARENT);

        assert wVal != null;
        int width = wVal.intValue();

        assert xVal != null;
        adAbsoluteX = dpToPx(xVal.intValue());
        assert yVal != null;
        adAbsoluteY = dpToPx(yVal.intValue());

        activity.runOnUiThread(() -> {
            NativeAdRequest adRequest = new NativeAdRequest.Builder(adUnitId, Collections.singletonList(NativeAdType.NATIVE)).build();

            NativeAdLoaderCallback callback = new NativeAdLoaderCallback() {
                @Override
                public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                    activity.runOnUiThread(() -> {
                        nativeAd.setAdEventCallback(new NativeAdEventCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() { fireEvent("onNativeAdShowed", null); }

                            @Override
                            public void onAdDismissedFullScreenContent() { fireEvent("onNativeAdDismissed", null); }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull FullScreenContentError error) {
                                JSObject data = new JSObject();
                                data.put("message", error.getMessage());
                                fireEvent("onNativeAdFailedToShow", data);
                            }

                            @Override
                            public void onAdImpression() { fireEvent("onNativeAdImpression", null); }

                            @Override
                            public void onAdClicked() { fireEvent("onNativeAdClicked", null); }

                            @Override
                            public void onAdPaid(@NonNull AdValue adValue) {
                                JSObject data = new JSObject();
                                data.put("value", adValue.getValueMicros());
                                data.put("currencyCode", adValue.getCurrencyCode());
                                data.put("precisionType", adValue.getPrecisionType().name());
                                fireEvent("onNativeAdPaid", data);
                            }
                        });

                        FrameLayout pendingContainer = buildAdContainer(activity, nativeAd, templateName, width, call);

                        if (pendingContainer == null) {
                            return;
                        }

                        if (adContainer != null && adContainer.getParent() != null) {
                            ((ViewGroup) adContainer.getParent()).removeView(adContainer);
                        }

                        if (currentNativeAd != null) {
                            currentNativeAd.destroy();
                        }

                        currentNativeAd = nativeAd;
                        adContainer = pendingContainer;

                        View webView = plugin.getBridge().getWebView();
                        ViewGroup webViewContainer = (ViewGroup) webView.getParent();

                        boolean isMedium = "medium".equalsIgnoreCase(templateName);
                        int adHeight = isMedium ? dpToPx(350) : ViewGroup.LayoutParams.WRAP_CONTENT;

                        ViewGroup.LayoutParams containerParams = new ViewGroup.LayoutParams(
                                width > 0 ? dpToPx(width) : ViewGroup.LayoutParams.MATCH_PARENT,
                                adHeight
                        );

                        webViewContainer.addView(adContainer, containerParams);

                        updateAdPosition();

                        setupGlobalLayoutListener(call);
                        syncScrollObserver();
                    });
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                    activity.runOnUiThread(() -> {
                        JSObject data = new JSObject();
                        data.put("message", adError.getMessage());
                        fireEvent("onNativeAdFailedToLoad", data);
                        call.reject(adError.getMessage());
                    });
                }
            };
            NativeAdLoader.load(adRequest, callback);
        });
    }

    private void updateAdPosition() {
        View webView = plugin.getBridge().getWebView();
        if (adContainer != null && webView != null) {

            float screenX = adAbsoluteX - webView.getScrollX();
            float screenY = adAbsoluteY - webView.getScrollY();

            adContainer.setX(screenX);
            adContainer.setY(screenY);
        }
    }

    private void syncScrollObserver() {
        View webView = plugin.getBridge().getWebView();
        if (webView == null) return;

        if (scrollListener == null) {
            scrollListener = () -> {

                updateAdPosition();
            };
            webView.getViewTreeObserver().addOnScrollChangedListener(scrollListener);
        }

        updateAdPosition();
    }

    private FrameLayout buildAdContainer(Activity activity, NativeAd nativeAd, String templateName, int width, PluginCall call) {
        FrameLayout newContainer = new FrameLayout(activity);

        int layoutId = "medium".equalsIgnoreCase(templateName) ?
                getResourceId("gnt_medium_template_view", "layout") :
                getResourceId("gnt_small_template_view", "layout");

        if (layoutId == 0) {
            String errorMsg = "Native Ads layout not found! Please set 'enableNativeAds': true in your package.json and sync.";
            Log.e(TAG, errorMsg);
            if (call != null) {
                call.reject(errorMsg);
            }
            return null;
        }

        NativeAdView adView = (NativeAdView) activity.getLayoutInflater().inflate(layoutId, null);

        adView.setHeadlineView(adView.findViewById(id("primary")));
        adView.setBodyView(adView.findViewById(id("secondary")));
        adView.setCallToActionView(adView.findViewById(id("cta")));
        adView.setIconView(adView.findViewById(id("icon")));
        adView.setStarRatingView(adView.findViewById(id("rating_bar")));
        MediaView mediaView = adView.findViewById(id("media_view"));

        if (adView.getHeadlineView() != null) ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        if (adView.getBodyView() != null) {
            adView.getBodyView().setVisibility(nativeAd.getBody() == null ? View.INVISIBLE : View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (adView.getCallToActionView() != null) {
            adView.getCallToActionView().setVisibility(nativeAd.getCallToAction() == null ? View.INVISIBLE : View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        if (adView.getIconView() != null) {
            if (nativeAd.getIcon() == null || nativeAd.getIcon().getDrawable() == null) {
                adView.getIconView().setVisibility(View.GONE);
            } else {
                ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
                adView.getIconView().setVisibility(View.VISIBLE);
            }
        }
        if (adView.getStarRatingView() != null) {
            if (nativeAd.getStarRating() == null) {
                adView.getStarRatingView().setVisibility(View.INVISIBLE);
            } else {
                ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
                adView.getStarRatingView().setVisibility(View.VISIBLE);
            }
        }

        adView.registerNativeAd(nativeAd, mediaView);

        boolean isMedium = "medium".equalsIgnoreCase(templateName);
        int adHeight = isMedium ? dpToPx(350) : ViewGroup.LayoutParams.WRAP_CONTENT;

        FrameLayout.LayoutParams adViewParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                adHeight
        );
        newContainer.addView(adView, adViewParams);

        return newContainer;
    }

    private void setupGlobalLayoutListener(PluginCall call) {
        if (adContainer == null) return;

        adContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                adContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int exactHeightPx = adContainer.getHeight();
                int exactWidthPx = adContainer.getWidth();

                try {
                    JSObject result = new JSObject();
                    result.put("width", pxToDp(exactWidthPx));
                    result.put("height", pxToDp(exactHeightPx));

                    fireEvent("onNativeAdLoaded", result);
                    call.resolve(result);
                } catch (Exception e) {
                    call.resolve();
                }
            }
        });
    }

    public void hideNativeAd(PluginCall call) {
        final Activity activity = plugin.getActivity();
        if (activity == null) {
            call.reject("Activity is null");
            return;
        }

        activity.runOnUiThread(() -> {
            View webView = plugin.getBridge().getWebView();

            if (scrollListener != null && webView != null) {
                webView.getViewTreeObserver().removeOnScrollChangedListener(scrollListener);
                scrollListener = null;
            }

            if (adContainer != null) {
                adContainer.setVisibility(View.GONE);
                if (adContainer.getParent() != null) {
                    ((ViewGroup) adContainer.getParent()).removeView(adContainer);
                }
                adContainer = null;
            }

            if (currentNativeAd != null) {
                currentNativeAd.destroy();
                currentNativeAd = null;
            }

            call.resolve();
        });
    }

    private int getResourceId(String name, String defType) {
        return plugin.getContext().getResources().getIdentifier(name, defType, plugin.getContext().getPackageName());
    }

    private int id(String name) {
        return getResourceId(name, "id");
    }

    private int dpToPx(int dp) {
        float density = plugin.getContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private int pxToDp(int px) {
        float density = plugin.getContext().getResources().getDisplayMetrics().density;
        return Math.round((float) px / density);
    }

    public void onDestroy() {
        final Activity activity = plugin.getActivity();
        if (activity == null) return;

        activity.runOnUiThread(() -> {
            View webView = plugin.getBridge().getWebView();
            if (scrollListener != null && webView != null) {
                webView.getViewTreeObserver().removeOnScrollChangedListener(scrollListener);
                scrollListener = null;
            }

            if (adContainer != null) {
                if (adContainer.getParent() != null) {
                    ((ViewGroup) adContainer.getParent()).removeView(adContainer);
                }
                adContainer.removeAllViews();
                adContainer = null;
            }

            if (currentNativeAd != null) {
                currentNativeAd.destroy();
                currentNativeAd = null;
            }
        });
    }

    private void fireEvent(String eventName, JSObject data) {
        if (data == null) {
            data = new JSObject();
        }
        plugin.notifyPluginListeners(eventName, data);
    }
}
