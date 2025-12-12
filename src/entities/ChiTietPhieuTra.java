/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

/**
 *
 * @author khang
 */
public class ChiTietPhieuTra {
    private PhieuTraHang phieuTra;
    private Thuoc thuoc;
    private LoThuoc loThuoc;
    private int soLuongTra;
    private double donGiaTra;
    private double thanhTienHoanTra;

    public ChiTietPhieuTra(PhieuTraHang phieuTra, Thuoc thuoc, LoThuoc loThuoc, int soLuongTra, double donGiaTra, double thanhTienHoanTra) {
        this.phieuTra = phieuTra;
        this.thuoc = thuoc;
        this.loThuoc = loThuoc;
        this.soLuongTra = soLuongTra;
        this.donGiaTra = donGiaTra;
        this.thanhTienHoanTra = thanhTienHoanTra;
    }

    public PhieuTraHang getPhieuTra() {
        return phieuTra;
    }

    public void setPhieuTra(PhieuTraHang phieuTra) {
        this.phieuTra = phieuTra;
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

    public int getSoLuongTra() {
        return soLuongTra;
    }

    public void setSoLuongTra(int soLuongTra) {
        this.soLuongTra = soLuongTra;
    }

    public double getDonGiaTra() {
        return donGiaTra;
    }

    public void setDonGiaTra(double donGiaTra) {
        this.donGiaTra = donGiaTra;
    }

    public double getThanhTienHoanTra() {
        return thanhTienHoanTra;
    }

    public void setThanhTienHoanTra(double thanhTienHoanTra) {
        this.thanhTienHoanTra = thanhTienHoanTra;
    }

    @Override
    public String toString() {
        return "ChiTietPhieuTra{" + "phieuTra=" + phieuTra + ", thuoc=" + thuoc + ", loThuoc=" + loThuoc + ", soLuongTra=" + soLuongTra + ", donGiaTra=" + donGiaTra + ", thanhTienHoanTra=" + thanhTienHoanTra + '}';
    }
    
}
