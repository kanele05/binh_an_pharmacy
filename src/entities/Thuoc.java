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
    private String donViTinh;
    private boolean trangThai;
    private NhomThuoc nhomThuoc;

    public Thuoc(String maThuoc, String tenThuoc, String hoatChat, String donViTinh, boolean trangThai, NhomThuoc nhomThuoc) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.hoatChat = hoatChat;
        this.donViTinh = donViTinh;
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
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
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

    @Override
    public String toString() {
        return "Thuoc{" + "maThuoc=" + maThuoc + ", tenThuoc=" + tenThuoc + ", hoatChat=" + hoatChat + ", donViTinh=" + donViTinh + ", trangThai=" + trangThai + ", nhomThuoc=" + nhomThuoc + '}';
    }
    
    
}
