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

package com.dailyyoga.cn.media.example.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.dailyyoga.cn.media.DailyyogaVideoView;
import com.dailyyoga.cn.media.MeasureHelper;
import com.dailyyoga.cn.media.PVOptions;
import com.dailyyoga.cn.media.example.R;
import com.dailyyoga.cn.media.example.content.RecentMediaStorage;
import com.dailyyoga.cn.media.example.fragments.TracksFragment;
import com.dailyyoga.cn.media.example.widget.AndroidMediaController;
import com.dailyyoga.cn.media.example.widget.InfoHudViewHolder;
import com.dailyyoga.cn.media.example.widget.MediaPlayerCompat;
import com.dailyyoga.cn.media.example.widget.TableLayoutBinder;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;

import static com.dailyyoga.cn.media.PVOptions.PV_PLAYER_ANDROID_MEDIA_PLAYER;
import static com.dailyyoga.cn.media.PVOptions.PV_PLAYER_DAILYYOGA_EXO_MEDIA_PLAYER;
import static com.dailyyoga.cn.media.PVOptions.PV_PLAYER_IJK_MEDIA_PLAYER;
import static com.dailyyoga.cn.media.PVOptions.RENDER_NONE;
import static com.dailyyoga.cn.media.PVOptions.RENDER_SURFACE_VIEW;
import static com.dailyyoga.cn.media.PVOptions.RENDER_TEXTURE_VIEW;

public class VideoActivity extends AppCompatActivity implements TracksFragment.ITrackHolder {
    private static final String TAG = "VideoActivity";

    private String mVideoPath;
    private Uri    mVideoUri;

    private AndroidMediaController mMediaController;
    private DailyyogaVideoView mVideoView;
    private TextView mToastTextView;
    private TableLayout mHudView;
    private DrawerLayout mDrawerLayout;
    private ViewGroup mRightDrawer;

    private boolean mBackPressed;
    private InfoHudViewHolder mHudViewHolder;

    public static Intent newIntent(Context context, String videoPath, String videoTitle) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra("videoPath", videoPath);
        intent.putExtra("videoTitle", videoTitle);
        return intent;
    }

    public static void intentTo(Context context, String videoPath, String videoTitle) {
        context.startActivity(newIntent(context, videoPath, videoTitle));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        // handle arguments
        mVideoPath = getIntent().getStringExtra("videoPath");

        Intent intent = getIntent();
        String intentAction = intent.getAction();
        if (!TextUtils.isEmpty(intentAction)) {
            if (intentAction.equals(Intent.ACTION_VIEW)) {
                mVideoPath = intent.getDataString();
            } else if (intentAction.equals(Intent.ACTION_SEND)) {
                mVideoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    String scheme = mVideoUri.getScheme();
                    if (TextUtils.isEmpty(scheme)) {
                        Log.e(TAG, "Null unknown scheme\n");
                        finish();
                        return;
                    }
                    if (scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                        mVideoPath = mVideoUri.getPath();
                    } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                        Log.e(TAG, "Can not resolve content below Android-ICS\n");
                        finish();
                        return;
                    } else {
                        Log.e(TAG, "Unknown scheme " + scheme + "\n");
                        finish();
                        return;
                    }
                }
            }
        }

        if (!TextUtils.isEmpty(mVideoPath)) {
            new RecentMediaStorage(this).saveUrlAsync(mVideoPath);
        }

        // init UI
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        mMediaController = new AndroidMediaController(this, false);
        mMediaController.setSupportActionBar(actionBar);

        mToastTextView = (TextView) findViewById(R.id.toast_text_view);
        mHudView = (TableLayout) findViewById(R.id.hud_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mRightDrawer = (ViewGroup) findViewById(R.id.right_drawer);

        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        mVideoView = (DailyyogaVideoView) findViewById(R.id.video_view);
        mVideoView.setPVOptions(pvOptions(this));
        mVideoView.setMediaController(mMediaController);
        setHudView(mHudView);

        String playerText = PVOptions.getPlayerText(this, mVideoView.getPVOptions().getPlayer());
        getSupportActionBar().setTitle(playerText);
        // prefer mVideoPath
        if (mVideoPath != null)
            mVideoView.setVideoPath(mVideoPath);
        else if (mVideoUri != null)
            mVideoView.setVideoURI(mVideoUri);
        else {
            Log.e(TAG, "Null Data Source\n");
            finish();
            return;
        }
        mVideoView.start();

        mVideoView.setOnErrorListener((mediaPlayer, frameworkErr, implErr) -> {
            /* Otherwise, pop up an error dialog so the user knows that
             * something bad has happened. Only try and pop up the dialog
             * if we're attached to a window. When we're going away and no
             * longer have a window, don't bother showing the user an error.
             */
            int messageId;
            if (frameworkErr == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                messageId = com.dailyyoga.cn.media.R.string.VideoView_error_text_invalid_progressive_playback;
            } else {
                messageId = com.dailyyoga.cn.media.R.string.VideoView_error_text_unknown;
            }
            new AlertDialog.Builder(VideoActivity.this)
                    .setMessage(messageId)
                    .setPositiveButton(com.dailyyoga.cn.media.R.string.VideoView_error_button,
                            (dialog, whichButton) -> {
                                /* If we get here, there is no onError listener, so
                                 * at least inform them that the video is over.
                                 */

                            })
                    .setCancelable(false)
                    .show();
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        mBackPressed = true;

        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBackPressed || !mVideoView.isBackgroundPlayEnabled()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        } else {
            mVideoView.enterBackground();
        }
        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_toggle_ratio) {
            int aspectRatio = mVideoView.toggleAspectRatio();
            String aspectRatioText = MeasureHelper.getAspectRatioText(this, aspectRatio);
            mToastTextView.setText(aspectRatioText);
            mMediaController.showOnce(mToastTextView);
            return true;
        } else if (id == R.id.action_toggle_player) {
//            int player = mVideoView.getPVOptions().getPlayer();
//            switch (player) {
//                case PV_PLAYER_ANDROID_MEDIA_PLAYER:
//                    player = PV_PLAYER_IJK_MEDIA_PLAYER;
//                    break;
//                case PV_PLAYER_IJK_MEDIA_PLAYER:
//                    player = PV_PLAYER_DAILYYOGA_EXO_MEDIA_PLAYER;
//                    break;
//                case PV_PLAYER_DAILYYOGA_EXO_MEDIA_PLAYER:
//                    player = PV_PLAYER_ANDROID_MEDIA_PLAYER;
//                    break;
//            }
//            mVideoView.getPVOptions().setPlayer(player);
//            player = mVideoView.togglePlayer();
//            String playerText = PVOptions.getPlayerText(this, player);
//            getSupportActionBar().setTitle(playerText);
//            mToastTextView.setText(playerText);
//            mMediaController.showOnce(mToastTextView);
            return true;
        } else if (id == R.id.action_toggle_render) {
            int render = mVideoView.getPVOptions().getPlayer();
            switch (render) {
                case RENDER_SURFACE_VIEW:
                    render = RENDER_TEXTURE_VIEW;
                    break;
                case RENDER_TEXTURE_VIEW:
                    render = RENDER_NONE;
                    break;
                case RENDER_NONE:
                    render = RENDER_SURFACE_VIEW;
                    break;
            }
            mVideoView.getPVOptions().setRender(render);
            render = mVideoView.toggleRender();
            String renderText = PVOptions.getRenderText(this, render);
            mToastTextView.setText(renderText);
            mMediaController.showOnce(mToastTextView);
            return true;
        } else if (id == R.id.action_show_info) {
            TableLayoutBinder.showMediaInfo(mVideoView.getMediaPlayer(), this);
        } else if (id == R.id.action_show_tracks) {
            if (mDrawerLayout.isDrawerOpen(mRightDrawer)) {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.right_drawer);
                if (f != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.remove(f);
                    transaction.commit();
                }
                mDrawerLayout.closeDrawer(mRightDrawer);
            } else {
                Fragment f = TracksFragment.newInstance();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.right_drawer, f);
                transaction.commit();
                mDrawerLayout.openDrawer(mRightDrawer);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public ITrackInfo[] getTrackInfo() {
        if (mVideoView == null)
            return null;

        return mVideoView.getTrackInfo();
    }

    @Override
    public void selectTrack(int stream) {
        if (mVideoView == null)
            return;
        MediaPlayerCompat.selectTrack(mVideoView.getMediaPlayer(), stream);
    }

    @Override
    public void deselectTrack(int stream) {
        if (mVideoView == null)
            return;
        MediaPlayerCompat.deselectTrack(mVideoView.getMediaPlayer(), stream);
    }

    @Override
    public int getSelectedTrack(int trackType) {
        if (mVideoView == null)
            return -1;
        return MediaPlayerCompat.getSelectedTrack(mVideoView.getMediaPlayer(), trackType);
    }

    public void setHudView(TableLayout tableLayout) {
        mHudViewHolder = new InfoHudViewHolder(this, tableLayout);
        mVideoView.setOnPreparedListener((iMediaPlayer, time) -> {
            mHudViewHolder.setMediaPlayer(iMediaPlayer);
            mHudViewHolder.updateLoadCost(time);
        });
    }

    public static PVOptions pvOptions(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        PVOptions pvOptions = new PVOptions();

        String key = context.getString(R.string.pref_key_enable_background_play);
        boolean enableBackgroundPlay = sharedPreferences.getBoolean(key, false);
        pvOptions.setEnableBackgroundPlay(enableBackgroundPlay);

        key = context.getString(R.string.pref_key_player);
        String value = sharedPreferences.getString(key, "");
        try {
            int player = Integer.parseInt(value);
            pvOptions.setPlayer(player);
        } catch (NumberFormatException e) {
        }

        key = context.getString(R.string.pref_key_render);
        value = sharedPreferences.getString(key, "");
        try {
            int render = Integer.parseInt(value);
            pvOptions.setRender(render);
        } catch (NumberFormatException e) {
        }

        key = context.getString(R.string.pref_key_using_media_codec);
        boolean usingMediaCodec = sharedPreferences.getBoolean(key, false);
        pvOptions.setUsingMediaCodec(usingMediaCodec);

        key = context.getString(R.string.pref_key_using_media_codec_auto_rotate);
        boolean usingMediaCodecAutoRotate = sharedPreferences.getBoolean(key, false);
        pvOptions.setUsingMediaCodecAutoRotate(usingMediaCodecAutoRotate);

        key = context.getString(R.string.pref_key_media_codec_handle_resolution_change);
        boolean mediaCodecHandleResolutionChange = sharedPreferences.getBoolean(key, false);
        pvOptions.setMediaCodecHandleResolutionChange(mediaCodecHandleResolutionChange);

        key =context.getString(R.string.pref_key_using_opensl_es);
        boolean usingOpenSLES = sharedPreferences.getBoolean(key, false);
        pvOptions.setUsingOpenSLES(usingOpenSLES);

        key =context.getString(R.string.pref_key_pixel_format);
        String pixelFormat = sharedPreferences.getString(key, "");
        pvOptions.setPixelFormat(pixelFormat);

        key =context.getString(R.string.pref_key_enable_detached_surface_texture);
        boolean enableDetachedSurfaceTextureView = sharedPreferences.getBoolean(key, false);
        pvOptions.setEnableDetachedSurfaceTextureView(enableDetachedSurfaceTextureView);

        key =context.getString(R.string.pref_key_using_mediadatasource);
        boolean usingMediaDataSource = sharedPreferences.getBoolean(key, false);
        pvOptions.setUsingMediaDataSource(usingMediaDataSource);

        key =context.getString(R.string.pref_key_last_directory);
        String lastDirectory = sharedPreferences.getString(key, "/");
        pvOptions.setLastDirectory(lastDirectory);
        return pvOptions;
    }

}
