package dao;

import connectDB.ConnectDB;
import entities.ChiTietPhieuNhap;
import entities.NhaCungCap;
import entities.NhanVien;
import entities.PhieuNhap;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhieuNhapDAO {

    // FIX Lỗi 3: Giữ method cũ cho tương thích ngược (non-atomic, dùng cho UI
    // display)
    public String generateNewMaPN() {
        String newMa = "PN001";
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT TOP 1 maPN FROM PhieuNhap ORDER BY maPN DESC";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                String lastMa = rs.getString(1);
                int number = Integer.parseInt(lastMa.substring(2)) + 1;
                newMa = String.format("PN%03d", number);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newMa;
    }

    /**
     * Tạo mã phiếu nhập mới một cách atomic trong transaction.
     * Sử dụng UPDLOCK, HOLDLOCK để tránh race condition.
     */
    private String generateNewMaPNInTransaction(Connection con) throws SQLException {
        String newMa = "PN001";
        String sql = "SELECT TOP 1 maPN FROM PhieuNhap WITH (UPDLOCK, HOLDLOCK) ORDER BY maPN DESC";
        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String lastMa = rs.getString(1);
                if (lastMa != null && lastMa.length() > 2) {
                    try {
                        int number = Integer.parseInt(lastMa.substring(2)) + 1;
                        newMa = String.format("PN%03d", number);
                    } catch (NumberFormatException e) {
                        // Keep default PN001
                    }
                }
            }
        }
        return newMa;
    }

    // DEPRECATED: Không sử dụng method này. Dùng flow riêng trong FormNhapHang để
    // tránh nhập trùng tồn kho
    // Hàm TRANSACTION - Tạo phiếu nhập với trạng thái "Chờ nhập" (chưa cộng tồn
    // kho)
    @Deprecated
    public boolean createPhieuNhap(PhieuNhap pn, ArrayList<ChiTietPhieuNhap> listCT) {
        Connection con = null;
        PreparedStatement stmtPN = null;
        PreparedStatement stmtCT = null;
        CallableStatement cstmtLo = null;
        boolean result = false;

        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // 1. Insert bảng PhieuNhap với trạng thái "Chờ nhập"
            String sqlPN = "INSERT INTO PhieuNhap (maPN, ngayTao, tongTien, trangThai, maNV, maNCC, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmtPN = con.prepareStatement(sqlPN);
            stmtPN.setString(1, pn.getMaPN());
            stmtPN.setDate(2, java.sql.Date.valueOf(pn.getNgayTao()));
            stmtPN.setDouble(3, pn.getTongTien());
            stmtPN.setString(4, "Chờ nhập"); // Trạng thái mặc định là "Chờ nhập"
            stmtPN.setString(5, pn.getNhanVien().getMaNV());
            stmtPN.setString(6, pn.getNcc().getMaNCC());
            stmtPN.setString(7, pn.getGhiChu() != null ? pn.getGhiChu() : "Nhập hàng từ hệ thống");
            stmtPN.executeUpdate();

            // 2. Insert ChiTietPhieuNhap - Dùng SP không cộng tồn kho
            String sqlCT = "INSERT INTO ChiTietPhieuNhap (maPN, maThuoc, maLo, hanSuDung, soLuong, donGia, thanhTien, donViTinh) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            String sqlSP = "{call sp_TimHoacTaoLoThuoc_KhongCongTon(?, ?, ?)}"; // Chỉ tạo lô, không cộng tồn

            stmtCT = con.prepareStatement(sqlCT);
            cstmtLo = con.prepareCall(sqlSP);

            for (ChiTietPhieuNhap ct : listCT) {
                // A. Gọi SP để lấy/tạo Mã Lô (không cộng tồn kho)
                cstmtLo.setString(1, ct.getThuoc().getMaThuoc());
                cstmtLo.setDate(2, java.sql.Date.valueOf(ct.getHanSuDung()));
                cstmtLo.registerOutParameter(3, Types.VARCHAR);
                cstmtLo.execute();

                String maLo = cstmtLo.getString(3);

                // B. Insert Chi Tiết với đơn vị tính
                stmtCT.setString(1, pn.getMaPN());
                stmtCT.setString(2, ct.getThuoc().getMaThuoc());
                stmtCT.setString(3, maLo);
                stmtCT.setDate(4, java.sql.Date.valueOf(ct.getHanSuDung()));
                stmtCT.setInt(5, ct.getSoLuong());
                stmtCT.setDouble(6, ct.getDonGia());
                stmtCT.setDouble(7, ct.getThanhTien());
                stmtCT.setString(8, ct.getDonViTinh());
                stmtCT.executeUpdate();
            }

            con.commit();
            result = true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (con != null)
                    con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (con != null)
                    con.setAutoCommit(true);
                if (stmtPN != null)
                    stmtPN.close();
                if (stmtCT != null)
                    stmtCT.close();
                if (cstmtLo != null)
                    cstmtLo.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public boolean insertHeader(PhieuNhap pn) {
        Connection con = null;
        PreparedStatement stmtPN = null;
        try {
            con = ConnectDB.getConnection();
            String sqlPN = "INSERT INTO PhieuNhap (maPN, ngayTao, tongTien, trangThai, maNV, maNCC, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmtPN = con.prepareStatement(sqlPN);
            stmtPN.setString(1, pn.getMaPN());
            stmtPN.setDate(2, java.sql.Date.valueOf(pn.getNgayTao()));
            stmtPN.setDouble(3, pn.getTongTien());
            stmtPN.setString(4, pn.getTrangThai());
            stmtPN.setString(5, pn.getNhanVien().getMaNV());
            stmtPN.setString(6, pn.getNcc().getMaNCC());
            stmtPN.setString(7, pn.getGhiChu());
            return stmtPN.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmtPN != null) {
                    stmtPN.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean xacNhanNhapKho(String maPN) {
        Connection con = null;
        CallableStatement cstmt = null;
        PreparedStatement stmtGetCT = null;
        boolean result = false;

        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            String sqlSP = "{call sp_XacNhanNhapKho(?)}";
            cstmt = con.prepareCall(sqlSP);
            cstmt.setString(1, maPN);
            cstmt.execute();

            String sqlGetCT = "SELECT maThuoc, donViTinh, donGia FROM ChiTietPhieuNhap WHERE maPN = ?";
            stmtGetCT = con.prepareStatement(sqlGetCT);
            stmtGetCT.setString(1, maPN);
            ResultSet rs = stmtGetCT.executeQuery();

            ChiTietBangGiaDAO ctbgDAO = new ChiTietBangGiaDAO();
            while (rs.next()) {
                String maThuoc = rs.getString("maThuoc");
                String donViTinh = rs.getString("donViTinh");
                double giaNhap = rs.getDouble("donGia");

                ctbgDAO.capNhatGiaNhap(maThuoc, donViTinh, giaNhap);
            }

            con.commit();
            result = true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (con != null)
                    con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (con != null)
                    con.setAutoCommit(true);
                if (cstmt != null)
                    cstmt.close();
                if (stmtGetCT != null)
                    stmtGetCT.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    // Lấy tất cả phiếu nhập
    public List<PhieuNhap> getAllPhieuNhap() {
        List<PhieuNhap> list = new ArrayList<>();
        String sql = "SELECT pn.*, nv.hoTen AS tenNV, ncc.tenNCC " +
                "FROM PhieuNhap pn " +
                "LEFT JOIN NhanVien nv ON pn.maNV = nv.maNV " +
                "LEFT JOIN NhaCungCap ncc ON pn.maNCC = ncc.maNCC " +
                "ORDER BY pn.ngayTao DESC";
        try {
            Connection con = ConnectDB.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                NhanVien nv = new NhanVien(rs.getString("maNV"));
                nv.setHoTen(rs.getString("tenNV"));

                NhaCungCap ncc = new NhaCungCap(rs.getString("maNCC"));
                ncc.setTenNCC(rs.getString("tenNCC"));

                PhieuNhap pn = new PhieuNhap(
                        rs.getString("maPN"),
                        rs.getDate("ngayTao") != null ? rs.getDate("ngayTao").toLocalDate() : null,
                        rs.getDouble("tongTien"),
                        rs.getString("trangThai"),
                        nv,
                        ncc,
                        rs.getString("ghiChu"));
                list.add(pn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy phiếu nhập theo mã
    public PhieuNhap getPhieuNhapById(String maPN) {
        String sql = "SELECT pn.*, nv.hoTen AS tenNV, ncc.tenNCC " +
                "FROM PhieuNhap pn " +
                "LEFT JOIN NhanVien nv ON pn.maNV = nv.maNV " +
                "LEFT JOIN NhaCungCap ncc ON pn.maNCC = ncc.maNCC " +
                "WHERE pn.maPN = ?";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maPN);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                NhanVien nv = new NhanVien(rs.getString("maNV"));
                nv.setHoTen(rs.getString("tenNV"));

                NhaCungCap ncc = new NhaCungCap(rs.getString("maNCC"));
                ncc.setTenNCC(rs.getString("tenNCC"));

                return new PhieuNhap(
                        rs.getString("maPN"),
                        rs.getDate("ngayTao") != null ? rs.getDate("ngayTao").toLocalDate() : null,
                        rs.getDouble("tongTien"),
                        rs.getString("trangThai"),
                        nv,
                        ncc,
                        rs.getString("ghiChu"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Tìm kiếm phiếu nhập theo từ khóa và bộ lọc
    public List<PhieuNhap> searchPhieuNhap(String keyword, String trangThai, String thoiGian) {
        List<PhieuNhap> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT pn.*, nv.hoTen AS tenNV, ncc.tenNCC ");
        sql.append("FROM PhieuNhap pn ");
        sql.append("LEFT JOIN NhanVien nv ON pn.maNV = nv.maNV ");
        sql.append("LEFT JOIN NhaCungCap ncc ON pn.maNCC = ncc.maNCC ");
        sql.append("WHERE 1=1 ");

        // Filter theo keyword
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (pn.maPN LIKE ? OR ncc.tenNCC LIKE ? OR nv.hoTen LIKE ?) ");
        }

        // Filter theo trạng thái
        if (trangThai != null && !trangThai.equals("Tất cả")) {
            sql.append("AND pn.trangThai = ? ");
        }

        // Filter theo thời gian
        if (thoiGian != null) {
            switch (thoiGian) {
                case "Tháng này":
                    sql.append("AND MONTH(pn.ngayTao) = MONTH(GETDATE()) AND YEAR(pn.ngayTao) = YEAR(GETDATE()) ");
                    break;
                case "Tháng trước":
                    sql.append(
                            "AND MONTH(pn.ngayTao) = MONTH(DATEADD(MONTH, -1, GETDATE())) AND YEAR(pn.ngayTao) = YEAR(DATEADD(MONTH, -1, GETDATE())) ");
                    break;
            }
        }

        sql.append("ORDER BY pn.ngayTao DESC");

        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql.toString());

            int paramIndex = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(paramIndex++, kw);
                ps.setString(paramIndex++, kw);
                ps.setString(paramIndex++, kw);
            }
            if (trangThai != null && !trangThai.equals("Tất cả")) {
                ps.setString(paramIndex++, trangThai);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                NhanVien nv = new NhanVien(rs.getString("maNV"));
                nv.setHoTen(rs.getString("tenNV"));

                NhaCungCap ncc = new NhaCungCap(rs.getString("maNCC"));
                ncc.setTenNCC(rs.getString("tenNCC"));

                PhieuNhap pn = new PhieuNhap(
                        rs.getString("maPN"),
                        rs.getDate("ngayTao") != null ? rs.getDate("ngayTao").toLocalDate() : null,
                        rs.getDouble("tongTien"),
                        rs.getString("trangThai"),
                        nv,
                        ncc,
                        rs.getString("ghiChu"));
                list.add(pn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Cập nhật trạng thái phiếu nhập
    public boolean updateTrangThai(String maPN, String trangThai) {
        String sql = "UPDATE PhieuNhap SET trangThai = ? WHERE maPN = ?";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, trangThai);
            ps.setString(2, maPN);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Hủy phiếu nhập (cập nhật trạng thái và rollback tồn kho nếu cần)
    public boolean huyPhieuNhap(String maPN) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // Lấy thông tin phiếu nhập
            PhieuNhap pn = getPhieuNhapById(maPN);
            if (pn == null || pn.getTrangThai().equals("Đã hủy")) {
                return false;
            }

            // Nếu đã nhập kho, cần rollback tồn kho
            if (pn.getTrangThai().equals("Đã nhập")) {
                // FIX Lỗi 4: Sửa cú pháp SQL Server UPDATE...FROM
                String sqlRollback = "UPDATE l SET l.soLuongTon = l.soLuongTon - ct.soLuong " +
                        "FROM LoThuoc l " +
                        "INNER JOIN ChiTietPhieuNhap ct ON l.maLo = ct.maLo " +
                        "WHERE ct.maPN = ?";
                PreparedStatement psRollback = con.prepareStatement(sqlRollback);
                psRollback.setString(1, maPN);
                psRollback.executeUpdate();
            }

            // Cập nhật trạng thái phiếu
            String sqlUpdate = "UPDATE PhieuNhap SET trangThai = N'Đã hủy' WHERE maPN = ?";
            PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
            psUpdate.setString(1, maPN);
            psUpdate.executeUpdate();

            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (con != null)
                    con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (con != null)
                    con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}