package com.example.a314gm.myvideoplayer.listener;

import android.media.MediaPlayer;

public interface VideoCallback {

    //准备好时回调
    void onPrepared(MediaPlayer mp);
    //缓存更新变化时回调
    void onBufferingUpdate(MediaPlayer mp, int percent);
    //视频size变化时回调
    void onVideoSizeChanged(MediaPlayer mp, int width, int height);
    //播放完成时回调
    void onCompletion(MediaPlayer mp);
    //发生错误时回调
    void onError(MediaPlayer mp, int what, int extra);
    //视频加载状态变化时回调
    void onLoadingChanged(boolean isShow);
    //视频状态变化时回调
     void onStateChanged(int curState);

}
