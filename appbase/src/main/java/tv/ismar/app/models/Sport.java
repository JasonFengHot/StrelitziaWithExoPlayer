package tv.ismar.app.models;

import java.util.List;

public class Sport {

    private List<SportGame> living;

    public List<SportGame> getLiving() {
        return living;
    }

    public void setLiving(List<SportGame> living) {
        this.living = living;
    }
}
