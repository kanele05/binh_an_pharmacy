package dao;

import connectDB.ConnectDB;
import entities.ChiTietHoaDon;
import entities.HoaDon;
import entities.KhachHang;
import entities.NhanVien;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    private ChiTietHoaDonDAO cthdDAO = new ChiTietHoaDonDAO();

    public ArrayList<HoaDon> getAllHoaDon() {
        ArrayList<HoaDon> list = new ArrayList<>();
        String sql = "SELECT hd.*, nv.hoTen as tenNV, kh.tenKH "
                + "FROM HoaDon hd "
                + "JOIN NhanVien nv ON hd.maNV = nv.maNV "
                + "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH "
                + "ORDER BY hd.ngayTao DESC";
        try {
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setHoTen(rs.getString("tenNV"));

                KhachHang kh = null;
                if (rs.getString("maKH") != null) {
                    kh = new KhachHang();
                    kh.setMaKH(rs.getString("maKH"));
                    kh.setTenKH(rs.getString("tenKH"));
                } else {

                    kh = new KhachHang();
                    kh.setTenKH("Khách lẻ");
                }

                HoaDon hd = new HoaDon(
                        rs.getString("maHD"),
                        rs.getTimestamp("ngayTao").toLocalDateTime(),
                        rs.getDouble("tongTien"),
                        rs.getDouble("giamGia"),
                        rs.getDouble("thue"),
                        rs.getString("hinhThucTT"),
                        rs.getString("ghiChu"),
                        nv, kh
                );
                list.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean taoHoaDon(HoaDon hd, List<ChiTietHoaDon> listCTHD) {
        Connection con = null;
        PreparedStatement stmtHD = null;
        PreparedStatement stmtKho = null;

        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            String sqlHD = "INSERT INTO HoaDon (maHD, ngayTao, tongTien, giamGia, thue, hinhThucTT, ghiChu, maNV, maKH) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmtHD = con.prepareStatement(sqlHD);
            stmtHD.setString(1, hd.getMaHD());
            stmtHD.setTimestamp(2, Timestamp.valueOf(hd.getNgayTao()));
            stmtHD.setDouble(3, hd.getTongTien());
            stmtHD.setDouble(4, hd.getGiamGia());
            stmtHD.setDouble(5, hd.getThue());
            stmtHD.setString(6, hd.getHinhThucTT());
            stmtHD.setString(7, hd.getGhiChu());
            stmtHD.setString(8, hd.getNhanVien().getMaNV());

            if (hd.getKhachHang() != null) {
                stmtHD.setString(9, hd.getKhachHang().getMaKH());
            } else {
                stmtHD.setNull(9, java.sql.Types.NVARCHAR);
            }
            stmtHD.executeUpdate();

            String sqlUpdateKho = "UPDATE LoThuoc SET soLuongTon = soLuongTon - ? WHERE maLo = ?";
            stmtKho = con.prepareStatement(sqlUpdateKho);

            for (ChiTietHoaDon ct : listCTHD) {

                cthdDAO.insert(con, ct);

                stmtKho.setInt(1, ct.getSoLuong());
                stmtKho.setString(2, ct.getLoThuoc().getMaLo());
                stmtKho.executeUpdate();
            }

            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
                if (stmtHD != null) {
                    stmtHD.close();
                }
                if (stmtKho != null) {
                    stmtKho.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public String getMaxMaHD() {
        String maxID = null;
        String sql = "SELECT TOP 1 maHD FROM HoaDon ORDER BY maHD DESC"; // Lấy mã lớn nhất
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                maxID = rs.getString("maHD");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maxID;
    }
}
