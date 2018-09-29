package com.example.a314gm.myvideoplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.a314gm.myvideoplayer.videoInfo.VideoInfo;

public class MainActivity extends AppCompatActivity {
    private VideoPlayView mVideoPlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVideoPlayView.findViewById(R.id.VideoPlayView);
        mVideoPlayView.startPlayVideo(new VideoInfo(){

            private String VideoTitle = "震惊！Android只用这样学习，工资上20k!";
            private String VideoPath = "";


            @Override
            public String getVideoTitle() {
                return VideoTitle;
            }

            @Override
            public String getVideoPath() {
                return VideoPath;
            }
        });
    }
}
