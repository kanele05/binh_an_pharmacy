package gui.manage.price;

import com.formdev.flatlaf.FlatClientProperties;
import dao.BangGiaDAO;
import dao.ChiTietBangGiaDAO;
import dao.ThuocDAO;
import entities.BangGia;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import net.miginfocom.swing.MigLayout;
import raven.datetime.component.date.DatePicker;
import raven.toast.Notifications;
import dao.DonViQuyDoiDAO;
import dao.ChiTietPhieuNhapDAO;
import entities.DonViQuyDoi;

public class DialogBangGia extends JDialog {

    private final Component parent;
    private boolean isSave = false;
    private boolean isDraftMode = false;

    private JTextField txtMaBG, txtTenBG;
    private JFormattedTextField txtNgayHL, txtNgayKT;
    private DatePicker datePickerHL, datePickerKT;
    private JTextArea txtGhiChu;

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JComboBox<String> cbNhomThuoc;

    private BangGiaDAO bgDAO = new BangGiaDAO();
    private ChiTietBangGiaDAO ctDAO = new ChiTietBangGiaDAO();
    private ThuocDAO thuocDAO = new ThuocDAO();
    private DonViQuyDoiDAO dvqdDAO = new DonViQuyDoiDAO();
    private ChiTietPhieuNhapDAO ctpnDAO = new ChiTietPhieuNhapDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DialogBangGia(Component parent, Object[] data) {
        super(SwingUtilities.windowForComponent(parent), "Thiết Lập Bảng Giá", ModalityType.APPLICATION_MODAL);
        this.parent = parent;

        if (data == null) {
            isDraftMode = true;
        } else {
            isDraftMode = false;
        }

        initComponents();

        if (isDraftMode) {
            initDraftMode();
        } else {
            initAdjustMode(data);
        }
    }

    private void initComponents() {
        setLayout(new MigLayout("wrap,fillx,insets 20, width 1000, height 700", "[fill]", "[]20[]10[grow]10[]"));

        String title = isDraftMode ? "TẠO BẢNG GIÁ MỚI (DỰ THẢO)" : "ĐIỀU CHỈNH GIÁ BÁN (ÁP DỤNG NGAY)";
        JLabel lbTitle = new JLabel(title);
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +6; foreground:$Accent.color");
        add(lbTitle, "center");

        add(createHeaderPanel());
        add(createToolbarPanel());
        add(createTablePanel(), "grow");
        add(createFooterPanel());

        pack();
        setLocationRelativeTo(parent);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0, fillx", "[][grow]20[][grow]", "[]10[]"));

        txtMaBG = new JTextField("(Tự động sinh)");
        txtMaBG.setEditable(false);
        txtMaBG.putClientProperty(FlatClientProperties.STYLE, "font:bold");

        txtTenBG = new JTextField();
        txtTenBG.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ví dụ: Bảng giá khuyến mãi 20/11");

        txtNgayHL = new JFormattedTextField();
        datePickerHL = new DatePicker();
        datePickerHL.setEditor(txtNgayHL);
        datePickerHL.setDateFormat("dd/MM/yyyy");
        datePickerHL.setCloseAfterSelected(true);

        txtNgayKT = new JFormattedTextField();
        datePickerKT = new DatePicker();
        datePickerKT.setEditor(txtNgayKT);
        datePickerKT.setDateFormat("dd/MM/yyyy");
        datePickerKT.setCloseAfterSelected(true);

        txtGhiChu = new JTextArea(2, 0);
        txtGhiChu.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập ghi chú cho bảng giá này (nếu có)...");
        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);

        panel.add(new JLabel("Mã Bảng Giá:"));
        panel.add(txtMaBG, "w 250");
        panel.add(new JLabel("Hiệu lực từ:"));
        panel.add(txtNgayHL, "wrap, w 150");

        panel.add(new JLabel("Tên Bảng Giá:"));
        panel.add(txtTenBG, "w 250");
        panel.add(new JLabel("Hiệu lực đến:"));
        panel.add(txtNgayKT, "wrap, w 150");
        panel.add(new JLabel("Ghi chú:"));
        panel.add(scrollGhiChu, "span 3, growx, h 60!");

        return panel;
    }

    private JPanel createToolbarPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 5, fillx", "[]10[]push[]5[]5[]5[]", "[]"));
        panel.setBorder(BorderFactory.createTitledBorder("Công cụ & Bộ lọc"));

        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm mã hoặc tên thuốc...");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTable();
            }
        });

        cbNhomThuoc = new JComboBox<>();
        cbNhomThuoc.addItem("Tất cả nhóm");
        ArrayList<String> listNhom = thuocDAO.getAllNhomThuocName();
        for (String s : listNhom) {
            cbNhomThuoc.addItem(s);
        }
        cbNhomThuoc.addActionListener(e -> filterTable());

        JButton btnGiam5 = new JButton("-5%");
        btnGiam5.addActionListener(e -> adjustPrice(-0.05));
        JButton btnTang5 = new JButton("+5%");
        btnTang5.addActionListener(e -> adjustPrice(0.05));
        JButton btnTang10 = new JButton("+10%");
        btnTang10.addActionListener(e -> adjustPrice(0.10));

        panel.add(txtSearch, "w 200!");
        panel.add(cbNhomThuoc, "w 200!");
        panel.add(new JLabel("Điều chỉnh giá:"));
        panel.add(btnGiam5);
        panel.add(btnTang5);
        panel.add(btnTang10);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());

        String[] cols = {"Mã Thuốc", "Tên Thuốc", "Nhóm", "ĐVT", "Giá Nhập", "GIÁ BÁN MỚI"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 5;
            }
        };
        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:28; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        RightAlignRenderer right = new RightAlignRenderer();
        table.getColumnModel().getColumn(4).setCellRenderer(right);
        table.getColumnModel().getColumn(5).setCellRenderer(right);

        panel.add(new JScrollPane(table));
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "push[][]"));
        JButton btnHuy = new JButton("Hủy bỏ");
        btnHuy.addActionListener(e -> dispose());

        JButton btnLuu = new JButton(isDraftMode ? "LƯU (CHƯA ÁP DỤNG)" : "LƯU & ÁP DỤNG NGAY");
        btnLuu.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold +2; margin:10,20,10,20");
        btnLuu.addActionListener(e -> {
            try {
                actionSave();
            } catch (SQLException ex) {
                Logger.getLogger(DialogBangGia.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        panel.add(btnHuy);
        panel.add(btnLuu);
        return panel;
    }

    private void initDraftMode() {

        loadDetails(null);

        datePickerHL.setSelectedDate(LocalDate.now());

        txtNgayHL.setEditable(true);
        txtNgayKT.setEditable(true);
    }

    private void initAdjustMode(Object[] data) {

        loadDetails(data[0].toString());

        txtTenBG.setText(data[1].toString() + " (Điều chỉnh)");

        datePickerHL.setSelectedDate(LocalDate.now());
        txtNgayHL.setEditable(false);
        txtNgayKT.setEditable(false);

        if (data.length > 4 && data[4] != null) {
            txtGhiChu.setText(data[4].toString());
        }
    }

    private void loadDetails(String maBG) {
        model.setRowCount(0);
        List<Object[]> list;
        if (maBG == null) {
            list = ctDAO.getChiTietBangGiaHienTai();
        } else {
            list = ctDAO.getChiTietFullByMaBG(maBG);
        }

        for (Object[] row : list) {
            String maThuoc = row[0].toString();
            String donViTinh = row[3].toString();

            double giaNhapCoBan = ctpnDAO.getGiaNhapCoBanMoiNhat(maThuoc);

            DonViQuyDoi dv = dvqdDAO.getDonViByTen(maThuoc, donViTinh);

            double giaNhapMoi = 0;
            if (dv != null && giaNhapCoBan > 0) {

                giaNhapMoi = giaNhapCoBan * dv.getGiaTriQuyDoi();
            } else {

                giaNhapMoi = Double.parseDouble(row[4].toString());
            }

            model.addRow(new Object[]{
                row[0],
                row[1],
                row[2],
                row[3],
                formatMoney(giaNhapMoi),
                formatMoney(Double.parseDouble(row[5].toString()))
            });
        }
    }

    private void actionSave() throws SQLException {
        if (txtTenBG.getText().trim().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng nhập tên bảng giá!");
            return;
        }

        String confirmMsg = isDraftMode ? "Lưu bảng giá này? (Bạn cần bấm 'Áp dụng' sau)" : "Lưu và áp dụng ngay lập tức?";
        if (JOptionPane.showConfirmDialog(this, confirmMsg, "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        LocalDate hl = datePickerHL.getSelectedDate();
        LocalDate kt = datePickerKT.isDateSelected() ? datePickerKT.getSelectedDate() : null;

        BangGia newBG = new BangGia("", txtTenBG.getText(), hl, kt, txtGhiChu.getText(), !isDraftMode);

        List<Object[]> details = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object[] row = new Object[6];
            row[0] = model.getValueAt(i, 0);
            row[3] = model.getValueAt(i, 3);
            row[5] = model.getValueAt(i, 5);
            details.add(row);
        }

        boolean success;
        if (isDraftMode) {
            success = bgDAO.insertDraftBangGia(newBG, details);
        } else {
            success = bgDAO.insertNewPriceList(newBG, details);
        }

        if (success) {
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Thành công!");
            isSave = true;
            dispose();
        } else {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Lỗi hệ thống!");
        }
    }

    private void filterTable() {
        String text = txtSearch.getText().trim();
        String group = cbNhomThuoc.getSelectedItem().toString();
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + text, 0, 1));
        }
        if (!group.equals("Tất cả nhóm")) {
            filters.add(RowFilter.regexFilter("(?i)" + group, 2));
        }
        sorter.setRowFilter(RowFilter.andFilter(filters));
    }

    private void adjustPrice(double percent) {
        int rowCount = table.getRowCount();
        if (rowCount == 0) {
            return;
        }
        for (int i = 0; i < rowCount; i++) {
            int modelRow = table.convertRowIndexToModel(i);
            double current = parseMoney(model.getValueAt(modelRow, 5).toString());
            model.setValueAt(formatMoney(Math.round(current * (1 + percent) / 100) * 100), modelRow, 5);
        }
    }

    private double parseMoney(String text) {
        try {
            return Double.parseDouble(text.replace(".", "").replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatMoney(double amount) {
        return new DecimalFormat("#,##0").format(amount);
    }

    public boolean isSave() {
        return isSave;
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
