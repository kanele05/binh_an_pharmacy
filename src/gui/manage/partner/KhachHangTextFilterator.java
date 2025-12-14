package gui.manage.partner;

import ca.odell.glazedlists.TextFilterator;
import entities.KhachHang;
import java.util.List;

public class KhachHangTextFilterator implements TextFilterator<KhachHang> {
    @Override
    public void getFilterStrings(List<String> baseList, KhachHang element) {
        if (element.getTenKH() != null) baseList.add(element.getTenKH());
        if (element.getSdt() != null) baseList.add(element.getSdt());
        if (element.getDiaChi() != null) baseList.add(element.getDiaChi());
    }
}
