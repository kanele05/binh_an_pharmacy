package gui.manage.partner;

import com.formdev.flatlaf.FlatClientProperties;
import dao.HoaDonDAO;
import entities.HoaDon;
import entities.KhachHang;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;

public class DialogLichSuMuaHang extends JDialog {
    
    private KhachHang khachHang;
    private JTable table;
    private DefaultTableModel model;
    private HoaDonDAO hoaDonDAO;
    private JLabel lblTongHoaDon, lblTongChiTieu;
    
    public DialogLichSuMuaHang(Component parent, KhachHang kh) {
        super(SwingUtilities.getWindowAncestor(parent), "Lịch sử mua hàng", ModalityType.APPLICATION_MODAL);
        this.khachHang = kh;
        this.hoaDonDAO = new HoaDonDAO();
        initComponents();
        loadData();
        setSize(900, 500);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][grow][]"));
        
        // Header với thông tin khách hàng
        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[]"));
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Lịch sử mua hàng của: " + khachHang.getTenKH());
        lblTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +4");
        JLabel lblSDT = new JLabel("SĐT: " + khachHang.getSdt() + " | Điểm tích lũy: " + khachHang.getDiemTichLuy());
        headerPanel.add(lblTitle, "wrap");
        headerPanel.add(lblSDT);
        add(headerPanel);
        
        // Table
        String[] columns = {"Mã HĐ", "Ngày mua", "Tổng tiền", "Giảm giá", "Thuế", "Thực thu", "Hình thức TT", "Nhân viên"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:28; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:32; font:bold");
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, "grow");
        
        // Footer với tổng kết
        JPanel footerPanel = new JPanel(new MigLayout("insets 10", "[]push[][]"));
        footerPanel.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:darken(@background,3%)");
        lblTongHoaDon = new JLabel("Tổng số hóa đơn: 0");
        lblTongChiTieu = new JLabel("Tổng chi tiêu: 0đ");
        lblTongChiTieu.putClientProperty(FlatClientProperties.STYLE, "font:bold +2; foreground:#4CAF50");
        
        JButton btnDong = new JButton("Đóng");
        btnDong.addActionListener(e -> dispose());
        
        footerPanel.add(lblTongHoaDon);
        footerPanel.add(lblTongChiTieu);
        footerPanel.add(btnDong);
        add(footerPanel);
    }
    
    private void loadData() {
        model.setRowCount(0);
        ArrayList<HoaDon> dsHD = hoaDonDAO.getHoaDonByKhachHang(khachHang.getMaKH());
        
        DecimalFormat df = new DecimalFormat("#,###");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        double tongChiTieu = 0;
        
        for (HoaDon hd : dsHD) {
            double thucThu = hd.getTongTien() + hd.getThue() - hd.getGiamGia();
            tongChiTieu += thucThu;
            
            model.addRow(new Object[]{
                hd.getMaHD(),
                hd.getNgayTao().format(dtf),
                df.format(hd.getTongTien()) + "đ",
                df.format(hd.getGiamGia()) + "đ",
                df.format(hd.getThue()) + "đ",
                df.format(thucThu) + "đ",
                hd.getHinhThucTT(),
                hd.getNhanVien() != null ? hd.getNhanVien().getHoTen() : ""
            });
        }
        
        lblTongHoaDon.setText("Tổng số hóa đơn: " + dsHD.size());
        lblTongChiTieu.setText("Tổng chi tiêu: " + df.format(tongChiTieu) + "đ");
    }
}
