package dao;

import connectDB.ConnectDB;
import entities.ChiTietHoaDon;
import entities.HoaDon;
import entities.LoThuoc;
import entities.Thuoc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChiTietHoaDonDAO {

    public ArrayList<ChiTietHoaDon> getChiTietByMaHD(String maHD) {
        ArrayList<ChiTietHoaDon> list = new ArrayList<>();
        String sql = "SELECT ct.*, t.tenThuoc "
                + "FROM ChiTietHoaDon ct "
                + "JOIN Thuoc t ON ct.maThuoc = t.maThuoc "
                + "WHERE ct.maHD = ?";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maHD);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                Thuoc thuoc = new Thuoc(rs.getString("maThuoc"));
                thuoc.setTenThuoc(rs.getString("tenThuoc"));

                LoThuoc lo = new LoThuoc(rs.getString("maLo"), null, null, null, 0, "", false);
                HoaDon hd = new HoaDon(rs.getString("maHD"), null, 0, 0, 0, "", "", null, null);

                ChiTietHoaDon ct = new ChiTietHoaDon(
                        hd, thuoc, lo,
                        rs.getInt("soLuong"),
                        rs.getDouble("donGia"),
                        rs.getDouble("thanhTien")
                );
                list.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insert(Connection con, ChiTietHoaDon ct) throws SQLException {
        String sql = "INSERT INTO ChiTietHoaDon (maHD, maThuoc, maLo, soLuong, donGia, thanhTien) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, ct.getHoaDon().getMaHD());
            stmt.setString(2, ct.getThuoc().getMaThuoc());
            stmt.setString(3, ct.getLoThuoc().getMaLo());
            stmt.setInt(4, ct.getSoLuong());
            stmt.setDouble(5, ct.getDonGia());
            stmt.setDouble(6, ct.getThanhTien());
            stmt.executeUpdate();
        }
    }
}
