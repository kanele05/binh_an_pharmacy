# KẾ HOẠCH HOÀN THIỆN CHƯƠNG TRÌNH QUẢN LÝ HIỆU THUỐC TÂY

## TỔNG QUAN DỰ ÁN HIỆN TẠI

### Đã hoàn thành:
- [x] Bán thuốc (FormBanHang) với live search
- [x] Đặt thuốc (FormDatThuoc)
- [x] CRUD Thuốc, Lô thuốc, Nhóm thuốc
- [x] CRUD Khách hàng, Nhà cung cấp, Nhân viên
- [x] Quản lý Bảng giá
- [x] Nhập hàng (FormNhapHang)
- [x] Trả hàng (FormTraHang)
- [x] Dashboard thống kê
- [x] Báo cáo doanh thu, tồn kho
- [x] Cảnh báo hết hạn, tồn kho thấp
- [x] Đăng nhập (Login) cơ bản
- [x] Quản lý hồ sơ cá nhân

---

## PHASE 1: TÍNH NĂNG CẦN THIẾT (Ưu tiên cao)

### 1.1 In ấn & Xuất dữ liệu
| Task | Mô tả | Độ phức tạp |
|------|-------|-------------|
| In hóa đơn bán hàng | In hóa đơn sau khi bán (PDF/máy in nhiệt) | Trung bình |
| In phiếu nhập kho | In phiếu nhập hàng từ NCC | Trung bình |
| In phiếu đặt hàng | In phiếu đặt thuốc cho khách | Đơn giản |
| Xuất Excel danh sách thuốc | Export danh sách thuốc ra Excel (đã có ExcelHelper) | Đơn giản |
| Xuất Excel báo cáo | Export các báo cáo doanh thu, tồn kho | Đơn giản |

### 1.2 Phân quyền người dùng
| Task | Mô tả | Độ phức tạp |
|------|-------|-------------|
| Phân quyền menu theo vai trò | Quản lý/Nhân viên có quyền khác nhau | Trung bình |
| Ẩn/hiện chức năng theo role | VD: Nhân viên không được xóa dữ liệu | Trung bình |
| Quản lý phiên đăng nhập | Timeout, đăng xuất tự động | Đơn giản |

### 1.3 Hoàn thiện nghiệp vụ bán hàng
| Task | Mô tả | Độ phức tạp |
|------|-------|-------------|
| Tích điểm khách hàng | Cộng điểm tích lũy khi mua (đã có field diemTichLuy) | Trung bình |
| Đổi điểm giảm giá | Dùng điểm tích lũy để giảm giá | Trung bình |
| Hủy hóa đơn | Cho phép hủy hóa đơn và hoàn kho | Trung bình |
| Chọn lô thuốc khi bán | Chọn lô theo FIFO/FEFO | Phức tạp |

---

## PHASE 2: NÂNG CAO TRẢI NGHIỆM (Ưu tiên trung bình)

### 2.1 Cải thiện tìm kiếm
| Task | Mô tả | Độ phức tạp |
|------|-------|-------------|
| Quét mã vạch | Tìm thuốc bằng barcode scanner | Trung bình |
| Tìm kiếm nâng cao thuốc | Lọc theo nhóm, hoạt chất, giá... | Trung bình |
| Tìm kiếm khách hàng theo SDT | Tìm nhanh khách khi bán | Đơn giản |

### 2.2 Cảnh báo & Thông báo
| Task | Mô tả | Độ phức tạp |
|------|-------|-------------|
| Cảnh báo khi bán thuốc sắp hết hạn | Hiện popup cảnh báo | Đơn giản |
| Thông báo đơn đặt hàng đến hạn | Nhắc đơn đặt sắp đến giờ lấy | Trung bình |
| Dashboard cảnh báo tổng hợp | Widget cảnh báo trên dashboard | Trung bình |

### 2.3 Báo cáo nâng cao
| Task | Mô tả | Độ phức tạp |
|------|-------|-------------|
| Biểu đồ doanh thu theo ngày/tuần/tháng | Chart bằng JFreeChart (đã có lib) | Trung bình |
| Báo cáo lợi nhuận | Doanh thu - chi phí nhập hàng | Trung bình |
| Báo cáo thuốc bán chạy | Top thuốc bán nhiều nhất | Đơn giản |
| Báo cáo khách hàng thân thiết | Top khách mua nhiều | Đơn giản |
| Báo cáo nhà cung cấp | Thống kê theo NCC | Đơn giản |

---

## PHASE 3: TÍNH NĂNG BỔ SUNG (Ưu tiên thấp)

### 3.1 Quản lý hệ thống
| Task | Mô tả | Độ phức tạp |
|------|-------|-------------|
| Sao lưu cơ sở dữ liệu | Backup database định kỳ | Trung bình |
| Phục hồi dữ liệu | Restore từ backup | Trung bình |
| Audit log | Ghi lại lịch sử thao tác của user | Phức tạp |
| Cấu hình hệ thống | Thay đổi thông tin hiệu thuốc, thuế... | Đơn giản |

### 3.2 Tương tác với thiết bị
| Task | Mô tả | Độ phức tạp |
|------|-------|-------------|
| Kết nối máy in nhiệt | In hóa đơn 58mm/80mm | Trung bình |
| Ngăn kéo tiền | Mở ngăn kéo khi thanh toán | Đơn giản |
| Đọc mã vạch | Tích hợp barcode scanner | Trung bình |

### 3.3 Tiện ích khác
| Task | Mô tả | Độ phức tạp |
|------|-------|-------------|
| Import thuốc từ Excel | Nhập danh sách thuốc hàng loạt | Trung bình |
| Tra cứu thuốc online | Tìm thông tin thuốc từ API | Phức tạp |
| Gửi SMS/Email khách hàng | Thông báo đơn hàng, khuyến mãi | Phức tạp |

---

## PHASE 4: TỐI ƯU & HOÀN THIỆN

### 4.1 Nâng cao UI/UX
| Task | Mô tả | Độ phức tạp |
|------|-------|-------------|
| Phím tắt bàn phím | F1-F12, Ctrl+S... cho thao tác nhanh | Đơn giản |
| Validation form đầy đủ | Kiểm tra dữ liệu nhập chặt chẽ | Trung bình |
| Loading indicator | Hiện loading khi truy vấn lâu | Đơn giản |
| Confirm dialog | Xác nhận trước khi xóa/sửa | Đơn giản |

### 4.2 Xử lý lỗi & Bảo mật
| Task | Mô tả | Độ phức tạp |
|------|-------|-------------|
| Mã hóa mật khẩu | Hash password (BCrypt/SHA) | Trung bình |
| Xử lý exception đầy đủ | Try-catch và thông báo lỗi rõ ràng | Trung bình |
| Connection pool | Tối ưu kết nối database | Trung bình |
| SQL Injection prevention | Dùng PreparedStatement | Trung bình |

### 4.3 Testing & Documentation
| Task | Mô tả | Độ phức tạp |
|------|-------|-------------|
| Unit test DAO | Test các phương thức CRUD | Trung bình |
| User manual | Hướng dẫn sử dụng | Trung bình |
| Javadoc | Document code | Đơn giản |

---

## ĐỀ XUẤT THỨ TỰ TRIỂN KHAI

### Sprint 1 (Cần thiết nhất):
1. ✅ In hóa đơn bán hàng
2. ✅ Phân quyền menu theo vai trò
3. ✅ Xuất Excel báo cáo
4. ✅ Tích điểm khách hàng

### Sprint 2 (Hoàn thiện bán hàng):
1. ✅ Đổi điểm giảm giá
2. ✅ Hủy hóa đơn
3. ✅ Chọn lô thuốc theo FEFO
4. ✅ Cảnh báo thuốc sắp hết hạn khi bán

### Sprint 3 (Báo cáo & Thống kê):
1. ✅ Biểu đồ doanh thu
2. ✅ Báo cáo lợi nhuận
3. ✅ Báo cáo thuốc bán chạy
4. ✅ Dashboard cảnh báo tổng hợp

### Sprint 4 (Quản trị hệ thống):
1. ✅ Sao lưu/phục hồi database
2. ✅ Mã hóa mật khẩu
3. ✅ Audit log
4. ✅ Cấu hình hệ thống

### Sprint 5 (Tích hợp & Mở rộng):
1. ✅ Quét mã vạch
2. ✅ Kết nối máy in nhiệt
3. ✅ Import thuốc từ Excel
4. ✅ Phím tắt bàn phím

---

## GHI CHÚ KỸ THUẬT

### Thư viện đã có sẵn:
- **POI**: Đã có, dùng cho Excel export/import
- **JFreeChart**: Đã có, dùng cho biểu đồ
- **FlatLaf**: Đã có, UI hiện đại
- **GlazedLists**: Đã có, live search tables
- **Log4j**: Đã có, dùng cho logging

### Cấu trúc code nên tuân theo:
- Entity → DAO → GUI Form
- Dùng PreparedStatement cho SQL
- Sử dụng Toast notification cho feedback
- Dialog cho form edit chi tiết

### Database:
- SQL Server đang chạy
- Schema đầy đủ trong `data/QLTHUOC.sql`
- Cần thêm bảng cho: AuditLog, Config (nếu cần)

---

*Cập nhật: 14/12/2024*
