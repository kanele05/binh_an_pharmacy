package dao;

import connectDB.ConnectDB;
import dto.ThuocTimKiem;
import entities.LoThuoc;
import entities.Thuoc;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoThuocDAO {

    private static final int DEFAULT_TON_TOI_THIEU = 10;

    public LoThuocDAO() {
    }

    ;
    public ArrayList<LoThuoc> getAllTblLoThuoc() {
        ArrayList<LoThuoc> dsLo = new ArrayList<>();
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();

            String sql = "SELECT l.maLo, l.maThuoc, t.tenThuoc, l.ngayNhap, l.hanSuDung, l.soLuongTon, l.trangThai, l.isDeleted "
                    + "FROM LoThuoc l "
                    + "JOIN Thuoc t ON l.maThuoc = t.maThuoc "
                    + "WHERE l.isDeleted = 0 "
                    + "ORDER BY l.hanSuDung ASC";

            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String maLo = rs.getString("maLo");
                String maThuoc = rs.getString("maThuoc");
                String tenThuoc = rs.getString("tenThuoc");

                Thuoc thuoc = new Thuoc(maThuoc);
                thuoc.setTenThuoc(tenThuoc);

                LocalDate ngayNhap = rs.getDate("ngayNhap").toLocalDate();
                LocalDate hanSuDung = rs.getDate("hanSuDung").toLocalDate();
                int soLuongTon = rs.getInt("soLuongTon");
                String trangThai = rs.getString("trangThai");
                boolean isDeleted = rs.getBoolean("isDeleted");

                LoThuoc lo = new LoThuoc(maLo, thuoc, ngayNhap, hanSuDung, soLuongTon, trangThai, isDeleted);
                dsLo.add(lo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsLo;
    }

    public boolean update(LoThuoc lo) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {

            String sql = "UPDATE LoThuoc SET hanSuDung = ?, soLuongTon = ?, trangThai = ? WHERE maLo = ?";
            stmt = con.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(lo.getHanSuDung()));
            stmt.setInt(2, lo.getSoLuongTon());
            stmt.setString(3, lo.getTrangThai());
            stmt.setString(4, lo.getMaLo());

            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        return n > 0;
    }

    public boolean delete(String maLo) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "UPDATE LoThuoc SET isDeleted = 1 WHERE maLo = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, maLo);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            stmt.close();
        }
        return n > 0;
    }

    public ArrayList<ThuocTimKiem> getDanhSachThuocBanHang() {
        ArrayList<ThuocTimKiem> listKho = new ArrayList<>();

        String sqlKho = "SELECT * FROM vw_ThuocBanHang ORDER BY maThuoc, hanSuDung ASC";

        String sqlDatHang = "SELECT ct.maThuoc, "
                + "SUM(ct.soLuong * ISNULL(dv.giaTriQuyDoi, 1)) as dangGiu "
                + "FROM ChiTietDonDat ct "
                + "JOIN DonDatHang d ON ct.maDonDat = d.maDonDat "
                + "LEFT JOIN DonViQuyDoi dv ON (ct.maThuoc = dv.maThuoc AND ct.donViTinh = dv.tenDonVi) " // Join bảng quy đổi
                + "WHERE d.trangThai = N'Đang giữ hàng' "
                + "GROUP BY ct.maThuoc";

        Connection con = null;
        try {
            con = ConnectDB.getConnection();

            Map<String, Integer> mapDangGiu = new HashMap<>();
            Statement st1 = con.createStatement();
            ResultSet rs1 = st1.executeQuery(sqlDatHang);
            while (rs1.next()) {
                mapDangGiu.put(rs1.getString("maThuoc"), rs1.getInt("dangGiu"));
            }
            rs1.close();
            st1.close();

            Statement st2 = con.createStatement();
            ResultSet rs = st2.executeQuery(sqlKho);

            while (rs.next()) {
                String maThuoc = rs.getString("maThuoc");
                int tonThucTe = rs.getInt("soLuongTon");
                int tonKhaDung = tonThucTe;

                if (mapDangGiu.containsKey(maThuoc)) {
                    int slCanGiu = mapDangGiu.get(maThuoc);

                    if (slCanGiu > 0) {

                        if (slCanGiu >= tonThucTe) {
                            tonKhaDung = 0;
                            mapDangGiu.put(maThuoc, slCanGiu - tonThucTe);
                        } else {

                            tonKhaDung = tonThucTe - slCanGiu;
                            mapDangGiu.put(maThuoc, 0);
                        }
                    }
                }

                if (tonKhaDung > 0) {
                    listKho.add(new ThuocTimKiem(
                            maThuoc,
                            rs.getString("tenThuoc"),
                            rs.getString("maLo"),
                            rs.getDate("hanSuDung").toLocalDate(),
                            tonKhaDung, // Tồn kho thực tế (đơn vị gốc)
                            rs.getString("donViTinh"), // Đơn vị bán (Vỉ/Hộp)
//                            rs.getString("donViCoBan"), // Đơn vị gốc (Viên)
                            rs.getInt("giaTriQuyDoi"), // Tỷ lệ quy đổi
                            rs.getDouble("giaBan") // Giá bán của đơn vị này
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listKho;
    }

    public String getMaThuocByMaLo(String maLo) {
        String maThuoc = "";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement p = con.prepareStatement("SELECT maThuoc FROM LoThuoc WHERE maLo = ?");
            p.setString(1, maLo);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                maThuoc = rs.getString("maThuoc");
            }
        } catch (Exception e) {
        }
        return maThuoc;
    }

    public List<entities.LoThuoc> getDanhSachLoKhaDung(String maThuoc) {
        java.util.List<entities.LoThuoc> list = new java.util.ArrayList<>();
        String sql = "SELECT maLo, soLuongTon, hanSuDung FROM LoThuoc "
                + "WHERE maThuoc = ? AND soLuongTon > 0 AND trangThai != N'Đã hết hạn' "
                + "ORDER BY hanSuDung ASC";
        try {
            java.sql.Connection con = connectDB.ConnectDB.getConnection();
            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maThuoc);
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                entities.LoThuoc lo = new entities.LoThuoc(
                        rs.getString("maLo"),
                        null, null,
                        rs.getDate("hanSuDung").toLocalDate(),
                        rs.getInt("soLuongTon"),
                        "", false
                );
                list.add(lo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getTongTonByMaThuoc(String maThuoc) {
        String sql = "SELECT COALESCE(SUM(soLuongTon), 0) as tongTon FROM LoThuoc WHERE maThuoc = ? AND trangThai != N'Đã hết hạn' AND isDeleted = 0";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maThuoc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("tongTon");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTonKhoByMaLo(String maLo) {
        String sql = "SELECT soLuongTon FROM LoThuoc WHERE maLo = ? AND trangThai != N'Đã hết hạn' AND isDeleted = 0";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("soLuongTon");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<dto.ThuocSapHetHan> getThuocSapHetHan(int soNgay) {
        List<dto.ThuocSapHetHan> list = new ArrayList<>();
        String sql = "SELECT l.maLo, l.maThuoc, t.tenThuoc, l.hanSuDung, l.soLuongTon, "
                + "DATEDIFF(DAY, GETDATE(), l.hanSuDung) as soNgayConLai "
                + "FROM LoThuoc l "
                + "JOIN Thuoc t ON l.maThuoc = t.maThuoc "
                + "WHERE l.hanSuDung > GETDATE() "
                + "AND DATEDIFF(DAY, GETDATE(), l.hanSuDung) <= ? "
                + "AND l.soLuongTon > 0 "
                + "AND l.isDeleted = 0 "
                + "AND l.trangThai != N'Đã hết hạn' "
                + "ORDER BY l.hanSuDung ASC";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, soNgay);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dto.ThuocSapHetHan thuoc = new dto.ThuocSapHetHan(
                        rs.getString("maThuoc"),
                        rs.getString("tenThuoc"),
                        rs.getString("maLo"),
                        rs.getDate("hanSuDung").toLocalDate(),
                        rs.getInt("soLuongTon"),
                        rs.getInt("soNgayConLai")
                );
                list.add(thuoc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<dto.ThuocSapHetHan> getThuocDaHetHan() {
        List<dto.ThuocSapHetHan> list = new ArrayList<>();
        String sql = "SELECT l.maLo, l.maThuoc, t.tenThuoc, l.hanSuDung, l.soLuongTon, "
                + "DATEDIFF(DAY, GETDATE(), l.hanSuDung) as soNgayConLai "
                + "FROM LoThuoc l "
                + "JOIN Thuoc t ON l.maThuoc = t.maThuoc "
                + "WHERE l.hanSuDung <= GETDATE() "
                + "AND l.soLuongTon > 0 "
                + "AND l.isDeleted = 0 "
                + "ORDER BY l.hanSuDung ASC";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dto.ThuocSapHetHan thuoc = new dto.ThuocSapHetHan(
                        rs.getString("maThuoc"),
                        rs.getString("tenThuoc"),
                        rs.getString("maLo"),
                        rs.getDate("hanSuDung").toLocalDate(),
                        rs.getInt("soLuongTon"),
                        rs.getInt("soNgayConLai")
                );
                list.add(thuoc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getSoLoSapHetHan(int soNgay) {
        String sql = "SELECT COUNT(*) as count FROM LoThuoc l "
                + "WHERE l.hanSuDung > GETDATE() "
                + "AND DATEDIFF(DAY, GETDATE(), l.hanSuDung) <= ? "
                + "AND l.soLuongTon > 0 "
                + "AND l.isDeleted = 0";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, soNgay);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getSoThuocTonThap() {
        String sql = "SELECT COUNT(*) as count FROM ("
                + "SELECT t.maThuoc "
                + "FROM Thuoc t "
                + "LEFT JOIN LoThuoc l ON t.maThuoc = l.maThuoc "
                + "AND l.isDeleted = 0 AND l.trangThai != N'Đã hết hạn' "
                + "WHERE t.trangThai = 1 "
                + "GROUP BY t.maThuoc, t.tonToiThieu "
                + "HAVING COALESCE(SUM(l.soLuongTon), 0) < ISNULL(t.tonToiThieu, " + DEFAULT_TON_TOI_THIEU + ")"
                + ") as subquery";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<String> getAllNhomThuoc() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT tenNhom FROM NhomThuoc ORDER BY tenNhom";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("tenNhom"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<dto.ThuocTonThap> getThuocTonThapByNhom(String tenNhom) {
        List<dto.ThuocTonThap> list = new ArrayList<>();
        String sql = "SELECT t.maThuoc, t.tenThuoc, t.donViCoBan, "
                + "ISNULL(t.tonToiThieu, " + DEFAULT_TON_TOI_THIEU + ") as tonToiThieu, "
                + "COALESCE(SUM(l.soLuongTon), 0) as tonKho, "
                + "nt.tenNhom, "
                + "(SELECT TOP 1 ncc.tenNCC FROM PhieuNhap pn "
                + " JOIN NhaCungCap ncc ON pn.maNCC = ncc.maNCC "
                + " JOIN ChiTietPhieuNhap ct ON pn.maPN = ct.maPN "
                + " WHERE ct.maThuoc = t.maThuoc "
                + " ORDER BY pn.ngayTao DESC) as nhaCungCap "
                + "FROM Thuoc t "
                + "LEFT JOIN LoThuoc l ON t.maThuoc = l.maThuoc "
                + "AND l.isDeleted = 0 AND l.trangThai != N'Đã hết hạn' "
                + "LEFT JOIN NhomThuoc nt ON t.maNhom = nt.maNhom "
                + "WHERE t.trangThai = 1 ";

        if (tenNhom != null && !tenNhom.isEmpty() && !tenNhom.equals("Tất cả nhóm")) {
            sql += "AND nt.tenNhom = N'" + tenNhom + "' ";
        }

        sql += "GROUP BY t.maThuoc, t.tenThuoc, t.donViCoBan, t.tonToiThieu, nt.tenNhom "
                + "HAVING COALESCE(SUM(l.soLuongTon), 0) < ISNULL(t.tonToiThieu, " + DEFAULT_TON_TOI_THIEU + ") "
                + "ORDER BY tonKho ASC";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int tonKho = rs.getInt("tonKho");
                int tonToiThieu = rs.getInt("tonToiThieu");
                int soLuongCanNhap = tonToiThieu - tonKho;
                dto.ThuocTonThap thuoc = new dto.ThuocTonThap(
                        rs.getString("maThuoc"),
                        rs.getString("tenThuoc"),
                        rs.getString("donViCoBan"),
                        tonKho,
                        tonToiThieu,
                        soLuongCanNhap,
                        rs.getString("nhaCungCap") != null ? rs.getString("nhaCungCap") : "Chưa có",
                        rs.getString("tenNhom")
                );
                list.add(thuoc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<dto.ThuocTonThap> getThuocTonThap() {
        List<dto.ThuocTonThap> list = new ArrayList<>();
        String sql = "SELECT t.maThuoc, t.tenThuoc, t.donViCoBan, "
                + "ISNULL(t.tonToiThieu, " + DEFAULT_TON_TOI_THIEU + ") as tonToiThieu, "
                + "COALESCE(SUM(l.soLuongTon), 0) as tonKho, "
                + "nt.tenNhom, "
                + "(SELECT TOP 1 ncc.tenNCC FROM PhieuNhap pn "
                + " JOIN NhaCungCap ncc ON pn.maNCC = ncc.maNCC "
                + " JOIN ChiTietPhieuNhap ct ON pn.maPN = ct.maPN "
                + " WHERE ct.maThuoc = t.maThuoc "
                + " ORDER BY pn.ngayTao DESC) as nhaCungCap "
                + "FROM Thuoc t "
                + "LEFT JOIN LoThuoc l ON t.maThuoc = l.maThuoc "
                + "AND l.isDeleted = 0 AND l.trangThai != N'Đã hết hạn' "
                + "LEFT JOIN NhomThuoc nt ON t.maNhom = nt.maNhom "
                + "WHERE t.trangThai = 1 "
                + "GROUP BY t.maThuoc, t.tenThuoc, t.donViCoBan, t.tonToiThieu, nt.tenNhom "
                + "HAVING COALESCE(SUM(l.soLuongTon), 0) < ISNULL(t.tonToiThieu, " + DEFAULT_TON_TOI_THIEU + ") "
                + "ORDER BY tonKho ASC";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int tonKho = rs.getInt("tonKho");
                int tonToiThieu = rs.getInt("tonToiThieu");
                int soLuongCanNhap = tonToiThieu - tonKho;
                dto.ThuocTonThap thuoc = new dto.ThuocTonThap(
                        rs.getString("maThuoc"),
                        rs.getString("tenThuoc"),
                        rs.getString("donViCoBan"),
                        tonKho,
                        tonToiThieu,
                        soLuongCanNhap,
                        rs.getString("nhaCungCap") != null ? rs.getString("nhaCungCap") : "Chưa có",
                        rs.getString("tenNhom")
                );
                list.add(thuoc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy danh sách tồn kho chi tiết theo lô để xuất báo cáo
     * @param nhomThuoc Nhóm thuốc cần lọc (null hoặc rỗng = tất cả)
     * @param trangThai Trạng thái lọc: "all", "sapHetHan", "daHetHan", "tonThap", "tonCao"
     * @return List các Map chứa thông tin chi tiết tồn kho
     */
    public List<Map<String, Object>> getBaoCaoTonKhoChiTiet(String nhomThuoc, String trangThai) {
        List<Map<String, Object>> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.maThuoc, t.tenThuoc, l.maLo, l.hanSuDung, l.soLuongTon, ");
        sql.append("COALESCE(ctn.donGia, 0) as giaVon, ");
        sql.append("l.soLuongTon * COALESCE(ctn.donGia, 0) as tongGiaTri, ");
        sql.append("l.trangThai, ");
        sql.append("DATEDIFF(DAY, GETDATE(), l.hanSuDung) as soNgayConLai, ");
        sql.append("nt.tenNhom ");
        sql.append("FROM LoThuoc l ");
        sql.append("JOIN Thuoc t ON l.maThuoc = t.maThuoc ");
        sql.append("LEFT JOIN NhomThuoc nt ON t.maNhom = nt.maNhom ");
        sql.append("LEFT JOIN (SELECT maThuoc, maLo, donGia FROM ChiTietPhieuNhap WHERE maLo IS NOT NULL) ctn ");
        sql.append("ON l.maLo = ctn.maLo ");
        sql.append("WHERE l.isDeleted = 0 ");

        // Lọc theo nhóm thuốc
        if (nhomThuoc != null && !nhomThuoc.isEmpty() && !nhomThuoc.equals("Tất cả nhóm")) {
            sql.append("AND nt.tenNhom = N'").append(nhomThuoc).append("' ");
        }

        // Lọc theo trạng thái
        if (trangThai != null) {
            switch (trangThai) {
                case "sapHetHan":
                    sql.append("AND DATEDIFF(DAY, GETDATE(), l.hanSuDung) BETWEEN 0 AND 30 ");
                    break;
                case "daHetHan":
                    sql.append("AND l.hanSuDung < GETDATE() ");
                    break;
                case "tonThap":
                    sql.append("AND l.soLuongTon < 10 AND l.soLuongTon > 0 ");
                    break;
                case "tonCao":
                    sql.append("AND l.soLuongTon > 500 ");
                    break;
            }
        }

        sql.append("ORDER BY l.hanSuDung ASC");

        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("maThuoc", rs.getString("maThuoc"));
                row.put("tenThuoc", rs.getString("tenThuoc"));
                row.put("maLo", rs.getString("maLo"));
                row.put("hanSuDung", rs.getDate("hanSuDung") != null ? rs.getDate("hanSuDung").toLocalDate() : null);
                row.put("soLuongTon", rs.getInt("soLuongTon"));
                row.put("giaVon", rs.getDouble("giaVon"));
                row.put("tongGiaTri", rs.getDouble("tongGiaTri"));
                row.put("soNgayConLai", rs.getInt("soNgayConLai"));

                // Xác định ghi chú dựa trên trạng thái
                int soNgay = rs.getInt("soNgayConLai");
                int soLuong = rs.getInt("soLuongTon");
                String ghiChu = "";
                if (soNgay < 0) {
                    ghiChu = "Đã hết hạn";
                } else if (soNgay <= 30) {
                    ghiChu = "Sắp hết hạn";
                } else if (soLuong < 10) {
                    ghiChu = "Tồn kho thấp";
                } else if (soLuong > 500) {
                    ghiChu = "Tồn kho cao";
                }
                row.put("ghiChu", ghiChu);

                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Map<String, Object> getTongHopTonKho() {
        Map<String, Object> result = new HashMap<>();
        String sql = "SELECT " +
                     "COALESCE(SUM(l.soLuongTon * COALESCE(ctn.donGia, 0)), 0) as tongGiaTri, " +
                     "COALESCE(SUM(l.soLuongTon), 0) as tongSoLuong, " +
                     "(SELECT COUNT(*) FROM LoThuoc WHERE isDeleted = 0 AND " +
                     "(hanSuDung < GETDATE() OR DATEDIFF(DAY, GETDATE(), hanSuDung) <= 30)) as soLoHetHan " +
                     "FROM LoThuoc l " +
                     "LEFT JOIN (SELECT maThuoc, maLo, donGia FROM ChiTietPhieuNhap WHERE maLo IS NOT NULL) ctn " +
                     "ON l.maLo = ctn.maLo " +
                     "WHERE l.isDeleted = 0";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result.put("tongGiaTri", rs.getDouble("tongGiaTri"));
                result.put("tongSoLuong", rs.getInt("tongSoLuong"));
                result.put("soLoHetHan", rs.getInt("soLoHetHan"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
