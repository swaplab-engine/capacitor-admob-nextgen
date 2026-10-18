import Capacitor
import Foundation
import GoogleMobileAds

@objc public class BannerExecutor: NSObject, BannerViewDelegate {

    private weak var plugin: AdMobNextGenPlugin?

    private var bannerView: BannerView?
    private var pendingBannerView: BannerView?

    private var lastAdUnitId: String = ""
    private var lastSizeStr: String = ""
    private var currentPosition: String = "BOTTOM"
    private var lastPosition: String = "BOTTOM"
    private var lastAdSize: AdSize = AdSizeBanner
    private var lastOrientation: UIDeviceOrientation = .unknown

    private var isBannerVisible: Bool = false
    private var isLoading: Bool = false
    private var isOverlapping: Bool = true
    private var isAutoShow: Bool = true
    private var isCollapsible: Bool = false

    private var activeBannerHeight: CGFloat = 0

    private var lastLoadTime: TimeInterval = 0
    private var minLoadInterval: TimeInterval = 5.0

    private var boundsObserverView: BoundsObserverView?
    private var lastObservedWidth: CGFloat = 0

    init(plugin: AdMobNextGenPlugin) {
        self.plugin = plugin
        super.init()
    }

    deinit {
        DispatchQueue.main.async {
            self.boundsObserverView?.removeFromSuperview()
            self.boundsObserverView = nil
        }
    }

    private func startObservingBounds() {

        guard boundsObserverView == nil, let rootVC = self.plugin?.bridge?.viewController else { return }

        self.lastObservedWidth = self.getSafeViewWidth()

        let observerView = BoundsObserverView()
        observerView.isUserInteractionEnabled = false 
        observerView.backgroundColor = .clear
        observerView.translatesAutoresizingMaskIntoConstraints = false

        rootVC.view.addSubview(observerView)
        rootVC.view.sendSubviewToBack(observerView) 

        NSLayoutConstraint.activate([
            observerView.leadingAnchor.constraint(equalTo: rootVC.view.leadingAnchor),
            observerView.trailingAnchor.constraint(equalTo: rootVC.view.trailingAnchor),
            observerView.topAnchor.constraint(equalTo: rootVC.view.topAnchor),
            observerView.bottomAnchor.constraint(equalTo: rootVC.view.bottomAnchor)
        ])

        observerView.onBoundsChanged = { [weak self] _ in
            guard let self = self else { return }

            DispatchQueue.main.async {
                self.layoutViews() 

                let currentWidth = self.getSafeViewWidth()

                if abs(self.lastObservedWidth - currentWidth) > 1.0 {
                    self.lastObservedWidth = currentWidth

                    if !self.lastAdUnitId.isEmpty {
                        var ret = JSObject()
                        ret["adUnitId"] = self.lastAdUnitId
                        ret["orientation"] = "RESIZE" 
                        self.plugin?.notifyListeners("onBannerOrientationChanged", data: ret)
                    }
                }
            }
        }

        self.boundsObserverView = observerView
    }

    private func getSafeViewWidth() -> CGFloat {

        if let window = self.getKeyWindow() {
            var frame = window.bounds
            if #available(iOS 11.0, *) {
                frame = frame.inset(by: window.safeAreaInsets)
            }
            return frame.size.width
        }

        guard let rootViewController = self.plugin?.bridge?.viewController else {
            return UIScreen.main.bounds.width
        }
        var frame = rootViewController.view.frame
        if #available(iOS 11.0, *) {
            frame = frame.inset(by: rootViewController.view.safeAreaInsets)
        }
        return frame.size.width
    }

    private func getKeyWindow() -> UIWindow? {
        if #available(iOS 13.0, *) {
            return UIApplication.shared.connectedScenes
                .compactMap { $0 as? UIWindowScene }
                .filter { $0.activationState == .foregroundActive }
                .first?.windows
                .first { $0.isKeyWindow }
        } else {
            return UIApplication.shared.keyWindow
        }
    }

    @objc public func createBanner(_ call: CAPPluginCall) {
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

        let requestedSize = call.getString("adSize", "ADAPTIVE").uppercased()
        let newPosition = call.getString("position", self.currentPosition)
            .uppercased()

        let newIsOverlapping = call.getBool("isOverlap", true)
        let newIsAutoShow = call.getBool("isAutoShow", true)
        let newIsCollapsible = call.getBool("isCollapsible", false)

        let retryOpt = call.getDouble("retryInterval")
        let newMinLoadInterval: TimeInterval =
            (retryOpt != nil) ? (retryOpt! / 1000.0) : 5.0

        DispatchQueue.main.async {

            self.startObservingBounds()

            let currentTime = Date().timeIntervalSince1970

            if self.isLoading {
                call.reject("Banner is currently loading")
                return
            }

            let isSameId = (self.lastAdUnitId == adUnitId)
            let isSameSize = (self.lastSizeStr == requestedSize)

            self.isOverlapping = newIsOverlapping
            self.isAutoShow = newIsAutoShow
            self.isCollapsible = newIsCollapsible
            self.minLoadInterval = newMinLoadInterval
            self.currentPosition = newPosition

            if self.bannerView != nil && isSameId && isSameSize {
                if self.isAutoShow {
                    self.showBannerInternal()
                    call.resolve(["status": "Banner Updated (Cached)"])
                } else {
                    self.hideBannerInternal()
                    call.resolve(["status": "Banner Hidden (Cached)"])
                }
                self.sendLoadedEvent(
                    adSize: self.lastAdSize,
                    isCollapsible: self.isCollapsible,
                    isRefresh: false
                )
                return
            }

            if (currentTime - self.lastLoadTime) < self.minLoadInterval {
                call.reject(
                    "Request too fast. Please wait \(self.minLoadInterval * 1000)ms to prevent invalid traffic."
                )
                return
            }

            self.lastPosition = self.currentPosition
            self.loadBanner(
                adUnitId: adUnitId,
                sizeStr: requestedSize,
                call: call
            )
        }
    }

    private func loadBanner(
        adUnitId: String,
        sizeStr: String,
        call: CAPPluginCall
    ) {

        self.isLoading = true
        self.lastLoadTime = Date().timeIntervalSince1970
        self.lastAdUnitId = adUnitId
        self.lastSizeStr = sizeStr

        guard let rootViewController = self.plugin?.bridge?.viewController
        else {
            call.reject("Root ViewController not found.")
            return
        }

        let adSize = self.getAdSize(sizeStr: sizeStr)
        self.lastAdSize = adSize

        let pendingView = BannerView(adSize: adSize)
        pendingView.translatesAutoresizingMaskIntoConstraints = true
        pendingView.adUnitID = adUnitId
        pendingView.rootViewController = rootViewController
        pendingView.delegate = self
        pendingView.isHidden = true  

        pendingView.paidEventHandler = { [weak self] value in
            guard let self = self else { return }
            var ret = JSObject()
            ret["adUnitId"] = self.lastAdUnitId
            ret["valueMicros"] = value.value.stringValue
            ret["currencyCode"] = value.currencyCode
            ret["precisionType"] = value.precision.rawValue
            self.plugin?.notifyListeners("onBannerAdPaid", data: ret)
        }

        self.pendingBannerView = pendingView
        rootViewController.view.addSubview(pendingView)

        let request = Request()

        if self.isCollapsible {
            let extras = Extras()
            let anchor = (self.currentPosition == "TOP") ? "top" : "bottom"
            extras.additionalParameters = ["collapsible": anchor]
            request.register(extras)
        }

        pendingView.load(request)
        call.resolve(["status": "Banner creation initiated."])
    }

    private func getAdSize(sizeStr: String) -> AdSize {

        let viewWidth = self.getSafeViewWidth()
        let size = sizeStr.uppercased()

        if size == "BANNER" { return AdSizeBanner }
        if size == "LARGE_BANNER" { return AdSizeLargeBanner }
        if size == "MEDIUM_RECTANGLE" { return AdSizeMediumRectangle }
        if size == "FULL_BANNER" { return AdSizeFullBanner }
        if size == "LEADERBOARD" { return AdSizeLeaderboard }
        if size == "FLUID" { return AdSizeFluid }

        if size == "LARGE_ANCHORED_ADAPTIVE" {
            return largeAnchoredAdaptiveBanner(width: viewWidth)
        }
        if size == "LARGE_PORTRAIT_ANCHORED_ADAPTIVE" {
            return largePortraitAnchoredAdaptiveBanner(width: viewWidth)
        }
        if size == "LARGE_LANDSCAPE_ANCHORED_ADAPTIVE" {
            return largeLandscapeAnchoredAdaptiveBanner(width: viewWidth)
        }

        if size == "CURRENT_ORIENTATION_INLINE_ADAPTIVE" {
            return currentOrientationInlineAdaptiveBanner(width: viewWidth)
        }
        if size == "PORTRAIT_INLINE_ADAPTIVE" {
            return portraitInlineAdaptiveBanner(width: viewWidth)
        }
        if size == "LANDSCAPE_INLINE_ADAPTIVE" {
            return landscapeInlineAdaptiveBanner(width: viewWidth)
        }

        return currentOrientationAnchoredAdaptiveBanner(width: viewWidth)
    }

    @objc private func handleOrientationChange() {
        let currentOrientation = UIDevice.current.orientation

        guard currentOrientation.isValidInterfaceOrientation,
            currentOrientation != self.lastOrientation
        else {
            return
        }

        self.lastOrientation = currentOrientation
        self.layoutViews()  

        if !self.lastAdUnitId.isEmpty {
            var ret = JSObject()
            ret["adUnitId"] = self.lastAdUnitId
            ret["orientation"] =
                currentOrientation.isLandscape ? "LANDSCAPE" : "PORTRAIT"
            self.plugin?.notifyListeners(
                "onBannerOrientationChanged",
                data: ret
            )
        }
    }

    @objc private func layoutViews() {
        DispatchQueue.main.async {

            guard let rootVC = self.plugin?.bridge?.viewController,
                  let webView = self.plugin?.bridge?.webView,
                  let window = self.getKeyWindow() 
            else { return }

            webView.translatesAutoresizingMaskIntoConstraints = true
            self.bannerView?.translatesAutoresizingMaskIntoConstraints = true

            var safeArea = window.safeAreaInsets

            let screenH = window.bounds.size.height
            let screenW = window.bounds.size.width

            if safeArea.bottom == 0 && screenH >= 812.0 {
                safeArea.bottom = 34.0
            }
            if safeArea.top == 0 && screenH >= 812.0 { safeArea.top = 44.0 }

            let fullScreenRect = CGRect(
                x: 0,
                y: 0,
                width: screenW,
                height: screenH
            )

            if self.isBannerVisible, let bannerView = self.bannerView,
                self.activeBannerHeight > 0
            {

                let intrinsicSize = bannerView.intrinsicContentSize
                let bH =
                    intrinsicSize.height > 0
                    ? intrinsicSize.height : self.activeBannerHeight
                let bW =
                    intrinsicSize.width > 0
                    ? intrinsicSize.width : bannerView.bounds.size.width
                let bX = (screenW - bW) / 2.0

                if self.currentPosition == "TOP" {
                    if !self.isOverlapping {
                        let totalTopOffset = safeArea.top + bH
                        var newWebFrame = fullScreenRect
                        newWebFrame.origin.y = totalTopOffset
                        newWebFrame.size.height = screenH - totalTopOffset
                        webView.frame = newWebFrame

                        let bY = -safeArea.top
                        bannerView.frame = CGRect(
                            x: bX,
                            y: bY,
                            width: bW,
                            height: bH
                        )
                    } else {
                        webView.frame = fullScreenRect
                        let bY = safeArea.top
                        bannerView.frame = CGRect(
                            x: bX,
                            y: bY,
                            width: bW,
                            height: bH
                        )
                    }
                    bannerView.isHidden = false

                } else {
                    let bY = screenH - safeArea.bottom - bH
                    bannerView.frame = CGRect(
                        x: bX,
                        y: bY,
                        width: bW,
                        height: bH
                    )
                    bannerView.isHidden = false

                    if !self.isOverlapping {
                        var newWebFrame = fullScreenRect
                        newWebFrame.size.height = bY
                        webView.frame = newWebFrame
                    } else {
                        webView.frame = fullScreenRect
                    }
                }
            } else {
                self.bannerView?.isHidden = true
                webView.frame = fullScreenRect
            }

            if self.isBannerVisible, let bannerView = self.bannerView {
                rootVC.view.bringSubviewToFront(bannerView)
            }
        }
    }

    private func showBannerInternal() {
        if self.bannerView != nil {
            self.isBannerVisible = true
            self.layoutViews()
        }
    }

    private func hideBannerInternal() {
        if self.bannerView != nil {
            self.isBannerVisible = false
            self.layoutViews()
        }
    }

    private func destroyBannerInternal() {

        if let pending = self.pendingBannerView {
            pending.removeFromSuperview()
            pending.delegate = nil
            self.pendingBannerView = nil
        }

        if let bannerView = self.bannerView {
            self.isBannerVisible = false
            self.activeBannerHeight = 0
            self.layoutViews()

            bannerView.removeFromSuperview()
            bannerView.delegate = nil
            self.bannerView = nil
        }
    }

    @objc public func showBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            if self.bannerView != nil {
                self.showBannerInternal()
                call.resolve()
            } else {
                call.reject("No banner loaded")
            }
        }
    }

    @objc public func hideBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.hideBannerInternal()
            call.resolve()
        }
    }

    @objc public func destroyBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.destroyBannerInternal()
            self.lastAdUnitId = ""
            self.lastSizeStr = ""
            call.resolve()
        }
    }

    private func sendLoadedEvent(
        adSize: AdSize,
        isCollapsible: Bool,
        isRefresh: Bool
    ) {
        var ret = JSObject()
        ret["adUnitId"] = self.lastAdUnitId
        ret["width"] = Double(adSize.size.width)
        ret["height"] = Double(adSize.size.height)
        ret["isCollapsible"] = isCollapsible

        if isRefresh {
            self.plugin?.notifyListeners("onBannerAdRefreshed", data: ret)
        } else {
            self.plugin?.notifyListeners("onBannerAdLoaded", data: ret)
        }
    }

    public func bannerViewDidReceiveAd(_ incomingBannerView: BannerView) {

        if incomingBannerView == self.pendingBannerView {
            self.isLoading = false

            self.bannerView?.removeFromSuperview()
            self.bannerView?.delegate = nil

            self.bannerView = self.pendingBannerView
            self.pendingBannerView = nil

            self.activeBannerHeight = incomingBannerView.adSize.size.height
            if self.activeBannerHeight == 0 {
                self.activeBannerHeight = self.lastAdSize.size.height
            }

            self.sendLoadedEvent(
                adSize: self.lastAdSize,
                isCollapsible: self.isCollapsible,
                isRefresh: false
            )

            if self.isAutoShow && !self.isBannerVisible {
                self.showBannerInternal()
            } else {
                self.layoutViews()
            }
        }

        else if incomingBannerView == self.bannerView {
            self.sendLoadedEvent(
                adSize: incomingBannerView.adSize,
                isCollapsible: self.isCollapsible,
                isRefresh: true
            )
            self.layoutViews()  
        }
    }

    public func bannerView(
        _ incomingBannerView: BannerView,
        didFailToReceiveAdWithError error: Error
    ) {

        if incomingBannerView == self.pendingBannerView {
            self.isLoading = false

            incomingBannerView.removeFromSuperview()
            incomingBannerView.delegate = nil
            self.pendingBannerView = nil

            var ret = JSObject()
            ret["adUnitId"] = self.lastAdUnitId
            ret["error"] = error.localizedDescription
            self.plugin?.notifyListeners("onBannerAdFailedToLoad", data: ret)
        }

        else if incomingBannerView == self.bannerView {
            var ret = JSObject()
            ret["adUnitId"] = self.lastAdUnitId
            ret["error"] = error.localizedDescription
            self.plugin?.notifyListeners("onBannerAdFailedToRefresh", data: ret)
        }
    }

    public func bannerViewDidRecordImpression(_ bannerView: BannerView) {
        var ret = JSObject()
        ret["adUnitId"] = self.lastAdUnitId
        self.plugin?.notifyListeners("onBannerAdImpression", data: ret)
    }

    public func bannerViewDidRecordClick(_ bannerView: BannerView) {
        var ret = JSObject()
        ret["adUnitId"] = self.lastAdUnitId
        self.plugin?.notifyListeners("onBannerAdClicked", data: ret)
    }

    public func bannerViewWillPresentScreen(_ bannerView: BannerView) {
        var ret = JSObject()
        ret["adUnitId"] = self.lastAdUnitId
        self.plugin?.notifyListeners("onBannerAdShowedFullScreen", data: ret)
    }

    public func bannerViewDidDismissScreen(_ bannerView: BannerView) {
        var ret = JSObject()
        ret["adUnitId"] = self.lastAdUnitId
        self.plugin?.notifyListeners("onBannerAdDismissedFullScreen", data: ret)
    }
}

class BoundsObserverView: UIView {
    var onBoundsChanged: ((CGRect) -> Void)?

    override func layoutSubviews() {
        super.layoutSubviews()

        onBoundsChanged?(self.bounds)
    }
}
