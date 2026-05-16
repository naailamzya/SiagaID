package com.siagaid.network;

import com.google.gson.annotations.SerializedName;
import com.siagaid.model.Gempa;
import java.util.List;

/**
 * Wrapper untuk semua response JSON dari BMKG.
 */
public class BmkgResponse {

    // ========================
    // RESPONSE: autogempa.json (1 gempa terbaru)
    // ========================
    public static class AutoGempaResponse {
        @SerializedName("Infogempa")
        public InfoGempa Infogempa;

        public static class InfoGempa {
            @SerializedName("gempa")
            public Gempa gempa;
        }
    }

    // ========================
    // RESPONSE: gempaterkini.json (15 gempa terakhir)
    // ========================
    public static class GempaTerkiniResponse {
        @SerializedName("Infogempa")
        public InfoGempaList Infogempa;

        public static class InfoGempaList {
            @SerializedName("gempa")
            public List<Gempa> gempa;
        }
    }

    // ========================
    // RESPONSE: gempadirasakan.json (gempa yang dirasakan)
    // ========================
    public static class GempaDirasakanResponse {
        @SerializedName("Infogempa")
        public InfoGempaList Infogempa;

        public static class InfoGempaList {
            @SerializedName("gempa")
            public List<Gempa> gempa;
        }
    }
}