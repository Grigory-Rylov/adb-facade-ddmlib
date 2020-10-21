package com.github.grishberg.android.adb

class NoOpLogger : AdbLogger {
    override fun d(msg: String) = Unit

    override fun e(s: String) = Unit

    override fun e(msg: String, t: Throwable) = Unit

    override fun w(msg: String) = Unit
}
