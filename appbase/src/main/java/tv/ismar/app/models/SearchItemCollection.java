package tv.ismar.app.models;

import android.util.SparseArray;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This is a easy to operated model of ItemList. It can remember which page of the section has been
 * filled. Remember NEVER USE {@link SearchItemCollection#objects} 's size() method to get the total
 * size of the ItemCollection. Use {@link SearchItemCollection#count} instead.
 *
 * @author bob
 */
public class SearchItemCollection implements Serializable {

    public static final int NUM_PER_PAGE = 100;
    private static final long serialVersionUID = -514170357600555210L;
    public int count;
    public int num_pages;
    public SparseArray<SemantichObjectEntity> objects;
    public String title;

    public boolean[] hasFilledValidItem;

    public SearchItemCollection(int num_pages, int count, String title) {
        this.count = count;
        this.num_pages = num_pages;
        this.hasFilledValidItem = new boolean[num_pages];
        this.objects = new SparseArray<SemantichObjectEntity>(count);
        this.title = title;
    }

    /**
     * Check if current index's page is already filled with valid data.
     *
     * @param index the index of {@link SearchItemCollection#objects}.
     * @return true if valid data has been filled. otherwise return false
     */
    public boolean isItemReady(int index) {
        // page is zero-based. but in api, it should be a nature number.
        int page = index / NUM_PER_PAGE;
        if (page < hasFilledValidItem.length) {
            return hasFilledValidItem[page];
        }
        return false;
    }

    public void fillItems(int page, ArrayList<SemantichObjectEntity> itemList) {
        if (hasFilledValidItem.length < num_pages) {
            hasFilledValidItem = new boolean[num_pages];
        }
        for (int i = 0; i < itemList.size(); i++) {
            objects.put(i + page * NUM_PER_PAGE, itemList.get(i));
        }
        hasFilledValidItem[page] = true;
    }
}
