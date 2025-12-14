package dao;

import connectDB.ConnectDB;
import entities.NhaCungCap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class NhaCungCapDAO {

    public NhaCungCapDAO() {
    }

    public ArrayList<NhaCungCap> getAllNhaCungCap() {
        ArrayList<NhaCungCap> dsNCC = new ArrayList<>();
        String sql = "SELECT * FROM NhaCungCap";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                dsNCC.add(mapResultSetToNhaCungCap(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsNCC;
    }

    public NhaCungCap getNhaCungCapByID(String maNCC) {
        NhaCungCap ncc = null;
        String sql = "SELECT * FROM NhaCungCap WHERE maNCC = ?";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maNCC);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ncc = mapResultSetToNhaCungCap(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ncc;
    }

    public boolean insert(NhaCungCap ncc) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "INSERT INTO NhaCungCap (maNCC, tenNCC, sdt, email, diaChi, nguoiLienHe) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, ncc.getMaNCC());
            stmt.setString(2, ncc.getTenNCC());
            stmt.setString(3, ncc.getSdt());
            stmt.setString(4, ncc.getEmail());
            stmt.setString(5, ncc.getDiaChi());
            stmt.setString(6, ncc.getNguoiLienHe());

            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
        return n > 0;
    }

    public boolean update(NhaCungCap ncc) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "UPDATE NhaCungCap SET tenNCC = ?, sdt = ?, email = ?, diaChi = ?, nguoiLienHe = ? WHERE maNCC = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, ncc.getTenNCC());
            stmt.setString(2, ncc.getSdt());
            stmt.setString(3, ncc.getEmail());
            stmt.setString(4, ncc.getDiaChi());
            stmt.setString(5, ncc.getNguoiLienHe());
            stmt.setString(6, ncc.getMaNCC());

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

    public boolean delete(String maNCC) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "DELETE FROM NhaCungCap WHERE maNCC = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, maNCC);
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

    public String getNewMaNCC() {
        String newID = "NCC001";
        String sql = "SELECT TOP 1 maNCC FROM NhaCungCap ORDER BY maNCC DESC";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                String lastID = rs.getString("maNCC");
                try {
                    int num = Integer.parseInt(lastID.substring(3)) + 1;
                    newID = String.format("NCC%03d", num);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newID;
    }

    private NhaCungCap mapResultSetToNhaCungCap(ResultSet rs) throws SQLException {
        String maNCC = rs.getString("maNCC");
        String tenNCC = rs.getString("tenNCC");
        String sdt = rs.getString("sdt");
        String email = rs.getString("email");
        String diaChi = rs.getString("diaChi");
        String nguoiLienHe = rs.getString("nguoiLienHe");
        return new NhaCungCap(maNCC, tenNCC, sdt, email, diaChi, nguoiLienHe);
    }
}
