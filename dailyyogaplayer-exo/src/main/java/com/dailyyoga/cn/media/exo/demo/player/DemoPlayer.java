/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dailyyoga.cn.media.exo.demo.player;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.EventLogger;

import java.util.concurrent.CopyOnWriteArrayList;

import tv.danmaku.ijk.media.player.misc.ITrackInfo;

/**
 * A wrapper around {link ExoPlayer} that provides a higher level interface. It can be prepared
 * with one of a number of {link RendererBuilder} classes to suit different use cases (e.g. DASH,
 * SmoothStreaming and so on).
 */
public class DemoPlayer implements Player.EventListener, AnalyticsListener {

    private static final String TAG = DemoPlayer.class.getSimpleName();

    /**
     * A listener for core events.
     */
    public interface Listener {

        void onStateChanged(boolean playWhenReady, int playbackState);

        void onError(ExoPlaybackException e);

        void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                float pixelWidthHeightRatio);

        void onRenderedFirstFrame();
    }

    private final SimpleExoPlayer player;
    private final CopyOnWriteArrayList<Listener> listeners;

    private int lastReportedPlaybackState = Player.STATE_IDLE;
    private boolean lastReportedPlayWhenReady;

    private Surface surface;

    public DemoPlayer(Context context) {

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);

        RenderersFactory renderersFactory =
                DemoUtil.buildRenderersFactory(/* context= */ context, true);
        DataSource.Factory dataSourceFactory = DemoUtil.getDataSourceFactory(/* context= */ context);
        MediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(dataSourceFactory);

        player = new SimpleExoPlayer.Builder(/* context= */ context, renderersFactory)
                .setMediaSourceFactory(mediaSourceFactory)
                .setTrackSelector(trackSelector)
                .build();

        player.addListener(this);
        player.addAnalyticsListener(new EventLogger(/* trackSelector= */ trackSelector));
        player.addAnalyticsListener(this);

        listeners = new CopyOnWriteArrayList<>();
    }

    public void setMediaItem(MediaItem mediaItem) {
        player.setMediaItem(mediaItem);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
        player.setVideoSurface(this.surface);
    }

    public Surface getSurface() {
        return surface;
    }

    public void prepare() {
        maybeReportPlayerState();
        player.prepare();
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        player.setPlayWhenReady(playWhenReady);
    }

    public void seekTo(long positionMs) {
        player.seekTo(positionMs);
    }

    public void release() {
        surface = null;
        player.removeListener(this);
        player.removeAnalyticsListener(this);
        player.release();
    }

    public void setRepeatMode(@Player.RepeatMode int repeatMode) {
        player.setRepeatMode(repeatMode);
    }

    public int getPlaybackState() {
        return player.getPlaybackState();
    }

    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public long getDuration() {
        return player.getDuration();
    }

    public int getBufferedPercentage() {
        return player.getBufferedPercentage();
    }

    public boolean getPlayWhenReady() {
        return player.getPlayWhenReady();
    }

    public ITrackInfo[] getTrackInfo() {
        return ExoTrackInfo.fromPlayer(player);
    }

    /* package */ Looper getPlaybackLooper() {
        return player.getPlaybackLooper();
    }

    @Override
    public void onPlaybackStateChanged(int state) {
        String string = "";
        switch (state) {
            case ExoPlayer.STATE_BUFFERING:
                string = "STATE_BUFFERING";
                break;
            case ExoPlayer.STATE_READY:
                string = "STATE_READY";
                break;
            case ExoPlayer.STATE_IDLE:
                string = "STATE_IDLE";
                break;
            case ExoPlayer.STATE_ENDED:
                string = "STATE_ENDED";
                break;
        }
        Log.d(TAG, "onPlaybackStateChanged()--state:" + string);
        maybeReportPlayerState();
    }

    @Override
    public void onPlayerError(ExoPlaybackException exception) {
        exception.printStackTrace();
        for (Listener listener : listeners) {
            listener.onError(exception);
        }
    }

    @Override
    public void onVideoSizeChanged(EventTime eventTime, int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        for (Listener listener : listeners) {
            listener.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio);
        }
    }

    @Override
    public void onRenderedFirstFrame(EventTime eventTime, @Nullable Surface surface) {
        for (Listener listener : listeners) {
            listener.onRenderedFirstFrame();
        }
    }

    private void maybeReportPlayerState() {
        boolean playWhenReady = player.getPlayWhenReady();
        int playbackState = getPlaybackState();
        if (lastReportedPlayWhenReady != playWhenReady || lastReportedPlaybackState != playbackState) {
            for (Listener listener : listeners) {
                listener.onStateChanged(playWhenReady, playbackState);
            }
            lastReportedPlayWhenReady = playWhenReady;
            lastReportedPlaybackState = playbackState;
        }
    }
}
