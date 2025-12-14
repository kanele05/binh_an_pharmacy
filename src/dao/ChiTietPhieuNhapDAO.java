package dao;

import connectDB.ConnectDB;
import entities.ChiTietPhieuNhap;
import entities.LoThuoc;
import entities.PhieuNhap;
import entities.Thuoc;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChiTietPhieuNhapDAO {

    public ChiTietPhieuNhapDAO() {
    }

    public boolean insert(ChiTietPhieuNhap ct) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "INSERT INTO ChiTietPhieuNhap (maPhieuNhap, maThuoc, maLo, hanSuDung, soLuong, donGiaNhap, thanhTien, donViTinh) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, ct.getPn().getMaPN());
            stmt.setString(2, ct.getThuoc().getMaThuoc());
            stmt.setString(3, ct.getLoThuoc() != null ? ct.getLoThuoc().getMaLo() : null);
            stmt.setDate(4, Date.valueOf(ct.getHanSuDung()));
            stmt.setInt(5, ct.getSoLuong());
            stmt.setDouble(6, ct.getDonGia());
            stmt.setDouble(7, ct.getThanhTien());
            stmt.setString(8, ct.getDonViTinh());

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

    public List<ChiTietPhieuNhap> getChiTietByMaPhieu(String maPhieu) {
        List<ChiTietPhieuNhap> listCT = new ArrayList<>();
        String sql = "SELECT ct.*, t.tenThuoc FROM ChiTietPhieuNhap ct "
                + "JOIN Thuoc t ON ct.maThuoc = t.maThuoc "
                + "WHERE ct.maPhieuNhap = ?";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maPhieu);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PhieuNhap pn = new PhieuNhap(maPhieu, null, 0, null, null, null);
                Thuoc thuoc = new Thuoc(rs.getString("maThuoc"));
                thuoc.setTenThuoc(rs.getString("tenThuoc"));
                LoThuoc lo = new LoThuoc(rs.getString("maLo"), null, null, null, 0, "", false);

                ChiTietPhieuNhap ct = new ChiTietPhieuNhap(
                        pn,
                        thuoc,
                        lo,
                        rs.getDate("hanSuDung").toLocalDate(),
                        rs.getInt("soLuong"),
                        rs.getDouble("donGiaNhap"),
                        rs.getDouble("thanhTien"),
                        rs.getString("donViTinh")
                );
                listCT.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listCT;
    }
}
