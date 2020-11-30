package edu.ptit.vn.appda2020.util;


import edu.ptit.vn.appda2020.model.GeoPoint;

public class HaversineScorer {
    public static double computeCost(GeoPoint from, GeoPoint to) {
        double R = 6372.8; // In kilometers

        double dLat = Math.toRadians(to.getLat() - from.getLat());
        double dLon = Math.toRadians(to.getLng() - from.getLng());
        double lat1 = Math.toRadians(from.getLat());
        double lat2 = Math.toRadians(to.getLat());

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }
}
