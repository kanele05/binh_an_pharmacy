package dao;

import connectDB.ConnectDB;
import entities.ChiTietDonDat;
import entities.DonDatHang;
import entities.KhachHang;
import entities.NhanVien;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DonDatHangDAO {

    private ChiTietDonDatDAO ctDAO = new ChiTietDonDatDAO();

    public String getNewMaDonDat() {
        String newID = "DD001";
        String sql = "SELECT TOP 1 maDonDat FROM DonDatHang ORDER BY maDonDat DESC";

        try {
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                String lastID = rs.getString("maDonDat");

                if (lastID != null && lastID.startsWith("DD")) {
                    try {

                        String numberPart = lastID.substring(2);

                        int number = Integer.parseInt(numberPart);
                        number++;

                        newID = String.format("DD%03d", number);

                    } catch (NumberFormatException e) {

                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newID;
    }

    public boolean taoDonDatHang(DonDatHang don, List<ChiTietDonDat> listCT) {
        Connection con = null;
        PreparedStatement stmtDon = null;

        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            String sqlDon = "INSERT INTO DonDatHang (maDonDat, tenKhach, sdtLienHe, gioHenLay, tongTien, ghiChu, trangThai, maNV, maKH) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            stmtDon = con.prepareStatement(sqlDon);
            stmtDon.setString(1, don.getMaDonDat());
            stmtDon.setString(2, don.getTenKhach());
            stmtDon.setString(3, don.getSdtLienHe());
            stmtDon.setTimestamp(4, Timestamp.valueOf(don.getGioHenLay()));
            stmtDon.setDouble(5, don.getTongTien());
            stmtDon.setString(6, don.getGhiChu());
            stmtDon.setString(7, don.getTrangThai());
            stmtDon.setString(8, don.getNhanVien().getMaNV());

            if (don.getKhachHang() != null) {
                stmtDon.setString(9, don.getKhachHang().getMaKH());
            } else {
                stmtDon.setNull(9, java.sql.Types.NVARCHAR);
            }

            stmtDon.executeUpdate();

            for (ChiTietDonDat ct : listCT) {
                ctDAO.insert(con, ct);
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
            }
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
                if (stmtDon != null) {
                    stmtDon.close();
                }
            } catch (SQLException e) {
            }
        }
        return false;
    }

    public ArrayList<DonDatHang> getAllDonDat() {
        ArrayList<DonDatHang> list = new ArrayList<>();
        String sql = "SELECT * FROM DonDatHang ORDER BY gioHenLay DESC";
        try {
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {

                NhanVien nv = new NhanVien(rs.getString("maNV"));

                KhachHang kh = null;
                if (rs.getString("maKH") != null) {
                    kh = new KhachHang();
                    kh.setMaKH(rs.getString("maKH"));
                }

                DonDatHang d = new DonDatHang(
                        rs.getString("maDonDat"),
                        rs.getString("tenKhach"),
                        rs.getString("sdtLienHe"),
                        rs.getTimestamp("gioHenLay").toLocalDateTime(),
                        rs.getDouble("tongTien"),
                        rs.getString("ghiChu"),
                        rs.getString("trangThai"),
                        nv, kh
                );
                list.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateTrangThai(String maDon, String trangThaiMoi) {
        String sql = "UPDATE DonDatHang SET trangThai = ? WHERE maDonDat = ?";
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, trangThaiMoi);
            stmt.setString(2, maDon);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public DonDatHang getDonDatByID(String id) {

        return null;
    }

    public boolean updateDonDatHang(DonDatHang don, List<ChiTietDonDat> listCT) {
        Connection con = null;
        PreparedStatement stmtDon = null;

        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            String sqlDon = "UPDATE DonDatHang SET tenKhach=?, sdtLienHe=?, gioHenLay=?, tongTien=?, ghiChu=?, maKH=? WHERE maDonDat=?";
            stmtDon = con.prepareStatement(sqlDon);
            stmtDon.setString(1, don.getTenKhach());
            stmtDon.setString(2, don.getSdtLienHe());
            stmtDon.setTimestamp(3, Timestamp.valueOf(don.getGioHenLay()));
            stmtDon.setDouble(4, don.getTongTien());
            stmtDon.setString(5, don.getGhiChu());

            if (don.getKhachHang() != null) {
                stmtDon.setString(6, don.getKhachHang().getMaKH());
            } else {
                stmtDon.setNull(6, java.sql.Types.NVARCHAR);
            }
            stmtDon.setString(7, don.getMaDonDat());

            int rows = stmtDon.executeUpdate();
            if (rows == 0) {
                return false;
            }

            ctDAO.deleteByMaDon(con, don.getMaDonDat());

            for (ChiTietDonDat ct : listCT) {
                ctDAO.insert(con, ct);
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
            }
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
                if (stmtDon != null) {
                    stmtDon.close();
                }
            } catch (SQLException e) {
            }
        }
        return false;
    }
}
