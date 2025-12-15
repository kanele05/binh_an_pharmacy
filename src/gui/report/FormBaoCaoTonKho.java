package gui.report;

import com.formdev.flatlaf.FlatClientProperties;
import dao.LoThuocDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import raven.toast.Notifications;
import utils.ExcelHelper;

public class FormBaoCaoTonKho extends JPanel {

    private JComboBox<String> cbNhomThuoc;
    private JComboBox<String> cbTrangThai;
    private JLabel lbTongGiaTri, lbTongSoLuong, lbThuocHetHan;
    private JTable table;
    private DefaultTableModel model;
    private JPanel chartPanel;

    private LoThuocDAO loThuocDAO;
    private List<Map<String, Object>> currentData;
    private Map<String, Object> currentSummary;
    private NumberFormat currencyFormat;

    public FormBaoCaoTonKho() {
        initComponents();
        init();
    }

    private void init() {
        loThuocDAO = new LoThuocDAO();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][][grow,fill]"));

        add(createHeaderPanel(), "wrap 20");

        add(createFilterPanel(), "wrap 20");

        add(createSummaryPanel(), "wrap 20");

        add(createContentPanel(), "grow");

        loadData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow,fill][]"));
        panel.setOpaque(false);
        JLabel lbTitle = new JLabel("Báo Cáo Tồn Kho & Hạn Sử Dụng");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        panel.add(lbTitle);
        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10, fillx", "[]10[200!]20[]10[200!]5[]push[]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        cbNhomThuoc = new JComboBox<>(new String[]{"Tất cả nhóm", "Kháng sinh", "Giảm đau", "Vitamin", "Dụng cụ y tế"});
        cbTrangThai = new JComboBox<>(new String[]{"Tất cả", "Sắp hết hạn (< 1 tháng)", "Đã hết hạn", "Tồn kho thấp (< 10)", "Tồn kho cao (> 500)"});

        JButton btnLoc = new JButton("Lọc dữ liệu");
        btnLoc.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
        btnLoc.addActionListener(e -> actionFilter());

        JButton btnXuatExcel = new JButton("Xuất Excel");
        btnXuatExcel.putClientProperty(FlatClientProperties.STYLE, "background:#009688; foreground:#fff; font:bold");
        btnXuatExcel.addActionListener(e -> actionExport());

        panel.add(new JLabel("Nhóm thuốc:"));
        panel.add(cbNhomThuoc, "w 200!");
        panel.add(new JLabel("Trạng thái:"));
        panel.add(cbTrangThai, "w 200!");
        panel.add(btnLoc);
        panel.add(btnXuatExcel);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0, fillx", "[grow, fill]20[grow, fill]20[grow, fill]", "[100!]"));
        panel.setOpaque(false);

        lbTongGiaTri = new JLabel("0 ₫");
        panel.add(createCard("Tổng Giá Trị Kho", lbTongGiaTri, "#4CAF50"));

        lbTongSoLuong = new JLabel("0");
        panel.add(createCard("Tổng Số Lượng Tồn", lbTongSoLuong, "#2196F3"));

        lbThuocHetHan = new JLabel("0 Lô");
        panel.add(createCard("Lô Sắp/Đã Hết Hạn", lbThuocHetHan, "#F44336"));

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

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[60%,fill][40%,fill]", "[grow,fill]"));
        panel.setOpaque(false);

        // Bảng dữ liệu bên trái
        panel.add(createTablePanel(), "grow");

        // Biểu đồ bên phải
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        chartPanel.setBorder(BorderFactory.createTitledBorder("Biểu đồ tồn kho"));
        panel.add(chartPanel, "grow");

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createTitledBorder("Chi tiết tồn kho theo Lô"));

        String[] columns = {"Mã Thuốc", "Tên Thuốc", "Lô SX", "Hạn Dùng", "Đơn Giá Vốn", "Tồn Kho", "Tổng Giá Trị", "Ghi Chú"};
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
        table.getColumnModel().getColumn(4).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(5).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(6).setCellRenderer(rightAlign);

        table.getColumnModel().getColumn(3).setCellRenderer(new ExpiryDateRenderer());

        panel.add(new JScrollPane(table));
        return panel;
    }

    private void updateChart() {
        chartPanel.removeAll();

        if (currentData == null || currentData.isEmpty()) {
            JLabel noDataLabel = new JLabel("Không có dữ liệu để hiển thị", SwingConstants.CENTER);
            noDataLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            chartPanel.add(noDataLabel, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();
            return;
        }

        // Tạo panel chứa 2 biểu đồ
        JPanel chartsContainer = new JPanel(new MigLayout("fill, wrap 1", "[fill]", "[50%,fill][50%,fill]"));
        chartsContainer.setOpaque(false);

        // Biểu đồ 1: Bar chart - Top 10 thuốc theo giá trị tồn kho
        chartsContainer.add(createBarChart(), "grow");

        // Biểu đồ 2: Pie chart - Phân bổ theo trạng thái hạn sử dụng
        chartsContainer.add(createPieChart(), "grow");

        chartPanel.add(chartsContainer, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private ChartPanel createBarChart() {
        // Tạo dataset cho biểu đồ - Top 10 thuốc theo giá trị tồn kho
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Sắp xếp và lấy top 10 thuốc có giá trị tồn kho cao nhất
        currentData.stream()
                .sorted((a, b) -> Double.compare((double) b.get("tongGiaTri"), (double) a.get("tongGiaTri")))
                .limit(10)
                .forEach(row -> {
                    String tenThuoc = row.get("tenThuoc") != null ? row.get("tenThuoc").toString() : "";
                    // Rút gọn tên nếu quá dài
                    if (tenThuoc.length() > 15) {
                        tenThuoc = tenThuoc.substring(0, 12) + "...";
                    }
                    double tongGiaTri = (double) row.get("tongGiaTri");
                    // Chuyển đổi sang triệu đồng để dễ đọc
                    dataset.addValue(tongGiaTri / 1000000, "Giá trị (triệu ₫)", tenThuoc);
                });

        // Tạo biểu đồ
        JFreeChart chart = ChartFactory.createBarChart(
                "Top 10 Thuốc Theo Giá Trị Tồn Kho",
                null,
                "Giá trị (triệu ₫)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        // Tùy chỉnh giao diện biểu đồ
        chart.setBackgroundPaint(new Color(0, 0, 0, 0));
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 12));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(245, 245, 245));
        plot.setDomainGridlinePaint(new Color(200, 200, 200));
        plot.setRangeGridlinePaint(new Color(200, 200, 200));
        plot.setOutlineVisible(false);

        // Tùy chỉnh renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, new Color(33, 150, 243)); // Xanh dương
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.1);

        // Xoay nhãn trục X
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 9));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));

        // Tạo panel chứa biểu đồ
        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(350, 200));
        cp.setMouseWheelEnabled(true);
        cp.setOpaque(false);

        return cp;
    }

    @SuppressWarnings("unchecked")
    private ChartPanel createPieChart() {
        // Đếm số lô theo trạng thái
        Map<String, Integer> statusCount = new HashMap<>();
        statusCount.put("Còn hạn", 0);
        statusCount.put("Sắp hết hạn", 0);
        statusCount.put("Đã hết hạn", 0);

        for (Map<String, Object> row : currentData) {
            String ghiChu = row.get("ghiChu") != null ? row.get("ghiChu").toString() : "";
            if (ghiChu.contains("Đã hết hạn")) {
                statusCount.put("Đã hết hạn", statusCount.get("Đã hết hạn") + 1);
            } else if (ghiChu.contains("Sắp hết hạn")) {
                statusCount.put("Sắp hết hạn", statusCount.get("Sắp hết hạn") + 1);
            } else {
                statusCount.put("Còn hạn", statusCount.get("Còn hạn") + 1);
            }
        }

        // Tạo dataset cho biểu đồ tròn
        DefaultPieDataset dataset = new DefaultPieDataset();
        if (statusCount.get("Còn hạn") > 0) {
            dataset.setValue("Còn hạn (" + statusCount.get("Còn hạn") + ")", statusCount.get("Còn hạn"));
        }
        if (statusCount.get("Sắp hết hạn") > 0) {
            dataset.setValue("Sắp hết hạn (" + statusCount.get("Sắp hết hạn") + ")", statusCount.get("Sắp hết hạn"));
        }
        if (statusCount.get("Đã hết hạn") > 0) {
            dataset.setValue("Đã hết hạn (" + statusCount.get("Đã hết hạn") + ")", statusCount.get("Đã hết hạn"));
        }

        // Tạo biểu đồ
        JFreeChart chart = ChartFactory.createPieChart(
                "Phân Bổ Theo Hạn Sử Dụng",
                dataset,
                true,
                true,
                false
        );

        // Tùy chỉnh giao diện
        chart.setBackgroundPaint(new Color(0, 0, 0, 0));
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 12));
        chart.getLegend().setBackgroundPaint(new Color(0, 0, 0, 0));

        PiePlot piePlot = (PiePlot) chart.getPlot();
        piePlot.setBackgroundPaint(new Color(0, 0, 0, 0));
        piePlot.setOutlineVisible(false);
        piePlot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        piePlot.setShadowPaint(null);

        // Màu sắc cho từng phần
        piePlot.setSectionPaint("Còn hạn (" + statusCount.get("Còn hạn") + ")", new Color(76, 175, 80)); // Xanh lá
        piePlot.setSectionPaint("Sắp hết hạn (" + statusCount.get("Sắp hết hạn") + ")", new Color(255, 152, 0)); // Cam
        piePlot.setSectionPaint("Đã hết hạn (" + statusCount.get("Đã hết hạn") + ")", new Color(244, 67, 54)); // Đỏ

        // Tạo panel chứa biểu đồ
        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(350, 200));
        cp.setMouseWheelEnabled(true);
        cp.setOpaque(false);

        return cp;
    }

    private void loadData() {
        String nhomThuoc = cbNhomThuoc.getSelectedItem().toString();
        String trangThaiText = cbTrangThai.getSelectedItem().toString();

        // Chuyển đổi trạng thái text sang code
        String trangThai = null;
        if (trangThaiText.contains("Sắp hết hạn")) {
            trangThai = "sapHetHan";
        } else if (trangThaiText.contains("Đã hết hạn")) {
            trangThai = "daHetHan";
        } else if (trangThaiText.contains("Tồn kho thấp")) {
            trangThai = "tonThap";
        } else if (trangThaiText.contains("Tồn kho cao")) {
            trangThai = "tonCao";
        }

        // Lấy dữ liệu từ DAO
        currentData = loThuocDAO.getBaoCaoTonKhoChiTiet(nhomThuoc, trangThai);
        currentSummary = loThuocDAO.getTongHopTonKho();

        // Xóa dữ liệu cũ
        model.setRowCount(0);

        // Format ngày
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Thêm dữ liệu mới
        for (Map<String, Object> row : currentData) {
            String maThuoc = row.get("maThuoc") != null ? row.get("maThuoc").toString() : "";
            String tenThuoc = row.get("tenThuoc") != null ? row.get("tenThuoc").toString() : "";
            String maLo = row.get("maLo") != null ? row.get("maLo").toString() : "";
            LocalDate hanSuDung = (LocalDate) row.get("hanSuDung");
            double giaVon = (double) row.get("giaVon");
            int soLuongTon = (int) row.get("soLuongTon");
            double tongGiaTri = (double) row.get("tongGiaTri");
            String ghiChu = row.get("ghiChu") != null ? row.get("ghiChu").toString() : "";

            model.addRow(new Object[]{
                maThuoc,
                tenThuoc,
                maLo,
                hanSuDung != null ? hanSuDung.format(dtf) : "",
                currencyFormat.format(giaVon),
                String.valueOf(soLuongTon),
                currencyFormat.format(tongGiaTri),
                ghiChu
            });
        }

        // Cập nhật summary
        double tongGiaTri = currentSummary.get("tongGiaTri") != null ? (double) currentSummary.get("tongGiaTri") : 0;
        int tongSoLuong = currentSummary.get("tongSoLuong") != null ? (int) currentSummary.get("tongSoLuong") : 0;
        int soLoHetHan = currentSummary.get("soLoHetHan") != null ? (int) currentSummary.get("soLoHetHan") : 0;

        lbTongGiaTri.setText(currencyFormat.format(tongGiaTri));
        lbTongSoLuong.setText(NumberFormat.getInstance(new Locale("vi", "VN")).format(tongSoLuong));
        lbThuocHetHan.setText(soLoHetHan + " Lô");

        // Cập nhật biểu đồ
        updateChart();
    }

    private void actionFilter() {
        String nhom = cbNhomThuoc.getSelectedItem().toString();
        String trangThai = cbTrangThai.getSelectedItem().toString();

        loadData();

        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER,
                "Đã lọc: " + nhom + " - " + trangThai);
    }

    private void actionExport() {
        if (currentData == null || currentData.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Không có dữ liệu để xuất!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu báo cáo tồn kho");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        // Tên file mặc định
        String defaultFileName = "BaoCaoTonKho_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".xlsx";
        fileChooser.setSelectedFile(new File(defaultFileName));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Đảm bảo file có đuôi .xlsx
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }

            try {
                ExcelHelper.exportBaoCaoTonKho(file, currentData, currentSummary);

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

    private class ExpiryDateRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String note = model.getValueAt(row, 7).toString();

            if (note.contains("Đã hết hạn")) {
                com.setForeground(new Color(211, 47, 47));
                com.setFont(com.getFont().deriveFont(java.awt.Font.BOLD));
            } else if (note.contains("Sắp hết hạn")) {
                com.setForeground(new Color(230, 81, 0));
                com.setFont(com.getFont().deriveFont(java.awt.Font.BOLD));
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
