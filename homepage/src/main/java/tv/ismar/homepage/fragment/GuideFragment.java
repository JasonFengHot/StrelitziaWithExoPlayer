package tv.ismar.homepage.fragment;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.utils.StringUtils;
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

//import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.ismartv.downloader.DownloadEntity;
import cn.ismartv.downloader.DownloadStatus;
import cn.ismartv.downloader.Md5;
import cn.ismartv.injectdb.library.query.Select;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.AppConstant;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.cache.CacheManager;
import tv.ismar.app.core.cache.DownloadClient;
import tv.ismar.app.entity.HomePagerEntity;
import tv.ismar.app.entity.HomePagerEntity.Carousel;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.homepage.R;
import tv.ismar.homepage.view.HomePageActivity;
import tv.ismar.homepage.widget.DaisyVideoView;
import tv.ismar.homepage.widget.DaisyViewContainer;
import tv.ismar.homepage.widget.HomeItemContainer;
import tv.ismar.homepage.widget.LabelImageView3;

/**
 * Created by huaijie on 5/18/15.
 */
public class GuideFragment extends ChannelBaseFragment {
    private String TAG = "GuideFragment";

    private static final int START_PLAYBACK = 0x0000;
    private static final int CAROUSEL_NEXT = 0x0010;

    private DaisyViewContainer guideRecommmendList;


    private ArrayList<String> allVideoUrl;
    private ArrayList<LabelImageView3> allItem;
    private HomeItemContainer film_post_layout;
    private ArrayList<Carousel> mCarousels;
    private LabelImageView3 toppage_carous_imageView1;
    private LabelImageView3 toppage_carous_imageView2;
    private LabelImageView3 toppage_carous_imageView3;
    private HomeItemContainer lastpostview;
    private BitmapDecoder bitmapDecoder;
    private DaisyVideoView mSurfaceView;


    private int mCurrentCarouselIndex = -1;
    private CarouselRepeatType mCarouselRepeatType = CarouselRepeatType.All;

    private ImageView linkedVideoLoadingImage;
    private HashMap<Integer, Integer> carouselMap;
    private Subscription homePageSub;
    private boolean isDestroyed = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View mView = LayoutInflater.from(mContext).inflate(R.layout.fragment_guide, null);
        guideRecommmendList = (DaisyViewContainer) mView.findViewById(R.id.recommend_list);
        toppage_carous_imageView1 = (LabelImageView3) mView.findViewById(R.id.toppage_carous_imageView1);
        toppage_carous_imageView2 = (LabelImageView3) mView.findViewById(R.id.toppage_carous_imageView2);
        toppage_carous_imageView3 = (LabelImageView3) mView.findViewById(R.id.toppage_carous_imageView3);
        film_post_layout = (HomeItemContainer) mView.findViewById(R.id.guide_center_layoutview);
        linkedVideoLoadingImage = (ImageView) mView.findViewById(R.id.linked_video_loading_image);

        mSurfaceView = (DaisyVideoView) mView.findViewById(R.id.linked_video);
        mSurfaceView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1)
                    film_post_layout.requestFocus();
            }
        });
        mSurfaceView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                film_post_layout.performClick();
            }
        });
        film_post_layout.setTag(R.id.view_position_tag, 1);
        film_post_layout.setOnClickListener(ItemClickListener);

        mLeftTopView = mSurfaceView;
        mRightTopView = toppage_carous_imageView1;

        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(mContext, R.drawable.guide_video_loading, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                linkedVideoLoadingImage.setBackgroundDrawable(bitmapDrawable);
            }
        });

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        isDestroyed = true;
        mHandler.removeCallbacksAndMessages(null);
        guideRecommmendList.removeAllViews();
        guideRecommmendList = null;
        mSurfaceView.setOnFocusChangeListener(null);
        mSurfaceView = null;
        itemFocusChangeListener = null;
        toppage_carous_imageView1 = null;
        toppage_carous_imageView2 = null;
        toppage_carous_imageView3 = null;
        lastpostview = null;
        if (linkedVideoLoadingImage != null && linkedVideoLoadingImage.getDrawingCache() != null && !linkedVideoLoadingImage.getDrawingCache().isRecycled()) {
            linkedVideoLoadingImage.getDrawingCache().recycle();
        }
        super.onDestroyView();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        AppConstant.purchase_channel = "homepage";
        AppConstant.purchase_page = "homepage";
        if (mCarousels == null) {
            fetchHomePage();
        } else {
            if (!mSurfaceView.isPlaying()) {
                playCarousel(500);
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (homePageSub != null && homePageSub.isUnsubscribed()) {
            homePageSub.unsubscribe();
        }
    }

    @Override
    public void onStop() {
        stopPlayback();
        super.onStop();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (bitmapDecoder != null && bitmapDecoder.isAlive()) {
            bitmapDecoder.interrupt();
        }
    }


    public void fetchHomePage() {
        homePageSub = SkyService.ServiceManager.getCacheSkyService().TvHomepageTop().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((HomePageActivity) getActivity()).new BaseObserver<HomePagerEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(HomePagerEntity homePagerEntity) {
                        if(homePagerEntity == null){
                            super.onError(new Exception("数据异常"));
                        } else {
                            if(!isDestroyed)
                            fillLayout(homePagerEntity);
                        }
                    }
                });

    }

    private void fillLayout(HomePagerEntity homePagerEntity) {
        if (mContext == null || guideRecommmendList == null)
            return;
        if (homePagerEntity == null) {
            new CallaPlay().exception_except("launcher", "launcher", "homepage",
                    "", 0, "api/tv/homepage/top/",
                    SimpleRestClient.appVersion, "data", ""
            );
            return;
        }
        ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity.getCarousels();
        ArrayList<HomePagerEntity.Poster> posters = homePagerEntity.getPosters();

        if (!carousels.isEmpty()) {
            initCarousel(carousels);
        }

        if (!posters.isEmpty()) {
            initPosters(posters);
        }
        if (scrollFromBorder) {
            if (isRight) {//右侧移入
                if ("bottom".equals(bottomFlag)) {//下边界移入
                    lastpostview.findViewById(R.id.poster_title).requestFocus();
                } else {//上边界边界移入
                    toppage_carous_imageView1.requestFocus();
                }
//                		}
            } else {//左侧移入
                if (!StringUtils.isEmpty(bottomFlag)) {
                    if ("bottom".equals(bottomFlag)) {

                    } else {

                    }
                }
            }
            ((HomePageActivity) getActivity()).resetBorderFocus();
        }

    }

    private void initPosters(ArrayList<HomePagerEntity.Poster> posters) {
        guideRecommmendList.removeAllViews();
        ArrayList<FrameLayout> imageViews = new ArrayList<FrameLayout>();
        for (int i = 0; i < 8; i++) {
            if (mContext == null) {
                return;
            }
            HomeItemContainer frameLayout = (HomeItemContainer) LayoutInflater
                    .from(mContext).inflate(R.layout.item_poster, null);
            ImageView itemView = (ImageView) frameLayout
                    .findViewById(R.id.poster_image);
            TextView textView = (TextView) frameLayout
                    .findViewById(R.id.poster_title);
            if (!TextUtils.isEmpty(posters.get(i).getIntroduction())) {
                textView.setText(posters.get(i).getIntroduction());
                textView.setVisibility(View.VISIBLE);
            }
            frameLayout.setFocusable(true);
            frameLayout.setClickable(true);
            textView.setOnClickListener(ItemClickListener);
            frameLayout.setTag(R.id.view_position_tag, i + 5);
            frameLayout.setOnClickListener(ItemClickListener);
            textView.setTag(R.id.poster_title, i);
            textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    Object tagObject = v.getTag(R.id.poster_title);
                    if (hasFocus) {
                        ((HomeItemContainer) v.getParent())
                                .setDrawBorder(true);
                        ((HomeItemContainer) v.getParent()).invalidate();
                        if (tagObject != null) {
                            int tagindex = Integer.parseInt(tagObject.toString());
                            if (tagindex == 0 || tagindex == 7) {
                                ((HomePageActivity) (getActivity())).setLastViewTag("bottom");
                            }
                        }
                    } else {
                        ((HomeItemContainer) v.getParent())
                                .setDrawBorder(false);
                        ((HomeItemContainer) v.getParent()).invalidate();
                    }
                }
            });

            Picasso.with(mContext).load(posters.get(i).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE)
                    .into(itemView);
            posters.get(i).setPosition(i);
            textView.setTag(posters.get(i));
            frameLayout.setTag(posters.get(i));
            if (i == 0) {
                frameLayout.setId(R.id.guidefragment_firstpost);
            }
            if (i == 7) {
                frameLayout.setId(R.id.guidefragment_lastpost);
                lastpostview = frameLayout;
            }
            imageViews.add(frameLayout);
            switch (i) {
                case 0:
                    mLeftBottomView = frameLayout;
                    break;
                case 7:
                    mRightBottomView = frameLayout;
                    break;
            }
        }

        guideRecommmendList.setFocusable(true);
        guideRecommmendList.setFocusableInTouchMode(true);
        guideRecommmendList.addAllViews(imageViews);

    }

    private void initCarousel(ArrayList<HomePagerEntity.Carousel> carousels) {
        carousels = new ArrayList<>(carousels);
        mCarousels = carousels;
        allItem = new ArrayList<>();
        allVideoUrl = new ArrayList<>();

        try {

            Picasso.with(mContext).load(carousels.get(0).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(toppage_carous_imageView1);
            toppage_carous_imageView1.setTag(0);
            toppage_carous_imageView1.setTag(R.drawable.launcher_selector, carousels.get(0));
            toppage_carous_imageView1.setOnClickListener(ItemClickListener);
            toppage_carous_imageView1.setOnFocusChangeListener(itemFocusChangeListener);
            carousels.get(0).setPosition(0);
            if (carousels.size() > 1) {
                Picasso.with(mContext).load(carousels.get(1).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(toppage_carous_imageView2);
                toppage_carous_imageView2.setTag(1);
                toppage_carous_imageView2.setTag(R.drawable.launcher_selector, carousels.get(1));
                toppage_carous_imageView2.setOnClickListener(ItemClickListener);
                toppage_carous_imageView2.setOnFocusChangeListener(itemFocusChangeListener);
                carousels.get(1).setPosition(1);
            }
            if (carousels.size() > 2) {
                Picasso.with(mContext).load(carousels.get(2).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(toppage_carous_imageView3);
                toppage_carous_imageView3.setTag(2);
                toppage_carous_imageView3.setTag(R.drawable.launcher_selector, carousels.get(2));
                toppage_carous_imageView3.setOnClickListener(ItemClickListener);
                toppage_carous_imageView3.setOnFocusChangeListener(itemFocusChangeListener);
                carousels.get(2).setPosition(2);
            }
            allItem.add(toppage_carous_imageView1);
            allItem.add(toppage_carous_imageView2);
            allItem.add(toppage_carous_imageView3);
            toppage_carous_imageView1.setTag(R.id.view_position_tag, 2);
            toppage_carous_imageView2.setTag(R.id.view_position_tag, 3);
            toppage_carous_imageView3.setTag(R.id.view_position_tag, 4);

            allVideoUrl.add(carousels.get(0).getVideo_url());
            if (carousels.size() > 1) {
                allVideoUrl.add(carousels.get(1).getVideo_url());
            }
            if (carousels.size() > 2) {
                allVideoUrl.add(carousels.get(2).getVideo_url());
            }
        } catch (Exception e) {
            new CallaPlay().exception_except("launcher", "launcher", "homepage",
                    "", 0, "",
                    SimpleRestClient.appVersion, "client", ""
            );
        }

        carouselMap = new HashMap<>();
        carouselMap.put(0, toppage_carous_imageView1.getId());
        if (carousels.size() > 1) {
            carouselMap.put(1, toppage_carous_imageView2.getId());
        }
        if (carousels.size() > 2) {
            carouselMap.put(2, toppage_carous_imageView3.getId());
        }

        playCarousel(0);
        boolean isPlayAd = ((HomePageActivity) getActivity()).isPlayingStartAd;
        // 播放首页广告时，导视不播放
        if (isPlayAd) {
            mHandler.removeMessages(START_PLAYBACK);
        }

    }

    @Override
    public void playCarouselVideo() {
        super.playCarouselVideo();
        Log.i("LH/", "playCarouselVideo");
        if (carouselMap != null) {
            mHandler.sendEmptyMessageDelayed(START_PLAYBACK, 0);
        }

    }

    private void playCarousel(int delay) {
        mHandler.removeMessages(CAROUSEL_NEXT);
        try {
            switch (mCarouselRepeatType) {
                case Once:
                    break;
                case All:
                    if (mCurrentCarouselIndex == mCarousels.size() - 1) {
                        mCurrentCarouselIndex = 0;
                    } else {
                        mCurrentCarouselIndex = mCurrentCarouselIndex + 1;
                    }
                    break;
            }

            for (int i = 0; i < allItem.size(); i++) {
                LabelImageView3 imageView = allItem.get(i);
                if (mCurrentCarouselIndex != i) {
                    imageView.setCustomFocus(false);
                } else {
                    imageView.setCustomFocus(true);
                }
            }

            film_post_layout.setNextFocusRightId(carouselMap.get(mCurrentCarouselIndex));

            film_post_layout.setTag(R.drawable.launcher_selector, mCarousels.get(mCurrentCarouselIndex));
            mHandler.removeMessages(START_PLAYBACK);
            mHandler.sendEmptyMessageDelayed(START_PLAYBACK, delay);
        } catch (Exception e) {
            new CallaPlay().exception_except("launcher", "launcher", "homepage",
                    "", 0, "",
                    SimpleRestClient.appVersion, "client", ""
            );
        }


    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_PLAYBACK:
                    startPlayback();
                    break;
                case CAROUSEL_NEXT:
                    playCarousel(0);
                    break;
            }

        }
    };


    private void startPlayback() {

        if (mSurfaceView == null)
            return;
        try {
            Log.d(TAG, "startPlayback is invoke...");

            mSurfaceView.setFocusable(false);
            mSurfaceView.setFocusableInTouchMode(false);
            String videoName = "guide_" + mCurrentCarouselIndex + ".mp4";
            String videoPath = CacheManager.getInstance().doRequest(mCarousels.get(mCurrentCarouselIndex).getVideo_url(), videoName, DownloadClient.StoreType.Internal);
            Log.d(TAG, "current video path ====> " + videoPath);
            CallaPlay play = new CallaPlay();
            play.homepage_vod_trailer_play(videoPath, "launcher");
            if (mSurfaceView.isPlaying() && mSurfaceView.getDataSource().equals(videoPath)) {
                return;
            }
            linkedVideoLoadingImage.setVisibility(View.VISIBLE);
            stopPlayback();
            initCallback();
            mSurfaceView.setVideoPath(videoPath);
//            mSurfaceView.start();
            mSurfaceView.setFocusable(true);
            mSurfaceView.setFocusableInTouchMode(true);

        } catch (Exception e) {
            e.printStackTrace();
            new CallaPlay().exception_except("launcher", "launcher", "homepage",
                    "", 0, "",
                    SimpleRestClient.appVersion, "client", ""
            );
        }
    }

    private void stopPlayback() {
//        mSurfaceView.pause();
        mSurfaceView.stopPlayback();

    }


    private View.OnFocusChangeListener itemFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            boolean focusFlag = true;
            for (ImageView imageView : allItem) {
                focusFlag = focusFlag && (!imageView.isFocused());
            }
            if (hasFocus) {
                ((HomePageActivity) (getActivity())).setLastViewTag("");
            }
            // all view not focus
            if (focusFlag) {
                mCarouselRepeatType = CarouselRepeatType.All;
            } else {
                if (hasFocus) {
//                    mHelper.onStop();
                    int position = (Integer) v.getTag();
                    mCarouselRepeatType = CarouselRepeatType.Once;
                    mCurrentCarouselIndex = position;
                    playCarousel(100);
                }
            }
        }
    };


    enum CarouselRepeatType {
        All,
        Once
    }


    private MediaPlayer.OnCompletionListener videoPlayEndListener;

    private MediaPlayer.OnErrorListener mVideoOnErrorListener;

    private MediaPlayer.OnPreparedListener mOnPreparedListener;

    private void initCallback(){
        videoPlayEndListener = new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlayback();
                mHandler.sendEmptyMessage(CAROUSEL_NEXT);
            }
        };
        mVideoOnErrorListener = new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                Log.e(TAG, "play video error!!!");

                return true;
            }
        };
        mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if(mp != null && !mp.isPlaying()){
                    mp.start();
                }
                if (bitmapDecoder != null && bitmapDecoder.isAlive()) {
                    bitmapDecoder.interrupt();
                }
                linkedVideoLoadingImage.setVisibility(View.GONE);
            }
        };
        mSurfaceView.setOnCompletionListener(videoPlayEndListener);
        mSurfaceView.setOnErrorListener(mVideoOnErrorListener);
        mSurfaceView.setOnPreparedListener(mOnPreparedListener);
    }
}



class Flag {

    private ChangeCallback changeCallback;

    public Flag(ChangeCallback changeCallback) {
        this.changeCallback = changeCallback;
    }

    private int position;

    public void setPosition(int position) {
        this.position = position;
        changeCallback.change(position);

    }

    public int getPosition() {
        return position;
    }

    public interface ChangeCallback {
        void change(int position);
    }
}



