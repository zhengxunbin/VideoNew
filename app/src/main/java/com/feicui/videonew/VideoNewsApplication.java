package com.feicui.videonew;
import android.app.Application;
import com.feicui.videonew.commons.ToastUtils;
public class VideoNewsApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
    }
}