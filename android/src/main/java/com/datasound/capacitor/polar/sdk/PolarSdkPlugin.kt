package com.datasound.capacitor.polar.sdk

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.polar.androidcommunications.api.ble.model.DisInfo
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarEcgData
import com.polar.sdk.api.model.PolarHrData
import com.polar.sdk.api.model.PolarSensorSetting
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit


@CapacitorPlugin(name = "PolarSdk")
class PolarSdkPlugin : Plugin() {
    companion object {
        private const val TAG = "HRActivity"
    }

    private var hrDisposable: Disposable? = null
    private var autoConnectDisposable: Disposable? = null
    private var ecgDisposable: Disposable? = null
    private var deviceId = ""

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
                setTime(polarDeviceInfo.deviceId)
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "CONNECTING: ${polarDeviceInfo.deviceId}")
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "DISCONNECTED: ${polarDeviceInfo.deviceId}")
            }

            override fun disInformationReceived(identifier: String, disInfo: DisInfo) {
                Log.d(TAG, "DIS INFO: $disInfo")
            }

            override fun batteryLevelReceived(identifier: String, level: Int) {
                Log.d(TAG, "BATTERY LEVEL: $level")
            }

            override fun bleSdkFeatureReady(identifier: String, feature: PolarBleApi.PolarBleSdkFeature) {
                Log.d(TAG, "feature ready $feature")
            }

        })
    }

    @PluginMethod
    fun connectPolar(call: PluginCall) {
        val timeoutHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (autoConnectDisposable != null) {
                autoConnectDisposable?.dispose()
                val error = JSObject()
                error.put("value", false)
                error.put("message", "Connection timed out")
                call.reject("Connection timed out")
            }
        }

        // Imposta il timeout a 10 secondi
        timeoutHandler.postDelayed(timeoutRunnable, 10000)

        if (autoConnectDisposable != null) {
            autoConnectDisposable?.dispose()
        }

        autoConnectDisposable = api.autoConnectToDevice(-60, "180D",  "H10")
            .subscribe(
                {
                    Log.d(TAG, "Connection attempt started")
                    // Se la connessione è completata, annulla il timeout
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    val result = JSObject()
                    result.put("value", true)
                    call.resolve(result)
                },
                { throwable: Throwable ->
                    Log.e(TAG, "Connection error: ${throwable.message}")
                    timeoutHandler.removeCallbacks(timeoutRunnable) // annulla il timeout in caso di errore

                    val error = JSObject()
                    error.put("value", false)
                    error.put("message", "Connection failed")
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
                        for (sample in hrData.samples) {
                            Log.d(TAG, "HR bpm: ${sample.hr} rrs: ${sample.rrsMs}")
                            val currentTimestamp = System.currentTimeMillis()
                            // Send data to JS
                            val data = JSObject()
                            data.put("bpm", sample.hr)
                            data.put("rrs", sample.rrsMs)
                            data.put("timestamp", currentTimestamp)
                            notifyListeners("hrData", data)
                            val result = JSObject()
                            result.put("value", true)
                            call.resolve(result)
                        }
                    },
                    { error: Throwable ->
                        Log.e(TAG, "HR stream failed. Reason: $error")
                        call.reject(error.toString())
                    },
                    { Log.d(TAG, "HR stream complete") }
                )
        } else {
            hrDisposable?.dispose()
            call.reject("error", "HR stream stopped")
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
                        for (data in polarEcgData.samples) {
                            Log.d(TAG, "    yV: ${data.voltage} timeStamp: ${data.timeStamp}")
                            val resData = JSObject()
                            resData.put("yV", data.voltage)
                            resData.put("timestamp", data.timeStamp)
                            notifyListeners("ecgData", resData)
                            val result = JSObject()
                            result.put("value", true)
                            call.resolve(result)
                        }
                    },
                    { error: Throwable ->
                        Log.e(TAG, "ECG stream failed. Reason $error")
                        call.reject(error.toString())
                    },
                    { Log.d(TAG, "ECG stream complete") }
                )
        } else {
            // NOTE stops streaming if it is "running"
            ecgDisposable?.dispose()
            call.reject("error", "HCG stream stopped")
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
    fun disconnectPolar(call: PluginCall) {
        api.disconnectFromDevice(deviceId)
        val result = JSObject()
        result.put("value", true)
        call.resolve(result)
    }

    private fun setTime(deviceId: String) {
        val calendar = Calendar.getInstance()
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

                Flowable.just(sensorSettings.first) // Restituiamo il primo set come risultato
            }
    }

    override fun handleOnDestroy() {
        hrDisposable?.dispose()
        ecgDisposable?.dispose()
        super.handleOnDestroy()
    }
}
