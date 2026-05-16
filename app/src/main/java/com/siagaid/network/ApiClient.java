package com.siagaid.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * Singleton Retrofit client.
 * Karena BMKG dan USGS dan OWM punya base URL berbeda,
 * kita buat 3 instance terpisah.
 */
public class ApiClient {

    // ========================
    // BASE URLs
    // ========================
    private static final String BASE_URL_BMKG = "https://data.bmkg.go.id/";
    private static final String BASE_URL_USGS = "https://earthquake.usgs.gov/";
    private static final String BASE_URL_OWM  = "https://api.openweathermap.org/";

    // ========================
    // SINGLETON INSTANCES
    // ========================
    private static Retrofit retrofitBmkg;
    private static Retrofit retrofitUsgs;
    private static Retrofit retrofitOwm;

    // ========================
    // OkHttp CLIENT (shared)
    // ========================
    private static OkHttpClient getOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    // ========================
    // BMKG CLIENT
    // ========================
    public static Retrofit getBmkgClient() {
        if (retrofitBmkg == null) {
            retrofitBmkg = new Retrofit.Builder()
                    .baseUrl(BASE_URL_BMKG)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitBmkg;
    }

    // ========================
    // USGS CLIENT
    // ========================
    public static Retrofit getUsgsClient() {
        if (retrofitUsgs == null) {
            retrofitUsgs = new Retrofit.Builder()
                    .baseUrl(BASE_URL_USGS)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitUsgs;
    }

    // ========================
    // OPENWEATHERMAP CLIENT
    // ========================
    public static Retrofit getOwmClient() {
        if (retrofitOwm == null) {
            retrofitOwm = new Retrofit.Builder()
                    .baseUrl(BASE_URL_OWM)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitOwm;
    }

    // ========================
    // API SERVICE INSTANCES
    // ========================
    public static ApiService getBmkgService() {
        return getBmkgClient().create(ApiService.class);
    }

    public static ApiService getUsgsService() {
        return getUsgsClient().create(ApiService.class);
    }

    public static ApiService getOwmService() {
        return getOwmClient().create(ApiService.class);
    }
}