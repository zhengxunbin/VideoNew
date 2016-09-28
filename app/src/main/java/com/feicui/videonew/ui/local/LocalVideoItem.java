package com.feicui.videonew.ui.local;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.UiThread;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.feicui.videonew.R;
import com.feicui.videoplayer.full.VideoViewActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：yuanchao on 2016/9/9 0009 14:38
 * 邮箱：yuanchao@feicuiedu.com
 */
//自定义控件 线程池写LocalVideoItem每个预览图都有一个线程池和很浪费 资源回收周期太长 而适配器只有一个
public class LocalVideoItem extends FrameLayout {

    @BindView(R.id.ivPreview) ImageView ivPreView;
    @BindView(R.id.tvVideoName) TextView tvVideoName;
    private String filePath; // 文件路径

    public LocalVideoItem(Context context) {
        this(context, null, 0);
    }

    public LocalVideoItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LocalVideoItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, 0);
        init();
    }

    public String getFilePath() {
        return filePath;
    }

    @UiThread
    public void setIvPreView(Bitmap bitmap){
        ivPreView.setImageBitmap(bitmap);
    }

    // 设置预览图像，可在后台线程执行此方法
    public void setIvPreView(final String filePath, final Bitmap bitmap) {
        if (!filePath.equals(this.filePath)) return;
        post(new Runnable() {
            @Override public void run() {
                //再次判断 主线程中执行时可能修改数据 需要再次修改
                if (!filePath.equals(LocalVideoItem.this.filePath)) return;
                ivPreView.setImageBitmap(bitmap);
            }
        });
    }

    private void init() {
        //直接加载到当前布局上
        LayoutInflater.from(getContext()).inflate(R.layout.item_local_video, this, true);
        ButterKnife.bind(this);
    }

    /**
     * 数据绑定(将cursor内容,设置到对应控件上)
     */
    public void bind(Cursor cursor) {
        // 取出文件路径
        filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        // 取出视频名称
        String videoName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
        tvVideoName.setText(videoName);
        ivPreView.setImageBitmap(null);
        // 清除预览图

        // 获取视频的预览图，是一个很费时的操作 (有不确定因素发生)
        // ------ 到后台线程执行

        // 同时会去获取多张预览图(处理数量太多，最好用线程池处理)
        // ------ 线程池处理

        // 已获取过的图像要做缓存
        // ------ LruCache  ThumbnailUtils(不能用Vitamio)缩略图工具
        //下面的方法加载图片会出现白屏，用户体验不好
        // Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
        // ivPreView.setImageBitmap(bitmap);
    }

    // click当前控件,全屏播放
    @OnClick
    public void click() {
        VideoViewActivity.open(getContext(), filePath);
    }
}