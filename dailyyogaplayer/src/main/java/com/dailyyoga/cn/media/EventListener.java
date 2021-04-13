package com.dailyyoga.cn.media;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * @author: YougaKingWu@gmail.com
 * @created on: 3/30/21 9:53 AM
 * @description:
 */
public interface EventListener {

    interface OnInfoListener {
        boolean onInfo(IMediaPlayer mediaPlayer, int arg1, int arg2);
    }

    interface OnErrorListener {
        boolean onError(IMediaPlayer mediaPlayer, int frameworkErr, int implErr);
    }

    interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(IMediaPlayer mediaPlayer, int var2, int var3, int var4, int var5);
    }

    interface OnSeekCompleteListener {
        void onSeekComplete(IMediaPlayer mediaPlayer);
    }

    interface OnBufferingUpdateListener {
        void onBufferingUpdate(IMediaPlayer mediaPlayer, int percent);
    }

    interface OnCompletionListener {
        void onCompletion(IMediaPlayer mediaPlayer);
    }

    interface OnPreparedListener {
        void onPrepared(IMediaPlayer mediaPlayer, long time);
    }

}
