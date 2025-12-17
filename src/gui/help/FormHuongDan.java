package gui.help;

import com.formdev.flatlaf.FlatClientProperties;
import dao.DonViQuyDoiDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import net.miginfocom.swing.MigLayout;

public class FormHuongDan extends JPanel {

    private JEditorPane editorPane;
    private JTable tableQuyDoi;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField txtSearch;
    private DonViQuyDoiDAO dvDAO = new DonViQuyDoiDAO();

    public FormHuongDan() {
        setLayout(new BorderLayout());
        init();
        loadStaticGuide();
        loadTableData();
    }

    private void init() {

        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        JScrollPane scrollGuide = new JScrollPane(editorPane);
        scrollGuide.setBorder(BorderFactory.createTitledBorder("Nội dung hướng dẫn"));

        JPanel pnlTable = new JPanel(new BorderLayout(0, 5));
        pnlTable.setBorder(BorderFactory.createTitledBorder("Tra cứu quy đổi nhanh"));

        JPanel pnlSearch = new JPanel(new MigLayout("insets 0", "[]10[grow]", "[]"));
        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Gõ để tìm theo tên thuốc, mã thuốc, đơn vị...");
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc:10");

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = txtSearch.getText().trim();
                if (text.length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {

                    try {
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    } catch (Exception ex) {

                    }
                }
            }
        });

        pnlSearch.add(new JLabel("Tìm kiếm:"));
        pnlSearch.add(txtSearch, "growx");

        String[] headers = {"Mã Thuốc", "Tên Thuốc", "Đơn Vị", "SL Quy Đổi", "Giá Bán"};
        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {

                if (columnIndex == 3) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        tableQuyDoi = new JTable(tableModel);
        tableQuyDoi.setRowHeight(30);
        tableQuyDoi.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tableQuyDoi.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tableQuyDoi.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        rowSorter = new TableRowSorter<>(tableModel);
        tableQuyDoi.setRowSorter(rowSorter);

        JScrollPane scrollTable = new JScrollPane(tableQuyDoi);

        pnlTable.add(pnlSearch, BorderLayout.NORTH);
        pnlTable.add(scrollTable, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollGuide, pnlTable);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.2);

        add(splitPane, BorderLayout.CENTER);
    }

    private void loadStaticGuide() {
        try {
            InputStream is = getClass().getResourceAsStream("/resources/help/user_guide.html");
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String content = reader.lines().collect(Collectors.joining("\n"));

                    content = content.replace("{{DYNAMIC_CONVERSION_TABLE}}", "");
                    editorPane.setText(content);
                    editorPane.setCaretPosition(0);
                }
            } else {
                editorPane.setText("<html><body><h2>Không tìm thấy tài liệu hướng dẫn.</h2></body></html>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTableData() {
        ArrayList<Object[]> list = dvDAO.getBangQuyDoiDayDu();
        DecimalFormat df = new DecimalFormat("#,##0 ₫");

        tableModel.setRowCount(0);
        for (Object[] row : list) {

            row[4] = df.format(row[4]);
            tableModel.addRow(row);
        }
    }
}
