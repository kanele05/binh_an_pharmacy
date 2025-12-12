CREATE DATABASE QLTHUOC
GO
USE QLTHUOC
GO

CREATE TABLE NhanVien (
    maNV NVARCHAR(50) PRIMARY KEY,
    hoTen NVARCHAR(100),
	ngaySinh DATE,
	gioiTinh BIT,
    sdt NVARCHAR(20),
    email NVARCHAR(100),
	diaChi NVARCHAR(100),
    matKhau NVARCHAR(255),
    vaiTro BIT,
    trangThai BIT
);

CREATE TABLE KhachHang (
    maKH NVARCHAR(50) PRIMARY KEY,
    tenKH NVARCHAR(100),
    sdt NVARCHAR(20),
	gioiTinh BIT,
	ngaySinh DATE,
    diaChi NVARCHAR(255),
    diemTichLuy INT DEFAULT 0,
	trangThai BIT
);

CREATE TABLE NhaCungCap (
    maNCC NVARCHAR(50) PRIMARY KEY,
    tenNCC NVARCHAR(100),
    sdt NVARCHAR(20),
    email NVARCHAR(100),
    diaChi NVARCHAR(255)
);

CREATE TABLE NhomThuoc (
    maNhom NVARCHAR(50) PRIMARY KEY,
    tenNhom NVARCHAR(100)
);

CREATE TABLE Thuoc (
    maThuoc NVARCHAR(50) PRIMARY KEY,
    tenThuoc NVARCHAR(100),
    hoatChat NVARCHAR(255),
    donViTinh NVARCHAR(50),
    trangThai BIT,
    maNhom NVARCHAR(50), -- Khóa ngoại tham chiếu NhomThuoc
    CONSTRAINT FK_Thuoc_NhomThuoc FOREIGN KEY (maNhom) REFERENCES NhomThuoc(maNhom)
);

CREATE TABLE LoThuoc (
    maLo NVARCHAR(50) PRIMARY KEY,
    maThuoc NVARCHAR(50), -- Khóa ngoại tham chiếu Thuoc
    ngayNhap DATE,
    hanSuDung DATE,
    soLuongTon INT,
    trangThai NVARCHAR(50),
	isDeleted BIT,
    CONSTRAINT FK_LoThuoc_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc)
);

CREATE TABLE BangGia (
    maBG NVARCHAR(50) PRIMARY KEY,
    tenBG NVARCHAR(100),
    ngayHieuLuc DATE,
    ngayKetThuc DATE,
	ghiChu NVARCHAR(200),
    trangThai BIT
);

CREATE TABLE ChiTietBangGia (
    maBG NVARCHAR(50),
    maThuoc NVARCHAR(50),
    donViTinh NVARCHAR(50),
    giaBan DECIMAL(18, 2),
    PRIMARY KEY (maBG, maThuoc),
    CONSTRAINT FK_CTBG_BangGia FOREIGN KEY (maBG) REFERENCES BangGia(maBG),
    CONSTRAINT FK_CTBG_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc)
);

CREATE TABLE HoaDon (
    maHD NVARCHAR(50) PRIMARY KEY,
    ngayTao DATETIME DEFAULT GETDATE(),
    tongTien DECIMAL(18, 2),
    giamGia DECIMAL(18, 2),
	thue DECIMAL(18, 2),
    hinhThucTT NVARCHAR(50),
    ghiChu NVARCHAR(255),
    maNV NVARCHAR(50),
    maKH NVARCHAR(50),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);

CREATE TABLE ChiTietHoaDon (
    maHD NVARCHAR(50),
    maThuoc NVARCHAR(50),
    maLo NVARCHAR(50),
    soLuong INT,
    donGia DECIMAL(18, 2),
    thanhTien DECIMAL(18, 2),
    PRIMARY KEY (maHD, maThuoc, maLo),
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
    CONSTRAINT FK_CTHD_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc),
    CONSTRAINT FK_CTHD_LoThuoc FOREIGN KEY (maLo) REFERENCES LoThuoc(maLo)
);

CREATE TABLE DonDatHang (
    maDonDat NVARCHAR(50) PRIMARY KEY,
    tenKhach NVARCHAR(100),
    sdtLienHe NVARCHAR(20),
    gioHenLay DATETIME,
    tongTien DECIMAL(18, 2),
	ghiChu NVARCHAR(255),
    trangThai NVARCHAR(50),
    maNV NVARCHAR(50), -- Nhân viên tạo đơn
    maKH NVARCHAR(50), -- Khách hàng (nếu là khách thành viên)
    CONSTRAINT FK_DonDat_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_DonDat_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);

CREATE TABLE ChiTietDonDat (
    maDonDat NVARCHAR(50),
    maThuoc NVARCHAR(50),
    soLuong INT,
    donGia DECIMAL(18, 2),
    thanhTien DECIMAL(18, 2),
    PRIMARY KEY (maDonDat, maThuoc),
    CONSTRAINT FK_CTDD_DonDat FOREIGN KEY (maDonDat) REFERENCES DonDatHang(maDonDat),
    CONSTRAINT FK_CTDD_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc)
);

CREATE TABLE PhieuNhap (
    maPN NVARCHAR(50) PRIMARY KEY,
    ngayTao DATE,
    tongTien DECIMAL(18, 2),
    trangThai NVARCHAR(50),
    maNV NVARCHAR(50), -- Nhân viên nhập
    maNCC NVARCHAR(50), -- Nhà cung cấp
    CONSTRAINT FK_PhieuNhap_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PhieuNhap_NhaCungCap FOREIGN KEY (maNCC) REFERENCES NhaCungCap(maNCC)
);

CREATE TABLE ChiTietPhieuNhap (
    maPN NVARCHAR(50),
    maThuoc NVARCHAR(50),
    maLo NVARCHAR(50),
    hanSuDung DATE,
    soLuong INT,
    donGia DECIMAL(18, 2),
    thanhTien DECIMAL(18, 2),
    PRIMARY KEY (maPN, maThuoc, maLo),
    CONSTRAINT FK_CTPN_PhieuNhap FOREIGN KEY (maPN) REFERENCES PhieuNhap(maPN),
    CONSTRAINT FK_CTPN_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc),
    CONSTRAINT FK_CTPN_LoThuoc FOREIGN KEY (maLo) REFERENCES LoThuoc(maLo)
);

CREATE TABLE PhieuTraHang (
    maPT NVARCHAR(50) PRIMARY KEY,
    ngayTra DATE,
    tongTienHoanTra DECIMAL(18, 2),
    lyDo NVARCHAR(255),
    maHD NVARCHAR(50), -- Trả từ hóa đơn nào
    maNV NVARCHAR(50), -- Nhân viên xử lý
    maKH NVARCHAR(50),
    CONSTRAINT FK_PhieuTra_HoaDon FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
    CONSTRAINT FK_PhieuTra_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PhieuTra_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);

CREATE TABLE ChiTietPhieuTra (
    maPT NVARCHAR(50),
    maThuoc NVARCHAR(50),
    maLo NVARCHAR(50),
    soLuongTra INT,
    donGiaTra DECIMAL(18, 2),
    thanhTienHoanTra DECIMAL(18, 2),
    PRIMARY KEY (maPT, maThuoc, maLo),
    CONSTRAINT FK_CTPT_PhieuTra FOREIGN KEY (maPT) REFERENCES PhieuTraHang(maPT),
    CONSTRAINT FK_CTPT_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc),
    CONSTRAINT FK_CTPT_LoThuoc FOREIGN KEY (maLo) REFERENCES LoThuoc(maLo)
);

-- =============================================
-- 1. INSERT NHÂN VIÊN (5 người)
-- Vai trò: 1 = Quản lý, 0 = Nhân viên
-- Giới tính: 1 = Nam, 0 = Nữ
-- Trạng thái: 1 = Đang làm
-- =============================================
INSERT INTO NhanVien (maNV, hoTen, ngaySinh, gioiTinh, sdt, email, diaChi, matKhau, vaiTro, trangThai) VALUES
(N'NV001', N'Nguyễn Quản Lý', '1990-01-01', 1, N'0909111222', N'admin@nhathuoc.com', N'Quận 1, TP.HCM', N'123456', 1, 1),
(N'NV002', N'Trần Thị Thu Ngân', '1995-05-15', 0, N'0909333444', N'ngantt@nhathuoc.com', N'Quận 3, TP.HCM', N'123456', 0, 1),
(N'NV003', N'Lê Văn Kho', '1992-08-20', 1, N'0909555666', N'kholv@nhathuoc.com', N'Bình Thạnh, TP.HCM', N'123456', 0, 1),
(N'NV004', N'Phạm Thị Dược', '1998-12-10', 0, N'0909777888', N'duocpt@nhathuoc.com', N'Gò Vấp, TP.HCM', N'123456', 0, 1),
(N'NV005', N'Hoàng Văn Bảo Vệ', '1985-03-30', 1, N'0909999000', N'baove@nhathuoc.com', N'Quận 12, TP.HCM', N'123456', 0, 1);

-- =============================================
-- 2. INSERT KHÁCH HÀNG (15 người)
-- =============================================
INSERT INTO KhachHang (maKH, tenKH, sdt, gioiTinh, ngaySinh, diaChi, diemTichLuy, trangThai) VALUES
(N'KH001', N'Nguyễn Văn An', N'0912345678', 1, '1990-01-01', N'123 Lê Lợi, Q1', 100, 1),
(N'KH002', N'Trần Thị Bích', N'0912345679', 0, '1992-02-02', N'456 Nguyễn Huệ, Q1', 250, 1),
(N'KH003', N'Lê Văn Cường', N'0912345680', 1, '1985-03-03', N'789 Hai Bà Trưng, Q3', 50, 1),
(N'KH004', N'Phạm Thị Dung', N'0912345681', 0, '1995-04-04', N'321 Võ Văn Tần, Q3', 0, 1),
(N'KH005', N'Hoàng Văn Em', N'0912345682', 1, '2000-05-05', N'654 CMT8, Q10', 500, 1),
(N'KH006', N'Đặng Thị Hoa', N'0912345683', 0, '1988-06-06', N'987 Ba Tháng Hai, Q10', 120, 1),
(N'KH007', N'Bùi Văn Giang', N'0912345684', 1, '1991-07-07', N'147 Lý Thường Kiệt, Q11', 80, 1),
(N'KH008', N'Đỗ Thị Hạnh', N'0912345685', 0, '1993-08-08', N'258 Lạc Long Quân, Q11', 300, 1),
(N'KH009', N'Ngô Văn Ích', N'0912345686', 1, '1980-09-09', N'369 Âu Cơ, Tân Bình', 10, 1),
(N'KH010', N'Dương Thị Kim', N'0912345687', 0, '1997-10-10', N'159 Cộng Hòa, Tân Bình', 150, 1),
(N'KH011', N'Lý Văn Long', N'0912345688', 1, '1999-11-11', N'753 Trường Chinh, Tân Phú', 60, 1),
(N'KH012', N'Mai Thị Mận', N'0912345689', 0, '1982-12-12', N'951 Lũy Bán Bích, Tân Phú', 450, 1),
(N'KH013', N'Trương Văn Nam', N'0912345690', 1, '1994-01-15', N'357 Lê Văn Sỹ, Phú Nhuận', 20, 1),
(N'KH014', N'Võ Thị Oanh', N'0912345691', 0, '1996-02-20', N'123 Phan Đăng Lưu, Phú Nhuận', 90, 1),
(N'KH015', N'Hồ Văn Phong', N'0912345692', 1, '1987-03-25', N'456 Hoàng Văn Thụ, Tân Bình', 5, 1);

-- =============================================
-- 3. INSERT NHÀ CUNG CẤP (10 NCC)
-- =============================================
INSERT INTO NhaCungCap (maNCC, tenNCC, sdt, email, diaChi) VALUES
(N'NCC001', N'Công ty CP Dược Hậu Giang (DHG)', N'02923891433', N'dhgpharma@dhg.com.vn', N'288 Nguyễn Văn Cừ, Ninh Kiều, Cần Thơ'),
(N'NCC002', N'Công ty CP Traphaco', N'18006612', N'info@traphaco.com.vn', N'75 Yên Ninh, Ba Đình, Hà Nội'),
(N'NCC003', N'Công ty CP Dược phẩm Imexpharm', N'02773851941', N'imexpharm@imex.com.vn', N'Số 4, đường 30/4, TP. Cao Lãnh'),
(N'NCC004', N'Công ty Sanofi Việt Nam', N'02838298526', N'contact-vn@sanofi.com', N'Số 10 Hàm Nghi, Quận 1, TP.HCM'),
(N'NCC005', N'Công ty Zuellig Pharma Việt Nam', N'02839102650', N'info@zuelligpharma.com', N'KCN Tân Bình, TP.HCM'),
(N'NCC006', N'Công ty CP Dược phẩm OPC', N'02838777133', N'info@opcpharma.com', N'1017 Hồng Bàng, Quận 6, TP.HCM'),
(N'NCC007', N'Công ty CP Dược phẩm SPM', N'02837652726', N'info@spm.com.vn', N'KCN Tân Tạo, Bình Tân, TP.HCM'),
(N'NCC008', N'Công ty TNHH Mega We Care', N'02838123123', N'megawecare@mega.com', N'E-Town, Tân Bình, TP.HCM'),
(N'NCC009', N'Công ty Dược phẩm Trung ương 1 (Pharbaco)', N'02438454561', N'pharbaco@pharbaco.com.vn', N'160 Tôn Đức Thắng, Đống Đa, Hà Nội'),
(N'NCC010', N'Công ty CP Pymepharco', N'02573829165', N'hcns@pymepharco.com', N'166 – 170 Nguyễn Huệ, Tuy Hòa, Phú Yên');

-- =============================================
-- 4. INSERT NHÓM THUỐC (7 Nhóm)
-- =============================================
INSERT INTO NhomThuoc (maNhom, tenNhom) VALUES
(N'NT001', N'Kháng sinh - Kháng khuẩn'),
(N'NT002', N'Giảm đau - Hạ sốt - Chống viêm'),
(N'NT003', N'Vitamin - Khoáng chất'),
(N'NT004', N'Tiêu hóa - Dạ dày'),
(N'NT005', N'Hô hấp (Ho, Hen suyễn)'),
(N'NT006', N'Tim mạch - Huyết áp'),
(N'NT007', N'Dụng cụ y tế & Khác');

-- =============================================
-- 5. INSERT THUỐC (30 Thuốc)
-- =============================================
INSERT INTO Thuoc (maThuoc, tenThuoc, hoatChat, donViTinh, trangThai, maNhom) VALUES
-- Nhóm 1: Kháng sinh
(N'T001', N'Amoxicillin 500mg', N'Amoxicillin', N'Vỉ', 1, N'NT001'),
(N'T002', N'Augmentin 625mg', N'Amoxicillin + Clavulanic', N'Hộp', 1, N'NT001'),
(N'T003', N'Cephalexin 500mg', N'Cephalexin', N'Vỉ', 1, N'NT001'),
(N'T004', N'Ciprofloxacin 500mg', N'Ciprofloxacin', N'Vỉ', 1, N'NT001'),
(N'T005', N'Azithromycin 250mg', N'Azithromycin', N'Hộp', 1, N'NT001'),

-- Nhóm 2: Giảm đau, hạ sốt
(N'T006', N'Panadol Extra', N'Paracetamol + Caffeine', N'Hộp', 1, N'NT002'),
(N'T007', N'Efferalgan 500mg', N'Paracetamol', N'Viên', 1, N'NT002'),
(N'T008', N'Hapacol 250mg', N'Paracetamol', N'Gói', 1, N'NT002'),
(N'T009', N'Ibuprofen 400mg', N'Ibuprofen', N'Vỉ', 1, N'NT002'),
(N'T010', N'Aspirin 81mg', N'Aspirin', N'Vỉ', 1, N'NT002'),
(N'T011', N'Salonpas (Dán)', N'Methyl Salicylate', N'Hộp', 1, N'NT002'),

-- Nhóm 3: Vitamin
(N'T012', N'Vitamin C 500mg', N'Ascorbic Acid', N'Lọ', 1, N'NT003'),
(N'T013', N'Vitamin E 400IU', N'Alpha Tocopherol', N'Hộp', 1, N'NT003'),
(N'T014', N'Vitamin 3B', N'B1, B6, B12', N'Vỉ', 1, N'NT003'),
(N'T015', N'Canxi Corbiere 10ml', N'Calcium Glucoheptonate', N'Ống', 1, N'NT003'),
(N'T016', N'Berocca', N'Vitamin tổng hợp', N'Tuýp', 1, N'NT003'),

-- Nhóm 4: Tiêu hóa
(N'T017', N'Berberin 100mg', N'Berberin', N'Lọ', 1, N'NT004'),
(N'T018', N'Smecta', N'Diosmectite', N'Gói', 1, N'NT004'),
(N'T019', N'Omeprazol 20mg', N'Omeprazole', N'Vỉ', 1, N'NT004'),
(N'T020', N'Gaviscon (Gói)', N'Natri alginat', N'Gói', 1, N'NT004'),
(N'T021', N'Men vi sinh Enterogermina', N'Bacillus clausii', N'Ống', 1, N'NT004'),

-- Nhóm 5: Hô hấp
(N'T022', N'Siro ho Prospan', N'Cao lá thường xuân', N'Chai', 1, N'NT005'),
(N'T023', N'Viên ngậm Bảo Thanh', N'Dược liệu', N'Vỉ', 1, N'NT005'),
(N'T024', N'Eugica đỏ', N'Tinh dầu tràm', N'Hộp', 1, N'NT005'),
(N'T025', N'Thuốc ho Methorphan', N'Dextromethorphan', N'Lọ', 1, N'NT005'),

-- Nhóm 6: Tim mạch
(N'T026', N'Amlodipin 5mg', N'Amlodipine', N'Vỉ', 1, N'NT006'),
(N'T027', N'Losartan 50mg', N'Losartan', N'Vỉ', 1, N'NT006'),

-- Nhóm 7: Dụng cụ & Khác
(N'T028', N'Khẩu trang Y tế 4 lớp', N'Vải không dệt', N'Hộp', 1, N'NT007'),
(N'T029', N'Nước muối sinh lý 0.9%', N'Natri Clorid', N'Chai', 1, N'NT007'),
(N'T030', N'Băng cá nhân Urgo', N'Vải', N'Hộp', 1, N'NT007'),
(N'T031', N'Cồn 70 độ', N'Ethanol', N'Chai', 1, N'NT007');
-- =============================================
-- 6. INSERT LÔ THUỐC (Quản lý tồn kho & Hạn dùng)
-- Lưu ý: Mình tạo dữ liệu đa dạng (Hết hạn, Sắp hết, Còn xa) để test báo cáo
-- =============================================
INSERT INTO LoThuoc (maLo, maThuoc, ngayNhap, hanSuDung, soLuongTon, trangThai, isDeleted) VALUES
-- Nhóm Kháng sinh
(N'L001', N'T001', '2023-01-10', '2025-01-10', 500, N'Đã hết hạn', 0), -- Sắp hết hạn
(N'L002', N'T002', '2023-05-20', '2026-05-20', 100, N'Còn hạn', 0),
(N'L003', N'T003', '2023-06-15', '2024-12-01', 50, N'Đã hết hạn', 0), -- Đã hết hạn (để test báo đỏ)
(N'L004', N'T004', '2023-08-01', '2026-08-01', 200, N'Còn hạn', 0),
(N'L005', N'T005', '2023-09-10', '2025-09-10', 150, N'Đã hết hạn', 0),

-- Nhóm Giảm đau (Panadol bán chạy nên nhập nhiều lô)
(N'L006', N'T006', '2023-10-01', '2026-10-01', 1000, N'Còn hạn', 0), -- Lô mới
(N'L007', N'T006', '2022-12-01', '2024-12-30', 20, N'Đã hết hạn', 0),  -- Lô cũ sắp hết hạn (Ưu tiên bán trước)
(N'L008', N'T007', '2023-02-15', '2025-02-15', 300, N'Đã hết hạn', 0),
(N'L009', N'T008', '2023-03-20', '2025-03-20', 5, N'Đã hết hạn', 0),   -- Tồn kho thấp (Test cảnh báo hết hàng)
(N'L010', N'T009', '2023-07-07', '2026-07-07', 400, N'Còn hạn', 0),
(N'L011', N'T010', '2023-01-01', '2024-01-01', 0, N'Đã hết hạn', 0),   -- Hết sạch hàng

-- Nhóm Vitamin
(N'L012', N'T012', '2023-11-11', '2025-11-11', 500, N'Đã hết hạn', 0),
(N'L013', N'T013', '2023-05-05', '2026-05-05', 250, N'Còn hạn', 0),
(N'L014', N'T014', '2023-09-09', '2025-12-31', 150, N'Sắp hết hạn', 0),
(N'L015', N'T015', '2023-12-01', '2026-01-28', 80, N'Sắp hết hạn', 0),  -- Hết hạn 
(N'L016', N'T016', '2023-10-20', '2026-10-20', 60, N'Còn hạn', 0),

-- Nhóm Tiêu hóa
(N'L017', N'T017', '2023-04-30', '2026-04-30', 300, N'Còn hạn', 0),
(N'L018', N'T018', '2023-08-15', '2026-02-26', 1200, N'Sắp hết hạn', 0), -- Tồn nhiều
(N'L019', N'T019', '2023-06-01', '2026-06-01', 100, N'Còn hạn', 0),
(N'L020', N'T020', '2023-02-28', '2025-02-28', 10, N'Đã hết hạn', 0), -- Tồn thấp

-- Nhóm Hô hấp
(N'L021', N'T022', '2023-09-15', '2026-09-15', 80, N'Còn hạn', 0),
(N'L022', N'T023', '2023-01-20', '2025-01-20', 200, N'Đã hết hạn', 0),
(N'L023', N'T024', '2023-11-01', '2026-11-01', 150, N'Còn hạn', 0),

-- Nhóm Dụng cụ
(N'L024', N'T028', '2023-05-10', '2028-05-10', 2000, N'Còn hạn', 0), -- Khẩu trang hạn dài
(N'L025', N'T029', '2023-07-20', '2026-07-20', 500, N'Còn hạn', 0),
(N'L026', N'T030', '2023-03-15', '2027-03-15', 300, N'Còn hạn', 0);

-- =============================================
-- 7. INSERT BẢNG GIÁ (Header)
-- =============================================
INSERT INTO BangGia (maBG, tenBG, ngayHieuLuc, ngayKetThuc, ghiChu, trangThai) VALUES
(N'BG001', N'Bảng giá bán lẻ 2024', '2024-01-01', NULL, N'Áp dụng cho khách lẻ toàn hệ thống', 1), -- Đang áp dụng
(N'BG002', N'Bảng giá khuyến mãi Tết', '2024-01-01', '2024-02-15', N'Giảm giá các loại Vitamin', 0); -- Hết hiệu lực

-- =============================================
-- 8. INSERT CHI TIẾT BẢNG GIÁ (Giá bán cho từng thuốc)
-- Link vào bảng giá BG001 (Giá chuẩn)
-- =============================================
INSERT INTO ChiTietBangGia (maBG, maThuoc, donViTinh, giaBan) VALUES
-- Kháng sinh
(N'BG001', N'T001', N'Vỉ', 15000),
(N'BG001', N'T002', N'Hộp', 220000),
(N'BG001', N'T003', N'Vỉ', 12000),
(N'BG001', N'T004', N'Vỉ', 18000),
(N'BG001', N'T005', N'Hộp', 85000),

-- Giảm đau
(N'BG001', N'T006', N'Hộp', 185000), -- Panadol Extra
(N'BG001', N'T007', N'Viên', 5000),
(N'BG001', N'T008', N'Gói', 3500),
(N'BG001', N'T009', N'Vỉ', 25000),
(N'BG001', N'T010', N'Vỉ', 15000),
(N'BG001', N'T011', N'Hộp', 32000),

-- Vitamin (Giá thường)
(N'BG001', N'T012', N'Lọ', 60000),
(N'BG001', N'T013', N'Hộp', 150000),
(N'BG001', N'T014', N'Vỉ', 45000),
(N'BG001', N'T015', N'Ống', 5000),
(N'BG001', N'T016', N'Tuýp', 85000),

-- Tiêu hóa
(N'BG001', N'T017', N'Lọ', 10000), -- Berberin rẻ
(N'BG001', N'T018', N'Gói', 4000),
(N'BG001', N'T019', N'Vỉ', 22000),
(N'BG001', N'T020', N'Gói', 6500),
(N'BG001', N'T021', N'Ống', 8000),

-- Hô hấp
(N'BG001', N'T022', N'Chai', 95000), -- Prospan
(N'BG001', N'T023', N'Vỉ', 35000),
(N'BG001', N'T024', N'Hộp', 55000),
(N'BG001', N'T025', N'Lọ', 40000),

-- Tim mạch
(N'BG001', N'T026', N'Vỉ', 30000),
(N'BG001', N'T027', N'Vỉ', 45000),

-- Dụng cụ
(N'BG001', N'T028', N'Hộp', 35000), -- Khẩu trang
(N'BG001', N'T029', N'Chai', 5000),  -- Nước muối
(N'BG001', N'T030', N'Hộp', 25000),
(N'BG001', N'T031', N'Chai', 15000);

-- =============================================
-- INSERT CHI TIẾT CHO BẢNG GIÁ KHUYẾN MÃI (BG002)
-- Giảm giá một số mặt hàng Vitamin & Khẩu trang
-- =============================================
INSERT INTO ChiTietBangGia (maBG, maThuoc, donViTinh, giaBan) VALUES
(N'BG002', N'T012', N'Lọ', 50000),  -- Vitamin C giảm 10k
(N'BG002', N'T013', N'Hộp', 135000), -- Vitamin E giảm 15k
(N'BG002', N'T016', N'Tuýp', 75000), -- Berocca giảm 10k
(N'BG002', N'T028', N'Hộp', 25000);  -- Khẩu trang giảm 10k
GO
-- View hiển thị danh sách thuốc với đầy đủ thông tin cần thiết
CREATE OR ALTER VIEW vw_DanhSachThuocFull AS
SELECT 
    t.maThuoc, 
    t.tenThuoc, 
    t.hoatChat, 
    t.donViTinh, 
    nt.tenNhom, 
    -- Giá nhập
    ISNULL((SELECT TOP 1 ctpn.donGia FROM ChiTietPhieuNhap ctpn JOIN PhieuNhap pn ON ctpn.maPN = pn.maPN WHERE ctpn.maThuoc = t.maThuoc ORDER BY pn.ngayTao DESC), 0) AS giaNhap, 
    -- Giá bán
    ISNULL((SELECT TOP 1 ctbg.giaBan FROM ChiTietBangGia ctbg JOIN BangGia bg ON ctbg.maBG = bg.maBG WHERE ctbg.maThuoc = t.maThuoc AND bg.trangThai = 1), 0) AS giaBan, 
    
    -- 1. TỒN KHO THỰC TẾ (Sửa logic: Phải loại bỏ những lô đã đánh dấu xóa isDeleted=1)
    ISNULL((
        SELECT SUM(soLuongTon) 
        FROM LoThuoc 
        WHERE maThuoc = t.maThuoc AND isDeleted = 0
    ), 0) AS tonKho,

    -- 2. TỒN KHO BÁN ĐƯỢC (Available: Chưa xóa AND Còn hạn AND Trạng thái ok)
    ISNULL((
        SELECT SUM(soLuongTon) 
        FROM LoThuoc 
        WHERE maThuoc = t.maThuoc 
          AND isDeleted = 0 
          AND hanSuDung > GETDATE()
          AND (trangThai = N'Còn hạn' OR trangThai = N'Sắp hết hạn')
    ), 0) AS tonKhoBanDuoc,

    t.trangThai
FROM Thuoc t 
JOIN NhomThuoc nt ON t.maNhom = nt.maNhom
GO

CREATE OR ALTER VIEW vw_ThuocBanHang AS
SELECT 
    t.maThuoc, 
    t.tenThuoc, 
    t.hoatChat,
    l.maLo, 
    l.hanSuDung, 
    l.soLuongTon, 
    t.donViTinh, 
    ISNULL(ctbg.giaBan, 0) as giaBan
FROM LoThuoc l 
JOIN Thuoc t ON l.maThuoc = t.maThuoc 
LEFT JOIN ChiTietBangGia ctbg ON t.maThuoc = ctbg.maThuoc 
LEFT JOIN BangGia bg ON ctbg.maBG = bg.maBG 
WHERE l.isDeleted = 0            -- Chưa bị hủy
  AND l.soLuongTon > 0           -- Còn tồn kho
  AND l.hanSuDung >= CAST(GETDATE() AS DATE) -- Chưa hết hạn
  AND bg.trangThai = 1           -- Bảng giá đang hiệu lực
  AND ISNULL(ctbg.giaBan, 0) > 0 -- Phải có giá bán mới được hiện
GO

INSERT INTO PhieuNhap (maPN, ngayTao, tongTien, trangThai, maNV, maNCC) VALUES
(N'PN001', '2023-01-10', 5000000, N'Đã nhập', N'NV003', N'NCC001'), -- L001
(N'PN002', '2023-05-20', 15000000, N'Đã nhập', N'NV003', N'NCC001'), -- L002
(N'PN003', '2023-06-15', 400000, N'Đã nhập', N'NV003', N'NCC001'), -- L003
(N'PN004', '2023-08-01', 2400000, N'Đã nhập', N'NV003', N'NCC001'), -- L004
(N'PN005', '2023-09-10', 9000000, N'Đã nhập', N'NV003', N'NCC001'), -- L005
(N'PN006', '2023-10-01', 140000000, N'Đã nhập', N'NV003', N'NCC002'), -- L006 (Panadol)
(N'PN007', '2022-12-01', 2800000, N'Đã nhập', N'NV003', N'NCC002'), -- L007
(N'PN008', '2023-02-15', 1050000, N'Đã nhập', N'NV003', N'NCC002'), -- L008
(N'PN009', '2023-03-20', 12500, N'Đã nhập', N'NV003', N'NCC002'), -- L009
(N'PN010', '2023-07-07', 8000000, N'Đã nhập', N'NV003', N'NCC002'), -- L010
(N'PN011', '2023-01-01', 0, N'Đã nhập', N'NV003', N'NCC002'), -- L011 (Hết hàng)
(N'PN012', '2023-11-11', 22500000, N'Đã nhập', N'NV003', N'NCC003'), -- L012 (Vitamin C)
(N'PN013', '2023-05-05', 27500000, N'Đã nhập', N'NV003', N'NCC003'), -- L013
(N'PN014', '2023-09-09', 5250000, N'Đã nhập', N'NV003', N'NCC003'), -- L014
(N'PN015', '2023-12-01', 280000, N'Đã nhập', N'NV003', N'NCC003'), -- L015
(N'PN016', '2023-10-20', 3600000, N'Đã nhập', N'NV003', N'NCC003'), -- L016
(N'PN017', '2023-04-30', 2100000, N'Đã nhập', N'NV003', N'NCC004'), -- L017
(N'PN018', '2023-08-15', 3000000, N'Đã nhập', N'NV003', N'NCC004'), -- L018
(N'PN019', '2023-06-01', 1500000, N'Đã nhập', N'NV003', N'NCC004'), -- L019
(N'PN020', '2023-02-28', 45000, N'Đã nhập', N'NV003', N'NCC004'), -- L020
(N'PN021', '2023-09-15', 5600000, N'Đã nhập', N'NV003', N'NCC005'), -- L021 (Prospan)
(N'PN022', '2023-01-20', 5000000, N'Đã nhập', N'NV003', N'NCC005'), -- L022
(N'PN023', '2023-11-01', 6000000, N'Đã nhập', N'NV003', N'NCC005'), -- L023
(N'PN024', '2023-05-10', 50000000, N'Đã nhập', N'NV003', N'NCC007'), -- L024 (Khẩu trang)
(N'PN025', '2023-07-20', 1500000, N'Đã nhập', N'NV003', N'NCC007'), -- L025
(N'PN026', '2023-03-15', 5250000, N'Đã nhập', N'NV003', N'NCC007'); -- L026

INSERT INTO ChiTietPhieuNhap (maPN, maThuoc, maLo, hanSuDung, soLuong, donGia, thanhTien) VALUES
(N'PN001', N'T001', N'L001', '2025-01-10', 500, 10000, 5000000),
(N'PN002', N'T002', N'L002', '2026-05-20', 100, 150000, 15000000),
(N'PN003', N'T003', N'L003', '2024-12-01', 50, 8000, 400000),
(N'PN004', N'T004', N'L004', '2026-08-01', 200, 12000, 2400000),
(N'PN005', N'T005', N'L005', '2025-09-10', 150, 60000, 9000000),
(N'PN006', N'T006', N'L006', '2026-10-01', 1000, 140000, 140000000), -- Panadol
(N'PN007', N'T006', N'L007', '2024-12-30', 20, 140000, 2800000),
(N'PN008', N'T007', N'L008', '2025-02-15', 300, 3500, 1050000),
(N'PN009', N'T008', N'L009', '2025-03-20', 5, 2500, 12500),
(N'PN010', N'T009', N'L010', '2026-07-07', 400, 20000, 8000000),
(N'PN011', N'T010', N'L011', '2024-01-01', 0, 10000, 0),
(N'PN012', N'T012', N'L012', '2025-11-11', 500, 45000, 22500000),
(N'PN013', N'T013', N'L013', '2026-05-05', 250, 110000, 27500000),
(N'PN014', N'T014', N'L014', '2025-09-09', 150, 35000, 5250000),
(N'PN015', N'T015', N'L015', '2024-06-01', 80, 3500, 280000),
(N'PN016', N'T016', N'L016', '2026-10-20', 60, 60000, 3600000),
(N'PN017', N'T017', N'L017', '2026-04-30', 300, 7000, 2100000),
(N'PN018', N'T018', N'L018', '2025-08-15', 1200, 2500, 3000000),
(N'PN019', N'T019', N'L019', '2026-06-01', 100, 15000, 1500000),
(N'PN020', N'T020', N'L020', '2025-02-28', 10, 4500, 45000),
(N'PN021', N'T022', N'L021', '2026-09-15', 80, 70000, 5600000),
(N'PN022', N'T023', N'L022', '2025-01-20', 200, 25000, 5000000),
(N'PN023', N'T024', N'L023', '2026-11-01', 150, 40000, 6000000),
(N'PN024', N'T028', N'L024', '2028-05-10', 2000, 25000, 50000000),
(N'PN025', N'T029', N'L025', '2026-07-20', 500, 3000, 1500000),
(N'PN026', N'T030', N'L026', '2027-03-15', 300, 17500, 5250000);

GO
CREATE OR ALTER PROCEDURE sp_TaoBangGiaMoi
    @TenBG NVARCHAR(100),
    @NgayHieuLuc DATE,
    @GhiChu NVARCHAR(200),
    @NewMaBG NVARCHAR(50) OUTPUT -- Tham số đầu ra để trả về Java
AS
BEGIN
    UPDATE BangGia 
    SET trangThai = 0, ngayKetThuc = GETDATE() 
    WHERE trangThai = 1;

    DECLARE @MaxID NVARCHAR(50);
    DECLARE @NextID INT;
    
    SELECT TOP 1 @MaxID = maBG FROM BangGia ORDER BY maBG DESC;
    
    IF @MaxID IS NULL
        SET @NextID = 1; -- Nếu chưa có thì bắt đầu là 1
    ELSE
        SET @NextID = CAST(SUBSTRING(@MaxID, 3, 10) AS INT) + 1; -- Lấy phần số + 1

    SET @NewMaBG = 'BG' + RIGHT('000' + CAST(@NextID AS NVARCHAR(10)), 3);

    INSERT INTO BangGia (maBG, tenBG, ngayHieuLuc, ngayKetThuc, ghiChu, trangThai)
    VALUES (@NewMaBG, @TenBG, @NgayHieuLuc, NULL, @GhiChu, 1);
END
GO