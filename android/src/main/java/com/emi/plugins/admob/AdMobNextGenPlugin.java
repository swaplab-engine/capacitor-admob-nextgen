package com.emi.plugins.admob;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.emi.plugins.admob.appopen.AppOpenExecutor;
import com.emi.plugins.admob.banner.BannerExecutor;
import com.emi.plugins.admob.interstitial.InterstitialExecutor;
import com.emi.plugins.admob.preloading.AppOpenPreloadExecutor;
import com.emi.plugins.admob.preloading.BannerPreloadExecutor;
import com.emi.plugins.admob.preloading.InterstitialPreloadExecutor;
import com.emi.plugins.admob.preloading.RewardedInterstitialPreloadExecutor;
import com.emi.plugins.admob.preloading.RewardedPreloadExecutor;
import com.emi.plugins.admob.rewarded.RewardedExecutor;
import com.emi.plugins.admob.rewardedinterstitial.RewardedInterstitialExecutor;
import com.emi.plugins.admob.ump.ConsentExecutor;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "AdMobNextGen")
public class AdMobNextGenPlugin extends Plugin {

    private AdMobNextGen coreImplementation;
    private ConsentExecutor consentExecutor;
    private BannerExecutor bannerExecutor;
    private AppOpenExecutor appOpenExecutor;
    private InterstitialExecutor interstitialExecutor;
    private RewardedExecutor rewardedExecutor;
    private RewardedInterstitialExecutor rewardedInterstitialExecutor;

    private BannerPreloadExecutor bannerPreloadExecutor;
    private AppOpenPreloadExecutor appOpenPreloadExecutor;
    private InterstitialPreloadExecutor interstitialPreloadExecutor;
    private RewardedPreloadExecutor rewardedPreloadExecutor;
    private RewardedInterstitialPreloadExecutor rewardedInterstitialPreloadExecutor;

    @Override
    public void load() {
        super.load();

        coreImplementation = new AdMobNextGen();
        consentExecutor = new ConsentExecutor(this);
        bannerExecutor = new BannerExecutor(this);
        appOpenExecutor = new AppOpenExecutor(this);
        interstitialExecutor = new InterstitialExecutor(this);
        rewardedExecutor = new RewardedExecutor(this);
        rewardedInterstitialExecutor = new RewardedInterstitialExecutor(this);

        bannerPreloadExecutor = new BannerPreloadExecutor(this);
        appOpenPreloadExecutor = new AppOpenPreloadExecutor(this);
        interstitialPreloadExecutor = new InterstitialPreloadExecutor(this);
        rewardedPreloadExecutor = new RewardedPreloadExecutor(this);
        rewardedInterstitialPreloadExecutor = new RewardedInterstitialPreloadExecutor(this);

        if (getActivity() != null) {
            applyAdMobAPI35WorkaroundIfNeeded(getActivity().getApplication());
        }
    }

    private static void applyAdMobAPI35WorkaroundIfNeeded(Application application) {
        if (Build.VERSION.SDK_INT < 35) {
            return;
        }

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {}

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                applyAPI35WorkaroundToActivity(activity);
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                applyAPI35WorkaroundToActivity(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {}

            @Override
            public void onActivityStopped(@NonNull Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {}
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private static void applyAPI35WorkaroundToActivity(Activity activity) {

        if (!"com.google.android.gms.ads.AdActivity".equals(activity.getClass().getName())) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = activity.getWindow().getInsetsController();
            if (controller != null) {

                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_DEFAULT);
            }
        }
    }

    private String getAppIdFromManifest() {
        try {
            Context context = getContext();
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (ai == null || ai.metaData == null) return null;
            return ai.metaData.getString("com.google.android.gms.ads.APPLICATION_ID");
        } catch (Exception e) {
            return null;
        }
    }

    @PluginMethod
    public void initialize(PluginCall call) {
        String appId = getAppIdFromManifest();

        if (appId == null || appId.isEmpty()) {
            call.reject("Application ID is missing. You MUST add it to your AndroidManifest.xml (or use the package.json hook).");
            return;
        }

        String maxAdContentRating = call.getString("maxAdContentRating", "");
        Boolean tagForChildDirectedTreatment = call.hasOption("tagForChildDirectedTreatment") ? call.getBoolean("tagForChildDirectedTreatment") : null;
        Boolean tagForUnderAgeOfConsent = call.hasOption("tagForUnderAgeOfConsent") ? call.getBoolean("tagForUnderAgeOfConsent") : null;
        Boolean isTesting = call.getBoolean("isTesting", false);

        coreImplementation.initializeSDK(
                getContext(),
                appId,
                maxAdContentRating,
                tagForChildDirectedTreatment,
                tagForUnderAgeOfConsent,
                isTesting,
                () -> {
                    JSObject result = new JSObject();
                    result.put("status", "INITIALIZED_SUCCESSFULLY");
                    call.resolve(result);
                }
        );
    }

    @PluginMethod
    public void requestConsentInfo(PluginCall call) {
        String manifestAppId = getAppIdFromManifest();
        if (manifestAppId == null || manifestAppId.isEmpty()) {
            call.reject("CRITICAL ERROR: UMP requires 'com.google.android.gms.ads.APPLICATION_ID' in AndroidManifest.xml. Please add it before calling requestConsentInfo.");
            return;
        }
        consentExecutor.requestConsentInfo(call);
    }

    @PluginMethod
    public void showPrivacyOptionsForm(PluginCall call) {
        consentExecutor.showPrivacyOptionsForm(call);
    }

    @PluginMethod
    public void getTCData(PluginCall call) {
        consentExecutor.getTCData(call);
    }

    @PluginMethod
    public void createBanner(PluginCall call) {
        bannerExecutor.createBanner(call);
    }

    @PluginMethod
    public void showBanner(PluginCall call) {
        bannerExecutor.showBanner(call);
    }

    @PluginMethod
    public void hideBanner(PluginCall call) {
        bannerExecutor.hideBanner(call);
    }

    @PluginMethod
    public void destroyBanner(PluginCall call) {
        bannerExecutor.destroyBanner(call);
    }

    @PluginMethod
    public void loadInterstitial(PluginCall call) {
        interstitialExecutor.load(getActivity(), call, new ActionCallback() {
            @Override
            public void onSuccess() {
                call.resolve();
            }
            @Override
            public void onError(String error) {
                call.reject("Failed: " + error);
            }
        });
    }

    @PluginMethod
    public void showInterstitial(PluginCall call) {
        interstitialExecutor.show(getActivity(), new ActionCallback() {
            @Override
            public void onSuccess() {
                call.resolve();
            }

            @Override
            public void onError(String error) {
                call.reject(error);
            }
        });
    }

    @PluginMethod
    public void loadRewarded(PluginCall call) {
        rewardedExecutor.load(getActivity(), call, new ActionCallback() {
            @Override
            public void onSuccess() {
                call.resolve();
            }

            @Override
            public void onError(String error) {
                call.reject("Failed to load rewarded ad: " + error);
            }
        });
    }

    @PluginMethod
    public void showRewarded(PluginCall call) {
        rewardedExecutor.show(getActivity(), new ActionCallback() {
            @Override
            public void onSuccess() {
                call.resolve();
            }

            @Override
            public void onError(String error) {
                call.reject(error);
            }
        });
    }

    @PluginMethod
    public void loadRewardedInterstitial(PluginCall call) {
        rewardedInterstitialExecutor.load(getActivity(), call, new ActionCallback() {
            @Override
            public void onSuccess() {
                call.resolve();
            }

            @Override
            public void onError(String error) {
                call.reject("Failed to load rewarded interstitial ad: " + error);
            }
        });
    }

    @PluginMethod
    public void showRewardedInterstitial(PluginCall call) {
        rewardedInterstitialExecutor.show(getActivity(), new ActionCallback() {
            @Override
            public void onSuccess() {
                call.resolve();
            }

            @Override
            public void onError(String error) {
                call.reject(error);
            }
        });
    }

    @PluginMethod
    public void loadAppOpen(PluginCall call) {
        appOpenExecutor.load(getActivity(), call, new ActionCallback() {
            @Override
            public void onSuccess() {
                call.resolve();
            }

            @Override
            public void onError(String error) {
                call.reject("Failed to load app open ad: " + error);
            }
        });
    }

    @PluginMethod
    public void showAppOpen(PluginCall call) {
        appOpenExecutor.show(getActivity(), new ActionCallback() {
            @Override
            public void onSuccess() {
                call.resolve();
            }

            @Override
            public void onError(String error) {
                call.reject(error);
            }
        });
    }

@PluginMethod
public void startPreloadAppOpen(PluginCall call) {
    appOpenPreloadExecutor.startPreload(call, new ActionCallback() {
        @Override public void onSuccess() {
            call.resolve();
        }
        @Override public void onError(String error) {
            call.reject("Failed: " + error);
        }
    });
}

@PluginMethod
public void pollAndShowAppOpen(PluginCall call) {
    appOpenPreloadExecutor.pollAndShow(getActivity(), new ActionCallback() {
        @Override public void onSuccess() {
            call.resolve();
        }
        @Override public void onError(String error) {
            call.reject("Failed: " + error);
        }
    });
}

@PluginMethod
public void isAppOpenPreloadAvailable(PluginCall call) {
    appOpenPreloadExecutor.checkAvailability(call);
}

    @PluginMethod
    public void startPreloadBanner(PluginCall call) {
        bannerPreloadExecutor.startPreload(call, new ActionCallback() {
            @Override public void onSuccess() {
                call.resolve();
            }
            @Override public void onError(String error) {
                call.reject("Failed: " + error);
            }
        });
    }

    @PluginMethod
    public void pollAndShowBanner(PluginCall call) {
        bannerPreloadExecutor.pollAndShow(getActivity(), new ActionCallback() {
            @Override public void onSuccess() {
                call.resolve();
            }
            @Override public void onError(String error) {
                call.reject("Failed: " + error);
            }
        });
    }

    @PluginMethod
    public void hidePreloadedBanner(PluginCall call) {
        bannerPreloadExecutor.hidePreloadedBanner(call);
    }

    @PluginMethod
    public void destroyPreloadedBanner(PluginCall call) {
        bannerPreloadExecutor.destroyPreloadedBanner(call);
    }

    @PluginMethod
    public void isBannerPreloadAvailable(PluginCall call) {
        bannerPreloadExecutor.checkAvailability(call);
    }

    @PluginMethod
    public void startPreloadInterstitial(PluginCall call) {
        interstitialPreloadExecutor.startPreload(call, new ActionCallback() {
            @Override public void onSuccess() {
                call.resolve();
            }
            @Override public void onError(String error) {
                call.reject("Failed: " + error);
            }
        });
    }

    @PluginMethod
    public void pollAndShowInterstitial(PluginCall call) {
        interstitialPreloadExecutor.pollAndShow(getActivity(), new ActionCallback() {
            @Override public void onSuccess() {
                call.resolve();
            }
            @Override public void onError(String error) {
                call.reject("Failed: " + error);
            }
        });
    }

    @PluginMethod
    public void isInterstitialPreloadAvailable(PluginCall call) {
        interstitialPreloadExecutor.checkAvailability(call);
    }

    @PluginMethod
    public void startPreloadRewarded(PluginCall call) {
        rewardedPreloadExecutor.startPreload(call, new ActionCallback() {
            @Override public void onSuccess() {
                call.resolve();
            }
            @Override public void onError(String error) {
                call.reject("Failed: " + error);
            }
        });
    }

    @PluginMethod
    public void pollAndShowRewarded(PluginCall call) {
        rewardedPreloadExecutor.pollAndShow(getActivity(), new ActionCallback() {
            @Override public void onSuccess() {
                call.resolve();
            }
            @Override public void onError(String error) {
                call.reject("Failed: " + error);
            }
        });
    }

    @PluginMethod
    public void isRewardedPreloadAvailable(PluginCall call) {
        rewardedPreloadExecutor.checkAvailability(call);
    }

    @PluginMethod
    public void startPreloadRewardedInterstitial(PluginCall call) {
        rewardedInterstitialPreloadExecutor.startPreload(call, new ActionCallback() {
            @Override public void onSuccess() {
                call.resolve();
            }
            @Override public void onError(String error) {
                call.reject("Failed: " + error);
            }
        });
    }

    @PluginMethod
    public void pollAndShowRewardedInterstitial(PluginCall call) {
        rewardedInterstitialPreloadExecutor.pollAndShow(getActivity(), new ActionCallback() {
            @Override public void onSuccess() {
                call.resolve();
            }
            @Override public void onError(String error) {
                call.reject("Failed: " + error);
            }
        });
    }

    @PluginMethod
    public void isRewardedInterstitialPreloadAvailable(PluginCall call) {
        rewardedInterstitialPreloadExecutor.checkAvailability(call);
    }

}
