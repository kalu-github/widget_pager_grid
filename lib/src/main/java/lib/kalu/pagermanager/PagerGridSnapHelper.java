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

    /**
     * fling 回调方法，方法中调用了snapFromFling，真正的对齐逻辑在snapFromFling里
     */
    @Override
    public boolean onFling(int velocityX, int velocityY) {
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager == null) {
            return false;
        }
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null) {
            return false;
        }
        final int minFlingVelocity = mRecyclerView.getMinFlingVelocity();
        final boolean ok1 = (Math.abs(velocityY) > minFlingVelocity || Math.abs(velocityX) > minFlingVelocity);
        return ok1 && snapFromFling(layoutManager, velocityX, velocityY);
    }

    /**
     * snapFromFling 方法被fling 触发，用来帮助实现fling 时View对齐
     */
    private boolean snapFromFling(@NonNull RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        // 首先需要判断LayoutManager 实现了ScrollVectorProvider 接口没有，
        //如果没有实现 ,则直接返回。
        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            return false;
        }
        // 创建一个SmoothScroller 用来做滑动到指定位置
        RecyclerView.SmoothScroller smoothScroller = createSnapScroller(layoutManager);
        if (smoothScroller == null) {
            return false;
        }
        // 根据x 和 y 方向的速度来获取需要对齐的View的位置，需要子类实现。
        int targetPosition = findTargetSnapPosition(layoutManager, velocityX, velocityY);
        if (targetPosition == RecyclerView.NO_POSITION) {
            return false;
        }
        // 最终通过 SmoothScroller 来滑动到指定位置
        smoothScroller.setTargetPosition(targetPosition);
        layoutManager.startSmoothScroll(smoothScroller);
        return true;
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
                if (velocityX > 1000) {
                    return manager.findNextPageFirstPosition();
                } else if (velocityX < -1000) {
                    return manager.findPrePageFirstPosition();
                } else {
                    return -1;
                }
            } else if (!horizontally && vertically) {
                if (velocityY > 1000) {
                    return manager.findNextPageFirstPosition();
                } else if (velocityY < -1000) {
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