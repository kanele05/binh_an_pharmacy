package gui.main;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.UIScale;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import gui.main.Application;
import gui.transaction.sales.FormBanHang;
import gui.report.FormBaoCaoDoanhThu;
import gui.report.FormBaoCaoTonKho;
import gui.report.FormCanhBaoHetHan;
import gui.report.FormCanhBaoTonKho;
import gui.transaction.inventory.FormDanhSachPhieuNhap;
import gui.transaction.order.FormDatThuoc;
import gui.transaction.order.FormDoiHang;
import gui.profile.FormHoSo;
import gui.manage.partner.FormKhachHang;
import gui.manage.partner.FormNhaCungCap;
import gui.transaction.inventory.FormNhapHang;
import gui.manage.product.FormQuanLyLoThuoc;
import gui.manage.product.FormQuanLyThuoc;
import gui.transaction.inventory.FormTraHang;
import gui.dashboard.FormDashboard;
import gui.components.menu.Menu;
import gui.components.menu.MenuAction;
import gui.manage.price.FormBangGia;
import gui.manage.staff.FormNhanVien;
import gui.help.FormHuongDan;
import utils.MenuPermission;
import raven.toast.Notifications;

public class MainForm extends JLayeredPane {

    public MainForm() {
        init();
    }

    private void init() {
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new MainFormLayout());
        menu = new Menu();
        panelBody = new JPanel(new BorderLayout());

        initMenuEvent();

        add(menu);
        add(panelBody);
    }

    @Override
    public void applyComponentOrientation(ComponentOrientation o) {
        super.applyComponentOrientation(o);

    }

    private void initMenuEvent() {
        menu.addMenuEvent((int index, int subIndex, MenuAction action) -> {

            if (index == 0) {
                Application.showForm(new FormDashboard());
            } else if (index == 1) {
                if (subIndex == 1) {
                    Application.showForm(new FormBanHang());
                } else if (subIndex == 2) {
                    Application.showForm(new FormDatThuoc());
                } else if (subIndex == 3) {
                    Application.showForm(new FormTraHang());
                } else if (subIndex == 4) {
                    Application.showForm(new FormDoiHang());
                } else if (subIndex == 5) {
                    Application.showForm(new FormBaoCaoDoanhThu());
                } else {
                    action.cancel();
                }
            } else if (index == 2) {
                if (subIndex == 1) {
                    Application.showForm(new FormQuanLyThuoc());
                } else if (subIndex == 2) {
                    FormQuanLyThuoc form = new FormQuanLyThuoc();
                    Application.showForm(form);
                    form.openThemMoi();
                } else if (subIndex == 3) {
                    Application.showForm(new FormCanhBaoHetHan());
                } else if (subIndex == 4) {
                    Application.showForm(new FormCanhBaoTonKho());
                }
            } else if (index == 3) {
                if (subIndex == 1) {
                    Application.showForm(new FormDanhSachPhieuNhap());
                } else if (subIndex == 2) {
                    Application.showForm(new FormNhapHang());
                }
            } else if (index == 4) {
                if (subIndex == 1) {
                Application.showForm(new FormQuanLyLoThuoc());
                }else if (subIndex == 2) {
                    Application.showForm(new FormBaoCaoTonKho());
                }
            } else if (index == 5) {
                if (subIndex == 1) {
                    Application.showForm(new FormBangGia());
                } else if (subIndex == 2) {
                    FormBangGia form = new FormBangGia();
                    Application.showForm(form);
                    form.openThemMoi();
                }
            } else if (index == 6) {
                if (subIndex == 1) {
                    Application.showForm(new FormNhanVien());
                } else if (subIndex == 2) {
                    FormNhanVien form = new FormNhanVien();
                    Application.showForm(form);
                    form.openThemMoi();
                }
            } else if (index == 7) {
                if (subIndex == 1) {
                    Application.showForm(new FormKhachHang());
                } else if (subIndex == 2) {
                    FormKhachHang form = new FormKhachHang();
                    Application.showForm(form);
                    form.openThemMoi();
                }
            } else if (index == 8) {
                Application.showForm(new FormNhaCungCap());
            } else if (index == 9) {
                if (subIndex == 1) {
                    Application.showForm(new FormHoSo());
                } else if (subIndex == 2) {
                    Application.logout();
                } else if (subIndex == 3) {
                    Application.showForm(new FormHuongDan());
                }
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

    /**
     * Làm mới menu theo quyền của người dùng hiện tại
     * Gọi sau khi đăng nhập
     */
    public void refreshMenuByPermission() {
        menu.refreshMenuByPermission();
    }

    private Menu menu;
    private JPanel panelBody;
    private JButton menuButton;

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

                int menubX;
                if (ltr) {

                } else {

                }

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
