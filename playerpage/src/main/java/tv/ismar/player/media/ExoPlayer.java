package tv.ismar.player.media;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.ismartv.exoplayer.EventLogger;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import tv.ismar.app.network.entity.ClipEntity;

/** Created by huibin on 11/23/16. */
public class ExoPlayer extends IsmartvPlayer
        implements com.google.android.exoplayer2.ExoPlayer.EventListener {
    private static final String TAG = "ExoPlayer";
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    protected String userAgent;
    private SimpleExoPlayer player;
    private EventLogger eventLogger;
    private MappingTrackSelector trackSelector;
    private Handler mainHandler;
    private DataSource.Factory mediaDataSourceFactory;
    private String[] videoPaths;
    private String path;
    private MediaSource mediaSource;

    private M3U8Service mM3U8Service;
    private Subscription mM3U8Sub;

    public ExoPlayer() {
        this(PlayerBuilder.MODE_SMART_PLAYER);

        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl("http://www.baidu.com/")
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .build();
        mM3U8Service = retrofit.create(M3U8Service.class);

        Logger.init(TAG) // default PRETTYLOGGER or use just init()
                .methodCount(3) // default 2
                .logLevel(LogLevel.FULL) // default LogLevel.FULL
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
        fetchM3u8(Arrays.asList(urls));
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
    public void stop() {
        player.stop();
    }

    @Override
    public void release() {
        player.release();
    }

    @Override
    public void seekTo(int position) {
        Log.d(TAG, "seekTo: " + position);
        player.seekTo(position);
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public int getDuration() {
        Log.d(TAG, "duration: " + player.getDuration());
        return (int) player.getDuration();
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
    public void switchQuality(ClipEntity.Quality quality) {}

    @Override
    public boolean isInPlaybackState() {
        return true;
    }

    private void initializePlayer() {
        if (player == null) {
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            player =
                    ExoPlayerFactory.newSimpleInstance(
                            mContext, trackSelector, new DefaultLoadControl());
            player.addListener(this);
            eventLogger = new EventLogger(trackSelector);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setMetadataOutput(eventLogger);
            mDaisyVideoView.setVisibility(View.VISIBLE);
            player.setVideoSurfaceView(mDaisyVideoView);

            player.setPlayWhenReady(true);

            //            MediaSource[] mediaSources = new MediaSource[uris.length];
            //            mediaSources[0] = buildMediaSource(uris[0]);
            //
            //            mediaSource = mediaSources[0];

            List<MediaSource> mediaSourceList = new ArrayList<>();

            //            return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler,
            // eventLogger);

            for (String path : videoPaths) {
                mediaSourceList.add(
                        new HlsMediaSource(
                                Uri.parse(path), mediaDataSourceFactory, mainHandler, eventLogger));
            }

            //            MediaSource secondSource = new ExtractorMediaSource(secondVideoUri,...);
            // Plays the first video, then the second video.
            ConcatenatingMediaSource concatenatedSource =
                    new ConcatenatingMediaSource(
                            mediaSourceList.toArray(new MediaSource[mediaSourceList.size()]));
            player.prepare(concatenatedSource);
            mOnStateChangedListener.onStarted();
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        //        int type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ? "." +
        // overrideExtension
        //                : uri.getLastPathSegment());
        return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(
                mContext, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    // ExoPlayer
    @Override
    public void onLoadingChanged(boolean isLoading) {
        //        if (isLoading) {
        //            mOnBufferChangedListener.onBufferStart();
        //        } else {
        //            mOnBufferChangedListener.onBufferEnd();
        //
        //        }
    }

    @Override
    public void onPlayerStateChanged(boolean b, int i) {}

    @Override
    public void onTimelineChanged(Timeline timeline, Object o) {}

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {}

    @Override
    public void onPlayerError(ExoPlaybackException e) {}

    @Override
    public void onPositionDiscontinuity() {}

    private void fetchM3u8(List<String> uris) {

        Observable.just(uris)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(
                        new Func1<List<String>, List<String>>() {
                            @Override
                            public List<String> call(List<String> strings) {
                                List<String> m3u8s = new ArrayList<>();
                                for (String s : strings) {
                                    try {
                                        Response<ResponseBody> response =
                                                mM3U8Service.m3u8(s).execute();
                                        File file = File.createTempFile("video", ".m3u8");
                                        BufferedSink bufferedSink = Okio.buffer(Okio.sink(file));
                                        bufferedSink.writeAll(response.body().source());
                                        bufferedSink.close();
                                        m3u8s.add(file.getAbsolutePath());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return m3u8s;
                            }
                        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Observer<List<String>>() {
                            @Override
                            public void onCompleted() {}

                            @Override
                            public void onError(Throwable throwable) {}

                            @Override
                            public void onNext(List<String> strings) {
                                Logger.d(strings);
                                videoPaths = strings.toArray(new String[strings.size()]);
                                mediaDataSourceFactory = buildDataSourceFactory(true);
                                initializePlayer();
                            }
                        });
    }

    private interface M3U8Service {
        @GET
        @Streaming
        Call<ResponseBody> m3u8(@Url String url);
    }
}
