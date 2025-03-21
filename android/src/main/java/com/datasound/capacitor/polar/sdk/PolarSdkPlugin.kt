package com.datasound.capacitor.polar.sdk

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.getcapacitor.JSObject
import com.getcapacitor.PermissionState
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback
import com.polar.androidcommunications.api.ble.model.DisInfo
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarAccelerometerData
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarEcgData
import com.polar.sdk.api.model.PolarHrData
import com.polar.sdk.api.model.PolarSensorSetting
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

const val ERROR_BLE_NOT_AVAILABLE = "bluetooth.notAvailable"
const val ERROR_BLUETOOTH_NOT_ENABLED = "bluetooth.notEnabled"
const val ERROR_PERMISSIONS_DENIED = "permissions.notGranted"
const val ERROR_CONNECTION_TIMED_OUT = "connection.timedOut"
const val ERROR_STREAM_ECG_FAILED = "stream.ecg.failed"
const val ERROR_STREAM_HR_FAILED = "stream.hr.failed"
const val ERROR_STREAM_ACC_FAILED = "stream.acc.failed"

@CapacitorPlugin(
    name = "PolarSdk",
    permissions = [
        Permission(
            strings = [
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ], alias = "ACCESS_COARSE_LOCATION"
        ),
        Permission(
            strings = [
                Manifest.permission.ACCESS_FINE_LOCATION,
            ], alias = "ACCESS_FINE_LOCATION"
        ),
        Permission(
            strings = [
                Manifest.permission.BLUETOOTH,
            ], alias = "BLUETOOTH"
        ),
        Permission(
            strings = [
                Manifest.permission.BLUETOOTH_ADMIN,
            ], alias = "BLUETOOTH_ADMIN"
        ),
        Permission(
            strings = [
                // Manifest.permission.BLUETOOTH_SCAN
                "android.permission.BLUETOOTH_SCAN",
            ], alias = "BLUETOOTH_SCAN"
        ),
        Permission(
            strings = [
                // Manifest.permission.BLUETOOTH_ADMIN
                "android.permission.BLUETOOTH_CONNECT",
            ], alias = "BLUETOOTH_CONNECT"
        ),
    ]
)
class PolarSdkPlugin : Plugin() {
    companion object {
        private const val TAG = "HRActivity"
    }

    private var hrDisposable: Disposable? = null
    private var autoConnectDisposable: Disposable? = null
    private var ecgDisposable: Disposable? = null
    private var accDisposable: Disposable? = null
    private var deviceId = ""
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var aliases: Array<String> = arrayOf()
    private var disInfoDeferred: CompletableDeferred<DisInfo>? = null

    private val api: PolarBleApi by lazy {
        // Notice all features are enabled
        PolarBleApiDefaultImpl.defaultImplementation(
            bridge.activity.applicationContext,
            setOf(
                PolarBleApi.PolarBleSdkFeature.FEATURE_HR,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_SDK_MODE,
                PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_H10_EXERCISE_RECORDING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_OFFLINE_RECORDING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_DEVICE_TIME_SETUP,
                PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_LED_ANIMATION
            )
        )
    }

    override fun load() {
        api.setApiLogger { str: String -> Log.d("SDK", str) }
        api.setApiCallback(object : PolarBleApiCallback() {
            override fun blePowerStateChanged(powered: Boolean) {
                Log.d(TAG, "BLE power: $powered")
            }

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "CONNECTED: ${polarDeviceInfo.deviceId}")
                deviceId = polarDeviceInfo.deviceId
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "CONNECTING: ${polarDeviceInfo.deviceId}")
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "DISCONNECTED: ${polarDeviceInfo.deviceId}")
                notifyListeners("disconnected", JSObject())
            }

            override fun disInformationReceived(identifier: String, disInfo: DisInfo) {
                Log.d(TAG, "DIS INFO: $disInfo")
                //Wait this callback to resolve connection
                disInfoDeferred?.complete(disInfo)
            }

            override fun batteryLevelReceived(identifier: String, level: Int) {
                Log.d(TAG, "BATTERY LEVEL: $level")
            }

            override fun bleSdkFeatureReady(
                identifier: String,
                feature: PolarBleApi.PolarBleSdkFeature
            ) {
                Log.d(TAG, "feature ready $feature")
            }

        })
    }


    @PluginMethod
    fun connectPolar(call: PluginCall) {
        aliases = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                "BLUETOOTH_SCAN",
                "BLUETOOTH_CONNECT",
            )
        } else {
            arrayOf(
                "ACCESS_COARSE_LOCATION",
                "ACCESS_FINE_LOCATION",
                "BLUETOOTH",
                "BLUETOOTH_ADMIN",
            )
        }
        requestPermissionForAliases(aliases, call, "checkPermission")
    }

    @PermissionCallback
    private fun checkPermission(call: PluginCall) {
        Log.i(TAG, "checkPermission")
        val granted: List<Boolean> = aliases.map { alias ->
            getPermissionState(alias) == PermissionState.GRANTED
        }
        // all have to be true
        if (granted.all { it }) {
            disInfoDeferred = CompletableDeferred()
            completeConnection(call)
        } else {
            call.reject(ERROR_PERMISSIONS_DENIED)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun completeConnection(call: PluginCall) {
        bluetoothAdapter =
            (activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        if (bluetoothAdapter == null) {
            call.reject(ERROR_BLE_NOT_AVAILABLE)
            return
        } else if (!bluetoothAdapter!!.isEnabled) {
            Log.i(TAG, "connectPolar: Bluetooth is not enabled")
            call.reject(ERROR_BLUETOOTH_NOT_ENABLED)
        }

        val timeoutHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (autoConnectDisposable != null) {
                autoConnectDisposable?.dispose()
                call.reject(ERROR_CONNECTION_TIMED_OUT)
            }
        }

        // try to connect to device for 10 seconds
        timeoutHandler.postDelayed(timeoutRunnable, 10000)

        if (autoConnectDisposable != null) {
            autoConnectDisposable?.dispose()
        }

        autoConnectDisposable = api.autoConnectToDevice(-60, "180D", "H10")
            .subscribe(
                {
                    Log.d(TAG, "Connection attempt started")
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    GlobalScope.launch {
                        try {
                            val disInfo =
                                disInfoDeferred?.await()  // Wait until disInfo is received
                            Log.i(TAG, "disInfo: $disInfo")
                            val result = JSObject()
                            result.put("value", true)
                            call.resolve(result)
                        } catch (e: Exception) {
                            call.reject("Connection failed")
                        }
                    }
                },
                { throwable: Throwable ->
                    Log.e(TAG, "Connection error: ${throwable.message}")
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    call.reject("Connection failed")
                }
            )
    }

    @PluginMethod
    fun streamHR(call: PluginCall) {
        val isDisposed = hrDisposable?.isDisposed ?: true
        if (isDisposed) {
            hrDisposable = api.startHrStreaming(deviceId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { hrData: PolarHrData ->
                        val result = JSObject()
                        result.put("value", true)
                        call.resolve(result)

                        for (sample in hrData.samples) {
                            Log.d(TAG, "HR bpm: ${sample.hr} rrs: ${sample.rrsMs}")
                            // Set timestamp
                            val currentTimestamp = System.currentTimeMillis()
                            // Send data to JS
                            val data = JSObject()
                            data.put("bpm", sample.hr)
                            data.put("rrs", sample.rrsMs)
                            data.put("timestamp", currentTimestamp)
                            notifyListeners("hrData", data)
                        }
                    },
                    { error: Throwable ->
                        Log.e(TAG, "HR stream failed. Reason: $error")
                        call.reject(ERROR_STREAM_HR_FAILED)
                    },
                    {
                        Log.d(TAG, "HR stream complete")
                    }
                )
        } else {
            hrDisposable?.dispose()
            call.reject("HR stream stopped")
        }
    }

    @PluginMethod
    fun streamEcg(call: PluginCall) {
        setTime(deviceId)
        val isDisposed = ecgDisposable?.isDisposed ?: true
        if (isDisposed) {
            Log.i(TAG, "streamEcg: isDisposed")
            ecgDisposable = requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.ECG)
                .flatMap { settings: PolarSensorSetting ->
                    api.startEcgStreaming(deviceId, settings)
                }
                .subscribe(
                    { polarEcgData: PolarEcgData ->
                        val result = JSObject()
                        result.put("value", true)
                        call.resolve(result)
                        for (data in polarEcgData.samples) {
                            Log.d(TAG, "yV: ${data.voltage} timeStamp: ${data.timeStamp}")
                            val resData = JSObject()
                            resData.put("yV", data.voltage)
                            resData.put("timestamp", data.timeStamp)
                            notifyListeners("ecgData", resData)
                        }
                    },
                    { error: Throwable ->
                        Log.e(TAG, "ECG stream failed. Reason $error")
                        call.reject(ERROR_STREAM_ECG_FAILED)
                    },
                    {
                        Log.d(TAG, "ECG stream complete")
                    }
                )
        } else {
            //stops streaming if it is "running"
            ecgDisposable?.dispose()
            call.reject("error", "HCG stream stopped")
        }
    }

    @PluginMethod
    fun streamAcc(call: PluginCall) {
        val isDisposed = accDisposable?.isDisposed ?: true
        if (isDisposed) {
            accDisposable = requestStreamSettingsAcc(deviceId, PolarBleApi.PolarDeviceDataType.ACC)
                .flatMap { settings: PolarSensorSetting ->
                    api.startAccStreaming(deviceId, settings)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { polarAccelerometerData: PolarAccelerometerData ->
                        for (data in polarAccelerometerData.samples) {
                            Log.d(TAG, "ACC    x: ${data.x} y: ${data.y} z: ${data.z} timeStamp: ${data.timeStamp}")
                            val resData = JSObject()
                            resData.put("x", data.x)
                            resData.put("y", data.y)
                            resData.put("z", data.z)
                            resData.put("timestamp", data.timeStamp)
                            notifyListeners("accData", resData)
                        }
                    },
                    { error: Throwable ->
                        Log.e(TAG, "ACC stream failed. Reason $error")
                        call.reject(ERROR_STREAM_ACC_FAILED)
                    },
                    {
                        Log.d(TAG, "ACC stream complete")
                    }
                )
        } else {
            accDisposable?.dispose()
            call.reject("error", "ACC stream stopped")
        }
    }

    @PluginMethod
    fun stopHR(call: PluginCall) {
        hrDisposable?.dispose()
        val result = JSObject()
        result.put("value", true)
        call.resolve(result)
    }

    @PluginMethod
    fun stopEcg(call: PluginCall) {
        ecgDisposable?.dispose()
        val result = JSObject()
        result.put("value", true)
        call.resolve(result)
    }

    @PluginMethod
    fun stopAcc(call: PluginCall) {
        accDisposable?.dispose()
        val result = JSObject()
        result.put("value", true)
        call.resolve(result)
    }

    @PluginMethod
    fun disconnectPolar(call: PluginCall) {
        api.disconnectFromDevice(deviceId)
        val result = JSObject()
        result.put("value", true)
        call.resolve(result)
    }

    private fun setTime(deviceId: String) {
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("GMT")
        calendar.time = Date()
        api.setLocalTime(deviceId, calendar)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val timeSetString = "time ${calendar.time} set to device"
                    Log.d(TAG, timeSetString)
                },
                { error: Throwable -> Log.e(TAG, "set time failed: $error") }
            )
    }

    private fun requestStreamSettings(
        identifier: String,
        feature: PolarBleApi.PolarDeviceDataType
    ): Flowable<PolarSensorSetting> {
        Log.i(TAG, "requestStreamSettings: ")
        val availableSettings = api.requestStreamSettings(identifier, feature)
        val allSettings = api.requestFullStreamSettings(identifier, feature)
            .onErrorReturn { error: Throwable ->
                Log.w(
                    TAG,
                    "Full stream settings are not available for feature $feature. REASON: $error"
                )
                PolarSensorSetting(emptyMap())
            }

        return Single.zip(
            availableSettings,
            allSettings
        ) { available: PolarSensorSetting, all: PolarSensorSetting ->
            if (available.settings.isEmpty()) {
                throw Throwable("Settings are not available")
            } else {
                Log.d(TAG, "Feature " + feature + " available settings " + available.settings)
                Log.d(TAG, "Feature " + feature + " all settings " + all.settings)

                return@zip android.util.Pair(available, all)
            }
        }
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable()
            .flatMap { sensorSettings: android.util.Pair<PolarSensorSetting, PolarSensorSetting> ->
                Flowable.just(sensorSettings.first)
            }
    }


    private fun requestStreamSettingsAcc(
        identifier: String,
        feature: PolarBleApi.PolarDeviceDataType
    ): Flowable<PolarSensorSetting> {
        Log.i(TAG, "requestStreamSettings: ")

        val availableSettings = api.requestStreamSettings(identifier, feature)
        val allSettings = api.requestFullStreamSettings(identifier, feature)
            .onErrorReturn { error: Throwable ->
                Log.w(
                    TAG,
                    "Full stream settings are not available for feature $feature. REASON: $error"
                )
                PolarSensorSetting(emptyMap())
            }

        return Single.zip(
            availableSettings,
            allSettings
        ) { available: PolarSensorSetting, all: PolarSensorSetting ->
            if (available.settings.isEmpty()) {
                throw Throwable("Settings are not available")
            } else {
                Log.d(TAG, "Feature $feature available settings ${available.settings}")
                Log.d(TAG, "Feature $feature all settings ${all.settings}")

                val customSettings = PolarSensorSetting(
                    mapOf(
                        PolarSensorSetting.SettingType.SAMPLE_RATE to 50, // 50 Hz
                        PolarSensorSetting.SettingType.RESOLUTION to 16,    // 16 bits
                        PolarSensorSetting.SettingType.RANGE to 4          // ±4g
                    )
                )

                return@zip customSettings
            }
        }
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable()
    }



    override fun handleOnDestroy() {
        hrDisposable?.dispose()
        ecgDisposable?.dispose()
        super.handleOnDestroy()
    }
}
