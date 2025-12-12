package gui.manage.price;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.formdev.flatlaf.FlatClientProperties;
import dao.BangGiaDAO;
import entities.BangGia;
import gui.manage.price.DialogBangGia;
import java.awt.Color;
import java.awt.Component;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormBangGia extends JPanel {

    private JTextField txtTimKiem;
    private JTable table;

    private EventList<BangGia> dsBangGia;
    private FilterList<BangGia> textFiltered;
    private EventTableModel<BangGia> tableModel;

    private BangGiaDAO bgDAO = new BangGiaDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FormBangGia() {
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
        JLabel lbTitle = new JLabel("Quản Lý Bảng Giá Bán");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        panel.add(lbTitle);
        return panel;
    }

    private JPanel createToolBarPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]push[]10[]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        txtTimKiem = new JTextField();
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm tên bảng giá...");
        JButton btnTaoMoi = new JButton("Tạo bảng giá mới");
        btnTaoMoi.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold");
        btnTaoMoi.addActionListener(e -> actionTaoMoi());

        JButton btnTim = new JButton("Tìm kiếm");

        JButton btnApDung = new JButton("Áp dụng bảng giá này");
        btnApDung.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
        btnApDung.addActionListener(e -> {
            try {
                actionApDung();
            } catch (SQLException ex) {
                Logger.getLogger(FormBangGia.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        JButton btnSua = new JButton("Chi tiết / Sửa");
        btnSua.addActionListener(e -> actionSua());

        panel.add(txtTimKiem, "w 250");
        panel.add(btnTim);
        panel.add(btnTaoMoi);
        panel.add(btnApDung);
        panel.add(btnSua);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        dsBangGia = new BasicEventList<>();

        TextFilterator<BangGia> textFilterator = new TextFilterator<BangGia>() {
            @Override
            public void getFilterStrings(List<String> baseList, BangGia bg) {
                baseList.add(bg.getMaBG());
                baseList.add(bg.getTenBG());
            }
        };
        TextComponentMatcherEditor<BangGia> textMatcherEditor = new TextComponentMatcherEditor<>(txtTimKiem, textFilterator);
        textFiltered = new FilterList<>(dsBangGia, textMatcherEditor);

        TableFormat<BangGia> tableFormat = new TableFormat<BangGia>() {
            @Override
            public int getColumnCount() {
                return 6;
            }

            @Override
            public String getColumnName(int i) {
                String[] columns = {"Mã BG", "Tên Bảng Giá", "Hiệu Lực Từ", "Hiệu Lực Đến", "Ghi Chú", "Trạng Thái"};
                return columns[i];
            }

            @Override
            public Object getColumnValue(BangGia bg, int i) {
                switch (i) {
                    case 0:
                        return bg.getMaBG();
                    case 1:
                        return bg.getTenBG();
                    case 2:
                        return bg.getNgayHieuLuc();
                    case 3:
                        return bg.getNgayKetThuc();
                    case 4:
                        return bg.getGhiChu();
                    case 5:
                        return bg.isTrangThai() ? "Đang áp dụng" : "Ngừng áp dụng";
                    default:
                        return null;
                }
            }
        };

        tableModel = new EventTableModel<>(textFiltered, tableFormat);
        table = new JTable(tableModel);

        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        DateRenderer dateRenderer = new DateRenderer();
        table.getColumnModel().getColumn(2).setCellRenderer(dateRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(dateRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());

        panel.add(new JScrollPane(table));
        return panel;
    }

    private class DateRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof LocalDate) {
                setText(((LocalDate) value).format(dateFormatter));
            } else if (value == null) {
                setText("---");
            }
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    }

    private class StatusRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value != null ? value.toString() : "";

            if (status.equals("Đang áp dụng")) {
                com.setForeground(new Color(56, 142, 60));
                com.setFont(com.getFont().deriveFont(java.awt.Font.BOLD));
            } else {
                com.setForeground(Color.GRAY);
                com.setFont(com.getFont().deriveFont(java.awt.Font.PLAIN));
            }
            if (isSelected) {
                com.setForeground(Color.WHITE);
            }
            return com;
        }
    }

    private void loadData() {
        dsBangGia.clear();
        ArrayList<BangGia> list = bgDAO.getAllTblBangGia();
        dsBangGia.addAll(list);
    }

    private void actionTaoMoi() {

        DialogBangGia dialog = new DialogBangGia(this, null);
        dialog.setVisible(true);
        if (dialog.isSave()) {
            loadData();
        }
    }

    private void actionSua() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn bảng giá để xem chi tiết!");
            return;
        }

        BangGia selectedBG = textFiltered.get(row);

        String ngayKT = selectedBG.getNgayKetThuc() != null ? selectedBG.getNgayKetThuc().format(dateFormatter) : "";
        String ghiChu = selectedBG.getGhiChu() != null ? selectedBG.getGhiChu() : "";
        Object[] data = {
            selectedBG.getMaBG(),
            selectedBG.getTenBG(),
            selectedBG.getNgayHieuLuc().format(dateFormatter),
            ngayKT,
            ghiChu
        };

        DialogBangGia dialog = new DialogBangGia(this, data);
        dialog.setVisible(true);

        if (dialog.isSave()) {
            loadData();
        }
    }

    private void actionApDung() throws SQLException {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn bảng giá muốn áp dụng!");
            return;
        }

        BangGia selectedBG = textFiltered.get(row);

        if (selectedBG.isTrangThai()) {
            Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Bảng giá này đang được áp dụng rồi!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Hệ thống sẽ cập nhật giá bán của toàn bộ thuốc theo bảng giá:\n" + selectedBG.getTenBG() + "\nTiếp tục?",
                "Xác nhận thay đổi giá", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (bgDAO.activateBangGia(selectedBG.getMaBG())) {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã áp dụng bảng giá mới thành công!");
                loadData();
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi khi áp dụng bảng giá!");
            }
        }
    }

    public void openThemMoi() {
        actionTaoMoi();
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
