package com.example.a314gm.myvideoplayer.videoInfo;

import java.io.Serializable;

public interface VideoInfo extends Serializable {

    //视频信息 视频信息类继承这个接口

    //返回标题
    String getVideoTitle();

    //返回路径
    String getVideoPath();
}
