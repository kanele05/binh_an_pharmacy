package gui.profile;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Component;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class DialogDoiMatKhau extends JDialog {

    private final Component parent;
    private JPasswordField txtMatKhauCu;
    private JPasswordField txtMatKhauMoi;
    private JPasswordField txtXacNhanMoi;

    public DialogDoiMatKhau(Component parent) {
        super(SwingUtilities.windowForComponent(parent), "Đổi Mật Khẩu", ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("wrap,fillx,insets 20, width 400", "[][grow,fill]", "[]15[]"));

        JLabel lbTitle = new JLabel("ĐỔI MẬT KHẨU");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:$Accent.color");
        add(lbTitle, "span 2, center, wrap 20");

        txtMatKhauCu = new JPasswordField();
        txtMatKhauCu.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");

        txtMatKhauMoi = new JPasswordField();
        txtMatKhauMoi.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");

        txtXacNhanMoi = new JPasswordField();
        txtXacNhanMoi.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");

        add(new JLabel("Mật khẩu hiện tại:"));
        add(txtMatKhauCu);

        add(new JLabel("Mật khẩu mới:"));
        add(txtMatKhauMoi);

        add(new JLabel("Xác nhận mới:"));
        add(txtXacNhanMoi);

        JLabel note = new JLabel("Mật khẩu nên có ít nhất 6 ký tự.");
        note.putClientProperty(FlatClientProperties.STYLE, "font:90%; foreground:$Label.disabledForeground");
        add(note, "span 2, wrap 10");

        JPanel pFooter = new JPanel(new MigLayout("insets 0", "push[][]"));
        JButton btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> dispose());

        JButton btnLuu = new JButton("Xác nhận");
        btnLuu.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold");
        btnLuu.addActionListener(e -> actionChangePass());

        pFooter.add(btnHuy);
        pFooter.add(btnLuu);
        add(pFooter, "span 2");

        pack();
        setLocationRelativeTo(parent);
    }

    private void actionChangePass() {
        String oldPass = new String(txtMatKhauCu.getPassword());
        String newPass = new String(txtMatKhauMoi.getPassword());
        String confirmPass = new String(txtXacNhanMoi.getPassword());

        if (oldPass.isEmpty() || newPass.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (!oldPass.equals("123456")) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Mật khẩu cũ không đúng!");
            return;
        }

        if (newPass.length() < 6) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Mật khẩu mới quá ngắn!");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Mật khẩu xác nhận không khớp!");
            return;
        }

        Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Đổi mật khẩu thành công!");
        dispose();
    }
}
