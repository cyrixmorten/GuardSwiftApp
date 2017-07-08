package com.guardswift.persistence.parse.data;

import com.parse.ParseInstallation;
import com.parse.ParseObject;


public class Installation {

    public static final String NAME = "name";
    public static final String MOBILE_NUMBER = "mobileNumber";

    private ParseObject installation;

    public Installation() {
        this.installation = ParseInstallation.getCurrentInstallation();
    }

    public Installation(ParseObject installation) {
        this.installation = installation;
    }

    public String getName() {
        String name = installation.getString(Installation.NAME);

        return name != null ? name : "";
    }

    public void setName(String name) {
        installation.put(Installation.NAME, name);
    }

    public String getMobileNumber() {
        String mobileNumber = installation.getString(Installation.MOBILE_NUMBER);

        return mobileNumber != null ? mobileNumber : "";
    }

    public void setMobileNumber(String countryCode, String mobileNumber) {
        setMobileNumber(countryCode+mobileNumber);
    }

    public void setMobileNumber(String mobileNumber) {
        // Ensure prefixed with '+'
        mobileNumber = mobileNumber.startsWith("+") ? mobileNumber : "+"+mobileNumber;

        installation.put(Installation.MOBILE_NUMBER, mobileNumber);
    }

    public void setEmptyMobileNumber() {
        installation.put(Installation.MOBILE_NUMBER, "");
    }

    public ParseObject getInstance() {
        return installation;
    }
}
