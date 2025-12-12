/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

/**
 *
 * @author khang
 */
public class ChiTietDonDat {
    private DonDatHang donDat;
    private Thuoc thuoc;
    private int soLuong;
    private double donGia;
    private double thanhTien;

    public ChiTietDonDat(DonDatHang donDat, Thuoc thuoc, int soLuong, double donGia, double thanhTien) {
        this.donDat = donDat;
        this.thuoc = thuoc;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public DonDatHang getDonDat() {
        return donDat;
    }

    public void setDonDat(DonDatHang donDat) {
        this.donDat = donDat;
    }

    public Thuoc getThuoc() {
        return thuoc;
    }

    public void setThuoc(Thuoc thuoc) {
        this.thuoc = thuoc;
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
        return "ChiTietDonDat{" + "donDat=" + donDat + ", thuoc=" + thuoc + ", soLuong=" + soLuong + ", donGia=" + donGia + ", thanhTien=" + thanhTien + '}';
    }
    
}
