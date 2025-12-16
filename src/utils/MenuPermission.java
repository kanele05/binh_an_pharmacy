package utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Quản lý phân quyền menu theo vai trò người dùng
 * - Admin (vaiTro = true): Có quyền truy cập tất cả chức năng
 * - Nhân viên (vaiTro = false): Chỉ có quyền truy cập một số chức năng cơ bản
 */
public class MenuPermission {

    // Map lưu trữ các menu bị ẩn theo vai trò
    // Key: menuIndex, Value: Set các subIndex bị ẩn (nếu rỗng = ẩn cả menu)
    private static final Map<Integer, Set<Integer>> STAFF_HIDDEN_MENUS = new HashMap<>();

    // Các menu index
    public static final int MENU_TRANG_CHU = 0;
    public static final int MENU_DON_HANG = 1;
    public static final int MENU_THUOC = 2;
    public static final int MENU_PHIEU_NHAP = 3;
    public static final int MENU_LO_THUOC = 4;
    public static final int MENU_BANG_GIA = 5;
    public static final int MENU_NHAN_VIEN = 6;
    public static final int MENU_KHACH_HANG = 7;
    public static final int MENU_NHA_CUNG_CAP = 8;
    public static final int MENU_HE_THONG = 9;

    static {
        // Cấu hình các menu bị ẩn đối với nhân viên (không phải admin)

        // Menu Thuốc: Nhân viên chỉ được xem danh mục, cảnh báo hết hạn và tồn kho
        // Ẩn "Thêm thuốc mới" (subIndex = 2)
        Set<Integer> thuocHidden = new HashSet<>();
        thuocHidden.add(2); // Thêm thuốc mới
        STAFF_HIDDEN_MENUS.put(MENU_THUOC, thuocHidden);

        // Menu Phiếu nhập: Nhân viên chỉ được xem danh sách
        // Ẩn "Lập phiếu nhập" (subIndex = 2)
        Set<Integer> phieuNhapHidden = new HashSet<>();
        phieuNhapHidden.add(2); // Lập phiếu nhập
        STAFF_HIDDEN_MENUS.put(MENU_PHIEU_NHAP, phieuNhapHidden);

        // Menu Bảng giá: Nhân viên chỉ được xem
        // Ẩn "Tạo bảng giá mới" (subIndex = 2)
        Set<Integer> bangGiaHidden = new HashSet<>();
        bangGiaHidden.add(2); // Tạo bảng giá mới
        STAFF_HIDDEN_MENUS.put(MENU_BANG_GIA, bangGiaHidden);

        // Menu Nhân viên: Ẩn hoàn toàn với nhân viên
        STAFF_HIDDEN_MENUS.put(MENU_NHAN_VIEN, new HashSet<>()); // Rỗng = ẩn cả menu

        // Menu Nhà cung cấp: Ẩn hoàn toàn với nhân viên
        STAFF_HIDDEN_MENUS.put(MENU_NHA_CUNG_CAP, new HashSet<>()); // Rỗng = ẩn cả menu
    }

    /**
     * Kiểm tra người dùng hiện tại có phải admin không
     */
    public static boolean isAdmin() {
        return Auth.isManager();
    }

    /**
     * Kiểm tra menu có được hiển thị không
     * @param menuIndex Index của menu chính
     * @return true nếu menu được hiển thị
     */
    public static boolean isMenuVisible(int menuIndex) {
        if (isAdmin()) {
            return true; // Admin thấy tất cả
        }

        // Nhân viên: kiểm tra menu có bị ẩn hoàn toàn không
        if (STAFF_HIDDEN_MENUS.containsKey(menuIndex)) {
            Set<Integer> hiddenSubs = STAFF_HIDDEN_MENUS.get(menuIndex);
            return !hiddenSubs.isEmpty(); // Nếu rỗng = ẩn cả menu
        }
        return true;
    }

    /**
     * Kiểm tra submenu có được hiển thị không
     * @param menuIndex Index của menu chính
     * @param subIndex Index của submenu
     * @return true nếu submenu được hiển thị
     */
    public static boolean isSubMenuVisible(int menuIndex, int subIndex) {
        if (isAdmin()) {
            return true; // Admin thấy tất cả
        }

        // Nhân viên: kiểm tra submenu có bị ẩn không
        if (STAFF_HIDDEN_MENUS.containsKey(menuIndex)) {
            Set<Integer> hiddenSubs = STAFF_HIDDEN_MENUS.get(menuIndex);
            if (hiddenSubs.isEmpty()) {
                return false; // Menu bị ẩn hoàn toàn
            }
            return !hiddenSubs.contains(subIndex);
        }
        return true;
    }

    /**
     * Kiểm tra người dùng có quyền truy cập chức năng không
     * @param menuIndex Index của menu
     * @param subIndex Index của submenu
     * @return true nếu có quyền
     */
    public static boolean hasPermission(int menuIndex, int subIndex) {
        return isSubMenuVisible(menuIndex, subIndex);
    }

    /**
     * Lọc menu items theo quyền
     * @param menuItems Mảng menu gốc
     * @param menuIndex Index của menu
     * @return Mảng menu đã lọc
     */
    public static String[] filterMenuItems(String[] menuItems, int menuIndex) {
        if (isAdmin() || menuItems.length <= 1) {
            return menuItems;
        }

        // Đếm số item được phép hiển thị
        int visibleCount = 1; // Menu title luôn hiển thị
        for (int i = 1; i < menuItems.length; i++) {
            if (isSubMenuVisible(menuIndex, i)) {
                visibleCount++;
            }
        }

        // Tạo mảng mới
        String[] filtered = new String[visibleCount];
        filtered[0] = menuItems[0]; // Menu title
        int idx = 1;
        for (int i = 1; i < menuItems.length; i++) {
            if (isSubMenuVisible(menuIndex, i)) {
                filtered[idx++] = menuItems[i];
            }
        }

        return filtered;
    }

    /**
     * Lấy tên vai trò hiển thị
     */
    public static String getRoleName() {
        if (Auth.isLogin()) {
            return Auth.user.isVaiTro() ? "Quản lý" : "Nhân viên";
        }
        return "Chưa đăng nhập";
    }
}
