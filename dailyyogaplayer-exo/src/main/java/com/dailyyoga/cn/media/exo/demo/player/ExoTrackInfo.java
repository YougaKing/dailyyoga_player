package com.dailyyoga.cn.media.exo.demo.player;

import com.dailyyoga.cn.media.misc.IMediaFormat;
import com.dailyyoga.cn.media.misc.ITrackInfo;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;


/**
 * @author: YougaKingWu@gmail.com
 * @created on: 4/9/21 3:56 PM
 * @description:
 */
public class ExoTrackInfo implements ITrackInfo {

    public static ITrackInfo[] fromPlayer(SimpleExoPlayer player) {
        return fromTrackGroupArray(player.getCurrentTrackGroups());
    }

    private static ITrackInfo[] fromTrackGroupArray(TrackGroupArray trackGroupArray) {
        if (trackGroupArray == null) {
            return null;
        } else {
            ExoTrackInfo[] exoTrackInfos = new ExoTrackInfo[trackGroupArray.length];
            for (int i = 0; i < trackGroupArray.length; ++i) {
                exoTrackInfos[i] = new ExoTrackInfo(trackGroupArray.get(i));
            }
            return exoTrackInfos;
        }
    }

    private Format mFormat;

    public ExoTrackInfo(TrackGroup trackGroup) {
        if (trackGroup.length == 0) return;
        this.mFormat = trackGroup.getFormat(0);
    }

    @Override
    public IMediaFormat getFormat() {
        return this.mFormat == null ? null : new ExoMediaFormat(this.mFormat);
    }

    @Override
    public String getLanguage() {
        return this.mFormat == null ? "und" : mFormat.language;
    }

    @Override
    public int getTrackType() {
        if (mFormat == null || mFormat.sampleMimeType == null) return 0;
        if (mFormat.sampleMimeType.contains("video/")) {
            return ITrackInfo.MEDIA_TRACK_TYPE_VIDEO;
        } else if (mFormat.sampleMimeType.contains("audio/")) {
            return ITrackInfo.MEDIA_TRACK_TYPE_AUDIO;
        }
        return 0;
    }

    @Override
    public String getInfoInline() {
        return "null";
    }
}
