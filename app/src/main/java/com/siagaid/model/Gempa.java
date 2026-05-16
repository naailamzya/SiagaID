package com.siagaid.model;

import java.io.Serializable;
public class Gempa implements Serializable {
    private String Tanggal;
    private String Jam;
    private String DateTime;
    private String Coordinates;
    private String Lintang;
    private String Bujur;
    private String Magnitude;
    private String Kedalaman;
    private String Wilayah;
    private String Potensi;
    private String Dirasakan;
    private String Shakemap;
    private String sumber;
    public Gempa() {}

    public String getTanggal() { return Tanggal; }
    public String getJam() { return Jam; }
    public String getDateTime() { return DateTime; }
    public String getCoordinates() { return Coordinates; }
    public String getLintang() { return Lintang; }
    public String getBujur() { return Bujur; }
    public String getMagnitude() { return Magnitude; }
    public String getKedalaman() { return Kedalaman; }
    public String getWilayah() { return Wilayah; }
    public String getPotensi() { return Potensi; }
    public String getDirasakan() { return Dirasakan; }
    public String getShakemap() { return Shakemap; }
    public String getSumber() { return sumber; }

    public void setTanggal(String Tanggal) { this.Tanggal = Tanggal; }
    public void setJam(String Jam) { this.Jam = Jam; }
    public void setDateTime(String DateTime) { this.DateTime = DateTime; }
    public void setCoordinates(String Coordinates) { this.Coordinates = Coordinates; }
    public void setLintang(String Lintang) { this.Lintang = Lintang; }
    public void setBujur(String Bujur) { this.Bujur = Bujur; }
    public void setMagnitude(String Magnitude) { this.Magnitude = Magnitude; }
    public void setKedalaman(String Kedalaman) { this.Kedalaman = Kedalaman; }
    public void setWilayah(String Wilayah) { this.Wilayah = Wilayah; }
    public void setPotensi(String Potensi) { this.Potensi = Potensi; }
    public void setDirasakan(String Dirasakan) { this.Dirasakan = Dirasakan; }
    public void setShakemap(String Shakemap) { this.Shakemap = Shakemap; }
    public void setSumber(String sumber) { this.sumber = sumber; }

    public double getLatitude() {
        try {
            if (Lintang == null || Lintang.isEmpty()) return 0;
            String clean = Lintang.trim();
            double value = Double.parseDouble(clean.replaceAll("[^\\d.]", ""));
            if (clean.toUpperCase().contains("LS")) value = -value;
            return value;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parse Bujur dari format BMKG ke double.
     * Contoh: "117.87 BT" → 117.87
     */
    public double getLongitude() {
        try {
            if (Bujur == null || Bujur.isEmpty()) return 0;
            String clean = Bujur.trim();
            double value = Double.parseDouble(clean.replaceAll("[^\\d.]", ""));
            if (clean.toUpperCase().contains("BB")) value = -value;
            return value;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parse magnitude ke double.
     * Contoh: "5.2" → 5.2
     */
    public double getMagnitudeDouble() {
        try {
            if (Magnitude == null || Magnitude.isEmpty()) return 0;
            return Double.parseDouble(Magnitude.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parse kedalaman ke int (angka saja).
     * Contoh: "10 km" → 10
     */
    public int getKedalamanInt() {
        try {
            if (Kedalaman == null || Kedalaman.isEmpty()) return 0;
            return Integer.parseInt(Kedalaman.trim().replaceAll("[^\\d]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}

}
