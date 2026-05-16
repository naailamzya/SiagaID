package com.siagaid.maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.siagaid.model.Gempa;

/**
 * Wrapper Gempa agar bisa dipakai oleh ClusterManager Google Maps.
 * Setiap gempa = 1 ClusterItem di peta.
 */
public class GempaClusterItem implements ClusterItem {

    private final LatLng position;
    private final String title;
    private final String snippet;
    private final Gempa gempa;

    public GempaClusterItem(Gempa gempa) {
        this.gempa    = gempa;
        this.position = new LatLng(gempa.getLatitude(), gempa.getLongitude());
        this.title    = "M " + gempa.getMagnitude() + " - " + gempa.getWilayah();
        this.snippet  = gempa.getTanggal() + " " + gempa.getJam()
                + " | Kedalaman: " + gempa.getKedalaman();
    }

    @Override
    public LatLng getPosition() { return position; }

    @Override
    public String getTitle() { return title; }

    @Override
    public String getSnippet() { return snippet; }

    @Override
    public Float getZIndex() { return 0f; }

    public Gempa getGempa() { return gempa; }
}