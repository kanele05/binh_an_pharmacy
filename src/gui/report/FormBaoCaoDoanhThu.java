package gui.report;

import com.formdev.flatlaf.FlatClientProperties;
import dao.ThongKeDAO;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.component.date.DatePicker;
import raven.toast.Notifications;
import utils.ExcelHelper;

public class FormBaoCaoDoanhThu extends JPanel {

    private JFormattedTextField txtTuNgay, txtDenNgay;
    private JLabel lbTongDoanhThu, lbTongLoiNhuan, lbTongDonHang;
    private JTable table;
    private DefaultTableModel model;

    private DatePicker datePicker1;
    private DatePicker datePicker2;

    private ThongKeDAO thongKeDAO;
    private List<Map<String, Object>> currentData;
    private Map<String, Object> currentSummary;
    private NumberFormat currencyFormat;

    public FormBaoCaoDoanhThu() {
        initComponents();
        init();
    }

    private void init() {
        thongKeDAO = new ThongKeDAO();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][][grow]"));

        add(createHeaderPanel(), "wrap 20");
        add(createFilterPanel(), "wrap 20");
        add(createSummaryPanel(), "wrap 20");
        add(createTablePanel(), "grow");

        // Thiết lập ngày mặc định (30 ngày gần đây)
        datePicker1.setSelectedDate(LocalDate.now().minusDays(30));
        datePicker2.setSelectedDate(LocalDate.now());

        loadData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow,fill][]"));
        panel.setOpaque(false);
        JLabel lbTitle = new JLabel("Báo Cáo Doanh Thu & Lợi Nhuận");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        panel.add(lbTitle);
        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10, fillx", "[]10[150!]20[]10[150!]5[]push[]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        txtTuNgay = new JFormattedTextField();
        txtDenNgay = new JFormattedTextField();

        datePicker1 = new DatePicker();
        datePicker1.setEditor(txtTuNgay);
        datePicker1.setDateFormat("dd/MM/yyyy");
        datePicker1.setCloseAfterSelected(true);
        datePicker1.setDateSelectionMode(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);

        datePicker2 = new DatePicker();
        datePicker2.setEditor(txtDenNgay);
        datePicker2.setDateFormat("dd/MM/yyyy");
        datePicker2.setCloseAfterSelected(true);
        datePicker2.setDateSelectionMode(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);

        JButton btnLoc = new JButton("Lọc dữ liệu");
        btnLoc.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
        btnLoc.addActionListener(e -> actionFilter());

        JButton btnXuatExcel = new JButton("Xuất Excel");
        btnXuatExcel.putClientProperty(FlatClientProperties.STYLE, "background:#009688; foreground:#fff; font:bold");
        btnXuatExcel.addActionListener(e -> actionExport());

        panel.add(new JLabel("Từ ngày:"));
        panel.add(txtTuNgay, "w 150!");
        panel.add(new JLabel("Đến ngày:"));
        panel.add(txtDenNgay, "w 150!");
        panel.add(btnLoc);
        panel.add(btnXuatExcel);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0, fillx", "[grow, fill]20[grow, fill]20[grow, fill]", "[100!]"));
        panel.setOpaque(false);

        lbTongDoanhThu = new JLabel("0 ₫");
        panel.add(createCard("Tổng Doanh Thu", lbTongDoanhThu, "#4CAF50"));

        lbTongLoiNhuan = new JLabel("0 ₫");
        panel.add(createCard("Lợi Nhuận Ước Tính", lbTongLoiNhuan, "#FFA000"));

        lbTongDonHang = new JLabel("0");
        panel.add(createCard("Tổng Số Đơn Hàng", lbTongDonHang, "#2196F3"));

        return panel;
    }

    private JPanel createCard(String title, JLabel lbValue, String colorHex) {
        JPanel card = new JPanel(new MigLayout("insets 20", "[]push[]", "[]5[]"));
        card.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:lighten(" + colorHex + ",10%);"
                + "border:0,0,0,0,shade(" + colorHex + ",5%),2");

        JLabel lbTitle = new JLabel(title);
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold; foreground:#fff");

        lbValue.putClientProperty(FlatClientProperties.STYLE, "font:bold +10; foreground:#fff");

        card.add(lbTitle, "wrap");
        card.add(lbValue, "span 2");
        return card;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createTitledBorder("Chi tiết doanh thu theo ngày"));

        String[] columns = {"Ngày", "Số Đơn", "Doanh Thu", "Giá Vốn", "Lợi Nhuận", "Tăng Trưởng"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        RightAlignRenderer rightAlign = new RightAlignRenderer();
        table.getColumnModel().getColumn(1).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(2).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(3).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(4).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(5).setCellRenderer(new GrowthRenderer());

        panel.add(new JScrollPane(table));
        return panel;
    }

    private void loadData() {
        if (!datePicker1.isDateSelected() || !datePicker2.isDateSelected()) {
            return;
        }

        LocalDate tuNgay = datePicker1.getSelectedDate();
        LocalDate denNgay = datePicker2.getSelectedDate();

        // Lấy dữ liệu từ DAO
        currentData = thongKeDAO.getBaoCaoDoanhThuChiTiet(tuNgay, denNgay);
        currentSummary = thongKeDAO.getTongHopDoanhThu(tuNgay, denNgay);

        // Xóa dữ liệu cũ
        model.setRowCount(0);

        // Format ngày
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Thêm dữ liệu mới
        for (Map<String, Object> row : currentData) {
            LocalDate ngay = (LocalDate) row.get("ngay");
            int soDon = (int) row.get("soDon");
            double doanhThu = (double) row.get("doanhThu");
            double giaVon = (double) row.get("giaVon");
            double loiNhuan = (double) row.get("loiNhuan");
            double tangTruong = (double) row.get("tangTruong");

            model.addRow(new Object[]{
                ngay != null ? ngay.format(dtf) : "",
                String.valueOf(soDon),
                currencyFormat.format(doanhThu),
                currencyFormat.format(giaVon),
                currencyFormat.format(loiNhuan),
                String.format("%+.1f%%", tangTruong)
            });
        }

        // Cập nhật summary
        double tongDoanhThu = currentSummary.get("tongDoanhThu") != null ? (double) currentSummary.get("tongDoanhThu") : 0;
        double tongLoiNhuan = currentSummary.get("tongLoiNhuan") != null ? (double) currentSummary.get("tongLoiNhuan") : 0;
        int tongDonHang = currentSummary.get("tongDonHang") != null ? (int) currentSummary.get("tongDonHang") : 0;

        lbTongDoanhThu.setText(currencyFormat.format(tongDoanhThu));
        lbTongLoiNhuan.setText(currencyFormat.format(tongLoiNhuan));
        lbTongDonHang.setText(tongDonHang + " Đơn");
    }

    private void actionFilter() {
        if (!datePicker1.isDateSelected() || !datePicker2.isDateSelected()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Vui lòng chọn đầy đủ Từ ngày và Đến ngày!");
            return;
        }

        LocalDate tuNgay = datePicker1.getSelectedDate();
        LocalDate denNgay = datePicker2.getSelectedDate();

        if (tuNgay.isAfter(denNgay)) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Từ ngày không được lớn hơn Đến ngày!");
            return;
        }

        loadData();

        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER,
                "Đã tải dữ liệu từ " + tuNgay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                " đến " + denNgay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private void actionExport() {
        if (currentData == null || currentData.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Không có dữ liệu để xuất!");
            return;
        }

        if (!datePicker1.isDateSelected() || !datePicker2.isDateSelected()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Vui lòng chọn khoảng thời gian trước khi xuất!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu báo cáo doanh thu");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        // Tên file mặc định
        String defaultFileName = "BaoCaoDoanhThu_" +
                datePicker1.getSelectedDate().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + "_" +
                datePicker2.getSelectedDate().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".xlsx";
        fileChooser.setSelectedFile(new File(defaultFileName));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Đảm bảo file có đuôi .xlsx
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }

            try {
                ExcelHelper.exportBaoCaoDoanhThu(file, currentData, currentSummary,
                        datePicker1.getSelectedDate(), datePicker2.getSelectedDate());

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                        "Xuất báo cáo thành công: " + file.getName());
            } catch (Exception e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                        "Lỗi xuất báo cáo: " + e.getMessage());
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

    private class GrowthRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);
            String text = value.toString();
            if (text.startsWith("+")) {
                com.setForeground(new Color(56, 142, 60));
            } else if (text.startsWith("-")) {
                com.setForeground(new Color(211, 47, 47));
            } else {
                com.setForeground(Color.BLACK);
            }

            if (isSelected) {
                com.setForeground(Color.WHITE);
            }
            return com;
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
