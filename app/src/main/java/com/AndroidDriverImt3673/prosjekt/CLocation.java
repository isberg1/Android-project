package com.AndroidDriverImt3673.prosjekt;

import android.location.Location;

public class CLocation extends Location{


    public CLocation(Location location) {
        super(location);
    }

    @Override
    public float distanceTo(Location dest) {
        float nDistance = super.distanceTo(dest);
        return nDistance;
    }

    @Override
    public float getAccuracy() {
        float nAccuracy = super.getAccuracy();
        return nAccuracy;
    }

    @Override
    public double getAltitude() {
        double nAltitude = super.getAltitude();
        return nAltitude;
    }

    @Override
    public float getSpeed() {
        float nSpeed = super.getSpeed() * 3.6f;
        return nSpeed;
    }
}
