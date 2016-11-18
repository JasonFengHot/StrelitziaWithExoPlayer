package tv.ismar.searchpage.data.http;

import java.util.List;

/**
 * Created by huaijie on 1/28/16.
 */
public class SharpHotWordsEntity {
    private int count;
    private List<SemantichObjectEntity> objects;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<SemantichObjectEntity> getObjects() {
        return objects;
    }

    public void setObjects(List<SemantichObjectEntity> objects) {
        this.objects = objects;
    }
}
