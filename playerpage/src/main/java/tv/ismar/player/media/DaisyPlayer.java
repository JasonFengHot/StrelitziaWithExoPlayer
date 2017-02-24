package tv.ismar.player.media;

import android.text.TextUtils;
import android.view.View;

import java.util.Arrays;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.util.Utils;

/**
 * Created by longhai on 16-9-12.
 */
public class DaisyPlayer extends IsmartvPlayer implements DaisyVideoView.AdErrorListener {

    private String[] mPaths;

    public DaisyPlayer(byte mode) {
        super(mode);
    }

    @Override
    public boolean isDownloadError() {
        if(mDaisyVideoView == null){
            return false;
        }
        return mDaisyVideoView.isDownloadError();
    }

    @Override
    public void bufferOnSharpS3Release() {
        if(mDaisyVideoView == null){
            return;
        }
        mDaisyVideoView.bufferOnSharpS3Release();
        mDaisyVideoView = null;

    }

    @Override
    protected void setMedia(String[] urls) {
        mPaths = urls;
        mDaisyVideoView.setVisibility(View.VISIBLE);
        mContainer.setVisibility(View.GONE);
        mDaisyVideoView.setSnToken(IsmartvActivator.getInstance().getSnToken());
        mDaisyVideoView.setAdErrorListener(this);
        mDaisyVideoView.setVideoPaths(mPaths, this);
        logVideoStart();
        super.setMedia(urls);

    }

    @Override
    public boolean isInPlaybackState() {
        if (mDaisyVideoView == null) {
            return false;
        }
        return mDaisyVideoView.isInPlaybackState();
    }

    @Override
    public void prepareAsync() {
        //调用prepareAsync, 播放器开始准备, 必须调用
        if (mDaisyVideoView == null) {
            return;
        }
        mDaisyVideoView.prepareAsync();
    }

    @Override
    public void start() {
        if (mDaisyVideoView == null) {
            return;
        }
        mDaisyVideoView.start();
    }

    @Override
    public void pause() {
        if (mDaisyVideoView == null) {
            return;
        }
        mDaisyVideoView.pause();
    }

    @Override
    public void stopPlayBack() {
        if (mDaisyVideoView == null) {
            return;
        }
        mDaisyVideoView.stopPlayback();
        super.stopPlayBack();
    }

    @Override
    public void seekTo(int position) {
        if (mDaisyVideoView == null) {
            return;
        }
        mDaisyVideoView.seekTo(position);
    }

    @Override
    public int getCurrentPosition() {
        if (mDaisyVideoView == null) {
            return 0;
        }
        return mDaisyVideoView.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        if (mDaisyVideoView == null) {
            return 0;
        }
        return mDaisyVideoView.getDuration();
    }

    @Override
    public int getAdCountDownTime() {
        if (mAdvertisementTime == null || !mIsPlayingAdvertisement || mDaisyVideoView == null) {
            return 0;
        }
        int totalAdTime = 0;
        int currentAd = mDaisyVideoView.getCurrentPlayUrl();
        if (currentAd == mPaths.length - 1) {
            return 0;
        } else if (currentAd == mAdvertisementTime.length - 1) {
            totalAdTime = mAdvertisementTime[mAdvertisementTime.length - 1];
        } else {
            for (int i = currentAd; i < mAdvertisementTime.length; i++) {
                totalAdTime += mAdvertisementTime[i];
            }
        }
        return totalAdTime * 1000 - getCurrentPosition();
    }

    @Override
    public boolean isPlaying() {
        if (mDaisyVideoView == null) {
            return false;
        }
        return mDaisyVideoView.isPlaying();
    }

    @Override
    public void switchQuality(ClipEntity.Quality quality) {
        String mediaUrl = getSmartQualityUrl(quality);
        if (!Utils.isEmptyText(mediaUrl)) {
            mQuality = quality;
            mPaths = new String[]{mediaUrl};

            mDaisyVideoView.release(true);
            mDaisyVideoView.setVideoPaths(mPaths, this);

            logVideoSwitchQuality();
        }
    }

    @Override
    public void onAdError(String url) {
        if (!TextUtils.isEmpty(url)) {
            for (int i = 0; i < mPaths.length; i++) {
                if (mPaths[i].equals(url)) {
                    if (i < mPaths.length - 1) {
                        String[] paths = Arrays.copyOfRange(mPaths, i + 1, mPaths.length);
                        setMedia(paths);
                    }
                    break;
                }
            }
        }
    }
}
