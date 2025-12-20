package gui.report;

import com.formdev.flatlaf.FlatClientProperties;
import dao.LoThuocDAO;
import dto.ThuocTonThap;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import net.miginfocom.swing.MigLayout;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import raven.toast.Notifications;

public class FormCanhBaoTonKho extends JPanel {

    private JTextField txtTimKiem;
    private JComboBox<String> cbNhomThuoc;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lbCanhBao;
    private JLabel lbTongSo;
    private LoThuocDAO loThuocDAO;
    private TableRowSorter<DefaultTableModel> sorter;

    public FormCanhBaoTonKho() {
        loThuocDAO = new LoThuocDAO();
        initComponents();
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow][]"));

        add(createHeaderPanel(), "wrap 20");
        add(createToolBarPanel(), "wrap 10");
        add(createTablePanel(), "grow");
        add(createFooterPanel(), "growx");

        loadNhomThuoc();
        loadData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow,fill]push[][]"));
        panel.setOpaque(false);

        JLabel lbTitle = new JLabel("Cảnh Báo Tồn Kho Thấp & Hết Hàng");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");

        lbTongSo = new JLabel("Tổng: 0 thuốc");
        lbTongSo.putClientProperty(FlatClientProperties.STYLE, ""
                + "font:bold +2;"
                + "foreground:#1976D2;"
                + "background:#BBDEFB;"
                + "border:5,10,5,10;");
//                + "arc:10");
        lbTongSo.setOpaque(true);

        lbCanhBao = new JLabel("0 mã đã hết sạch hàng!");
        lbCanhBao.putClientProperty(FlatClientProperties.STYLE, ""
                + "font:bold +2;"
                + "foreground:#D32F2F;"
                + "background:#FFCDD2;"
                + "border:5,10,5,10;");
//                + "arc:10");
        lbCanhBao.setOpaque(true);

        panel.add(lbTitle);
        panel.add(lbTongSo, "gapright 10");
        panel.add(lbCanhBao);
        return panel;
    }

    private JPanel createToolBarPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]10[]push[][][]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        txtTimKiem = new JTextField();
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm tên thuốc, mã...");
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        cbNhomThuoc = new JComboBox<>();
        cbNhomThuoc.addActionListener(e -> loadData());

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> loadData());

        JButton btnTaoDuTru = new JButton("Tạo dự trù nhập hàng");
        btnTaoDuTru.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
        btnTaoDuTru.addActionListener(e -> actionTaoDuTru());

        JButton btnXuatExcel = new JButton("Xuất Excel");
        btnXuatExcel.putClientProperty(FlatClientProperties.STYLE, "background:#009688; foreground:#fff; font:bold");
        btnXuatExcel.addActionListener(e -> actionExport());

        panel.add(txtTimKiem, "w 200!");
        panel.add(new JLabel("Nhóm:"));
        panel.add(cbNhomThuoc, "w 150!");

        panel.add(btnRefresh);
        panel.add(btnTaoDuTru);
        panel.add(btnXuatExcel);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] columns = {"Mã Thuốc", "Tên Thuốc", "ĐVT", "Định Mức Min", "Tồn Hiện Tại", "Gợi Ý Nhập", "Nhà Cung Cấp"};
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

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);

        table.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new StockHighlightRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new RightAlignRenderer());

        panel.add(new JScrollPane(table));
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 5"));
        panel.setOpaque(false);

        JLabel note1 = new JLabel("■ Đỏ: Hết hàng (Tồn = 0)");
        note1.setForeground(new Color(211, 47, 47));

        JLabel note2 = new JLabel("■ Cam: Dưới định mức tối thiểu");
        note2.setForeground(new Color(230, 81, 0));

        panel.add(new JLabel("Chú thích:"));
        panel.add(note1, "gapleft 10");
        panel.add(note2, "gapleft 10");

        return panel;
    }

    private void loadNhomThuoc() {
        cbNhomThuoc.removeAllItems();
        cbNhomThuoc.addItem("Tất cả nhóm");
        List<String> nhomList = loThuocDAO.getAllNhomThuoc();
        for (String nhom : nhomList) {
            cbNhomThuoc.addItem(nhom);
        }
    }

    private void loadData() {
        model.setRowCount(0);

        String selectedNhom = (String) cbNhomThuoc.getSelectedItem();
        List<ThuocTonThap> list;

        if (selectedNhom == null || selectedNhom.equals("Tất cả nhóm")) {
            list = loThuocDAO.getThuocTonThap();
        } else {
            list = loThuocDAO.getThuocTonThapByNhom(selectedNhom);
        }

        for (ThuocTonThap thuoc : list) {
            model.addRow(new Object[]{
                thuoc.getMaThuoc(),
                thuoc.getTenThuoc(),
                thuoc.getDonViTinh() != null ? thuoc.getDonViTinh() : "N/A",
                thuoc.getTonToiThieu(),
                thuoc.getTonKho(),
                thuoc.getSoLuongCanNhap(),
                thuoc.getNhaCungCap()
            });
        }

        countOutOfStock();
        lbTongSo.setText("Tổng: " + model.getRowCount() + " thuốc");
    }

    private void countOutOfStock() {
        int count = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            int ton = Integer.parseInt(model.getValueAt(i, 4).toString());
            if (ton == 0) {
                count++;
            }
        }

        if (count > 0) {
            lbCanhBao.setText("Nguy hiểm: " + count + " mặt hàng đã hết sạch trong kho!");
            lbCanhBao.setVisible(true);
        } else {
            lbCanhBao.setVisible(false);
        }
    }

    private void filterTable() {
        String text = txtTimKiem.getText().trim().toLowerCase();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void actionTaoDuTru() {
        if (model.getRowCount() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Không có dữ liệu để tạo dự trù!");
            return;
        }

        // Collect all displayed items
        java.util.List<ThuocTonThap> listThuoc = new java.util.ArrayList<>();
        for (int i = 0; i < table.getRowCount(); i++) {
            int modelRow = table.convertRowIndexToModel(i);
            String ma = model.getValueAt(modelRow, 0).toString();
            String ten = model.getValueAt(modelRow, 1).toString();
            int ton = Integer.parseInt(model.getValueAt(modelRow, 4).toString());
            int min = Integer.parseInt(model.getValueAt(modelRow, 3).toString());
            int canNhap = Integer.parseInt(model.getValueAt(modelRow, 5).toString());
            
            ThuocTonThap t = new ThuocTonThap(ma, ten, model.getValueAt(modelRow, 2).toString(), ton, min, canNhap, "", "");
            listThuoc.add(t);
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có muốn tạo phiếu nhập dự thảo cho " + listThuoc.size() + " thuốc này không?",
                "Tạo dự trù", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            gui.transaction.inventory.FormNhapHang formNhap = new gui.transaction.inventory.FormNhapHang();
            formNhap.addThuocTuCanhBao(listThuoc);
            
            gui.main.Application.showForm(formNhap);
            gui.main.Application.setSelectedMenu(3, 2, false); // Menu Nhập hàng -> Lập phiếu nhập (false = không trigger sự kiện)
        }
    }

    private void actionExport() {
        if (model.getRowCount() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Không có dữ liệu để xuất!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file");
        fileChooser.setSelectedFile(new File("CanhBaoTonKho.xlsx"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Cảnh Báo Tồn Kho");

                // Header style
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                // Create header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Mã Thuốc", "Tên Thuốc", "ĐVT", "Định Mức Min", "Tồn Hiện Tại", "Gợi Ý Nhập", "Nhà Cung Cấp"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Create data rows
                for (int i = 0; i < model.getRowCount(); i++) {
                    Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Cell cell = row.createCell(j);
                        Object value = model.getValueAt(i, j);
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            cell.setCellValue(value != null ? value.toString() : "");
                        }
                    }
                }

                // Auto-size columns
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Write to file
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                        "Đã xuất file thành công: " + file.getName());

            } catch (Exception e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                        "Lỗi khi xuất file: " + e.getMessage());
            }
        }
    }

    private class StockHighlightRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);

            try {
                int modelRow = table.convertRowIndexToModel(row);
                int ton = Integer.parseInt(model.getValueAt(modelRow, 4).toString());
                int min = Integer.parseInt(model.getValueAt(modelRow, 3).toString());

                if (ton == 0) {
                    com.setForeground(new Color(211, 47, 47));
                    com.setFont(com.getFont().deriveFont(java.awt.Font.BOLD));
                } else if (ton < min) {
                    com.setForeground(new Color(230, 81, 0));
                    com.setFont(com.getFont().deriveFont(java.awt.Font.BOLD));
                } else {
                    com.setForeground(Color.BLACK);
                }

                if (isSelected) {
                    com.setForeground(Color.WHITE);
                }

            } catch (Exception e) {
            }

            return com;
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
