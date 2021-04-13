package com.dailyyoga.cn.media.android;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import tv.danmaku.ijk.media.player.AbstractMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;
import tv.danmaku.ijk.media.player.MediaInfo;
import tv.danmaku.ijk.media.player.misc.AndroidTrackInfo;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;
import tv.danmaku.ijk.media.player.pragma.DebugLog;

/**
 * @author: YougaKingWu@gmail.com
 * @created on: 4/8/21 1:46 PM
 * @description:
 */
public class AndroidMediaPlayer extends AbstractMediaPlayer {

    private final MediaPlayer mInternalMediaPlayer;
    private final AndroidMediaPlayerListenerHolder mInternalListenerAdapter;
    private String mDataSource;
    private MediaDataSource mMediaDataSource;
    private final Object mInitLock = new Object();
    private boolean mIsReleased;
    private static MediaInfo sMediaInfo;

    public AndroidMediaPlayer() {
        synchronized (this.mInitLock) {
            this.mInternalMediaPlayer = new MediaPlayer();
        }

        this.mInternalMediaPlayer.setAudioStreamType(3);
        this.mInternalListenerAdapter = new AndroidMediaPlayerListenerHolder(this);
        this.attachInternalListeners();
    }

    public MediaPlayer getInternalMediaPlayer() {
        return this.mInternalMediaPlayer;
    }

    @Override
    public void setDisplay(SurfaceHolder sh) {
        synchronized (this.mInitLock) {
            if (!this.mIsReleased) {
                this.mInternalMediaPlayer.setDisplay(sh);
            }

        }
    }

    @TargetApi(14)
    @Override
    public void setSurface(Surface surface) {
        this.mInternalMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        this.mInternalMediaPlayer.setDataSource(context, uri);
    }

    @TargetApi(14)
    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        this.mInternalMediaPlayer.setDataSource(context, uri, headers);
    }

    @Override
    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        this.mInternalMediaPlayer.setDataSource(fd);
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        this.mDataSource = path;
        Uri uri = Uri.parse(path);
        String scheme = uri.getScheme();
        if (!TextUtils.isEmpty(scheme) && scheme.equalsIgnoreCase("file")) {
            this.mInternalMediaPlayer.setDataSource(uri.getPath());
        } else {
            this.mInternalMediaPlayer.setDataSource(path);
        }

    }

    @TargetApi(23)
    @Override
    public void setDataSource(IMediaDataSource mediaDataSource) {
        this.releaseMediaDataSource();
        this.mMediaDataSource = new MediaDataSourceProxy(mediaDataSource);
        this.mInternalMediaPlayer.setDataSource(this.mMediaDataSource);
    }

    @Override
    public String getDataSource() {
        return this.mDataSource;
    }

    private void releaseMediaDataSource() {
        if (this.mMediaDataSource != null) {
            try {
                this.mMediaDataSource.close();
            } catch (IOException var2) {
                var2.printStackTrace();
            }

            this.mMediaDataSource = null;
        }
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        this.mInternalMediaPlayer.prepareAsync();
    }

    @Override
    public void start() throws IllegalStateException {
        this.mInternalMediaPlayer.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        this.mInternalMediaPlayer.stop();
    }

    @Override
    public void pause() throws IllegalStateException {
        this.mInternalMediaPlayer.pause();
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        this.mInternalMediaPlayer.setScreenOnWhilePlaying(screenOn);
    }

    @Override
    public ITrackInfo[] getTrackInfo() {
        try {
            return AndroidTrackInfo.fromMediaPlayer(this.mInternalMediaPlayer);
        } catch (Throwable t) {
            t.fillInStackTrace();
            return null;
        }
    }

    @Override
    public int getVideoWidth() {
        return this.mInternalMediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return this.mInternalMediaPlayer.getVideoHeight();
    }

    @Override
    public int getVideoSarNum() {
        return 1;
    }

    @Override
    public int getVideoSarDen() {
        return 1;
    }

    @Override
    public boolean isPlaying() {
        try {
            return this.mInternalMediaPlayer.isPlaying();
        } catch (IllegalStateException var2) {
            DebugLog.printStackTrace(var2);
            return false;
        }
    }

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        this.mInternalMediaPlayer.seekTo((int) msec);
    }

    @Override
    public long getCurrentPosition() {
        try {
            return (long) this.mInternalMediaPlayer.getCurrentPosition();
        } catch (IllegalStateException var2) {
            DebugLog.printStackTrace(var2);
            return 0L;
        }
    }

    @Override
    public long getDuration() {
        try {
            return (long) this.mInternalMediaPlayer.getDuration();
        } catch (IllegalStateException var2) {
            DebugLog.printStackTrace(var2);
            return 0L;
        }
    }

    @Override
    public void release() {
        this.mIsReleased = true;
        this.mInternalMediaPlayer.release();
        this.releaseMediaDataSource();
        this.resetListeners();
        this.attachInternalListeners();
    }

    @Override
    public void reset() {
        try {
            this.mInternalMediaPlayer.reset();
        } catch (IllegalStateException var2) {
            DebugLog.printStackTrace(var2);
        }

        this.releaseMediaDataSource();
        this.resetListeners();
        this.attachInternalListeners();
    }

    @Override
    public void setLooping(boolean looping) {
        this.mInternalMediaPlayer.setLooping(looping);
    }

    @Override
    public boolean isLooping() {
        return this.mInternalMediaPlayer.isLooping();
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        this.mInternalMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public int getAudioSessionId() {
        return this.mInternalMediaPlayer.getAudioSessionId();
    }

    @Override
    public MediaInfo getMediaInfo() {
        if (sMediaInfo == null) {
            MediaInfo module = new MediaInfo();
            module.mVideoDecoder = "android";
            module.mVideoDecoderImpl = "HW";
            module.mAudioDecoder = "android";
            module.mAudioDecoderImpl = "HW";
            sMediaInfo = module;
        }
        return sMediaInfo;
    }

    @Override
    public void setLogEnabled(boolean enable) {
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    @Override
    public void setWakeMode(Context context, int mode) {
        this.mInternalMediaPlayer.setWakeMode(context, mode);
    }

    @Override
    public void setAudioStreamType(int streamtype) {
        this.mInternalMediaPlayer.setAudioStreamType(streamtype);
    }

    @Override
    public void setKeepInBackground(boolean keepInBackground) {
    }

    private void attachInternalListeners() {
        this.mInternalMediaPlayer.setOnPreparedListener(this.mInternalListenerAdapter);
        this.mInternalMediaPlayer.setOnBufferingUpdateListener(this.mInternalListenerAdapter);
        this.mInternalMediaPlayer.setOnCompletionListener(this.mInternalListenerAdapter);
        this.mInternalMediaPlayer.setOnSeekCompleteListener(this.mInternalListenerAdapter);
        this.mInternalMediaPlayer.setOnVideoSizeChangedListener(this.mInternalListenerAdapter);
        this.mInternalMediaPlayer.setOnErrorListener(this.mInternalListenerAdapter);
        this.mInternalMediaPlayer.setOnInfoListener(this.mInternalListenerAdapter);
        this.mInternalMediaPlayer.setOnTimedTextListener(this.mInternalListenerAdapter);
    }

    private class AndroidMediaPlayerListenerHolder implements android.media.MediaPlayer.OnPreparedListener,
            android.media.MediaPlayer.OnCompletionListener,
            android.media.MediaPlayer.OnBufferingUpdateListener,
            android.media.MediaPlayer.OnSeekCompleteListener,
            android.media.MediaPlayer.OnVideoSizeChangedListener,
            android.media.MediaPlayer.OnErrorListener,
            android.media.MediaPlayer.OnInfoListener,
            android.media.MediaPlayer.OnTimedTextListener {
        public final WeakReference<AndroidMediaPlayer> mWeakMediaPlayer;

        public AndroidMediaPlayerListenerHolder(AndroidMediaPlayer mp) {
            this.mWeakMediaPlayer = new WeakReference<>(mp);
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            AndroidMediaPlayer self = (AndroidMediaPlayer) this.mWeakMediaPlayer.get();
            return self != null && AndroidMediaPlayer.this.notifyOnInfo(what, extra);
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            AndroidMediaPlayer self = (AndroidMediaPlayer) this.mWeakMediaPlayer.get();
            return self != null && AndroidMediaPlayer.this.notifyOnError(what, extra);
        }

        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            AndroidMediaPlayer self = (AndroidMediaPlayer) this.mWeakMediaPlayer.get();
            if (self != null) {
                AndroidMediaPlayer.this.notifyOnVideoSizeChanged(width, height, 1, 1);
            }
        }

        @Override
        public void onSeekComplete(MediaPlayer mp) {
            AndroidMediaPlayer self = (AndroidMediaPlayer) this.mWeakMediaPlayer.get();
            if (self != null) {
                AndroidMediaPlayer.this.notifyOnSeekComplete();
            }
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            AndroidMediaPlayer self = (AndroidMediaPlayer) this.mWeakMediaPlayer.get();
            if (self != null) {
                AndroidMediaPlayer.this.notifyOnBufferingUpdate(percent);
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            AndroidMediaPlayer self = (AndroidMediaPlayer) this.mWeakMediaPlayer.get();
            if (self != null) {
                AndroidMediaPlayer.this.notifyOnCompletion();
            }
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            AndroidMediaPlayer self = (AndroidMediaPlayer) this.mWeakMediaPlayer.get();
            if (self != null) {
                AndroidMediaPlayer.this.notifyOnPrepared();
            }
        }

        @Override
        public void onTimedText(MediaPlayer mp, TimedText text) {
            AndroidMediaPlayer self = (AndroidMediaPlayer) this.mWeakMediaPlayer.get();
            if (self != null) {
                IjkTimedText ijkText = null;
                if (text != null) {
                    ijkText = new IjkTimedText(text.getBounds(), text.getText());
                }
                AndroidMediaPlayer.this.notifyOnTimedText(ijkText);
            }
        }
    }

    @TargetApi(23)
    private static class MediaDataSourceProxy extends MediaDataSource {
        private final IMediaDataSource mMediaDataSource;

        public MediaDataSourceProxy(IMediaDataSource mediaDataSource) {
            this.mMediaDataSource = mediaDataSource;
        }

        @Override
        public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
            return this.mMediaDataSource.readAt(position, buffer, offset, size);
        }

        @Override
        public long getSize() throws IOException {
            return this.mMediaDataSource.getSize();
        }

        @Override
        public void close() throws IOException {
            this.mMediaDataSource.close();
        }
    }
}
