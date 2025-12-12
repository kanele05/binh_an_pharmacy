/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

/**
 *
 * @author khang
 */
public class NhomThuoc {
    private String maNhom;
    private String tenNhom;

    public NhomThuoc(String maNhom, String tenNhom) {
        this.maNhom = maNhom;
        this.tenNhom = tenNhom;
    }

    public NhomThuoc(String maNhom) {
        this.maNhom = maNhom;
    }

    
    public String getMaNhom() {
        return maNhom;
    }

    public void setMaNhom(String maNhom) {
        this.maNhom = maNhom;
    }

    public String getTenNhom() {
        return tenNhom;
    }

    public void setTenNhom(String tenNhom) {
        this.tenNhom = tenNhom;
    }

    @Override
    public String toString() {
        return "NhomThuoc{" + "maNhom=" + maNhom + ", tenNhom=" + tenNhom + '}';
    }
    
    
}
