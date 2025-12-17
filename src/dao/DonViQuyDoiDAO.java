package dao;

import connectDB.ConnectDB;
import entities.DonViQuyDoi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DonViQuyDoiDAO {

    public ArrayList<DonViQuyDoi> getAllDonViByMaThuoc(String maThuoc) {
        ArrayList<DonViQuyDoi> list = new ArrayList<>();

        // Chỉ lấy từ bảng DonViQuyDoi (đã bao gồm đơn vị cơ bản với giaTriQuyDoi = 1)
        String sql = "SELECT dv.id, dv.maThuoc, dv.tenDonVi, dv. giaTriQuyDoi, dv.laDonViCoBan, "
                + "       ISNULL((SELECT TOP 1 ctbg. giaBan FROM ChiTietBangGia ctbg "
                + "               JOIN BangGia bg ON ctbg. maBG = bg.maBG "
                + "               WHERE ctbg.maThuoc = dv.maThuoc AND ctbg.donViTinh = dv.tenDonVi AND bg.trangThai = 1), dv.giaBan) as giaBanHienTai "
                + "FROM DonViQuyDoi dv "
                + "WHERE dv. maThuoc = ? "
                + "ORDER BY dv.giaTriQuyDoi ASC";

        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maThuoc);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                DonViQuyDoi dv = new DonViQuyDoi(
                        rs.getInt("id"),
                        rs.getString("maThuoc"),
                        rs.getString("tenDonVi"),
                        rs.getInt("giaTriQuyDoi"),
                        rs.getDouble("giaBanHienTai"),
                        rs.getBoolean("laDonViCoBan")
                );
                list.add(dv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public DonViQuyDoi getDonViByTen(String maThuoc, String tenDonVi) {
        DonViQuyDoi dv = null;
        String sql = "SELECT dv.id, dv.maThuoc, dv.tenDonVi, dv.giaTriQuyDoi, dv.laDonViCoBan, "
                + "ISNULL((SELECT TOP 1 ctbg.giaBan "
                + "        FROM ChiTietBangGia ctbg "
                + "        JOIN BangGia bg ON ctbg.maBG = bg.maBG "
                + "        WHERE ctbg.maThuoc = dv.maThuoc "
                + "          AND ctbg.donViTinh = dv.tenDonVi "
                + "          AND bg.trangThai = 1), dv.giaBan) as giaBanHienTai "
                + "FROM DonViQuyDoi dv "
                + "WHERE dv. maThuoc = ? AND dv.tenDonVi = ? ";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maThuoc);
            stmt.setString(2, tenDonVi);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                dv = new DonViQuyDoi(
                        rs.getInt("id"),
                        rs.getString("maThuoc"),
                        rs.getString("tenDonVi"),
                        rs.getInt("giaTriQuyDoi"),
                        rs.getDouble("giaBanHienTai"),
                        rs.getBoolean("laDonViCoBan")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dv;
    }

    public boolean insert(DonViQuyDoi dv) {
        String sql = "INSERT INTO DonViQuyDoi (maThuoc, tenDonVi, giaTriQuyDoi, giaBan, laDonViCoBan) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, dv.getMaThuoc());
            stmt.setString(2, dv.getTenDonVi());
            stmt.setInt(3, dv.getGiaTriQuyDoi());
            stmt.setDouble(4, dv.getGiaBan());
            stmt.setBoolean(5, dv.isLaDonViCoBan());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM DonViQuyDoi WHERE id = ?";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkExist(String maThuoc, String tenDonVi) {
        String sql = "SELECT COUNT(*) FROM DonViQuyDoi WHERE maThuoc = ? AND tenDonVi = ?";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maThuoc);
            stmt.setString(2, tenDonVi);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Object[]> getBangQuyDoiDayDu() {
        ArrayList<Object[]> list = new ArrayList<>();
        String sql = "SELECT t.maThuoc, t.tenThuoc, dv.tenDonVi, dv.giaTriQuyDoi, dv.giaBan "
                + "FROM DonViQuyDoi dv "
                + "JOIN Thuoc t ON dv.maThuoc = t.maThuoc "
                + "WHERE t.trangThai = 1 "
                + // Chỉ lấy thuốc đang kinh doanh
                "ORDER BY t.maThuoc ASC, dv.giaTriQuyDoi ASC";

        try {
            Connection con = ConnectDB.getConnection();
            java.sql.Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("maThuoc"),
                    rs.getString("tenThuoc"),
                    rs.getString("tenDonVi"),
                    rs.getInt("giaTriQuyDoi"),
                    rs.getDouble("giaBan")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
