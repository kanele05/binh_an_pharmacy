package gui.manage.staff;

import com.formdev.flatlaf.FlatClientProperties;
import dao.NhanVienDAO;
import entities.NhanVien;
import java.awt.Component;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import raven.datetime.component.date.DatePicker;
import raven.toast.Notifications;

public class DialogNhanVien extends JDialog {

    private final Component parent;
    private boolean isSave = false;
    private final boolean isEdit;
    private NhanVienDAO nhanVienDAO;

    private JTextField txtMaNV, txtHoTen, txtSDT, txtEmail;
    private JFormattedTextField txtNgaySinh;
    private JComboBox<String> cbGioiTinh, cbVaiTro, cbTrangThai;
    private JTextArea txtDiaChi;
    private DatePicker datePicker;

    public DialogNhanVien(Component parent, Object[] data) {
        super(SwingUtilities.windowForComponent(parent), "Thông Tin Nhân Viên", ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.isEdit = (data != null);
        this.nhanVienDAO = new NhanVienDAO();

        initComponents();
        if (isEdit) {
            fillData(data);
        } else {

            generateNewMaNV();
        }
    }

    private void initComponents() {
        setLayout(new MigLayout("wrap,fillx,insets 20, width 500", "[label, 100]10[grow,fill]", "[]15[]"));

        JLabel lbTitle = new JLabel(isEdit ? "SỬA THÔNG TIN NHÂN VIÊN" : "THÊM NHÂN VIÊN MỚI");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:$Accent.color");
        add(lbTitle, "span 2, center, wrap 20");

        txtMaNV = new JTextField();
        txtMaNV.setEditable(false);
        txtMaNV.putClientProperty(FlatClientProperties.STYLE, "font:bold; foreground:#808080");
        add(new JLabel("Mã NV:"));
        add(txtMaNV);

        txtHoTen = new JTextField();
        txtHoTen.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ví dụ: Lê Hoàng (Viết hoa chữ cái đầu)");
        add(new JLabel("Họ tên:"));
        add(txtHoTen);

        cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"});

        txtNgaySinh = new JFormattedTextField();
        datePicker = new DatePicker();
        datePicker.setEditor(txtNgaySinh);
        datePicker.setDateFormat("dd/MM/yyyy");
        datePicker.setCloseAfterSelected(true);
        datePicker.setDateSelectionMode(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);

        add(new JLabel("Giới tính:"));
        add(cbGioiTinh, "split 3, w 100!");
        add(new JLabel("Ngày sinh:"), "gapleft 10");
        add(txtNgaySinh, "grow");

        txtSDT = new JTextField();
        txtSDT.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "10 số, bắt đầu bằng 0");

        txtEmail = new JTextField();
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "@gmail.com hoặc @yahoo.com");

        add(new JLabel("Số ĐT:"));
        add(txtSDT);

        add(new JLabel("Email:"));
        add(txtEmail);

        cbVaiTro = new JComboBox<>(new String[]{"Quản lý", "Nhân viên bán hàng"});
        cbTrangThai = new JComboBox<>(new String[]{"Đang làm", "Đã nghỉ"});

        add(new JLabel("Vai trò:"));
        add(cbVaiTro, "split 3, grow");
        add(new JLabel("Trạng thái:"), "gapleft 10");
        add(cbTrangThai, "grow");

        txtDiaChi = new JTextArea(3, 0);
        JScrollPane scrollAddr = new JScrollPane(txtDiaChi);
        add(new JLabel("Địa chỉ:"));
        add(scrollAddr);

        JPanel pFooter = new JPanel(new MigLayout("insets 20 0 0 0", "push[][]"));
        JButton btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> dispose());

        JButton btnLuu = new JButton("Lưu");
        btnLuu.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold");
        btnLuu.addActionListener(e -> {
            try {
                actionSave();
            } catch (SQLException ex) {
                Logger.getLogger(DialogNhanVien.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        pFooter.add(btnHuy);
        pFooter.add(btnLuu);
        add(pFooter, "span 2");

        pack();
        setLocationRelativeTo(parent);
    }

    private void generateNewMaNV() {
        ArrayList<NhanVien> list = nhanVienDAO.getAllTblNhanVien();
        if (list.isEmpty()) {
            txtMaNV.setText("NV001");
        } else {

            String lastID = list.get(list.size() - 1).getMaNV();
            try {
                int number = Integer.parseInt(lastID.substring(2));
                number++;
                txtMaNV.setText(String.format("NV%03d", number));
            } catch (Exception e) {
                txtMaNV.setText("NV" + (list.size() + 1));
            }
        }
    }

    private void fillData(Object[] data) {

        txtMaNV.setText(data[0].toString());
        txtHoTen.setText(data[1].toString());
        cbGioiTinh.setSelectedItem(data[2].toString());
        txtSDT.setText(data[3].toString());
        txtEmail.setText(data[4].toString());
        cbVaiTro.setSelectedItem(data[5].toString());
        cbTrangThai.setSelectedItem(data[6].toString());

        if (data[7] != null && data[7] instanceof LocalDate) {
            datePicker.setSelectedDate((LocalDate) data[7]);
        }
        if (data[8] != null) {
            txtDiaChi.setText(data[8].toString());
        }
    }

    private boolean validateInput() {

        String hoTen = txtHoTen.getText().trim();

        String nameRegex = "^([A-Z][a-z]*\\s+)+[A-Z][a-z]*$";

        if (hoTen.isEmpty() || !hoTen.matches(nameRegex)) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Họ tên không hợp lệ! (Phải có ít nhất 2 từ, viết hoa chữ cái đầu)");
            txtHoTen.requestFocus();
            return false;
        }

        if (!datePicker.isDateSelected()) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Vui lòng chọn ngày sinh!");
            txtNgaySinh.requestFocus();
            return false;
        }

        LocalDate dob = datePicker.getSelectedDate();
        if (dob.plusYears(18).isAfter(LocalDate.now())) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Nhân viên phải đủ 18 tuổi!");
            return false;
        }

        String sdt = txtSDT.getText().trim();
        if (!sdt.matches("^0\\d{9}$")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0!");
            txtSDT.requestFocus();
            return false;
        }

        String email = txtEmail.getText().trim();
        if (!email.endsWith("@gmail.com") && !email.endsWith("@yahoo.com")) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Email phải có đuôi @gmail.com hoặc @yahoo.com!");
            txtEmail.requestFocus();
            return false;
        }

        return true;
    }

    private void actionSave() throws SQLException {
        if (!validateInput()) {
            return;
        }

        NhanVien nv = new NhanVien();
        nv.setMaNV(txtMaNV.getText());
        nv.setHoTen(txtHoTen.getText().trim());
        nv.setGioiTinh(cbGioiTinh.getSelectedItem().toString().equals("Nam"));
        nv.setNgaySinh(datePicker.getSelectedDate());
        nv.setSdt(txtSDT.getText().trim());
        nv.setEmail(txtEmail.getText().trim());
        nv.setDiaChi(txtDiaChi.getText().trim());
        nv.setVaiTro(cbVaiTro.getSelectedItem().toString().equals("Quản lý"));
        nv.setTrangThai(cbTrangThai.getSelectedItem().toString().equals("Đang làm"));

        if (isEdit) {

            if (nhanVienDAO.update(nv)) {

                isSave = true;
                dispose();
            }
        } else {

            nv.setMatKhau("123456");
            if (nhanVienDAO.insert(nv)) {

                isSave = true;
                dispose();
            }
        }
    }

    public boolean isSave() {
        return isSave;
    }

    public Object[] getData() {
        return new Object[]{
            txtMaNV.getText(),
            txtHoTen.getText().trim(),
            cbGioiTinh.getSelectedItem(),
            txtSDT.getText().trim(),
            txtEmail.getText().trim(),
            cbVaiTro.getSelectedItem(),
            cbTrangThai.getSelectedItem()
        };
    }
}
