package gui.manage.staff;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.formdev.flatlaf.FlatClientProperties;
import dao.NhanVienDAO;
import entities.NhanVien;
import java.awt.Component;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormNhanVien extends JPanel {

    private JTextField txtTimKiem;
    private JComboBox<String> cbLocVaiTro;
    private JComboBox<String> cbLocTrangThai;
    private JTable table;

    private EventList<NhanVien> employees;
    private FilterList<NhanVien> textFiltered;
    private FilterList<NhanVien> comboFiltered;
    private EventTableModel<NhanVien> tableModel;

    private NhanVienDAO nhanVienDAO = new NhanVienDAO();

    public FormNhanVien() {
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
        JLabel lbTitle = new JLabel("Quản Lý Nhân Viên");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        panel.add(lbTitle);
        return panel;
    }

    private JPanel createToolBarPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]10[]10[]10[]push[][]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        txtTimKiem = new JTextField();
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm nhanh theo tên, mã, sđt...");

        JButton btnTim = new JButton("Tìm kiếm");

        cbLocVaiTro = new JComboBox<>(new String[]{"Tất cả vai trò", "Quản lý", "Nhân viên"});
        cbLocVaiTro.addActionListener(e -> updateFilters());

        cbLocTrangThai = new JComboBox<>(new String[]{"Tất cả trạng thái", "Đang làm", "Đã nghỉ"});
        cbLocTrangThai.addActionListener(e -> updateFilters());

        JButton btnThem = new JButton("Thêm mới");
        btnThem.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold");
        btnThem.addActionListener(e -> actionThem());

        JButton btnSua = new JButton("Sửa");
        btnSua.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
        btnSua.addActionListener(e -> actionSua());

        JButton btnXoa = new JButton("Nghỉ việc");
        btnXoa.putClientProperty(FlatClientProperties.STYLE, "background:#F44336; foreground:#fff; font:bold");
        btnXoa.addActionListener(e -> {
            try {
                actionXoa();
            } catch (SQLException ex) {
                Logger.getLogger(FormNhanVien.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        JButton btnResetPass = new JButton("Reset Pass");
        btnResetPass.addActionListener(e -> {
            try {
                actionResetPass();
            } catch (SQLException ex) {
                Logger.getLogger(FormNhanVien.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        panel.add(txtTimKiem, "w 250");
        panel.add(btnTim);

        panel.add(new JLabel("Vai trò:"));
        panel.add(cbLocVaiTro);

        panel.add(new JLabel("Trạng thái:"));
        panel.add(cbLocTrangThai);

        panel.add(btnResetPass);
        panel.add(btnThem);
        panel.add(btnSua);
        panel.add(btnXoa);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        employees = new BasicEventList<>();
        TextFilterator<NhanVien> textFilterator = new TextFilterator<NhanVien>() {
            @Override
            public void getFilterStrings(List<String> baseList, NhanVien nv) {
                baseList.add(nv.getMaNV());
                baseList.add(nv.getHoTen());
                baseList.add(nv.getSdt());
                baseList.add(nv.getEmail());
            }
        };
        TextComponentMatcherEditor<NhanVien> textMatcherEditor = new TextComponentMatcherEditor<>(txtTimKiem, textFilterator);
        textFiltered = new FilterList<>(employees, textMatcherEditor);
        comboFiltered = new FilterList<>(textFiltered);

        TableFormat<NhanVien> tableFormat = new TableFormat<NhanVien>() {
            @Override
            public int getColumnCount() {
                return 7;
            }

            @Override
            public String getColumnName(int i) {
                String[] columns = {"Mã NV", "Họ Tên", "Giới Tính", "Số ĐT", "Email", "Vai Trò", "Trạng Thái"};
                return columns[i];
            }

            @Override
            public Object getColumnValue(NhanVien nv, int i) {
                switch (i) {
                    case 0:
                        return nv.getMaNV();
                    case 1:
                        return nv.getHoTen();
                    case 2:
                        return nv.isGioiTinh() ? "Nam" : "Nữ";
                    case 3:
                        return nv.getSdt();
                    case 4:
                        return nv.getEmail();
                    case 5:
                        return nv.isVaiTro() ? "Quản lý" : "Nhân viên";
                    case 6:
                        return nv.isTrangThai() ? "Đang làm" : "Đã nghỉ";
                    default:
                        return null;
                }
            }
        };

        tableModel = new EventTableModel<>(comboFiltered, tableFormat);
        table = new JTable(tableModel);

        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        CenterAlignRenderer centerRenderer = new CenterAlignRenderer();
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        panel.add(new JScrollPane(table));
        return panel;
    }

    private class CenterAlignRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    }

    private void loadData() {
        ArrayList<NhanVien> listNV = nhanVienDAO.getAllTblNhanVien();
        employees.clear();
        employees.addAll(listNV);
    }

    private void updateFilters() {
        String roleFilter = cbLocVaiTro.getSelectedItem().toString();
        String statusFilter = cbLocTrangThai.getSelectedItem().toString();

        Matcher<NhanVien> matcher = new Matcher<NhanVien>() {
            @Override
            public boolean matches(NhanVien nv) {
                String dbRole = nv.isVaiTro() ? "Quản lý" : "Nhân viên";
                String dbStatus = nv.isTrangThai() ? "Đang làm" : "Đã nghỉ";

                boolean matchRole = roleFilter.equals("Tất cả vai trò") || roleFilter.equals(dbRole);
                boolean matchStatus = statusFilter.equals("Tất cả trạng thái") || statusFilter.equals(dbStatus);

                return matchRole && matchStatus;
            }
        };

        comboFiltered.setMatcher(matcher);
    }

    private void actionThem() {
        DialogNhanVien dialog = new DialogNhanVien(this, null);
        dialog.setVisible(true);
        if (dialog.isSave()) {
            loadData();
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Thêm nhân viên mới thành công!");
        }
    }

    private void actionSua() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn nhân viên để sửa!");
            return;
        }
        NhanVien selectedNV = comboFiltered.get(row);
        NhanVien nvFull = nhanVienDAO.getNhanVienByID(selectedNV.getMaNV());

        if (nvFull != null) {
            Object[] data = new Object[]{
                nvFull.getMaNV(),
                nvFull.getHoTen(),
                nvFull.isGioiTinh() ? "Nam" : "Nữ",
                nvFull.getSdt(),
                nvFull.getEmail(),
                nvFull.isVaiTro() ? "Quản lý" : "Nhân viên bán hàng",
                nvFull.isTrangThai() ? "Đang làm" : "Đã nghỉ",
                nvFull.getNgaySinh(),
                nvFull.getDiaChi()
            };

            DialogNhanVien dialog = new DialogNhanVien(this, data);
            dialog.setVisible(true);

            if (dialog.isSave()) {
                loadData();
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Cập nhật thông tin thành công!");
            }
        }
    }

    private void actionXoa() throws SQLException {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn nhân viên!");
            return;
        }

        NhanVien selectedNV = comboFiltered.get(row);

        if (!selectedNV.isTrangThai()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Nhân viên này đã nghỉ việc rồi!");
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this,
                "Xác nhận cập nhật trạng thái 'ĐÃ NGHỈ VIỆC' cho: " + selectedNV.getHoTen() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            if (nhanVienDAO.delete(selectedNV.getMaNV())) {
                loadData();
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã cập nhật trạng thái thành công!");
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi hệ thống!");
            }
        }
    }

    private void actionResetPass() throws SQLException {
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }

        NhanVien selectedNV = comboFiltered.get(row);

        int opt = JOptionPane.showConfirmDialog(this, "Reset mật khẩu của '" + selectedNV.getHoTen() + "' về mặc định (123456)?", "Reset Password", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            if (nhanVienDAO.resetPassword(selectedNV.getMaNV(), "123456")) {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã reset mật khẩu thành công!");
            }
        }
    }

    public void openThemMoi() {
        actionThem();
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
