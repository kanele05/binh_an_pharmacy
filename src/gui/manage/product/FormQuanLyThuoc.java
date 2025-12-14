package gui.manage.product;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import dao.ThuocDAO;
import dto.ThuocFullInfo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormQuanLyThuoc extends javax.swing.JPanel {

    private JTextField txtTimKiem;
    private JComboBox<String> cbNhomThuoc;
    private JTable table;
    private EventList<ThuocFullInfo> medicines;
    private FilterList<ThuocFullInfo> textFiltered;
    private FilterList<ThuocFullInfo> comboFiltered;
    private EventTableModel<ThuocFullInfo> tableModel;

    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnXuatExcel;
    private JButton btnKhoiPhuc;

    private ThuocDAO thuocDAO = new ThuocDAO();

    public FormQuanLyThuoc() {
        initComponents();
        init();
    }

    private void init() {

        setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow]"));

        add(createHeaderPanel(), "wrap 20");

        add(createToolBarPanel(), "wrap 10");

        add(createTablePanel(), "grow");

        add(createFooterPanel(), "growx");

        loadNhomThuocToComboBox();
        loadData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow,fill][]"));
        panel.setOpaque(false);

        JLabel lbTitle = new JLabel("Danh Sách Thuốc");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");

        panel.add(lbTitle);
        return panel;
    }

    private JPanel createToolBarPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]push[][][]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        txtTimKiem = new JTextField();
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo tên, mã thuốc, hoạt chất...");

        JButton btnTim = new JButton("Tìm kiếm");

        cbNhomThuoc = new JComboBox<>();
        cbNhomThuoc.addActionListener(e -> updateFilters());

        btnThem = createButton("Thêm mới", "#4CAF50");
        btnSua = createButton("Sửa", "#2196F3");
        btnXoa = createButton("Xóa", "#F44336");
        btnXuatExcel = createButton("Xuất Excel", "#009688");
        btnKhoiPhuc = createButton("Khôi phục", "#FFC125");

        btnThem.addActionListener(e -> actionThem());
        btnSua.addActionListener(e -> actionSua());
        btnXoa.addActionListener(e -> {
            try {
                actionXoa();
            } catch (SQLException ex) {
                Logger.getLogger(FormQuanLyThuoc.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        btnKhoiPhuc.addActionListener(e -> {
            try {
                actionKhoiPhuc();
            } catch (SQLException ex) {
                Logger.getLogger(FormQuanLyThuoc.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        panel.add(txtTimKiem, "w 250");
        panel.add(btnTim);
        panel.add(new JLabel("Lọc:"));
        panel.add(cbNhomThuoc);

        panel.add(btnThem);
        panel.add(btnSua);
        panel.add(btnXoa);
        panel.add(btnKhoiPhuc);
        panel.add(btnXuatExcel);

        return panel;
    }

    private JButton createButton(String text, String colorHex) {
        JButton btn = new JButton(text);
        btn.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:" + colorHex + ";"
                + "foreground:#FFFFFF;"
                + "font:bold;"
                + "borderWidth:0;"
                + "focusWidth:0");
        return btn;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        medicines = new BasicEventList<>();
        TextFilterator<ThuocFullInfo> textFilterator = new TextFilterator<ThuocFullInfo>() {
            @Override
            public void getFilterStrings(List<String> list, ThuocFullInfo e) {
                list.add(e.getMaThuoc());
                list.add(e.getTenThuoc());
                list.add(e.getHoatChat());
            }
        };
        TextComponentMatcherEditor<ThuocFullInfo> textMatcharEditor = new TextComponentMatcherEditor<>(txtTimKiem, textFilterator);
        textFiltered = new FilterList<>(medicines, textMatcharEditor);
        comboFiltered = new FilterList<>(textFiltered);

        TableFormat<ThuocFullInfo> tableFormat = new TableFormat<ThuocFullInfo>() {
            @Override
            public int getColumnCount() {
                return 8;
            }

            @Override
            public String getColumnName(int i) {
                String[] columns = {"Mã Thuốc", "Tên Thuốc", "Nhóm", "Hoạt Chất", "ĐVT", "Giá Nhập", "Giá Bán", "Tồn Kho"};
                return columns[i];
            }

            @Override
            public Object getColumnValue(ThuocFullInfo e, int i) {
                switch (i) {
                    case 0:
                        return e.getMaThuoc();
                    case 1:
                        return e.getTenThuoc();
                    case 2:
                        return e.getTenNhom();
                    case 3:
                        return e.getHoatChat();
                    case 4:
                        return e.getDonViCoBan();
                    case 5:
                        return e.getGiaNhap();
                    case 6:
                        return e.getGiaBan();
                    case 7:
                        return e.getTonKho();
                    default:
                        return null;
                }
            }
        };
        tableModel = new EventTableModel<>(comboFiltered, tableFormat);
        table = new JTable(tableModel);

        table.putClientProperty(FlatClientProperties.STYLE, ""
                + "rowHeight:30;"
                + "showHorizontalLines:true;"
                + "intercellSpacing:0,1;");

        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, ""
                + "height:35;"
                + "font:bold;");

        StatusAndAlignRenderer renderer = new StatusAndAlignRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 5"));
        panel.setOpaque(false);

        JLabel note1 = new JLabel("Chữ Đỏ / Nền Hồng: Ngừng kinh doanh");
        note1.putClientProperty(FlatClientProperties.STYLE, "foreground:#D32F2F; font:bold");

        JLabel note2 = new JLabel("Chữ Đen / Nền Trắng: Đang kinh doanh bình thường");
        note2.putClientProperty(FlatClientProperties.STYLE, "foreground:#333333");

        panel.add(new JLabel("Chú thích:"), "wrap");
        panel.add(note1);
        panel.add(note2, "gapleft 20");

        return panel;
    }

    public void openThemMoi() {
        actionThem();
    }

    private void loadNhomThuocToComboBox() {
        cbNhomThuoc.removeAllItems();
        cbNhomThuoc.addItem("Tất cả nhóm thuốc");

        ArrayList<String> dsTenNhom = thuocDAO.getAllNhomThuocName();
        for (String tenNhom : dsTenNhom) {
            cbNhomThuoc.addItem(tenNhom);
        }
    }

    private void updateFilters() {
        String nhomThuocFilter = cbNhomThuoc.getSelectedItem().toString();
        Matcher<ThuocFullInfo> matcher = new Matcher<ThuocFullInfo>() {
            @Override
            public boolean matches(ThuocFullInfo e) {
                String dbNhomThuoc = e.getTenNhom();
                boolean matchNhomThuoc = nhomThuocFilter.equals("Tất cả nhóm thuốc") || nhomThuocFilter.equals(dbNhomThuoc);
                return matchNhomThuoc;
            }
        };
        comboFiltered.setMatcher(matcher);
    }

    private void actionKhoiPhuc() throws SQLException {

        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn thuốc cần khôi phục!");
            return;
        }

        ThuocFullInfo selectedThuoc = medicines.get(row);

        if (selectedThuoc.isTrangThai()) {
            Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Thuốc này đang kinh doanh bình thường!");
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this,
                "Xác nhận khôi phục kinh doanh cho thuốc: " + selectedThuoc.getTenThuoc() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {

            if (thuocDAO.restore(selectedThuoc.getMaThuoc())) {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã khôi phục thành công!");
                loadData();
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi hệ thống!");
            }
        }
    }

    private void loadData() {
        ArrayList<ThuocFullInfo> listThuoc = thuocDAO.getAllThuocFullInfo();
        medicines.clear();
        medicines.addAll(listThuoc);
    }

    private void actionThem() {
        DialogThuoc dialog = new DialogThuoc(this, null);
        dialog.setVisible(true);

        if (dialog.isSave()) {

            Object[] data = dialog.getData();

            ThuocFullInfo t = new ThuocFullInfo(
                    data[0].toString(),
                    data[1].toString(),
                    data[3].toString(),
                    data[4].toString(),
                    data[2].toString(),
                    0,
                    Double.parseDouble(data[6].toString().replace(",", "")),
                    0,
                    0,
                    true
            );

            if (thuocDAO.themThuocMoi(t)) {
                loadData();
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Thêm thuốc thành công (Tồn kho: 0)");
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Thêm thất bại!");
            }
        }
    }

    private void actionSua() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn thuốc để sửa!");
            return;
        }

        ThuocFullInfo selectedThuoc = comboFiltered.get(row);
        ThuocFullInfo thuocFull = thuocDAO.getThuocFullInfoByID(selectedThuoc.getMaThuoc());
        if (thuocFull != null) {
            Object[] data = new Object[]{
                thuocFull.getMaThuoc(),
                thuocFull.getTenThuoc(),
                thuocFull.getTenNhom(),
                thuocFull.getHoatChat(),
                thuocFull.getDonViCoBan(),
                thuocFull.getGiaBan(),};
            DialogThuoc dialog = new DialogThuoc(this, data);
            dialog.setVisible(true);
            if (dialog.isSave()) {
                loadData();
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Cập nhật thành công!");
            }
        }
    }

    private void actionXoa() throws SQLException {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn thuốc để xóa!");
            return;
        }

        ThuocFullInfo selectedThuoc = comboFiltered.get(row);

        if (!selectedThuoc.isTrangThai()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Thuốc này đã ngừng kinh doanh rồi!");
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this,
                "Xác nhận cập nhật trạng thái 'Ngừng kinh doanh' cho: " + selectedThuoc.getTenThuoc() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            if (thuocDAO.delete(selectedThuoc.getMaThuoc())) {
                loadData();
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã cập nhật trạng thái thành công!");
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi hệ thống!");
            }
        }
    }

    private class StatusAndAlignRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int modelRow = table.convertRowIndexToModel(row);
            ThuocFullInfo t = comboFiltered.get(modelRow);

            if (!t.isTrangThai()) {
                com.setForeground(new Color(211, 47, 47));
                if (!isSelected) {
                    com.setBackground(new Color(255, 235, 238));
                }
            } else {
                com.setForeground(new Color(51, 51, 51));
                if (!isSelected) {
                    com.setBackground(Color.WHITE);
                }
            }

            if (column == 5 || column == 6 || column == 7) {
                setHorizontalAlignment(JLabel.RIGHT);
            } else if (column == 3 || column == 8) {
                setHorizontalAlignment(JLabel.CENTER);
            } else {
                setHorizontalAlignment(JLabel.LEFT);
            }
            if (column == 7) {
                int tongTon = t.getTonKho();
                int banDuoc = t.getTonKhoBanDuoc();
                int hangLoi = tongTon - banDuoc;

                if (hangLoi > 0) {

                    setText(tongTon + " (Hết hạn: " + hangLoi + ")");

                    if (!isSelected) {
                        com.setForeground(Color.RED);
                    }
                } else {
                    setText(String.valueOf(tongTon));
                }
                setHorizontalAlignment(JLabel.RIGHT);
            }
            return com;
        }
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
