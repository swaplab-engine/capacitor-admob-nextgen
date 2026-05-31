import Capacitor
import Foundation

@objc(AdMobNextGenPlugin)
public class AdMobNextGenPlugin: CAPPlugin, CAPBridgedPlugin {

    public let identifier = "AdMobNextGenPlugin"
    public let jsName = "AdMobNextGen"

    public var isInitialized: Bool = false

    public let pluginMethods: [CAPPluginMethod] = [

        CAPPluginMethod(name: "initialize", returnType: CAPPluginReturnPromise),

        CAPPluginMethod(
            name: "requestTrackingAuthorization",
            returnType: CAPPluginReturnPromise
        ),

        CAPPluginMethod(
            name: "requestConsentInfo",
            returnType: CAPPluginReturnPromise
        ),
        CAPPluginMethod(
            name: "showPrivacyOptionsForm",
            returnType: CAPPluginReturnPromise
        ),
        CAPPluginMethod(name: "getTCData", returnType: CAPPluginReturnPromise),

        CAPPluginMethod(
            name: "loadAppOpen",
            returnType: CAPPluginReturnPromise
        ),
        CAPPluginMethod(
            name: "showAppOpen",
            returnType: CAPPluginReturnPromise
        ),

        CAPPluginMethod(
            name: "createBanner",
            returnType: CAPPluginReturnPromise
        ),
        CAPPluginMethod(name: "showBanner", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "hideBanner", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(
            name: "destroyBanner",
            returnType: CAPPluginReturnPromise
        ),

        CAPPluginMethod(
            name: "showNativeAd",
            returnType: CAPPluginReturnPromise
        ),
        CAPPluginMethod(
            name: "hideNativeAd",
            returnType: CAPPluginReturnPromise
        ),

        CAPPluginMethod(
            name: "loadInterstitial",
            returnType: CAPPluginReturnPromise
        ),
        CAPPluginMethod(
            name: "showInterstitial",
            returnType: CAPPluginReturnPromise
        ),

        CAPPluginMethod(
            name: "loadRewarded",
            returnType: CAPPluginReturnPromise
        ),
        CAPPluginMethod(
            name: "showRewarded",
            returnType: CAPPluginReturnPromise
        ),

        CAPPluginMethod(
            name: "loadRewardedInterstitial",
            returnType: CAPPluginReturnPromise
        ),
        CAPPluginMethod(
            name: "showRewardedInterstitial",
            returnType: CAPPluginReturnPromise
        ),
    ]

    private var consentExecutor: ConsentExecutor!
    private var trackingExecutor: TrackingExecutor!
    private let coreImplementation = AdMobNextGen()

    private var appOpenExecutor: AppOpenExecutor!
    private var bannerExecutor: BannerExecutor!
    private var nativeExecutor: NativeExecutor!
    private var interstitialExecutor: InterstitialExecutor!
    private var rewardedExecutor: RewardedExecutor!
    private var rewardedInterstitialExecutor: RewardedInterstitialExecutor!

    override public func load() {
        super.load()
        self.consentExecutor = ConsentExecutor(plugin: self)
        self.trackingExecutor = TrackingExecutor(plugin: self)
        self.appOpenExecutor = AppOpenExecutor(plugin: self)
        self.bannerExecutor = BannerExecutor(plugin: self)
        self.nativeExecutor = NativeExecutor(plugin: self)
        self.interstitialExecutor = InterstitialExecutor(plugin: self)
        self.rewardedExecutor = RewardedExecutor(plugin: self)
        self.rewardedInterstitialExecutor = RewardedInterstitialExecutor(plugin: self)
    }

    @objc func initialize(_ call: CAPPluginCall) {
        let maxAdContentRating = call.getString("maxAdContentRating", "")

        var childDirected: NSNumber? = nil
        if let hasChildOpt = call.options["tagForChildDirectedTreatment"]
            as? Bool
        {
            childDirected = NSNumber(value: hasChildOpt)
        }

        var underAge: NSNumber? = nil
        if let hasUnderAgeOpt = call.options["tagForUnderAgeOfConsent"] as? Bool
        {
            underAge = NSNumber(value: hasUnderAgeOpt)
        }

        let isTesting = call.getBool("isTesting", false)

        coreImplementation.initializeSDK(
            maxAdContentRating: maxAdContentRating,
            tagForChildDirectedTreatment: childDirected,
            tagForUnderAgeOfConsent: underAge,
            isTesting: isTesting
        ) { [weak self] in
            self?.isInitialized = true
            var result = JSObject()
            result["status"] = "INITIALIZED_SUCCESSFULLY"
            call.resolve(result)
        }
    }

    @objc func requestTrackingAuthorization(_ call: CAPPluginCall) {
        trackingExecutor.requestTrackingAuthorization(call)
    }

    @objc func requestConsentInfo(_ call: CAPPluginCall) {
        consentExecutor.requestConsentInfo(call)
    }

    @objc func showPrivacyOptionsForm(_ call: CAPPluginCall) {
        consentExecutor.showPrivacyOptionsForm(call)
    }

    @objc func getTCData(_ call: CAPPluginCall) {
        consentExecutor.getTCData(call)
    }

    @objc func loadAppOpen(_ call: CAPPluginCall) {
        appOpenExecutor.loadAd(call)
    }

    @objc func showAppOpen(_ call: CAPPluginCall) {
        appOpenExecutor.showAd(call)
    }

    @objc func createBanner(_ call: CAPPluginCall) {
        bannerExecutor.createBanner(call)
    }
    @objc func showBanner(_ call: CAPPluginCall) {
        bannerExecutor.showBanner(call)
    }
    @objc func hideBanner(_ call: CAPPluginCall) {
        bannerExecutor.hideBanner(call)
    }
    @objc func destroyBanner(_ call: CAPPluginCall) {
        bannerExecutor.destroyBanner(call)
    }

    @objc func showNativeAd(_ call: CAPPluginCall) {
        nativeExecutor.showNativeAd(call)
    }

    @objc func hideNativeAd(_ call: CAPPluginCall) {
        nativeExecutor.hideNativeAd(call)
    }

    @objc func loadInterstitial(_ call: CAPPluginCall) {
        interstitialExecutor.loadAd(call)
    }

    @objc func showInterstitial(_ call: CAPPluginCall) {
        interstitialExecutor.showAd(call)
    }

    @objc func loadRewarded(_ call: CAPPluginCall) {
        rewardedExecutor.loadAd(call)
    }

    @objc func showRewarded(_ call: CAPPluginCall) {
        rewardedExecutor.showAd(call)
    }

    @objc func loadRewardedInterstitial(_ call: CAPPluginCall) {
        rewardedInterstitialExecutor.loadAd(call)
    }

    @objc func showRewardedInterstitial(_ call: CAPPluginCall) {
        rewardedInterstitialExecutor.showAd(call)
    }
}
