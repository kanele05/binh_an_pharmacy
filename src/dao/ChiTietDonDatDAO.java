package dao;

import connectDB.ConnectDB;
import entities.ChiTietDonDat;
import entities.DonDatHang;
import entities.Thuoc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ChiTietDonDatDAO {

    public void insert(Connection con, ChiTietDonDat ct) throws SQLException {
        String sql = "INSERT INTO ChiTietDonDat (maDonDat, maThuoc, soLuong, donGia, thanhTien, donViTinh) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, ct.getDonDat().getMaDonDat());
            stmt.setString(2, ct.getThuoc().getMaThuoc());
            stmt.setInt(3, ct.getSoLuong());
            stmt.setDouble(4, ct.getDonGia());
            stmt.setDouble(5, ct.getThanhTien());
            stmt.setString(6, ct.getDonViTinh());
            stmt.executeUpdate();
        }
    }

    public ArrayList<ChiTietDonDat> getChiTietByMaDon(String maDon) {
        ArrayList<ChiTietDonDat> list = new ArrayList<>();
        String sql = "SELECT ct.*, t.tenThuoc, t.donViCoBan "
                + "FROM ChiTietDonDat ct "
                + "JOIN Thuoc t ON ct.maThuoc = t.maThuoc "
                + "WHERE ct.maDonDat = ?";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maDon);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                Thuoc t = new Thuoc(rs.getString("maThuoc"));
                t.setTenThuoc(rs.getString("tenThuoc"));
                t.setDonViTinh(rs.getString("donViCoBan"));
                DonDatHang don = new DonDatHang(rs.getString("maDonDat"), null, null, null, 0, null, null, null, null);

                ChiTietDonDat ct = new ChiTietDonDat(
                        don, t,
                        rs.getInt("soLuong"),
                        rs.getDouble("donGia"),
                        rs.getDouble("thanhTien"),
                        rs.getString("donViTinh")
                );
                list.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void deleteByMaDon(Connection con, String maDon) throws SQLException {
        String sql = "DELETE FROM ChiTietDonDat WHERE maDonDat = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maDon);
            stmt.executeUpdate();
        }
    }
}
