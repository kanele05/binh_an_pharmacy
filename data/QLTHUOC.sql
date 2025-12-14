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
    donViCoBan NVARCHAR(50),
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
    PRIMARY KEY (maBG, maThuoc, donViTinh),
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
INSERT INTO Thuoc (maThuoc, tenThuoc, hoatChat, donViCoBan, trangThai, maNhom) VALUES
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
    t.donViCoBan, 
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
    t.donViCoBan AS donViTinh,
    1 AS giaTriQuyDoi,
    ISNULL(ctbg.giaBan, 0) AS giaBan
FROM LoThuoc l 
JOIN Thuoc t ON l.maThuoc = t.maThuoc 
LEFT JOIN ChiTietBangGia ctbg ON t.maThuoc = ctbg.maThuoc 
    AND ctbg.donViTinh = t.donViCoBan
LEFT JOIN BangGia bg ON ctbg.maBG = bg.maBG 
WHERE l.isDeleted = 0
  AND l.soLuongTon > 0
  AND l.hanSuDung >= CAST(GETDATE() AS DATE)
  AND (bg.trangThai = 1 OR bg.trangThai IS NULL)
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

-- =====================================================
-- SCRIPT CẬP NHẬT DATABASE - HỆ THỐNG QUẢN LÝ HIỆU THUỐC BÌNH AN
-- Ngày tạo: 15/12/2025
-- Mô tả: Bổ sung các cột và stored procedures cho phù hợp với Entity classes
-- LƯU Ý:  Chạy script này SAU KHI đã có database QLTHUOC từ script gốc
-- =====================================================

USE QLTHUOC
GO

-- =====================================================
-- 1. BỔ SUNG CỘT CHO BẢNG THUỐC
-- =====================================================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'Thuoc') AND name = 'TonToiThieu')
BEGIN
    ALTER TABLE Thuoc ADD TonToiThieu INT DEFAULT 10;
    PRINT N'✅ Đã thêm cột TonToiThieu vào bảng Thuoc';
END
GO

-- Cập nhật giá trị mặc định cho các thuốc hiện có
UPDATE Thuoc SET TonToiThieu = 10 WHERE TonToiThieu IS NULL;
GO

-- =====================================================
-- 2. BỔ SUNG CỘT CHO BẢNG NHÀ CUNG CẤP
-- =====================================================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'NhaCungCap') AND name = 'nguoiLienHe')
BEGIN
    ALTER TABLE NhaCungCap ADD nguoiLienHe NVARCHAR(100) NULL;
    PRINT N'✅ Đã thêm cột nguoiLienHe vào bảng NhaCungCap';
END
GO

IF NOT EXISTS (SELECT * FROM sys. columns WHERE object_id = OBJECT_ID(N'NhaCungCap') AND name = 'trangThai')
BEGIN
    ALTER TABLE NhaCungCap ADD trangThai BIT DEFAULT 1;
    PRINT N'✅ Đã thêm cột trangThai vào bảng NhaCungCap';
END
GO

-- Cập nhật giá trị mặc định
UPDATE NhaCungCap SET trangThai = 1 WHERE trangThai IS NULL;
GO

-- =====================================================
-- 3. BỔ SUNG CỘT CHO BẢNG PHIẾU NHẬP
-- =====================================================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'PhieuNhap') AND name = 'ghiChu')
BEGIN
    ALTER TABLE PhieuNhap ADD ghiChu NVARCHAR(500) NULL;
    PRINT N'✅ Đã thêm cột ghiChu vào bảng PhieuNhap';
END
GO

-- =====================================================
-- 4. BỔ SUNG CỘT CHO BẢNG PHIẾU TRẢ HÀNG
-- =====================================================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'PhieuTraHang') AND name = 'trangThai')
BEGIN
    ALTER TABLE PhieuTraHang ADD trangThai NVARCHAR(50) DEFAULT N'Đã trả';
    PRINT N'✅ Đã thêm cột trangThai vào bảng PhieuTraHang';
END
GO

IF NOT EXISTS (SELECT * FROM sys. columns WHERE object_id = OBJECT_ID(N'PhieuTraHang') AND name = 'ghiChu')
BEGIN
    ALTER TABLE PhieuTraHang ADD ghiChu NVARCHAR(500) NULL;
    PRINT N'✅ Đã thêm cột ghiChu vào bảng PhieuTraHang';
END
GO

-- =====================================================
-- 5. BỔ SUNG CỘT ĐƠN VỊ TÍNH CHO CHI TIẾT HÓA ĐƠN
-- =====================================================
IF NOT EXISTS (SELECT * FROM sys. columns WHERE object_id = OBJECT_ID(N'ChiTietHoaDon') AND name = 'donViTinh')
BEGIN
    ALTER TABLE ChiTietHoaDon ADD donViTinh NVARCHAR(50) NULL;
    PRINT N'✅ Đã thêm cột donViTinh vào bảng ChiTietHoaDon';
END
GO

-- =====================================================
-- 6. BỔ SUNG CỘT ĐƠN VỊ TÍNH CHO CHI TIẾT ĐƠN ĐẶT
-- =====================================================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'ChiTietDonDat') AND name = 'donViTinh')
BEGIN
    ALTER TABLE ChiTietDonDat ADD donViTinh NVARCHAR(50) NULL;
    PRINT N'✅ Đã thêm cột donViTinh vào bảng ChiTietDonDat';
END
GO

-- =====================================================
-- 7. BỔ SUNG CỘT ĐƠN VỊ TÍNH CHO CHI TIẾT PHIẾU TRẢ
-- =====================================================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'ChiTietPhieuTra') AND name = 'donViTinh')
BEGIN
    ALTER TABLE ChiTietPhieuTra ADD donViTinh NVARCHAR(50) NULL;
    PRINT N'✅ Đã thêm cột donViTinh vào bảng ChiTietPhieuTra';
END
GO

-- =====================================================
-- 8. STORED PROCEDURES CHO NGHIỆP VỤ
-- =====================================================

-- SP:  Lấy tổng tồn kho theo mã thuốc (cho validate số lượng)
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_GetTongTonByMaThuoc')
    DROP PROCEDURE sp_GetTongTonByMaThuoc;
GO

CREATE PROCEDURE sp_GetTongTonByMaThuoc
    @MaThuoc NVARCHAR(50)
AS
BEGIN
    SELECT COALESCE(SUM(soLuongTon), 0) AS TongTon
    FROM LoThuoc 
    WHERE maThuoc = @MaThuoc 
      AND isDeleted = 0 
      AND hanSuDung > GETDATE();
END
GO
PRINT N'✅ Đã tạo SP sp_GetTongTonByMaThuoc';

-- SP: Lấy tồn kho theo lô cụ thể (cho FormBanHang)
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_GetTonKhoByMaLo')
    DROP PROCEDURE sp_GetTonKhoByMaLo;
GO

CREATE PROCEDURE sp_GetTonKhoByMaLo
    @MaLo NVARCHAR(50)
AS
BEGIN
    SELECT soLuongTon
    FROM LoThuoc 
    WHERE maLo = @MaLo 
      AND isDeleted = 0;
END
GO
PRINT N'✅ Đã tạo SP sp_GetTonKhoByMaLo';

-- SP: Lấy danh sách thuốc sắp hết hạn (trong N ngày)
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_GetThuocSapHetHan')
    DROP PROCEDURE sp_GetThuocSapHetHan;
GO

CREATE PROCEDURE sp_GetThuocSapHetHan
    @SoNgay INT = 30
AS
BEGIN
    SELECT 
        l.maLo,
        t.maThuoc,
        t. tenThuoc,
        t.donViCoBan,
        l.soLuongTon,
        l.hanSuDung,
        DATEDIFF(DAY, GETDATE(), l.hanSuDung) AS SoNgayConLai
    FROM LoThuoc l
    INNER JOIN Thuoc t ON l.maThuoc = t. maThuoc
    WHERE l.isDeleted = 0 
      AND l.soLuongTon > 0
      AND l.hanSuDung <= DATEADD(DAY, @SoNgay, GETDATE())
      AND l.hanSuDung > GETDATE()
    ORDER BY l.hanSuDung ASC;
END
GO
PRINT N'✅ Đã tạo SP sp_GetThuocSapHetHan';

-- SP: Lấy danh sách thuốc tồn kho thấp (dưới mức tối thiểu)
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_GetThuocTonThap')
    DROP PROCEDURE sp_GetThuocTonThap;
GO

CREATE PROCEDURE sp_GetThuocTonThap
AS
BEGIN
    SELECT 
        t.maThuoc,
        t.tenThuoc,
        t.donViCoBan,
        t.TonToiThieu,
        COALESCE(SUM(l. soLuongTon), 0) AS TongTon,
        t.TonToiThieu - COALESCE(SUM(l.soLuongTon), 0) AS CanNhapThem
    FROM Thuoc t
    LEFT JOIN LoThuoc l ON t. maThuoc = l.maThuoc 
        AND l.isDeleted = 0 
        AND l.hanSuDung > GETDATE()
    WHERE t.trangThai = 1
    GROUP BY t.maThuoc, t.tenThuoc, t.donViCoBan, t.TonToiThieu
    HAVING COALESCE(SUM(l.soLuongTon), 0) < t.TonToiThieu
    ORDER BY (t.TonToiThieu - COALESCE(SUM(l.soLuongTon), 0)) DESC;
END
GO
PRINT N'✅ Đã tạo SP sp_GetThuocTonThap';

-- SP: Lấy doanh thu theo khoảng thời gian
IF EXISTS (SELECT * FROM sys. procedures WHERE name = 'sp_GetDoanhThu')
    DROP PROCEDURE sp_GetDoanhThu;
GO

CREATE PROCEDURE sp_GetDoanhThu
    @TuNgay DATE,
    @DenNgay DATE
AS
BEGIN
    SELECT 
        CAST(ngayTao AS DATE) AS Ngay,
        COUNT(*) AS SoHoaDon,
        SUM(tongTien) AS TongDoanhThu,
        SUM(thue) AS TongThue,
        SUM(giamGia) AS TongGiamGia,
        SUM(tongTien + thue - giamGia) AS ThucThu
    FROM HoaDon
    WHERE CAST(ngayTao AS DATE) BETWEEN @TuNgay AND @DenNgay
    GROUP BY CAST(ngayTao AS DATE)
    ORDER BY Ngay;
END
GO
PRINT N'✅ Đã tạo SP sp_GetDoanhThu';

-- SP:  Lấy Top thuốc bán chạy
IF EXISTS (SELECT * FROM sys. procedures WHERE name = 'sp_GetTopThuocBanChay')
    DROP PROCEDURE sp_GetTopThuocBanChay;
GO

CREATE PROCEDURE sp_GetTopThuocBanChay
    @TopN INT = 10,
    @TuNgay DATE = NULL,
    @DenNgay DATE = NULL
AS
BEGIN
    IF @TuNgay IS NULL SET @TuNgay = DATEADD(MONTH, -1, GETDATE());
    IF @DenNgay IS NULL SET @DenNgay = GETDATE();
    
    SELECT TOP (@TopN)
        t.maThuoc,
        t.tenThuoc,
        SUM(ct.soLuong) AS TongSoLuongBan,
        SUM(ct.thanhTien) AS TongDoanhThu,
        COUNT(DISTINCT hd.maHD) AS SoLanBan
    FROM ChiTietHoaDon ct
    INNER JOIN Thuoc t ON ct.maThuoc = t.maThuoc
    INNER JOIN HoaDon hd ON ct.maHD = hd.maHD
    WHERE CAST(hd.ngayTao AS DATE) BETWEEN @TuNgay AND @DenNgay
    GROUP BY t.maThuoc, t.tenThuoc
    ORDER BY TongSoLuongBan DESC;
END
GO
PRINT N'✅ Đã tạo SP sp_GetTopThuocBanChay';

-- SP: Thống kê tổng quan cho Dashboard
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_GetDashboardStats')
    DROP PROCEDURE sp_GetDashboardStats;
GO

CREATE PROCEDURE sp_GetDashboardStats
AS
BEGIN
    -- Doanh thu hôm nay
    SELECT 
        N'HomNay' AS LoaiThongKe,
        COUNT(*) AS SoHoaDon,
        COALESCE(SUM(tongTien + thue - giamGia), 0) AS DoanhThu
    FROM HoaDon 
    WHERE CAST(ngayTao AS DATE) = CAST(GETDATE() AS DATE)
    
    UNION ALL
    
    -- Doanh thu tuần này (từ thứ 2 đầu tuần)
    SELECT 
        N'TuanNay' AS LoaiThongKe,
        COUNT(*) AS SoHoaDon,
        COALESCE(SUM(tongTien + thue - giamGia), 0) AS DoanhThu
    FROM HoaDon 
    WHERE ngayTao >= DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), 0)
    
    UNION ALL
    
    -- Doanh thu tháng này
    SELECT 
        N'ThangNay' AS LoaiThongKe,
        COUNT(*) AS SoHoaDon,
        COALESCE(SUM(tongTien + thue - giamGia), 0) AS DoanhThu
    FROM HoaDon 
    WHERE MONTH(ngayTao) = MONTH(GETDATE()) AND YEAR(ngayTao) = YEAR(GETDATE());
END
GO
PRINT N'✅ Đã tạo SP sp_GetDashboardStats';

-- SP: Kiểm tra hóa đơn có thể trả hàng không (< 30 ngày)
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_CheckHoaDonCoTheTraHang')
    DROP PROCEDURE sp_CheckHoaDonCoTheTraHang;
GO

CREATE PROCEDURE sp_CheckHoaDonCoTheTraHang
    @MaHD NVARCHAR(50)
AS
BEGIN
    DECLARE @NgayTao DATETIME;
    DECLARE @SoNgay INT;
    
    SELECT @NgayTao = ngayTao FROM HoaDon WHERE maHD = @MaHD;
    
    IF @NgayTao IS NULL
    BEGIN
        SELECT 0 AS CoTheTraHang, N'Không tìm thấy hóa đơn' AS ThongBao, 0 AS SoNgay;
        RETURN;
    END
    
    SET @SoNgay = DATEDIFF(DAY, @NgayTao, GETDATE());
    
    IF @SoNgay <= 30
        SELECT 1 AS CoTheTraHang, 
               N'Hóa đơn trong thời hạn trả hàng (' + CAST(@SoNgay AS NVARCHAR) + N' ngày)' AS ThongBao,
               @SoNgay AS SoNgay;
    ELSE
        SELECT 0 AS CoTheTraHang, 
               N'Hóa đơn đã quá 30 ngày (' + CAST(@SoNgay AS NVARCHAR) + N' ngày), không thể trả hàng' AS ThongBao,
               @SoNgay AS SoNgay;
END
GO
PRINT N'✅ Đã tạo SP sp_CheckHoaDonCoTheTraHang';

-- SP:  Tìm hoặc tạo lô thuốc khi nhập hàng (cộng dồn nếu cùng thuốc + cùng HSD)
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_TimHoacTaoLoThuoc')
    DROP PROCEDURE sp_TimHoacTaoLoThuoc;
GO

CREATE PROCEDURE sp_TimHoacTaoLoThuoc
    @MaThuoc NVARCHAR(50),
    @HanSuDung DATE,
    @SoLuongNhap INT,
    @MaLo NVARCHAR(50) OUTPUT
AS
BEGIN
    -- Tìm lô có cùng mã thuốc và HSD (chưa bị xóa)
    SELECT TOP 1 @MaLo = maLo 
    FROM LoThuoc 
    WHERE maThuoc = @MaThuoc 
      AND hanSuDung = @HanSuDung 
      AND isDeleted = 0;
    
    IF @MaLo IS NOT NULL
    BEGIN
        -- Cộng dồn vào lô hiện có
        UPDATE LoThuoc 
        SET soLuongTon = soLuongTon + @SoLuongNhap,
            ngayNhap = GETDATE(),
            trangThai = CASE 
                WHEN @HanSuDung <= DATEADD(DAY, 30, GETDATE()) THEN N'Sắp hết hạn'
                ELSE N'Còn hạn'
            END
        WHERE maLo = @MaLo;
        PRINT N'Đã cộng dồn ' + CAST(@SoLuongNhap AS NVARCHAR) + N' vào lô:  ' + @MaLo;
    END
    ELSE
    BEGIN
        -- Tạo mã lô mới
        DECLARE @MaxLo NVARCHAR(50);
        DECLARE @NextID INT;
        
        SELECT TOP 1 @MaxLo = maLo FROM LoThuoc ORDER BY maLo DESC;
        
        IF @MaxLo IS NULL
            SET @NextID = 1;
        ELSE
            SET @NextID = CAST(SUBSTRING(@MaxLo, 2, 10) AS INT) + 1;
        
        SET @MaLo = 'L' + RIGHT('000' + CAST(@NextID AS NVARCHAR(10)), 3);
        
        -- Xác định trạng thái
        DECLARE @TrangThai NVARCHAR(50);
        SET @TrangThai = CASE 
            WHEN @HanSuDung <= GETDATE() THEN N'Đã hết hạn'
            WHEN @HanSuDung <= DATEADD(DAY, 30, GETDATE()) THEN N'Sắp hết hạn'
            ELSE N'Còn hạn'
        END;
        
        -- Tạo lô mới
        INSERT INTO LoThuoc (maLo, maThuoc, ngayNhap, hanSuDung, soLuongTon, trangThai, isDeleted)
        VALUES (@MaLo, @MaThuoc, GETDATE(), @HanSuDung, @SoLuongNhap, @TrangThai, 0);
        
        PRINT N'Đã tạo lô mới: ' + @MaLo;
    END
END
GO
PRINT N'✅ Đã tạo SP sp_TimHoacTaoLoThuoc';

-- SP:  Lấy lịch sử mua hàng của khách hàng
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_GetLichSuMuaHang')
    DROP PROCEDURE sp_GetLichSuMuaHang;
GO

CREATE PROCEDURE sp_GetLichSuMuaHang
    @MaKH NVARCHAR(50)
AS
BEGIN
    SELECT 
        hd.maHD,
        hd.ngayTao,
        hd.tongTien,
        hd. thue,
        hd.giamGia,
        (hd.tongTien + hd.thue - hd.giamGia) AS ThucThu,
        hd.hinhThucTT,
        nv.hoTen AS TenNhanVien
    FROM HoaDon hd
    INNER JOIN NhanVien nv ON hd.maNV = nv.maNV
    WHERE hd.maKH = @MaKH
    ORDER BY hd.ngayTao DESC;
END
GO
PRINT N'✅ Đã tạo SP sp_GetLichSuMuaHang';

-- SP: Lấy mã mới cho các bảng (generic)
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_GetNewID')
    DROP PROCEDURE sp_GetNewID;
GO

CREATE PROCEDURE sp_GetNewID
    @TableName NVARCHAR(50),
    @Prefix NVARCHAR(10),
    @NewID NVARCHAR(50) OUTPUT
AS
BEGIN
    DECLARE @MaxID NVARCHAR(50);
    DECLARE @NextNum INT;
    DECLARE @SQL NVARCHAR(500);
    DECLARE @PK NVARCHAR(50);
    
    -- Xác định tên cột khóa chính dựa vào tên bảng
    SET @PK = CASE @TableName
        WHEN 'HoaDon' THEN 'maHD'
        WHEN 'PhieuNhap' THEN 'maPN'
        WHEN 'PhieuTraHang' THEN 'maPT'
        WHEN 'KhachHang' THEN 'maKH'
        WHEN 'NhaCungCap' THEN 'maNCC'
        WHEN 'DonDatHang' THEN 'maDonDat'
        WHEN 'LoThuoc' THEN 'maLo'
        ELSE 'ma'
    END;
    
    SET @SQL = N'SELECT TOP 1 @MaxID = ' + @PK + N' FROM ' + @TableName + N' ORDER BY ' + @PK + N' DESC';
    
    EXEC sp_executesql @SQL, N'@MaxID NVARCHAR(50) OUTPUT', @MaxID OUTPUT;
    
    IF @MaxID IS NULL
        SET @NextNum = 1;
    ELSE
        SET @NextNum = CAST(SUBSTRING(@MaxID, LEN(@Prefix) + 1, 10) AS INT) + 1;
    
    SET @NewID = @Prefix + RIGHT('000' + CAST(@NextNum AS NVARCHAR(10)), 3);
END
GO
PRINT N'✅ Đã tạo SP sp_GetNewID';

-- =====================================================
-- 9. CẬP NHẬT VIEWS
-- =====================================================
GO
-- View hiển thị danh sách thuốc với đầy đủ thông tin (đã cập nhật thêm TonToiThieu)
CREATE OR ALTER VIEW vw_DanhSachThuocFull AS
SELECT 
    t.maThuoc, 
    t.tenThuoc, 
    t.hoatChat, 
    t.donViCoBan, 
    nt.tenNhom,
    t.TonToiThieu,
    -- Giá nhập
    ISNULL((SELECT TOP 1 ctpn.donGia FROM ChiTietPhieuNhap ctpn JOIN PhieuNhap pn ON ctpn.maPN = pn.maPN WHERE ctpn.maThuoc = t.maThuoc ORDER BY pn.ngayTao DESC), 0) AS giaNhap, 
    -- Giá bán
    ISNULL((SELECT TOP 1 ctbg.giaBan FROM ChiTietBangGia ctbg JOIN BangGia bg ON ctbg. maBG = bg.maBG WHERE ctbg.maThuoc = t.maThuoc AND bg.trangThai = 1), 0) AS giaBan, 
    
    -- Tồn kho thực tế
    ISNULL((
        SELECT SUM(soLuongTon) 
        FROM LoThuoc 
        WHERE maThuoc = t.maThuoc AND isDeleted = 0
    ), 0) AS tonKho,

    -- Tồn kho bán được
    ISNULL((
        SELECT SUM(soLuongTon) 
        FROM LoThuoc 
        WHERE maThuoc = t. maThuoc 
          AND isDeleted = 0 
          AND hanSuDung > GETDATE()
          AND (trangThai = N'Còn hạn' OR trangThai = N'Sắp hết hạn')
    ), 0) AS tonKhoBanDuoc,

    t.trangThai
FROM Thuoc t 
JOIN NhomThuoc nt ON t. maNhom = nt.maNhom
GO
PRINT N'✅ Đã cập nhật VIEW vw_DanhSachThuocFull';

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'DonViQuyDoi')
BEGIN
    CREATE TABLE DonViQuyDoi (
        id INT IDENTITY(1,1) PRIMARY KEY,
        maThuoc NVARCHAR(50) NOT NULL,
        tenDonVi NVARCHAR(50) NOT NULL,
        giaTriQuyDoi INT NOT NULL DEFAULT 1,
        giaBan DECIMAL(18, 2) DEFAULT 0,
        laDonViCoBan BIT DEFAULT 0,
        CONSTRAINT FK_DVQD_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc)
    );
    PRINT N'✅ Đã tạo bảng DonViQuyDoi';
END
GO

-- Xóa dữ liệu cũ nếu có
DELETE FROM DonViQuyDoi;
GO

-- Insert dữ liệu đơn vị quy đổi cho các thuốc
INSERT INTO DonViQuyDoi (maThuoc, tenDonVi, giaTriQuyDoi, giaBan, laDonViCoBan) VALUES
-- T001: Amoxicillin 500mg (Vỉ 10 viên, Hộp 10 vỉ)
(N'T001', N'Viên', 1, 1500, 1),
(N'T001', N'Vỉ', 10, 15000, 0),
(N'T001', N'Hộp', 100, 140000, 0),

-- T002: Augmentin 625mg
(N'T002', N'Viên', 1, 22000, 1),
(N'T002', N'Hộp', 10, 220000, 0),

-- T003: Cephalexin 500mg
(N'T003', N'Viên', 1, 1200, 1),
(N'T003', N'Vỉ', 10, 12000, 0),

-- T004: Ciprofloxacin 500mg
(N'T004', N'Viên', 1, 1800, 1),
(N'T004', N'Vỉ', 10, 18000, 0),

-- T005: Azithromycin 250mg
(N'T005', N'Viên', 1, 17000, 1),
(N'T005', N'Hộp', 5, 85000, 0),

-- T006: Panadol Extra
(N'T006', N'Viên', 1, 1850, 1),
(N'T006', N'Vỉ', 12, 22000, 0),
(N'T006', N'Hộp', 120, 185000, 0),

-- T007: Efferalgan 500mg
(N'T007', N'Viên', 1, 5000, 1),

-- T008: Hapacol 250mg
(N'T008', N'Gói', 1, 3500, 1),
(N'T008', N'Hộp', 20, 65000, 0),

-- T009: Ibuprofen 400mg
(N'T009', N'Viên', 1, 2500, 1),
(N'T009', N'Vỉ', 10, 25000, 0),

-- T010: Aspirin 81mg
(N'T010', N'Viên', 1, 500, 1),
(N'T010', N'Vỉ', 30, 15000, 0),

-- T011: Salonpas
(N'T011', N'Miếng', 1, 4000, 1),
(N'T011', N'Hộp', 8, 32000, 0),

-- T012: Vitamin C 500mg
(N'T012', N'Viên', 1, 600, 1),
(N'T012', N'Lọ', 100, 60000, 0),

-- T013: Vitamin E 400IU
(N'T013', N'Viên', 1, 3000, 1),
(N'T013', N'Hộp', 50, 150000, 0),

-- T014: Vitamin 3B
(N'T014', N'Viên', 1, 4500, 1),
(N'T014', N'Vỉ', 10, 45000, 0),

-- T015: Canxi Corbiere
(N'T015', N'Ống', 1, 5000, 1),
(N'T015', N'Hộp', 30, 140000, 0),

-- T016: Berocca
(N'T016', N'Viên', 1, 8500, 1),
(N'T016', N'Tuýp', 10, 85000, 0),

-- T017: Berberin
(N'T017', N'Viên', 1, 200, 1),
(N'T017', N'Lọ', 50, 10000, 0),

-- T018: Smecta
(N'T018', N'Gói', 1, 4000, 1),
(N'T018', N'Hộp', 30, 110000, 0),

-- T019: Omeprazol 20mg
(N'T019', N'Viên', 1, 2200, 1),
(N'T019', N'Vỉ', 10, 22000, 0),

-- T020: Gaviscon
(N'T020', N'Gói', 1, 6500, 1),
(N'T020', N'Hộp', 24, 150000, 0),

-- T021: Enterogermina
(N'T021', N'Ống', 1, 8000, 1),
(N'T021', N'Hộp', 20, 150000, 0),

-- T022: Prospan
(N'T022', N'Chai', 1, 95000, 1),

-- T023: Viên ngậm Bảo Thanh
(N'T023', N'Viên', 1, 3500, 1),
(N'T023', N'Vỉ', 10, 35000, 0),

-- T024: Eugica đỏ
(N'T024', N'Viên', 1, 2750, 1),
(N'T024', N'Hộp', 20, 55000, 0),

-- T025: Methorphan
(N'T025', N'Lọ', 1, 40000, 1),

-- T026: Amlodipin 5mg
(N'T026', N'Viên', 1, 1000, 1),
(N'T026', N'Vỉ', 30, 30000, 0),

-- T027: Losartan 50mg
(N'T027', N'Viên', 1, 1500, 1),
(N'T027', N'Vỉ', 30, 45000, 0),

-- T028: Khẩu trang
(N'T028', N'Cái', 1, 700, 1),
(N'T028', N'Hộp', 50, 35000, 0),

-- T029: Nước muối sinh lý
(N'T029', N'Chai', 1, 5000, 1),
(N'T029', N'Thùng', 24, 110000, 0),

-- T030: Băng cá nhân Urgo
(N'T030', N'Miếng', 1, 500, 1),
(N'T030', N'Hộp', 50, 25000, 0),

-- T031: Cồn 70 độ
(N'T031', N'Chai', 1, 15000, 1);
GO
PRINT N'✅ Đã insert dữ liệu DonViQuyDoi';

IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_GetDonViQuyDoiByMaThuoc')
    DROP PROCEDURE sp_GetDonViQuyDoiByMaThuoc;
GO

CREATE PROCEDURE sp_GetDonViQuyDoiByMaThuoc
    @MaThuoc NVARCHAR(50)
AS
BEGIN
    SELECT id, maThuoc, tenDonVi, giaTriQuyDoi, giaBan, laDonViCoBan
    FROM DonViQuyDoi
    WHERE maThuoc = @MaThuoc
    ORDER BY giaTriQuyDoi ASC;
END
GO
PRINT N'✅ Đã tạo SP sp_GetDonViQuyDoiByMaThuoc';

UPDATE LoThuoc
SET trangThai = CASE 
    WHEN hanSuDung <= GETDATE() THEN N'Đã hết hạn'
    WHEN hanSuDung <= DATEADD(DAY, 30, GETDATE()) THEN N'Sắp hết hạn'
    ELSE N'Còn hạn'
END
WHERE isDeleted = 0;

PRINT N'✅ Đã cập nhật trạng thái lô thuốc';
GO

UPDATE Thuoc SET donViCoBan = N'Viên' WHERE maThuoc IN ('T001', 'T002', 'T003', 'T004', 'T005', 'T006', 'T007', 'T009', 'T010', 'T012', 'T013', 'T014', 'T016', 'T017', 'T019', 'T023', 'T024', 'T026', 'T027');
UPDATE Thuoc SET donViCoBan = N'Gói' WHERE maThuoc IN ('T008', 'T018', 'T020');
UPDATE Thuoc SET donViCoBan = N'Ống' WHERE maThuoc IN ('T015', 'T021');
UPDATE Thuoc SET donViCoBan = N'Miếng' WHERE maThuoc IN ('T011', 'T030');
UPDATE Thuoc SET donViCoBan = N'Chai' WHERE maThuoc IN ('T022', 'T025', 'T029', 'T031');
UPDATE Thuoc SET donViCoBan = N'Cái' WHERE maThuoc = 'T028';

PRINT N'✅ Đã cập nhật đơn vị cơ bản cho Thuốc';
GO

DELETE FROM DonViQuyDoi;
GO

-- Insert dữ liệu đơn vị quy đổi (đơn vị cơ bản có giaTriQuyDoi = 1)
INSERT INTO DonViQuyDoi (maThuoc, tenDonVi, giaTriQuyDoi, giaBan, laDonViCoBan) VALUES
-- ===== NHÓM KHÁNG SINH =====
-- T001: Amoxicillin 500mg (1 Vỉ = 10 Viên, 1 Hộp = 10 Vỉ = 100 Viên)
(N'T001', N'Viên', 1, 1500, 1),
(N'T001', N'Vỉ', 10, 14000, 0),
(N'T001', N'Hộp', 100, 135000, 0),

-- T002: Augmentin 625mg (1 Hộp = 14 Viên)
(N'T002', N'Viên', 1, 16000, 1),
(N'T002', N'Hộp', 14, 220000, 0),

-- T003: Cephalexin 500mg (1 Vỉ = 10 Viên)
(N'T003', N'Viên', 1, 1200, 1),
(N'T003', N'Vỉ', 10, 11000, 0),

-- T004: Ciprofloxacin 500mg (1 Vỉ = 10 Viên)
(N'T004', N'Viên', 1, 1800, 1),
(N'T004', N'Vỉ', 10, 17000, 0),

-- T005: Azithromycin 250mg (1 Hộp = 6 Viên)
(N'T005', N'Viên', 1, 14500, 1),
(N'T005', N'Hộp', 6, 85000, 0),

-- ===== NHÓM GIẢM ĐAU - HẠ SỐT =====
-- T006: Panadol Extra (1 Vỉ = 12 Viên, 1 Hộp = 10 Vỉ = 120 Viên)
(N'T006', N'Viên', 1, 1600, 1),
(N'T006', N'Vỉ', 12, 18000, 0),
(N'T006', N'Hộp', 120, 175000, 0),

-- T007: Efferalgan 500mg (bán lẻ viên)
(N'T007', N'Viên', 1, 5000, 1),

-- T008: Hapacol 250mg (1 Hộp = 24 Gói)
(N'T008', N'Gói', 1, 3500, 1),
(N'T008', N'Hộp', 24, 80000, 0),

-- T009: Ibuprofen 400mg (1 Vỉ = 10 Viên)
(N'T009', N'Viên', 1, 2500, 1),
(N'T009', N'Vỉ', 10, 24000, 0),

-- T010: Aspirin 81mg (1 Vỉ = 30 Viên)
(N'T010', N'Viên', 1, 500, 1),
(N'T010', N'Vỉ', 30, 14000, 0),

-- T011: Salonpas (1 Gói = 10 Miếng, 1 Hộp = 2 Gói = 20 Miếng)
(N'T011', N'Miếng', 1, 1700, 1),
(N'T011', N'Gói', 10, 16000, 0),
(N'T011', N'Hộp', 20, 31000, 0),

-- ===== NHÓM VITAMIN =====
-- T012: Vitamin C 500mg (1 Lọ = 100 Viên)
(N'T012', N'Viên', 1, 600, 1),
(N'T012', N'Lọ', 100, 58000, 0),

-- T013: Vitamin E 400IU (1 Hộp = 30 Viên)
(N'T013', N'Viên', 1, 5000, 1),
(N'T013', N'Hộp', 30, 145000, 0),

-- T014: Vitamin 3B (1 Vỉ = 10 Viên)
(N'T014', N'Viên', 1, 4500, 1),
(N'T014', N'Vỉ', 10, 43000, 0),

-- T015: Canxi Corbiere (1 Hộp = 30 Ống)
(N'T015', N'Ống', 1, 5000, 1),
(N'T015', N'Hộp', 30, 145000, 0),

-- T016: Berocca (1 Tuýp = 10 Viên)
(N'T016', N'Viên', 1, 8500, 1),
(N'T016', N'Tuýp', 10, 82000, 0),

-- ===== NHÓM TIÊU HÓA =====
-- T017: Berberin (1 Lọ = 100 Viên)
(N'T017', N'Viên', 1, 100, 1),
(N'T017', N'Lọ', 100, 9500, 0),

-- T018: Smecta (1 Hộp = 30 Gói)
(N'T018', N'Gói', 1, 4000, 1),
(N'T018', N'Hộp', 30, 115000, 0),

-- T019: Omeprazol 20mg (1 Vỉ = 14 Viên, 1 Hộp = 2 Vỉ = 28 Viên)
(N'T019', N'Viên', 1, 1600, 1),
(N'T019', N'Vỉ', 14, 21000, 0),
(N'T019', N'Hộp', 28, 40000, 0),

-- T020: Gaviscon (1 Hộp = 24 Gói)
(N'T020', N'Gói', 1, 6500, 1),
(N'T020', N'Hộp', 24, 150000, 0),

-- T021: Enterogermina (1 Hộp = 20 Ống)
(N'T021', N'Ống', 1, 8000, 1),
(N'T021', N'Hộp', 20, 155000, 0),

-- ===== NHÓM HÔ HẤP =====
-- T022: Prospan (bán theo Chai)
(N'T022', N'Chai', 1, 95000, 1),

-- T023: Viên ngậm Bảo Thanh (1 Vỉ = 10 Viên)
(N'T023', N'Viên', 1, 3500, 1),
(N'T023', N'Vỉ', 10, 33000, 0),

-- T024: Eugica đỏ (1 Vỉ = 10 Viên, 1 Hộp = 10 Vỉ = 100 Viên)
(N'T024', N'Viên', 1, 550, 1),
(N'T024', N'Vỉ', 10, 5200, 0),
(N'T024', N'Hộp', 100, 50000, 0),

-- T025: Methorphan (bán theo Lọ/Chai)
(N'T025', N'Chai', 1, 40000, 1),

-- ===== NHÓM TIM MẠCH =====
-- T026: Amlodipin 5mg (1 Vỉ = 10 Viên, 1 Hộp = 3 Vỉ = 30 Viên)
(N'T026', N'Viên', 1, 1000, 1),
(N'T026', N'Vỉ', 10, 9500, 0),
(N'T026', N'Hộp', 30, 28000, 0),

-- T027: Losartan 50mg (1 Vỉ = 10 Viên, 1 Hộp = 3 Vỉ = 30 Viên)
(N'T027', N'Viên', 1, 1500, 1),
(N'T027', N'Vỉ', 10, 14500, 0),
(N'T027', N'Hộp', 30, 43000, 0),

-- ===== NHÓM DỤNG CỤ Y TẾ =====
-- T028: Khẩu trang (1 Hộp = 50 Cái)
(N'T028', N'Cái', 1, 700, 1),
(N'T028', N'Hộp', 50, 33000, 0),

-- T029: Nước muối sinh lý (bán theo Chai)
(N'T029', N'Chai', 1, 5000, 1),
(N'T029', N'Thùng', 24, 115000, 0),

-- T030: Băng cá nhân Urgo (1 Hộp = 100 Miếng)
(N'T030', N'Miếng', 1, 250, 1),
(N'T030', N'Hộp', 100, 24000, 0),

-- T031: Cồn 70 độ (bán theo Chai)
(N'T031', N'Chai', 1, 15000, 1);
GO

DELETE FROM ChiTietBangGia;
GO

-- Insert giá cho BG001 (Bảng giá bán lẻ 2024) - TẤT CẢ đơn vị tính
INSERT INTO ChiTietBangGia (maBG, maThuoc, donViTinh, giaBan) VALUES
-- ===== T001: Amoxicillin 500mg =====
(N'BG001', N'T001', N'Viên', 1500),
(N'BG001', N'T001', N'Vỉ', 14000),
(N'BG001', N'T001', N'Hộp', 135000),

-- ===== T002: Augmentin 625mg =====
(N'BG001', N'T002', N'Viên', 16000),
(N'BG001', N'T002', N'Hộp', 220000),

-- ===== T003: Cephalexin 500mg =====
(N'BG001', N'T003', N'Viên', 1200),
(N'BG001', N'T003', N'Vỉ', 11000),

-- ===== T004: Ciprofloxacin 500mg =====
(N'BG001', N'T004', N'Viên', 1800),
(N'BG001', N'T004', N'Vỉ', 17000),

-- ===== T005: Azithromycin 250mg =====
(N'BG001', N'T005', N'Viên', 14500),
(N'BG001', N'T005', N'Hộp', 85000),

-- ===== T006: Panadol Extra =====
(N'BG001', N'T006', N'Viên', 1600),
(N'BG001', N'T006', N'Vỉ', 18000),
(N'BG001', N'T006', N'Hộp', 175000),

-- ===== T007: Efferalgan 500mg =====
(N'BG001', N'T007', N'Viên', 5000),

-- ===== T008: Hapacol 250mg =====
(N'BG001', N'T008', N'Gói', 3500),
(N'BG001', N'T008', N'Hộp', 80000),

-- ===== T009: Ibuprofen 400mg =====
(N'BG001', N'T009', N'Viên', 2500),
(N'BG001', N'T009', N'Vỉ', 24000),

-- ===== T010: Aspirin 81mg =====
(N'BG001', N'T010', N'Viên', 500),
(N'BG001', N'T010', N'Vỉ', 14000),

-- ===== T011: Salonpas =====
(N'BG001', N'T011', N'Miếng', 1700),
(N'BG001', N'T011', N'Gói', 16000),
(N'BG001', N'T011', N'Hộp', 31000),

-- ===== T012: Vitamin C 500mg =====
(N'BG001', N'T012', N'Viên', 600),
(N'BG001', N'T012', N'Lọ', 58000),

-- ===== T013: Vitamin E 400IU =====
(N'BG001', N'T013', N'Viên', 5000),
(N'BG001', N'T013', N'Hộp', 145000),

-- ===== T014: Vitamin 3B =====
(N'BG001', N'T014', N'Viên', 4500),
(N'BG001', N'T014', N'Vỉ', 43000),

-- ===== T015: Canxi Corbiere =====
(N'BG001', N'T015', N'Ống', 5000),
(N'BG001', N'T015', N'Hộp', 145000),

-- ===== T016: Berocca =====
(N'BG001', N'T016', N'Viên', 8500),
(N'BG001', N'T016', N'Tuýp', 82000),

-- ===== T017: Berberin =====
(N'BG001', N'T017', N'Viên', 100),
(N'BG001', N'T017', N'Lọ', 9500),

-- ===== T018: Smecta =====
(N'BG001', N'T018', N'Gói', 4000),
(N'BG001', N'T018', N'Hộp', 115000),

-- ===== T019: Omeprazol 20mg =====
(N'BG001', N'T019', N'Viên', 1600),
(N'BG001', N'T019', N'Vỉ', 21000),
(N'BG001', N'T019', N'Hộp', 40000),

-- ===== T020: Gaviscon =====
(N'BG001', N'T020', N'Gói', 6500),
(N'BG001', N'T020', N'Hộp', 150000),

-- ===== T021: Enterogermina =====
(N'BG001', N'T021', N'Ống', 8000),
(N'BG001', N'T021', N'Hộp', 155000),

-- ===== T022: Prospan =====
(N'BG001', N'T022', N'Chai', 95000),

-- ===== T023: Viên ngậm Bảo Thanh =====
(N'BG001', N'T023', N'Viên', 3500),
(N'BG001', N'T023', N'Vỉ', 33000),

-- ===== T024: Eugica đỏ =====
(N'BG001', N'T024', N'Viên', 550),
(N'BG001', N'T024', N'Vỉ', 5200),
(N'BG001', N'T024', N'Hộp', 50000),

-- ===== T025: Methorphan =====
(N'BG001', N'T025', N'Chai', 40000),

-- ===== T026: Amlodipin 5mg =====
(N'BG001', N'T026', N'Viên', 1000),
(N'BG001', N'T026', N'Vỉ', 9500),
(N'BG001', N'T026', N'Hộp', 28000),

-- ===== T027: Losartan 50mg =====
(N'BG001', N'T027', N'Viên', 1500),
(N'BG001', N'T027', N'Vỉ', 14500),
(N'BG001', N'T027', N'Hộp', 43000),

-- ===== T028: Khẩu trang =====
(N'BG001', N'T028', N'Cái', 700),
(N'BG001', N'T028', N'Hộp', 33000),

-- ===== T029: Nước muối =====
(N'BG001', N'T029', N'Chai', 5000),
(N'BG001', N'T029', N'Thùng', 115000),

-- ===== T030: Băng cá nhân =====
(N'BG001', N'T030', N'Miếng', 250),
(N'BG001', N'T030', N'Hộp', 24000),

-- ===== T031: Cồn 70 độ =====
(N'BG001', N'T031', N'Chai', 15000);
GO

DELETE FROM ChiTietPhieuNhap;
DELETE FROM PhieuNhap;
DELETE FROM LoThuoc;
GO

-- Insert lại lô thuốc (số lượng tính theo đơn vị cơ bản - viên/gói/ống...)
INSERT INTO LoThuoc (maLo, maThuoc, ngayNhap, hanSuDung, soLuongTon, trangThai, isDeleted) VALUES
-- Nhóm Kháng sinh (tồn theo VIÊN)
(N'L001', N'T001', '2024-01-10', '2026-01-10', 5000, N'Còn hạn', 0),      -- 5000 viên = 50 vỉ = 5 hộp
(N'L002', N'T002', '2024-05-20', '2026-05-20', 1400, N'Còn hạn', 0),      -- 1400 viên = 100 hộp
(N'L003', N'T003', '2023-06-15', '2024-12-01', 500, N'Đã hết hạn', 0),    -- Đã hết hạn
(N'L004', N'T004', '2024-08-01', '2026-08-01', 2000, N'Còn hạn', 0),      -- 2000 viên
(N'L005', N'T005', '2024-09-10', '2026-09-10', 900, N'Còn hạn', 0),       -- 900 viên = 150 hộp

-- Nhóm Giảm đau (Panadol bán chạy)
(N'L006', N'T006', '2024-10-01', '2026-10-01', 12000, N'Còn hạn', 0),     -- 12000 viên = 100 hộp
(N'L007', N'T006', '2023-12-01', '2025-01-15', 240, N'Sắp hết hạn', 0),   -- 240 viên = 2 hộp (ưu tiên bán trước)
(N'L008', N'T007', '2024-02-15', '2026-02-15', 300, N'Còn hạn', 0),       -- 300 viên
(N'L009', N'T008', '2024-03-20', '2026-03-20', 50, N'Còn hạn', 0),        -- 50 gói (tồn thấp)
(N'L010', N'T009', '2024-07-07', '2026-07-07', 4000, N'Còn hạn', 0),      -- 4000 viên
(N'L011', N'T010', '2023-01-01', '2024-01-01', 0, N'Đã hết hạn', 0),      -- Hết hàng
(N'L012', N'T011', '2024-06-01', '2026-06-01', 400, N'Còn hạn', 0),       -- 400 miếng = 20 hộp

-- Nhóm Vitamin
(N'L013', N'T012', '2024-11-11', '2026-11-11', 5000, N'Còn hạn', 0),      -- 5000 viên = 50 lọ
(N'L014', N'T013', '2024-05-05', '2026-05-05', 2500, N'Còn hạn', 0),      -- 2500 viên
(N'L015', N'T014', '2024-09-09', '2025-01-20', 1500, N'Sắp hết hạn', 0),  -- 1500 viên (sắp hết hạn)
(N'L016', N'T015', '2024-12-01', '2026-06-01', 800, N'Còn hạn', 0),       -- 800 ống
(N'L017', N'T016', '2024-10-20', '2026-10-20', 600, N'Còn hạn', 0),       -- 600 viên = 60 tuýp

-- Nhóm Tiêu hóa
(N'L018', N'T017', '2024-04-30', '2026-04-30', 3000, N'Còn hạn', 0),      -- 3000 viên = 30 lọ
(N'L019', N'T018', '2024-08-15', '2026-02-26', 1200, N'Sắp hết hạn', 0),  -- 1200 gói = 40 hộp
(N'L020', N'T019', '2024-06-01', '2026-06-01', 1000, N'Còn hạn', 0),      -- 1000 viên
(N'L021', N'T020', '2024-02-28', '2025-02-28', 100, N'Sắp hết hạn', 0),   -- 100 gói (tồn thấp)
(N'L022', N'T021', '2024-07-15', '2026-07-15', 400, N'Còn hạn', 0),       -- 400 ống = 20 hộp

-- Nhóm Hô hấp
(N'L023', N'T022', '2024-09-15', '2026-09-15', 80, N'Còn hạn', 0),        -- 80 chai
(N'L024', N'T023', '2024-01-20', '2025-01-20', 2000, N'Sắp hết hạn', 0),  -- 2000 viên
(N'L025', N'T024', '2024-11-01', '2026-11-01', 1500, N'Còn hạn', 0),      -- 1500 viên
(N'L026', N'T025', '2024-08-01', '2026-08-01', 50, N'Còn hạn', 0),        -- 50 chai

-- Nhóm Tim mạch
(N'L027', N'T026', '2024-06-01', '2026-06-01', 3000, N'Còn hạn', 0),      -- 3000 viên = 100 hộp
(N'L028', N'T027', '2024-07-01', '2026-07-01', 3000, N'Còn hạn', 0),      -- 3000 viên = 100 hộp

-- Nhóm Dụng cụ
(N'L029', N'T028', '2024-05-10', '2028-05-10', 10000, N'Còn hạn', 0),     -- 10000 cái = 200 hộp
(N'L030', N'T029', '2024-07-20', '2026-07-20', 500, N'Còn hạn', 0),       -- 500 chai
(N'L031', N'T030', '2024-03-15', '2027-03-15', 3000, N'Còn hạn', 0),      -- 3000 miếng = 30 hộp
(N'L032', N'T031', '2024-04-01', '2026-04-01', 100, N'Còn hạn', 0);       -- 100 chai
GO

PRINT N'✅ Đã cập nhật bảng LoThuoc';

-- =====================================================
-- 5. INSERT LẠI PHIẾU NHẬP VÀ CHI TIẾT
-- =====================================================
PRINT N'🔄 Đang cập nhật PhieuNhap và ChiTietPhieuNhap...';

INSERT INTO PhieuNhap (maPN, ngayTao, tongTien, trangThai, maNV, maNCC) VALUES
(N'PN001', '2024-01-10', 5000000, N'Đã nhập', N'NV003', N'NCC001'),
(N'PN002', '2024-05-20', 17600000, N'Đã nhập', N'NV003', N'NCC001'),
(N'PN003', '2024-08-01', 2800000, N'Đã nhập', N'NV003', N'NCC001'),
(N'PN004', '2024-09-10', 10800000, N'Đã nhập', N'NV003', N'NCC001'),
(N'PN005', '2024-10-01', 14400000, N'Đã nhập', N'NV003', N'NCC002'),
(N'PN006', '2024-02-15', 1200000, N'Đã nhập', N'NV003', N'NCC002'),
(N'PN007', '2024-03-20', 140000, N'Đã nhập', N'NV003', N'NCC002'),
(N'PN008', '2024-07-07', 8000000, N'Đã nhập', N'NV003', N'NCC002'),
(N'PN009', '2024-06-01', 560000, N'Đã nhập', N'NV003', N'NCC004'),
(N'PN010', '2024-11-11', 2250000, N'Đã nhập', N'NV003', N'NCC003'),
(N'PN011', '2024-05-05', 11250000, N'Đã nhập', N'NV003', N'NCC003'),
(N'PN012', '2024-09-09', 6000000, N'Đã nhập', N'NV003', N'NCC003'),
(N'PN013', '2024-12-01', 2800000, N'Đã nhập', N'NV003', N'NCC003'),
(N'PN014', '2024-10-20', 4200000, N'Đã nhập', N'NV003', N'NCC003'),
(N'PN015', '2024-04-30', 210000, N'Đã nhập', N'NV003', N'NCC004'),
(N'PN016', '2024-08-15', 3000000, N'Đã nhập', N'NV003', N'NCC004'),
(N'PN017', '2024-06-01', 1200000, N'Đã nhập', N'NV003', N'NCC004'),
(N'PN018', '2024-02-28', 500000, N'Đã nhập', N'NV003', N'NCC004'),
(N'PN019', '2024-07-15', 2800000, N'Đã nhập', N'NV003', N'NCC004'),
(N'PN020', '2024-09-15', 6080000, N'Đã nhập', N'NV003', N'NCC005'),
(N'PN021', '2024-01-20', 6000000, N'Đã nhập', N'NV003', N'NCC005'),
(N'PN022', '2024-11-01', 675000, N'Đã nhập', N'NV003', N'NCC005'),
(N'PN023', '2024-08-01', 1600000, N'Đã nhập', N'NV003', N'NCC005'),
(N'PN024', '2024-06-01', 2400000, N'Đã nhập', N'NV003', N'NCC006'),
(N'PN025', '2024-07-01', 3600000, N'Đã nhập', N'NV003', N'NCC006'),
(N'PN026', '2024-05-10', 5000000, N'Đã nhập', N'NV003', N'NCC007'),
(N'PN027', '2024-07-20', 2000000, N'Đã nhập', N'NV003', N'NCC007'),
(N'PN028', '2024-03-15', 450000, N'Đã nhập', N'NV003', N'NCC007'),
(N'PN029', '2024-04-01', 1000000, N'Đã nhập', N'NV003', N'NCC007');
GO

INSERT INTO ChiTietPhieuNhap (maPN, maThuoc, maLo, hanSuDung, soLuong, donGia, thanhTien) VALUES
(N'PN001', N'T001', N'L001', '2026-01-10', 5000, 1000, 5000000),
(N'PN002', N'T002', N'L002', '2026-05-20', 1400, 12571, 17600000),
(N'PN003', N'T004', N'L004', '2026-08-01', 2000, 1400, 2800000),
(N'PN004', N'T005', N'L005', '2026-09-10', 900, 12000, 10800000),
(N'PN005', N'T006', N'L006', '2026-10-01', 12000, 1200, 14400000),
(N'PN006', N'T007', N'L008', '2026-02-15', 300, 4000, 1200000),
(N'PN007', N'T008', N'L009', '2026-03-20', 50, 2800, 140000),
(N'PN008', N'T009', N'L010', '2026-07-07', 4000, 2000, 8000000),
(N'PN009', N'T011', N'L012', '2026-06-01', 400, 1400, 560000),
(N'PN010', N'T012', N'L013', '2026-11-11', 5000, 450, 2250000),
(N'PN011', N'T013', N'L014', '2026-05-05', 2500, 4500, 11250000),
(N'PN012', N'T014', N'L015', '2025-01-20', 1500, 4000, 6000000),
(N'PN013', N'T015', N'L016', '2026-06-01', 800, 3500, 2800000),
(N'PN014', N'T016', N'L017', '2026-10-20', 600, 7000, 4200000),
(N'PN015', N'T017', N'L018', '2026-04-30', 3000, 70, 210000),
(N'PN016', N'T018', N'L019', '2026-02-26', 1200, 2500, 3000000),
(N'PN017', N'T019', N'L020', '2026-06-01', 1000, 1200, 1200000),
(N'PN018', N'T020', N'L021', '2025-02-28', 100, 5000, 500000),
(N'PN019', N'T021', N'L022', '2026-07-15', 400, 7000, 2800000),
(N'PN020', N'T022', N'L023', '2026-09-15', 80, 76000, 6080000),
(N'PN021', N'T023', N'L024', '2025-01-20', 2000, 3000, 6000000),
(N'PN022', N'T024', N'L025', '2026-11-01', 1500, 450, 675000),
(N'PN023', N'T025', N'L026', '2026-08-01', 50, 32000, 1600000),
(N'PN024', N'T026', N'L027', '2026-06-01', 3000, 800, 2400000),
(N'PN025', N'T027', N'L028', '2026-07-01', 3000, 1200, 3600000),
(N'PN026', N'T028', N'L029', '2028-05-10', 10000, 500, 5000000),
(N'PN027', N'T029', N'L030', '2026-07-20', 500, 4000, 2000000),
(N'PN028', N'T030', N'L031', '2027-03-15', 3000, 150, 450000),
(N'PN029', N'T031', N'L032', '2026-04-01', 100, 10000, 1000000);
GO

PRINT N'✅ Đã cập nhật PhieuNhap và ChiTietPhieuNhap';

-- =====================================================
-- 6. CẬP NHẬT TRẠNG THÁI LÔ THUỐC
-- =====================================================
UPDATE LoThuoc
SET trangThai = CASE 
    WHEN hanSuDung <= GETDATE() THEN N'Đã hết hạn'
    WHEN hanSuDung <= DATEADD(DAY, 30, GETDATE()) THEN N'Sắp hết hạn'
    ELSE N'Còn hạn'
END
WHERE isDeleted = 0;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'DonViQuyDoi') AND name = 'trangThai')
BEGIN
    ALTER TABLE DonViQuyDoi ADD trangThai BIT DEFAULT 1;
    PRINT N'✅ Đã thêm cột trangThai vào bảng DonViQuyDoi';
END
GO

-- Cập nhật tất cả các dòng hiện có thành active
UPDATE DonViQuyDoi SET trangThai = 1 WHERE trangThai IS NULL;
GO
ALTER TABLE NhaCungCap
DROP COLUMN nguoiLienHe