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
            let showFormIfRequired = call.getBool("showFormIfRequired", true)

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

                if showFormIfRequired {
                    guard let viewController = self.plugin?.bridge?.viewController else {
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
                } else {

                    self.sendConsentStatus(call)
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
        var tcData = JSObject()

        let allPrefs = defaults.dictionaryRepresentation()
        for (key, value) in allPrefs {
            if key.hasPrefix("IABTCF_") {
                if let strVal = value as? String {
                    tcData[key] = strVal
                } else if let intVal = value as? Int {
                    tcData[key] = intVal
                } else if let doubleVal = value as? Double {
                    tcData[key] = doubleVal
                } else if let boolVal = value as? Bool {
                    tcData[key] = boolVal
                }
            }
        }

        let purposeConsents = defaults.string(forKey: "IABTCF_PurposeConsents") ?? ""
        let purposeLegitimateInterests = defaults.string(forKey: "IABTCF_PurposeLegitimateInterests") ?? ""
        let vendorConsents = defaults.string(forKey: "IABTCF_VendorConsents") ?? ""
        let gdprApplies = defaults.integer(forKey: "IABTCF_gdprApplies")

        var isPersonalizedAllowed = false
        var statusMessage = "Unknown"

        var isAdMobPersonalizedAdsAllowed = false
        var isAdMobNonPersonalizedAdsAllowed = false
        var adMobConsentStatus = "Unknown"

        if gdprApplies == 0 {

            isPersonalizedAllowed = true
            statusMessage = "Not GDPR region. Personalized Ads allowed by default."

            isAdMobPersonalizedAdsAllowed = true
            isAdMobNonPersonalizedAdsAllowed = true
            adMobConsentStatus = "Not GDPR region. AdMob ads allowed by default."
        } else {

            if !purposeConsents.isEmpty {
                if purposeConsents.first == "1" {
                    isPersonalizedAllowed = true
                    statusMessage = "Purpose 1 Granted. Legacy check passed."
                } else {
                    isPersonalizedAllowed = false
                    statusMessage = "Purpose 1 Denied. Legacy check failed."
                }
            }

            let hasPurpose1 = checkConsent(purposeConsents, id: 1)
            let hasPurpose3 = checkConsent(purposeConsents, id: 3)
            let hasPurpose4 = checkConsent(purposeConsents, id: 4)

            let hasRequiredLI_or_Consent =
                hasConsentOrLI(consents: purposeConsents, lis: purposeLegitimateInterests, id: 2) &&
                hasConsentOrLI(consents: purposeConsents, lis: purposeLegitimateInterests, id: 7) &&
                hasConsentOrLI(consents: purposeConsents, lis: purposeLegitimateInterests, id: 9) &&
                hasConsentOrLI(consents: purposeConsents, lis: purposeLegitimateInterests, id: 10)

            let hasVendorGoogle = checkConsent(vendorConsents, id: 755) 

            if hasPurpose1 && hasVendorGoogle && hasRequiredLI_or_Consent {
                isAdMobNonPersonalizedAdsAllowed = true

                if hasPurpose3 && hasPurpose4 {
                    isAdMobPersonalizedAdsAllowed = true
                    adMobConsentStatus = "Strict requirements met for Personalized Ads (Purposes 1,3,4 + LI 2,7,9,10 + Vendor 755)."
                } else {
                    isAdMobPersonalizedAdsAllowed = false
                    adMobConsentStatus = "Requirements met for Non-Personalized Ads only."
                }
            } else {
                isAdMobPersonalizedAdsAllowed = false
                isAdMobNonPersonalizedAdsAllowed = false
                adMobConsentStatus = "Insufficient strict consent (Missing P1, Vendor 755, or P2,7,9,10). Limited Ads only."
            }
        }

        tcData["isPersonalizedAllowed"] = isPersonalizedAllowed
        tcData["statusMessage"] = statusMessage

        tcData["isAdMobPersonalizedAdsAllowed"] = isAdMobPersonalizedAdsAllowed
        tcData["isAdMobNonPersonalizedAdsAllowed"] = isAdMobNonPersonalizedAdsAllowed
        tcData["adMobConsentStatus"] = adMobConsentStatus

        call.resolve(tcData)
    }

    private func checkConsent(_ consentString: String, id: Int) -> Bool {
        guard id > 0, consentString.count >= id else { return false }
        let index = consentString.index(consentString.startIndex, offsetBy: id - 1)
        return consentString[index] == "1"
    }

    private func hasConsentOrLI(consents: String, lis: String, id: Int) -> Bool {
        let hasConsent = checkConsent(consents, id: id)
        let hasLI = checkConsent(lis, id: id)
        return hasConsent || hasLI
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
