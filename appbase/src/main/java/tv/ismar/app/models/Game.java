package tv.ismar.app.models;

import java.util.List;

/** Created by admin on 2016/10/27. */
public class Game {

    private List<SportGame> living;

    private List<SportGame> highlight;

    public List<SportGame> getLiving() {
        return living;
    }

    public void setLiving(List<SportGame> living) {
        this.living = living;
    }

    public List<SportGame> getHighlight() {
        return highlight;
    }

    public void setHighlight(List<SportGame> highlight) {
        this.highlight = highlight;
    }
}
