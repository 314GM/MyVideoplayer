package com.example.a314gm.myvideoplayer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.a314gm.myvideoplayer.videoInfo.VideoDataInfo;
import com.example.a314gm.myvideoplayer.videoInfo.VideoInfo;

public class Main2Activity extends AppCompatActivity {

    private Button playButton;
    private Button playButton2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        playButton = findViewById(R.id.video_play_button);
        playButton2 = findViewById(R.id.video_play_button2);
        final Intent intent = new Intent(Main2Activity.this,MainActivity.class);
        final VideoDataInfo info = new VideoDataInfo("震惊！Android只用这样学习，工资上20k!"
                ,"https://media.w3.org/2010/05/sintel/trailer.mp4");
        final VideoDataInfo info2 = new VideoDataInfo("超酷 ONE！Aimer！"
                ,"http://221.228.226.5/14/z/w/y/y/zwyyobhyqvmwslabxyoaixvyubmekc/sh.yinyuetai.com/4599015ED06F94848EBF877EAAE13886.mp4");

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("info", info);
                startActivity(intent);
            }
        });
        playButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("info", info2);
                startActivity(intent);
            }
        });
    }
}
