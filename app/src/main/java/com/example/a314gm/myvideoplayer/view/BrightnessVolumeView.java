package com.example.a314gm.myvideoplayer.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.a314gm.myvideoplayer.R;

public class BrightnessVolumeView extends FrameLayout {

    public static final int BRIGHTNESS_SETTINGS = 1;
    public static final int VOLUME_SETTINGS = 2;

    private TextView mTitle;
    private ImageView mImage;
    private ProgressBar mProgressBar;

    private Context mContext;

    public BrightnessVolumeView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public BrightnessVolumeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BrightnessVolumeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        mContext = context;

        LayoutInflater.from(mContext).inflate(R.layout.layout_brightness_volume_view, this);

        mTitle = findViewById(R.id.system_ui_title);
        mImage = findViewById(R.id.system_ui_image);
        mProgressBar =  findViewById(R.id.system_ui_seek_bar);

        hide();
    }


    public void show(int type,int progress,int max) {
        if (type == BRIGHTNESS_SETTINGS) {
            mTitle.setText("亮度");
            mImage.setImageResource(R.mipmap.ic_brightness);
        } else if (type == VOLUME_SETTINGS) {
            mTitle.setText("音量");
            mImage.setImageResource(progress == 0
                    ? R.mipmap.ic_volume_down
                    : R.mipmap.ic_volume_up);
        }
        mProgressBar.setMax(max);
        mProgressBar.setProgress(progress);
        setVisibility(VISIBLE);
    }

    public void hide() {
        setVisibility(GONE);
    }
}
