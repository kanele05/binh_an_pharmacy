package gui.manage.partner;

import com.formdev.flatlaf.FlatClientProperties;
import dao.NhaCungCapDAO;

import entities.NhaCungCap;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

import com.formdev.flatlaf.FlatClientProperties;
import dao.NhaCungCapDAO;
import entities.NhaCungCap;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

public class FormNhaCungCap extends JPanel {

    private JTextField txtTimKiem;
    private JTable table;
    private DefaultTableModel model;
    private NhaCungCapDAO nccDao = new NhaCungCapDAO();
    private javax.swing.Timer searchTimer;
    public FormNhaCungCap() {
        initComponents();
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow]"));

        // 1. Header
        add(createHeaderPanel(), "wrap 20");

        // 2. Toolbar
        add(createToolBarPanel(), "wrap 10");

        // 3. Table
        add(createTablePanel(), "grow");

        loadData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow,fill][]"));
        panel.setOpaque(false);
        JLabel lbTitle = new JLabel("Danh Sách Nhà Cung Cấp");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        panel.add(lbTitle);
        return panel;
    }

    private JPanel createToolBarPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]push[][]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:darken(@background,3%)");

        txtTimKiem = new JTextField();
        txtTimKiem.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm tên NCC, SĐT, Email...");
        
        JButton btnTim = new JButton("Tìm kiếm");
        txtTimKiem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                actionTimRealtime();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                actionTimRealtime();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                actionTimRealtime();
            }
        });

        JButton btnThem = new JButton("Thêm mới");
        btnThem.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold");
        btnThem.addActionListener(e -> actionThem());

        JButton btnSua = new JButton("Sửa");
        btnSua.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
        btnSua.addActionListener(e -> actionSua());
        
        JButton btnXoa = new JButton("Xóa");
        btnXoa.putClientProperty(FlatClientProperties.STYLE, "background:#F44336; foreground:#fff; font:bold");
        btnXoa.addActionListener(e -> actionXoa());

        panel.add(txtTimKiem, "w 250");
        panel.add(btnTim);
        
        panel.add(btnThem);
        panel.add(btnSua);
        panel.add(btnXoa);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] columns = {"Mã NCC", "Tên Nhà Cung Cấp", "Số Điện Thoại", "Email", "Địa Chỉ"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");
        
        panel.add(new JScrollPane(table));
        return panel;
    }

    private void loadData() {
        model.setRowCount(0);
        ArrayList<NhaCungCap> list = nccDao.getAllNhaCungCap();
        
        for(NhaCungCap ncc : list){
            String maNcc = ncc.getMaNCC();
            String tenNcc = ncc.getTenNCC();
            String sdt = ncc.getSdt();
            String email = ncc.getEmail();
            String diaChi = ncc.getDiaChi();
            
            model.addRow(new Object[]{
                maNcc,
                tenNcc,
                sdt,
                email,
                diaChi
            });
        }
    }

    private void actionThem() {
        DialogNhaCungCap dialog = new DialogNhaCungCap(this, null);
        dialog.setVisible(true);
        String maNCC = nccDao.getNewMaNCC();
        dialog.setMaNCC(maNCC);
        if (!dialog.isSave()) {
            return; 
        }
        NhaCungCap ncc = dialog.getNhaCungCap();
        if (nccDao.insert(ncc)) {
            model.addRow(new Object[]{
                ncc.getMaNCC(),
                ncc.getTenNCC(),
                ncc.getSdt(),
                ncc.getEmail(),
                ncc.getDiaChi()
            });

            Notifications.getInstance().show(
                Notifications.Type.SUCCESS,
                Notifications.Location.TOP_CENTER,
                "Thêm nhà cung cấp thành công!"
            );
        } else {
            Notifications.getInstance().show(
                Notifications.Type.ERROR,
                Notifications.Location.TOP_CENTER,
                "Thêm thất bại! Mã NCC có thể đã tồn tại."
            );
        }
    }


    private void actionSua() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(
                Notifications.Type.WARNING,
                Notifications.Location.TOP_CENTER,
                "Chọn nhà cung cấp cần sửa!"
            );
            return;
        }
        Object[] oldData = new Object[model.getColumnCount()];
        for (int i = 0; i < model.getColumnCount(); i++) {
            oldData[i] = model.getValueAt(row, i);
        }

        DialogNhaCungCap dialog = new DialogNhaCungCap(this, oldData);
        dialog.setVisible(true);

        if (!dialog.isSave()) return;

        Object[] newData = dialog.getData();
        if (nccDao.update(newData)) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                model.setValueAt(newData[i], row, i);
            }

            Notifications.getInstance().show(
                Notifications.Type.SUCCESS,
                Notifications.Location.TOP_CENTER,
                "Cập nhật nhà cung cấp thành công!"
            );
        } else {
            Notifications.getInstance().show(
                Notifications.Type.ERROR,
                Notifications.Location.TOP_CENTER,
                "Cập nhật thất bại!"
            );
        }
    }


    private void actionXoa() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(
                Notifications.Type.WARNING,
                Notifications.Location.TOP_CENTER,
                "Chọn nhà cung cấp cần xóa!"
            );
            return;
        }

        String maNCC = model.getValueAt(row, 0).toString();
        String tenNCC = model.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Xóa nhà cung cấp: " + tenNCC +
            "?\nLưu ý: Không thể xóa nếu đã có lịch sử nhập hàng.",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;
        if (nccDao.delete(maNCC)) {
            model.removeRow(row);
            Notifications.getInstance().show(
                Notifications.Type.SUCCESS,
                Notifications.Location.TOP_CENTER,
                "Đã xóa nhà cung cấp!"
            );
        } else {
            Notifications.getInstance().show(
                Notifications.Type.ERROR,
                Notifications.Location.TOP_CENTER,
                "Không thể xóa nhà cung cấp này!"
            );
        }
    }


    private void actionTimRealtime() {
        if (searchTimer != null && searchTimer.isRunning()) {
            searchTimer.stop();
        }

        searchTimer = new javax.swing.Timer(300, e -> {
            String keyword = txtTimKiem.getText().trim();
            model.setRowCount(0);

            ArrayList<NhaCungCap> list =
                    keyword.isEmpty()
                    ? nccDao.getAllNhaCungCap()
                    : nccDao.searchNhaCungCap(keyword);

            for (NhaCungCap ncc : list) {
                model.addRow(new Object[]{
                    ncc.getMaNCC(),
                    ncc.getTenNCC(),
                    ncc.getSdt(),
                    ncc.getEmail(),
                    ncc.getDiaChi()
                });
            }
        });
        searchTimer.setRepeats(false);
        searchTimer.start();
    }

    @SuppressWarnings("unchecked")

    private void initComponents() {
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE)
        );
    }
}
