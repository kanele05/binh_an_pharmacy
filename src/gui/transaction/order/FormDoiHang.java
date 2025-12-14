package gui.transaction.order;

import com.formdev.flatlaf.FlatClientProperties;
import dao.*;
import dto.ThuocTimKiem;
import entities.*;
import java.awt.*;
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

public class FormDoiHang extends JPanel {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private PanelDanhSachDoiHang panelDanhSach;
    private PanelTaoPhieuDoi panelTaoPhieu;

    private PhieuDoiHangDAO phieuDoiDAO = new PhieuDoiHangDAO();
    private HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private ChiTietHoaDonDAO ctHoaDonDAO = new ChiTietHoaDonDAO();
    private LoThuocDAO loThuocDAO = new LoThuocDAO();

    public FormDoiHang() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        panelDanhSach = new PanelDanhSachDoiHang();
        panelTaoPhieu = new PanelTaoPhieuDoi();

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

    // ==================== PANEL DANH SÁCH PHIẾU ĐỔI ====================
    private class PanelDanhSachDoiHang extends JPanel {

        private JTable table;
        private DefaultTableModel model;
        private JTextField txtSearch;
        private ArrayList<PhieuDoiHang> dsPhieuDoi;

        public PanelDanhSachDoiHang() {
            setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow]"));

            JLabel lbTitle = new JLabel("Lịch Sử Đổi Hàng");
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
            txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm mã phiếu, tên khách...");
            JButton btnTim = new JButton("Tìm kiếm");
            btnTim.addActionListener(e -> actionTimKiem());
            txtSearch.addActionListener(e -> actionTimKiem());

            JButton btnTaoMoi = new JButton("➕ Lập phiếu đổi hàng");
            btnTaoMoi.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
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

            String[] cols = {"Mã Phiếu", "Ngày Đổi", "Khách Hàng", "Tiền Trả", "Tiền Mới", "Chênh Lệch", "Trạng Thái", "Người Xử Lý"};
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
            table.getColumnModel().getColumn(4).setCellRenderer(new RightAlignRenderer());
            table.getColumnModel().getColumn(5).setCellRenderer(new ChenhLechRenderer());

            // Double click to view details
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = table.getSelectedRow();
                        if (row >= 0 && dsPhieuDoi != null && row < dsPhieuDoi.size()) {
                            showChiTietPhieuDoi(dsPhieuDoi.get(row));
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
                dsPhieuDoi = phieuDoiDAO.searchPhieuDoi(keyword);
                displayData();
            }
        }

        public void loadData() {
            dsPhieuDoi = phieuDoiDAO.getAllPhieuDoi();
            displayData();
        }

        private void displayData() {
            model.setRowCount(0);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (PhieuDoiHang pd : dsPhieuDoi) {
                String ngayDoi = pd.getNgayDoi() != null ? pd.getNgayDoi().format(dtf) : "";
                String tenKH = pd.getKhachHang() != null ? pd.getKhachHang().getTenKH() : "Khách lẻ";
                String tenNV = pd.getNhanVien() != null ? pd.getNhanVien().getHoTen() : "";
                String trangThai = pd.getTrangThai() != null ? pd.getTrangThai() : "Hoàn tất";

                String chenhLechStr;
                double chenhLech = pd.getChenhLech();
                if (chenhLech > 0) {
                    chenhLechStr = "+ " + formatMoney(chenhLech);
                } else if (chenhLech < 0) {
                    chenhLechStr = "- " + formatMoney(Math.abs(chenhLech));
                } else {
                    chenhLechStr = "0 ₫";
                }

                model.addRow(new Object[]{
                    pd.getMaPD(),
                    ngayDoi,
                    tenKH,
                    formatMoney(pd.getTongTienTra()),
                    formatMoney(pd.getTongTienMoi()),
                    chenhLechStr,
                    trangThai,
                    tenNV
                });
            }
        }

        private void showChiTietPhieuDoi(PhieuDoiHang pd) {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                    "Chi tiết phiếu đổi: " + pd.getMaPD(), JDialog.ModalityType.APPLICATION_MODAL);
            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(this);

            JPanel content = new JPanel(new MigLayout("wrap, fill, insets 20", "[fill]", "[][grow][]"));

            // Header info
            JPanel headerPanel = new JPanel(new MigLayout("insets 10", "[]30[]", "[]5[]5[]5[]"));
            headerPanel.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:darken(@background,3%)");

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            headerPanel.add(new JLabel("Mã phiếu: " + pd.getMaPD()), "");
            headerPanel.add(new JLabel("Ngày đổi: " + (pd.getNgayDoi() != null ? pd.getNgayDoi().format(dtf) : "")), "wrap");
            headerPanel.add(new JLabel("Khách hàng: " + (pd.getKhachHang() != null ? pd.getKhachHang().getTenKH() : "Khách lẻ")), "");
            headerPanel.add(new JLabel("Người xử lý: " + (pd.getNhanVien() != null ? pd.getNhanVien().getHoTen() : "")), "wrap");
            headerPanel.add(new JLabel("Hóa đơn gốc: " + (pd.getHoaDonGoc() != null ? pd.getHoaDonGoc().getMaHD() : "---")), "");
            headerPanel.add(new JLabel("Phiếu trả: " + (pd.getPhieuTra() != null ? pd.getPhieuTra().getMaPT() : "---")), "wrap");
            headerPanel.add(new JLabel("Hóa đơn mới: " + (pd.getHoaDonMoi() != null ? pd.getHoaDonMoi().getMaHD() : "---")), "");
            headerPanel.add(new JLabel("Lý do: " + (pd.getLyDo() != null ? pd.getLyDo() : "")), "wrap");

            content.add(headerPanel, "growx, wrap 10");

            // Summary panel
            JPanel summaryPanel = new JPanel(new MigLayout("insets 15", "[grow][grow][grow]", "[]"));
            summaryPanel.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:lighten(#E3F2FD,5%)");

            JLabel lbTra = new JLabel("Tiền trả: " + formatMoney(pd.getTongTienTra()));
            lbTra.putClientProperty(FlatClientProperties.STYLE, "font:bold +2; foreground:#F57C00");

            JLabel lbMoi = new JLabel("Tiền mới: " + formatMoney(pd.getTongTienMoi()));
            lbMoi.putClientProperty(FlatClientProperties.STYLE, "font:bold +2; foreground:#1976D2");

            String chenhLechText;
            Color chenhLechColor;
            if (pd.getChenhLech() > 0) {
                chenhLechText = "Khách trả thêm: " + formatMoney(pd.getChenhLech());
                chenhLechColor = new Color(56, 142, 60);
            } else if (pd.getChenhLech() < 0) {
                chenhLechText = "Hoàn khách: " + formatMoney(Math.abs(pd.getChenhLech()));
                chenhLechColor = new Color(211, 47, 47);
            } else {
                chenhLechText = "Chênh lệch: 0 ₫";
                chenhLechColor = Color.BLACK;
            }
            JLabel lbChenhLech = new JLabel(chenhLechText);
            lbChenhLech.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
            lbChenhLech.setForeground(chenhLechColor);

            summaryPanel.add(lbTra);
            summaryPanel.add(lbMoi);
            summaryPanel.add(lbChenhLech);

            content.add(summaryPanel, "growx, wrap 10");

            // Close button
            JButton btnClose = new JButton("Đóng");
            btnClose.addActionListener(e -> dialog.dispose());
            content.add(btnClose, "right");

            dialog.add(content);
            dialog.setVisible(true);
        }
    }

    // ==================== PANEL TẠO PHIẾU ĐỔI ====================
    private class PanelTaoPhieuDoi extends JPanel {

        private JTextField txtSearchHD;
        private JLabel lbMaHD, lbKhachHang, lbNgayMua;
        private JTable tableTra;
        private DefaultTableModel modelTra;
        private JLabel lbTongTra;

        private JTextField txtSearchThuoc;
        private JTable tableMua;
        private DefaultTableModel modelMua;
        private JLabel lbTongMua;

        private JTextArea txtLyDo;
        private JLabel lbChenhLech;
        private JButton btnHoanTat;

        private double tongTienTra = 0;
        private double tongTienMua = 0;
        private boolean isUpdating = false;

        // Data storage
        private HoaDon currentHoaDon;
        private ArrayList<ChiTietHoaDon> dsChiTietHoaDon;
        private ArrayList<ChiTietTraTemp> dsTraHang = new ArrayList<>();
        private ArrayList<ChiTietMuaTemp> dsMuaHang = new ArrayList<>();
        private ArrayList<ThuocTimKiem> dsThuocTimKiem;

        public PanelTaoPhieuDoi() {
            initCreateUI();
        }

        private void initCreateUI() {
            setLayout(new MigLayout("wrap,fill,insets 20", "[50%][50%]", "[][grow][]"));

            JPanel header = new JPanel(new MigLayout("insets 0", "[]10[]push[]"));
            header.setOpaque(false);

            JButton btnBack = new JButton("← Quay lại");
            btnBack.addActionListener(e -> showDanhSach());

            JLabel lbTitle = new JLabel("Đổi Hàng & Bù Trừ Chênh Lệch");
            lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");

            header.add(btnBack);
            header.add(lbTitle);

            add(header, "span 2, growx, wrap 20");

            add(createPanelTraHang(), "grow, h 100%");
            add(createPanelMuaHang(), "grow, h 100%");
            add(createFooterPanel(), "span 2, growx");
        }

        public void resetForm() {
            txtSearchHD.setText("");
            txtSearchThuoc.setText("");
            modelTra.setRowCount(0);
            modelMua.setRowCount(0);
            lbMaHD.setText("Mã HĐ: ---");
            lbKhachHang.setText("Khách hàng: ---");
            lbNgayMua.setText("Ngày mua: ---");
            lbTongTra.setText("Giá trị trả: 0 ₫");
            lbTongMua.setText("Giá trị mới: 0 ₫");
            lbChenhLech.setText("Chênh lệch: 0 ₫");
            lbChenhLech.setForeground(Color.BLACK);
            txtLyDo.setText("");
            tongTienTra = 0;
            tongTienMua = 0;
            currentHoaDon = null;
            dsChiTietHoaDon = null;
            dsTraHang.clear();
            dsMuaHang.clear();
        }

        private JPanel createPanelTraHang() {
            JPanel panel = new JPanel(new MigLayout("insets 10, fill", "[grow][]", "[]5[]10[grow][]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            panel.setBorder(BorderFactory.createTitledBorder("1. Hàng Khách Trả Lại"));

            txtSearchHD = new JTextField();
            txtSearchHD.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mã hóa đơn hoặc SĐT khách...");
            JButton btnTim = new JButton("Tìm HĐ");
            btnTim.addActionListener(e -> actionTimHoaDon());
            txtSearchHD.addActionListener(e -> actionTimHoaDon());

            panel.add(txtSearchHD, "growx");
            panel.add(btnTim, "wrap");

            // Info panel
            JPanel infoPanel = new JPanel(new MigLayout("insets 5", "[]15[]15[]"));
            infoPanel.setOpaque(false);
            lbMaHD = new JLabel("Mã HĐ: ---");
            lbKhachHang = new JLabel("Khách hàng: ---");
            lbNgayMua = new JLabel("Ngày mua: ---");
            infoPanel.add(lbMaHD);
            infoPanel.add(lbKhachHang);
            infoPanel.add(lbNgayMua);
            panel.add(infoPanel, "span 2, wrap");

            String[] cols = {"Mã Thuốc", "Tên SP", "Mã Lô", "Đã mua", "SL Trả", "Giá gốc", "Thành tiền", "ĐVT"};
            modelTra = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return col == 4; // Chỉ cho phép sửa SL Trả
                }
            };
            tableTra = new JTable(modelTra);
            tableTra.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
            tableTra.getColumnModel().getColumn(5).setCellRenderer(new RightAlignRenderer());
            tableTra.getColumnModel().getColumn(6).setCellRenderer(new RightAlignRenderer());

            // Hide mã thuốc và mã lô columns
            tableTra.getColumnModel().getColumn(0).setMinWidth(0);
            tableTra.getColumnModel().getColumn(0).setMaxWidth(0);
            tableTra.getColumnModel().getColumn(2).setMinWidth(0);
            tableTra.getColumnModel().getColumn(2).setMaxWidth(0);

            modelTra.addTableModelListener(e -> {
                if (!isUpdating && e.getColumn() == 4) {
                    tinhTienTra();
                }
            });

            panel.add(new JScrollPane(tableTra), "span 2, grow, wrap");

            lbTongTra = new JLabel("Giá trị trả: 0 ₫");
            lbTongTra.putClientProperty(FlatClientProperties.STYLE, "font:bold +2; foreground:#F57C00");
            panel.add(lbTongTra, "span 2, right");

            return panel;
        }

        private JPanel createPanelMuaHang() {
            JPanel panel = new JPanel(new MigLayout("insets 10, fill", "[grow][]", "[]10[grow][]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            panel.setBorder(BorderFactory.createTitledBorder("2. Chọn Hàng Đổi Mới"));

            txtSearchThuoc = new JTextField();
            txtSearchThuoc.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm tên thuốc mới...");
            JButton btnChon = new JButton("Chọn thuốc");
            btnChon.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff");
            btnChon.addActionListener(e -> actionChonThuocMoi());

            panel.add(txtSearchThuoc, "growx");
            panel.add(btnChon, "wrap");

            String[] cols = {"Mã Thuốc", "Tên thuốc mới", "Mã Lô", "SL", "Đơn giá", "Thành tiền", "ĐVT"};
            modelMua = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return col == 3; // Chỉ cho phép sửa SL
                }
            };
            tableMua = new JTable(modelMua);
            tableMua.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
            tableMua.getColumnModel().getColumn(4).setCellRenderer(new RightAlignRenderer());
            tableMua.getColumnModel().getColumn(5).setCellRenderer(new RightAlignRenderer());

            // Hide mã thuốc và mã lô columns
            tableMua.getColumnModel().getColumn(0).setMinWidth(0);
            tableMua.getColumnModel().getColumn(0).setMaxWidth(0);
            tableMua.getColumnModel().getColumn(2).setMinWidth(0);
            tableMua.getColumnModel().getColumn(2).setMaxWidth(0);

            modelMua.addTableModelListener(e -> {
                if (!isUpdating && e.getColumn() == 3) {
                    tinhTienMua();
                }
            });

            // Add delete button
            JButton btnXoa = new JButton("Xóa dòng");
            btnXoa.putClientProperty(FlatClientProperties.STYLE, "background:#FFCDD2; foreground:#C62828");
            btnXoa.addActionListener(e -> {
                int row = tableMua.getSelectedRow();
                if (row >= 0) {
                    dsMuaHang.remove(row);
                    modelMua.removeRow(row);
                    tinhTienMua();
                }
            });

            panel.add(new JScrollPane(tableMua), "span 2, grow, wrap");

            JPanel bottomPanel = new JPanel(new MigLayout("insets 0", "[]push[]"));
            bottomPanel.setOpaque(false);
            bottomPanel.add(btnXoa);

            lbTongMua = new JLabel("Giá trị mới: 0 ₫");
            lbTongMua.putClientProperty(FlatClientProperties.STYLE, "font:bold +2; foreground:#1976D2");
            bottomPanel.add(lbTongMua);

            panel.add(bottomPanel, "span 2, growx");

            return panel;
        }

        private JPanel createFooterPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 15", "[grow][][]", "[][]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:lighten(#E8F5E9,5%)");

            txtLyDo = new JTextArea(2, 0);
            txtLyDo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập lý do đổi hàng...");
            txtLyDo.setLineWrap(true);
            txtLyDo.setWrapStyleWord(true);
            JScrollPane scrollLyDo = new JScrollPane(txtLyDo);

            lbChenhLech = new JLabel("Chênh lệch: 0 ₫");
            lbChenhLech.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");

            btnHoanTat = new JButton("XÁC NHẬN ĐỔI HÀNG");
            btnHoanTat.putClientProperty(FlatClientProperties.STYLE, ""
                    + "background:#2196F3; foreground:#fff; font:bold +2; arc:10; margin:10,20,10,20");
            btnHoanTat.addActionListener(e -> actionHoanTat());

            panel.add(new JLabel("Lý do đổi hàng:"), "wrap");
            panel.add(scrollLyDo, "growx, h 50!");
            panel.add(lbChenhLech, "gapleft 20");
            panel.add(btnHoanTat, "gapleft 20");

            return panel;
        }

        private void actionTimHoaDon() {
            String keyword = txtSearchHD.getText().trim();
            if (keyword.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                        "Vui lòng nhập mã HĐ hoặc SĐT khách hàng!");
                return;
            }

            ArrayList<HoaDon> dsHoaDon = hoaDonDAO.searchHoaDon(keyword);

            if (dsHoaDon.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                        "Không tìm thấy hóa đơn nào!");
                return;
            }

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

            content.add(new JLabel("Tìm thấy " + dsHoaDon.size() + " hóa đơn. Chọn hóa đơn cần đổi hàng:"), "wrap 10");

            String[] cols = {"Mã HĐ", "Ngày tạo", "Khách hàng", "Tổng tiền"};
            DefaultTableModel modelSelect = new DefaultTableModel(cols, 0);
            JTable tableSelect = new JTable(modelSelect);
            tableSelect.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30");
            tableSelect.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (HoaDon hd : dsHoaDon) {
                String ngay = hd.getNgayTao() != null ? hd.getNgayTao().format(dtf) : "";
                String tenKH = hd.getKhachHang() != null ? hd.getKhachHang().getTenKH() : "Khách lẻ";
                modelSelect.addRow(new Object[]{
                    hd.getMaHD(), ngay, tenKH, formatMoney(hd.getTongTien())
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
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                            "Vui lòng chọn một hóa đơn!");
                    return;
                }
                dialog.dispose();
                loadHoaDon(dsHoaDon.get(row));
            });

            btnHuy.addActionListener(e -> dialog.dispose());

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
            // Check if invoice can be exchanged (within 30 days)
            if (hd.getNgayTao() != null) {
                long daysBetween = ChronoUnit.DAYS.between(hd.getNgayTao().toLocalDate(), LocalDate.now());
                if (daysBetween > 30) {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                            "Hóa đơn đã quá 30 ngày, không thể đổi hàng!");
                    return;
                }
            }

            currentHoaDon = hd;
            dsChiTietHoaDon = ctHoaDonDAO.getChiTietByMaHD(hd.getMaHD());

            // Update UI
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            lbMaHD.setText("Mã HĐ: " + hd.getMaHD());

            String tenKH = "Khách lẻ";
            if (hd.getKhachHang() != null && hd.getKhachHang().getTenKH() != null) {
                tenKH = hd.getKhachHang().getTenKH();
            }
            lbKhachHang.setText("KH: " + tenKH);
            lbNgayMua.setText("Ngày: " + (hd.getNgayTao() != null ? hd.getNgayTao().format(dtf) : ""));

            // Load invoice details
            isUpdating = true;
            modelTra.setRowCount(0);
            dsTraHang.clear();

            for (ChiTietHoaDon ct : dsChiTietHoaDon) {
                modelTra.addRow(new Object[]{
                    ct.getThuoc().getMaThuoc(),
                    ct.getThuoc().getTenThuoc(),
                    ct.getLoThuoc().getMaLo(),
                    ct.getSoLuong(),
                    0, // SL Trả - mặc định là 0
                    formatMoney(ct.getDonGia()),
                    "0 ₫",
                    ct.getDonViTinh()
                });
            }
            isUpdating = false;

            // Clear purchase cart
            modelMua.setRowCount(0);
            dsMuaHang.clear();
            tongTienTra = 0;
            tongTienMua = 0;
            lbTongTra.setText("Giá trị trả: 0 ₫");
            lbTongMua.setText("Giá trị mới: 0 ₫");
            tinhChenhLech();

            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                    "Đã tải hóa đơn " + hd.getMaHD());
        }

        private void actionChonThuocMoi() {
            String keyword = txtSearchThuoc.getText().trim();

            // Load thuốc có thể bán
            dsThuocTimKiem = loThuocDAO.getDanhSachThuocBanHang();

            if (dsThuocTimKiem.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                        "Không có thuốc nào trong kho!");
                return;
            }

            // Filter by keyword if provided
            ArrayList<ThuocTimKiem> filtered = new ArrayList<>();
            if (!keyword.isEmpty()) {
                for (ThuocTimKiem t : dsThuocTimKiem) {
                    if (t.getTenThuoc().toLowerCase().contains(keyword.toLowerCase()) ||
                        t.getMaThuoc().toLowerCase().contains(keyword.toLowerCase())) {
                        filtered.add(t);
                    }
                }
            } else {
                filtered.addAll(dsThuocTimKiem);
            }

            if (filtered.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                        "Không tìm thấy thuốc phù hợp!");
                return;
            }

            // Show selection dialog
            showThuocSelectionDialog(filtered);
        }

        private void showThuocSelectionDialog(ArrayList<ThuocTimKiem> dsThuoc) {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chọn thuốc mới", JDialog.ModalityType.APPLICATION_MODAL);
            dialog.setSize(800, 500);
            dialog.setLocationRelativeTo(this);

            JPanel content = new JPanel(new MigLayout("fill, insets 15", "[fill]", "[][grow][]"));

            content.add(new JLabel("Chọn thuốc để đổi (tồn kho khả dụng):"), "wrap 10");

            String[] cols = {"Mã Thuốc", "Tên Thuốc", "Mã Lô", "Tồn kho", "Đơn vị", "Giá bán", "HSD"};
            DefaultTableModel modelSelect = new DefaultTableModel(cols, 0);
            JTable tableSelect = new JTable(modelSelect);
            tableSelect.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30");
            tableSelect.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (ThuocTimKiem t : dsThuoc) {
                modelSelect.addRow(new Object[]{
                    t.getMaThuoc(),
                    t.getTenThuoc(),
                    t.getMaLo(),
                    t.getSoLuongTon(),
                    t.getDonViTinh(),
                    formatMoney(t.getGiaBan()),
                    t.getHanSuDung().format(dtf)
                });
            }

            content.add(new JScrollPane(tableSelect), "grow, wrap 10");

            JPanel btnPanel = new JPanel(new MigLayout("insets 0", "push[]10[]"));
            JButton btnChon = new JButton("Thêm vào giỏ đổi");
            btnChon.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff");
            JButton btnHuy = new JButton("Đóng");

            btnChon.addActionListener(e -> {
                int row = tableSelect.getSelectedRow();
                if (row < 0) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                            "Vui lòng chọn một thuốc!");
                    return;
                }

                ThuocTimKiem selected = dsThuoc.get(row);

                // Check if already added
                for (ChiTietMuaTemp temp : dsMuaHang) {
                    if (temp.maThuoc.equals(selected.getMaThuoc()) && temp.maLo.equals(selected.getMaLo())) {
                        Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                                "Thuốc này đã được thêm!");
                        return;
                    }
                }

                // Ask for quantity
                String input = JOptionPane.showInputDialog(dialog,
                        "Thuốc: " + selected.getTenThuoc() + "\n" +
                        "Tồn kho: " + selected.getSoLuongTon() + " " + selected.getDonViTinh() + "\n" +
                        "Giá: " + formatMoney(selected.getGiaBan()) + "/" + selected.getDonViTinh() + "\n\n" +
                        "Nhập số lượng:",
                        "1");

                if (input == null) return;

                try {
                    int sl = Integer.parseInt(input.trim());
                    if (sl <= 0) {
                        Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                                "Số lượng phải lớn hơn 0!");
                        return;
                    }
                    if (sl > selected.getSoLuongTon()) {
                        Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                                "Số lượng vượt quá tồn kho (" + selected.getSoLuongTon() + ")!");
                        return;
                    }

                    // Add to cart
                    ChiTietMuaTemp temp = new ChiTietMuaTemp();
                    temp.maThuoc = selected.getMaThuoc();
                    temp.tenThuoc = selected.getTenThuoc();
                    temp.maLo = selected.getMaLo();
                    temp.soLuong = sl;
                    temp.soLuongTon = selected.getSoLuongTon(); // Lưu tồn kho để validate
                    temp.donGia = selected.getGiaBan();
                    temp.thanhTien = sl * selected.getGiaBan();
                    temp.donViTinh = selected.getDonViTinh();
                    temp.giaTriQuyDoi = selected.getGiaTriQuyDoi();
                    dsMuaHang.add(temp);

                    isUpdating = true;
                    modelMua.addRow(new Object[]{
                        temp.maThuoc,
                        temp.tenThuoc,
                        temp.maLo,
                        temp.soLuong,
                        formatMoney(temp.donGia),
                        formatMoney(temp.thanhTien),
                        temp.donViTinh
                    });
                    isUpdating = false;

                    tinhTienMua();
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                            "Đã thêm " + selected.getTenThuoc());

                } catch (NumberFormatException ex) {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                            "Vui lòng nhập số hợp lệ!");
                }
            });

            btnHuy.addActionListener(e -> dialog.dispose());

            // Double-click to add
            tableSelect.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        btnChon.doClick();
                    }
                }
            });

            btnPanel.add(btnChon);
            btnPanel.add(btnHuy);
            content.add(btnPanel);

            dialog.add(content);
            dialog.setVisible(true);
        }

        private void tinhTienTra() {
            isUpdating = true;
            tongTienTra = 0;
            dsTraHang.clear();

            try {
                for (int i = 0; i < modelTra.getRowCount(); i++) {
                    int daMua = Integer.parseInt(modelTra.getValueAt(i, 3).toString());
                    int tra = Integer.parseInt(modelTra.getValueAt(i, 4).toString());

                    if (tra > daMua) {
                        tra = daMua;
                        modelTra.setValueAt(tra, i, 4);
                        Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                                "Không thể trả quá số lượng mua!");
                    }

                    if (tra < 0) {
                        tra = 0;
                        modelTra.setValueAt(tra, i, 4);
                    }

                    double gia = parseMoney(modelTra.getValueAt(i, 5).toString());
                    double thanhTien = tra * gia;
                    modelTra.setValueAt(formatMoney(thanhTien), i, 6);
                    tongTienTra += thanhTien;

                    // Save to dsTraHang if tra > 0
                    if (tra > 0) {
                        ChiTietTraTemp temp = new ChiTietTraTemp();
                        temp.maThuoc = modelTra.getValueAt(i, 0).toString();
                        temp.tenThuoc = modelTra.getValueAt(i, 1).toString();
                        temp.maLo = modelTra.getValueAt(i, 2).toString();
                        temp.soLuongTra = tra;
                        temp.donGiaTra = gia;
                        temp.thanhTienHoan = thanhTien;
                        temp.donViTinh = modelTra.getValueAt(i, 7).toString();
                        dsTraHang.add(temp);
                    }
                }

                lbTongTra.setText("Giá trị trả: " + formatMoney(tongTienTra));
                tinhChenhLech();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isUpdating = false;
            }
        }

        private void tinhTienMua() {
            isUpdating = true;
            tongTienMua = 0;

            try {
                for (int i = 0; i < modelMua.getRowCount(); i++) {
                    int sl = Integer.parseInt(modelMua.getValueAt(i, 3).toString());
                    double gia = parseMoney(modelMua.getValueAt(i, 4).toString());

                    // Validate số lượng không vượt quá tồn kho
                    if (i < dsMuaHang.size()) {
                        int tonKho = dsMuaHang.get(i).soLuongTon;
                        if (sl > tonKho) {
                            sl = tonKho;
                            modelMua.setValueAt(sl, i, 3);
                            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                                    "Số lượng không thể vượt quá tồn kho (" + tonKho + ")!");
                        }
                        if (sl < 0) {
                            sl = 0;
                            modelMua.setValueAt(sl, i, 3);
                        }
                    }

                    double thanhTien = sl * gia;
                    modelMua.setValueAt(formatMoney(thanhTien), i, 5);
                    tongTienMua += thanhTien;

                    // Update dsMuaHang
                    if (i < dsMuaHang.size()) {
                        dsMuaHang.get(i).soLuong = sl;
                        dsMuaHang.get(i).thanhTien = thanhTien;
                    }
                }

                lbTongMua.setText("Giá trị mới: " + formatMoney(tongTienMua));
                tinhChenhLech();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isUpdating = false;
            }
        }

        private void tinhChenhLech() {
            double diff = tongTienMua - tongTienTra;
            if (diff > 0) {
                lbChenhLech.setText("Khách trả thêm: " + formatMoney(diff));
                lbChenhLech.setForeground(new Color(56, 142, 60));
            } else if (diff < 0) {
                lbChenhLech.setText("Hoàn tiền khách: " + formatMoney(Math.abs(diff)));
                lbChenhLech.setForeground(new Color(211, 47, 47));
            } else {
                lbChenhLech.setText("Chênh lệch: 0 ₫");
                lbChenhLech.setForeground(Color.BLACK);
            }
        }

        private void actionHoanTat() {
            // Validate
            if (currentHoaDon == null) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                        "Vui lòng chọn hóa đơn cần đổi!");
                return;
            }

            // Tính lại danh sách trả
            tinhTienTra();

            if (dsTraHang.isEmpty() && dsMuaHang.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                        "Vui lòng chọn ít nhất một sản phẩm để trả hoặc đổi!");
                return;
            }

            if (dsTraHang.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                        "Vui lòng chọn ít nhất một sản phẩm để trả!");
                return;
            }

            String lyDo = txtLyDo.getText().trim();
            if (lyDo.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                        "Vui lòng nhập lý do đổi hàng!");
                return;
            }

            // Confirm
            String message = "Xác nhận đổi hàng?\n\n" +
                    "Tiền hàng trả: " + formatMoney(tongTienTra) + "\n" +
                    "Tiền hàng mới: " + formatMoney(tongTienMua) + "\n" +
                    lbChenhLech.getText() + "\n\n" +
                    "Lý do: " + lyDo;

            int confirm = JOptionPane.showConfirmDialog(this, message, "Xác nhận đổi hàng",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Get current user
            NhanVien nhanVien = Auth.user;
            if (nhanVien == null) {
                nhanVien = new NhanVien();
                nhanVien.setMaNV("NV001");
            }

            // Create PhieuDoiHang
            String maPD = phieuDoiDAO.getNewMaPhieuDoi();
            double chenhLech = tongTienMua - tongTienTra;

            PhieuDoiHang phieuDoi = new PhieuDoiHang(
                    maPD,
                    LocalDate.now(),
                    tongTienTra,
                    tongTienMua,
                    chenhLech,
                    lyDo,
                    currentHoaDon,
                    null, null,
                    nhanVien,
                    currentHoaDon.getKhachHang(),
                    "Hoàn tất",
                    ""
            );

            // Create ChiTietPhieuTra list
            List<ChiTietPhieuTra> listTraHang = new ArrayList<>();
            for (ChiTietTraTemp temp : dsTraHang) {
                Thuoc thuoc = new Thuoc(temp.maThuoc);
                thuoc.setTenThuoc(temp.tenThuoc);
                LoThuoc lo = new LoThuoc(temp.maLo, null, null, null, 0, "", false);

                ChiTietPhieuTra ct = new ChiTietPhieuTra(
                        null, thuoc, lo,
                        temp.soLuongTra,
                        temp.donGiaTra,
                        temp.thanhTienHoan,
                        temp.donViTinh
                );
                listTraHang.add(ct);
            }

            // Create ChiTietHoaDon list (for new items)
            List<ChiTietHoaDon> listHangMoi = new ArrayList<>();
            for (ChiTietMuaTemp temp : dsMuaHang) {
                Thuoc thuoc = new Thuoc(temp.maThuoc);
                thuoc.setTenThuoc(temp.tenThuoc);
                LoThuoc lo = new LoThuoc(temp.maLo, null, null, null, 0, "", false);

                ChiTietHoaDon ct = new ChiTietHoaDon(
                        null, thuoc, lo,
                        temp.soLuong,
                        temp.donGia,
                        temp.thanhTien,
                        temp.donViTinh
                );
                listHangMoi.add(ct);
            }

            // Save to database
            boolean success = phieuDoiDAO.taoPhieuDoi(phieuDoi, listTraHang, listHangMoi);

            if (success) {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                        "Đã tạo phiếu đổi hàng " + maPD + " thành công!");
                showDanhSach();
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                        "Có lỗi xảy ra khi lưu phiếu đổi hàng!");
            }
        }

        // Helper classes
        private class ChiTietTraTemp {
            String maThuoc;
            String tenThuoc;
            String maLo;
            int soLuongTra;
            double donGiaTra;
            double thanhTienHoan;
            String donViTinh;
        }

        private class ChiTietMuaTemp {
            String maThuoc;
            String tenThuoc;
            String maLo;
            int soLuong;
            int soLuongTon; // Lưu số lượng tồn kho để validate khi sửa
            double donGia;
            double thanhTien;
            String donViTinh;
            int giaTriQuyDoi;
        }
    }

    // ==================== UTILITY METHODS ====================
    private double parseMoney(String text) {
        try {
            return Double.parseDouble(text.replace(".", "").replace(",", "").replace(" ₫", "").replace("₫", "").trim());
        } catch (Exception e) {
            return 0;
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

    private class ChenhLechRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.RIGHT);
            String text = value != null ? value.toString() : "";
            if (text.startsWith("+")) {
                setForeground(new Color(56, 142, 60));
            } else if (text.startsWith("-")) {
                setForeground(new Color(211, 47, 47));
            } else {
                setForeground(Color.BLACK);
            }
            return this;
        }
    }
}
