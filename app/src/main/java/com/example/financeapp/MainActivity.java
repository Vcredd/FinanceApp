package com.example.financeapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.financeapp.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvSoDu, tvTongThu, tvTongChi;
    private RecyclerView recyclerView;
    private GiaoDichAdapter adapter;
    private List<GiaoDichAdapter.GiaoDichItem> giaoDichList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        tvSoDu = findViewById(R.id.tvSoDu);
        tvTongThu = findViewById(R.id.tvTongThu);
        tvTongChi = findViewById(R.id.tvTongChi);
        recyclerView = findViewById(R.id.recyclerViewGiaoDich);
        FloatingActionButton fab = findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        giaoDichList = new ArrayList<>();

        // Kiểm tra nếu chưa có dữ liệu thì chèn thử dữ liệu mẫu để kiểm tra kết nối
        insertSampleDataIfEmpty();

        // Cập nhật số dư và danh sách giao dịch từ CSDL
        updateDashboard();

        // Xử lý sự kiện bấm nút Thêm (+)
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboard(); // Cập nhật lại khi quay lại màn hình chính
    }

    private void insertSampleDataIfEmpty() {
        Cursor cursor = dbHelper.getAllGiaoDich();
        if (cursor == null || cursor.getCount() == 0) {
            // Thêm 1 khoản thu và 1 khoản chi làm dữ liệu mẫu
            dbHelper.addGiaoDich(25000000, "THU", "15/10/2026", "Lương tháng 10", 6);
            dbHelper.addGiaoDich(12500000, "CHI", "15/10/2026", "Mua sắm & Ăn uống", 1);
        }
        if (cursor != null) cursor.close();
    }

    private void updateDashboard() {
        // 1. Tính toán tổng thu, chi, số dư từ CSDL SQLite
        int tongThu = dbHelper.getTongTien("THU");
        int tongChi = dbHelper.getTongTien("CHI");
        int soDu = tongThu - tongChi;

        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyVN = NumberFormat.getCurrencyInstance(localeVN);

        tvTongThu.setText(currencyVN.format(tongThu));
        tvTongChi.setText(currencyVN.format(tongChi));
        tvSoDu.setText(currencyVN.format(soDu));

        // 2. Load danh sách giao dịch từ SQLite lên RecyclerView
        giaoDichList.clear();
        Cursor cursor = dbHelper.getAllGiaoDich();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int soTien = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GD_SOTIEN));
                String loai = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GD_LOAI));
                String ngay = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GD_NGAY));
                String ghiChu = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GD_GHICHU));
                String tenDanhMuc = cursor.getString(cursor.getColumnIndexOrThrow("ten_danh_muc"));

                giaoDichList.add(new GiaoDichAdapter.GiaoDichItem(tenDanhMuc, ghiChu, ngay, soTien, loai));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter = new GiaoDichAdapter(giaoDichList);
        recyclerView.setAdapter(adapter);
    }
}