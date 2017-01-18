package tv.ismar.player.media;

import android.net.Uri;
import android.os.Handler;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer.EventListener;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelections;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;

import cn.ismartv.exoplayer.EventLogger;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.network.entity.ClipEntity;

/**
 * Created by huibin on 11/23/16.
 */


public class ExoPlayer extends IsmartvPlayer implements EventListener, TrackSelector.EventListener<MappedTrackInfo> {
    private static final String TAG = "ExoPlayer";
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private SimpleExoPlayer player;
    private EventLogger eventLogger;
    private MappingTrackSelector trackSelector;

    private Handler mainHandler;

    private DataSource.Factory mediaDataSourceFactory;

    protected String userAgent;


    private String[] videoPaths;
    private String path;
    private MediaSource mediaSource;

    private M3U8Service mM3U8Service;
    private Subscription mM3U8Sub;


    public ExoPlayer() {
        this(PlayerBuilder.MODE_SMART_PLAYER);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.baidu.com/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        mM3U8Service = retrofit.create(M3U8Service.class);

        Logger.init(TAG)                 // default PRETTYLOGGER or use just init()
                .methodCount(3)                 // default 2
                .logLevel(LogLevel.FULL)        // default LogLevel.FULL
                .methodOffset(2);


        // default 0
    }


    public ExoPlayer(byte mode) {
        super(mode);
        mainHandler = new Handler();

        userAgent = "ExoPlayerDemo";
    }


    @Override
    protected void setMedia(String[] urls) {
        super.setMedia(urls);
        Logger.d(urls);
        videoPaths = urls;
        fetchM3u8(urls[0]);
    }


    @Override
    public void prepareAsync() {
        player.prepare(mediaSource);
    }

    @Override
    public void start() {
        player.setPlayWhenReady(true);

    }

    @Override
    public void pause() {
        mOnStateChangedListener.onPaused();
        player.setPlayWhenReady(false);

    }

    @Override
    public void seekTo(int position) {

    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getAdCountDownTime() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return player.getPlayWhenReady();

    }

    @Override
    public void switchQuality(ClipEntity.Quality quality) {

    }

    @Override
    public boolean isInPlaybackState() {
        return false;
    }

    private void initializePlayer() {
        if (player == null) {
            eventLogger = new EventLogger();
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);
            trackSelector.addListener(this);
            trackSelector.addListener(eventLogger);
            player = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector, new DefaultLoadControl());
            player.addListener(this);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setId3Output(eventLogger);
            mDaisyVideoView.setVisibility(View.VISIBLE);
            player.setVideoSurfaceView(mDaisyVideoView);


            player.setPlayWhenReady(true);

            Uri[] uris = new Uri[1];
            uris[0] = Uri.parse(path);

            MediaSource[] mediaSources = new MediaSource[uris.length];
            mediaSources[0] = buildMediaSource(uris[0]);

            mediaSource = mediaSources[0];

            player.prepare(mediaSource);
            mOnStateChangedListener.onStarted();
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
//        int type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension
//                : uri.getLastPathSegment());
        return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
    }


    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }


    DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(mContext, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    //ExoPlayer
    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (isLoading) {
            mOnBufferChangedListener.onBufferStart();
        } else {
            mOnBufferChangedListener.onBufferEnd();

        }
    }

    @Override
    public void onPlayerStateChanged(boolean b, int i) {

    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object o) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onTrackSelectionsChanged(TrackSelections<? extends MappedTrackInfo> trackSelections) {

    }

    private void fetchM3u8(String url) {
        mM3U8Sub = mM3U8Service.m3u8(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            File file = File.createTempFile("video", ".m3u8");
                            Logger.d(file);
                            BufferedSink bufferedSink = Okio.buffer(Okio.sink(file));
                            bufferedSink.writeAll(responseBody.source());
                            bufferedSink.close();
                            path = file.getAbsolutePath();
//                            path = "asset:///test.m3u8";
                            mediaDataSourceFactory = buildDataSourceFactory(true);
                            initializePlayer();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private interface M3U8Service {
        @GET
        @Streaming
        Observable<ResponseBody> m3u8(
                @Url String url
        );
    }
}
