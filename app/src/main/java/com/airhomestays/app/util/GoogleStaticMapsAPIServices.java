package com.airhomestays.app.util;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.airhomestays.app.Constants;

import java.util.ArrayList;


public class GoogleStaticMapsAPIServices {
    private static final double EARTH_RADIUS_KM = 6371;

    public static String getStaticMapURL(Location location, int radiusMeters)
    {
        String GOOGLE_STATIC_MAPS_API_KEY = Constants.getGoogleMapKey();
        String pathString = "";
        if (radiusMeters > 0)
        {
            ArrayList<LatLng> circlePoints = getCircleAsPolyline(location, radiusMeters);
            if (circlePoints.size() > 0)
            {
                String encodedPathLocations = PolyUtil.encode(circlePoints);
                pathString = "&path=color:0x00888590%7Cweight:3%7Cfillcolor:0x00888560%7Cenc:" + encodedPathLocations;
            }
        }

        return "https://maps.googleapis.com/maps/api/staticmap?&zoom=16&size=620x540&" +
                pathString +
                "&key=" + GOOGLE_STATIC_MAPS_API_KEY;
    }

    private static ArrayList<LatLng> getCircleAsPolyline(Location center, int radiusMeters)
    {
        ArrayList<LatLng> path = new ArrayList<>();

        double latitudeRadians = center.getLatitude() * Math.PI / 180.0;
        double longitudeRadians = center.getLongitude() * Math.PI / 180.0;
        double radiusRadians = radiusMeters / 700.0 / EARTH_RADIUS_KM;

        double calcLatPrefix = Math.sin(latitudeRadians) * Math.cos(radiusRadians);
        double calcLatSuffix = Math.cos(latitudeRadians) * Math.sin(radiusRadians);

        for (int angle = 0; angle < 361; angle += 10)
        {
            double angleRadians = angle * Math.PI / 180.0;

            double latitude = Math.asin(calcLatPrefix + calcLatSuffix * Math.cos(angleRadians));
            double longitude = ((longitudeRadians + Math.atan2(Math.sin(angleRadians) * Math.sin(radiusRadians) * Math.cos(latitudeRadians), Math.cos(radiusRadians) - Math.sin(latitudeRadians) * Math.sin(latitude))) * 180) / Math.PI;
            latitude = latitude * 180.0 / Math.PI;

            path.add(new LatLng(latitude, longitude));
        }

        return path;
    }
}