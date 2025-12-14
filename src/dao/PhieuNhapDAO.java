package dao;

import connectDB.ConnectDB;
import entities.ChiTietPhieuNhap;
import entities.PhieuNhap;
import java.sql.*;
import java.util.ArrayList;

public class PhieuNhapDAO {

    // Hàm tạo mã phiếu nhập mới tự động (VD: PN001 -> PN002)
    public String generateNewMaPN() {
        String newMa = "PN001";
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT TOP 1 maPN FROM PhieuNhap ORDER BY maPN DESC";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                String lastMa = rs.getString(1);
                int number = Integer.parseInt(lastMa.substring(2)) + 1;
                newMa = String.format("PN%03d", number);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newMa;
    }

    // Hàm TRANSACTION quan trọng nhất
    public boolean createPhieuNhap(PhieuNhap pn, ArrayList<ChiTietPhieuNhap> listCT) {
        Connection con = null;
        PreparedStatement stmtPN = null;
        PreparedStatement stmtCT = null;
        CallableStatement cstmtLo = null; // Để gọi Stored Procedure
        boolean result = false;

        try {
            con = ConnectDB.getConnection();
            // 1. Tắt Auto Commit để bắt đầu Transaction
            con.setAutoCommit(false);

            // 2. Insert bảng PhieuNhap
            String sqlPN = "INSERT INTO PhieuNhap (maPN, ngayTao, tongTien, trangThai, maNV, maNCC, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmtPN = con.prepareStatement(sqlPN);
            stmtPN.setString(1, pn.getMaPN());
            stmtPN.setDate(2, java.sql.Date.valueOf(pn.getNgayTao())); // Lưu ý convert Date
            stmtPN.setDouble(3, pn.getTongTien());
            stmtPN.setString(4, "Đã nhập");
            stmtPN.setString(5, pn.getNhanVien().getMaNV());
            stmtPN.setString(6, pn.getNcc().getMaNCC());
            stmtPN.setString(7, "Nhập hàng từ hệ thống");
            stmtPN.executeUpdate();

            // 3. Insert ChiTietPhieuNhap & Cập nhật Lô (Gọi SP)
            String sqlCT = "INSERT INTO ChiTietPhieuNhap (maPN, maThuoc, maLo, hanSuDung, soLuong, donGia, thanhTien) VALUES (?, ?, ?, ?, ?, ?, ?)";
            String sqlSP = "{call sp_TimHoacTaoLoThuoc(?, ?, ?, ?)}"; // Input: MaThuoc, HSD, SL -> Output: MaLo
            
            stmtCT = con.prepareStatement(sqlCT);
            cstmtLo = con.prepareCall(sqlSP);

            for (ChiTietPhieuNhap ct : listCT) {
                // A. Gọi SP để lấy/tạo Mã Lô và cập nhật tồn kho
                cstmtLo.setString(1, ct.getThuoc().getMaThuoc());
                cstmtLo.setDate(2, java.sql.Date.valueOf(ct.getHanSuDung())); // java.util.Date -> java.sql.Date
                cstmtLo.setInt(3, ct.getSoLuong());
                cstmtLo.registerOutParameter(4, Types.VARCHAR); // Tham số OUTPUT @MaLo
                cstmtLo.execute();
                
                String maLo = cstmtLo.getString(4); // Lấy mã lô vừa được SP trả về
                
                // B. Insert Chi Tiết
                stmtCT.setString(1, pn.getMaPN());
                stmtCT.setString(2, ct.getThuoc().getMaThuoc());
                stmtCT.setString(3, maLo); // Dùng mã lô lấy từ SP
                stmtCT.setDate(4, java.sql.Date.valueOf(ct.getHanSuDung()));
                stmtCT.setInt(5, ct.getSoLuong());
                stmtCT.setDouble(6, ct.getDonGia());
                stmtCT.setDouble(7, ct.getThanhTien());
                stmtCT.executeUpdate();
            }

            // 4. Nếu chạy đến đây mà không lỗi -> Commit (Lưu thật)
            con.commit();
            result = true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (con != null) con.rollback(); // Có lỗi -> Hoàn tác mọi thứ
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            // Đóng kết nối và bật lại auto commit
            try {
                if (con != null) con.setAutoCommit(true);
                if (stmtPN != null) stmtPN.close();
                if (stmtCT != null) stmtCT.close();
                if (cstmtLo != null) cstmtLo.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}