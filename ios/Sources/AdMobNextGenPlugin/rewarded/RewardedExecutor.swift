import Capacitor
import Foundation
import GoogleMobileAds

@objc public class RewardedExecutor: NSObject, FullScreenContentDelegate {

    private weak var plugin: AdMobNextGenPlugin?

    private var rewardedAd: RewardedAd?
    private var currentAdUnitId: String = ""

    private var lastLoadTime: TimeInterval = 0

    private var isRewardEarned: Bool = false

    init(plugin: AdMobNextGenPlugin) {
        self.plugin = plugin
        super.init()
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

        if self.rewardedAd != nil && self.currentAdUnitId == adUnitId {

            var ret = JSObject()
            ret["adUnitId"] = self.currentAdUnitId
            ret["message"] = "Ad already loaded (Cached)"
            plugin.notifyListeners("onRewardedAdLoaded", data: ret)
            call.resolve()
            return
        }

        let retryOpt = call.getDouble("retryInterval")
        let minLoadInterval: TimeInterval =
            (retryOpt != nil) ? (retryOpt! / 1000.0) : 5.0  
        let currentTime = Date().timeIntervalSince1970

        if (currentTime - lastLoadTime) < minLoadInterval {
            call.reject(
                "Request too fast. Please wait \(minLoadInterval * 1000)ms to prevent invalid traffic."
            )
            return
        }

        self.currentAdUnitId = adUnitId

        let request = Request()

        Task { @MainActor in
            do {

                let ad = try await RewardedAd.load(
                    with: adUnitId,
                    request: request
                )

                self.rewardedAd = ad
                self.rewardedAd?.fullScreenContentDelegate = self

                self.rewardedAd?.paidEventHandler = { [weak self] value in
                    guard let self = self else { return }
                    var ret = JSObject()
                    ret["adUnitId"] = self.currentAdUnitId
                    ret["valueMicros"] = value.value.stringValue
                    ret["currencyCode"] = value.currencyCode
                    ret["precisionType"] = value.precision.rawValue
                    self.plugin?.notifyListeners("onRewardedAdPaid", data: ret)
                }

                self.lastLoadTime = Date().timeIntervalSince1970  

                var ret = JSObject()
                ret["adUnitId"] = self.currentAdUnitId
                plugin.notifyListeners("onRewardedAdLoaded", data: ret)

                call.resolve()

            } catch {

                var ret = JSObject()
                ret["error"] = error.localizedDescription
                plugin.notifyListeners("onRewardedAdFailedToLoad", data: ret)

                call.reject(error.localizedDescription)
            }
        }
    }

    @objc public func showAd(_ call: CAPPluginCall) {
        guard let plugin = self.plugin else { return }

        if self.rewardedAd == nil {
            call.reject("The rewarded ad is not ready yet.")
            return
        }

        DispatchQueue.main.async {
            guard let viewController = plugin.bridge?.viewController else {
                call.reject("Root ViewController not found.")
                return
            }

            if let ad = self.rewardedAd {
                self.isRewardEarned = false

                ad.present(from: viewController) { [weak self] in
                    self?.isRewardEarned = true
                    let reward = ad.adReward
                    var ret = JSObject()
                    ret["amount"] = reward.amount.doubleValue  
                    ret["type"] = reward.type
                    self?.plugin?.notifyListeners(
                        "onRewardedAdReward",
                        data: ret
                    )
                }
                call.resolve()
            }
        }
    }

    public func adWillPresentFullScreenContent(_ ad: FullScreenPresentingAd) {
        plugin?.notifyListeners("onRewardedAdShowed", data: [:])
    }

    public func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        rewardedAd = nil  

        if !self.isRewardEarned {
            plugin?.notifyListeners("onRewardedAdSkip", data: [:])
        }
        plugin?.notifyListeners("onRewardedAdDismissed", data: [:])
    }

    public func ad(
        _ ad: FullScreenPresentingAd,
        didFailToPresentFullScreenContentWithError error: Error
    ) {
        rewardedAd = nil  
        var ret = JSObject()
        ret["error"] = error.localizedDescription
        plugin?.notifyListeners("onRewardedAdFailedToShow", data: ret)
    }

    public func adDidRecordImpression(_ ad: FullScreenPresentingAd) {
        plugin?.notifyListeners("onRewardedAdImpression", data: [:])
    }

    public func adDidRecordClick(_ ad: FullScreenPresentingAd) {
        plugin?.notifyListeners("onRewardedAdClicked", data: [:])
    }
}
