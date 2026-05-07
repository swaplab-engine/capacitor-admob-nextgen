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
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "AdMobNextGenPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/AdMobNextGenPlugin"),
        .testTarget(
            name: "AdMobNextGenPluginTests",
            dependencies: ["AdMobNextGenPlugin"],
            path: "ios/Tests/AdMobNextGenPluginTests")
    ]
)