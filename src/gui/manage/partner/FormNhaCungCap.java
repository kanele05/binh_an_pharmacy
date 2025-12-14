package gui.manage.partner;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.*;
import ca.odell.glazedlists.matchers.*;
import com.formdev.flatlaf.FlatClientProperties;
import dao.NhaCungCapDAO;
import entities.NhaCungCap;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormNhaCungCap extends JPanel {

    private JTextField txtTimKiem;
    private JTable table;
    private NhaCungCapDAO nhaCungCapDAO;

    // GlazedLists components
    private EventList<NhaCungCap> nhaCungCapList;
    private FilterList<NhaCungCap> filteredList;
    private SortedList<NhaCungCap> sortedList;

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
        JPanel panel = new JPanel(new MigLayout("insets 10", "[]push[][]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        txtTimKiem = new JTextField();
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm tên NCC, SĐT, Email...");

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

        panel.add(btnThem);
        panel.add(btnSua);
        panel.add(btnXoa);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Initialize GlazedLists
        nhaCungCapList = new BasicEventList<>();
        TextFilterator<NhaCungCap> textFilterator = new TextFilterator<NhaCungCap>() {
            @Override
            public void getFilterStrings(List<String> baseList, NhaCungCap element) {
                if (element.getMaNCC()!= null) {
                    baseList.add(element.getMaNCC());
                }
                if (element.getTenNCC() != null) {
                    baseList.add(element.getTenNCC());
                }
                if (element.getSdt() != null) {
                    baseList.add(element.getSdt());
                }
                if (element.getEmail() != null) {
                    baseList.add(element.getEmail());
                }
            }
        };
        filteredList = new FilterList<>(nhaCungCapList,
                new TextComponentMatcherEditor<>(txtTimKiem, textFilterator));

        // Create sorted list
        sortedList = new SortedList<>(filteredList, null);

        // Create custom TableFormat
        ca.odell.glazedlists.gui.TableFormat<NhaCungCap> tableFormat = new NhaCungCapTableFormat();

        // Create EventTableModel
        EventTableModel<NhaCungCap> eventTableModel = new EventTableModel<>(sortedList, tableFormat);

        table = new JTable(eventTableModel);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        panel.add(new JScrollPane(table));
        return panel;
    }

    // Custom TableFormat class
    private class NhaCungCapTableFormat implements ca.odell.glazedlists.gui.TableFormat<NhaCungCap> {

        public int getColumnCount() {
            return 5;
        }

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Mã NCC";
                case 1:
                    return "Tên Nhà Cung Cấp";
                case 2:
                    return "Số Điện Thoại";
                case 3:
                    return "Email";
                case 4:
                    return "Địa Chỉ";
                default:
                    return "";
            }
        }

        public Object getColumnValue(NhaCungCap ncc, int column) {
            switch (column) {
                case 0:
                    return ncc.getMaNCC();
                case 1:
                    return ncc.getTenNCC();
                case 2:
                    return ncc.getSdt();
                case 3:
                    return ncc.getEmail() != null ? ncc.getEmail() : "";
                case 4:
                    return ncc.getDiaChi() != null ? ncc.getDiaChi() : "";
                default:
                    return "";
            }
        }
    }

    private void loadData() {
        nhaCungCapList.clear();
        ArrayList<NhaCungCap> dsNCC = nhaCungCapDAO.getAllNhaCungCap();
        nhaCungCapList.addAll(dsNCC);
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
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Chọn nhà cung cấp cần sửa!");
            return;
        }

        NhaCungCap ncc = sortedList.get(row);

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
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi cơ sở dữ liệu: " + e.getMessage());
            }
        }
    }

    private void actionXoa() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Chọn nhà cung cấp cần xóa!");
            return;
        }

        NhaCungCap ncc = sortedList.get(row);
        String maNCC = ncc.getMaNCC();
        String tenNCC = ncc.getTenNCC();

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
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi cơ sở dữ liệu: " + e.getMessage());
            }
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
