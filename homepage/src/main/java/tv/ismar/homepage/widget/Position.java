package tv.ismar.homepage.widget;

/**
 * Created by huaijie on 7/2/15.
 */
public class Position {

    private PositioinChangeCallback changeCallback;

    private int position;

    public Position(PositioinChangeCallback changeCallback) {
        this.changeCallback = changeCallback;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
        changeCallback.onChange(position);
    }

    public interface PositioinChangeCallback {
        void onChange(int position);
    }
}
