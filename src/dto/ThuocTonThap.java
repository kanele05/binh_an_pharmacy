package dto;

public class ThuocTonThap {
    private String maThuoc;
    private String tenThuoc;
    private int tonKho;
    private int tonToiThieu;
    private int soLuongCanNhap;

    public ThuocTonThap() {
    }

    public ThuocTonThap(String maThuoc, String tenThuoc, int tonKho, int tonToiThieu, int soLuongCanNhap) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.tonKho = tonKho;
        this.tonToiThieu = tonToiThieu;
        this.soLuongCanNhap = soLuongCanNhap;
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
}
