package com.siagaid.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import com.siagaid.maps.CustomClusterRenderer;
import com.siagaid.model.Gempa;

import java.util.List;

/**
 * Utility class untuk operasi Google Maps.
 * - Hitung radius dampak gempa
 * - Gambar circle overlay
 * - Navigasi ke lokasi gempa
 * - Fit kamera ke semua marker
 */
public class MapsUtils {

    // ========================
    // HITUNG RADIUS DAMPAK
    // ========================

    /**
     * Estimasi radius dampak gempa dalam meter.
     * Rumus: magnitude * 50 km → dikonversi ke meter
     * Contoh: M 5.0 → 250 km → 250.000 meter
     */
    public static double getRadiusInMeters(double magnitude) {
        return magnitude * 50 * 1000; // km → meter
    }

    // ========================
    // GAMBAR CIRCLE OVERLAY
    // ========================

    /**
     * Menggambar lingkaran radius dampak di sekitar lokasi gempa.
     * @return Circle object agar bisa dihapus nanti
     */
    public static Circle drawRadiusCircle(GoogleMap map, Gempa gempa) {
        double magnitude = gempa.getMagnitudeDouble();
        double radius    = getRadiusInMeters(magnitude);
        int    color     = CustomClusterRenderer.getColorByMagnitude(magnitude);
        LatLng position  = new LatLng(gempa.getLatitude(), gempa.getLongitude());

        CircleOptions circleOptions = new CircleOptions()
                .center(position)
                .radius(radius)
                .strokeColor(color)
                .strokeWidth(2f)
                .fillColor(Color.argb(40,
                        Color.red(color),
                        Color.green(color),
                        Color.blue(color))); // semi-transparan

        return map.addCircle(circleOptions);
    }

    // ========================
    // NAVIGASI KE LOKASI GEMPA
    // ========================

    /**
     * Buka Google Maps eksternal untuk navigasi ke lokasi gempa.
     * Fallback ke browser jika Google Maps tidak terinstall.
     */
    public static void navigateToLocation(Context context, double lat, double lng,
                                          String label) {
        // Coba buka Google Maps app
        Uri gmmIntentUri = Uri.parse(
                "google.navigation:q=" + lat + "," + lng + "&mode=d"
        );
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            // Google Maps terinstall
            context.startActivity(mapIntent);
        } else {
            // Fallback: buka di browser
            Uri browserUri = Uri.parse(
                    "https://www.google.com/maps/dir/?api=1&destination="
                            + lat + "," + lng
            );
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
            if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(browserIntent);
            } else {
                Toast.makeText(context,
                        "Tidak ada aplikasi maps yang tersedia",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Overload: buka maps hanya dengan koordinat (tanpa label)
     */
    public static void navigateToLocation(Context context, double lat, double lng) {
        navigateToLocation(context, lat, lng, "Lokasi Gempa");
    }

    // ========================
    // BUKA MAPS UNTUK LIHAT LOKASI (bukan navigasi)
    // ========================

    /**
     * Buka Google Maps untuk melihat pin lokasi gempa (bukan navigasi).
     */
    public static void showLocationOnMaps(Context context, double lat, double lng,
                                          String label) {
        Uri gmmIntentUri = Uri.parse(
                "geo:" + lat + "," + lng + "?q=" + lat + "," + lng
                        + "(" + Uri.encode(label) + ")"
        );
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            // Fallback browser
            Uri browserUri = Uri.parse(
                    "https://maps.google.com/?q=" + lat + "," + lng
            );
            context.startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }

    // ========================
    // FIT KAMERA KE SEMUA MARKER
    // ========================

    /**
     * Gerakkan kamera agar semua gempa terlihat di layar.
     */
    public static void fitCameraToGempaList(GoogleMap map, List<Gempa> gempaList) {
        if (gempaList == null || gempaList.isEmpty()) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean hasValidPoint = false;

        for (Gempa gempa : gempaList) {
            double lat = gempa.getLatitude();
            double lng = gempa.getLongitude();
            if (lat != 0 || lng != 0) {
                builder.include(new LatLng(lat, lng));
                hasValidPoint = true;
            }
        }

        if (!hasValidPoint) return;

        try {
            LatLngBounds bounds = builder.build();
            map.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds, 100) // padding 100px
            );
        } catch (Exception e) {
            // Fallback ke center Indonesia jika error
            map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            new LatLng(-2.5489, 118.0149), 4f
                    )
            );
        }
    }

    // ========================
    // ZOOM KE SATU GEMPA
    // ========================

    /**
     * Zoom kamera ke satu lokasi gempa dengan animasi.
     */
    public static void zoomToGempa(GoogleMap map, Gempa gempa) {
        LatLng position = new LatLng(gempa.getLatitude(), gempa.getLongitude());
        map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(position, 8f)
        );
    }

    // ========================
    // DEFAULT CAMERA (Indonesia)
    // ========================

    /**
     * Set kamera ke posisi default: tengah Indonesia.
     */
    public static void setDefaultCamera(GoogleMap map) {
        map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        new LatLng(-2.5489, 118.0149), 4f
                )
        );
    }

    // ========================
    // SHARE LOKASI GEMPA
    // ========================

    /**
     * Share info gempa via aplikasi lain (WhatsApp, dll).
     */
    public static void shareGempaInfo(Context context, Gempa gempa) {
        String info = "🚨 INFO GEMPA BUMI\n\n"
                + "📍 Lokasi   : " + gempa.getWilayah() + "\n"
                + "📊 Magnitudo: M " + gempa.getMagnitude() + "\n"
                + "🔽 Kedalaman: " + gempa.getKedalaman() + "\n"
                + "📅 Waktu    : " + gempa.getTanggal() + " " + gempa.getJam() + "\n"
                + "⚠️ Potensi  : " + gempa.getPotensi() + "\n\n"
                + "📌 Koordinat: " + gempa.getLatitude() + ", " + gempa.getLongitude() + "\n"
                + "🗺️ Maps     : https://maps.google.com/?q="
                + gempa.getLatitude() + "," + gempa.getLongitude() + "\n\n"
                + "Sumber: BMKG | SiagaID App";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, info);
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Info Gempa"));
    }
}