/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

/**
 *
 * @author khang
 */
public class Thuoc {
    private String maThuoc;
    private String tenThuoc;
    private String hoatChat;
    private String donViCoBan;
    private boolean trangThai;
    private NhomThuoc nhomThuoc;
    private int tonToiThieu = 10;

    public Thuoc() {
    }
    
    public Thuoc(String maThuoc, String tenThuoc, String hoatChat, String donViCoBan, boolean trangThai, NhomThuoc nhomThuoc) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.hoatChat = hoatChat;
        this.donViCoBan = donViCoBan;
        this.trangThai = trangThai;
        this.nhomThuoc = nhomThuoc;
    }

    public Thuoc(String maThuoc) {
        this.maThuoc = maThuoc;
    }
    
    public String getMaThuoc() {
        return maThuoc;
    }

    public void setMaThuoc(String maThuoc) {
        this.maThuoc = maThuoc;
    }

    public String getTenThuoc() {
        return tenThuoc;
    }

    public void setTenThuoc(String tenThuoc) {
        this.tenThuoc = tenThuoc;
    }

    public String getHoatChat() {
        return hoatChat;
    }

    public void setHoatChat(String hoatChat) {
        this.hoatChat = hoatChat;
    }

    public String getDonViTinh() {
        return donViCoBan;
    }

    public void setDonViTinh(String donViCoBan) {
        this.donViCoBan = donViCoBan;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public NhomThuoc getNhomThuoc() {
        return nhomThuoc;
    }

    public void setNhomThuoc(NhomThuoc nhomThuoc) {
        this.nhomThuoc = nhomThuoc;
    }

    public int getTonToiThieu() {
        return tonToiThieu;
    }

    public void setTonToiThieu(int tonToiThieu) {
        this.tonToiThieu = tonToiThieu;
    }

    @Override
    public String toString() {
        return "Thuoc{" + "maThuoc=" + maThuoc + ", tenThuoc=" + tenThuoc + ", hoatChat=" + hoatChat + ", donViCoBan=" + donViCoBan + ", trangThai=" + trangThai + ", nhomThuoc=" + nhomThuoc + '}';
    }
    
    
}
