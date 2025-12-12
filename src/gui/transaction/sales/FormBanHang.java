package gui.transaction.sales;

import gui.manage.partner.DialogKhachHang;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.formdev.flatlaf.FlatClientProperties;
import dao.HoaDonDAO;
import dao.KhachHangDAO;
import dao.LoThuocDAO;
import dto.ThuocTimKiem;
import entities.ChiTietHoaDon;
import entities.HoaDon;
import entities.KhachHang;
import entities.LoThuoc;
import entities.NhanVien;
import entities.Thuoc;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;
import utils.Auth;

public class FormBanHang extends JPanel {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private PanelDanhSachHoaDon panelDanhSach;
    private PanelBanHangGiaoDien panelBanHang;

    public FormBanHang() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        panelDanhSach = new PanelDanhSachHoaDon();
        panelBanHang = new PanelBanHangGiaoDien();

        mainPanel.add(panelDanhSach, "LIST");
        mainPanel.add(panelBanHang, "POS");

        add(mainPanel, BorderLayout.CENTER);
    }

    public void showBanHang() {
        panelBanHang.resetForm();
        cardLayout.show(mainPanel, "POS");
    }

    public void showDanhSach() {
        panelDanhSach.loadData();
        cardLayout.show(mainPanel, "LIST");
    }

    private class PanelDanhSachHoaDon extends JPanel {

        private JTable table;
        private JTextField txtSearch;

        private EventList<HoaDon> sourceList;
        private FilterList<HoaDon> filterList;
        private EventTableModel<HoaDon> tableModel;

        private HoaDonDAO hoaDonDAO = new HoaDonDAO();

        private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        public PanelDanhSachHoaDon() {
            setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow]"));

            JLabel lbTitle = new JLabel("Lịch Sử Bán Hàng");
            lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
            add(lbTitle, "wrap 20");

            add(createToolBar(), "wrap 10");
            add(createTable(), "grow");

            loadData();
        }

        private JPanel createToolBar() {
            JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]10[]10[]push[]", "[]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");

            JLabel lbSearch = new JLabel("Tìm kiếm:");
            lbSearch.putClientProperty(FlatClientProperties.STYLE, "font:bold");

            txtSearch = new JTextField();
            txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mã HĐ, tên khách, tên nhân viên...");

            JButton btnRefresh = new JButton("Làm mới");
            btnRefresh.addActionListener(e -> {
                txtSearch.setText("");
                loadData();
            });

            JButton btnXemChiTiet = new JButton("Xem chi tiết");
            btnXemChiTiet.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
            btnXemChiTiet.addActionListener(e -> actionXemChiTiet());

            JButton btnBanHang = new JButton("+ Tạo hóa đơn mới");
            btnBanHang.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold");
            btnBanHang.addActionListener(e -> showBanHang());

            panel.add(lbSearch);
            panel.add(txtSearch, "w 300");
            panel.add(btnRefresh);
            panel.add(btnXemChiTiet);
            panel.add(btnBanHang);
            return panel;
        }

        private void actionXemChiTiet() {
            int row = table.getSelectedRow();
            if (row == -1) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn hóa đơn để xem!");
                return;
            }

            entities.HoaDon selectedHD = filterList.get(row);

            DialogChiTietHoaDon dialog = new DialogChiTietHoaDon(SwingUtilities.getWindowAncestor(this), selectedHD);
            dialog.setVisible(true);
        }

        private JPanel createTable() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            sourceList = new BasicEventList<>();

            TextFilterator<HoaDon> textFilterator = new TextFilterator<HoaDon>() {
                @Override
                public void getFilterStrings(List<String> list, HoaDon hd) {
                    list.add(hd.getMaHD());
                    list.add(hd.getNhanVien().getHoTen());
                    if (hd.getKhachHang() != null) {
                        list.add(hd.getKhachHang().getTenKH());
                        list.add(hd.getKhachHang().getSdt());
                    } else {
                        list.add("Khách lẻ");
                    }
                    list.add(hd.getNgayTao().format(dateTimeFormat));
                }
            };

            TextComponentMatcherEditor<HoaDon> matcherEditor = new TextComponentMatcherEditor<>(txtSearch, textFilterator);
            filterList = new FilterList<>(sourceList, matcherEditor);

            TableFormat<HoaDon> tableFormat = new TableFormat<HoaDon>() {
                @Override
                public int getColumnCount() {
                    return 6;
                }

                @Override
                public String getColumnName(int i) {
                    String[] cols = {"Mã HĐ", "Ngày Bán", "Khách Hàng", "Tổng Thu", "Thanh Toán", "Người Bán"};
                    return cols[i];
                }

                @Override
                public Object getColumnValue(HoaDon hd, int i) {
                    switch (i) {
                        case 0:
                            return hd.getMaHD();
                        case 1:
                            return hd.getNgayTao();
                        case 2:
                            return (hd.getKhachHang() != null) ? hd.getKhachHang().getTenKH() : "Khách lẻ";
                        case 3:
                            return hd.getTongTien() + hd.getThue() - hd.getGiamGia();
                        case 4:
                            return hd.getHinhThucTT();
                        case 5:
                            return hd.getNhanVien().getHoTen();
                        default:
                            return null;
                    }
                }
            };

            tableModel = new EventTableModel<>(filterList, tableFormat);
            table = new JTable(tableModel);

            table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
            table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

            table.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer());
            table.getColumnModel().getColumn(1).setCellRenderer(new DateRenderer());
            table.getColumnModel().getColumn(3).setCellRenderer(new CurrencyRenderer());
            table.getColumnModel().getColumn(4).setCellRenderer(new CenterRenderer());

            panel.add(new JScrollPane(table));
            return panel;
        }

        public void loadData() {
            sourceList.clear();
            sourceList.addAll(hoaDonDAO.getAllHoaDon());
        }

        private class CenterRenderer extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                return this;
            }
        }

        private class DateRenderer extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (v instanceof java.time.LocalDateTime) {
                    setText(((java.time.LocalDateTime) v).format(dateTimeFormat));
                }
                setHorizontalAlignment(JLabel.CENTER);
                return this;
            }
        }

        private class CurrencyRenderer extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (v instanceof Double) {
                    setText(currencyFormat.format(v));
                    setForeground(new java.awt.Color(0, 150, 136));
                    setFont(getFont().deriveFont(java.awt.Font.BOLD));
                }
                setHorizontalAlignment(JLabel.RIGHT);
                return this;
            }
        }
    }

    private class PanelBanHangGiaoDien extends JPanel {

        private JTextField txtTimKiem, txtMaKH, txtTenKH, txtSDT;
        private JComboBox<String> cbHinhThucTT;
        private JTable tableThuoc, tableGioHang;

        private EventList<ThuocTimKiem> sourceList;
        private FilterList<ThuocTimKiem> filterList;
        private EventTableModel<ThuocTimKiem> tableModelThuoc;

        private DefaultTableModel modelGioHang;

        private JLabel lbTongTien, lbThanhTien, lbTienThue, lbGiamGia;
        private JLabel lbDiemHienCo;
        private JCheckBox chkDungDiem;
        private JTextField txtThueVAT;

        private JButton btnThemVaoGio, btnXoaKhoiGio, btnThanhToan, btnHuyHD;

        private double tongTien = 0;
        private double thueVAT = 0;
        private double tienGiamGia = 0;
        private final double DEFAULT_VAT_RATE = 5.0;

        private LoThuocDAO loThuocDAO = new LoThuocDAO();
        private KhachHangDAO khachHangDAO = new KhachHangDAO();
        private HoaDonDAO hoaDonDAO = new HoaDonDAO();
        private JLabel lbMaHD;
        private KhachHang currentKhachHang = null;
        private boolean isUpdating = false;

        public PanelBanHangGiaoDien() {
            initPOS();
        }

        private void initPOS() {
            setLayout(new MigLayout("wrap,fillx,insets 20", "[70%][30%]", "[][][grow][]"));

            JPanel header = new JPanel(new MigLayout("insets 0", "[]10[]push[]"));
            header.setOpaque(false);
            JButton btnBack = new JButton(" Quay lại");
            btnBack.addActionListener(e -> showDanhSach());
            JLabel lbTitle = new JLabel("Bán hàng / Tạo hóa đơn");
            lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
            header.add(btnBack);
            header.add(lbTitle);
            add(header, "span 2, growx, wrap 15");

            add(createSearchPanel(), "grow");
            add(createCustomerPanel(), "grow,wrap");
            add(createMedicineTablePanel(), "grow");
            add(createCartPanel(), "grow,wrap");
            add(createPaymentPanel(), "span 2,grow");

            loadKhoThuoc();
        }

        public void resetForm() {
            modelGioHang.setRowCount(0);
            setKhachVangLai();
            txtTimKiem.setText("");
            tinhTongTien();
            loadKhoThuoc();
            generateNewMaHD();
        }

        private JPanel createSearchPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 15,fillx", "[]10[]push[]", "[]"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");

            JLabel lbSearch = new JLabel("Tìm thuốc:");
            lbSearch.putClientProperty(FlatClientProperties.STYLE, "font:bold");
            txtTimKiem = new JTextField();
            txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Gõ tên thuốc, mã lô, hoạt chất...");

            JButton btnRefresh = new JButton("Làm mới");
            btnRefresh.addActionListener(e -> loadKhoThuoc());

            panel.add(lbSearch, "split 2");
            panel.add(txtTimKiem, "w 100%");
            panel.add(btnRefresh);
            return panel;
        }

        private JPanel createCustomerPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 15,wrap 2", "[][grow,fill]", ""));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            JLabel lbTitle = new JLabel("Thông tin khách hàng");
            lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +1");
            panel.add(lbTitle, "span 2, wrap 10");

            txtSDT = new JTextField();
            txtSDT.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập SĐT + Enter");
            txtSDT.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        timKiemKhachHang();
                    }
                }
            });

            JButton btnTimKH = new JButton("Tìm kiếm");
            btnTimKH.addActionListener(e -> timKiemKhachHang());
            JButton btnThemKH = new JButton("+");
            btnThemKH.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold");
            btnThemKH.setToolTipText("Thêm khách hàng mới");
            btnThemKH.addActionListener(e -> themKhachHangMoi());

            panel.add(new JLabel("SĐT:"));
            panel.add(txtSDT, "split 3, growx");
            panel.add(btnTimKH, "w 30!");
            panel.add(btnThemKH, "w 30!, wrap");

            txtMaKH = new JTextField();
            txtMaKH.setEditable(false);
            txtTenKH = new JTextField();
            txtTenKH.setEditable(false);

            panel.add(new JLabel("Mã KH:"));
            panel.add(txtMaKH, "wrap");
            panel.add(new JLabel("Tên KH:"));
            panel.add(txtTenKH, "wrap");

            lbDiemHienCo = new JLabel("0 điểm");
            lbDiemHienCo.putClientProperty(FlatClientProperties.STYLE, "foreground:$Accent.color; font:bold");

            chkDungDiem = new JCheckBox("Dùng điểm");
            chkDungDiem.setEnabled(false);
            chkDungDiem.addActionListener(e -> tinhTongTien());

            panel.add(new JLabel("Điểm:"));
            panel.add(lbDiemHienCo, "split 2");
            panel.add(chkDungDiem, "gapleft 10");

            return panel;
        }

        private JPanel createMedicineTablePanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            JLabel title = new JLabel("Kho thuốc (Lô còn hạn)");
            title.putClientProperty(FlatClientProperties.STYLE, "font:bold +1");

            sourceList = new BasicEventList<>();
            TextFilterator<ThuocTimKiem> filterator = (list, t) -> {
                list.add(t.getMaThuoc());
                list.add(t.getTenThuoc());
                list.add(t.getMaLo());
            };
            filterList = new FilterList<>(sourceList, new TextComponentMatcherEditor<>(txtTimKiem, filterator));

            TableFormat<ThuocTimKiem> tableFormat = new TableFormat<ThuocTimKiem>() {
                @Override
                public int getColumnCount() {
                    return 6;
                }

                @Override
                public String getColumnName(int i) {
                    String[] cols = {"Mã Thuốc", "Tên Thuốc", "Lô", "HSD", "Tồn", "Giá Bán"};
                    return cols[i];
                }

                @Override
                public Object getColumnValue(ThuocTimKiem t, int i) {
                    switch (i) {
                        case 0:
                            return t.getMaThuoc();
                        case 1:
                            return t.getTenThuoc();
                        case 2:
                            return t.getMaLo();
                        case 3:
                            return t.getHanSuDung();
                        case 4:
                            return t.getSoLuongTon();
                        case 5:
                            return t.getGiaBan();
                        default:
                            return null;
                    }
                }
            };

            tableModelThuoc = new EventTableModel<>(filterList, tableFormat);
            tableThuoc = new JTable(tableModelThuoc);

            tableThuoc.getColumnModel().getColumn(3).setCellRenderer(new DateRenderer());
            tableThuoc.getColumnModel().getColumn(5).setCellRenderer(new CurrencyRenderer());
            tableThuoc.putClientProperty(FlatClientProperties.STYLE, "showHorizontalLines:true; rowHeight:30");
            tableThuoc.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

            btnThemVaoGio = new JButton("Thêm vào giỏ");
            btnThemVaoGio.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold");
            btnThemVaoGio.addActionListener(e -> themVaoGioHang());

            panel.add(title, BorderLayout.NORTH);
            panel.add(new JScrollPane(tableThuoc), BorderLayout.CENTER);
            panel.add(btnThemVaoGio, BorderLayout.SOUTH);
            return panel;
        }

        private JPanel createCartPanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            JLabel title = new JLabel("Giỏ hàng");
            title.putClientProperty(FlatClientProperties.STYLE, "font:bold +1");

            String[] cols = {"Mã Lô", "Tên thuốc", "SL", "Giá", "Thành tiền"};
            modelGioHang = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return col == 2;
                }
            };
            tableGioHang = new JTable(modelGioHang);
            tableGioHang.putClientProperty(FlatClientProperties.STYLE, "showHorizontalLines:true; rowHeight:30");
            tableGioHang.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

            modelGioHang.addTableModelListener(e -> {
                if (!isUpdating) {
                    tinhTongTien();
                }
            });

            btnXoaKhoiGio = new JButton("Xóa khỏi giỏ");
            btnXoaKhoiGio.putClientProperty(FlatClientProperties.STYLE, "background:#F44336; foreground:#fff");
            btnXoaKhoiGio.addActionListener(e -> xoaKhoiGioHang());

            panel.add(title, BorderLayout.NORTH);
            panel.add(new JScrollPane(tableGioHang), BorderLayout.CENTER);
            panel.add(btnXoaKhoiGio, BorderLayout.SOUTH);
            return panel;
        }

        private JPanel createPaymentPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 15", "[grow][][]", ""));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:lighten(#E8F5E9,5%)");

            JPanel infoPanel = new JPanel(new MigLayout("wrap 2", "[][grow,right]", "[]5[]5[]5[]"));
            infoPanel.setOpaque(false);

            lbTongTien = new JLabel("0 ₫");
            lbTongTien.putClientProperty(FlatClientProperties.STYLE, "font:bold +4");

            lbGiamGia = new JLabel("0 ₫");
            lbGiamGia.putClientProperty(FlatClientProperties.STYLE, "foreground:$Accent.color");

            txtThueVAT = new JTextField(String.valueOf((int) DEFAULT_VAT_RATE), 3);
            txtThueVAT.setHorizontalAlignment(JTextField.RIGHT);
            txtThueVAT.setEditable(false);

            lbTienThue = new JLabel("0 ₫");
            lbThanhTien = new JLabel("0 ₫");
            lbThanhTien.putClientProperty(FlatClientProperties.STYLE, "font:bold +8; foreground:#4CAF50");

            infoPanel.add(new JLabel("Tổng tiền hàng:"));
            infoPanel.add(lbTongTien);

            infoPanel.add(new JLabel("Giảm giá (Điểm):"));
            infoPanel.add(lbGiamGia);

            JPanel pTax = new JPanel(new MigLayout("insets 0", "[]2[]"));
            pTax.setOpaque(false);
            pTax.add(new JLabel("Thuế VAT (%):"));
            pTax.add(txtThueVAT);
            infoPanel.add(pTax);
            infoPanel.add(lbTienThue);
            lbMaHD = new JLabel("Loading...");
            lbMaHD.putClientProperty(FlatClientProperties.STYLE, "font:bold; foreground:#2196F3");
            infoPanel.add(new JLabel("Mã hóa đơn:"));
            infoPanel.add(lbMaHD);

            infoPanel.add(new JSeparator(), "span 2, growx, gapy 5");
            infoPanel.add(new JLabel("THÀNH TIỀN:"));
            infoPanel.add(lbThanhTien);

            JPanel ttPanel = new JPanel(new MigLayout("", "[][]", ""));
            ttPanel.setOpaque(false);
            ttPanel.add(new JLabel("Hình thức TT:"));
            cbHinhThucTT = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản"});
            ttPanel.add(cbHinhThucTT);

            btnThanhToan = new JButton("THANH TOÁN");
            btnThanhToan.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#FFFFFF; font:bold +4; arc:15");
            btnThanhToan.setPreferredSize(new Dimension(200, 60));
            btnThanhToan.addActionListener(e -> thanhToan());

            btnHuyHD = new JButton("Hủy");
            btnHuyHD.putClientProperty(FlatClientProperties.STYLE, "background:#9E9E9E; arc:15; foreground:#fff");
            btnHuyHD.setPreferredSize(new Dimension(100, 60));
            btnHuyHD.addActionListener(e -> resetForm());

            panel.add(infoPanel, "grow");
            panel.add(ttPanel, "");
            panel.add(btnHuyHD, "");
            panel.add(btnThanhToan, "");

            return panel;
        }

        private void generateNewMaHD() {
            String lastID = hoaDonDAO.getMaxMaHD();
            String newID = "HD001";

            if (lastID != null && !lastID.isEmpty()) {
                try {

                    String numberPart = lastID.substring(2);

                    int number = Integer.parseInt(numberPart);
                    number++;

                    newID = String.format("HD%03d", number);

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

            lbMaHD.setText(newID);
        }

        private void loadKhoThuoc() {
            sourceList.clear();
            sourceList.addAll(loThuocDAO.getDanhSachThuocBanHang());
        }

        private void timKiemKhachHang() {
            String sdt = txtSDT.getText().trim();
            if (sdt.isEmpty()) {
                setKhachVangLai();
                return;
            }

            KhachHang kh = khachHangDAO.getKhachHangBySDT(sdt);
            if (kh != null) {
                currentKhachHang = kh;
                txtMaKH.setText(kh.getMaKH());
                txtTenKH.setText(kh.getTenKH());
                lbDiemHienCo.setText(kh.getDiemTichLuy() + " điểm");

                if (kh.getDiemTichLuy() > 0) {
                    chkDungDiem.setEnabled(true);
                    chkDungDiem.setText("Dùng " + kh.getDiemTichLuy() + " điểm (-" + formatCurrency(kh.getDiemTichLuy() * 100) + ")");
                } else {
                    chkDungDiem.setEnabled(false);
                    chkDungDiem.setText("Không có điểm");
                }
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Khách hàng thành viên!");
            } else {
                setKhachVangLai();
                int opt = JOptionPane.showConfirmDialog(this, "Khách hàng chưa tồn tại. Tạo mới?", "Thông báo", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    themKhachHangMoi();
                }
            }
            tinhTongTien();
        }

        private void setKhachVangLai() {
            currentKhachHang = null;
            txtMaKH.setText("Vãng lai");
            txtTenKH.setText("Khách lẻ");
            lbDiemHienCo.setText("0 điểm");
            chkDungDiem.setSelected(false);
            chkDungDiem.setEnabled(false);
            chkDungDiem.setText("Dùng điểm");
        }

        private void themKhachHangMoi() {
            DialogKhachHang dialog = new DialogKhachHang(SwingUtilities.getWindowAncestor(this), null);
            dialog.setVisible(true);
            if (dialog.isSave()) {
                Object[] data = dialog.getData();
                try {
                    KhachHang newKH = new KhachHang();
                    newKH.setMaKH(data[0].toString());
                    newKH.setTenKH(data[1].toString());
                    newKH.setSdt(data[2].toString());
                    newKH.setGioiTinh(data[3].toString().equals("Nam"));
                    newKH.setDiaChi(data[5].toString());
                    newKH.setDiemTichLuy(0);
                    newKH.setTrangThai(true);

                    if (khachHangDAO.insert(newKH)) {
                        txtSDT.setText(newKH.getSdt());
                        timKiemKhachHang();
                        Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Tạo khách hàng thành công!");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void themVaoGioHang() {
            int row = tableThuoc.getSelectedRow();
            if (row == -1) {
                return;
            }
            ThuocTimKiem t = filterList.get(row);

            String slStr = JOptionPane.showInputDialog(this, "Số lượng bán (Tồn " + t.getSoLuongTon() + "):", "1");
            try {
                int sl = Integer.parseInt(slStr);
                if (sl <= 0 || sl > t.getSoLuongTon()) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Số lượng không hợp lệ!");
                    return;
                }

                boolean exists = false;
                for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                    if (modelGioHang.getValueAt(i, 0).equals(t.getMaLo())) {
                        int slOld = Integer.parseInt(modelGioHang.getValueAt(i, 2).toString());
                        int slNew = slOld + sl;
                        if (slNew > t.getSoLuongTon()) {
                            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vượt quá tồn kho!");
                            return;
                        }
                        modelGioHang.setValueAt(slNew, i, 2);
                        modelGioHang.setValueAt(formatCurrency(slNew * t.getGiaBan()), i, 4);
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    modelGioHang.addRow(new Object[]{t.getMaLo(), t.getTenThuoc(), sl, formatCurrency(t.getGiaBan()), formatCurrency(sl * t.getGiaBan())});
                }
                tinhTongTien();
            } catch (Exception e) {
            }
        }

        private void xoaKhoiGioHang() {
            int row = tableGioHang.getSelectedRow();
            if (row != -1) {
                modelGioHang.removeRow(row);
                tinhTongTien();
            }
        }

        private void tinhTongTien() {
            isUpdating = true;
            try {
                tongTien = 0;
                for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                    try {
                        int sl = Integer.parseInt(modelGioHang.getValueAt(i, 2).toString());
                        String giaTxt = modelGioHang.getValueAt(i, 3).toString().replace(".", "").replace(" ₫", "");
                        double gia = Double.parseDouble(giaTxt);
                        double thanhTien = sl * gia;
                        modelGioHang.setValueAt(formatCurrency(thanhTien), i, 4);
                        tongTien += thanhTien;
                    } catch (Exception e) {
                    }
                }

                tienGiamGia = 0;
                if (currentKhachHang != null && chkDungDiem.isSelected()) {
                    tienGiamGia = currentKhachHang.getDiemTichLuy() * 100;
                    if (tienGiamGia > tongTien) {
                        tienGiamGia = tongTien;
                    }
                }

                double tienSauGiam = tongTien - tienGiamGia;
                thueVAT = tienSauGiam * (DEFAULT_VAT_RATE / 100);
                double thanhTienCuoi = tienSauGiam + thueVAT;

                lbTongTien.setText(formatCurrency(tongTien));
                lbGiamGia.setText(formatCurrency(tienGiamGia));
                lbTienThue.setText(formatCurrency(thueVAT));
                lbThanhTien.setText(formatCurrency(thanhTienCuoi));
            } finally {
                isUpdating = false;
            }
        }

        private void thanhToan() {
            if (modelGioHang.getRowCount() == 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Giỏ hàng đang trống!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Thanh toán đơn hàng " + lbMaHD.getText() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {

                HoaDon hd = new HoaDon();
                hd.setMaHD(lbMaHD.getText());
                hd.setNgayTao(java.time.LocalDateTime.now());
                if (Auth.isLogin()) {
                    hd.setNhanVien(Auth.user);
                } else {
                    hd.setNhanVien(new NhanVien("NV001"));
                }
                hd.setKhachHang(currentKhachHang);

                String tongTienStr = lbTongTien.getText().replace(".", "").replace(" ₫", "");
                String thueStr = lbTienThue.getText().replace(".", "").replace(" ₫", "");
                String giamGiaStr = lbGiamGia.getText().replace(".", "").replace(" ₫", "");

                hd.setTongTien(Double.parseDouble(tongTienStr));
                hd.setThue(Double.parseDouble(thueStr));
                hd.setGiamGia(Double.parseDouble(giamGiaStr));
                hd.setHinhThucTT(cbHinhThucTT.getSelectedItem().toString());
                hd.setGhiChu("Bán hàng tại quầy");

                List<ChiTietHoaDon> listCT = new java.util.ArrayList<>();
                for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                    String maLo = modelGioHang.getValueAt(i, 0).toString();
                    String tenThuoc = modelGioHang.getValueAt(i, 1).toString();
                    int soLuong = Integer.parseInt(modelGioHang.getValueAt(i, 2).toString());
                    double donGia = Double.parseDouble(modelGioHang.getValueAt(i, 3).toString().replace(".", "").replace(" ₫", ""));
                    double thanhTien = Double.parseDouble(modelGioHang.getValueAt(i, 4).toString().replace(".", "").replace(" ₫", ""));

                    Thuoc t = new Thuoc("");

                    LoThuoc lo = new LoThuoc(maLo, null, null, null, 0, "", false);

                    String maThuoc = loThuocDAO.getMaThuocByMaLo(maLo);
                    t.setMaThuoc(maThuoc);

                    ChiTietHoaDon ct = new ChiTietHoaDon(hd, t, lo, soLuong, donGia, thanhTien);
                    listCT.add(ct);
                }

                if (hoaDonDAO.taoHoaDon(hd, listCT)) {

                    if (currentKhachHang != null) {

                        if (chkDungDiem.isSelected()) {
                            khachHangDAO.truDiem(currentKhachHang.getMaKH(), currentKhachHang.getDiemTichLuy());
                        }

                        int diemMoi = (int) ((hd.getTongTien() - hd.getGiamGia()) / 10000);
                        if (diemMoi > 0) {
                            khachHangDAO.congDiem(currentKhachHang.getMaKH(), diemMoi);
                        }
                    }

                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Thanh toán thành công!");
                    resetForm();
                } else {
                    Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi khi lưu hóa đơn!");
                }

            } catch (Exception e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi dữ liệu!");
            }
        }

        private String formatCurrency(double amount) {
            return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + " ₫";
        }
    }

    private class DateRenderer extends DefaultTableCellRenderer {

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            if (v instanceof LocalDate) {
                setText(((LocalDate) v).format(df));
            }
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    }

    private class CurrencyRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            if (v instanceof Double) {
                setText(NumberFormat.getInstance(new Locale("vi", "VN")).format(v) + " ₫");
            }
            setHorizontalAlignment(JLabel.RIGHT);
            return this;
        }
    }
}
