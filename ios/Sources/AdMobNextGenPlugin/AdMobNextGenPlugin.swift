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
    ]

    private var consentExecutor: ConsentExecutor!
    private var trackingExecutor: TrackingExecutor!  
    private let coreImplementation = AdMobNextGen()

    private var appOpenExecutor: AppOpenExecutor!

    override public func load() {
        super.load()
        self.consentExecutor = ConsentExecutor(plugin: self)
        self.trackingExecutor = TrackingExecutor(plugin: self)
        self.appOpenExecutor = AppOpenExecutor(plugin: self)
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
}
