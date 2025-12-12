package dao;

import connectDB.ConnectDB;
import entities.BangGia;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BangGiaDAO {

    public BangGiaDAO() {
    }

    public ArrayList<BangGia> getAllTblBangGia() {
        ArrayList<BangGia> dsBangGia = new ArrayList<>();
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();

            String sql = "SELECT * FROM BangGia ORDER BY maBG DESC";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {

                String maBG = rs.getString(1);
                String tenBG = rs.getString(2);
                java.sql.Date dHieuLuc = rs.getDate(3);
                LocalDate ngayHieuLuc = (dHieuLuc != null) ? dHieuLuc.toLocalDate() : null;
                java.sql.Date dKetThuc = rs.getDate(4);
                LocalDate ngayKetThuc = (dKetThuc != null) ? dKetThuc.toLocalDate() : null;
                String ghiChu = rs.getString(5);
                boolean trangThai = rs.getBoolean(6);
                dsBangGia.add(new BangGia(maBG, tenBG, ngayHieuLuc, ngayKetThuc, ghiChu, trangThai));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsBangGia;
    }

    public boolean insertNewPriceList(BangGia newBG, List<Object[]> details) throws SQLException {

        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        CallableStatement cstmt = null;
        PreparedStatement pstmtDetail = null;
        try {
            con.setAutoCommit(false);
            String sqlProc = "{call sp_TaoBangGiaMoi(?, ?, ?, ?)}";
            cstmt = con.prepareCall(sqlProc);
            cstmt.setString(1, newBG.getTenBG());
            cstmt.setDate(2, Date.valueOf(newBG.getNgayHieuLuc()));
            cstmt.setString(3, newBG.getGhiChu());
            cstmt.registerOutParameter(4, java.sql.Types.VARCHAR);
            cstmt.execute();
            String newMaBG = cstmt.getString(4);

            String sqlDetail = "INSERT INTO ChiTietBangGia (maBG, maThuoc, donViTinh, giaBan) VALUES (?, ?, ?, ?)";
            pstmtDetail = con.prepareStatement(sqlDetail);
            for (Object[] row : details) {
                pstmtDetail.setString(1, newMaBG);
                pstmtDetail.setString(2, row[0].toString());
                pstmtDetail.setString(3, row[3].toString());
                double giaBan = Double.parseDouble(row[5].toString().replace(".", "").replace(",", ""));
                pstmtDetail.setDouble(4, giaBan);
                pstmtDetail.addBatch();
            }
            pstmtDetail.executeBatch();
            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
            }
            return false;
        } finally {
            try {
                if (cstmt != null) {
                    cstmt.close();
                }
                if (pstmtDetail != null) {
                    pstmtDetail.close();
                }
                con.setAutoCommit(true);
            } catch (SQLException e) {
            }
        }
    }

    public boolean insertDraftBangGia(BangGia newBG, List<Object[]> details) throws SQLException {
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmtHeader = null;
        PreparedStatement stmtDetail = null;

        try {
            con.setAutoCommit(false);

            String newMaBG = "BG001";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT TOP 1 maBG FROM BangGia ORDER BY maBG DESC");
            if (rs.next()) {
                String lastID = rs.getString(1);
                int num = Integer.parseInt(lastID.substring(2)) + 1;
                newMaBG = String.format("BG%03d", num);
            }

            String sqlHeader = "INSERT INTO BangGia (maBG, tenBG, ngayHieuLuc, ngayKetThuc, ghiChu, trangThai) VALUES (?, ?, ?, ?, ?, ?)";
            stmtHeader = con.prepareStatement(sqlHeader);
            stmtHeader.setString(1, newMaBG);
            stmtHeader.setString(2, newBG.getTenBG());
            stmtHeader.setDate(3, Date.valueOf(newBG.getNgayHieuLuc()));

            if (newBG.getNgayKetThuc() != null) {
                stmtHeader.setDate(4, Date.valueOf(newBG.getNgayKetThuc()));
            } else {
                stmtHeader.setNull(4, java.sql.Types.DATE);
            }

            stmtHeader.setString(5, newBG.getGhiChu());
            stmtHeader.setBoolean(6, false);
            stmtHeader.executeUpdate();

            String sqlDetail = "INSERT INTO ChiTietBangGia (maBG, maThuoc, donViTinh, giaBan) VALUES (?, ?, ?, ?)";
            stmtDetail = con.prepareStatement(sqlDetail);
            for (Object[] row : details) {
                stmtDetail.setString(1, newMaBG);
                stmtDetail.setString(2, row[0].toString());
                stmtDetail.setString(3, row[3].toString());
                double giaBan = Double.parseDouble(row[5].toString().replace(".", "").replace(",", ""));
                stmtDetail.setDouble(4, giaBan);
                stmtDetail.addBatch();
            }
            stmtDetail.executeBatch();

            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException ex) {
            }
            return false;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
            }
        }
    }

    public boolean activateBangGia(String maBG) throws SQLException {

        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        try {
            con.setAutoCommit(false);
            String sqlOff = "UPDATE BangGia SET trangThai = 0, ngayKetThuc = GETDATE() WHERE trangThai = 1";
            PreparedStatement stmtOff = con.prepareStatement(sqlOff);
            stmtOff.executeUpdate();

            String sqlOn = "UPDATE BangGia SET trangThai = 1, ngayHieuLuc = GETDATE(), ngayKetThuc = NULL WHERE maBG = ?";
            PreparedStatement stmtOn = con.prepareStatement(sqlOn);
            stmtOn.setString(1, maBG);
            stmtOn.executeUpdate();

            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException ex) {
            }
            return false;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
            }
        }
    }
}
