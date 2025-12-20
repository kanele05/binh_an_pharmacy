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
import entities.DonViQuyDoi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import net.miginfocom.swing.MigLayout;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import raven.toast.Notifications;
import utils.MenuPermission;

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
        btnXuatExcel.addActionListener(e -> actionXuatExcel());

        panel.add(txtTimKiem, "w 250");
        panel.add(btnTim);
        panel.add(new JLabel("Lọc:"));
        panel.add(cbNhomThuoc);

        if (MenuPermission.isAdmin()) {
            panel.add(btnThem);
            panel.add(btnSua);
            panel.add(btnXoa);
            panel.add(btnKhoiPhuc);
        }
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
            ThuocFullInfo t = new ThuocFullInfo();
            t.setMaThuoc(data[0].toString());
            t.setTenThuoc(data[1].toString());
            t.setTenNhom(data[2].toString());
            t.setHoatChat(data[3].toString());
            t.setDonViCoBan(data[4].toString());

            String giaStr = data[6].toString().replace(".", "").replace(",", "").replace("₫", "").trim();
            t.setGiaBan(Double.parseDouble(giaStr));

            ArrayList<DonViQuyDoi> listDVQD = dialog.getListDonViQuyDoi();

            if (thuocDAO.themThuocMoi(t, listDVQD)) {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_RIGHT, "Thêm thuốc thành công!");
                loadData();
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_RIGHT, "Thêm thất bại!");
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
                // Get updated data
                Object[] newData = dialog.getData();
                ArrayList<DonViQuyDoi> listDVQD = dialog.getListDonViQuyDoi();

                ThuocFullInfo t = new ThuocFullInfo();
                t.setMaThuoc(newData[0].toString());
                t.setTenThuoc(newData[1].toString());
                t.setTenNhom(newData[2].toString());
                t.setHoatChat(newData[3].toString());
                t.setDonViCoBan(newData[4].toString());
                
                String giaStr = newData[6].toString().replace(".", "").replace(",", "").replace("₫", "").trim();
                t.setGiaBan(Double.parseDouble(giaStr));

                if (thuocDAO.updateThuoc(t, listDVQD)) {
                    loadData();
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Cập nhật thành công!");
                } else {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Cập nhật thất bại!");
                }
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

    private void actionXuatExcel() {
        if (comboFiltered.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Không có dữ liệu để xuất!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file Excel");
        fileChooser.setSelectedFile(new File("DanhSachThuoc_" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".xlsx"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }

            try {
                exportToExcel(file);
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Xuất Excel thành công!");

                int openFile = JOptionPane.showConfirmDialog(this, "Bạn có muốn mở file vừa xuất?", "Thông báo", JOptionPane.YES_NO_OPTION);
                if (openFile == JOptionPane.YES_OPTION) {
                    java.awt.Desktop.getDesktop().open(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi xuất Excel: " + e.getMessage());
            }
        }
    }

    private void exportToExcel(File file) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách thuốc");

        DecimalFormat moneyFormat = new DecimalFormat("#,##0");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        CellStyle currencyStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        currencyStyle.setDataFormat(format.getFormat("#,##0\" ₫\""));
        currencyStyle.setAlignment(HorizontalAlignment.RIGHT);
        currencyStyle.setBorderBottom(BorderStyle.THIN);
        currencyStyle.setBorderTop(BorderStyle.THIN);
        currencyStyle.setBorderLeft(BorderStyle.THIN);
        currencyStyle.setBorderRight(BorderStyle.THIN);

        CellStyle inactiveStyle = workbook.createCellStyle();
        Font inactiveFont = workbook.createFont();
        inactiveFont.setColor(IndexedColors.RED.getIndex());
        inactiveStyle.setFont(inactiveFont);
        inactiveStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        inactiveStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        inactiveStyle.setBorderBottom(BorderStyle.THIN);
        inactiveStyle.setBorderTop(BorderStyle.THIN);
        inactiveStyle.setBorderLeft(BorderStyle.THIN);
        inactiveStyle.setBorderRight(BorderStyle.THIN);

        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DANH SÁCH THUỐC");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

        Row dateRow = sheet.createRow(rowNum++);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Ngày xuất: " + LocalDate.now().format(dtf));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"STT", "Mã Thuốc", "Tên Thuốc", "Nhóm", "Hoạt Chất", "ĐVT", "Giá Nhập", "Giá Bán", "Tồn Kho", "Trạng Thái"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int stt = 1;
        for (ThuocFullInfo t : comboFiltered) {
            Row dataRow = sheet.createRow(rowNum++);
            boolean isInactive = !t.isTrangThai();
            CellStyle rowStyle = isInactive ? inactiveStyle : dataStyle;

            Cell sttCell = dataRow.createCell(0);
            sttCell.setCellValue(stt++);
            sttCell.setCellStyle(rowStyle);

            Cell maCell = dataRow.createCell(1);
            maCell.setCellValue(t.getMaThuoc());
            maCell.setCellStyle(rowStyle);

            Cell tenCell = dataRow.createCell(2);
            tenCell.setCellValue(t.getTenThuoc());
            tenCell.setCellStyle(rowStyle);

            Cell nhomCell = dataRow.createCell(3);
            nhomCell.setCellValue(t.getTenNhom());
            nhomCell.setCellStyle(rowStyle);

            Cell hcCell = dataRow.createCell(4);
            hcCell.setCellValue(t.getHoatChat());
            hcCell.setCellStyle(rowStyle);

            Cell dvtCell = dataRow.createCell(5);
            dvtCell.setCellValue(t.getDonViCoBan());
            dvtCell.setCellStyle(rowStyle);

            Cell giaNhapCell = dataRow.createCell(6);
            giaNhapCell.setCellValue(t.getGiaNhap());
            giaNhapCell.setCellStyle(isInactive ? inactiveStyle : currencyStyle);

            Cell giaBanCell = dataRow.createCell(7);
            giaBanCell.setCellValue(t.getGiaBan());
            giaBanCell.setCellStyle(isInactive ? inactiveStyle : currencyStyle);

            Cell tonKhoCell = dataRow.createCell(8);
            tonKhoCell.setCellValue(t.getTonKho());
            tonKhoCell.setCellStyle(rowStyle);

            Cell trangThaiCell = dataRow.createCell(9);
            trangThaiCell.setCellValue(t.isTrangThai() ? "Đang kinh doanh" : "Ngừng kinh doanh");
            trangThaiCell.setCellStyle(rowStyle);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.close();
        workbook.close();
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
