package gui.manage.partner;

import com.formdev.flatlaf.FlatClientProperties;
import dao.NhaCungCapDAO;
import entities.NhaCungCap;
import java.awt.Component;
import java.awt.Dialog;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class DialogNhaCungCap extends JDialog {

    private final Component parent;
    private boolean isSave = false;
    private final boolean isEdit;
    private NhaCungCap nhaCungCap;
    private NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAO();

    private JTextField txtMaNCC, txtTenNCC, txtSDT, txtEmail;
    private JTextArea txtDiaChi;

     public DialogNhaCungCap(Component parent, Object[] data) {
        super(SwingUtilities.windowForComponent(parent), "Thông Tin Nhà Cung Cấp", Dialog.ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.isEdit = (data != null);
        initComponents();

       if(isEdit) fillData(data); 
       else {
            txtMaNCC.setText(nhaCungCapDAO.getNewMaNCC());
        }
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                txtTenNCC.requestFocusInWindow();
            }
        });
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
        // 0:Mã, 1:Tên, 2:SĐT, 3:Email, 4:ĐC
        txtMaNCC.setText(data[0].toString());
        txtTenNCC.setText(data[1].toString());
        txtSDT.setText(data[2].toString());
        txtEmail.setText(data[3].toString());
        txtDiaChi.setText(data[4].toString());
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
public Object[] getData() {
        return new Object[]{
            txtMaNCC.getText().equals("AUTO") ? "NCC" + System.currentTimeMillis() % 1000 : txtMaNCC.getText(),
            txtTenNCC.getText(),
            txtSDT.getText(),
            txtEmail.getText(),
            txtDiaChi.getText()
        };
    }
    public NhaCungCap getNhaCungCap() {
        return new NhaCungCap(
            txtMaNCC.getText().equals("AUTO")
                ? "NCC" + String.format("%03d", System.currentTimeMillis() % 1000)
                : txtMaNCC.getText().trim(),
            txtTenNCC.getText().trim(),
            txtSDT.getText().trim(),
            txtEmail.getText().trim(),
            txtDiaChi.getText().trim()
        );
}
    public boolean isSave() {
        return isSave;
    }
    public void setMaNCC(String maNCC) {
        txtMaNCC.setText(maNCC);
    }
}
