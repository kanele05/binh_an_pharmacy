package gui.manage.partner;

import com.formdev.flatlaf.FlatClientProperties;
import dao.NhaCungCapDAO;
import entities.NhaCungCap;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormNhaCungCap extends JPanel {

    private JTextField txtTimKiem;
    private JTable table;
    private DefaultTableModel model;
    private NhaCungCapDAO nhaCungCapDAO;

    public FormNhaCungCap() {
        nhaCungCapDAO = new NhaCungCapDAO();
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
        btnTim.addActionListener(e -> actionTimKiem());

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
        model.setRowCount(0);
        ArrayList<NhaCungCap> dsNCC = nhaCungCapDAO.getAllNhaCungCap();
        for (NhaCungCap ncc : dsNCC) {
            model.addRow(new Object[]{
                ncc.getMaNCC(),
                ncc.getTenNCC(),
                ncc.getSdt(),
                ncc.getEmail() != null ? ncc.getEmail() : "",
                ncc.getDiaChi() != null ? ncc.getDiaChi() : ""
            });
        }
    }

    private void actionThem() {
        DialogNhaCungCap dialog = new DialogNhaCungCap(this, null);
        dialog.setVisible(true);
        if (dialog.isSave()) {
            try {
                NhaCungCap ncc = dialog.getNhaCungCap();
                if (nhaCungCapDAO.insert(ncc)) {
                    loadData();
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Thêm nhà cung cấp thành công!");
                } else {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Thêm nhà cung cấp thất bại!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi: " + e.getMessage());
            }
        }
    }

    public void openThemMoi() {
        actionThem();
    }

    private void actionSua() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Chọn nhà cung cấp cần sửa!");
            return;
        }

        String maNCC = model.getValueAt(row, 0).toString();
        NhaCungCap ncc = nhaCungCapDAO.getNhaCungCapByID(maNCC);
        
        if (ncc == null) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Không tìm thấy nhà cung cấp!");
            return;
        }

        DialogNhaCungCap dialog = new DialogNhaCungCap(this, ncc);
        dialog.setVisible(true);

        if (dialog.isSave()) {
            try {
                NhaCungCap updatedNCC = dialog.getNhaCungCap();
                if (nhaCungCapDAO.update(updatedNCC)) {
                    loadData();
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Cập nhật thành công!");
                } else {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Cập nhật thất bại!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi: " + e.getMessage());
            }
        }
    }

    private void actionXoa() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Chọn nhà cung cấp cần xóa!");
            return;
        }

        String maNCC = model.getValueAt(row, 0).toString();
        String tenNCC = model.getValueAt(row, 1).toString();
        
        // Kiểm tra xem NCC có phiếu nhập hay không
        if (nhaCungCapDAO.hasPhieuNhap(maNCC)) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Không thể xóa nhà cung cấp đã có phiếu nhập!");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "Xóa nhà cung cấp: " + tenNCC + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                if (nhaCungCapDAO.delete(maNCC)) {
                    loadData();
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã xóa!");
                } else {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Xóa thất bại!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi: " + e.getMessage());
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
        ArrayList<NhaCungCap> dsNCC = nhaCungCapDAO.searchNhaCungCap(keyword);
        for (NhaCungCap ncc : dsNCC) {
            model.addRow(new Object[]{
                ncc.getMaNCC(),
                ncc.getTenNCC(),
                ncc.getSdt(),
                ncc.getEmail() != null ? ncc.getEmail() : "",
                ncc.getDiaChi() != null ? ncc.getDiaChi() : ""
            });
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
