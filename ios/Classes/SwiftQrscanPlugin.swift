import Flutter
import UIKit

public class SwiftQrscanPlugin: NSObject, FlutterPlugin {
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "qr_scan", binaryMessenger: registrar.messenger())
        let instance = SwiftQrscanPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if call.method == "scan" {
            //扫描二维码
            print(bundle.bundlePath)
            let sb = UIStoryboard(name: "QRCode", bundle: bundle)
            let vc = sb.instantiateViewController(withIdentifier: "QRCodeViewController") as! QRCodeViewController
            vc.modalPresentationStyle = .fullScreen
            vc.closure = { string in
                result(string)
            }
            viewControllerWithWindow(nil)?.present(vc, animated: true, completion: nil)
        } else {
            result("iOS " + UIDevice.current.systemVersion)
        }
    }
    
    func viewControllerWithWindow(_ window: UIWindow?) -> UIViewController? {
        var windowToUse = window
        if windowToUse == nil {
            for window in UIApplication.shared.windows {
                if window.isKeyWindow {
                    windowToUse = window
                    break
                }
            }
        }
        var topController = windowToUse?.rootViewController
        while topController?.presentedViewController != nil {
            topController = topController?.presentedViewController
        }
        return topController
    }
    
    lazy var bundle: Bundle = {
        guard let url = Bundle(for: Self.self).url(forResource: "qrscan", withExtension: "bundle"), let bundle = Bundle(url: url) else {
            return Bundle.main
        }
        return bundle
    }()
}
