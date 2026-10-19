import Foundation
import GoogleMobileAds

@objc public class AdMobNextGen: NSObject {

    @objc public func initializeSDK(
        maxAdContentRating: String,
        tagForChildDirectedTreatment: NSNumber?,
        tagForUnderAgeOfConsent: NSNumber?,
        isTesting: Bool,
        completion: @escaping () -> Void
    ) {

        let requestConfiguration = MobileAds.shared.requestConfiguration

        if !maxAdContentRating.isEmpty {
            requestConfiguration.maxAdContentRating = GADMaxAdContentRating(
                rawValue: maxAdContentRating
            )
        }

        if let childDirected = tagForChildDirectedTreatment?.boolValue, childDirected {
            requestConfiguration.ageRestrictedTreatment = .child
        } else if let underAge = tagForUnderAgeOfConsent?.boolValue, underAge {
            requestConfiguration.ageRestrictedTreatment = .teen
        } else if (tagForChildDirectedTreatment != nil && tagForChildDirectedTreatment?.boolValue == false) ||
                  (tagForUnderAgeOfConsent != nil && tagForUnderAgeOfConsent?.boolValue == false) {
            requestConfiguration.ageRestrictedTreatment = .unspecified
        }

        MobileAds.shared.start { status in

            completion()
        }
    }
}
