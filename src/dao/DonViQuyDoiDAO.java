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

        // Sử dụng UNION ALL để lấy cả Đơn vị cơ bản (trong bảng Thuoc) và Đơn vị quy đổi
        String sql = "SELECT 0 as id, t.maThuoc, t.donViCoBan as tenDonVi, 1 as giaTriQuyDoi, "
                + "       ISNULL((SELECT TOP 1 ctbg.giaBan FROM ChiTietBangGia ctbg "
                + "               JOIN BangGia bg ON ctbg.maBG = bg.maBG "
                + "               WHERE ctbg.maThuoc = t.maThuoc AND ctbg.donViTinh = t.donViCoBan AND bg.trangThai = 1), 0) as giaBanHienTai, "
                + "       CAST(1 as BIT) as trangThai "
                + "FROM Thuoc t WHERE t.maThuoc = ? "
                + "UNION ALL "
                + "SELECT dv.id, dv.maThuoc, dv.tenDonVi, dv.giaTriQuyDoi, "
                + "       ISNULL((SELECT TOP 1 ctbg.giaBan FROM ChiTietBangGia ctbg "
                + "               JOIN BangGia bg ON ctbg.maBG = bg.maBG "
                + "               WHERE ctbg.maThuoc = dv.maThuoc AND ctbg.donViTinh = dv.tenDonVi AND bg.trangThai = 1), 0) as giaBanHienTai, "
                + "       dv.trangThai "
                + "FROM DonViQuyDoi dv WHERE dv.maThuoc = ? AND dv.trangThai = 1 "
                + "ORDER BY giaTriQuyDoi ASC";

        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maThuoc); // Tham số cho vế 1 (Bảng Thuoc)
            stmt.setString(2, maThuoc); // Tham số cho vế 2 (Bảng DonViQuyDoi)

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                DonViQuyDoi dv = new DonViQuyDoi(
                        rs.getInt("id"),
                        rs.getString("maThuoc"),
                        rs.getString("tenDonVi"),
                        rs.getInt("giaTriQuyDoi"),
                        rs.getDouble("giaBanHienTai"),
                        rs.getBoolean("trangThai")
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
        String sql = "SELECT dv.*, "
                + "ISNULL((SELECT TOP 1 ctbg.giaBan "
                + "        FROM ChiTietBangGia ctbg "
                + "        JOIN BangGia bg ON ctbg.maBG = bg.maBG "
                + "        WHERE ctbg.maThuoc = dv.maThuoc "
                + "          AND ctbg.donViTinh = dv.tenDonVi "
                + "          AND bg.trangThai = 1), 0) as giaBanHienTai "
                + "FROM DonViQuyDoi dv "
                + "WHERE dv.maThuoc = ? AND dv.tenDonVi = ?";
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
                        rs.getBoolean("trangThai")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dv;
    }

    public boolean insert(DonViQuyDoi dv) {
        String sql = "INSERT INTO DonViQuyDoi (maThuoc, tenDonVi, giaTriQuyDoi, trangThai) VALUES (?, ?, ?, ?)";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, dv.getMaThuoc());
            stmt.setString(2, dv.getTenDonVi());
            stmt.setInt(3, dv.getGiaTriQuyDoi());
            stmt.setBoolean(4, true);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "UPDATE DonViQuyDoi SET trangThai = 0 WHERE id = ?";
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
        String sql = "SELECT COUNT(*) FROM DonViQuyDoi WHERE maThuoc = ? AND tenDonVi = ? AND trangThai = 1";
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
}
