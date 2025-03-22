package com.example.myapplication;

public class GeoUtils {
    private static final double EARTH_RADIUS = 6371.0;
    private static double haversine(double val){
        return Math.pow(Math.sin(val/2),2);
    }
    public static double calulateDistance(double startLat,double startLong,double endLat,double endLong) {
        double dLat = Math.toRadians(endLat - startLat);
        double dLong = Math.toRadians(endLong - startLong);
        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);
        double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
    public static boolean isWithin5Km(double lat1, double lon1, double lat2, double lon2){
        return calulateDistance(lat1,lon1,lat2,lon2)<=10.0;
    }


}
