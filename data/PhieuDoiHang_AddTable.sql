-- Script thêm bảng PhieuDoiHang cho chức năng Đổi Hàng
-- Đổi thuốc = Trả thuốc + Lập hóa đơn mới
-- Chạy script này trên database QLTHUOC

USE QLTHUOC
GO

-- Tạo bảng PhieuDoiHang
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='PhieuDoiHang' AND xtype='U')
BEGIN
    CREATE TABLE PhieuDoiHang (
        maPD NVARCHAR(50) PRIMARY KEY,
        ngayDoi DATE DEFAULT GETDATE(),
        tongTienTra DECIMAL(18, 2),      -- Tổng tiền hàng trả lại
        tongTienMoi DECIMAL(18, 2),      -- Tổng tiền hàng mới
        chenhLech DECIMAL(18, 2),        -- Chênh lệch = tongTienMoi - tongTienTra
        lyDo NVARCHAR(255),
        maHDGoc NVARCHAR(50),            -- Hóa đơn gốc (hóa đơn khách mua trước đó)
        maPT NVARCHAR(50),               -- Phiếu trả hàng được tạo
        maHDMoi NVARCHAR(50),            -- Hóa đơn mới được tạo (cho hàng đổi)
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
    PRINT N'Đã tạo bảng PhieuDoiHang thành công!'
END
ELSE
BEGIN
    PRINT N'Bảng PhieuDoiHang đã tồn tại.'
END
GO
