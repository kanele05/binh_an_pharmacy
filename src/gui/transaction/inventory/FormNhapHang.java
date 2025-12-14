package gui.transaction.inventory;

import com.formdev.flatlaf.FlatClientProperties;
import dao.NhaCungCapDAO;
import dao.PhieuNhapDAO;
import dao.ThuocDAO;
import dto.ThuocFullInfo;
import entities.ChiTietPhieuNhap;
import entities.NhaCungCap;
import entities.NhanVien;
import entities.PhieuNhap;
import entities.Thuoc;
import java.awt.Component;
import java.awt.Frame;
import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
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
    private boolean isUpdating = false;
    private DecimalFormat moneyFormat = new DecimalFormat("#,##0");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
        // Lấy tên người nhập từ Auth.user thay vì hardcode
        String tenNguoiNhap = (Auth.user != null) ? Auth.user.getHoTen() : "Chưa đăng nhập";
        txtNguoiNhap = new JTextField(tenNguoiNhap);
        txtNguoiNhap.setEditable(false);

        panel.add(new JLabel("Nhà cung cấp:"));
        panel.add(cbNhaCungCap, "w 300, wrap");

        panel.add(new JLabel("Người nhập:"));
        panel.add(txtNguoiNhap, "w 300");

        // Nút thêm thuốc thủ công
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
        btnImport.addActionListener(e -> actionImportExcel());

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
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {

            writer.write("\ufeff");

            writer.write("Mã thuốc,Tên thuốc,Đơn vị tính,Lô sản xuất,Hạn sử dụng,Số lượng,Giá nhập");
            writer.newLine();

            writer.write("T001,Paracetamol 500mg,Vỉ,A123,2025-12-25,100,3500");
            writer.newLine();

            writer.write("T002,Amoxicillin 500mg,Hộp,B456,2026-06-15,50,45000");
            writer.newLine();

            writer.write("T003,Vitamin C 1000mg,Lọ,C789,2025-01-30,200,25000");
            writer.newLine();

            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã lưu file mẫu thành công!");

            try {
                java.awt.Desktop.getDesktop().open(file);
            } catch (Exception e) {
            }

        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi khi lưu file!");
        }
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] columns = {
            "Mã thuốc", "Tên thuốc", "ĐVT",
            "Hạn SD",
            "Số lượng", "Giá nhập", "Thành tiền"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Cho phép sửa: Hạn SD (3), Số lượng (4), Giá nhập (5)
                return column == 3 || column == 4 || column == 5;
            }
        };

        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true; gridColor:#e0e0e0");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80);   // Mã thuốc
        table.getColumnModel().getColumn(1).setPreferredWidth(200);  // Tên thuốc
        table.getColumnModel().getColumn(2).setPreferredWidth(60);   // ĐVT
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // Hạn SD
        table.getColumnModel().getColumn(4).setPreferredWidth(80);   // Số lượng
        table.getColumnModel().getColumn(5).setPreferredWidth(100);  // Giá nhập
        table.getColumnModel().getColumn(6).setPreferredWidth(120);  // Thành tiền

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

    private class RightAlignRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);
            return this;
        }
    }

    private void actionThemThuoc() {
        // Hiển thị dialog chọn thuốc
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        DialogThemThuocNhap dialog = new DialogThemThuocNhap(parentFrame, this);
        dialog.setVisible(true);
    }

    // Method được gọi từ DialogThemThuocNhap để thêm thuốc vào bảng
    public void themThuocVaoBang(ThuocFullInfo thuoc, int soLuong, double donGia, LocalDate hanSuDung) {
        // Kiểm tra thuốc đã có trong giỏ chưa
        for (int i = 0; i < model.getRowCount(); i++) {
            String maThuocTrongBang = model.getValueAt(i, 0).toString();
            if (maThuocTrongBang.equals(thuoc.getMaThuoc())) {
                // Cập nhật số lượng nếu thuốc đã có
                int slCu = Integer.parseInt(model.getValueAt(i, 4).toString().replace(",", ""));
                int slMoi = slCu + soLuong;
                model.setValueAt(slMoi, i, 4);
                tinhTienHang();
                Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER,
                    "Đã cập nhật số lượng thuốc " + thuoc.getTenThuoc());
                return;
            }
        }

        // Thêm mới vào bảng
        Thuoc t = new Thuoc();
        t.setMaThuoc(thuoc.getMaThuoc());
        t.setTenThuoc(thuoc.getTenThuoc());
        t.setDonViTinh(thuoc.getDonViCoBan());

        ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
        ct.setThuoc(t);
        ct.setSoLuong(soLuong);
        ct.setDonGia(donGia);
        ct.setHanSuDung(hanSuDung);
        ct.setDonViTinh(thuoc.getDonViCoBan());
        ct.setThanhTien(soLuong * donGia);
        gioHang.add(ct);

        isUpdating = true;
        model.addRow(new Object[]{
            thuoc.getMaThuoc(),
            thuoc.getTenThuoc(),
            thuoc.getDonViCoBan(),
            hanSuDung.format(dateFormat),
            soLuong,
            formatMoney(donGia),
            formatMoney(soLuong * donGia)
        });
        isUpdating = false;
        tinhTienHang();

        Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
            "Đã thêm thuốc: " + thuoc.getTenThuoc());
    }

    private void actionXoaDong() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn dòng cần xóa!");
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
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã xóa dòng!");
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
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đã xóa tất cả!");
        }
    }

    private void actionImportExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file nhập hàng (.xlsx)");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xlsx", "xls"));

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            try {
                List<ChiTietPhieuNhap> listFromExcel = ExcelHelper.readPhieuNhapFromExcel(fileToOpen);

                if (listFromExcel.isEmpty()) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, "File Excel rỗng!");
                    return;
                }

                gioHang.clear();
                gioHang.addAll(listFromExcel);
                model.setRowCount(0);

                isUpdating = true;
                for (ChiTietPhieuNhap ct : gioHang) {
                    model.addRow(new Object[]{
                        ct.getThuoc().getMaThuoc(),
                        ct.getThuoc().getTenThuoc(),
                        ct.getThuoc().getDonViTinh(),
                        ct.getHanSuDung() != null ? ct.getHanSuDung().format(dateFormat) : "",
                        ct.getSoLuong(),
                        formatMoney(ct.getDonGia()),
                        formatMoney(ct.getThanhTien())
                    });
                }
                isUpdating = false;

                tinhTienHang();
                Notifications.getInstance().show(Notifications.Type.SUCCESS, "Đã tải " + listFromExcel.size() + " dòng từ Excel!");

            } catch (Exception e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, "Lỗi đọc file: " + e.getMessage());
            }
        }
    }

    private void tinhTienHang() {
        isUpdating = true;
        double tongCong = 0;
        try {
            for (int i = 0; i < model.getRowCount(); i++) {
                try {
                    String slStr = model.getValueAt(i, 4).toString().replace(",", "");
                    String giaStr = model.getValueAt(i, 5).toString().replace(".", "").replace(",", "").replace(" ₫", "").trim();

                    double sl = Double.parseDouble(slStr);
                    double gia = Double.parseDouble(giaStr);
                    double thanhTien = sl * gia;

                    model.setValueAt(formatMoney(thanhTien), i, 6);

                    tongCong += thanhTien;
                } catch (Exception e) {
                    // Skip invalid rows
                }
            }
            lbTongTien.setText(formatMoney(tongCong) + " ₫");
        } finally {
            isUpdating = false;
        }
    }

    private void syncGioHangFromTable() {
        gioHang.clear();
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                Thuoc t = new Thuoc();
                t.setMaThuoc(model.getValueAt(i, 0).toString());
                t.setTenThuoc(model.getValueAt(i, 1).toString());
                t.setDonViTinh(model.getValueAt(i, 2).toString());

                String hsdStr = model.getValueAt(i, 3).toString();
                LocalDate hsd;
                try {
                    hsd = LocalDate.parse(hsdStr, dateFormat);
                } catch (Exception e) {
                    try {
                        hsd = LocalDate.parse(hsdStr);
                    } catch (Exception e2) {
                        hsd = LocalDate.now().plusYears(1);
                    }
                }

                int sl = Integer.parseInt(model.getValueAt(i, 4).toString().replace(",", ""));
                double gia = Double.parseDouble(model.getValueAt(i, 5).toString().replace(".", "").replace(",", "").replace(" ₫", "").trim());

                ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
                ct.setThuoc(t);
                ct.setSoLuong(sl);
                ct.setDonGia(gia);
                ct.setHanSuDung(hsd);
                ct.setDonViTinh(t.getDonViTinh());
                ct.setThanhTien(sl * gia);
                gioHang.add(ct);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void actionLuuPhieu() {
        // Sync data từ table vào giỏ hàng
        syncGioHangFromTable();

        if (gioHang.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, "Chưa có dữ liệu để nhập!");
            return;
        }

        if (cbNhaCungCap.getSelectedItem() == null) {
            Notifications.getInstance().show(Notifications.Type.WARNING, "Vui lòng chọn nhà cung cấp!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận nhập kho " + gioHang.size() + " mặt hàng?", "Thông báo", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PhieuNhap pn = new PhieuNhap();
                PhieuNhapDAO dao = new PhieuNhapDAO();
                NhanVien nv = (Auth.user != null) ? Auth.user : new NhanVien("NV001");

                pn.setMaPN(dao.generateNewMaPN());
                pn.setNgayTao(LocalDate.now());

                // Parse tổng tiền từ label
                String tongTienStr = lbTongTien.getText().replace(".", "").replace(",", "").replace(" ₫", "").trim();
                pn.setTongTien(Double.parseDouble(tongTienStr));
                pn.setNhanVien(nv);

                NhaCungCap ncc = (NhaCungCap) cbNhaCungCap.getSelectedItem();
                pn.setNcc(ncc);
                pn.setGhiChu(txtGhiChu.getText().isEmpty() ? "Nhập hàng từ hệ thống" : txtGhiChu.getText());

                boolean success = dao.createPhieuNhap(pn, gioHang);

                if (success) {
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, "Nhập kho thành công! Mã phiếu: " + pn.getMaPN());
                    // Reset form
                    model.setRowCount(0);
                    gioHang.clear();
                    lbTongTien.setText("0 ₫");
                    txtGhiChu.setText("");
                } else {
                    Notifications.getInstance().show(Notifications.Type.ERROR, "Lỗi khi lưu vào CSDL!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, "Lỗi hệ thống: " + e.getMessage());
            }
        }
    }

    private String formatMoney(double money) {
        return moneyFormat.format(money);
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
