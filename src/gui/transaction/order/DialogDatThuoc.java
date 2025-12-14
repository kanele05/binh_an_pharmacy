package gui.transaction.order;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.formdev.flatlaf.FlatClientProperties;
import dao.ChiTietBangGiaDAO;
import dao.ChiTietDonDatDAO;
import dao.DonDatHangDAO;
import dao.DonViQuyDoiDAO;
import dao.KhachHangDAO;
import dao.LoThuocDAO;
import dao.ThuocDAO;
import dto.ThuocTimKiem;
import entities.ChiTietDonDat;
import entities.DonDatHang;
import entities.DonViQuyDoi;
import entities.KhachHang;
import entities.NhanVien;
import entities.Thuoc;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import net.miginfocom.swing.MigLayout;
import raven.datetime.component.date.DatePicker;
import raven.toast.Notifications;
import utils.Auth;

/**
 * @deprecated This dialog has been deprecated in favor of embedded panel approach.
 * The functionality has been moved to {@link FormDatThuoc} as inner panels
 * (PanelDatThuocGiaoDien and PanelDanhSachDonDat) using CardLayout pattern.
 * This class is kept for backward compatibility only.
 * 
 * Use FormDatThuoc.showTaoPhieu() or FormDatThuoc.showSuaPhieu() instead.
 */
@Deprecated
public class DialogDatThuoc extends JDialog {

    private final Component parent;
    private boolean isSave = false;

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

    public DialogDatThuoc(Component parent) {
        super(SwingUtilities.windowForComponent(parent), "Tạo Phiếu Đặt Hàng", ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        initComponents();
        loadDataThuoc();
    }

    public DialogDatThuoc(Component parent, DonDatHang donEdit) {
        super(SwingUtilities.windowForComponent(parent), "Cập Nhật Đơn Đặt: " + donEdit.getMaDonDat(), ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.donHienTai = donEdit;
        initComponents();
        loadDataThuoc();

        loadDataToForm();
    }

    private void initComponents() {

        setLayout(new MigLayout("wrap,fill,insets 15, width 1100, height 700", "[45%,fill][55%,fill]", "[][grow][]"));

        JLabel lbTitle = new JLabel("TẠO PHIẾU ĐẶT THUỐC");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +6; foreground:$Accent.color");
        add(lbTitle, "span 2, center, gapbottom 10");

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

        pack();
        setLocationRelativeTo(parent);
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
                return 5;
            }

            @Override
            public String getColumnName(int i) {
                // --- THÊM CỘT ĐVT ---
                return new String[]{"Mã", "Tên Thuốc", "ĐVT", "Tồn Tổng", "Giá Bán"}[i];
            }

            @Override
            public Object getColumnValue(ThuocTimKiem t, int i) {
                switch (i) {
                    case 0:
                        return t.getMaThuoc();
                    case 1:
                        return t.getTenThuoc();
                    case 2:
                        return t.getDonViTinh();
                    case 3:
                        return t.getSoLuongTon();
                    case 4:
                        return formatMoney(t.getGiaBan());
                    default:
                        return null;
                }
            }
        };

        EventTableModel<ThuocTimKiem> etm = new EventTableModel<>(filterList, tf);
        tableThuoc = new JTable(etm);
        tableThuoc.putClientProperty(FlatClientProperties.STYLE, "rowHeight:25; showHorizontalLines:true");
        tableThuoc.getColumnModel().getColumn(4).setCellRenderer(new RightAlignRenderer());

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

        String[] cols = {"Mã Thuốc", "Tên thuốc", "SL Đặt", "Đơn vị tính", "Đơn giá", "Thành tiền", "Hidden_List"};
        modelGioHang = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 2 || c == 3;
            }
        };

        modelGioHang.addTableModelListener(e -> {
            if (!isUpdating) {
                tinhTongTien();
            }
        });

        tableGioHang = new JTable(modelGioHang);
        tableGioHang.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        tableGioHang.getColumnModel().getColumn(4).setCellRenderer(new RightAlignRenderer());
        tableGioHang.getColumnModel().getColumn(5).setCellRenderer(new RightAlignRenderer());
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

            tableGioHang.getColumnModel().getColumn(3).setCellEditor(unitEditor);

            modelGioHang.addTableModelListener(e -> {
                if (e.getColumn() == 2) {
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
        btnHuy.addActionListener(e -> dispose());

        panel.add(lbTongTien);
        panel.add(btnHuy);
        panel.add(btnLuu);
        return panel;
    }

    private void loadDataThuoc() {
        sourceList.clear();

        sourceList.addAll(loThuocDAO.getDanhSachThuocBanHang());
    }

    private void loadDataToForm() {
        if (donHienTai == null) {
            return;
        }

        txtSDT.setText(donHienTai.getSdtLienHe());
        if (donHienTai.getKhachHang() != null) {
            khachHangSelected = donHienTai.getKhachHang();
            txtTenKH.setText(donHienTai.getKhachHang().getTenKH());
            txtTenKH.setEditable(false);
        } else {
            txtTenKH.setText(donHienTai.getTenKhach());
            txtTenKH.setEditable(true);
        }

        

        List<ChiTietDonDat> listCT = ctDAO.getChiTietByMaDon(donHienTai.getMaDonDat());
        modelGioHang.setRowCount(0);

        for (ChiTietDonDat ct : listCT) {
            List<DonViQuyDoi> listDVT = dvqdDAO.getAllDonViByMaThuoc(ct.getThuoc().getMaThuoc());
            listDVT.removeIf(dv -> dv.getGiaBan() <= 0);
            if (listDVT.isEmpty()) { 
                 listDVT.add(new DonViQuyDoi(0, ct.getThuoc().getMaThuoc(), ct.getDonViTinh(), 1, ct.getDonGia(), true));
            }
            modelGioHang.addRow(new Object[]{
                ct.getThuoc().getMaThuoc(),
                ct.getThuoc().getTenThuoc(),
                ct.getSoLuong(),
                ct.getDonViTinh(),
                formatMoney(ct.getDonGia()),
                formatMoney(ct.getThanhTien()),
                listDVT
            });
        }
        tinhTongTien();
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

        String input = JOptionPane.showInputDialog(this, "Nhập số lượng đặt (Tổng tồn: " + t.getSoLuongTon() + "):", "1");
        if (input == null) {
            return;
        }

        try {
            int sl = Integer.parseInt(input);
            if (sl <= 0 || sl > t.getSoLuongTon()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, "Số lượng không hợp lệ hoặc quá tồn kho!");
                return;
            }

            boolean exist = false;
            for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                String maTrongGio = modelGioHang.getValueAt(i, 0).toString();
                String dvtTrongGio = modelGioHang.getValueAt(i, 3).toString();

                if (maTrongGio.equals(t.getMaThuoc()) && dvtTrongGio.equals(dvtChuan.getTenDonVi())) {
                    int slCu = Integer.parseInt(modelGioHang.getValueAt(i, 2).toString());
                    modelGioHang.setValueAt(slCu + 1, i, 2);
                    tinhThanhTienRow(i);
                    return;
                }
            }

            if (!exist) {
                modelGioHang.addRow(new Object[]{
                    t.getMaThuoc(),
                    t.getTenThuoc(),
                    sl,
                    dvtChuan.getTenDonVi(),
                    formatMoney(dvtChuan.getGiaBan()),
                    formatMoney(dvtChuan.getGiaBan()),
                    listDVT // Lưu list này để load vào combobox
                });
            }
            tinhTongTien();

        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, "Vui lòng nhập số!");
        }
    }


    private void updateGiaKhiDoiDonVi(int row) {
        isUpdating = true;
        try {

            Object valDVT = modelGioHang.getValueAt(row, 3);
            if (valDVT == null) {
                return;
            }
            String tenMoi = valDVT.toString();

            Object valList = modelGioHang.getValueAt(row, 6);

            if (valList instanceof List) {
                List<entities.DonViQuyDoi> list = (List<entities.DonViQuyDoi>) valList;

                for (entities.DonViQuyDoi dv : list) {
                    if (dv.getTenDonVi().equals(tenMoi)) {
                        
                        // Validate stock when changing unit
                        String maThuoc = modelGioHang.getValueAt(row, 0).toString();
                        int soLuongDat = Integer.parseInt(modelGioHang.getValueAt(row, 2).toString());
                        int soLuongQuyDoi = soLuongDat * dv.getGiaTriQuyDoi();
                        
                        // Get total stock for this drug
                        int tonKho = loThuocDAO.getTongTonByMaThuoc(maThuoc);
                        
                        if (soLuongQuyDoi > tonKho) {
                            // Check for division by zero
                            if (dv.getGiaTriQuyDoi() <= 0) {
                                Notifications.getInstance().show(
                                    Notifications.Type.ERROR,
                                    Notifications.Location.TOP_CENTER,
                                    "Đơn vị quy đổi không hợp lệ"
                                );
                                return;
                            }
                            
                            // Calculate maximum quantity for this unit
                            int soLuongToiDa = tonKho / dv.getGiaTriQuyDoi();
                            
                            if (soLuongToiDa > 0) {
                                modelGioHang.setValueAt(soLuongToiDa, row, 2);
                                modelGioHang.setValueAt(formatCurrency(dv.getGiaBan()), row, 4);
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

    private String formatCurrency(double amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + " ₫";
    }

    private void tinhThanhTienRow(int row) {
        if (row < 0) {
            return;
        }
        try {
            int sl = Integer.parseInt(modelGioHang.getValueAt(row, 2).toString());
            double donGia = parseMoney(modelGioHang.getValueAt(row, 4));

            double thanhTien = sl * donGia;
            modelGioHang.setValueAt(formatMoney(thanhTien), row, 5);

            tinhTongTien(); // Cập nhật tổng hóa đơn
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
                    int sl = Integer.parseInt(modelGioHang.getValueAt(i, 2).toString());

                    double gia = parseMoney(modelGioHang.getValueAt(i, 4));

                    double tt = sl * gia;

                    modelGioHang.setValueAt(formatMoney(tt), i, 5);

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

    private void actionSave() {
        if (txtTenKH.getText().trim().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, "Vui lòng nhập tên khách hàng!");
            return;
        }
        if (modelGioHang.getRowCount() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, "Chưa chọn thuốc nào!");
            return;
        }

        try {

            String tenKH = khachHangSelected != null ? khachHangSelected.getTenKH() : txtTenKH.getText().trim();
            String sdt = txtSDT.getText().trim();
            LocalDate date = datePicker.getSelectedDate();
            Date time = (Date) timeSpinner.getValue();
            LocalTime localTime = time.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            LocalDateTime henLay = LocalDateTime.of(date != null ? date : LocalDate.now(), localTime);

            List<ChiTietDonDat> listChiTiet = new ArrayList<>();
            for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                String maThuoc = modelGioHang.getValueAt(i, 0).toString();
                int sl = Integer.parseInt(modelGioHang.getValueAt(i, 2).toString());
                String donViTinh = modelGioHang.getValueAt(i, 3).toString();
                double donGia = parseMoney(modelGioHang.getValueAt(i, 4));
                double thanhTien = parseMoney(modelGioHang.getValueAt(i, 5));

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
                    this.isSave = true;
                    dispose();
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
                    this.isSave = true;
                    dispose();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR, "Lỗi dữ liệu!");
        }
    }

    public boolean isSave() {
        return isSave;
    }

    public KhachHang getKhachHang() {
        return khachHangSelected;
    }

    public String getTenKhach() {
        return tenKhachVangLai;
    }

    public String getSDT() {
        return txtSDT.getText().trim();
    }

    public LocalDateTime getThoiGianHen() {
        return thoiGianHen;
    }

    public double getTongTien() {
        return tongTien;
    }

    public DefaultTableModel getModelGioHang() {
        return modelGioHang;
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
