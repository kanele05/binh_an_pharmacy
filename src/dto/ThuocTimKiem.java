package dto;

import java.time.LocalDate;

public class ThuocTimKiem {

    private String maThuoc;
    private String tenThuoc;
    private String maLo;
    private LocalDate hanSuDung;
    private int soLuongTon;
    private String donViTinh;
    private double giaBan;

    public ThuocTimKiem(String maThuoc, String tenThuoc, String maLo, LocalDate hanSuDung, int soLuongTon, String donViTinh, double giaBan) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.maLo = maLo;
        this.hanSuDung = hanSuDung;
        this.soLuongTon = soLuongTon;
        this.donViTinh = donViTinh;
        this.giaBan = giaBan;
    }

    public String getMaThuoc() {
        return maThuoc;
    }

    public String getTenThuoc() {
        return tenThuoc;
    }

    public String getMaLo() {
        return maLo;
    }

    public LocalDate getHanSuDung() {
        return hanSuDung;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public double getGiaBan() {
        return giaBan;
    }
}
