package tv.ismar.player.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableField;

import tv.ismar.player.BR;
import tv.ismar.player.presenter.PlayerPagePresenter;

/**
 * Created by longhai on 16-9-8.
 */
public class PlayerPageViewModel extends BaseObservable {

    private Context mContext;
    private PlayerPagePresenter mPresenter;
    public ObservableField<String> itemTitle;

    private int mCurrentPosition = 0;
    private int mClipLength = 0;

    public PlayerPageViewModel(Context context, PlayerPagePresenter presenter) {
        mContext = context;
        mPresenter = presenter;

        itemTitle = new ObservableField<>();
    }

    public void setPanelData(String title) {
        itemTitle.set(title);
    }

    public void updateTimer(int position, int length) {
        mCurrentPosition = position;
        mClipLength = length;
        notifyPropertyChanged(BR.timer);
    }

    @Bindable
    public String getTimer() {
        String text = getTimeString(mCurrentPosition) + "/"
                + getTimeString(mClipLength);
        return text;
    }

    private String getTimeString(int ms) {
        int left = ms;
        int hour = left / 3600000;
        left %= 3600000;
        int min = left / 60000;
        left %= 60000;
        int sec = left / 1000;
        return String.format("%1$02d:%2$02d:%3$02d", hour, min, sec);
    }

}
