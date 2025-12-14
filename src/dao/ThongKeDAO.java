package dao;

import connectDB.ConnectDB;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ThongKeDAO {

    public ThongKeDAO() {
    }

    public double getDoanhThuHomNay() {
        double doanhThu = 0;
        String sql = "SELECT COALESCE(SUM(tongTien - giamGia), 0) as doanhThu "
                + "FROM HoaDon "
                + "WHERE CAST(ngayTao AS DATE) = CAST(GETDATE() AS DATE)";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                doanhThu = rs.getDouble("doanhThu");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doanhThu;
    }

    public double getDoanhThuTuanNay() {
        double doanhThu = 0;
        String sql = "SELECT COALESCE(SUM(tongTien - giamGia), 0) as doanhThu "
                + "FROM HoaDon "
                + "WHERE DATEPART(WEEK, ngayTao) = DATEPART(WEEK, GETDATE()) "
                + "AND YEAR(ngayTao) = YEAR(GETDATE())";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                doanhThu = rs.getDouble("doanhThu");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doanhThu;
    }

    public double getDoanhThuThangNay() {
        double doanhThu = 0;
        String sql = "SELECT COALESCE(SUM(tongTien - giamGia), 0) as doanhThu "
                + "FROM HoaDon "
                + "WHERE MONTH(ngayTao) = MONTH(GETDATE()) "
                + "AND YEAR(ngayTao) = YEAR(GETDATE())";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                doanhThu = rs.getDouble("doanhThu");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doanhThu;
    }

    public double getDoanhThuTheoKhoangThoiGian(LocalDate tuNgay, LocalDate denNgay) {
        double doanhThu = 0;
        String sql = "SELECT COALESCE(SUM(tongTien - giamGia), 0) as doanhThu "
                + "FROM HoaDon "
                + "WHERE CAST(ngayTao AS DATE) BETWEEN ? AND ?";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(tuNgay));
            stmt.setDate(2, Date.valueOf(denNgay));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                doanhThu = rs.getDouble("doanhThu");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doanhThu;
    }

    public Map<String, Integer> getTopThuocBanChay(int topN) {
        Map<String, Integer> topThuoc = new HashMap<>();
        String sql = "SELECT TOP " + topN + " t.tenThuoc, SUM(ct.soLuong) as tongSL "
                + "FROM ChiTietHoaDon ct "
                + "JOIN Thuoc t ON ct.maThuoc = t.maThuoc "
                + "JOIN HoaDon h ON ct.maHD = h.maHD "
                + "WHERE MONTH(h.ngayTao) = MONTH(GETDATE()) "
                + "AND YEAR(h.ngayTao) = YEAR(GETDATE()) "
                + "GROUP BY t.tenThuoc "
                + "ORDER BY tongSL DESC";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                topThuoc.put(rs.getString("tenThuoc"), rs.getInt("tongSL"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topThuoc;
    }

    public int getSoHoaDonHomNay() {
        int soHD = 0;
        String sql = "SELECT COUNT(*) as soHD "
                + "FROM HoaDon "
                + "WHERE CAST(ngayTao AS DATE) = CAST(GETDATE() AS DATE)";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                soHD = rs.getInt("soHD");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return soHD;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            
            // Call stored procedure if exists
            String sql = "{CALL sp_GetDashboardStats()}";
            CallableStatement stmt = con.prepareCall(sql);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                stats.put("doanhThuHomNay", rs.getDouble("DoanhThuHomNay"));
                stats.put("soHoaDonHomNay", rs.getInt("SoHoaDonHomNay"));
                stats.put("soKhachHang", rs.getInt("SoKhachHang"));
                stats.put("tongThuoc", rs.getInt("TongThuoc"));
            }
        } catch (SQLException e) {
            // If stored procedure doesn't exist, use individual methods
            stats.put("doanhThuHomNay", getDoanhThuHomNay());
            stats.put("soHoaDonHomNay", getSoHoaDonHomNay());
        }
        return stats;
    }
}
