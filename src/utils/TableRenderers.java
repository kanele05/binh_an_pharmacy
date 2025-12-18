package utils;

import java.awt.Component;
import java.awt.Font;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Shared table cell renderers for consistent formatting across the application.
 */
public class TableRenderers {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Renderer for center-aligned text cells.
     */
    public static class CenterRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    }

    /**
     * Renderer for LocalDateTime values with dd/MM/yyyy HH:mm format.
     */
    public static class DateTimeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof LocalDateTime) {
                setText(((LocalDateTime) value).format(DATE_TIME_FORMAT));
            }
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    }

    /**
     * Renderer for LocalDate values with dd/MM/yyyy format.
     */
    public static class DateRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof LocalDate) {
                setText(((LocalDate) value).format(DATE_FORMAT));
            }
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    }

    /**
     * Renderer for currency values with Vietnamese format and styling.
     */
    public static class CurrencyRenderer extends DefaultTableCellRenderer {
        private static final java.awt.Color CURRENCY_COLOR = new java.awt.Color(0, 150, 136);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Double) {
                setText(CURRENCY_FORMAT.format(value));
                if (!isSelected) {
                    setForeground(CURRENCY_COLOR);
                }
                setFont(getFont().deriveFont(Font.BOLD));
            }
            setHorizontalAlignment(JLabel.RIGHT);
            return this;
        }
    }

    /**
     * Format a double value as Vietnamese currency.
     */
    public static String formatCurrency(double amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + " ₫";
    }

    /**
     * Parse a Vietnamese currency string to double.
     */
    public static double parseCurrency(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(text.replace(".", "").replace(",", "").replace(" ₫", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
