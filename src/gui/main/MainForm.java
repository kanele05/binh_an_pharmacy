package gui.main;

import com.formdev.flatlaf.util.UIScale;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import gui.components.menu.Menu;
import gui.components.menu.MenuAction;
import gui.dashboard.FormDashboard;
import gui.help.FormHuongDan;
import gui.manage.partner.FormKhachHang;
import gui.manage.partner.FormNhaCungCap;
import gui.manage.price.FormBangGia;
import gui.manage.product.FormQuanLyLoThuoc;
import gui.manage.product.FormQuanLyThuoc;
import gui.manage.staff.FormNhanVien;
import gui.profile.FormHoSo;
import gui.report.FormBaoCaoDoanhThu;
import gui.report.FormBaoCaoTonKho;
import gui.report.FormCanhBaoHetHan;
import gui.report.FormCanhBaoTonKho;
import gui.transaction.inventory.FormDanhSachPhieuNhap;
import gui.transaction.inventory.FormNhapHang;
import gui.transaction.inventory.FormTraHang;
import gui.transaction.order.FormDatThuoc;
import gui.transaction.order.FormDoiHang;
import gui.transaction.sales.FormBanHang;

/**
 * Main form chứa menu và panel nội dung.
 * Refactored: Sử dụng MenuRouter thay vì nested if-else.
 */
public class MainForm extends JLayeredPane {

    private Menu menu;
    private JPanel panelBody;

    public MainForm() {
        init();
    }

    private void init() {
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new MainFormLayout());
        menu = new Menu();
        panelBody = new JPanel(new BorderLayout());

        initMenuRoutes();

        add(menu);
        add(panelBody);
    }

    @Override
    public void applyComponentOrientation(ComponentOrientation o) {
        super.applyComponentOrientation(o);
    }

    /**
     * Định nghĩa routing cho menu sử dụng Map thay vì if-else.
     */
    private void initMenuRoutes() {
        MenuRouter router = new MenuRouter();

        // 0: Dashboard
        router.register(0, 1, FormDashboard::new);

        // 1: Giao dịch
        router.register(1, 1, FormBanHang::new);
        router.register(1, 2, FormDatThuoc::new);
        router.register(1, 3, FormTraHang::new);
        router.register(1, 4, FormDoiHang::new);
        router.register(1, 5, FormBaoCaoDoanhThu::new);

        // 2: Quản lý thuốc
        router.register(2, 1, FormQuanLyThuoc::new);
        router.register(2, 2, () -> {
            FormQuanLyThuoc form = new FormQuanLyThuoc();
            form.openThemMoi();
            return form;
        });
        router.register(2, 3, FormCanhBaoHetHan::new);
        router.register(2, 4, FormCanhBaoTonKho::new);

        // 3: Nhập hàng
        router.register(3, 1, FormDanhSachPhieuNhap::new);
        router.register(3, 2, FormNhapHang::new);

        // 4: Tồn kho
        router.register(4, 1, FormQuanLyLoThuoc::new);
        router.register(4, 2, FormBaoCaoTonKho::new);

        // 5: Bảng giá
        router.register(5, 1, FormBangGia::new);
        router.register(5, 2, () -> {
            FormBangGia form = new FormBangGia();
            form.openThemMoi();
            return form;
        });

        // 6: Nhân viên
        router.register(6, 1, FormNhanVien::new);
        router.register(6, 2, () -> {
            FormNhanVien form = new FormNhanVien();
            form.openThemMoi();
            return form;
        });

        // 7: Khách hàng
        router.register(7, 1, FormKhachHang::new);
        router.register(7, 2, () -> {
            FormKhachHang form = new FormKhachHang();
            form.openThemMoi();
            return form;
        });

        // 8: Nhà cung cấp
        router.register(8, 0, FormNhaCungCap::new);

        // 9: Tài khoản
        router.register(9, 1, FormHoSo::new);
        router.registerAction(9, 2, Application::logout);
        router.register(9, 3, FormHuongDan::new);

        // Đăng ký event handler
        menu.addMenuEvent((int index, int subIndex, MenuAction action) -> {
            if (!router.handle(index, subIndex)) {
                action.cancel();
            }
        });
    }

    public void hideMenu() {
        menu.hideMenuItem();
    }

    public void showForm(Component component) {
        panelBody.removeAll();
        panelBody.add(component);
        panelBody.repaint();
        panelBody.revalidate();
    }

    public void setSelectedMenu(int index, int subIndex) {
        menu.setSelectedMenu(index, subIndex);
    }

    public void refreshMenuByPermission() {
        menu.refreshMenuByPermission();
    }

    /**
     * Router quản lý điều hướng menu.
     * Sử dụng Map để lưu mapping giữa (index, subIndex) và form factory.
     */
    private static class MenuRouter {
        private final Map<String, Supplier<? extends JComponent>> formFactories = new HashMap<>();
        private final Map<String, Runnable> actions = new HashMap<>();

        private String key(int index, int subIndex) {
            return index + ":" + subIndex;
        }

        public void register(int index, int subIndex, Supplier<? extends JComponent> factory) {
            formFactories.put(key(index, subIndex), factory);
        }

        public void registerAction(int index, int subIndex, Runnable action) {
            actions.put(key(index, subIndex), action);
        }

        public boolean handle(int index, int subIndex) {
            String k = key(index, subIndex);

            // Check for actions first
            if (actions.containsKey(k)) {
                actions.get(k).run();
                return true;
            }

            // Check for form factories
            if (formFactories.containsKey(k)) {
                Application.showForm(formFactories.get(k).get());
                return true;
            }

            return false;
        }
    }

    /**
     * Custom layout manager for main form.
     */
    private class MainFormLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                return new Dimension(5, 5);
            }
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                return new Dimension(0, 0);
            }
        }

        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                boolean ltr = parent.getComponentOrientation().isLeftToRight();
                Insets insets = UIScale.scale(parent.getInsets());
                int x = insets.left;
                int y = insets.top;
                int width = parent.getWidth() - (insets.left + insets.right);
                int height = parent.getHeight() - (insets.top + insets.bottom);
                int menuWidth = UIScale.scale(menu.isMenuFull() ? menu.getMenuMaxWidth() : menu.getMenuMinWidth());
                int menuX = ltr ? x : x + width - menuWidth;
                menu.setBounds(menuX, y, menuWidth, height);

                int gap = UIScale.scale(5);
                int bodyWidth = width - menuWidth - gap;
                int bodyHeight = height;
                int bodyx = ltr ? (x + menuWidth + gap) : x;
                int bodyy = y;
                panelBody.setBounds(bodyx, bodyy, bodyWidth, bodyHeight);
            }
        }
    }
}
