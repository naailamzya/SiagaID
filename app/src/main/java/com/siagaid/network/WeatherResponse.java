package com.siagaid.network;

import java.util.List;

/**
 * Model response dari OpenWeatherMap API.
 */
public class WeatherResponse {

    public String name;         // Nama kota
    public Main main;
    public Wind wind;
    public List<Weather> weather;
    public Sys sys;
    public int visibility;
    public Clouds clouds;

    public static class Main {
        public double temp;
        public double feels_like;
        public double temp_min;
        public double temp_max;
        public int humidity;
        public int pressure;
    }

    public static class Wind {
        public double speed;
        public int deg;
    }

    public static class Weather {
        public int id;
        public String main;
        public String description;
        public String icon;
    }

    public static class Sys {
        public String country;
        public long sunrise;
        public long sunset;
    }

    public static class Clouds {
        public int all;
    }
}