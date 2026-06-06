import Capacitor
import Foundation
import GoogleMobileAds

@objc
public class RewardedInterstitialExecutor: NSObject, FullScreenContentDelegate {

    private weak var plugin: AdMobNextGenPlugin?

    private var rewardedInterstitialAd: RewardedInterstitialAd?
    private var currentAdUnitId: String = ""

    private var lastLoadTime: TimeInterval = 0

    private var isRewardInterstitialEarned: Bool = false

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
                self.rewardedInterstitialAd =
                    try await RewardedInterstitialAd.load(
                        with: adUnitId,
                        request: request
                    )
                self.rewardedInterstitialAd?.fullScreenContentDelegate = self

                self.rewardedInterstitialAd?.paidEventHandler = {
                    [weak self] value in
                    guard let self = self else { return }
                    var ret = JSObject()
                    ret["adUnitId"] = self.currentAdUnitId
                    ret["valueMicros"] = value.value.stringValue
                    ret["currencyCode"] = value.currencyCode
                    ret["precisionType"] = value.precision.rawValue
                    self.plugin?.notifyListeners(
                        "onRewardedInterstitialAdPaid",
                        data: ret
                    )
                }

                self.lastLoadTime = Date().timeIntervalSince1970  

                var ret = JSObject()
                ret["adUnitId"] = self.currentAdUnitId
                plugin.notifyListeners(
                    "onRewardedInterstitialAdLoaded",
                    data: ret
                )

                call.resolve()

            } catch {
                self.rewardedInterstitialAd = nil

                var ret = JSObject()
                ret["error"] = error.localizedDescription
                plugin.notifyListeners(
                    "onRewardedInterstitialAdFailedToLoad",
                    data: ret
                )

                call.reject(error.localizedDescription)
            }
        }
    }

    @objc public func showAd(_ call: CAPPluginCall) {
        guard let plugin = self.plugin else { return }

        if self.rewardedInterstitialAd == nil {
            call.reject("The rewarded interstitial ad is not ready yet.")
            return
        }

        DispatchQueue.main.async {
            guard let viewController = plugin.bridge?.viewController else {
                call.reject("Root ViewController not found.")
                return
            }

            if let ad = self.rewardedInterstitialAd {
                self.isRewardInterstitialEarned = false

                ad.present(from: viewController) { [weak self] in
                    self?.isRewardInterstitialEarned = true
                    let reward = ad.adReward
                    var ret = JSObject()
                    ret["amount"] = reward.amount.doubleValue
                    ret["type"] = reward.type
                    self?.plugin?.notifyListeners(
                        "onRewardedInterstitialAdReward",
                        data: ret
                    )
                }
                call.resolve()
            }
        }
    }

    public func adWillPresentFullScreenContent(_ ad: FullScreenPresentingAd) {
        plugin?.notifyListeners("onRewardedInterstitialAdShowed", data: [:])
    }

    public func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        rewardedInterstitialAd = nil  
        if !self.isRewardInterstitialEarned {
            plugin?.notifyListeners("onRewardedInterstitialAdSkip", data: [:])
        }
        plugin?.notifyListeners("onRewardedInterstitialAdDismissed", data: [:])
    }

    public func ad(
        _ ad: FullScreenPresentingAd,
        didFailToPresentFullScreenContentWithError error: Error
    ) {
        rewardedInterstitialAd = nil  
        var ret = JSObject()
        ret["error"] = error.localizedDescription
        plugin?.notifyListeners(
            "onRewardedInterstitialAdFailedToShow",
            data: ret
        )
    }

    public func adDidRecordImpression(_ ad: FullScreenPresentingAd) {
        plugin?.notifyListeners("onRewardedInterstitialAdImpression", data: [:])
    }

    public func adDidRecordClick(_ ad: FullScreenPresentingAd) {
        plugin?.notifyListeners("onRewardedInterstitialAdClicked", data: [:])
    }
}
