package com.feicui.videoplayer.full;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import com.feicui.videoplayer.R;
import io.vov.vitamio.widget.MediaController;
public class CustorMediaController extends MediaController {
    private MediaPlayerControl mediaPlayerControl;
    //音频和亮度处理用到的2个API
    private final AudioManager audioManager;
    private Window window;
    private final int maxVolume;
    private int currentVolume;
    private float currentBrightness;//亮度范围0—1，不需要设置最大亮度
    public CustorMediaController(Context context) {
        super(context);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        window = ((Activity) context).getWindow();
    }
    // 通过重过此方法，来自定义layout
    @Override protected View makeControllerView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_custom_video_controller, this);
        initView(view);
        return view;
    }
   //父类私有不能继承，没有get方法，但是有set方法，可以重写
    @Override public void setMediaPlayer(MediaPlayerControl player) {
        super.setMediaPlayer(player);
        mediaPlayerControl = player;
    }

    private void initView(View view) {
        // forward快进
        ImageButton btnFastForward = (ImageButton) view.findViewById(R.id.btnFastForward);
        btnFastForward.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                //拿到当前播放进度
                long postion = mediaPlayerControl.getCurrentPosition();
                postion += 10000;
                if (postion >= mediaPlayerControl.getDuration()) {
                    postion = mediaPlayerControl.getDuration();
                }
                mediaPlayerControl.seekTo(postion);
            }
        });
        // rewide 快退
        ImageButton btnFastRewide = (ImageButton) view.findViewById(R.id.btnFastRewind);
        btnFastRewide.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                long postion = mediaPlayerControl.getCurrentPosition();
                postion -= 10000;
                if (postion < 0) postion = 0;
                mediaPlayerControl.seekTo(postion);
            }
        });
        // 调整视图(左边调整亮度,右边是音量)
        final View adjustView = view.findViewById(R.id.adjustView);
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            //只需要重写滑动，其他交给gesture去处理
            @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float startX = e1.getX();
                //开始的距离
                float startY = e1.getY();
                float endX = e2.getX();
                float endY = e2.getY();
                float width = adjustView.getWidth();
                float height = adjustView.getHeight();
                //垂直移动屏幕上滑动的比重
                float percentage = (startY - endY) / height;
                // 左侧: 亮度
                if (startX < width / 5) {
                    adjustbRrightness(percentage);
                }
                // 右侧: 音量
                else if (startX > width * 4 / 5) {
                    adjustVolume(percentage);
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });
        // 对view进行touch监听
        // 但是，我们自己不去判读处理各种touch动用了,我们交给gesture去做
        adjustView.setOnTouchListener(new OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                //手势按下的时候
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    currentBrightness = window.getAttributes().screenBrightness;
                }
                gestureDetector.onTouchEvent(event);
                // 在调整过程中，一直显示
                show();
                return true;
            }
        });

    }

    private void adjustVolume(float percentage) {
        int volume = (int) ((percentage * maxVolume) + currentVolume);
        //判断一下，否则可能会出错
        volume = volume > maxVolume ? maxVolume : volume;
        volume = volume < 0 ? 0 : volume;
        // 设置音量 显示UI
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
    }

    private void adjustbRrightness(float percentage) {
        float brightness = percentage + currentBrightness;
        brightness = brightness > 1.0f ? 1.0f : brightness;
        brightness = brightness < 0 ? 0 : brightness;
        // 设置亮度 修改窗口的属性
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = brightness;
        window.setAttributes(layoutParams);
    }
}
