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
}
