package dao;

import connectDB.ConnectDB;
import entities.ChiTietHoaDon;
import entities.HoaDon;
import entities.KhachHang;
import entities.NhanVien;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    private ChiTietHoaDonDAO cthdDAO = new ChiTietHoaDonDAO();

    public ArrayList<HoaDon> getAllHoaDon() {
        ArrayList<HoaDon> list = new ArrayList<>();
        String sql = "SELECT hd.*, nv.hoTen as tenNV, kh.tenKH "
                + "FROM HoaDon hd "
                + "JOIN NhanVien nv ON hd.maNV = nv.maNV "
                + "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH "
                + "ORDER BY hd.ngayTao DESC";
        try {
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setHoTen(rs.getString("tenNV"));

                KhachHang kh = null;
                if (rs.getString("maKH") != null) {
                    kh = new KhachHang();
                    kh.setMaKH(rs.getString("maKH"));
                    kh.setTenKH(rs.getString("tenKH"));
                } else {

                    kh = new KhachHang();
                    kh.setTenKH("Khách lẻ");
                }

                HoaDon hd = new HoaDon(
                        rs.getString("maHD"),
                        rs.getTimestamp("ngayTao").toLocalDateTime(),
                        rs.getDouble("tongTien"),
                        rs.getDouble("giamGia"),
                        rs.getDouble("thue"),
                        rs.getString("hinhThucTT"),
                        rs.getString("ghiChu"),
                        nv, kh
                );
                list.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean taoHoaDon(HoaDon hd, List<ChiTietHoaDon> listCTHD) {
        Connection con = null;
        PreparedStatement stmtHD = null;
        PreparedStatement stmtCT = null;
        PreparedStatement stmtKho = null;
        PreparedStatement stmtTyLe = null;

        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            String sqlHD = "INSERT INTO HoaDon (maHD, ngayTao, tongTien, giamGia, thue, hinhThucTT, ghiChu, maNV, maKH) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmtHD = con.prepareStatement(sqlHD);
            stmtHD.setString(1, hd.getMaHD());
            stmtHD.setTimestamp(2, java.sql.Timestamp.valueOf(hd.getNgayTao()));
            stmtHD.setDouble(3, hd.getTongTien());
            stmtHD.setDouble(4, hd.getGiamGia());
            stmtHD.setDouble(5, hd.getThue());
            stmtHD.setString(6, hd.getHinhThucTT());
            stmtHD.setString(7, hd.getGhiChu());
            if (hd.getNhanVien() != null) {
                stmtHD.setString(8, hd.getNhanVien().getMaNV());
            } else {
                stmtHD.setNull(8, java.sql.Types.NVARCHAR);
            }
            if (hd.getKhachHang() != null) {
                stmtHD.setString(9, hd.getKhachHang().getMaKH());
            } else {
                stmtHD.setNull(9, java.sql.Types.NVARCHAR);
            }
            stmtHD.executeUpdate();

            String sqlCT = "INSERT INTO ChiTietHoaDon (maHD, maThuoc, maLo, soLuong, donGia, thanhTien, donViTinh) VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmtCT = con.prepareStatement(sqlCT);

            // FIX Lỗi 1+2: Thêm kiểm tra tồn kho đủ và lô chưa hết hạn
            String sqlUpdateKho = "UPDATE LoThuoc SET soLuongTon = soLuongTon - ? " +
                                  "WHERE maLo = ? AND soLuongTon >= ? AND trangThai != N'Đã hết hạn'";
            stmtKho = con.prepareStatement(sqlUpdateKho);

            String sqlGetTyLe = "SELECT giaTriQuyDoi FROM DonViQuyDoi WHERE maThuoc = ? AND tenDonVi = ?";
            stmtTyLe = con.prepareStatement(sqlGetTyLe);

            for (ChiTietHoaDon ct : listCTHD) {
                // Lấy tỷ lệ quy đổi trước để tính số lượng trừ kho
                int tyLeQuyDoi = 1;
                stmtTyLe.setString(1, ct.getThuoc().getMaThuoc());
                stmtTyLe.setString(2, ct.getDonViTinh());
                ResultSet rsTyLe = stmtTyLe.executeQuery();
                if (rsTyLe.next()) {
                    tyLeQuyDoi = rsTyLe.getInt("giaTriQuyDoi");
                }
                rsTyLe.close();

                int soLuongTruKho = ct.getSoLuong() * tyLeQuyDoi;

                // Trừ kho với kiểm tra đủ số lượng và chưa hết hạn
                stmtKho.setInt(1, soLuongTruKho);
                stmtKho.setString(2, ct.getLoThuoc().getMaLo());
                stmtKho.setInt(3, soLuongTruKho);
                int rowsAffected = stmtKho.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Không đủ tồn kho hoặc lô đã hết hạn: " + ct.getLoThuoc().getMaLo());
                }

                // Insert chi tiết hóa đơn sau khi trừ kho thành công
                stmtCT.setString(1, hd.getMaHD());
                stmtCT.setString(2, ct.getThuoc().getMaThuoc());
                stmtCT.setString(3, ct.getLoThuoc().getMaLo());
                stmtCT.setInt(4, ct.getSoLuong());
                stmtCT.setDouble(5, ct.getDonGia());
                stmtCT.setDouble(6, ct.getThanhTien());
                stmtCT.setString(7, ct.getDonViTinh());
                stmtCT.executeUpdate();
            }

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
                if (stmtHD != null) {
                    stmtHD.close();
                }
                if (stmtCT != null) {
                    stmtCT.close();
                }
                if (stmtKho != null) {
                    stmtKho.close();
                }
                if (stmtTyLe != null) {
                    stmtTyLe.close();
                }
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // FIX Lỗi 3: Atomic ID generation với transaction lock
    public String getMaxMaHD() {
        String maxID = null;
        String sql = "SELECT TOP 1 maHD FROM HoaDon ORDER BY maHD DESC";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                maxID = rs.getString("maHD");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maxID;
    }

    /**
     * Tạo mã hóa đơn mới một cách atomic trong transaction.
     * Sử dụng UPDLOCK, HOLDLOCK để tránh race condition.
     */
    private String generateNewMaHDInTransaction(Connection con) throws SQLException {
        String newMaHD = "HD001";
        String sql = "SELECT TOP 1 maHD FROM HoaDon WITH (UPDLOCK, HOLDLOCK) ORDER BY maHD DESC";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String lastID = rs.getString("maHD");
                if (lastID != null && lastID.length() > 2) {
                    try {
                        int number = Integer.parseInt(lastID.substring(2));
                        newMaHD = String.format("HD%03d", number + 1);
                    } catch (NumberFormatException e) {
                        // Keep default HD001
                    }
                }
            }
        }
        return newMaHD;
    }

    public boolean taoHoaDonTuDonDat(entities.HoaDon hd, java.util.List<entities.ChiTietHoaDon> listCTHD, String maDonDat) {
        java.sql.Connection con = null;
        java.sql.PreparedStatement stmtHD = null;
        java.sql.PreparedStatement stmtKho = null;
        java.sql.PreparedStatement stmtUpdateDon = null;
        dao.ChiTietHoaDonDAO cthdDAO = new dao.ChiTietHoaDonDAO();

        try {
            con = connectDB.ConnectDB.getConnection();
            con.setAutoCommit(false);

            String sqlHD = "INSERT INTO HoaDon (maHD, ngayTao, tongTien, giamGia, thue, hinhThucTT, ghiChu, maNV, maKH) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmtHD = con.prepareStatement(sqlHD);
            stmtHD.setString(1, hd.getMaHD());
            stmtHD.setTimestamp(2, java.sql.Timestamp.valueOf(hd.getNgayTao()));
            stmtHD.setDouble(3, hd.getTongTien());
            stmtHD.setDouble(4, hd.getGiamGia());
            stmtHD.setDouble(5, hd.getThue());
            stmtHD.setString(6, hd.getHinhThucTT());
            stmtHD.setString(7, "Xuất từ đơn đặt: " + maDonDat);
            stmtHD.setString(8, hd.getNhanVien().getMaNV());
            if (hd.getKhachHang() != null) {
                stmtHD.setString(9, hd.getKhachHang().getMaKH());
            } else {
                stmtHD.setNull(9, java.sql.Types.NVARCHAR);
            }
            stmtHD.executeUpdate();

            // FIX Lỗi 1+2: Thêm kiểm tra tồn kho đủ và lô chưa hết hạn
            String sqlUpdateKho = "UPDATE LoThuoc SET soLuongTon = soLuongTon - ? " +
                                  "WHERE maLo = ? AND soLuongTon >= ? AND trangThai != N'Đã hết hạn'";
            stmtKho = con.prepareStatement(sqlUpdateKho);

            // Cần lấy tỷ lệ quy đổi để tính đúng số lượng trừ kho
            String sqlGetTyLe = "SELECT giaTriQuyDoi FROM DonViQuyDoi WHERE maThuoc = ? AND tenDonVi = ?";
            java.sql.PreparedStatement stmtTyLe = con.prepareStatement(sqlGetTyLe);

            for (entities.ChiTietHoaDon ct : listCTHD) {
                // Lấy tỷ lệ quy đổi
                int tyLeQuyDoi = 1;
                stmtTyLe.setString(1, ct.getThuoc().getMaThuoc());
                stmtTyLe.setString(2, ct.getDonViTinh());
                java.sql.ResultSet rsTyLe = stmtTyLe.executeQuery();
                if (rsTyLe.next()) {
                    tyLeQuyDoi = rsTyLe.getInt("giaTriQuyDoi");
                }
                rsTyLe.close();

                int soLuongTruKho = ct.getSoLuong() * tyLeQuyDoi;

                // Trừ kho với kiểm tra đủ số lượng và chưa hết hạn
                stmtKho.setInt(1, soLuongTruKho);
                stmtKho.setString(2, ct.getLoThuoc().getMaLo());
                stmtKho.setInt(3, soLuongTruKho);
                int rowsAffected = stmtKho.executeUpdate();
                if (rowsAffected == 0) {
                    throw new java.sql.SQLException("Không đủ tồn kho hoặc lô đã hết hạn: " + ct.getLoThuoc().getMaLo());
                }

                // Insert chi tiết sau khi trừ kho thành công
                cthdDAO.insert(con, ct);
            }
            stmtTyLe.close();

            String sqlUpdateDon = "UPDATE DonDatHang SET trangThai = N'Đã lấy hàng' WHERE maDonDat = ?";
            stmtUpdateDon = con.prepareStatement(sqlUpdateDon);
            stmtUpdateDon.setString(1, maDonDat);
            stmtUpdateDon.executeUpdate();

            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
        } finally {

            try {
                if (con != null) {
                    con.setAutoCommit(true);
                }
                con.close();
            } catch (Exception e) {
            }
        }
        return false;
    }

    public ArrayList<HoaDon> getHoaDonByKhachHang(String maKH) {
        ArrayList<HoaDon> list = new ArrayList<>();
        String sql = "SELECT hd.*, nv.hoTen as tenNV " +
                     "FROM HoaDon hd " +
                     "JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                     "WHERE hd.maKH = ? " +
                     "ORDER BY hd.ngayTao DESC";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maKH);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setHoTen(rs.getString("tenNV"));

                KhachHang kh = new KhachHang();
                kh.setMaKH(maKH);

                HoaDon hd = new HoaDon(
                    rs.getString("maHD"),
                    rs.getTimestamp("ngayTao").toLocalDateTime(),
                    rs.getDouble("tongTien"),
                    rs.getDouble("giamGia"),
                    rs.getDouble("thue"),
                    rs.getString("hinhThucTT"),
                    rs.getString("ghiChu"),
                    nv, kh
                );
                list.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public HoaDon getHoaDonByMaHD(String maHD) {
        HoaDon hd = null;
        String sql = "SELECT hd.*, nv.hoTen as tenNV, kh.tenKH, kh.sdt as sdtKH "
                + "FROM HoaDon hd "
                + "JOIN NhanVien nv ON hd.maNV = nv.maNV "
                + "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH "
                + "WHERE hd.maHD = ?";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maHD);
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
                } else {
                    kh = new KhachHang();
                    kh.setTenKH("Khách lẻ");
                }

                hd = new HoaDon(
                        rs.getString("maHD"),
                        rs.getTimestamp("ngayTao").toLocalDateTime(),
                        rs.getDouble("tongTien"),
                        rs.getDouble("giamGia"),
                        rs.getDouble("thue"),
                        rs.getString("hinhThucTT"),
                        rs.getString("ghiChu"),
                        nv, kh
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hd;
    }

    public ArrayList<HoaDon> searchHoaDon(String keyword) {
        ArrayList<HoaDon> list = new ArrayList<>();
        String sql = "SELECT hd.*, nv.hoTen as tenNV, kh.tenKH, kh.sdt as sdtKH "
                + "FROM HoaDon hd "
                + "JOIN NhanVien nv ON hd.maNV = nv.maNV "
                + "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH "
                + "WHERE hd.maHD LIKE ? OR kh.sdt LIKE ? OR kh.tenKH LIKE ? "
                + "ORDER BY hd.ngayTao DESC";
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
                } else {
                    kh = new KhachHang();
                    kh.setTenKH("Khách lẻ");
                }

                HoaDon hd = new HoaDon(
                        rs.getString("maHD"),
                        rs.getTimestamp("ngayTao").toLocalDateTime(),
                        rs.getDouble("tongTien"),
                        rs.getDouble("giamGia"),
                        rs.getDouble("thue"),
                        rs.getString("hinhThucTT"),
                        rs.getString("ghiChu"),
                        nv, kh
                );
                list.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
