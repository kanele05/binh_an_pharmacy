/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

/**
 *
 * @author khang
 */
public class ChiTietHoaDon {
    private HoaDon hoaDon;
    private Thuoc thuoc;
    private LoThuoc loThuoc;
    private int soLuong;
    private double donGia;
    private double thanhTien;
    private String donViTinh;

    public ChiTietHoaDon(HoaDon hoaDon, Thuoc thuoc, LoThuoc loThuoc, int soLuong, double donGia, double thanhTien, String donViTinh) {
        this.hoaDon = hoaDon;
        this.thuoc = thuoc;
        this.loThuoc = loThuoc;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
        this.donViTinh = donViTinh;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
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

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    @Override
    public String toString() {
        return "ChiTietHoaDon{" + "hoaDon=" + hoaDon + ", thuoc=" + thuoc + ", loThuoc=" + loThuoc + ", soLuong=" + soLuong + ", donGia=" + donGia + ", thanhTien=" + thanhTien + ", donViTinh=" + donViTinh + '}';
    }
    
}
