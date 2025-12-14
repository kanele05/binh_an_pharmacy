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

    // Hàm tạo mã phiếu nhập mới tự động (VD: PN001 -> PN002)
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

    // Hàm TRANSACTION quan trọng nhất
    public boolean createPhieuNhap(PhieuNhap pn, ArrayList<ChiTietPhieuNhap> listCT) {
        Connection con = null;
        PreparedStatement stmtPN = null;
        PreparedStatement stmtCT = null;
        CallableStatement cstmtLo = null; // Để gọi Stored Procedure
        boolean result = false;

        try {
            con = ConnectDB.getConnection();
            // 1. Tắt Auto Commit để bắt đầu Transaction
            con.setAutoCommit(false);

            // 2. Insert bảng PhieuNhap
            String sqlPN = "INSERT INTO PhieuNhap (maPN, ngayTao, tongTien, trangThai, maNV, maNCC, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmtPN = con.prepareStatement(sqlPN);
            stmtPN.setString(1, pn.getMaPN());
            stmtPN.setDate(2, java.sql.Date.valueOf(pn.getNgayTao())); // Lưu ý convert Date
            stmtPN.setDouble(3, pn.getTongTien());
            stmtPN.setString(4, "Đã nhập");
            stmtPN.setString(5, pn.getNhanVien().getMaNV());
            stmtPN.setString(6, pn.getNcc().getMaNCC());
            stmtPN.setString(7, "Nhập hàng từ hệ thống");
            stmtPN.executeUpdate();

            // 3. Insert ChiTietPhieuNhap & Cập nhật Lô (Gọi SP)
            String sqlCT = "INSERT INTO ChiTietPhieuNhap (maPN, maThuoc, maLo, hanSuDung, soLuong, donGia, thanhTien) VALUES (?, ?, ?, ?, ?, ?, ?)";
            String sqlSP = "{call sp_TimHoacTaoLoThuoc(?, ?, ?, ?)}"; // Input: MaThuoc, HSD, SL -> Output: MaLo
            
            stmtCT = con.prepareStatement(sqlCT);
            cstmtLo = con.prepareCall(sqlSP);

            for (ChiTietPhieuNhap ct : listCT) {
                // A. Gọi SP để lấy/tạo Mã Lô và cập nhật tồn kho
                cstmtLo.setString(1, ct.getThuoc().getMaThuoc());
                cstmtLo.setDate(2, java.sql.Date.valueOf(ct.getHanSuDung())); // java.util.Date -> java.sql.Date
                cstmtLo.setInt(3, ct.getSoLuong());
                cstmtLo.registerOutParameter(4, Types.VARCHAR); // Tham số OUTPUT @MaLo
                cstmtLo.execute();
                
                String maLo = cstmtLo.getString(4); // Lấy mã lô vừa được SP trả về
                
                // B. Insert Chi Tiết
                stmtCT.setString(1, pn.getMaPN());
                stmtCT.setString(2, ct.getThuoc().getMaThuoc());
                stmtCT.setString(3, maLo); // Dùng mã lô lấy từ SP
                stmtCT.setDate(4, java.sql.Date.valueOf(ct.getHanSuDung()));
                stmtCT.setInt(5, ct.getSoLuong());
                stmtCT.setDouble(6, ct.getDonGia());
                stmtCT.setDouble(7, ct.getThanhTien());
                stmtCT.executeUpdate();
            }

            // 4. Nếu chạy đến đây mà không lỗi -> Commit (Lưu thật)
            con.commit();
            result = true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (con != null) con.rollback(); // Có lỗi -> Hoàn tác mọi thứ
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            // Đóng kết nối và bật lại auto commit
            try {
                if (con != null) con.setAutoCommit(true);
                if (stmtPN != null) stmtPN.close();
                if (stmtCT != null) stmtCT.close();
                if (cstmtLo != null) cstmtLo.close();
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
                    rs.getString("ghiChu")
                );
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
                    rs.getString("ghiChu")
                );
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
                    sql.append("AND MONTH(pn.ngayTao) = MONTH(DATEADD(MONTH, -1, GETDATE())) AND YEAR(pn.ngayTao) = YEAR(DATEADD(MONTH, -1, GETDATE())) ");
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
                    rs.getString("ghiChu")
                );
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
                String sqlRollback = "UPDATE LoThuoc SET soLuongTon = soLuongTon - ct.soLuong " +
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
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (con != null) con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}