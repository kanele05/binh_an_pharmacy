package gui.help;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import raven.toast.Notifications;

/**
 * Form hướng dẫn sử dụng - Mở file HTML hướng dẫn trong trình duyệt
 */
public class FormHuongDan extends JPanel {

    public FormHuongDan() {
        initComponents();
        openUserGuide();
    }

    private void initComponents() {
        setLayout(new MigLayout("wrap,fill,insets 20", "[center]", "[center]"));
        putClientProperty(FlatClientProperties.STYLE, "background:@background");

        JLabel lbLoading = new JLabel("Đang mở hướng dẫn sử dụng...");
        lbLoading.putClientProperty(FlatClientProperties.STYLE, "font:bold +4");
        add(lbLoading);

        JLabel lbInfo = new JLabel("Nếu trình duyệt không tự động mở, vui lòng kiểm tra trình duyệt mặc định.");
        lbInfo.putClientProperty(FlatClientProperties.STYLE, "foreground:$Text.secondary");
        add(lbInfo);

        JButton btnOpenManual = new JButton("Mở lại hướng dẫn");
        btnOpenManual.putClientProperty(FlatClientProperties.STYLE,
            "background:#2196F3; foreground:#fff; font:bold");
        btnOpenManual.addActionListener(e -> openUserGuide());
        add(btnOpenManual, "gapy 20");
    }

    /**
     * Mở file HTML hướng dẫn trong trình duyệt mặc định
     */
    private void openUserGuide() {
        try {
            // Tạo file tạm từ resources
            File tempFile = extractHtmlToTemp();

            if (tempFile != null && tempFile.exists()) {
                // Mở file trong trình duyệt mặc định
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(tempFile.toURI());
                        Notifications.getInstance().show(
                            Notifications.Type.SUCCESS,
                            Notifications.Location.TOP_CENTER,
                            "Đã mở hướng dẫn sử dụng trong trình duyệt!"
                        );
                    } else if (desktop.isSupported(Desktop.Action.OPEN)) {
                        desktop.open(tempFile);
                    }
                } else {
                    // Fallback cho các hệ điều hành không hỗ trợ Desktop
                    openBrowserFallback(tempFile.toURI());
                }
            } else {
                Notifications.getInstance().show(
                    Notifications.Type.ERROR,
                    Notifications.Location.TOP_CENTER,
                    "Không tìm thấy file hướng dẫn!"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.getInstance().show(
                Notifications.Type.ERROR,
                Notifications.Location.TOP_CENTER,
                "Lỗi khi mở hướng dẫn: " + e.getMessage()
            );
        }
    }

    /**
     * Extract file HTML từ resources vào thư mục temp
     */
    private File extractHtmlToTemp() {
        try {
            // Đọc file từ resources
            InputStream is = getClass().getResourceAsStream("/resources/help/user_guide.html");

            if (is == null) {
                // Thử đường dẫn khác
                is = getClass().getClassLoader().getResourceAsStream("resources/help/user_guide.html");
            }

            if (is == null) {
                // Fallback: tìm file trong thư mục hiện tại
                String userDir = System.getProperty("user.dir");
                File localFile = new File(userDir + File.separator + "src" + File.separator +
                    "resources" + File.separator + "help" + File.separator + "user_guide.html");
                if (localFile.exists()) {
                    return localFile;
                }
                return null;
            }

            // Tạo file tạm
            File tempFile = File.createTempFile("user_guide_", ".html");
            tempFile.deleteOnExit();

            // Ghi nội dung vào file tạm
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            is.close();

            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fallback để mở trình duyệt trên các hệ điều hành khác nhau
     */
    private void openBrowserFallback(URI uri) {
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();

        try {
            if (os.contains("win")) {
                rt.exec("rundll32 url.dll,FileProtocolHandler " + uri.toString());
            } else if (os.contains("mac")) {
                rt.exec("open " + uri.toString());
            } else if (os.contains("nix") || os.contains("nux")) {
                String[] browsers = {"google-chrome", "firefox", "mozilla", "opera", "epiphany", "konqueror", "netscape"};
                StringBuilder cmd = new StringBuilder();
                for (int i = 0; i < browsers.length; i++) {
                    if (i > 0) cmd.append(" || ");
                    cmd.append(browsers[i]).append(" \"").append(uri.toString()).append("\"");
                }
                rt.exec(new String[]{"sh", "-c", cmd.toString()});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
