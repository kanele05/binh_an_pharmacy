package entities;

import java.time.LocalDate;

/**
 * Entity PhieuDoiHang - Phiếu đổi hàng
 * Đổi thuốc = Trả thuốc + Lập hóa đơn mới
 * - phieuTra: Phiếu trả hàng cho các sản phẩm khách trả lại
 * - hoaDonMoi: Hóa đơn mới cho các sản phẩm khách đổi lấy
 */
public class PhieuDoiHang {
    private String maPD;
    private LocalDate ngayDoi;
    private double tongTienTra;     // Tổng tiền hàng trả lại
    private double tongTienMoi;     // Tổng tiền hàng mới
    private double chenhLech;       // Chênh lệch = tongTienMoi - tongTienTra
    private String lyDo;
    private HoaDon hoaDonGoc;       // Hóa đơn gốc (hóa đơn khách mua trước đó)
    private PhieuTraHang phieuTra;  // Phiếu trả hàng được tạo
    private HoaDon hoaDonMoi;       // Hóa đơn mới được tạo (cho hàng đổi)
    private NhanVien nhanVien;
    private KhachHang khachHang;
    private String trangThai;
    private String ghiChu;

    public PhieuDoiHang() {
    }

    public PhieuDoiHang(String maPD, LocalDate ngayDoi, double tongTienTra, double tongTienMoi,
                        double chenhLech, String lyDo, HoaDon hoaDonGoc, PhieuTraHang phieuTra,
                        HoaDon hoaDonMoi, NhanVien nhanVien, KhachHang khachHang,
                        String trangThai, String ghiChu) {
        this.maPD = maPD;
        this.ngayDoi = ngayDoi;
        this.tongTienTra = tongTienTra;
        this.tongTienMoi = tongTienMoi;
        this.chenhLech = chenhLech;
        this.lyDo = lyDo;
        this.hoaDonGoc = hoaDonGoc;
        this.phieuTra = phieuTra;
        this.hoaDonMoi = hoaDonMoi;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public String getMaPD() {
        return maPD;
    }

    public void setMaPD(String maPD) {
        this.maPD = maPD;
    }

    public LocalDate getNgayDoi() {
        return ngayDoi;
    }

    public void setNgayDoi(LocalDate ngayDoi) {
        this.ngayDoi = ngayDoi;
    }

    public double getTongTienTra() {
        return tongTienTra;
    }

    public void setTongTienTra(double tongTienTra) {
        this.tongTienTra = tongTienTra;
    }

    public double getTongTienMoi() {
        return tongTienMoi;
    }

    public void setTongTienMoi(double tongTienMoi) {
        this.tongTienMoi = tongTienMoi;
    }

    public double getChenhLech() {
        return chenhLech;
    }

    public void setChenhLech(double chenhLech) {
        this.chenhLech = chenhLech;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public HoaDon getHoaDonGoc() {
        return hoaDonGoc;
    }

    public void setHoaDonGoc(HoaDon hoaDonGoc) {
        this.hoaDonGoc = hoaDonGoc;
    }

    public PhieuTraHang getPhieuTra() {
        return phieuTra;
    }

    public void setPhieuTra(PhieuTraHang phieuTra) {
        this.phieuTra = phieuTra;
    }

    public HoaDon getHoaDonMoi() {
        return hoaDonMoi;
    }

    public void setHoaDonMoi(HoaDon hoaDonMoi) {
        this.hoaDonMoi = hoaDonMoi;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public String toString() {
        return "PhieuDoiHang{" + "maPD=" + maPD + ", ngayDoi=" + ngayDoi +
               ", tongTienTra=" + tongTienTra + ", tongTienMoi=" + tongTienMoi +
               ", chenhLech=" + chenhLech + ", trangThai=" + trangThai + '}';
    }
}
