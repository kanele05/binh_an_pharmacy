package gui.transaction.inventory;

import com.formdev.flatlaf.FlatClientProperties;
import dao.ThuocDAO;
import dto.ThuocFullInfo;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class DialogThemThuocNhap extends JDialog {

    private FormNhapHang formNhapHang;
    private ThuocDAO thuocDAO = new ThuocDAO();
    private List<ThuocFullInfo> dsThuoc;
    private DecimalFormat moneyFormat = new DecimalFormat("#,##0 ₫");

    private JTextField txtTimKiem;
    private JTable tableThuoc;
    private DefaultTableModel modelThuoc;
    private JSpinner spnSoLuong;
    private JTextField txtDonGia;
    private JSpinner spnHanSuDung;

    public DialogThemThuocNhap(Frame parent, FormNhapHang formNhapHang) {
        super(parent, "Thêm Thuốc Vào Phiếu Nhập", true);
        this.formNhapHang = formNhapHang;
        initUI();
        loadDanhSachThuoc();
        setSize(800, 600);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new MigLayout("wrap, fill, insets 20", "[fill]", "[][grow][]"));

        // Panel tìm kiếm
        add(createSearchPanel(), "wrap 10");

        // Panel bảng thuốc
        add(createTablePanel(), "grow, wrap 10");

        // Panel nhập thông tin
        add(createInputPanel(), "wrap 15");

        // Panel nút bấm
        add(createButtonPanel());
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10", "[][grow]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:darken(@background,3%)");

        JLabel lbTimKiem = new JLabel("Tìm thuốc:");
        lbTimKiem.putClientProperty(FlatClientProperties.STYLE, "font:bold");

        txtTimKiem = new JTextField();
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mã thuốc, tên thuốc hoặc hoạt chất...");
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterThuoc(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterThuoc(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterThuoc(); }
        });

        panel.add(lbTimKiem);
        panel.add(txtTimKiem, "growx");

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Mã thuốc", "Tên thuốc", "Hoạt chất", "ĐVT", "Giá nhập gợi ý", "Tồn kho"};
        modelThuoc = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableThuoc = new JTable(modelThuoc);
        tableThuoc.putClientProperty(FlatClientProperties.STYLE, "rowHeight:28; showHorizontalLines:true");
        tableThuoc.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:32; font:bold");

        // Set column widths
        tableThuoc.getColumnModel().getColumn(0).setPreferredWidth(70);
        tableThuoc.getColumnModel().getColumn(1).setPreferredWidth(180);
        tableThuoc.getColumnModel().getColumn(2).setPreferredWidth(150);
        tableThuoc.getColumnModel().getColumn(3).setPreferredWidth(60);
        tableThuoc.getColumnModel().getColumn(4).setPreferredWidth(100);
        tableThuoc.getColumnModel().getColumn(5).setPreferredWidth(70);

        // Right align cho cột số
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tableThuoc.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tableThuoc.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        // Khi chọn thuốc, tự động điền giá nhập gợi ý
        tableThuoc.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableThuoc.getSelectedRow();
                if (row >= 0) {
                    String giaStr = modelThuoc.getValueAt(row, 4).toString()
                            .replace(".", "").replace(",", "").replace(" ₫", "").trim();
                    try {
                        double gia = Double.parseDouble(giaStr);
                        txtDonGia.setText(moneyFormat.format(gia).replace(" ₫", ""));
                    } catch (Exception ex) {
                        txtDonGia.setText("0");
                    }
                }
            }
        });

        // Double click để thêm nhanh
        tableThuoc.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    actionThemThuoc();
                }
            }
        });

        panel.add(new JScrollPane(tableThuoc), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 15", "[][grow,fill][][grow,fill][][grow,fill]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:15; background:lighten(#E3F2FD,5%)");

        // Số lượng
        JLabel lbSoLuong = new JLabel("Số lượng:");
        lbSoLuong.putClientProperty(FlatClientProperties.STYLE, "font:bold");
        spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        JSpinner.NumberEditor editorSL = new JSpinner.NumberEditor(spnSoLuong, "#,##0");
        spnSoLuong.setEditor(editorSL);

        // Đơn giá
        JLabel lbDonGia = new JLabel("Đơn giá nhập:");
        lbDonGia.putClientProperty(FlatClientProperties.STYLE, "font:bold");
        txtDonGia = new JTextField("0");
        txtDonGia.setHorizontalAlignment(JTextField.RIGHT);

        // Hạn sử dụng
        JLabel lbHSD = new JLabel("Hạn SD (tháng):");
        lbHSD.putClientProperty(FlatClientProperties.STYLE, "font:bold");
        // Mặc định hạn sử dụng là 24 tháng kể từ ngày nhập
        spnHanSuDung = new JSpinner(new SpinnerNumberModel(24, 1, 120, 1));

        panel.add(lbSoLuong);
        panel.add(spnSoLuong, "w 100");
        panel.add(lbDonGia);
        panel.add(txtDonGia, "w 120");
        panel.add(lbHSD);
        panel.add(spnHanSuDung, "w 80");

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "push[][]", "[]"));
        panel.setOpaque(false);

        JButton btnThem = new JButton("Thêm vào phiếu");
        btnThem.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:#4CAF50;"
                + "foreground:#ffffff;"
                + "font:bold;"
                + "arc:8");
        btnThem.addActionListener(e -> actionThemThuoc());

        JButton btnDong = new JButton("Đóng");
        btnDong.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:#607D8B;"
                + "foreground:#ffffff;"
                + "font:bold;"
                + "arc:8");
        btnDong.addActionListener(e -> dispose());

        panel.add(btnThem, "w 150, h 40");
        panel.add(btnDong, "w 100, h 40");

        return panel;
    }

    private void loadDanhSachThuoc() {
        dsThuoc = thuocDAO.getAllThuocFullInfo();
        displayThuoc(dsThuoc);
    }

    private void displayThuoc(List<ThuocFullInfo> list) {
        modelThuoc.setRowCount(0);
        for (ThuocFullInfo t : list) {
            if (t.isTrangThai()) { // Chỉ hiển thị thuốc đang hoạt động
                modelThuoc.addRow(new Object[]{
                    t.getMaThuoc(),
                    t.getTenThuoc(),
                    t.getHoatChat(),
                    t.getDonViCoBan(),
                    moneyFormat.format(t.getGiaNhap()),
                    t.getTonKho()
                });
            }
        }
    }

    private void filterThuoc() {
        String keyword = txtTimKiem.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            displayThuoc(dsThuoc);
            return;
        }

        List<ThuocFullInfo> filtered = dsThuoc.stream()
            .filter(t -> t.getMaThuoc().toLowerCase().contains(keyword)
                    || t.getTenThuoc().toLowerCase().contains(keyword)
                    || (t.getHoatChat() != null && t.getHoatChat().toLowerCase().contains(keyword)))
            .collect(Collectors.toList());

        displayThuoc(filtered);
    }

    private void actionThemThuoc() {
        int selectedRow = tableThuoc.getSelectedRow();
        if (selectedRow == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng chọn thuốc!");
            return;
        }

        // Lấy thông tin thuốc
        String maThuoc = modelThuoc.getValueAt(selectedRow, 0).toString();
        ThuocFullInfo thuoc = dsThuoc.stream()
                .filter(t -> t.getMaThuoc().equals(maThuoc))
                .findFirst()
                .orElse(null);

        if (thuoc == null) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Không tìm thấy thuốc!");
            return;
        }

        // Lấy thông tin nhập
        int soLuong = (Integer) spnSoLuong.getValue();

        double donGia;
        try {
            String giaStr = txtDonGia.getText().replace(".", "").replace(",", "").trim();
            donGia = Double.parseDouble(giaStr);
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Đơn giá không hợp lệ!");
            return;
        }

        if (donGia <= 0) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Đơn giá phải lớn hơn 0!");
            return;
        }

        int thangHSD = (Integer) spnHanSuDung.getValue();
        LocalDate hanSuDung = LocalDate.now().plusMonths(thangHSD);

        // Gọi method của FormNhapHang để thêm thuốc
        formNhapHang.themThuocVaoBang(thuoc, soLuong, donGia, hanSuDung);

        // Reset form
        spnSoLuong.setValue(1);
        txtTimKiem.setText("");
        txtTimKiem.requestFocus();
    }
}
