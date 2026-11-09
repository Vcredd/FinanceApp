package com.example.financeapp;

import static com.example.financeapp.DatabaseHelper.TABLE_GIAO_DICH;
import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
                // Lấy thêm ID giao dịch từ SQLite
                int idGiaoDich = cursor.getInt(cursor.getColumnIndexOrThrow("id_giao_dich"));
                int soTien = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GD_SOTIEN));
                String loai = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GD_LOAI));
                String ngay = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GD_NGAY));
                String ghiChu = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GD_GHICHU));
                String tenDanhMuc = cursor.getString(cursor.getColumnIndexOrThrow("ten_danh_muc"));

                // Truyền ID vào Constructor mới
                giaoDichList.add(new GiaoDichAdapter.GiaoDichItem(idGiaoDich, tenDanhMuc, ghiChu, ngay, soTien, loai));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter = new GiaoDichAdapter(giaoDichList);

        // Xử lý sự kiện nhấn giữ
        adapter.setOnItemLongClickListener(item -> showOptionsDialog(item));

        recyclerView.setAdapter(adapter);
    }
    private void showOptionsDialog(GiaoDichAdapter.GiaoDichItem item) {
        String[] options = {"Chỉnh sửa", "Xóa giao dịch"};

        new AlertDialog.Builder(this)
                .setTitle("Tùy chọn")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Xử lý Chỉnh sửa: Mở AddTransactionActivity và truyền ID sang
                        Intent intent = new Intent(DashboardActivity.this, AddTransactionActivity.class);
                        intent.putExtra("EDIT_ID", item.idGiaoDich);
                        startActivity(intent);
                    } else if (which == 1) {
                        // Xử lý Xóa: Hiển thị thêm 1 dialog xác nhận cho an toàn
                        confirmDelete(item.idGiaoDich);
                    }
                })
                .show();
    }

    private void confirmDelete(int idGiaoDich) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.deleteGiaoDich(idGiaoDich);
                    loadFilteredData();      // Tải lại danh sách
                    updateDashboardHeader(); // Cập nhật lại tổng tiền số dư
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}