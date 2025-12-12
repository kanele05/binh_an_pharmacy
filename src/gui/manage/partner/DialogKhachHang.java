package gui.manage.partner;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Component;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class DialogKhachHang extends JDialog {

    private final Component parent;
    private boolean isSave = false;
    private final boolean isEdit;

    private JTextField txtMaKH, txtTenKH, txtSDT, txtNgaySinh;
    private JComboBox<String> cbGioiTinh;
    private JTextArea txtDiaChi;
    private JSpinner spinDiem;

    public DialogKhachHang(Component parent, Object[] data) {
        super(SwingUtilities.windowForComponent(parent), "Thông Tin Khách Hàng", ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.isEdit = (data != null);
        initComponents();
        if (isEdit) {
            fillData(data);
        }
    }

    private void initComponents() {
        setLayout(new MigLayout("wrap,fillx,insets 20, width 500", "[label, 100]10[grow,fill]", "[]15[]"));

        JLabel lbTitle = new JLabel(isEdit ? "SỬA THÔNG TIN KHÁCH" : "THÊM KHÁCH HÀNG MỚI");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:$Accent.color");
        add(lbTitle, "span 2, center, wrap 20");

        txtMaKH = new JTextField(isEdit ? "" : "AUTO");
        txtMaKH.setEditable(false);
        add(new JLabel("Mã KH:"));
        add(txtMaKH);

        txtTenKH = new JTextField();
        txtTenKH.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập họ và tên");
        add(new JLabel("Họ tên:"));
        add(txtTenKH);

        txtSDT = new JTextField();
        cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});

        add(new JLabel("Số ĐT:"));
        add(txtSDT, "split 3, w 150!");
        add(new JLabel("Giới tính:"), "gapleft 10");
        add(cbGioiTinh, "grow");

        txtNgaySinh = new JTextField();
        txtNgaySinh.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/MM/yyyy");
        add(new JLabel("Ngày sinh:"));
        add(txtNgaySinh);

        txtDiaChi = new JTextArea(3, 0);
        JScrollPane scrollAddr = new JScrollPane(txtDiaChi);
        add(new JLabel("Địa chỉ:"));
        add(scrollAddr);

        spinDiem = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));
        if (isEdit) {
            add(new JLabel("Điểm tích lũy:"));
            add(spinDiem);
        } else {
            spinDiem.setVisible(false);
        }

        JPanel pFooter = new JPanel(new MigLayout("insets 20 0 0 0", "push[][]"));
        JButton btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> dispose());

        JButton btnLuu = new JButton("Lưu");
        btnLuu.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold");
        btnLuu.addActionListener(e -> actionSave());

        pFooter.add(btnHuy);
        pFooter.add(btnLuu);
        add(pFooter, "span 2");

        pack();
        setLocationRelativeTo(parent);
    }

    private void fillData(Object[] data) {

        txtMaKH.setText(data[0].toString());
        txtTenKH.setText(data[1].toString());
        txtSDT.setText(data[2].toString());
        cbGioiTinh.setSelectedItem(data[3].toString());
        txtNgaySinh.setText(data[4].toString());
        txtDiaChi.setText(data[5].toString());
        try {
            spinDiem.setValue(Integer.parseInt(data[6].toString()));
        } catch (Exception e) {
        }
    }

    private void actionSave() {
        if (txtTenKH.getText().isEmpty() || txtSDT.getText().isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng nhập tên và số điện thoại!");
            return;
        }
        isSave = true;
        dispose();
    }

    public boolean isSave() {
        return isSave;
    }

    public Object[] getData() {
        return new Object[]{
            txtMaKH.getText().equals("AUTO") ? "KH" + System.currentTimeMillis() % 1000 : txtMaKH.getText(),
            txtTenKH.getText(),
            txtSDT.getText(),
            cbGioiTinh.getSelectedItem(),
            txtNgaySinh.getText(),
            txtDiaChi.getText(),
            spinDiem.getValue()
        };
    }
}
