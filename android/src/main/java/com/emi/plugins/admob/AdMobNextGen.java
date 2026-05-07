package com.emi.plugins.admob;

import android.content.Context;
import com.getcapacitor.Logger;
import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig;

public class AdMobNextGen {

    public void initializeSDK(Context context, String appId, Runnable onComplete) {
        new Thread(() -> {
            MobileAds.initialize(
                    context,
                    new InitializationConfig.Builder(appId).build(),
                    initializationStatus -> {

                        onComplete.run();
                    }
            );
        }).start();
    }
}
