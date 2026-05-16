package com.siagaid.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper untuk membuat dan mengelola database SQLite.
 * Menyimpan data bookmark gempa.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // ========================
    // KONSTANTA DATABASE
    // ========================
    private static final String DATABASE_NAME    = "siagaid.db";
    private static final int    DATABASE_VERSION  = 1;

    // ========================
    // TABEL BOOKMARK
    // ========================
    public static final String TABLE_BOOKMARK      = "bookmark";
    public static final String COL_ID              = "id";
    public static final String COL_TANGGAL         = "tanggal";
    public static final String COL_JAM             = "jam";
    public static final String COL_DATETIME        = "datetime";
    public static final String COL_KOORDINAT       = "koordinat";
    public static final String COL_LINTANG         = "lintang";
    public static final String COL_BUJUR           = "bujur";
    public static final String COL_MAGNITUDE       = "magnitude";
    public static final String COL_KEDALAMAN       = "kedalaman";
    public static final String COL_WILAYAH         = "wilayah";
    public static final String COL_POTENSI         = "potensi";
    public static final String COL_DIRASAKAN       = "dirasakan";
    public static final String COL_SHAKEMAP        = "shakemap";
    public static final String COL_SUMBER          = "sumber";
    public static final String COL_SAVED_AT        = "saved_at";

    // ========================
    // CREATE TABLE SQL
    // ========================
    private static final String CREATE_TABLE_BOOKMARK =
            "CREATE TABLE " + TABLE_BOOKMARK + " (" +
                    COL_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TANGGAL   + " TEXT, " +
                    COL_JAM       + " TEXT, " +
                    COL_DATETIME  + " TEXT, " +
                    COL_KOORDINAT + " TEXT, " +
                    COL_LINTANG   + " TEXT, " +
                    COL_BUJUR     + " TEXT, " +
                    COL_MAGNITUDE + " TEXT, " +
                    COL_KEDALAMAN + " TEXT, " +
                    COL_WILAYAH   + " TEXT, " +
                    COL_POTENSI   + " TEXT, " +
                    COL_DIRASAKAN + " TEXT, " +
                    COL_SHAKEMAP  + " TEXT, " +
                    COL_SUMBER    + " TEXT, " +
                    COL_SAVED_AT  + " INTEGER" +  // epoch millis
                    ")";

    // ========================
    // SINGLETON
    // ========================
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ========================
    // LIFECYCLE
    // ========================

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BOOKMARK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop table lama, buat ulang
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARK);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign keys jika diperlukan nanti
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON");
        }
    }
}