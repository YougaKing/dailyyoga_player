package com.dailyyoga.cn.media.example.tv;
/*
 * Copyright (C) 2006 The Android Open Source Project
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

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.dailyyoga.cn.media.IMediaController;
import com.dailyyoga.cn.media.example.R;
import com.dailyyoga.cn.media.example.RxScheduler;
import com.dailyyoga.cn.media.example.databinding.MediaControllerBinding;

import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * A view containing controls for a MediaPlayer. Typically contains the
 * buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress
 * slider. It takes care of synchronizing the controls with the state
 * of the MediaPlayer.
 * <p>
 * The way to use this class is to instantiate it programmatically.
 * The MediaController will create a default set of controls
 * and put them in a window floating above your application. Specifically,
 * the controls will float above the view specified with setAnchorView().
 * The window will disappear if left idle for three seconds and reappear
 * when the user touches the anchor view.
 * <p>
 * Functions like show() and hide() have no effect when MediaController
 * is created in an xml layout.
 * <p>
 * MediaController will hide and
 * show the buttons according to these rules:
 * <ul>
 * <li> The "previous" and "next" buttons are hidden until setPrevNextListeners()
 *   has been called
 * <li> The "previous" and "next" buttons are visible but disabled if
 *   setPrevNextListeners() was called with null listeners
 * <li> The "rewind" and "fastforward" buttons are shown unless requested
 *   otherwise by using the MediaController(Context, boolean) constructor
 *   with the boolean set to false
 * </ul>
 */
public class AndroidMediaController extends ConstraintLayout implements IMediaController {

    private static final String TAG = "AndroidMediaController";
    private final PopupWindow mWindow;
    private final int mAnimStyle;
    private final MediaControllerBinding mBinding;
    private MediaPlayerControl mPlayer;
    private View mAnchor;
    private boolean mShowing;
    private boolean mDragging;
    private static final int sDefaultTimeout = 3000;
    private final static int sPreSeek = 2;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private final PublishSubject<Integer> mLifecycleSubject = PublishSubject.create();
    private final PublishSubject<Integer> mKeyDpadSeekSubject = PublishSubject.create();
    private IMediaControllerListener mIMediaControllerListener;
    private int mDuration;
    private int mCurrentPosition;

    public AndroidMediaController(Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.media_controller, this, true);
        mBinding = MediaControllerBinding.bind(view);
        initControllerView();

        mWindow = new PopupWindow(context);
        mWindow.setBackgroundDrawable(null);
        mWindow.setTouchable(true);
        mWindow.setFocusable(true);
        mWindow.setOutsideTouchable(true);
        mAnimStyle = android.R.style.Animation;

        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        requestFocus();

        mKeyDpadSeekSubject.buffer(500, TimeUnit.MILLISECONDS)
                .compose(RxScheduler.applySchedulers())
                .takeUntil(mLifecycleSubject)
                .filter(integers -> !integers.isEmpty())
                .map(integers -> {
                    int count = 0;
                    for (int i : integers) {
                        count += i;
                    }
                    return count;
                })
                .subscribe(this::onKeyDpadSeek, Throwable::printStackTrace).isDisposed();

        Observable.timer(500, TimeUnit.MILLISECONDS)
                .compose(RxScheduler.applySchedulers())
                .takeUntil(mLifecycleSubject)
                .subscribe(integers -> {
                    if (mPlayer == null) return;
                    mCurrentPosition = mPlayer.getCurrentPosition();
                }, Throwable::printStackTrace).isDisposed();
    }

    // Update the dynamic parts of mDecorLayoutParams
    // Must be called with mAnchor != NULL.
    private void updateFloatingWindowLayout() {
        if (mAnchor == null) return;
        int[] location = new int[2];
        mAnchor.getLocationOnScreen(location);
        Rect anchorRect = new Rect(location[0], location[1],
                location[0] + mAnchor.getWidth(), location[1]
                + mAnchor.getHeight());

        mWindow.setAnimationStyle(mAnimStyle);
        mWindow.showAtLocation(mAnchor, Gravity.BOTTOM,
                anchorRect.left, 0);
    }

    // This is called whenever mAnchor's layout bound changes
    private final View.OnLayoutChangeListener mLayoutChangeListener =
            new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight,
                                           int oldBottom) {
                    if (mShowing) {
                        updateFloatingWindowLayout();
                    }
                }
            };

    @Override
    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     * When VideoView calls this method, it will use the VideoView's parent
     * as the anchor.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */
    @Override
    public void setAnchorView(View view) {
        if (mAnchor != null) {
            mAnchor.removeOnLayoutChangeListener(mLayoutChangeListener);
        }
        mAnchor = view;
        if (mAnchor != null) {
            mAnchor.addOnLayoutChangeListener(mLayoutChangeListener);
        }

        mWindow.setContentView(mBinding.getRoot());
        mWindow.setWidth(LayoutParams.MATCH_PARENT);
        mWindow.setHeight(LayoutParams.MATCH_PARENT);
    }

    private void initControllerView() {
        mBinding.ivPlay.setOnClickListener(mPauseListener);
        mBinding.seekBar.setOnSeekBarChangeListener(mSeekListener);
        mBinding.seekBar.setMax(1000);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    @Override
    public boolean isAndroidTV() {
        return true;
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    @Override
    public void show() {
        show(sDefaultTimeout);
    }

    @Override
    public void showOnce(View view) {
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        try {
            if (!mPlayer.canPause()) {
                mBinding.ivPlay.setEnabled(false);
            }
            // TODO What we really should do is add a canSeek to the MediaPlayerControl interface;
            // this scheme can break the case when applications want to allow seek through the
            // progress bar but disable forward/backward buttons.
            //
            // However, currently the flags SEEK_BACKWARD_AVAILABLE, SEEK_FORWARD_AVAILABLE,
            // and SEEK_AVAILABLE are all (un)set together; as such the aforementioned issue
            // shouldn't arise in existing applications.
            if (!mPlayer.canSeekBackward() && !mPlayer.canSeekForward()) {
                mBinding.seekBar.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    @Override
    public void show(int timeout) {
        if (!isFocusable()) return;
        boolean isPlaying = mPlayer != null && mPlayer.isPlaying();
        if (mIMediaControllerListener != null) {
            mIMediaControllerListener.showMediaController(isPlaying);
        }
        if (!mShowing && mAnchor != null) {
            setProgress();
            mBinding.ivPlay.requestFocus();
            disableUnsupportedButtons();
            updateFloatingWindowLayout();
            mShowing = true;
        }
        updatePausePlay();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        post(mShowProgress);

        if (timeout != 0) {
            removeCallbacks(mFadeOut);
            postDelayed(mFadeOut, timeout);
        }
    }

    @Override
    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    @Override
    public void hide() {
        Log.d(TAG, "hide()");
        if (mIMediaControllerListener != null) mIMediaControllerListener.hideMediaController();
        if (mAnchor == null)
            return;
        if (mShowing) {
            try {
                removeCallbacks(mShowProgress);
                mWindow.dismiss();
            } catch (IllegalArgumentException ex) {
                Log.w(TAG, "already removed");
            }
            mShowing = false;
        }
    }

    private final Runnable mFadeOut = this::hide;

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && mShowing && mPlayer.isPlaying()) {
                postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    private final Runnable mStopTracking = new Runnable() {
        @Override
        public void run() {
            mSeekListener.onStopTrackingTouch(mBinding.seekBar);
        }
    };

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        mDuration = mPlayer.getDuration();
        if (mDuration > 0) {
            // use long to avoid overflow
            long pos = 1000L * position / mDuration;
            mBinding.seekBar.setProgress((int) pos);
        }
        int percent = mPlayer.getBufferPercentage();
        mBinding.seekBar.setSecondaryProgress(percent * 10);

        mBinding.tvTotalTime.setText(stringForTime(mDuration));
        mBinding.tvCurrentTime.setText(stringForTime(position));

        return position;
    }

    public long getCurrentPosition() {
        if (mPlayer == null) return 0L;
        long seekProgress = mBinding.seekBar.getProgress() * mDuration / 1000L;
        long currentPosition = Math.max(seekProgress, mCurrentPosition);
        return Math.max(currentPosition, mPlayer.getCurrentPosition());
    }

    public int getDuration() {
        return mDuration;
    }

    public void stopPlayback() {
        mLifecycleSubject.onNext(0);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!isFocusable()) {
            hide();
        }
        final boolean uniqueDown = event.getAction() == KeyEvent.ACTION_DOWN;
        if (!uniqueDown) return super.dispatchKeyEvent(event);
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (mPlayer != null && mPlayer.canSeekBackward()) {
                    mKeyDpadSeekSubject.onNext(-sPreSeek);
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (mPlayer != null && mPlayer.canSeekForward()) {
                    mKeyDpadSeekSubject.onNext(sPreSeek);
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (mIMediaControllerListener != null) {
                    mIMediaControllerListener.showSwitchUi();
                }
                return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void onKeyDpadSeek(int seek) {
        if (mDuration < 0) return;
        mSeekListener.onStartTrackingTouch(mBinding.seekBar);
        int progress = mBinding.seekBar.getProgress() + seek;
        Log.i(TAG, "onKeyDpadSeek()--progress:" + progress);
        mBinding.seekBar.setProgress(progress);
        mSeekListener.onProgressChanged(mBinding.seekBar, progress, true);
        removeCallbacks(mStopTracking);
        postDelayed(mStopTracking, sDefaultTimeout);
    }

    private final View.OnClickListener mPauseListener = v -> {
        doPauseResume();
        show(sDefaultTimeout);
    };

    private void updatePausePlay() {
        if (mPlayer.isPlaying()) {
            mBinding.ivPlay.setImageResource(R.drawable.media_pause);
        } else {
            mBinding.ivPlay.setImageResource(R.drawable.media_play);
        }
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlay();
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            removeCallbacks(mShowProgress);
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }
            long newPosition = (mDuration * progress) / 1000L;
            Log.w(TAG, "onProgressChanged()--progress:" + mPlayer.getCurrentPosition() + "--" + newPosition);
            mPlayer.seekTo((int) newPosition);
            mBinding.tvTotalTime.setText(stringForTime((int) newPosition));
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            post(mShowProgress);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        mBinding.ivPlay.setEnabled(enabled);
        mBinding.seekBar.setEnabled(enabled);
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    public void setIMediaControllerListener(IMediaControllerListener IMediaControllerListener) {
        mIMediaControllerListener = IMediaControllerListener;
    }

    @Override
    public void setFocusable(boolean focusable) {
        super.setFocusable(focusable);
        Log.d(TAG, "focusable:" + focusable);
    }

    public interface IMediaControllerListener {

        void showMediaController(boolean isPlaying);

        void hideMediaController();

        void showSwitchUi();
    }
}

