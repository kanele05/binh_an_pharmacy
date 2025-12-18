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

            // Lấy giá nhập và quy đổi về đơn vị tính của chi tiết bảng giá
            // Công thức: giaNhap = (donGia_nhập / giaTriQuyDoi_nhập) * giaTriQuyDoi_banggia
            String sql = "SELECT t.maThuoc, t.tenThuoc, nt.tenNhom, ct.donViTinh, "
                    + "ISNULL((SELECT TOP 1 "
                    + "        (ctpn.donGia / ISNULL(NULLIF(dvqd_nhap.giaTriQuyDoi, 0), 1)) * ISNULL(dvqd_bg.giaTriQuyDoi, 1) "
                    + "        FROM ChiTietPhieuNhap ctpn "
                    + "        JOIN PhieuNhap pn ON ctpn.maPN = pn.maPN "
                    + "        LEFT JOIN DonViQuyDoi dvqd_nhap ON ctpn.maThuoc = dvqd_nhap.maThuoc AND ctpn.donViTinh = dvqd_nhap.tenDonVi "
                    + "        LEFT JOIN DonViQuyDoi dvqd_bg ON t.maThuoc = dvqd_bg.maThuoc AND ct.donViTinh = dvqd_bg.tenDonVi "
                    + "        WHERE ctpn.maThuoc = t.maThuoc "
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

        // Lấy giá nhập và quy đổi về đơn vị tính của chi tiết bảng giá
        // Công thức: giaNhap = (donGia_nhập / giaTriQuyDoi_nhập) * giaTriQuyDoi_banggia
        String sql = "SELECT t.maThuoc, t.tenThuoc, nt.tenNhom, ct.donViTinh, "
                + "ISNULL((SELECT TOP 1 "
                + "        (ctpn.donGia / ISNULL(NULLIF(dvqd_nhap.giaTriQuyDoi, 0), 1)) * ISNULL(dvqd_bg.giaTriQuyDoi, 1) "
                + "        FROM ChiTietPhieuNhap ctpn "
                + "        JOIN PhieuNhap pn ON ctpn.maPN = pn.maPN "
                + "        LEFT JOIN DonViQuyDoi dvqd_nhap ON ctpn.maThuoc = dvqd_nhap.maThuoc AND ctpn.donViTinh = dvqd_nhap.tenDonVi "
                + "        LEFT JOIN DonViQuyDoi dvqd_bg ON t.maThuoc = dvqd_bg.maThuoc AND ct.donViTinh = dvqd_bg.tenDonVi "
                + "        WHERE ctpn.maThuoc = t.maThuoc "
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
     * Cập nhật giá nhập cho TẤT CẢ các đơn vị quy đổi của thuốc khi xác nhận nhập kho.
     * Giá nhập được tính dựa trên giá nhập của đơn vị lớn và giá trị quy đổi.
     * Ví dụ: Nhập 1 hộp 50 viên giá 70000 -> viên = 70000/50 = 1400, vỉ 10 viên = 1400*10 = 14000
     *
     * @param maThuoc   Mã thuốc
     * @param donViTinh Đơn vị tính (đơn vị từ phiếu nhập)
     * @param giaNhap   Giá nhập của đơn vị nhập
     * @return true nếu thành công
     */
    public boolean capNhatGiaNhap(String maThuoc, String donViTinh, double giaNhap) {
        try {
            Connection con = ConnectDB.getConnection();

            // Lấy giá trị quy đổi của đơn vị nhập
            String sqlGetQuyDoi = "SELECT giaTriQuyDoi FROM DonViQuyDoi WHERE maThuoc = ? AND tenDonVi = ?";
            PreparedStatement stmtQuyDoi = con.prepareStatement(sqlGetQuyDoi);
            stmtQuyDoi.setString(1, maThuoc);
            stmtQuyDoi.setString(2, donViTinh);
            ResultSet rsQuyDoi = stmtQuyDoi.executeQuery();

            int giaTriQuyDoiNhap = 1;
            if (rsQuyDoi.next()) {
                giaTriQuyDoiNhap = rsQuyDoi.getInt("giaTriQuyDoi");
            }

            // Tính giá nhập cho đơn vị cơ bản (1 đơn vị nhỏ nhất)
            // Giá đơn vị cơ bản = giá nhập / giá trị quy đổi
            double giaNhapDonViCoBan = giaNhap / giaTriQuyDoiNhap;

            // Lấy TẤT CẢ các đơn vị quy đổi của thuốc này
            String sqlGetAllDonVi = "SELECT tenDonVi, giaTriQuyDoi FROM DonViQuyDoi WHERE maThuoc = ?";
            PreparedStatement stmtAllDonVi = con.prepareStatement(sqlGetAllDonVi);
            stmtAllDonVi.setString(1, maThuoc);
            ResultSet rsAllDonVi = stmtAllDonVi.executeQuery();

            // Cập nhật giá cho TẤT CẢ các đơn vị quy đổi
            while (rsAllDonVi.next()) {
                String tenDonVi = rsAllDonVi.getString("tenDonVi");
                int giaTriQuyDoi = rsAllDonVi.getInt("giaTriQuyDoi");

                // Giá nhập của đơn vị này = giá đơn vị cơ bản * giá trị quy đổi
                double giaNhapDonVi = giaNhapDonViCoBan * giaTriQuyDoi;

                // Cập nhật hoặc thêm vào ChiTietBangGia với giá bán = giá nhập * 1.2 (20% lãi)
                capNhatChiTietBangGia(con, maThuoc, tenDonVi, giaNhapDonVi * 1.2);
            }

            // Nếu đơn vị nhập chưa có trong DonViQuyDoi, thêm mới
            String sqlCheck = "SELECT id FROM DonViQuyDoi WHERE maThuoc = ? AND tenDonVi = ?";
            PreparedStatement stmtCheck = con.prepareStatement(sqlCheck);
            stmtCheck.setString(1, maThuoc);
            stmtCheck.setString(2, donViTinh);
            ResultSet rs = stmtCheck.executeQuery();

            if (!rs.next()) {
                // Thêm vào DonViQuyDoi
                String sqlInsert = "INSERT INTO DonViQuyDoi (maThuoc, tenDonVi, giaTriQuyDoi, giaBan, laDonViCoBan) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmtInsert = con.prepareStatement(sqlInsert);
                stmtInsert.setString(1, maThuoc);
                stmtInsert.setString(2, donViTinh);
                stmtInsert.setInt(3, giaTriQuyDoiNhap);
                stmtInsert.setDouble(4, giaNhap * 1.2);
                stmtInsert.setBoolean(5, giaTriQuyDoiNhap == 1);
                stmtInsert.executeUpdate();

                // Thêm vào ChiTietBangGia
                capNhatChiTietBangGia(con, maThuoc, donViTinh, giaNhap * 1.2);
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
