package utils;

import entities.ChiTietPhieuNhap;
import entities.Thuoc;
import java.io.File;
import java.io.FileInputStream;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
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
}