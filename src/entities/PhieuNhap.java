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
public class PhieuNhap {
    private String maPN;
    private LocalDate ngayTao;
    private double tongTien;
    private String trangThai;
    private NhanVien nhanVien;
    private NhaCungCap ncc;

    public PhieuNhap(String maPN, LocalDate ngayTao, double tongTien, String trangThai, NhanVien nhanVien, NhaCungCap ncc) {
        this.maPN = maPN;
        this.ngayTao = ngayTao;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
        this.nhanVien = nhanVien;
        this.ncc = ncc;
    }

    public String getMaPN() {
        return maPN;
    }

    public void setMaPN(String maPN) {
        this.maPN = maPN;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
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

    public NhaCungCap getNcc() {
        return ncc;
    }

    public void setNcc(NhaCungCap ncc) {
        this.ncc = ncc;
    }

    @Override
    public String toString() {
        return "PhieuNhap{" + "maPN=" + maPN + ", ngayTao=" + ngayTao + ", tongTien=" + tongTien + ", trangThai=" + trangThai + ", nhanVien=" + nhanVien + ", ncc=" + ncc + '}';
    }
    
}
