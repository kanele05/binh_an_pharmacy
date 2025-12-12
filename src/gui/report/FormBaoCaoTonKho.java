package gui.report;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Color;
import java.awt.Component;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormBaoCaoTonKho extends JPanel {

    private JComboBox<String> cbNhomThuoc;
    private JComboBox<String> cbTrangThai;
    private JLabel lbTongGiaTri, lbTongSoLuong, lbThuocHetHan;
    private JTable table;
    private DefaultTableModel model;

    public FormBaoCaoTonKho() {
        initComponents();
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][][grow]"));

        add(createHeaderPanel(), "wrap 20");

        add(createFilterPanel(), "wrap 20");

        add(createSummaryPanel(), "wrap 20");

        add(createTablePanel(), "grow");

        loadDataMock();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow,fill][]"));
        panel.setOpaque(false);
        JLabel lbTitle = new JLabel("Báo Cáo Tồn Kho & Hạn Sử Dụng");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        panel.add(lbTitle);
        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10, fillx", "[]10[200!]20[]10[200!]5[]push[]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        cbNhomThuoc = new JComboBox<>(new String[]{"Tất cả nhóm", "Kháng sinh", "Giảm đau", "Vitamin", "Dụng cụ y tế"});
        cbTrangThai = new JComboBox<>(new String[]{"Tất cả", "Sắp hết hạn (< 3 tháng)", "Đã hết hạn", "Tồn kho thấp (< 10)", "Tồn kho cao (> 500)"});

        JButton btnLoc = new JButton("Lọc dữ liệu");
        btnLoc.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
        btnLoc.addActionListener(e -> actionFilter());

        JButton btnXuatExcel = new JButton("Xuất Excel");
        btnXuatExcel.putClientProperty(FlatClientProperties.STYLE, "background:#009688; foreground:#fff; font:bold");
        btnXuatExcel.addActionListener(e -> actionExport());

        panel.add(new JLabel("Nhóm thuốc:"));
        panel.add(cbNhomThuoc, "w 200!");
        panel.add(new JLabel("Trạng thái:"));
        panel.add(cbTrangThai, "w 200!");
        panel.add(btnLoc);
        panel.add(btnXuatExcel);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0, fillx", "[grow, fill]20[grow, fill]20[grow, fill]", "[100!]"));
        panel.setOpaque(false);

        lbTongGiaTri = new JLabel("0 ₫");
        panel.add(createCard("Tổng Giá Trị Kho", lbTongGiaTri, "#4CAF50"));

        lbTongSoLuong = new JLabel("0");
        panel.add(createCard("Tổng Số Lượng Tồn", lbTongSoLuong, "#2196F3"));

        lbThuocHetHan = new JLabel("0 Lô");
        panel.add(createCard("Lô Sắp/Đã Hết Hạn", lbThuocHetHan, "#F44336"));

        return panel;
    }

    private JPanel createCard(String title, JLabel lbValue, String colorHex) {
        JPanel card = new JPanel(new MigLayout("insets 20", "[]push[]", "[]5[]"));
        card.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:lighten(" + colorHex + ",10%);"
                + "border:0,0,0,0,shade(" + colorHex + ",5%),2");

        JLabel lbTitle = new JLabel(title);
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold; foreground:#fff");

        lbValue.putClientProperty(FlatClientProperties.STYLE, "font:bold +10; foreground:#fff");

        card.add(lbTitle, "wrap");
        card.add(lbValue, "span 2");
        return card;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createTitledBorder("Chi tiết tồn kho theo Lô"));

        String[] columns = {"Mã Thuốc", "Tên Thuốc", "Lô SX", "Hạn Dùng", "Đơn Giá Vốn", "Tồn Kho", "Tổng Giá Trị", "Ghi Chú"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        RightAlignRenderer rightAlign = new RightAlignRenderer();
        table.getColumnModel().getColumn(4).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(5).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(6).setCellRenderer(rightAlign);

        table.getColumnModel().getColumn(3).setCellRenderer(new ExpiryDateRenderer());

        panel.add(new JScrollPane(table));
        return panel;
    }

    private void loadDataMock() {

        model.addRow(new Object[]{"T001", "Paracetamol 500mg", "A101", "01/01/2023", "2.000 ₫", "500", "1.000.000 ₫", "Đã hết hạn"});
        model.addRow(new Object[]{"T002", "Vitamin C", "B202", "15/01/2024", "15.000 ₫", "20", "300.000 ₫", "Sắp hết hạn"});
        model.addRow(new Object[]{"T003", "Khẩu trang Y tế", "C303", "20/12/2025", "30.000 ₫", "1000", "30.000.000 ₫", ""});
        model.addRow(new Object[]{"T004", "Siro ho Prospan", "D404", "10/10/2024", "120.000 ₫", "5", "600.000 ₫", "Tồn kho thấp"});

        lbTongGiaTri.setText("31.900.000 ₫");
        lbTongSoLuong.setText("1.525");
        lbThuocHetHan.setText("2 Lô");
    }

    private void actionFilter() {
        String nhom = cbNhomThuoc.getSelectedItem().toString();
        String trangThai = cbTrangThai.getSelectedItem().toString();
        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER,
                "Đang lọc: " + nhom + " - " + trangThai);
    }

    private void actionExport() {
        Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Xuất báo cáo tồn kho thành công!");
    }

    private class RightAlignRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);
            return this;
        }
    }

    private class ExpiryDateRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String note = model.getValueAt(row, 7).toString();

            if (note.contains("Đã hết hạn")) {
                com.setForeground(new Color(211, 47, 47));
                com.setFont(com.getFont().deriveFont(java.awt.Font.BOLD));
            } else if (note.contains("Sắp hết hạn")) {
                com.setForeground(new Color(230, 81, 0));
                com.setFont(com.getFont().deriveFont(java.awt.Font.BOLD));
            } else {
                com.setForeground(Color.BLACK);
            }

            if (isSelected) {
                com.setForeground(Color.WHITE);
            }
            return com;
        }
    }

    @SuppressWarnings("unchecked")

    private void initComponents() {
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE)
        );
    }
}
