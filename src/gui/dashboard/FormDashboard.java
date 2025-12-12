package gui.dashboard;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import net.miginfocom.swing.MigLayout;

public class FormDashboard extends javax.swing.JPanel {

    public FormDashboard() {
        initComponents();
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fillx,insets 20", "[fill]", "[][][][]"));

        JLabel lbTitle = new JLabel("Dashboard - Tổng quan");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        add(lbTitle, "wrap 20");

        JPanel statsPanel = new JPanel(new MigLayout("insets 0", "[grow,fill][grow,fill][grow,fill][grow,fill]", "[]"));
        statsPanel.add(createStatCard("Doanh thu hôm nay", "15.500.000 ₫", "#4CAF50", "trend_up.svg"));
        statsPanel.add(createStatCard("Đơn hàng hôm nay", "45", "#2196F3", "cart.svg"));
        statsPanel.add(createStatCard("Sắp hết hạn", "12", "#FF9800", "warning.svg"));
        statsPanel.add(createStatCard("Tồn kho thấp", "8", "#F44336", "inventory.svg"));
        add(statsPanel, "wrap 20");

        JPanel contentPanel = new JPanel(new MigLayout("insets 0", "[60%][40%]", "[grow,fill]"));
        contentPanel.add(createRevenueChartPanel(), "grow");
        contentPanel.add(createTopMedicinesPanel(), "grow");
        add(contentPanel, "grow,push");

        add(createAlertPanel(), "wrap");
    }

    private JPanel createStatCard(String title, String value, String color, String icon) {
        JPanel card = new JPanel(new MigLayout("insets 15", "[]push[]", "[]5[]"));
        card.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        JLabel lbTitle = new JLabel(title);
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:-1;foreground:darken(@foreground,30%)");

        JLabel lbValue = new JLabel(value);
        lbValue.putClientProperty(FlatClientProperties.STYLE, "font:bold +6");

        JLabel lbIcon = new JLabel();
        lbIcon.putClientProperty(FlatClientProperties.STYLE,
                "foreground:" + color);

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

        JLabel chartPlaceholder = new JLabel("[ Biểu đồ doanh thu ]", JLabel.CENTER);
        chartPlaceholder.putClientProperty(FlatClientProperties.STYLE,
                "foreground:darken(@foreground,50%)");
        panel.add(chartPlaceholder, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTopMedicinesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Top 5 thuốc bán chạy");
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {"Thuốc", "SL"};
        Object[][] data = {
            {"Paracetamol 500mg", "120"},
            {"Vitamin C 1000mg", "95"},
            {"Amoxicillin 500mg", "87"},
            {"Omeprazole 20mg", "76"},
            {"Ibuprofen 400mg", "65"}
        };

        JTable table = new JTable(data, columns);
        table.putClientProperty(FlatClientProperties.STYLE, "showHorizontalLines:false");
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAlertPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 15,wrap", "[grow,fill]", "[]5[]5[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:lighten(#FFF3E0,3%)");

        JLabel title = new JLabel("⚠ Cảnh báo cần xử lý");
        title.putClientProperty(FlatClientProperties.STYLE,
                "font:bold +1;foreground:#F57C00");
        panel.add(title);

        panel.add(createAlertItem("Paracetamol 500mg (Lô A123) sẽ hết hạn sau 2 tháng"));
        panel.add(createAlertItem("Amoxicillin 250mg còn 15 hộp (dưới mức tồn kho tối thiểu)"));

        return panel;
    }

    private JLabel createAlertItem(String text) {
        JLabel label = new JLabel("• " + text);
        label.putClientProperty(FlatClientProperties.STYLE, "foreground:#E65100");
        return label;
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
