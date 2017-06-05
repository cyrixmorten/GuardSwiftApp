package com.guardswift.util;

import android.location.Address;

import com.google.common.collect.Maps;
import com.guardswift.core.exceptions.HandleException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

/**
 * Created by cyrix on 11/22/15.
 */
public class GeocodedAddress {

    private static final String TAG = GeocodedAddress.class.getSimpleName();

    public static String POSTALCODE = "postalCode";
    public static String CITY = "city";
    public static String ADDRESS = "address";
    public static String STATE = "state";
    public static String COUNTRY = "country";

    private boolean hasData;
    private final Address address;

    public GeocodedAddress(JSONObject jsonAddress) {
        address = new Address(Locale.getDefault());

        if (jsonAddress == null) {
            hasData = false;
            return;
        }

        try {
            address.setPostalCode(jsonAddress.getString(POSTALCODE));
            address.setLocality(jsonAddress.getString(CITY));
            address.setAddressLine(0, jsonAddress.getString(ADDRESS));
            address.setAdminArea(jsonAddress.getString(STATE));
            address.setCountryName(jsonAddress.getString(COUNTRY));

            hasData = true;

        } catch (JSONException e) {
            new HandleException(TAG, "createWithFontAwesomeIcon from json", e);
            hasData = false;
        }
    }

    public GeocodedAddress(Address geoAddress) {
        this.address = geoAddress;
    }

    public boolean hasData() {
        return hasData;
    }

    public String getPostalCode() {
        return address.getPostalCode();
    }

    public String getCity() {
        return address.getLocality();
    }

    public String getAddress() {
        return address.getAddressLine(0);
    }

    public String getState() {
        return address.getAdminArea();
    }

    public String getCountry() {
        return address.getCountryName();
    }

    public JSONObject toJSON() {
        Map<String, String> addressMap = Maps.newHashMap();
        addressMap.put(ADDRESS, address.getAddressLine(0));
        addressMap.put(CITY, address.getLocality());
        addressMap.put(STATE, address.getAdminArea());
        addressMap.put(COUNTRY, address.getCountryName());
        addressMap.put(POSTALCODE, address.getPostalCode());
        // Wrap in JSONObject
        return new JSONObject(addressMap);
    }

    public String getFullAddress() {
        return getAddress() + ", " + getPostalCode() + ", " + getCity();
    }
}
