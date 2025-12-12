package gui.manage.partner;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Component;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class DialogNhaCungCap extends JDialog {

    private final Component parent;
    private boolean isSave = false;
    private final boolean isEdit;

    private JTextField txtMaNCC, txtTenNCC, txtSDT, txtEmail;
    private JTextArea txtDiaChi;

    public DialogNhaCungCap(Component parent, Object[] data) {
        super(SwingUtilities.windowForComponent(parent), "Thông Tin Nhà Cung Cấp", ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.isEdit = (data != null);
        initComponents();
        if (isEdit) {
            fillData(data);
        }
    }

    private void initComponents() {
        setLayout(new MigLayout("wrap,fillx,insets 20, width 500", "[label, 100]10[grow,fill]", "[]15[]"));

        JLabel lbTitle = new JLabel(isEdit ? "SỬA NHÀ CUNG CẤP" : "THÊM NHÀ CUNG CẤP");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:$Accent.color");
        add(lbTitle, "span 2, center, wrap 20");

        txtMaNCC = new JTextField(isEdit ? "" : "AUTO");
        txtMaNCC.setEditable(false);
        add(new JLabel("Mã NCC:"));
        add(txtMaNCC);

        txtTenNCC = new JTextField();
        txtTenNCC.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tên công ty / nhà phân phối");
        add(new JLabel("Tên NCC:"));
        add(txtTenNCC);

        txtSDT = new JTextField();
        txtEmail = new JTextField();

        add(new JLabel("Điện thoại:"));
        add(txtSDT);

        add(new JLabel("Email:"));
        add(txtEmail);

        txtDiaChi = new JTextArea(3, 0);
        JScrollPane scrollAddr = new JScrollPane(txtDiaChi);
        add(new JLabel("Địa chỉ:"));
        add(scrollAddr);

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

        txtMaNCC.setText(data[0].toString());
        txtTenNCC.setText(data[1].toString());
        txtSDT.setText(data[2].toString());
        txtEmail.setText(data[3].toString());
        txtDiaChi.setText(data[4].toString());
    }

    private void actionSave() {
        if (txtTenNCC.getText().trim().isEmpty() || txtSDT.getText().trim().isEmpty()) {
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
            txtMaNCC.getText().equals("AUTO") ? "NCC" + System.currentTimeMillis() % 1000 : txtMaNCC.getText(),
            txtTenNCC.getText(),
            txtSDT.getText(),
            txtEmail.getText(),
            txtDiaChi.getText()
        };
    }
}
