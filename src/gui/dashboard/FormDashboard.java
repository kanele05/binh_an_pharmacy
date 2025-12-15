package gui.dashboard;

import com.formdev.flatlaf.FlatClientProperties;
import dao.LoThuocDAO;
import dao.ThongKeDAO;
import dto.ThuocSapHetHan;
import dto.ThuocTonThap;
import gui.main.Application;
import gui.report.FormCanhBaoHetHan;
import gui.report.FormCanhBaoTonKho;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class FormDashboard extends javax.swing.JPanel {

    private ThongKeDAO thongKeDAO;
    private LoThuocDAO loThuocDAO;
    private NumberFormat currencyFormat;

    public FormDashboard() {
        thongKeDAO = new ThongKeDAO();
        loThuocDAO = new LoThuocDAO();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        initComponents();
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fillx,insets 20", "[fill]", "[][][][grow]"));

        JLabel lbTitle = new JLabel("Dashboard - Tổng quan");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        add(lbTitle, "wrap 20");

        add(createStatsPanel(), "wrap 20");

        JPanel contentPanel = new JPanel(new MigLayout("insets 0", "[60%,fill][40%,fill]", "[grow,fill]"));
        contentPanel.add(createRevenueChartPanel(), "grow");
        contentPanel.add(createTopMedicinesPanel(), "grow");
        add(contentPanel, "grow, h 300!, wrap 20");

        add(createAlertPanel(), "wrap");
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new MigLayout("insets 0", "[grow,fill][grow,fill][grow,fill][grow,fill]", "[]"));

        double doanhThuHomNay = thongKeDAO.getDoanhThuHomNay();
        int soHoaDonHomNay = thongKeDAO.getSoHoaDonHomNay();
        int soLoSapHetHan = loThuocDAO.getSoLoSapHetHan(30);
        int soThuocTonThap = loThuocDAO.getSoThuocTonThap();

        statsPanel.add(createStatCard("Doanh thu hôm nay", currencyFormat.format(doanhThuHomNay), "#4CAF50", true));
        statsPanel.add(createStatCard("Đơn hàng hôm nay", String.valueOf(soHoaDonHomNay), "#2196F3", false));
        statsPanel.add(createStatCard("Sắp hết hạn", String.valueOf(soLoSapHetHan), "#FF9800", false));
        statsPanel.add(createStatCard("Tồn kho thấp", String.valueOf(soThuocTonThap), "#F44336", false));

        return statsPanel;
    }

    private JPanel createStatCard(String title, String value, String color, boolean isLarge) {
        JPanel card = new JPanel(new MigLayout("insets 15", "[]push[]", "[]5[]"));
        card.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        JLabel lbTitle = new JLabel(title);
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:-1;foreground:darken(@foreground,30%)");

        JLabel lbValue = new JLabel(value);
        if (isLarge) {
            lbValue.putClientProperty(FlatClientProperties.STYLE, "font:bold +4");
        } else {
            lbValue.putClientProperty(FlatClientProperties.STYLE, "font:bold +6");
        }

        JLabel lbIcon = new JLabel();
        lbIcon.putClientProperty(FlatClientProperties.STYLE, "foreground:" + color);

        card.add(lbTitle, "wrap");
        card.add(lbValue);
        card.add(lbIcon, "east");

        return card;
    }

    private JPanel createRevenueChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Doanh thu 7 ngày gần nhất");
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        panel.add(title, BorderLayout.NORTH);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<Map<String, Object>> data = thongKeDAO.getDoanhThu7NgayGanNhat();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM");

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(df);
            double revenue = 0;

            for (Map<String, Object> row : data) {
                LocalDate rowDate = (LocalDate) row.get("ngay");
                if (rowDate.equals(date)) {
                    revenue = (double) row.get("doanhThu");
                    break;
                }
            }
            dataset.addValue(revenue / 1000000, "Doanh thu (triệu)", dateStr);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                null,
                null,
                "Triệu đồng",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        chart.setBackgroundPaint(null);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(null);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(33, 150, 243));
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 250));
        chartPanel.setBackground(null);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTopMedicinesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Top 5 thuốc bán chạy tháng này");
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {"Thuốc", "SL"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Map<String, Object>> topThuoc = thongKeDAO.getTopThuocBanChayThang(5);
        if (topThuoc.isEmpty()) {
            model.addRow(new Object[]{"Chưa có dữ liệu", "-"});
        } else {
            for (Map<String, Object> thuoc : topThuoc) {
                model.addRow(new Object[]{
                    thuoc.get("tenThuoc"),
                    thuoc.get("soLuong")
                });
            }
        }

        JTable table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "showHorizontalLines:false; rowHeight:28");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "font:bold");
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAlertPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 15,wrap", "[grow,fill][grow,fill]", "[]10[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:lighten(#FFF3E0,3%)");

        JLabel title = new JLabel("⚠ Cảnh báo cần xử lý");
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +2;foreground:#F57C00");
        panel.add(title, "span 2, wrap 10");

        JPanel expiryPanel = createExpiryAlertSubPanel();
        JPanel stockPanel = createStockAlertSubPanel();

        panel.add(expiryPanel, "grow");
        panel.add(stockPanel, "grow");

        return panel;
    }

    private JPanel createExpiryAlertSubPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10, wrap", "[grow,fill]", "[]5[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:15;"
                + "background:lighten(#FFCDD2,5%)");

        JLabel titleLabel = new JLabel("Thuốc sắp hết hạn (< 30 ngày)");
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold;foreground:#D32F2F");
        panel.add(titleLabel);

        List<ThuocSapHetHan> expiringList = loThuocDAO.getThuocSapHetHan(30);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        int count = 0;
        for (ThuocSapHetHan thuoc : expiringList) {
            if (count >= 3) break;
            String text = String.format("• %s (Lô %s) - HSD: %s - Còn %d ngày",
                    thuoc.getTenThuoc(),
                    thuoc.getMaLo(),
                    thuoc.getHanSuDung().format(df),
                    thuoc.getSoNgayConLai());
            JLabel item = new JLabel(text);
            item.putClientProperty(FlatClientProperties.STYLE, "foreground:#C62828");
            panel.add(item);
            count++;
        }

        if (expiringList.isEmpty()) {
            JLabel noData = new JLabel("Không có thuốc sắp hết hạn");
            noData.putClientProperty(FlatClientProperties.STYLE, "foreground:#4CAF50");
            panel.add(noData);
        } else if (expiringList.size() > 3) {
            JLabel moreLabel = new JLabel(">> Xem tất cả " + expiringList.size() + " lô...");
            moreLabel.putClientProperty(FlatClientProperties.STYLE, "foreground:#1976D2;font:bold");
            moreLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            moreLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Application.showForm(new FormCanhBaoHetHan());
                }
            });
            panel.add(moreLabel);
        }

        return panel;
    }

    private JPanel createStockAlertSubPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10, wrap", "[grow,fill]", "[]5[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:15;"
                + "background:lighten(#FFE0B2,5%)");

        JLabel titleLabel = new JLabel("Thuốc tồn kho thấp");
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold;foreground:#E65100");
        panel.add(titleLabel);

        List<ThuocTonThap> lowStockList = loThuocDAO.getThuocTonThap();

        int count = 0;
        for (ThuocTonThap thuoc : lowStockList) {
            if (count >= 3) break;
            String status = thuoc.getTonKho() == 0 ? "HẾT HÀNG" : "còn " + thuoc.getTonKho();
            String text = String.format("• %s: %s (min: %d)",
                    thuoc.getTenThuoc(),
                    status,
                    thuoc.getTonToiThieu());
            JLabel item = new JLabel(text);
            if (thuoc.getTonKho() == 0) {
                item.putClientProperty(FlatClientProperties.STYLE, "foreground:#D32F2F;font:bold");
            } else {
                item.putClientProperty(FlatClientProperties.STYLE, "foreground:#E65100");
            }
            panel.add(item);
            count++;
        }

        if (lowStockList.isEmpty()) {
            JLabel noData = new JLabel("Tồn kho ổn định");
            noData.putClientProperty(FlatClientProperties.STYLE, "foreground:#4CAF50");
            panel.add(noData);
        } else if (lowStockList.size() > 3) {
            JLabel moreLabel = new JLabel(">> Xem tất cả " + lowStockList.size() + " thuốc...");
            moreLabel.putClientProperty(FlatClientProperties.STYLE, "foreground:#1976D2;font:bold");
            moreLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            moreLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Application.showForm(new FormCanhBaoTonKho());
                }
            });
            panel.add(moreLabel);
        }

        return panel;
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
