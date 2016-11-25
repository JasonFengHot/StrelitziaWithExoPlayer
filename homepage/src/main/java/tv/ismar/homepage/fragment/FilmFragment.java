package tv.ismar.homepage.fragment;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.utils.StringUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

//import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.ismartv.downloader.DownloadEntity;
import cn.ismartv.downloader.DownloadStatus;
import cn.ismartv.downloader.Md5;
import cn.ismartv.injectdb.library.query.Select;
import okhttp3.HttpUrl;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import tv.ismar.app.entity.HomePagerEntity;
import tv.ismar.app.entity.HomePagerEntity.Carousel;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.app.util.HardwareUtils;
import tv.ismar.homepage.R;
import tv.ismar.homepage.view.HomePageActivity;
import tv.ismar.homepage.widget.DaisyVideoView;
import tv.ismar.homepage.widget.HomeItemContainer;
import tv.ismar.homepage.widget.LabelImageView3;

/**
 * Created by huaijie on 5/18/15.
 */
public class FilmFragment extends ChannelBaseFragment {
    private static final String TAG = "FilmFragment";
    private static final int START_PLAYBACK = 0x0000;
    private static final int CAROUSEL_NEXT = 0x0010;

    private LinearLayout guideRecommmendList;
//    private RelativeLayout carouselLayout;
    private HomeItemContainer film_post_layout;
    private LabelImageView3 film_carous_imageView1;
    private LabelImageView3 film_carous_imageView2;
    private LabelImageView3 film_carous_imageView3;
    private LabelImageView3 film_carous_imageView4;
    private LabelImageView3 film_carous_imageView5;

    private ImageView linkedVideoImage;
    private TextView film_linked_title;
    private LabelImageView3 film_lefttop_image;

    private ArrayList<Carousel> mCarousels;
    private ArrayList<LabelImageView3> allItem;


    private DaisyVideoView mSurfaceView;

    private int mCurrentCarouselIndex = -1;
    private CarouselRepeatType mCarouselRepeatType = CarouselRepeatType.All;

    private String mChannelName;
    private HomeItemContainer morelayout;
    private HomeItemContainer firstpost;
    private LabelImageView3 firstcarousel;
    private BitmapDecoder bitmapDecoder;

    private Subscription playSubscription;
    private Subscription dataSubscription;
    private Subscription checkSubscription;

    private HashMap<Integer, Integer> carouselMap;
    private boolean externalStorageIsEnable = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.fragment_film, null);
        guideRecommmendList = (LinearLayout) mView.findViewById(R.id.film_recommend_list);
        film_carous_imageView1 = (LabelImageView3) mView.findViewById(R.id.film_carous_imageView1);
        film_carous_imageView2 = (LabelImageView3) mView.findViewById(R.id.film_carous_imageView2);
        film_carous_imageView3 = (LabelImageView3) mView.findViewById(R.id.film_carous_imageView3);
        film_carous_imageView4 = (LabelImageView3) mView.findViewById(R.id.film_carous_imageView4);
        film_carous_imageView5 = (LabelImageView3) mView.findViewById(R.id.film_carous_imageView5);
        mRightTopView = film_carous_imageView1;
        mSurfaceView = (DaisyVideoView) mView.findViewById(R.id.film_linked_video);
        mSurfaceView.setOnCompletionListener(mOnCompletionListener);
        mSurfaceView.setOnErrorListener(mVideoOnErrorListener);
        mSurfaceView.setOnPreparedListener(mOnPreparedListener);

        film_lefttop_image = (LabelImageView3) mView.findViewById(R.id.film_lefttop_image);
        film_post_layout = (HomeItemContainer) mView.findViewById(R.id.film_post_layout);
        linkedVideoImage = (ImageView) mView.findViewById(R.id.film_linked_image);
        film_linked_title = (TextView) mView.findViewById(R.id.film_linked_title);
        film_post_layout.setNextFocusRightId(R.id.filmfragment_firstcarousel);
        film_post_layout.setOnClickListener(ItemClickListener);
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
        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(mContext, R.drawable.guide_video_loading, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                linkedVideoImage.setBackgroundDrawable(bitmapDrawable);
            }
        });
        return mView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if (playSubscription != null && !playSubscription.isUnsubscribed()) {
            playSubscription.unsubscribe();
        }

        if (dataSubscription != null && !dataSubscription.isUnsubscribed()) {
            dataSubscription.unsubscribe();
        }

        if (checkSubscription != null && !checkSubscription.isUnsubscribed()) {
            checkSubscription.unsubscribe();
        }

        playSubscription = null;
        dataSubscription = null;
        checkSubscription = null;

        mHandler.removeMessages(START_PLAYBACK);
        mHandler.removeMessages(CAROUSEL_NEXT);
        film_post_layout.removeAllViews();
        guideRecommmendList.removeAllViews();
        guideRecommmendList = null;
        film_carous_imageView1 = null;
        film_carous_imageView2 = null;
        film_carous_imageView3 = null;
        film_carous_imageView4 = null;
        film_carous_imageView5 = null;
        film_post_layout = null;
        if (film_lefttop_image != null && film_lefttop_image.getDrawingCache() != null && !film_lefttop_image.getDrawingCache().isRecycled()) {
            film_lefttop_image.getDrawingCache().recycle();
            film_lefttop_image = null;
        }
        super.onDestroyView();

    }


    @Override
    public void onResume() {
        super.onResume();
        checkExternalIsEnable();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
//        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
//        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
//        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
//        intentFilter.addDataScheme("file");
        if (mCarousels == null) {
            if (channelEntity != null)
                fetchHomePage(channelEntity.getHomepage_url());
        } else {
            if (!mSurfaceView.isPlaying()) {
                playCarousel(500);
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
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

    private void fetchHomePage(String url) {
        mChannelName = getChannelEntity().getChannel();
        dataSubscription = ((HomePageActivity)getActivity()).mSkyService.fetchHomePage(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HomePagerEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "fetchHomePage: " + e.getMessage());
                    }

                    @Override
                    public void onNext(HomePagerEntity homePagerEntity) {
                        fillLayout(homePagerEntity);
                    }
                });
    }

    private void fillLayout(HomePagerEntity homePagerEntity) {
        if (mContext == null || guideRecommmendList == null)
            return;
        ArrayList<HomePagerEntity.Poster> posters = homePagerEntity.getPosters();
        ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity.getCarousels();

        Log.d(TAG, "posters size: " + posters.size());
        Log.d(TAG, "carousels size: " + carousels.size());
        initPosters(posters);
        initCarousel(carousels);
        if (scrollFromBorder) {
            if (isRight) {//右侧移入
                if ("bottom".equals(bottomFlag)) {//下边界移入
                    morelayout.requestFocus();
                } else {//上边界边界移入
                    firstcarousel.requestFocus();
                }
//                		}
            } else {//左侧移入
                if ("bottom".equals(bottomFlag)) {
                    firstpost.requestFocus();
                } else {
                    film_lefttop_image.requestFocus();
                }
//                	}
            }
            ((HomePageActivity) getActivity()).resetBorderFocus();
        }
    }

    private HomeItemContainer focusView;

    private void initPosters(ArrayList<HomePagerEntity.Poster> posters) {
        if (guideRecommmendList == null || mContext == null)
            return;
        guideRecommmendList.removeAllViews();
        posters.get(0).setPosition(0);
        film_lefttop_image.setUrl(posters.get(0).getCustom_image());
        film_lefttop_image.setTitle(posters.get(0).getIntroduction());
        film_lefttop_image.setOnClickListener(ItemClickListener);
        film_lefttop_image.setTag(posters.get(0));
        film_lefttop_image.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    ((HomePageActivity) (getActivity())).setLastViewTag("");
                }
            }
        });
        mLeftTopView = film_lefttop_image;
        int width = getResources().getDimensionPixelOffset(R.dimen.guide_bottom_item_w);
        int height = getResources().getDimensionPixelOffset(R.dimen.guide_bottom_h);
        float space = getResources().getDimensionPixelOffset(R.dimen.guide_bottom_space);
        for (int i = 1; i <= posters.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            if (i > 1) {
                params.setMargins((int)space, 0, 0, 0);
            }
            ImageView itemView = new ImageView(mContext);
            itemView.setFocusable(true);
            itemView.setLayoutParams(params);
            itemView.setOnClickListener(ItemClickListener);
            if (i <= posters.size() - 1) {
                posters.get(i).setPosition(i);
                HomeItemContainer frameLayout = (HomeItemContainer) LayoutInflater.from(mContext).inflate(R.layout.item_poster, null);
                ImageView postitemView = (ImageView) frameLayout.findViewById(R.id.poster_image);
                TextView textView = (TextView) frameLayout.findViewById(R.id.poster_title);
                if (!StringUtils.isEmpty(posters.get(i).getIntroduction())) {
                    textView.setText(posters.get(i).getIntroduction());
                    textView.setVisibility(View.VISIBLE);
                }
                textView.setTag(R.id.poster_title, i);
                textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        Object tagObject = v.getTag(R.id.poster_title);
                        if (hasFocus) {
                            ((HomeItemContainer) v.getParent())
                                    .setDrawBorder(true);
                            ((HomeItemContainer) v.getParent()).invalidate();
                            focusView = ((HomeItemContainer) v.getParent());
                            if (tagObject != null) {
                                int tagindex = Integer.parseInt(tagObject.toString());
                                if (tagindex == 1) {
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
                textView.setOnClickListener(ItemClickListener);
                textView.setTag(posters.get(i));
                frameLayout.setOnClickListener(ItemClickListener);
                Picasso.with(mContext).load(posters.get(i).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(postitemView);
                frameLayout.setTag(posters.get(i));
                frameLayout.setLayoutParams(params);
                if (i == 1) {
                    frameLayout.setId(R.id.filmfragment_firstpost);
                }
                if (i == 2) {
                    frameLayout.setId(R.id.filmfragment_secondpost);
                }
                if (i == 3) {
                    frameLayout.setId(R.id.filmfragment_thirdpost);
                }
                guideRecommmendList.addView(frameLayout);
                if (i == 1) {
                    firstpost = frameLayout;
                }

            } else {
                params.width = width;
                params.height = height;
                morelayout = (HomeItemContainer) LayoutInflater.from(
                        mContext).inflate(R.layout.toppagelistmorebutton,
                        null);
                morelayout.setLayoutParams(params);
                View view = morelayout.findViewById(R.id.listmore);
                view.setOnClickListener(ItemClickListener);

                mRightBottomView = morelayout;
                guideRecommmendList.addView(morelayout);
                morelayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View arg0, boolean arg1) {
                        if (arg1) {
                            ((HomePageActivity) (getActivity())).setLastViewTag("bottom");
                        }
                    }
                });
            }
        }
    }


    private void initCarousel( ArrayList<HomePagerEntity.Carousel> carousels) {
        allItem = new ArrayList<LabelImageView3>();
        mCarousels = carousels;
        carouselMap =new HashMap<>();

        ArrayList<HomePagerEntity.Carousel> newerCarousels = new ArrayList<>();
        Log.i("LH/", "filmCarousel:" + carousels.size());


        for (int i = 0; i < carousels.size(); i++) {
//            DownloadManager.getInstance().start(carousels.get(i).getVideo_url(), mChannelName + "_" + i, new Gson().toJson(carousels.get(i)), Environment.getExternalStorageDirectory() + "/Daisy/");

            List<DownloadEntity> downloadEntities = new Select().from(DownloadEntity.class).where("title = ?", mChannelName + "_"+ i).orderBy(" start_time DESC").execute();

            if (downloadEntities!= null && downloadEntities.size() >1){
                if (downloadEntities.get(0).status == DownloadStatus.COMPLETED) {
                    newerCarousels.add(carousels.get(i));

                    File file = new File(downloadEntities.get(1).savePath);
                    if (file.exists()) {
                        file.delete();
                    }
                    downloadEntities.get(1).delete();

                }else {
                    newerCarousels.add(carousels.get(i));
                }

            }else {
                newerCarousels.add(carousels.get(i));
            }
        }
        carousels = newerCarousels;
        mCarousels = newerCarousels;

        try {
            Picasso.with(mContext).load(carousels.get(0).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(film_carous_imageView1);
            film_carous_imageView1.setTag(0);
            film_carous_imageView1.setTag(R.drawable.launcher_selector, carousels.get(0));
            film_carous_imageView1.setOnClickListener(ItemClickListener);
            film_carous_imageView1.setOnFocusChangeListener(itemFocusChangeListener);
            carousels.get(0).setPosition(0);
            Picasso.with(mContext).load(carousels.get(1).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(film_carous_imageView2);
            film_carous_imageView2.setTag(1);
            film_carous_imageView2.setTag(R.drawable.launcher_selector, carousels.get(1));
            film_carous_imageView2.setOnClickListener(ItemClickListener);
            film_carous_imageView2.setOnFocusChangeListener(itemFocusChangeListener);
            carousels.get(1).setPosition(1);
            Picasso.with(mContext).load(carousels.get(2).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(film_carous_imageView3);
            film_carous_imageView3.setTag(2);
            film_carous_imageView3.setTag(R.drawable.launcher_selector, carousels.get(2));
            film_carous_imageView3.setOnClickListener(ItemClickListener);
            film_carous_imageView3.setOnFocusChangeListener(itemFocusChangeListener);
            carousels.get(2).setPosition(2);

            Picasso.with(mContext).load(carousels.get(3).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(film_carous_imageView4);
            film_carous_imageView4.setTag(3);
            film_carous_imageView4.setTag(R.drawable.launcher_selector, carousels.get(3));
            film_carous_imageView4.setOnClickListener(ItemClickListener);
            film_carous_imageView4.setOnFocusChangeListener(itemFocusChangeListener);
            carousels.get(3).setPosition(3);

            Picasso.with(mContext).load(carousels.get(4).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(film_carous_imageView5);
            film_carous_imageView5.setTag(4);
            film_carous_imageView5.setTag(R.drawable.launcher_selector, carousels.get(4));
            film_carous_imageView5.setOnClickListener(ItemClickListener);
            film_carous_imageView5.setOnFocusChangeListener(itemFocusChangeListener);
            carousels.get(4).setPosition(4);
            allItem.add(film_carous_imageView1);
            allItem.add(film_carous_imageView2);
            allItem.add(film_carous_imageView3);
            allItem.add(film_carous_imageView4);
            allItem.add(film_carous_imageView5);
        } catch (Exception e) {
        }

        firstcarousel = film_carous_imageView1;
        carouselMap = new HashMap<>();
        carouselMap.put(0, film_carous_imageView1.getId());
        carouselMap.put(1, film_carous_imageView2.getId());
        carouselMap.put(2, film_carous_imageView3.getId());
        carouselMap.put(3, film_carous_imageView4.getId());
        carouselMap.put(4, film_carous_imageView5.getId());

//        int width = getResources().getDimensionPixelOffset(R.dimen.film_carousel_item_w);
//        int height = getResources().getDimensionPixelOffset(R.dimen.film_carousel_item_h);
//        int topMargin = getResources().getDimensionPixelOffset(R.dimen.film_carousel_item_space);
//        for (int i = 0; i < carousels.size() && i < 5; i++) {
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
//            LabelImageView3 itemView = new LabelImageView3(mContext);
//            if (i == 0) {
//                params.topMargin = 0;
//                itemView.setId(R.id.filmfragment_firstcarousel);
//                firstcarousel = itemView;
//            } else {
//                itemView.setId(R.id.filmfragment_firstcarousel + i * 5);
//                params.topMargin = topMargin;
//                params.addRule(RelativeLayout.BELOW, R.id.filmfragment_firstcarousel + 5 * (i - 1));
//            }
//            if (mContext == null)
//                return;
//            itemView.setFocusable(true);
//            itemView.setFocusableInTouchMode(true);
//            itemView.setNeedZoom(true);
//            Picasso.with(mContext).load(carousels.get(i).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE)
//                    .into(itemView);
//            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
//            itemView.setLayoutParams(params);
//            itemView.setTag(i);
//            carousels.get(i).setPosition(i);
//            itemView.setTag(R.drawable.launcher_selector, carousels.get(i));
//            itemView.setOnClickListener(ItemClickListener);
//            itemView.setOnFocusChangeListener(itemFocusChangeListener);
//            int shadowcolor = mContext.getResources().getColor(R.color.carousel_focus);
//            itemView.setFrontColor(shadowcolor);
//            carouselMap.put(i, itemView.getId());
//            allItem.add(itemView);
//            carouselLayout.addView(itemView);
//
//            if (i == 0) {
//
//            }
//        }
        playCarousel(0);
    }


    private void startPlayback() {
        if (mSurfaceView == null)
            return;
        try {
            Log.d(TAG, "startPlayback is invoke...");

            mSurfaceView.setFocusable(false);
            mSurfaceView.setFocusableInTouchMode(false);
            String videoName = "guide_" + mCurrentCarouselIndex + ".mp4";
            String videoPath = mCarousels.get(mCurrentCarouselIndex).getVideo_url();
            DownloadEntity downloadEntity = new Select().from(DownloadEntity.class).where("url_md5 = ?", Md5.md5String(videoPath)).executeSingle();
            if (downloadEntity!= null && downloadEntity.status == DownloadStatus.COMPLETED){
                videoPath = downloadEntity.savePath;
            }

            Log.d(TAG, "current video path ====> " + videoPath);

            if (mSurfaceView.isPlaying() && mSurfaceView.getDataSource().equals(videoPath)) {
                return;
            }
            linkedVideoImage.setVisibility(View.VISIBLE);
            stopPlayback();
            mSurfaceView.setVideoPath(videoPath);
            mSurfaceView.start();
            mSurfaceView.setFocusable(true);
            mSurfaceView.setFocusableInTouchMode(true);

        } catch (Exception e) {
        }
    }

    private void stopPlayback() {
        mSurfaceView.pause();
        mSurfaceView.stopPlayback();
    }

    private void playCarousel(final int delay) {
        mHandler.removeMessages(CAROUSEL_NEXT);
        if (film_post_layout == null)
            return;
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

//        String videoUrl = mCarousels.get(mCurrentCarouselIndex).getVideo_url();
//        if (playSubscription != null && !playSubscription.isUnsubscribed()) {
//            playSubscription.unsubscribe();
//        }
        film_post_layout.setNextFocusRightId(carouselMap.get(mCurrentCarouselIndex));
        film_post_layout.setTag(R.drawable.launcher_selector, mCarousels.get(mCurrentCarouselIndex));
        mHandler.removeMessages(START_PLAYBACK);
        mHandler.sendEmptyMessageDelayed(START_PLAYBACK, delay);
//        playSubscription = null;
//        playSubscription = Observable.just(videoUrl)
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.io())
//                .map(new Func1<String, Boolean>() {
//                    @Override
//                    public Boolean call(String s) {
//                        Log.i(TAG, "map thread: " + Thread.currentThread().getName());
//                        HttpUrl parsed = HttpUrl.parse(s);
//                        if (TextUtils.isEmpty(s) || parsed == null) {
//                            return false;
//                        }
//                        DownloadEntity downloadEntity = new Select().from(DownloadEntity.class).where("url_md5 = ?", Md5.md5String(s)).executeSingle();
//                        return externalStorageIsEnable && downloadEntity.status == DownloadStatus.COMPLETED;
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<Boolean>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        playImage();
//                    }
//
//                    @Override
//                    public void onNext(Boolean enable) {
//                        Log.i(TAG, "onNext thread: " + Thread.currentThread().getName());
//                        if (enable) {
//                            playVideo(delay);
//                        } else {
//                            playImage();
//                        }
//                    }
//                });
    }

    private boolean externalStorageIsEnable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                final File file = new File(HardwareUtils.getSDCardCachePath(), "/text/test" + ".mp4");
                if (!file.getParentFile().exists()) {
                    boolean result = file.getParentFile().mkdirs();
                    if (!result) {
                        Log.i(TAG, "externalStorageIsEnable file.getParentFile().mkdirs()");
                        return false;
                    }

                }
                if (!file.exists()) {
                    boolean result = file.createNewFile();
                    if (!result) {
                        Log.i(TAG, "file.createNewFile()");
                        return false;
                    }
                }

                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write("hello world!!!");
                fileWriter.flush();
                fileWriter.close();

                FileReader fileReader = new FileReader(file);
                fileReader.read();
                fileReader.close();
                return true;
            } catch (IOException e) {
                Log.i(TAG, "externalStorageIsEnable IOException: " + e.getMessage());
                return false;
            }
        } else {
            Log.i(TAG, "externalStorageIsEnable not MEDIA_MOUNTED");
            return false;
        }
    }

    private void playImage() {
        if (mSurfaceView.getVisibility() == View.VISIBLE) {
            mSurfaceView.setVisibility(View.GONE);
        }

        if (linkedVideoImage.getVisibility() == View.GONE) {
            linkedVideoImage.setVisibility(View.VISIBLE);
        }


        String url = mCarousels.get(mCurrentCarouselIndex).getVideo_image();
        String intro = mCarousels.get(mCurrentCarouselIndex).getIntroduction();
        if (!StringUtils.isEmpty(intro)) {
            film_linked_title.setVisibility(View.VISIBLE);
            film_linked_title.setText(intro);
        } else {
            film_linked_title.setVisibility(View.GONE);
        }
        Picasso.with(mContext).load(url).memoryPolicy(MemoryPolicy.NO_STORE).into(linkedVideoImage, new Callback() {
            int pauseTime = Integer.parseInt(mCarousels.get(mCurrentCarouselIndex).getPause_time());

            @Override
            public void onSuccess() {
                mHandler.sendEmptyMessageDelayed(CAROUSEL_NEXT, pauseTime * 1000);
            }

            @Override
            public void onError() {
                mHandler.sendEmptyMessageDelayed(CAROUSEL_NEXT, pauseTime * 1000);
            }
        });
    }

    private void playVideo(int delay) {
        if (mSurfaceView.getVisibility() == View.GONE) {
            mSurfaceView.setVisibility(View.VISIBLE);
        }

        if (linkedVideoImage.getVisibility() == View.VISIBLE) {
            linkedVideoImage.setVisibility(View.GONE);
        }


        String intro = mCarousels.get(mCurrentCarouselIndex).getIntroduction();
        if (!StringUtils.isEmpty(intro)) {
            film_linked_title.setVisibility(View.VISIBLE);
            film_linked_title.setText(intro);
        } else {
            film_linked_title.setVisibility(View.GONE);
        }
        mHandler.removeMessages(START_PLAYBACK);
        mHandler.sendEmptyMessageDelayed(START_PLAYBACK, delay);
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

    enum CarouselRepeatType {
        All,
        Once
    }

    private View.OnFocusChangeListener itemFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(final View v, boolean hasFocus) {
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
                    int position = (Integer) v.getTag();
                    mCarouselRepeatType = CarouselRepeatType.Once;
                    mCurrentCarouselIndex = position;
                    playCarousel(100);
                }
            }
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            stopPlayback();
            mHandler.sendEmptyMessage(CAROUSEL_NEXT);
        }
    };

    private MediaPlayer.OnErrorListener mVideoOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {

            Log.e(TAG, "play video error!!!");

            return true;
        }
    };

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (bitmapDecoder != null && bitmapDecoder.isAlive()) {
                bitmapDecoder.interrupt();
            }
            linkedVideoImage.setVisibility(View.GONE);
        }
    };

    public void refreshData() {
        checkExternalIsEnable();
        fetchHomePage(channelEntity.getHomepage_url());
    }

    private void checkExternalIsEnable() {
        checkSubscription = Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        subscriber.onNext("check external storage");
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String aBoolean) {
                        return externalStorageIsEnable();

                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        externalStorageIsEnable = aBoolean;
                    }
                });
    }
}

