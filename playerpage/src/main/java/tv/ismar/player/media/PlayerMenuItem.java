package tv.ismar.player.media;

import java.util.ArrayList;

public class PlayerMenuItem {

    protected String title;
    protected int id = -1;
    protected boolean selected;
    public boolean isSub = false;
    protected ArrayList<PlayerMenuItem> subItems;

    PlayerMenuItem(int id, String title, boolean selected) {
        this.id = id;
        this.title = title;
        this.selected = selected;
    }

    PlayerMenuItem(int id, String title) {
        this.id = id;
        this.title = title;
        this.selected = false;
    }

    public PlayerMenuItem addItem(int id, String title, boolean selected) {
        if (subItems == null) {
            subItems = new ArrayList<PlayerMenuItem>();
        }
        PlayerMenuItem item = new PlayerMenuItem(id, title, selected);
        subItems.add(item);
        return item;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitle(int id, String title) {
        PlayerMenuItem item = findItem(id);
        if (item != null) {
            item.setTitle(title);
        }
    }

    public PlayerMenuItem addItem(int id, String title) {
        return addItem(id, title, false);
    }

    public PlayerMenuItem addSubMenu(int id, String title, boolean selected) {
        PlayerMenuItem item = addItem(id, title, selected);
        item.isSub = true;
        return item;
    }

    public PlayerMenuItem addSubMenu(int id, String title) {
        return addSubMenu(id, title, false);
    }

    public PlayerMenuItem findItem(int id) {
        if (this.id == id)
            return this;
        if (subItems != null) {
            for (PlayerMenuItem item : subItems) {
                PlayerMenuItem find = item.findItem(id);
                if (find != null)
                    return find;
            }
        }
        return null;
    }

    public void clear() {
        subItems = null;
    }

    public void select() {
        selected = true;
    }

    public void unselect() {
        selected = false;
    }

    public boolean isSelected() {
        return selected;
    }
}
