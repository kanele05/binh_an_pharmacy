package gui.main;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.*;
import javax.swing.*;

/**
 * Màn hình chờ (Splash Screen) hiển thị khi khởi động ứng dụng
 */
public class SplashScreen extends JWindow {

    private JProgressBar progressBar;
    private JLabel statusLabel;
    private Timer timer;
    private int progress = 0;
    private Runnable onComplete;

    public SplashScreen() {
        initComponents();
    }

    private void initComponents() {
        // Panel chính với gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Gradient từ xanh dương đậm đến xanh dương nhạt
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(41, 128, 185),
                    0, getHeight(), new Color(109, 213, 250)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(41, 128, 185), 2));

        // Panel trung tâm chứa logo và thông tin
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 20, 50));

        // Logo
        JLabel logoLabel = new JLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            logoLabel.setIcon(new FlatSVGIcon("resources/icon/svg/logo.svg", 120, 120));
        } catch (Exception e) {
            // Nếu không load được logo, hiển thị text thay thế
            logoLabel.setText("Bình An Pharmacy");
            logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        }

        // Tên ứng dụng
        JLabel titleLabel = new JLabel("NHÀ THUỐC BÌNH AN");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        // Phụ đề
        JLabel subtitleLabel = new JLabel("Hệ thống quản lý nhà thuốc");
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));

        // Thêm components vào center panel
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(logoLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createVerticalGlue());

        // Panel dưới chứa progress bar
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 30, 50));

        // Status label
        statusLabel = new JLabel("Đang khởi động...");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(255, 255, 255, 180));

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setPreferredSize(new Dimension(300, 6));
        progressBar.setMaximumSize(new Dimension(300, 6));
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(false);
        progressBar.setBackground(new Color(255, 255, 255, 50));
        progressBar.setForeground(Color.WHITE);

        // Style progress bar với FlatLaf
        progressBar.putClientProperty(FlatClientProperties.PROGRESS_BAR_LARGE_HEIGHT, 6);
        progressBar.putClientProperty(FlatClientProperties.STYLE,
            "arc: 10;" +
            "foreground: #FFFFFF;");

        // Version label
        JLabel versionLabel = new JLabel("Phiên bản 1.0.0");
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        versionLabel.setForeground(new Color(255, 255, 255, 120));

        bottomPanel.add(statusLabel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        bottomPanel.add(progressBar);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        bottomPanel.add(versionLabel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setSize(450, 350);
        setLocationRelativeTo(null);
    }

    /**
     * Bắt đầu animation loading trong khoảng 3 giây
     * @param onComplete callback khi hoàn thành
     */
    public void startLoading(Runnable onComplete) {
        this.onComplete = onComplete;
        progress = 0;

        // Các thông báo trạng thái
        String[] statusMessages = {
            "Đang khởi động...",
            "Đang tải cấu hình...",
            "Đang kết nối cơ sở dữ liệu...",
            "Đang tải giao diện...",
            "Đang hoàn tất..."
        };

        // Timer chạy mỗi 30ms, tổng cộng 100 lần = 3000ms = 3 giây
        timer = new Timer(30, e -> {
            progress++;
            progressBar.setValue(progress);

            // Cập nhật status message theo progress
            int messageIndex = Math.min(progress / 20, statusMessages.length - 1);
            statusLabel.setText(statusMessages[messageIndex]);

            if (progress >= 100) {
                timer.stop();
                // Delay nhỏ trước khi đóng
                Timer closeTimer = new Timer(200, evt -> {
                    dispose();
                    if (this.onComplete != null) {
                        this.onComplete.run();
                    }
                });
                closeTimer.setRepeats(false);
                closeTimer.start();
            }
        });
        timer.start();
    }

    /**
     * Hiển thị splash screen và bắt đầu loading
     * @param onComplete callback khi hoàn thành
     */
    public static void showSplash(Runnable onComplete) {
        SplashScreen splash = new SplashScreen();
        splash.setVisible(true);
        splash.startLoading(onComplete);
    }
}
