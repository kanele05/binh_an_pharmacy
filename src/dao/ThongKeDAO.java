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
     * Đã trừ tiền hoàn trả từ phiếu trả hàng và phiếu đổi hàng
     * @param tuNgay Ngày bắt đầu
     * @param denNgay Ngày kết thúc
     * @return List các Map chứa thông tin: ngay, soDon, doanhThu, tienHoanTra, giaVon, loiNhuan
     */
    public List<Map<String, Object>> getBaoCaoDoanhThuChiTiet(LocalDate tuNgay, LocalDate denNgay) {
        List<Map<String, Object>> result = new ArrayList<>();

        // Query lấy doanh thu theo ngày với giá vốn từ giá nhập lô gần nhất (có cùng đơn vị tính)
        // Sử dụng CROSS APPLY để lấy giá nhập gần nhất cho mỗi chi tiết hóa đơn
        String sqlDoanhThu = "SELECT CAST(h.ngayTao AS DATE) as ngay, " +
                     "COUNT(DISTINCT h.maHD) as soDon, " +
                     "COALESCE(SUM(h.tongTien - h.giamGia), 0) as doanhThu, " +
                     "COALESCE(SUM(ct.soLuong * ISNULL(giaNhap.donGia, ct.donGia * 0.7)), 0) as giaVon " +
                     "FROM HoaDon h " +
                     "LEFT JOIN ChiTietHoaDon ct ON h.maHD = ct.maHD " +
                     "OUTER APPLY ( " +
                     "    SELECT TOP 1 ctn.donGia " +
                     "    FROM ChiTietPhieuNhap ctn " +
                     "    JOIN PhieuNhap pn ON ctn.maPN = pn.maPN " +
                     "    WHERE ctn.maThuoc = ct.maThuoc " +
                     "    AND ctn.donViTinh = ct.donViTinh " +
                     "    AND pn.trangThai = N'Đã nhập' " +
                     "    AND pn.ngayTao <= h.ngayTao " +
                     "    ORDER BY pn.ngayTao DESC " +
                     ") giaNhap " +
                     "WHERE CAST(h.ngayTao AS DATE) BETWEEN ? AND ? " +
                     "GROUP BY CAST(h.ngayTao AS DATE) " +
                     "ORDER BY ngay DESC";

        // Query lấy tiền hoàn trả từ phiếu trả hàng theo ngày
        String sqlHoanTra = "SELECT CAST(ngayTra AS DATE) as ngay, " +
                     "COALESCE(SUM(tongTienHoanTra), 0) as tienHoanTra " +
                     "FROM PhieuTraHang " +
                     "WHERE CAST(ngayTra AS DATE) BETWEEN ? AND ? " +
                     "AND trangThai = N'Đã xử lý' OR trangThai = N'Đổi hàng' " +
                     "GROUP BY CAST(ngayTra AS DATE)";

        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();

            // Lấy doanh thu theo ngày
            Map<LocalDate, Map<String, Object>> dataMap = new HashMap<>();
            PreparedStatement stmt1 = con.prepareStatement(sqlDoanhThu);
            stmt1.setDate(1, Date.valueOf(tuNgay));
            stmt1.setDate(2, Date.valueOf(denNgay));
            ResultSet rs1 = stmt1.executeQuery();

            while (rs1.next()) {
                Map<String, Object> row = new HashMap<>();
                LocalDate ngay = rs1.getDate("ngay").toLocalDate();
                row.put("ngay", ngay);
                row.put("soDon", rs1.getInt("soDon"));
                row.put("doanhThu", rs1.getDouble("doanhThu"));
                row.put("giaVon", rs1.getDouble("giaVon"));
                row.put("tienHoanTra", 0.0);
                dataMap.put(ngay, row);
            }

            // Lấy tiền hoàn trả theo ngày và trừ vào doanh thu
            PreparedStatement stmt2 = con.prepareStatement(sqlHoanTra);
            stmt2.setDate(1, Date.valueOf(tuNgay));
            stmt2.setDate(2, Date.valueOf(denNgay));
            ResultSet rs2 = stmt2.executeQuery();

            while (rs2.next()) {
                LocalDate ngay = rs2.getDate("ngay").toLocalDate();
                double tienHoanTra = rs2.getDouble("tienHoanTra");

                if (dataMap.containsKey(ngay)) {
                    dataMap.get(ngay).put("tienHoanTra", tienHoanTra);
                } else {
                    // Ngày chỉ có hoàn trả mà không có bán hàng
                    Map<String, Object> row = new HashMap<>();
                    row.put("ngay", ngay);
                    row.put("soDon", 0);
                    row.put("doanhThu", 0.0);
                    row.put("giaVon", 0.0);
                    row.put("tienHoanTra", tienHoanTra);
                    dataMap.put(ngay, row);
                }
            }

            // Sắp xếp theo ngày giảm dần và tính lợi nhuận
            List<Map<String, Object>> tempList = new ArrayList<>(dataMap.values());
            tempList.sort((a, b) -> ((LocalDate) b.get("ngay")).compareTo((LocalDate) a.get("ngay")));

            // Tính lợi nhuận = doanhThu - tienHoanTra - giaVon
            for (Map<String, Object> row : tempList) {
                double doanhThu = (double) row.get("doanhThu");
                double tienHoanTra = (double) row.get("tienHoanTra");
                double giaVon = (double) row.get("giaVon");
                double doanhThuThuc = doanhThu - tienHoanTra;
                double loiNhuan = doanhThuThuc - giaVon;
                row.put("doanhThuThuc", doanhThuThuc);
                row.put("loiNhuan", loiNhuan);
            }

            // Tính tăng trưởng so với ngày trước
            for (int i = 0; i < tempList.size(); i++) {
                Map<String, Object> row = tempList.get(i);
                double doanhThuThuc = (double) row.get("doanhThuThuc");

                if (i < tempList.size() - 1) {
                    double doanhThuNgayTruoc = (double) tempList.get(i + 1).get("doanhThuThuc");
                    if (doanhThuNgayTruoc > 0) {
                        double tangTruong = ((doanhThuThuc - doanhThuNgayTruoc) / doanhThuNgayTruoc) * 100;
                        row.put("tangTruong", tangTruong);
                    } else {
                        row.put("tangTruong", doanhThuThuc > 0 ? 100.0 : 0.0);
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
     * Đã trừ tiền hoàn trả từ phiếu trả hàng
     * @param tuNgay Ngày bắt đầu
     * @param denNgay Ngày kết thúc
     * @return Map chứa: tongDoanhThu, tongTienHoanTra, tongDoanhThuThuc, tongLoiNhuan, tongDonHang
     */
    public Map<String, Object> getTongHopDoanhThu(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> result = new HashMap<>();

        // Sử dụng giá nhập từ lô gần nhất có cùng đơn vị tính
        String sqlDoanhThu = "SELECT COUNT(DISTINCT h.maHD) as tongDon, " +
                     "COALESCE(SUM(h.tongTien - h.giamGia), 0) as tongDoanhThu, " +
                     "COALESCE(SUM(ct.soLuong * ISNULL(giaNhap.donGia, ct.donGia * 0.7)), 0) as tongGiaVon " +
                     "FROM HoaDon h " +
                     "LEFT JOIN ChiTietHoaDon ct ON h.maHD = ct.maHD " +
                     "OUTER APPLY ( " +
                     "    SELECT TOP 1 ctn.donGia " +
                     "    FROM ChiTietPhieuNhap ctn " +
                     "    JOIN PhieuNhap pn ON ctn.maPN = pn.maPN " +
                     "    WHERE ctn.maThuoc = ct.maThuoc " +
                     "    AND ctn.donViTinh = ct.donViTinh " +
                     "    AND pn.trangThai = N'Đã nhập' " +
                     "    AND pn.ngayTao <= h.ngayTao " +
                     "    ORDER BY pn.ngayTao DESC " +
                     ") giaNhap " +
                     "WHERE CAST(h.ngayTao AS DATE) BETWEEN ? AND ?";

        String sqlHoanTra = "SELECT COALESCE(SUM(tongTienHoanTra), 0) as tongTienHoanTra " +
                     "FROM PhieuTraHang " +
                     "WHERE CAST(ngayTra AS DATE) BETWEEN ? AND ? " +
                     "AND trangThai = N'Đã xử lý' OR trangThai = N'Đổi hàng'";

        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();

            // Lấy tổng doanh thu
            PreparedStatement stmt1 = con.prepareStatement(sqlDoanhThu);
            stmt1.setDate(1, Date.valueOf(tuNgay));
            stmt1.setDate(2, Date.valueOf(denNgay));
            ResultSet rs1 = stmt1.executeQuery();

            double tongDoanhThu = 0;
            double tongGiaVon = 0;
            int tongDonHang = 0;

            if (rs1.next()) {
                tongDonHang = rs1.getInt("tongDon");
                tongDoanhThu = rs1.getDouble("tongDoanhThu");
                tongGiaVon = rs1.getDouble("tongGiaVon");
            }

            // Lấy tổng tiền hoàn trả
            PreparedStatement stmt2 = con.prepareStatement(sqlHoanTra);
            stmt2.setDate(1, Date.valueOf(tuNgay));
            stmt2.setDate(2, Date.valueOf(denNgay));
            ResultSet rs2 = stmt2.executeQuery();

            double tongTienHoanTra = 0;
            if (rs2.next()) {
                tongTienHoanTra = rs2.getDouble("tongTienHoanTra");
            }

            // Tính doanh thu thực và lợi nhuận
            double tongDoanhThuThuc = tongDoanhThu - tongTienHoanTra;
            double tongLoiNhuan = tongDoanhThuThuc - tongGiaVon;

            result.put("tongDonHang", tongDonHang);
            result.put("tongDoanhThu", tongDoanhThu);
            result.put("tongTienHoanTra", tongTienHoanTra);
            result.put("tongDoanhThuThuc", tongDoanhThuThuc);
            result.put("tongGiaVon", tongGiaVon);
            result.put("tongLoiNhuan", tongLoiNhuan);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Map<String, Object>> getDoanhThu7NgayGanNhat() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT CAST(ngayTao AS DATE) as ngay, "
                + "COALESCE(SUM(tongTien - giamGia), 0) as doanhThu "
                + "FROM HoaDon "
                + "WHERE CAST(ngayTao AS DATE) >= DATEADD(DAY, -6, CAST(GETDATE() AS DATE)) "
                + "GROUP BY CAST(ngayTao AS DATE) "
                + "ORDER BY ngay ASC";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("ngay", rs.getDate("ngay").toLocalDate());
                row.put("doanhThu", rs.getDouble("doanhThu"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Map<String, Object>> getTopThuocBanChayThang(int topN) {
        List<Map<String, Object>> result = new ArrayList<>();
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
                Map<String, Object> row = new HashMap<>();
                row.put("tenThuoc", rs.getString("tenThuoc"));
                row.put("soLuong", rs.getInt("tongSL"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
