package com.example.a314gm.myvideoplayer.view;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a314gm.myvideoplayer.R;
import com.example.a314gm.myvideoplayer.listener.VideoControlListener;
import com.example.a314gm.myvideoplayer.utils.DisplayUtils;
import com.example.a314gm.myvideoplayer.utils.NetworkUtils;
import com.example.a314gm.myvideoplayer.utils.StringUtils;
import com.example.a314gm.myvideoplayer.videoInfo.VideoInfo;

public class ControllerView extends FrameLayout {


    public static final int DEFAULT_SHOW_TIME = 3000;       //默认的展示时间

    private View mControllerBack;                           //顶部返回按钮
    private View mControllerTitle;                          //顶部布局
    private TextView mVideoTitle;                           //视频标题
    private View mControllerBottom;                         //底部布局
    private SeekBar mPlayerSeekBar;                         //进度条
    private ImageView mVideoPlayState;                      //播放/暂停按钮
    private TextView mVideoProgress;                        //进度条当前播放时间
    private TextView mVideoDuration;                        //进度条总时间
    private ImageView mVideoFullScreen;                     //全屏按钮
    private ImageView mScreenLock;                          //锁屏按钮图片
    private ErrorView mErrorView;                           //错误显示View

    private boolean isScreenLock;                           //是否锁屏屏幕
    private boolean mShowing;                               //是否为显示状态
    private boolean mAllowUnWifiPlay;                       //是否允许在没有WiFi的情况下播放
    private boolean mDragging;                              //进度条是否在被拖拽
    private long mDraggingProgress;                         //进度条当前播放时间
    private VideoPlayer mPlayer;                            //播放器
    private VideoInfo videoInfo;                            //视频数据
    private VideoControlListener onVideoControlListener;    //控制监听
    private Context mContext;


    public ControllerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ControllerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ControllerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.layout_controller_view, this);
        // 返回按钮
        mControllerBack = findViewById(R.id.video_back);
        mControllerBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onVideoControlListener != null) {
                    onVideoControlListener.onBack();
                }
            }
        });
        // 顶部
        mControllerTitle = findViewById(R.id.video_controller_title);
        mVideoTitle = mControllerTitle.findViewById(R.id.video_title);
        // 底部
        mControllerBottom = findViewById(R.id.video_controller_bottom);
        mPlayerSeekBar = mControllerBottom.findViewById(R.id.player_seek_bar);
        mVideoPlayState = mControllerBottom.findViewById(R.id.player_pause);
        mVideoProgress = mControllerBottom.findViewById(R.id.player_progress);
        mVideoDuration = mControllerBottom.findViewById(R.id.player_duration);
        mVideoFullScreen = mControllerBottom.findViewById(R.id.video_full_screen);
        mVideoFullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onVideoControlListener.onFullScreen();
            }
        });
        //给播放按钮添加点击事件，点击暂停，再点击播放
        mVideoPlayState.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doPauseResume();
            }
        });
        //先设置一个播放按钮
        mVideoPlayState.setImageResource(R.drawable.ic_play);

        //给进度条添加事件监听
        mPlayerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            //用户触摸手势已经开始
            @Override
            public void onStartTrackingTouch(SeekBar bar) {
                show(3600000);

                mDragging = true;

                removeCallbacks(mShowProgress);
            }

            //当进度被改变
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                //判断是否是用户触发的
                if (!fromuser) {
                    return;
                }

                long duration = mPlayer.getDuration();
                mDraggingProgress = (duration * progress) / 1000L;

                if (mVideoProgress != null) {
                    mVideoProgress.setText(StringUtils.stringForTime((int) mDraggingProgress));
                }
            }

            //用户触摸手势已经结束
            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                mPlayer.seekTo((int) mDraggingProgress);
                play();
                mDragging = false;
                mDraggingProgress = 0;

                post(mShowProgress);
            }
        });
        // 锁屏按钮
        mScreenLock = findViewById(R.id.player_lock_screen);
        mScreenLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScreenLock) unlock();
                else lock();
                show();
            }
        });

        //错误提示
        mErrorView = findViewById(R.id.video_controller_error);
        mErrorView.setOnVideoControlListener(new VideoControlListener() {
            @Override
            public void onBack() {

            }

            @Override
            public void onFullScreen() {

            }

            @Override
            public void onRetry(int errorStatus) {
                retry(errorStatus);
            }
        });

        mPlayerSeekBar.setMax(1000);

    }

    //设置控制监听
    public void setOnVideoControlListener(VideoControlListener onVideoControlListener) {
        this.onVideoControlListener = onVideoControlListener;
    }
    //设置播放器
    public void setMediaPlayer(VideoPlayer player) {
        mPlayer = player;
        updatePausePlay();
    }
    //设置视频信息
    public void setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
        mVideoTitle.setText(videoInfo.getVideoTitle());
    }

    //切换显示
    public void toggleDisplay() {
        if (mShowing) {
            hide();
        } else {
            show();
        }
    }

    public void show() {
        show(DEFAULT_SHOW_TIME);
    }

    //控件显示
    public void show(int timeout) {
        setProgress();
        //如果没有锁定，全部设置可见
        if (!isScreenLock) {
            mControllerBack.setVisibility(VISIBLE);
            mControllerTitle.setVisibility(VISIBLE);
            mControllerBottom.setVisibility(VISIBLE);
        } else {
            //锁定了屏幕
            if (!DisplayUtils.isPortrait(mContext)) {
                //如果是横屏，返回按钮设置不可见
                mControllerBack.setVisibility(GONE);
            }
            mControllerTitle.setVisibility(GONE);
            mControllerBottom.setVisibility(GONE);
        }

        if (!DisplayUtils.isPortrait(mContext)) {
            //如果是横屏，锁屏按钮可见
            mScreenLock.setVisibility(VISIBLE);
        }

        mShowing = true;

        updatePausePlay();

        //将mShowProgress线程加入到消息队列中，该runnable将会在线程中执行
        post(mShowProgress);

        //延迟timeout毫秒更新UI，让控件隐藏
        if (timeout > 0) {
            //如果上一个延迟线程还没执行，就取消
            removeCallbacks(mFadeOut);
            postDelayed(mFadeOut, timeout);
        }
    }

    private void hide() {
        if (!mShowing) {
            return;
        }

        if (!DisplayUtils.isPortrait(mContext)) {
            // 横屏才消失
            mControllerBack.setVisibility(GONE);
        }
        mControllerTitle.setVisibility(GONE);
        mControllerBottom.setVisibility(GONE);
        mScreenLock.setVisibility(GONE);

        //如果上一个更新进度条的线程还没执行，就取消更新
        removeCallbacks(mShowProgress);

        mShowing = false;
    }

    //用于视频播放按钮自动隐藏的线程
    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    //用于视频播放进度条和播放计更新的线程
    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && mShowing && mPlayer.isPlaying()) {
                //延迟1000- (pos % 1000)毫秒更新进度条
                postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    //设置进度
    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mPlayerSeekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mPlayerSeekBar.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mPlayerSeekBar.setSecondaryProgress(percent * 10);
        }

        mVideoProgress.setText(StringUtils.stringForTime(position));
        mVideoDuration.setText(StringUtils.stringForTime(duration));

        return position;
    }

    //检查错误
    public void checkShowError(boolean isNetChanged) {
        boolean isConnect = NetworkUtils.isNetworkConnected(mContext);
        boolean isMobileNet = NetworkUtils.isMobileConnected(mContext);
        boolean isWifiNet = NetworkUtils.isWifiConnected(mContext);

        if (isConnect) {
            // 如果已经联网
            if (mErrorView.getCurStatus() == ErrorView.STATUS_NO_NETWORK_ERROR && !(isMobileNet && !isWifiNet)) {
                // 如果之前是无网络
            } else if (videoInfo == null) {
                // 优先判断是否有video数据
                showError(ErrorView.STATUS_VIDEO_DETAIL_ERROR);
            } else if (isMobileNet && !isWifiNet && !mAllowUnWifiPlay) {
                // 如果是手机流量，且未同意过播放，且非本地视频，则提示错误
                mErrorView.showError(ErrorView.STATUS_UN_WIFI_ERROR);
                mPlayer.pause();
            } else if (isWifiNet && isNetChanged && mErrorView.getCurStatus() == ErrorView.STATUS_UN_WIFI_ERROR) {
                // 如果是wifi流量，且之前是非wifi错误，则恢复播放
                playFromUnWifiError();
            } else if (!isNetChanged) {
                showError(ErrorView.STATUS_VIDEO_SRC_ERROR);
            }
        } else {
            mPlayer.pause();
            showError(ErrorView.STATUS_NO_NETWORK_ERROR);
        }
    }

    //显示错误
    private void showError(int status) {
        mErrorView.showError(status);
        hide();
        // 如果提示了错误，则看需要解锁
        if (isScreenLock) {
            unlock();
        }
    }
    //隐藏错误
    public void hideErrorView() {
        mErrorView.hideError();
    }

    //重新加载
    private void reload() {
        mPlayer.restart();
    }

    //释放取消线程
    public void release() {
        removeCallbacks(mShowProgress);
        removeCallbacks(mFadeOut);
    }

    //重试
    private void retry(int status) {
        Log.i("DDD", "retry " + status);

        switch (status) {
            case ErrorView.STATUS_VIDEO_DETAIL_ERROR:
                // 传递给activity
                if (onVideoControlListener != null) {
                    onVideoControlListener.onRetry(status);
                }
                break;
            case ErrorView.STATUS_VIDEO_SRC_ERROR:
                reload();
                break;
            case ErrorView.STATUS_UN_WIFI_ERROR:
                allowUnWifiPlay();
                break;
            case ErrorView.STATUS_NO_NETWORK_ERROR:
                // 无网络时
                if (NetworkUtils.isNetworkConnected(mContext)) {
                    if (videoInfo == null) {
                        // 如果video为空，重新请求详情
                        retry(ErrorView.STATUS_VIDEO_DETAIL_ERROR);
                    } else if (mPlayer.isInPlaybackState()) {
                        // 如果有video，可以直接播放的直接恢复
                        mPlayer.start();
                    } else {
                        // 视频未准备好，重新加载
                        reload();
                    }
                } else {
                    Toast.makeText(mContext, "网络未连接", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //确定是否为锁屏状态
    public boolean isLock() {
        return isScreenLock;
    }

    //锁屏
    private void lock() {
        Log.i("DDD", "lock");
        isScreenLock = true;
        mScreenLock.setImageResource(R.drawable.ic_lock);
    }

    //解锁
    private void unlock() {
        Log.i("DDD", "unlock");
        isScreenLock = false;
        mScreenLock.setImageResource(R.drawable.ic_lock_open);
    }

    //允许非WiFi播放
    private void allowUnWifiPlay() {
        Log.i("DDD", "allowUnWifiPlay");

        mAllowUnWifiPlay = true;

        playFromUnWifiError();
    }


    private void playFromUnWifiError() {
        Log.i("DDD", "playFromUnWifiError");

        if (mPlayer.isInPlaybackState()) {
            mPlayer.start();
        } else {
            mPlayer.restart();
        }
    }

    //根据是否在播放更改播放按钮
    public void updatePausePlay() {
        if (mPlayer.isPlaying()) {
            mVideoPlayState.setImageResource(R.drawable.ic_pause);
        } else {
            mVideoPlayState.setImageResource(R.drawable.ic_play);
        }
    }

    //暂停/恢复
    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    //暂停
    private void pause() {
        mPlayer.pause();
        updatePausePlay();
        removeCallbacks(mFadeOut);
    }

    //播放
    private void play() {
        mPlayer.start();
        show();
    }

    //屏幕方向改变触发
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggleVideoLayoutParams();
    }

    void toggleVideoLayoutParams() {
        //防止意外发生，锁定isPortrait
        final boolean isPortrait = DisplayUtils.isPortrait(mContext);
        //竖屏显示返回按钮和全屏按钮
        if (isPortrait) {
            mControllerBack.setVisibility(VISIBLE);
            mVideoFullScreen.setVisibility(VISIBLE);
            mScreenLock.setVisibility(GONE);
        } else {
            //横屏不显示全屏按钮
            mVideoFullScreen.setVisibility(GONE);
            if (mShowing) {
                mScreenLock.setVisibility(VISIBLE);
            }
        }
    }

}
