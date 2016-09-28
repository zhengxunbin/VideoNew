package com.feicui.videonew.ui.likes;

import android.content.Context;
import android.util.AttributeSet;

import com.feicui.videonew.bombapi.BombConst;
import com.feicui.videonew.bombapi.entity.NewsEntity;
import com.feicui.videonew.bombapi.other.InQuery;
import com.feicui.videonew.bombapi.result.QueryResult;
import com.feicui.videonew.ui.UserManager;
import com.feicui.videonew.ui.base.BaseResourceView;

import retrofit2.Call;

/**
 * 我的收藏列表视图
 *
 * 作者：yuanchao on 2016/8/19 0019 11:50
 * 邮箱：yuanchao@feicuiedu.com
 */
public class LikesListView extends BaseResourceView<NewsEntity,LikesItemView> {
    public LikesListView(Context context) {
        super(context);
    }

    public LikesListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LikesListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override protected Call<QueryResult<NewsEntity>> queryData(int limit, int skip) {
        String userId = UserManager.getInstance().getObjectId();
        InQuery where = new InQuery(BombConst.FIELD_LIKES, BombConst.TABLE_USER,userId);
        return newsApi.getLikedList(limit, skip, where);
    }

    @Override protected int getLimit() {
        return 15;
    }

    @Override protected LikesItemView createItemView() {
        return new LikesItemView(getContext());
    }

    public void clear(){
        adapter.clear();
    }
}
