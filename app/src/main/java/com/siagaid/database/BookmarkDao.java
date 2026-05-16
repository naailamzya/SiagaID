package com.siagaid.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.siagaid.model.Gempa;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Data Access Object untuk tabel bookmark.
 * Semua operasi database dijalankan di background thread (Executor).
 * Callback hasil dikembalikan ke UI thread via Handler.
 */
public class BookmarkDao {

    private final DatabaseHelper dbHelper;

    // Background thread executor
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Handler untuk update UI dari background thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ========================
    // CALLBACK INTERFACES
    // ========================
    public interface OnBookmarkListCallback {
        void onResult(List<Gempa> bookmarks);
    }

    public interface OnBookmarkCheckCallback {
        void onResult(boolean isBookmarked);
    }

    public interface OnBookmarkActionCallback {
        void onResult(boolean success);
    }

    // ========================
    // CONSTRUCTOR
    // ========================
    public BookmarkDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ========================
    // INSERT (Simpan bookmark)
    // ========================
    public void insert(Gempa gempa, OnBookmarkActionCallback callback) {
        executor.execute(() -> {
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COL_TANGGAL,   gempa.getTanggal());
                values.put(DatabaseHelper.COL_JAM,       gempa.getJam());
                values.put(DatabaseHelper.COL_DATETIME,  gempa.getDateTime());
                values.put(DatabaseHelper.COL_KOORDINAT, gempa.getCoordinates());
                values.put(DatabaseHelper.COL_LINTANG,   gempa.getLintang());
                values.put(DatabaseHelper.COL_BUJUR,     gempa.getBujur());
                values.put(DatabaseHelper.COL_MAGNITUDE, gempa.getMagnitude());
                values.put(DatabaseHelper.COL_KEDALAMAN, gempa.getKedalaman());
                values.put(DatabaseHelper.COL_WILAYAH,   gempa.getWilayah());
                values.put(DatabaseHelper.COL_POTENSI,   gempa.getPotensi());
                values.put(DatabaseHelper.COL_DIRASAKAN, gempa.getDirasakan());
                values.put(DatabaseHelper.COL_SHAKEMAP,  gempa.getShakemap());
                values.put(DatabaseHelper.COL_SUMBER,    gempa.getSumber());
                values.put(DatabaseHelper.COL_SAVED_AT,  System.currentTimeMillis());

                long result = db.insert(DatabaseHelper.TABLE_BOOKMARK, null, values);

                boolean success = result != -1;
                mainHandler.post(() -> {
                    if (callback != null) callback.onResult(success);
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    if (callback != null) callback.onResult(false);
                });
            }
        });
    }

    // ========================
    // DELETE (Hapus bookmark)
    // ========================
    public void delete(Gempa gempa, OnBookmarkActionCallback callback) {
        executor.execute(() -> {
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                // Hapus berdasarkan kombinasi unik: DateTime + Wilayah
                int rows = db.delete(
                        DatabaseHelper.TABLE_BOOKMARK,
                        DatabaseHelper.COL_DATETIME + " = ? AND " +
                                DatabaseHelper.COL_WILAYAH  + " = ?",
                        new String[]{gempa.getDateTime(), gempa.getWilayah()}
                );

                boolean success = rows > 0;
                mainHandler.post(() -> {
                    if (callback != null) callback.onResult(success);
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    if (callback != null) callback.onResult(false);
                });
            }
        });
    }

    // ========================
    // DELETE BY ID
    // ========================
    public void deleteById(int id, OnBookmarkActionCallback callback) {
        executor.execute(() -> {
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                int rows = db.delete(
                        DatabaseHelper.TABLE_BOOKMARK,
                        DatabaseHelper.COL_ID + " = ?",
                        new String[]{String.valueOf(id)}
                );
                boolean success = rows > 0;
                mainHandler.post(() -> {
                    if (callback != null) callback.onResult(success);
                });
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    if (callback != null) callback.onResult(false);
                });
            }
        });
    }

    // ========================
    // GET ALL (Ambil semua bookmark)
    // ========================
    public void getAll(OnBookmarkListCallback callback) {
        executor.execute(() -> {
            List<Gempa> list = new ArrayList<>();
            try {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.query(
                        DatabaseHelper.TABLE_BOOKMARK,
                        null,
                        null,
                        null,
                        null,
                        null,
                        DatabaseHelper.COL_SAVED_AT + " DESC" // terbaru dulu
                );

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        Gempa gempa = cursorToGempa(cursor);
                        list.add(gempa);
                    }
                    cursor.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            mainHandler.post(() -> {
                if (callback != null) callback.onResult(list);
            });
        });
    }

    // ========================
    // IS BOOKMARKED (Cek status)
    // ========================
    public void isBookmarked(Gempa gempa, OnBookmarkCheckCallback callback) {
        executor.execute(() -> {
            boolean found = false;
            try {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.query(
                        DatabaseHelper.TABLE_BOOKMARK,
                        new String[]{DatabaseHelper.COL_ID},
                        DatabaseHelper.COL_DATETIME + " = ? AND " +
                                DatabaseHelper.COL_WILAYAH  + " = ?",
                        new String[]{gempa.getDateTime(), gempa.getWilayah()},
                        null, null, null
                );

                if (cursor != null) {
                    found = cursor.getCount() > 0;
                    cursor.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean finalFound = found;
            mainHandler.post(() -> {
                if (callback != null) callback.onResult(finalFound);
            });
        });
    }

    // ========================
    // HELPER: Cursor → Gempa
    // ========================
    private Gempa cursorToGempa(Cursor cursor) {
        Gempa gempa = new Gempa();
        try {
            gempa.setTanggal(getStringOrEmpty(cursor, DatabaseHelper.COL_TANGGAL));
            gempa.setJam(getStringOrEmpty(cursor, DatabaseHelper.COL_JAM));
            gempa.setDateTime(getStringOrEmpty(cursor, DatabaseHelper.COL_DATETIME));
            gempa.setCoordinates(getStringOrEmpty(cursor, DatabaseHelper.COL_KOORDINAT));
            gempa.setLintang(getStringOrEmpty(cursor, DatabaseHelper.COL_LINTANG));
            gempa.setBujur(getStringOrEmpty(cursor, DatabaseHelper.COL_BUJUR));
            gempa.setMagnitude(getStringOrEmpty(cursor, DatabaseHelper.COL_MAGNITUDE));
            gempa.setKedalaman(getStringOrEmpty(cursor, DatabaseHelper.COL_KEDALAMAN));
            gempa.setWilayah(getStringOrEmpty(cursor, DatabaseHelper.COL_WILAYAH));
            gempa.setPotensi(getStringOrEmpty(cursor, DatabaseHelper.COL_POTENSI));
            gempa.setDirasakan(getStringOrEmpty(cursor, DatabaseHelper.COL_DIRASAKAN));
            gempa.setShakemap(getStringOrEmpty(cursor, DatabaseHelper.COL_SHAKEMAP));
            gempa.setSumber(getStringOrEmpty(cursor, DatabaseHelper.COL_SUMBER));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gempa;
    }

    private String getStringOrEmpty(Cursor cursor, String columnName) {
        try {
            int index = cursor.getColumnIndex(columnName);
            if (index >= 0 && !cursor.isNull(index)) {
                return cursor.getString(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // ========================
    // SHUTDOWN EXECUTOR
    // ========================
    public void shutdown() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }
}