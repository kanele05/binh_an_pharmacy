package dao;

import connectDB.ConnectDB;
import entities.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho PhieuDoiHang
 * Đổi thuốc = Trả thuốc + Lập hóa đơn mới
 */
public class PhieuDoiHangDAO {

    public PhieuDoiHangDAO() {
    }

    /**
     * Tạo mã phiếu đổi mới
     */
    public String getNewMaPhieuDoi() {
        String newID = "PD001";
        String sql = "SELECT TOP 1 maPD FROM PhieuDoiHang ORDER BY maPD DESC";
        try {
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                String lastID = rs.getString("maPD");
                try {
                    int num = Integer.parseInt(lastID.substring(2)) + 1;
                    newID = String.format("PD%03d", num);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newID;
    }

    /**
     * Tạo mã phiếu trả mới
     */
    private String getNewMaPhieuTra(Connection con) throws SQLException {
        String newID = "PT001";
        String sql = "SELECT TOP 1 maPT FROM PhieuTraHang ORDER BY maPT DESC";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        if (rs.next()) {
            String lastID = rs.getString("maPT");
            try {
                int num = Integer.parseInt(lastID.substring(2)) + 1;
                newID = String.format("PT%03d", num);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return newID;
    }

    /**
     * Tạo mã hóa đơn mới
     */
    private String getNewMaHoaDon(Connection con) throws SQLException {
        String newID = "HD001";
        String sql = "SELECT TOP 1 maHD FROM HoaDon ORDER BY maHD DESC";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        if (rs.next()) {
            String lastID = rs.getString("maHD");
            try {
                int num = Integer.parseInt(lastID.substring(2)) + 1;
                newID = String.format("HD%03d", num);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return newID;
    }

    /**
     * Tạo phiếu đổi hàng hoàn chỉnh
     * Bao gồm: Tạo PhieuTraHang + Tạo HoaDon mới + Tạo PhieuDoiHang
     *
     * @param phieuDoi Thông tin phiếu đổi
     * @param listTraHang Danh sách chi tiết hàng trả lại
     * @param listHangMoi Danh sách chi tiết hàng mới
     * @return true nếu thành công
     */
    public boolean taoPhieuDoi(PhieuDoiHang phieuDoi,
                               List<ChiTietPhieuTra> listTraHang,
                               List<ChiTietHoaDon> listHangMoi) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // === 1. TẠO PHIẾU TRẢ HÀNG ===
            String maPT = getNewMaPhieuTra(con);
            PhieuTraHang phieuTra = new PhieuTraHang(
                    maPT,
                    LocalDate.now(),
                    phieuDoi.getTongTienTra(),
                    phieuDoi.getLyDo(),
                    phieuDoi.getHoaDonGoc(),
                    phieuDoi.getNhanVien(),
                    phieuDoi.getKhachHang(),
                    "Đổi hàng",
                    "Thuộc phiếu đổi: " + phieuDoi.getMaPD()
            );

            // Insert PhieuTraHang
            String sqlPhieuTra = "INSERT INTO PhieuTraHang (maPT, ngayTra, tongTienHoanTra, lyDo, maHD, maNV, maKH, trangThai, ghiChu) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmtPT = con.prepareStatement(sqlPhieuTra);
            stmtPT.setString(1, phieuTra.getMaPT());
            stmtPT.setDate(2, Date.valueOf(phieuTra.getNgayTra()));
            stmtPT.setDouble(3, phieuTra.getTongTienHoanTra());
            stmtPT.setString(4, phieuTra.getLyDo());
            stmtPT.setString(5, phieuTra.getHoaDon() != null ? phieuTra.getHoaDon().getMaHD() : null);
            stmtPT.setString(6, phieuTra.getNhanVien().getMaNV());
            stmtPT.setString(7, phieuTra.getKhachHang() != null ? phieuTra.getKhachHang().getMaKH() : null);
            stmtPT.setString(8, phieuTra.getTrangThai());
            stmtPT.setString(9, phieuTra.getGhiChu());
            stmtPT.executeUpdate();

            // Insert ChiTietPhieuTra và cập nhật tồn kho (hoàn lại hàng)
            String sqlCTPT = "INSERT INTO ChiTietPhieuTra (maPT, maThuoc, maLo, soLuongTra, donGiaTra, thanhTienHoanTra, donViTinh) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            String sqlUpdateLoTra = "UPDATE LoThuoc SET soLuongTon = soLuongTon + ? WHERE maLo = ?";

            for (ChiTietPhieuTra ct : listTraHang) {
                ct.setPhieuTra(phieuTra);

                PreparedStatement stmtCTPT = con.prepareStatement(sqlCTPT);
                stmtCTPT.setString(1, maPT);
                stmtCTPT.setString(2, ct.getThuoc().getMaThuoc());
                stmtCTPT.setString(3, ct.getLoThuoc().getMaLo());
                stmtCTPT.setInt(4, ct.getSoLuongTra());
                stmtCTPT.setDouble(5, ct.getDonGiaTra());
                stmtCTPT.setDouble(6, ct.getThanhTienHoanTra());
                stmtCTPT.setString(7, ct.getDonViTinh());
                stmtCTPT.executeUpdate();

                // Hoàn lại tồn kho
                PreparedStatement stmtUpdateLo = con.prepareStatement(sqlUpdateLoTra);
                stmtUpdateLo.setInt(1, ct.getSoLuongTra());
                stmtUpdateLo.setString(2, ct.getLoThuoc().getMaLo());
                stmtUpdateLo.executeUpdate();
            }

            // === 2. TẠO HÓA ĐƠN MỚI (cho hàng đổi) ===
            String maHDMoi = null;
            if (listHangMoi != null && !listHangMoi.isEmpty()) {
                maHDMoi = getNewMaHoaDon(con);
                HoaDon hoaDonMoi = new HoaDon(
                        maHDMoi,
                        LocalDateTime.now(),
                        phieuDoi.getTongTienMoi(),
                        0, 0,
                        "Tiền mặt",
                        "Đổi hàng từ phiếu: " + phieuDoi.getMaPD(),
                        phieuDoi.getNhanVien(),
                        phieuDoi.getKhachHang()
                );

                // Insert HoaDon
                String sqlHD = "INSERT INTO HoaDon (maHD, ngayTao, tongTien, giamGia, thue, hinhThucTT, ghiChu, maNV, maKH) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmtHD = con.prepareStatement(sqlHD);
                stmtHD.setString(1, hoaDonMoi.getMaHD());
                stmtHD.setTimestamp(2, Timestamp.valueOf(hoaDonMoi.getNgayTao()));
                stmtHD.setDouble(3, hoaDonMoi.getTongTien());
                stmtHD.setDouble(4, hoaDonMoi.getGiamGia());
                stmtHD.setDouble(5, hoaDonMoi.getThue());
                stmtHD.setString(6, hoaDonMoi.getHinhThucTT());
                stmtHD.setString(7, hoaDonMoi.getGhiChu());
                stmtHD.setString(8, hoaDonMoi.getNhanVien().getMaNV());
                stmtHD.setString(9, hoaDonMoi.getKhachHang() != null ? hoaDonMoi.getKhachHang().getMaKH() : null);
                stmtHD.executeUpdate();

                // Insert ChiTietHoaDon và trừ tồn kho
                String sqlCTHD = "INSERT INTO ChiTietHoaDon (maHD, maThuoc, maLo, soLuong, donGia, thanhTien, donViTinh) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                String sqlUpdateLoMoi = "UPDATE LoThuoc SET soLuongTon = soLuongTon - ? WHERE maLo = ?";
                String sqlGetTyLe = "SELECT giaTriQuyDoi FROM DonViQuyDoi WHERE maThuoc = ? AND tenDonVi = ?";

                for (ChiTietHoaDon ct : listHangMoi) {
                    ct.setHoaDon(hoaDonMoi);

                    PreparedStatement stmtCTHD = con.prepareStatement(sqlCTHD);
                    stmtCTHD.setString(1, maHDMoi);
                    stmtCTHD.setString(2, ct.getThuoc().getMaThuoc());
                    stmtCTHD.setString(3, ct.getLoThuoc().getMaLo());
                    stmtCTHD.setInt(4, ct.getSoLuong());
                    stmtCTHD.setDouble(5, ct.getDonGia());
                    stmtCTHD.setDouble(6, ct.getThanhTien());
                    stmtCTHD.setString(7, ct.getDonViTinh());
                    stmtCTHD.executeUpdate();

                    // Tính tỷ lệ quy đổi
                    int tyLeQuyDoi = 1;
                    PreparedStatement stmtTyLe = con.prepareStatement(sqlGetTyLe);
                    stmtTyLe.setString(1, ct.getThuoc().getMaThuoc());
                    stmtTyLe.setString(2, ct.getDonViTinh());
                    ResultSet rsTyLe = stmtTyLe.executeQuery();
                    if (rsTyLe.next()) {
                        tyLeQuyDoi = rsTyLe.getInt("giaTriQuyDoi");
                    }
                    rsTyLe.close();

                    // Trừ tồn kho
                    int soLuongTruKho = ct.getSoLuong() * tyLeQuyDoi;
                    PreparedStatement stmtUpdateLo = con.prepareStatement(sqlUpdateLoMoi);
                    stmtUpdateLo.setInt(1, soLuongTruKho);
                    stmtUpdateLo.setString(2, ct.getLoThuoc().getMaLo());
                    stmtUpdateLo.executeUpdate();
                }

                phieuDoi.setHoaDonMoi(hoaDonMoi);
            }

            phieuDoi.setPhieuTra(phieuTra);

            // === 3. TẠO PHIẾU ĐỔI HÀNG ===
            String sqlPD = "INSERT INTO PhieuDoiHang (maPD, ngayDoi, tongTienTra, tongTienMoi, chenhLech, lyDo, "
                    + "maHDGoc, maPT, maHDMoi, maNV, maKH, trangThai, ghiChu) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmtPD = con.prepareStatement(sqlPD);
            stmtPD.setString(1, phieuDoi.getMaPD());
            stmtPD.setDate(2, Date.valueOf(phieuDoi.getNgayDoi()));
            stmtPD.setDouble(3, phieuDoi.getTongTienTra());
            stmtPD.setDouble(4, phieuDoi.getTongTienMoi());
            stmtPD.setDouble(5, phieuDoi.getChenhLech());
            stmtPD.setString(6, phieuDoi.getLyDo());
            stmtPD.setString(7, phieuDoi.getHoaDonGoc() != null ? phieuDoi.getHoaDonGoc().getMaHD() : null);
            stmtPD.setString(8, maPT);
            stmtPD.setString(9, maHDMoi);
            stmtPD.setString(10, phieuDoi.getNhanVien().getMaNV());
            stmtPD.setString(11, phieuDoi.getKhachHang() != null ? phieuDoi.getKhachHang().getMaKH() : null);
            stmtPD.setString(12, phieuDoi.getTrangThai());
            stmtPD.setString(13, phieuDoi.getGhiChu());
            stmtPD.executeUpdate();

            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Lấy tất cả phiếu đổi hàng
     */
    public ArrayList<PhieuDoiHang> getAllPhieuDoi() {
        ArrayList<PhieuDoiHang> dsPhieu = new ArrayList<>();
        String sql = "SELECT pd.*, kh.tenKH, kh.sdt as sdtKH, nv.hoTen as tenNV "
                + "FROM PhieuDoiHang pd "
                + "LEFT JOIN KhachHang kh ON pd.maKH = kh.maKH "
                + "LEFT JOIN NhanVien nv ON pd.maNV = nv.maNV "
                + "ORDER BY pd.ngayDoi DESC";
        try {
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setHoTen(rs.getString("tenNV"));

                KhachHang kh = null;
                if (rs.getString("maKH") != null) {
                    kh = new KhachHang();
                    kh.setMaKH(rs.getString("maKH"));
                    kh.setTenKH(rs.getString("tenKH"));
                    kh.setSdt(rs.getString("sdtKH"));
                }

                HoaDon hdGoc = null;
                if (rs.getString("maHDGoc") != null) {
                    hdGoc = new HoaDon();
                    hdGoc.setMaHD(rs.getString("maHDGoc"));
                }

                PhieuTraHang pt = null;
                if (rs.getString("maPT") != null) {
                    pt = new PhieuTraHang(rs.getString("maPT"), null, 0, null, null, null, null);
                }

                HoaDon hdMoi = null;
                if (rs.getString("maHDMoi") != null) {
                    hdMoi = new HoaDon();
                    hdMoi.setMaHD(rs.getString("maHDMoi"));
                }

                PhieuDoiHang pd = new PhieuDoiHang(
                        rs.getString("maPD"),
                        rs.getDate("ngayDoi") != null ? rs.getDate("ngayDoi").toLocalDate() : null,
                        rs.getDouble("tongTienTra"),
                        rs.getDouble("tongTienMoi"),
                        rs.getDouble("chenhLech"),
                        rs.getString("lyDo"),
                        hdGoc, pt, hdMoi,
                        nv, kh,
                        rs.getString("trangThai"),
                        rs.getString("ghiChu")
                );
                dsPhieu.add(pd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsPhieu;
    }

    /**
     * Tìm kiếm phiếu đổi hàng
     */
    public ArrayList<PhieuDoiHang> searchPhieuDoi(String keyword) {
        ArrayList<PhieuDoiHang> dsPhieu = new ArrayList<>();
        String sql = "SELECT pd.*, kh.tenKH, kh.sdt as sdtKH, nv.hoTen as tenNV "
                + "FROM PhieuDoiHang pd "
                + "LEFT JOIN KhachHang kh ON pd.maKH = kh.maKH "
                + "LEFT JOIN NhanVien nv ON pd.maNV = nv.maNV "
                + "WHERE pd.maPD LIKE ? OR kh.tenKH LIKE ? OR kh.sdt LIKE ? "
                + "ORDER BY pd.ngayDoi DESC";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setHoTen(rs.getString("tenNV"));

                KhachHang kh = null;
                if (rs.getString("maKH") != null) {
                    kh = new KhachHang();
                    kh.setMaKH(rs.getString("maKH"));
                    kh.setTenKH(rs.getString("tenKH"));
                    kh.setSdt(rs.getString("sdtKH"));
                }

                HoaDon hdGoc = null;
                if (rs.getString("maHDGoc") != null) {
                    hdGoc = new HoaDon();
                    hdGoc.setMaHD(rs.getString("maHDGoc"));
                }

                PhieuTraHang pt = null;
                if (rs.getString("maPT") != null) {
                    pt = new PhieuTraHang(rs.getString("maPT"), null, 0, null, null, null, null);
                }

                HoaDon hdMoi = null;
                if (rs.getString("maHDMoi") != null) {
                    hdMoi = new HoaDon();
                    hdMoi.setMaHD(rs.getString("maHDMoi"));
                }

                PhieuDoiHang pd = new PhieuDoiHang(
                        rs.getString("maPD"),
                        rs.getDate("ngayDoi") != null ? rs.getDate("ngayDoi").toLocalDate() : null,
                        rs.getDouble("tongTienTra"),
                        rs.getDouble("tongTienMoi"),
                        rs.getDouble("chenhLech"),
                        rs.getString("lyDo"),
                        hdGoc, pt, hdMoi,
                        nv, kh,
                        rs.getString("trangThai"),
                        rs.getString("ghiChu")
                );
                dsPhieu.add(pd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsPhieu;
    }

    /**
     * Lấy phiếu đổi theo mã
     */
    public PhieuDoiHang getPhieuDoiByMa(String maPD) {
        PhieuDoiHang pd = null;
        String sql = "SELECT pd.*, kh.tenKH, kh.sdt as sdtKH, nv.hoTen as tenNV "
                + "FROM PhieuDoiHang pd "
                + "LEFT JOIN KhachHang kh ON pd.maKH = kh.maKH "
                + "LEFT JOIN NhanVien nv ON pd.maNV = nv.maNV "
                + "WHERE pd.maPD = ?";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maPD);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setHoTen(rs.getString("tenNV"));

                KhachHang kh = null;
                if (rs.getString("maKH") != null) {
                    kh = new KhachHang();
                    kh.setMaKH(rs.getString("maKH"));
                    kh.setTenKH(rs.getString("tenKH"));
                    kh.setSdt(rs.getString("sdtKH"));
                }

                HoaDon hdGoc = null;
                if (rs.getString("maHDGoc") != null) {
                    hdGoc = new HoaDon();
                    hdGoc.setMaHD(rs.getString("maHDGoc"));
                }

                PhieuTraHang pt = null;
                if (rs.getString("maPT") != null) {
                    pt = new PhieuTraHang(rs.getString("maPT"), null, 0, null, null, null, null);
                }

                HoaDon hdMoi = null;
                if (rs.getString("maHDMoi") != null) {
                    hdMoi = new HoaDon();
                    hdMoi.setMaHD(rs.getString("maHDMoi"));
                }

                pd = new PhieuDoiHang(
                        rs.getString("maPD"),
                        rs.getDate("ngayDoi") != null ? rs.getDate("ngayDoi").toLocalDate() : null,
                        rs.getDouble("tongTienTra"),
                        rs.getDouble("tongTienMoi"),
                        rs.getDouble("chenhLech"),
                        rs.getString("lyDo"),
                        hdGoc, pt, hdMoi,
                        nv, kh,
                        rs.getString("trangThai"),
                        rs.getString("ghiChu")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pd;
    }
}
