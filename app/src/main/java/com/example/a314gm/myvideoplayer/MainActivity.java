package com.example.a314gm.myvideoplayer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.example.a314gm.myvideoplayer.listener.VideoControlListener;
import com.example.a314gm.myvideoplayer.utils.DisplayUtils;
import com.example.a314gm.myvideoplayer.videoInfo.VideoInfo;

public class MainActivity extends AppCompatActivity {
    private VideoPlayView mVideoPlayView;

    public static void start(Context context, VideoInfo info) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("info", info);
        context.startActivity(intent);
    }

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
        VideoInfo info = (VideoInfo) getIntent().getSerializableExtra("info");
        Log.i("DDD info：",info.getVideoPath()+" "+ info.getVideoTitle());
        mVideoPlayView.startPlayVideo(info);
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
