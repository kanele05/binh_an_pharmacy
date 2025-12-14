package gui.transaction.order;

import com.formdev.flatlaf.FlatClientProperties;
import dao.*;
import entities.*;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;
import utils.Auth;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DialogThanhToanDonDat extends JDialog {

    private final DonDatHang donDatHang;
    private final Component parent;
    private boolean isSuccess = false;

    private JLabel lbMaHD, lbKhachHang, lbTongTienHang, lbGiamGia, lbVAT, lbThanhTien;
    private JCheckBox chkDungDiem;
    private JComboBox<String> cbHinhThucTT;
    private JTable table;
    private DefaultTableModel model;

    private final ChiTietDonDatDAO ctDonDAO = new ChiTietDonDatDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final LoThuocDAO loThuocDAO = new LoThuocDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final DonViQuyDoiDAO dvqdDAO = new DonViQuyDoiDAO();
    private double tongTienHang = 0;
    private double tienGiam = 0;
    private double tienVAT = 0;
    private double tongThanhToan = 0;
    private final double VAT_RATE = 0.05;

    public DialogThanhToanDonDat(Component parent, DonDatHang donDatHang) {
        super(SwingUtilities.windowForComponent(parent), "Thanh Toán Đơn Đặt Hàng", ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.donDatHang = donDatHang;
        if (this.donDatHang.getKhachHang() != null) {
            String maKH = this.donDatHang.getKhachHang().getMaKH();

            KhachHang khFull = khachHangDAO.getKhachHangByID(maKH);
            if (khFull != null) {
                this.donDatHang.setKhachHang(khFull);
            }
        }
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new MigLayout("wrap, fill, insets 20, width 800", "[grow]", "[][grow][]"));

        JLabel lbTitle = new JLabel("XÁC NHẬN LẤY HÀNG & THANH TOÁN");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +6; foreground:$Accent.color");
        add(lbTitle, "center, wrap 20");

        JPanel pInfo = new JPanel(new MigLayout("insets 10, fillx", "[][grow]push[][grow]", "[]5[]"));
        pInfo.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:darken(@background,3%)");

        lbMaHD = new JLabel("Mã HĐ: (Tự tạo)");
        lbMaHD.putClientProperty(FlatClientProperties.STYLE, "font:bold");

        String tenKhach = donDatHang.getKhachHang() != null ? donDatHang.getKhachHang().getTenKH() : donDatHang.getTenKhach();
        lbKhachHang = new JLabel(tenKhach);
        lbKhachHang.putClientProperty(FlatClientProperties.STYLE, "font:bold");

        pInfo.add(new JLabel("Đơn đặt hàng:"));
        pInfo.add(new JLabel(donDatHang.getMaDonDat(), JLabel.LEFT));
        pInfo.add(new JLabel("Mã HĐ Mới:"));
        pInfo.add(lbMaHD, "wrap");

        pInfo.add(new JLabel("Khách hàng:"));
        pInfo.add(lbKhachHang);
        pInfo.add(new JLabel("SĐT:"));
        pInfo.add(new JLabel(donDatHang.getSdtLienHe()), "wrap");

        add(pInfo, "growx");

        String[] cols = {"Mã Thuốc", "Tên Thuốc", "ĐVT", "SL", "Đơn Giá", "Thành Tiền", "Lô Xuất"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getColumnModel().getColumn(4).setCellRenderer(new RightAlignRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new RightAlignRenderer());

        add(new JScrollPane(table), "grow, h 250!");

        JPanel pPay = new JPanel(new MigLayout("insets 15", "[grow][right]", "[]5[]5[]5[]10[]"));
        pPay.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:lighten(#E8F5E9,5%)");

        lbTongTienHang = new JLabel("0 ₫");
        lbGiamGia = new JLabel("0 ₫");
        lbVAT = new JLabel("0 ₫");
        lbThanhTien = new JLabel("0 ₫");
        lbThanhTien.putClientProperty(FlatClientProperties.STYLE, "font:bold +6; foreground:#D32F2F");

        chkDungDiem = new JCheckBox("Dùng điểm tích lũy");
        if (donDatHang.getKhachHang() != null && donDatHang.getKhachHang().getDiemTichLuy() > 0) {
            int diem = donDatHang.getKhachHang().getDiemTichLuy();
            chkDungDiem.setText("Dùng " + diem + " điểm (-" + formatMoney(diem * 100) + ")");
        } else {
            chkDungDiem.setEnabled(false);
        }
        chkDungDiem.addActionListener(e -> calculateTotal());

        pPay.add(new JLabel("Tổng tiền hàng:"));
        pPay.add(lbTongTienHang, "wrap");

        pPay.add(chkDungDiem, "split 2");
        pPay.add(new JLabel("Giảm giá:"), "gapleft push");
        pPay.add(lbGiamGia, "wrap");

        pPay.add(new JLabel("Thuế VAT (5%):"));
        pPay.add(lbVAT, "wrap");

        pPay.add(new JSeparator(), "span 2, growx, wrap");

        pPay.add(new JLabel("THANH TOÁN:"));
        pPay.add(lbThanhTien, "wrap");

        add(pPay, "growx");

        JPanel pAction = new JPanel(new MigLayout("insets 0", "[]push[][]"));
        pAction.setOpaque(false);

        cbHinhThucTT = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản"});

        JButton btnHuy = new JButton("Hủy bỏ");
        btnHuy.addActionListener(e -> dispose());

        JButton btnXacNhan = new JButton("THANH TOÁN & HOÀN TẤT");
        btnXacNhan.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold +2");
        btnXacNhan.addActionListener(e -> actionPayment());

        pAction.add(new JLabel("Hình thức TT:"), "split 2");
        pAction.add(cbHinhThucTT);
        pAction.add(btnHuy);
        pAction.add(btnXacNhan);

        add(pAction, "growx");

        generateMaHD();
    }

    private void loadData() {
        List<ChiTietDonDat> listCT = ctDonDAO.getChiTietByMaDon(donDatHang.getMaDonDat());
        model.setRowCount(0);
        tongTienHang = 0;

        for (ChiTietDonDat ct : listCT) {
            String maThuoc = ct.getThuoc().getMaThuoc();
            String tenThuoc = ct.getThuoc().getTenThuoc();
            String donViDat = ct.getDonViTinh();
            double donGiaDat = ct.getDonGia();
            String donViCoBan = ct.getThuoc().getDonViTinh();
            int heSoQuyDoi = 1;
            DonViQuyDoi dvqd = dvqdDAO.getDonViByTen(maThuoc, donViDat);
            if (dvqd != null) {
                heSoQuyDoi = dvqd.getGiaTriQuyDoi();

            }
            int soLuongDat = ct.getSoLuong();
            int tongSoLuongCoBanCan = soLuongDat * heSoQuyDoi;

            List<LoThuoc> listLo = loThuocDAO.getDanhSachLoKhaDung(maThuoc);

            int soLuongDaLay = 0;

            for (LoThuoc lo : listLo) {
                if (soLuongDaLay >= tongSoLuongCoBanCan) {
                    break;
                }
                int slCanLay = tongSoLuongCoBanCan - soLuongDaLay;
                int slLayTuLoNay = Math.min(slCanLay, lo.getSoLuongTon());
                double soLuongHienThi = (double) slLayTuLoNay / heSoQuyDoi;
                double thanhTienHienThi = soLuongHienThi * donGiaDat;
                model.addRow(new Object[]{
                    maThuoc,
                    tenThuoc + (heSoQuyDoi > 1 ? " (Quy đổi từ " + donViDat + ")" : ""),
                    donViDat,
                    formatQty(soLuongHienThi),
                    formatMoney(donGiaDat),
                    formatMoney(thanhTienHienThi),
                    lo.getMaLo()
                });

                tongTienHang += thanhTienHienThi;
                soLuongDaLay += slLayTuLoNay;
            }

            if (soLuongDaLay < tongSoLuongCoBanCan) {
                int thieu = tongSoLuongCoBanCan - soLuongDaLay;
                double thieuHienThi = (double) thieu / heSoQuyDoi;
                double tienThieu = thieuHienThi * donGiaDat;
                model.addRow(new Object[]{
                    maThuoc,
                    tenThuoc + " (THIẾU HÀNG)",
                    donViDat,
                    formatQty(thieu),
                    formatMoney(donGiaDat),
                    formatMoney(tienThieu),
                    "KHÔNG ĐỦ TỒN"
                });
                tongTienHang += tienThieu;
            }
        }
        calculateTotal();
    }

    private void calculateTotal() {
        tienGiam = 0;
        if (chkDungDiem.isSelected() && donDatHang.getKhachHang() != null) {

            double diemQuyDoi = donDatHang.getKhachHang().getDiemTichLuy() * 100;
            if (diemQuyDoi > tongTienHang * 0.5) {
                tienGiam = tongTienHang * 0.5;
            } else {
                tienGiam = diemQuyDoi;
            }
        }

        double sauGiam = tongTienHang - tienGiam;
        tienVAT = sauGiam * VAT_RATE;
        tongThanhToan = sauGiam + tienVAT;

        lbTongTienHang.setText(formatMoney(tongTienHang));
        lbGiamGia.setText(formatMoney(tienGiam));
        lbVAT.setText(formatMoney(tienVAT));
        lbThanhTien.setText(formatMoney(tongThanhToan));
    }

    private void actionPayment() {

        for (int i = 0; i < model.getRowCount(); i++) {
            String maLo = model.getValueAt(i, 6).toString();
            if (maLo.equals("KHÔNG ĐỦ TỒN")) {
                Notifications.getInstance().show(Notifications.Type.ERROR,
                        "Không đủ hàng trong kho để đáp ứng đơn đặt này!");
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận thanh toán " + lbThanhTien.getText() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        HoaDon hd = new HoaDon();
        hd.setMaHD(lbMaHD.getText());
        hd.setNgayTao(LocalDateTime.now());
        hd.setTongTien(tongTienHang);
        hd.setGiamGia(tienGiam);
        hd.setThue(tienVAT);
        hd.setHinhThucTT(cbHinhThucTT.getSelectedItem().toString());
        hd.setNhanVien(Auth.isLogin() ? Auth.user : new NhanVien("NV001"));
        hd.setKhachHang(donDatHang.getKhachHang());

        List<ChiTietHoaDon> listCTHD = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String maThuoc = model.getValueAt(i, 0).toString();
            double slDouble = parseQty(model.getValueAt(i, 3).toString());
            int sl = (int) Math.round(slDouble);
            double donGia = parseMoney(model.getValueAt(i, 4).toString());
            double thanhTien = parseMoney(model.getValueAt(i, 5).toString());
            String maLo = model.getValueAt(i, 6).toString();
            String donViTinh = model.getValueAt(i, 2).toString();
            ChiTietHoaDon ct = new ChiTietHoaDon(hd, new Thuoc(maThuoc), new LoThuoc(maLo, null, null, null, 0, "", false), sl, donGia, thanhTien, donViTinh);
            listCTHD.add(ct);
        }

        if (hoaDonDAO.taoHoaDonTuDonDat(hd, listCTHD, donDatHang.getMaDonDat())) {

            if (chkDungDiem.isSelected() && donDatHang.getKhachHang() != null) {
                try {
                    khachHangDAO.truDiem(donDatHang.getKhachHang().getMaKH(), donDatHang.getKhachHang().getDiemTichLuy());
                } catch (Exception e) {
                }
            }

            if (donDatHang.getKhachHang() != null) {
                int diemMoi = (int) (tongThanhToan / 10000);
                try {
                    khachHangDAO.congDiem(donDatHang.getKhachHang().getMaKH(), diemMoi);
                } catch (Exception e) {
                }
            }

            Notifications.getInstance().show(Notifications.Type.SUCCESS, "Thanh toán thành công! Đã chuyển đổi đơn hàng.");
            isSuccess = true;
            dispose();
        } else {
            Notifications.getInstance().show(Notifications.Type.ERROR, "Lỗi khi tạo hóa đơn!");
        }
    }

    private String formatQty(double qty) {
        if (qty == (long) qty) {
            return String.format("%d", (long) qty);
        }
        return String.format("%.1f", qty);
    }

    private double parseQty(String text) {
        try {
            return Double.parseDouble(text.replace(",", "."));
        } catch (Exception e) {
            return 0;
        }
    }

    private void generateMaHD() {
        String maxID = hoaDonDAO.getMaxMaHD();

        if (maxID == null) {
            lbMaHD.setText("HD001");
        } else {
            try {
                int num = Integer.parseInt(maxID.substring(2)) + 1;
                lbMaHD.setText(String.format("HD%03d", num));
            } catch (Exception e) {
                lbMaHD.setText("HD" + System.currentTimeMillis());
            }
        }
    }

    private String formatMoney(double amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + " ₫";
    }

    private double parseMoney(String text) {
        try {
            return Double.parseDouble(text.replace(".", "").replace(",", "").replace(" ₫", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    private class RightAlignRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);
            return this;
        }
    }

    public void packAndCenter() {
        pack();
        setLocationRelativeTo(parent);
    }
}
