import Foundation
import Capacitor
import GoogleMobileAds
import GoogleMobileAds_Private

@objc public class PictureInPictureExecutor: NSObject, PictureInPictureAdDelegate {
    private weak var plugin: AdMobNextGenPlugin?
    private var pipAd: PictureInPictureAd?
    private var pipOptions: PictureInPictureAdOptions?

    private var lastLoadTime: TimeInterval = 0

    init(plugin: AdMobNextGenPlugin) {
        self.plugin = plugin
        super.init()
    }

    @objc public func loadAd(_ call: CAPPluginCall) {
        guard let plugin = self.plugin else { return }
        if !plugin.isInitialized {
            call.reject("Google Mobile Ads SDK has not been initialized. Please call initialize() first.")
            return
        }

        let adUnitId = call.getString("adUnitId", "")
        if adUnitId.isEmpty {
            call.reject("adUnitId is required.")
            return
        }

        let isAutoShow = call.getBool("isAutoShow", false)
        let retryOpt = call.getDouble("retryInterval")
        let minLoadInterval: TimeInterval = (retryOpt != nil) ? (retryOpt! / 1000.0) : 5.0

        let positionStr = call.getString("position", "DEFAULT")
        var position: PictureInPictureAdPosition = .default
        switch positionStr {
        case "TOP_LEFT": position = .topLeft
        case "TOP_RIGHT": position = .topRight
        case "BOTTOM_LEFT": position = .bottomLeft
        case "BOTTOM_RIGHT": position = .bottomRight
        default: position = .default
        }

        let scopeStr = call.getString("presentationScope", "SCREEN")
        var scope: PictureInPictureAdPresentationScope = .screen
        if scopeStr == "APPLICATION" {
            scope = .application
        }

        let options = PictureInPictureAdOptions()
        options.position = position
        options.presentationScope = scope
        self.pipOptions = options

        if let existingAd = self.pipAd {
            if isAutoShow {
                DispatchQueue.main.async { [weak self] in
                    guard let self = self else { return }
                    existingAd.show(with: self.pipOptions!)
                }
            }
            call.resolve()
            return
        }

        let currentTime = Date().timeIntervalSince1970
        if (currentTime - lastLoadTime) < minLoadInterval {
            call.reject("Request too fast. Please wait \(minLoadInterval * 1000)ms to prevent invalid traffic.")
            return
        }

        self.lastLoadTime = currentTime

        Task { @MainActor [weak self] in
            guard let self = self else { return }

            do {
                let request = Request()
                let ad = try await PictureInPictureAd.load(with: adUnitId, request: request)

                self.pipAd?.hide()
                self.pipAd = ad
                self.pipAd?.delegate = self

                self.pipAd?.paidEventHandler = { [weak self] value in
                    guard let self = self else { return }
                    var ret = JSObject()
                    ret["adUnitId"] = adUnitId
                    ret["valueMicros"] = value.value.stringValue
                    ret["currencyCode"] = value.currencyCode
                    ret["precisionType"] = value.precision.rawValue
                    self.plugin?.notifyListeners("onPipAdPaid", data: ret)
                }

                self.lastLoadTime = Date().timeIntervalSince1970

                var ret = JSObject()
                ret["adUnitId"] = adUnitId
                self.plugin?.notifyListeners("onPipAdLoaded", data: ret)

                if isAutoShow {
                    self.pipAd?.show(with: self.pipOptions!)
                }

                call.resolve()

            } catch {
                var ret = JSObject()
                ret["error"] = error.localizedDescription
                self.plugin?.notifyListeners("onPipAdFailedToLoad", data: ret)
                call.reject(error.localizedDescription)
            }
        }
    }

    @objc public func showAd(_ call: CAPPluginCall) {
        if self.pipAd == nil {
            call.reject("The Picture-in-Picture ad is not ready yet.")
            return
        }

        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }
            self.pipAd?.show(with: self.pipOptions!)
            call.resolve()
        }
    }

    @objc public func hideAd(_ call: CAPPluginCall) {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }
            self.pipAd?.hide()
            call.resolve()
        }
    }

    public func destroyAd() {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }
            self.pipAd?.hide()
            self.pipAd = nil
        }
    }

    public func pictureInPictureAdDidShow(_ pictureInPictureAd: PictureInPictureAd) {
        plugin?.notifyListeners("onPipAdShown", data: [:])
    }

    public func pictureInPictureAdDidHide(_ pictureInPictureAd: PictureInPictureAd) {
        plugin?.notifyListeners("onPipAdHidden", data: [:])
    }

    public func pictureInPictureAdDidFailToShow(_ pictureInPictureAd: PictureInPictureAd, error: Error) {
        var ret = JSObject()
        ret["error"] = error.localizedDescription
        plugin?.notifyListeners("onPipAdFailedToShow", data: ret)
    }

    public func pictureInPictureAdDidRecordImpression(_ pictureInPictureAd: PictureInPictureAd) {
        plugin?.notifyListeners("onPipAdImpression", data: [:])
    }

    public func pictureInPictureAdDidRecordClick(_ pictureInPictureAd: PictureInPictureAd) {
        plugin?.notifyListeners("onPipAdClicked", data: [:])
    }

    public func pictureInPictureAdWillPresentScreen(_ pictureInPictureAd: PictureInPictureAd) {
        plugin?.notifyListeners("onPipAdShowed", data: [:])
    }

    public func pictureInPictureAdDidDismissScreen(_ pictureInPictureAd: PictureInPictureAd) {
        plugin?.notifyListeners("onPipAdDismissed", data: [:])
    }
}
