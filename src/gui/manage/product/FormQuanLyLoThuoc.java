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
import dao.LoThuocDAO;
import entities.LoThuoc;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.component.date.DatePicker;
import raven.toast.Notifications;

public class FormQuanLyLoThuoc extends javax.swing.JPanel {

    private JTextField txtTimKiem;
    private JComboBox<String> cbTrangThai;
    private JTable table;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private LoThuocDAO loThuocDAO = new LoThuocDAO();
    private EventList<LoThuoc> loThuocs;
    private FilterList<LoThuoc> textFiltered;
    private FilterList<LoThuoc> comboFiltered;
    private EventTableModel<LoThuoc> tableModel;

    public FormQuanLyLoThuoc() {
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
        JLabel lbTitle = new JLabel("Quản Lý Lô Thuốc & Hạn Sử Dụng");
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
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo mã lô, tên thuốc...");

        JButton btnTim = new JButton("Tìm kiếm");

        cbTrangThai = new JComboBox<>(new String[]{"Tất cả", "Còn hạn", "Sắp hết hạn", "Đã hết hạn", "Hết hàng"});
        cbTrangThai.addActionListener(e -> filterData());

        JButton btnHuyLo = new JButton("Hủy lô hết hạn");
        btnHuyLo.putClientProperty(FlatClientProperties.STYLE, "background:#F44336; foreground:#fff; font:bold");
        btnHuyLo.addActionListener(e -> {
            try {
                actionHuyLo();
            } catch (SQLException ex) {
                Logger.getLogger(FormQuanLyLoThuoc.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        JButton btnSua = new JButton("Sửa thông tin");
        btnSua.addActionListener(e -> actionSua());

        panel.add(txtTimKiem, "w 250");
        panel.add(btnTim);
        panel.add(new JLabel("Trạng thái:"));
        panel.add(cbTrangThai);

        panel.add(btnSua);
        panel.add(btnHuyLo);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        loThuocs = new BasicEventList<>();
        TextFilterator<LoThuoc> textFilterator = new TextFilterator<LoThuoc>() {
            @Override
            public void getFilterStrings(List<String> list, LoThuoc e) {
                list.add(e.getMaLo());
                list.add(e.getThuoc().getTenThuoc());
            }
        };
        TextComponentMatcherEditor<LoThuoc> textMatcherEditor = new TextComponentMatcherEditor<>(txtTimKiem, textFilterator);
        textFiltered = new FilterList<>(loThuocs, textMatcherEditor);
        comboFiltered = new FilterList<>(textFiltered);

        TableFormat<LoThuoc> tableFormat = new TableFormat<LoThuoc>() {
            @Override
            public int getColumnCount() {
                return 6;
            }

            @Override
            public String getColumnName(int i) {
                String[] columns = {"Mã Lô", "Tên Thuốc", "Ngày Nhập", "Hạn Sử Dụng", "Tồn Kho", "Trạng Thái"};
                return columns[i];
            }

            @Override
            public Object getColumnValue(LoThuoc e, int i) {
                switch (i) {
                    case 0:
                        return e.getMaLo();
                    case 1:
                        return e.getThuoc().getTenThuoc();
                    case 2:
                        return e.getNgayNhap().format(dateFormatter);
                    case 3:
                        return e.getHanSuDung().format(dateFormatter);
                    case 4:
                        return e.getSoLuongTon();
                    case 5:
                        return e.getTrangThai();
                    default:
                        return null;
                }
            }
        };

        tableModel = new EventTableModel<>(comboFiltered, tableFormat);
        table = new JTable(tableModel);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        ExpiryDateRenderer dateRenderer = new ExpiryDateRenderer();
        table.getColumnModel().getColumn(3).setCellRenderer(dateRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(dateRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        panel.add(scroll);

        return panel;
    }

    private class ExpiryDateRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String hsdStr = table.getValueAt(row, 3).toString();

            try {
                LocalDate hsd = LocalDate.parse(hsdStr, dateFormatter);
                LocalDate homNay = LocalDate.now();

                long daysBetween = ChronoUnit.DAYS.between(homNay, hsd);

                if (daysBetween < 0) {

                    if (!isSelected) {
                        com.setForeground(new Color(211, 47, 47));
                        com.setFont(com.getFont().deriveFont(java.awt.Font.BOLD));
                    }
                } else if (daysBetween <= 90) {

                    if (!isSelected) {
                        com.setForeground(new Color(230, 81, 0));
                    }
                } else {

                    if (!isSelected) {
                        com.setForeground(new Color(56, 142, 60));
                    }
                }

            } catch (Exception e) {

            }

            return com;
        }
    }

    private void loadData() {
        ArrayList<LoThuoc> listLo = loThuocDAO.getAllTblLoThuoc();
        loThuocs.clear();
        loThuocs.addAll(listLo);
    }

    private void filterData() {
        String statusFilter = cbTrangThai.getSelectedItem().toString();
        Matcher<LoThuoc> matcher = new Matcher<LoThuoc>() {
            @Override
            public boolean matches(LoThuoc e) {
                String dbStatus = e.getTrangThai();
                boolean matchStatus = statusFilter.equals("Tất cả") || statusFilter.equals(dbStatus);
                return matchStatus;
            }
        };
        comboFiltered.setMatcher(matcher);
    }

    private void actionHuyLo() throws SQLException {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Chọn lô cần hủy!");
            return;
        }

        String trangThai = tableModel.getValueAt(row, 5).toString();
        if (!trangThai.equals("Đã hết hạn")) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Lô này chưa hết hạn (" + trangThai + "). Bạn có chắc chắn muốn hủy không?",
                    "Cảnh báo", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Xác nhận tiêu hủy lô thuốc đã hết hạn này?",
                    "Tiêu hủy", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        LoThuoc selectedLo = comboFiltered.get(row);
        if (loThuocDAO.delete(selectedLo.getMaLo())) {
            loadData();
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã hủy thành công!");
        } else {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi hệ thống!");
        }
    }

    private void actionSua() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn lô thuốc để sửa!");
            return;
        }

        LoThuoc selectedLo = comboFiltered.get(row);

        JPanel pInput = new JPanel(new java.awt.GridLayout(0, 1, 5, 5));

        JFormattedTextField txtHSD = new JFormattedTextField();
        DatePicker datePicker = new DatePicker();
        datePicker.setEditor(txtHSD);
        datePicker.setDateFormat("dd/MM/yyyy");
        datePicker.setCloseAfterSelected(true);

        datePicker.setSelectedDate(selectedLo.getHanSuDung());

        JSpinner spinTon = new JSpinner(new SpinnerNumberModel(selectedLo.getSoLuongTon(), 0, 100000, 1));

        pInput.add(new JLabel("Hạn sử dụng mới:"));
        pInput.add(txtHSD);
        pInput.add(new JLabel("Số lượng tồn kho thực tế:"));
        pInput.add(spinTon);

        int result = JOptionPane.showConfirmDialog(this, pInput,
                "Điều chỉnh Lô: " + selectedLo.getMaLo() + " - " + selectedLo.getThuoc().getTenThuoc(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {

                if (!datePicker.isDateSelected()) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn hạn sử dụng!");
                    return;
                }

                LocalDate newHSD = datePicker.getSelectedDate();
                int newTon = (int) spinTon.getValue();

                String newStatus = "Còn hạn";
                if (newHSD.isBefore(LocalDate.now())) {
                    newStatus = "Đã hết hạn";
                } else if (newHSD.minusDays(90).isBefore(LocalDate.now())) {
                    newStatus = "Sắp hết hạn";
                }

                if (newTon == 0 && (newStatus.equalsIgnoreCase("Còn hạn") || newStatus.equalsIgnoreCase("Sắp hết hạn"))) {
                    newStatus = "Hết hàng";
                } else if (newStatus.equals("Đã hết hạn") && newTon > 0) {
                    newStatus = "Đã hết hạn";
                }

                selectedLo.setHanSuDung(newHSD);
                selectedLo.setSoLuongTon(newTon);
                selectedLo.setTrangThai(newStatus);

                if (loThuocDAO.update(selectedLo)) {
                    loadData();
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Cập nhật thành công!");
                } else {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi khi lưu dữ liệu!");
                }

            } catch (SQLException ex) {
                Logger.getLogger(FormQuanLyLoThuoc.class.getName()).log(Level.SEVERE, null, ex);
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi Database!");
            }
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
