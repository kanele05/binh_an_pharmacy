package dao;

import connectDB.ConnectDB;
import entities.BangGia;
import entities.ChiTietBangGia;
import entities.Thuoc;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietBangGiaDAO {

    public ArrayList<ChiTietBangGia> getChiTietByMaBG(String maBG) {
        ArrayList<ChiTietBangGia> list = new ArrayList<>();

        String sql = "SELECT ct.*, t.tenThuoc "
                + "FROM ChiTietBangGia ct "
                + "JOIN Thuoc t ON ct.maThuoc = t.maThuoc "
                + "WHERE ct.maBG = ?";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maBG);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Thuoc t = new Thuoc(rs.getString("maThuoc"));
                t.setTenThuoc(rs.getString("tenThuoc"));

                ChiTietBangGia ct = new ChiTietBangGia(
                        new BangGia(rs.getString("maBG")),
                        t,
                        rs.getString("donViTinh"),
                        rs.getDouble("giaBan")
                );
                list.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(ChiTietBangGia ct) {
        String sql = "INSERT INTO ChiTietBangGia (maBG, maThuoc, donViTinh, giaBan) VALUES (?, ?, ?, ?)";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, ct.getBangGia().getMaBG());
            stmt.setString(2, ct.getThuoc().getMaThuoc());
            stmt.setString(3, ct.getDonViTinh());
            stmt.setDouble(4, ct.getGiaBan());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(ChiTietBangGia ct) {
        String sql = "UPDATE ChiTietBangGia SET giaBan = ? WHERE maBG = ? AND maThuoc = ? AND donViTinh = ?";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDouble(1, ct.getGiaBan());
            stmt.setString(2, ct.getBangGia().getMaBG());
            stmt.setString(3, ct.getThuoc().getMaThuoc());
            stmt.setString(4, ct.getDonViTinh());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maBG, String maThuoc, String donViTinh) {
        String sql = "DELETE FROM ChiTietBangGia WHERE maBG = ? AND maThuoc = ? AND donViTinh = ?";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maBG);
            stmt.setString(2, maThuoc);
            stmt.setString(3, donViTinh);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Object[]> getChiTietBangGiaHienTai() {
        List<Object[]> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();

            String sql = "SELECT t.maThuoc, t.tenThuoc, nt.tenNhom, ct.donViTinh, "
                    + "ISNULL((SELECT TOP 1 ctpn.donGia "
                    + "        FROM ChiTietPhieuNhap ctpn "
                    + "        JOIN PhieuNhap pn ON ctpn.maPN = pn.maPN "
                    + "        WHERE ctpn.maThuoc = t.maThuoc "
                    + "        ORDER BY pn.ngayTao DESC), 0) as giaNhap, "
                    + "ct.giaBan "
                    + "FROM ChiTietBangGia ct "
                    + "JOIN Thuoc t ON ct.maThuoc = t.maThuoc "
                    + "JOIN BangGia bg ON ct.maBG = bg.maBG "
                    + "LEFT JOIN NhomThuoc nt ON t.maNhom = nt.maNhom "
                    + "WHERE bg.trangThai = 1 AND t.trangThai = 1";

            PreparedStatement stmt = con.prepareStatement(sql);
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

    public List<Object[]> getChiTietFullByMaBG(String maBG) {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT t.maThuoc, t.tenThuoc, nt.tenNhom, ct.donViTinh, "
                + "ISNULL((SELECT TOP 1 ctpn.donGia "
                + "        FROM ChiTietPhieuNhap ctpn "
                + "        JOIN PhieuNhap pn ON ctpn.maPN = pn.maPN "
                + "        WHERE ctpn.maThuoc = t.maThuoc "
                + "        ORDER BY pn.ngayTao DESC), 0) as giaNhap, "
                + "ct.giaBan "
                + "FROM ChiTietBangGia ct "
                + "JOIN Thuoc t ON ct.maThuoc = t.maThuoc "
                + "LEFT JOIN NhomThuoc nt ON t.maNhom = nt.maNhom "
                + "WHERE ct.maBG = ?";
        try {
            Connection con = ConnectDB.getConnection();
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

    public double getGiaBanMoi(String maThuoc, String donViTinh) {
        double giaBan = 0;

        String sql = "SELECT TOP 1 ct.giaBan "
                + "FROM ChiTietBangGia ct "
                + "JOIN BangGia bg ON ct.maBG = bg.maBG "
                + "WHERE ct.maThuoc = ? "
                + "AND ct.donViTinh = ? "
                + "AND bg.trangThai = 1";

        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maThuoc);
            stmt.setString(2, donViTinh);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                giaBan = rs.getDouble("giaBan");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return giaBan;
    }
}
