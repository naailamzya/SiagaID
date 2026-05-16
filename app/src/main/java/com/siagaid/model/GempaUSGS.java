package com.siagaid.model;

import java.io.Serializable;
import java.util.List;

/**
 * Model data gempa dari USGS GeoJSON API.
 * Struktur: FeatureCollection → features[] → properties + geometry
 */
public class GempaUSGS implements Serializable {
    public static class FeatureCollection {
        public String type;
        public List<Feature> features;
    }

    // ========================
    // FEATURE (setiap gempa)
    // ========================
    public static class Feature implements Serializable {
        public String type;
        public Properties properties;
        public Geometry geometry;
        public String id;

        // Helper: konversi ke model Gempa agar bisa dipakai adapter yang sama
        public Gempa toGempa() {
            Gempa gempa = new Gempa();
            if (properties != null) {
                gempa.setMagnitude(properties.mag != null ?
                        String.valueOf(properties.mag) : "0");
                gempa.setWilayah(properties.place != null ?
                        properties.place : "Unknown Location");
                gempa.setPotensi(properties.alert != null ?
                        properties.alert : "-");

                // Konversi epoch milliseconds ke string waktu
                if (properties.time != null) {
                    java.text.SimpleDateFormat sdf =
                            new java.text.SimpleDateFormat("dd MMM yyyy HH:mm:ss",
                                    java.util.Locale.getDefault());
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Jakarta"));
                    String dateTime = sdf.format(new java.util.Date(properties.time));
                    gempa.setDateTime(dateTime);
                    gempa.setTanggal(dateTime.substring(0, 11));
                    gempa.setJam(dateTime.substring(12));
                }
            }
            if (geometry != null && geometry.coordinates != null
                    && geometry.coordinates.size() >= 3) {
                double lon = geometry.coordinates.get(0);
                double lat = geometry.coordinates.get(1);
                double depth = geometry.coordinates.get(2);
                gempa.setBujur(String.valueOf(lon));
                gempa.setLintang(String.valueOf(lat));
                gempa.setKedalaman(String.valueOf((int) depth) + " km");
                gempa.setCoordinates(lat + "," + lon);
            }
            gempa.setSumber("USGS");
            return gempa;
        }
    }

    // ========================
    // PROPERTIES
    // ========================
    public static class Properties implements Serializable {
        public Double mag;
        public String place;
        public Long time;
        public Long updated;
        public String url;
        public String detail;
        public Integer felt;
        public Double cdi;
        public Double mmi;
        public String alert;
        public String status;
        public Integer tsunami;
        public String type;
        public String title;
    }

    // ========================
    // GEOMETRY
    // ========================
    public static class Geometry implements Serializable {
        public String type;
        public List<Double> coordinates; // [longitude, latitude, depth]
    }
}