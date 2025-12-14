package entities;

public class DonViQuyDoi {

    private int id;                 // ID tự tăng
    private String maThuoc;         // Link tới thuốc
    private String tenDonVi;        // Ví dụ: "Hộp", "Vỉ"
    private int giaTriQuyDoi;       // Ví dụ: 1 Hộp = 100 (viên cơ bản)
    private double giaBan;          // Giá bán riêng cho đơn vị này
    private boolean laDonViCoBan;      // Còn dùng hay không

    public DonViQuyDoi() {
    }

    public DonViQuyDoi(int id, String maThuoc, String tenDonVi, int giaTriQuyDoi, double giaBan, boolean laDonViCoBan) {
        this.id = id;
        this.maThuoc = maThuoc;
        this.tenDonVi = tenDonVi;
        this.giaTriQuyDoi = giaTriQuyDoi;
        this.giaBan = giaBan;
        this.laDonViCoBan = laDonViCoBan;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaThuoc() {
        return maThuoc;
    }

    public void setMaThuoc(String maThuoc) {
        this.maThuoc = maThuoc;
    }

    public String getTenDonVi() {
        return tenDonVi;
    }

    public void setTenDonVi(String tenDonVi) {
        this.tenDonVi = tenDonVi;
    }

    public int getGiaTriQuyDoi() {
        return giaTriQuyDoi;
    }

    public void setGiaTriQuyDoi(int giaTriQuyDoi) {
        this.giaTriQuyDoi = giaTriQuyDoi;
    }

    public double getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(double giaBan) {
        this.giaBan = giaBan;
    }

    public boolean isLaDonViCoBan() {
        return laDonViCoBan;
    }

    public void setLaDonViCoBan(boolean laDonViCoBan) {
        this.laDonViCoBan = laDonViCoBan;
    }

    

    @Override
    public String toString() {
        return tenDonVi; // Để hiển thị trên ComboBox cho đẹp
    }
}
