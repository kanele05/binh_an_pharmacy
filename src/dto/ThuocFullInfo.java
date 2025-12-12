package dto;

public class ThuocFullInfo {

    private String maThuoc;
    private String tenThuoc;
    private String hoatChat;
    private String donViTinh;
    private String tenNhom;
    private double giaNhap;
    private double giaBan;
    private int tonKho;
    private int tonKhoBanDuoc;
    private boolean trangThai;

    public ThuocFullInfo(String maThuoc, String tenThuoc, String hoatChat, String donViTinh, String tenNhom, double giaNhap, double giaBan, int tonKho, int tonKhoBanDuoc, boolean trangThai) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.hoatChat = hoatChat;
        this.donViTinh = donViTinh;
        this.tenNhom = tenNhom;
        this.giaNhap = giaNhap;
        this.giaBan = giaBan;
        this.tonKho = tonKho;
        this.tonKhoBanDuoc = tonKhoBanDuoc;
        this.trangThai = trangThai;
    }

    public int getTonKhoBanDuoc() {
        return tonKhoBanDuoc;
    }

    public String getMaThuoc() {
        return maThuoc;
    }

    public String getTenThuoc() {
        return tenThuoc;
    }

    public String getHoatChat() {
        return hoatChat;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public String getTenNhom() {
        return tenNhom;
    }

    public double getGiaNhap() {
        return giaNhap;
    }

    public double getGiaBan() {
        return giaBan;
    }

    public int getTonKho() {
        return tonKho;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

}
