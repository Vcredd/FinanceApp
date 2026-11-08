package com.example.financeapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class GiaoDichAdapter extends RecyclerView.Adapter<GiaoDichAdapter.ViewHolder> {

    public static class GiaoDichItem {
        public String tenDanhMuc;
        public String ghiChu;
        public String ngay;
        public int soTien;
        public String loai;

        public GiaoDichItem(String tenDanhMuc, String ghiChu, String ngay, int soTien, String loai) {
            this.tenDanhMuc = tenDanhMuc;
            this.ghiChu = ghiChu;
            this.ngay = ngay;
            this.soTien = soTien;
            this.loai = loai;
        }
    }

    private List<GiaoDichItem> list;

    public GiaoDichAdapter(List<GiaoDichItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_giao_dich, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GiaoDichItem item = list.get(position);
        holder.tvTenDanhMuc.setText(item.tenDanhMuc);
        holder.tvGhiChu.setText(item.ghiChu);
        holder.tvNgay.setText(item.ngay);

        // Đổi Icon và Màu nền theo Tên danh mục
        setCategoryIconAndBackground(holder, item.tenDanhMuc);

        // Đổi màu số tiền (Đỏ nếu Chi, Xanh nếu Thu)
        NumberFormat currencyVN = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        if ("CHI".equals(item.loai)) {
            holder.tvSoTien.setText("- " + currencyVN.format(item.soTien));
            holder.tvSoTien.setTextColor(Color.parseColor("#F44336"));
        } else {
            holder.tvSoTien.setText("+ " + currencyVN.format(item.soTien));
            holder.tvSoTien.setTextColor(Color.parseColor("#4CAF50"));
        }
    }

    private void setCategoryIconAndBackground(ViewHolder holder, String tenDanhMuc) {
        int iconRes = R.drawable.ic_food;
        String bgColor = "#FFE0B2"; // Cam nhạt mặc định

        if (tenDanhMuc != null) {
            switch (tenDanhMuc.trim()) {
                case "Ăn uống":
                    iconRes = R.drawable.ic_food;
                    bgColor = "#FFE0B2"; // Cam nhạt
                    break;
                case "Mua sắm":
                    iconRes = R.drawable.ic_shopping;
                    bgColor = "#FFCDD2"; // Đỏ nhạt
                    break;
                case "Di chuyển":
                    iconRes = R.drawable.ic_transport;
                    bgColor = "#BBDEFB"; // Xanh dương nhạt
                    break;
                case "Hóa đơn":
                    iconRes = R.drawable.ic_bill;
                    bgColor = "#E0F2F1"; // Xanh ngọc nhạt
                    break;
                case "Giải trí":
                    iconRes = R.drawable.ic_entertainment;
                    bgColor = "#F8BBD0"; // Hồng nhạt
                    break;
                case "Lương":
                case "Thưởng":
                    iconRes = android.R.drawable.ic_menu_compass;
                    bgColor = "#C8E6C9"; // Xanh lá nhạt
                    break;
                default:
                    iconRes = R.drawable.ic_food;
                    bgColor = "#E0E0E0"; // Xám nhạt
                    break;
            }
        }

        holder.ivIcon.setImageResource(iconRes);

        // Đổi màu nền hình tròn linh hoạt
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_circle_icon);
        if (drawable != null) {
            GradientDrawable mutableDrawable = (GradientDrawable) drawable.mutate();
            mutableDrawable.setColor(Color.parseColor(bgColor));
            holder.ivIcon.setBackground(mutableDrawable);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTenDanhMuc, tvGhiChu, tvNgay, tvSoTien;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTenDanhMuc = itemView.findViewById(R.id.tvTenDanhMuc);
            tvGhiChu = itemView.findViewById(R.id.tvGhiChu);
            tvNgay = itemView.findViewById(R.id.tvNgay);
            tvSoTien = itemView.findViewById(R.id.tvSoTien);
        }
    }
}
