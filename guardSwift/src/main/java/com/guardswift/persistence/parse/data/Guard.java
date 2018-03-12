package com.guardswift.persistence.parse.data;

import android.location.Location;

import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.Positioned;
import com.guardswift.persistence.parse.query.GuardQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseSession;

import java.util.Date;
import java.util.List;

@ParseClassName("Guard")
public class Guard extends ExtendedParseObject implements Positioned {

    public enum Role {ADMIN}


    private static final String guardId = "guardId";
    public static final String name = "name";
    public static final String session = "session";
    public static final String installation = "installation";
    private static final String mobileNumber = "mobileNumber";

    private static final String alarmNotify = "alarmNotify";
    private static final String alarmSound = "alarmSound";
    private static final String alarmSMS = "alarmSMS";

    private static final String lastLogin = "lastLogin";
    private static final String lastLogout = "lastLogout";
    private static final String position = "position";
    private static final String isOnline = "isOnline";

    // access rights
    private static final String accessRegular = "accessRegular";
    private static final String accessDistrict = "accessDistrict";
    private static final String accessStatic = "accessStatic";
    private static final String roles = "roles";


    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<Guard> getAllNetworkQuery() {
        return new GuardQueryBuilder(false).build();
    }


    public static GuardQueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new GuardQueryBuilder(fromLocalDatastore);
    }


    public int getGuardId() {
        return getInt(Guard.guardId);
    }

    public String getName() {
        return getString(Guard.name);
    }

    public void setName(String name) {
        put(Guard.name, name);
    }

    public ParseObject getOwner() {
        return getParseObject(owner);
    }

    private void setLastLogin(Date lastLogin) {
        put(Guard.lastLogin, lastLogin);
    }

    public Date getLastLogin() {
        return getDate(Guard.lastLogin);
    }

    private void setLastLogout(Date lastLogout) {
        put(Guard.lastLogout, lastLogout);
    }

    public Date getLastLogout() {
        return getDate(Guard.lastLogout);
    }


    public void setOnline(boolean online) {
        if (online) {
            setLastLogin(new Date());
        } else {
            setLastLogout(new Date());
        }
        put(isOnline, online);
    }

    public boolean isOnline() {
        return has(isOnline) && getBoolean(isOnline);
    }

    public void setPosition(Location location) {
        setPosition(ParseModule.geoPointFromLocation(location));
    }

    public void setPosition(ParseGeoPoint location) {
        if (location == null)
            return;

        put(Guard.position, location);
    }

    public ParseGeoPoint getPosition() {
        return getParseGeoPoint(position);
    }

    public boolean canAccessRegularTasks() {
        return getBoolean(accessRegular);
    }


    public boolean canAccessStaticTasks() {
        return getBoolean(accessStatic);
    }

    public boolean canAccessAlarms() {
        return getBoolean(accessRegular); // todo temporary
    }

    public void setSession(ParseSession session) {
        put(Guard.session, session);
    }

    public void setInstallation() {
        put(Guard.installation, com.parse.ParseInstallation.getCurrentInstallation());
    }

    public void setMobile(String mobile) {
        put(Guard.mobileNumber, mobile);
    }

    public String getMobileNumber() {
        return getStringSafe(Guard.mobileNumber);
    }


    public boolean isAlarmNotificationsEnabled() {
        return getBoolean(Guard.alarmNotify);
    }

    public void enableAlarmNotification(boolean enable) {
        put(Guard.alarmNotify, enable);
    }

    public boolean isAlarmSoundEnabled() {
        return isAlarmNotificationsEnabled() && getBoolean(Guard.alarmSound);
    }

    public void enableAlarmSound(boolean enable) {
        if (enable) {
            put(Guard.alarmNotify, true);
        }
        put(Guard.alarmSound, enable);
    }

    public boolean isAlarmSMSEnabled() {
        return isAlarmNotificationsEnabled() && getBoolean(Guard.alarmSMS);
    }

    public void enableAlarmSMS(boolean enable) {
        put(Guard.alarmSMS, enable);
    }

    public boolean hasRole(Role role) {
        if (!has(Guard.roles)) {
            return false;
        }

        List<String> roles = getList(Guard.roles);
        for (String guardRole: roles) {
            if (guardRole.toLowerCase().equals(role.name().toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public Installation getInstallation() {
        return new Installation(getLDSFallbackParseObject(Guard.installation));
    }

    public String getInstallationName() {
        if (!has(Guard.installation)) {
            return "";
        }

        return getInstallation().getName();
    }

    public String getAlarmMobile() {
        if (has(Guard.installation)) {
            return getInstallation().getMobileNumber();
        }

        return getMobileNumber();
    }
}
