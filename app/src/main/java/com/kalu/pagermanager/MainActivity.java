package com.kalu.pagermanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lib.kalu.pagermanager.PagerGridLayoutManager;
import lib.kalu.pagermanager.PagerGridSnapHelper;

public class MainActivity extends AppCompatActivity implements PagerGridLayoutManager.OnPagerGridLayoutManagerChangeListener {

    private int mRows = 2;
    private int mColumns = 3;
    private RecyclerView mRecyclerView;
    private MainAdapter mAdapter;
    private PagerGridLayoutManager mLayoutManager;
    private TextView mPageTotal;        // 总页数
    private TextView mPageCurrent;      // 当前页数

    private int mTotal = 0;
    private int mCurrent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPageTotal = (TextView) findViewById(R.id.page_total);
        mPageCurrent = (TextView) findViewById(R.id.page_current);

        mLayoutManager = new PagerGridLayoutManager(mRows, mColumns, LinearLayout.HORIZONTAL);


        // 系统带的 RecyclerView，无需自定义
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // 水平分页布局管理器
        mLayoutManager.setOnPagerGridLayoutManagerChangeListener(this);    // 设置页面变化监听器
        mRecyclerView.setLayoutManager(mLayoutManager);

        // 设置滚动辅助工具
        PagerGridSnapHelper pageSnapHelper = new PagerGridSnapHelper();
        pageSnapHelper.attachToRecyclerView(mRecyclerView);

        // 使用原生的 Adapter 即可
        mAdapter = new MainAdapter();
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    public void onRefresh(View view) {

        Toast.makeText(getApplicationContext(), "onRefresh", Toast.LENGTH_SHORT).show();

        final RecyclerView recycler = findViewById(R.id.recycler_view);
        final PagerGridLayoutManager manager = (PagerGridLayoutManager) recycler.getLayoutManager();
        manager.refreshLayoutManager(3, 4, LinearLayout.HORIZONTAL);
        recycler.getAdapter().notifyDataSetChanged();
    }

    public void addOne(View view) {
        mAdapter.data.add(0, "add");
        mAdapter.notifyDataSetChanged();
    }

    public void removeOne(View view) {
        if (mAdapter.data.size() > 0) {
            mAdapter.data.remove(0);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void addMore(View view) {
        List<String> data = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            data.add(i + "a");
        }
        mAdapter.data.addAll(data);
        mAdapter.notifyDataSetChanged();
    }

    public void prePage(View view) {
        mLayoutManager.scrollPrePage();
    }

    public void nextPage(View view) {
        mLayoutManager.scrollNextPage();
    }

    public void smoothPrePage(View view) {
        mLayoutManager.smoothPrePage();
    }

    public void smoothNextPage(View view) {
        mLayoutManager.smoothNextPage();
    }

    public void firstPage(View view) {
        mRecyclerView.smoothScrollToPosition(0);
    }

    public void lastPage(View view) {
        mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public void onChange(int pagesCount, int pagesIndex) {

        mTotal = pagesCount;
        Log.e("TAG", "总页数 = " + pagesCount);
        mPageTotal.setText("共 " + pagesCount + " 页");

        mCurrent = pagesIndex;
        Log.e("TAG", "选中页码 = " + pagesIndex);
        mPageCurrent.setText("第 " + (pagesIndex + 1) + " 页");
    }
}
