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

            // Lấy giá nhập theo đúng đơn vị tính của từng dòng chi tiết bảng giá
            String sql = "SELECT t.maThuoc, t.tenThuoc, nt.tenNhom, ct.donViTinh, "
                    + "ISNULL((SELECT TOP 1 ctpn.donGia "
                    + "        FROM ChiTietPhieuNhap ctpn "
                    + "        JOIN PhieuNhap pn ON ctpn.maPN = pn.maPN "
                    + "        WHERE ctpn.maThuoc = t.maThuoc "
                    + "          AND ctpn.donViTinh = ct.donViTinh "
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

        // Lấy giá nhập theo đúng đơn vị tính của từng dòng chi tiết bảng giá
        String sql = "SELECT t.maThuoc, t.tenThuoc, nt.tenNhom, ct.donViTinh, "
                + "ISNULL((SELECT TOP 1 ctpn.donGia "
                + "        FROM ChiTietPhieuNhap ctpn "
                + "        JOIN PhieuNhap pn ON ctpn.maPN = pn.maPN "
                + "        WHERE ctpn.maThuoc = t.maThuoc "
                + "          AND ctpn.donViTinh = ct.donViTinh "
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

    /**
     * Cập nhật hoặc thêm mới giá nhập vào bảng DonViQuyDoi khi xác nhận nhập kho
     * Đồng thời tự động tính giá nhập cho đơn vị cơ bản = giá nhập đơn vị lớn / giá trị quy đổi
     * @param maThuoc Mã thuốc
     * @param donViTinh Đơn vị tính (đơn vị lớn từ phiếu nhập)
     * @param giaNhap Giá nhập của đơn vị lớn
     * @return true nếu thành công
     */
    public boolean capNhatGiaNhap(String maThuoc, String donViTinh, double giaNhap) {
        try {
            Connection con = ConnectDB.getConnection();

            // Lấy thông tin đơn vị cơ bản và giá trị quy đổi của đơn vị nhập
            String sqlGetInfo = "SELECT t.donViCoBan, " +
                    "ISNULL((SELECT giaTriQuyDoi FROM DonViQuyDoi WHERE maThuoc = ? AND tenDonVi = ?), 1) as giaTriQuyDoi " +
                    "FROM Thuoc t WHERE t.maThuoc = ?";
            PreparedStatement stmtInfo = con.prepareStatement(sqlGetInfo);
            stmtInfo.setString(1, maThuoc);
            stmtInfo.setString(2, donViTinh);
            stmtInfo.setString(3, maThuoc);
            ResultSet rsInfo = stmtInfo.executeQuery();

            String donViCoBan = donViTinh;
            int giaTriQuyDoi = 1;
            if (rsInfo.next()) {
                donViCoBan = rsInfo.getString("donViCoBan");
                giaTriQuyDoi = rsInfo.getInt("giaTriQuyDoi");
            }

            // Tính giá nhập cho đơn vị cơ bản = giá nhập đơn vị lớn / giá trị quy đổi
            double giaNhapDonViCoBan = giaNhap / giaTriQuyDoi;

            // Kiểm tra xem đơn vị nhập này đã tồn tại trong DonViQuyDoi chưa
            String sqlCheck = "SELECT id FROM DonViQuyDoi WHERE maThuoc = ? AND tenDonVi = ?";
            PreparedStatement stmtCheck = con.prepareStatement(sqlCheck);
            stmtCheck.setString(1, maThuoc);
            stmtCheck.setString(2, donViTinh);
            ResultSet rs = stmtCheck.executeQuery();

            if (!rs.next()) {
                // Chưa tồn tại - tạo mới đơn vị quy đổi với giá bán mặc định = giá nhập * 1.2 (20% lãi)
                boolean laDonViCoBan = donViTinh.equalsIgnoreCase(donViCoBan);

                // Thêm vào DonViQuyDoi
                String sqlInsert = "INSERT INTO DonViQuyDoi (maThuoc, tenDonVi, giaTriQuyDoi, giaBan, laDonViCoBan) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmtInsert = con.prepareStatement(sqlInsert);
                stmtInsert.setString(1, maThuoc);
                stmtInsert.setString(2, donViTinh);
                stmtInsert.setInt(3, laDonViCoBan ? 1 : giaTriQuyDoi);
                stmtInsert.setDouble(4, giaNhap * 1.2); // Giá bán mặc định = giá nhập + 20%
                stmtInsert.setBoolean(5, laDonViCoBan);
                stmtInsert.executeUpdate();
            }

            // Thêm vào ChiTietBangGia nếu chưa có (cho đơn vị nhập)
            capNhatChiTietBangGia(con, maThuoc, donViTinh, giaNhap * 1.2);

            // Nếu đơn vị nhập khác đơn vị cơ bản, cũng cập nhật giá cho đơn vị cơ bản
            if (!donViTinh.equalsIgnoreCase(donViCoBan)) {
                capNhatChiTietBangGia(con, maThuoc, donViCoBan, giaNhapDonViCoBan * 1.2);
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật hoặc thêm mới giá bán vào ChiTietBangGia
     */
    private void capNhatChiTietBangGia(Connection con, String maThuoc, String donViTinh, double giaBan) throws SQLException {
        // Kiểm tra đã có trong ChiTietBangGia chưa
        String sqlCheckBG = "SELECT 1 FROM ChiTietBangGia ct JOIN BangGia bg ON ct.maBG = bg.maBG WHERE ct.maThuoc = ? AND ct.donViTinh = ? AND bg.trangThai = 1";
        PreparedStatement stmtCheckBG = con.prepareStatement(sqlCheckBG);
        stmtCheckBG.setString(1, maThuoc);
        stmtCheckBG.setString(2, donViTinh);
        ResultSet rsBG = stmtCheckBG.executeQuery();

        if (!rsBG.next()) {
            // Lấy mã bảng giá đang active
            String sqlGetBG = "SELECT TOP 1 maBG FROM BangGia WHERE trangThai = 1";
            PreparedStatement stmtGetBG = con.prepareStatement(sqlGetBG);
            ResultSet rsBGActive = stmtGetBG.executeQuery();

            if (rsBGActive.next()) {
                String maBG = rsBGActive.getString("maBG");
                String sqlInsertBG = "INSERT INTO ChiTietBangGia (maBG, maThuoc, donViTinh, giaBan) VALUES (?, ?, ?, ?)";
                PreparedStatement stmtInsertBG = con.prepareStatement(sqlInsertBG);
                stmtInsertBG.setString(1, maBG);
                stmtInsertBG.setString(2, maThuoc);
                stmtInsertBG.setString(3, donViTinh);
                stmtInsertBG.setDouble(4, giaBan);
                stmtInsertBG.executeUpdate();
            }
        }
    }
}
