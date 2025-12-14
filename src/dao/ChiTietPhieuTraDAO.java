package dao;

import connectDB.ConnectDB;
import entities.ChiTietPhieuTra;
import entities.LoThuoc;
import entities.PhieuTraHang;
import entities.Thuoc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChiTietPhieuTraDAO {

    public ChiTietPhieuTraDAO() {
    }

    public boolean insert(ChiTietPhieuTra ct) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "INSERT INTO ChiTietPhieuTra (maPhieuTra, maThuoc, maLo, soLuongTra, donGiaTra, thanhTienHoanTra, donViTinh) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, ct.getPhieuTra().getMaPT());
            stmt.setString(2, ct.getThuoc().getMaThuoc());
            stmt.setString(3, ct.getLoThuoc().getMaLo());
            stmt.setInt(4, ct.getSoLuongTra());
            stmt.setDouble(5, ct.getDonGiaTra());
            stmt.setDouble(6, ct.getThanhTienHoanTra());
            stmt.setString(7, ct.getDonViTinh());

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

    public List<ChiTietPhieuTra> getChiTietByMaPhieu(String maPhieu) {
        List<ChiTietPhieuTra> listCT = new ArrayList<>();
        String sql = "SELECT ct.*, t.tenThuoc FROM ChiTietPhieuTra ct "
                + "JOIN Thuoc t ON ct.maThuoc = t.maThuoc "
                + "WHERE ct.maPhieuTra = ?";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maPhieu);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PhieuTraHang pt = new PhieuTraHang(maPhieu, null, 0, null, null, null, null);
                Thuoc thuoc = new Thuoc(rs.getString("maThuoc"));
                thuoc.setTenThuoc(rs.getString("tenThuoc"));
                LoThuoc lo = new LoThuoc(rs.getString("maLo"), null, null, null, 0, "", false);

                ChiTietPhieuTra ct = new ChiTietPhieuTra(
                        pt,
                        thuoc,
                        lo,
                        rs.getInt("soLuongTra"),
                        rs.getDouble("donGiaTra"),
                        rs.getDouble("thanhTienHoanTra"),
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
