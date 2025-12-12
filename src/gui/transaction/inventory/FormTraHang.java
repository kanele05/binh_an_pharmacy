package gui.transaction.inventory;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormTraHang extends JPanel {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private PanelDanhSachPhieuTra panelDanhSach;
    private PanelTaoPhieuTra panelTaoPhieu;

    public FormTraHang() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        panelDanhSach = new PanelDanhSachPhieuTra();
        panelTaoPhieu = new PanelTaoPhieuTra();

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

    private class PanelDanhSachPhieuTra extends JPanel {

        private JTable table;
        private DefaultTableModel model;

        public PanelDanhSachPhieuTra() {
            setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow]"));

            JLabel lbTitle = new JLabel("Lịch Sử Trả Hàng");
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
            txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm mã phiếu trả, tên khách...");
            JButton btnTim = new JButton("Tìm kiếm");

            JButton btnTaoMoi = new JButton("➕ Lập phiếu trả hàng");
            btnTaoMoi.putClientProperty(FlatClientProperties.STYLE, "background:#F44336; foreground:#fff; font:bold");
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

            String[] cols = {"Mã Phiếu", "Ngày Trả", "Khách Hàng", "Tiền Hoàn", "Lý Do", "Người Xử Lý"};
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
            model.addRow(new Object[]{"PT001", "08/12/2023", "Nguyễn Văn A", "50.000 ₫", "Hết hạn sử dụng", "Admin"});
            model.addRow(new Object[]{"PT002", "07/12/2023", "Trần Thị B", "120.000 ₫", "Mua nhầm loại", "NhanVien1"});
        }
    }

    private class PanelTaoPhieuTra extends JPanel {

        private JTextField txtSearch;
        private JLabel lbKhachHang, lbNgayMua, lbTongHoaDon;
        private JTable tableHoaDon, tableTraHang;
        private DefaultTableModel modelHoaDon, modelTraHang;
        private JLabel lbTienHoan;
        private JTextArea txtLyDo;
        private double tongTienHoan = 0;

        public PanelTaoPhieuTra() {
            initCreateUI();
        }

        private void initCreateUI() {
            setLayout(new MigLayout("wrap,fill,insets 20", "[50%][50%]", "[][][grow][]"));

            JPanel header = new JPanel(new MigLayout("insets 0", "[]10[]push[]"));
            header.setOpaque(false);

            JButton btnBack = new JButton(" Quay lại");
            btnBack.addActionListener(e -> showDanhSach());

            JLabel lbTitle = new JLabel("Tiếp Nhận Trả Hàng");
            lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");

            header.add(btnBack);
            header.add(lbTitle);

            add(header, "span 2, growx, wrap 20");

            add(createSearchPanel(), "grow, span 1 2, w 50%");
            add(createReturnCartPanel(), "grow, span 1 2, w 50%, wrap");
            add(createFooterPanel(), "span 2, growx");
        }

        public void resetForm() {
            txtSearch.setText("");
            lbKhachHang.setText("Khách hàng: ---");
            lbNgayMua.setText("Ngày mua: ---");
            lbTongHoaDon.setText("Tổng cũ: 0 ₫");
            modelHoaDon.setRowCount(0);
            modelTraHang.setRowCount(0);
            txtLyDo.setText("");
            lbTienHoan.setText("0 ₫");
            tongTienHoan = 0;
        }

        private JPanel createSearchPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 15, fillx", "[grow][]", "[]10[]10[grow]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            panel.setBorder(BorderFactory.createTitledBorder("1. Tìm hóa đơn mua hàng"));

            txtSearch = new JTextField();
            txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mã hóa đơn hoặc SĐT khách...");
            JButton btnTim = new JButton("Tìm hóa đơn");
            btnTim.addActionListener(e -> actionTimHoaDon());

            panel.add(txtSearch, "growx");
            panel.add(btnTim, "wrap");

            JPanel infoPanel = new JPanel(new MigLayout("insets 10", "[]20[]"));
            infoPanel.putClientProperty(FlatClientProperties.STYLE, "background:lighten(@background,5%); arc:10");
            lbKhachHang = new JLabel("Khách hàng: ---");
            lbNgayMua = new JLabel("Ngày mua: ---");
            lbTongHoaDon = new JLabel("Tổng cũ: 0 ₫");
            infoPanel.add(lbKhachHang);
            infoPanel.add(lbNgayMua, "wrap");
            infoPanel.add(lbTongHoaDon, "span 2");

            panel.add(infoPanel, "span 2, growx, wrap");

            String[] cols = {"Mã SP", "Tên SP", "SL Mua", "Đơn giá", "Thao tác"};
            modelHoaDon = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return col == 4;
                }
            };
            tableHoaDon = new JTable(modelHoaDon);
            tableHoaDon.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
            tableHoaDon.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer());

            JButton btnChonTra = new JButton("Trả món đang chọn >>");
            btnChonTra.addActionListener(e -> actionChonTraHang());

            panel.add(new JLabel("Chi tiết hóa đơn:"), "span 2, wrap");
            panel.add(new JScrollPane(tableHoaDon), "span 2, grow");
            panel.add(btnChonTra, "span 2, right");

            return panel;
        }

        private JPanel createReturnCartPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 15, fill", "[grow]", "[][grow][]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            panel.setBorder(BorderFactory.createTitledBorder("2. Danh sách hàng nhận lại"));

            String[] cols = {"Tên SP", "SL Trả", "Hoàn tiền", "Thành tiền"};
            modelTraHang = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            tableTraHang = new JTable(modelTraHang);
            tableTraHang.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
            tableTraHang.getColumnModel().getColumn(2).setCellRenderer(new RightAlignRenderer());
            tableTraHang.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer());

            JButton btnXoa = new JButton("Xóa dòng");
            btnXoa.putClientProperty(FlatClientProperties.STYLE, "background:#FFCDD2; foreground:#C62828");
            btnXoa.addActionListener(e -> actionXoaDongTra());

            panel.add(new JScrollPane(tableTraHang), "grow, wrap");
            panel.add(btnXoa, "right");

            return panel;
        }

        private JPanel createFooterPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 15", "[grow][right]", "[]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:lighten(#E8F5E9,5%)");

            txtLyDo = new JTextArea(3, 0);
            txtLyDo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập lý do khách trả hàng...");
            JScrollPane scrollNote = new JScrollPane(txtLyDo);

            JPanel pTotal = new JPanel(new MigLayout("insets 0", "[]10[]"));
            pTotal.setOpaque(false);
            lbTienHoan = new JLabel("0 ₫");
            lbTienHoan.putClientProperty(FlatClientProperties.STYLE, "font:bold +10; foreground:#D32F2F");

            JButton btnHoanTat = new JButton("HOÀN TẤT & IN PHIẾU");
            btnHoanTat.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold +2; margin:10,20,10,20");
            btnHoanTat.addActionListener(e -> actionHoanTatTraHang());

            pTotal.add(new JLabel("TỔNG TIỀN HOÀN KHÁCH:"));
            pTotal.add(lbTienHoan, "wrap");
            pTotal.add(btnHoanTat, "span 2, right");

            panel.add(new JLabel("Ghi chú / Lý do:"), "wrap");
            panel.add(scrollNote, "growx, h 80!");
            panel.add(pTotal);

            return panel;
        }

        private void actionTimHoaDon() {
            String keyword = txtSearch.getText().trim();
            if (keyword.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng nhập mã HĐ hoặc SĐT!");
                return;
            }

            Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Đã tìm thấy hóa đơn " + keyword);
            lbKhachHang.setText("Khách hàng: Nguyễn Văn A - 0909123456");
            lbNgayMua.setText("Ngày mua: 05/12/2023");
            lbTongHoaDon.setText("Tổng cũ: 560.000 ₫");
            modelHoaDon.setRowCount(0);
            modelTraHang.setRowCount(0);
            updateTongTien();
            modelHoaDon.addRow(new Object[]{"SP001", "Paracetamol 500mg", 5, "5.000"});
            modelHoaDon.addRow(new Object[]{"SP002", "Vitamin C Lọ", 2, "30.000"});
        }

        private void actionChonTraHang() {
            int row = tableHoaDon.getSelectedRow();
            if (row == -1) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Chọn sản phẩm bên trái để trả!");
                return;
            }
            String tenSP = modelHoaDon.getValueAt(row, 1).toString();
            int slDaMua = Integer.parseInt(modelHoaDon.getValueAt(row, 2).toString());
            double donGia = parseMoney(modelHoaDon.getValueAt(row, 3).toString());

            String input = JOptionPane.showInputDialog(this, "Khách đã mua: " + slDaMua + "\nNhập số lượng muốn trả:", "1");
            if (input == null) {
                return;
            }
            try {
                int slTra = Integer.parseInt(input);
                if (slTra <= 0 || slTra > slDaMua) {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Số lượng không hợp lệ!");
                    return;
                }
                double thanhTienHoan = slTra * donGia;
                modelTraHang.addRow(new Object[]{tenSP, slTra, formatMoney(donGia), formatMoney(thanhTienHoan)});
                updateTongTien();
            } catch (NumberFormatException e) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Vui lòng nhập số!");
            }
        }

        private void actionXoaDongTra() {
            int row = tableTraHang.getSelectedRow();
            if (row != -1) {
                modelTraHang.removeRow(row);
                updateTongTien();
            }
        }

        private void updateTongTien() {
            tongTienHoan = 0;
            for (int i = 0; i < modelTraHang.getRowCount(); i++) {
                tongTienHoan += parseMoney(modelTraHang.getValueAt(i, 3).toString());
            }
            lbTienHoan.setText(formatMoney(tongTienHoan));
        }

        private void actionHoanTatTraHang() {
            if (modelTraHang.getRowCount() == 0) {
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận hoàn tiền: " + lbTienHoan.getText() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã xử lý trả hàng thành công!");
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
