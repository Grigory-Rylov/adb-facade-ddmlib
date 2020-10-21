package com.github.grishberg.android.adb

import com.android.ddmlib.Client
import java.util.concurrent.TimeUnit

class DdmClientWrapper(
    private val client: Client,
    private val logger: AdbLogger
) : ClientWrapper {
    override fun dumpHprof() = client.dumpHprof()

    override fun executeGarbageCollector() = client.executeGarbageCollector()

    override fun requestMethodProfilingStatus() = client.requestMethodProfilingStatus()

    override fun startMethodTracer() = client.startMethodTracer()

    override fun startOpenGlTracing(): Boolean = client.startOpenGlTracing()

    override fun startSamplingProfiler(samplingInterval: Int, timeUnit: TimeUnit) =
        client.startSamplingProfiler(samplingInterval, timeUnit)

    override fun stopMethodTracer() = client.stopMethodTracer()

    override fun stopOpenGlTracing() = client.stopOpenGlTracing()

    override fun stopSamplingProfiler() = client.stopSamplingProfiler()
}
