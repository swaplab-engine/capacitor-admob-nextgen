package com.emi.plugins.admob.ump;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.emi.plugins.admob.AdMobNextGenPlugin;

import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map; 

public class ConsentExecutor {

    private final AdMobNextGenPlugin plugin;
    private ConsentInformation consentInformation;

    public ConsentExecutor(AdMobNextGenPlugin plugin) {
        this.plugin = plugin;
        this.consentInformation = UserMessagingPlatform.getConsentInformation(plugin.getActivity());
    }

    public void requestConsentInfo(final PluginCall call) {
        Activity activity = plugin.getActivity();

        Boolean debugOpt = call.getBoolean("debug", false);
        boolean debugMode = (debugOpt != null) ? debugOpt : false;

        Boolean resetOpt = call.getBoolean("reset", false);
        boolean resetConsent = (resetOpt != null) ? resetOpt : false;

        Boolean underAgeOpt = call.getBoolean("tagForUnderAgeOfConsent", false);
        boolean tagForUnderAgeOfConsent = (underAgeOpt != null) ? underAgeOpt : false;

        Boolean showFormOpt = call.getBoolean("showFormIfRequired", true);
        final boolean showFormIfRequired = (showFormOpt != null) ? showFormOpt : true;

        String manualTestDeviceId = call.getString("testDeviceId", "");

        activity.runOnUiThread(() -> {
            if (resetConsent) {
                consentInformation.reset();
            }

            ConsentRequestParameters.Builder paramsBuilder = new ConsentRequestParameters.Builder();
            paramsBuilder.setTagForUnderAgeOfConsent(tagForUnderAgeOfConsent);

            if (debugMode) {
                ConsentDebugSettings.Builder debugSettingsBuilder = new ConsentDebugSettings.Builder(activity)
                        .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA);

                if (manualTestDeviceId != null && !manualTestDeviceId.isEmpty()) {
                    debugSettingsBuilder.addTestDeviceHashedId(manualTestDeviceId);
                } else {
                    String deviceId = getDeviceId(activity);
                    if (deviceId != null) {
                        debugSettingsBuilder.addTestDeviceHashedId(deviceId);
                    }
                }
                paramsBuilder.setConsentDebugSettings(debugSettingsBuilder.build());
            }

            ConsentRequestParameters params = paramsBuilder.build();

            consentInformation.requestConsentInfoUpdate(
                    activity,
                    params,
                    () -> {
                        plugin.notifyPluginListeners("onConsentInfoUpdated", new JSObject());

                        if (showFormIfRequired) {
                            UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                                    activity,
                                    (FormError loadAndShowError) -> {
                                        if (loadAndShowError != null) {
                                            sendErrorEvent(loadAndShowError);
                                            call.reject(loadAndShowError.getMessage());
                                        } else {
                                            plugin.notifyPluginListeners("onConsentFormDismissed", new JSObject());
                                            sendConsentStatus(call);
                                        }
                                    }
                            );
                        } else {

                            sendConsentStatus(call);
                        }
                    },
                    (FormError requestConsentError) -> {
                        sendErrorEvent(requestConsentError);
                        call.reject(requestConsentError.getMessage());
                    }
            );
        });
    }

    public void showPrivacyOptionsForm(final PluginCall call) {
        Activity activity = plugin.getActivity();
        activity.runOnUiThread(() -> {
            UserMessagingPlatform.showPrivacyOptionsForm(
                    activity,
                    (FormError formError) -> {
                        if (formError != null) {
                            sendErrorEvent(formError);
                            call.reject(formError.getMessage());
                        } else {
                            plugin.notifyPluginListeners("onConsentFormDismissed", new JSObject());
                            sendConsentStatus(call);
                        }
                    }
            );
        });
    }

    public void getTCData(final PluginCall call) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(plugin.getActivity());
            JSObject tcData = new JSObject();

            Map<String, ?> allEntries = prefs.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().startsWith("IABTCF_")) {
                    tcData.put(entry.getKey(), entry.getValue());
                }
            }

            String purposeConsents = prefs.getString("IABTCF_PurposeConsents", "");
            String purposeLegitimateInterests = prefs.getString("IABTCF_PurposeLegitimateInterests", ""); 
            String vendorConsents = prefs.getString("IABTCF_VendorConsents", "");
            int gdprApplies = prefs.getInt("IABTCF_gdprApplies", 0);

            boolean isPersonalizedAllowed = false;
            String statusMessage = "Unknown";

            boolean isAdMobPersonalizedAdsAllowed = false;
            boolean isAdMobNonPersonalizedAdsAllowed = false;
            String adMobConsentStatus = "Unknown";

            if (gdprApplies == 0) {

                isPersonalizedAllowed = true;
                statusMessage = "Not GDPR region. Personalized Ads allowed by default.";

                isAdMobPersonalizedAdsAllowed = true;
                isAdMobNonPersonalizedAdsAllowed = true;
                adMobConsentStatus = "Not GDPR region. AdMob ads allowed by default.";
            } else {

                if (purposeConsents != null && purposeConsents.length() > 0) {
                    char p1 = purposeConsents.charAt(0);
                    if (p1 == '1') {
                        isPersonalizedAllowed = true;
                        statusMessage = "Purpose 1 Granted. Legacy check passed.";
                    } else {
                        isPersonalizedAllowed = false;
                        statusMessage = "Purpose 1 Denied. Legacy check failed.";
                    }
                }

                boolean hasPurpose1 = checkConsent(purposeConsents, 1);
                boolean hasPurpose3 = checkConsent(purposeConsents, 3);
                boolean hasPurpose4 = checkConsent(purposeConsents, 4);

                boolean hasRequiredLI_or_Consent = 
                    hasConsentOrLI(purposeConsents, purposeLegitimateInterests, 2) &&
                    hasConsentOrLI(purposeConsents, purposeLegitimateInterests, 7) &&
                    hasConsentOrLI(purposeConsents, purposeLegitimateInterests, 9) &&
                    hasConsentOrLI(purposeConsents, purposeLegitimateInterests, 10);

                boolean hasVendorGoogle = checkConsent(vendorConsents, 755); 

                if (hasPurpose1 && hasVendorGoogle && hasRequiredLI_or_Consent) {
                    isAdMobNonPersonalizedAdsAllowed = true;

                    if (hasPurpose3 && hasPurpose4) {
                        isAdMobPersonalizedAdsAllowed = true;
                        adMobConsentStatus = "Strict requirements met for Personalized Ads (Purposes 1,3,4 + LI 2,7,9,10 + Vendor 755).";
                    } else {
                        isAdMobPersonalizedAdsAllowed = false;
                        adMobConsentStatus = "Requirements met for Non-Personalized Ads only.";
                    }
                } else {
                    isAdMobPersonalizedAdsAllowed = false;
                    isAdMobNonPersonalizedAdsAllowed = false;
                    adMobConsentStatus = "Insufficient strict consent (Missing P1, Vendor 755, or P2,7,9,10). Limited Ads only.";
                }
            }

            tcData.put("isPersonalizedAllowed", isPersonalizedAllowed);
            tcData.put("statusMessage", statusMessage);

            tcData.put("isAdMobPersonalizedAdsAllowed", isAdMobPersonalizedAdsAllowed);
            tcData.put("isAdMobNonPersonalizedAdsAllowed", isAdMobNonPersonalizedAdsAllowed);
            tcData.put("adMobConsentStatus", adMobConsentStatus);

            call.resolve(tcData);
        } catch (Exception e) {
            call.reject("Failed to read TC Data: " + e.getMessage());
        }
    }

    private boolean checkConsent(String consentString, int id) {
        if (consentString == null || consentString.length() < id) {
            return false;
        }
        return consentString.charAt(id - 1) == '1';
    }

    private boolean hasConsentOrLI(String consents, String lis, int id) {
        boolean hasConsent = checkConsent(consents, id);
        boolean hasLI = checkConsent(lis, id);
        return hasConsent || hasLI;
    }

    private void sendConsentStatus(PluginCall call) {
        JSObject result = new JSObject();
        boolean canRequestAds = consentInformation.canRequestAds();
        result.put("canRequestAds", canRequestAds);

        ConsentInformation.PrivacyOptionsRequirementStatus requirementStatus = consentInformation.getPrivacyOptionsRequirementStatus();
        result.put("privacyOptionsRequirementStatus", requirementStatus.name());
        result.put("isPrivacyOptionsRequired", requirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED);
        result.put("consentStatus", consentInformation.getConsentStatus());

        plugin.notifyPluginListeners("onConsentStatusChange", result);

        if (call != null) {
            call.resolve(result);
        }
    }

    private void sendErrorEvent(FormError error) {
        JSObject errData = new JSObject();
        errData.put("code", error.getErrorCode());
        errData.put("message", error.getMessage());
        plugin.notifyPluginListeners("onConsentError", errData);
    }

    private String getDeviceId(Context context) {
        try {
            @SuppressLint("HardwareIds") 
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (androidId == null || androidId.isEmpty()) return null;
            return md5(androidId).toUpperCase(Locale.getDefault());
        } catch (Exception e) {
            return null;
        }
    }

    private String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String h = Integer.toHexString(0xFF & b);
                while (h.length() < 2) h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}
