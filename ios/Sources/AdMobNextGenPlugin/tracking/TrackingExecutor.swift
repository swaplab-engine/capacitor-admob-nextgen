import Foundation
import Capacitor
import AppTrackingTransparency

@objc public class TrackingExecutor: NSObject {
    
    private weak var plugin: CAPPlugin?
    
    init(plugin: CAPPlugin) {
        self.plugin = plugin
        super.init()
    }
    
    @objc func requestTrackingAuthorization(_ call: CAPPluginCall) {
        // ATT is only applicable for iOS 14.5 and above
        if #available(iOS 14.5, *) {
            DispatchQueue.main.async {
                ATTrackingManager.requestTrackingAuthorization { status in
                    var result = JSObject()
                    
                    switch status {
                    case .authorized:
                        result["status"] = "authorized"
                    case .denied:
                        result["status"] = "denied"
                    case .notDetermined:
                        result["status"] = "notDetermined"
                    case .restricted:
                        result["status"] = "restricted"
                    @unknown default:
                        result["status"] = "unknown"
                    }
                    
                    call.resolve(result)
                }
            }
        } else {
            // Pre-iOS 14.5 devices have tracking enabled by default
            var result = JSObject()
            result["status"] = "authorized"
            call.resolve(result)
        }
    }
}
