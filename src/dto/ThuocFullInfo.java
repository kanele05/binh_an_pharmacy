package dto;

public class ThuocFullInfo {

    private String maThuoc;
    private String tenThuoc;
    private String hoatChat;
    private String donViCoBan;
    private String tenNhom;
    private double giaNhap;
    private double giaBan;
    private int tonKho;
    private int tonKhoBanDuoc;
    private boolean trangThai;

    public ThuocFullInfo(String maThuoc, String tenThuoc, String hoatChat, String donViCoBan, String tenNhom, double giaNhap, double giaBan, int tonKho, int tonKhoBanDuoc, boolean trangThai) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.hoatChat = hoatChat;
        this.donViCoBan = donViCoBan;
        this.tenNhom = tenNhom;
        this.giaNhap = giaNhap;
        this.giaBan = giaBan;
        this.tonKho = tonKho;
        this.tonKhoBanDuoc = tonKhoBanDuoc;
        this.trangThai = trangThai;
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

    public String getDonViCoBan() {
        return donViCoBan;
    }

    public void setDonViCoBan(String donViCoBan) {
        this.donViCoBan = donViCoBan;
    }

    public String getTenNhom() {
        return tenNhom;
    }

    public void setTenNhom(String tenNhom) {
        this.tenNhom = tenNhom;
    }

    public double getGiaNhap() {
        return giaNhap;
    }

    public void setGiaNhap(double giaNhap) {
        this.giaNhap = giaNhap;
    }

    public double getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(double giaBan) {
        this.giaBan = giaBan;
    }

    public int getTonKho() {
        return tonKho;
    }

    public void setTonKho(int tonKho) {
        this.tonKho = tonKho;
    }

    public int getTonKhoBanDuoc() {
        return tonKhoBanDuoc;
    }

    public void setTonKhoBanDuoc(int tonKhoBanDuoc) {
        this.tonKhoBanDuoc = tonKhoBanDuoc;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return "ThuocFullInfo{" + "maThuoc=" + maThuoc + ", tenThuoc=" + tenThuoc + ", hoatChat=" + hoatChat + ", donViCoBan=" + donViCoBan + ", tenNhom=" + tenNhom + ", giaNhap=" + giaNhap + ", giaBan=" + giaBan + ", tonKho=" + tonKho + ", tonKhoBanDuoc=" + tonKhoBanDuoc + ", trangThai=" + trangThai + '}';
    }

}
