package gui.transaction.inventory;

import com.formdev.flatlaf.FlatClientProperties;
import dao.ChiTietPhieuNhapDAO;
import dao.PhieuNhapDAO;
import entities.ChiTietPhieuNhap;
import entities.PhieuNhap;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;
import utils.ImportReceiptPDFGenerator;

public class FormDanhSachPhieuNhap extends javax.swing.JPanel {

    private JTextField txtTimKiem;
    private JComboBox<String> cbTrangThai;
    private JComboBox<String> cbThoiGian;
    private JTable table;
    private DefaultTableModel model;
    private PhieuNhapDAO phieuNhapDAO = new PhieuNhapDAO();
    private ChiTietPhieuNhapDAO chiTietDAO = new ChiTietPhieuNhapDAO();
    private DecimalFormat moneyFormat = new DecimalFormat("#,##0 ₫");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FormDanhSachPhieuNhap() {
        initComponents();
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow]"));

        add(createHeaderPanel(), "wrap 20");

        add(createToolBarPanel(), "wrap 10");

        add(createTablePanel(), "grow");

        loadData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow,fill][]"));
        panel.setOpaque(false);
        JLabel lbTitle = new JLabel("Danh Sách Phiếu Nhập Hàng");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        panel.add(lbTitle);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> loadData());
        panel.add(btnRefresh);

        return panel;
    }

    private JPanel createToolBarPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]10[]push[][][]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        txtTimKiem = new JTextField();
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo mã phiếu, NCC...");
        // Thêm listener để tìm kiếm realtime
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchData();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchData();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchData();
            }
        });

        cbThoiGian = new JComboBox<>(new String[] { "Tất cả", "Tháng này", "Tháng trước" });
        cbThoiGian.addActionListener(e -> searchData());

        cbTrangThai = new JComboBox<>(new String[] { "Tất cả", "Đã nhập", "Chờ nhập", "Đã hủy" });
        cbTrangThai.addActionListener(e -> searchData());

        JButton btnXemChiTiet = new JButton("Xem chi tiết");
        btnXemChiTiet.addActionListener(e -> actionXemChiTiet());

        JButton btnInPhieu = new JButton("In phiếu");
        btnInPhieu.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:#1976D2;"
                + "foreground:#ffffff;"
                + "font:bold");
        btnInPhieu.addActionListener(e -> actionInPhieu());

        JButton btnHuyPhieu = new JButton("Hủy phiếu");
        btnHuyPhieu.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:#f44336;"
                + "foreground:#ffffff;"
                + "font:bold");
        btnHuyPhieu.addActionListener(e -> actionHuyPhieu());

        JButton btnXacNhan = new JButton("Xác nhận nhập kho");
        btnXacNhan.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:#4CAF50;"
                + "foreground:#ffffff;"
                + "font:bold");
        btnXacNhan.addActionListener(e -> actionXacNhan());

        panel.add(txtTimKiem, "w 250");
        panel.add(cbThoiGian);
        panel.add(cbTrangThai);

        panel.add(btnXemChiTiet);
        panel.add(btnInPhieu);
        panel.add(btnHuyPhieu);
        panel.add(btnXacNhan);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] columns = { "Mã Phiếu", "Nhà Cung Cấp", "Ngày Tạo", "Người Nhập", "Tổng Tiền", "Trạng Thái" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        table.getColumnModel().getColumn(4).setCellRenderer(new RightAlignRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());

        // Double click để xem chi tiết
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    actionXemChiTiet();
                }
            }
        });

        panel.add(new JScrollPane(table));
        return panel;
    }

    private class RightAlignRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);
            return this;
        }
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value != null ? value.toString() : "";

            if (status.contains("Chờ")) {
                com.setForeground(new Color(255, 152, 0));
                com.setFont(com.getFont().deriveFont(java.awt.Font.BOLD));
            } else if (status.contains("Đã nhập")) {
                com.setForeground(new Color(56, 142, 60));
            } else if (status.contains("hủy")) {
                com.setForeground(new Color(211, 47, 47));
            } else {
                com.setForeground(Color.GRAY);
            }

            if (isSelected) {
                com.setForeground(Color.WHITE);
            }
            return com;
        }
    }

    private void loadData() {
        model.setRowCount(0);
        List<PhieuNhap> list = phieuNhapDAO.getAllPhieuNhap();
        for (PhieuNhap pn : list) {
            model.addRow(new Object[] {
                    pn.getMaPN(),
                    pn.getNcc() != null ? pn.getNcc().getTenNCC() : "",
                    pn.getNgayTao() != null ? pn.getNgayTao().format(dateFormat) : "",
                    pn.getNhanVien() != null ? pn.getNhanVien().getHoTen() : "",
                    moneyFormat.format(pn.getTongTien()),
                    pn.getTrangThai()
            });
        }
    }

    private void searchData() {
        String keyword = txtTimKiem.getText().trim();
        String trangThai = (String) cbTrangThai.getSelectedItem();
        String thoiGian = (String) cbThoiGian.getSelectedItem();

        model.setRowCount(0);
        List<PhieuNhap> list = phieuNhapDAO.searchPhieuNhap(keyword, trangThai, thoiGian);
        for (PhieuNhap pn : list) {
            model.addRow(new Object[] {
                    pn.getMaPN(),
                    pn.getNcc() != null ? pn.getNcc().getTenNCC() : "",
                    pn.getNgayTao() != null ? pn.getNgayTao().format(dateFormat) : "",
                    pn.getNhanVien() != null ? pn.getNhanVien().getHoTen() : "",
                    moneyFormat.format(pn.getTongTien()),
                    pn.getTrangThai()
            });
        }
    }

    private void actionXacNhan() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Vui lòng chọn phiếu cần xác nhận!");
            return;
        }

        String currentStatus = model.getValueAt(row, 5).toString();
        String maPhieu = model.getValueAt(row, 0).toString();

        if (currentStatus.contains("Đã nhập")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Phiếu này đã nhập kho rồi!");
            return;
        }
        if (!currentStatus.contains("Chờ nhập")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Chỉ có thể xác nhận phiếu ở trạng thái 'Chờ nhập'!");
            return;
        }
        if (currentStatus.contains("hủy")) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                    "Không thể nhập kho phiếu đã hủy!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận hàng mã phiếu " + maPhieu + " đã về kho đầy đủ?\n"
                        + "Hệ thống sẽ cập nhật số lượng tồn kho.",
                "Xác nhận nhập kho", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Gọi method xacNhanNhapKho để cập nhật tồn kho
            boolean success = phieuNhapDAO.xacNhanNhapKho(maPhieu);
            if (success) {
                model.setValueAt("Đã nhập", row, 5);
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                        "Đã nhập kho thành công!");
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                        "Lỗi khi cập nhật tồn kho!");
            }
        }
    }

    private void actionHuyPhieu() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Vui lòng chọn phiếu cần hủy!");
            return;
        }

        String currentStatus = model.getValueAt(row, 5).toString();
        String maPhieu = model.getValueAt(row, 0).toString();

        if (currentStatus.contains("hủy")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Phiếu này đã bị hủy rồi!");
            return;
        }

        String warning = currentStatus.contains("Đã nhập")
                ? "Phiếu này đã nhập kho. Hủy phiếu sẽ trừ số lượng tồn kho đã nhập.\n"
                : "";

        int confirm = JOptionPane.showConfirmDialog(this,
                warning + "Bạn có chắc chắn muốn hủy phiếu " + maPhieu + "?",
                "Xác nhận hủy phiếu", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = phieuNhapDAO.huyPhieuNhap(maPhieu);
            if (success) {
                model.setValueAt("Đã hủy", row, 5);
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                        "Đã hủy phiếu thành công!");
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                        "Lỗi khi hủy phiếu!");
            }
        }
    }

    private void actionXemChiTiet() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Vui lòng chọn phiếu để xem chi tiết!");
            return;
        }
        String maPhieu = model.getValueAt(row, 0).toString();

        // Hiển thị dialog chi tiết phiếu nhập
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        DialogChiTietPhieuNhap dialog = new DialogChiTietPhieuNhap(parentFrame, maPhieu);
        dialog.setVisible(true);
    }

    private void actionInPhieu() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Vui lòng chọn phiếu cần in!");
            return;
        }
        String maPhieu = model.getValueAt(row, 0).toString();

        // Lấy thông tin phiếu nhập
        PhieuNhap phieuNhap = phieuNhapDAO.getPhieuNhapById(maPhieu);
        if (phieuNhap == null) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                    "Không tìm thấy thông tin phiếu nhập!");
            return;
        }

        // Tạo và mở PDF
        boolean success = ImportReceiptPDFGenerator.generateAndOpenImportReceipt(phieuNhap);
        if (success) {
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                    "Đã tạo phiếu nhập PDF thành công!");
        } else {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                    "Lỗi khi tạo phiếu nhập PDF!");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
