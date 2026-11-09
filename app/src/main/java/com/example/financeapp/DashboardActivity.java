package com.example.financeapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.financeapp.DatabaseHelper;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvSoDu, tvTongThu, tvTongChi;
    private RecyclerView recyclerView;
    private GiaoDichAdapter adapter;
    private List<GiaoDichAdapter.GiaoDichItem> giaoDichList;

    // Lưu trạng thái bộ lọc hiện tại
    private String selectedTimeFilter = "TODAY"; // TODAY, THIS_MONTH, ALL
    private String selectedTypeFilter = "ALL";   // ALL, THU, CHI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);

        tvSoDu = findViewById(R.id.tvSoDu);
        tvTongThu = findViewById(R.id.tvTongThu);
        tvTongChi = findViewById(R.id.tvTongChi);
        recyclerView = findViewById(R.id.recyclerViewGiaoDich);
        FloatingActionButton fab = findViewById(R.id.fabAdd);

        ChipGroup chipGroupTime = findViewById(R.id.chipGroupTime);
        ChipGroup chipGroupType = findViewById(R.id.chipGroupType);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        giaoDichList = new ArrayList<>();

        // Nút thêm mới (+)
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        // 1. Sự kiện chọn Lọc Thời Gian (Hôm nay / Tháng này / Tất cả)
        chipGroupTime.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipToday) {
                selectedTimeFilter = "TODAY";
            } else if (checkedId == R.id.chipThisMonth) {
                selectedTimeFilter = "THIS_MONTH";
            } else if (checkedId == R.id.chipAllTime) {
                selectedTimeFilter = "ALL";
            }
            loadFilteredData();
        });

        // 2. Sự kiện chọn Lọc Loại (Tất cả / Thu / Chi)
        chipGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAllType) {
                selectedTypeFilter = "ALL";
            } else if (checkedId == R.id.chipIncome) {
                selectedTypeFilter = "THU";
            } else if (checkedId == R.id.chipExpense) {
                selectedTypeFilter = "CHI";
            }
            loadFilteredData();
        });

        updateDashboardHeader();
        loadFilteredData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboardHeader();
        loadFilteredData();
    }

    // Cập nhật số tổng quan ở thẻ trên cùng
    private void updateDashboardHeader() {
        int tongThu = dbHelper.getTongTien("THU");
        int tongChi = dbHelper.getTongTien("CHI");
        int soDu = tongThu - tongChi;

        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyVN = NumberFormat.getCurrencyInstance(localeVN);

        tvTongThu.setText(currencyVN.format(tongThu));
        tvTongChi.setText(currencyVN.format(tongChi));
        tvSoDu.setText(currencyVN.format(soDu));
    }

    // Tải lại danh sách theo bộ lọc
    private void loadFilteredData() {
        giaoDichList.clear();
        Cursor cursor = dbHelper.getGiaoDichFiltered(selectedTimeFilter, selectedTypeFilter);

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