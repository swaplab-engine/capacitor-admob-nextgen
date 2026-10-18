const fs = require('fs');
const path = require('path');

console.log("-----------------------------------------");
console.log("AdMob Next-Gen: iOS Scene Lifecycle Migration (Capacitor 8.5+)");
console.log("-----------------------------------------");

const projectRoot = process.cwd();
const iosAppPath = path.join(projectRoot, 'ios', 'App', 'App');
const infoPlistPath = path.join(iosAppPath, 'Info.plist');
const appDelegatePath = path.join(iosAppPath, 'AppDelegate.swift');
const sceneDelegatePath = path.join(iosAppPath, 'SceneDelegate.swift');

if (!fs.existsSync(iosAppPath)) {
    console.log('  [Skip] iOS platform directory not found.');
    process.exit(0);
}

// =================================================================
// 1. UPDATE INFO.PLIST (Inject UIApplicationSceneManifest)
// =================================================================
if (fs.existsSync(infoPlistPath)) {
    let plistContent = fs.readFileSync(infoPlistPath, 'utf8');
    if (!plistContent.includes('<key>UIApplicationSceneManifest</key>')) {
        const sceneManifestXML = `	<key>UIApplicationSceneManifest</key>
	<dict>
		<key>UIApplicationSupportsMultipleScenes</key>
		<true/>
		<key>UISceneConfigurations</key>
		<dict>
			<key>UIWindowSceneSessionRoleApplication</key>
			<array>
				<dict>
					<key>UISceneConfigurationName</key>
					<string>Default Configuration</string>
					<key>UISceneDelegateClassName</key>
					<string>$(PRODUCT_MODULE_NAME).SceneDelegate</string>
				</dict>
			</array>
		</dict>
	</dict>`;
        
        plistContent = plistContent.replace(/<\/dict>\n*<\/plist>/, `${sceneManifestXML}\n</dict>\n</plist>`);
        fs.writeFileSync(infoPlistPath, plistContent, 'utf8');
        console.log('  [Injected] UIApplicationSceneManifest added to Info.plist.');
    } else {
        console.log('  [Skip] UIApplicationSceneManifest already exists in Info.plist.');
    }
}

// =================================================================
// 2. PATCH APPDELEGATE.SWIFT (Inject Lifecycle & Proxy)
// =================================================================
if (fs.existsSync(appDelegatePath)) {
    let appDelegateContent = fs.readFileSync(appDelegatePath, 'utf8');
    let isModified = false;

    // A. Inject UISceneSession function into AppDelegate
    if (!appDelegateContent.includes('configurationForConnecting connectingSceneSession')) {
        const delegateMethods = `
    // MARK: UISceneSession Lifecycle (Injected by AdMob Next-Gen)
    @available(iOS 13.0, *)
    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        let config = UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
        config.delegateClass = SceneDelegate.self
        return config
    }
`;      
        appDelegateContent = appDelegateContent.replace(/(})(\s*)$/, `${delegateMethods}\n$1$2`);
        isModified = true;
        console.log('  [Patched] UISceneSession lifecycle methods added to AppDelegate.swift.');
    }

    // B. Check for developer's manual SceneDelegate.swift
    if (fs.existsSync(sceneDelegatePath)) {
        console.log('  [Skip] Physical SceneDelegate.swift found. Deferring to manual configuration.');
    } else {
        // Inject SceneDelegate class below AppDelegate
        if (!appDelegateContent.includes('class SceneDelegate')) {
            const sceneDelegateClass = `
// MARK: - SceneDelegate (Injected by AdMob Next-Gen for Capacitor 8.5+)
@available(iOS 13.0, *)
class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        guard let windowScene = scene as? UIWindowScene else { return }
        
        window = UIWindow(windowScene: windowScene)
        window?.rootViewController = CAPBridgeViewController()
        window?.makeKeyAndVisible()
        
        SceneDelegateProxy.shared.scene(scene, willConnectTo: session, options: connectionOptions)
    }
    
    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
        SceneDelegateProxy.shared.scene(scene, openURLContexts: URLContexts)
    }
    
    func scene(_ scene: UIScene, continue userActivity: NSUserActivity) {
        SceneDelegateProxy.shared.scene(scene, continue: userActivity)
    }
}
`;
            appDelegateContent += sceneDelegateClass;
            isModified = true;
            console.log('  [Injected] SceneDelegate class appended to AppDelegate.swift.');
        }
    }

    if (isModified) {
        fs.writeFileSync(appDelegatePath, appDelegateContent, 'utf8');
    } else {
        console.log('  [Skip] AppDelegate.swift already contains the latest Scene configuration.');
    }
} else {
    console.log('  [Error] AppDelegate.swift not found.');
}

console.log("iOS Scene Migration Check Completed.\n");