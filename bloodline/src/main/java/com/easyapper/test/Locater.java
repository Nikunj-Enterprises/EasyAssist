package com.easyapper.test;

import com.easyapper.bloodline.LocationReader;

public class Locater implements LocationReader {
    @Override
    public float getLangitude() {
        return 22.7899f;
    }

    @Override
    public float getLatitude() {
        return 98.77645f;
    }
}
