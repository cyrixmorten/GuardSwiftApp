package com.guardswift.persistence.parse.documentation.gps;

public class TrackerData {

    private long time;

    private float bearing;
    private double altitude;

    private float accuracy;
    private float speed;

    private double longitude;
    private double latitude;

    private int activityType;
    private int activityConfidence;

    private String provider;

    public int getActivityConfidence() {
        return activityConfidence;
    }

    public long getTime() {
        return time;
    }

    public float getBearing() {
        return bearing;
    }

    public double getAltitude() {
        return altitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public float getSpeed() {
        return speed;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getActivityType() {
        return activityType;
    }

    @Override
    public String toString() {
        return "TrackerData{" +
                "time=" + time +
                ", bearing=" + bearing +
                ", altitude=" + altitude +
                ", accuracy=" + accuracy +
                ", speed=" + speed +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", activityType=" + activityType +
                ", activityConfidence=" + activityConfidence +
                ", provider='" + provider + '\'' +
                '}';
    }
}