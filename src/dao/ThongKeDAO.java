package dao;

import connectDB.ConnectDB;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    /**
     * Lấy báo cáo doanh thu chi tiết theo từng ngày trong khoảng thời gian
     * @param tuNgay Ngày bắt đầu
     * @param denNgay Ngày kết thúc
     * @return List các Map chứa thông tin: ngay, soDon, doanhThu, giaVon, loiNhuan
     */
    public List<Map<String, Object>> getBaoCaoDoanhThuChiTiet(LocalDate tuNgay, LocalDate denNgay) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT CAST(h.ngayTao AS DATE) as ngay, " +
                     "COUNT(DISTINCT h.maHD) as soDon, " +
                     "COALESCE(SUM(h.tongTien - h.giamGia), 0) as doanhThu, " +
                     "COALESCE(SUM(ct.soLuong * ct.donGia * 0.7), 0) as giaVon " +  // Ước tính giá vốn ~ 70% giá bán
                     "FROM HoaDon h " +
                     "LEFT JOIN ChiTietHoaDon ct ON h.maHD = ct.maHD " +
                     "WHERE CAST(h.ngayTao AS DATE) BETWEEN ? AND ? " +
                     "GROUP BY CAST(h.ngayTao AS DATE) " +
                     "ORDER BY ngay DESC";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(tuNgay));
            stmt.setDate(2, Date.valueOf(denNgay));
            ResultSet rs = stmt.executeQuery();

            double doanhThuTruoc = 0;
            List<Map<String, Object>> tempList = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("ngay", rs.getDate("ngay").toLocalDate());
                row.put("soDon", rs.getInt("soDon"));
                row.put("doanhThu", rs.getDouble("doanhThu"));
                row.put("giaVon", rs.getDouble("giaVon"));
                row.put("loiNhuan", rs.getDouble("doanhThu") - rs.getDouble("giaVon"));
                tempList.add(row);
            }

            // Tính tăng trưởng so với ngày trước
            for (int i = 0; i < tempList.size(); i++) {
                Map<String, Object> row = tempList.get(i);
                double doanhThu = (double) row.get("doanhThu");

                if (i < tempList.size() - 1) {
                    double doanhThuNgayTruoc = (double) tempList.get(i + 1).get("doanhThu");
                    if (doanhThuNgayTruoc > 0) {
                        double tangTruong = ((doanhThu - doanhThuNgayTruoc) / doanhThuNgayTruoc) * 100;
                        row.put("tangTruong", tangTruong);
                    } else {
                        row.put("tangTruong", doanhThu > 0 ? 100.0 : 0.0);
                    }
                } else {
                    row.put("tangTruong", 0.0);
                }
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Lấy tổng hợp báo cáo doanh thu trong khoảng thời gian
     * @param tuNgay Ngày bắt đầu
     * @param denNgay Ngày kết thúc
     * @return Map chứa: tongDoanhThu, tongLoiNhuan, tongDonHang
     */
    public Map<String, Object> getTongHopDoanhThu(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> result = new HashMap<>();
        String sql = "SELECT COUNT(DISTINCT h.maHD) as tongDon, " +
                     "COALESCE(SUM(h.tongTien - h.giamGia), 0) as tongDoanhThu, " +
                     "COALESCE(SUM(ct.soLuong * ct.donGia * 0.7), 0) as tongGiaVon " +
                     "FROM HoaDon h " +
                     "LEFT JOIN ChiTietHoaDon ct ON h.maHD = ct.maHD " +
                     "WHERE CAST(h.ngayTao AS DATE) BETWEEN ? AND ?";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(tuNgay));
            stmt.setDate(2, Date.valueOf(denNgay));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                result.put("tongDonHang", rs.getInt("tongDon"));
                result.put("tongDoanhThu", rs.getDouble("tongDoanhThu"));
                result.put("tongGiaVon", rs.getDouble("tongGiaVon"));
                result.put("tongLoiNhuan", rs.getDouble("tongDoanhThu") - rs.getDouble("tongGiaVon"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
