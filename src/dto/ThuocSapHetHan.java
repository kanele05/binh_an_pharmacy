package dto;

import java.time.LocalDate;

public class ThuocSapHetHan {
    private String maThuoc;
    private String tenThuoc;
    private String maLo;
    private LocalDate hanSuDung;
    private int soLuongTon;
    private int soNgayConLai;

    public ThuocSapHetHan() {
    }

    public ThuocSapHetHan(String maThuoc, String tenThuoc, String maLo, LocalDate hanSuDung, int soLuongTon, int soNgayConLai) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.maLo = maLo;
        this.hanSuDung = hanSuDung;
        this.soLuongTon = soLuongTon;
        this.soNgayConLai = soNgayConLai;
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

    public int getSoNgayConLai() {
        return soNgayConLai;
    }

    public void setSoNgayConLai(int soNgayConLai) {
        this.soNgayConLai = soNgayConLai;
    }
}
