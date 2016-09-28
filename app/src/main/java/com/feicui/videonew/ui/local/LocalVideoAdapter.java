package com.feicui.videonew.ui.local;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 作者：yuanchao on 2016/9/9 0009 14:32
 * 邮箱：yuanchao@feicuiedu.com
 */
//可以自己写适配器 但是有CursorAdapter我们可以继承
public class LocalVideoAdapter extends CursorAdapter{
    // 用来加载视频预览图的线程池 线程池的管理
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    // 用来缓存已加载过的预览图像(不然可能会出现内存溢出)
    private LruCache<String,Bitmap> lruCache = new LruCache<String,Bitmap>(5 * 1024 * 1024){
        //告知系统图片的大小，以便于计算内存
        @Override protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    public LocalVideoAdapter(Context context) {
        super(context, null, true);
    }
   //创建视图
    @Override public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //调用自己写的LocalVideoItem-> 加载视图布局  使代码结构清晰 布局上控件较多我们可以单独拿出来写布局
        //如果其他页面要使用这里的布局，我们可以直接调用 不用重写 也便于修改和后期维护
        return new LocalVideoItem(context);
    }
   //将数据绑定到视图上
    @Override public void bindView(View view, Context context, Cursor cursor) {
        final LocalVideoItem localVideoItem = (LocalVideoItem) view;
        localVideoItem.bind(cursor);
        // 从缓存中获取预览图
        final String filePath = localVideoItem.getFilePath();
        Bitmap bitmap = lruCache.get(filePath);
        if(bitmap != null){
            localVideoItem.setIvPreView(bitmap);
            return;
        }
        // 后台线程获取视频预览图像 不会造成主线程阻塞 造成用户体验不好
        executorService.submit(new Runnable() {
            @Override public void run() {
                // 加载视频的预览图像
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
                // 缓存当前预览图像,文件路径做为key
                lruCache.put(filePath, bitmap);
                // 将图像设置到控件上
                // 注意：当前是在后台线程内
                localVideoItem.setIvPreView(filePath, bitmap);
            }
        });
    }

    public void release(){
        executorService.shutdown();
    }
}
