package gui.transaction.sales;

import gui.manage.partner.DialogKhachHang;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.formdev.flatlaf.FlatClientProperties;
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
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;
import utils.Auth;
import utils.InvoicePDFGenerator;
import utils.TableRenderers;

/**
 * Panel giao diện bán hàng (POS).
 * Được tách từ FormBanHang để giảm độ phức tạp.
 */
public class PanelBanHangGiaoDien extends JPanel {

    // UI Components
    private JTextField txtTimKiem, txtMaKH, txtTenKH, txtSDT;
    private JComboBox<String> cbHinhThucTT;
    private JTable tableThuoc, tableGioHang;
    private JLabel lbTongTien, lbThanhTien, lbTienThue, lbGiamGia;
    private JLabel lbDiemHienCo, lbMaHD;
    private JCheckBox chkDungDiem;
    private JTextField txtThueVAT;
    private JButton btnThemVaoGio, btnXoaKhoiGio, btnThanhToan, btnHuyHD;

    // Table models
    private EventList<ThuocTimKiem> sourceList;
    private FilterList<ThuocTimKiem> filterList;
    private EventTableModel<ThuocTimKiem> tableModelThuoc;
    private DefaultTableModel modelGioHang;

    // DAOs
    private LoThuocDAO loThuocDAO;
    private KhachHangDAO khachHangDAO;
    private HoaDonDAO hoaDonDAO;
    private DonViQuyDoiDAO dvqdDAO;

    // State
    private double tongTien = 0;
    private double thueVAT = 0;
    private double tienGiamGia = 0;
    private final double DEFAULT_VAT_RATE = 5.0;
    private KhachHang currentKhachHang = null;
    private boolean isUpdating = false;

    // Parent reference
    private FormBanHang parentForm;

    public PanelBanHangGiaoDien(FormBanHang parentForm) {
        this.parentForm = parentForm;
        initDAOs();
        initComponents();
        loadKhoThuoc();
    }

    private void initDAOs() {
        loThuocDAO = new LoThuocDAO();
        khachHangDAO = new KhachHangDAO();
        hoaDonDAO = new HoaDonDAO();
        dvqdDAO = new DonViQuyDoiDAO();
    }

    private void initComponents() {
        setLayout(new MigLayout("wrap,fillx,insets 20", "[70%][30%]", "[][][grow][]"));

        add(createHeader(), "span 2, growx, wrap 15");
        add(createSearchPanel(), "grow");
        add(createCustomerPanel(), "grow,wrap");
        add(createMedicineTablePanel(), "grow");
        add(createCartPanel(), "grow,wrap");
        add(createPaymentPanel(), "span 2,grow");
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new MigLayout("insets 0", "[]10[]push[]"));
        header.setOpaque(false);

        JButton btnBack = new JButton(" Quay lại");
        btnBack.addActionListener(e -> parentForm.showDanhSach());

        JLabel lbTitle = new JLabel("Bán hàng / Tạo hóa đơn");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");

        header.add(btnBack);
        header.add(lbTitle);
        return header;
    }

    public void resetForm() {
        modelGioHang.setRowCount(0);
        setKhachVangLai();
        txtTimKiem.setText("");
        tinhTongTien();
        loadKhoThuoc();
        generateNewMaHD();
    }

    public void focusTimKiem() {
        txtTimKiem.requestFocusInWindow();
        txtTimKiem.selectAll();
    }

    public void themVaoGioHangAction() {
        themVaoGioHang();
    }

    public void thanhToanAction() {
        thanhToan();
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
        filterList = new FilterList<>(sourceList,
            new TextComponentMatcherEditor<>(txtTimKiem, (list, t) -> {
                list.add(t.getMaThuoc());
                list.add(t.getTenThuoc());
                list.add(t.getMaLo());
            }));

        TableFormat<ThuocTimKiem> tableFormat = new TableFormat<ThuocTimKiem>() {
            private final String[] cols = {"Mã Thuốc", "Tên Thuốc", "Lô", "HSD", "Tồn", "Giá Bán"};

            @Override
            public int getColumnCount() {
                return cols.length;
            }

            @Override
            public String getColumnName(int i) {
                return cols[i];
            }

            @Override
            public Object getColumnValue(ThuocTimKiem t, int i) {
                switch (i) {
                    case 0: return t.getMaThuoc();
                    case 1: return t.getTenThuoc();
                    case 2: return t.getMaLo();
                    case 3: return t.getHanSuDung();
                    case 4: return t.getSoLuongTon();
                    case 5: return TableRenderers.formatCurrency(t.getGiaBan());
                    default: return null;
                }
            }
        };

        tableModelThuoc = new EventTableModel<>(filterList, tableFormat);
        tableThuoc = new JTable(tableModelThuoc);

        tableThuoc.putClientProperty(FlatClientProperties.STYLE, "showHorizontalLines:true; rowHeight:30");
        tableThuoc.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");
        tableThuoc.getColumnModel().getColumn(3).setCellRenderer(new TableRenderers.DateRenderer());

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

        // Hide the list column
        tableGioHang.removeColumn(tableGioHang.getColumnModel().getColumn(6));

        // Setup unit cell editor
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
            public void editingCanceled(ChangeEvent e) {}
        });
        tableGioHang.getColumnModel().getColumn(2).setCellEditor(unitEditor);

        // Listen for quantity changes
        modelGioHang.addTableModelListener(e -> {
            if (e.getColumn() == 3 && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                if (row >= 0 && !isUpdating) {
                    validateSoLuongTonKho(row);
                }
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
        btnThanhToan.putClientProperty(FlatClientProperties.STYLE,
            "background:#4CAF50; foreground:#FFFFFF; font:bold +4; arc:15");
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

    // Business logic methods
    @SuppressWarnings("unchecked")
    private void updateGiaKhiDoiDonVi(int row) {
        isUpdating = true;
        try {
            Object valDVT = modelGioHang.getValueAt(row, 2);
            if (valDVT == null) return;
            String tenMoi = valDVT.toString();

            Object valList = modelGioHang.getValueAt(row, 6);
            if (!(valList instanceof List)) return;

            List<DonViQuyDoi> list = (List<DonViQuyDoi>) valList;
            for (DonViQuyDoi dv : list) {
                if (dv.getTenDonVi().equals(tenMoi)) {
                    if (dv.getGiaTriQuyDoi() <= 0) {
                        Notifications.getInstance().show(Notifications.Type.ERROR,
                            Notifications.Location.TOP_CENTER, "Đơn vị quy đổi không hợp lệ");
                        return;
                    }

                    String maLo = modelGioHang.getValueAt(row, 0).toString();
                    int soLuongBan = Integer.parseInt(modelGioHang.getValueAt(row, 3).toString());

                    long soLuongQuyDoiLong = (long) soLuongBan * (long) dv.getGiaTriQuyDoi();
                    if (soLuongQuyDoiLong > Integer.MAX_VALUE) {
                        Notifications.getInstance().show(Notifications.Type.ERROR,
                            Notifications.Location.TOP_CENTER, "Số lượng quy đổi vượt quá giới hạn cho phép");
                        return;
                    }
                    int soLuongQuyDoi = (int) soLuongQuyDoiLong;

                    int tonKho = loThuocDAO.getTonKhoByMaLo(maLo);

                    if (soLuongQuyDoi > tonKho) {
                        int soLuongToiDa = tonKho / dv.getGiaTriQuyDoi();
                        if (soLuongToiDa > 0) {
                            modelGioHang.setValueAt(soLuongToiDa, row, 3);
                            modelGioHang.setValueAt(TableRenderers.formatCurrency(dv.getGiaBan()), row, 4);
                            Notifications.getInstance().show(Notifications.Type.WARNING,
                                Notifications.Location.TOP_CENTER,
                                "Số lượng quy đổi (" + soLuongQuyDoi + ") vượt tồn lô (" + tonKho + "). Đã tự động điều chỉnh về " + soLuongToiDa + " " + tenMoi);
                        } else {
                            Notifications.getInstance().show(Notifications.Type.ERROR,
                                Notifications.Location.TOP_CENTER,
                                "Không đủ tồn lô để bán với đơn vị " + tenMoi + ". Tồn lô hiện tại: " + tonKho);
                            return;
                        }
                    } else {
                        modelGioHang.setValueAt(TableRenderers.formatCurrency(dv.getGiaBan()), row, 4);
                    }
                    tinhTongTien();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isUpdating = false;
        }
    }

    @SuppressWarnings("unchecked")
    private void validateSoLuongTonKho(int row) {
        isUpdating = true;
        try {
            String maLo = modelGioHang.getValueAt(row, 0).toString();
            int soLuongBan = Integer.parseInt(modelGioHang.getValueAt(row, 3).toString());
            String donViTinh = modelGioHang.getValueAt(row, 2).toString();

            List<DonViQuyDoi> listDVT = (List<DonViQuyDoi>) modelGioHang.getValueAt(row, 6);
            int giaTriQuyDoi = 1;
            for (DonViQuyDoi dv : listDVT) {
                if (dv.getTenDonVi().equals(donViTinh)) {
                    giaTriQuyDoi = dv.getGiaTriQuyDoi();
                    break;
                }
            }

            int soLuongThucTe = soLuongBan * giaTriQuyDoi;
            int tonKho = loThuocDAO.getTonKhoByMaLo(maLo);

            if (soLuongThucTe > tonKho) {
                int soLuongToiDa = tonKho / giaTriQuyDoi;
                if (soLuongToiDa > 0) {
                    modelGioHang.setValueAt(soLuongToiDa, row, 3);
                    Notifications.getInstance().show(Notifications.Type.WARNING,
                        Notifications.Location.TOP_CENTER,
                        "Số lượng (" + soLuongThucTe + ") vượt quá tồn lô (" + tonKho + ")! Đã điều chỉnh về " + soLuongToiDa);
                } else {
                    modelGioHang.removeRow(row);
                    Notifications.getInstance().show(Notifications.Type.ERROR,
                        Notifications.Location.TOP_CENTER, "Không đủ tồn lô! Đã xóa thuốc khỏi giỏ hàng.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            isUpdating = false;
        }
    }

    private void themVaoGioHang() {
        int row = tableThuoc.getSelectedRow();
        if (row == -1) return;

        ThuocTimKiem t = filterList.get(row);

        List<DonViQuyDoi> listDVT = dvqdDAO.getAllDonViByMaThuoc(t.getMaThuoc());
        listDVT.removeIf(dv -> dv.getGiaBan() <= 0);
        if (listDVT.isEmpty()) {
            listDVT.add(new DonViQuyDoi(0, t.getMaThuoc(), t.getDonViTinh(), 1, t.getGiaBan(), true));
        }

        DonViQuyDoi baseUnit = listDVT.get(0);
        for (DonViQuyDoi dv : listDVT) {
            if (dv.getGiaTriQuyDoi() == 1) {
                baseUnit = dv;
                break;
            }
        }

        String slStr = JOptionPane.showInputDialog(this,
                "Số lượng bán (" + baseUnit.getTenDonVi() + ") - Tồn Lô: " + t.getSoLuongTon(), "1");
        try {
            if (slStr == null) return;
            int sl = Integer.parseInt(slStr);
            if (sl <= 0 || sl > t.getSoLuongTon()) {
                Notifications.getInstance().show(Notifications.Type.WARNING,
                    Notifications.Location.TOP_CENTER, "Số lượng không hợp lệ hoặc quá tồn!");
                return;
            }

            boolean exists = false;
            for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                if (modelGioHang.getValueAt(i, 0).equals(t.getMaLo())
                        && modelGioHang.getValueAt(i, 2).equals(baseUnit.getTenDonVi())) {
                    int slOld = Integer.parseInt(modelGioHang.getValueAt(i, 3).toString());
                    int slNew = slOld + sl;
                    if (slNew > t.getSoLuongTon()) {
                        Notifications.getInstance().show(Notifications.Type.WARNING,
                            Notifications.Location.TOP_CENTER, "Vượt quá tồn kho!");
                        return;
                    }
                    modelGioHang.setValueAt(slNew, i, 3);
                    modelGioHang.setValueAt(TableRenderers.formatCurrency(slNew * baseUnit.getGiaBan()), i, 5);
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
                    TableRenderers.formatCurrency(baseUnit.getGiaBan()),
                    TableRenderers.formatCurrency(sl * baseUnit.getGiaBan()),
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
                    double gia = TableRenderers.parseCurrency(giaTxt);
                    double thanhTien = sl * gia;
                    modelGioHang.setValueAt(TableRenderers.formatCurrency(thanhTien), i, 5);
                    tongTien += thanhTien;
                } catch (Exception e) {
                    // Skip invalid rows
                }
            }

            tienGiamGia = 0;
            if (currentKhachHang != null && chkDungDiem.isSelected()) {
                tienGiamGia = currentKhachHang.getDiemTichLuy() * 100;
                // FIX Lỗi 5: Giảm giá không được vượt quá (tongTien + VAT)
                double maxGiamGia = tongTien + (tongTien * DEFAULT_VAT_RATE / 100);
                if (tienGiamGia > maxGiamGia) {
                    tienGiamGia = maxGiamGia;
                }
            }

            // FIX Lỗi 5: VAT tính trên tổng tiền TRƯỚC giảm giá (theo quy định kế toán VN)
            thueVAT = tongTien * (DEFAULT_VAT_RATE / 100);
            double thanhTienCuoi = tongTien + thueVAT - tienGiamGia;

            lbTongTien.setText(TableRenderers.formatCurrency(tongTien));
            lbGiamGia.setText(TableRenderers.formatCurrency(tienGiamGia));
            lbTienThue.setText(TableRenderers.formatCurrency(thueVAT));
            lbThanhTien.setText(TableRenderers.formatCurrency(thanhTienCuoi));
        } finally {
            isUpdating = false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean validateTatCaSoLuong() {
        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
            String maLo = modelGioHang.getValueAt(i, 0).toString();
            int soLuongBan = Integer.parseInt(modelGioHang.getValueAt(i, 3).toString());
            String donViTinh = modelGioHang.getValueAt(i, 2).toString();

            List<DonViQuyDoi> listDVT = (List<DonViQuyDoi>) modelGioHang.getValueAt(i, 6);
            int giaTriQuyDoi = 1;
            for (DonViQuyDoi dv : listDVT) {
                if (dv.getTenDonVi().equals(donViTinh)) {
                    giaTriQuyDoi = dv.getGiaTriQuyDoi();
                    break;
                }
            }

            int soLuongThucTe = soLuongBan * giaTriQuyDoi;
            int tonKho = loThuocDAO.getTonKhoByMaLo(maLo);

            if (soLuongThucTe > tonKho) {
                Notifications.getInstance().show(Notifications.Type.ERROR,
                    Notifications.Location.TOP_CENTER,
                    "Thuốc '" + modelGioHang.getValueAt(i, 1) + "' vượt quá tồn lô!");
                return false;
            }
        }
        return true;
    }

    private void thanhToan() {
        if (modelGioHang.getRowCount() == 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING,
                Notifications.Location.TOP_CENTER, "Giỏ hàng đang trống!");
            return;
        }

        if (tableGioHang.isEditing()) {
            tableGioHang.getCellEditor().stopCellEditing();
        }

        if (!validateTatCaSoLuong()) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Thanh toán đơn hàng " + lbMaHD.getText() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            HoaDon hd = new HoaDon();
            hd.setMaHD(lbMaHD.getText());
            hd.setNgayTao(java.time.LocalDateTime.now());
            hd.setNhanVien(Auth.isLogin() ? Auth.user : new NhanVien("NV001"));
            hd.setKhachHang(currentKhachHang);
            hd.setTongTien(TableRenderers.parseCurrency(lbTongTien.getText()));
            hd.setThue(TableRenderers.parseCurrency(lbTienThue.getText()));
            hd.setGiamGia(TableRenderers.parseCurrency(lbGiamGia.getText()));
            hd.setHinhThucTT(cbHinhThucTT.getSelectedItem().toString());
            hd.setGhiChu("Bán hàng tại quầy");

            List<ChiTietHoaDon> listCT = new java.util.ArrayList<>();
            for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                String maLo = modelGioHang.getValueAt(i, 0).toString();
                String tenThuoc = modelGioHang.getValueAt(i, 1).toString();
                String donViTinh = modelGioHang.getValueAt(i, 2).toString();
                int soLuong = Integer.parseInt(modelGioHang.getValueAt(i, 3).toString());
                double donGia = TableRenderers.parseCurrency(modelGioHang.getValueAt(i, 4).toString());
                double thanhTien = TableRenderers.parseCurrency(modelGioHang.getValueAt(i, 5).toString());

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
                    if (chkDungDiem.isSelected() && currentKhachHang.getDiemTichLuy() > 0) {
                        int diemSuDung = (int) Math.min(currentKhachHang.getDiemTichLuy(), tongTien / 100);
                        khachHangDAO.truDiem(currentKhachHang.getMaKH(), diemSuDung);
                    }
                    double tienTinhDiem = hd.getTongTien() - hd.getGiamGia();
                    int diemMoi = (int) Math.round(tienTinhDiem / 10000.0);
                    if (diemMoi > 0) {
                        khachHangDAO.congDiem(currentKhachHang.getMaKH(), diemMoi);
                    }
                }
                Notifications.getInstance().show(Notifications.Type.SUCCESS,
                    Notifications.Location.TOP_CENTER, "Thanh toán thành công!");

                int printOption = JOptionPane.showConfirmDialog(this,
                    "Bạn có muốn in hóa đơn PDF không?",
                    "In hóa đơn",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

                if (printOption == JOptionPane.YES_OPTION) {
                    InvoicePDFGenerator.generateAndOpenInvoice(hd);
                }
                resetForm();
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR,
                    Notifications.Location.TOP_CENTER, "Lỗi khi lưu hóa đơn!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(Notifications.Type.ERROR,
                Notifications.Location.TOP_CENTER, "Lỗi dữ liệu!");
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
                // Keep default
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
                chkDungDiem.setText("Dùng " + kh.getDiemTichLuy() + " điểm (-" +
                    TableRenderers.formatCurrency(kh.getDiemTichLuy() * 100) + ")");
            } else {
                chkDungDiem.setEnabled(false);
                chkDungDiem.setText("Không có điểm");
            }
            Notifications.getInstance().show(Notifications.Type.SUCCESS,
                Notifications.Location.TOP_CENTER, "Khách hàng thành viên!");
        } else {
            setKhachVangLai();
            int opt = JOptionPane.showConfirmDialog(this,
                "Khách hàng chưa tồn tại. Tạo mới?", "Thông báo", JOptionPane.YES_NO_OPTION);
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
            try {
                KhachHang newKH = dialog.getKhachHang();
                if (khachHangDAO.insert(newKH)) {
                    txtSDT.setText(newKH.getSdt());
                    timKiemKhachHang();
                    Notifications.getInstance().show(Notifications.Type.SUCCESS,
                        Notifications.Location.TOP_CENTER, "Tạo khách hàng thành công!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
