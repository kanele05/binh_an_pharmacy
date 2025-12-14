package gui.transaction.order;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.formdev.flatlaf.FlatClientProperties;
import dao.DonDatHangDAO;
import entities.DonDatHang;
import java.awt.Component;
import java.awt.Color;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormDatThuoc extends javax.swing.JPanel {

    private JTextField txtTimKiem;
    private JComboBox<String> cbTrangThai;
    private JTable table;
    private EventList<DonDatHang> sourceList;
    private FilterList<DonDatHang> textFilteredList;
    private FilterList<DonDatHang> compositeFilteredList;
    private EventTableModel<DonDatHang> tableModel;
    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private DonDatHangDAO donDatHangDAO = new DonDatHangDAO();

    public FormDatThuoc() {
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
        JLabel lbTitle = new JLabel("Đơn Đặt Thuốc");
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
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm SĐT hoặc tên khách...");

        JButton btnTim = new JButton("Tìm kiếm");
        cbTrangThai = new JComboBox<>(new String[]{"Tất cả", "Đang giữ hàng", "Đã lấy hàng", "Đã hủy"});
        cbTrangThai.addActionListener(e -> filterStatus());
        JButton btnTaoPhieu = new JButton("Tạo phiếu giữ");
        btnTaoPhieu.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
        btnTaoPhieu.addActionListener(e -> actionTaoPhieu());

        JButton btnKhachDen = new JButton("Khách đến lấy hàng");
        btnKhachDen.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold");
        btnKhachDen.addActionListener(e -> actionKhachDenLay());

        JButton btnEdit = new JButton("Chỉnh sửa");
        btnEdit.putClientProperty(FlatClientProperties.STYLE, "background:#FF9800; foreground:#fff; font:bold");
        btnEdit.addActionListener(e -> actionEditPhieu());

        panel.add(txtTimKiem, "w 250");
        panel.add(btnTim);
        panel.add(new JLabel("Lọc trạng thái:"));
        panel.add(cbTrangThai);
        panel.add(btnTaoPhieu);
        panel.add(btnEdit);
        panel.add(btnKhachDen);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        sourceList = new BasicEventList<>();

        TextFilterator<DonDatHang> textFilterator = (list, d) -> {
            list.add(d.getMaDonDat());
            list.add(d.getTenKhach());
            list.add(d.getSdtLienHe());
        };
        TextComponentMatcherEditor<DonDatHang> textMatcher = new TextComponentMatcherEditor<>(txtTimKiem, textFilterator);
        textFilteredList = new FilterList<>(sourceList, textMatcher);

        compositeFilteredList = new FilterList<>(textFilteredList);

        TableFormat<DonDatHang> tableFormat = new TableFormat<DonDatHang>() {
            @Override
            public int getColumnCount() {
                return 7;
            }

            @Override
            public String getColumnName(int i) {
                return new String[]{"Mã Phiếu", "Khách Hàng", "SĐT", "Giờ Hẹn Lấy", "Tổng Tiền", "Ghi Chú", "Trạng Thái"}[i];
            }

            @Override
            public Object getColumnValue(DonDatHang d, int i) {
                switch (i) {
                    case 0:
                        return d.getMaDonDat();
                    case 1:
                        return d.getTenKhach();
                    case 2:
                        return d.getSdtLienHe();
                    case 3:
                        return d.getGioHenLay();
                    case 4:
                        return d.getTongTien();
                    case 5:
                        return d.getGhiChu();
                    case 6:
                        return d.getTrangThai();
                    default:
                        return null;
                }
            }
        };

        tableModel = new EventTableModel<>(compositeFilteredList, tableFormat);

        table = new JTable(tableModel);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        table.getColumnModel().getColumn(3).setCellRenderer(new DateRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new CurrencyRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());

        panel.add(new JScrollPane(table));
        return panel;
    }

    private class DateRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            if (v instanceof LocalDateTime) {
                setText(((LocalDateTime) v).format(dateTimeFormat));
            }
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    }

    private class CurrencyRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            if (v instanceof Double) {
                setText(currencyFormat.format(v));
                setForeground(new Color(0, 150, 136));
            }
            setHorizontalAlignment(JLabel.RIGHT);
            return this;
        }
    }

    private class StatusRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value != null ? value.toString() : "";
            if (status.equals("Đang giữ hàng")) {
                setForeground(new Color(255, 152, 0));
                setFont(getFont().deriveFont(java.awt.Font.BOLD));
            } else if (status.equals("Đã lấy hàng")) {
                setForeground(new Color(76, 175, 80));
            } else {
                setForeground(Color.GRAY);
            }
            if (isSelected) {
                setForeground(Color.WHITE);
            }
            return com;
        }
    }

    private void loadData() {
        sourceList.clear();
        sourceList.addAll(donDatHangDAO.getAllDonDat());
    }

    private void filterStatus() {
        String selected = cbTrangThai.getSelectedItem().toString();
        Matcher<DonDatHang> matcher = item -> {
            if (selected.equals("Tất cả")) {
                return true;
            }
            return item.getTrangThai().equalsIgnoreCase(selected);
        };
        compositeFilteredList.setMatcher(matcher);
    }

    private void actionEditPhieu() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, "Vui lòng chọn phiếu cần sửa!");
            return;
        }

        DonDatHang donSelected = compositeFilteredList.get(row);

        if (!donSelected.getTrangThai().equals("Đang giữ hàng")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, "Chỉ được sửa đơn đang giữ hàng!");
            return;
        }

        DialogDatThuoc dialog = new DialogDatThuoc(this, donSelected);
        dialog.setVisible(true);

        if (dialog.isSave()) {
            loadData();

        }
    }

    private void actionTaoPhieu() {
        DialogDatThuoc dialog = new DialogDatThuoc(this);
        dialog.setVisible(true);
        if (dialog.isSave()) {
            loadData();
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã tạo phiếu và trừ tồn kho tạm thời!");
        }
    }

    private void actionKhachDenLay() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn phiếu giữ hàng!");
            return;
        }

        DonDatHang don = compositeFilteredList.get(row);

        if (!don.getTrangThai().equalsIgnoreCase("Đang giữ hàng")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, "Chỉ xử lý được đơn đang giữ hàng!");
            return;
        }

        DialogThanhToanDonDat dialog = new DialogThanhToanDonDat(this, don);
        dialog.packAndCenter();
        dialog.setVisible(true);

        if (dialog.isSuccess()) {
            loadData();
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
