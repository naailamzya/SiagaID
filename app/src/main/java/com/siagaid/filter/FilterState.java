package com.siagaid.filter;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * POJO untuk menyimpan state filter gempa.
 * State disimpan ke SharedPreferences agar persistent.
 */
public class FilterState {

    private static final String PREF_NAME        = "siagaid_prefs";
    private static final String KEY_MAG_MIN      = "filter_mag_min";
    private static final String KEY_MAG_MAX      = "filter_mag_max";
    private static final String KEY_DEPTH        = "filter_depth";
    private static final String KEY_BMKG_TERBARU = "filter_bmkg_terbaru";
    private static final String KEY_BMKG_DIRASAKAN = "filter_bmkg_dirasakan";
    private static final String KEY_USGS         = "filter_usgs";
    private static final String KEY_TIME_RANGE   = "filter_time_range";
    private static final String KEY_SORT_BY      = "filter_sort_by";
    private static final String KEY_KEYWORD      = "filter_keyword";

    // ========================
    // FIELDS
    // ========================
    public float  magnitudeMin     = 0.0f;
    public float  magnitudeMax     = 9.0f;
    public int    depthCategory    = 0;    // 0=semua,1=dangkal,2=menengah,3=dalam
    public boolean showBmkgTerbaru  = true;
    public boolean showBmkgDirasakan = true;
    public boolean showUsgs         = true;
    public int    timeRange        = 0;    // 0=24jam,1=3hari,2=7hari,3=semua
    public int    sortBy           = 0;    // 0=terbaru,1=terlama,2=magTertinggi,
    // 3=magTerendah,4=terdangkal
    public String searchKeyword    = "";

    // ========================
    // KONSTANTA SORT
    // ========================
    public static final int SORT_TERBARU        = 0;
    public static final int SORT_TERLAMA        = 1;
    public static final int SORT_MAG_TERTINGGI  = 2;
    public static final int SORT_MAG_TERENDAH   = 3;
    public static final int SORT_TERDANGKAL     = 4;

    // ========================
    // KONSTANTA DEPTH
    // ========================
    public static final int DEPTH_SEMUA    = 0;
    public static final int DEPTH_DANGKAL  = 1; // 0-60 km
    public static final int DEPTH_MENENGAH = 2; // 60-300 km
    public static final int DEPTH_DALAM    = 3; // >300 km

    // ========================
    // KONSTANTA TIME RANGE
    // ========================
    public static final int TIME_24JAM = 0;
    public static final int TIME_3HARI = 1;
    public static final int TIME_7HARI = 2;
    public static final int TIME_SEMUA = 3;

    // ========================
    // SAVE ke SharedPreferences
    // ========================
    public void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat(KEY_MAG_MIN,        magnitudeMin)
                .putFloat(KEY_MAG_MAX,        magnitudeMax)
                .putInt(KEY_DEPTH,            depthCategory)
                .putBoolean(KEY_BMKG_TERBARU, showBmkgTerbaru)
                .putBoolean(KEY_BMKG_DIRASAKAN, showBmkgDirasakan)
                .putBoolean(KEY_USGS,         showUsgs)
                .putInt(KEY_TIME_RANGE,       timeRange)
                .putInt(KEY_SORT_BY,          sortBy)
                .putString(KEY_KEYWORD,       searchKeyword)
                .apply();
    }

    // ========================
    // LOAD dari SharedPreferences
    // ========================
    public static FilterState load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);

        FilterState state = new FilterState();
        state.magnitudeMin      = prefs.getFloat(KEY_MAG_MIN, 0.0f);
        state.magnitudeMax      = prefs.getFloat(KEY_MAG_MAX, 9.0f);
        state.depthCategory     = prefs.getInt(KEY_DEPTH, 0);
        state.showBmkgTerbaru   = prefs.getBoolean(KEY_BMKG_TERBARU, true);
        state.showBmkgDirasakan = prefs.getBoolean(KEY_BMKG_DIRASAKAN, true);
        state.showUsgs          = prefs.getBoolean(KEY_USGS, true);
        state.timeRange         = prefs.getInt(KEY_TIME_RANGE, 0);
        state.sortBy            = prefs.getInt(KEY_SORT_BY, 0);
        state.searchKeyword     = prefs.getString(KEY_KEYWORD, "");
        return state;
    }

    // ========================
    // RESET ke default
    // ========================
    public void reset() {
        magnitudeMin      = 0.0f;
        magnitudeMax      = 9.0f;
        depthCategory     = 0;
        showBmkgTerbaru   = true;
        showBmkgDirasakan = true;
        showUsgs          = true;
        timeRange         = 0;
        sortBy            = 0;
        searchKeyword     = "";
    }

    // ========================
    // HITUNG JUMLAH FILTER AKTIF
    // (untuk badge di icon filter)
    // ========================
    public int getActiveFilterCount() {
        int count = 0;
        if (magnitudeMin != 0.0f || magnitudeMax != 9.0f) count++;
        if (depthCategory != 0) count++;
        if (!showBmkgTerbaru || !showBmkgDirasakan || !showUsgs) count++;
        if (timeRange != 0) count++;
        if (sortBy != 0) count++;
        return count;
    }

    // ========================
    // SUMMARY FILTER AKTIF
    // (teks di bawah chip)
    // ========================
    public String getFilterSummary() {
        if (getActiveFilterCount() == 0) return "";

        StringBuilder sb = new StringBuilder();

        if (magnitudeMin != 0.0f || magnitudeMax != 9.0f) {
            sb.append("M ").append(magnitudeMin).append("-").append(magnitudeMax);
        }

        if (depthCategory != 0) {
            if (sb.length() > 0) sb.append(" · ");
            switch (depthCategory) {
                case DEPTH_DANGKAL:  sb.append("Dangkal");  break;
                case DEPTH_MENENGAH: sb.append("Menengah"); break;
                case DEPTH_DALAM:    sb.append("Dalam");    break;
            }
        }

        if (timeRange != 0) {
            if (sb.length() > 0) sb.append(" · ");
            switch (timeRange) {
                case TIME_3HARI: sb.append("3 hari");  break;
                case TIME_7HARI: sb.append("7 hari");  break;
                case TIME_SEMUA: sb.append("Semua");   break;
            }
        }

        return sb.toString();
    }

    // ========================
    // CEK APAKAH SEMUA DEFAULT
    // ========================
    public boolean isDefault() {
        return getActiveFilterCount() == 0 && searchKeyword.isEmpty();
    }
}