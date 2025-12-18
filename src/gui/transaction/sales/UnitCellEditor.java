package gui.transaction.sales;

import java.awt.Component;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import entities.DonViQuyDoi;

/**
 * Cell editor cho cột đơn vị tính trong giỏ hàng.
 * Hiển thị ComboBox với danh sách đơn vị quy đổi của thuốc.
 */
public class UnitCellEditor extends DefaultCellEditor {

    private JComboBox<String> comboBox;

    public UnitCellEditor() {
        super(new JComboBox<>());
        this.comboBox = (JComboBox<String>) getComponent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        comboBox.removeAllItems();

        int modelRow = table.convertRowIndexToModel(row);
        Object valList = table.getModel().getValueAt(modelRow, 6);

        if (valList instanceof List) {
            List<DonViQuyDoi> list = (List<DonViQuyDoi>) valList;
            for (DonViQuyDoi dv : list) {
                comboBox.addItem(dv.getTenDonVi());
            }
        }

        if (comboBox.getItemCount() == 0) {
            comboBox.addItem(value != null ? value.toString() : "");
        }

        comboBox.setSelectedItem(value);
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}
