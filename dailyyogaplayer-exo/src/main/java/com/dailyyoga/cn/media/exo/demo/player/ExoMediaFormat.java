package com.dailyyoga.cn.media.exo.demo.player;

import com.dailyyoga.cn.media.misc.IMediaFormat;
import com.google.android.exoplayer2.Format;


/**
 * @author: YougaKingWu@gmail.com
 * @created on: 4/9/21 4:34 PM
 * @description:
 */
public class ExoMediaFormat implements IMediaFormat {

    private Format mFormat;

    public ExoMediaFormat(Format format) {
        mFormat = format;
    }

    @Override
    public String getString(String s) {
        if (mFormat == null) return null;
        switch (s) {
            case KEY_IJK_CODEC_LONG_NAME_UI:
                return mFormat.codecs;
            default:
            case KEY_IJK_CODEC_PROFILE_LEVEL_UI:
            case KEY_IJK_CODEC_PIXEL_FORMAT_UI:
                return "N/A";
            case KEY_IJK_RESOLUTION_UI:
                return mFormat.width + "x" + mFormat.height;
            case KEY_IJK_FRAME_RATE_UI:
                return String.valueOf(mFormat.frameRate);
            case KEY_IJK_BIT_RATE_UI:
                return mFormat.bitrate + "kb/s";
            case KEY_IJK_SAMPLE_RATE_UI:
                return mFormat.sampleRate + "Hz";
            case KEY_IJK_CHANNEL_UI:
                return String.valueOf(mFormat.channelCount);
        }
    }

    @Override
    public int getInteger(String s) {
        return 0;
    }
}
