package com.siagaid.filter;

import com.siagaid.model.Gempa;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class untuk filter dan sort list gempa.
 * Semua operasi bekerja secara LOKAL (tidak fetch ulang API).
 */
public class GempaFilterHelper {

    // ========================
    // FILTER MAGNITUDO
    // ========================
    public static List<Gempa> filterByMagnitude(List<Gempa> list,
                                                float min, float max) {
        List<Gempa> result = new ArrayList<>();
        for (Gempa g : list) {
            double mag = g.getMagnitudeDouble();
            if (mag >= min && mag <= max) result.add(g);
        }
        return result;
    }

    // ========================
    // FILTER KEDALAMAN
    // ========================
    public static List<Gempa> filterByDepth(List<Gempa> list, int category) {
        if (category == FilterState.DEPTH_SEMUA) return new ArrayList<>(list);

        List<Gempa> result = new ArrayList<>();
        for (Gempa g : list) {
            int depth = g.getKedalamanInt();
            switch (category) {
                case FilterState.DEPTH_DANGKAL:
                    if (depth >= 0 && depth <= 60) result.add(g);
                    break;
                case FilterState.DEPTH_MENENGAH:
                    if (depth > 60 && depth <= 300) result.add(g);
                    break;
                case FilterState.DEPTH_DALAM:
                    if (depth > 300) result.add(g);
                    break;
            }
        }
        return result;
    }

    // ========================
    // FILTER SUMBER DATA
    // ========================
    public static List<Gempa> filterBySource(List<Gempa> list,
                                             boolean bmkgTerbaru,
                                             boolean bmkgDirasakan,
                                             boolean usgs) {
        // Kalau semua aktif, return semua
        if (bmkgTerbaru && bmkgDirasakan && usgs) return new ArrayList<>(list);

        List<Gempa> result = new ArrayList<>();
        for (Gempa g : list) {
            String sumber = g.getSumber();
            if (sumber == null) sumber = "";

            if (bmkgTerbaru && sumber.equals("BMKG_TERBARU")) {
                result.add(g);
            } else if (bmkgDirasakan && sumber.equals("BMKG_DIRASAKAN")) {
                result.add(g);
            } else if (usgs && sumber.equals("USGS")) {
                result.add(g);
            }
        }
        return result;
    }

    // ========================
    // FILTER WAKTU
    // ========================
    public static List<Gempa> filterByTime(List<Gempa> list, int timeRange) {
        if (timeRange == FilterState.TIME_SEMUA) return new ArrayList<>(list);

        long now = System.currentTimeMillis();
        long rangeMillis;

        switch (timeRange) {
            case FilterState.TIME_24JAM:
                rangeMillis = 24L * 60 * 60 * 1000;
                break;
            case FilterState.TIME_3HARI:
                rangeMillis = 3L * 24 * 60 * 60 * 1000;
                break;
            case FilterState.TIME_7HARI:
                rangeMillis = 7L * 24 * 60 * 60 * 1000;
                break;
            default:
                return new ArrayList<>(list);
        }

        List<Gempa> result = new ArrayList<>();
        for (Gempa g : list) {
            long gempaTime = parseGempaTime(g);
            if (gempaTime > 0 && (now - gempaTime) <= rangeMillis) {
                result.add(g);
            } else if (gempaTime <= 0) {
                // Kalau tidak bisa parse waktu, tetap masukkan
                result.add(g);
            }
        }
        return result;
    }

    // ========================
    // FILTER KEYWORD (SEARCH)
    // ========================
    public static List<Gempa> filterByKeyword(List<Gempa> list, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>(list);
        }

        String query = keyword.toLowerCase(Locale.getDefault()).trim();
        List<Gempa> result = new ArrayList<>();

        for (Gempa g : list) {
            String wilayah = g.getWilayah() != null ?
                    g.getWilayah().toLowerCase(Locale.getDefault()) : "";
            String tanggal = g.getTanggal() != null ?
                    g.getTanggal().toLowerCase(Locale.getDefault()) : "";
            String magnitude = g.getMagnitude() != null ?
                    g.getMagnitude().toLowerCase(Locale.getDefault()) : "";

            if (wilayah.contains(query)
                    || tanggal.contains(query)
                    || magnitude.contains(query)) {
                result.add(g);
            }
        }
        return result;
    }

    // ========================
    // SORT LIST
    // ========================
    public static List<Gempa> sortList(List<Gempa> list, int sortBy) {
        List<Gempa> result = new ArrayList<>(list);

        switch (sortBy) {
            case FilterState.SORT_TERBARU:
                Collections.sort(result, (a, b) -> {
                    long timeA = parseGempaTime(a);
                    long timeB = parseGempaTime(b);
                    return Long.compare(timeB, timeA); // DESC
                });
                break;

            case FilterState.SORT_TERLAMA:
                Collections.sort(result, (a, b) -> {
                    long timeA = parseGempaTime(a);
                    long timeB = parseGempaTime(b);
                    return Long.compare(timeA, timeB); // ASC
                });
                break;

            case FilterState.SORT_MAG_TERTINGGI:
                Collections.sort(result, (a, b) ->
                        Double.compare(b.getMagnitudeDouble(),
                                a.getMagnitudeDouble())); // DESC
                break;

            case FilterState.SORT_MAG_TERENDAH:
                Collections.sort(result, (a, b) ->
                        Double.compare(a.getMagnitudeDouble(),
                                b.getMagnitudeDouble())); // ASC
                break;

            case FilterState.SORT_TERDANGKAL:
                Collections.sort(result, (a, b) ->
                        Integer.compare(a.getKedalamanInt(),
                                b.getKedalamanInt())); // ASC
                break;
        }

        return result;
    }

    // ========================
    // APPLY ALL FILTERS
    // ========================
    public static List<Gempa> applyAllFilters(List<Gempa> originalList,
                                              FilterState state) {
        if (originalList == null || originalList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Gempa> result = new ArrayList<>(originalList);

        // 1. Filter magnitude
        result = filterByMagnitude(result, state.magnitudeMin, state.magnitudeMax);

        // 2. Filter kedalaman
        result = filterByDepth(result, state.depthCategory);

        // 3. Filter sumber
        result = filterBySource(result,
                state.showBmkgTerbaru,
                state.showBmkgDirasakan,
                state.showUsgs);

        // 4. Filter waktu
        result = filterByTime(result, state.timeRange);

        // 5. Filter keyword search
        if (state.searchKeyword != null && !state.searchKeyword.isEmpty()) {
            result = filterByKeyword(result, state.searchKeyword);
        }

        // 6. Sort
        result = sortList(result, state.sortBy);

        return result;
    }

    // ========================
    // HELPER: Parse waktu gempa
    // ========================
    private static long parseGempaTime(Gempa gempa) {
        try {
            // Coba parse DateTime dulu
            if (gempa.getDateTime() != null && !gempa.getDateTime().isEmpty()) {
                // Format BMKG: "2024-01-15T10:30:00+07:00"
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
                Date date = sdf.parse(gempa.getDateTime());
                if (date != null) return date.getTime();
            }

            // Fallback: parse Tanggal + Jam
            if (gempa.getTanggal() != null && gempa.getJam() != null) {
                String dateStr = gempa.getTanggal() + " " + gempa.getJam();
                // Format BMKG: "15 Jan 2024" + "10:30:00 WIB"
                dateStr = dateStr.replace(" WIB", "")
                        .replace(" WITA", "")
                        .replace(" WIT", "").trim();
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "dd MMM yyyy HH:mm:ss", Locale.getDefault());
                Date date = sdf.parse(dateStr);
                if (date != null) return date.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}