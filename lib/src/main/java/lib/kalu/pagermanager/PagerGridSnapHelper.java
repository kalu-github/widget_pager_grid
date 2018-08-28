package lib.kalu.pagermanager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.View;

/**
 * description: 手势
 * created by kalu on 2018/8/28 11:20
 */
public final class PagerGridSnapHelper extends SnapHelper {

    private RecyclerView mRecyclerView;                     // RecyclerView

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        super.attachToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    /**
     * 计算需要滚动的向量，用于页面自动回滚对齐
     *
     * @param layoutManager 布局管理器
     * @param targetView    目标控件
     * @return 需要滚动的距离
     */
    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        if (!(layoutManager instanceof PagerGridLayoutManager)) {
            Log.e("kalu", "calculateDistanceToFinalSnap ==> layoutManager == null");
            return new int[2];
        } else {
            final int position = layoutManager.getPosition(targetView);
            Log.e("kalu", "calculateDistanceToFinalSnap ==> position = " + position + ", targetView = " + targetView);
            return ((PagerGridLayoutManager) layoutManager).getSnapOffset(position);
        }
    }

    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        final boolean isNull = (!(layoutManager instanceof PagerGridLayoutManager));
        return isNull ? null : ((PagerGridLayoutManager) layoutManager).findSnapView();
    }

    @Override
    public boolean onFling(int velocityX, int velocityY) {
        Log.e("kalu", "onFling ==> velocityX = " + velocityX + ", velocityY = " + velocityY);

        if (null == mRecyclerView) {
            Log.e("kalu", "onFling ==> null == mRecyclerView");
            return false;
        }

        final RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (null == layoutManager) {
            Log.e("kalu", "onFling ==> null == layoutManager");
            return false;
        }

        final int targetPosition = findTargetSnapPosition(layoutManager, velocityX, velocityY);
        if (targetPosition == RecyclerView.NO_POSITION) {
            Log.e("kalu", "onFling ==> targetPosition == -1");
            return false;
        }

        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            Log.e("kalu", "onFling ==> layoutManager != RecyclerView.SmoothScroller.ScrollVectorProvider");
            return false;
        }

        final RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (null == adapter) {
            Log.e("kalu", "onFling ==> null == adapter");
            return false;
        }

        final RecyclerView.SmoothScroller smoothScroller = createSnapScroller(layoutManager);
        if (smoothScroller == null) {
            Log.e("kalu", "onFling ==> null == smoothScroller");
            return false;
        }

        final boolean ok = (Math.abs(velocityY) > 500 || Math.abs(velocityX) > 500);
        smoothScroller.setTargetPosition(targetPosition);
        layoutManager.startSmoothScroll(smoothScroller);
        return ok;
    }

    /**
     * 获取目标控件的位置下标
     * (获取滚动后第一个View的下标)
     *
     * @param layoutManager 布局管理器
     * @param velocityX     X 轴滚动速率
     * @param velocityY     Y 轴滚动速率
     * @return 目标控件的下标
     */
    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        Log.e("kalu", "findTargetSnapPosition ==> velocityX = " + velocityX + ", velocityY = " + velocityY);

        if (null == layoutManager || !(layoutManager instanceof PagerGridLayoutManager)) {
            return -1;
        } else {
            final PagerGridLayoutManager manager = (PagerGridLayoutManager) layoutManager;
            final boolean horizontally = manager.canScrollHorizontally();
            final boolean vertically = manager.canScrollVertically();
            if (horizontally && !vertically) {
                if (velocityX > 500) {
                    return manager.findNextPageFirstPosition();
                } else if (velocityX < -500) {
                    return manager.findPrePageFirstPosition();
                } else {
                    return -1;
                }
            } else if (!horizontally && vertically) {
                if (velocityY > 500) {
                    return manager.findNextPageFirstPosition();
                } else if (velocityY < -500) {
                    return manager.findPrePageFirstPosition();
                } else {
                    return -1;
                }
            } else {
                return -1;
            }
        }
    }

    @Nullable
    @Override
    protected RecyclerView.SmoothScroller createScroller(RecyclerView.LayoutManager layoutManager) {
        final boolean isNull = (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider));
        return isNull ? null : new PagerGridSmoothScroller(mRecyclerView.getContext());
    }

    @Nullable
    @Override
    protected LinearSmoothScroller createSnapScroller(RecyclerView.LayoutManager layoutManager) {
        final boolean isNull = !(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider);
        return isNull ? null : new PagerGridSmoothScroller(mRecyclerView.getContext());
    }
}