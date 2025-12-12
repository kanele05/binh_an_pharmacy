package gui.report;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormCanhBaoHetHan extends JPanel {

    private JComboBox<String> cbKhoangThoiGian;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lbCanhBaoDo;

    public FormCanhBaoHetHan() {
        initComponents();
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow][]"));

        add(createHeaderPanel(), "wrap 20");

        add(createActionPanel(), "wrap 10");

        add(createTablePanel(), "grow");

        add(createFooterPanel(), "growx");

        loadData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow,fill]push[]"));
        panel.setOpaque(false);

        JLabel lbTitle = new JLabel("Cảnh Báo Thuốc Sắp Hết Hạn");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");

        lbCanhBaoDo = new JLabel("0 lô cần xử lý gấp!");
        lbCanhBaoDo.putClientProperty(FlatClientProperties.STYLE, ""
                + "font:bold +2;"
                + "foreground:#D32F2F;"
                + "background:#FFCDD2;"
                + "border:5,10,5,10;"
                + "arc:10");
        lbCanhBaoDo.setOpaque(true);

        panel.add(lbTitle);
        panel.add(lbCanhBaoDo);
        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]push[][]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        cbKhoangThoiGian = new JComboBox<>(new String[]{
            "Hết hạn trong 30 ngày (Gấp)",
            "Hết hạn trong 3 tháng",
            "Hết hạn trong 6 tháng",
            "Đã hết hạn (Cần hủy)"
        });
        cbKhoangThoiGian.addActionListener(e -> filterData());

        JButton btnTaoPhieuHuy = new JButton("Tạo phiếu hủy hàng");
        btnTaoPhieuHuy.putClientProperty(FlatClientProperties.STYLE, "background:#D32F2F; foreground:#fff; font:bold");
        btnTaoPhieuHuy.addActionListener(e -> actionTaoPhieuHuy());

        JButton btnTraNCC = new JButton("Trả nhà cung cấp");
        btnTraNCC.addActionListener(e -> actionTraNCC());

        panel.add(new JLabel("Lọc theo thời gian:"));
        panel.add(cbKhoangThoiGian);

        panel.add(btnTraNCC);
        panel.add(btnTaoPhieuHuy);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] columns = {"Mã Thuốc", "Tên Thuốc", "Lô SX", "Hạn Sử Dụng", "Còn Lại (Ngày)", "Tồn Kho", "Đơn Giá Vốn", "Hành Động Gợi Ý"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        table.getColumnModel().getColumn(4).setCellRenderer(new CenterAlignRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new RightAlignRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new RightAlignRenderer());

        table.getColumnModel().getColumn(3).setCellRenderer(new ExpiryHighlightRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new ExpiryHighlightRenderer());

        panel.add(new JScrollPane(table));
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 5"));
        panel.setOpaque(false);

        JLabel note1 = new JLabel("■ Đỏ: Đã hết hạn hoặc < 30 ngày");
        note1.setForeground(new Color(211, 47, 47));

        JLabel note2 = new JLabel("■ Cam: Còn 1 - 3 tháng");
        note2.setForeground(new Color(230, 81, 0));

        JLabel note3 = new JLabel("■ Vàng: Còn 3 - 6 tháng");
        note3.setForeground(new Color(245, 127, 23));

        panel.add(new JLabel("Chú thích màu sắc:"));
        panel.add(note1, "gapleft 10");
        panel.add(note2, "gapleft 10");
        panel.add(note3, "gapleft 10");

        return panel;
    }

    private void loadData() {
        model.setRowCount(0);
        LocalDate today = LocalDate.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        addMockRow("T001", "Paracetamol 500mg", "A101", today.minusDays(5), 50, 2000);
        addMockRow("T002", "Vitamin C", "B202", today.plusDays(20), 120, 15000);
        addMockRow("T003", "Siro Ho", "C303", today.plusDays(80), 30, 45000);
        addMockRow("T004", "Men vi sinh", "D404", today.plusDays(150), 200, 12000);

        countCriticalItems();
    }

    private void addMockRow(String ma, String ten, String lo, LocalDate hsd, int ton, double gia) {
        LocalDate today = LocalDate.now();
        long daysLeft = ChronoUnit.DAYS.between(today, hsd);
        String suggestion = "";

        if (daysLeft < 0) {
            suggestion = "Hủy bỏ ngay";
        } else if (daysLeft < 30) {
            suggestion = "Giảm giá sâu / Trả hàng";
        } else if (daysLeft < 90) {
            suggestion = "Khuyến mãi";
        } else {
            suggestion = "Theo dõi";
        }

        model.addRow(new Object[]{
            ma, ten, lo,
            hsd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            daysLeft,
            ton,
            String.format("%,.0f ₫", gia),
            suggestion
        });
    }

    private void countCriticalItems() {
        int count = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            long days = Long.parseLong(model.getValueAt(i, 4).toString());
            if (days < 30) {
                count++;
            }
        }
        lbCanhBaoDo.setText(count + " lô cần xử lý gấp (< 30 ngày)!");
        if (count == 0) {
            lbCanhBaoDo.setVisible(false);
        } else {
            lbCanhBaoDo.setVisible(true);
        }
    }

    private void filterData() {
        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER,
                "Đang lọc: " + cbKhoangThoiGian.getSelectedItem());

    }

    private void actionTaoPhieuHuy() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Chọn các lô thuốc cần hủy!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tạo phiếu xuất hủy cho " + rows.length + " lô thuốc đã chọn?\nHành động này sẽ trừ tồn kho về 0.",
                "Xác nhận hủy hàng", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã tạo phiếu hủy (Mã: PXH001)!");

            for (int i = rows.length - 1; i >= 0; i--) {
                model.removeRow(rows[i]);
            }
            countCriticalItems();
        }
    }

    private void actionTraNCC() {
        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Chức năng đang phát triển: Tạo phiếu xuất trả NCC.");
    }

    private class ExpiryHighlightRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            try {
                long days = Long.parseLong(model.getValueAt(row, 4).toString());

                if (days < 0) {
                    com.setForeground(new Color(211, 47, 47));
                    com.setFont(com.getFont().deriveFont(java.awt.Font.BOLD));
                } else if (days <= 30) {
                    com.setForeground(new Color(211, 47, 47));
                } else if (days <= 90) {
                    com.setForeground(new Color(230, 81, 0));
                } else if (days <= 180) {
                    com.setForeground(new Color(245, 127, 23));
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

    private class CenterAlignRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.CENTER);
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
