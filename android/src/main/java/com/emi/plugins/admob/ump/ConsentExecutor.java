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
                        plugin.notifyListeners("onConsentInfoUpdated", new JSObject());

                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                                activity,
                                (FormError loadAndShowError) -> {
                                    if (loadAndShowError != null) {
                                        sendErrorEvent(loadAndShowError);
                                        call.reject(loadAndShowError.getMessage());
                                    } else {
                                        plugin.notifyListeners("onConsentFormDismissed", new JSObject());
                                        sendConsentStatus(call);
                                    }
                                }
                        );
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
                            plugin.notifyListeners("onConsentFormDismissed", new JSObject());
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

            String tcString = prefs.getString("IABTCF_TCString", "");
            String purposeConsents = prefs.getString("IABTCF_PurposeConsents", "");
            String vendorConsents = prefs.getString("IABTCF_VendorConsents", "");
            int gdprApplies = prefs.getInt("IABTCF_gdprApplies", 0);

            tcData.put("tcString", tcString);
            tcData.put("purposeConsents", purposeConsents);
            tcData.put("vendorConsents", vendorConsents);
            tcData.put("gdprApplies", gdprApplies);

            boolean isPersonalizedAllowed = false;
            String statusMessage = "Unknown";

            if (gdprApplies == 0) {
                isPersonalizedAllowed = true;
                statusMessage = "Not GDPR region. Personalized Ads allowed by default.";
            } else {
                if (purposeConsents != null && purposeConsents.length() > 0) {
                    char p1 = purposeConsents.charAt(0);
                    if (p1 == '1') {
                        isPersonalizedAllowed = true;
                        statusMessage = "Purpose 1 Granted. Personalized Ads allowed.";
                    } else {
                        isPersonalizedAllowed = false;
                        statusMessage = "Purpose 1 Denied. Non-Personalized / Limited Ads only.";
                    }
                } else {
                    isPersonalizedAllowed = false;
                    statusMessage = "No consent data found (User hasn't answered yet).";
                }
            }

            tcData.put("isPersonalizedAllowed", isPersonalizedAllowed);
            tcData.put("statusMessage", statusMessage);

            call.resolve(tcData);
        } catch (Exception e) {
            call.reject("Failed to read TC Data: " + e.getMessage());
        }
    }

    private void sendConsentStatus(PluginCall call) {
        JSObject result = new JSObject();
        boolean canRequestAds = consentInformation.canRequestAds();
        result.put("canRequestAds", canRequestAds);

        ConsentInformation.PrivacyOptionsRequirementStatus requirementStatus = consentInformation.getPrivacyOptionsRequirementStatus();
        result.put("privacyOptionsRequirementStatus", requirementStatus.name());
        result.put("isPrivacyOptionsRequired", requirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED);
        result.put("consentStatus", consentInformation.getConsentStatus());

        plugin.notifyListeners("onConsentStatusChange", result);

        if (call != null) {
            call.resolve(result);
        }
    }

    private void sendErrorEvent(FormError error) {
        JSObject errData = new JSObject();
        errData.put("code", error.getErrorCode());
        errData.put("message", error.getMessage());
        plugin.notifyListeners("onConsentError", errData);
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
