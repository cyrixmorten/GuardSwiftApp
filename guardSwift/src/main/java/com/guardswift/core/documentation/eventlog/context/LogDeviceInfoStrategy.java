package com.guardswift.core.documentation.eventlog.context;

import android.os.Build;

import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.Device;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

/**
 * Created by cyrix on 6/8/15.
 */
public class LogDeviceInfoStrategy implements LogContextStrategy {

    public static final String installation = "installation";
    public static final String deviceModel = "deviceModel";
    public static final String deviceManufacturer = "deviceManufacturer";
    public static final String gsVersion = "gsVersion";
    public static final String batteryLevel = "batteryLevel";

    @Override
    public void log(ParseObject toParseObject) {

        Device device = new Device(GuardSwiftApplication.getInstance());

        toParseObject.put(LogDeviceInfoStrategy.installation, ParseInstallation.getCurrentInstallation());
        toParseObject.put(LogDeviceInfoStrategy.deviceModel, Build.MODEL);
        toParseObject.put(LogDeviceInfoStrategy.deviceManufacturer, Build.MANUFACTURER);
        toParseObject.put(LogDeviceInfoStrategy.gsVersion, device.getVersionCode());
        toParseObject.put(LogDeviceInfoStrategy.batteryLevel, device.getBatteryLevelPct());
    }
}
