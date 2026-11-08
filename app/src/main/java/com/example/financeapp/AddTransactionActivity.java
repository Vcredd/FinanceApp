package com.example.financeapp;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.financeapp.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private MaterialButtonToggleGroup toggleLoai;
    private TextInputEditText etSoTien, etNgayGiaoDich, etGhiChu;
    private Spinner spinnerDanhMuc;
    private MaterialButton btnLuuGiaoDich;

    private String currentLoai = "CHI"; // Mặc định chọn Chi tiền
    private final Calendar calendar = Calendar.getInstance();
    private final List<Integer> listIdDanhMuc = new ArrayList<>();
    private final List<String> listTenDanhMuc = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ View
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        toggleLoai = findViewById(R.id.toggleLoaiGiaoDich);
        etSoTien = findViewById(R.id.etSoTien);
        etNgayGiaoDich = findViewById(R.id.etNgayGiaoDich);
        etGhiChu = findViewById(R.id.etGhiChu);
        spinnerDanhMuc = findViewById(R.id.spinnerDanhMuc);
        btnLuuGiaoDich = findViewById(R.id.btnLuuGiaoDich);

        // Mặc định thiết lập ngày hiện tại
        updateDateField();

        // Load danh mục loại CHI mặc định
        loadDanhMuc(currentLoai);

        // Xử lý chuyển đổi giữa CHI và THU
        toggleLoai.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnLoaiChi) {
                    currentLoai = "CHI";
                } else if (checkedId == R.id.btnLoaiThu) {
                    currentLoai = "THU";
                }
                loadDanhMuc(currentLoai);
            }
        });

        // Chọn ngày bằng DatePickerDialog
        etNgayGiaoDich.setOnClickListener(v -> {
            new DatePickerDialog(AddTransactionActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateField();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Xử lý nút Lưu Giao Dịch
        btnLuuGiaoDich.setOnClickListener(v -> luuGiaoDich());
    }

    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etNgayGiaoDich.setText(sdf.format(calendar.getTime()));
    }

    private void loadDanhMuc(String loai) {
        listIdDanhMuc.clear();
        listTenDanhMuc.clear();

        Cursor cursor = dbHelper.getDanhMucByLoai(loai);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DM_ID));
                String ten = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DM_TEN));

                listIdDanhMuc.add(id);
                listTenDanhMuc.add(ten);
            } while (cursor.moveToNext());
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listTenDanhMuc);
        spinnerDanhMuc.setAdapter(adapter);
    }

    private void luuGiaoDich() {
        String strSoTien = etSoTien.getText().toString().trim();
        String ngay = etNgayGiaoDich.getText().toString().trim();
        String ghiChu = etGhiChu.getText().toString().trim();

        // Kiểm tra hợp lệ (Luồng ngoại lệ Use Case UC1.1)
        if (strSoTien.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        int soTien;
        try {
            soTien = Integer.parseInt(strSoTien);
            if (soTien <= 0) {
                Toast.makeText(this, "Số tiền phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền nhập không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (listIdDanhMuc.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn danh mục!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPos = spinnerDanhMuc.getSelectedItemPosition();
        int idDanhMuc = listIdDanhMuc.get(selectedPos);

        // Lưu bản ghi mới vào SQLite
        long result = dbHelper.addGiaoDich(soTien, currentLoai, ngay, ghiChu, idDanhMuc);

        if (result > 0) {
            Toast.makeText(this, "Thêm giao dịch thành công!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng màn hình và trở về Dashboard
        } else {
            Toast.makeText(this, "Lỗi khi lưu giao dịch!", Toast.LENGTH_SHORT).show();
        }
    }
}
