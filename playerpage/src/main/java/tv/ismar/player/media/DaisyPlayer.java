package tv.ismar.player.media;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.util.Utils;

/**
 * Created by longhai on 16-9-12.
 */
public class DaisyPlayer extends IsmartvPlayer implements DaisyVideoView.AdErrorListener {

    private String[] mPaths;
    private ClipEntity.Quality mQuality;
    private List<ClipEntity.Quality> mQualities;

    public DaisyPlayer(byte mode) {
        super(mode);
    }

    @Override
    public boolean isDownloadError() {
        if (mDaisyVideoView == null) {
            return false;
        }
        return mDaisyVideoView.isDownloadError();
    }

    @Override
    protected void setMedia(String[] urls) {
        super.setMedia(urls);
        if (mDaisyVideoView == null) {
            return;
        }
        mDaisyVideoView.setmOnDataSourceSetListener(mOnDataSourceSetListener);
        mDaisyVideoView.setmOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mDaisyVideoView.setmOnBufferChangedListener(mOnBufferChangedListener);
        mDaisyVideoView.setmOnStateChangedListener(mOnStateChangedListener);
        mDaisyVideoView.setmOnInfoListener(mOnInfoListener);

        mPaths = urls;
        mDaisyVideoView.setSnToken(IsmartvActivator.getInstance().getSnToken());
        mDaisyVideoView.setAdErrorListener(this);
        mLogMedia.setQuality(getQualityIndex(mQuality));
        mDaisyVideoView.setVideoPaths(mPaths, mStartPosition, mLogMedia, false);

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
        mDaisyVideoView.stopPlayback(true);
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
        if (mAdvertisementTime == null || mDaisyVideoView == null || !mDaisyVideoView.ismIsPlayingAdvertisement()) {
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

            if (mItemEntity != null && !mItemEntity.getLiveVideo()) {
                mStartPosition = getCurrentPosition();
            }
            mDaisyVideoView.release(true);
            mLogMedia.setQuality(getQualityIndex(mQuality));
            mDaisyVideoView.setVideoPaths(mPaths, mStartPosition, mLogMedia, true);
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

    @Override
    public ClipEntity.Quality getCurrentQuality() {
        return mQuality;
    }

    @Override
    public List<ClipEntity.Quality> getQulities() {
        return mQualities;
    }

    protected String initSmartQuality(ClipEntity.Quality initQuality) {
        if (mClipEntity == null) {
            return null;
        }
        String defaultQualityUrl = null;
        mQualities = new ArrayList<>();
        String low = mClipEntity.getLow();
        if (!Utils.isEmptyText(low)) {
            mQualities.add(ClipEntity.Quality.QUALITY_LOW);
        }
        String adaptive = mClipEntity.getAdaptive();
        if (!Utils.isEmptyText(adaptive)) {
            mQualities.add(ClipEntity.Quality.QUALITY_ADAPTIVE);
        }
        String normal = mClipEntity.getNormal();
        if (!Utils.isEmptyText(normal)) {
            mQualities.add(ClipEntity.Quality.QUALITY_NORMAL);
        }
        String medium = mClipEntity.getMedium();
        if (!Utils.isEmptyText(medium)) {
            mQualities.add(ClipEntity.Quality.QUALITY_MEDIUM);
        }
        String high = mClipEntity.getHigh();
        if (!Utils.isEmptyText(high)) {
            mQualities.add(ClipEntity.Quality.QUALITY_HIGH);
        }
        String ultra = mClipEntity.getUltra();
        if (!Utils.isEmptyText(ultra)) {
            mQualities.add(ClipEntity.Quality.QUALITY_ULTRA);
        }
        String blueray = mClipEntity.getBlueray();
        if (!Utils.isEmptyText(blueray)) {
            mQualities.add(ClipEntity.Quality.QUALITY_BLUERAY);
        }
        String _4k = mClipEntity.get_4k();
        if (!Utils.isEmptyText(_4k)) {
            mQualities.add(ClipEntity.Quality.QUALITY_4K);
        }
        if (!mQualities.isEmpty()) {
            if (initQuality != null) {
                mQuality = initQuality;
            } else {
                mQuality = mQualities.get(mQualities.size() - 1);
            }
            defaultQualityUrl = getSmartQualityUrl(mQuality);
            Log.i(TAG, "initDefaultQualityUrl:" + defaultQualityUrl);
            if (Utils.isEmptyText(defaultQualityUrl)) {
                Log.i(TAG, "Get init quality error, use default quality.");
                defaultQualityUrl = getSmartQualityUrl(mQualities.get(mQualities.size() - 1));
                mQuality = mQualities.get(mQualities.size() - 1);
            }
        }
        return defaultQualityUrl;
    }

    @Override
    public void logVideoExit(int exitPosition, String source) {
        if (mDaisyVideoView != null) {
            mDaisyVideoView.logVideoExit(exitPosition, source);
        }
    }
}
