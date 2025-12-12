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
public class HoaDon {
    private String maHD;
    private LocalDateTime ngayTao;
    private double tongTien;
    private double giamGia;
    private double thue;
    private String hinhThucTT;
    private String ghiChu;
    private NhanVien nhanVien;
    private KhachHang khachHang;

    public HoaDon(String maHD, LocalDateTime ngayTao, double tongTien, double giamGia, double thue, String hinhThucTT, String ghiChu, NhanVien nhanVien, KhachHang khachHang) {
        this.maHD = maHD;
        this.ngayTao = ngayTao;
        this.tongTien = tongTien;
        this.giamGia = giamGia;
        this.thue = thue;
        this.hinhThucTT = hinhThucTT;
        this.ghiChu = ghiChu;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
    }
    public HoaDon(){}
    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public double getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(double giamGia) {
        this.giamGia = giamGia;
    }

    public double getThue() {
        return thue;
    }

    public void setThue(double thue) {
        this.thue = thue;
    }

    public String getHinhThucTT() {
        return hinhThucTT;
    }

    public void setHinhThucTT(String hinhThucTT) {
        this.hinhThucTT = hinhThucTT;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
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
        return "HoaDon{" + "maHD=" + maHD + ", ngayTao=" + ngayTao + ", tongTien=" + tongTien + ", giamGia=" + giamGia + ", thue=" + thue + ", hinhThucTT=" + hinhThucTT + ", ghiChu=" + ghiChu + ", nhanVien=" + nhanVien + ", khachHang=" + khachHang + '}';
    }
    
}
