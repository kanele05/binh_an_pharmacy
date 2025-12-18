package gui.transaction.sales;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.formdev.flatlaf.FlatClientProperties;
import dao.HoaDonDAO;
import entities.HoaDon;
import java.awt.BorderLayout;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;
import utils.TableRenderers;

/**
 * Panel hiển thị danh sách hóa đơn đã bán.
 * Được tách từ FormBanHang để giảm độ phức tạp.
 */
public class PanelDanhSachHoaDon extends JPanel {

    private JTable table;
    private JTextField txtSearch;
    private EventList<HoaDon> sourceList;
    private FilterList<HoaDon> filterList;
    private EventTableModel<HoaDon> tableModel;
    private HoaDonDAO hoaDonDAO;
    private FormBanHang parentForm;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PanelDanhSachHoaDon(FormBanHang parentForm) {
        this.parentForm = parentForm;
        this.hoaDonDAO = new HoaDonDAO();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new MigLayout("wrap,fill,insets 20", "[fill]", "[][][grow]"));

        JLabel lbTitle = new JLabel("Lịch Sử Bán Hàng");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +8");
        add(lbTitle, "wrap 20");

        add(createToolBar(), "wrap 10");
        add(createTable(), "grow");
    }

    private JPanel createToolBar() {
        JPanel panel = new JPanel(new MigLayout("insets 10", "[]10[]10[]10[]push[]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");

        JLabel lbSearch = new JLabel("Tìm kiếm:");
        lbSearch.putClientProperty(FlatClientProperties.STYLE, "font:bold");

        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mã HĐ, tên khách, tên nhân viên...");

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadData();
        });

        JButton btnXemChiTiet = new JButton("Xem chi tiết");
        btnXemChiTiet.putClientProperty(FlatClientProperties.STYLE, "background:#2196F3; foreground:#fff; font:bold");
        btnXemChiTiet.addActionListener(e -> actionXemChiTiet());

        JButton btnBanHang = new JButton("+ Tạo hóa đơn mới");
        btnBanHang.putClientProperty(FlatClientProperties.STYLE, "background:#4CAF50; foreground:#fff; font:bold");
        btnBanHang.addActionListener(e -> parentForm.showBanHang());

        panel.add(lbSearch);
        panel.add(txtSearch, "w 300");
        panel.add(btnRefresh);
        panel.add(btnXemChiTiet);
        panel.add(btnBanHang);
        return panel;
    }

    private void actionXemChiTiet() {
        int row = table.getSelectedRow();
        if (row == -1) {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER,
                    "Vui lòng chọn hóa đơn để xem!");
            return;
        }

        HoaDon selectedHD = filterList.get(row);
        DialogChiTietHoaDon dialog = new DialogChiTietHoaDon(SwingUtilities.getWindowAncestor(this), selectedHD);
        dialog.setVisible(true);
    }

    private JPanel createTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        sourceList = new BasicEventList<>();

        TextFilterator<HoaDon> textFilterator = new TextFilterator<HoaDon>() {
            @Override
            public void getFilterStrings(List<String> list, HoaDon hd) {
                list.add(hd.getMaHD());
                list.add(hd.getNhanVien().getHoTen());
                if (hd.getKhachHang() != null) {
                    list.add(hd.getKhachHang().getTenKH());
                    list.add(hd.getKhachHang().getSdt());
                } else {
                    list.add("Khách lẻ");
                }
                list.add(hd.getNgayTao().format(dateTimeFormat));
            }
        };

        TextComponentMatcherEditor<HoaDon> matcherEditor = new TextComponentMatcherEditor<>(txtSearch, textFilterator);
        filterList = new FilterList<>(sourceList, matcherEditor);

        TableFormat<HoaDon> tableFormat = new TableFormat<HoaDon>() {
            private final String[] cols = {"Mã HĐ", "Ngày Bán", "Khách Hàng", "Tổng Thu", "Thanh Toán", "Người Bán"};

            @Override
            public int getColumnCount() {
                return cols.length;
            }

            @Override
            public String getColumnName(int i) {
                return cols[i];
            }

            @Override
            public Object getColumnValue(HoaDon hd, int i) {
                switch (i) {
                    case 0: return hd.getMaHD();
                    case 1: return hd.getNgayTao();
                    case 2: return (hd.getKhachHang() != null) ? hd.getKhachHang().getTenKH() : "Khách lẻ";
                    case 3: return hd.getTongTien() + hd.getThue() - hd.getGiamGia();
                    case 4: return hd.getHinhThucTT();
                    case 5: return hd.getNhanVien().getHoTen();
                    default: return null;
                }
            }
        };

        tableModel = new EventTableModel<>(filterList, tableFormat);
        table = new JTable(tableModel);

        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:30; showHorizontalLines:true");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:35; font:bold");

        // Use shared renderers
        table.getColumnModel().getColumn(0).setCellRenderer(new TableRenderers.CenterRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new TableRenderers.DateTimeRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new TableRenderers.CurrencyRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new TableRenderers.CenterRenderer());

        panel.add(new JScrollPane(table));
        return panel;
    }

    public void loadData() {
        sourceList.clear();
        sourceList.addAll(hoaDonDAO.getAllHoaDon());
    }
}
