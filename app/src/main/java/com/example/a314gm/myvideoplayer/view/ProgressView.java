package com.example.a314gm.myvideoplayer.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a314gm.myvideoplayer.R;
import com.example.a314gm.myvideoplayer.utils.StringUtils;

public class ProgressView extends FrameLayout {

    private ImageView mSeekIcon;
    private TextView mSeekCurProgress;
    private TextView mSeekDuration;

    private int mStartProgress = -1;  //记录当前进度值
    private int mDelProgress = -1;    //记录进度进度变化值
    private int mDuration = -1;       //记录进度值总长度

    public ProgressView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProgressView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_progress_view, this);

        mSeekIcon = findViewById(R.id.iv_seek_direction);
        mSeekCurProgress = findViewById(R.id.tv_seek_current_progress);
        mSeekDuration = findViewById(R.id.tv_seek_duration);
    }

    public void show(int delProgress, int curPosition, int duration){
                 //delProgress 进度变化值 curPosition 当前进度 duration 总长度
        if (duration <= 0) return;

        // 获取第一次显示时的开始进度
        if (mStartProgress == -1) {
            mStartProgress = curPosition;
        }

        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }

        mDuration = duration;
        mDelProgress -= delProgress;
        int targetProgress = getTargetProgress();

        if (delProgress > 0) {
            // 回退
            mSeekIcon.setImageResource(R.drawable.ic_fast_rewind);
        } else {
            // 前进
            mSeekIcon.setImageResource(R.drawable.ic_fast_forward);
        }
        mSeekCurProgress.setText(StringUtils.stringForTime(targetProgress));
        mSeekDuration.setText(StringUtils.stringForTime(mDuration));
    }

    /**
     * 获取滑动结束后的目标进度
     */
    public int getTargetProgress() {
        if (mDuration == -1) {
            return -1;
        }
        int newSeekProgress = mStartProgress + mDelProgress;
        if (newSeekProgress <= 0) newSeekProgress = 0;
        if (newSeekProgress >= mDuration) newSeekProgress = mDuration;
        return newSeekProgress;
    }

    public void hide() {
        mDuration = -1;
        mStartProgress = -1;
        mDelProgress = -1;
        setVisibility(GONE);
    }


}
