package gui.transaction.sales;

import com.formdev.flatlaf.FlatClientProperties;
import dao.ChiTietHoaDonDAO;
import entities.ChiTietHoaDon;
import entities.HoaDon;
import utils.InvoicePDFGenerator;
import java.awt.Component;
import java.awt.Cursor;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;

public class DialogChiTietHoaDon extends JDialog {

    private final HoaDon hoaDon;
    private final Component parent;
    private final ChiTietHoaDonDAO ctDAO = new ChiTietHoaDonDAO();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DefaultTableModel tableModel;

    public DialogChiTietHoaDon(Component parent, HoaDon hoaDon) {
        super(SwingUtilities.windowForComponent(parent), "Chi Tiết Hóa Đơn", ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.hoaDon = hoaDon;
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new MigLayout("wrap,fill,insets 20, width 700", "[fill]", "[][][grow][][]"));

        JLabel lbTitle = new JLabel("HÓA ĐƠN BÁN HÀNG");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +10; foreground:$Accent.color");
        add(lbTitle, "center, gapbottom 10");

        JPanel pInfo = new JPanel(new MigLayout("insets 0, fillx", "[][grow][][grow]", "[]5[]"));
        pInfo.setOpaque(false);

        addLabel(pInfo, "Mã hóa đơn:", hoaDon.getMaHD() != null ? hoaDon.getMaHD() : "");
        String ngayLap = (hoaDon.getNgayTao() != null) ? hoaDon.getNgayTao().format(dateTimeFormat) : "";
        addLabel(pInfo, "Ngày lập:", ngayLap);

        String tenKH = (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getTenKH() != null)
            ? hoaDon.getKhachHang().getTenKH() : "Khách lẻ";
        addLabel(pInfo, "Khách hàng:", tenKH);
        String tenNV = (hoaDon.getNhanVien() != null && hoaDon.getNhanVien().getHoTen() != null)
            ? hoaDon.getNhanVien().getHoTen() : "";
        addLabel(pInfo, "Nhân viên:", tenNV);

        add(pInfo, "gapbottom 10");

        String[] cols = {"STT", "Tên Thuốc", "Lô", "Đơn Vị", "Đơn Giá", "SL", "Thành Tiền"};

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:25; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:30; font:bold");

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(JLabel.RIGHT);

        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);
        table.getColumnModel().getColumn(4).setCellRenderer(right);
        table.getColumnModel().getColumn(5).setCellRenderer(center);
        table.getColumnModel().getColumn(6).setCellRenderer(right);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY));
        add(scroll, "grow, h 300!");

        JPanel pSum = new JPanel(new MigLayout("insets 10, fillx", "push[][]", "[]5[]5[]5[]"));
        pSum.putClientProperty(FlatClientProperties.STYLE, "background:darken(@background,3%); arc:10");

        pSum.add(new JLabel("Tổng tiền hàng:"));
        JLabel lbTongTien = new JLabel(currencyFormat.format(hoaDon.getTongTien()));
        lbTongTien.putClientProperty(FlatClientProperties.STYLE, "font:bold");
        pSum.add(lbTongTien, "wrap");

        pSum.add(new JLabel("Giảm giá (Điểm):"));
        JLabel lbGiamGia = new JLabel("-" + currencyFormat.format(hoaDon.getGiamGia()));
        lbGiamGia.putClientProperty(FlatClientProperties.STYLE, "foreground:#F44336");
        pSum.add(lbGiamGia, "wrap");

        pSum.add(new JLabel("Thuế VAT:"));
        JLabel lbThue = new JLabel("+" + currencyFormat.format(hoaDon.getThue()));
        pSum.add(lbThue, "wrap");

        pSum.add(new JSeparator(), "span 2, growx, wrap");

        pSum.add(new JLabel("TỔNG THANH TOÁN:"));
        double thucThu = hoaDon.getTongTien() + hoaDon.getThue() - hoaDon.getGiamGia();
        JLabel lbThucThu = new JLabel(currencyFormat.format(thucThu));
        lbThucThu.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:#4CAF50");
        pSum.add(lbThucThu);

        add(pSum, "growx");

        // Button panel
        JPanel pButtons = new JPanel(new MigLayout("insets 0", "push[][]", ""));
        pButtons.setOpaque(false);

        JButton btnPrint = new JButton("In PDF");
        btnPrint.putClientProperty(FlatClientProperties.STYLE, "foreground:#FFFFFF; background:#2196F3");
        btnPrint.addActionListener(e -> printInvoicePDF());
        pButtons.add(btnPrint, "gapright 10");

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        pButtons.add(btnClose);

        add(pButtons, "right");

        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Generate and open PDF invoice
     */
    private void printInvoicePDF() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            boolean success = InvoicePDFGenerator.generateAndOpenInvoice(hoaDon);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Đã tạo hóa đơn PDF thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Không thể tạo hóa đơn PDF. Vui lòng thử lại.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void addLabel(JPanel p, String title, String value) {
        p.add(new JLabel(title), "gapright 10");
        JLabel lbValue = new JLabel(value);
        lbValue.putClientProperty(FlatClientProperties.STYLE, "font:bold");
        p.add(lbValue);
    }

    private void loadData() {

        tableModel.setRowCount(0);
        ArrayList<ChiTietHoaDon> list = ctDAO.getChiTietByMaHD(hoaDon.getMaHD());

        int stt = 1;
        for (ChiTietHoaDon ct : list) {
            String tenThuoc = (ct.getThuoc() != null && ct.getThuoc().getTenThuoc() != null)
                ? ct.getThuoc().getTenThuoc() : "";
            String maLo = (ct.getLoThuoc() != null && ct.getLoThuoc().getMaLo() != null)
                ? ct.getLoThuoc().getMaLo() : "";
            String donViTinh = ct.getDonViTinh() != null ? ct.getDonViTinh() : "";

            tableModel.addRow(new Object[]{
                stt++,
                tenThuoc,
                maLo,
                donViTinh,
                currencyFormat.format(ct.getDonGia()),
                ct.getSoLuong(),
                currencyFormat.format(ct.getThanhTien())
            });
        }
    }
}
