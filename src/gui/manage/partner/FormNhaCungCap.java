package gui.manage.partner;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Component;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormNhaCungCap extends JPanel {

    private JTextField txtTimKiem;
    private JTable table;
    private DefaultTableModel model;

    public FormNhaCungCap() {
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
        JLabel lbTitle = new JLabel("Danh Sách Nhà Cung Cấp");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        panel.add(lbTitle);
        return panel;
    }

    private JPanel createToolBarPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]push[][]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        txtTimKiem = new JTextField();
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm tên NCC, SĐT, Email...");

        JButton btnTim = new JButton("Tìm kiếm");

        JButton btnThem = new JButton("Thêm mới");
        btnThem.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold");
        btnThem.addActionListener(e -> actionThem());

        JButton btnSua = new JButton("Sửa");
        btnSua.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
        btnSua.addActionListener(e -> actionSua());

        JButton btnXoa = new JButton("Xóa");
        btnXoa.putClientProperty(FlatClientProperties.STYLE, "background:#F44336; foreground:#fff; font:bold");
        btnXoa.addActionListener(e -> actionXoa());

        panel.add(txtTimKiem, "w 250");
        panel.add(btnTim);

        panel.add(btnThem);
        panel.add(btnSua);
        panel.add(btnXoa);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] columns = {"Mã NCC", "Tên Nhà Cung Cấp", "Số Điện Thoại", "Email", "Địa Chỉ"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        panel.add(new JScrollPane(table));
        return panel;
    }

    private void loadData() {

        model.addRow(new Object[]{"NCC001", "Dược Hậu Giang (DHG)", "02923891433", "dhgpharma@dhgpharma.com.vn", "288 Nguyễn Văn Cừ, Cần Thơ"});
        model.addRow(new Object[]{"NCC002", "Sanofi Việt Nam", "02838298526", "contact-vn@sanofi.com", "Q1, TP.HCM"});
        model.addRow(new Object[]{"NCC003", "Zuellig Pharma", "02839102650", "info@zuelligpharma.com", "Tân Bình, TP.HCM"});
        model.addRow(new Object[]{"NCC004", "Dược phẩm Imexpharm", "02773851941", "imexpharm@imexpharm.com", "Cao Lãnh, Đồng Tháp"});
    }

    private void actionThem() {
        DialogNhaCungCap dialog = new DialogNhaCungCap(this, null);
        dialog.setVisible(true);
        if (dialog.isSave()) {
            model.addRow(dialog.getData());
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Thêm nhà cung cấp thành công!");
        }
    }

    private void actionSua() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Chọn nhà cung cấp cần sửa!");
            return;
        }

        Object[] currentData = new Object[model.getColumnCount()];
        for (int i = 0; i < model.getColumnCount(); i++) {
            currentData[i] = model.getValueAt(row, i);
        }

        DialogNhaCungCap dialog = new DialogNhaCungCap(this, currentData);
        dialog.setVisible(true);

        if (dialog.isSave()) {
            Object[] newData = dialog.getData();
            for (int i = 0; i < model.getColumnCount(); i++) {
                model.setValueAt(newData[i], row, i);
            }
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Cập nhật thành công!");
        }
    }

    private void actionXoa() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Chọn nhà cung cấp cần xóa!");
            return;
        }

        String tenNCC = model.getValueAt(row, 1).toString();
        if (JOptionPane.showConfirmDialog(this, "Xóa nhà cung cấp: " + tenNCC + "?\nLưu ý: Không thể xóa nếu đã có lịch sử nhập hàng.", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            model.removeRow(row);
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã xóa!");
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
