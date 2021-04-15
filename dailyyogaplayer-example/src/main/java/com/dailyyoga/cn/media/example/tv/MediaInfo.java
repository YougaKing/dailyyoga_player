package com.dailyyoga.cn.media.example.tv;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * @author: YougaKingWu@gmail.com
 * @created on: 4/15/21 2:02 PM
 * @description:
 */
public class MediaInfo implements Serializable {

    public String videoPath;
    public String coverUrl;
    public boolean controller;
    public long currentPosition;
    public boolean hadBgm;
    public boolean rebootPlayer;

    public MediaInfo(String videoPath, String coverUrl) {
        this.videoPath = videoPath;
        this.coverUrl = coverUrl;
    }

    public boolean unavailable(){
        return TextUtils.isEmpty(videoPath);
    }
}
