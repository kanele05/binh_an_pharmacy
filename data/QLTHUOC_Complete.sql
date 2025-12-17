-- =====================================================
-- SCRIPT DATABASE HOÀN CHỈNH - HỆ THỐNG QUẢN LÝ NHÀ THUỐC
-- Database: QLTHUOC
-- Version: 1.0
-- Date: 2025-12-16
-- =====================================================

-- =====================================================
-- PHẦN 1: TẠO DATABASE VÀ CẤU TRÚC BẢNG
-- =====================================================

CREATE DATABASE QLTHUOC
GO
USE QLTHUOC
GO

-- -----------------------------------------------------
-- 1.1 Bảng NhanVien (Nhân viên)
-- -----------------------------------------------------
CREATE TABLE NhanVien (
    maNV NVARCHAR(50) PRIMARY KEY,
    hoTen NVARCHAR(100),
    ngaySinh DATE,
    gioiTinh BIT,                    -- 1: Nam, 0: Nữ
    sdt NVARCHAR(20),
    email NVARCHAR(100),
    diaChi NVARCHAR(100),
    matKhau NVARCHAR(255),
    vaiTro BIT,                      -- 1: Quản lý, 0: Nhân viên
    trangThai BIT                    -- 1: Đang làm, 0: Nghỉ việc
);

-- -----------------------------------------------------
-- 1.2 Bảng KhachHang (Khách hàng)
-- -----------------------------------------------------
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

-- -----------------------------------------------------
-- 1.3 Bảng NhaCungCap (Nhà cung cấp)
-- -----------------------------------------------------
CREATE TABLE NhaCungCap (
    maNCC NVARCHAR(50) PRIMARY KEY,
    tenNCC NVARCHAR(100),
    sdt NVARCHAR(20),
    email NVARCHAR(100),
    diaChi NVARCHAR(255),
    trangThai BIT DEFAULT 1
);

-- -----------------------------------------------------
-- 1.4 Bảng NhomThuoc (Nhóm thuốc)
-- -----------------------------------------------------
CREATE TABLE NhomThuoc (
    maNhom NVARCHAR(50) PRIMARY KEY,
    tenNhom NVARCHAR(100)
);

-- -----------------------------------------------------
-- 1.5 Bảng Thuoc (Thuốc)
-- -----------------------------------------------------
CREATE TABLE Thuoc (
    maThuoc NVARCHAR(50) PRIMARY KEY,
    tenThuoc NVARCHAR(100),
    hoatChat NVARCHAR(255),
    donViCoBan NVARCHAR(50),
    trangThai BIT,
    TonToiThieu INT DEFAULT 10,
    maNhom NVARCHAR(50),
    CONSTRAINT FK_Thuoc_NhomThuoc FOREIGN KEY (maNhom) REFERENCES NhomThuoc(maNhom)
);

-- -----------------------------------------------------
-- 1.6 Bảng LoThuoc (Lô thuốc - Quản lý tồn kho & HSD)
-- -----------------------------------------------------
CREATE TABLE LoThuoc (
    maLo NVARCHAR(50) PRIMARY KEY,
    maThuoc NVARCHAR(50),
    ngayNhap DATE,
    hanSuDung DATE,
    soLuongTon INT,
    trangThai NVARCHAR(50),
    isDeleted BIT,
    CONSTRAINT FK_LoThuoc_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc)
);

-- -----------------------------------------------------
-- 1.7 Bảng DonViQuyDoi (Đơn vị quy đổi cho thuốc)
-- -----------------------------------------------------
CREATE TABLE DonViQuyDoi (
    id INT IDENTITY(1,1) PRIMARY KEY,
    maThuoc NVARCHAR(50) NOT NULL,
    tenDonVi NVARCHAR(50) NOT NULL,
    giaTriQuyDoi INT NOT NULL DEFAULT 1,
    giaBan DECIMAL(18, 2) DEFAULT 0,
    laDonViCoBan BIT DEFAULT 0,
    trangThai BIT DEFAULT 1,
    CONSTRAINT FK_DVQD_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc)
);

-- -----------------------------------------------------
-- 1.8 Bảng BangGia (Bảng giá)
-- -----------------------------------------------------
CREATE TABLE BangGia (
    maBG NVARCHAR(50) PRIMARY KEY,
    tenBG NVARCHAR(100),
    ngayHieuLuc DATE,
    ngayKetThuc DATE,
    ghiChu NVARCHAR(200),
    trangThai BIT
);

-- -----------------------------------------------------
-- 1.9 Bảng ChiTietBangGia (Chi tiết bảng giá)
-- -----------------------------------------------------
CREATE TABLE ChiTietBangGia (
    maBG NVARCHAR(50),
    maThuoc NVARCHAR(50),
    donViTinh NVARCHAR(50),
    giaBan DECIMAL(18, 2),
    PRIMARY KEY (maBG, maThuoc, donViTinh),
    CONSTRAINT FK_CTBG_BangGia FOREIGN KEY (maBG) REFERENCES BangGia(maBG),
    CONSTRAINT FK_CTBG_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc)
);

-- -----------------------------------------------------
-- 1.10 Bảng HoaDon (Hóa đơn bán hàng)
-- -----------------------------------------------------
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

-- -----------------------------------------------------
-- 1.11 Bảng ChiTietHoaDon (Chi tiết hóa đơn)
-- -----------------------------------------------------
CREATE TABLE ChiTietHoaDon (
    maHD NVARCHAR(50),
    maThuoc NVARCHAR(50),
    maLo NVARCHAR(50),
    soLuong INT,
    donGia DECIMAL(18, 2),
    thanhTien DECIMAL(18, 2),
    donViTinh NVARCHAR(50) NULL,
    PRIMARY KEY (maHD, maThuoc, maLo),
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
    CONSTRAINT FK_CTHD_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc),
    CONSTRAINT FK_CTHD_LoThuoc FOREIGN KEY (maLo) REFERENCES LoThuoc(maLo)
);

-- -----------------------------------------------------
-- 1.12 Bảng DonDatHang (Đơn đặt hàng)
-- -----------------------------------------------------
CREATE TABLE DonDatHang (
    maDonDat NVARCHAR(50) PRIMARY KEY,
    tenKhach NVARCHAR(100),
    sdtLienHe NVARCHAR(20),
    gioHenLay DATETIME,
    tongTien DECIMAL(18, 2),
    ghiChu NVARCHAR(255),
    trangThai NVARCHAR(50),
    maNV NVARCHAR(50),
    maKH NVARCHAR(50),
    CONSTRAINT FK_DonDat_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_DonDat_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);

-- -----------------------------------------------------
-- 1.13 Bảng ChiTietDonDat (Chi tiết đơn đặt)
-- -----------------------------------------------------
CREATE TABLE ChiTietDonDat (
    maDonDat NVARCHAR(50),
    maThuoc NVARCHAR(50),
    soLuong INT,
    donGia DECIMAL(18, 2),
    thanhTien DECIMAL(18, 2),
    donViTinh NVARCHAR(50) NULL,
    PRIMARY KEY (maDonDat, maThuoc),
    CONSTRAINT FK_CTDD_DonDat FOREIGN KEY (maDonDat) REFERENCES DonDatHang(maDonDat),
    CONSTRAINT FK_CTDD_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc)
);

-- -----------------------------------------------------
-- 1.14 Bảng PhieuNhap (Phiếu nhập hàng)
-- -----------------------------------------------------
CREATE TABLE PhieuNhap (
    maPN NVARCHAR(50) PRIMARY KEY,
    ngayTao DATE,
    tongTien DECIMAL(18, 2),
    trangThai NVARCHAR(50),
    ghiChu NVARCHAR(500) NULL,
    maNV NVARCHAR(50),
    maNCC NVARCHAR(50),
    CONSTRAINT FK_PhieuNhap_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PhieuNhap_NhaCungCap FOREIGN KEY (maNCC) REFERENCES NhaCungCap(maNCC)
);

-- -----------------------------------------------------
-- 1.15 Bảng ChiTietPhieuNhap (Chi tiết phiếu nhập)
-- -----------------------------------------------------
CREATE TABLE ChiTietPhieuNhap (
    maPN NVARCHAR(50),
    maThuoc NVARCHAR(50),
    maLo NVARCHAR(50),
    hanSuDung DATE,
    soLuong INT,
    donGia DECIMAL(18, 2),
    thanhTien DECIMAL(18, 2),
    donViTinh NVARCHAR(50) NULL,
    PRIMARY KEY (maPN, maThuoc, maLo),
    CONSTRAINT FK_CTPN_PhieuNhap FOREIGN KEY (maPN) REFERENCES PhieuNhap(maPN),
    CONSTRAINT FK_CTPN_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc),
    CONSTRAINT FK_CTPN_LoThuoc FOREIGN KEY (maLo) REFERENCES LoThuoc(maLo)
);

-- -----------------------------------------------------
-- 1.16 Bảng PhieuTraHang (Phiếu trả hàng)
-- -----------------------------------------------------
CREATE TABLE PhieuTraHang (
    maPT NVARCHAR(50) PRIMARY KEY,
    ngayTra DATE,
    tongTienHoanTra DECIMAL(18, 2),
    lyDo NVARCHAR(255),
    trangThai NVARCHAR(50) DEFAULT N'Đã trả',
    ghiChu NVARCHAR(500) NULL,
    maHD NVARCHAR(50),
    maNV NVARCHAR(50),
    maKH NVARCHAR(50),
    CONSTRAINT FK_PhieuTra_HoaDon FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
    CONSTRAINT FK_PhieuTra_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PhieuTra_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);

-- -----------------------------------------------------
-- 1.17 Bảng ChiTietPhieuTra (Chi tiết phiếu trả)
-- -----------------------------------------------------
CREATE TABLE ChiTietPhieuTra (
    maPT NVARCHAR(50),
    maThuoc NVARCHAR(50),
    maLo NVARCHAR(50),
    soLuongTra INT,
    donGiaTra DECIMAL(18, 2),
    thanhTienHoanTra DECIMAL(18, 2),
    donViTinh NVARCHAR(50) NULL,
    PRIMARY KEY (maPT, maThuoc, maLo),
    CONSTRAINT FK_CTPT_PhieuTra FOREIGN KEY (maPT) REFERENCES PhieuTraHang(maPT),
    CONSTRAINT FK_CTPT_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc),
    CONSTRAINT FK_CTPT_LoThuoc FOREIGN KEY (maLo) REFERENCES LoThuoc(maLo)
);

-- -----------------------------------------------------
-- 1.18 Bảng PhieuDoiHang (Phiếu đổi hàng)
-- -----------------------------------------------------
CREATE TABLE PhieuDoiHang (
    maPD NVARCHAR(50) PRIMARY KEY,
    ngayDoi DATE DEFAULT GETDATE(),
    tongTienTra DECIMAL(18, 2),      -- Tổng tiền hàng trả lại
    tongTienMoi DECIMAL(18, 2),      -- Tổng tiền hàng mới
    chenhLech DECIMAL(18, 2),        -- Chênh lệch = tongTienMoi - tongTienTra
    lyDo NVARCHAR(255),
    maHDGoc NVARCHAR(50),            -- Hóa đơn gốc
    maPT NVARCHAR(50),               -- Phiếu trả hàng được tạo
    maHDMoi NVARCHAR(50),            -- Hóa đơn mới được tạo
    maNV NVARCHAR(50),
    maKH NVARCHAR(50),
    trangThai NVARCHAR(50),
    ghiChu NVARCHAR(255),
    CONSTRAINT FK_PhieuDoiHang_HoaDonGoc FOREIGN KEY (maHDGoc) REFERENCES HoaDon(maHD),
    CONSTRAINT FK_PhieuDoiHang_PhieuTra FOREIGN KEY (maPT) REFERENCES PhieuTraHang(maPT),
    CONSTRAINT FK_PhieuDoiHang_HoaDonMoi FOREIGN KEY (maHDMoi) REFERENCES HoaDon(maHD),
    CONSTRAINT FK_PhieuDoiHang_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PhieuDoiHang_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);
GO

-- =====================================================
-- PHẦN 2: TẠO VIEWS
-- =====================================================

-- -----------------------------------------------------
-- 2.1 View danh sách thuốc đầy đủ thông tin
-- -----------------------------------------------------
CREATE OR ALTER VIEW vw_DanhSachThuocFull AS
SELECT
    t.maThuoc,
    t.tenThuoc,
    t.hoatChat,
    t.donViCoBan,
    nt.tenNhom,
    t.TonToiThieu,
    -- Giá nhập theo đơn vị cơ bản
    ISNULL((
        SELECT TOP 1 
            ROUND(
                CASE 
                    WHEN ctpn.donViTinh = t.donViCoBan THEN ctpn.donGia
                    ELSE ctpn.donGia / NULLIF((
                        SELECT TOP 1 dv.giaTriQuyDoi 
                        FROM DonViQuyDoi dv 
                        WHERE dv.maThuoc = t.maThuoc AND dv.tenDonVi = ctpn.donViTinh
                    ), 0)
                END, 
            2)
        FROM ChiTietPhieuNhap ctpn 
        JOIN PhieuNhap pn ON ctpn.maPN = pn.maPN 
        WHERE ctpn.maThuoc = t.maThuoc 
        ORDER BY pn.ngayTao DESC 
    ), 0) AS giaNhap,
    -- Giá bán theo đơn vị cơ bản
    ISNULL((SELECT TOP 1 ctbg.giaBan
            FROM ChiTietBangGia ctbg
            JOIN BangGia bg ON ctbg.maBG = bg.maBG
            WHERE ctbg.maThuoc = t.maThuoc
              AND ctbg.donViTinh = t.donViCoBan
              AND bg.trangThai = 1), 0) AS giaBan,
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
        WHERE maThuoc = t.maThuoc
          AND isDeleted = 0
          AND hanSuDung > GETDATE()
          AND (trangThai = N'Còn hạn' OR trangThai = N'Sắp hết hạn')
    ), 0) AS tonKhoBanDuoc,
    t.trangThai
FROM Thuoc t
JOIN NhomThuoc nt ON t.maNhom = nt.maNhom
GO

-- -----------------------------------------------------
-- 2.2 View thuốc bán hàng
-- -----------------------------------------------------
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

-- =====================================================
-- PHẦN 3: TẠO STORED PROCEDURES
-- =====================================================

-- -----------------------------------------------------
-- 3.1 SP Tạo bảng giá mới
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_TaoBangGiaMoi
    @TenBG NVARCHAR(100),
    @NgayHieuLuc DATE,
    @GhiChu NVARCHAR(200),
    @NewMaBG NVARCHAR(50) OUTPUT
AS
BEGIN
    UPDATE BangGia
    SET trangThai = 0, ngayKetThuc = GETDATE()
    WHERE trangThai = 1;

    DECLARE @MaxID NVARCHAR(50);
    DECLARE @NextID INT;

    SELECT TOP 1 @MaxID = maBG FROM BangGia ORDER BY maBG DESC;

    IF @MaxID IS NULL
        SET @NextID = 1;
    ELSE
        SET @NextID = CAST(SUBSTRING(@MaxID, 3, 10) AS INT) + 1;

    SET @NewMaBG = 'BG' + RIGHT('000' + CAST(@NextID AS NVARCHAR(10)), 3);

    INSERT INTO BangGia (maBG, tenBG, ngayHieuLuc, ngayKetThuc, ghiChu, trangThai)
    VALUES (@NewMaBG, @TenBG, @NgayHieuLuc, NULL, @GhiChu, 1);
END
GO

-- -----------------------------------------------------
-- 3.2 SP Lấy tổng tồn kho theo mã thuốc
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_GetTongTonByMaThuoc
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

-- -----------------------------------------------------
-- 3.3 SP Lấy tồn kho theo lô
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_GetTonKhoByMaLo
    @MaLo NVARCHAR(50)
AS
BEGIN
    SELECT soLuongTon
    FROM LoThuoc
    WHERE maLo = @MaLo
      AND isDeleted = 0;
END
GO

-- -----------------------------------------------------
-- 3.4 SP Lấy thuốc sắp hết hạn
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_GetThuocSapHetHan
    @SoNgay INT = 30
AS
BEGIN
    SELECT
        l.maLo,
        t.maThuoc,
        t.tenThuoc,
        t.donViCoBan,
        l.soLuongTon,
        l.hanSuDung,
        DATEDIFF(DAY, GETDATE(), l.hanSuDung) AS SoNgayConLai
    FROM LoThuoc l
    INNER JOIN Thuoc t ON l.maThuoc = t.maThuoc
    WHERE l.isDeleted = 0
      AND l.soLuongTon > 0
      AND l.hanSuDung <= DATEADD(DAY, @SoNgay, GETDATE())
      AND l.hanSuDung > GETDATE()
    ORDER BY l.hanSuDung ASC;
END
GO

-- -----------------------------------------------------
-- 3.5 SP Lấy thuốc tồn thấp
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_GetThuocTonThap
AS
BEGIN
    SELECT
        t.maThuoc,
        t.tenThuoc,
        t.donViCoBan,
        t.TonToiThieu,
        COALESCE(SUM(l.soLuongTon), 0) AS TongTon,
        t.TonToiThieu - COALESCE(SUM(l.soLuongTon), 0) AS CanNhapThem
    FROM Thuoc t
    LEFT JOIN LoThuoc l ON t.maThuoc = l.maThuoc
        AND l.isDeleted = 0
        AND l.hanSuDung > GETDATE()
    WHERE t.trangThai = 1
    GROUP BY t.maThuoc, t.tenThuoc, t.donViCoBan, t.TonToiThieu
    HAVING COALESCE(SUM(l.soLuongTon), 0) < t.TonToiThieu
    ORDER BY (t.TonToiThieu - COALESCE(SUM(l.soLuongTon), 0)) DESC;
END
GO

-- -----------------------------------------------------
-- 3.6 SP Lấy doanh thu
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_GetDoanhThu
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

-- -----------------------------------------------------
-- 3.7 SP Lấy top thuốc bán chạy
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_GetTopThuocBanChay
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

-- -----------------------------------------------------
-- 3.8 SP Thống kê Dashboard
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_GetDashboardStats
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

    -- Doanh thu tuần này
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

-- -----------------------------------------------------
-- 3.9 SP Kiểm tra hóa đơn có thể trả hàng
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_CheckHoaDonCoTheTraHang
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

-- -----------------------------------------------------
-- 3.10 SP Tìm hoặc tạo lô thuốc
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_TimHoacTaoLoThuoc
    @MaThuoc NVARCHAR(50),
    @HanSuDung DATE,
    @SoLuongNhap INT,
    @MaLo NVARCHAR(50) OUTPUT
AS
BEGIN
    -- Tìm lô có cùng mã thuốc và HSD
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
    END
END
GO

-- -----------------------------------------------------
-- 3.11 SP Tìm hoặc tạo lô thuốc v2 (có hỗ trợ đơn vị tính)
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_TimHoacTaoLoThuoc_v2
    @MaThuoc NVARCHAR(50),
    @HanSuDung DATE,
    @SoLuong INT,
    @DonViTinh NVARCHAR(50),
    @MaLo NVARCHAR(50) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    -- Lấy giá trị quy đổi
    DECLARE @GiaTriQuyDoi INT = 1;
    SELECT @GiaTriQuyDoi = ISNULL(giaTriQuyDoi, 1)
    FROM DonViQuyDoi
    WHERE maThuoc = @MaThuoc AND tenDonVi = @DonViTinh;

    -- Tính số lượng theo đơn vị cơ bản
    DECLARE @SoLuongCoBan INT = @SoLuong * @GiaTriQuyDoi;

    -- Tìm lô có cùng mã thuốc và HSD
    SELECT TOP 1 @MaLo = maLo
    FROM LoThuoc
    WHERE maThuoc = @MaThuoc
      AND hanSuDung = @HanSuDung
      AND isDeleted = 0;

    IF @MaLo IS NOT NULL
    BEGIN
        UPDATE LoThuoc
        SET soLuongTon = soLuongTon + @SoLuongCoBan,
            ngayNhap = GETDATE(),
            trangThai = CASE
                WHEN @HanSuDung <= DATEADD(DAY, 30, GETDATE()) THEN N'Sắp hết hạn'
                ELSE N'Còn hạn'
            END
        WHERE maLo = @MaLo;
    END
    ELSE
    BEGIN
        DECLARE @MaxLo NVARCHAR(50);
        DECLARE @NextID INT;

        SELECT TOP 1 @MaxLo = maLo FROM LoThuoc ORDER BY maLo DESC;

        IF @MaxLo IS NULL
            SET @NextID = 1;
        ELSE
            SET @NextID = CAST(SUBSTRING(@MaxLo, 2, 10) AS INT) + 1;

        SET @MaLo = 'L' + RIGHT('000' + CAST(@NextID AS NVARCHAR(10)), 3);

        DECLARE @TrangThai NVARCHAR(50);
        SET @TrangThai = CASE
            WHEN @HanSuDung <= GETDATE() THEN N'Đã hết hạn'
            WHEN @HanSuDung <= DATEADD(DAY, 30, GETDATE()) THEN N'Sắp hết hạn'
            ELSE N'Còn hạn'
        END;

        INSERT INTO LoThuoc (maLo, maThuoc, ngayNhap, hanSuDung, soLuongTon, trangThai, isDeleted)
        VALUES (@MaLo, @MaThuoc, GETDATE(), @HanSuDung, @SoLuongCoBan, @TrangThai, 0);
    END
END
GO

-- -----------------------------------------------------
-- 3.12 SP Tìm hoặc tạo lô thuốc không cộng tồn
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_TimHoacTaoLoThuoc_KhongCongTon
    @MaThuoc NVARCHAR(50),
    @HanSuDung DATE,
    @MaLo NVARCHAR(50) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP 1 @MaLo = maLo
    FROM LoThuoc
    WHERE maThuoc = @MaThuoc
      AND hanSuDung = @HanSuDung
      AND isDeleted = 0;

    IF @MaLo IS NULL
    BEGIN
        DECLARE @MaxLo NVARCHAR(50);
        DECLARE @NextID INT;

        SELECT TOP 1 @MaxLo = maLo FROM LoThuoc ORDER BY maLo DESC;

        IF @MaxLo IS NULL
            SET @NextID = 1;
        ELSE
            SET @NextID = CAST(SUBSTRING(@MaxLo, 2, 10) AS INT) + 1;

        SET @MaLo = 'L' + RIGHT('000' + CAST(@NextID AS NVARCHAR(10)), 3);

        DECLARE @TrangThai NVARCHAR(50);
        SET @TrangThai = CASE
            WHEN @HanSuDung <= GETDATE() THEN N'Đã hết hạn'
            WHEN @HanSuDung <= DATEADD(DAY, 30, GETDATE()) THEN N'Sắp hết hạn'
            ELSE N'Còn hạn'
        END;

        INSERT INTO LoThuoc (maLo, maThuoc, ngayNhap, hanSuDung, soLuongTon, trangThai, isDeleted)
        VALUES (@MaLo, @MaThuoc, GETDATE(), @HanSuDung, 0, @TrangThai, 0);
    END
END
GO

-- -----------------------------------------------------
-- 3.13 SP Xác nhận nhập kho
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_XacNhanNhapKho
    @MaPN NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        IF NOT EXISTS (SELECT 1 FROM PhieuNhap WHERE maPN = @MaPN AND trangThai = N'Chờ nhập')
        BEGIN
            RAISERROR(N'Phiếu nhập không tồn tại hoặc không ở trạng thái Chờ nhập', 16, 1);
            RETURN;
        END

        UPDATE l
        SET l.soLuongTon = l.soLuongTon + (ct.soLuong * ISNULL(dv.giaTriQuyDoi, 1)),
            l.ngayNhap = GETDATE()
        FROM LoThuoc l
        JOIN ChiTietPhieuNhap ct ON l.maLo = ct.maLo
        LEFT JOIN DonViQuyDoi dv ON ct.maThuoc = dv.maThuoc AND ct.donViTinh = dv.tenDonVi
        WHERE ct.maPN = @MaPN;

        UPDATE PhieuNhap
        SET trangThai = N'Đã nhập'
        WHERE maPN = @MaPN;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

-- -----------------------------------------------------
-- 3.14 SP Lấy giá nhập gần nhất
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_GetGiaNhapGanNhat
    @MaThuoc NVARCHAR(50),
    @DonViTinh NVARCHAR(50),
    @GiaNhap DECIMAL(18,2) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP 1 @GiaNhap = ct.donGia
    FROM ChiTietPhieuNhap ct
    JOIN PhieuNhap pn ON ct.maPN = pn.maPN
    WHERE ct.maThuoc = @MaThuoc
      AND ct.donViTinh = @DonViTinh
      AND pn.trangThai = N'Đã nhập'
    ORDER BY pn.ngayTao DESC;

    IF @GiaNhap IS NULL
        SET @GiaNhap = 0;
END
GO

-- -----------------------------------------------------
-- 3.15 SP Cộng dồn lô khi sửa HSD
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_CongDonLoKhiSuaHSD
    @MaLoGoc NVARCHAR(50),
    @HanSuDungMoi DATE,
    @ResultCode INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        DECLARE @MaThuoc NVARCHAR(50), @SoLuongTon INT;
        SELECT @MaThuoc = maThuoc, @SoLuongTon = soLuongTon
        FROM LoThuoc WHERE maLo = @MaLoGoc AND isDeleted = 0;

        IF @MaThuoc IS NULL
        BEGIN
            SET @ResultCode = -1;
            RAISERROR(N'Không tìm thấy lô thuốc', 16, 1);
            RETURN;
        END

        DECLARE @MaLoTrung NVARCHAR(50);
        SELECT TOP 1 @MaLoTrung = maLo
        FROM LoThuoc
        WHERE maThuoc = @MaThuoc
          AND hanSuDung = @HanSuDungMoi
          AND maLo != @MaLoGoc
          AND isDeleted = 0;

        IF @MaLoTrung IS NOT NULL
        BEGIN
            BEGIN TRANSACTION;

            UPDATE LoThuoc
            SET soLuongTon = soLuongTon + @SoLuongTon
            WHERE maLo = @MaLoTrung;

            UPDATE LoThuoc
            SET isDeleted = 1, soLuongTon = 0
            WHERE maLo = @MaLoGoc;

            UPDATE ChiTietPhieuNhap
            SET maLo = @MaLoTrung
            WHERE maLo = @MaLoGoc;

            COMMIT TRANSACTION;
            SET @ResultCode = 1;
        END
        ELSE
        BEGIN
            UPDATE LoThuoc
            SET hanSuDung = @HanSuDungMoi,
                trangThai = CASE
                    WHEN @HanSuDungMoi <= GETDATE() THEN N'Đã hết hạn'
                    WHEN @HanSuDungMoi <= DATEADD(DAY, 30, GETDATE()) THEN N'Sắp hết hạn'
                    ELSE N'Còn hạn'
                END
            WHERE maLo = @MaLoGoc;

            SET @ResultCode = 0;
        END
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        SET @ResultCode = -1;
        THROW;
    END CATCH
END
GO

-- -----------------------------------------------------
-- 3.16 SP Lấy lịch sử mua hàng
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_GetLichSuMuaHang
    @MaKH NVARCHAR(50)
AS
BEGIN
    SELECT
        hd.maHD,
        hd.ngayTao,
        hd.tongTien,
        hd.thue,
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

-- -----------------------------------------------------
-- 3.17 SP Lấy mã mới (generic)
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_GetNewID
    @TableName NVARCHAR(50),
    @Prefix NVARCHAR(10),
    @NewID NVARCHAR(50) OUTPUT
AS
BEGIN
    DECLARE @MaxID NVARCHAR(50);
    DECLARE @NextNum INT;
    DECLARE @SQL NVARCHAR(500);
    DECLARE @PK NVARCHAR(50);

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

-- -----------------------------------------------------
-- 3.18 SP Lấy đơn vị quy đổi theo mã thuốc
-- -----------------------------------------------------
CREATE OR ALTER PROCEDURE sp_GetDonViQuyDoiByMaThuoc
    @MaThuoc NVARCHAR(50)
AS
BEGIN
    SELECT id, maThuoc, tenDonVi, giaTriQuyDoi, giaBan, laDonViCoBan
    FROM DonViQuyDoi
    WHERE maThuoc = @MaThuoc
    ORDER BY giaTriQuyDoi ASC;
END
GO

-- =====================================================
-- PHẦN 4: INSERT DỮ LIỆU MẪU
-- =====================================================

-- -----------------------------------------------------
-- 4.1 Nhân viên (5 người)
-- -----------------------------------------------------
INSERT INTO NhanVien (maNV, hoTen, ngaySinh, gioiTinh, sdt, email, diaChi, matKhau, vaiTro, trangThai) VALUES
(N'NV001', N'Nguyễn Quản Lý', '1990-01-01', 1, N'0909111222', N'admin@nhathuoc.com', N'Quận 1, TP.HCM', N'123456', 1, 1),
(N'NV002', N'Trần Thị Thu Ngân', '1995-05-15', 0, N'0909333444', N'ngantt@nhathuoc.com', N'Quận 3, TP.HCM', N'123456', 0, 1),
(N'NV003', N'Lê Văn Kho', '1992-08-20', 1, N'0909555666', N'kholv@nhathuoc.com', N'Bình Thạnh, TP.HCM', N'123456', 0, 1),
(N'NV004', N'Phạm Thị Dược', '1998-12-10', 0, N'0909777888', N'duocpt@nhathuoc.com', N'Gò Vấp, TP.HCM', N'123456', 0, 1),
(N'NV005', N'Hoàng Văn Bảo Vệ', '1985-03-30', 1, N'0909999000', N'baove@nhathuoc.com', N'Quận 12, TP.HCM', N'123456', 0, 1);

-- -----------------------------------------------------
-- 4.2 Khách hàng (15 người)
-- -----------------------------------------------------
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

-- -----------------------------------------------------
-- 4.3 Nhà cung cấp (10 NCC)
-- -----------------------------------------------------
INSERT INTO NhaCungCap (maNCC, tenNCC, sdt, email, diaChi, trangThai) VALUES
(N'NCC001', N'Công ty CP Dược Hậu Giang (DHG)', N'02923891433', N'dhgpharma@dhg.com.vn', N'288 Nguyễn Văn Cừ, Ninh Kiều, Cần Thơ', 1),
(N'NCC002', N'Công ty CP Traphaco', N'18006612', N'info@traphaco.com.vn', N'75 Yên Ninh, Ba Đình, Hà Nội', 1),
(N'NCC003', N'Công ty CP Dược phẩm Imexpharm', N'02773851941', N'imexpharm@imex.com.vn', N'Số 4, đường 30/4, TP. Cao Lãnh', 1),
(N'NCC004', N'Công ty Sanofi Việt Nam', N'02838298526', N'contact-vn@sanofi.com', N'Số 10 Hàm Nghi, Quận 1, TP.HCM', 1),
(N'NCC005', N'Công ty Zuellig Pharma Việt Nam', N'02839102650', N'info@zuelligpharma.com', N'KCN Tân Bình, TP.HCM', 1),
(N'NCC006', N'Công ty CP Dược phẩm OPC', N'02838777133', N'info@opcpharma.com', N'1017 Hồng Bàng, Quận 6, TP.HCM', 1),
(N'NCC007', N'Công ty CP Dược phẩm SPM', N'02837652726', N'info@spm.com.vn', N'KCN Tân Tạo, Bình Tân, TP.HCM', 1),
(N'NCC008', N'Công ty TNHH Mega We Care', N'02838123123', N'megawecare@mega.com', N'E-Town, Tân Bình, TP.HCM', 1),
(N'NCC009', N'Công ty Dược phẩm Trung ương 1 (Pharbaco)', N'02438454561', N'pharbaco@pharbaco.com.vn', N'160 Tôn Đức Thắng, Đống Đa, Hà Nội', 1),
(N'NCC010', N'Công ty CP Pymepharco', N'02573829165', N'hcns@pymepharco.com', N'166 – 170 Nguyễn Huệ, Tuy Hòa, Phú Yên', 1);

-- -----------------------------------------------------
-- 4.4 Nhóm thuốc (7 nhóm)
-- -----------------------------------------------------
INSERT INTO NhomThuoc (maNhom, tenNhom) VALUES
(N'NT001', N'Kháng sinh - Kháng khuẩn'),
(N'NT002', N'Giảm đau - Hạ sốt - Chống viêm'),
(N'NT003', N'Vitamin - Khoáng chất'),
(N'NT004', N'Tiêu hóa - Dạ dày'),
(N'NT005', N'Hô hấp (Ho, Hen suyễn)'),
(N'NT006', N'Tim mạch - Huyết áp'),
(N'NT007', N'Dụng cụ y tế & Khác');

-- -----------------------------------------------------
-- 4.5 Thuốc (31 loại)
-- -----------------------------------------------------
INSERT INTO Thuoc (maThuoc, tenThuoc, hoatChat, donViCoBan, trangThai, maNhom) VALUES
-- Nhóm 1: Kháng sinh
(N'T001', N'Amoxicillin 500mg', N'Amoxicillin', N'Viên', 1, N'NT001'),
(N'T002', N'Augmentin 625mg', N'Amoxicillin + Clavulanic', N'Viên', 1, N'NT001'),
(N'T003', N'Cephalexin 500mg', N'Cephalexin', N'Viên', 1, N'NT001'),
(N'T004', N'Ciprofloxacin 500mg', N'Ciprofloxacin', N'Viên', 1, N'NT001'),
(N'T005', N'Azithromycin 250mg', N'Azithromycin', N'Viên', 1, N'NT001'),

-- Nhóm 2: Giảm đau, hạ sốt
(N'T006', N'Panadol Extra', N'Paracetamol + Caffeine', N'Viên', 1, N'NT002'),
(N'T007', N'Efferalgan 500mg', N'Paracetamol', N'Viên', 1, N'NT002'),
(N'T008', N'Hapacol 250mg', N'Paracetamol', N'Gói', 1, N'NT002'),
(N'T009', N'Ibuprofen 400mg', N'Ibuprofen', N'Viên', 1, N'NT002'),
(N'T010', N'Aspirin 81mg', N'Aspirin', N'Viên', 1, N'NT002'),
(N'T011', N'Salonpas (Dán)', N'Methyl Salicylate', N'Miếng', 1, N'NT002'),

-- Nhóm 3: Vitamin
(N'T012', N'Vitamin C 500mg', N'Ascorbic Acid', N'Viên', 1, N'NT003'),
(N'T013', N'Vitamin E 400IU', N'Alpha Tocopherol', N'Viên', 1, N'NT003'),
(N'T014', N'Vitamin 3B', N'B1, B6, B12', N'Viên', 1, N'NT003'),
(N'T015', N'Canxi Corbiere 10ml', N'Calcium Glucoheptonate', N'Ống', 1, N'NT003'),
(N'T016', N'Berocca', N'Vitamin tổng hợp', N'Viên', 1, N'NT003'),

-- Nhóm 4: Tiêu hóa
(N'T017', N'Berberin 100mg', N'Berberin', N'Viên', 1, N'NT004'),
(N'T018', N'Smecta', N'Diosmectite', N'Gói', 1, N'NT004'),
(N'T019', N'Omeprazol 20mg', N'Omeprazole', N'Viên', 1, N'NT004'),
(N'T020', N'Gaviscon (Gói)', N'Natri alginat', N'Gói', 1, N'NT004'),
(N'T021', N'Men vi sinh Enterogermina', N'Bacillus clausii', N'Ống', 1, N'NT004'),

-- Nhóm 5: Hô hấp
(N'T022', N'Siro ho Prospan', N'Cao lá thường xuân', N'Chai', 1, N'NT005'),
(N'T023', N'Viên ngậm Bảo Thanh', N'Dược liệu', N'Viên', 1, N'NT005'),
(N'T024', N'Eugica đỏ', N'Tinh dầu tràm', N'Viên', 1, N'NT005'),
(N'T025', N'Thuốc ho Methorphan', N'Dextromethorphan', N'Chai', 1, N'NT005'),

-- Nhóm 6: Tim mạch
(N'T026', N'Amlodipin 5mg', N'Amlodipine', N'Viên', 1, N'NT006'),
(N'T027', N'Losartan 50mg', N'Losartan', N'Viên', 1, N'NT006'),

-- Nhóm 7: Dụng cụ & Khác
(N'T028', N'Khẩu trang Y tế 4 lớp', N'Vải không dệt', N'Cái', 1, N'NT007'),
(N'T029', N'Nước muối sinh lý 0.9%', N'Natri Clorid', N'Chai', 1, N'NT007'),
(N'T030', N'Băng cá nhân Urgo', N'Vải', N'Miếng', 1, N'NT007'),
(N'T031', N'Cồn 70 độ', N'Ethanol', N'Chai', 1, N'NT007');

-- -----------------------------------------------------
-- 4.6 Lô thuốc
-- -----------------------------------------------------
INSERT INTO LoThuoc (maLo, maThuoc, ngayNhap, hanSuDung, soLuongTon, trangThai, isDeleted) VALUES
-- Nhóm Kháng sinh
(N'L001', N'T001', '2024-01-10', '2026-01-10', 5000, N'Còn hạn', 0),
(N'L002', N'T002', '2024-05-20', '2026-05-20', 1400, N'Còn hạn', 0),
(N'L003', N'T003', '2023-06-15', '2024-12-01', 500, N'Đã hết hạn', 0),
(N'L004', N'T004', '2024-08-01', '2026-08-01', 2000, N'Còn hạn', 0),
(N'L005', N'T005', '2024-09-10', '2026-09-10', 900, N'Còn hạn', 0),

-- Nhóm Giảm đau
(N'L006', N'T006', '2024-10-01', '2026-10-01', 12000, N'Còn hạn', 0),
(N'L007', N'T006', '2023-12-01', '2025-01-15', 240, N'Sắp hết hạn', 0),
(N'L008', N'T007', '2024-02-15', '2026-02-15', 300, N'Còn hạn', 0),
(N'L009', N'T008', '2024-03-20', '2026-03-20', 50, N'Còn hạn', 0),
(N'L010', N'T009', '2024-07-07', '2026-07-07', 4000, N'Còn hạn', 0),
(N'L011', N'T010', '2023-01-01', '2024-01-01', 0, N'Đã hết hạn', 0),
(N'L012', N'T011', '2024-06-01', '2026-06-01', 400, N'Còn hạn', 0),

-- Nhóm Vitamin
(N'L013', N'T012', '2024-11-11', '2026-11-11', 5000, N'Còn hạn', 0),
(N'L014', N'T013', '2024-05-05', '2026-05-05', 2500, N'Còn hạn', 0),
(N'L015', N'T014', '2024-09-09', '2025-01-20', 1500, N'Sắp hết hạn', 0),
(N'L016', N'T015', '2024-12-01', '2026-06-01', 800, N'Còn hạn', 0),
(N'L017', N'T016', '2024-10-20', '2026-10-20', 600, N'Còn hạn', 0),

-- Nhóm Tiêu hóa
(N'L018', N'T017', '2024-04-30', '2026-04-30', 3000, N'Còn hạn', 0),
(N'L019', N'T018', '2024-08-15', '2026-02-26', 1200, N'Sắp hết hạn', 0),
(N'L020', N'T019', '2024-06-01', '2026-06-01', 1000, N'Còn hạn', 0),
(N'L021', N'T020', '2024-02-28', '2025-02-28', 100, N'Sắp hết hạn', 0),
(N'L022', N'T021', '2024-07-15', '2026-07-15', 400, N'Còn hạn', 0),

-- Nhóm Hô hấp
(N'L023', N'T022', '2024-09-15', '2026-09-15', 80, N'Còn hạn', 0),
(N'L024', N'T023', '2024-01-20', '2025-01-20', 2000, N'Sắp hết hạn', 0),
(N'L025', N'T024', '2024-11-01', '2026-11-01', 1500, N'Còn hạn', 0),
(N'L026', N'T025', '2024-08-01', '2026-08-01', 50, N'Còn hạn', 0),

-- Nhóm Tim mạch
(N'L027', N'T026', '2024-06-01', '2026-06-01', 3000, N'Còn hạn', 0),
(N'L028', N'T027', '2024-07-01', '2026-07-01', 3000, N'Còn hạn', 0),

-- Nhóm Dụng cụ
(N'L029', N'T028', '2024-05-10', '2028-05-10', 10000, N'Còn hạn', 0),
(N'L030', N'T029', '2024-07-20', '2026-07-20', 500, N'Còn hạn', 0),
(N'L031', N'T030', '2024-03-15', '2027-03-15', 3000, N'Còn hạn', 0),
(N'L032', N'T031', '2024-04-01', '2026-04-01', 100, N'Còn hạn', 0);
GO

-- -----------------------------------------------------
-- 4.7 Bảng giá
-- -----------------------------------------------------
INSERT INTO BangGia (maBG, tenBG, ngayHieuLuc, ngayKetThuc, ghiChu, trangThai) VALUES
(N'BG001', N'Bảng giá bán lẻ 2024', '2024-01-01', NULL, N'Áp dụng cho khách lẻ toàn hệ thống', 1),
(N'BG002', N'Bảng giá khuyến mãi Tết', '2024-01-01', '2024-02-15', N'Giảm giá các loại Vitamin', 0);

-- -----------------------------------------------------
-- 4.8 Chi tiết bảng giá
-- -----------------------------------------------------
INSERT INTO ChiTietBangGia (maBG, maThuoc, donViTinh, giaBan) VALUES
-- T001: Amoxicillin 500mg
(N'BG001', N'T001', N'Viên', 1500),
(N'BG001', N'T001', N'Vỉ', 14000),
(N'BG001', N'T001', N'Hộp', 135000),

-- T002: Augmentin 625mg
(N'BG001', N'T002', N'Viên', 16000),
(N'BG001', N'T002', N'Hộp', 220000),

-- T003: Cephalexin 500mg
(N'BG001', N'T003', N'Viên', 1200),
(N'BG001', N'T003', N'Vỉ', 11000),

-- T004: Ciprofloxacin 500mg
(N'BG001', N'T004', N'Viên', 1800),
(N'BG001', N'T004', N'Vỉ', 17000),

-- T005: Azithromycin 250mg
(N'BG001', N'T005', N'Viên', 14500),
(N'BG001', N'T005', N'Hộp', 85000),

-- T006: Panadol Extra
(N'BG001', N'T006', N'Viên', 1600),
(N'BG001', N'T006', N'Vỉ', 18000),
(N'BG001', N'T006', N'Hộp', 175000),

-- T007: Efferalgan 500mg
(N'BG001', N'T007', N'Viên', 5000),

-- T008: Hapacol 250mg
(N'BG001', N'T008', N'Gói', 3500),
(N'BG001', N'T008', N'Hộp', 80000),

-- T009: Ibuprofen 400mg
(N'BG001', N'T009', N'Viên', 2500),
(N'BG001', N'T009', N'Vỉ', 24000),

-- T010: Aspirin 81mg
(N'BG001', N'T010', N'Viên', 500),
(N'BG001', N'T010', N'Vỉ', 14000),

-- T011: Salonpas
(N'BG001', N'T011', N'Miếng', 1700),
(N'BG001', N'T011', N'Gói', 16000),
(N'BG001', N'T011', N'Hộp', 31000),

-- T012: Vitamin C 500mg
(N'BG001', N'T012', N'Viên', 600),
(N'BG001', N'T012', N'Lọ', 58000),

-- T013: Vitamin E 400IU
(N'BG001', N'T013', N'Viên', 5000),
(N'BG001', N'T013', N'Hộp', 145000),

-- T014: Vitamin 3B
(N'BG001', N'T014', N'Viên', 4500),
(N'BG001', N'T014', N'Vỉ', 43000),

-- T015: Canxi Corbiere
(N'BG001', N'T015', N'Ống', 5000),
(N'BG001', N'T015', N'Hộp', 145000),

-- T016: Berocca
(N'BG001', N'T016', N'Viên', 8500),
(N'BG001', N'T016', N'Tuýp', 82000),

-- T017: Berberin
(N'BG001', N'T017', N'Viên', 100),
(N'BG001', N'T017', N'Lọ', 9500),

-- T018: Smecta
(N'BG001', N'T018', N'Gói', 4000),
(N'BG001', N'T018', N'Hộp', 115000),

-- T019: Omeprazol 20mg
(N'BG001', N'T019', N'Viên', 1600),
(N'BG001', N'T019', N'Vỉ', 21000),
(N'BG001', N'T019', N'Hộp', 40000),

-- T020: Gaviscon
(N'BG001', N'T020', N'Gói', 6500),
(N'BG001', N'T020', N'Hộp', 150000),

-- T021: Enterogermina
(N'BG001', N'T021', N'Ống', 8000),
(N'BG001', N'T021', N'Hộp', 155000),

-- T022: Prospan
(N'BG001', N'T022', N'Chai', 95000),

-- T023: Viên ngậm Bảo Thanh
(N'BG001', N'T023', N'Viên', 3500),
(N'BG001', N'T023', N'Vỉ', 33000),

-- T024: Eugica đỏ
(N'BG001', N'T024', N'Viên', 550),
(N'BG001', N'T024', N'Vỉ', 5200),
(N'BG001', N'T024', N'Hộp', 50000),

-- T025: Methorphan
(N'BG001', N'T025', N'Chai', 40000),

-- T026: Amlodipin 5mg
(N'BG001', N'T026', N'Viên', 1000),
(N'BG001', N'T026', N'Vỉ', 9500),
(N'BG001', N'T026', N'Hộp', 28000),

-- T027: Losartan 50mg
(N'BG001', N'T027', N'Viên', 1500),
(N'BG001', N'T027', N'Vỉ', 14500),
(N'BG001', N'T027', N'Hộp', 43000),

-- T028: Khẩu trang
(N'BG001', N'T028', N'Cái', 700),
(N'BG001', N'T028', N'Hộp', 33000),

-- T029: Nước muối
(N'BG001', N'T029', N'Chai', 5000),
(N'BG001', N'T029', N'Thùng', 115000),

-- T030: Băng cá nhân
(N'BG001', N'T030', N'Miếng', 250),
(N'BG001', N'T030', N'Hộp', 24000),

-- T031: Cồn 70 độ
(N'BG001', N'T031', N'Chai', 15000);
GO

-- -----------------------------------------------------
-- 4.9 Đơn vị quy đổi
-- -----------------------------------------------------
INSERT INTO DonViQuyDoi (maThuoc, tenDonVi, giaTriQuyDoi, giaBan, laDonViCoBan) VALUES
-- T001: Amoxicillin 500mg (1 Vỉ = 10 Viên, 1 Hộp = 100 Viên)
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

-- T006: Panadol Extra (1 Vỉ = 12 Viên, 1 Hộp = 120 Viên)
(N'T006', N'Viên', 1, 1600, 1),
(N'T006', N'Vỉ', 12, 18000, 0),
(N'T006', N'Hộp', 120, 175000, 0),

-- T007: Efferalgan 500mg
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

-- T011: Salonpas (1 Gói = 10 Miếng, 1 Hộp = 20 Miếng)
(N'T011', N'Miếng', 1, 1700, 1),
(N'T011', N'Gói', 10, 16000, 0),
(N'T011', N'Hộp', 20, 31000, 0),

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

-- T017: Berberin (1 Lọ = 100 Viên)
(N'T017', N'Viên', 1, 100, 1),
(N'T017', N'Lọ', 100, 9500, 0),

-- T018: Smecta (1 Hộp = 30 Gói)
(N'T018', N'Gói', 1, 4000, 1),
(N'T018', N'Hộp', 30, 115000, 0),

-- T019: Omeprazol 20mg (1 Vỉ = 14 Viên, 1 Hộp = 28 Viên)
(N'T019', N'Viên', 1, 1600, 1),
(N'T019', N'Vỉ', 14, 21000, 0),
(N'T019', N'Hộp', 28, 40000, 0),

-- T020: Gaviscon (1 Hộp = 24 Gói)
(N'T020', N'Gói', 1, 6500, 1),
(N'T020', N'Hộp', 24, 150000, 0),

-- T021: Enterogermina (1 Hộp = 20 Ống)
(N'T021', N'Ống', 1, 8000, 1),
(N'T021', N'Hộp', 20, 155000, 0),

-- T022: Prospan
(N'T022', N'Chai', 1, 95000, 1),

-- T023: Viên ngậm Bảo Thanh (1 Vỉ = 10 Viên)
(N'T023', N'Viên', 1, 3500, 1),
(N'T023', N'Vỉ', 10, 33000, 0),

-- T024: Eugica đỏ (1 Vỉ = 10 Viên, 1 Hộp = 100 Viên)
(N'T024', N'Viên', 1, 550, 1),
(N'T024', N'Vỉ', 10, 5200, 0),
(N'T024', N'Hộp', 100, 50000, 0),

-- T025: Methorphan
(N'T025', N'Chai', 1, 40000, 1),

-- T026: Amlodipin 5mg (1 Vỉ = 10 Viên, 1 Hộp = 30 Viên)
(N'T026', N'Viên', 1, 1000, 1),
(N'T026', N'Vỉ', 10, 9500, 0),
(N'T026', N'Hộp', 30, 28000, 0),

-- T027: Losartan 50mg (1 Vỉ = 10 Viên, 1 Hộp = 30 Viên)
(N'T027', N'Viên', 1, 1500, 1),
(N'T027', N'Vỉ', 10, 14500, 0),
(N'T027', N'Hộp', 30, 43000, 0),

-- T028: Khẩu trang (1 Hộp = 50 Cái)
(N'T028', N'Cái', 1, 700, 1),
(N'T028', N'Hộp', 50, 33000, 0),

-- T029: Nước muối sinh lý (1 Thùng = 24 Chai)
(N'T029', N'Chai', 1, 5000, 1),
(N'T029', N'Thùng', 24, 115000, 0),

-- T030: Băng cá nhân Urgo (1 Hộp = 100 Miếng)
(N'T030', N'Miếng', 1, 250, 1),
(N'T030', N'Hộp', 100, 24000, 0),

-- T031: Cồn 70 độ
(N'T031', N'Chai', 1, 15000, 1);
GO

-- -----------------------------------------------------
-- 4.10 Phiếu nhập
-- -----------------------------------------------------
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

-- -----------------------------------------------------
-- 4.11 Chi tiết phiếu nhập
-- -----------------------------------------------------
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

-- =====================================================
-- PHẦN 5: CẬP NHẬT TRẠNG THÁI LÔ THUỐC
-- =====================================================
UPDATE LoThuoc
SET trangThai = CASE
    WHEN hanSuDung <= GETDATE() THEN N'Đã hết hạn'
    WHEN hanSuDung <= DATEADD(DAY, 30, GETDATE()) THEN N'Sắp hết hạn'
    ELSE N'Còn hạn'
END
WHERE isDeleted = 0;
GO

-- Cập nhật đơn vị tính mặc định cho chi tiết phiếu nhập
UPDATE ct
SET ct.donViTinh = t.donViCoBan
FROM ChiTietPhieuNhap ct
JOIN Thuoc t ON ct.maThuoc = t.maThuoc
WHERE ct.donViTinh IS NULL;
GO

