package dao;

import connectDB.ConnectDB;
import entities.NhanVien;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

public class NhanVienDAO {

    public NhanVienDAO() {
    }

    public ArrayList<NhanVien> getAllTblNhanVien() {
        ArrayList<NhanVien> dsNhanVien = new ArrayList<>();
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM NhanVien";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String maNV = rs.getString("maNV");
                String hoTen = rs.getString("hoTen");
                LocalDate ngaySinh = rs.getDate("ngaySinh").toLocalDate();
                boolean gioiTinh = rs.getBoolean("gioiTinh");
                String sdt = rs.getString("sdt");
                String email = rs.getString("email");
                String diaChi = rs.getString("diaChi");
                String matKhau = rs.getString("matKhau");
                boolean vaiTro = rs.getBoolean("vaiTro");
                boolean trangThai = rs.getBoolean("trangThai");

                NhanVien nv = new NhanVien(maNV, hoTen, ngaySinh, gioiTinh, sdt, email, diaChi, matKhau, vaiTro, trangThai);
                dsNhanVien.add(nv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsNhanVien;
    }

    public boolean insert(NhanVien nv) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "INSERT INTO NhanVien (maNV, hoTen, ngaySinh, gioiTinh, sdt, email, diaChi, matKhau, vaiTro, trangThai) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, nv.getMaNV());
            stmt.setString(2, nv.getHoTen());
            stmt.setDate(3, Date.valueOf(nv.getNgaySinh()));
            stmt.setBoolean(4, nv.isGioiTinh());
            stmt.setString(5, nv.getSdt());
            stmt.setString(6, nv.getEmail());
            stmt.setString(7, nv.getDiaChi());
            stmt.setString(8, nv.getMatKhau());
            stmt.setBoolean(9, nv.isVaiTro());
            stmt.setBoolean(10, nv.isTrangThai());

            n = stmt.executeUpdate();
        } catch (SQLException e) {
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

    public boolean update(NhanVien nv) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "UPDATE NhanVien SET hoTen=?, ngaySinh=?, gioiTinh=?, sdt=?, email=?, diaChi=?, vaiTro=?, trangThai=? WHERE maNV=?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, nv.getHoTen());
            stmt.setDate(2, Date.valueOf(nv.getNgaySinh()));
            stmt.setBoolean(3, nv.isGioiTinh());
            stmt.setString(4, nv.getSdt());
            stmt.setString(5, nv.getEmail());
            stmt.setString(6, nv.getDiaChi());
            stmt.setBoolean(7, nv.isVaiTro());
            stmt.setBoolean(8, nv.isTrangThai());
            stmt.setString(9, nv.getMaNV());

            n = stmt.executeUpdate();
        } catch (SQLException e) {
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

    public boolean delete(String maNV) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {

            String sql = "UPDATE NhanVien SET trangThai = 0 WHERE maNV = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, maNV);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
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

    public boolean resetPassword(String maNV, String matKhauMoi) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "UPDATE NhanVien SET matKhau = ? WHERE maNV = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, matKhauMoi);
            stmt.setString(2, maNV);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
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

    public NhanVien getNhanVienByID(String id) {
        NhanVien nv = null;
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM NhanVien WHERE maNV = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String maNV = rs.getString("maNV");
                String hoTen = rs.getString("hoTen");
                LocalDate ngaySinh = rs.getDate("ngaySinh").toLocalDate();
                boolean gioiTinh = rs.getBoolean("gioiTinh");
                String sdt = rs.getString("sdt");
                String email = rs.getString("email");
                String diaChi = rs.getString("diaChi");
                String matKhau = rs.getString("matKhau");
                boolean vaiTro = rs.getBoolean("vaiTro");
                boolean trangThai = rs.getBoolean("trangThai");
                nv = new NhanVien(maNV, hoTen, ngaySinh, gioiTinh, sdt, email, diaChi, matKhau, vaiTro, trangThai);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nv;
    }

    public NhanVien checkLogin(String maNV, String matKhau) {
        NhanVien nv = null;

        String sql = "SELECT * FROM NhanVien WHERE maNV = ? AND matKhau = ? AND trangThai = 1";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maNV);
            stmt.setString(2, matKhau);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hoTen = rs.getString("hoTen");
                LocalDate ngaySinh = rs.getDate("ngaySinh").toLocalDate();
                boolean gioiTinh = rs.getBoolean("gioiTinh");
                String sdt = rs.getString("sdt");
                String email = rs.getString("email");
                String diaChi = rs.getString("diaChi");
                boolean vaiTro = rs.getBoolean("vaiTro");
                boolean trangThai = rs.getBoolean("trangThai");

                nv = new NhanVien(maNV, hoTen, ngaySinh, gioiTinh, sdt, email, diaChi, matKhau, vaiTro, trangThai);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nv;
    }
}
