package dao;

import connectDB.ConnectDB;
import entities.NhomThuoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class NhomThuocDAO {

    // ===============================
    // Lấy nhóm thuốc theo tên
    // ===============================
    public NhomThuoc getByTen(String tenNhom) {
        String sql = "SELECT * FROM NhomThuoc WHERE tenNhom = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tenNhom);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new NhomThuoc(
                        rs.getString("maNhom"),
                        rs.getString("tenNhom")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean insert(NhomThuoc nhom) {
        String sql = "INSERT INTO NhomThuoc(maNhom, tenNhom) VALUES (?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nhom.getMaNhom());
            ps.setString(2, nhom.getTenNhom());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public String getMaNhomByTen(String tenNhom) {
        String sql = "SELECT maNhom FROM NhomThuoc WHERE tenNhom = ?";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, tenNhom.trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("maNhom");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String generateMaNhom() {
        String sql = "SELECT MAX(maNhom) FROM NhomThuoc";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String max = rs.getString(1);
                if (max != null) {
                    int num = Integer.parseInt(max.substring(2));
                    return String.format("NT%03d", num + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "NT001";
    }
}
