package utils;

import entities.ChiTietPhieuNhap;
import entities.Thuoc;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelHelper {

    public static List<ChiTietPhieuNhap> readPhieuNhapFromExcel(File file) throws Exception {
        List<ChiTietPhieuNhap> list = new ArrayList<>();
        FileInputStream fis = new FileInputStream(file);
        
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        Iterator<Row> iterator = sheet.iterator();
        
        // Bỏ qua header
        if (iterator.hasNext()) {
            iterator.next();
        }

        DataFormatter fmt = new DataFormatter();

        while (iterator.hasNext()) {
            Row currentRow = iterator.next();
            
            // Bỏ qua dòng trống
            if (currentRow.getCell(0) == null || fmt.formatCellValue(currentRow.getCell(0)).trim().isEmpty()) {
                continue;
            }

            // [0] Mã Thuốc
            String maThuoc = fmt.formatCellValue(currentRow.getCell(0));
            // [1] Tên Thuốc
            String tenThuoc = fmt.formatCellValue(currentRow.getCell(1)); 
            // [2] Đơn vị tính
            String donVi = fmt.formatCellValue(currentRow.getCell(2));
            
            // [3] Lô sản xuất (Hiện tại hệ thống đang tự sinh mã Lô L001... nên ta tạm thời chưa lưu cột này vào DB,
            // hoặc bạn có thể lưu vào ghi chú nếu cần. Ở đây mình chỉ đọc lướt qua để không bị lệch cột)
            String loSanXuat = fmt.formatCellValue(currentRow.getCell(3)); 

            // [4] Hạn sử dụng (Date)
            Cell cellHSD = currentRow.getCell(4);
            java.time.LocalDate hsd = java.time.LocalDate.now();
            if (cellHSD != null) {
                if (DateUtil.isCellDateFormatted(cellHSD)) {
                     // Trường hợp Excel format là Date
                    hsd = cellHSD.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                } else {
                    try {
                        // Trường hợp Excel format là Text "2025-12-25"
                        hsd = java.time.LocalDate.parse(fmt.formatCellValue(cellHSD));
                    } catch (Exception e) { 
                        System.out.println("Lỗi parse ngày: " + fmt.formatCellValue(cellHSD));
                    }
                }
            }

            // [5] Số lượng (Number)
            int soLuong = 0;
            Cell cellSL = currentRow.getCell(5);
            if (cellSL != null && cellSL.getCellType() == CellType.NUMERIC) {
                soLuong = (int) cellSL.getNumericCellValue();
            } else if (cellSL != null) {
                try {
                    soLuong = Integer.parseInt(fmt.formatCellValue(cellSL));
                } catch (NumberFormatException e) { soLuong = 0; }
            }

            // [6] Giá nhập (Number)
            double donGia = 0;
            Cell cellGia = currentRow.getCell(6);
            if (cellGia != null && cellGia.getCellType() == CellType.NUMERIC) {
                donGia = cellGia.getNumericCellValue();
            } else if (cellGia != null) {
                try {
                    String strGia = fmt.formatCellValue(cellGia).replace(",", "");
                    donGia = Double.parseDouble(strGia);
                } catch (NumberFormatException e) { donGia = 0; }
            }

            // Tạo đối tượng
            Thuoc t = new Thuoc();
            t.setMaThuoc(maThuoc);
            t.setTenThuoc(tenThuoc);
            t.setDonViTinh(donVi);

            ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
            ct.setThuoc(t);
            ct.setSoLuong(soLuong);
            ct.setDonGia(donGia);
            ct.setHanSuDung(hsd);
            ct.setDonViTinh(donVi);
            ct.setThanhTien(soLuong * donGia);
            ct.setLoThuoc(null); // Sẽ được xử lý tự động khi lưu vào DB

            list.add(ct);
        }

        workbook.close();
        fis.close();
        return list;
    }

    /**
     * Xuất báo cáo doanh thu ra file Excel
     * Bao gồm thông tin hoàn trả và doanh thu thực tế
     * @param file File đích
     * @param chiTietList Danh sách dữ liệu chi tiết theo ngày
     * @param tongHop Map chứa thông tin tổng hợp
     * @param tuNgay Ngày bắt đầu
     * @param denNgay Ngày kết thúc
     */
    public static void exportBaoCaoDoanhThu(File file, List<Map<String, Object>> chiTietList,
            Map<String, Object> tongHop, LocalDate tuNgay, LocalDate denNgay) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Báo cáo doanh thu");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Tạo các style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle percentStyle = createPercentStyle(workbook);
        CellStyle normalStyle = createNormalStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle refundStyle = createRefundStyle(workbook);

        int rowNum = 0;

        // Tiêu đề báo cáo
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO DOANH THU & LỢI NHUẬN");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        // Thời gian báo cáo
        Row periodRow = sheet.createRow(rowNum++);
        Cell periodCell = periodRow.createCell(0);
        periodCell.setCellValue("Từ ngày: " + tuNgay.format(dtf) + " - Đến ngày: " + denNgay.format(dtf));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

        // Dòng trống
        rowNum++;

        // Thông tin tổng hợp
        Row summaryTitleRow = sheet.createRow(rowNum++);
        Cell summaryTitleCell = summaryTitleRow.createCell(0);
        summaryTitleCell.setCellValue("TỔNG HỢP");
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        CellStyle boldStyle = workbook.createCellStyle();
        boldStyle.setFont(boldFont);
        summaryTitleCell.setCellStyle(boldStyle);

        Row summaryRow1 = sheet.createRow(rowNum++);
        summaryRow1.createCell(0).setCellValue("Tổng doanh thu gốc:");
        Cell totalRevenue = summaryRow1.createCell(1);
        totalRevenue.setCellValue(tongHop.get("tongDoanhThu") != null ? (double) tongHop.get("tongDoanhThu") : 0);
        totalRevenue.setCellStyle(currencyStyle);

        Row summaryRow2 = sheet.createRow(rowNum++);
        summaryRow2.createCell(0).setCellValue("Tiền hoàn trả:");
        Cell totalRefund = summaryRow2.createCell(1);
        totalRefund.setCellValue(tongHop.get("tongTienHoanTra") != null ? (double) tongHop.get("tongTienHoanTra") : 0);
        totalRefund.setCellStyle(refundStyle);

        Row summaryRow3 = sheet.createRow(rowNum++);
        summaryRow3.createCell(0).setCellValue("Doanh thu thực tế:");
        Cell totalActualRevenue = summaryRow3.createCell(1);
        totalActualRevenue.setCellValue(tongHop.get("tongDoanhThuThuc") != null ? (double) tongHop.get("tongDoanhThuThuc") : 0);
        totalActualRevenue.setCellStyle(currencyStyle);

        Row summaryRow4 = sheet.createRow(rowNum++);
        summaryRow4.createCell(0).setCellValue("Lợi nhuận thực tế:");
        Cell totalProfit = summaryRow4.createCell(1);
        totalProfit.setCellValue(tongHop.get("tongLoiNhuan") != null ? (double) tongHop.get("tongLoiNhuan") : 0);
        totalProfit.setCellStyle(currencyStyle);

        Row summaryRow5 = sheet.createRow(rowNum++);
        summaryRow5.createCell(0).setCellValue("Tổng số đơn hàng:");
        summaryRow5.createCell(1).setCellValue(tongHop.get("tongDonHang") != null ? (int) tongHop.get("tongDonHang") : 0);

        // Dòng trống
        rowNum++;

        // Header bảng chi tiết
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Ngày", "Số Đơn", "Doanh Thu", "Hoàn Trả", "Doanh Thu Thực", "Lợi Nhuận", "Tăng Trưởng"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Dữ liệu chi tiết
        for (Map<String, Object> row : chiTietList) {
            Row dataRow = sheet.createRow(rowNum++);

            // Ngày
            Cell dateCell = dataRow.createCell(0);
            LocalDate ngay = (LocalDate) row.get("ngay");
            if (ngay != null) {
                dateCell.setCellValue(ngay.format(dtf));
            }
            dateCell.setCellStyle(dateStyle);

            // Số đơn
            Cell soDonCell = dataRow.createCell(1);
            soDonCell.setCellValue((int) row.get("soDon"));
            soDonCell.setCellStyle(normalStyle);

            // Doanh thu gốc
            Cell doanhThuCell = dataRow.createCell(2);
            doanhThuCell.setCellValue((double) row.get("doanhThu"));
            doanhThuCell.setCellStyle(currencyStyle);

            // Tiền hoàn trả
            Cell hoanTraCell = dataRow.createCell(3);
            double tienHoanTra = row.get("tienHoanTra") != null ? (double) row.get("tienHoanTra") : 0;
            hoanTraCell.setCellValue(tienHoanTra);
            hoanTraCell.setCellStyle(tienHoanTra > 0 ? refundStyle : currencyStyle);

            // Doanh thu thực
            Cell doanhThuThucCell = dataRow.createCell(4);
            double doanhThuThuc = row.get("doanhThuThuc") != null ? (double) row.get("doanhThuThuc") : (double) row.get("doanhThu");
            doanhThuThucCell.setCellValue(doanhThuThuc);
            doanhThuThucCell.setCellStyle(currencyStyle);

            // Lợi nhuận
            Cell loiNhuanCell = dataRow.createCell(5);
            loiNhuanCell.setCellValue((double) row.get("loiNhuan"));
            loiNhuanCell.setCellStyle(currencyStyle);

            // Tăng trưởng
            Cell tangTruongCell = dataRow.createCell(6);
            double tangTruong = (double) row.get("tangTruong");
            tangTruongCell.setCellValue(String.format("%+.1f%%", tangTruong));
            tangTruongCell.setCellStyle(percentStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Ghi file
        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.close();
        workbook.close();
    }

    /**
     * Xuất báo cáo tồn kho ra file Excel
     * @param file File đích
     * @param chiTietList Danh sách dữ liệu tồn kho chi tiết
     * @param tongHop Map chứa thông tin tổng hợp
     */
    public static void exportBaoCaoTonKho(File file, List<Map<String, Object>> chiTietList,
            Map<String, Object> tongHop) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Báo cáo tồn kho");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Tạo các style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle normalStyle = createNormalStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle warningStyle = createWarningStyle(workbook);
        CellStyle dangerStyle = createDangerStyle(workbook);

        int rowNum = 0;

        // Tiêu đề báo cáo
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO TỒN KHO & HẠN SỬ DỤNG");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        // Ngày xuất báo cáo
        Row dateRow = sheet.createRow(rowNum++);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Ngày xuất: " + LocalDate.now().format(dtf));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

        // Dòng trống
        rowNum++;

        // Thông tin tổng hợp
        Row summaryTitleRow = sheet.createRow(rowNum++);
        summaryTitleRow.createCell(0).setCellValue("TỔNG HỢP");

        Row summaryRow1 = sheet.createRow(rowNum++);
        summaryRow1.createCell(0).setCellValue("Tổng giá trị kho:");
        Cell totalValue = summaryRow1.createCell(1);
        totalValue.setCellValue(tongHop.get("tongGiaTri") != null ? (double) tongHop.get("tongGiaTri") : 0);
        totalValue.setCellStyle(currencyStyle);

        Row summaryRow2 = sheet.createRow(rowNum++);
        summaryRow2.createCell(0).setCellValue("Tổng số lượng tồn:");
        summaryRow2.createCell(1).setCellValue(tongHop.get("tongSoLuong") != null ? (int) tongHop.get("tongSoLuong") : 0);

        Row summaryRow3 = sheet.createRow(rowNum++);
        summaryRow3.createCell(0).setCellValue("Số lô sắp/đã hết hạn:");
        summaryRow3.createCell(1).setCellValue(tongHop.get("soLoHetHan") != null ? (int) tongHop.get("soLoHetHan") : 0);

        // Dòng trống
        rowNum++;

        // Header bảng chi tiết
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Mã Thuốc", "Tên Thuốc", "Lô SX", "Hạn Dùng", "Đơn Giá Vốn", "Tồn Kho", "Tổng Giá Trị", "Ghi Chú"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Dữ liệu chi tiết
        for (Map<String, Object> row : chiTietList) {
            Row dataRow = sheet.createRow(rowNum++);
            String ghiChu = row.get("ghiChu") != null ? row.get("ghiChu").toString() : "";

            // Xác định style dựa trên ghi chú
            CellStyle rowStyle = normalStyle;
            if (ghiChu.contains("Đã hết hạn")) {
                rowStyle = dangerStyle;
            } else if (ghiChu.contains("Sắp hết hạn") || ghiChu.contains("Tồn kho thấp")) {
                rowStyle = warningStyle;
            }

            // Mã thuốc
            Cell maThuocCell = dataRow.createCell(0);
            maThuocCell.setCellValue(row.get("maThuoc") != null ? row.get("maThuoc").toString() : "");
            maThuocCell.setCellStyle(rowStyle);

            // Tên thuốc
            Cell tenThuocCell = dataRow.createCell(1);
            tenThuocCell.setCellValue(row.get("tenThuoc") != null ? row.get("tenThuoc").toString() : "");
            tenThuocCell.setCellStyle(rowStyle);

            // Lô SX
            Cell maLoCell = dataRow.createCell(2);
            maLoCell.setCellValue(row.get("maLo") != null ? row.get("maLo").toString() : "");
            maLoCell.setCellStyle(rowStyle);

            // Hạn dùng
            Cell hanDungCell = dataRow.createCell(3);
            LocalDate hanSuDung = (LocalDate) row.get("hanSuDung");
            if (hanSuDung != null) {
                hanDungCell.setCellValue(hanSuDung.format(dtf));
            }
            hanDungCell.setCellStyle(ghiChu.contains("hết hạn") ? dangerStyle : dateStyle);

            // Đơn giá vốn
            Cell giaVonCell = dataRow.createCell(4);
            giaVonCell.setCellValue((double) row.get("giaVon"));
            giaVonCell.setCellStyle(currencyStyle);

            // Tồn kho
            Cell tonKhoCell = dataRow.createCell(5);
            tonKhoCell.setCellValue((int) row.get("soLuongTon"));
            tonKhoCell.setCellStyle(rowStyle);

            // Tổng giá trị
            Cell tongGiaTriCell = dataRow.createCell(6);
            tongGiaTriCell.setCellValue((double) row.get("tongGiaTri"));
            tongGiaTriCell.setCellStyle(currencyStyle);

            // Ghi chú
            Cell ghiChuCell = dataRow.createCell(7);
            ghiChuCell.setCellValue(ghiChu);
            ghiChuCell.setCellStyle(rowStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Ghi file
        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.close();
        workbook.close();
    }

    // --- Helper methods để tạo style ---

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0\" ₫\""));
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createWarningStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createDangerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.DARK_RED.getIndex());
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createRefundStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0\" ₫\""));
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}