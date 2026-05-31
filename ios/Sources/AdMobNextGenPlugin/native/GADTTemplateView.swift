import GoogleMobileAds
import UIKit

@objc(GADTTemplateView)
public class GADTTemplateView: NativeAdView {

    @IBOutlet public weak var adBadge: UILabel!

    public override func awakeFromNib() {
        super.awakeFromNib()

        if let badge = adBadge {
            badge.layer.borderColor = badge.textColor.cgColor
            badge.layer.borderWidth = 1.0
            badge.layer.cornerRadius = 3.0
            badge.layer.masksToBounds = true
        }
    }

    public override var nativeAd: NativeAd? {
        didSet {
            super.nativeAd = nativeAd

            guard let nativeAd = nativeAd else { return }

            (self.headlineView as? UILabel)?.text = nativeAd.headline

            if let iconImgView = self.iconView as? UIImageView {
                iconImgView.image = nativeAd.icon?.image
                iconImgView.isHidden = nativeAd.icon == nil
            }

            if let ctaBtn = self.callToActionView as? UIButton {
                ctaBtn.setTitle(nativeAd.callToAction, for: .normal)
                ctaBtn.isHidden = nativeAd.callToAction == nil
                ctaBtn.isUserInteractionEnabled = false
            }

            let advView = self.advertiserView as? UILabel
            let strView = self.storeView as? UILabel

            if nativeAd.advertiser != nil && nativeAd.store == nil {
                strView?.isHidden = true
                advView?.text = nativeAd.advertiser
                advView?.isHidden = false
            } else if nativeAd.store != nil && nativeAd.advertiser == nil {
                advView?.isHidden = true
                strView?.text = nativeAd.store
                strView?.isHidden = false
            } else if nativeAd.advertiser != nil && nativeAd.store != nil {
                strView?.isHidden = true
                advView?.text = nativeAd.advertiser
                advView?.isHidden = false
            }

            let ratingView = self.starRatingView as? UILabel
            let bdyView = self.bodyView as? UILabel

            if let starRating = nativeAd.starRating, starRating.doubleValue > 0
            {
                var stars = ""
                let ratingInt = Int(starRating.doubleValue)
                for _ in 0..<ratingInt { stars += "\u{2605}" }
                for _ in ratingInt..<5 { stars += "\u{2606}" }
                ratingView?.text = stars
                bdyView?.isHidden = true
                ratingView?.isHidden = false
            } else {
                ratingView?.isHidden = true
                bdyView?.text = nativeAd.body
                bdyView?.isHidden = false
            }

            self.mediaView?.mediaContent = nativeAd.mediaContent
        }
    }
}

@objc(GADTMediumTemplateView)
public class GADTMediumTemplateView: GADTTemplateView {
    public override func awakeFromNib() {
        super.awakeFromNib()
    }
}

@objc(GADTSmallTemplateView)
public class GADTSmallTemplateView: GADTTemplateView {
    public override func awakeFromNib() {
        super.awakeFromNib()
    }
}
