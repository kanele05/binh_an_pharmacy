package gui.help;

import dao.DonViQuyDoiDAO;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class FormHuongDan extends JPanel {

    private JEditorPane editorPane;

    public FormHuongDan() {
        setLayout(new BorderLayout());
        init();
    }

    private void init() {
        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");

        loadDynamicContent();

        JScrollPane scrollPane = new JScrollPane(editorPane);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadDynamicContent() {
        try {

            String content = "";
            InputStream is = getClass().getResourceAsStream("/resources/help/user_guide.html");
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    content = reader.lines().collect(Collectors.joining("\n"));
                }
            } else {
                editorPane.setText("<html><body><h1>Lỗi: Không tìm thấy file hướng dẫn!</h1></body></html>");
                return;
            }

            String tableHtml = generateConversionTable();

            content = content.replace("{{DYNAMIC_CONVERSION_TABLE}}", tableHtml);

            editorPane.setText(content);

            editorPane.setCaretPosition(0);

        } catch (Exception e) {
            e.printStackTrace();
            editorPane.setText("<html><body><h1>Lỗi khi tải hướng dẫn: " + e.getMessage() + "</h1></body></html>");
        }
    }

    private String generateConversionTable() {
        StringBuilder sb = new StringBuilder();
        DonViQuyDoiDAO dvDAO = new DonViQuyDoiDAO();
        ArrayList<Object[]> listData = dvDAO.getBangQuyDoiDayDu();
        DecimalFormat df = new DecimalFormat("#,##0 ₫");

        sb.append("<table class='table-conversion'>");
        sb.append("<thead>");
        sb.append("<tr>");
        sb.append("<th>Mã Thuốc</th>");
        sb.append("<th>Tên Thuốc</th>");
        sb.append("<th>Đơn Vị Quy Đổi</th>");
        sb.append("<th>Số Lượng (Cơ bản)</th>");
        sb.append("<th>Giá Bán</th>");
        sb.append("</tr>");
        sb.append("</thead>");
        sb.append("<tbody>");

        if (listData.isEmpty()) {
            sb.append("<tr><td colspan='5' style='text-align:center;'>Chưa có dữ liệu quy đổi nào.</td></tr>");
        } else {
            for (Object[] row : listData) {
                sb.append("<tr>");
                sb.append("<td>").append(row[0]).append("</td>");
                sb.append("<td>").append(row[1]).append("</td>");
                sb.append("<td><b>").append(row[2]).append("</b></td>");
                sb.append("<td style='text-align:center;'>").append(row[3]).append("</td>");
                sb.append("<td style='text-align:right;'>").append(df.format(row[4])).append("</td>");
                sb.append("</tr>");
            }
        }

        sb.append("</tbody>");
        sb.append("</table>");

        sb.append("<p><i>* Dữ liệu được cập nhật tự động từ hệ thống quản lý kho.</i></p>");

        return sb.toString();
    }
}
