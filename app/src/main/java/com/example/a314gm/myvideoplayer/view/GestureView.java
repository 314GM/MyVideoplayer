package com.example.a314gm.myvideoplayer.view;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public abstract class GestureView extends FrameLayout implements GestureDetector.OnGestureListener {


    public static final int GESTURE_ACTION_PROGRESS = 1;    //进度调节
    public static final int GESTURE_ACTION_VOLUME = 2;      //音量调节
    public static final int GESTURE_ACTION_BRIGHTNESS = 3;  //亮度调节
    private int mGestureAction;                             //记录当前动作
    private float mCurrentVolume;                           //当前音量
    private int mMaxVolume;                                 //最大音量
    private int mCurrentBrightness;                         //当前亮度
    private int mMaxBrightness = 255;                       //最大亮度
    private ContentResolver mContentResolver;
    private GestureDetector mGestureDetector;

    protected AudioManager mAudioManager;
    protected Context mContext;
    protected Activity mActivity;
    protected Window mWindow;

    public GestureView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public GestureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GestureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mActivity = (Activity)context;
        mWindow = mActivity.getWindow();
        mGestureDetector = new GestureDetector(context, this);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        //获取系统最大音量
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
                endGesture(mGestureAction);
                break;
        }
        /*蠢逼bug，我说触控怎么没反应，原来是没有返回true
        返回了return super.onTouchEvent(event);
        点击事件没有被拦截，发到下游去了……*/
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {

        //每次手势按下，重置手势动作
        mGestureAction = -1;
        //每次按下获取当前系统的媒体音量
        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //每次按下获取当前系统的亮度
        mContentResolver = mActivity.getContentResolver();
        try {
            mCurrentBrightness = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        final int width = getWidth();
        final int height = getHeight();
        if (width <= 0 || height <= 0) {
            return false;
        }

        /**
         *  根据手势起始2个点判断行为
         *  手势为两种情况
         *  1.X方向移动大于y方向移动是进度调节，否则是亮度调节或者音量调节
         *  2.左半屏是亮度调节，右半屏音量调节
         */
        if (mGestureAction < 0) {
            float moveX = e2.getX() - e1.getX();
            float moveY = e2.getY() - e1.getY();
            if (Math.abs(moveX) >= Math.abs(moveY))//X 方向移动大于y方向移动
                mGestureAction = GESTURE_ACTION_PROGRESS;//进度变化
            else if (e1.getX() <= width / 2) //起始点在右边
                mGestureAction = GESTURE_ACTION_BRIGHTNESS;//亮度变化
            else mGestureAction = GESTURE_ACTION_VOLUME;//除上两种外是声音变化
        }

        //手势计算
        switch (mGestureAction) {
            case GESTURE_ACTION_PROGRESS: { // 进度变化
                // 默认滑动一个屏幕 视频移动3分钟.
                int delProgress = (int) (1.0f * distanceX / width * 180 * 1000);
                // 更新快进弹框
                updateUI(GESTURE_ACTION_PROGRESS,delProgress,0);
                break;
            }
            case GESTURE_ACTION_VOLUME: { // 音量变化
                float progress = mMaxVolume * (distanceY / height) + mCurrentVolume;

                if (progress <= 0) progress = 0;
                if (progress >= mMaxVolume) progress = mMaxVolume;

                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(progress), 0);
                updateUI(GESTURE_ACTION_VOLUME, Math.round(progress),mMaxVolume);
                mCurrentVolume = progress;
                break;
            }
            case GESTURE_ACTION_BRIGHTNESS: { // 亮度变化

                try {
                    //设置显示模式
                    if (Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
                            == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                        Settings.System.putInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    }

                    int progress = (int) (mMaxBrightness * (distanceY / height) + mCurrentBrightness);

                    if (progress <= 0) progress = 0;
                    if (progress >= mMaxBrightness) progress = mMaxBrightness;

                    //设置亮度
                    WindowManager.LayoutParams params = mWindow.getAttributes();
                    params.screenBrightness = progress / (float) mMaxBrightness;
                    mWindow.setAttributes(params);

                    updateUI(GESTURE_ACTION_BRIGHTNESS,progress,mMaxBrightness);

                    mCurrentBrightness = progress;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public abstract void updateUI(int type,int x,int max);

    public abstract void endGesture(int type);
}
