package com.example.sameer.hound;

/**
 * Created by Sameer on 8/2/2015.
 */
public class location {
    double longitude;
    double latitude;

    public location() {
        this.longitude = 0;
        this.latitude = 0;
    }

    public location(double longitude,double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }
}
