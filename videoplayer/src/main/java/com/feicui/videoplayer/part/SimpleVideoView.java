package com.feicui.videoplayer.part;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.feicui.videoplayer.R;
import com.feicui.videoplayer.full.VideoViewActivity;
import java.io.IOException;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

/**
 * 一个自定义的VideoView,使用MediaPlayer+SurfaceView来实现视频的播放
 * <p/>
 * MediaPlayer来做视频播放的控制，SurfaceView来显示视频
 * <p/>
 * 视图方面(initView方法中进行初始化)将简单实现:放一个播放/暂停按钮，一个进度条,一个全屏按钮,和一个SurfaceView
 * <p/>
 * 本API实现结构：
 * <ul>
 * <li/>提供setVideoPath方法(要在onResume方法调用前来调用): 设置播放谁
 * <li/>提供onResume方法(在activity的onResume来调用): 初始化MediaPlayer,准备MediaPlayer
 * <li/>提供onPause方法 (在activity的onPause来调用): 释放MediaPlayer,暂停mediaPlayer
 * </ul>
 * <p/>
 */
public class SimpleVideoView extends FrameLayout {

    private static final int PROGRESS_MAX = 1000;

    private String videoPath; // 视频播放URL
    private MediaPlayer mediaPlayer;
    private boolean isPrepared; // 是否已准备好
    private boolean isPlaying; // 是否正在播放

    // 视图相关start-------------------
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ImageView ivPreview;//资源准备好后，先显示预览图片
    private ImageButton btnToggle;//开始暂停按钮
    private ProgressBar progressBar;
    // 视图相关end-------------------
    //使用第一个构造器触发第二个构造器
    public SimpleVideoView(Context context) {
        this(context, null);
    }
    //使用第二个构造器触发第三个构造器
    public SimpleVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化视图
        init();
    }
    //进度条的管理
    private Handler handler = new Handler() {
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isPlaying) {
                // 每200毫秒更新一次播放进度
                int progress = (int) (mediaPlayer.getCurrentPosition() * PROGRESS_MAX / mediaPlayer.getDuration());
                progressBar.setProgress(progress);
                handler.sendEmptyMessageDelayed(0, 200);
            }
        }
    };
    private void init() {
        // Vitamio的初始化
        Vitamio.isInitialized(getContext());
        // inflate
        LayoutInflater.from(getContext()).inflate(R.layout.view_simple_video_player, this, true);
        // 初始化SurfaceView
        initSurfaceView();
        // 初始化视频播放控制视图
        initControllerViews();
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }
    //自定义onResume、onPause方法与Activity状态保持同步
    public void onResume() {
        initMediaPlayer(); // 初始化MediaPlayer，设置一系列监听器
        prepareMediaPlayer(); // 准备MediaPlayer，同时更新UI状态
    }
    public void onPause() {
        pauseMediaPlayer(); // 暂停播放，同时更新UI状态(比如播放按钮变成暂停按钮)
        releaseMediaPlayer(); // 释放MediaPlayer，同时更新UI状态
    }

    private void initSurfaceView() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        // 注意：vitamio使用SurfaceView播放时要设置pixelFormat 像素解码格式(不设置其他机器可能出现黑屏)
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
    }

    private void initControllerViews() {
        // 预览图
        ivPreview = (ImageView) findViewById(R.id.ivPreview);
        // 播放、暂停
        btnToggle = (ImageButton) findViewById(R.id.btnToggle);
        btnToggle.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    pauseMediaPlayer();
                } else if (isPrepared) {
                    startMediaPlayer();
                } else {
                    Toast.makeText(getContext(), "Can't play now!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 设置进度条
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(PROGRESS_MAX);
        // 全屏播放按钮
        ImageButton btnFullScreen = (ImageButton) findViewById(R.id.btnFullScreen);
        btnFullScreen.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                VideoViewActivity.open(getContext(), videoPath);
            }
        });
    }

    // 初始化MediaPlayer，设置一系列监听器
    private void initMediaPlayer() {
        Vitamio.isInitialized(getContext());
        mediaPlayer = new MediaPlayer(getContext());
        mediaPlayer.setDisplay(surfaceHolder);
        // 监听处理
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override public void onPrepared(MediaPlayer mp) {
                //准备好在播放，否则底层会出错导致程序崩溃
                isPrepared = true;
                startMediaPlayer();
            }
        });
        //在线视屏,Vitamio需要对音频进行处理 否则可能播放不了在线视屏
        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override public boolean onInfo(MediaPlayer mp, int what, int extra) {
                // vitamio5.0，要进行audio处理,才能对在线视频进行播放
                if (what == MediaPlayer.MEDIA_INFO_FILE_OPEN_OK){
                    //获取音频缓冲大小
                    //long bufferSize=mediaPlayer.audioTrackInit();
                    //mediaPlayer.audioInitedOk(bufferSize);
                    mediaPlayer.audioInitedOk(mediaPlayer.audioTrackInit());
                    return true;
                }
                return false;
            }
        });
        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                int layoutWidth = surfaceView.getWidth();
                int layoutHeigth = layoutWidth * height / width;
                // 更新surfaceView的size
                ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
                layoutParams.width = layoutWidth;
                layoutParams.height = layoutHeigth;
                surfaceView.setLayoutParams(layoutParams);
            }
        });
    }

    // 开始播放，同时更新UI状态
    private void startMediaPlayer() {
        ivPreview.setVisibility(View.INVISIBLE);
        btnToggle.setImageResource(R.drawable.ic_pause);
        mediaPlayer.start();
        isPlaying = true;
        handler.sendEmptyMessage(0);
    }
    // 准备播放，同时更新UI状态
    private void prepareMediaPlayer() {
        try {
            //重置，将资源清空
            mediaPlayer.reset();
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
            //
            ivPreview.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            Log.d("SimpleVideoView", " prepare MediaPlayer " + e.getMessage());
        }
    }

    // 暂停播放，同时更新UI状态
    private void pauseMediaPlayer() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        isPlaying = false;
        btnToggle.setImageResource(R.drawable.ic_play_arrow);
        handler.removeMessages(0);
    }
    // 释放MediaPlayer，同时更新UI状态
    private void releaseMediaPlayer() {
        mediaPlayer.release();
        mediaPlayer = null;
        isPrepared = false;
        progressBar.setProgress(0);
    }
}