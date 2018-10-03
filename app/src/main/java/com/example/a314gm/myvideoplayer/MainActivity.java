package com.example.a314gm.myvideoplayer;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.a314gm.myvideoplayer.listener.VideoControlListener;
import com.example.a314gm.myvideoplayer.utils.DisplayUtils;
import com.example.a314gm.myvideoplayer.videoInfo.VideoInfo;

public class MainActivity extends AppCompatActivity {
    private VideoPlayView mVideoPlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVideoPlayView = findViewById(R.id.video_play_view);
        mVideoPlayView.setOnVideoControlListener(new VideoControlListener() {
            @Override
            public void onBack() {

            }

            @Override
            public void onFullScreen() {
                DisplayUtils.toggleScreenOrientation(MainActivity.this);
            }

            @Override
            public void onRetry(int errorStatus) {

            }
        });
        mVideoPlayView.startPlayVideo(new VideoInfo(){

            private String VideoTitle = "震惊！Android只用这样学习，工资上20k!";
            private String VideoPath = "http://10.10.4.200:8080/examples/%E5%89%91%E7%BD%913%E9%87%8D%E5%88%B6%E7%89%88%20-%20%E5%BF%B5%E5%A5%B4%E5%A8%87(%E7%A0%B4%E9%98%B5%E5%AD%90)%20@%20%E5%8F%8C%E7%BA%BF%E4%B8%80%E5%8C%BA%202018_3_28%2016_41_56.mp4";


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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        mVideoPlayView.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mVideoPlayView.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mVideoPlayView.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!DisplayUtils.isPortrait(this)) {
            if(!mVideoPlayView.isLock()) {
                DisplayUtils.toggleScreenOrientation(this);
            }
        } else {
            super.onBackPressed();
        }
    }
}
