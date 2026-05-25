import Capacitor
import Foundation
import GoogleMobileAds

@objc public class AppOpenExecutor: NSObject, FullScreenContentDelegate {

    private weak var plugin: AdMobNextGenPlugin?

    private var appOpenAd: AppOpenAd?
    private var currentAdUnitId: String = ""

    private var isLoadingAd: Bool = false
    private var isShowingAd: Bool = false
    private var loadTime: Date?

    private var lastRequestTime: TimeInterval = 0

    init(plugin: AdMobNextGenPlugin) {
        self.plugin = plugin
        super.init()
    }

    private func wasLoadTimeLessThanNHoursAgo(numHours: Double) -> Bool {
        guard let loadTime = loadTime else { return false }
        let timeIntervalInSeconds = numHours * 3600.0
        return Date().timeIntervalSince(loadTime) < timeIntervalInSeconds
    }

    private func isAdAvailable() -> Bool {
        return appOpenAd != nil && wasLoadTimeLessThanNHoursAgo(numHours: 4.0)
    }

    @objc public func loadAd(_ call: CAPPluginCall) {
        guard let plugin = self.plugin else { return }

        if !plugin.isInitialized {
            call.reject(
                "Google Mobile Ads SDK has not been initialized. Please call initialize() first."
            )
            return
        }

        let adUnitId = call.getString("adUnitId", "")
        if adUnitId.isEmpty {
            call.reject("Ad Unit ID is required.")
            return
        }

        let retryOpt = call.getDouble("retryInterval")
        let minLoadInterval: TimeInterval =
            (retryOpt != nil) ? (retryOpt! / 1000.0) : 5.0  
        let currentTime = Date().timeIntervalSince1970

        if (currentTime - lastRequestTime) < minLoadInterval {
            call.reject(
                "Request too fast. Please wait \(minLoadInterval * 1000)ms to prevent invalid traffic."
            )
            return
        }

        if isLoadingAd || isAdAvailable() {
            call.reject("App open ad is already loading or available.")
            return
        }

        self.currentAdUnitId = adUnitId
        self.lastRequestTime = currentTime
        self.isLoadingAd = true

        let request = Request()

        Task { @MainActor in
            do {
                self.appOpenAd = try await AppOpenAd.load(
                    with: adUnitId,
                    request: request
                )
                self.appOpenAd?.fullScreenContentDelegate = self

                self.appOpenAd?.paidEventHandler = { [weak self] value in
                    guard let self = self else { return }
                    var ret = JSObject()
                    ret["adUnitId"] = self.currentAdUnitId
                    ret["valueMicros"] = value.value.stringValue  
                    ret["currencyCode"] = value.currencyCode

                    ret["precisionType"] = value.precision.rawValue
                    self.plugin?.notifyListeners("onAppOpenAdPaid", data: ret)
                }

                self.isLoadingAd = false
                self.loadTime = Date()

                var ret = JSObject()
                ret["adUnitId"] = self.currentAdUnitId
                plugin.notifyListeners("onAppOpenAdLoaded", data: ret)

                call.resolve()
            } catch {
                self.isLoadingAd = false
                self.appOpenAd = nil

                var ret = JSObject()
                ret["error"] = error.localizedDescription
                plugin.notifyListeners("onAppOpenAdFailedToLoad", data: ret)

                call.reject(error.localizedDescription)
            }
        }
    }

    @objc public func showAd(_ call: CAPPluginCall) {
        guard let plugin = self.plugin else { return }

        if isShowingAd {
            call.reject("App open ad is already showing.")
            return
        }

        if !isAdAvailable() {
            call.reject("App open ad is not ready or has expired.")
            return
        }

        DispatchQueue.main.async {
            guard let viewController = plugin.bridge?.viewController else {
                call.reject("Root ViewController not found.")
                return
            }

            if let ad = self.appOpenAd {
                self.isShowingAd = true
                ad.present(from: viewController)
                call.resolve()
            }
        }
    }

    public func adWillPresentFullScreenContent(_ ad: FullScreenPresentingAd) {
        plugin?.notifyListeners("onAppOpenAdShowed", data: [:])
    }

    public func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        appOpenAd = nil  
        isShowingAd = false
        plugin?.notifyListeners("onAppOpenAdDismissed", data: [:])
    }

    public func ad(
        _ ad: FullScreenPresentingAd,
        didFailToPresentFullScreenContentWithError error: Error
    ) {
        appOpenAd = nil  
        isShowingAd = false

        var ret = JSObject()
        ret["error"] = error.localizedDescription
        plugin?.notifyListeners("onAppOpenAdFailedToShow", data: ret)
    }

    public func adDidRecordImpression(_ ad: FullScreenPresentingAd) {
        plugin?.notifyListeners("onAppOpenAdImpression", data: [:])
    }

    public func adDidRecordClick(_ ad: FullScreenPresentingAd) {
        plugin?.notifyListeners("onAppOpenAdClicked", data: [:])
    }

}
