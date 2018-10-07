package com.example.a314gm.myvideoplayer.videoInfo;

public class VideoDataInfo implements VideoInfo {

    public String VideoTitle;
    public String VideoPath;

    public VideoDataInfo(String VideoTitle, String VideoPath){
        this.VideoPath = VideoPath;
        this.VideoTitle = VideoTitle;
    }

    @Override
    public String getVideoTitle() {
        return VideoTitle;
    }

    @Override
    public String getVideoPath() {
        return VideoPath;
    }
}
