package tv.ismar.iqiyiplayer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.qiyi.sdk.player.AdItem.AdType;
import com.qiyi.sdk.player.BitStream;
import com.qiyi.sdk.player.IAdController;
import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.IMediaPlayer;
import com.qiyi.sdk.player.IMediaPlayer.OnBitStreamChangeListener;
import com.qiyi.sdk.player.IMediaPlayer.OnBitStreamInfoListener;
import com.qiyi.sdk.player.IMediaPlayer.OnBufferChangedListener;
import com.qiyi.sdk.player.IMediaPlayer.OnHeaderTailerInfoListener;
import com.qiyi.sdk.player.IMediaPlayer.OnInfoListener;
import com.qiyi.sdk.player.IMediaPlayer.OnPreviewInfoListener;
import com.qiyi.sdk.player.IMediaPlayer.OnSeekCompleteListener;
import com.qiyi.sdk.player.IMediaPlayer.OnStateChangedListener;
import com.qiyi.sdk.player.IMediaPlayer.OnVideoSizeChangedListener;
import com.qiyi.sdk.player.IMediaProfile;
import com.qiyi.sdk.player.ISdkError;
import com.qiyi.sdk.player.IVideoOverlay;
import com.qiyi.sdk.player.OnFeedbackFinishedListener;
import com.qiyi.sdk.player.Parameter;
import com.qiyi.sdk.player.PlayerSdk;
import com.qiyi.sdk.player.PlayerSdk.OnInitializedListener;
import com.qiyi.sdk.player.SdkVideo;
import com.qiyi.sdk.player.VideoRatio;
import com.qiyi.sdk.player.constants.SdkConstants;
import com.qiyi.video.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SdkTestActivity extends Activity implements OnClickListener, OnCheckedChangeListener, OnItemSelectedListener {
    private static final String TAG = "SdkTestActivity";
    private static final int MSG_PLAY_TIME = 101;
    private static final int MSG_PLAY_NEXT_MOVIE = 102;
    private static final int MSG_RETRY_INIT = 103;
    // seek steps                            5s         10s         30s         1m          5m          10m
    private static final int[] SEEK_STEPS = {5000, 10000, 30000, 60000, 300000, 600000};
    private static final HashMap<BitStream, String> BITSTREAM_NAMES;
    private static final String TOAST_TEXT_PREVIEW_FINISHED = "Vip Video Preview Finished.";
    private static final boolean USE_SURFACEVIEW = true;
    private static boolean mIsUserAMonkey = ActivityManager.isUserAMonkey();

    static {
        BITSTREAM_NAMES = new HashMap<BitStream, String>();

        BITSTREAM_NAMES.put(BitStream.BITSTREAM_STANDARD, "流畅");  //增加流畅码流
        BITSTREAM_NAMES.put(BitStream.BITSTREAM_HIGH, "高清");

        BITSTREAM_NAMES.put(BitStream.BITSTREAM_720P, "720P");
        BITSTREAM_NAMES.put(BitStream.BITSTREAM_720P_DOLBY, "杜比720P");
        BITSTREAM_NAMES.put(BitStream.BITSTREAM_720P_H265, "H265_720P");

        BITSTREAM_NAMES.put(BitStream.BITSTREAM_1080P, "1080P");
        BITSTREAM_NAMES.put(BitStream.BITSTREAM_1080P_DOLBY, "杜比1080P");
        BITSTREAM_NAMES.put(BitStream.BITSTREAM_1080P_H265, "H265_1080P");

        BITSTREAM_NAMES.put(BitStream.BITSTREAM_4K, "4K");
        BITSTREAM_NAMES.put(BitStream.BITSTREAM_4K_DOLBY, "杜比4K");
        BITSTREAM_NAMES.put(BitStream.BITSTREAM_4K_H265, "H265_4K");
    }

    private int mSeekStepIndex;
    private Button mBtnRW;
    private Button mBtnPlay;
    private Button mBtnPause;
    private Button mBtnStop;
    private Button mBtnFF;
    private Button mBtnIncreaseStep;
    private Button mBtnDecreaseStep;
    private Button mBtnFullScreen;
    private Button mBtnUp;
    private Button mBtnDown;
    private Button mBtnLeft;
    private Button mBtnRight;
    private TextView mTxtSeekStep;
    private TextView mTxtPlayTime;
    private TextView mTxtBuffering;
    private ProgressBar mProgressBar;
    private TextView mTxtCurrentBitStream;
    private TextView mTxtMediaInfo;
    private RadioGroup mRgAspectRatio;
    private Spinner mPlayBitStreamSpinner;
    private Spinner mVipBitStreamSpinner;
    private ArrayAdapter<String> mPlaySpinnerAdapter;
    private ArrayAdapter<String> mVipSpinnerAdapter;
    private ViewGroup mFullScreenParent;
    private ViewGroup mWindowedParent;
    private LayoutParams mFlParamsWindowed;
    private IMediaPlayer mPlayer;
    private IVideoOverlay mVideoOverlay;
    private List<BitStream> mBitStreamList = new ArrayList<BitStream>();
    private boolean mIsFullScreen;
    private boolean mQiyiSdkInitialized;
    private boolean mIsPreview;
    private PlayListManager mPlaylistManager;
    private MyVideoSurfaceView mSurfaceView;
    private Surface mSurface;
    private IMedia mCurrentVideo;
    //    private IAdController mAdController;
    private OnFeedbackFinishedListener mFeedbackFinishedListener = new OnFeedbackFinishedListener() {
        @Override
        public void onFailed(ISdkError error) {
            String text = "mFeedbackFinishedListener.onFailed(" + error + ")";
            Log.d(TAG, text);
            showToast(text);
        }

        @Override
        public void onSuccess(String feedbackId, String ip) {
            //报障成功时, 会返回故障id和ip地址, 把这两个信息反馈给爱奇艺, 可以从后台拿到报障信息进行分析
            String text = "mFeedbackFinishedListener.onSuccess(feedbackId=" + feedbackId + ", ip=" + ip + ")";
            Log.d(TAG, text);
            showToast(text);
        }

    };
    private OnBufferChangedListener mBufferChangedListener = new OnBufferChangedListener() {

        @Override
        public void onBufferStart(IMediaPlayer player) {
            Log.d(TAG, "onBufferStart");
            mTxtBuffering.setVisibility(View.VISIBLE);
        }

        @Override
        public void onBufferEnd(IMediaPlayer player) {
            Log.d(TAG, "onBufferEnd");
            mTxtBuffering.setVisibility(View.GONE);
        }
    };
    private OnSeekCompleteListener mSeekCompleteListener = new OnSeekCompleteListener() {
        @Override
        public void onSeekCompleted(IMediaPlayer player) {
            Log.d(TAG, "onSeekComplete");
        }
    };
    private OnVideoSizeChangedListener mVideoSizeChangedListener = new OnVideoSizeChangedListener() {

        @Override
        public void onVideoSizeChanged(IMediaPlayer player, int width, int height) {
            Log.d(TAG, "onVideoSizeChanged(" + width + ", " + height + ")");
        }
    };
    private OnHeaderTailerInfoListener mHeaderTailerInfoListener = new OnHeaderTailerInfoListener() {
        @Override
        public void onHeaderTailerInfoReady(IMediaPlayer player, int headerTime, int tailerTime) {
            //TODO, 片头片尾时间，单位改为毫秒
            Log.d(TAG, "onHeaderTailerInfoReady(" + headerTime + "/" + tailerTime + ")");
        }
    };
    //    客户端广告交互实现参考代码
//    extraParams.addAdsHint(Parameter.HINT_TYPE_SKIP_AD, "下");
//    extraParams.addAdsHint(Parameter.HINT_TYPE_HIDE_PAUSE_AD, "下");
//    extraParams.addAdsHint(Parameter.HINT_TYPE_SHOW_CLICK_THROUGH_AD, "右");
//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        int code = event.getKeyCode();
//        int action = event.getAction();
//        if (action != KeyEvent.ACTION_UP || mPlayer == null || mPlayer.getAdController() == null || !mIsInFullScreenMode) {
//            return super.dispatchKeyEvent(event);
//        }
//        IAdController adController = mPlayer.getAdController();
//
//        switch (code) {
//        case KeyEvent.KEYCODE_DPAD_DOWN:
//            if (mCurrentPlayState == STATE_AD_PLAYING) {
//                adController.skipAd();//若广告为悦享看广告，此方法会响应并跳过广告
//            } else if (isPauseAdShown()) {
//                adController.hideAd(AdType.PAUSE);//隐藏暂停广告
//            }
//            break;
//        case KeyEvent.KEYCODE_BACK:
//            if(isClickThroughAdShown()) {
//                adController.hideAd(AdType.CLICKTHROUGH);//隐藏H5/图片跳转广告，恢复前贴/中插广告播放
//            }
//            break;
//        case KeyEvent.KEYCODE_DPAD_RIGHT:
//            if(adController.isEnableClickThroughAd()) {
//                adController.showAd(AdType.CLICKTHROUGH);//跳转至H5/图片广告展示
//            }
//            break;
//        default:
//            return super.dispatchKeyEvent(event);
//        }
//        return true;
//    }
    private OnPreviewInfoListener mPreviewInfoListener = new OnPreviewInfoListener() {

        @Override
        public void onPreviewInfoReady(IMediaPlayer player, final boolean isPreview, final int previewEndTimeInSecond) {
            Log.d(TAG, "onPreviewInfoReady: isPreview=" + isPreview + ", previewEndTimeInSecond=" + previewEndTimeInSecond);
            mIsPreview = isPreview;
            String text = "isPreview=" + isPreview + ", previewEndTimeInSecond=" + previewEndTimeInSecond;
            showToast(text);
        }
    };
    private OnStateChangedListener mStateChangedListener = new OnStateChangedListener() {
        @Override
        public boolean onError(IMediaPlayer player, ISdkError error) {
            String text = "onError: error=" + error;
            String text2 = "\r\nerrorcode:" + ((error != null) ? error.getCode() : null);
            Log.e(TAG, "error type: " + error.getType() + " detail type: " + error.getDetailType());
            Log.e(TAG, text + text2);
            showToast(text + text2);

            //发送报障, 异步处理, 可以在onError时, 调用此方法, 客户端可以在UI上以合适形式进行引导
//            PlayerSdk.getInstance().sendFeedback(mCurrentVideo, error, mFeedbackFinishedListener);
            return false;
        }

        @Override
        public void onAdStart(IMediaPlayer player) {
            Log.d(TAG, "onAdStart");
        }

        @Override
        public void onAdEnd(IMediaPlayer player) {
            Log.d(TAG, "onAdEnd");
        }

        @Override
        public void onStarted(IMediaPlayer player) {
            Log.d(TAG, "onStarted: current position=" + player.getCurrentPosition() + ", duration=" + player.getDuration());
            mHandler.removeMessages(MSG_PLAY_TIME);
            mHandler.sendEmptyMessage(MSG_PLAY_TIME);
            mTxtPlayTime.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCompleted(IMediaPlayer player) {
            Log.d(TAG, "onCompleted");
            //TODO, onPreviewComplete回调接口去掉, 保持状态不重复; 当onComplete回调时，判断如果是试看, 即表示试看结束
            if (mIsPreview) {
                showToast(TOAST_TEXT_PREVIEW_FINISHED);
            }
            mHandler.removeMessages(MSG_PLAY_TIME);
            mTxtPlayTime.setVisibility(View.GONE);
            playNextMovieInPlaylist();
        }

        @Override
        public void onPaused(IMediaPlayer player) {
            Log.d(TAG, "onPaused");
        }

        @Override
        public void onStopped(IMediaPlayer player) {
            Log.d(TAG, "onStopped");
            playNextMovieInPlaylist();
        }

        @Override
        public void onPrepared(IMediaPlayer player) {
            Log.d(TAG, "onPrepared");
            //TODO, onPrepared时调用start
            if (player != null) {
                player.start();
            }
        }

        @Override
        public void onMiddleAdEnd(IMediaPlayer arg0) {
            Log.d(TAG, "onMiddleAdEnd");
        }

        @Override
        public void onMiddleAdStart(IMediaPlayer arg0) {
            Log.d(TAG, "onMiddleAdStart");
        }
    };
    private OnBitStreamInfoListener mBitStreamInfoListener = new OnBitStreamInfoListener() {

        @Override
        public void onPlayableBitStreamListUpdate(IMediaPlayer player, List<BitStream> bitstreamList) {
            Log.d(TAG, "onPlayableBitStreamListUpdate(" + bitstreamList + ")");
            mBitStreamList = bitstreamList;
            updatePlayBitStreamListUI(bitstreamList);
        }

        @Override
        public void onBitStreamSelected(IMediaPlayer player, final BitStream bitstream) {
            Log.d(TAG, "onBitStreamSelected(" + bitstream + ")");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTxtCurrentBitStream.setText(BITSTREAM_NAMES.get(bitstream));
                }
            });
        }

        @Override
        public void onVipBitStreamListUpdate(IMediaPlayer player, List<BitStream> bitstreamList) {
            Log.d(TAG, "onVipBitStreamListUpdate(" + bitstreamList + ")");
            updateVipBitStreamListUI(bitstreamList);
        }
    };
    private OnInfoListener mOnInfoListener = new OnInfoListener() {
        @Override
        public void onInfo(IMediaPlayer player, int what, Object extra) {
            Log.d(TAG, "mOnInfoListener.onInfo(player:" + player + ",what:" + what + ",extra:" + extra + ")");
            switch (what) {
                case IMediaPlayer.MEDIA_INFO_DEGRADE_BITSTREAM_DELAY:
                    long delayMillis = (Long) extra;
                    final String text = "would degrade bitstream for carton in " + delayMillis + "ms.";
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SdkTestActivity.this, text, Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
                case IMediaPlayer.MEDIA_INFO_MIDDLE_AD_COMING:
                    final String textAdInfo = "即将播放广告";
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SdkTestActivity.this, textAdInfo, Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
                case IMediaPlayer.MEDIA_INFO_MIDDLE_AD_SKIPPED:
                    Log.d(TAG, "middle ad playback skipped");
                    break;
                default:
                    break;
            }
        }
    };
    private OnBitStreamChangeListener mOnBitStreamChangeListener = new OnBitStreamChangeListener() {

        @Override
        public void OnBitStreamChanging(BitStream from, BitStream to) {

        }

        @Override
        public void OnBitStreamChanged(final BitStream to) {
            Log.d(TAG, "onBitStreamSelected(" + to + ")");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTxtCurrentBitStream.setText(BITSTREAM_NAMES.get(to));
                }
            });
        }
    };
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PLAY_TIME:
                    String playTime;
                    int curPos = mPlayer.getCurrentPosition();
                    int duration = mPlayer.getDuration();
                    Log.d(TAG, "MSG_PLAY_TIME: curPos=" + curPos + ", duration=" + duration);
                    playTime = getPlaybackTimeString(curPos);
                    playTime += " / ";
                    playTime += getPlaybackTimeString(duration);
                    Log.d(TAG, "MSG_PLAY_TIME: time string=" + playTime);
                    mTxtPlayTime.setText(playTime);
                    final int max = mProgressBar.getMax();
                    int progress = Math.round(((float) curPos / duration) * max);
                    int secondaryProgress = mPlayer.getCachePercent() * max / 100;
                    mProgressBar.setProgress(progress);
                    mProgressBar.setSecondaryProgress(secondaryProgress);
                    sendEmptyMessageDelayed(MSG_PLAY_TIME, 1000);
                    Log.d(TAG, "MSG_PLAY_TIME: isPlaying=" + mPlayer.isPlaying());
                    break;
                case MSG_PLAY_NEXT_MOVIE:
                    mPlaylistManager.moveToNext();
                    startPlayMovie(mPlaylistManager.getCurrent());
                    break;

                case MSG_RETRY_INIT:
                    initQiyiPlayerSdk();
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_sdk_test);
        initViews();

        mPlaylistManager = new PlayListManager();
        mPlaylistManager.initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        initQiyiPlayerSdk();
        getPackageName();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (isFinishing()) {
            releasePlayer();
        }
    }

    private static String getTimeString(int timeInMs) {
        int second = timeInMs / 1000;
        if (second < 60) {
            return String.valueOf(second) + "s";
        } else {
            int min = second / 60;
            int sec = second % 60;
            return String.valueOf(min) + "m" + (sec > 0 ? String.valueOf(sec) : "");
        }
    }

    private static String getPlaybackTimeString(int timeInMs) {
        int second = timeInMs / 1000;
        int minute = second / 60;
        if (minute > 0) {
            second %= 60;
        }
        int hour = minute / 60;
        if (hour > 0) {
            minute %= 60;
        }

        String hourStr = String.format("%02d", hour);
        String minStr = String.format("%02d", minute);
        String secStr = String.format("%02d", second);
        String ret = hourStr + ":" + minStr + ":" + secStr;
        Log.d(TAG, "getPlaybackTimeString(" + timeInMs + "): hour="
                + hour + ", minute=" + minute + ", second=" + second + ", result=" + ret);
        return ret;
    }

    private void initViews() {
        mBtnRW = (Button) findViewById(R.id.btn_rw);
        mBtnRW.setOnClickListener(this);
        mBtnPlay = (Button) findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(this);
        mBtnPause = (Button) findViewById(R.id.btn_pause);
        mBtnPause.setOnClickListener(this);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mBtnStop.setOnClickListener(this);
        mBtnFF = (Button) findViewById(R.id.btn_ff);
        mBtnFF.setOnClickListener(this);
        mBtnUp = (Button) findViewById(R.id.btn_up);
        mBtnUp.setOnClickListener(this);
        mBtnDown = (Button) findViewById(R.id.btn_down);
        mBtnDown.setOnClickListener(this);
        mBtnLeft = (Button) findViewById(R.id.btn_left);
        mBtnLeft.setOnClickListener(this);
        mBtnRight = (Button) findViewById(R.id.btn_right);
        mBtnRight.setOnClickListener(this);

        mBtnIncreaseStep = (Button) findViewById(R.id.btn_increase_step);
        mBtnIncreaseStep.setOnClickListener(this);
        mBtnDecreaseStep = (Button) findViewById(R.id.btn_decrease_step);
        mBtnDecreaseStep.setOnClickListener(this);
        mBtnFullScreen = (Button) findViewById(R.id.btn_fullscreen);
        mBtnFullScreen.setOnClickListener(this);

        mTxtSeekStep = (TextView) findViewById(R.id.txt_seek_step);
        mTxtSeekStep.setText(getTimeString(SEEK_STEPS[mSeekStepIndex]));
        mTxtPlayTime = (TextView) findViewById(R.id.txt_playtime);

        mTxtBuffering = (TextView) findViewById(R.id.txt_buffering);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        mRgAspectRatio = (RadioGroup) findViewById(R.id.rg_aspect_ratio);
        mRgAspectRatio.setOnCheckedChangeListener(this);

        mPlayBitStreamSpinner = (Spinner) findViewById(R.id.spinner_bitstream);
        mPlayBitStreamSpinner.setOnItemSelectedListener(this);
        mPlayBitStreamSpinner.setPrompt("请选择码流");
        mPlaySpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        mPlaySpinnerAdapter.add("请选择码流");
        mPlayBitStreamSpinner.setAdapter(mPlaySpinnerAdapter);

        mVipBitStreamSpinner = (Spinner) findViewById(R.id.spinner_vipbitstream);
        mVipBitStreamSpinner.setPrompt("付费码流列表");
        mVipSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        mVipSpinnerAdapter.add("付费码流列表");
        mVipBitStreamSpinner.setAdapter(mVipSpinnerAdapter);
        mTxtCurrentBitStream = (TextView) findViewById(R.id.txt_current_bitstream);

        // DEBUG CODE, performing test retry
        Button retryBtn = (Button) findViewById(R.id.btn_next);
        retryBtn.setOnClickListener(this);

        mFullScreenParent = (ViewGroup) findViewById(R.id.fl_fullscreen_container);
        mWindowedParent = (ViewGroup) findViewById(R.id.fl_windowed_container);
        mFlParamsWindowed = mWindowedParent.getLayoutParams();
        if (!USE_SURFACEVIEW) {
            mWindowedParent.addView(new VideoSurfaceTexture(getApplicationContext()), mFlParamsWindowed);
        }

        mTxtMediaInfo = (TextView) findViewById(R.id.txt_media_info);
    }

    private boolean isPauseAdShown() {
        IAdController adController = mPlayer != null ? mPlayer.getAdController() : null;
        if (adController != null) {
            List<AdType> adTypes = adController.getShownAdType();
            if (adTypes != null && adTypes.contains(AdType.PAUSE)) {
                return true;
            }
        }
        return false;
    }

    private boolean isClickThroughAdShown() {
        IAdController adController = mPlayer != null ? mPlayer.getAdController() : null;
        if (adController != null) {
            List<AdType> adTypes = adController.getShownAdType();
            if (adTypes != null && adTypes.contains(AdType.CLICKTHROUGH)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        IAdController adController = mPlayer != null ? mPlayer.getAdController() : null;
        switch (v.getId()) {
            case R.id.btn_rw:
                if (mPlayer != null) {
                    //不再提供seek方法, 只提供seekTo, 原seek方法实现方式
                    int currentPosition = mPlayer.getCurrentPosition();
                    mPlayer.seekTo(currentPosition - SEEK_STEPS[mSeekStepIndex]);
                }
                break;
            case R.id.btn_ff:
                if (mPlayer != null) {
                    //不再提供seek方法, 只提供seekTo, 原seek方法实现方式
                    int currentPosition = mPlayer.getCurrentPosition();
                    mPlayer.seekTo(currentPosition + SEEK_STEPS[mSeekStepIndex]);
                }
                break;
            case R.id.btn_play:
                if (mPlayer != null && !mPlayer.isPlaying()) {
                    mPlayer.start();
                }
                break;
            case R.id.btn_pause:
                if (mPlayer != null) {
                    mPlayer.pause();
                }
                break;
            case R.id.btn_stop:
                if (mPlayer != null) {
                    mPlayer.stop();
                }
                break;
            case R.id.btn_increase_step:
                mSeekStepIndex = Math.min(SEEK_STEPS.length - 1, mSeekStepIndex + 1);
                mTxtSeekStep.setText(getTimeString(SEEK_STEPS[mSeekStepIndex]));
                break;
            case R.id.btn_decrease_step:
                mSeekStepIndex = Math.max(0, mSeekStepIndex - 1);
                mTxtSeekStep.setText(getTimeString(SEEK_STEPS[mSeekStepIndex]));
                break;
            case R.id.btn_next:
                playNextMovieInPlaylist();
                break;
            case R.id.btn_fullscreen:
                changeToFullScreen();
                mIsFullScreen = true;
                break;
            case R.id.btn_up:
                break;
            case R.id.btn_down:
                //隐藏暂停广告
                if (adController != null && isPauseAdShown()) {
                    adController.hideAd(AdType.PAUSE);
                }

                //跳过悦享看广告
                if (adController != null) {
                    adController.skipAd();
                }
                break;
            case R.id.btn_left:
                //隐藏跳转广告, 返回视频播放
                if (adController != null && isClickThroughAdShown()) {
                    adController.hideAd(AdType.CLICKTHROUGH);
                }
                break;
            case R.id.btn_right:
                //跳转广告
                if (adController != null && adController.isEnableClickThroughAd()) {
                    adController.showAd(AdType.CLICKTHROUGH);
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_original:
                if (mPlayer != null) {
                    //原始比例
                    mPlayer.setVideoRatio(VideoRatio.ORIGINAL);
                }
                break;
            case R.id.rb_stretched:
                if (mPlayer != null) {
                    //充满全屏
                    mPlayer.setVideoRatio(VideoRatio.STRETCH_TO_FIT);
                }
                break;
            case R.id.rb_custom:
                if (mPlayer != null) {
                    //固定比例4:3
                    mPlayer.setVideoRatio(VideoRatio.FIXED_4_3);
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "Spinner(bitstream).onItemSelected: position=" + position);
        if (position == 0) {
            // 0 is the place holder for "请选择码流"
        } else {
            BitStream bs = mBitStreamList.get(position - 1);
            if (bs != null) {
                mPlayer.switchBitStream(bs);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

//    private OnDegradeBitstreamListener mOnDegradeBitstreamListener = new OnDegradeBitstreamListener() {
//        
//        @Override
//        public void wouldDegradeBitstreamAfterMillis(IMediaPlayer player, final BitStream bs, long delayMs) {
//            Log.d(TAG, "wouldDegradeBitstream(" + bs + ")");
////            if (player != null) {
////                player.cancelDegradeBitstream();
////            }
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(SdkTestActivity.this, "would degrade bitstream to " + bs + " for carton.", Toast.LENGTH_LONG).show();
//                }
//            });
//        }
//        
//        @Override
//        public void onBitstreamDegraded(IMediaPlayer player, final BitStream bs) {
//            Log.d(TAG, "onBitstreamDegraded(" + bs + ")");
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    mTxtCurrentBitStream.setText(BITSTREAM_NAMES.get(bs));
//                }
//            });
//        }
//    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        int action = event.getAction();
        if (action != KeyEvent.ACTION_UP) {
            return super.dispatchKeyEvent(event);
        }

        switch (code) {
            case KeyEvent.KEYCODE_BACK:
                if (mIsFullScreen) {
                    changeToWindowed();
                    mIsFullScreen = false;
                    return true;
                } else {
                    Log.d(TAG, "isUserAMonkey=" + mIsUserAMonkey);
                    if (mIsUserAMonkey) {
                        return true;
                    }
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                Log.d(TAG, "isUserAMonkey=" + mIsUserAMonkey);
                if (mIsUserAMonkey) {
                    return true;
                } else {
                    SdkTestActivity.this.finish();
                    Intent it = new Intent();
                    it.setClass(SdkTestActivity.this, BlankActivity.class);
                    startActivity(it);
                }
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * initQiyiPlayerSdk
     * SDK初始化
     * 注：
     * 1. 初始化在程序启动时调用, 只需初始调用一次;
     * 2. 初始化为异步操作, 要确保在OnInitializedListener.onSuccess返回时才能继续使用SDK
     */
    private void initQiyiPlayerSdk() {
        if (mQiyiSdkInitialized) {//初始化应只调用一次
            return;
        }
        Parameter extraParams = new Parameter();
        //debug code
        extraParams.setInitPlayerSdkAfter(0);  //SDK初始化在调用initialize之后delay一定时间开始执行, 单位为毫秒.
        extraParams.setCustomerAppVersion("1");      //传入客户App版本号
        extraParams.setDeviceId("sh_dvhxt5rk");   //传入deviceId, VIP项目必传, 登录和鉴权使用
        extraParams.setDeviceInfo("yt-x703l");
        extraParams.setShowAdCountDown(true);

        extraParams.addAdsHint(Parameter.HINT_TYPE_SKIP_AD, "下");
        extraParams.addAdsHint(Parameter.HINT_TYPE_HIDE_PAUSE_AD, "下");
        extraParams.addAdsHint(Parameter.HINT_TYPE_SHOW_CLICK_THROUGH_AD, "右");

        PlayerSdk.getInstance().initialize(this, extraParams, new OnInitializedListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(SdkTestActivity.this, "QiyiSdk init success!", Toast.LENGTH_SHORT).show();
                mQiyiSdkInitialized = true;
                doOnSuccess();    //初始化成功后做
            }

            @Override
            public void onFailed(int what, int extra) {
                Message msg = mHandler.obtainMessage(MSG_RETRY_INIT);
                int retryDelayMs = -1;//重试间隔规则参见SDK文档
                if (what == SdkConstants.ERROR_INIT_DEV_CHECK_FAIL) {
                    retryDelayMs = 30000;
                } else if (what == SdkConstants.ERROR_INIT_UNKNOWN || what == SdkConstants.ERROR_INIT_PLUGIN_LOAD_FAIL) {
                    retryDelayMs = 10000;
                }
                if (retryDelayMs >= 0) {
                    mHandler.sendMessageDelayed(msg, retryDelayMs);
                }
                Toast.makeText(SdkTestActivity.this, "QiyiSdk init fail: what=" + what + ", retry in  =" + retryDelayMs + "ms", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void doOnSuccess() {
        if (!mQiyiSdkInitialized) {
            return;
        }
        LogUtils.setDebug(true);
        //login, 同步操作, 有网络接口调用, 可能耗时, 请注意. 初始只需调用一次, 登录成功后一直有效, 如需登出, 请调用logout
        Log.d(TAG,"login:" + PlayerSdk.getInstance().login("ec625b4f863d5217f427242538a8b1211f4b4c6beb2d52887355fbb862f47cfa"));
        startPlayMovie(mPlaylistManager.getCurrent());
        Map<String, Object> extra = new HashMap<String, Object>();
        startPlayMovie(new SdkVideo("244034600", "244034600", true, IMedia.DRM_TYPE_NONE, 0, null));

        //轮播节目播放
//        SdkVideo liveVideo = new SdkVideo("380079222", BitStream.BITSTREAM_HIGH, false);
//        startPlayMovie(liveVideo);

    }

    /**
     * releasePlayer
     * 释放播放器
     */
    private void releasePlayer() {
        mHandler.removeCallbacksAndMessages(null);
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    //debug-code, 切全屏的方法
    private void changeToFullScreen() {
        if (!mIsFullScreen) {
            mSurfaceView.setIgnoreWindowChange(true);
            mWindowedParent.removeAllViews();
            mWindowedParent.setVisibility(View.GONE);
            mVideoOverlay.changeParent(mFullScreenParent);
            mFullScreenParent.setVisibility(View.VISIBLE);
            mSurfaceView.setIgnoreWindowChange(false);
            return;
        }
    }

    //debug-code, 切小窗口方法
    private void changeToWindowed() {
        if (mIsFullScreen) {
            mSurfaceView.setIgnoreWindowChange(true);
            mFullScreenParent.removeAllViews();
            mFullScreenParent.setVisibility(View.GONE);
            mVideoOverlay.changeParent(mWindowedParent);
            mWindowedParent.setVisibility(View.VISIBLE);
            mSurfaceView.setIgnoreWindowChange(false);
            return;
        }
    }

    private void startPlayMovie(IMedia media) {
        if (!mQiyiSdkInitialized) {//必须SDK初始化成功后调用
            return;
        }

        IMediaProfile profile = PlayerSdk.getInstance().getMediaProfile();
        Log.d(TAG, "IMediaProfile, defaultStream=" + profile.getBitstream());
        Log.d(TAG, "IMediaProfile, isSupportDolby=" + profile.isSupportDolby());
        Log.d(TAG, "IMediaProfile, isSupportH265=" + profile.isSupportH265());

        if (!USE_SURFACEVIEW && mSurface == null) {
            return;
        }

        releasePlayer();

        //创建IVideoOverlay对象, 不支持实现IVideoOverlay接口，必须调用PlaySdk.getInstance().createVideoOverlay创建
        //创建IVideoOverlay对象, 不需创建SurfaceView, 直接传入父容器即可
        if (USE_SURFACEVIEW) {
//            mVideoOverlay = PlayerSdk.getInstance().createVideoOverlay(mWindowedParent);

            //创建IVideoOverlay对象, 如需修改SurfaceView, 请继承VideoSurfaceView
            mSurfaceView = new MyVideoSurfaceView(getApplicationContext());
            mVideoOverlay = PlayerSdk.getInstance().createVideoOverlay(mWindowedParent, mSurfaceView);
        }

        //IMediaPlayer对象通过QiyiPlayerSdk.getInstance().createVideoPlayer()创建
        mPlayer = PlayerSdk.getInstance().createMediaPlayer();

        //setVideo方法, 更名为setData, 必须调用, 需传入IMedia对象, 起播时间点修改为从IMedia对象获取, 不从setData传参
        mPlayer.setData(media);
        mCurrentVideo = media;

        //设置IVideoOverlay对象, 必须调用
        if (USE_SURFACEVIEW) {
            mPlayer.setDisplay(mVideoOverlay);
        } else {
            mPlayer.setSurface(mSurface);
        }

        //设置播放状态回调监听器, 需要时设置
        mPlayer.setOnStateChangedListener(mStateChangedListener);

        //设置码流信息回调监听器, 需要时设置
        mPlayer.setOnBitStreamInfoListener(mBitStreamInfoListener);

        //设置VIP试看信息回调监听器, 需要时设置
        mPlayer.setOnPreviewInfoListener(mPreviewInfoListener);

        //设置视频分辨率回调监听器, 需要时设置
        mPlayer.setOnVideoSizeChangedListener(mVideoSizeChangedListener);

        //设置seek完成监听器, 需要时设置
        mPlayer.setOnSeekCompleteListener(mSeekCompleteListener);

        //设置片头片尾信息监听器, 需要时设置
        mPlayer.setOnHeaderTailerInfoListener(mHeaderTailerInfoListener);

        //设置缓冲事件监听器, 需要时设置
        mPlayer.setOnBufferChangedListener(mBufferChangedListener);

        mPlayer.setOnInfoListener(mOnInfoListener);

        //设置监听码流切换监听器，需要时设置
        mPlayer.setOnBitStreamChangeListener(mOnBitStreamChangeListener);
        //调用prepareAsync, 播放器开始准备, 必须调用
        mPlayer.prepareAsync();

        //debug code.
        showPlaybackInfo(media);
    }

    //debug code
    private void playNextMovieInPlaylist() {
        mHandler.removeMessages(MSG_PLAY_NEXT_MOVIE);
        mHandler.sendEmptyMessage(MSG_PLAY_NEXT_MOVIE);
    }

    //debug code
    private void showPlaybackInfo(IMedia media) {
        final StringBuilder content = new StringBuilder();
        String albumId = media != null ? media.getAlbumId() : "";
        String tvId = media != null ? media.getTvId() : "";
        String channelId = media != null ? media.getCarouselChannelId() : "";
        boolean isVip = media != null && media.isVip();
        int liveType = media != null ? media.getLiveType() : -1;

        String version = PlayerSdk.getInstance().getVersion() + "_" + PlayerSdk.getInstance().getPlayercoreVersion();
        content.append("QiyiSdk Version: " + version);
        if (channelId != null && !channelId.isEmpty()) {
            content.append("\r\n节目类型: 轮播节目");
            content.append("\r\nchannelId:");
            content.append(channelId);
            content.append("\r\nliveType:");
            content.append(liveType);
        } else if (tvId != null && !tvId.isEmpty()) {
            content.append("\r\n节目类型: 点播节目");
            content.append("\r\nalbumId: ");
            content.append(albumId);
            content.append("\r\ntvId: ");
            content.append(tvId);
        }
        content.append("\r\n是否VIP: ");
        content.append(isVip);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTxtMediaInfo.setText(content.toString());
            }
        });
    }

    //debug code
    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SdkTestActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    //debug code
    private void updatePlayBitStreamListUI(final List<BitStream> bitstreamList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlaySpinnerAdapter.clear();
                mPlaySpinnerAdapter.add("请选择码流");
                for (BitStream definition : bitstreamList) {
                    String name = BITSTREAM_NAMES.get(definition);
                    if (name != null) {
                        mPlaySpinnerAdapter.add(name);
                    }
                }
            }
        });
    }

    private void updateVipBitStreamListUI(final List<BitStream> bitstreamList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVipSpinnerAdapter.clear();
                mVipSpinnerAdapter.add("付费码流列表");
                for (BitStream definition : bitstreamList) {
                    String name = BITSTREAM_NAMES.get(definition);
                    if (name != null) {
                        mVipSpinnerAdapter.add(name);
                    }
                }
            }
        });
    }

    class VideoSurfaceTexture extends GLSurfaceView {
        private static final String TAG = "VideoSurfaceTexture";

        private VideoRender mRenderer;

        public VideoSurfaceTexture(Context context) {
            super(context);
            setEGLContextClientVersion(2);
            mRenderer = new VideoRender(context);
            setRenderer(mRenderer);
        }

        @Override
        public void onPause() {
            super.onPause();
            mRenderer.pause();
        }

        private class VideoRender implements GLSurfaceView.Renderer {
            private TexturePainter mTexturePainter;

            public VideoRender(Context context) {
                mTexturePainter = new TexturePainter();
            }

            @Override
            public void onDrawFrame(GL10 glUnused) {
                mTexturePainter.drawTexture();
            }

            @Override
            public void onSurfaceChanged(GL10 glUnused, int width, int height) {
                Log.d(TAG, "onSurfaceChanged(" + width + ", " + height + ")");
            }

            @Override
            public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
                Log.d(TAG, "onSurfaceCreated()");
                //If in GLSurfaceView, this should be called in onSurfaceCreated().
                //Otherwise, the video frames will not be rendered.
                mSurface = mTexturePainter.prepareTexture();
                post(new Runnable() {
                    @Override
                    public void run() {
                        doOnSuccess();
                    }
                });
            }

            public void pause() {
                mTexturePainter.releaseTexture();
            }
        }
    }
}
