package gui.transaction.sales;

import com.formdev.flatlaf.FlatClientProperties;
import dao.ChiTietHoaDonDAO;
import entities.ChiTietHoaDon;
import entities.HoaDon;
import java.awt.Component;
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

        addLabel(pInfo, "Mã hóa đơn:", hoaDon.getMaHD());
        addLabel(pInfo, "Ngày lập:", hoaDon.getNgayTao().format(dateTimeFormat));

        String tenKH = (hoaDon.getKhachHang() != null) ? hoaDon.getKhachHang().getTenKH() : "Khách lẻ";
        addLabel(pInfo, "Khách hàng:", tenKH);
        addLabel(pInfo, "Nhân viên:", hoaDon.getNhanVien().getHoTen());

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

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        add(btnClose, "right");

        pack();
        setLocationRelativeTo(parent);
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
            tableModel.addRow(new Object[]{
                stt++,
                ct.getThuoc().getTenThuoc(),
                ct.getLoThuoc().getMaLo(),
                ct.getDonViTinh(),
                currencyFormat.format(ct.getDonGia()),
                ct.getSoLuong(),
                currencyFormat.format(ct.getThanhTien())
            });
        }
    }
}
