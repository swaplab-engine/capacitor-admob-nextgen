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
            requestConfiguration.maxAdContentRating = GADMaxAdContentRating(rawValue: maxAdContentRating)
        }

        if let childDirected = tagForChildDirectedTreatment {
            requestConfiguration.tagForChildDirectedTreatment = childDirected
        }

        if let underAge = tagForUnderAgeOfConsent {
            requestConfiguration.tagForUnderAgeOfConsent = underAge
        }

        MobileAds.shared.start { status in

            completion()
        }
    }
}
