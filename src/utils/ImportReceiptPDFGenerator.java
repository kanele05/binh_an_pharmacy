package utils;

import dao.ChiTietPhieuNhapDAO;
import entities.ChiTietPhieuNhap;
import entities.NhaCungCap;
import entities.NhanVien;
import entities.PhieuNhap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class to generate PDF import receipts (Phiếu nhập kho)
 */
public class ImportReceiptPDFGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Page dimensions (A4)
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN = 50;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    // Font sizes
    private static final float FONT_SIZE_TITLE = 18;
    private static final float FONT_SIZE_HEADER = 12;
    private static final float FONT_SIZE_NORMAL = 10;
    private static final float FONT_SIZE_SMALL = 9;

    // Table column widths (proportional) - STT, Tên thuốc, Mã lô, HSD, ĐVT, SL, Đơn giá, Thành tiền
    private static final float[] COL_WIDTHS = {30, 140, 50, 60, 50, 40, 70, 80};

    /**
     * Generate PDF import receipt and save to file
     * @param phieuNhap The import receipt to generate
     * @param outputPath Output file path
     * @return true if successful
     */
    public static boolean generateImportReceipt(PhieuNhap phieuNhap, String outputPath) {
        try (PDDocument document = new PDDocument()) {
            // Load Vietnamese-compatible font
            PDType0Font fontRegular = loadFont(document, "/fonts/Roboto-Regular.ttf");
            PDType0Font fontBold = loadFont(document, "/fonts/Roboto-Bold.ttf");

            // Create page
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float yPos = PAGE_HEIGHT - MARGIN;

                // Draw header
                yPos = drawHeader(cs, fontBold, fontRegular, yPos);

                // Draw receipt info
                yPos = drawReceiptInfo(cs, fontBold, fontRegular, phieuNhap, yPos);

                // Draw table
                yPos = drawTable(cs, fontBold, fontRegular, phieuNhap, yPos);

                // Draw summary
                yPos = drawSummary(cs, fontBold, fontRegular, phieuNhap, yPos);

                // Draw footer
                drawFooter(cs, fontBold, fontRegular, phieuNhap, yPos);
            }

            // Save document
            document.save(outputPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generate and open PDF import receipt
     */
    public static boolean generateAndOpenImportReceipt(PhieuNhap phieuNhap) {
        String fileName = "PhieuNhap_" + phieuNhap.getMaPN() + ".pdf";
        String tempDir = System.getProperty("java.io.tmpdir");
        String outputPath = tempDir + File.separator + fileName;

        if (generateImportReceipt(phieuNhap, outputPath)) {
            try {
                File pdfFile = new File(outputPath);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Load font from resources or use fallback
     */
    private static PDType0Font loadFont(PDDocument document, String resourcePath) throws IOException {
        try (InputStream is = ImportReceiptPDFGenerator.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                return PDType0Font.load(document, is);
            }
        } catch (Exception e) {
            // Ignore and try fallback
        }

        // Try to load from FlatLaf Roboto font in lib
        try {
            String userDir = System.getProperty("user.dir");
            String[] jarPaths = {
                userDir + File.separator + "lib" + File.separator + "flatlaf-fonts-roboto-2.137.jar",
                userDir + "/lib/flatlaf-fonts-roboto-2.137.jar"
            };

            for (String jarPath : jarPaths) {
                File jarFile = new File(jarPath);
                if (jarFile.exists()) {
                    try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile)) {
                        String fontName = resourcePath.contains("Bold") ?
                            "com/formdev/flatlaf/fonts/roboto/Roboto-Bold.ttf" :
                            "com/formdev/flatlaf/fonts/roboto/Roboto-Regular.ttf";
                        java.util.zip.ZipEntry entry = jar.getEntry(fontName);
                        if (entry != null) {
                            try (InputStream is = jar.getInputStream(entry)) {
                                byte[] fontData = is.readAllBytes();
                                return PDType0Font.load(document, new java.io.ByteArrayInputStream(fontData));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Final fallback: use system font (Windows fonts that support Vietnamese)
        String[] fallbackPaths = {
            "C:\\Windows\\Fonts\\arial.ttf",
            "C:/Windows/Fonts/arial.ttf",
            "C:\\Windows\\Fonts\\segoeui.ttf",
            "C:/Windows/Fonts/segoeui.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"
        };

        for (String path : fallbackPaths) {
            File fontFile = new File(path);
            if (fontFile.exists()) {
                return PDType0Font.load(document, fontFile);
            }
        }

        throw new IOException("No suitable font found for Vietnamese text");
    }

    /**
     * Draw pharmacy header
     */
    private static float drawHeader(PDPageContentStream cs, PDType0Font fontBold, PDType0Font fontRegular, float yPos) throws IOException {
        // Pharmacy name
        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_TITLE);
        String pharmacyName = "NHÀ THUỐC BÌNH AN";
        float textWidth = fontBold.getStringWidth(pharmacyName) / 1000 * FONT_SIZE_TITLE;
        cs.newLineAtOffset((PAGE_WIDTH - textWidth) / 2, yPos);
        cs.showText(pharmacyName);
        cs.endText();
        yPos -= 20;

        // Address
        cs.beginText();
        cs.setFont(fontRegular, FONT_SIZE_NORMAL);
        String address = "Địa chỉ: 12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM";
        textWidth = fontRegular.getStringWidth(address) / 1000 * FONT_SIZE_NORMAL;
        cs.newLineAtOffset((PAGE_WIDTH - textWidth) / 2, yPos);
        cs.showText(address);
        cs.endText();
        yPos -= 15;

        // Phone
        cs.beginText();
        cs.setFont(fontRegular, FONT_SIZE_NORMAL);
        String phone = "Điện thoại: 0917 774 020";
        textWidth = fontRegular.getStringWidth(phone) / 1000 * FONT_SIZE_NORMAL;
        cs.newLineAtOffset((PAGE_WIDTH - textWidth) / 2, yPos);
        cs.showText(phone);
        cs.endText();
        yPos -= 30;

        // Receipt title
        cs.beginText();
        cs.setFont(fontBold, 16);
        String title = "PHIẾU NHẬP KHO";
        textWidth = fontBold.getStringWidth(title) / 1000 * 16;
        cs.newLineAtOffset((PAGE_WIDTH - textWidth) / 2, yPos);
        cs.showText(title);
        cs.endText();
        yPos -= 10;

        // Draw line
        cs.setLineWidth(1);
        cs.moveTo(MARGIN, yPos);
        cs.lineTo(PAGE_WIDTH - MARGIN, yPos);
        cs.stroke();

        return yPos - 20;
    }

    /**
     * Draw import receipt information section
     */
    private static float drawReceiptInfo(PDPageContentStream cs, PDType0Font fontBold, PDType0Font fontRegular, PhieuNhap phieuNhap, float yPos) throws IOException {
        float leftCol = MARGIN;
        float rightCol = PAGE_WIDTH / 2 + 20;

        // Row 1: Receipt ID and Date
        String maPN = (phieuNhap.getMaPN() != null) ? phieuNhap.getMaPN() : "";
        String ngayTao = (phieuNhap.getNgayTao() != null) ? phieuNhap.getNgayTao().format(DATE_FORMAT) : "";

        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_NORMAL);
        cs.newLineAtOffset(leftCol, yPos);
        cs.showText("Mã phiếu nhập: ");
        cs.setFont(fontRegular, FONT_SIZE_NORMAL);
        cs.showText(maPN);
        cs.endText();

        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_NORMAL);
        cs.newLineAtOffset(rightCol, yPos);
        cs.showText("Ngày tạo: ");
        cs.setFont(fontRegular, FONT_SIZE_NORMAL);
        cs.showText(ngayTao);
        cs.endText();
        yPos -= 18;

        // Row 2: Supplier and Staff
        NhaCungCap ncc = phieuNhap.getNcc();
        String supplierName = (ncc != null && ncc.getTenNCC() != null) ? ncc.getTenNCC() : "";

        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_NORMAL);
        cs.newLineAtOffset(leftCol, yPos);
        cs.showText("Nhà cung cấp: ");
        cs.setFont(fontRegular, FONT_SIZE_NORMAL);
        cs.showText(supplierName);
        cs.endText();

        NhanVien nv = phieuNhap.getNhanVien();
        String staffName = (nv != null && nv.getHoTen() != null) ? nv.getHoTen() : "";
        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_NORMAL);
        cs.newLineAtOffset(rightCol, yPos);
        cs.showText("Nhân viên lập: ");
        cs.setFont(fontRegular, FONT_SIZE_NORMAL);
        cs.showText(staffName);
        cs.endText();
        yPos -= 18;

        // Row 3: Status and Note
        String status = (phieuNhap.getTrangThai() != null) ? phieuNhap.getTrangThai() : "";
        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_NORMAL);
        cs.newLineAtOffset(leftCol, yPos);
        cs.showText("Trạng thái: ");
        cs.setFont(fontRegular, FONT_SIZE_NORMAL);
        cs.showText(status);
        cs.endText();

        String note = (phieuNhap.getGhiChu() != null && !phieuNhap.getGhiChu().isEmpty()) 
            ? phieuNhap.getGhiChu() : "";
        if (!note.isEmpty()) {
            // Truncate if too long
            if (note.length() > 40) {
                note = note.substring(0, 37) + "...";
            }
            cs.beginText();
            cs.setFont(fontBold, FONT_SIZE_NORMAL);
            cs.newLineAtOffset(rightCol, yPos);
            cs.showText("Ghi chú: ");
            cs.setFont(fontRegular, FONT_SIZE_NORMAL);
            cs.showText(note);
            cs.endText();
        }

        return yPos - 25;
    }

    /**
     * Draw items table
     */
    private static float drawTable(PDPageContentStream cs, PDType0Font fontBold, PDType0Font fontRegular, PhieuNhap phieuNhap, float yPos) throws IOException {
        // Get receipt details
        ChiTietPhieuNhapDAO ctDAO = new ChiTietPhieuNhapDAO();
        List<ChiTietPhieuNhap> items = ctDAO.getChiTietByMaPhieu(phieuNhap.getMaPN());

        // Table headers
        String[] headers = {"STT", "Tên Thuốc", "Mã Lô", "HSD", "ĐVT", "SL", "Đơn Giá", "Thành Tiền"};
        float tableWidth = 0;
        for (float w : COL_WIDTHS) tableWidth += w;

        float startX = (PAGE_WIDTH - tableWidth) / 2;
        float rowHeight = 20;

        // Draw header background
        cs.setNonStrokingColor(0.9f, 0.9f, 0.9f);
        cs.addRect(startX, yPos - rowHeight, tableWidth, rowHeight);
        cs.fill();
        cs.setNonStrokingColor(0, 0, 0);

        // Draw header text
        float xPos = startX;
        for (int i = 0; i < headers.length; i++) {
            cs.beginText();
            cs.setFont(fontBold, FONT_SIZE_SMALL);
            float textWidth = fontBold.getStringWidth(headers[i]) / 1000 * FONT_SIZE_SMALL;
            float cellCenter = xPos + (COL_WIDTHS[i] - textWidth) / 2;
            cs.newLineAtOffset(cellCenter, yPos - 14);
            cs.showText(headers[i]);
            cs.endText();
            xPos += COL_WIDTHS[i];
        }

        // Draw header border
        cs.setLineWidth(0.5f);
        cs.addRect(startX, yPos - rowHeight, tableWidth, rowHeight);
        cs.stroke();

        yPos -= rowHeight;

        // Draw rows
        int stt = 1;
        for (ChiTietPhieuNhap ct : items) {
            xPos = startX;

            // STT
            drawCellText(cs, fontRegular, String.valueOf(stt++), xPos, yPos, COL_WIDTHS[0], true);
            xPos += COL_WIDTHS[0];

            // Tên thuốc
            String tenThuoc = "";
            if (ct.getThuoc() != null && ct.getThuoc().getTenThuoc() != null) {
                tenThuoc = ct.getThuoc().getTenThuoc();
                if (tenThuoc.length() > 22) {
                    tenThuoc = tenThuoc.substring(0, 19) + "...";
                }
            }
            drawCellText(cs, fontRegular, tenThuoc, xPos, yPos, COL_WIDTHS[1], false);
            xPos += COL_WIDTHS[1];

            // Mã lô
            String maLo = (ct.getLoThuoc() != null && ct.getLoThuoc().getMaLo() != null)
                ? ct.getLoThuoc().getMaLo() : "";
            drawCellText(cs, fontRegular, maLo, xPos, yPos, COL_WIDTHS[2], true);
            xPos += COL_WIDTHS[2];

            // HSD
            String hsd = (ct.getHanSuDung() != null) ? ct.getHanSuDung().format(DATE_FORMAT) : "";
            drawCellText(cs, fontRegular, hsd, xPos, yPos, COL_WIDTHS[3], true);
            xPos += COL_WIDTHS[3];

            // ĐVT
            String donViTinh = (ct.getDonViTinh() != null) ? ct.getDonViTinh() : "";
            drawCellText(cs, fontRegular, donViTinh, xPos, yPos, COL_WIDTHS[4], true);
            xPos += COL_WIDTHS[4];

            // SL
            drawCellText(cs, fontRegular, String.valueOf(ct.getSoLuong()), xPos, yPos, COL_WIDTHS[5], true);
            xPos += COL_WIDTHS[5];

            // Đơn giá
            drawCellText(cs, fontRegular, formatCurrency(ct.getDonGia()), xPos, yPos, COL_WIDTHS[6], true);
            xPos += COL_WIDTHS[6];

            // Thành tiền
            drawCellText(cs, fontRegular, formatCurrency(ct.getThanhTien()), xPos, yPos, COL_WIDTHS[7], true);

            // Draw row border
            cs.addRect(startX, yPos - rowHeight, tableWidth, rowHeight);
            cs.stroke();

            yPos -= rowHeight;
        }

        // Draw vertical lines
        xPos = startX;
        float tableTop = yPos + rowHeight * (items.size() + 1);
        for (int i = 0; i <= COL_WIDTHS.length; i++) {
            cs.moveTo(xPos, tableTop);
            cs.lineTo(xPos, yPos);
            cs.stroke();
            if (i < COL_WIDTHS.length) xPos += COL_WIDTHS[i];
        }

        return yPos - 20;
    }

    /**
     * Draw cell text
     */
    private static void drawCellText(PDPageContentStream cs, PDType0Font font, String text, float x, float y, float width, boolean center) throws IOException {
        cs.beginText();
        cs.setFont(font, FONT_SIZE_SMALL);
        float textWidth = font.getStringWidth(text) / 1000 * FONT_SIZE_SMALL;
        float xPos = center ? x + (width - textWidth) / 2 : x + 3;
        cs.newLineAtOffset(xPos, y - 14);
        cs.showText(text);
        cs.endText();
    }

    /**
     * Draw summary section
     */
    private static float drawSummary(PDPageContentStream cs, PDType0Font fontBold, PDType0Font fontRegular, PhieuNhap phieuNhap, float yPos) throws IOException {
        float rightAlign = PAGE_WIDTH - MARGIN - 150;
        float valueX = PAGE_WIDTH - MARGIN - 5;

        // Separator line
        cs.setLineWidth(0.5f);
        cs.moveTo(rightAlign - 50, yPos + 10);
        cs.lineTo(PAGE_WIDTH - MARGIN, yPos + 10);
        cs.stroke();

        // Total
        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_HEADER);
        cs.newLineAtOffset(rightAlign - 50, yPos);
        cs.showText("TỔNG TIỀN NHẬP:");
        cs.endText();

        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_HEADER);
        String totalStr = formatCurrency(phieuNhap.getTongTien());
        float textWidth = fontBold.getStringWidth(totalStr) / 1000 * FONT_SIZE_HEADER;
        cs.newLineAtOffset(valueX - textWidth, yPos);
        cs.showText(totalStr);
        cs.endText();

        return yPos - 40;
    }

    /**
     * Draw footer with signatures
     */
    private static void drawFooter(PDPageContentStream cs, PDType0Font fontBold, PDType0Font fontRegular, PhieuNhap phieuNhap, float yPos) throws IOException {
        float col1 = MARGIN + 50;
        float col2 = PAGE_WIDTH / 2;
        float col3 = PAGE_WIDTH - MARGIN - 100;

        // Signature labels
        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_NORMAL);
        String label1 = "Người giao hàng";
        float textWidth = fontBold.getStringWidth(label1) / 1000 * FONT_SIZE_NORMAL;
        cs.newLineAtOffset(col1 - textWidth / 2, yPos);
        cs.showText(label1);
        cs.endText();

        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_NORMAL);
        String label2 = "Thủ kho";
        textWidth = fontBold.getStringWidth(label2) / 1000 * FONT_SIZE_NORMAL;
        cs.newLineAtOffset(col2 - textWidth / 2, yPos);
        cs.showText(label2);
        cs.endText();

        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_NORMAL);
        String label3 = "Người lập phiếu";
        textWidth = fontBold.getStringWidth(label3) / 1000 * FONT_SIZE_NORMAL;
        cs.newLineAtOffset(col3 - textWidth / 2, yPos);
        cs.showText(label3);
        cs.endText();

        yPos -= 15;

        // Signature hint
        cs.beginText();
        cs.setFont(fontRegular, FONT_SIZE_SMALL);
        String hint = "(Ký, ghi rõ họ tên)";
        textWidth = fontRegular.getStringWidth(hint) / 1000 * FONT_SIZE_SMALL;
        cs.newLineAtOffset(col1 - textWidth / 2, yPos);
        cs.showText(hint);
        cs.endText();

        cs.beginText();
        cs.setFont(fontRegular, FONT_SIZE_SMALL);
        cs.newLineAtOffset(col2 - textWidth / 2, yPos);
        cs.showText(hint);
        cs.endText();

        cs.beginText();
        cs.setFont(fontRegular, FONT_SIZE_SMALL);
        cs.newLineAtOffset(col3 - textWidth / 2, yPos);
        cs.showText(hint);
        cs.endText();

        // Staff name under "Người lập phiếu"
        yPos -= 50;
        NhanVien nv = phieuNhap.getNhanVien();
        String staffName = (nv != null && nv.getHoTen() != null) ? nv.getHoTen() : "";
        if (!staffName.isEmpty()) {
            cs.beginText();
            cs.setFont(fontRegular, FONT_SIZE_NORMAL);
            textWidth = fontRegular.getStringWidth(staffName) / 1000 * FONT_SIZE_NORMAL;
            cs.newLineAtOffset(col3 - textWidth / 2, yPos);
            cs.showText(staffName);
            cs.endText();
        }
    }

    /**
     * Format currency without currency symbol for cleaner display
     */
    private static String formatCurrency(double amount) {
        return String.format("%,.0f đ", amount);
    }
}


