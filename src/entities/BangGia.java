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
public class BangGia {
    private String maBG;
    private String tenBG;
    private LocalDate ngayHieuLuc;
    private LocalDate ngayKetThuc;
    private String ghiChu;
    private boolean trangThai;

    public BangGia(String maBG, String tenBG, LocalDate ngayHieuLuc, LocalDate ngayKetThuc, String ghiChu, boolean trangThai) {
        this.maBG = maBG;
        this.tenBG = tenBG;
        this.ngayHieuLuc = ngayHieuLuc;
        this.ngayKetThuc = ngayKetThuc;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
    }

    public String getMaBG() {
        return maBG;
    }

    public void setMaBG(String maBG) {
        this.maBG = maBG;
    }

    public String getTenBG() {
        return tenBG;
    }

    public void setTenBG(String tenBG) {
        this.tenBG = tenBG;
    }

    public LocalDate getNgayHieuLuc() {
        return ngayHieuLuc;
    }

    public void setNgayHieuLuc(LocalDate ngayHieuLuc) {
        this.ngayHieuLuc = ngayHieuLuc;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return "BangGia{" + "maBG=" + maBG + ", tenBG=" + tenBG + ", ngayHieuLuc=" + ngayHieuLuc + ", ngayKetThuc=" + ngayKetThuc + ", ghiChu=" + ghiChu + ", trangThai=" + trangThai + '}';
    }
    
    
}
