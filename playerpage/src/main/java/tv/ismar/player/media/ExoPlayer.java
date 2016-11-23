package tv.ismar.player.media;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;
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

import cn.ismartv.exoplayer.EventLogger;
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

    public ExoPlayer() {
        this(PlayerBuilder.MODE_SMART_PLAYER);
    }


    public ExoPlayer(byte mode) {
        super(mode);
        mainHandler = new Handler();

        userAgent = "ExoPlayerDemo";
    }


    @Override
    protected void setMedia(String[] urls) {
        super.setMedia(urls);
        videoPaths = urls;
        path = "asset:///test.m3u8";
        Log.d(TAG, "video path: " + path);
        mediaDataSourceFactory = buildDataSourceFactory(true);
        initializePlayer();
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
            mSurfaceView.setVisibility(View.VISIBLE);
            player.setVideoSurfaceView(mSurfaceView);


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
        if (isLoading){
            mCurrentState = STATE_BUFFERING;
            mOnBufferChangedListener.onBufferStart();
        }else {
            mCurrentState = STATE_PLAYING;
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
}
