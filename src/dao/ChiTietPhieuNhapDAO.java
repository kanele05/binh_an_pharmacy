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
            // Thêm cột donViTinh vào câu INSERT
            String sql = "INSERT INTO ChiTietPhieuNhap (maPN, maThuoc, maLo, hanSuDung, soLuong, donGia, thanhTien, donViTinh) "
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
        // Lấy đơn vị tính từ cột donViTinh, nếu null thì fallback về donViCoBan
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

                // Lấy đơn vị tính từ DB, fallback về đơn vị cơ bản nếu null
                String donViTinh = rs.getString("donViTinh");
                if (donViTinh == null || donViTinh.isEmpty()) {
                    donViTinh = rs.getString("donViCoBan");
                }

                ChiTietPhieuNhap ct = new ChiTietPhieuNhap(
                        pn,
                        thuoc,
                        lo,
                        rs.getDate("hanSuDung") != null ? rs.getDate("hanSuDung").toLocalDate() : null,
                        rs.getInt("soLuong"),
                        rs.getDouble("donGia"),
                        rs.getDouble("thanhTien"),
                        donViTinh
                );
                listCT.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listCT;
    }

    public double getGiaNhapCoBanMoiNhat(String maThuoc) {
        double giaCoBan = 0;

        String sql = "SELECT TOP 1 ctpn.donGia, dv.giaTriQuyDoi "
                + "FROM ChiTietPhieuNhap ctpn "
                + "JOIN PhieuNhap pn ON ctpn.maPN = pn.maPN "
                + "JOIN DonViQuyDoi dv ON ctpn.maThuoc = dv.maThuoc AND ctpn.donViTinh = dv.tenDonVi "
                + "WHERE ctpn.maThuoc = ? "
                + "ORDER BY pn.ngayTao DESC, pn.maPN DESC";

        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maThuoc);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double donGiaNhap = rs.getDouble("donGia");
                int giaTriQuyDoi = rs.getInt("giaTriQuyDoi");
                if (giaTriQuyDoi != 0) {
                    giaCoBan = donGiaNhap / giaTriQuyDoi;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return giaCoBan;
    }
}
