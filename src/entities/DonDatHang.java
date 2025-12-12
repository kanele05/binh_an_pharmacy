/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import java.time.LocalDateTime;

/**
 *
 * @author khang
 */
public class DonDatHang {
    private String maDonDat;
    private String tenKhach;
    private String sdtLienHe;
    private LocalDateTime gioHenLay;
    private double tongTien;
    private String ghiChu;
    private String trangThai;
    private NhanVien nhanVien;
    private KhachHang khachHang;

    public DonDatHang(String maDonDat, String tenKhach, String sdtLienHe, LocalDateTime gioHenLay, double tongTien, String ghiChu, String trangThai, NhanVien nhanVien, KhachHang khachHang) {
        this.maDonDat = maDonDat;
        this.tenKhach = tenKhach;
        this.sdtLienHe = sdtLienHe;
        this.gioHenLay = gioHenLay;
        this.tongTien = tongTien;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
    }

    public String getMaDonDat() {
        return maDonDat;
    }

    public void setMaDonDat(String maDonDat) {
        this.maDonDat = maDonDat;
    }

    public String getTenKhach() {
        return tenKhach;
    }

    public void setTenKhach(String tenKhach) {
        this.tenKhach = tenKhach;
    }

    public String getSdtLienHe() {
        return sdtLienHe;
    }

    public void setSdtLienHe(String sdtLienHe) {
        this.sdtLienHe = sdtLienHe;
    }

    public LocalDateTime getGioHenLay() {
        return gioHenLay;
    }

    public void setGioHenLay(LocalDateTime gioHenLay) {
        this.gioHenLay = gioHenLay;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    @Override
    public String toString() {
        return "DonDatHang{" + "maDonDat=" + maDonDat + ", tenKhach=" + tenKhach + ", sdtLienHe=" + sdtLienHe + ", gioHenLay=" + gioHenLay + ", tongTien=" + tongTien + ", ghiChu=" + ghiChu + ", trangThai=" + trangThai + ", nhanVien=" + nhanVien + ", khachHang=" + khachHang + '}';
    }
    
}
