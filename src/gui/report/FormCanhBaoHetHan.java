package gui.report;

import com.formdev.flatlaf.FlatClientProperties;
import dao.LoThuocDAO;
import dto.ThuocSapHetHan;
import java.awt.Color;
import java.awt.Component;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormCanhBaoHetHan extends JPanel {

    private JComboBox<String> cbKhoangThoiGian;
    private JTextField txtTimKiem;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lbCanhBaoDo;
    private JLabel lbTongSoLo;
    private LoThuocDAO loThuocDAO;
    private TableRowSorter<DefaultTableModel> sorter;

    public FormCanhBaoHetHan() {
        loThuocDAO = new LoThuocDAO();
        initComponents();
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow][]"));

        add(createHeaderPanel(), "wrap 20");
        add(createActionPanel(), "wrap 10");
        add(createTablePanel(), "grow");
        add(createFooterPanel(), "growx");

        loadData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow,fill]push[][]"));
        panel.setOpaque(false);

        JLabel lbTitle = new JLabel("Cảnh Báo Thuốc Sắp Hết Hạn");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");

        lbTongSoLo = new JLabel("Tổng: 0 lô");
        lbTongSoLo.putClientProperty(FlatClientProperties.STYLE, ""
                + "font:bold +2;"
                + "foreground:#1976D2;"
                + "background:#BBDEFB;"
                + "border:5,10,5,10;");
//                + "arc:10");
        lbTongSoLo.setOpaque(true);

        lbCanhBaoDo = new JLabel("0 lô cần xử lý gấp!");
        lbCanhBaoDo.putClientProperty(FlatClientProperties.STYLE, ""
                + "font:bold +2;"
                + "foreground:#D32F2F;"
                + "background:#FFCDD2;"
                + "border:5,10,5,10;");
//                + "arc:10");
        lbCanhBaoDo.setOpaque(true);

        panel.add(lbTitle);
        panel.add(lbTongSoLo, "gapright 10");
        panel.add(lbCanhBaoDo);
        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]10[]push[][]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        txtTimKiem = new JTextField();
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm kiếm theo tên, mã...");
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        cbKhoangThoiGian = new JComboBox<>(new String[]{
            "Hết hạn trong 30 ngày (Gấp)",
            "Hết hạn trong 3 tháng",
            "Hết hạn trong 6 tháng",
            "Đã hết hạn (Cần hủy)"
        });
        cbKhoangThoiGian.addActionListener(e -> loadData());

        JButton btnTaoPhieuHuy = new JButton("Tiêu hủy thuốc");
        btnTaoPhieuHuy.putClientProperty(FlatClientProperties.STYLE, "background:#D32F2F; foreground:#fff; font:bold");
        btnTaoPhieuHuy.addActionListener(e -> actionTieuHuyThuoc());

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> loadData());

        panel.add(txtTimKiem, "w 200!");
        panel.add(new JLabel("Lọc:"));
        panel.add(cbKhoangThoiGian);

        panel.add(btnRefresh);
        panel.add(btnTaoPhieuHuy);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] columns = {"Mã Thuốc", "Tên Thuốc", "Lô SX", "Hạn Sử Dụng", "Còn Lại (Ngày)", "Tồn Kho", "Hành Động Gợi Ý", "TrangThai"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Ẩn cột TrangThai (cột 7)
        table.getColumnModel().getColumn(7).setMinWidth(0);
        table.getColumnModel().getColumn(7).setMaxWidth(0);
        table.getColumnModel().getColumn(7).setPreferredWidth(0);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);

        // Renderer cho cột Tên Thuốc để hiển thị "(Ngừng KD)"
        NgungKDRenderer ngungKDRenderer = new NgungKDRenderer();
        table.getColumnModel().getColumn(0).setCellRenderer(ngungKDRenderer); // Mã Thuốc
        table.getColumnModel().getColumn(1).setCellRenderer(new TenThuocRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(ngungKDRenderer); // Lô SX
        table.getColumnModel().getColumn(3).setCellRenderer(new ExpiryHighlightRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new ExpiryHighlightRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new RightAlignRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(ngungKDRenderer); // Hành Động Gợi Ý

        panel.add(new JScrollPane(table));
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 5"));
        panel.setOpaque(false);

        JLabel note1 = new JLabel("■ Đỏ: Đã hết hạn hoặc < 30 ngày");
        note1.setForeground(new Color(211, 47, 47));

        JLabel note2 = new JLabel("■ Cam: Còn 1 - 3 tháng");
        note2.setForeground(new Color(230, 81, 0));

        JLabel note3 = new JLabel("■ Vàng: Còn 3 - 6 tháng");
        note3.setForeground(new Color(245, 127, 23));

        JLabel noteNgungKD = new JLabel("■ Nền hồng + (Ngừng KD): Thuốc ngừng kinh doanh");
        noteNgungKD.setForeground(new Color(211, 47, 47));

        panel.add(new JLabel("Chú thích:"));
        panel.add(note1, "gapleft 10");
        panel.add(note2, "gapleft 10");
        panel.add(note3, "gapleft 10");
        panel.add(noteNgungKD, "gapleft 10");

        return panel;
    }

    private void loadData() {
        model.setRowCount(0);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        int selectedIndex = cbKhoangThoiGian.getSelectedIndex();
        List<ThuocSapHetHan> list;

        switch (selectedIndex) {
            case 0: // 30 ngày
                list = loThuocDAO.getThuocSapHetHan(30);
                break;
            case 1: // 3 tháng
                list = loThuocDAO.getThuocSapHetHan(90);
                break;
            case 2: // 6 tháng
                list = loThuocDAO.getThuocSapHetHan(180);
                break;
            case 3: // Đã hết hạn
                list = loThuocDAO.getThuocDaHetHan();
                break;
            default:
                list = loThuocDAO.getThuocSapHetHan(30);
        }

        for (ThuocSapHetHan thuoc : list) {
            String suggestion = getSuggestion(thuoc.getSoNgayConLai());
            model.addRow(new Object[]{
                thuoc.getMaThuoc(),
                thuoc.getTenThuoc(),
                thuoc.getMaLo(),
                thuoc.getHanSuDung().format(df),
                thuoc.getSoNgayConLai(),
                thuoc.getSoLuongTon(),
                suggestion,
                thuoc.isTrangThaiThuoc() // cột ẩn - trạng thái thuốc
            });
        }

        countCriticalItems();
        lbTongSoLo.setText("Tổng: " + model.getRowCount() + " lô");
    }

    private String getSuggestion(long daysLeft) {
        if (daysLeft < 0) {
            return "Hủy bỏ ngay";
        } else if (daysLeft < 30) {
            return "Giảm giá sâu / Trả hàng";
        } else if (daysLeft < 90) {
            return "Khuyến mãi";
        } else {
            return "Theo dõi";
        }
    }

    private void countCriticalItems() {
        int count = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            long days = Long.parseLong(model.getValueAt(i, 4).toString());
            if (days < 30) {
                count++;
            }
        }
        lbCanhBaoDo.setText(count + " lô cần xử lý gấp (< 30 ngày)!");
        lbCanhBaoDo.setVisible(count > 0);
    }

    private void filterTable() {
        String text = txtTimKiem.getText().trim().toLowerCase();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void actionTieuHuyThuoc() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn các lô thuốc cần tiêu hủy!");
            return;
        }

        StringBuilder sb = new StringBuilder("Danh sách lô thuốc sẽ bị tiêu hủy:\n");
        for (int row : rows) {
            int modelRow = table.convertRowIndexToModel(row);
            sb.append("- Mã lô: ").append(model.getValueAt(modelRow, 2))
              .append(" (").append(model.getValueAt(modelRow, 1)).append(")\n");
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                sb.toString() + "\nBạn có chắc chắn muốn tiêu hủy " + rows.length + " lô thuốc này?\nHành động này sẽ xóa lô khỏi kho và không thể hoàn tác.",
                "Xác nhận tiêu hủy", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean hasError = false;
            for (int i = rows.length - 1; i >= 0; i--) {
                int modelRow = table.convertRowIndexToModel(rows[i]);
                String maLo = model.getValueAt(modelRow, 2).toString();
                try {
                    if (!loThuocDAO.delete(maLo)) {
                        hasError = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    hasError = true;
                }
            }
            
            if (!hasError) {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã tiêu hủy các lô thuốc thành công!");
            } else {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Đã tiêu hủy nhưng có một số lô gặp lỗi!");
            }
            loadData();
        }
    }

    private class TenThuocRenderer extends DefaultTableCellRenderer {

        private final Color NGUNG_KD_BG = new Color(255, 235, 238); // Nền hồng nhạt
        private final Color NGUNG_KD_FG = new Color(211, 47, 47);   // Chữ đỏ

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            try {
                int modelRow = table.convertRowIndexToModel(row);
                Object trangThaiObj = model.getValueAt(modelRow, 7); // Cột ẩn TrangThai
                boolean trangThaiThuoc = trangThaiObj != null && (Boolean) trangThaiObj;

                if (!trangThaiThuoc) {
                    // Thuốc ngừng kinh doanh
                    String tenThuoc = value != null ? value.toString() : "";
                    setText(tenThuoc + " (Ngừng KD)");
                    if (!isSelected) {
                        setBackground(NGUNG_KD_BG);
                        setForeground(NGUNG_KD_FG);
                    }
                    setFont(getFont().deriveFont(java.awt.Font.BOLD));
                } else {
                    if (!isSelected) {
                        setBackground(table.getBackground());
                        setForeground(table.getForeground());
                    }
                    setFont(table.getFont());
                }
            } catch (Exception e) {
                // Ignore errors
            }

            return com;
        }
    }

    private class ExpiryHighlightRenderer extends DefaultTableCellRenderer {

        private final Color NGUNG_KD_BG = new Color(255, 235, 238); // Nền hồng nhạt

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            try {
                int modelRow = table.convertRowIndexToModel(row);
                long days = Long.parseLong(model.getValueAt(modelRow, 4).toString());

                // Kiểm tra trạng thái thuốc
                Object trangThaiObj = model.getValueAt(modelRow, 7);
                boolean trangThaiThuoc = trangThaiObj != null && (Boolean) trangThaiObj;

                if (!trangThaiThuoc && !isSelected) {
                    // Thuốc ngừng kinh doanh - nền hồng
                    setBackground(NGUNG_KD_BG);
                } else if (!isSelected) {
                    setBackground(table.getBackground());
                }

                if (days < 0) {
                    com.setForeground(new Color(211, 47, 47));
                    com.setFont(com.getFont().deriveFont(java.awt.Font.BOLD));
                } else if (days <= 30) {
                    com.setForeground(new Color(211, 47, 47));
                } else if (days <= 90) {
                    com.setForeground(new Color(230, 81, 0));
                } else if (days <= 180) {
                    com.setForeground(new Color(245, 127, 23));
                } else {
                    com.setForeground(Color.BLACK);
                }

                if (isSelected) {
                    com.setForeground(Color.WHITE);
                }

            } catch (Exception e) {
            }

            if (column == 4) {
                setHorizontalAlignment(JLabel.CENTER);
            }

            return com;
        }
    }

    private class NgungKDRenderer extends DefaultTableCellRenderer {

        private final Color NGUNG_KD_BG = new Color(255, 235, 238); // Nền hồng nhạt

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            try {
                int modelRow = table.convertRowIndexToModel(row);
                Object trangThaiObj = model.getValueAt(modelRow, 7); // Cột ẩn TrangThai
                boolean trangThaiThuoc = trangThaiObj != null && (Boolean) trangThaiObj;

                if (!trangThaiThuoc && !isSelected) {
                    // Thuốc ngừng kinh doanh - nền hồng
                    setBackground(NGUNG_KD_BG);
                } else if (!isSelected) {
                    setBackground(table.getBackground());
                }
            } catch (Exception e) {
                // Ignore errors
            }

            return com;
        }
    }

    private class RightAlignRenderer extends DefaultTableCellRenderer {

        private final Color NGUNG_KD_BG = new Color(255, 235, 238); // Nền hồng nhạt

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);

            try {
                int modelRow = table.convertRowIndexToModel(row);
                Object trangThaiObj = model.getValueAt(modelRow, 7); // Cột ẩn TrangThai
                boolean trangThaiThuoc = trangThaiObj != null && (Boolean) trangThaiObj;

                if (!trangThaiThuoc && !isSelected) {
                    // Thuốc ngừng kinh doanh - nền hồng
                    setBackground(NGUNG_KD_BG);
                } else if (!isSelected) {
                    setBackground(table.getBackground());
                }
            } catch (Exception e) {
                // Ignore errors
            }

            return this;
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
