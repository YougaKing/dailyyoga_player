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

package com.dailyyoga.cn.media;

import android.content.Context;

import androidx.annotation.NonNull;

import static com.dailyyoga.cn.media.IRenderView.AR_ASPECT_FIT_PARENT;

public class PVOptions {

    public static final int PV_PLAYER_ANDROID_MEDIA_PLAYER = 0;
    public static final int PV_PLAYER_IJK_MEDIA_PLAYER = 1;
    public static final int PV_PLAYER_DAILYYOGA_EXO_MEDIA_PLAYER = 2;

    public static final int RENDER_SURFACE_VIEW = 0;
    public static final int RENDER_TEXTURE_VIEW = 1;
    public static final int RENDER_NONE = 2;

    private boolean enableBackgroundPlay;
    private int player = PV_PLAYER_ANDROID_MEDIA_PLAYER;
    private int render = RENDER_SURFACE_VIEW;
    private boolean enableDetachedSurfaceTextureView;
    private int aspectRatio = AR_ASPECT_FIT_PARENT;

    private boolean usingMediaCodec;
    private boolean usingMediaCodecAutoRotate;
    private boolean mediaCodecHandleResolutionChange;
    private boolean usingOpenSLES;
    private String pixelFormat;
    private boolean usingMediaDataSource;
    private String lastDirectory;

    public PVOptions() {
    }

    public boolean isEnableBackgroundPlay() {
        return enableBackgroundPlay;
    }

    public void setEnableBackgroundPlay(boolean enableBackgroundPlay) {
        this.enableBackgroundPlay = enableBackgroundPlay;
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public int getRender() {
        return render;
    }

    public void setRender(int render) {
        this.render = render;
    }

    public int getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(int aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public boolean isUsingMediaCodec() {
        return usingMediaCodec;
    }

    public void setUsingMediaCodec(boolean usingMediaCodec) {
        this.usingMediaCodec = usingMediaCodec;
    }

    public boolean isUsingMediaCodecAutoRotate() {
        return usingMediaCodecAutoRotate;
    }

    public void setUsingMediaCodecAutoRotate(boolean usingMediaCodecAutoRotate) {
        this.usingMediaCodecAutoRotate = usingMediaCodecAutoRotate;
    }

    public boolean isMediaCodecHandleResolutionChange() {
        return mediaCodecHandleResolutionChange;
    }

    public void setMediaCodecHandleResolutionChange(boolean mediaCodecHandleResolutionChange) {
        this.mediaCodecHandleResolutionChange = mediaCodecHandleResolutionChange;
    }

    public boolean isUsingOpenSLES() {
        return usingOpenSLES;
    }

    public void setUsingOpenSLES(boolean usingOpenSLES) {
        this.usingOpenSLES = usingOpenSLES;
    }

    public String getPixelFormat() {
        return pixelFormat;
    }

    public void setPixelFormat(String pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    public boolean isEnableDetachedSurfaceTextureView() {
        return enableDetachedSurfaceTextureView;
    }

    public void setEnableDetachedSurfaceTextureView(boolean enableDetachedSurfaceTextureView) {
        this.enableDetachedSurfaceTextureView = enableDetachedSurfaceTextureView;
    }

    public boolean isUsingMediaDataSource() {
        return usingMediaDataSource;
    }

    public void setUsingMediaDataSource(boolean usingMediaDataSource) {
        this.usingMediaDataSource = usingMediaDataSource;
    }

    public String getLastDirectory() {
        return lastDirectory;
    }

    public void setLastDirectory(String lastDirectory) {
        this.lastDirectory = lastDirectory;
    }

    @NonNull
    public static String getRenderText(Context context, int render) {
        String text;
        switch (render) {
            case RENDER_NONE:
                text = context.getString(R.string.VideoView_render_none);
                break;
            case RENDER_SURFACE_VIEW:
                text = context.getString(R.string.VideoView_render_surface_view);
                break;
            case RENDER_TEXTURE_VIEW:
                text = context.getString(R.string.VideoView_render_texture_view);
                break;
            default:
                text = context.getString(R.string.N_A);
                break;
        }
        return text;
    }

    @NonNull
    public static String getPlayerText(Context context, int player) {
        String text;
        switch (player) {
            case PVOptions.PV_PLAYER_ANDROID_MEDIA_PLAYER:
                text = context.getString(R.string.VideoView_player_AndroidMediaPlayer);
                break;
            case PVOptions.PV_PLAYER_IJK_MEDIA_PLAYER:
                text = context.getString(R.string.VideoView_player_IjkMediaPlayer);
                break;
            case PVOptions.PV_PLAYER_DAILYYOGA_EXO_MEDIA_PLAYER:
                text = context.getString(R.string.VideoView_player_IjkExoMediaPlayer);
                break;
            default:
                text = context.getString(R.string.N_A);
                break;
        }
        return text;
    }
}
