package liubin.com.myapplication.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;
import butterknife.BindView;
import com.example.mylibrary.base.EndlessScrollListener;
import com.example.mylibrary.base.TopBarActivity;
import liubin.com.myapplication.R;
import liubin.com.myapplication.bean.StringData;

public class MVPFragment
    extends ListMVPFragment<TopBarActivity, String, StringData, IListMVPPresenter<StringData>> {
  private static final int PAGE_SIZE = 20;

  @BindView(R.id.recyclerview) RecyclerView mRecyclerView;
  @BindView(R.id.swip) SwipeRefreshLayout mSwipeRefreshLayout;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPresenter = new MVPPresenter(this, this);
    mPresenter.loadData(PAGE_SIZE, false);
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mSwipeRefreshLayout.setColorSchemeResources(//
        android.R.color.holo_blue_bright,//
        android.R.color.holo_green_light,//
        android.R.color.holo_orange_light,//
        android.R.color.holo_red_light);
    mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override public void onRefresh() {
        mPresenter.loadData(PAGE_SIZE, true);
      }
    });

    mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    mRecyclerView.setAdapter(new BasicAdapter(getActivity(), mData, this));
    mRecyclerView.addOnScrollListener(new EndlessScrollListener(this));

    // 这一句可以在任何时候调用
    setEmptyMessage("这里没有数据", R.drawable.ic_conn_no_network);
  }

  @Override public boolean checkHasMore(StringData data) {
    //判断是否还有更多数据
    if (data == null || !data.isSuccess()) {
      return true;
    }
    return data.getData() != null && data.getData().size() == PAGE_SIZE;
  }

  /**
   * 初始化状态栏,标题栏
   *
   * @param activity {@link TopBarActivity}
   */
  @Override public void initTopBar(TopBarActivity activity) {
    super.initTopBar(activity);
    Toolbar toolBar = activity.getToolBar();
    toolBar.setTitle("MVP基本使用");
    toolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
    toolBar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mActivity.finish();
      }
    });
  }

  @Override public int getFragmentContentLayoutResourceID() {
    return R.layout.content_mvp;
  }

  @Override public void onStatusUpdated() {
    mSwipeRefreshLayout.setRefreshing(isLoading());
    mRecyclerView.getAdapter().notifyDataSetChanged();
  }

  @Override public void onSuccess(StringData data, boolean isRefresh) {
    if (!data.isSuccess()) {// 服务端返回异常代码
      Toast.makeText(getContext(), data.getMessage(), Toast.LENGTH_LONG).show();
      return;
    }

    if (isRefresh) mData.clear();
    if (data.getData() != null && data.getData().size() > 0) {
      mData.addAll(data.getData());
    }
  }

  @Override public void loadMore() {
    mPresenter.loadData(PAGE_SIZE, false);
  }
}