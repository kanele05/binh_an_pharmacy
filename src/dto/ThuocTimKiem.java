package dto;

import java.time.LocalDate;

public class ThuocTimKiem {

    private String maThuoc;
    private String tenThuoc;
    private String maLo;
    private LocalDate hanSuDung;
    private int soLuongTon;
    private String donViTinh;
    private int giaTriQuyDoi;
    private double giaBan;

    public ThuocTimKiem(String maThuoc, String tenThuoc, String maLo, LocalDate hanSuDung, int soLuongTon, String donViTinh, int giaTriQuyDoi, double giaBan) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.maLo = maLo;
        this.hanSuDung = hanSuDung;
        this.soLuongTon = soLuongTon;
        this.donViTinh = donViTinh;
        this.giaTriQuyDoi = giaTriQuyDoi;
        this.giaBan = giaBan;
    }

    public String getMaThuoc() {
        return maThuoc;
    }

    public void setMaThuoc(String maThuoc) {
        this.maThuoc = maThuoc;
    }

    public String getTenThuoc() {
        return tenThuoc;
    }

    public void setTenThuoc(String tenThuoc) {
        this.tenThuoc = tenThuoc;
    }

    public String getMaLo() {
        return maLo;
    }

    public void setMaLo(String maLo) {
        this.maLo = maLo;
    }

    public LocalDate getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(LocalDate hanSuDung) {
        this.hanSuDung = hanSuDung;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(int soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
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

    @Override
    public String toString() {
        return tenThuoc + " (" + donViTinh + ")";
    }
}
