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
import dao.ChiTietBangGiaDAO;
import dao.DonViQuyDoiDAO;
import dao.HoaDonDAO;
import dao.KhachHangDAO;
import dao.LoThuocDAO;
import dto.ThuocTimKiem;
import entities.ChiTietHoaDon;
import entities.DonViQuyDoi;
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
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;
import utils.Auth;

public class FormBanHang extends JPanel {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private PanelDanhSachHoaDon panelDanhSach;
    private PanelBanHangGiaoDien panelBanHang;
    private DonViQuyDoiDAO donViQuyDoiDAO = new DonViQuyDoiDAO();
    private ChiTietBangGiaDAO chiTietBangGiaDAO = new ChiTietBangGiaDAO();

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
        private dao.DonViQuyDoiDAO dvqdDAO = new dao.DonViQuyDoiDAO();

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
                    return new String[]{"Mã Thuốc", "Tên Thuốc", "Lô", "HSD", "Tồn", "Giá Bán"}[i];
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
                            return formatCurrency(t.getGiaBan());
                        default:
                            return null;
                    }
                }
            };

            tableModelThuoc = new EventTableModel<>(filterList, tableFormat);
            tableThuoc = new JTable(tableModelThuoc);

            tableThuoc.putClientProperty(FlatClientProperties.STYLE, "showHorizontalLines:true; rowHeight:30");
            tableThuoc.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");
            tableThuoc.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer(){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    
                    if (value instanceof java.time.LocalDate) {
                        setText(((java.time.LocalDate) value).format(formatter));
                    }
                    
                    setHorizontalAlignment(JLabel.CENTER); // Căn giữa cho đẹp
                    return this;
                }
            });
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

            String[] cols = {"Mã Lô", "Tên thuốc", "Đơn vị", "SL", "Đơn giá", "Thành tiền", "Hidden_List"};
            modelGioHang = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return col == 2 || col == 3;
                }
            };
            tableGioHang = new JTable(modelGioHang);
            tableGioHang.putClientProperty(FlatClientProperties.STYLE, "showHorizontalLines:true; rowHeight:30");
            tableGioHang.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

            tableGioHang.removeColumn(tableGioHang.getColumnModel().getColumn(6));

            UnitCellEditor unitEditor = new UnitCellEditor();

            unitEditor.addCellEditorListener(new CellEditorListener() {
                @Override
                public void editingStopped(ChangeEvent e) {

                    int row = tableGioHang.getSelectedRow();
                    if (row != -1) {
                        updateGiaKhiDoiDonVi(row);
                    }
                }

                @Override
                public void editingCanceled(ChangeEvent e) {
                }
            });

            tableGioHang.getColumnModel().getColumn(2).setCellEditor(unitEditor);

            modelGioHang.addTableModelListener(e -> {
                if (e.getColumn() == 3) {
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


        private void updateGiaKhiDoiDonVi(int row) {
            isUpdating = true;
            try {

                Object valDVT = modelGioHang.getValueAt(row, 2);
                if (valDVT == null) {
                    return;
                }
                String tenMoi = valDVT.toString();

                Object valList = modelGioHang.getValueAt(row, 6);

                if (valList instanceof List) {
                    List<entities.DonViQuyDoi> list = (List<entities.DonViQuyDoi>) valList;

                    for (entities.DonViQuyDoi dv : list) {
                        if (dv.getTenDonVi().equals(tenMoi)) {

                            // Validate conversion rate first (fail fast)
                            if (dv.getGiaTriQuyDoi() <= 0) {
                                Notifications.getInstance().show(
                                    Notifications.Type.ERROR,
                                    Notifications.Location.TOP_CENTER,
                                    "Đơn vị quy đổi không hợp lệ"
                                );
                                return;
                            }

                            // Validate stock when changing unit
                            String maLo = modelGioHang.getValueAt(row, 0).toString();
                            int soLuongBan = Integer.parseInt(modelGioHang.getValueAt(row, 3).toString());
                            
                            // Calculate converted quantity using long to prevent overflow
                            long soLuongQuyDoiLong = (long) soLuongBan * (long) dv.getGiaTriQuyDoi();
                            if (soLuongQuyDoiLong > Integer.MAX_VALUE) {
                                Notifications.getInstance().show(
                                    Notifications.Type.ERROR,
                                    Notifications.Location.TOP_CENTER,
                                    "Số lượng quy đổi vượt quá giới hạn cho phép"
                                );
                                return;
                            }
                            int soLuongQuyDoi = (int) soLuongQuyDoiLong;
                            
                            // Get stock for this specific lot
                            int tonKho = loThuocDAO.getTonKhoByMaLo(maLo);
                            
                            if (soLuongQuyDoi > tonKho) {
                                // Calculate maximum quantity for this unit
                                int soLuongToiDa = tonKho / dv.getGiaTriQuyDoi();
                                
                                if (soLuongToiDa > 0) {
                                    modelGioHang.setValueAt(soLuongToiDa, row, 3);
                                    modelGioHang.setValueAt(formatCurrency(dv.getGiaBan()), row, 4);
                                    Notifications.getInstance().show(
                                        Notifications.Type.WARNING,
                                        Notifications.Location.TOP_CENTER,
                                        "Số lượng quy đổi (" + soLuongQuyDoi + ") vượt tồn lô (" + tonKho + "). Đã tự động điều chỉnh về " + soLuongToiDa + " " + tenMoi
                                    );
                                } else {
                                    // Cannot change to this unit - not enough stock
                                    Notifications.getInstance().show(
                                        Notifications.Type.ERROR,
                                        Notifications.Location.TOP_CENTER,
                                        "Không đủ tồn lô để bán với đơn vị " + tenMoi + ". Tồn lô hiện tại: " + tonKho
                                    );
                                    // Don't update the unit - user needs to reduce quantity first
                                    return;
                                }
                            } else {
                                // Stock is sufficient, proceed with update
                                modelGioHang.setValueAt(formatCurrency(dv.getGiaBan()), row, 4);
                            }

                            tinhTongTien();
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isUpdating = false;
            }
        }

        private void themVaoGioHang() {
            int row = tableThuoc.getSelectedRow();
            if (row == -1) {
                return;
            }
            ThuocTimKiem t = filterList.get(row);

            List<entities.DonViQuyDoi> listDVT = dvqdDAO.getAllDonViByMaThuoc(t.getMaThuoc());
            listDVT.removeIf(dv -> dv.getGiaBan() <= 0);
            if (listDVT.isEmpty()) {

                listDVT.add(new entities.DonViQuyDoi(0, t.getMaThuoc(), t.getDonViTinh(), 1, t.getGiaBan(), true));
            }

            entities.DonViQuyDoi baseUnit = listDVT.get(0);
            for (entities.DonViQuyDoi dv : listDVT) {
                if (dv.getGiaTriQuyDoi() == 1) {
                    baseUnit = dv;
                    break;
                }
            }

            String slStr = JOptionPane.showInputDialog(this,
                    "Số lượng bán (" + baseUnit.getTenDonVi() + ") - Tồn Lô: " + t.getSoLuongTon(), "1");
            try {
                if (slStr == null) {
                    return;
                }
                int sl = Integer.parseInt(slStr);
                if (sl <= 0 || sl > t.getSoLuongTon()) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Số lượng không hợp lệ hoặc quá tồn!");
                    return;
                }

                boolean exists = false;
                for (int i = 0; i < modelGioHang.getRowCount(); i++) {

                    if (modelGioHang.getValueAt(i, 0).equals(t.getMaLo())
                            && modelGioHang.getValueAt(i, 2).equals(baseUnit.getTenDonVi())) {

                        int slOld = Integer.parseInt(modelGioHang.getValueAt(i, 3).toString());
                        int slNew = slOld + sl;
                        if (slNew > t.getSoLuongTon()) {
                            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vượt quá tồn kho!");
                            return;
                        }
                        modelGioHang.setValueAt(slNew, i, 3);
                        modelGioHang.setValueAt(formatCurrency(slNew * baseUnit.getGiaBan()), i, 5);
                        exists = true;
                        break;
                    }
                }

                if (!exists) {

                    modelGioHang.addRow(new Object[]{
                        t.getMaLo(),
                        t.getTenThuoc(),
                        baseUnit.getTenDonVi(),
                        sl,
                        formatCurrency(baseUnit.getGiaBan()),
                        formatCurrency(sl * baseUnit.getGiaBan()),
                        listDVT
                    });
                }
                tinhTongTien();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void xoaKhoiGioHang() {
            int row = tableGioHang.getSelectedRow();
            if (row != -1) {

                if (tableGioHang.isEditing()) {
                    tableGioHang.getCellEditor().stopCellEditing();
                }
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
                        int sl = Integer.parseInt(modelGioHang.getValueAt(i, 3).toString());
                        String giaTxt = modelGioHang.getValueAt(i, 4).toString();
                        double gia = parseMoney(giaTxt);

                        double thanhTien = sl * gia;
                        modelGioHang.setValueAt(formatCurrency(thanhTien), i, 5);
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

            if (tableGioHang.isEditing()) {
                tableGioHang.getCellEditor().stopCellEditing();
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Thanh toán đơn hàng " + lbMaHD.getText() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                HoaDon hd = new HoaDon();
                hd.setMaHD(lbMaHD.getText());
                hd.setNgayTao(java.time.LocalDateTime.now());
                hd.setNhanVien(Auth.isLogin() ? Auth.user : new NhanVien("NV001"));
                hd.setKhachHang(currentKhachHang);

                hd.setTongTien(parseMoney(lbTongTien.getText()));
                hd.setThue(parseMoney(lbTienThue.getText()));
                hd.setGiamGia(parseMoney(lbGiamGia.getText()));
                hd.setHinhThucTT(cbHinhThucTT.getSelectedItem().toString());
                hd.setGhiChu("Bán hàng tại quầy");

                List<ChiTietHoaDon> listCT = new java.util.ArrayList<>();
                for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                    String maLo = modelGioHang.getValueAt(i, 0).toString();
                    String tenThuoc = modelGioHang.getValueAt(i, 1).toString();
                    String donViTinh = modelGioHang.getValueAt(i, 2).toString();
                    int soLuong = Integer.parseInt(modelGioHang.getValueAt(i, 3).toString());
                    double donGia = parseMoney(modelGioHang.getValueAt(i, 4).toString());
                    double thanhTien = parseMoney(modelGioHang.getValueAt(i, 5).toString());

                    Thuoc t = new Thuoc("");
                    String maThuoc = loThuocDAO.getMaThuocByMaLo(maLo);
                    t.setMaThuoc(maThuoc);
                    LoThuoc lo = new LoThuoc(maLo, null, null, null, 0, "", false);

                    ChiTietHoaDon ct = new ChiTietHoaDon(hd, t, lo, soLuong, donGia, thanhTien, donViTinh);
                    ct.setDonViTinh(donViTinh);

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

        private String formatCurrency(double amount) {
            return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + " ₫";
        }

        private double parseMoney(String text) {
            try {
                if (text == null) {
                    return 0;
                }
                return Double.parseDouble(text.replace(".", "").replace(",", "").replace(" ₫", "").trim());
            } catch (Exception e) {
                return 0;
            }
        }
    }

    private class UnitCellEditor extends javax.swing.DefaultCellEditor {

        private javax.swing.JComboBox<String> comboBox;

        public UnitCellEditor() {
            super(new javax.swing.JComboBox<>());
            this.comboBox = (javax.swing.JComboBox<String>) getComponent();
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int column) {
            comboBox.removeAllItems();

            int modelRow = table.convertRowIndexToModel(row);
            Object valList = table.getModel().getValueAt(modelRow, 6);

            if (valList instanceof java.util.List) {
                java.util.List<entities.DonViQuyDoi> list = (java.util.List<entities.DonViQuyDoi>) valList;
                for (entities.DonViQuyDoi dv : list) {
                    comboBox.addItem(dv.getTenDonVi());
                }
            }

            if (comboBox.getItemCount() == 0) {
                comboBox.addItem(value != null ? value.toString() : "");
            }

            comboBox.setSelectedItem(value);
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }
}
