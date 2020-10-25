package com.github.grishberg.android.adb

import com.android.ddmlib.IDevice
import com.android.ddmlib.IShellOutputReceiver
import java.util.concurrent.TimeUnit

class AdbConnectedDeviceWrapper(
    private val device: IDevice,
    private val logger: AdbLogger
) : ConnectedDeviceWrapper {
    override val serialNumber: String
        get() = device.serialNumber

    override fun executeShellCommand(cmd: String, receiver: ShellOutReceiver) {
        device.executeShellCommand(cmd, IShellOutReceiverAdapter(receiver))
    }

    override fun executeShellCommand(
        cmd: String,
        receiver: ShellOutReceiver,
        maxTimeToOutputResponse: Long,
        maxTimeUnits: TimeUnit
    ) {
        device.executeShellCommand(
            cmd, IShellOutReceiverAdapter(receiver), maxTimeToOutputResponse,
            maxTimeUnits
        )
    }

    override fun getClient(applicationName: String): ClientWrapper? {
        val client = device.getClient(applicationName) ?: return null
        return DdmClientWrapper(client, logger)
    }

    private class IShellOutReceiverAdapter(
        private val receiver: ShellOutReceiver
    ) : IShellOutputReceiver {
        override fun addOutput(data: ByteArray, offset: Int, length: Int) {
            receiver.addOutput(data, offset, length)
        }

        override fun flush() = receiver.flush()

        override fun isCancelled() = receiver.isCancelled()
    }
}
