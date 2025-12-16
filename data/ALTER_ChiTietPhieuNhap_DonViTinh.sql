-- =====================================================
-- Script: Thêm cột đơn vị tính vào bảng ChiTietPhieuNhap
-- Author: Claude AI
-- Date: 2025-12-16
-- Description: Thêm cột donViTinh vào ChiTietPhieuNhap
--              để lưu đơn vị tính khi nhập hàng (giống như ChiTietHoaDon)
-- =====================================================

USE QLTHUOC
GO

-- 1. Thêm cột donViTinh vào bảng ChiTietPhieuNhap
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'ChiTietPhieuNhap') AND name = 'donViTinh')
BEGIN
    ALTER TABLE ChiTietPhieuNhap ADD donViTinh NVARCHAR(50) NULL;
    PRINT N'✅ Đã thêm cột donViTinh vào bảng ChiTietPhieuNhap';
END
ELSE
BEGIN
    PRINT N'ℹ️ Cột donViTinh đã tồn tại trong bảng ChiTietPhieuNhap';
END
GO

-- 2. Cập nhật dữ liệu cũ: lấy đơn vị cơ bản của thuốc làm đơn vị tính mặc định
UPDATE ct
SET ct.donViTinh = t.donViCoBan
FROM ChiTietPhieuNhap ct
JOIN Thuoc t ON ct.maThuoc = t.maThuoc
WHERE ct.donViTinh IS NULL;

PRINT N'✅ Đã cập nhật đơn vị tính mặc định cho các chi tiết phiếu nhập cũ';
GO

-- 3. Tạo/cập nhật Stored Procedure để tìm hoặc tạo lô thuốc (có hỗ trợ đơn vị tính)
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_TimHoacTaoLoThuoc_v2')
    DROP PROCEDURE sp_TimHoacTaoLoThuoc_v2;
GO

CREATE PROCEDURE sp_TimHoacTaoLoThuoc_v2
    @MaThuoc NVARCHAR(50),
    @HanSuDung DATE,
    @SoLuong INT,              -- Số lượng theo đơn vị nhập
    @DonViTinh NVARCHAR(50),   -- Đơn vị tính nhập
    @MaLo NVARCHAR(50) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    -- Lấy giá trị quy đổi của đơn vị nhập
    DECLARE @GiaTriQuyDoi INT = 1;
    SELECT @GiaTriQuyDoi = ISNULL(giaTriQuyDoi, 1)
    FROM DonViQuyDoi
    WHERE maThuoc = @MaThuoc AND tenDonVi = @DonViTinh;

    -- Tính số lượng theo đơn vị cơ bản
    DECLARE @SoLuongCoBan INT = @SoLuong * @GiaTriQuyDoi;

    -- Tìm lô có cùng mã thuốc và HSD (chưa bị xóa)
    SELECT TOP 1 @MaLo = maLo
    FROM LoThuoc
    WHERE maThuoc = @MaThuoc
      AND hanSuDung = @HanSuDung
      AND isDeleted = 0;

    IF @MaLo IS NOT NULL
    BEGIN
        -- Cộng dồn vào lô hiện có (theo đơn vị cơ bản)
        UPDATE LoThuoc
        SET soLuongTon = soLuongTon + @SoLuongCoBan,
            ngayNhap = GETDATE(),
            trangThai = CASE
                WHEN @HanSuDung <= DATEADD(DAY, 30, GETDATE()) THEN N'Sắp hết hạn'
                ELSE N'Còn hạn'
            END
        WHERE maLo = @MaLo;
        PRINT N'Đã cộng dồn ' + CAST(@SoLuongCoBan AS NVARCHAR) + N' (đơn vị cơ bản) vào lô: ' + @MaLo;
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

        -- Tạo lô mới (số lượng theo đơn vị cơ bản)
        INSERT INTO LoThuoc (maLo, maThuoc, ngayNhap, hanSuDung, soLuongTon, trangThai, isDeleted)
        VALUES (@MaLo, @MaThuoc, GETDATE(), @HanSuDung, @SoLuongCoBan, @TrangThai, 0);

        PRINT N'Đã tạo lô mới: ' + @MaLo + N' với số lượng ' + CAST(@SoLuongCoBan AS NVARCHAR) + N' (đơn vị cơ bản)';
    END
END
GO
PRINT N'✅ Đã tạo SP sp_TimHoacTaoLoThuoc_v2';
GO

-- 4. Tạo SP chỉ tìm/tạo lô nhưng không cộng tồn (dùng khi trạng thái Chờ nhập)
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_TimHoacTaoLoThuoc_KhongCongTon')
    DROP PROCEDURE sp_TimHoacTaoLoThuoc_KhongCongTon;
GO

CREATE PROCEDURE sp_TimHoacTaoLoThuoc_KhongCongTon
    @MaThuoc NVARCHAR(50),
    @HanSuDung DATE,
    @MaLo NVARCHAR(50) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    -- Tìm lô có cùng mã thuốc và HSD (chưa bị xóa)
    SELECT TOP 1 @MaLo = maLo
    FROM LoThuoc
    WHERE maThuoc = @MaThuoc
      AND hanSuDung = @HanSuDung
      AND isDeleted = 0;

    IF @MaLo IS NULL
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

        -- Tạo lô mới với số lượng = 0 (chờ xác nhận nhập kho)
        INSERT INTO LoThuoc (maLo, maThuoc, ngayNhap, hanSuDung, soLuongTon, trangThai, isDeleted)
        VALUES (@MaLo, @MaThuoc, GETDATE(), @HanSuDung, 0, @TrangThai, 0);

        PRINT N'Đã tạo lô mới (chờ nhập): ' + @MaLo;
    END
END
GO
PRINT N'✅ Đã tạo SP sp_TimHoacTaoLoThuoc_KhongCongTon';
GO

-- 5. Tạo SP cập nhật tồn kho khi xác nhận nhập kho
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_XacNhanNhapKho')
    DROP PROCEDURE sp_XacNhanNhapKho;
GO

CREATE PROCEDURE sp_XacNhanNhapKho
    @MaPN NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        -- Kiểm tra phiếu nhập có trạng thái "Chờ nhập" không
        IF NOT EXISTS (SELECT 1 FROM PhieuNhap WHERE maPN = @MaPN AND trangThai = N'Chờ nhập')
        BEGIN
            RAISERROR(N'Phiếu nhập không tồn tại hoặc không ở trạng thái Chờ nhập', 16, 1);
            RETURN;
        END

        -- Cập nhật số lượng tồn kho cho từng lô
        UPDATE l
        SET l.soLuongTon = l.soLuongTon + (ct.soLuong * ISNULL(dv.giaTriQuyDoi, 1)),
            l.ngayNhap = GETDATE()
        FROM LoThuoc l
        JOIN ChiTietPhieuNhap ct ON l.maLo = ct.maLo
        LEFT JOIN DonViQuyDoi dv ON ct.maThuoc = dv.maThuoc AND ct.donViTinh = dv.tenDonVi
        WHERE ct.maPN = @MaPN;

        -- Cập nhật trạng thái phiếu nhập
        UPDATE PhieuNhap
        SET trangThai = N'Đã nhập'
        WHERE maPN = @MaPN;

        COMMIT TRANSACTION;
        PRINT N'✅ Đã xác nhận nhập kho phiếu: ' + @MaPN;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO
PRINT N'✅ Đã tạo SP sp_XacNhanNhapKho';
GO

-- 6. Tạo SP lấy giá nhập gần nhất theo đơn vị tính
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_GetGiaNhapGanNhat')
    DROP PROCEDURE sp_GetGiaNhapGanNhat;
GO

CREATE PROCEDURE sp_GetGiaNhapGanNhat
    @MaThuoc NVARCHAR(50),
    @DonViTinh NVARCHAR(50),
    @GiaNhap DECIMAL(18,2) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    -- Tìm giá nhập gần nhất của thuốc với đơn vị tính tương ứng
    SELECT TOP 1 @GiaNhap = ct.donGia
    FROM ChiTietPhieuNhap ct
    JOIN PhieuNhap pn ON ct.maPN = pn.maPN
    WHERE ct.maThuoc = @MaThuoc
      AND ct.donViTinh = @DonViTinh
      AND pn.trangThai = N'Đã nhập'
    ORDER BY pn.ngayTao DESC;

    -- Nếu không tìm thấy, trả về 0
    IF @GiaNhap IS NULL
        SET @GiaNhap = 0;
END
GO
PRINT N'✅ Đã tạo SP sp_GetGiaNhapGanNhat';
GO

-- 7. Tạo SP cộng dồn lô khi sửa hạn sử dụng trùng ngày
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_CongDonLoKhiSuaHSD')
    DROP PROCEDURE sp_CongDonLoKhiSuaHSD;
GO

CREATE PROCEDURE sp_CongDonLoKhiSuaHSD
    @MaLoGoc NVARCHAR(50),
    @HanSuDungMoi DATE,
    @ResultCode INT OUTPUT  -- 0: Thành công (không cộng dồn), 1: Đã cộng dồn, -1: Lỗi
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        -- Lấy thông tin lô gốc
        DECLARE @MaThuoc NVARCHAR(50), @SoLuongTon INT;
        SELECT @MaThuoc = maThuoc, @SoLuongTon = soLuongTon
        FROM LoThuoc WHERE maLo = @MaLoGoc AND isDeleted = 0;

        IF @MaThuoc IS NULL
        BEGIN
            SET @ResultCode = -1;
            RAISERROR(N'Không tìm thấy lô thuốc', 16, 1);
            RETURN;
        END

        -- Tìm lô khác cùng thuốc, cùng HSD mới
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

            -- Cộng dồn số lượng vào lô trùng
            UPDATE LoThuoc
            SET soLuongTon = soLuongTon + @SoLuongTon
            WHERE maLo = @MaLoTrung;

            -- Đánh dấu lô gốc là đã xóa
            UPDATE LoThuoc
            SET isDeleted = 1, soLuongTon = 0
            WHERE maLo = @MaLoGoc;

            -- Cập nhật chi tiết phiếu nhập để trỏ đến lô mới
            UPDATE ChiTietPhieuNhap
            SET maLo = @MaLoTrung
            WHERE maLo = @MaLoGoc;

            COMMIT TRANSACTION;
            SET @ResultCode = 1; -- Đã cộng dồn
            PRINT N'Đã cộng dồn lô ' + @MaLoGoc + N' vào lô ' + @MaLoTrung;
        END
        ELSE
        BEGIN
            -- Không có lô trùng, chỉ cập nhật HSD
            UPDATE LoThuoc
            SET hanSuDung = @HanSuDungMoi,
                trangThai = CASE
                    WHEN @HanSuDungMoi <= GETDATE() THEN N'Đã hết hạn'
                    WHEN @HanSuDungMoi <= DATEADD(DAY, 30, GETDATE()) THEN N'Sắp hết hạn'
                    ELSE N'Còn hạn'
                END
            WHERE maLo = @MaLoGoc;

            SET @ResultCode = 0; -- Chỉ cập nhật HSD
        END
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        SET @ResultCode = -1;
        THROW;
    END CATCH
END
GO
PRINT N'✅ Đã tạo SP sp_CongDonLoKhiSuaHSD';
GO

PRINT N'';
PRINT N'=====================================================';
PRINT N'✅ HOÀN TẤT - Script đã thực thi thành công!';
PRINT N'=====================================================';
GO
