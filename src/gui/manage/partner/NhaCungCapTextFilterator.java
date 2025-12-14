package gui.manage.partner;

import ca.odell.glazedlists.TextFilterator;
import entities.NhaCungCap;
import java.util.List;

public class NhaCungCapTextFilterator implements TextFilterator<NhaCungCap> {
    @Override
    public void getFilterStrings(List<String> baseList, NhaCungCap element) {
        if (element.getTenNCC() != null) baseList.add(element.getTenNCC());
        if (element.getSdt() != null) baseList.add(element.getSdt());
        if (element.getEmail() != null) baseList.add(element.getEmail());
    }
}
