package dto;

public class ThuocTonThap {
    private String maThuoc;
    private String tenThuoc;
    private String donViTinh;
    private int tonKho;
    private int tonToiThieu;
    private int soLuongCanNhap;
    private String nhaCungCap;
    private String tenNhom;

    public ThuocTonThap() {
    }

    public ThuocTonThap(String maThuoc, String tenThuoc, int tonKho, int tonToiThieu, int soLuongCanNhap) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.tonKho = tonKho;
        this.tonToiThieu = tonToiThieu;
        this.soLuongCanNhap = soLuongCanNhap;
    }

    public ThuocTonThap(String maThuoc, String tenThuoc, String donViTinh, int tonKho, int tonToiThieu, int soLuongCanNhap, String nhaCungCap, String tenNhom) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.donViTinh = donViTinh;
        this.tonKho = tonKho;
        this.tonToiThieu = tonToiThieu;
        this.soLuongCanNhap = soLuongCanNhap;
        this.nhaCungCap = nhaCungCap;
        this.tenNhom = tenNhom;
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

    public int getTonKho() {
        return tonKho;
    }

    public void setTonKho(int tonKho) {
        this.tonKho = tonKho;
    }

    public int getTonToiThieu() {
        return tonToiThieu;
    }

    public void setTonToiThieu(int tonToiThieu) {
        this.tonToiThieu = tonToiThieu;
    }

    public int getSoLuongCanNhap() {
        return soLuongCanNhap;
    }

    public void setSoLuongCanNhap(int soLuongCanNhap) {
        this.soLuongCanNhap = soLuongCanNhap;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public String getNhaCungCap() {
        return nhaCungCap;
    }

    public void setNhaCungCap(String nhaCungCap) {
        this.nhaCungCap = nhaCungCap;
    }

    public String getTenNhom() {
        return tenNhom;
    }

    public void setTenNhom(String tenNhom) {
        this.tenNhom = tenNhom;
    }
}
