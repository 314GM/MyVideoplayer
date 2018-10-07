package com.example.a314gm.myvideoplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Switch;

import com.example.a314gm.myvideoplayer.listener.VideoCallback;
import com.example.a314gm.myvideoplayer.listener.VideoControlListener;
import com.example.a314gm.myvideoplayer.utils.NetworkUtils;
import com.example.a314gm.myvideoplayer.videoInfo.VideoInfo;
import com.example.a314gm.myvideoplayer.view.BrightnessVolumeView;
import com.example.a314gm.myvideoplayer.view.ControllerView;
import com.example.a314gm.myvideoplayer.view.GestureView;
import com.example.a314gm.myvideoplayer.view.ProgressView;
import com.example.a314gm.myvideoplayer.view.VideoPlayer;

public class VideoPlayView extends GestureView {

    private SurfaceView mSurfaceView;                       //播放荧幕
    private View mLoading;                                  //loading图标
    private ControllerView mControllerView;                 //播放控制视图
    private BrightnessVolumeView mBrightnessVolumeView;     //亮度和音量调整视图
    private ProgressView mProgressView;                     //进度条视图
    private VideoPlayer mVideoPlayer;                       //播放器
    private Context mContext;

    private boolean isBackgroundPause;                      //后台暂停
    private int initWidth;                                  //视频宽高
    private int initHeight;

    public VideoPlayView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoPlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoPlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    //更新控制UI
    @Override
    public void updateUI(int type, int x, int max) {

        switch(type){
            case GestureView.GESTURE_ACTION_PROGRESS:
                mProgressView.show(x, mVideoPlayer.getCurrentPosition(), mVideoPlayer.getDuration());
                break;
            case GestureView.GESTURE_ACTION_VOLUME:
                mBrightnessVolumeView.show(mBrightnessVolumeView.VOLUME_SETTINGS, x, max);
                break;
            case GestureView.GESTURE_ACTION_BRIGHTNESS:
                mBrightnessVolumeView.show(mBrightnessVolumeView.BRIGHTNESS_SETTINGS, x, max);
                break;
        }
    }

    //重写父类的endgesture结束手势动作
    @Override
    public void endGesture(int type) {
        switch (type) {
            case GestureView.GESTURE_ACTION_BRIGHTNESS:
            case GestureView.GESTURE_ACTION_VOLUME:
                Log.i("DDD", "endGesture: left right");
                mBrightnessVolumeView.hide();
                break;
            case GestureView.GESTURE_ACTION_PROGRESS:
                Log.i("DDD", "endGesture: bottom");
                mVideoPlayer.seekTo(mProgressView.getTargetProgress());
                mProgressView.hide();
                break;
        }
    }

    //初始化控件
    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.layout_video_view, this);
        mSurfaceView = findViewById(R.id.video_surface);
        mLoading = findViewById(R.id.video_loading);
        mControllerView = findViewById(R.id.video_controller);
        mBrightnessVolumeView = findViewById(R.id.video_system_overlay);
        mProgressView = findViewById(R.id.video_progress_overlay);

        initPlayer();

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initWidth = getWidth();
                initHeight = getHeight();

                if (mVideoPlayer != null) {
                    mVideoPlayer.setDisplay(holder);
                    mVideoPlayer.openVideo();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        registerNetChangedReceiver();
    }

    //初始化播放器
    private void initPlayer() {
        mVideoPlayer = new VideoPlayer();
        //注册监听回调
        mVideoPlayer.setCallback(new VideoCallback() {

            @Override
            public void onStateChanged(int curState) {
                switch (curState) {
                    case VideoPlayer.STATE_IDLE:
                        //放弃音频焦点
                        mAudioManager.abandonAudioFocus(null);
                        break;
                    case VideoPlayer.STATE_PREPARING:
                        //请求长时间音频焦点
                        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                        break;
                }
            }

            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height){

            }

            @Override
            public void onCompletion(MediaPlayer mp) {
                mControllerView.updatePausePlay();
            }

            @Override
            public void onError(MediaPlayer mp, int what, int extra) {
                mControllerView.checkShowError(false);
            }

            @Override
            public void onLoadingChanged(boolean isShow) {
                if (isShow) showLoading();
                else hideLoading();
            }

            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoPlayer.start();
                mControllerView.show();
                mControllerView.hideErrorView();
            }

            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {

            }
        });
        //给ControllerView注入灵魂
        mControllerView.setMediaPlayer(mVideoPlayer);
    }

    //显示/隐藏loadingView
    private void showLoading() {
        mLoading.setVisibility(VISIBLE);
    }

    private void hideLoading() {
        mLoading.setVisibility(GONE);
    }

    //停止播放
    public void onStop() {
        if (mVideoPlayer.isPlaying()) {
            // 如果已经开始且在播放，则暂停同时记录状态
            isBackgroundPause = true;
            mVideoPlayer.pause();
        }
    }

    //从停止->开始播放
    public void onStart() {
        if (isBackgroundPause) {
            // 如果切换到后台暂停，后又切回来，则继续播放
            isBackgroundPause = false;
            mVideoPlayer.start();
        }
    }

    //销毁
    public void onDestroy() {
        mVideoPlayer.stop();
        mControllerView.release();
        unRegisterNetChangedReceiver();
    }

    //开始播放
    public void startPlayVideo(final VideoInfo video) {
        if (video == null) {
            return;
        }

        mVideoPlayer.reset();

        String videoPath = video.getVideoPath();
        mControllerView.setVideoInfo(video);
        mVideoPlayer.setVideoPath(videoPath);
    }

    //重写GestureDetector.OnGestureListener的单击，按下，滑动方法
    //轻击抬起
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        mControllerView.toggleDisplay();
        return super.onSingleTapUp(e);
    }

    //按下屏幕
    @Override
    public boolean onDown(MotionEvent e) {
        if (isLock()) {
            return false;
        }
        return super.onDown(e);
    }

    //滑动
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (isLock()) {
            return false;
        }
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    public boolean isLock() {
        return mControllerView.isLock();
    }

    //暴露给用户注册控制监听
    public void setOnVideoControlListener(VideoControlListener onVideoControlListener) {
        mControllerView.setOnVideoControlListener(onVideoControlListener);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //切换屏幕方向
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getLayoutParams().width = initWidth;
            getLayoutParams().height = initHeight;
        } else {
            getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
            getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
        }

    }

    private NetChangedReceiver netChangedReceiver;

    //注册网络状态变更广播接收器
    public void registerNetChangedReceiver() {
        if (netChangedReceiver == null) {
            netChangedReceiver = new NetChangedReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            mActivity.registerReceiver(netChangedReceiver, filter);
        }
    }

    //注销网络状态变更广播接收器
    public void unRegisterNetChangedReceiver() {
        if (netChangedReceiver != null) {
            mActivity.unregisterReceiver(netChangedReceiver);
        }
    }

    //网络连接广播接收器
    private class NetChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Parcelable extra = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (extra != null && extra instanceof NetworkInfo) {
                NetworkInfo netInfo = (NetworkInfo) extra;

                if (NetworkUtils.isNetworkConnected(context) && netInfo.getState() != NetworkInfo.State.CONNECTED) {
                    // 网络连接的情况下只处理连接完成状态
                    return;
                }
                mControllerView.checkShowError(true);
            }
        }
    }
}
