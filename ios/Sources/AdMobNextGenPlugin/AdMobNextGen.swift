import Foundation
import GoogleMobileAds

@objc public class AdMobNextGen: NSObject {
    
    /// Initializes the core Google Mobile Ads SDK and applies global request configurations.
    @objc public func initializeSDK(
        maxAdContentRating: String,
        tagForChildDirectedTreatment: NSNumber?,
        tagForUnderAgeOfConsent: NSNumber?,
        isTesting: Bool,
        completion: @escaping () -> Void
    ) {
        
        // 1. Apply Global Request Configurations
        // FIX: GADMobileAds.sharedInstance diubah menjadi MobileAds.shared
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
        
        // 2. Initialize the SDK
        // FIX: GADMobileAds.sharedInstance diubah menjadi MobileAds.shared
        MobileAds.shared.start { status in
            // Initialization is complete.
            // 'status' contains adapter initialization states if using mediation.
            completion()
        }
    }
}
