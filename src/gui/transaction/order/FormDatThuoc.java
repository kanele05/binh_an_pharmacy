package gui.transaction.order;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.formdev.flatlaf.FlatClientProperties;
import dao.ChiTietDonDatDAO;
import dao.DonDatHangDAO;
import dao.DonViQuyDoiDAO;
import dao.KhachHangDAO;
import dao.LoThuocDAO;
import dto.ThuocTimKiem;
import entities.ChiTietDonDat;
import entities.DonDatHang;
import entities.DonViQuyDoi;
import entities.KhachHang;
import entities.LoThuoc;
import entities.NhanVien;
import entities.Thuoc;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Color;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import raven.datetime.component.date.DatePicker;
import raven.toast.Notifications;
import utils.Auth;

public class FormDatThuoc extends javax.swing.JPanel {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private PanelDanhSachDonDat panelDanhSach;
    private PanelDatThuocGiaoDien panelDatThuoc;

    public FormDatThuoc() {
        initComponents();
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        panelDanhSach = new PanelDanhSachDonDat();
        panelDatThuoc = new PanelDatThuocGiaoDien();

        mainPanel.add(panelDanhSach, "LIST");
        mainPanel.add(panelDatThuoc, "FORM");

        add(mainPanel, BorderLayout.CENTER);
    }

    public void showTaoPhieu() {
        panelDatThuoc.resetForm();
        cardLayout.show(mainPanel, "FORM");
    }

    public void showSuaPhieu(DonDatHang don) {
        panelDatThuoc.loadDataToForm(don);
        cardLayout.show(mainPanel, "FORM");
    }

    public void showDanhSach() {
        panelDanhSach.loadData();
        cardLayout.show(mainPanel, "LIST");
    }

    // Inner class for list view panel
    private class PanelDanhSachDonDat extends JPanel {

        private JTextField txtTimKiem;
        private JComboBox<String> cbTrangThai;
        private JTable table;
        private EventList<DonDatHang> sourceList;
        private FilterList<DonDatHang> textFilteredList;
        private FilterList<DonDatHang> compositeFilteredList;
        private EventTableModel<DonDatHang> tableModel;
        private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        private DonDatHangDAO donDatHangDAO = new DonDatHangDAO();

        public PanelDanhSachDonDat() {
            initPanel();
        }

        private void initPanel() {
            setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow]"));

            add(createHeaderPanel(), "wrap 20");

            add(createToolBarPanel(), "wrap 10");

            add(createTablePanel(), "grow");

            loadData();
        }

        private JPanel createHeaderPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 0", "[grow,fill][]"));
            panel.setOpaque(false);
            JLabel lbTitle = new JLabel("Đơn Đặt Thuốc");
            lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
            panel.add(lbTitle);
            return panel;
        }

        private JPanel createToolBarPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]push[][]", "[]"));
            panel.putClientProperty(FlatClientProperties.STYLE, ""
                    + "arc:20;"
                    + "background:darken(@background,3%)");

            txtTimKiem = new JTextField();
            txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm SĐT hoặc tên khách...");

            JButton btnTim = new JButton("Tìm kiếm");
            cbTrangThai = new JComboBox<>(new String[]{"Tất cả", "Đang giữ hàng", "Đã lấy hàng", "Đã hủy"});
            cbTrangThai.addActionListener(e -> filterStatus());
            JButton btnTaoPhieu = new JButton("Tạo phiếu giữ");
            btnTaoPhieu.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
            btnTaoPhieu.addActionListener(e -> actionTaoPhieu());

            JButton btnKhachDen = new JButton("Khách đến lấy hàng");
            btnKhachDen.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold");
            btnKhachDen.addActionListener(e -> actionKhachDenLay());

            JButton btnEdit = new JButton("Chỉnh sửa");
            btnEdit.putClientProperty(FlatClientProperties.STYLE, "background:#FF9800; foreground:#fff; font:bold");
            btnEdit.addActionListener(e -> actionEditPhieu());

            panel.add(txtTimKiem, "w 250");
            panel.add(btnTim);
            panel.add(new JLabel("Lọc trạng thái:"));
            panel.add(cbTrangThai);
            panel.add(btnTaoPhieu);
            panel.add(btnEdit);
            panel.add(btnKhachDen);

            return panel;
        }

        private JPanel createTablePanel() {
            JPanel panel = new JPanel(new java.awt.BorderLayout());
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            sourceList = new BasicEventList<>();

            TextFilterator<DonDatHang> textFilterator = (list, d) -> {
                list.add(d.getMaDonDat());
                list.add(d.getTenKhach());
                list.add(d.getSdtLienHe());
            };
            TextComponentMatcherEditor<DonDatHang> textMatcher = new TextComponentMatcherEditor<>(txtTimKiem, textFilterator);
            textFilteredList = new FilterList<>(sourceList, textMatcher);

            compositeFilteredList = new FilterList<>(textFilteredList);

            TableFormat<DonDatHang> tableFormat = new TableFormat<DonDatHang>() {
                @Override
                public int getColumnCount() {
                    return 7;
                }

                @Override
                public String getColumnName(int i) {
                    return new String[]{"Mã Phiếu", "Khách Hàng", "SĐT", "Giờ Hẹn Lấy", "Tổng Tiền", "Ghi Chú", "Trạng Thái"}[i];
                }

                @Override
                public Object getColumnValue(DonDatHang d, int i) {
                    switch (i) {
                        case 0:
                            return d.getMaDonDat();
                        case 1:
                            return d.getTenKhach();
                        case 2:
                            return d.getSdtLienHe();
                        case 3:
                            return d.getGioHenLay();
                        case 4:
                            return d.getTongTien();
                        case 5:
                            return d.getGhiChu();
                        case 6:
                            return d.getTrangThai();
                        default:
                            return null;
                    }
                }
            };

            tableModel = new EventTableModel<>(compositeFilteredList, tableFormat);

            table = new JTable(tableModel);
            table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
            table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

            table.getColumnModel().getColumn(3).setCellRenderer(new DateRenderer());
            table.getColumnModel().getColumn(4).setCellRenderer(new CurrencyRenderer());
            table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());

            panel.add(new JScrollPane(table));
            return panel;
        }

        private class DateRenderer extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (v instanceof LocalDateTime) {
                    setText(((LocalDateTime) v).format(dateTimeFormat));
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
                    setForeground(new Color(0, 150, 136));
                }
                setHorizontalAlignment(JLabel.RIGHT);
                return this;
            }
        }

        private class StatusRenderer extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "";
                if (status.equals("Đang giữ hàng")) {
                    setForeground(new Color(255, 152, 0));
                    setFont(getFont().deriveFont(java.awt.Font.BOLD));
                } else if (status.equals("Đã lấy hàng")) {
                    setForeground(new Color(76, 175, 80));
                } else {
                    setForeground(Color.GRAY);
                }
                if (isSelected) {
                    setForeground(Color.WHITE);
                }
                return com;
            }
        }

        public void loadData() {
            sourceList.clear();
            sourceList.addAll(donDatHangDAO.getAllDonDat());
        }

        private void filterStatus() {
            String selected = cbTrangThai.getSelectedItem().toString();
            Matcher<DonDatHang> matcher = item -> {
                if (selected.equals("Tất cả")) {
                    return true;
                }
                return item.getTrangThai().equalsIgnoreCase(selected);
            };
            compositeFilteredList.setMatcher(matcher);
        }

        private void actionEditPhieu() {
            int row = table.getSelectedRow();
            if (row == -1) {
                Notifications.getInstance().show(Notifications.Type.WARNING, "Vui lòng chọn phiếu cần sửa!");
                return;
            }

            DonDatHang donSelected = compositeFilteredList.get(row);

            if (!donSelected.getTrangThai().equals("Đang giữ hàng")) {
                Notifications.getInstance().show(Notifications.Type.WARNING, "Chỉ được sửa đơn đang giữ hàng!");
                return;
            }

            showSuaPhieu(donSelected);
        }

        private void actionTaoPhieu() {
            showTaoPhieu();
        }

        private void actionKhachDenLay() {
            int row = table.getSelectedRow();
            if (row == -1) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn phiếu giữ hàng!");
                return;
            }

            DonDatHang don = compositeFilteredList.get(row);

            if (!don.getTrangThai().equalsIgnoreCase("Đang giữ hàng")) {
                Notifications.getInstance().show(Notifications.Type.WARNING, "Chỉ xử lý được đơn đang giữ hàng!");
                return;
            }

            DialogThanhToanDonDat dialog = new DialogThanhToanDonDat(FormDatThuoc.this, don);
            dialog.packAndCenter();
            dialog.setVisible(true);

            if (dialog.isSuccess()) {
                loadData();
            }
        }
    }

    // Inner class for form panel
    private class PanelDatThuocGiaoDien extends JPanel {

        private KhachHang khachHangSelected = null;
        private String tenKhachVangLai = "";
        private LocalDateTime thoiGianHen;
        private double tongTien = 0;

        private KhachHangDAO khachHangDAO = new KhachHangDAO();
        private LoThuocDAO loThuocDAO = new LoThuocDAO();
        private DonDatHangDAO donDatHangDAO = new DonDatHangDAO();

        private JTextField txtSDT, txtTenKH;
        private JFormattedTextField txtNgayHen;
        private DatePicker datePicker;
        private JSpinner timeSpinner;

        private JTextField txtTimThuoc;
        private JTable tableThuoc;
        private EventList<ThuocTimKiem> sourceList;
        private FilterList<ThuocTimKiem> filterList;

        private JTable tableGioHang;
        private DefaultTableModel modelGioHang;
        private JLabel lbTongTien;
        private boolean isUpdating = false;

        private DonDatHang donHienTai = null;
        private ChiTietDonDatDAO ctDAO = new ChiTietDonDatDAO();
        private DonViQuyDoiDAO dvqdDAO = new DonViQuyDoiDAO();

        public PanelDatThuocGiaoDien() {
            initFormPanel();
            loadDataThuoc();
        }

        private void initFormPanel() {
            setLayout(new MigLayout("wrap,fill,insets 15", "[45%,fill][55%,fill]", "[][grow][]"));

            // Header with back button
            JPanel header = new JPanel(new MigLayout("insets 0", "[]10[]push"));
            header.setOpaque(false);
            JButton btnBack = new JButton("← Quay lại");
            btnBack.addActionListener(e -> showDanhSach());
            JLabel lbTitle = new JLabel("TẠO PHIẾU ĐẶT THUỐC");
            lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +6; foreground:$Accent.color");
            header.add(btnBack);
            header.add(lbTitle);
            add(header, "span 2, growx, wrap 10");

            JPanel pLeft = new JPanel(new MigLayout("wrap, fillx, insets 0", "[fill]", "[]10[grow]"));
            pLeft.setOpaque(false);
            pLeft.add(createCustomerPanel());
            pLeft.add(createSourceTablePanel(), "grow");
            add(pLeft, "grow");

            JPanel pRight = new JPanel(new MigLayout("wrap, fill, insets 0", "[fill]", "[grow][]"));
            pRight.setOpaque(false);
            pRight.add(createCartPanel(), "grow");
            pRight.add(createFooterPanel(), "growx");
            add(pRight, "grow");
        }

        public void resetForm() {
            donHienTai = null;
            khachHangSelected = null;
            txtSDT.setText("");
            txtTenKH.setText("");
            txtTenKH.setEditable(true);
            datePicker.setSelectedDate(LocalDate.now());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, 30);
            timeSpinner.setValue(cal.getTime());
            modelGioHang.setRowCount(0);
            tongTien = 0;
            lbTongTien.setText("Tổng cộng: 0 ₫");
            loadDataThuoc();
        }

        public void loadDataToForm(DonDatHang donEdit) {
            this.donHienTai = donEdit;

            txtSDT.setText(donEdit.getSdtLienHe());
            if (donEdit.getKhachHang() != null) {
                khachHangSelected = donEdit.getKhachHang();
                txtTenKH.setText(donEdit.getKhachHang().getTenKH());
                txtTenKH.setEditable(false);
            } else {
                txtTenKH.setText(donEdit.getTenKhach());
                txtTenKH.setEditable(true);
            }

            List<ChiTietDonDat> listCT = ctDAO.getChiTietByMaDon(donEdit.getMaDonDat());
            modelGioHang.setRowCount(0);

            for (ChiTietDonDat ct : listCT) {
                List<DonViQuyDoi> listDVT = dvqdDAO.getAllDonViByMaThuoc(ct.getThuoc().getMaThuoc());
                listDVT.removeIf(dv -> dv.getGiaBan() <= 0);
                if (listDVT.isEmpty()) {
                    listDVT.add(new DonViQuyDoi(0, ct.getThuoc().getMaThuoc(), ct.getDonViTinh(), 1, ct.getDonGia(), true));
                }

                // Lấy giá bán đúng theo đơn vị tính đã chọn
                double donGiaTheoUnit = ct.getDonGia();
                for (DonViQuyDoi dv : listDVT) {
                    if (dv.getTenDonVi().equals(ct.getDonViTinh())) {
                        donGiaTheoUnit = dv.getGiaBan();
                        break;
                    }
                }

                // Lấy danh sách lô của thuốc
                List<LoThuoc> listLo = loThuocDAO.getLoByMaThuoc(ct.getThuoc().getMaThuoc());
                String loDisplay = "";
                if (!listLo.isEmpty()) {
                    LoThuoc loMacDinh = listLo.get(0);
                    loDisplay = loMacDinh.getMaLo() + " (" + loMacDinh.getHanSuDung().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";
                }

                modelGioHang.addRow(new Object[]{
                    ct.getThuoc().getMaThuoc(),
                    ct.getThuoc().getTenThuoc(),
                    loDisplay,                      // Lô (HSD) - cột 2
                    ct.getSoLuong(),                // SL Đặt - cột 3
                    ct.getDonViTinh(),              // Đơn vị tính - cột 4
                    formatMoney(donGiaTheoUnit),    // Đơn giá - cột 5 (load đúng giá theo đơn vị)
                    formatMoney(ct.getSoLuong() * donGiaTheoUnit), // Thành tiền - cột 6
                    listDVT,                        // Hidden_List - cột 7
                    listLo                          // Hidden_LoList - cột 8
                });
            }
            tinhTongTien();
            loadDataThuoc();
        }

        private JPanel createCustomerPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 15, fillx, wrap 2", "[][grow]", "[]5[]5[]"));
            panel.setBorder(BorderFactory.createTitledBorder("Thông tin đặt hàng"));
            panel.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:darken(@background,3%)");

            txtSDT = new JTextField();
            txtSDT.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập SĐT và Enter...");
            txtSDT.addActionListener(e -> timKhachHang());
            JButton btnTim = new JButton("Tìm kiếm");
            btnTim.addActionListener(e -> timKhachHang());

            panel.add(new JLabel("Số điện thoại:"));
            panel.add(txtSDT, "split 2, growx");
            panel.add(btnTim);

            txtTenKH = new JTextField();
            txtTenKH.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tên nếu là khách mới");
            panel.add(new JLabel("Tên khách:"));
            panel.add(txtTenKH, "growx");

            txtNgayHen = new JFormattedTextField();
            datePicker = new DatePicker();
            datePicker.setEditor(txtNgayHen);
            datePicker.setDateFormat("dd/MM/yyyy");
            datePicker.setSelectedDate(LocalDate.now());

            SpinnerDateModel timeModel = new SpinnerDateModel();
            timeSpinner = new JSpinner(timeModel);
            JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
            timeSpinner.setEditor(timeEditor);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, 30);
            timeSpinner.setValue(cal.getTime());

            panel.add(new JLabel("Hẹn lấy lúc:"));
            panel.add(timeSpinner, "split 2, w 80!");
            panel.add(txtNgayHen, "growx");

            return panel;
        }

        private JPanel createSourceTablePanel() {
            JPanel panel = new JPanel(new MigLayout("insets 0, fill, wrap", "[fill]", "[][grow]"));
            panel.setBorder(BorderFactory.createTitledBorder("Tra cứu thuốc"));

            JPanel toolBar = new JPanel(new MigLayout("insets 0", "[]5[grow]", ""));

            txtTimThuoc = new JTextField();
            txtTimThuoc.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Gõ tên thuốc hoặc mã thuốc để tìm...");

            toolBar.add(new JLabel("Tìm nhanh:"));
            toolBar.add(txtTimThuoc, "growx");
            panel.add(toolBar);

            sourceList = new BasicEventList<>();

            TextFilterator<ThuocTimKiem> filterator = (list, t) -> {
                list.add(t.getTenThuoc());
                list.add(t.getMaThuoc());
            };

            TextComponentMatcherEditor<ThuocTimKiem> textMatcher = new TextComponentMatcherEditor<>(txtTimThuoc, filterator);
            filterList = new FilterList<>(sourceList, textMatcher);

            TableFormat<ThuocTimKiem> tf = new TableFormat<ThuocTimKiem>() {
                @Override
                public int getColumnCount() {
                    return 7;
                }

                @Override
                public String getColumnName(int i) {
                    return new String[]{"Mã", "Tên Thuốc", "Lô", "HSD", "ĐVT", "Tồn Tổng", "Giá Bán"}[i];
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
                            return t.getDonViTinh();
                        case 5:
                            return t.getSoLuongTon();
                        case 6:
                            return t.getGiaBan();
                        default:
                            return null;
                    }
                }
            };

            EventTableModel<ThuocTimKiem> etm = new EventTableModel<>(filterList, tf);
            tableThuoc = new JTable(etm);
            tableThuoc.putClientProperty(FlatClientProperties.STYLE, "rowHeight:25; showHorizontalLines:true");
            tableThuoc.getColumnModel().getColumn(6).setCellRenderer(new RightAlignRenderer());
            tableThuoc.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    
                    if (value instanceof java.time.LocalDate) {
                        setText(((java.time.LocalDate) value).format(formatter));
                    }
                    
                    setHorizontalAlignment(JLabel.CENTER);
                    return this;
                }
            });
            tableThuoc.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        themVaoGio();
                    }
                }
            });

            JScrollPane scroll = new JScrollPane(tableThuoc);
            panel.add(scroll, "grow");

            JButton btnAdd = new JButton("Thêm vào phiếu");
            btnAdd.addActionListener(e -> themVaoGio());
            panel.add(btnAdd, "right");

            return panel;
        }

        private JPanel createCartPanel() {
            JPanel panel = new JPanel(new java.awt.BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder("Danh sách đặt"));

            String[] cols = {"Mã Thuốc", "Tên thuốc", "Lô (HSD)", "SL Đặt", "Đơn vị tính", "Đơn giá", "Thành tiền", "Hidden_List", "Hidden_LoList"};
            modelGioHang = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return c == 2 || c == 3 || c == 4; // Lô, SL Đặt, Đơn vị tính
                }
            };

            modelGioHang.addTableModelListener(e -> {
                if (!isUpdating) {
                    tinhTongTien();
                }
            });

            tableGioHang = new JTable(modelGioHang);
            tableGioHang.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
            tableGioHang.getColumnModel().getColumn(5).setCellRenderer(new RightAlignRenderer()); // Đơn giá
            tableGioHang.getColumnModel().getColumn(6).setCellRenderer(new RightAlignRenderer()); // Thành tiền
            // Ẩn 2 cột hidden
            tableGioHang.removeColumn(tableGioHang.getColumnModel().getColumn(8)); // Hidden_LoList
            tableGioHang.removeColumn(tableGioHang.getColumnModel().getColumn(7)); // Hidden_List

            // Editor cho cột Lô
            LoCellEditor loEditor = new LoCellEditor();
            tableGioHang.getColumnModel().getColumn(2).setCellEditor(loEditor);

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

            tableGioHang.getColumnModel().getColumn(4).setCellEditor(unitEditor); // Đơn vị tính ở cột 4

            modelGioHang.addTableModelListener(e -> {
                // Column 3 is "SL Đặt" (Quantity Ordered) - sau khi thêm cột Lô
                if (e.getColumn() == 3 && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    if (row >= 0 && !isUpdating) {
                        validateSoLuongTonKho(row);
                    }
                    tinhTongTien();
                }
            });
            JScrollPane scroll = new JScrollPane(tableGioHang);

            JButton btnXoa = new JButton("Xóa dòng");
            btnXoa.putClientProperty(FlatClientProperties.STYLE, "foreground:#F44336");
            btnXoa.addActionListener(e -> {
                int row = tableGioHang.getSelectedRow();
                if (row != -1) {
                    if (tableGioHang.isEditing()) {
                        tableGioHang.getCellEditor().stopCellEditing();
                    }
                    modelGioHang.removeRow(row);
                    tinhTongTien();
                }
            });

            panel.add(scroll, java.awt.BorderLayout.CENTER);
            panel.add(btnXoa, java.awt.BorderLayout.SOUTH);
            return panel;
        }

        private JPanel createFooterPanel() {
            JPanel panel = new JPanel(new MigLayout("insets 10", "push[]20[]"));

            lbTongTien = new JLabel("Tổng cộng: 0 ₫");
            lbTongTien.putClientProperty(FlatClientProperties.STYLE, "font:bold +10; foreground:#D32F2F");

            JButton btnLuu = new JButton("LƯU PHIẾU");
            btnLuu.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold +2; margin:10,20,10,20");
            btnLuu.addActionListener(e -> actionSave());

            JButton btnHuy = new JButton("Hủy");
            btnHuy.addActionListener(e -> showDanhSach());

            panel.add(lbTongTien);
            panel.add(btnHuy);
            panel.add(btnLuu);
            return panel;
        }

        private void loadDataThuoc() {
            sourceList.clear();
            sourceList.addAll(loThuocDAO.getDanhSachThuocBanHang());
        }

        private void timKhachHang() {
            String sdt = txtSDT.getText().trim();
            if (sdt.isEmpty()) {
                return;
            }

            KhachHang kh = khachHangDAO.getKhachHangBySDT(sdt);
            if (kh != null) {
                khachHangSelected = kh;
                txtTenKH.setText(kh.getTenKH());
                txtTenKH.setEditable(false);
                Notifications.getInstance().show(Notifications.Type.SUCCESS, "Đã tìm thấy khách hàng!");
            } else {
                khachHangSelected = null;
                txtTenKH.setText("");
                txtTenKH.setEditable(true);
                txtTenKH.requestFocus();
                Notifications.getInstance().show(Notifications.Type.INFO, "Khách mới - Vui lòng nhập tên.");
            }
        }

        private void themVaoGio() {
            int row = tableThuoc.getSelectedRow();
            if (row == -1) {
                return;
            }

            ThuocTimKiem t = filterList.get(row);
            List<DonViQuyDoi> listDVT = dvqdDAO.getAllDonViByMaThuoc(t.getMaThuoc());
            if (listDVT.isEmpty()) {
                listDVT.add(new DonViQuyDoi(0, t.getMaThuoc(), t.getDonViTinh(), 1, t.getGiaBan(), true));
            }
            DonViQuyDoi dvtChuan = listDVT.get(0);
            for (DonViQuyDoi dv : listDVT) {
                if (dv.getGiaTriQuyDoi() == 1) {
                    dvtChuan = dv;
                    break;
                }
            }

            // Lấy danh sách lô của thuốc, sắp xếp theo HSD gần nhất
            List<LoThuoc> listLo = loThuocDAO.getLoByMaThuoc(t.getMaThuoc());
            if (listLo.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, "Thuốc này không còn lô nào khả dụng!");
                return;
            }

            // Mặc định chọn lô có HSD gần nhất
            LoThuoc loMacDinh = listLo.get(0);
            String loDisplay = loMacDinh.getMaLo() + " (" + loMacDinh.getHanSuDung().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";

            String input = JOptionPane.showInputDialog(FormDatThuoc.this, "Nhập số lượng đặt (Tồn lô gần nhất: " + loMacDinh.getSoLuongTon() + "):", "1");
            if (input == null) {
                return;
            }

            try {
                int sl = Integer.parseInt(input);
                if (sl <= 0 || sl > t.getSoLuongTon()) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, "Số lượng không hợp lệ hoặc quá tồn kho!");
                    return;
                }

                // Check if item already exists in cart (cùng mã + cùng lô + cùng đơn vị)
                for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                    String maTrongGio = modelGioHang.getValueAt(i, 0).toString();
                    String loTrongGio = modelGioHang.getValueAt(i, 2).toString();
                    String dvtTrongGio = modelGioHang.getValueAt(i, 4).toString();

                    if (maTrongGio.equals(t.getMaThuoc()) && loTrongGio.equals(loDisplay) && dvtTrongGio.equals(dvtChuan.getTenDonVi())) {
                        int slCu = Integer.parseInt(modelGioHang.getValueAt(i, 3).toString());
                        modelGioHang.setValueAt(slCu + sl, i, 3);
                        tinhThanhTienRow(i);
                        return;
                    }
                }

                // Add new item if not exists
                modelGioHang.addRow(new Object[]{
                    t.getMaThuoc(),
                    t.getTenThuoc(),
                    loDisplay,                     // Lô (HSD)
                    sl,                            // SL Đặt
                    dvtChuan.getTenDonVi(),        // Đơn vị tính
                    formatMoney(dvtChuan.getGiaBan()), // Đơn giá
                    formatMoney(dvtChuan.getGiaBan() * sl), // Thành tiền
                    listDVT,                       // Hidden_List
                    listLo                         // Hidden_LoList
                });
                tinhTongTien();

            } catch (Exception e) {
                Notifications.getInstance().show(Notifications.Type.WARNING, "Vui lòng nhập số!");
            }
        }

        private void updateGiaKhiDoiDonVi(int row) {
            isUpdating = true;
            try {
                Object valDVT = modelGioHang.getValueAt(row, 4); // Cột đơn vị tính
                if (valDVT == null) {
                    return;
                }
                String tenMoi = valDVT.toString();

                Object valList = modelGioHang.getValueAt(row, 7); // Hidden_List

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
                            String maThuoc = modelGioHang.getValueAt(row, 0).toString();
                            int soLuongDat = Integer.parseInt(modelGioHang.getValueAt(row, 3).toString()); // SL Đặt

                            // Calculate converted quantity using long to prevent overflow
                            long soLuongQuyDoiLong = (long) soLuongDat * (long) dv.getGiaTriQuyDoi();
                            if (soLuongQuyDoiLong > Integer.MAX_VALUE) {
                                Notifications.getInstance().show(
                                    Notifications.Type.ERROR,
                                    Notifications.Location.TOP_CENTER,
                                    "Số lượng quy đổi vượt quá giới hạn cho phép"
                                );
                                return;
                            }
                            int soLuongQuyDoi = (int) soLuongQuyDoiLong;

                            // Get total stock for this drug
                            int tonKho = loThuocDAO.getTongTonByMaThuoc(maThuoc);

                            if (soLuongQuyDoi > tonKho) {
                                // Calculate maximum quantity for this unit
                                int soLuongToiDa = tonKho / dv.getGiaTriQuyDoi();

                                if (soLuongToiDa > 0) {
                                    modelGioHang.setValueAt(soLuongToiDa, row, 3); // SL Đặt
                                    modelGioHang.setValueAt(formatMoney(dv.getGiaBan()), row, 5); // Đơn giá
                                    // Tính thành tiền cho dòng này
                                    double thanhTien = soLuongToiDa * dv.getGiaBan();
                                    modelGioHang.setValueAt(formatMoney(thanhTien), row, 6); // Thành tiền
                                    Notifications.getInstance().show(
                                        Notifications.Type.WARNING,
                                        Notifications.Location.TOP_CENTER,
                                        "Số lượng quy đổi (" + soLuongQuyDoi + ") vượt tồn kho (" + tonKho + "). Đã tự động điều chỉnh về " + soLuongToiDa + " " + tenMoi
                                    );
                                } else {
                                    // Cannot change to this unit - not enough stock
                                    Notifications.getInstance().show(
                                        Notifications.Type.ERROR,
                                        Notifications.Location.TOP_CENTER,
                                        "Không đủ tồn kho để đặt với đơn vị " + tenMoi + ". Tồn kho hiện tại: " + tonKho
                                    );
                                    // Don't update the unit - user needs to reduce quantity first
                                    return;
                                }
                            } else {
                                // Stock is sufficient, proceed with update
                                modelGioHang.setValueAt(formatMoney(dv.getGiaBan()), row, 5); // Đơn giá
                                // Tính thành tiền cho dòng này
                                double thanhTien = soLuongDat * dv.getGiaBan();
                                modelGioHang.setValueAt(formatMoney(thanhTien), row, 6); // Thành tiền
                            }

                            // Tính lại tổng tiền toàn bộ giỏ hàng
                            capNhatTongTien();
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

        // Phương thức tính tổng tiền - không kiểm tra isUpdating
        private void capNhatTongTien() {
            double total = 0;
            for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                try {
                    double tt = parseMoney(modelGioHang.getValueAt(i, 6)); // Thành tiền
                    total += tt;
                } catch (Exception e) {
                }
            }
            this.tongTien = total;
            lbTongTien.setText("Tổng cộng: " + formatMoney(total));
        }

        private void validateSoLuongTonKho(int row) {
            isUpdating = true;
            try {
                String maThuoc = modelGioHang.getValueAt(row, 0).toString();
                int soLuongDat = Integer.parseInt(modelGioHang.getValueAt(row, 3).toString()); // SL Đặt
                String donViTinh = modelGioHang.getValueAt(row, 4).toString(); // Đơn vị tính
                double donGia = parseMoney(modelGioHang.getValueAt(row, 5)); // Đơn giá từ cột hiển thị

                // Lấy giá trị quy đổi của đơn vị hiện tại
                List<DonViQuyDoi> listDVT = (List<DonViQuyDoi>) modelGioHang.getValueAt(row, 7); // Hidden_List
                int giaTriQuyDoi = 1;
                for (DonViQuyDoi dv : listDVT) {
                    if (dv.getTenDonVi().equals(donViTinh)) {
                        giaTriQuyDoi = dv.getGiaTriQuyDoi();
                        break;
                    }
                }

                // Tính số lượng thực tế và so sánh với tồn kho
                int soLuongThucTe = soLuongDat * giaTriQuyDoi;
                int tonKho = loThuocDAO.getTongTonByMaThuoc(maThuoc);

                if (soLuongThucTe > tonKho) {
                    // Tự động điều chỉnh về số lượng tối đa
                    int soLuongToiDa = tonKho / giaTriQuyDoi;
                    if (soLuongToiDa > 0) {
                        modelGioHang.setValueAt(soLuongToiDa, row, 3); // SL Đặt
                        // Cập nhật thành tiền
                        double thanhTien = soLuongToiDa * donGia;
                        modelGioHang.setValueAt(formatMoney(thanhTien), row, 6); // Thành tiền
                        Notifications.getInstance().show(
                            Notifications.Type.WARNING,
                            Notifications.Location.TOP_CENTER,
                            "Số lượng (" + soLuongThucTe + ") vượt quá tồn kho (" + tonKho + ")! Đã điều chỉnh về " + soLuongToiDa
                        );
                    } else {
                        // No stock available, remove the row
                        modelGioHang.removeRow(row);
                        Notifications.getInstance().show(
                            Notifications.Type.ERROR,
                            Notifications.Location.TOP_CENTER,
                            "Không đủ tồn kho! Đã xóa thuốc khỏi giỏ hàng."
                        );
                    }
                } else {
                    // Cập nhật thành tiền khi số lượng thay đổi hợp lệ
                    double thanhTien = soLuongDat * donGia;
                    modelGioHang.setValueAt(formatMoney(thanhTien), row, 6); // Thành tiền
                }
                // Cập nhật tổng tiền
                capNhatTongTien();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                isUpdating = false;
            }
        }

        private void tinhThanhTienRow(int row) {
            if (row < 0) {
                return;
            }
            try {
                int sl = Integer.parseInt(modelGioHang.getValueAt(row, 3).toString()); // SL Đặt
                double donGia = parseMoney(modelGioHang.getValueAt(row, 5)); // Đơn giá

                double thanhTien = sl * donGia;
                modelGioHang.setValueAt(formatMoney(thanhTien), row, 6); // Thành tiền

                tinhTongTien();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void tinhTongTien() {
            if (isUpdating) {
                return;
            }
            isUpdating = true;
            try {
                double total = 0;
                for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                    try {
                        int sl = Integer.parseInt(modelGioHang.getValueAt(i, 3).toString()); // SL Đặt
                        double gia = parseMoney(modelGioHang.getValueAt(i, 5)); // Đơn giá
                        double tt = sl * gia;
                        modelGioHang.setValueAt(formatMoney(tt), i, 6); // Thành tiền
                        total += tt;
                    } catch (Exception e) {
                    }
                }
                this.tongTien = total;
                lbTongTien.setText("Tổng cộng: " + formatMoney(total));

            } finally {
                isUpdating = false;
            }
        }

        private boolean validateTatCaSoLuong() {
            for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                String maThuoc = modelGioHang.getValueAt(i, 0).toString();
                int soLuongDat = Integer.parseInt(modelGioHang.getValueAt(i, 3).toString()); // SL Đặt
                String donViTinh = modelGioHang.getValueAt(i, 4).toString(); // Đơn vị tính

                List<DonViQuyDoi> listDVT = (List<DonViQuyDoi>) modelGioHang.getValueAt(i, 7); // Hidden_List
                int giaTriQuyDoi = 1;
                for (DonViQuyDoi dv : listDVT) {
                    if (dv.getTenDonVi().equals(donViTinh)) {
                        giaTriQuyDoi = dv.getGiaTriQuyDoi();
                        break;
                    }
                }

                int soLuongThucTe = soLuongDat * giaTriQuyDoi;
                int tonKho = loThuocDAO.getTongTonByMaThuoc(maThuoc);

                if (soLuongThucTe > tonKho) {
                    Notifications.getInstance().show(
                        Notifications.Type.ERROR,
                        Notifications.Location.TOP_CENTER,
                        "Thuốc '" + modelGioHang.getValueAt(i, 1) + "' vượt quá tồn kho!"
                    );
                    return false;
                }
            }
            return true;
        }

        private void actionSave() {
            if (txtTenKH.getText().trim().isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, "Vui lòng nhập tên khách hàng!");
                return;
            }
            if (modelGioHang.getRowCount() == 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, "Chưa chọn thuốc nào!");
                return;
            }

            // Validate all quantities before saving
            if (!validateTatCaSoLuong()) {
                return;
            }

            try {
                String tenKH = khachHangSelected != null ? khachHangSelected.getTenKH() : txtTenKH.getText().trim();
                String sdt = txtSDT.getText().trim();
                LocalDate date = datePicker.getSelectedDate();
                Date time = (Date) timeSpinner.getValue();
                LocalTime localTime = time.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
                LocalDateTime henLay = LocalDateTime.of(date != null ? date : LocalDate.now(), localTime);

                // Validate giờ hẹn lấy phải sau thời điểm hiện tại ít nhất 30 phút
                LocalDateTime thoiGianToiThieu = LocalDateTime.now().plusMinutes(30);
                if (henLay.isBefore(thoiGianToiThieu)) {
                    Notifications.getInstance().show(Notifications.Type.WARNING,
                        Notifications.Location.TOP_CENTER,
                        "Giờ hẹn lấy phải sau thời điểm hiện tại ít nhất 30 phút!");
                    return;
                }

                List<ChiTietDonDat> listChiTiet = new ArrayList<>();
                for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                    String maThuoc = modelGioHang.getValueAt(i, 0).toString();
                    // Cột 2 là Lô (HSD), bỏ qua cho ChiTietDonDat
                    int sl = Integer.parseInt(modelGioHang.getValueAt(i, 3).toString()); // SL Đặt
                    String donViTinh = modelGioHang.getValueAt(i, 4).toString(); // Đơn vị tính
                    double donGia = parseMoney(modelGioHang.getValueAt(i, 5)); // Đơn giá
                    double thanhTien = parseMoney(modelGioHang.getValueAt(i, 6)); // Thành tiền

                    Thuoc t = new Thuoc(maThuoc);

                    ChiTietDonDat ct = new ChiTietDonDat(null, t, sl, donGia, thanhTien, donViTinh);
                    listChiTiet.add(ct);
                }

                if (donHienTai == null) {
                    String maDon = donDatHangDAO.getNewMaDonDat();
                    NhanVien nv = Auth.isLogin() ? Auth.user : new NhanVien("NV001");
                    DonDatHang don = new DonDatHang(maDon, tenKH, sdt, henLay, tongTien, "Đặt qua điện thoại", "Đang giữ hàng", nv, khachHangSelected);

                    for (ChiTietDonDat ct : listChiTiet) {
                        ct.setDonDat(don);
                    }

                    if (donDatHangDAO.taoDonDatHang(don, listChiTiet)) {
                        Notifications.getInstance().show(Notifications.Type.SUCCESS, "Tạo phiếu thành công!");
                        showDanhSach();
                    }
                } else {
                    donHienTai.setTenKhach(tenKH);
                    donHienTai.setSdtLienHe(sdt);
                    donHienTai.setGioHenLay(henLay);
                    donHienTai.setTongTien(tongTien);
                    donHienTai.setKhachHang(khachHangSelected);

                    for (ChiTietDonDat ct : listChiTiet) {
                        ct.setDonDat(donHienTai);
                    }

                    if (donDatHangDAO.updateDonDatHang(donHienTai, listChiTiet)) {
                        Notifications.getInstance().show(Notifications.Type.SUCCESS, "Cập nhật phiếu thành công!");
                        showDanhSach();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Notifications.getInstance().show(Notifications.Type.ERROR, "Lỗi dữ liệu!");
            }
        }

        private String formatMoney(double tien) {
            return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(tien);
        }

        private double parseMoney(Object value) {
            try {
                if (value == null) {
                    return 0;
                }

                String raw = value.toString().replaceAll("[^0-9]", "");
                return Double.parseDouble(raw);
            } catch (Exception e) {
                return 0;
            }
        }

        private class RightAlignRenderer extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(JLabel.RIGHT);
                return this;
            }
        }
    }

    // Inner class for lot cell editor
    private class LoCellEditor extends javax.swing.DefaultCellEditor {
        private javax.swing.JComboBox<String> comboBox;

        public LoCellEditor() {
            super(new javax.swing.JComboBox<>());
            this.comboBox = (javax.swing.JComboBox<String>) getComponent();
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int column) {
            comboBox.removeAllItems();
            int modelRow = table.convertRowIndexToModel(row);
            Object valList = table.getModel().getValueAt(modelRow, 8); // Hidden_LoList

            if (valList instanceof java.util.List) {
                java.util.List<LoThuoc> list = (java.util.List<LoThuoc>) valList;
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (LoThuoc lo : list) {
                    comboBox.addItem(lo.getMaLo() + " (" + lo.getHanSuDung().format(fmt) + ")");
                }
            }

            if (value != null) {
                comboBox.setSelectedItem(value.toString());
            }
            return comboBox;
        }
    }

    // Inner class for unit cell editor (shared by both panels if needed)
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
            Object valList = table.getModel().getValueAt(modelRow, 7); // Hidden_List

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
