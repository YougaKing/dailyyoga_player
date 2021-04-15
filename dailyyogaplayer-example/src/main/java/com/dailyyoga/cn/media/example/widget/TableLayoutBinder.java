/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
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

package com.dailyyoga.cn.media.example.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.dailyyoga.cn.media.IMediaPlayer;
import com.dailyyoga.cn.media.example.R;
import com.dailyyoga.cn.media.misc.IMediaFormat;
import com.dailyyoga.cn.media.misc.ITrackInfo;

import java.util.Locale;


public class TableLayoutBinder {
    private Context mContext;
    public ViewGroup mTableView;
    public TableLayout mTableLayout;

    public TableLayoutBinder(Context context) {
        this(context, R.layout.table_media_info);
    }

    public TableLayoutBinder(Context context, int layoutResourceId) {
        mContext = context;
        mTableView = (ViewGroup) LayoutInflater.from(mContext).inflate(layoutResourceId, null);
        mTableLayout = (TableLayout) mTableView.findViewById(R.id.table);
    }

    public TableLayoutBinder(Context context, TableLayout tableLayout) {
        mContext = context;
        mTableView = tableLayout;
        mTableLayout = tableLayout;
    }

    public View appendRow1(String name, String value) {
        return appendRow(R.layout.table_media_info_row1, name, value);
    }

    public View appendRow1(int nameId, String value) {
        return appendRow1(mContext.getString(nameId), value);
    }

    public View appendRow2(String name, String value) {
        return appendRow(R.layout.table_media_info_row2, name, value);
    }

    public View appendRow2(int nameId, String value) {
        return appendRow2(mContext.getString(nameId), value);
    }

    public View appendSection(String name) {
        return appendRow(R.layout.table_media_info_section, name, null);
    }

    public View appendSection(int nameId) {
        return appendSection(mContext.getString(nameId));
    }

    public View appendRow(int layoutId, String name, String value) {
        ViewGroup rowView = (ViewGroup) LayoutInflater.from(mContext).inflate(layoutId, mTableLayout, false);
        setNameValueText(rowView, name, value);

        mTableLayout.addView(rowView);
        return rowView;
    }

    public ViewHolder obtainViewHolder(View rowView) {
        ViewHolder viewHolder = (ViewHolder) rowView.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.mNameTextView = (TextView) rowView.findViewById(R.id.name);
            viewHolder.mValueTextView = (TextView) rowView.findViewById(R.id.value);
            rowView.setTag(viewHolder);
        }
        return viewHolder;
    }

    public void setNameValueText(View rowView, String name, String value) {
        ViewHolder viewHolder = obtainViewHolder(rowView);
        viewHolder.setName(name);
        viewHolder.setValue(value);
    }

    public void setValueText(View rowView, String value) {
        ViewHolder viewHolder = obtainViewHolder(rowView);
        viewHolder.setValue(value);
    }

    public ViewGroup buildLayout() {
        return mTableView;
    }

    public AlertDialog.Builder buildAlertDialogBuilder() {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(mContext);
        dlgBuilder.setView(buildLayout());
        return dlgBuilder;
    }

    private static class ViewHolder {
        public TextView mNameTextView;
        public TextView mValueTextView;

        public void setName(String name) {
            if (mNameTextView != null) {
                mNameTextView.setText(name);
            }
        }

        public void setValue(String value) {
            if (mValueTextView != null) {
                mValueTextView.setText(value);
            }
        }
    }

    public static void showMediaInfo(IMediaPlayer iMediaPlayer, Context context) {
        if (iMediaPlayer == null)
            return;

        int selectedVideoTrack = MediaPlayerCompat.getSelectedTrack(iMediaPlayer, ITrackInfo.MEDIA_TRACK_TYPE_VIDEO);
        int selectedAudioTrack = MediaPlayerCompat.getSelectedTrack(iMediaPlayer, ITrackInfo.MEDIA_TRACK_TYPE_AUDIO);
        int selectedSubtitleTrack = MediaPlayerCompat.getSelectedTrack(iMediaPlayer, ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT);

        TableLayoutBinder builder = new TableLayoutBinder(context);
        builder.appendSection(R.string.mi_player);
        builder.appendRow2(R.string.mi_player, MediaPlayerCompat.getName(iMediaPlayer));
        builder.appendSection(R.string.mi_media);
        builder.appendRow2(R.string.mi_resolution, buildResolution(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight(), iMediaPlayer.getVideoSarNum(), iMediaPlayer.getVideoSarDen()));
        builder.appendRow2(R.string.mi_length, buildTimeMilli(iMediaPlayer.getDuration()));

        ITrackInfo trackInfos[] = iMediaPlayer.getTrackInfo();
        if (trackInfos != null) {
            int index = -1;
            for (ITrackInfo trackInfo : trackInfos) {
                index++;

                int trackType = trackInfo.getTrackType();
                if (index == selectedVideoTrack) {
                    builder.appendSection(context.getString(R.string.mi_stream_fmt1, index) + " " + context.getString(R.string.mi__selected_video_track));
                } else if (index == selectedAudioTrack) {
                    builder.appendSection(context.getString(R.string.mi_stream_fmt1, index) + " " + context.getString(R.string.mi__selected_audio_track));
                } else if (index == selectedSubtitleTrack) {
                    builder.appendSection(context.getString(R.string.mi_stream_fmt1, index) + " " + context.getString(R.string.mi__selected_subtitle_track));
                } else {
                    builder.appendSection(context.getString(R.string.mi_stream_fmt1, index));
                }
                builder.appendRow2(R.string.mi_type, buildTrackType(trackType, context));
                builder.appendRow2(R.string.mi_language, buildLanguage(trackInfo.getLanguage()));

                IMediaFormat mediaFormat = trackInfo.getFormat();
                if (mediaFormat == null) return;
                switch (trackType) {
                    case ITrackInfo.MEDIA_TRACK_TYPE_VIDEO:
                        builder.appendRow2(R.string.mi_codec, mediaFormat.getString(IMediaFormat.KEY_IJK_CODEC_LONG_NAME_UI));
                        builder.appendRow2(R.string.mi_profile_level, mediaFormat.getString(IMediaFormat.KEY_IJK_CODEC_PROFILE_LEVEL_UI));
                        builder.appendRow2(R.string.mi_pixel_format, mediaFormat.getString(IMediaFormat.KEY_IJK_CODEC_PIXEL_FORMAT_UI));
                        builder.appendRow2(R.string.mi_resolution, mediaFormat.getString(IMediaFormat.KEY_IJK_RESOLUTION_UI));
                        builder.appendRow2(R.string.mi_frame_rate, mediaFormat.getString(IMediaFormat.KEY_IJK_FRAME_RATE_UI));
                        builder.appendRow2(R.string.mi_bit_rate, mediaFormat.getString(IMediaFormat.KEY_IJK_BIT_RATE_UI));
                        break;
                    case ITrackInfo.MEDIA_TRACK_TYPE_AUDIO:
                        builder.appendRow2(R.string.mi_codec, mediaFormat.getString(IMediaFormat.KEY_IJK_CODEC_LONG_NAME_UI));
                        builder.appendRow2(R.string.mi_profile_level, mediaFormat.getString(IMediaFormat.KEY_IJK_CODEC_PROFILE_LEVEL_UI));
                        builder.appendRow2(R.string.mi_sample_rate, mediaFormat.getString(IMediaFormat.KEY_IJK_SAMPLE_RATE_UI));
                        builder.appendRow2(R.string.mi_channels, mediaFormat.getString(IMediaFormat.KEY_IJK_CHANNEL_UI));
                        builder.appendRow2(R.string.mi_bit_rate, mediaFormat.getString(IMediaFormat.KEY_IJK_BIT_RATE_UI));
                        break;
                    default:
                        break;
                }
            }
        }

        AlertDialog.Builder adBuilder = builder.buildAlertDialogBuilder();
        adBuilder.setTitle(R.string.media_information);
        adBuilder.setNegativeButton(R.string.close, null);
        adBuilder.show();
    }

    private static String buildResolution(int width, int height, int sarNum, int sarDen) {
        StringBuilder sb = new StringBuilder();
        sb.append(width);
        sb.append(" x ");
        sb.append(height);

        if (sarNum > 1 || sarDen > 1) {
            sb.append("[");
            sb.append(sarNum);
            sb.append(":");
            sb.append(sarDen);
            sb.append("]");
        }

        return sb.toString();
    }

    private static String buildTimeMilli(long duration) {
        long total_seconds = duration / 1000;
        long hours = total_seconds / 3600;
        long minutes = (total_seconds % 3600) / 60;
        long seconds = total_seconds % 60;
        if (duration <= 0) {
            return "--:--";
        }
        if (hours >= 100) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }

    private static String buildTrackType(int type, Context context) {
        switch (type) {
            case ITrackInfo.MEDIA_TRACK_TYPE_VIDEO:
                return context.getString(R.string.TrackType_video);
            case ITrackInfo.MEDIA_TRACK_TYPE_AUDIO:
                return context.getString(R.string.TrackType_audio);
            case ITrackInfo.MEDIA_TRACK_TYPE_SUBTITLE:
                return context.getString(R.string.TrackType_subtitle);
            case ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT:
                return context.getString(R.string.TrackType_timedtext);
            case ITrackInfo.MEDIA_TRACK_TYPE_METADATA:
                return context.getString(R.string.TrackType_metadata);
            case ITrackInfo.MEDIA_TRACK_TYPE_UNKNOWN:
            default:
                return context.getString(R.string.TrackType_unknown);
        }
    }

    private static String buildLanguage(String language) {
        if (TextUtils.isEmpty(language))
            return "und";
        return language;
    }
}
