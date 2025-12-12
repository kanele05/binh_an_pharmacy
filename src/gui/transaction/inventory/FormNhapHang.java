package gui.transaction.inventory;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Random;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormNhapHang extends javax.swing.JPanel {

    private JComboBox<String> cbNhaCungCap;
    private JTextField txtNguoiNhap;
    private JTextField txtGhiChu;
    private JLabel lbTongTien;
    private JTable table;
    private DefaultTableModel model;

    private boolean isUpdating = false;

    public FormNhapHang() {
        initComponents();
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow][]"));

        add(createHeaderPanel(), "wrap 20");

        add(createActionPanel(), "wrap 10");

        add(createTablePanel(), "grow");

        add(createFooterPanel(), "growx");
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow,fill][]"));
        panel.setOpaque(false);
        JLabel lbTitle = new JLabel("Nhập Hàng & Quản Lý Lô");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        panel.add(lbTitle);
        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 15", "[][grow]push[][]", "[]10[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        cbNhaCungCap = new JComboBox<>(new String[]{"Công ty Dược Hậu Giang", "Sanofi Việt Nam", "Zuellig Pharma", "Nhà cung cấp khác..."});
        txtNguoiNhap = new JTextField("Admin");
        txtNguoiNhap.setEditable(false);

        panel.add(new JLabel("Nhà cung cấp:"));
        panel.add(cbNhaCungCap, "w 300, wrap");

        panel.add(new JLabel("Người nhập:"));
        panel.add(txtNguoiNhap, "w 300");

        JButton btnMau = new JButton("Tải file mẫu");
        btnMau.addActionListener(e -> actionDownloadTemplate());
        JButton btnImport = new JButton("Nhập từ Excel");
        btnImport.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:#009688;"
                + "foreground:#ffffff;"
                + "font:bold;"
                + "iconTextGap:10");

        btnImport.addActionListener(e -> actionImportExcel());

        panel.add(btnMau, "cell 2 0, span 1 2, right");
        panel.add(btnImport, "cell 3 0, span 1 2");

        return panel;
    }

    private void actionDownloadTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file mẫu nhập hàng");
        fileChooser.setSelectedFile(new File("Mau_Nhap_Hang.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            saveCSVFile(fileToSave);
        }
    }

    private void saveCSVFile(File file) {
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {

            writer.write("\ufeff");

            writer.write("Mã thuốc,Tên thuốc,Đơn vị tính,Lô sản xuất,Hạn sử dụng,Số lượng,Giá nhập");
            writer.newLine();

            writer.write("T001,Paracetamol 500mg,Vỉ,A123,12/2025,100,3500");
            writer.newLine();

            writer.write("T002,Amoxicillin 500mg,Hộp,B456,06/2026,50,45000");
            writer.newLine();

            writer.write("T003,Vitamin C 1000mg,Lọ,C789,01/2025,200,25000");
            writer.newLine();

            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã lưu file mẫu thành công!");

            try {
                java.awt.Desktop.getDesktop().open(file);
            } catch (Exception e) {

            }

        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi khi lưu file!");
        }
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] columns = {
            "Mã thuốc", "Tên thuốc", "ĐVT",
            "Lô SX", "Hạn SD",
            "Số lượng", "Giá nhập", "Thành tiền"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {

                return column == 3 || column == 4 || column == 5 || column == 6;
            }
        };

        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true; gridColor:#e0e0e0");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        table.getColumnModel().getColumn(5).setCellRenderer(new RightAlignRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new RightAlignRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new RightAlignRenderer());

        model.addTableModelListener(e -> {
            if (!isUpdating) {
                tinhTienHang();
            }
        });

        panel.add(new JScrollPane(table));
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 15, fillx", "[grow][][200!]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:lighten(#E8F5E9,3%)");

        txtGhiChu = new JTextField();
        txtGhiChu.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ghi chú phiếu nhập...");

        lbTongTien = new JLabel("0 ₫");
        lbTongTien.putClientProperty(FlatClientProperties.STYLE, "font:bold +10; foreground:#D32F2F");

        JButton btnLuu = new JButton("LƯU PHIẾU NHẬP");
        btnLuu.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:#4CAF50;"
                + "foreground:#ffffff;"
                + "font:bold +2;"
                + "borderWidth:0;"
                + "arc:10");
        btnLuu.addActionListener(e -> actionLuuPhieu());

        panel.add(new JLabel("Ghi chú:"), "split 2, gapright 10");
        panel.add(txtGhiChu, "growx");

        panel.add(new JLabel("TỔNG CỘNG:"), "right");
        panel.add(lbTongTien, "right, wrap");

        panel.add(btnLuu, "span 3, growx, h 50!");

        return panel;
    }

    private class RightAlignRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);
            return this;
        }
    }

    private void actionImportExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file Excel nhập hàng");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xlsx", "xls"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            readExcelSimulated(file);
        }
    }

    private void readExcelSimulated(File file) {
        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Đang đọc file: " + file.getName());

        new Thread(() -> {
            try {
                Thread.sleep(1000);

                SwingUtilities.invokeLater(() -> {

                    model.setRowCount(0);
                    isUpdating = true;

                    Object[][] mockData = {
                        {"T005", "Panadol Extra", "Vỉ", "A101", "12/2025", "100", "12000"},
                        {"T008", "Berberin", "Lọ", "B202", "06/2026", "50", "8500"},
                        {"T012", "Vitamin C 500mg", "Hộp", "C303", "01/2025", "200", "25000"},
                        {"T025", "Khẩu trang Y tế", "Hộp", "KT01", "12/2028", "50", "35000"}
                    };

                    for (Object[] row : mockData) {
                        double sl = Double.parseDouble(row[5].toString());
                        double gia = Double.parseDouble(row[6].toString());
                        double thanhTien = sl * gia;

                        model.addRow(new Object[]{
                            row[0], row[1], row[2], row[3], row[4],
                            row[5],
                            formatMoney(gia),
                            formatMoney(thanhTien)
                        });
                    }

                    isUpdating = false;
                    tinhTienHang();

                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã import dữ liệu thành công!");
                });

            } catch (InterruptedException e) {
            }
        }).start();
    }

    private void tinhTienHang() {
        isUpdating = true;
        double tongCong = 0;
        try {
            for (int i = 0; i < model.getRowCount(); i++) {
                try {

                    String slStr = model.getValueAt(i, 5).toString();
                    String giaStr = model.getValueAt(i, 6).toString().replace(".", "").replace(",", "").replace(" ₫", "");

                    double sl = Double.parseDouble(slStr);
                    double gia = Double.parseDouble(giaStr);
                    double thanhTien = sl * gia;

                    model.setValueAt(formatMoney(thanhTien), i, 7);

                    tongCong += thanhTien;
                } catch (Exception e) {

                }
            }
            lbTongTien.setText(formatMoney(tongCong));
        } finally {
            isUpdating = false;
        }
    }

    private void actionLuuPhieu() {
        if (model.getRowCount() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Danh sách nhập đang trống!");
            return;
        }

        for (int i = 0; i < model.getRowCount(); i++) {
            String lo = model.getValueAt(i, 3).toString();
            String hsd = model.getValueAt(i, 4).toString();
            if (lo.trim().isEmpty() || hsd.trim().isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                        "Dòng " + (i + 1) + " thiếu Lô SX hoặc Hạn SD!");
                table.setRowSelectionInterval(i, i);
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận nhập kho với tổng tiền: " + lbTongTien.getText() + "?\nHệ thống sẽ cập nhật tồn kho theo từng Lô.",
                "Lưu Phiếu Nhập", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {

            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Nhập kho thành công!");
            model.setRowCount(0);
            lbTongTien.setText("0 ₫");
            txtGhiChu.setText("");
        }
    }

    private String formatMoney(double money) {
        return new DecimalFormat("#,##0 ₫").format(money);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
