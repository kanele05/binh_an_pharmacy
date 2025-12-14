package gui.manage.partner;

import com.formdev.flatlaf.FlatClientProperties;
import dao.KhachHangDAO;
import entities.KhachHang;
import java.awt.Component;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormKhachHang extends JPanel {

    private JTextField txtTimKiem;
    private JComboBox<String> cbLocGioiTinh;
    private JTable table;
    private DefaultTableModel model;
    private KhachHangDAO khachHangDAO;

    public FormKhachHang() {
        khachHangDAO = new KhachHangDAO();
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
        JLabel lbTitle = new JLabel("Danh Sách Khách Hàng");
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
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo tên, SĐT...");

        JButton btnTim = new JButton("Tìm kiếm");
        btnTim.addActionListener(e -> actionTimKiem());

        cbLocGioiTinh = new JComboBox<>(new String[]{"Tất cả", "Nam", "Nữ"});
        cbLocGioiTinh.addActionListener(e -> actionLocGioiTinh());

        JButton btnThem = new JButton("Thêm mới");
        btnThem.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold");
        btnThem.addActionListener(e -> actionThem());

        JButton btnSua = new JButton("Sửa");
        btnSua.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
        btnSua.addActionListener(e -> actionSua());

        JButton btnXoa = new JButton("Xóa");
        btnXoa.putClientProperty(FlatClientProperties.STYLE, "background:#F44336; foreground:#fff; font:bold");
        btnXoa.addActionListener(e -> actionXoa());

        JButton btnLichSu = new JButton("Xem lịch sử mua");
        btnLichSu.addActionListener(e -> actionXemLichSu());

        panel.add(txtTimKiem, "w 250");
        panel.add(btnTim);
        panel.add(new JLabel("Giới tính:"));
        panel.add(cbLocGioiTinh);

        panel.add(btnLichSu);
        panel.add(btnThem);
        panel.add(btnSua);
        panel.add(btnXoa);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] columns = {"Mã KH", "Họ Tên", "Số ĐT", "Giới Tính", "Ngày Sinh", "Địa Chỉ", "Điểm Tích Lũy"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        table.getColumnModel().getColumn(6).setCellRenderer(new RightAlignRenderer());

        panel.add(new JScrollPane(table));
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

    private void loadData() {
        model.setRowCount(0);
        ArrayList<KhachHang> dsKH = khachHangDAO.getAllKhachHang();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (KhachHang kh : dsKH) {
            String ngaySinh = kh.getNgaySinh() != null ? kh.getNgaySinh().format(formatter) : "";
            String gioiTinh = kh.isGioiTinh() ? "Nam" : "Nữ";
            model.addRow(new Object[]{
                kh.getMaKH(),
                kh.getTenKH(),
                kh.getSdt(),
                gioiTinh,
                ngaySinh,
                kh.getDiaChi() != null ? kh.getDiaChi() : "",
                kh.getDiemTichLuy()
            });
        }
    }

    private void actionThem() {
        DialogKhachHang dialog = new DialogKhachHang(this, null);
        dialog.setVisible(true);
        if (dialog.isSave()) {
            try {
                KhachHang kh = dialog.getKhachHang();
                if (khachHangDAO.insert(kh)) {
                    loadData();
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Thêm khách hàng thành công!");
                } else {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Thêm khách hàng thất bại!");
                }
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi cơ sở dữ liệu: " + e.getMessage());
            }
        }
    }

    public void openThemMoi() {

        actionThem();
    }

    private void actionSua() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Chọn khách hàng cần sửa!");
            return;
        }

        String maKH = model.getValueAt(row, 0).toString();
        KhachHang kh = khachHangDAO.getKhachHangByID(maKH);
        
        if (kh == null) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Không tìm thấy khách hàng!");
            return;
        }

        DialogKhachHang dialog = new DialogKhachHang(this, kh);
        dialog.setVisible(true);

        if (dialog.isSave()) {
            try {
                KhachHang updatedKH = dialog.getKhachHang();
                if (khachHangDAO.update(updatedKH)) {
                    loadData();
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Cập nhật thành công!");
                } else {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Cập nhật thất bại!");
                }
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi cơ sở dữ liệu: " + e.getMessage());
            }
        }
    }

    private void actionXoa() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Chọn khách hàng cần xóa!");
            return;
        }

        String maKH = model.getValueAt(row, 0).toString();
        String tenKH = model.getValueAt(row, 1).toString();

        if (JOptionPane.showConfirmDialog(this, "Xóa khách hàng: " + tenKH + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                if (khachHangDAO.delete(maKH)) {
                    loadData();
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã xóa!");
                } else {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Xóa thất bại!");
                }
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi cơ sở dữ liệu: " + e.getMessage());
            }
        }
    }

    private void actionTimKiem() {
        String keyword = txtTimKiem.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }
        
        model.setRowCount(0);
        ArrayList<KhachHang> dsKH = khachHangDAO.searchKhachHang(keyword);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (KhachHang kh : dsKH) {
            String ngaySinh = kh.getNgaySinh() != null ? kh.getNgaySinh().format(formatter) : "";
            String gioiTinh = kh.isGioiTinh() ? "Nam" : "Nữ";
            model.addRow(new Object[]{
                kh.getMaKH(),
                kh.getTenKH(),
                kh.getSdt(),
                gioiTinh,
                ngaySinh,
                kh.getDiaChi() != null ? kh.getDiaChi() : "",
                kh.getDiemTichLuy()
            });
        }
    }

    private void actionLocGioiTinh() {
        String selected = cbLocGioiTinh.getSelectedItem().toString();
        
        if (selected.equals("Tất cả")) {
            loadData();
            return;
        }
        
        Boolean gioiTinh = selected.equals("Nam");
        model.setRowCount(0);
        ArrayList<KhachHang> dsKH = khachHangDAO.filterByGioiTinh(gioiTinh);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (KhachHang kh : dsKH) {
            String ngaySinh = kh.getNgaySinh() != null ? kh.getNgaySinh().format(formatter) : "";
            String gt = kh.isGioiTinh() ? "Nam" : "Nữ";
            model.addRow(new Object[]{
                kh.getMaKH(),
                kh.getTenKH(),
                kh.getSdt(),
                gt,
                ngaySinh,
                kh.getDiaChi() != null ? kh.getDiaChi() : "",
                kh.getDiemTichLuy()
            });
        }
    }

    private void actionXemLichSu() {
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }
        String tenKH = model.getValueAt(row, 1).toString();

        JOptionPane.showMessageDialog(this,
                "Lịch sử mua hàng của: " + tenKH + "\n\n"
                + "- 08/12/2023: Panadol (50.000đ)\n"
                + "- 01/12/2023: Vitamin C (120.000đ)\n"
                + "- 20/11/2023: Khẩu trang (35.000đ)",
                "Lịch sử giao dịch", JOptionPane.INFORMATION_MESSAGE);
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
