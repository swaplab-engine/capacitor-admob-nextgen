// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorAdmobNextgen",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "CapacitorAdmobNextgen",
            targets: ["AdMobNextGenPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0"),
        .package(url: "https://github.com/googleads/swift-package-manager-google-mobile-ads.git", exact: "13.3.0"),
        .package(url: "https://github.com/googleads/swift-package-manager-google-user-messaging-platform.git", exact: "3.1.0"),

        // ADMOB_MEDIATION_SPM_DEPENDENCIES_START
        // ADMOB_MEDIATION_SPM_DEPENDENCIES_END
    ],
    targets: [
        .target(
            name: "AdMobNextGenPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm"),
                .product(name: "GoogleMobileAds", package: "swift-package-manager-google-mobile-ads"),
                .product(name: "GoogleUserMessagingPlatform", package: "swift-package-manager-google-user-messaging-platform"),

                // ADMOB_MEDIATION_SPM_TARGETS_START
                // ADMOB_MEDIATION_SPM_TARGETS_END
            ],
            path: "ios/Sources/AdMobNextGenPlugin",
            resources: [
                // ADMOB_NATIVE_RESOURCES_START
                // ADMOB_NATIVE_RESOURCES_END
            ]
          ),
        .testTarget(
            name: "AdMobNextGenPluginTests",
            dependencies: ["AdMobNextGenPlugin"],
            path: "ios/Tests/AdMobNextGenPluginTests")
    ]
)
