package com.android.ddmlib;


import com.github.grishberg.android.adb.AdbLogger;

public class MonitorThreadLoggerBridge {
    public static void setLogger(AdbLogger logger) {
        MonitorThread.log = logger;
    }
}
