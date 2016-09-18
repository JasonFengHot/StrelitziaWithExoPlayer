package tv.ismar.player.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableField;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.player.BR;
import tv.ismar.player.R;
import tv.ismar.player.media.IsmartvPlayer;
import tv.ismar.player.presenter.PlayerPagePresenter;

/**
 * Created by longhai on 16-9-8.
 */
public class PlayerPageViewModel extends BaseObservable {

    private Context mContext;
    private PlayerPagePresenter mPresenter;
    public ObservableField<String> itemTitle;

    private IsmartvPlayer mIsmartvPlayer;
    private int mCurrentPosition = 0;
    private int mClipLength = 0;

    public PlayerPageViewModel(Context context, PlayerPagePresenter presenter) {
        mContext = context;
        mPresenter = presenter;

        itemTitle = new ObservableField<>();
    }

    public void setPanelData(IsmartvPlayer ismartvPlayer, String title) {
        itemTitle.set(title);
        mIsmartvPlayer = ismartvPlayer;

        notifyPropertyChanged(BR.quality);
        notifyPropertyChanged(BR.qualityResource);
    }

    public void updateTimer(int position, int length) {
        mCurrentPosition = position;
        mClipLength = length;
        notifyPropertyChanged(BR.timer);
    }

    public void updatePlayerPause() {
        notifyPropertyChanged(BR.playPauseBackgroundRes);
    }

    @Bindable
    public String getTimer() {
        String text = getTimeString(mCurrentPosition) + "/"
                + getTimeString(mClipLength);
        return text;
    }

    @Bindable
    public String getQuality() {
        if (mIsmartvPlayer == null) {
            return "";
        }
        switch (mIsmartvPlayer.getCurrentQuality()) {
            case QUALITY_LOW:// 已弃用
                return ClipEntity.Quality.getString(ClipEntity.Quality.QUALITY_LOW);
            case QUALITY_ADAPTIVE:// 自适应
                return ClipEntity.Quality.getString(ClipEntity.Quality.QUALITY_ADAPTIVE);
            case QUALITY_NORMAL:// 流畅
            case QUALITY_MEDIUM:// 高清
            case QUALITY_HIGH:// 超清
            case QUALITY_ULTRA:// 1080P
            case QUALITY_BLUERAY:// 蓝光
            case QUALITY_4K:// 4K
            default:
                return "";
        }
    }

    @Bindable
    public Drawable getQualityResource() {
        if (mIsmartvPlayer == null) {
            return new ColorDrawable(0);
        }
        Log.i("LH/", "quality:" + mIsmartvPlayer.getCurrentQuality());
        switch (mIsmartvPlayer.getCurrentQuality()) {
            case QUALITY_LOW:// 已弃用
                return mContext.getResources().getDrawable(R.drawable.player_quality_back);
            case QUALITY_ADAPTIVE:// 自适应
                return mContext.getResources().getDrawable(R.drawable.player_quality_back);
            case QUALITY_NORMAL:// 流畅
                return mContext.getResources().getDrawable(R.drawable.player_stream_normal);
            case QUALITY_MEDIUM:// 高清
                return mContext.getResources().getDrawable(R.drawable.player_stream_high);
            case QUALITY_HIGH:// 超清
                return mContext.getResources().getDrawable(R.drawable.player_stream_super);
            case QUALITY_ULTRA:// 1080P
                return mContext.getResources().getDrawable(R.drawable.player_stream_1080p);
            case QUALITY_BLUERAY:// 蓝光
                return mContext.getResources().getDrawable(R.drawable.player_stream_blueray);
            case QUALITY_4K:// 4K
                return mContext.getResources().getDrawable(R.drawable.player_stream_4k);
            default:
                return mContext.getResources().getDrawable(R.drawable.player_quality_back);
        }
    }

    @Bindable
    public Drawable getPlayPauseBackgroundRes() {
        if (mIsmartvPlayer != null && mIsmartvPlayer.isInPlaybackState()) {
            if (mIsmartvPlayer.isPlaying()) {
                return mContext.getResources().getDrawable(R.drawable.selector_player_pause);
            } else {
                return mContext.getResources().getDrawable(R.drawable.selector_player_play);
            }
        }
        return mContext.getResources().getDrawable(R.drawable.selector_player_pause);
    }

    public void onPlayPause() {
        if (mIsmartvPlayer != null && mIsmartvPlayer.isInPlaybackState()) {
            if (mIsmartvPlayer.isPlaying()) {
                mIsmartvPlayer.pause();
            } else {
                mIsmartvPlayer.start();
            }
        }

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
