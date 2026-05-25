import Capacitor
import Foundation
import UserMessagingPlatform

@objc public class ConsentExecutor: NSObject {

    private weak var plugin: CAPPlugin?

    init(plugin: CAPPlugin) {
        self.plugin = plugin
        super.init()
    }

    @objc func requestConsentInfo(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let debugMode = call.getBool("debug", false)
            let resetConsent = call.getBool("reset", false)
            let tagForUnderAgeOfConsent = call.getBool(
                "tagForUnderAgeOfConsent",
                false
            )
            let manualTestDeviceId = call.getString("testDeviceId", "")

            if resetConsent {

                ConsentInformation.shared.reset()
            }

            let parameters = RequestParameters()

            parameters.isTaggedForUnderAgeOfConsent = tagForUnderAgeOfConsent

            if debugMode {

                let debugSettings = DebugSettings()
                debugSettings.geography = .EEA

                if !manualTestDeviceId.isEmpty {
                    debugSettings.testDeviceIdentifiers = [manualTestDeviceId]
                }

                parameters.debugSettings = debugSettings
            }

            ConsentInformation.shared.requestConsentInfoUpdate(with: parameters)
            { [weak self] error in
                guard let self = self else { return }

                if let error = error {
                    self.sendErrorEvent(error)
                    call.reject(error.localizedDescription)
                    return
                }

                self.plugin?.notifyListeners("onConsentInfoUpdated", data: [:])

                guard let viewController = self.plugin?.bridge?.viewController
                else {
                    call.reject("ViewController is null")
                    return
                }

                ConsentForm.loadAndPresentIfRequired(from: viewController) {
                    loadAndShowError in
                    if let error = loadAndShowError {
                        self.sendErrorEvent(error)
                        call.reject(error.localizedDescription)
                    } else {
                        self.plugin?.notifyListeners(
                            "onConsentFormDismissed",
                            data: [:]
                        )
                        self.sendConsentStatus(call)
                    }
                }
            }
        }
    }

    @objc func showPrivacyOptionsForm(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let viewController = self.plugin?.bridge?.viewController
            else {
                call.reject("ViewController is null")
                return
            }

            ConsentForm.presentPrivacyOptionsForm(from: viewController) {
                [weak self] formError in
                guard let self = self else { return }

                if let error = formError {
                    self.sendErrorEvent(error)
                    call.reject(error.localizedDescription)
                } else {
                    self.plugin?.notifyListeners(
                        "onConsentFormDismissed",
                        data: [:]
                    )
                    self.sendConsentStatus(call)
                }
            }
        }
    }

    @objc func getTCData(_ call: CAPPluginCall) {
        let defaults = UserDefaults.standard
        let tcString = defaults.string(forKey: "IABTCF_TCString") ?? ""
        let purposeConsents =
            defaults.string(forKey: "IABTCF_PurposeConsents") ?? ""
        let vendorConsents =
            defaults.string(forKey: "IABTCF_VendorConsents") ?? ""
        let gdprApplies = defaults.integer(forKey: "IABTCF_gdprApplies")

        var isPersonalizedAllowed = false
        var statusMessage = "Unknown"

        if gdprApplies == 0 {
            isPersonalizedAllowed = true
            statusMessage =
                "Not GDPR region. Personalized Ads allowed by default."
        } else {
            if !purposeConsents.isEmpty {
                if purposeConsents.first == "1" {
                    isPersonalizedAllowed = true
                    statusMessage =
                        "Purpose 1 Granted. Personalized Ads allowed."
                } else {
                    isPersonalizedAllowed = false
                    statusMessage =
                        "Purpose 1 Denied. Non-Personalized / Limited Ads only."
                }
            } else {
                isPersonalizedAllowed = false
                statusMessage =
                    "No consent data found (User hasn't answered yet)."
            }
        }

        var tcData = JSObject()
        tcData["tcString"] = tcString
        tcData["purposeConsents"] = purposeConsents
        tcData["vendorConsents"] = vendorConsents
        tcData["gdprApplies"] = gdprApplies
        tcData["isPersonalizedAllowed"] = isPersonalizedAllowed
        tcData["statusMessage"] = statusMessage

        call.resolve(tcData)
    }

    private func sendConsentStatus(_ call: CAPPluginCall?) {

        let consentInfo = ConsentInformation.shared
        var result = JSObject()

        result["canRequestAds"] = consentInfo.canRequestAds

        let requirementStatus = consentInfo.privacyOptionsRequirementStatus
        var statusString = "UNKNOWN"
        var isRequired = false

        switch requirementStatus {
        case .required:
            statusString = "REQUIRED"
            isRequired = true
        case .notRequired:
            statusString = "NOT_REQUIRED"
        default:
            statusString = "UNKNOWN"
        }

        result["privacyOptionsRequirementStatus"] = statusString
        result["isPrivacyOptionsRequired"] = isRequired
        result["consentStatus"] = consentInfo.consentStatus.rawValue

        plugin?.notifyListeners("onConsentStatusChange", data: result)
        call?.resolve(result)
    }

    private func sendErrorEvent(_ error: Error) {
        let nsError = error as NSError
        var errData = JSObject()
        errData["code"] = nsError.code
        errData["message"] = nsError.localizedDescription
        plugin?.notifyListeners("onConsentError", data: errData)
    }
}
