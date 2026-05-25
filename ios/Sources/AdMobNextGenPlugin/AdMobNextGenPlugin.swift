import Foundation
import Capacitor

/**
 * Main entry point for the Capacitor AdMob Next-Gen Plugin on iOS.
 */
@objc(AdMobNextGenPlugin)
public class AdMobNextGenPlugin: CAPPlugin, CAPBridgedPlugin {
    
    public let identifier = "AdMobNextGenPlugin"
    public let jsName = "AdMobNextGen"
    
    public var isInitialized: Bool = false
    
    // ==========================================
    // 1. REGISTER EXPORTED METHODS TO JAVASCRIPT
    // ==========================================
    public let pluginMethods: [CAPPluginMethod] = [
        // Core Methods
        CAPPluginMethod(name: "initialize", returnType: CAPPluginReturnPromise),
        
        // ATT / Tracking Methods
        CAPPluginMethod(name: "requestTrackingAuthorization", returnType: CAPPluginReturnPromise),
        
        // UMP / Consent Methods
        CAPPluginMethod(name: "requestConsentInfo", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "showPrivacyOptionsForm", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getTCData", returnType: CAPPluginReturnPromise)
    ]
    
    // ==========================================
    // 2. EXECUTOR & CORE INSTANCES
    // ==========================================
    private var consentExecutor: ConsentExecutor!
    private var trackingExecutor: TrackingExecutor! // Tambahkan instance baru
    private let coreImplementation = AdMobNextGen()

    // ==========================================
    // 3. PLUGIN INITIALIZATION
    // ==========================================
    override public func load() {
        super.load()
        self.consentExecutor = ConsentExecutor(plugin: self)
        self.trackingExecutor = TrackingExecutor(plugin: self) // Inisialisasi di sini
    }

    // ==========================================
    // 4. METHOD IMPLEMENTATIONS
    // ==========================================
    
    @objc func initialize(_ call: CAPPluginCall) {
        let maxAdContentRating = call.getString("maxAdContentRating", "")
        
        var childDirected: NSNumber? = nil
        if let hasChildOpt = call.options["tagForChildDirectedTreatment"] as? Bool {
            childDirected = NSNumber(value: hasChildOpt)
        }
        
        var underAge: NSNumber? = nil
        if let hasUnderAgeOpt = call.options["tagForUnderAgeOfConsent"] as? Bool {
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
    
    // --- ATT DELEGATE ---
    @objc func requestTrackingAuthorization(_ call: CAPPluginCall) {
        trackingExecutor.requestTrackingAuthorization(call)
    }
    
    // --- UMP DELEGATES ---
    @objc func requestConsentInfo(_ call: CAPPluginCall) {
        consentExecutor.requestConsentInfo(call)
    }
    
    @objc func showPrivacyOptionsForm(_ call: CAPPluginCall) {
        consentExecutor.showPrivacyOptionsForm(call)
    }
    
    @objc func getTCData(_ call: CAPPluginCall) {
        consentExecutor.getTCData(call)
    }
}
