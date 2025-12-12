package dao;

import connectDB.ConnectDB;
import entities.KhachHang;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

public class KhachHangDAO {

    public KhachHangDAO() {
    }

    public ArrayList<KhachHang> getAllKhachHang() {
        ArrayList<KhachHang> dsKH = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang WHERE trangThai = 1";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                dsKH.add(mapResultSetToKhachHang(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsKH;
    }

    public KhachHang getKhachHangBySDT(String sdt) {
        KhachHang kh = null;
        String sql = "SELECT * FROM KhachHang WHERE sdt = ? AND trangThai = 1";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, sdt);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                kh = mapResultSetToKhachHang(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kh;
    }

    public KhachHang getKhachHangByID(String maKH) {
        KhachHang kh = null;
        String sql = "SELECT * FROM KhachHang WHERE maKH = ?";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maKH);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                kh = mapResultSetToKhachHang(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kh;
    }

    public boolean insert(KhachHang kh) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "INSERT INTO KhachHang (maKH, tenKH, sdt, gioiTinh, ngaySinh, diaChi, diemTichLuy, trangThai) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, kh.getMaKH());
            stmt.setString(2, kh.getTenKH());
            stmt.setString(3, kh.getSdt());
            stmt.setBoolean(4, kh.isGioiTinh());

            if (kh.getNgaySinh() != null) {
                stmt.setDate(5, Date.valueOf(kh.getNgaySinh()));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }

            stmt.setString(6, kh.getDiaChi());
            stmt.setInt(7, kh.getDiemTichLuy());
            stmt.setBoolean(8, kh.isTrangThai());

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

    public boolean update(KhachHang kh) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "UPDATE KhachHang SET tenKH=?, sdt=?, gioiTinh=?, ngaySinh=?, diaChi=?, diemTichLuy=?, trangThai=? WHERE maKH=?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, kh.getTenKH());
            stmt.setString(2, kh.getSdt());
            stmt.setBoolean(3, kh.isGioiTinh());

            if (kh.getNgaySinh() != null) {
                stmt.setDate(4, Date.valueOf(kh.getNgaySinh()));
            } else {
                stmt.setNull(4, java.sql.Types.DATE);
            }

            stmt.setString(5, kh.getDiaChi());
            stmt.setInt(6, kh.getDiemTichLuy());
            stmt.setBoolean(7, kh.isTrangThai());
            stmt.setString(8, kh.getMaKH());

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

    public boolean delete(String maKH) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "UPDATE KhachHang SET trangThai = 0 WHERE maKH = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, maKH);
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

    public boolean congDiem(String maKH, int diemCong) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "UPDATE KhachHang SET diemTichLuy = diemTichLuy + ? WHERE maKH = ?";
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, diemCong);
            stmt.setString(2, maKH);
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

    public boolean truDiem(String maKH, int diemTru) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {

            String sql = "UPDATE KhachHang SET diemTichLuy = CASE WHEN diemTichLuy >= ? THEN diemTichLuy - ? ELSE 0 END WHERE maKH = ?";
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, diemTru);
            stmt.setInt(2, diemTru);
            stmt.setString(3, maKH);
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

    private KhachHang mapResultSetToKhachHang(ResultSet rs) throws SQLException {
        String maKH = rs.getString("maKH");
        String tenKH = rs.getString("tenKH");
        String sdt = rs.getString("sdt");
        boolean gioiTinh = rs.getBoolean("gioiTinh");
        LocalDate ngaySinh = rs.getDate("ngaySinh") != null ? rs.getDate("ngaySinh").toLocalDate() : null;
        String diaChi = rs.getString("diaChi");
        int diemTichLuy = rs.getInt("diemTichLuy");
        boolean trangThai = rs.getBoolean("trangThai");

        return new KhachHang(maKH, tenKH, sdt, gioiTinh, ngaySinh, diaChi, diemTichLuy, trangThai);
    }
}
