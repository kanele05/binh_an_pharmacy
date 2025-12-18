package dao;

import connectDB.ConnectDB;
import entities.NhaCungCap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class NhaCungCapDAO {

    public NhaCungCapDAO() {
    }

    public ArrayList<NhaCungCap> getAllNhaCungCap() {
        ArrayList<NhaCungCap> dsNCC = new ArrayList<>();
        String sql = "SELECT * FROM NhaCungCap";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                dsNCC.add(mapResultSetToNhaCungCap(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsNCC;
    }
    public ArrayList<String> getAllTenNhaCungCap(){
           ArrayList<String> dsTenNCC = new ArrayList<>();
           try {
               ConnectDB.getInstance();
               Connection con = ConnectDB.getConnection();
               String sql = "Select tenNCC from NhaCungCap";
               Statement statement = con.createStatement();
               ResultSet rs = statement.executeQuery(sql);
               while (rs.next()) {                   
                   dsTenNCC.add(rs.getString("tenNCC"));
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
           return dsTenNCC;
       }
    public NhaCungCap getNhaCungCapByID(String maNCC) {
        NhaCungCap ncc = null;
        String sql = "SELECT * FROM NhaCungCap WHERE maNCC = ?";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maNCC);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ncc = mapResultSetToNhaCungCap(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ncc;
    }

    public boolean insert(NhaCungCap ncc) {
    	   String sql = "INSERT INTO NhaCungCap (maNCC, tenNCC, sdt, email, diaChi) VALUES (?, ?, ?, ?, ?)";    	   try (Connection con  = ConnectDB.getConnection();
    		PreparedStatement ps = con.prepareStatement(sql);) {
    		   ps.setString(1, ncc.getMaNCC());
    		   ps.setString(2, ncc.getTenNCC());
    		   ps.setString(3, ncc.getSdt());
    		   ps.setString(4, ncc.getEmail());
    		   ps.setString(5, ncc.getDiaChi());

    		   int rows = ps.executeUpdate();
    		   
    		   return rows > 0;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
       }
    public boolean update(Object[] data) {
    	   String sql =
		      "UPDATE NhaCungCap "
		    + "SET tenNCC = ?, sdt = ?, email = ?, diaChi = ? "
		    + "WHERE maNCC = ?";

    	    try (Connection con = ConnectDB.getConnection();
    	         PreparedStatement ps = con.prepareStatement(sql)) {

    	        ps.setString(1, data[1].toString());
    	        ps.setString(2, data[2].toString());
    	        ps.setString(3, data[3].toString());
    	        ps.setString(4, data[4].toString());
    	        ps.setString(5, data[0].toString());

    	        return ps.executeUpdate() > 0;

    	    } catch (SQLException e) {
    	        e.printStackTrace();
    	    }
    	    return false;
    	}

    public boolean delete(String maNCC) {
    	    String sql = "DELETE FROM NhaCungCap WHERE maNCC = ?";

    	    try (Connection con = ConnectDB.getConnection();
    	         PreparedStatement ps = con.prepareStatement(sql)) {

    	        ps.setString(1, maNCC);
    	        return ps.executeUpdate() > 0;

    	    } catch (SQLException e) {
    	        e.printStackTrace();
    	    }
    	    return false;
    	}

    public String getNewMaNCC() {
        String newID = "NCC001";
        String sql = "SELECT TOP 1 maNCC FROM NhaCungCap ORDER BY maNCC DESC";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                String lastID = rs.getString("maNCC");
                try {
                    int num = Integer.parseInt(lastID.substring(3)) + 1;
                    newID = String.format("NCC%03d", num);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newID;
    }

    public ArrayList<NhaCungCap> searchNhaCungCap(String keyword) {
        ArrayList<NhaCungCap> dsNCC = new ArrayList<>();
        String sql = "SELECT * FROM NhaCungCap WHERE tenNCC LIKE ? OR sdt LIKE ? OR email LIKE ?";
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
                dsNCC.add(mapResultSetToNhaCungCap(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsNCC;
    }

    public boolean hasPhieuNhap(String maNCC) {
        boolean hasPhieuNhap = false;
        String sql = "SELECT COUNT(*) AS count FROM PhieuNhap WHERE maNCC = ?";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maNCC);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                hasPhieuNhap = rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hasPhieuNhap;
    }

    private NhaCungCap mapResultSetToNhaCungCap(ResultSet rs) throws SQLException {
        String maNCC = rs.getString("maNCC");
        String tenNCC = rs.getString("tenNCC");
        String sdt = rs.getString("sdt");
        String email = rs.getString("email");
        String diaChi = rs.getString("diaChi");
        return new NhaCungCap(maNCC, tenNCC, sdt, email, diaChi);
    }
}
