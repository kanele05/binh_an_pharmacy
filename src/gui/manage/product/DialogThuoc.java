package gui.manage.product;

import com.formdev.flatlaf.FlatClientProperties;
import dao.DonViQuyDoiDAO;
import dao.ThuocDAO;
import dto.ThuocFullInfo;
import entities.DonViQuyDoi;
import java.awt.Component;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class DialogThuoc extends JDialog {

    private final Component parent;
    private final boolean isEdit;
    private boolean isSave = false;

    private JTextField txtMaThuoc;
    private JTextField txtTenThuoc;
    private JComboBox<String> cbNhomThuoc;
    private JTextField txtHoatChat;
    private JComboBox<String> cbDVT;
    private JSpinner spinGiaBan;

    private Object[] currentData;
    private ThuocDAO thuocDAO = new ThuocDAO();
    private DonViQuyDoiDAO dvqdDAO = new DonViQuyDoiDAO();
    private JTable tblDonViQuyDoi;
    private DefaultTableModel modelDVQD;
    private DonViQuyDoiDAO donViDAO = new DonViQuyDoiDAO();

    public DialogThuoc(Component parent, Object[] data) {
        super(SwingUtilities.windowForComponent(parent), "Thuốc", ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.currentData = data;
        this.isEdit = (data != null);

        initComponents();
        if (isEdit) {
            fillData();
        } else {
            generateNewMaThuoc();
        }
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                txtTenThuoc.requestFocusInWindow();
            }
        });
    }

    private void initComponents() {
        setTitle(isEdit ? "Cập Nhật Thông Tin Thuốc" : "Thêm Thuốc Mới");

        setLayout(new MigLayout("wrap,fillx,insets 20, width 600", "[label, 100]10[grow,fill]", "[]15[]"));

        JLabel lbTitle = new JLabel(isEdit ? "SỬA THÔNG TIN" : "THÊM THUỐC MỚI");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:$Accent.color");
        add(lbTitle, "span 2, center, wrap 20");

        add(new JLabel("Mã thuốc:"));
        txtMaThuoc = new JTextField();
        txtMaThuoc.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tự động tạo nếu để trống");
        txtMaThuoc.setEditable(false);
        add(txtMaThuoc);

        add(new JLabel("Tên thuốc:"));
        txtTenThuoc = new JTextField();
        txtTenThuoc.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ví dụ: Panadol Extra");
        add(txtTenThuoc);

        add(new JLabel("Nhóm thuốc:"));
        cbNhomThuoc = new JComboBox<>();
        loadNhomThuoc();

        JButton btnAddNhom = new JButton("+");
        btnAddNhom.setToolTipText("Thêm nhóm thuốc mới");
        btnAddNhom.addActionListener(e -> actionThemNhomNhanh());

        add(cbNhomThuoc, "split 2, growx");
        add(btnAddNhom, "w 30!, h 30!");

        add(new JLabel("Hoạt chất:"));
        txtHoatChat = new JTextField();
        txtHoatChat.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ví dụ: Paracetamol");
        add(txtHoatChat);

        add(new JLabel("Đơn vị tính:"));
        cbDVT = new JComboBox<>();
        loadDVT();

        JButton btnAddDVT = new JButton("+");
        btnAddDVT.setToolTipText("Thêm đơn vị tính mới");
        btnAddDVT.addActionListener(e -> actionThemDVTNhanh());

        add(cbDVT, "split 2, width 50%");
        add(btnAddDVT, "w 30!, h 30!, wrap");

        add(new JLabel("Giá bán:"));
        spinGiaBan = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000000000.0, 1000.0));
        spinGiaBan.setEditor(new JSpinner.NumberEditor(spinGiaBan, "#,##0 ₫"));
        add(spinGiaBan);

        add(new JSeparator(), "span 2, growx, gapy 10 10");
        JLabel lbDVQD = new JLabel("Thiết lập đơn vị quy đổi (Tùy chọn):");
        lbDVQD.putClientProperty(FlatClientProperties.STYLE, "font:bold");
        add(lbDVQD, "span 2, wrap");

        initTableDonViQuyDoi();

        JPanel panelActions = new JPanel(new MigLayout("insets 20 0 0 0", "push[][]"));
        panelActions.setOpaque(false);

        JButton btnHuy = new JButton("Hủy bỏ");
        btnHuy.addActionListener(e -> closeDialog());

        JButton btnLuu = new JButton("Lưu thông tin");
        btnLuu.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:#4CAF50;"
                + "foreground:#ffffff;"
                + "font:bold");
        btnLuu.addActionListener(e -> actionSave());

        panelActions.add(btnHuy);
        panelActions.add(btnLuu);
        add(panelActions, "span 2, growx");

        pack();
        setLocationRelativeTo(parent);
    }

    private void initTableDonViQuyDoi() {

        modelDVQD = new DefaultTableModel(new Object[] { "Tên Đơn Vị", "Giá Trị Quy Đổi (SL)", "Giá Bán" }, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) {
                    return Integer.class;
                }
                if (columnIndex == 2) {
                    return Double.class;
                }
                return String.class;
            }
        };
        tblDonViQuyDoi = new JTable(modelDVQD);
        tblDonViQuyDoi.setRowHeight(25);

        // Thêm combobox cho cột tên đơn vị
        TableColumn tenDonViColumn = tblDonViQuyDoi.getColumnModel().getColumn(0);
        tenDonViColumn.setCellEditor(new DefaultCellEditor(createTenDonViComboBox()));

        JPanel pnlTable = new JPanel(new MigLayout("insets 0", "[grow]5[]", "[grow]"));
        pnlTable.add(new JScrollPane(tblDonViQuyDoi), "grow, height 120!");

        JPanel pnlControl = new JPanel(new MigLayout("wrap, insets 0", "fill", "top"));
        JButton btnAddRow = new JButton("+");
        btnAddRow.addActionListener(e -> {
            java.util.List<String> listTenDonVi = donViDAO.getAllTenDonVi();
            String defaultTenDonVi = listTenDonVi.isEmpty() ? "" : listTenDonVi.get(0);
            modelDVQD.addRow(new Object[] { defaultTenDonVi, 10, 0.0 });
        });

        JButton btnDelRow = new JButton("-");
        btnDelRow.addActionListener(e -> {
            int row = tblDonViQuyDoi.getSelectedRow();
            if (row != -1) {
                modelDVQD.removeRow(row);
            }
        });

        pnlControl.add(btnAddRow);
        pnlControl.add(btnDelRow);
        pnlTable.add(pnlControl, "top");

        add(pnlTable, "span 2, growx");
    }

    private void actionThemNhomNhanh() {
        String tenNhomMoi = JOptionPane.showInputDialog(this, "Nhập tên nhóm thuốc mới:", "Thêm Nhóm",
                JOptionPane.PLAIN_MESSAGE);

        if (tenNhomMoi != null && !tenNhomMoi.trim().isEmpty()) {
            tenNhomMoi = tenNhomMoi.trim();

            for (int i = 0; i < cbNhomThuoc.getItemCount(); i++) {
                if (cbNhomThuoc.getItemAt(i).equalsIgnoreCase(tenNhomMoi)) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                            "Nhóm thuốc này đã tồn tại!");
                    return;
                }
            }

            if (thuocDAO.themNhomThuocNhanh(tenNhomMoi)) {
                cbNhomThuoc.addItem(tenNhomMoi);
                cbNhomThuoc.setSelectedItem(tenNhomMoi);
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,
                        "Đã thêm nhóm mới!");
            } else {
                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER,
                        "Lỗi khi lưu nhóm thuốc!");
            }
        }
    }

    private void actionThemDVTNhanh() {
        String dvtMoi = JOptionPane.showInputDialog(this, "Nhập tên đơn vị tính mới:", "Thêm ĐVT",
                JOptionPane.PLAIN_MESSAGE);

        if (dvtMoi != null && !dvtMoi.trim().isEmpty()) {
            dvtMoi = dvtMoi.trim();

            for (int i = 0; i < cbDVT.getItemCount(); i++) {
                if (cbDVT.getItemAt(i).equalsIgnoreCase(dvtMoi)) {
                    cbDVT.setSelectedIndex(i);
                    return;
                }
            }

            cbDVT.addItem(dvtMoi);
            cbDVT.setSelectedItem(dvtMoi);
        }
    }

    private JComboBox<String> createTenDonViComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setEditable(true);

        java.util.List<String> listTenDonVi = donViDAO.getAllTenDonVi();
        for (String tenDV : listTenDonVi) {
            comboBox.addItem(tenDV);
        }

        String[] donViPhoBien = { "Viên", "Vỉ", "Hộp", "Chai", "Ống", "Tuýp", "Lọ", "Gói" };
        for (String dv : donViPhoBien) {
            if (!listTenDonVi.contains(dv)) {
                comboBox.addItem(dv);
            }
        }

        return comboBox;
    }

    private void loadNhomThuoc() {
        cbNhomThuoc.removeAllItems();
        ArrayList<String> dsNhom = thuocDAO.getAllNhomThuocName();
        for (String tenNhom : dsNhom) {
            cbNhomThuoc.addItem(tenNhom);
        }
    }

    private void loadDVT() {
        cbDVT.removeAllItems();
        ArrayList<String> listDVT = thuocDAO.getAllDonViTinh();
        for (String dvt : listDVT) {
            cbDVT.addItem(dvt);
        }
    }

    // dsdasdasdđâs
    private void fillData() {
        txtMaThuoc.setText(currentData[0].toString());
        txtTenThuoc.setText(currentData[1].toString());
        cbNhomThuoc.setSelectedItem(currentData[2].toString());
        txtHoatChat.setText(currentData[3].toString());
        cbDVT.setSelectedItem(currentData[4].toString());

        if (currentData[5] instanceof Number) {
            spinGiaBan.setValue(((Number) currentData[5]).doubleValue());
        } else {
            try {
                spinGiaBan.setValue(parseCurrency(currentData[5].toString()));
            } catch (Exception e) {
                spinGiaBan.setValue(0.0);
            }
        }

        // Load DonViQuyDoi
        modelDVQD.setRowCount(0);
        String maThuoc = currentData[0].toString();
        ArrayList<DonViQuyDoi> listDV = dvqdDAO.getAllDonViByMaThuoc(maThuoc);
        for (DonViQuyDoi dv : listDV) {
            if (!dv.isLaDonViCoBan()) {
                modelDVQD.addRow(new Object[]{dv.getTenDonVi(), dv.getGiaTriQuyDoi(), dv.getGiaBan()});
            }
        }
    }

    private double parseCurrency(String text) {
        return Double.parseDouble(text.replace(".", "").replace(",", "").trim());
    }

    private void actionSave() {
        if (!validateTenThuoc()) {
            return;
        }
        if (!validateHoatChat()) {
            return;
        }
        if (!validateGiaBan()) {
            return;
        }

        if (tblDonViQuyDoi.isEditing()) {
            tblDonViQuyDoi.getCellEditor().stopCellEditing();
        }

        for (int i = 0; i < modelDVQD.getRowCount(); i++) {
            String tenDV = modelDVQD.getValueAt(i, 0).toString().trim();
            int quyDoi = Integer.parseInt(modelDVQD.getValueAt(i, 1).toString());

            if (tenDV.isEmpty()) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                        "Tên đơn vị quy đổi không được để trống!");
                return;
            }
            if (tenDV.equalsIgnoreCase(cbDVT.getSelectedItem().toString())) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                        "Đơn vị quy đổi không được trùng đơn vị cơ bản!");
                return;
            }
            if (quyDoi <= 1) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                        "Giá trị quy đổi phải lớn hơn 1!");
                return;
            }
        }
        isSave = true;
        closeDialog();
    }

    private boolean validateTenThuoc() {
        String tenThuoc = txtTenThuoc.getText().trim();

        if (tenThuoc.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Tên thuốc không được để trống!");
            txtTenThuoc.requestFocus();
            return false;
        }
        if (!tenThuoc.matches("^[a-zA-Z0-9\\s]+$")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Tên thuốc chỉ được chứa chữ cái, số và dấu cách!");
            txtTenThuoc.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateHoatChat() {
        String hoatChat = txtHoatChat.getText().trim();

        if (hoatChat.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Hoạt chất không được để trống!");
            txtHoatChat.requestFocus();
            return false;
        }
        if (!hoatChat.matches("^[a-zA-Z0-9\\s]+$")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Hoạt chất chỉ được chứa chữ cái, số và dấu cách!");
            txtHoatChat.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateGiaBan() {
        try {
            double giaBan = ((Number) spinGiaBan.getValue()).doubleValue();

            if (giaBan <= 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                        "Giá bán phải lớn hơn 0!");
                spinGiaBan.requestFocus();
                return false;
            }

            return true;
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Giá bán không hợp lệ!");
            spinGiaBan.requestFocus();
            return false;
        }
    }

    private void closeDialog() {
        dispose();
    }

    public boolean isSave() {
        return isSave;
    }

    public Object[] getData() {
        DecimalFormat df = new DecimalFormat("#,##0");
        return new Object[] {
                txtMaThuoc.getText().isEmpty() ? "AUTO" : txtMaThuoc.getText(),
                txtTenThuoc.getText(),
                cbNhomThuoc.getSelectedItem(),
                txtHoatChat.getText(),
                cbDVT.getSelectedItem(),
                0,
                df.format(spinGiaBan.getValue()),
                0
        };
    }

    private void generateNewMaThuoc() {
        ArrayList<ThuocFullInfo> list = thuocDAO.getAllThuocFullInfo();
        if (list.isEmpty()) {
            txtMaThuoc.setText("T001");
        } else {

            String lastID = list.get(list.size() - 1).getMaThuoc();
            try {
                int number = Integer.parseInt(lastID.substring(1));
                number++;
                txtMaThuoc.setText(String.format("T%03d", number));
            } catch (Exception e) {
                txtMaThuoc.setText("T" + (list.size() + 1));
            }
        }
    }

    public ArrayList<DonViQuyDoi> getListDonViQuyDoi() {
        ArrayList<DonViQuyDoi> list = new ArrayList<>();
        String maThuoc = txtMaThuoc.getText();

        for (int i = 0; i < modelDVQD.getRowCount(); i++) {
            String tenDV = modelDVQD.getValueAt(i, 0).toString().trim();
            int quyDoi = Integer.parseInt(modelDVQD.getValueAt(i, 1).toString());
            double giaBan = Double.parseDouble(modelDVQD.getValueAt(i, 2).toString());

            DonViQuyDoi dv = new DonViQuyDoi();
            dv.setMaThuoc(maThuoc);
            dv.setTenDonVi(tenDV);
            dv.setGiaTriQuyDoi(quyDoi);
            dv.setGiaBan(giaBan);
            dv.setLaDonViCoBan(false);
            // adasdasdsad adsa
            list.add(dv);
        }
        return list;
    }
}
