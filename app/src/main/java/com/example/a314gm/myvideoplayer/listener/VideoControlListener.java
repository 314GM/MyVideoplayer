package com.example.a314gm.myvideoplayer.listener;

public interface VideoControlListener {

    //返回
    void onBack();

    //全屏
    void onFullScreen();

    //错误后的重试
    void onRetry(int errorStatus);

}
