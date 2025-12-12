package gui.transaction.order;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormDoiHang extends JPanel {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private PanelDanhSachDoiHang panelDanhSach;
    private PanelTaoPhieuDoi panelTaoPhieu;

    public FormDoiHang() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        panelDanhSach = new PanelDanhSachDoiHang();
        panelTaoPhieu = new PanelTaoPhieuDoi();

        mainPanel.add(panelDanhSach, "LIST");
        mainPanel.add(panelTaoPhieu, "CREATE");

        add(mainPanel, BorderLayout.CENTER);
    }

    public void showTaoPhieu() {
        panelTaoPhieu.resetForm();
        cardLayout.show(mainPanel, "CREATE");
    }

    public void showDanhSach() {
        panelDanhSach.loadData();
        cardLayout.show(mainPanel, "LIST");
    }

    private class PanelDanhSachDoiHang extends JPanel {

        private JTable table;
        private DefaultTableModel model;

        public PanelDanhSachDoiHang() {
            setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow]"));

            JLabel lbTitle = new JLabel("Lịch Sử Đổi Hàng");
            lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
            add(lbTitle, "wrap 20");

            add(createToolBar(), "wrap 10");

            add(createTable(), "grow");

            loadData();
        }

        private JPanel createToolBar() {
            JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]push[]", "[]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");

            JTextField txtSearch = new JTextField();
            txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm mã phiếu, tên khách...");
            JButton btnTim = new JButton("Tìm kiếm");

            JButton btnTaoMoi = new JButton("➕ Lập phiếu đổi hàng");
            btnTaoMoi.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
            btnTaoMoi.addActionListener(e -> showTaoPhieu());

            panel.add(txtSearch, "w 250");
            panel.add(btnTim);
            panel.add(btnTaoMoi);

            return panel;
        }

        private JPanel createTable() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            String[] cols = {"Mã Phiếu", "Ngày Đổi", "Khách Hàng", "Chênh Lệch", "Trạng Thái", "Người Xử Lý"};
            model = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };

            table = new JTable(model);
            table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
            table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

            table.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer());

            panel.add(new JScrollPane(table));
            return panel;
        }

        public void loadData() {
            model.setRowCount(0);
            model.addRow(new Object[]{"DH005", "08/12/2023", "Nguyễn Văn A", "+ 50.000 ₫", "Hoàn tất", "Admin"});
            model.addRow(new Object[]{"DH004", "07/12/2023", "Trần Thị B", "- 120.000 ₫", "Hoàn tất", "NhanVien1"});
        }
    }

    private class PanelTaoPhieuDoi extends JPanel {

        private JTextField txtSearchHD;
        private JTable tableTra;
        private DefaultTableModel modelTra;
        private JLabel lbTongTra;

        private JTextField txtSearchThuoc;
        private JTable tableMua;
        private DefaultTableModel modelMua;
        private JLabel lbTongMua;

        private JLabel lbChenhLech;
        private JButton btnHoanTat;

        private double tongTienTra = 0;
        private double tongTienMua = 0;
        private boolean isUpdating = false;

        public PanelTaoPhieuDoi() {
            initCreateUI();
        }

        private void initCreateUI() {
            setLayout(new MigLayout("wrap,fill,insets 20", "[50%][50%]", "[][grow][]"));

            JPanel header = new JPanel(new MigLayout("insets 0", "[]10[]push[]"));
            header.setOpaque(false);

            JButton btnBack = new JButton(" Quay lại");
            btnBack.addActionListener(e -> showDanhSach());

            JLabel lbTitle = new JLabel("Đổi Hàng & Bù Trừ Chênh Lệch");
            lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");

            header.add(btnBack);
            header.add(lbTitle);

            add(header, "span 2, growx, wrap 20");

            add(createPanelTraHang(), "grow, h 100%");
            add(createPanelMuaHang(), "grow, h 100%");
            add(createFooterPanel(), "span 2, growx");
        }

        public void resetForm() {
            txtSearchHD.setText("");
            txtSearchThuoc.setText("");
            modelTra.setRowCount(0);
            modelMua.setRowCount(0);
            lbTongTra.setText("Giá trị trả: 0 ₫");
            lbTongMua.setText("Giá trị mới: 0 ₫");
            lbChenhLech.setText("Chênh lệch: 0 ₫");
            lbChenhLech.setForeground(Color.BLACK);
            tongTienTra = 0;
            tongTienMua = 0;
        }

        private JPanel createPanelTraHang() {
            JPanel panel = new JPanel(new MigLayout("insets 10, fill", "[grow][]", "[]10[grow][]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            panel.setBorder(BorderFactory.createTitledBorder("1. Hàng Khách Trả Lại"));

            txtSearchHD = new JTextField();
            txtSearchHD.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mã hóa đơn cũ...");
            JButton btnTim = new JButton("Tìm");
            btnTim.addActionListener(e -> actionTimHoaDon());

            panel.add(txtSearchHD, "growx");
            panel.add(btnTim, "wrap");

            String[] cols = {"Tên SP", "Đã mua", "SL Trả", "Giá gốc", "Thành tiền"};
            modelTra = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return col == 2;
                }
            };
            tableTra = new JTable(modelTra);
            tableTra.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
            tableTra.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer());
            tableTra.getColumnModel().getColumn(4).setCellRenderer(new RightAlignRenderer());
            modelTra.addTableModelListener(e -> {
                if (!isUpdating) {
                    tinhTienTra();
                }
            });

            panel.add(new JScrollPane(tableTra), "span 2, grow, wrap");

            lbTongTra = new JLabel("Giá trị trả: 0 ₫");
            lbTongTra.putClientProperty(FlatClientProperties.STYLE, "font:bold +2; foreground:#F57C00");
            panel.add(lbTongTra, "span 2, right");

            return panel;
        }

        private JPanel createPanelMuaHang() {
            JPanel panel = new JPanel(new MigLayout("insets 10, fill", "[grow][]", "[]10[grow][]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            panel.setBorder(BorderFactory.createTitledBorder("2. Chọn Hàng Đổi Mới"));

            txtSearchThuoc = new JTextField();
            txtSearchThuoc.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm thuốc mới...");
            JButton btnChon = new JButton("Chọn");
            btnChon.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff");
            btnChon.addActionListener(e -> actionChonThuocMoi());

            panel.add(txtSearchThuoc, "growx");
            panel.add(btnChon, "wrap");

            String[] cols = {"Tên thuốc mới", "SL", "Đơn giá", "Thành tiền"};
            modelMua = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return col == 1;
                }
            };
            tableMua = new JTable(modelMua);
            tableMua.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
            tableMua.getColumnModel().getColumn(2).setCellRenderer(new RightAlignRenderer());
            tableMua.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer());
            modelMua.addTableModelListener(e -> {
                if (!isUpdating) {
                    tinhTienMua();
                }
            });

            panel.add(new JScrollPane(tableMua), "span 2, grow, wrap");

            lbTongMua = new JLabel("Giá trị mới: 0 ₫");
            lbTongMua.putClientProperty(FlatClientProperties.STYLE, "font:bold +2; foreground:#1976D2");
            panel.add(lbTongMua, "span 2, right");

            return panel;
        }

        private JPanel createFooterPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 15", "[grow]push[]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:lighten(#E8F5E9,5%)");

            lbChenhLech = new JLabel("Chênh lệch: 0 ₫");
            lbChenhLech.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");

            btnHoanTat = new JButton("XÁC NHẬN ĐỔI HÀNG");
            btnHoanTat.putClientProperty(FlatClientProperties.STYLE, ""
                    + "background:#2196F3; foreground:#fff; font:bold +2; arc:10; margin:10,20,10,20");
            btnHoanTat.addActionListener(e -> actionHoanTat());

            panel.add(lbChenhLech);
            panel.add(btnHoanTat);
            return panel;
        }

        private void actionTimHoaDon() {
            if (txtSearchHD.getText().isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Nhập mã hóa đơn!");
                return;
            }
            isUpdating = true;
            modelTra.setRowCount(0);
            modelTra.addRow(new Object[]{"Siro Ho Prospan", 5, 0, "150.000", "0"});
            modelTra.addRow(new Object[]{"Vitamin C", 2, 0, "50.000", "0"});
            isUpdating = false;
            Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Đã tìm thấy hóa đơn!");
            tinhChenhLech();
        }

        private void actionChonThuocMoi() {
            isUpdating = true;
            modelMua.addRow(new Object[]{"Thuốc Cảm Cúm (Mới)", 1, "45.000", "45.000"});
            isUpdating = false;
            tinhTienMua();
        }

        private void tinhTienTra() {
            isUpdating = true;
            tongTienTra = 0;
            try {
                for (int i = 0; i < modelTra.getRowCount(); i++) {
                    int daMua = Integer.parseInt(modelTra.getValueAt(i, 1).toString());
                    int tra = Integer.parseInt(modelTra.getValueAt(i, 2).toString());
                    if (tra > daMua) {
                        tra = daMua;
                        modelTra.setValueAt(tra, i, 2);
                        Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Không thể trả quá số lượng mua!");
                    }
                    double gia = parseMoney(modelTra.getValueAt(i, 3).toString());
                    double thanhTien = tra * gia;
                    modelTra.setValueAt(formatMoney(thanhTien), i, 4);
                    tongTienTra += thanhTien;
                }
                lbTongTra.setText("Giá trị trả: " + formatMoney(tongTienTra));
                tinhChenhLech();
            } catch (Exception e) {
            } finally {
                isUpdating = false;
            }
        }

        private void tinhTienMua() {
            isUpdating = true;
            tongTienMua = 0;
            try {
                for (int i = 0; i < modelMua.getRowCount(); i++) {
                    int sl = Integer.parseInt(modelMua.getValueAt(i, 1).toString());
                    double gia = parseMoney(modelMua.getValueAt(i, 2).toString());
                    double thanhTien = sl * gia;
                    modelMua.setValueAt(formatMoney(thanhTien), i, 3);
                    tongTienMua += thanhTien;
                }
                lbTongMua.setText("Giá trị mới: " + formatMoney(tongTienMua));
                tinhChenhLech();
            } catch (Exception e) {
            } finally {
                isUpdating = false;
            }
        }

        private void tinhChenhLech() {
            double diff = tongTienMua - tongTienTra;
            if (diff > 0) {
                lbChenhLech.setText("Khách trả thêm: " + formatMoney(diff));
                lbChenhLech.setForeground(new Color(56, 142, 60));
            } else if (diff < 0) {
                lbChenhLech.setText("Hoàn tiền khách: " + formatMoney(Math.abs(diff)));
                lbChenhLech.setForeground(new Color(211, 47, 47));
            } else {
                lbChenhLech.setText("Chênh lệch: 0 ₫");
                lbChenhLech.setForeground(Color.BLACK);
            }
        }

        private void actionHoanTat() {
            if (tongTienTra == 0 && tongTienMua == 0) {
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Xác nhận đổi hàng?\n" + lbChenhLech.getText(),
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đổi hàng thành công!");
                showDanhSach();
            }
        }
    }

    private double parseMoney(String text) {
        try {
            return Double.parseDouble(text.replace(".", "").replace(",", "").replace(" ₫", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatMoney(double amount) {
        return new DecimalFormat("#,##0 ₫").format(amount);
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
