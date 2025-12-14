package gui.manage.partner;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.*;
import ca.odell.glazedlists.matchers.*;
import com.formdev.flatlaf.FlatClientProperties;
import dao.KhachHangDAO;
import entities.KhachHang;
import java.awt.Component;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormKhachHang extends JPanel {

    private JTextField txtTimKiem;
    private JComboBox<String> cbLocGioiTinh;
    private JTable table;
    private KhachHangDAO khachHangDAO;
    
    // GlazedLists components
    private EventList<KhachHang> khachHangList;
    private FilterList<KhachHang> filteredList;
    private FilterList<KhachHang> gioiTinhFilteredList;
    private SortedList<KhachHang> sortedList;
    private GioiTinhMatcherEditor gioiTinhMatcherEditor;

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
        JPanel panel = new JPanel(new MigLayout("insets 10", "[]push[][]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        txtTimKiem = new JTextField();
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo tên, SĐT...");

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

        // Initialize GlazedLists
        khachHangList = new BasicEventList<>();
        
        // Create text filter for live search
        filteredList = new FilterList<>(khachHangList, 
            new TextComponentMatcherEditor<>(txtTimKiem, new KhachHangTextFilterator()));
        
        // Create gender filter on top of text filter
        gioiTinhMatcherEditor = new GioiTinhMatcherEditor();
        gioiTinhFilteredList = new FilterList<>(filteredList, gioiTinhMatcherEditor);
        
        // Create sorted list
        sortedList = new SortedList<>(gioiTinhFilteredList, null);
        
        // Create custom TableFormat
        ca.odell.glazedlists.gui.TableFormat<KhachHang> tableFormat = new KhachHangTableFormat();
        
        // Create EventTableModel
        EventTableModel<KhachHang> eventTableModel = new EventTableModel<>(sortedList, tableFormat);
        
        table = new JTable(eventTableModel);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        table.getColumnModel().getColumn(6).setCellRenderer(new RightAlignRenderer());

        panel.add(new JScrollPane(table));
        return panel;
    }
    
    // Custom MatcherEditor for Gender filter
    private class GioiTinhMatcherEditor extends AbstractMatcherEditor<KhachHang> {
        public void setGioiTinh(Boolean gioiTinh) {
            if (gioiTinh == null) {
                fireMatchAll();
            } else {
                fireChanged(new GioiTinhMatcher(gioiTinh));
            }
        }
    }
    
    private class GioiTinhMatcher implements Matcher<KhachHang> {
        private final Boolean gioiTinh;
        
        public GioiTinhMatcher(Boolean gioiTinh) {
            this.gioiTinh = gioiTinh;
        }
        
        public boolean matches(KhachHang kh) {
            return kh.isGioiTinh() == gioiTinh;
        }
    }
    
    // Custom TableFormat class
    private class KhachHangTableFormat implements ca.odell.glazedlists.gui.TableFormat<KhachHang> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        public int getColumnCount() {
            return 7;
        }
        
        public String getColumnName(int column) {
            switch (column) {
                case 0: return "Mã KH";
                case 1: return "Họ Tên";
                case 2: return "Số ĐT";
                case 3: return "Giới Tính";
                case 4: return "Ngày Sinh";
                case 5: return "Địa Chỉ";
                case 6: return "Điểm Tích Lũy";
                default: return "";
            }
        }
        
        public Object getColumnValue(KhachHang kh, int column) {
            switch (column) {
                case 0: return kh.getMaKH();
                case 1: return kh.getTenKH();
                case 2: return kh.getSdt();
                case 3: return kh.isGioiTinh() ? "Nam" : "Nữ";
                case 4: return kh.getNgaySinh() != null ? kh.getNgaySinh().format(formatter) : "";
                case 5: return kh.getDiaChi() != null ? kh.getDiaChi() : "";
                case 6: return kh.getDiemTichLuy();
                default: return "";
            }
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

    private void loadData() {
        khachHangList.clear();
        ArrayList<KhachHang> dsKH = khachHangDAO.getAllKhachHang();
        khachHangList.addAll(dsKH);
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

        KhachHang kh = sortedList.get(row);
        
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

        KhachHang kh = sortedList.get(row);
        String maKH = kh.getMaKH();
        String tenKH = kh.getTenKH();

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

    private void actionLocGioiTinh() {
        String selected = cbLocGioiTinh.getSelectedItem().toString();
        
        if (selected.equals("Tất cả")) {
            gioiTinhMatcherEditor.setGioiTinh(null);
        } else {
            Boolean gioiTinh = selected.equals("Nam");
            gioiTinhMatcherEditor.setGioiTinh(gioiTinh);
        }
    }

    private void actionXemLichSu() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn khách hàng!");
            return;
        }
        
        KhachHang kh = sortedList.get(row);
        
        if (kh == null) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Không tìm thấy khách hàng!");
            return;
        }
        
        DialogLichSuMuaHang dialog = new DialogLichSuMuaHang(this, kh);
        dialog.setVisible(true);
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
