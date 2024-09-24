import Foundation

@objc public class PolarSdk: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
