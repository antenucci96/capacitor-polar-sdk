// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorPolarSdk",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "CapacitorPolarSdk",
            targets: ["PolarSdkPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
            .package(name: "PolarBleSdk", url: "https://github.com/polarofficial/polar-ble-sdk.git", .upToNextMajor(from: "5.0.0"))

    ],
    targets: [
        .target(
            name: "PolarSdkPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/PolarSdkPlugin"),
        .testTarget(
            name: "PolarSdkPluginTests",
            dependencies: ["PolarSdkPlugin"],
            path: "ios/Tests/PolarSdkPluginTests")
    ]
)