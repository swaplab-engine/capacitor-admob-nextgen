package com.emi.plugins.admob;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import com.getcapacitor.Logger;
import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.common.AgeRestrictedTreatment;
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig;
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdMobNextGen {

    public void initializeSDK(Context context, String appId, String maxRating, Boolean childTag, Boolean underAgeTag, Boolean isTesting, Runnable onComplete) {

        RequestConfiguration.Builder configBuilder = new RequestConfiguration.Builder();

        if (maxRating != null && !maxRating.isEmpty()) {
            switch (maxRating.toUpperCase()) {
                case "G": configBuilder.setMaxAdContentRating(RequestConfiguration.MaxAdContentRating.MAX_AD_CONTENT_RATING_G);
                    break;
                case "PG": configBuilder.setMaxAdContentRating(RequestConfiguration.MaxAdContentRating.MAX_AD_CONTENT_RATING_PG);
                    break;
                case "T": configBuilder.setMaxAdContentRating(RequestConfiguration.MaxAdContentRating.MAX_AD_CONTENT_RATING_T);
                    break;
                case "MA": configBuilder.setMaxAdContentRating(RequestConfiguration.MaxAdContentRating.MAX_AD_CONTENT_RATING_MA);
                    break;
            }
        }

        if (childTag != null && childTag) {
            configBuilder.setAgeRestrictedTreatment(AgeRestrictedTreatment.CHILD);
        } else if (underAgeTag != null && underAgeTag) {
            configBuilder.setAgeRestrictedTreatment(AgeRestrictedTreatment.TEEN);
        } else if ((childTag != null && !childTag) || (underAgeTag != null && !underAgeTag)) {
            configBuilder.setAgeRestrictedTreatment(AgeRestrictedTreatment.UNSPECIFIED);
        }

        if (isTesting != null && isTesting) {
            String deviceId = getDeviceId(context);
            if (deviceId != null && !deviceId.isEmpty()) {
                List<String> testDeviceIds = new ArrayList<>();
                testDeviceIds.add(deviceId);
                configBuilder.setTestDeviceIds(testDeviceIds);
                Logger.info("AdMobNextGen", "Test Device ID registered: " + deviceId);
            }
        }

        RequestConfiguration requestConfiguration = configBuilder.build();

        new Thread(() -> {
            MobileAds.initialize(
                    context,
                    new InitializationConfig.Builder(appId)
                            .setRequestConfiguration(requestConfiguration)
                            .build(),
                    initializationStatus -> {
                        Logger.info("AdMobNextGen", "GMA Next-Gen SDK initialized.");
                        onComplete.run();
                    }
            );
        }).start();
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
