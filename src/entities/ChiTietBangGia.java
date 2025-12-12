/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

/**
 *
 * @author khang
 */
public class ChiTietBangGia {
    private BangGia bangGia;
    private Thuoc thuoc;
    private String donViTinh;
    private double giaBan;

    public ChiTietBangGia(BangGia bangGia, Thuoc thuoc, String donViTinh, double giaBan) {
        this.bangGia = bangGia;
        this.thuoc = thuoc;
        this.donViTinh = donViTinh;
        this.giaBan = giaBan;
    }

    public BangGia getBangGia() {
        return bangGia;
    }

    public void setBangGia(BangGia bangGia) {
        this.bangGia = bangGia;
    }

    public Thuoc getThuoc() {
        return thuoc;
    }

    public void setThuoc(Thuoc thuoc) {
        this.thuoc = thuoc;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public double getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(double giaBan) {
        this.giaBan = giaBan;
    }

    @Override
    public String toString() {
        return "ChiTietBangGia{" + "bangGia=" + bangGia + ", thuoc=" + thuoc + ", donViTinh=" + donViTinh + ", giaBan=" + giaBan + '}';
    }
    
    
}
