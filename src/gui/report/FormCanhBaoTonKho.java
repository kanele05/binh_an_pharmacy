package gui.report;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Color;
import java.awt.Component;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormCanhBaoTonKho extends JPanel {

    private JTextField txtTimKiem;
    private JComboBox<String> cbNhomThuoc;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lbCanhBao;

    public FormCanhBaoTonKho() {
        initComponents();
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow][]"));

        add(createHeaderPanel(), "wrap 20");

        add(createToolBarPanel(), "wrap 10");

        add(createTablePanel(), "grow");

        add(createFooterPanel(), "growx");

        loadData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow,fill]push[]"));
        panel.setOpaque(false);

        JLabel lbTitle = new JLabel("Cảnh Báo Tồn Kho Thấp & Hết Hàng");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");

        lbCanhBao = new JLabel("0 mã đã hết sạch hàng!");
        lbCanhBao.putClientProperty(FlatClientProperties.STYLE, ""
                + "font:bold +2;"
                + "foreground:#D32F2F;"
                + "background:#FFCDD2;"
                + "border:5,10,5,10;"
                + "arc:10");
        lbCanhBao.setOpaque(true);

        panel.add(lbTitle);
        panel.add(lbCanhBao);
        return panel;
    }

    private JPanel createToolBarPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]push[][]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        txtTimKiem = new JTextField();
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm tên thuốc, mã...");

        cbNhomThuoc = new JComboBox<>(new String[]{"Tất cả nhóm", "Kháng sinh", "Giảm đau", "Tiêu hóa", "Dụng cụ"});

        JButton btnTaoDuTru = new JButton("Tạo dự trù nhập hàng");
        btnTaoDuTru.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
        btnTaoDuTru.addActionListener(e -> actionTaoDuTru());

        JButton btnXuatExcel = new JButton("Xuất Excel");
        btnXuatExcel.putClientProperty(FlatClientProperties.STYLE, "background:#009688; foreground:#fff; font:bold");
        btnXuatExcel.addActionListener(e -> actionExport());

        panel.add(txtTimKiem, "w 250!");
        panel.add(cbNhomThuoc);

        panel.add(btnTaoDuTru);
        panel.add(btnXuatExcel);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] columns = {"Mã Thuốc", "Tên Thuốc", "ĐVT", "Định Mức Min", "Tồn Hiện Tại", "Gợi Ý Nhập", "Nhà Cung Cấp"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        table.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new RightAlignRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new RightAlignRenderer());

        table.getColumnModel().getColumn(4).setCellRenderer(new StockHighlightRenderer());

        panel.add(new JScrollPane(table));
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 5"));
        panel.setOpaque(false);

        JLabel note1 = new JLabel("■ Đỏ: Hết hàng (Tồn = 0)");
        note1.setForeground(new Color(211, 47, 47));

        JLabel note2 = new JLabel("■ Cam: Dưới định mức tối thiểu");
        note2.setForeground(new Color(230, 81, 0));

        panel.add(new JLabel("Chú thích:"));
        panel.add(note1, "gapleft 10");
        panel.add(note2, "gapleft 10");

        return panel;
    }

    private void loadData() {
        model.setRowCount(0);

        model.addRow(new Object[]{"T001", "Efferalgan 500mg", "Hộp", 50, 0, 100, "Dược Hậu Giang"});

        model.addRow(new Object[]{"T002", "Smecta (Thuốc bột)", "Hộp", 20, 5, 50, "Sanofi VN"});

        model.addRow(new Object[]{"T003", "Khẩu trang 4 lớp", "Hộp", 100, 15, 200, "Công ty May 10"});

        model.addRow(new Object[]{"T004", "Nước muối sinh lý", "Chai", 10, 2, 30, "Vimedimex"});

        countOutOfStock();
    }

    private void countOutOfStock() {
        int count = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            int ton = Integer.parseInt(model.getValueAt(i, 4).toString());
            if (ton == 0) {
                count++;
            }
        }

        if (count > 0) {
            lbCanhBao.setText("Nguy hiểm: " + count + " mặt hàng đã hết sạch trong kho!");
            lbCanhBao.setVisible(true);
        } else {
            lbCanhBao.setVisible(false);
        }
    }

    private void actionTaoDuTru() {

        if (model.getRowCount() == 0) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Hệ thống sẽ tự động tạo phiếu dự trù nhập hàng dựa trên số lượng 'Gợi Ý Nhập'.\n"
                + "Bạn có muốn tiếp tục sang màn hình Nhập Hàng?",
                "Tạo dự trù", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {

            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                    "Đã chuyển dữ liệu sang phân hệ Nhập Hàng!");
        }
    }

    private void actionExport() {
        Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã xuất file Excel cảnh báo!");
    }

    private class StockHighlightRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);

            try {
                int ton = Integer.parseInt(value.toString());

                int min = Integer.parseInt(model.getValueAt(row, 3).toString());

                if (ton == 0) {
                    com.setForeground(new Color(211, 47, 47));
                    com.setFont(com.getFont().deriveFont(java.awt.Font.BOLD));
                } else if (ton < min) {
                    com.setForeground(new Color(230, 81, 0));
                    com.setFont(com.getFont().deriveFont(java.awt.Font.BOLD));
                } else {
                    com.setForeground(Color.BLACK);
                }

                if (isSelected) {
                    com.setForeground(Color.WHITE);
                }

            } catch (Exception e) {
            }

            return com;
        }
    }

    private class RightAlignRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);
            return this;
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
