package dao;

import connectDB.ConnectDB;
import entities.ChiTietPhieuNhap;
import entities.LoThuoc;
import entities.PhieuNhap;
import entities.Thuoc;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhieuNhapDAO {

    public PhieuNhapDAO() {
    }

    public String getNewMaPhieuNhap() {
        String newID = "PN001";
        String sql = "SELECT TOP 1 maPhieuNhap FROM PhieuNhap ORDER BY maPhieuNhap DESC";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                String lastID = rs.getString("maPhieuNhap");
                try {
                    int num = Integer.parseInt(lastID.substring(2)) + 1;
                    newID = String.format("PN%03d", num);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newID;
    }

    public boolean insert(PhieuNhap phieu) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "INSERT INTO PhieuNhap (maPhieuNhap, maNCC, maNV, ngayNhap, tongTien, ghiChu, trangThai) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, phieu.getMaPN());
            stmt.setString(2, phieu.getNcc().getMaNCC());
            stmt.setString(3, phieu.getNhanVien().getMaNV());
            stmt.setDate(4, Date.valueOf(phieu.getNgayTao()));
            stmt.setDouble(5, phieu.getTongTien());
            stmt.setString(6, phieu.getGhiChu());
            stmt.setString(7, phieu.getTrangThai());

            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return n > 0;
    }

    public ArrayList<PhieuNhap> getAllPhieuNhap() {
        ArrayList<PhieuNhap> dsPhieu = new ArrayList<>();
        String sql = "SELECT pn.*, ncc.tenNCC, nv.tenNV "
                + "FROM PhieuNhap pn "
                + "LEFT JOIN NhaCungCap ncc ON pn.maNCC = ncc.maNCC "
                + "LEFT JOIN NhanVien nv ON pn.maNV = nv.maNV "
                + "ORDER BY pn.ngayNhap DESC";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                // This would need proper entity mapping with NhanVien and NhaCungCap
                // Simplified for now
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsPhieu;
    }

    public PhieuNhap getPhieuNhapById(String maPhieu) {
        PhieuNhap phieu = null;
        String sql = "SELECT * FROM PhieuNhap WHERE maPhieuNhap = ?";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maPhieu);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Map to PhieuNhap entity
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return phieu;
    }

    public boolean taoPhieuNhap(PhieuNhap phieu, List<ChiTietPhieuNhap> listCT) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // Insert PhieuNhap
            String sqlPhieu = "INSERT INTO PhieuNhap (maPhieuNhap, maNCC, maNV, ngayNhap, tongTien, ghiChu, trangThai) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmtPhieu = con.prepareStatement(sqlPhieu);
            stmtPhieu.setString(1, phieu.getMaPN());
            stmtPhieu.setString(2, phieu.getNcc().getMaNCC());
            stmtPhieu.setString(3, phieu.getNhanVien().getMaNV());
            stmtPhieu.setDate(4, Date.valueOf(phieu.getNgayTao()));
            stmtPhieu.setDouble(5, phieu.getTongTien());
            stmtPhieu.setString(6, phieu.getGhiChu());
            stmtPhieu.setString(7, phieu.getTrangThai());
            stmtPhieu.executeUpdate();

            // Insert ChiTietPhieuNhap and update/create LoThuoc
            ChiTietPhieuNhapDAO ctDAO = new ChiTietPhieuNhapDAO();
            for (ChiTietPhieuNhap ct : listCT) {
                // Insert ChiTietPhieuNhap
                ctDAO.insert(ct);

                // Find or create LoThuoc with same drug and expiry date
                String sqlCheckLo = "SELECT maLo, soLuongTon FROM LoThuoc WHERE maThuoc = ? AND hanSuDung = ? AND isDeleted = 0";
                PreparedStatement stmtCheckLo = con.prepareStatement(sqlCheckLo);
                stmtCheckLo.setString(1, ct.getThuoc().getMaThuoc());
                stmtCheckLo.setDate(2, Date.valueOf(ct.getHanSuDung()));
                ResultSet rsLo = stmtCheckLo.executeQuery();

                if (rsLo.next()) {
                    // Update existing lot
                    String maLo = rsLo.getString("maLo");
                    int soLuongHienTai = rsLo.getInt("soLuongTon");
                    String sqlUpdateLo = "UPDATE LoThuoc SET soLuongTon = ? WHERE maLo = ?";
                    PreparedStatement stmtUpdateLo = con.prepareStatement(sqlUpdateLo);
                    stmtUpdateLo.setInt(1, soLuongHienTai + ct.getSoLuong());
                    stmtUpdateLo.setString(2, maLo);
                    stmtUpdateLo.executeUpdate();
                } else {
                    // Create new lot
                    String sqlNewLo = "SELECT TOP 1 maLo FROM LoThuoc ORDER BY maLo DESC";
                    Statement stNewLo = con.createStatement();
                    ResultSet rsNewLo = stNewLo.executeQuery(sqlNewLo);
                    String newMaLo = "LO001";
                    if (rsNewLo.next()) {
                        String lastID = rsNewLo.getString("maLo");
                        try {
                            int num = Integer.parseInt(lastID.substring(2)) + 1;
                            newMaLo = String.format("LO%03d", num);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }

                    String sqlInsertLo = "INSERT INTO LoThuoc (maLo, maThuoc, ngayNhap, hanSuDung, soLuongTon, trangThai, isDeleted) "
                            + "VALUES (?, ?, ?, ?, ?, ?, 0)";
                    PreparedStatement stmtInsertLo = con.prepareStatement(sqlInsertLo);
                    stmtInsertLo.setString(1, newMaLo);
                    stmtInsertLo.setString(2, ct.getThuoc().getMaThuoc());
                    stmtInsertLo.setDate(3, Date.valueOf(LocalDate.now()));
                    stmtInsertLo.setDate(4, Date.valueOf(ct.getHanSuDung()));
                    stmtInsertLo.setInt(5, ct.getSoLuong());
                    stmtInsertLo.setString(6, "Còn hàng");
                    stmtInsertLo.executeUpdate();
                }
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
                if (con != null) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}
