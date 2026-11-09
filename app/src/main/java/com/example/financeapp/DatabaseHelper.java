package com.example.financeapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "QuanLyChiTieu.db";
    private static final int DATABASE_VERSION = 1;

    // Tên Bảng
    public static final String TABLE_DANH_MUC = "DANH_MUC";
    public static final String TABLE_GIAO_DICH = "GIAO_DICH";

    // Cột bảng DANH_MUC
    public static final String COL_DM_ID = "id_danh_muc";
    public static final String COL_DM_TEN = "ten_danh_muc";
    public static final String COL_DM_LOAI = "loai"; // 'THU' hoặc 'CHI'
    public static final String COL_DM_ICON = "bieu_tuong";
    public static final String COL_DM_MAC_DINH = "mac_dinh";

    // Cột bảng GIAO_DICH
    public static final String COL_GD_ID = "id_giao_dich";
    public static final String COL_GD_SOTIEN = "so_tien";
    public static final String COL_GD_LOAI = "loai";
    public static final String COL_GD_NGAY = "ngay_giao_dich";
    public static final String COL_GD_GHICHU = "ghi_chu";
    public static final String COL_GD_ID_DANHMUC = "id_danh_muc";

    // Lệnh tạo bảng
    private static final String CREATE_TABLE_DANH_MUC = "CREATE TABLE " + TABLE_DANH_MUC + "("
            + COL_DM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_DM_TEN + " TEXT NOT NULL,"
            + COL_DM_LOAI + " TEXT NOT NULL,"
            + COL_DM_ICON + " TEXT,"
            + COL_DM_MAC_DINH + " INTEGER DEFAULT 1"
            + ")";

    private static final String CREATE_TABLE_GIAO_DICH = "CREATE TABLE " + TABLE_GIAO_DICH + "("
            + COL_GD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_GD_SOTIEN + " INTEGER NOT NULL,"
            + COL_GD_LOAI + " TEXT NOT NULL,"
            + COL_GD_NGAY + " TEXT NOT NULL,"
            + COL_GD_GHICHU + " TEXT,"
            + COL_GD_ID_DANHMUC + " INTEGER NOT NULL,"
            + "FOREIGN KEY(" + COL_GD_ID_DANHMUC + ") REFERENCES " + TABLE_DANH_MUC + "(" + COL_DM_ID + ")"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // Bật khóa ngoại
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_DANH_MUC);
        db.execSQL(CREATE_TABLE_GIAO_DICH);

        // Tạo Index để tăng tốc độ truy vấn theo yêu cầu NFR
        db.execSQL("CREATE INDEX idx_gd_ngay ON " + TABLE_GIAO_DICH + "(" + COL_GD_NGAY + ")");
        db.execSQL("CREATE INDEX idx_gd_danhmuc ON " + TABLE_GIAO_DICH + "(" + COL_GD_ID_DANHMUC + ")");

        // Thêm dữ liệu danh mục mẫu
        insertDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GIAO_DICH);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DANH_MUC);
        onCreate(db);
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        String[] chi = {"Ăn uống", "Mua sắm", "Di chuyển", "Hóa đơn", "Giải trí"};
        for (String ten : chi) {
            ContentValues values = new ContentValues();
            values.put(COL_DM_TEN, ten);
            values.put(COL_DM_LOAI, "CHI");
            db.insert(TABLE_DANH_MUC, null, values);
        }

        String[] thu = {"Lương", "Thưởng", "Bán đồ"};
        for (String ten : thu) {
            ContentValues values = new ContentValues();
            values.put(COL_DM_TEN, ten);
            values.put(COL_DM_LOAI, "THU");
            db.insert(TABLE_DANH_MUC, null, values);
        }
    }

    // Hàm CRUD cơ bản cho GIAO_DICH
    public long addGiaoDich(int soTien, String loai, String ngay, String ghiChu, int idDanhMuc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_GD_SOTIEN, soTien);
        values.put(COL_GD_LOAI, loai);
        values.put(COL_GD_NGAY, ngay);
        values.put(COL_GD_GHICHU, ghiChu);
        values.put(COL_GD_ID_DANHMUC, idDanhMuc);
        return db.insert(TABLE_GIAO_DICH, null, values);
    }

    public Cursor getDanhMucByLoai(String loai) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_DANH_MUC + " WHERE loai = ?", new String[]{loai});
    }
    public Cursor getAllGiaoDich() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT g.*, d.ten_danh_muc, d.bieu_tuong FROM " + TABLE_GIAO_DICH + " g " +
                "INNER JOIN " + TABLE_DANH_MUC + " d ON g.id_danh_muc = d.id_danh_muc " +
                "ORDER BY g.ngay_giao_dich DESC";
        return db.rawQuery(query, null);
    }

    public Cursor getGiaoDichFiltered(String timeFilter, String typeFilter) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder query = new StringBuilder(
                "SELECT g.*, d.ten_danh_muc, d.bieu_tuong FROM " + TABLE_GIAO_DICH + " g " +
                        "INNER JOIN " + TABLE_DANH_MUC + " d ON g.id_danh_muc = d.id_danh_muc WHERE 1=1 "
        );

        List<String> argsList = new ArrayList<>();

        // 1. Lọc theo thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        if ("TODAY".equals(timeFilter)) {
            String todayStr = sdf.format(cal.getTime());
            query.append("AND g.ngay_giao_dich = ? ");
            argsList.add(todayStr);
        } else if ("THIS_MONTH".equals(timeFilter)) {
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("/MM/yyyy", Locale.getDefault());
            String currentMonthYear = monthYearFormat.format(cal.getTime());
            query.append("AND g.ngay_giao_dich LIKE ? ");
            argsList.add("%" + currentMonthYear);
        }

        // 2. Lọc theo loại (THU/CHI)
        if ("THU".equals(typeFilter)) {
            query.append("AND g.loai = 'THU' ");
        } else if ("CHI".equals(typeFilter)) {
            query.append("AND g.loai = 'CHI' ");
        }

        // Sắp xếp ngày mới nhất lên đầu
        query.append("ORDER BY g.id_giao_dich DESC");

        String[] args = argsList.toArray(new String[0]);
        return db.rawQuery(query.toString(), args);
    }

    // Tính tổng theo loại
    public int getTongTien(String loai) {
        SQLiteDatabase db = this.getReadableDatabase();
        int tong = 0;
        Cursor cursor = db.rawQuery("SELECT SUM(so_tien) FROM " + TABLE_GIAO_DICH + " WHERE loai = ?", new String[]{loai});
        if (cursor.moveToFirst()) {
            tong = cursor.getInt(0);
        }
        cursor.close();
        return tong;
    }

    public void deleteGiaoDich(int idGiaoDich) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GIAO_DICH, "id_giao_dich = ?", new String[]{String.valueOf(idGiaoDich)});
        db.close();
    }
}