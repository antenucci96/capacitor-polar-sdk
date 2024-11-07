import Foundation
import Capacitor
import PolarBleSdk
import CoreBluetooth

@objc(PolarSdkPlugin)
public class PolarSdkPlugin: CAPPlugin, PolarBleApiObserver, PolarBleApiPowerStateObserver, PolarBleApiDeviceInfoObserver, PolarBleApiBatteryLevelObserver, PolarBleApiDeviceTimeObserver, PolarBleApiEcgObserver, PolarBleApiHrObserver {
    private let TAG = "HRActivity"
    private var api: PolarBleApi!
    private var deviceId: String = ""
    private var hrDisposable: Disposable?
    private var ecgDisposable: Disposable?
    private var autoConnectDisposable: Disposable?
    private var disInfoDeferred: CompletableDeferred<DisInfo>?

    override public func load() {
        api = PolarBleApiDefaultImpl.polarImplementation(DispatchQueue.main, features: [
            .featureHr,
            .featureSdkMode,
            .featureBatteryInfo,
            .featureH10ExerciseRecording,
            .featureOfflineRecording,
            .featureOnlineStreaming,
            .featureDeviceTimeSetup,
            .featureDeviceInfo,
            .featureLedAnimation
        ])
        api.observer = self
        api.powerStateObserver = self
        api.deviceInfoObserver = self
        api.batteryLevelObserver = self
        api.deviceTimeObserver = self
        api.ecgObserver = self
        api.hrObserver = self
    }

    @objc func connectPolar(_ call: CAPPluginCall) {
        // Richiede permessi per Bluetooth
        requestBluetoothPermission { granted in
            if granted {
                self.disInfoDeferred = CompletableDeferred<DisInfo>()
                self.completeConnection(call)
            } else {
                call.reject("permissions.notGranted")
            }
        }
    }

    private func requestBluetoothPermission(completion: @escaping (Bool) -> Void) {
        // Gestione permessi per iOS 13+
        if #available(iOS 13.1, *) {
            let manager = CBManager.authorization
            if manager == .allowedAlways || manager == .restricted {
                completion(true)
            } else {
                completion(false)
            }
        } else {
            completion(true)
        }
    }

    private func completeConnection(_ call: CAPPluginCall) {
        if !api.isBlePowerOn {
            call.reject("bluetooth.notEnabled")
            return
        }

        autoConnectDisposable = api.autoConnectToDevice(-60, deviceType: "H10")
            .subscribe(
                onNext: { [weak self] _ in
                    self?.disInfoDeferred?.await { disInfo in
                        let result = JSObject()
                        result["value"] = true
                        call.resolve(result)
                    }
                },
                onError: { error in
                    call.reject("connection.timedOut")
                }
            )
    }

    public func powerStateChanged(_ isPowered: Bool) {
        CAPLog.print("\(TAG): BLE Power state changed: \(isPowered)")
    }

    public func deviceConnected(_ deviceId: String) {
        CAPLog.print("\(TAG): Connected to device \(deviceId)")
        self.deviceId = deviceId
    }

    public func deviceDisconnected(_ deviceId: String) {
        CAPLog.print("\(TAG): Disconnected from device \(deviceId)")
        notifyListeners("disconnected", data: [:])
    }

    public func batteryLevelReceived(_ deviceId: String, level: UInt) {
        CAPLog.print("\(TAG): Battery level \(level)")
    }

    public func bleSdkFeatureReady(_ deviceId: String, feature: PolarBleSdkFeature) {
        CAPLog.print("\(TAG): Feature ready \(feature)")
    }

    @objc func streamHR(_ call: CAPPluginCall) {
        guard hrDisposable == nil else {
            call.reject("HR stream already running")
            return
        }

        hrDisposable = api.startHrStreaming(deviceId)
            .subscribe(
                onNext: { hrData in
                    let data = JSObject()
                    data["bpm"] = hrData.hr
                    data["rrs"] = hrData.rrsMs
                    data["timestamp"] = Date().timeIntervalSince1970
                    self.notifyListeners("hrData", data: data)
                },
                onError: { error in
                    call.reject("stream.hr.failed")
                },
                onCompleted: {
                    call.resolve(["value": true])
                }
            )
    }

    @objc func streamEcg(_ call: CAPPluginCall) {
        setTime(deviceId: deviceId)

        ecgDisposable = api.requestEcgSettings(deviceId)
            .flatMap { settings in
                self.api.startEcgStreaming(self.deviceId, settings: settings)
            }
            .subscribe(
                onNext: { ecgData in
                    let data = JSObject()
                    data["yV"] = ecgData.voltage
                    data["timestamp"] = ecgData.timeStamp
                    self.notifyListeners("ecgData", data: data)
                },
                onError: { error in
                    call.reject("stream.ecg.failed")
                },
                onCompleted: {
                    call.resolve(["value": true])
                }
            )
    }

    private func setTime(deviceId: String) {
        let date = Date()
        api.setLocalTime(deviceId, date)
            .subscribe(
                onNext: { _ in
                    CAPLog.print("\(TAG): Time set to device")
                },
                onError: { error in
                    CAPLog.print("\(TAG): Set time failed \(error)")
                }
            )
    }

    @objc func stopHR(_ call: CAPPluginCall) {
        hrDisposable?.dispose()
        hrDisposable = nil
        call.resolve(["value": true])
    }

    @objc func stopEcg(_ call: CAPPluginCall) {
        ecgDisposable?.dispose()
        ecgDisposable = nil
        call.resolve(["value": true])
    }

    @objc func disconnectPolar(_ call: CAPPluginCall) {
        api.disconnectFromDevice(deviceId)
        call.resolve(["value": true])
    }

    public func blePowerStateChanged(_ isPowered: Bool) {
        CAPLog.print("\(TAG): BLE power state: \(isPowered)")
    }

    override public func handleOnDestroy() {
        hrDisposable?.dispose()
        ecgDisposable?.dispose()
    }
}

