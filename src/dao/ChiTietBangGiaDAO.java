package dao;

import connectDB.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietBangGiaDAO {

    public List<Object[]> getChiTietBangGiaHienTai() {
        List<Object[]> list = new ArrayList<>();
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();

            String sql = "SELECT t.maThuoc, t.tenThuoc, nt.tenNhom, t.donViTinh, "
                    + "ISNULL((SELECT TOP 1 ctpn.donGia "
                    + "FROM ChiTietPhieuNhap ctpn "
                    + "JOIN PhieuNhap pn ON ctpn.maPN = pn.maPN "
                    + "WHERE ctpn.maThuoc = t.maThuoc "
                    + "ORDER BY pn.ngayTao DESC), 0) as giaNhap, "
                    + "ISNULL((SELECT TOP 1 giaBan "
                    + "FROM ChiTietBangGia ct "
                    + "JOIN BangGia bg ON ct.maBG = bg.maBG "
                    + "WHERE ct.maThuoc = t.maThuoc AND bg.trangThai = 1), 0) as giaBan "
                    + "FROM Thuoc t "
                    + "LEFT JOIN NhomThuoc nt ON t.maNhom = nt.maNhom "
                    + "WHERE t.trangThai = 1";

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(readRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Object[]> getChiTietByMaBG(String maBG) {
        List<Object[]> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();

            String sql = "SELECT t.maThuoc, t.tenThuoc, nt.tenNhom, t.donViTinh, "
                    + "ISNULL((SELECT TOP 1 ctpn.donGia "
                    + "FROM ChiTietPhieuNhap ctpn "
                    + "JOIN PhieuNhap pn ON ctpn.maPN = pn.maPN "
                    + "WHERE ctpn.maThuoc = t.maThuoc "
                    + "ORDER BY pn.ngayTao DESC), 0) as giaNhap, "
                    + "ct.giaBan "
                    + "FROM ChiTietBangGia ct "
                    + "JOIN Thuoc t ON ct.maThuoc = t.maThuoc "
                    + "LEFT JOIN NhomThuoc nt ON t.maNhom = nt.maNhom "
                    + "WHERE ct.maBG = ?";

            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maBG);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(readRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Object[] readRow(ResultSet rs) throws SQLException {
        return new Object[]{
            rs.getString("maThuoc"),
            rs.getString("tenThuoc"),
            rs.getString("tenNhom"),
            rs.getString("donViTinh"),
            rs.getDouble("giaNhap"),
            rs.getDouble("giaBan")
        };
    }
}
