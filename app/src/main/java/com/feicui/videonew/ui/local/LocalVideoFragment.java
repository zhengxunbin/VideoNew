package com.feicui.videonew.ui.local;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.feicui.videonew.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 作者：yuanchao on 2016/9/9 0009 10:07
 * 邮箱：yuanchao@feicuiedu.com
 */
//callback需要指定加载的数据类型
public class LocalVideoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    //private static final String TAG = "LocalVideoFragment";

    private Unbinder unbinder;
    //将本地视屏放在gridView上面
    @BindView(R.id.gridView)GridView gridView;

    private LocalVideoAdapter adapter;
   //不要自己去创建loader 自己创建还要异步处理 调用别人写的
    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new LocalVideoAdapter(getContext());
        //3个参数 id 数据 当前对象 怎么传？去实现callback接口
        //初始当前页面的loader(加载器，去loader视屏数据)
        getLoaderManager().initLoader(0, null, this);//初始化当前的loader
    }
   //创建视图
    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local_video,container,false);
    }
  //负责数据初始化
    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        gridView.setAdapter(adapter);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override public void onDestroy() {
        super.onDestroy();
        adapter.release();
    }

    // loadercallback start ----------------
    //创建所需loader对象
    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                //要传音频就修改Video
                MediaStore.Video.Media._ID, // 视频ID
                MediaStore.Video.Media.DATA, // 视频文件路径
                MediaStore.Video.Media.DISPLAY_NAME,// 视频名称
        };
        return new CursorLoader(
                getContext(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,// 视频provider URI 也可以读其他数据(比如手机联系人)
                projection,//查询我们需要的信息
                null,null,null
        );
    }
   //数据加载完成 就会触发下面的方法
    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Log.d(TAG, "onLoadFinished: "+data.getCount());
        //查询数据库中的名字 我们最好使用索引 可能存在数据的删除、添加id不确定
        //测试
        /*if (data.moveToFirst()){
            int columnIndex=data.getColumnIndex( MediaStore.Video.Media.DISPLAY_NAME);
            String diasplayName=data.getColumnName(columnIndex);
            Log.d(TAG, "onLoadFinished: "+diasplayName);
        }while (data.moveToNext())*/
        adapter.swapCursor(data);
    }
   //数据加载重置
    @Override public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
    // loadercallback end ----------------
}
