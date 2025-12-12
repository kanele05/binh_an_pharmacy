/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import java.time.LocalDate;

/**
 *
 * @author khang
 */
public class PhieuTraHang {
    private String maPT;
    private LocalDate ngayTra;
    private double tongTienHoanTra;
    private String lyDo;
    private HoaDon hoaDon;
    private NhanVien nhanVien;
    private KhachHang khachHang;

    public PhieuTraHang(String maPT, LocalDate ngayTra, double tongTienHoanTra, String lyDo, HoaDon hoaDon, NhanVien nhanVien, KhachHang khachHang) {
        this.maPT = maPT;
        this.ngayTra = ngayTra;
        this.tongTienHoanTra = tongTienHoanTra;
        this.lyDo = lyDo;
        this.hoaDon = hoaDon;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
    }

    public String getMaPT() {
        return maPT;
    }

    public void setMaPT(String maPT) {
        this.maPT = maPT;
    }

    public LocalDate getNgayTra() {
        return ngayTra;
    }

    public void setNgayTra(LocalDate ngayTra) {
        this.ngayTra = ngayTra;
    }

    public double getTongTienHoanTra() {
        return tongTienHoanTra;
    }

    public void setTongTienHoanTra(double tongTienHoanTra) {
        this.tongTienHoanTra = tongTienHoanTra;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
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
        return "PhieuTraHang{" + "maPT=" + maPT + ", ngayTra=" + ngayTra + ", tongTienHoanTra=" + tongTienHoanTra + ", lyDo=" + lyDo + ", hoaDon=" + hoaDon + ", nhanVien=" + nhanVien + ", khachHang=" + khachHang + '}';
    }
    
}
