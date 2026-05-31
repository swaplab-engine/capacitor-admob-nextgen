import Capacitor
import Foundation
import GoogleMobileAds
import UIKit

public class NativeExecutor: NSObject, NativeAdLoaderDelegate, NativeAdDelegate
{

    private weak var plugin: AdMobNextGenPlugin?
    private var adLoader: AdLoader?
    private var currentNativeAd: NativeAd?
    private var adContainer: UIView?

    private var scrollObserver: NSKeyValueObservation?
    private var lastShowTime: TimeInterval = 0

    private var adAbsoluteX: CGFloat = 0
    private var adAbsoluteY: CGFloat = 0
    private var adWidth: CGFloat = 0
    private var isMediumTemplate: Bool = false

    private var savedCall: CAPPluginCall?

    public init(plugin: AdMobNextGenPlugin) {
        self.plugin = plugin
        super.init()
    }

    public func showNativeAd(_ call: CAPPluginCall) {
        guard let plugin = plugin else {
            call.reject("Plugin is not initialized")
            return
        }

        let adUnitId = call.getString("adUnitId", "")
        if adUnitId.isEmpty {
            call.reject("adUnitId is required")
            return
        }

        let retryOpt = call.getDouble("retryInterval") ?? 5000.0
        let minLoadInterval = retryOpt / 1000.0
        let currentTime = Date().timeIntervalSince1970

        if (currentTime - lastShowTime) < minLoadInterval {
            call.reject(
                "Request too fast. Please wait \(retryOpt)ms to prevent invalid traffic."
            )
            return
        }
        lastShowTime = currentTime

        let templateName = call.getString("template", "small")
        self.isMediumTemplate = (templateName.lowercased() == "medium")

        let xVal = call.getDouble("x") ?? 0.0
        let yVal = call.getDouble("y") ?? 0.0
        let wVal = call.getDouble("width")

        self.adAbsoluteX = CGFloat(xVal)
        self.adAbsoluteY = CGFloat(yVal)

        DispatchQueue.main.async { [weak self] in
            guard let self = self,
                let viewController = self.plugin?.bridge?.viewController
            else { return }

            self.adWidth =
                (wVal != nil && wVal! > 0)
                ? CGFloat(wVal!) : viewController.view.bounds.width
            self.savedCall = call

            self.adLoader = AdLoader(
                adUnitID: adUnitId,
                rootViewController: viewController,
                adTypes: [.native],
                options: nil
            )
            self.adLoader?.delegate = self
            self.adLoader?.load(Request())
        }
    }

    public func adLoader(_ adLoader: AdLoader, didReceive nativeAd: NativeAd) {
        nativeAd.delegate = self

        nativeAd.paidEventHandler = { [weak self] adValue in
            let multiplier = NSDecimalNumber(value: 1_000_000)
            let microsValue = adValue.value.multiplying(by: multiplier)
                .int64Value

            var precisionStr = "UNKNOWN"
            switch adValue.precision {
            case .estimated: precisionStr = "ESTIMATED"
            case .publisherProvided: precisionStr = "PUBLISHER_PROVIDED"
            case .precise: precisionStr = "PRECISE"
            @unknown default: precisionStr = "UNKNOWN"
            }

            let data: [String: Any] = [
                "value": microsValue,
                "currencyCode": adValue.currencyCode,
                "precisionType": precisionStr,
            ]
            self?.fireEvent("onNativeAdPaid", data: data)
        }

        DispatchQueue.main.async { [weak self] in
            guard let self = self,
                let webView = self.plugin?.bridge?.webView,
                let webViewContainer = webView.superview
            else { return }

            self.adContainer?.removeFromSuperview()
            self.currentNativeAd = nil

            self.currentNativeAd = nativeAd

            let frameworkBundle = Bundle(for: NativeExecutor.self)

            let spmBundleName = "CapacitorAdmobNextgen_AdMobNextGenPlugin"

            let bundle: Bundle
            if let bundleURL = frameworkBundle.url(
                forResource: spmBundleName,
                withExtension: "bundle"
            ),
                let spmBundle = Bundle(url: bundleURL)
            {
                bundle = spmBundle  
            } else {
                bundle = frameworkBundle  
            }

            let nibName =
                self.isMediumTemplate
                ? "GADTMediumTemplateView" : "GADTSmallTemplateView"

            guard
                let nibObjects = bundle.loadNibNamed(
                    nibName,
                    owner: nil,
                    options: nil
                ),
                let templateView = nibObjects.first as? GADTTemplateView
            else {

                let errorMsg =
                    "Native Ads layout not found! Please ensure 'enableNativeAds' is true for iOS in your package.json and sync."
                print("[\(NativeExecutor.description())] \(errorMsg)")
                self.savedCall?.reject(errorMsg)
                return
            }

            templateView.nativeAd = nativeAd

            let adHeight: CGFloat = self.isMediumTemplate ? 350.0 : 120.0

            let container = UIView(
                frame: CGRect(x: 0, y: 0, width: self.adWidth, height: adHeight)
            )
            container.clipsToBounds = true

            templateView.translatesAutoresizingMaskIntoConstraints = false
            container.addSubview(templateView)

            NSLayoutConstraint.activate([
                templateView.leadingAnchor.constraint(
                    equalTo: container.leadingAnchor
                ),
                templateView.trailingAnchor.constraint(
                    equalTo: container.trailingAnchor
                ),
                templateView.topAnchor.constraint(equalTo: container.topAnchor),
                templateView.bottomAnchor.constraint(
                    equalTo: container.bottomAnchor
                ),
            ])

            container.layoutIfNeeded()

            self.adContainer = container
            webViewContainer.addSubview(container)

            self.updateAdPosition()
            self.syncScrollObserver(webView: webView)

            self.savedCall?.resolve([
                "width": Int(self.adWidth),
                "height": Int(adHeight),
            ])

            self.fireEvent(
                "onNativeAdLoaded",
                data: ["width": Int(self.adWidth), "height": Int(adHeight)]
            )
        }
    }

    public func adLoader(
        _ adLoader: AdLoader,
        didFailToReceiveAdWithError error: Error
    ) {
        let errorMsg = error.localizedDescription
        savedCall?.reject(errorMsg)
        fireEvent("onNativeAdFailedToLoad", data: ["message": errorMsg])
    }

    private func updateAdPosition() {
        guard let container = adContainer, let webView = plugin?.bridge?.webView
        else { return }

        let screenX = adAbsoluteX - webView.scrollView.contentOffset.x
        let screenY = adAbsoluteY - webView.scrollView.contentOffset.y

        container.transform = CGAffineTransform(
            translationX: screenX,
            y: screenY
        )
    }

    private func syncScrollObserver(webView: WKWebView) {
        scrollObserver?.invalidate()

        scrollObserver = webView.scrollView.observe(
            \.contentOffset,
            options: [.new]
        ) { [weak self] (_, _) in
            self?.updateAdPosition()
        }
    }

    public func hideNativeAd(_ call: CAPPluginCall) {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }

            self.scrollObserver?.invalidate()
            self.scrollObserver = nil

            self.adContainer?.removeFromSuperview()
            self.adContainer = nil
            self.currentNativeAd = nil

            call.resolve()
        }
    }

    public func onDestroy() {
        DispatchQueue.main.async { [weak self] in
            self?.scrollObserver?.invalidate()
            self?.scrollObserver = nil
            self?.adContainer?.removeFromSuperview()
            self?.adContainer = nil
            self?.currentNativeAd = nil
        }
    }

    public func nativeAdDidRecordImpression(_ nativeAd: NativeAd) {
        fireEvent("onNativeAdImpression", data: nil)
    }

    public func nativeAdDidRecordClick(_ nativeAd: NativeAd) {
        fireEvent("onNativeAdClicked", data: nil)
    }

    public func nativeAdWillPresentScreen(_ nativeAd: NativeAd) {
        fireEvent("onNativeAdShowed", data: nil)
    }

    public func nativeAdWillDismissScreen(_ nativeAd: NativeAd) {
    }

    public func nativeAdDidDismissScreen(_ nativeAd: NativeAd) {
        fireEvent("onNativeAdDismissed", data: nil)
    }

    private func fireEvent(_ eventName: String, data: [String: Any]?) {
        plugin?.notifyListeners(eventName, data: data ?? [:])
    }
}
