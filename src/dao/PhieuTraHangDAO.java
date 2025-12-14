package dao;

import connectDB.ConnectDB;
import entities.ChiTietPhieuTra;
import entities.HoaDon;
import entities.PhieuTraHang;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class PhieuTraHangDAO {

    public PhieuTraHangDAO() {
    }

    public String getNewMaPhieuTra() {
        String newID = "PT001";
        String sql = "SELECT TOP 1 maPhieuTra FROM PhieuTraHang ORDER BY maPhieuTra DESC";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                String lastID = rs.getString("maPhieuTra");
                try {
                    int num = Integer.parseInt(lastID.substring(2)) + 1;
                    newID = String.format("PT%03d", num);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newID;
    }

    public boolean insert(PhieuTraHang phieu) throws SQLException {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            String sql = "INSERT INTO PhieuTraHang (maPhieuTra, maHD, maNV, maKH, ngayTra, lyDoTra, tongTienHoanTra, trangThai, ghiChu) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, phieu.getMaPT());
            stmt.setString(2, phieu.getHoaDon() != null ? phieu.getHoaDon().getMaHD() : null);
            stmt.setString(3, phieu.getNhanVien().getMaNV());
            stmt.setString(4, phieu.getKhachHang() != null ? phieu.getKhachHang().getMaKH() : null);
            stmt.setDate(5, Date.valueOf(phieu.getNgayTra()));
            stmt.setString(6, phieu.getLyDo());
            stmt.setDouble(7, phieu.getTongTienHoanTra());
            stmt.setString(8, phieu.getTrangThai());
            stmt.setString(9, phieu.getGhiChu());

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

    public ArrayList<PhieuTraHang> getAllPhieuTra() {
        ArrayList<PhieuTraHang> dsPhieu = new ArrayList<>();
        String sql = "SELECT pt.*, kh.tenKH, kh.sdt as sdtKH, nv.hoTen as tenNV "
                + "FROM PhieuTraHang pt "
                + "LEFT JOIN KhachHang kh ON pt.maKH = kh.maKH "
                + "LEFT JOIN NhanVien nv ON pt.maNV = nv.maNV "
                + "ORDER BY pt.ngayTra DESC";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                entities.NhanVien nv = new entities.NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setHoTen(rs.getString("tenNV"));

                entities.KhachHang kh = null;
                if (rs.getString("maKH") != null) {
                    kh = new entities.KhachHang();
                    kh.setMaKH(rs.getString("maKH"));
                    kh.setTenKH(rs.getString("tenKH"));
                    kh.setSdt(rs.getString("sdtKH"));
                }

                HoaDon hd = null;
                if (rs.getString("maHD") != null) {
                    hd = new HoaDon();
                    hd.setMaHD(rs.getString("maHD"));
                }

                PhieuTraHang pt = new PhieuTraHang(
                        rs.getString("maPhieuTra"),
                        rs.getDate("ngayTra") != null ? rs.getDate("ngayTra").toLocalDate() : null,
                        rs.getDouble("tongTienHoanTra"),
                        rs.getString("lyDoTra"),
                        hd, nv, kh,
                        rs.getString("trangThai"),
                        rs.getString("ghiChu")
                );
                dsPhieu.add(pt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsPhieu;
    }

    public ArrayList<PhieuTraHang> searchPhieuTra(String keyword) {
        ArrayList<PhieuTraHang> dsPhieu = new ArrayList<>();
        String sql = "SELECT pt.*, kh.tenKH, kh.sdt as sdtKH, nv.hoTen as tenNV "
                + "FROM PhieuTraHang pt "
                + "LEFT JOIN KhachHang kh ON pt.maKH = kh.maKH "
                + "LEFT JOIN NhanVien nv ON pt.maNV = nv.maNV "
                + "WHERE pt.maPhieuTra LIKE ? OR kh.tenKH LIKE ? OR kh.sdt LIKE ? "
                + "ORDER BY pt.ngayTra DESC";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                entities.NhanVien nv = new entities.NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setHoTen(rs.getString("tenNV"));

                entities.KhachHang kh = null;
                if (rs.getString("maKH") != null) {
                    kh = new entities.KhachHang();
                    kh.setMaKH(rs.getString("maKH"));
                    kh.setTenKH(rs.getString("tenKH"));
                    kh.setSdt(rs.getString("sdtKH"));
                }

                HoaDon hd = null;
                if (rs.getString("maHD") != null) {
                    hd = new HoaDon();
                    hd.setMaHD(rs.getString("maHD"));
                }

                PhieuTraHang pt = new PhieuTraHang(
                        rs.getString("maPhieuTra"),
                        rs.getDate("ngayTra") != null ? rs.getDate("ngayTra").toLocalDate() : null,
                        rs.getDouble("tongTienHoanTra"),
                        rs.getString("lyDoTra"),
                        hd, nv, kh,
                        rs.getString("trangThai"),
                        rs.getString("ghiChu")
                );
                dsPhieu.add(pt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsPhieu;
    }

    public PhieuTraHang getPhieuTraByMa(String maPhieu) {
        PhieuTraHang pt = null;
        String sql = "SELECT pt.*, kh.tenKH, kh.sdt as sdtKH, nv.hoTen as tenNV "
                + "FROM PhieuTraHang pt "
                + "LEFT JOIN KhachHang kh ON pt.maKH = kh.maKH "
                + "LEFT JOIN NhanVien nv ON pt.maNV = nv.maNV "
                + "WHERE pt.maPhieuTra = ?";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maPhieu);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                entities.NhanVien nv = new entities.NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setHoTen(rs.getString("tenNV"));

                entities.KhachHang kh = null;
                if (rs.getString("maKH") != null) {
                    kh = new entities.KhachHang();
                    kh.setMaKH(rs.getString("maKH"));
                    kh.setTenKH(rs.getString("tenKH"));
                    kh.setSdt(rs.getString("sdtKH"));
                }

                HoaDon hd = null;
                if (rs.getString("maHD") != null) {
                    hd = new HoaDon();
                    hd.setMaHD(rs.getString("maHD"));
                }

                pt = new PhieuTraHang(
                        rs.getString("maPhieuTra"),
                        rs.getDate("ngayTra") != null ? rs.getDate("ngayTra").toLocalDate() : null,
                        rs.getDouble("tongTienHoanTra"),
                        rs.getString("lyDoTra"),
                        hd, nv, kh,
                        rs.getString("trangThai"),
                        rs.getString("ghiChu")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pt;
    }

    public boolean taoPhieuTra(PhieuTraHang phieu, List<ChiTietPhieuTra> listCT) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // Insert PhieuTraHang
            String sqlPhieu = "INSERT INTO PhieuTraHang (maPhieuTra, maHD, maNV, maKH, ngayTra, lyDoTra, tongTienHoanTra, trangThai, ghiChu) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmtPhieu = con.prepareStatement(sqlPhieu);
            stmtPhieu.setString(1, phieu.getMaPT());
            stmtPhieu.setString(2, phieu.getHoaDon() != null ? phieu.getHoaDon().getMaHD() : null);
            stmtPhieu.setString(3, phieu.getNhanVien().getMaNV());
            stmtPhieu.setString(4, phieu.getKhachHang() != null ? phieu.getKhachHang().getMaKH() : null);
            stmtPhieu.setDate(5, Date.valueOf(phieu.getNgayTra()));
            stmtPhieu.setString(6, phieu.getLyDo());
            stmtPhieu.setDouble(7, phieu.getTongTienHoanTra());
            stmtPhieu.setString(8, phieu.getTrangThai());
            stmtPhieu.setString(9, phieu.getGhiChu());
            stmtPhieu.executeUpdate();

            // Insert ChiTietPhieuTra and update LoThuoc
            // NOTE: ctDAO.insert() uses its own connection (same pattern as existing code)
            // All operations share same ConnectDB singleton connection pool
            ChiTietPhieuTraDAO ctDAO = new ChiTietPhieuTraDAO();
            for (ChiTietPhieuTra ct : listCT) {
                // Insert ChiTietPhieuTra
                ctDAO.insert(ct);

                // Return quantity to lot
                String sqlUpdateLo = "UPDATE LoThuoc SET soLuongTon = soLuongTon + ? WHERE maLo = ?";
                PreparedStatement stmtUpdateLo = con.prepareStatement(sqlUpdateLo);
                stmtUpdateLo.setInt(1, ct.getSoLuongTra());
                stmtUpdateLo.setString(2, ct.getLoThuoc().getMaLo());
                stmtUpdateLo.executeUpdate();
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
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public boolean checkHoaDonCoTheTraHang(String maHD) {
        String sql = "SELECT ngayTao FROM HoaDon WHERE maHD = ?";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maHD);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                LocalDate ngayTao = rs.getDate("ngayTao").toLocalDate();
                LocalDate now = LocalDate.now();
                long daysBetween = ChronoUnit.DAYS.between(ngayTao, now);
                return daysBetween <= 30;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
