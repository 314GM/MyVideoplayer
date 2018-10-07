package com.example.a314gm.myvideoplayer.view;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.a314gm.myvideoplayer.listener.VideoCallback;

import java.io.IOException;

/**
 * 只包含最基础的播放器功能，MediaPlayer可以替换成其他框架的播放器
 */
public class VideoPlayer {

    private static final String TAG = "VideoPlayer";

    public static final int STATE_ERROR = -1;               //错误状态
    public static final int STATE_IDLE = 0;                 //空闲状态
    public static final int STATE_PREPARING = 1;            //准备中状态
    public static final int STATE_PREPARED = 2;             //准备完毕状态
    public static final int STATE_PLAYING = 3;              //播放ing状态
    public static final int STATE_PAUSED = 4;               //暂停状态
    public static final int STATE_PLAYBACK_COMPLETED = 5;   //播放完毕状态

    private MediaPlayer player;
    private int curState = STATE_IDLE;                      //标记状态

    private VideoCallback callback;
    private int currentBufferPercentage;                    //当前缓冲百分比
    private String path;                                    //记录路径
    private SurfaceHolder surfaceHolder;

    //设置视频回调
    public void setCallback(VideoCallback playerCallback) {
        this.callback = playerCallback;
    }

    //错误监听，并修改视频播放器状态
    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.d(TAG, "Error: " + framework_err + "," + impl_err);
            setCurrentState(STATE_ERROR);
            if (callback != null) {
                callback.onError(player, framework_err, impl_err);
            }
            return true;
        }
    };

    //设置视频播放器为空闲状态
    public VideoPlayer() {
        setCurrentState(STATE_IDLE);
    }

    //设置播放器的surfaceHolder
    public void setDisplay(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }

    //设置播放器的播放路径
    public void setVideoPath(String path) {
        this.path = path;
        openVideo();
    }

    //返回播放视频的播放路径
    public String getVideoPath() {
        return path;
    }


    public void openVideo() {
        if (path == null || surfaceHolder == null) {
            //还没有准备好播放，等会再试一次
            return;
        }

        //重置MediaPlayer至未初始化状态。
        reset();

        try {
            player = new MediaPlayer();

            //注册网络流媒体的缓冲变化监听器
            player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    //当前缓冲百分比更新
                    currentBufferPercentage = percent;
                    if (callback != null) callback.onBufferingUpdate(mp, percent);
                }
            });

            //注册网络流媒体播放结束监听器
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    setCurrentState(STATE_PLAYBACK_COMPLETED);
                    if (callback != null) {
                        callback.onCompletion(mp);
                    }
                }
            });

            player.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    setCurrentState(STATE_PLAYING);
                    SystemClock.sleep(200);
                    mp.start();
                }
            });

            //当播放媒体时出现警告信息时，会回调该函数
            player.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (callback != null) {
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                            callback.onLoadingChanged(true);
                        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                            callback.onLoadingChanged(false);
                        }
                    }
                    return false;
                }
            });

            //注册播放器发生错误监听器
            player.setOnErrorListener(mErrorListener);
            //注册播放器大小变化监听器
            player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    if (callback != null) callback.onVideoSizeChanged(mp, width, height);
                }
            });
            //注册播放器缓冲完成监听器
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    setCurrentState(STATE_PREPARED);
                    if (callback != null) {
                        callback.onPrepared(mp);
                    }
                }
            });
            currentBufferPercentage = 0;

            //设置视频路径
            player.setDataSource(path);

            //设置设置显示方式
            player.setDisplay(surfaceHolder);

            //设置音频流的类型
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);

            //设置使用SurfaceHolder来显示
            player.setScreenOnWhilePlaying(true);

            //准备播放异步音频
            player.prepareAsync();

            //设置状态为准备中
            setCurrentState(STATE_PREPARING);
        } catch (IOException | IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + path, ex);
            setCurrentState(STATE_ERROR);
            mErrorListener.onError(player, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    //开始播放
    public void start() {
        Log.i("DDD", "start");
        if (isInPlaybackState()) {
            player.start();
            setCurrentState(STATE_PLAYING);
        }
    }

    //重启播放器
    public void restart() {
        Log.i("DDD", "restart");
        openVideo();
    }

    //暂停
    public void pause() {
        if (isInPlaybackState()) {
            if (player.isPlaying()) {
                player.pause();
                setCurrentState(STATE_PAUSED);
            }
        }
    }

    //重置MediaPlayer至未初始化状态。
    public void reset() {
        if (player != null) {
            player.reset();                 //重置MediaPlayer至未初始化状态。
            player.release();               //回收流媒体资源。
            setCurrentState(STATE_IDLE);    //更改播放器状态为空闲
        }
    }

    //设置状态
    private void setCurrentState(int state) {
        curState = state;
        if (callback != null) {
            callback.onStateChanged(curState);
            switch (state) {
                case STATE_IDLE:
                case STATE_ERROR:
                case STATE_PREPARED:
                    callback.onLoadingChanged(false);
                    break;
                case STATE_PREPARING:
                    callback.onLoadingChanged(true);
                    break;
            }
        }
    }

    //停止播放
    public void stop() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
            surfaceHolder = null;
            setCurrentState(STATE_IDLE);
        }
    }

    //获取视频的时长
    public int getDuration() {
        if (isInPlaybackState()) {
            return player.getDuration();
        }
        return -1;
    }

    //获取当前播放的位置
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    //跳转
    public void seekTo(int progress) {
        if (isInPlaybackState()) {
            player.seekTo(progress);
        }
    }

    public void seekTo2(int progress) {
        if (player != null) {
            if (progress <= player.getDuration()) {
                player.seekTo(progress);
            } else {
                player.seekTo(player.getDuration());
            }
        }
    }



    //是否播放
    public boolean isPlaying() {
        return isInPlaybackState() && player.isPlaying();
    }

    //获得缓冲百分比
    public int getBufferPercentage() {
        if (player != null) {
            return currentBufferPercentage;
        }
        return 0;
    }

    //意外情况
    public boolean isInPlaybackState() {
        return (player != null &&
                curState != STATE_ERROR &&
                curState != STATE_IDLE &&
                curState != STATE_PREPARING);
    }

}
