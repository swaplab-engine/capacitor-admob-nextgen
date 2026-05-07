package com.emi.plugins.admob;

import com.emi.plugins.admob.interstitial.InterstitialExecutor;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "AdMobNextGen")
public class AdMobNextGenPlugin extends Plugin {

    private AdMobNextGen coreImplementation;
    private InterstitialExecutor interstitialExecutor;

    @Override
    public void load() {
        super.load();

        coreImplementation = new AdMobNextGen();
        interstitialExecutor = new InterstitialExecutor(this);
    }

    @PluginMethod
    public void initialize(PluginCall call) {
        String appId = call.getString("appId");
        if (appId == null) {
            call.reject("Application ID is required.");
            return;
        }

        coreImplementation.initializeSDK(getContext(), appId, () -> {
            JSObject result = new JSObject();
            result.put("status", "INITIALIZED_SUCCESSFULLY");
            call.resolve(result);
        });
    }

    @PluginMethod
    public void loadInterstitial(PluginCall call) {
        String adUnitId = call.getString("adUnitId");
        if (adUnitId == null) {
            call.reject("Ad Unit ID is required.");
            return;
        }

        interstitialExecutor.load(getActivity(), adUnitId, new ActionCallback() {
            @Override
            public void onSuccess() {
                call.resolve();
            }

            @Override
            public void onError(String error) {
                call.reject("Failed to load interstitial: " + error);
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
}
