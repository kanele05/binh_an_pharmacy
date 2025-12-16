package utils;

import dao.ChiTietHoaDonDAO;
import entities.ChiTietHoaDon;
import entities.HoaDon;
import entities.KhachHang;
import entities.NhanVien;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Utility class to generate PDF invoices for sales
 */
public class InvoicePDFGenerator {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

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

    // Table column widths (proportional)
    private static final float[] COL_WIDTHS = {30, 170, 50, 50, 80, 40, 80};

    /**
     * Generate PDF invoice and save to file
     * @param hoaDon The invoice to generate
     * @param outputPath Output file path
     * @return true if successful
     */
    public static boolean generateInvoice(HoaDon hoaDon, String outputPath) {
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

                // Draw invoice info
                yPos = drawInvoiceInfo(cs, fontBold, fontRegular, hoaDon, yPos);

                // Draw table
                yPos = drawTable(cs, fontBold, fontRegular, hoaDon, yPos);

                // Draw summary
                yPos = drawSummary(cs, fontBold, fontRegular, hoaDon, yPos);

                // Draw footer
                drawFooter(cs, fontRegular, yPos);
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
     * Generate and open PDF invoice
     */
    public static boolean generateAndOpenInvoice(HoaDon hoaDon) {
        String fileName = "HoaDon_" + hoaDon.getMaHD() + ".pdf";
        String tempDir = System.getProperty("java.io.tmpdir");
        String outputPath = tempDir + File.separator + fileName;

        if (generateInvoice(hoaDon, outputPath)) {
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
        try (InputStream is = InvoicePDFGenerator.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                return PDType0Font.load(document, is);
            }
        } catch (Exception e) {
            // Ignore and try fallback
        }

        // Try to load from FlatLaf Roboto font in lib
        try {
            String userDir = System.getProperty("user.dir");
            // Try multiple path formats for Windows compatibility
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
                                // Read all bytes first to avoid stream close issues
                                byte[] fontData = is.readAllBytes();
                                return PDType0Font.load(document, new java.io.ByteArrayInputStream(fontData));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Ignore and try system font
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
        String phone = "Điện thoại: 0909 123 456";
        textWidth = fontRegular.getStringWidth(phone) / 1000 * FONT_SIZE_NORMAL;
        cs.newLineAtOffset((PAGE_WIDTH - textWidth) / 2, yPos);
        cs.showText(phone);
        cs.endText();
        yPos -= 30;

        // Invoice title
        cs.beginText();
        cs.setFont(fontBold, 16);
        String title = "HÓA ĐƠN BÁN HÀNG";
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
     * Draw invoice information section
     */
    private static float drawInvoiceInfo(PDPageContentStream cs, PDType0Font fontBold, PDType0Font fontRegular, HoaDon hoaDon, float yPos) throws IOException {
        float leftCol = MARGIN;
        float rightCol = PAGE_WIDTH / 2 + 20;

        // Row 1: Invoice ID and Date - handle null safely
        String maHD = (hoaDon.getMaHD() != null) ? hoaDon.getMaHD() : "";
        String ngayTao = (hoaDon.getNgayTao() != null) ? hoaDon.getNgayTao().format(DATE_FORMAT) : "";

        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_NORMAL);
        cs.newLineAtOffset(leftCol, yPos);
        cs.showText("Mã hóa đơn: ");
        cs.setFont(fontRegular, FONT_SIZE_NORMAL);
        cs.showText(maHD);
        cs.endText();

        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_NORMAL);
        cs.newLineAtOffset(rightCol, yPos);
        cs.showText("Ngày lập: ");
        cs.setFont(fontRegular, FONT_SIZE_NORMAL);
        cs.showText(ngayTao);
        cs.endText();
        yPos -= 18;

        // Row 2: Customer and Staff
        KhachHang kh = hoaDon.getKhachHang();
        String customerName = (kh != null && kh.getTenKH() != null) ? kh.getTenKH() : "Khách lẻ";
        String customerPhone = (kh != null && kh.getSdt() != null) ? kh.getSdt() : "";

        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_NORMAL);
        cs.newLineAtOffset(leftCol, yPos);
        cs.showText("Khách hàng: ");
        cs.setFont(fontRegular, FONT_SIZE_NORMAL);
        cs.showText(customerName);
        cs.endText();

        NhanVien nv = hoaDon.getNhanVien();
        String staffName = (nv != null && nv.getHoTen() != null) ? nv.getHoTen() : "";
        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_NORMAL);
        cs.newLineAtOffset(rightCol, yPos);
        cs.showText("Nhân viên: ");
        cs.setFont(fontRegular, FONT_SIZE_NORMAL);
        cs.showText(staffName);
        cs.endText();
        yPos -= 18;

        // Row 3: Customer phone and Payment method
        if (!customerPhone.isEmpty()) {
            cs.beginText();
            cs.setFont(fontBold, FONT_SIZE_NORMAL);
            cs.newLineAtOffset(leftCol, yPos);
            cs.showText("SĐT: ");
            cs.setFont(fontRegular, FONT_SIZE_NORMAL);
            cs.showText(customerPhone);
            cs.endText();
        }

        String paymentMethod = (hoaDon.getHinhThucTT() != null && !hoaDon.getHinhThucTT().isEmpty())
            ? hoaDon.getHinhThucTT() : "Tiền mặt";
        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_NORMAL);
        cs.newLineAtOffset(rightCol, yPos);
        cs.showText("Thanh toán: ");
        cs.setFont(fontRegular, FONT_SIZE_NORMAL);
        cs.showText(paymentMethod);
        cs.endText();

        return yPos - 25;
    }

    /**
     * Draw items table
     */
    private static float drawTable(PDPageContentStream cs, PDType0Font fontBold, PDType0Font fontRegular, HoaDon hoaDon, float yPos) throws IOException {
        // Get invoice details
        ChiTietHoaDonDAO ctDAO = new ChiTietHoaDonDAO();
        ArrayList<ChiTietHoaDon> items = ctDAO.getChiTietByMaHD(hoaDon.getMaHD());

        // Table headers
        String[] headers = {"STT", "Tên Thuốc", "Lô", "ĐVT", "Đơn Giá", "SL", "Thành Tiền"};
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
        for (ChiTietHoaDon ct : items) {
            xPos = startX;

            // STT
            drawCellText(cs, fontRegular, String.valueOf(stt++), xPos, yPos, COL_WIDTHS[0], true);
            xPos += COL_WIDTHS[0];

            // Tên thuốc - handle null safely
            String tenThuoc = "";
            if (ct.getThuoc() != null && ct.getThuoc().getTenThuoc() != null) {
                tenThuoc = ct.getThuoc().getTenThuoc();
                if (tenThuoc.length() > 28) {
                    tenThuoc = tenThuoc.substring(0, 25) + "...";
                }
            }
            drawCellText(cs, fontRegular, tenThuoc, xPos, yPos, COL_WIDTHS[1], false);
            xPos += COL_WIDTHS[1];

            // Lô - handle null safely
            String maLo = (ct.getLoThuoc() != null && ct.getLoThuoc().getMaLo() != null)
                ? ct.getLoThuoc().getMaLo() : "";
            drawCellText(cs, fontRegular, maLo, xPos, yPos, COL_WIDTHS[2], true);
            xPos += COL_WIDTHS[2];

            // ĐVT - handle null safely
            String donViTinh = (ct.getDonViTinh() != null) ? ct.getDonViTinh() : "";
            drawCellText(cs, fontRegular, donViTinh, xPos, yPos, COL_WIDTHS[3], true);
            xPos += COL_WIDTHS[3];

            // Đơn giá
            drawCellText(cs, fontRegular, formatCurrency(ct.getDonGia()), xPos, yPos, COL_WIDTHS[4], true);
            xPos += COL_WIDTHS[4];

            // SL
            drawCellText(cs, fontRegular, String.valueOf(ct.getSoLuong()), xPos, yPos, COL_WIDTHS[5], true);
            xPos += COL_WIDTHS[5];

            // Thành tiền
            drawCellText(cs, fontRegular, formatCurrency(ct.getThanhTien()), xPos, yPos, COL_WIDTHS[6], true);

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
    private static float drawSummary(PDPageContentStream cs, PDType0Font fontBold, PDType0Font fontRegular, HoaDon hoaDon, float yPos) throws IOException {
        float rightAlign = PAGE_WIDTH - MARGIN - 150;
        float valueX = PAGE_WIDTH - MARGIN - 5;

        // Subtotal
        drawSummaryLine(cs, fontRegular, "Tổng tiền hàng:", formatCurrency(hoaDon.getTongTien()), rightAlign, valueX, yPos);
        yPos -= 18;

        // Discount
        if (hoaDon.getGiamGia() > 0) {
            drawSummaryLine(cs, fontRegular, "Giảm giá (Điểm):", "-" + formatCurrency(hoaDon.getGiamGia()), rightAlign, valueX, yPos);
            yPos -= 18;
        }

        // VAT
        drawSummaryLine(cs, fontRegular, "Thuế VAT (5%):", "+" + formatCurrency(hoaDon.getThue()), rightAlign, valueX, yPos);
        yPos -= 5;

        // Separator line
        cs.setLineWidth(0.5f);
        cs.moveTo(rightAlign - 50, yPos);
        cs.lineTo(PAGE_WIDTH - MARGIN, yPos);
        cs.stroke();
        yPos -= 18;

        // Total
        double total = hoaDon.getTongTien() + hoaDon.getThue() - hoaDon.getGiamGia();
        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_HEADER);
        cs.newLineAtOffset(rightAlign - 50, yPos);
        cs.showText("TỔNG THANH TOÁN:");
        cs.endText();

        cs.beginText();
        cs.setFont(fontBold, FONT_SIZE_HEADER);
        String totalStr = formatCurrency(total);
        float textWidth = fontBold.getStringWidth(totalStr) / 1000 * FONT_SIZE_HEADER;
        cs.newLineAtOffset(valueX - textWidth, yPos);
        cs.showText(totalStr);
        cs.endText();

        return yPos - 30;
    }

    /**
     * Draw summary line
     */
    private static void drawSummaryLine(PDPageContentStream cs, PDType0Font font, String label, String value, float labelX, float valueX, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, FONT_SIZE_NORMAL);
        cs.newLineAtOffset(labelX, y);
        cs.showText(label);
        cs.endText();

        cs.beginText();
        cs.setFont(font, FONT_SIZE_NORMAL);
        float textWidth = font.getStringWidth(value) / 1000 * FONT_SIZE_NORMAL;
        cs.newLineAtOffset(valueX - textWidth, y);
        cs.showText(value);
        cs.endText();
    }

    /**
     * Draw footer
     */
    private static void drawFooter(PDPageContentStream cs, PDType0Font font, float yPos) throws IOException {
        yPos -= 20;

        // Thank you message
        cs.beginText();
        cs.setFont(font, FONT_SIZE_NORMAL);
        String thankYou = "Cảm ơn quý khách đã mua hàng!";
        float textWidth = font.getStringWidth(thankYou) / 1000 * FONT_SIZE_NORMAL;
        cs.newLineAtOffset((PAGE_WIDTH - textWidth) / 2, yPos);
        cs.showText(thankYou);
        cs.endText();
        yPos -= 15;

        // Return policy
        cs.beginText();
        cs.setFont(font, FONT_SIZE_SMALL);
        String policy = "Sản phẩm đã mua được đổi trả trong vòng 7 ngày nếu còn nguyên vẹn.";
        textWidth = font.getStringWidth(policy) / 1000 * FONT_SIZE_SMALL;
        cs.newLineAtOffset((PAGE_WIDTH - textWidth) / 2, yPos);
        cs.showText(policy);
        cs.endText();
    }

    /**
     * Format currency without currency symbol for cleaner display
     */
    private static String formatCurrency(double amount) {
        return String.format("%,.0f đ", amount);
    }
}
