package gui.manage.partner;

import com.formdev.flatlaf.FlatClientProperties;
import dao.KhachHangDAO;
import entities.KhachHang;
import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class DialogKhachHang extends JDialog {

    private final Component parent;
    private boolean isSave = false;
    private final boolean isEdit;
    private KhachHang khachHang;
    private KhachHangDAO khachHangDAO;

    private JTextField txtMaKH, txtTenKH, txtSDT, txtNgaySinh;
    private JComboBox<String> cbGioiTinh;
    private JTextArea txtDiaChi;
    private JSpinner spinDiem;

    public DialogKhachHang(Component parent, KhachHang kh) {
        super(SwingUtilities.windowForComponent(parent), "Thông Tin Khách Hàng", ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.isEdit = (kh != null);
        this.khachHangDAO = new KhachHangDAO();
        initComponents();
        if (isEdit) {
            fillData(kh);
        } else {
            txtMaKH.setText(khachHangDAO.getNextMaKH());
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

    private void fillData(KhachHang kh) {
        txtMaKH.setText(kh.getMaKH());
        txtTenKH.setText(kh.getTenKH());
        txtSDT.setText(kh.getSdt());
        cbGioiTinh.setSelectedItem(kh.isGioiTinh() ? "Nam" : "Nữ");
        if (kh.getNgaySinh() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            txtNgaySinh.setText(kh.getNgaySinh().format(formatter));
        }
        txtDiaChi.setText(kh.getDiaChi() != null ? kh.getDiaChi() : "");
        spinDiem.setValue(kh.getDiemTichLuy());
    }

    private void actionSave() {
        // Validate tên khách hàng
        String tenKH = txtTenKH.getText().trim();
        if (tenKH.isEmpty()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Vui lòng nhập tên khách hàng!");
            txtTenKH.requestFocus();
            return;
        }
        
        // Kiểm tra tên có ít nhất 2 từ
        String[] words = tenKH.split("\\s+");
        if (words.length < 2) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Tên khách hàng phải có ít nhất 2 từ!");
            txtTenKH.requestFocus();
            return;
        }
        
        // Kiểm tra mỗi từ phải bắt đầu bằng chữ hoa
        for (String word : words) {
            if (word.length() > 0 && !Character.isUpperCase(word.charAt(0))) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Mỗi từ trong tên phải bắt đầu bằng chữ hoa!");
                txtTenKH.requestFocus();
                return;
            }
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
        
        // Validate ngày sinh nếu có nhập
        LocalDate ngaySinh = null;
        String ngaySinhStr = txtNgaySinh.getText().trim();
        if (!ngaySinhStr.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                ngaySinh = LocalDate.parse(ngaySinhStr, formatter);
                
                // Kiểm tra không được là ngày tương lai
                if (ngaySinh.isAfter(LocalDate.now())) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Ngày sinh không được là ngày tương lai!");
                    txtNgaySinh.requestFocus();
                    return;
                }
            } catch (DateTimeParseException e) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Định dạng ngày sinh không đúng (dd/MM/yyyy)!");
                txtNgaySinh.requestFocus();
                return;
            }
        }
        
        // Tạo đối tượng KhachHang
        String maKH = txtMaKH.getText();
        boolean gioiTinh = cbGioiTinh.getSelectedItem().equals("Nam");
        String diaChi = txtDiaChi.getText().trim();
        int diemTichLuy = (Integer) spinDiem.getValue();
        
        khachHang = new KhachHang(maKH, tenKH, sdt, gioiTinh, ngaySinh, 
                                   diaChi.isEmpty() ? null : diaChi, diemTichLuy, true);
        
        isSave = true;
        dispose();
    }

    public boolean isSave() {
        return isSave;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }
}
