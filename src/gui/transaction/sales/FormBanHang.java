package gui.transaction.sales;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Form chính quản lý bán hàng.
 * Sử dụng CardLayout để chuyển đổi giữa:
 * - Danh sách hóa đơn (LIST)
 * - Giao diện bán hàng POS (POS)
 *
 * Refactored: Các inner class đã được tách ra thành các file riêng:
 * - PanelDanhSachHoaDon.java
 * - PanelBanHangGiaoDien.java
 * - UnitCellEditor.java
 * - TableRenderers.java (utils)
 */
public class FormBanHang extends JPanel {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private PanelDanhSachHoaDon panelDanhSach;
    private PanelBanHangGiaoDien panelBanHang;

    public FormBanHang() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        panelDanhSach = new PanelDanhSachHoaDon(this);
        panelBanHang = new PanelBanHangGiaoDien(this);

        mainPanel.add(panelDanhSach, "LIST");
        mainPanel.add(panelBanHang, "POS");

        add(mainPanel, BorderLayout.CENTER);

        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;

        // F1: Mở màn hình bán hàng
        registerKeyboardAction("openSales", KeyEvent.VK_F1, this::showBanHang);

        // F2: Focus ô tìm kiếm thuốc
        registerKeyboardAction("focusSearch", KeyEvent.VK_F2, () -> panelBanHang.focusTimKiem());

        // F3: Thêm thuốc vào giỏ hàng
        registerKeyboardAction("addToCart", KeyEvent.VK_F3, () -> panelBanHang.themVaoGioHangAction());

        // F4: Thanh toán
        registerKeyboardAction("payment", KeyEvent.VK_F4, () -> panelBanHang.thanhToanAction());

        // F5: Mở hướng dẫn sử dụng
        registerKeyboardAction("showHelp", KeyEvent.VK_F5, this::showHelpDialog);
    }

    private void registerKeyboardAction(String actionName, int keyCode, Runnable action) {
        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        getInputMap(condition).put(KeyStroke.getKeyStroke(keyCode, 0), actionName);
        getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void showHelpDialog() {
        String helpContent = "<html>" +
            "<body style='width: 350px; padding: 10px; font-family: Arial, sans-serif;'>" +
            "<h2 style='color: #4CAF50; text-align: center;'>Hướng Dẫn Sử Dụng Phím Tắt</h2>" +
            "<hr>" +
            "<table style='width: 100%; border-collapse: collapse;'>" +
            "<tr style='background-color: #E8F5E9;'>" +
                "<td style='padding: 8px; font-weight: bold;'>F1</td>" +
                "<td style='padding: 8px;'>Mở màn hình bán hàng</td>" +
            "</tr>" +
            "<tr>" +
                "<td style='padding: 8px; font-weight: bold;'>F2</td>" +
                "<td style='padding: 8px;'>Focus vào ô tìm kiếm thuốc</td>" +
            "</tr>" +
            "<tr style='background-color: #E8F5E9;'>" +
                "<td style='padding: 8px; font-weight: bold;'>F3</td>" +
                "<td style='padding: 8px;'>Thêm thuốc đang chọn vào giỏ hàng</td>" +
            "</tr>" +
            "<tr>" +
                "<td style='padding: 8px; font-weight: bold;'>F4</td>" +
                "<td style='padding: 8px;'>Thực hiện thanh toán</td>" +
            "</tr>" +
            "<tr style='background-color: #E8F5E9;'>" +
                "<td style='padding: 8px; font-weight: bold;'>F5</td>" +
                "<td style='padding: 8px;'>Mở hướng dẫn sử dụng (cửa sổ này)</td>" +
            "</tr>" +
            "<tr>" +
                "<td style='padding: 8px; font-weight: bold;'>Enter</td>" +
                "<td style='padding: 8px;'>Tìm kiếm khách hàng (trong ô SĐT)</td>" +
            "</tr>" +
            "</table>" +
            "<hr>" +
            "<p style='color: #666; font-size: 11px; text-align: center;'>" +
            "<i>Sử dụng phím tắt giúp bán hàng nhanh hơn!</i>" +
            "</p>" +
            "</body>" +
            "</html>";

        JOptionPane.showMessageDialog(
            SwingUtilities.getWindowAncestor(this),
            helpContent,
            "Hướng Dẫn Sử Dụng - Phím Tắt Bán Hàng",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void showBanHang() {
        panelBanHang.resetForm();
        cardLayout.show(mainPanel, "POS");
    }

    public void showDanhSach() {
        panelDanhSach.loadData();
        cardLayout.show(mainPanel, "LIST");
    }
}
