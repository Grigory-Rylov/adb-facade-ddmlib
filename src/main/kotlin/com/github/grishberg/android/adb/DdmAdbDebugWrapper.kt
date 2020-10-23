package com.github.grishberg.android.adb

import com.android.ddmlib.Client
import com.android.ddmlib.ClientData
import com.android.ddmlib.DdmPreferences

class DdmAdbDebugWrapper(
    private val logger: AdbLogger
) : AdbDebugWrapper {
    override fun getSelectedDebugPort(): Int = DdmPreferences.getSelectedDebugPort()

    override fun setDebugPortBase(port: Int) {
        DdmPreferences.setSelectedDebugPort(port)
    }

    override fun setMethodProfilingHandler(handler: MethodProfilingHandler) {
        ClientData.setMethodProfilingHandler(MethodProfilingHandlerAdapter(handler, logger))
    }

    override fun setProfilerBufferSizeMb(sizeInMb: Int) {
        DdmPreferences.setProfilerBufferSizeMb(sizeInMb)
    }

    private class MethodProfilingHandlerAdapter(
        private val innerHandler: MethodProfilingHandler,
        private val logger: AdbLogger
    ) : ClientData.IMethodProfilingHandler {
        override fun onSuccess(remoteFilePath: String, client: Client) {
            innerHandler.onSuccess(remoteFilePath, DdmClientWrapper(client, logger))
        }

        override fun onSuccess(data: ByteArray, client: Client) {
            innerHandler.onSuccess(data, DdmClientWrapper(client, logger))
        }

        override fun onStartFailure(client: Client, message: String) {
            innerHandler.onStartFailure(DdmClientWrapper(client, logger), message)
        }

        override fun onEndFailure(client: Client, message: String) {
            innerHandler.onEndFailure(DdmClientWrapper(client, logger), message)
        }
    }
}
