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
public class ChiTietPhieuNhap {
    private PhieuNhap pn;
    private Thuoc thuoc;
    private LoThuoc loThuoc;
    private LocalDate hanSuDung;
    private int soLuong;
    private double donGia;
    private double thanhTien;

    public ChiTietPhieuNhap(PhieuNhap pn, Thuoc thuoc, LoThuoc loThuoc, LocalDate hanSuDung, int soLuong, double donGia, double thanhTien) {
        this.pn = pn;
        this.thuoc = thuoc;
        this.loThuoc = loThuoc;
        this.hanSuDung = hanSuDung;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public PhieuNhap getPn() {
        return pn;
    }

    public void setPn(PhieuNhap pn) {
        this.pn = pn;
    }

    public Thuoc getThuoc() {
        return thuoc;
    }

    public void setThuoc(Thuoc thuoc) {
        this.thuoc = thuoc;
    }

    public LoThuoc getLoThuoc() {
        return loThuoc;
    }

    public void setLoThuoc(LoThuoc loThuoc) {
        this.loThuoc = loThuoc;
    }

    public LocalDate getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(LocalDate hanSuDung) {
        this.hanSuDung = hanSuDung;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }

    @Override
    public String toString() {
        return "ChiTietPhieuNhap{" + "pn=" + pn + ", thuoc=" + thuoc + ", loThuoc=" + loThuoc + ", hanSuDung=" + hanSuDung + ", soLuong=" + soLuong + ", donGia=" + donGia + ", thanhTien=" + thanhTien + '}';
    }
    
}
