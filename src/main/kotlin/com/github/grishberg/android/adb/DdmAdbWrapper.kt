package com.github.grishberg.android.adb

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.android.ddmlib.MonitorThreadLoggerBridge
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Collections

private const val TAG = "DdmAdbWrapper"

class DdmAdbWrapper(
    private val logger: AdbLogger,
    private val clientSupport: Boolean,
    androidHome: String? = null,
    private val forceNewBridge: Boolean = false
) : AdbWrapper {
    private var bridge: AndroidDebugBridge? = null
    private val androidSdkPath: String? = androidHome ?: System.getenv("ANDROID_HOME")
    private val innerDeviceListener = DeviceChangedListenerAdapter()
    private val deviceListeners = mutableListOf<DeviceChangedListener>()

    init {
        MonitorThreadLoggerBridge.setLogger(logger)
    }

    override fun allowedToConnectAndStop(): Boolean = true

    override fun connect() {
        logger.d("$TAG: connect, bridge=$bridge")
        if (bridge != null) {
            stop()
        }

        AndroidDebugBridge.initIfNeeded(clientSupport)

        logger.d("$TAG: creating ADB bridge with android_home=$androidSdkPath")
        if (androidSdkPath != null) {
            val adbPath = File(androidSdkPath, "platform-tools/adb").absolutePath
            bridge = AndroidDebugBridge.createBridge(adbPath, forceNewBridge)
        } else {
            bridge = AndroidDebugBridge.createBridge()
        }
        logger.d("$TAG: connected, bridge=$bridge")
    }

    override fun connect(remoterAddress: String) {
        adbWifiConnect(remoterAddress)
        connect()
    }

    private fun adbWifiConnect(ipAddress: String): Boolean {
        var connected = false
        logger.d("$TAG: connect to $ipAddress...")

        val process =
            Runtime.getRuntime().exec(File(androidSdkPath, "platform-tools/adb").absolutePath + " connect " + ipAddress)
        val inBuffer = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        var message: String? = null
        while (inBuffer.readLine().also { line = it } != null) {
            if (line!!.contains("connected")) {
                connected = true
            }
            message = line
        }
        if (connected) {
            logger.d("$TAG: $message")
            return true
        }

        return false
    }

    override fun deviceList(): List<ConnectedDeviceWrapper> {
        val result = mutableListOf<ConnectedDeviceWrapper>()
        val devices = bridge?.devices?.asList() ?: Collections.emptyList()
        for (device in devices) {
            result.add(AdbConnectedDeviceWrapper(device, logger))
        }
        return result
    }

    override fun hasInitialDeviceList(): Boolean {
        return bridge?.hasInitialDeviceList() ?: false
    }

    override fun isConnected(): Boolean {
        return bridge?.isConnected ?: false
    }

    override fun addDeviceChangedListener(listener: DeviceChangedListener) {
        if (deviceListeners.isEmpty()) {
            AndroidDebugBridge.addDeviceChangeListener(innerDeviceListener)
        }
        deviceListeners.add(listener)
    }

    override fun removeDeviceChangedListener(listener: DeviceChangedListener) {
        deviceListeners.remove(listener)
        if (deviceListeners.isEmpty()) {
            AndroidDebugBridge.addDeviceChangeListener(innerDeviceListener)
        }
    }

    override fun stop() {
        logger.d("$TAG: stop")
        bridge = null
        AndroidDebugBridge.disconnectBridge()
        AndroidDebugBridge.terminate()
    }

    private inner class DeviceChangedListenerAdapter : AndroidDebugBridge.IDeviceChangeListener {
        override fun deviceConnected(device: IDevice) {
            logger.d("$TAG: connected $device")
            for (listener in deviceListeners) {
                listener.deviceConnected(AdbConnectedDeviceWrapper(device, logger))
            }
        }

        override fun deviceDisconnected(device: IDevice) {
            logger.d("$TAG: connected $device")

            for (listener in deviceListeners) {
                listener.deviceDisconnected(AdbConnectedDeviceWrapper(device, logger))
            }
        }

        override fun deviceChanged(device: IDevice, changeMask: Int) {
            for (listener in deviceListeners) {
                listener.deviceChanged(AdbConnectedDeviceWrapper(device, logger), changeMask)
            }
        }
    }
}
