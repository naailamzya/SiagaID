package com.siagaid.maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Custom renderer untuk ClusterManager.
 * - Warna marker berdasarkan magnitudo gempa
 * - Warna cluster berdasarkan jumlah item
 */
public class CustomClusterRenderer extends DefaultClusterRenderer<GempaClusterItem> {

    private final Context context;

    public CustomClusterRenderer(Context context, GoogleMap map,
                                 ClusterManager<GempaClusterItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    // ========================
    // WARNA MARKER per GEMPA
    // ========================
    @Override
    protected void onBeforeClusterItemRendered(GempaClusterItem item,
                                               MarkerOptions markerOptions) {
        double mag = item.getGempa().getMagnitudeDouble();
        BitmapDescriptor icon = createMarkerIcon(mag);
        markerOptions.icon(icon).title(item.getTitle()).snippet(item.getSnippet());
    }

    // ========================
    // WARNA CLUSTER
    // ========================
    @Override
    protected void onBeforeClusterRendered(Cluster<GempaClusterItem> cluster,
                                           MarkerOptions markerOptions) {
        int count = cluster.getSize();
        BitmapDescriptor icon = createClusterIcon(count);
        markerOptions.icon(icon);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<GempaClusterItem> cluster) {
        // Cluster jika ada 3 atau lebih gempa berdekatan
        return cluster.getSize() >= 3;
    }

    // ========================
    // BUAT ICON MARKER (lingkaran berwarna)
    // ========================
    private BitmapDescriptor createMarkerIcon(double magnitude) {
        int color = getColorByMagnitude(magnitude);

        int size = 60;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Lingkaran luar (border putih)
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.WHITE);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, borderPaint);

        // Lingkaran dalam (warna magnitude)
        Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(color);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4, fillPaint);

        // Teks magnitude
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(18f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);

        String magText = String.valueOf(magnitude);
        if (magText.length() > 3) magText = magText.substring(0, 3);

        float y = size / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(magText, size / 2f, y, textPaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // ========================
    // BUAT ICON CLUSTER (lingkaran dengan jumlah)
    // ========================
    private BitmapDescriptor createClusterIcon(int count) {
        int color = getClusterColor(count);

        int size = 80;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Shadow / outer ring
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.argb(60, 0, 0, 0));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, shadowPaint);

        // Lingkaran luar semi-transparan
        Paint outerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outerPaint.setColor(Color.argb(120,
                Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2, outerPaint);

        // Lingkaran dalam
        Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerPaint.setColor(color);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 12, innerPaint);

        // Teks jumlah
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(22f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);

        float y = size / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(String.valueOf(count), size / 2f, y, textPaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // ========================
    // WARNA BERDASARKAN MAGNITUDO
    // ========================
    public static int getColorByMagnitude(double magnitude) {
        if (magnitude >= 6.0) {
            return Color.parseColor("#E74C3C"); // Merah — berbahaya
        } else if (magnitude >= 5.0) {
            return Color.parseColor("#E67E22"); // Oranye — waspada
        } else if (magnitude >= 4.0) {
            return Color.parseColor("#F1C40F"); // Kuning — perhatian
        } else {
            return Color.parseColor("#2ECC71"); // Hijau — aman
        }
    }

    // ========================
    // WARNA CLUSTER BERDASARKAN JUMLAH
    // ========================
    private int getClusterColor(int count) {
        if (count >= 50) {
            return Color.parseColor("#E74C3C"); // Merah
        } else if (count >= 20) {
            return Color.parseColor("#E67E22"); // Oranye
        } else if (count >= 10) {
            return Color.parseColor("#F1C40F"); // Kuning
        } else {
            return Color.parseColor("#3498DB"); // Biru
        }
    }
}