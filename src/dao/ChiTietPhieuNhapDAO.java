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
            // Fixed: column names khớp với DB schema (maPN thay vì maPhieuNhap, donGia thay vì donGiaNhap)
            String sql = "INSERT INTO ChiTietPhieuNhap (maPN, maThuoc, maLo, hanSuDung, soLuong, donGia, thanhTien) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, ct.getPn().getMaPN());
            stmt.setString(2, ct.getThuoc().getMaThuoc());
            stmt.setString(3, ct.getLoThuoc() != null ? ct.getLoThuoc().getMaLo() : null);
            stmt.setDate(4, Date.valueOf(ct.getHanSuDung()));
            stmt.setInt(5, ct.getSoLuong());
            stmt.setDouble(6, ct.getDonGia());
            stmt.setDouble(7, ct.getThanhTien());

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
        // Fixed: column name maPN thay vì maPhieuNhap
        String sql = "SELECT ct.*, t.tenThuoc, t.donViCoBan FROM ChiTietPhieuNhap ct "
                + "JOIN Thuoc t ON ct.maThuoc = t.maThuoc "
                + "WHERE ct.maPN = ?";
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
                thuoc.setDonViTinh(rs.getString("donViCoBan"));
                LoThuoc lo = new LoThuoc(rs.getString("maLo"), null, null, null, 0, "", false);

                // Fixed: column name donGia thay vì donGiaNhap
                ChiTietPhieuNhap ct = new ChiTietPhieuNhap(
                        pn,
                        thuoc,
                        lo,
                        rs.getDate("hanSuDung") != null ? rs.getDate("hanSuDung").toLocalDate() : null,
                        rs.getInt("soLuong"),
                        rs.getDouble("donGia"),
                        rs.getDouble("thanhTien"),
                        rs.getString("donViCoBan")
                );
                listCT.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listCT;
    }
}
