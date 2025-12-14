package gui.transaction.inventory;

import com.formdev.flatlaf.FlatClientProperties;
import dao.ChiTietHoaDonDAO;
import dao.ChiTietPhieuTraDAO;
import dao.HoaDonDAO;
import dao.PhieuTraHangDAO;
import entities.ChiTietHoaDon;
import entities.ChiTietPhieuTra;
import entities.HoaDon;
import entities.KhachHang;
import entities.LoThuoc;
import entities.NhanVien;
import entities.PhieuTraHang;
import entities.Thuoc;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;
import utils.Auth;

public class FormTraHang extends JPanel {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private PanelDanhSachPhieuTra panelDanhSach;
    private PanelTaoPhieuTra panelTaoPhieu;

    private PhieuTraHangDAO phieuTraDAO = new PhieuTraHangDAO();
    private HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private ChiTietHoaDonDAO ctHoaDonDAO = new ChiTietHoaDonDAO();
    private ChiTietPhieuTraDAO ctPhieuTraDAO = new ChiTietPhieuTraDAO();

    public FormTraHang() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        panelDanhSach = new PanelDanhSachPhieuTra();
        panelTaoPhieu = new PanelTaoPhieuTra();

        mainPanel.add(panelDanhSach, "LIST");
        mainPanel.add(panelTaoPhieu, "CREATE");

        add(mainPanel, BorderLayout.CENTER);
    }

    public void showTaoPhieu() {
        panelTaoPhieu.resetForm();
        cardLayout.show(mainPanel, "CREATE");
    }

    public void showDanhSach() {
        panelDanhSach.loadData();
        cardLayout.show(mainPanel, "LIST");
    }

    private class PanelDanhSachPhieuTra extends JPanel {

        private JTable table;
        private DefaultTableModel model;
        private JTextField txtSearch;
        private ArrayList<PhieuTraHang> dsPhieuTra;

        public PanelDanhSachPhieuTra() {
            setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow]"));

            JLabel lbTitle = new JLabel("Lịch Sử Trả Hàng");
            lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
            add(lbTitle, "wrap 20");

            add(createToolBar(), "wrap 10");

            add(createTable(), "grow");

            loadData();
        }

        private JPanel createToolBar() {
            JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]push[]", "[]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");

            txtSearch = new JTextField();
            txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm mã phiếu trả, tên khách...");
            JButton btnTim = new JButton("Tìm kiếm");
            btnTim.addActionListener(e -> actionTimKiem());

            txtSearch.addActionListener(e -> actionTimKiem());

            JButton btnTaoMoi = new JButton("➕ Lập phiếu trả hàng");
            btnTaoMoi.putClientProperty(FlatClientProperties.STYLE, "background:#F44336; foreground:#fff; font:bold");
            btnTaoMoi.addActionListener(e -> showTaoPhieu());

            panel.add(txtSearch, "w 250");
            panel.add(btnTim);
            panel.add(btnTaoMoi);

            return panel;
        }

        private JPanel createTable() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            String[] cols = {"Mã Phiếu", "Ngày Trả", "Khách Hàng", "Tiền Hoàn", "Lý Do", "Người Xử Lý", "Trạng Thái"};
            model = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };

            table = new JTable(model);
            table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:35; showHorizontalLines:true");
            table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

            table.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer());

            // Double click to view details
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = table.getSelectedRow();
                        if (row >= 0 && row < dsPhieuTra.size()) {
                            showChiTietPhieuTra(dsPhieuTra.get(row));
                        }
                    }
                }
            });
            table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            panel.add(new JScrollPane(table));
            return panel;
        }

        private void actionTimKiem() {
            String keyword = txtSearch.getText().trim();
            if (keyword.isEmpty()) {
                loadData();
            } else {
                dsPhieuTra = phieuTraDAO.searchPhieuTra(keyword);
                displayData();
            }
        }

        public void loadData() {
            dsPhieuTra = phieuTraDAO.getAllPhieuTra();
            displayData();
        }

        private void displayData() {
            model.setRowCount(0);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (PhieuTraHang pt : dsPhieuTra) {
                String ngayTra = pt.getNgayTra() != null ? pt.getNgayTra().format(dtf) : "";
                String tenKH = pt.getKhachHang() != null ? pt.getKhachHang().getTenKH() : "Khách lẻ";
                String tenNV = pt.getNhanVien() != null ? pt.getNhanVien().getHoTen() : "";
                String trangThai = pt.getTrangThai() != null ? pt.getTrangThai() : "Đã xử lý";

                model.addRow(new Object[]{
                    pt.getMaPT(),
                    ngayTra,
                    tenKH,
                    formatMoney(pt.getTongTienHoanTra()),
                    pt.getLyDo(),
                    tenNV,
                    trangThai
                });
            }
        }

        private void showChiTietPhieuTra(PhieuTraHang pt) {
            List<ChiTietPhieuTra> listCT = ctPhieuTraDAO.getChiTietByMaPhieu(pt.getMaPT());

            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết phiếu trả: " + pt.getMaPT(), JDialog.ModalityType.APPLICATION_MODAL);
            dialog.setSize(700, 500);
            dialog.setLocationRelativeTo(this);

            JPanel content = new JPanel(new MigLayout("wrap, fill, insets 20", "[fill]", "[][grow][]"));

            // Header info
            JPanel headerPanel = new JPanel(new MigLayout("insets 10", "[]20[]", "[]5[]5[]"));
            headerPanel.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:darken(@background,3%)");

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            headerPanel.add(new JLabel("Mã phiếu: " + pt.getMaPT()), "");
            headerPanel.add(new JLabel("Ngày trả: " + (pt.getNgayTra() != null ? pt.getNgayTra().format(dtf) : "")), "wrap");
            headerPanel.add(new JLabel("Khách hàng: " + (pt.getKhachHang() != null ? pt.getKhachHang().getTenKH() : "Khách lẻ")), "");
            headerPanel.add(new JLabel("Người xử lý: " + (pt.getNhanVien() != null ? pt.getNhanVien().getHoTen() : "")), "wrap");
            headerPanel.add(new JLabel("Hóa đơn gốc: " + (pt.getHoaDon() != null ? pt.getHoaDon().getMaHD() : "---")), "");
            headerPanel.add(new JLabel("Lý do: " + (pt.getLyDo() != null ? pt.getLyDo() : "")), "wrap");

            content.add(headerPanel, "growx, wrap 10");

            // Table chi tiết
            String[] cols = {"Tên thuốc", "Mã lô", "SL trả", "Đơn giá", "Thành tiền", "ĐVT"};
            DefaultTableModel modelCT = new DefaultTableModel(cols, 0);
            JTable tableCT = new JTable(modelCT);
            tableCT.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");

            for (ChiTietPhieuTra ct : listCT) {
                modelCT.addRow(new Object[]{
                    ct.getThuoc().getTenThuoc(),
                    ct.getLoThuoc().getMaLo(),
                    ct.getSoLuongTra(),
                    formatMoney(ct.getDonGiaTra()),
                    formatMoney(ct.getThanhTienHoanTra()),
                    ct.getDonViTinh()
                });
            }

            content.add(new JScrollPane(tableCT), "grow, wrap 10");

            // Footer - total
            JPanel footerPanel = new JPanel(new MigLayout("insets 10", "push[][]"));
            footerPanel.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:lighten(#E8F5E9,5%)");

            JLabel lbTotal = new JLabel("TỔNG TIỀN HOÀN: " + formatMoney(pt.getTongTienHoanTra()));
            lbTotal.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:#D32F2F");
            footerPanel.add(lbTotal);

            JButton btnClose = new JButton("Đóng");
            btnClose.addActionListener(e -> dialog.dispose());
            footerPanel.add(btnClose);

            content.add(footerPanel, "growx");

            dialog.add(content);
            dialog.setVisible(true);
        }
    }

    private class PanelTaoPhieuTra extends JPanel {

        private JTextField txtSearch;
        private JLabel lbKhachHang, lbNgayMua, lbTongHoaDon, lbMaHD;
        private JTable tableHoaDon, tableTraHang;
        private DefaultTableModel modelHoaDon, modelTraHang;
        private JLabel lbTienHoan;
        private JTextArea txtLyDo;
        private double tongTienHoan = 0;

        private HoaDon currentHoaDon;
        private ArrayList<ChiTietHoaDon> dsChiTietHoaDon;
        private ArrayList<ChiTietTraHangTemp> dsTraHang = new ArrayList<>();

        public PanelTaoPhieuTra() {
            initCreateUI();
        }

        private void initCreateUI() {
            setLayout(new MigLayout("wrap,fill,insets 20", "[50%][50%]", "[][][grow][]"));

            JPanel header = new JPanel(new MigLayout("insets 0", "[]10[]push[]"));
            header.setOpaque(false);

            JButton btnBack = new JButton("← Quay lại");
            btnBack.addActionListener(e -> showDanhSach());

            JLabel lbTitle = new JLabel("Tiếp Nhận Trả Hàng");
            lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");

            header.add(btnBack);
            header.add(lbTitle);

            add(header, "span 2, growx, wrap 20");

            add(createSearchPanel(), "grow, span 1 2, w 50%");
            add(createReturnCartPanel(), "grow, span 1 2, w 50%, wrap");
            add(createFooterPanel(), "span 2, growx");
        }

        public void resetForm() {
            txtSearch.setText("");
            lbMaHD.setText("Mã HĐ: ---");
            lbKhachHang.setText("Khách hàng: ---");
            lbNgayMua.setText("Ngày mua: ---");
            lbTongHoaDon.setText("Tổng HĐ: 0 ₫");
            modelHoaDon.setRowCount(0);
            modelTraHang.setRowCount(0);
            txtLyDo.setText("");
            lbTienHoan.setText("0 ₫");
            tongTienHoan = 0;
            currentHoaDon = null;
            dsChiTietHoaDon = null;
            dsTraHang.clear();
        }

        private JPanel createSearchPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 15, fillx", "[grow][]", "[]10[]10[grow][]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            panel.setBorder(BorderFactory.createTitledBorder("1. Tìm hóa đơn mua hàng"));

            txtSearch = new JTextField();
            txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mã hóa đơn hoặc SĐT khách...");
            JButton btnTim = new JButton("Tìm hóa đơn");
            btnTim.addActionListener(e -> actionTimHoaDon());
            txtSearch.addActionListener(e -> actionTimHoaDon());

            panel.add(txtSearch, "growx");
            panel.add(btnTim, "wrap");

            JPanel infoPanel = new JPanel(new MigLayout("insets 10", "[]20[]", "[]5[]"));
            infoPanel.putClientProperty(FlatClientProperties.STYLE, "background:lighten(@background,5%); arc:10");
            lbMaHD = new JLabel("Mã HĐ: ---");
            lbKhachHang = new JLabel("Khách hàng: ---");
            lbNgayMua = new JLabel("Ngày mua: ---");
            lbTongHoaDon = new JLabel("Tổng HĐ: 0 ₫");

            infoPanel.add(lbMaHD);
            infoPanel.add(lbKhachHang, "wrap");
            infoPanel.add(lbNgayMua);
            infoPanel.add(lbTongHoaDon);

            panel.add(infoPanel, "span 2, growx, wrap");

            String[] cols = {"Mã Thuốc", "Tên Thuốc", "Mã Lô", "SL Mua", "Đơn giá", "ĐVT"};
            modelHoaDon = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            tableHoaDon = new JTable(modelHoaDon);
            tableHoaDon.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
            tableHoaDon.getColumnModel().getColumn(4).setCellRenderer(new RightAlignRenderer());

            JButton btnChonTra = new JButton("Trả món đang chọn >>");
            btnChonTra.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff");
            btnChonTra.addActionListener(e -> actionChonTraHang());

            panel.add(new JLabel("Chi tiết hóa đơn:"), "span 2, wrap");
            panel.add(new JScrollPane(tableHoaDon), "span 2, grow, wrap");
            panel.add(btnChonTra, "span 2, right");

            return panel;
        }

        private JPanel createReturnCartPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 15, fill", "[grow]", "[][grow][]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            panel.setBorder(BorderFactory.createTitledBorder("2. Danh sách hàng nhận lại"));

            String[] cols = {"Tên Thuốc", "Mã Lô", "SL Trả", "Đơn giá hoàn", "Thành tiền", "ĐVT"};
            modelTraHang = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            tableTraHang = new JTable(modelTraHang);
            tableTraHang.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
            tableTraHang.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer());
            tableTraHang.getColumnModel().getColumn(4).setCellRenderer(new RightAlignRenderer());

            JButton btnXoa = new JButton("Xóa dòng đang chọn");
            btnXoa.putClientProperty(FlatClientProperties.STYLE, "background:#FFCDD2; foreground:#C62828");
            btnXoa.addActionListener(e -> actionXoaDongTra());

            panel.add(new JScrollPane(tableTraHang), "grow, wrap");
            panel.add(btnXoa, "right");

            return panel;
        }

        private JPanel createFooterPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 15", "[grow][right]", "[]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:lighten(#E8F5E9,5%)");

            txtLyDo = new JTextArea(3, 0);
            txtLyDo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập lý do khách trả hàng...");
            txtLyDo.setLineWrap(true);
            txtLyDo.setWrapStyleWord(true);
            JScrollPane scrollNote = new JScrollPane(txtLyDo);

            JPanel pTotal = new JPanel(new MigLayout("insets 0", "[]10[]"));
            pTotal.setOpaque(false);
            lbTienHoan = new JLabel("0 ₫");
            lbTienHoan.putClientProperty(FlatClientProperties.STYLE, "font:bold +10; foreground:#D32F2F");

            JButton btnHoanTat = new JButton("✓ HOÀN TẤT TRẢ HÀNG");
            btnHoanTat.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold +2; margin:10,20,10,20");
            btnHoanTat.addActionListener(e -> actionHoanTatTraHang());

            pTotal.add(new JLabel("TỔNG TIỀN HOÀN KHÁCH:"));
            pTotal.add(lbTienHoan, "wrap");
            pTotal.add(btnHoanTat, "span 2, right");

            panel.add(new JLabel("Ghi chú / Lý do trả hàng:"), "wrap");
            panel.add(scrollNote, "growx, h 80!");
            panel.add(pTotal);

            return panel;
        }

        private void actionTimHoaDon() {
            String keyword = txtSearch.getText().trim();
            if (keyword.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng nhập mã HĐ hoặc SĐT khách hàng!");
                return;
            }

            // Search invoices
            ArrayList<HoaDon> dsHoaDon = hoaDonDAO.searchHoaDon(keyword);

            if (dsHoaDon.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Không tìm thấy hóa đơn nào!");
                return;
            }

            // If only one result, use it directly
            if (dsHoaDon.size() == 1) {
                loadHoaDon(dsHoaDon.get(0));
                return;
            }

            // Multiple results - show selection dialog
            showHoaDonSelectionDialog(dsHoaDon);
        }

        private void showHoaDonSelectionDialog(ArrayList<HoaDon> dsHoaDon) {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chọn hóa đơn", JDialog.ModalityType.APPLICATION_MODAL);
            dialog.setSize(700, 400);
            dialog.setLocationRelativeTo(this);

            JPanel content = new JPanel(new MigLayout("fill, insets 15", "[fill]", "[][grow][]"));

            content.add(new JLabel("Tìm thấy " + dsHoaDon.size() + " hóa đơn. Chọn hóa đơn cần xử lý trả hàng:"), "wrap 10");

            String[] cols = {"Mã HĐ", "Ngày tạo", "Khách hàng", "Tổng tiền", "Ghi chú"};
            DefaultTableModel modelSelect = new DefaultTableModel(cols, 0);
            JTable tableSelect = new JTable(modelSelect);
            tableSelect.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30");
            tableSelect.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (HoaDon hd : dsHoaDon) {
                String ngay = hd.getNgayTao() != null ? hd.getNgayTao().format(dtf) : "";
                String tenKH = hd.getKhachHang() != null ? hd.getKhachHang().getTenKH() : "Khách lẻ";
                modelSelect.addRow(new Object[]{
                    hd.getMaHD(),
                    ngay,
                    tenKH,
                    formatMoney(hd.getTongTien()),
                    hd.getGhiChu()
                });
            }

            content.add(new JScrollPane(tableSelect), "grow, wrap 10");

            JPanel btnPanel = new JPanel(new MigLayout("insets 0", "push[]10[]"));
            JButton btnChon = new JButton("Chọn");
            btnChon.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff");
            JButton btnHuy = new JButton("Hủy");

            btnChon.addActionListener(e -> {
                int row = tableSelect.getSelectedRow();
                if (row < 0) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn một hóa đơn!");
                    return;
                }
                dialog.dispose();
                loadHoaDon(dsHoaDon.get(row));
            });

            btnHuy.addActionListener(e -> dialog.dispose());

            // Double-click to select
            tableSelect.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = tableSelect.getSelectedRow();
                        if (row >= 0) {
                            dialog.dispose();
                            loadHoaDon(dsHoaDon.get(row));
                        }
                    }
                }
            });

            btnPanel.add(btnChon);
            btnPanel.add(btnHuy);
            content.add(btnPanel);

            dialog.add(content);
            dialog.setVisible(true);
        }

        private void loadHoaDon(HoaDon hd) {
            // Check if invoice can be returned (within 30 days)
            if (hd.getNgayTao() != null) {
                long daysBetween = ChronoUnit.DAYS.between(hd.getNgayTao().toLocalDate(), LocalDate.now());
                if (daysBetween > 30) {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                            "Hóa đơn đã quá 30 ngày, không thể trả hàng!");
                    return;
                }
            }

            currentHoaDon = hd;
            dsChiTietHoaDon = ctHoaDonDAO.getChiTietByMaHD(hd.getMaHD());

            // Update UI
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            lbMaHD.setText("Mã HĐ: " + hd.getMaHD());

            String tenKH = "Khách lẻ";
            String sdtKH = "";
            if (hd.getKhachHang() != null && hd.getKhachHang().getTenKH() != null) {
                tenKH = hd.getKhachHang().getTenKH();
                sdtKH = hd.getKhachHang().getSdt() != null ? " - " + hd.getKhachHang().getSdt() : "";
            }
            lbKhachHang.setText("Khách hàng: " + tenKH + sdtKH);
            lbNgayMua.setText("Ngày mua: " + (hd.getNgayTao() != null ? hd.getNgayTao().format(dtf) : ""));
            lbTongHoaDon.setText("Tổng HĐ: " + formatMoney(hd.getTongTien()));

            // Load invoice details
            modelHoaDon.setRowCount(0);
            for (ChiTietHoaDon ct : dsChiTietHoaDon) {
                modelHoaDon.addRow(new Object[]{
                    ct.getThuoc().getMaThuoc(),
                    ct.getThuoc().getTenThuoc(),
                    ct.getLoThuoc().getMaLo(),
                    ct.getSoLuong(),
                    formatMoney(ct.getDonGia()),
                    ct.getDonViTinh()
                });
            }

            // Clear return cart
            modelTraHang.setRowCount(0);
            dsTraHang.clear();
            updateTongTien();

            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                    "Đã tải hóa đơn " + hd.getMaHD());
        }

        private void actionChonTraHang() {
            if (currentHoaDon == null) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng tìm và chọn hóa đơn trước!");
                return;
            }

            int row = tableHoaDon.getSelectedRow();
            if (row == -1) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Chọn sản phẩm từ hóa đơn để trả!");
                return;
            }

            ChiTietHoaDon ctHD = dsChiTietHoaDon.get(row);

            // Check if already added
            for (ChiTietTraHangTemp temp : dsTraHang) {
                if (temp.maThuoc.equals(ctHD.getThuoc().getMaThuoc()) && temp.maLo.equals(ctHD.getLoThuoc().getMaLo())) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Sản phẩm này đã được thêm vào danh sách trả!");
                    return;
                }
            }

            int slDaMua = ctHD.getSoLuong();
            double donGia = ctHD.getDonGia();

            String input = JOptionPane.showInputDialog(this,
                    "Sản phẩm: " + ctHD.getThuoc().getTenThuoc() + "\n"
                    + "Lô: " + ctHD.getLoThuoc().getMaLo() + "\n"
                    + "Số lượng đã mua: " + slDaMua + " " + ctHD.getDonViTinh() + "\n\n"
                    + "Nhập số lượng muốn trả:",
                    String.valueOf(slDaMua));

            if (input == null) {
                return;
            }

            try {
                int slTra = Integer.parseInt(input.trim());
                if (slTra <= 0) {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Số lượng phải lớn hơn 0!");
                    return;
                }
                if (slTra > slDaMua) {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Số lượng trả không được vượt quá số lượng đã mua (" + slDaMua + ")!");
                    return;
                }

                double thanhTienHoan = slTra * donGia;

                // Add to return list
                ChiTietTraHangTemp temp = new ChiTietTraHangTemp();
                temp.maThuoc = ctHD.getThuoc().getMaThuoc();
                temp.tenThuoc = ctHD.getThuoc().getTenThuoc();
                temp.maLo = ctHD.getLoThuoc().getMaLo();
                temp.soLuongTra = slTra;
                temp.donGiaTra = donGia;
                temp.thanhTienHoan = thanhTienHoan;
                temp.donViTinh = ctHD.getDonViTinh();
                dsTraHang.add(temp);

                modelTraHang.addRow(new Object[]{
                    temp.tenThuoc,
                    temp.maLo,
                    temp.soLuongTra,
                    formatMoney(temp.donGiaTra),
                    formatMoney(temp.thanhTienHoan),
                    temp.donViTinh
                });

                updateTongTien();

            } catch (NumberFormatException e) {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Vui lòng nhập số hợp lệ!");
            }
        }

        private void actionXoaDongTra() {
            int row = tableTraHang.getSelectedRow();
            if (row != -1) {
                dsTraHang.remove(row);
                modelTraHang.removeRow(row);
                updateTongTien();
            }
        }

        private void updateTongTien() {
            tongTienHoan = 0;
            for (ChiTietTraHangTemp temp : dsTraHang) {
                tongTienHoan += temp.thanhTienHoan;
            }
            lbTienHoan.setText(formatMoney(tongTienHoan));
        }

        private void actionHoanTatTraHang() {
            if (currentHoaDon == null) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn hóa đơn trước!");
                return;
            }

            if (dsTraHang.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn ít nhất một sản phẩm để trả!");
                return;
            }

            String lyDo = txtLyDo.getText().trim();
            if (lyDo.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng nhập lý do trả hàng!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Xác nhận hoàn tiền: " + lbTienHoan.getText() + " cho khách?\n\n"
                    + "Lý do: " + lyDo,
                    "Xác nhận trả hàng",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Get current user
            NhanVien nhanVien = Auth.user;
            if (nhanVien == null) {
                nhanVien = new NhanVien();
                nhanVien.setMaNV("NV001");
            }

            // Create PhieuTraHang
            String maPhieu = phieuTraDAO.getNewMaPhieuTra();

            PhieuTraHang phieu = new PhieuTraHang(
                    maPhieu,
                    LocalDate.now(),
                    tongTienHoan,
                    lyDo,
                    currentHoaDon,
                    nhanVien,
                    currentHoaDon.getKhachHang(),
                    "Đã xử lý",
                    ""
            );

            // Create ChiTietPhieuTra list
            List<ChiTietPhieuTra> listCT = new ArrayList<>();
            for (ChiTietTraHangTemp temp : dsTraHang) {
                Thuoc thuoc = new Thuoc(temp.maThuoc);
                thuoc.setTenThuoc(temp.tenThuoc);

                LoThuoc lo = new LoThuoc(temp.maLo, null, null, null, 0, "", false);

                ChiTietPhieuTra ct = new ChiTietPhieuTra(
                        phieu,
                        thuoc,
                        lo,
                        temp.soLuongTra,
                        temp.donGiaTra,
                        temp.thanhTienHoan,
                        temp.donViTinh
                );
                listCT.add(ct);
            }

            // Save to database
            boolean success = phieuTraDAO.taoPhieuTra(phieu, listCT);

            if (success) {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                        "Đã tạo phiếu trả hàng " + maPhieu + " thành công!");
                showDanhSach();
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                        "Có lỗi xảy ra khi lưu phiếu trả hàng!");
            }
        }

        // Helper class to store return item temporarily
        private class ChiTietTraHangTemp {
            String maThuoc;
            String tenThuoc;
            String maLo;
            int soLuongTra;
            double donGiaTra;
            double thanhTienHoan;
            String donViTinh;
        }
    }

    private String formatMoney(double amount) {
        return new DecimalFormat("#,##0 ₫").format(amount);
    }

    private class RightAlignRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);
            return this;
        }
    }
}
