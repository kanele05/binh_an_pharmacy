package gui.transaction.inventory;

import com.formdev.flatlaf.FlatClientProperties;
import dao.ChiTietPhieuNhapDAO;
import dao.PhieuNhapDAO;
import entities.ChiTietPhieuNhap;
import entities.PhieuNhap;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;

public class DialogChiTietPhieuNhap extends JDialog {

    private String maPhieu;
    private PhieuNhapDAO phieuNhapDAO = new PhieuNhapDAO();
    private ChiTietPhieuNhapDAO chiTietDAO = new ChiTietPhieuNhapDAO();
    private DecimalFormat moneyFormat = new DecimalFormat("#,##0 ₫");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JLabel lbMaPhieu, lbNgayTao, lbNCC, lbNguoiNhap, lbTrangThai, lbGhiChu, lbTongTien;
    private JTable table;
    private DefaultTableModel model;

    public DialogChiTietPhieuNhap(Frame parent, String maPhieu) {
        super(parent, "Chi Tiết Phiếu Nhập - " + maPhieu, true);
        this.maPhieu = maPhieu;
        initUI();
        loadData();
        setSize(900, 600);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new MigLayout("wrap, fill, insets 20", "[fill]", "[][grow][]"));

        // Panel thông tin header
        add(createInfoPanel(), "wrap 15");

        // Panel bảng chi tiết
        add(createTablePanel(), "grow, wrap 15");

        // Panel footer
        add(createFooterPanel());
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 4, insets 15", "[right][grow,fill][right][grow,fill]", "[]10[]10[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:darken(@background,3%)");

        // Header title
        JLabel lbTitle = new JLabel("THÔNG TIN PHIẾU NHẬP");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +4");
        panel.add(lbTitle, "span 4, center, wrap 15");

        // Row 1
        panel.add(createLabel("Mã phiếu:"));
        lbMaPhieu = createValueLabel("");
        panel.add(lbMaPhieu);

        panel.add(createLabel("Ngày tạo:"));
        lbNgayTao = createValueLabel("");
        panel.add(lbNgayTao);

        // Row 2
        panel.add(createLabel("Nhà cung cấp:"));
        lbNCC = createValueLabel("");
        panel.add(lbNCC);

        panel.add(createLabel("Người nhập:"));
        lbNguoiNhap = createValueLabel("");
        panel.add(lbNguoiNhap);

        // Row 3
        panel.add(createLabel("Trạng thái:"));
        lbTrangThai = createValueLabel("");
        panel.add(lbTrangThai);

        panel.add(createLabel("Ghi chú:"));
        lbGhiChu = createValueLabel("");
        panel.add(lbGhiChu);

        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel lb = new JLabel(text);
        lb.putClientProperty(FlatClientProperties.STYLE, "font:bold");
        return lb;
    }

    private JLabel createValueLabel(String text) {
        JLabel lb = new JLabel(text);
        return lb;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table header
        JLabel lbTableTitle = new JLabel("DANH SÁCH THUỐC NHẬP");
        lbTableTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        lbTableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(lbTableTitle, BorderLayout.NORTH);

        // Table
        String[] columns = {"STT", "Mã thuốc", "Tên thuốc", "ĐVT", "Mã lô", "Hạn SD", "Số lượng", "Đơn giá", "Thành tiền"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:28; showHorizontalLines:true; gridColor:#e0e0e0");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:32; font:bold");

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(40);  // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(80);  // Mã thuốc
        table.getColumnModel().getColumn(2).setPreferredWidth(180); // Tên thuốc
        table.getColumnModel().getColumn(3).setPreferredWidth(60);  // ĐVT
        table.getColumnModel().getColumn(4).setPreferredWidth(70);  // Mã lô
        table.getColumnModel().getColumn(5).setPreferredWidth(90);  // Hạn SD
        table.getColumnModel().getColumn(6).setPreferredWidth(70);  // Số lượng
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Đơn giá
        table.getColumnModel().getColumn(8).setPreferredWidth(120); // Thành tiền

        // Right align for numbers
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);

        // Center align for STT
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10", "[grow][]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:lighten(#E8F5E9,3%)");

        // Tổng tiền
        JPanel totalPanel = new JPanel(new MigLayout("insets 0", "[]10[]", "[]"));
        totalPanel.setOpaque(false);
        JLabel lbTotalLabel = new JLabel("TỔNG TIỀN:");
        lbTotalLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +4");
        lbTongTien = new JLabel("0 ₫");
        lbTongTien.putClientProperty(FlatClientProperties.STYLE, "font:bold +6; foreground:#D32F2F");
        totalPanel.add(lbTotalLabel);
        totalPanel.add(lbTongTien);

        panel.add(totalPanel, "grow");

        // Button đóng
        JButton btnDong = new JButton("Đóng");
        btnDong.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:#607D8B;"
                + "foreground:#ffffff;"
                + "font:bold;"
                + "borderWidth:0;"
                + "arc:8");
        btnDong.addActionListener(e -> dispose());
        panel.add(btnDong, "w 100, h 40");

        return panel;
    }

    private void loadData() {
        // Load thông tin phiếu nhập
        PhieuNhap pn = phieuNhapDAO.getPhieuNhapById(maPhieu);
        if (pn != null) {
            lbMaPhieu.setText(pn.getMaPN());
            lbNgayTao.setText(pn.getNgayTao() != null ? pn.getNgayTao().format(dateFormat) : "");
            lbNCC.setText(pn.getNcc() != null ? pn.getNcc().getTenNCC() : "");
            lbNguoiNhap.setText(pn.getNhanVien() != null ? pn.getNhanVien().getHoTen() : "");
            lbTrangThai.setText(pn.getTrangThai());
            lbGhiChu.setText(pn.getGhiChu() != null ? pn.getGhiChu() : "");
            lbTongTien.setText(moneyFormat.format(pn.getTongTien()));

            // Style trạng thái
            String status = pn.getTrangThai();
            if (status != null) {
                if (status.contains("Đã nhập")) {
                    lbTrangThai.setForeground(new Color(56, 142, 60));
                } else if (status.contains("hủy")) {
                    lbTrangThai.setForeground(new Color(211, 47, 47));
                } else {
                    lbTrangThai.setForeground(new Color(255, 152, 0));
                }
            }
        }

        // Load chi tiết phiếu nhập
        List<ChiTietPhieuNhap> listCT = chiTietDAO.getChiTietByMaPhieu(maPhieu);
        model.setRowCount(0);
        int stt = 1;
        for (ChiTietPhieuNhap ct : listCT) {
            model.addRow(new Object[]{
                stt++,
                ct.getThuoc() != null ? ct.getThuoc().getMaThuoc() : "",
                ct.getThuoc() != null ? ct.getThuoc().getTenThuoc() : "",
                ct.getDonViTinh() != null ? ct.getDonViTinh() : (ct.getThuoc() != null ? ct.getThuoc().getDonViTinh() : ""),
                ct.getLoThuoc() != null ? ct.getLoThuoc().getMaLo() : "",
                ct.getHanSuDung() != null ? ct.getHanSuDung().format(dateFormat) : "",
                ct.getSoLuong(),
                moneyFormat.format(ct.getDonGia()),
                moneyFormat.format(ct.getThanhTien())
            });
        }
    }
}
