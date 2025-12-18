package gui.transaction.inventory;

import com.formdev.flatlaf.FlatClientProperties;
import dao.ChiTietPhieuNhapDAO;
import dao.DonViQuyDoiDAO;
import dao.LoThuocDAO;
import dao.NhaCungCapDAO;
import dao.NhomThuocDAO;
import dao.PhieuNhapDAO;
import dao.ThuocDAO;
import dto.ThuocFullInfo;
import entities.ChiTietPhieuNhap;
import entities.DonViQuyDoi;
import entities.LoThuoc;
import entities.NhaCungCap;
import entities.NhanVien;
import entities.NhomThuoc;
import entities.PhieuNhap;
import entities.Thuoc;
import gui.manage.partner.DialogNhaCungCap;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import static org.apache.poi.ss.usermodel.CellType.BLANK;
import static org.apache.poi.ss.usermodel.CellType.BOOLEAN;
import static org.apache.poi.ss.usermodel.CellType.FORMULA;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.apache.poi.ss.usermodel.CellType.STRING;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import raven.toast.Notifications;
import utils.Auth;
import utils.ExcelHelper;

public class FormNhapHang extends javax.swing.JPanel {

    private JComboBox<NhaCungCap> cbNhaCungCap;
    private JTextField txtNguoiNhap;
    private JTextField txtGhiChu;
    private JLabel lbTongTien;
    private JTable table;
    private DefaultTableModel model;
    private ArrayList<ChiTietPhieuNhap> gioHang = new ArrayList<>();
    private NhaCungCapDAO nccDAO = new NhaCungCapDAO();
    private ThuocDAO thuocDAO = new ThuocDAO();
    private LoThuocDAO ltDao = new LoThuocDAO();
    private NhomThuocDAO ntDao = new NhomThuocDAO();
    private PhieuNhapDAO pnDao = new PhieuNhapDAO();
    private ChiTietPhieuNhapDAO ct = new ChiTietPhieuNhapDAO();
    private boolean isUpdating = false;
    private DecimalFormat moneyFormat = new DecimalFormat("#,##0");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private NhaCungCapDAO nccDao = new NhaCungCapDAO();
    private int nextMaLoNumber = -1;

    public FormNhapHang() {
        initComponents();
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow][]"));

        add(createHeaderPanel(), "wrap 20");

        add(createActionPanel(), "wrap 10");

        add(createTablePanel(), "grow");

        add(createFooterPanel(), "growx");
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow,fill][]"));
        panel.setOpaque(false);
        JLabel lbTitle = new JLabel("Nhập Hàng & Quản Lý Lô");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        panel.add(lbTitle);
        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 15", "[][grow]push[][][][]", "[]10[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        cbNhaCungCap = new JComboBox<>();
        ArrayList<NhaCungCap> listNCC = nccDAO.getAllNhaCungCap();
        for (NhaCungCap ncc : listNCC) {
            cbNhaCungCap.addItem(ncc);
        }
        String tenNguoiNhap = (Auth.user != null) ? Auth.user.getHoTen() : "Chưa đăng nhập";
        txtNguoiNhap = new JTextField(tenNguoiNhap);
        txtNguoiNhap.setEditable(false);
        JButton btnAddNCC = new JButton("+");
        btnAddNCC.putClientProperty(FlatClientProperties.STYLE,
                "arc:15; background:#FFFFFF; font:bold;");

        btnAddNCC.setMargin(new Insets(2, 10, 2, 10));
        btnAddNCC.addActionListener(e -> actionThem());
        panel.add(new JLabel("Nhà cung cấp:"));
        panel.add(cbNhaCungCap, "w 300, split 2");
        panel.add(btnAddNCC, "gapleft 5, wrap");
        panel.add(new JLabel("Người nhập:"));
        panel.add(txtNguoiNhap, "w 300");

        // Nút thêm thuốc thủ công
        initNextMaLo();
        JButton btnThemThuoc = new JButton("+ Thêm thuốc");
        btnThemThuoc.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:#2196F3;"
                + "foreground:#ffffff;"
                + "font:bold;"
                + "arc:8");
        btnThemThuoc.addActionListener(e -> actionThemThuoc());

        // Nút xóa dòng
        JButton btnXoaDong = new JButton("Xóa dòng");
        btnXoaDong.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:#f44336;"
                + "foreground:#ffffff;"
                + "font:bold;"
                + "arc:8");
        btnXoaDong.addActionListener(e -> actionXoaDong());

        JButton btnMau = new JButton("Tải file mẫu");
        btnMau.addActionListener(e -> actionDownloadTemplate());

        JButton btnImport = new JButton("Nhập từ Excel");
        btnImport.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:#009688;"
                + "foreground:#ffffff;"
                + "font:bold;"
                + "arc:8");
        btnImport.addActionListener(e -> importExcel());

        panel.add(btnThemThuoc, "cell 2 0, span 1 2");
        panel.add(btnXoaDong, "cell 3 0, span 1 2");
        panel.add(btnMau, "cell 4 0, span 1 2");
        panel.add(btnImport, "cell 5 0, span 1 2");

        return panel;
    }

    private void actionDownloadTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file mẫu nhập hàng");
        fileChooser.setSelectedFile(new File("Mau_Nhap_Hang.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            saveCSVFile(fileToSave);
        }
    }

    private void saveCSVFile(File file) {
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(file),
                        java.nio.charset.StandardCharsets.UTF_8))) {

            writer.write("\ufeff");

            writer.write(
                    "Mã thuốc,Tên thuốc,Đơn vị tính,Hoạt chất,Nhóm thuốc,Đơn giá,Lô 1,Hạn sử dụng 1, Số lượng 1,Lô 2, Hạn sử dụng 2");
            writer.newLine();

            writer.write(
                    "T001,Paracetamol 500mg,Viên,Paracetamol,Giảm đau hạ sốt,1500,L001,2026-12-31,100,LO002,2027-06-30");
            writer.newLine();

            writer.write(
                    "T002,Amoxicillin 500mg,Viên,Amoxicillin,Kháng sinh,2500,L010,2026-08-15,200,LO011,2027-02-20");
            writer.newLine();

            writer.write("T003,Vitamin C 500mg,Viên,Acid Ascorbic,Vitamin,1200,L021,2027-01-10,300,LO022,2027-09-05");
            writer.newLine();

            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                    "Đã lưu file mẫu thành công!");

            try {
                java.awt.Desktop.getDesktop().open(file);
            } catch (Exception e) {
            }

        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                    "Lỗi khi lưu file!");
        }
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] columns = {
                "Mã thuốc", "Tên thuốc", "Đơn vị tính", "Hoạt chất", "Nhóm thuốc",
                "Đơn giá", "Số lượng tổng", "Số lô",
                "Số lượng lô", "Hạn sử dụng", "Thành tiền"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Cho phép sửa: Hạn SD (3), Số lượng (4), Giá nhập (5)
                return column == 3 || column == 4 || column == 5;
            }
        };

        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE,
                "rowHeight:30; showHorizontalLines:true; gridColor:#e0e0e0");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80); // Mã thuốc
        table.getColumnModel().getColumn(1).setPreferredWidth(200); // Tên thuốc
        table.getColumnModel().getColumn(2).setPreferredWidth(60); // ĐVT
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Hạn SD
        table.getColumnModel().getColumn(4).setPreferredWidth(80); // Số lượng
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Giá nhập
        table.getColumnModel().getColumn(6).setPreferredWidth(120); // Thành tiền

        table.getColumnModel().getColumn(4).setCellRenderer(new RightAlignRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new RightAlignRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new RightAlignRenderer());

        model.addTableModelListener(e -> {
            if (!isUpdating) {
                tinhTienHang();
            }
        });

        panel.add(new JScrollPane(table));
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 15, fillx", "[grow][][200!]", "[]10[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:lighten(#E8F5E9,3%)");

        txtGhiChu = new JTextField();
        txtGhiChu.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ghi chú phiếu nhập...");

        lbTongTien = new JLabel("0 ₫");
        lbTongTien.putClientProperty(FlatClientProperties.STYLE, "font:bold +10; foreground:#D32F2F");

        JButton btnXoaHet = new JButton("Xóa tất cả");
        btnXoaHet.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:#ff9800;"
                + "foreground:#ffffff;"
                + "font:bold;"
                + "borderWidth:0;"
                + "arc:10");
        btnXoaHet.addActionListener(e -> actionXoaHet());

        JButton btnLuu = new JButton("LƯU PHIẾU NHẬP");
        btnLuu.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:#4CAF50;"
                + "foreground:#ffffff;"
                + "font:bold +2;"
                + "borderWidth:0;"
                + "arc:10");
        btnLuu.addActionListener(e -> actionLuuPhieu());

        panel.add(new JLabel("Ghi chú:"), "split 2, gapright 10");
        panel.add(txtGhiChu, "growx");

        panel.add(new JLabel("TỔNG CỘNG:"), "right");
        panel.add(lbTongTien, "right, wrap");

        panel.add(btnXoaHet, "span 1, w 120, h 50!");
        panel.add(btnLuu, "span 2, growx, h 50!");

        return panel;
    }

        private void actionThem() {
        DialogNhaCungCap dialog = new DialogNhaCungCap(this, null);
        dialog.setVisible(true);

        if (!dialog.isSave()) {
            return;
        }

        NhaCungCap ncc = dialog.getNhaCungCap();
        if (nccDAO.insert(ncc)) {
            loadNhaCungCapCombo();
            for (int i = 0; i < cbNhaCungCap.getItemCount(); i++) {
                NhaCungCap item = cbNhaCungCap.getItemAt(i);
                if (item.getMaNCC().equals(ncc.getMaNCC())) {
                    cbNhaCungCap.setSelectedIndex(i);
                    break;
                }
            }
            Notifications.getInstance().show(
                    Notifications.Type.SUCCESS,
                    Notifications.Location.TOP_CENTER,
                    "Thêm nhà cung cấp thành công!");
        } else {
            Notifications.getInstance().show(
                    Notifications.Type.ERROR,
                    Notifications.Location.TOP_CENTER,
                    "Thêm nhà cung cấp thất bại!");
        }
    }
    private void loadNhaCungCapCombo() {
        cbNhaCungCap.removeAllItems();
        ArrayList<NhaCungCap> listNCC = nccDAO.getAllNhaCungCap();
        for (NhaCungCap ncc : listNCC) {
            cbNhaCungCap.addItem(ncc);
        }
    }
    private class RightAlignRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);
            return this;
        }
    }

    private void actionThemThuoc() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        DialogThemThuocNhap dialog = new DialogThemThuocNhap(parentFrame, this);
        dialog.setVisible(true);
    }

    public void themThuocVaoBang(ThuocFullInfo thuoc, int soLuong, double donGia, LocalDate hanSuDung, String donViTinh) {
        try {
            isUpdating = true;

            // === BƯỚC 1: Lấy tỷ lệ quy đổi ===
            String donViCoBan = thuoc.getDonViCoBan();
            int giaTriQuyDoi = 1;
            if (!donViTinh.equals(donViCoBan)) {
                DonViQuyDoiDAO dvqDao = new DonViQuyDoiDAO();
                DonViQuyDoi dvq = dvqDao.getDonViByTen(thuoc.getMaThuoc(), donViTinh);
                if (dvq != null) {
                    giaTriQuyDoi = dvq.getGiaTriQuyDoi();
                }
            }

            // === BƯỚC 2: Quy đổi về đơn vị cơ bản ===
            int soLuongCoBan = soLuong * giaTriQuyDoi;
            double donGiaCoBan = donGia / giaTriQuyDoi;

            // === BƯỚC 3: Tìm dòng cùng maThuoc + cùng HSD ===
            int existingRow = -1;
            String hsdStr = hanSuDung.format(dateFormat);
            for (int i = 0; i < model.getRowCount(); i++) {
                String maThuocTrongBang = getCell(i, 0);
                String hsdTrongBang = getCell(i, 9);  // Cột HSD

                if (maThuocTrongBang.equals(thuoc.getMaThuoc()) && hsdTrongBang.equals(hsdStr)) {
                    existingRow = i;
                    break;
                }
            }

            // === BƯỚC 4: Cộng dồn hoặc thêm mới ===
            if (existingRow >= 0) {
                // Cộng dồn số lượng (đã là đơn vị cơ bản)
                int slLoCu = parseIntFromCell(getCell(existingRow, 8));
                int slLoMoi = slLoCu + soLuongCoBan;

                double donGiaCu = parseMoney(getCell(existingRow, 5));
                model.setValueAt(slLoMoi, existingRow, 8);
                model.setValueAt(slLoMoi, existingRow, 6);

                double thanhTienMoi = slLoMoi * donGiaCu;
                model.setValueAt(formatMoney(thanhTienMoi), existingRow, 10);

                Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER,
                        "Đã cộng dồn " + soLuongCoBan + " " + donViCoBan + " vào lô cùng HSD");
            } else {
                // Thêm dòng mới với đơn vị cơ bản
                String maLoMoi = getNextMaLo();
                double thanhTien = soLuongCoBan * donGiaCoBan;

                model.addRow(new Object[] {
                        thuoc.getMaThuoc(),
                        thuoc.getTenThuoc(),
                        donViCoBan,             // Đơn vị cơ bản
                        thuoc.getHoatChat(),
                        thuoc.getTenNhom(),
                        formatMoney(donGiaCoBan), // Giá đơn vị cơ bản
                        soLuongCoBan,
                        maLoMoi,
                        soLuongCoBan,
                        hsdStr,
                        formatMoney(thanhTien)
                });

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                        "Đã thêm 1 lô cho thuốc: " + thuoc.getTenThuoc());
            }
        } finally {
            isUpdating = false;
        }
        tinhTienHang();
    }

    private void actionXoaDong() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Vui lòng chọn dòng cần xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa dòng này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (selectedRow < gioHang.size()) {
                gioHang.remove(selectedRow);
            }
            model.removeRow(selectedRow);
            tinhTienHang();
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                    "Đã xóa dòng!");
        }
    }

    private void actionXoaHet() {
        if (model.getRowCount() == 0) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa tất cả?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            model.setRowCount(0);
            gioHang.clear();
            lbTongTien.setText("0 ₫");
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                    "Đã xóa tất cả!");
        }
    }
    private void importExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file Excel");

        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        File file = fileChooser.getSelectedFile();

        try (FileInputStream fis = new FileInputStream(file);
                Workbook workbook = file.getName().toLowerCase().endsWith(".xlsx")
                        ? new XSSFWorkbook(fis)
                        : new HSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            model.setRowCount(0);
            final int LOT_START_COL = 7;

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null)
                    continue;

                String ma = getCellString(row.getCell(0));
                String ten = getCellString(row.getCell(1));
                String dvt = getCellString(row.getCell(2));
                String hoatChat = getCellString(row.getCell(3));
                String nhomThuoc = getCellString(row.getCell(4));

                Double donGia = getCellNumber(row.getCell(5));
                Double tongSL = getCellNumber(row.getCell(6));

                int lastCellNum = row.getLastCellNum();

                for (int c = LOT_START_COL; c + 2 < lastCellNum; c += 3) {
                    String soLo = getCellString(row.getCell(c));
                    Date hsdDate = getCellDate(row.getCell(c + 1));
                    Double slLo = getCellNumber(row.getCell(c + 2));

                    boolean allEmpty = (soLo == null || soLo.trim().isEmpty()) &&
                            hsdDate == null &&
                            (slLo == null || slLo <= 0);
                    if (allEmpty)
                        continue;

                    double thanhTien = (donGia != null ? donGia : 0)
                            * (slLo != null ? slLo : 0);

                    String hsdFormatted = hsdDate == null ? ""
                            : DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    .format(hsdDate.toInstant()
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate());

                    model.addRow(new Object[] {
                            ma,
                            ten == null || ten.isEmpty() ? "Không tên" : ten,
                            dvt == null || dvt.isEmpty() ? "Hộp" : dvt,
                            hoatChat == null ? "" : hoatChat,
                            nhomThuoc == null ? "" : nhomThuoc,
                            formatMoney(donGia),
                            tongSL,
                            soLo,
                            slLo,
                            hsdFormatted,
                            formatMoney(thanhTien)
                    });
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Import thành công " + model.getRowCount() + " lô thuốc!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            tinhTienHang();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi đọc file: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actionLuuPhieu() {
        try {
            List<String> errors = validateBeforeSave();
            if (!errors.isEmpty()) {
                StringBuilder msg = new StringBuilder("<html><b>Dữ liệu không hợp lệ:</b><br><br>");
                for (int i = 0; i < Math.min(15, errors.size()); i++) {
                    msg.append("").append(errors.get(i)).append("<br>");
                }
                if (errors.size() > 15) {
                    msg.append("... và ").append(errors.size() - 15).append(" lỗi khác.<br>");
                }
                msg.append("</html>");
                JOptionPane.showMessageDialog(this, msg.toString(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Xác nhận lưu phiếu nhập với " + model.getRowCount() + " dòng?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            luuDanhSachThuoc();
            String maPN = pnDao.generateNewMaPN();
            PhieuNhap pn = taoPhieuNhap(maPN);

            if (!pnDao.insertHeader(pn)) {
                throw new Exception("Không thể lưu thông tin phiếu nhập!");
            }
            luuChiTietVaLoThuoc(pn);

            JOptionPane.showMessageDialog(this,
                    "Lưu phiếu nhập thành công!\nMã phiếu: " + pn.getMaPN(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            model.setRowCount(0);
            gioHang.clear();
            lbTongTien.setText("0 ₫");
            txtGhiChu.setText("");
            nextMaLoNumber = -1;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi lưu phiếu nhập: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private PhieuNhap taoPhieuNhap(String maPhieuNhap) {
        PhieuNhap pn = new PhieuNhap();
        pn.setMaPN(maPhieuNhap);
        pn.setNgayTao(LocalDate.now());
        pn.setTongTien(tinhTienHang2());
        pn.setTrangThai("Chờ Nhập");

        NhanVien nv = (Auth.user != null) ? Auth.user : new NhanVien("NV001");
        pn.setNhanVien(nv);

        NhaCungCap nccSelected = (NhaCungCap) cbNhaCungCap.getSelectedItem();
        if (nccSelected != null) {
            pn.setNcc(nccSelected);
        }

        pn.setGhiChu(txtGhiChu.getText().trim());

        return pn;
    }

    private void luuChiTietVaLoThuoc(PhieuNhap pn) throws Exception {
        for (int i = 0; i < model.getRowCount(); i++) {
            String maThuoc = getCell(i, 0).trim();
            if (maThuoc.isEmpty()) {
                continue;
            }

            String tenThuoc = getCell(i, 1);
            String donViTinh = getCell(i, 2);
            String hoatChat = getCell(i, 3);
            String tenNhom = getCell(i, 4);
            double donGia = parseMoney(getCell(i, 5));
            String soLo = getCell(i, 7);
            int soLuongLo = parseIntFromCell(getCell(i, 8));
            LocalDate hsd = LocalDate.parse(getCell(i, 9), dateFormat);

            Thuoc thuoc = thuocDAO.getThuocById(maThuoc);
            if (thuoc == null) {
                throw new Exception("Thuốc có mã '" + maThuoc
                        + "' chưa tồn tại trong hệ thống. Vui lòng kiểm tra lại dữ liệu thuốc.");
            }
            if (ltDao.isLoThuocDaTonTai(soLo)) {
                throw new Exception("Lô '" + soLo + "' đã tồn tại trong hệ thống!");
            }
            LoThuoc lo = new LoThuoc();
            lo.setMaLo(soLo);
            lo.setThuoc(thuoc);
            lo.setNgayNhap(LocalDate.now());
            lo.setHanSuDung(hsd);
            lo.setSoLuongTon(soLuongLo);
            lo.setTrangThai("Còn Hạn");
            lo.setIsDeleted(false);
            ltDao.insertLoThuoc(lo);

            ChiTietPhieuNhap ctNhap = new ChiTietPhieuNhap();
            ctNhap.setPn(pn);
            ctNhap.setThuoc(thuoc);
            ctNhap.setLoThuoc(lo);
            ctNhap.setHanSuDung(hsd);
            ctNhap.setSoLuong(soLuongLo);
            ctNhap.setDonGia(donGia);
            ctNhap.setThanhTien(donGia * soLuongLo);
            ctNhap.setDonViTinh(donViTinh);

            ct.insert(ctNhap);
        }
    }

    private void luuDanhSachThuoc() throws Exception {
        for (int i = 0; i < model.getRowCount(); i++) {
            String maThuoc = getCell(i, 0).trim();
            String tenThuoc = getCell(i, 1).trim();
            String dvt = getCell(i, 2).trim();
            String hoatChat = getCell(i, 3).trim();
            String tenNhom = getCell(i, 4).trim();

            if (maThuoc.isEmpty() && tenThuoc.isEmpty()) {
                continue;
            }
            if (thuocDAO.getThuocById(maThuoc) != null) {
                continue;
            }
            NhomThuoc nhom = ntDao.getByTen(tenNhom);
            if (nhom == null) {
                throw new Exception("Nhóm thuốc '" + tenNhom + "' cho mã thuốc '" + maThuoc + "' không tồn tại.");
            }

            Thuoc thuoc = new Thuoc();
            thuoc.setMaThuoc(maThuoc);
            thuoc.setTenThuoc(tenThuoc);
            thuoc.setHoatChat(hoatChat);
            thuoc.setDonViCoBan(dvt);
            thuoc.setTrangThai(true);
            thuoc.setNhomThuoc(nhom);

            if (!thuocDAO.insertThuoc(thuoc)) {
                throw new Exception("Không thể lưu thông tin thuốc mới có mã '" + maThuoc + "'.");
            }
        }
    }

    private void tinhTienHang() {
        isUpdating = true;
        try {
            double tongCong = tinhTongTienBang();
            lbTongTien.setText(formatMoney(tongCong) + " ₫");
        } finally {
            isUpdating = false;
        }
    }

    private double tinhTienHang2() {
        return tinhTongTienBang();
    }

    private double tinhTongTienBang() {
        final int COL_DON_GIA = 5;
        final int COL_SO_LUONG_LO = 8;
        final int COL_THANH_TIEN = 10;

        double tongCong = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                Object giaObj = model.getValueAt(i, COL_DON_GIA);
                Object slLoObj = model.getValueAt(i, COL_SO_LUONG_LO);

                if (giaObj == null || slLoObj == null) {
                    continue;
                }

                double gia = parseMoney(giaObj.toString());
                int soLuongLo = parseIntFromCell(slLoObj.toString());
                if (gia <= 0 || soLuongLo <= 0) {
                    continue;
                }

                double thanhTien = gia * soLuongLo;
                model.setValueAt(formatMoney(thanhTien), i, COL_THANH_TIEN);
                tongCong += thanhTien;
            } catch (Exception e) {
            }
        }
        return tongCong;
    }

    private List<String> validateBeforeSave() {
        List<String> errors = new ArrayList<>();

        if (model.getRowCount() == 0) {
            errors.add("Chưa có dòng dữ liệu nào trong bảng phiếu nhập.");
        }
        if (cbNhaCungCap.getSelectedItem() == null) {
            errors.add("Vui lòng chọn nhà cung cấp.");
        }
        if (Auth.user == null) {
            errors.add("Không xác định được nhân viên nhập (Auth.user = null). Vui lòng đăng nhập lại.");
        }
        Set<String> lotKeys = new HashSet<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            int rowIndex = i + 1;
            String maThuoc = getCell(i, 0);
            String tenThuoc = getCell(i, 1);
            String dvt = getCell(i, 2);
            String hoatChat = getCell(i, 3);
            String tenNhom = getCell(i, 4);
            String donGiaStr = getCell(i, 5);
            String soLo = getCell(i, 7);
            String soLuongLoStr = getCell(i, 8);
            String hsdStr = getCell(i, 9);

            if (maThuoc.isEmpty() && tenThuoc.isEmpty() && soLo.isEmpty()) {
                continue;
            }
            if (maThuoc.isEmpty()) {
                errors.add("Dòng " + rowIndex + ": Mã thuốc không được để trống.");
            }
            if (tenThuoc.isEmpty()) {
                errors.add("Dòng " + rowIndex + ": Tên thuốc không được để trống.");
            }
            if (dvt.isEmpty()) {
                errors.add("Dòng " + rowIndex + ": Đơn vị tính không được để trống.");
            }
            double donGia = parseMoney(donGiaStr);
            if (donGia <= 0) {
                errors.add("Dòng " + rowIndex + ": Đơn giá phải lớn hơn 0.");
            }
            if (soLo.isEmpty()) {
                errors.add("Dòng " + rowIndex + ": Số lô không được để trống.");
            }
            int soLuongLo = parseIntFromCell(soLuongLoStr);
            if (soLuongLo <= 0) {
                errors.add("Dòng " + rowIndex + ": Số lượng lô phải > 0.");
            }
            try {
                LocalDate hsd = LocalDate.parse(hsdStr, dateFormat);
                if (!hsd.isAfter(LocalDate.now())) {
                    errors.add("Dòng " + rowIndex + ": Hạn sử dụng phải lớn hơn ngày hiện tại.");
                }
            } catch (Exception ex) {
                errors.add("Dòng " + rowIndex + ": Hạn sử dụng không hợp lệ, định dạng phải là dd/MM/yyyy.");
            }
            if (!maThuoc.isEmpty() && thuocDAO.getThuocById(maThuoc) == null) {
                if (tenNhom.isEmpty()) {
                    errors.add("Dòng " + rowIndex + ": Thuốc mới phải có Nhóm thuốc.");
                } else if (ntDao.getByTen(tenNhom) == null) {
                    errors.add("Dòng " + rowIndex + ": Nhóm thuốc '" + tenNhom + "' không tồn tại trong hệ thống.");
                }
            }
            if (!soLo.isEmpty()) {
                String key = soLo;
                if (!lotKeys.add(key)) {
                    errors.add("Dòng " + rowIndex + ": Lô '" + soLo
                            + "' bị trùng với dòng khác trong phiếu.");
                }
                if (ltDao.isLoThuocDaTonTai(soLo)) {
                    errors.add("Dòng " + rowIndex + ": Lô '" + soLo
                            + "' đã tồn tại trong hệ thống.");
                }
            }
        }
        return errors;
    }

    // Khởi tạo bộ sinh mã lô: lấy maLo lớn nhất trong DB rồi +1 làm điểm bắt đầu
    private void initNextMaLo() {
        try {
            String maxMaLo = ltDao.getMaxMaLo();
            if (maxMaLo == null || maxMaLo.trim().isEmpty()) {
                nextMaLoNumber = 1;
            } else {
                String digits = maxMaLo.replaceAll("\\D+", "");
                if (digits.isEmpty()) {
                    nextMaLoNumber = 1;
                } else {
                    nextMaLoNumber = Integer.parseInt(digits) + 1;
                }
            }
        } catch (Exception e) {
            nextMaLoNumber = 1;
        }
    }

    // Lấy mã lô tiếp theo trong phiên nhập hiện tại (không phụ thuộc đã lưu DB hay
    // chưa)
    private String getNextMaLo() {
        if (nextMaLoNumber < 1) {
            initNextMaLo();
        }
        String ma = String.format("L%03d", nextMaLoNumber);
        nextMaLoNumber++;
        return ma;
    }

    private static class ExcelImportError {
        final int row;
        final String message;

        ExcelImportError(int row, String message) {
            this.row = row;
            this.message = message;
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null)
            return "";
        CellType type = cell.getCellType();
        switch (type) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date d = cell.getDateCellValue();
                    return d == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(d);
                }
                double num = cell.getNumericCellValue();
                if (num == (long) num) {
                    return String.valueOf((long) num);
                } else {
                    DecimalFormat df = new DecimalFormat("#.######");
                    return df.format(num);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    double val = cell.getNumericCellValue();
                    if (val == (long) val)
                        return String.valueOf((long) val);
                    return String.valueOf(val);
                } catch (Exception ex) {
                    return cell.getStringCellValue();
                }
            case BLANK:
            default:
                return "";
        }
    }

    private double parseMoney(String moneyStr) {
        if (moneyStr == null || moneyStr.trim().isEmpty()) {
            return 0.0;
        }
        String clean = moneyStr
                .replace("đ", "")
                .replace("Đ", "")
                .replace(".", "")
                .replace(",", "")
                .replace(" ", "")
                .trim();
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            System.err.println("Lỗi parse tiền: " + moneyStr);
            return 0.0;
        }
    }

    private String getCell(int row, int col) {
        Object val = model.getValueAt(row, col);
        return val == null ? "" : val.toString().trim();
    }

    private Double getCellNumber(Cell cell) {
        if (cell == null)
            return 0.0;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().replaceAll("[^0-9\\.,-]", "").replace(",", "");
                if (s.isEmpty())
                    return 0.0;
                return Double.parseDouble(s);
            } else if (cell.getCellType() == CellType.FORMULA) {
                return cell.getNumericCellValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private Date getCellDate(Cell cell) {
        if (cell == null)
            return null;
        try {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim();
                try {
                    return new SimpleDateFormat("dd/MM/yyyy").parse(s);
                } catch (Exception ex) {
                    return null;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private String formatDate(Date date) {
        if (date == null)
            return "";
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    private String formatMoney(double money) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(money).replace(",", ".") + " đ";
    }

    private int parseIntFromCell(String raw) {
        if (raw == null)
            return 0;
        String s = raw.trim().replaceAll("\\s+", "");
        if (s.isEmpty())
            return 0;
        if (s.contains(".") && s.contains(",")) {
            s = s.replace(".", "");
            s = s.replace(',', '.');
        } else if (s.contains(",")) {
            s = s.replace(',', '.');
        } else {
            int firstDot = s.indexOf('.');
            int lastDot = s.lastIndexOf('.');
            if (firstDot != lastDot) {
                s = s.replace(".", "");
            }
        }
        double d;
        try {
            d = Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            String fallback = s.replaceAll("[^0-9.\\-]", "");
            if (fallback.isEmpty())
                return 0;
            d = Double.parseDouble(fallback);
        }
        return (int) Math.round(d);
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
