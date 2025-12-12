package dao;

import connectDB.ConnectDB;
import dto.ThuocFullInfo;
import entities.NhomThuoc;
import entities.Thuoc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ThuocDAO {

    public ThuocDAO() {
    }

    public ArrayList<ThuocFullInfo> getAllThuocFullInfo() {
        ArrayList<ThuocFullInfo> dsThuoc = new ArrayList<>();

        String sql = "SELECT * FROM vw_DanhSachThuocFull";

        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                dsThuoc.add(new ThuocFullInfo(
                        rs.getString("maThuoc"),
                        rs.getString("tenThuoc"),
                        rs.getString("hoatChat"),
                        rs.getString("donViTinh"),
                        rs.getString("tenNhom"),
                        rs.getDouble("giaNhap"),
                        rs.getDouble("giaBan"),
                        rs.getInt("tonKho"),
                        rs.getInt("tonKhoBanDuoc"),
                        rs.getBoolean("trangThai")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsThuoc;
    }

    public ArrayList<String> getAllDonViTinh() {
        ArrayList<String> dsDVT = new ArrayList<>();
        String sql = "SELECT DISTINCT donViTinh FROM Thuoc";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                dsDVT.add(rs.getString("donViTinh"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsDVT;
    }

    public ArrayList<String> getAllNhomThuocName() {
        ArrayList<String> dsTenNhom = new ArrayList<>();
        String sql = "SELECT tenNhom FROM NhomThuoc";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                dsTenNhom.add(rs.getString("tenNhom"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsTenNhom;
    }

    public ThuocFullInfo getThuocFullInfoByID(String id) {
        ThuocFullInfo thuoc = null;
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM vw_DanhSachThuocFull WHERE maThuoc = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String maThuoc = rs.getString(1);
                String tenThuoc = rs.getString(2);
                String hoatChat = rs.getString(3);
                String donViTinh = rs.getString(4);
                String tenNhom = rs.getString(5);
                double giaNhap = rs.getDouble(6);
                double giaBan = rs.getDouble(7);
                int tonKho = rs.getInt(8);
                int tonKhoBanDuoc = rs.getInt(9);
                boolean trangThai = rs.getBoolean(10);
                thuoc = new ThuocFullInfo(maThuoc, tenThuoc, hoatChat, donViTinh, tenNhom, giaNhap, giaBan, tonKho, tonKhoBanDuoc, trangThai);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return thuoc;
    }

    public boolean delete(String maThuoc) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "UPDATE Thuoc SET trangThai = 0 WHERE maThuoc = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, maThuoc);
            n = stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
        return n > 0;
    }

    public boolean restore(String maThuoc) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "UPDATE Thuoc SET trangThai = 1 WHERE maThuoc = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, maThuoc);
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

    public boolean themThuocMoi(ThuocFullInfo t) {
        Connection con = null;
        PreparedStatement stmtThuoc = null;
        PreparedStatement stmtGia = null;

        try {
            con = ConnectDB.getConnection();

            con.setAutoCommit(false);

            String sqlThuoc = "INSERT INTO Thuoc (maThuoc, tenThuoc, hoatChat, donViTinh, maNhom, trangThai) VALUES (?, ?, ?, ?, ?, ?)";
            stmtThuoc = con.prepareStatement(sqlThuoc);
            stmtThuoc.setString(1, t.getMaThuoc());
            stmtThuoc.setString(2, t.getTenThuoc());
            stmtThuoc.setString(3, t.getHoatChat());
            stmtThuoc.setString(4, t.getDonViTinh());

            String maNhom = getMaNhomByTen(con, t.getTenNhom());
            stmtThuoc.setString(5, maNhom);

            stmtThuoc.setBoolean(6, true);
            stmtThuoc.executeUpdate();

            String maBG = "BG001";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT TOP 1 maBG FROM BangGia WHERE trangThai = 1");
            if (rs.next()) {
                maBG = rs.getString("maBG");
            } else {

                maBG = taoBangGiaMacDinh(con);
            }

            String sqlGia = "INSERT INTO ChiTietBangGia (maBG, maThuoc, donViTinh, giaBan) VALUES (?, ?, ?, ?)";
            stmtGia = con.prepareStatement(sqlGia);
            stmtGia.setString(1, maBG);
            stmtGia.setString(2, t.getMaThuoc());
            stmtGia.setString(3, t.getDonViTinh());
            stmtGia.setDouble(4, t.getGiaBan());
            stmtGia.executeUpdate();

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
                con.close();
            } catch (Exception ex) {
            }
        }
        return false;
    }

    private String getMaNhomByTen(Connection con, String tenNhom) throws SQLException {
        String sql = "SELECT maNhom FROM NhomThuoc WHERE tenNhom = ?";
        PreparedStatement p = con.prepareStatement(sql);
        p.setString(1, tenNhom);
        ResultSet rs = p.executeQuery();
        if (rs.next()) {
            return rs.getString("maNhom");
        }
        return "NT007";
    }

    private String taoBangGiaMacDinh(Connection con) throws SQLException {
        String newID = "BG001";
        String sql = "INSERT INTO BangGia (maBG, tenBG, ngayHieuLuc, trangThai) VALUES (?, ?, GETDATE(), 1)";
        PreparedStatement p = con.prepareStatement(sql);
        p.setString(1, newID);
        p.setString(2, "Bảng giá tiêu chuẩn");
        p.executeUpdate();
        return newID;
    }

    public boolean themNhomThuocNhanh(String tenNhom) {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = ConnectDB.getConnection();

            String sqlMaxID = "SELECT TOP 1 maNhom FROM NhomThuoc ORDER BY maNhom DESC";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sqlMaxID);
            String nextID = "NT001";

            if (rs.next()) {
                String lastID = rs.getString("maNhom");
                try {
                    int num = Integer.parseInt(lastID.substring(2)) + 1;
                    nextID = String.format("NT%03d", num);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String sqlInsert = "INSERT INTO NhomThuoc(maNhom, tenNhom) VALUES(?, ?)";
            stmt = con.prepareStatement(sqlInsert);
            stmt.setString(1, nextID);
            stmt.setString(2, tenNhom);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
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
        return false;
    }
}
