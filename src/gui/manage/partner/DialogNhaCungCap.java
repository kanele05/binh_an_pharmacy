package gui.manage.partner;

import com.formdev.flatlaf.FlatClientProperties;
import dao.NhaCungCapDAO;
import entities.NhaCungCap;
import java.awt.Component;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class DialogNhaCungCap extends JDialog {

    private final Component parent;
    private boolean isSave = false;
    private final boolean isEdit;
    private NhaCungCap nhaCungCap;
    private NhaCungCapDAO nhaCungCapDAO;

    private JTextField txtMaNCC, txtTenNCC, txtSDT, txtEmail;
    private JTextArea txtDiaChi;

    public DialogNhaCungCap(Component parent, NhaCungCap ncc) {
        super(SwingUtilities.windowForComponent(parent), "Thông Tin Nhà Cung Cấp", ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.isEdit = (ncc != null);
        this.nhaCungCapDAO = new NhaCungCapDAO();
        initComponents();
        if (isEdit) {
            fillData(ncc);
        } else {
            txtMaNCC.setText(nhaCungCapDAO.getNewMaNCC());
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

    private void fillData(NhaCungCap ncc) {
        txtMaNCC.setText(ncc.getMaNCC());
        txtTenNCC.setText(ncc.getTenNCC());
        txtSDT.setText(ncc.getSdt());
        txtEmail.setText(ncc.getEmail() != null ? ncc.getEmail() : "");
        txtDiaChi.setText(ncc.getDiaChi() != null ? ncc.getDiaChi() : "");
    }

    private void actionSave() {
        // Validate tên NCC
        String tenNCC = txtTenNCC.getText().trim();
        if (tenNCC.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng nhập tên nhà cung cấp!");
            txtTenNCC.requestFocus();
            return;
        }
        
        // Validate số điện thoại
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng nhập số điện thoại!");
            txtSDT.requestFocus();
            return;
        }
        
        if (!sdt.matches("\\d{10}")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Số điện thoại phải có đúng 10 chữ số!");
            txtSDT.requestFocus();
            return;
        }
        
        if (!sdt.startsWith("0")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Số điện thoại phải bắt đầu bằng số 0!");
            txtSDT.requestFocus();
            return;
        }
        
        // Validate email nếu có nhập
        String email = txtEmail.getText().trim();
        if (!email.isEmpty()) {
            if (!email.endsWith("@gmail.com") && !email.endsWith("@yahoo.com")) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Email phải kết thúc bằng @gmail.com hoặc @yahoo.com!");
                txtEmail.requestFocus();
                return;
            }
        }
        
        // Tạo đối tượng NhaCungCap
        String maNCC = txtMaNCC.getText();
        String diaChi = txtDiaChi.getText().trim();
        
        nhaCungCap = new NhaCungCap(maNCC, tenNCC, sdt, 
                                     email.isEmpty() ? null : email, 
                                     diaChi.isEmpty() ? null : diaChi);
        
        isSave = true;
        dispose();
    }

    public boolean isSave() {
        return isSave;
    }

    public NhaCungCap getNhaCungCap() {
        return nhaCungCap;
    }
}
