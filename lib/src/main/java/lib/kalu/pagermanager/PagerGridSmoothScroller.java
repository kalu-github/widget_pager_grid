package lib.kalu.pagermanager;

import android.content.Context;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;

/**
 * description: 平滑滚动器
 * created by kalu on 2018/8/8 16:44
 */
public final class PagerGridSmoothScroller extends LinearSmoothScroller {

    public PagerGridSmoothScroller(Context context) {
        super(context);
    }

    /**
     * 在滚动过程中，targetView即将要进入到视野时，将匀速滚动变换为减速滚动，然后一直滚动目的坐标位置，使滚动效果更真实，这是由onTargetFound()方法决定。
     *
     * @param targetView
     * @param state
     * @param action
     */
    @Override
    protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {

        if (null == targetView)
            return;

        final ViewParent parent = targetView.getParent();
        if (null == parent || !(parent instanceof RecyclerView))
            return;

        final RecyclerView recycler = (RecyclerView) parent;
        final RecyclerView.LayoutManager manager = recycler.getLayoutManager();
        if (null == manager || !(manager instanceof PagerGridLayoutManager))
            return;

        final PagerGridLayoutManager layoutManager = (PagerGridLayoutManager) manager;
        final int position = recycler.getChildAdapterPosition(targetView);
        final int[] snapDistances = layoutManager.getSnapOffset(position);
        final int dx = snapDistances[0];
        final int dy = snapDistances[1];

        Log.e("kalu", "onTargetFound ==> dx = " + dx + ", dy = " + dy);
        action.update(dx, dy, 500, null);
    }

//    @Nullable
//    @Override
//    public PointF computeScrollVectorForPosition(int targetPosition) {
//        return computeScrollVectorForPosition(targetPosition);
//    }

    /**
     * 控制滑动位置
     *
     * @param viewStart
     * @param viewEnd
     * @param boxStart
     * @param boxEnd
     * @param snapPreference
     * @return
     */
//    @Override
//    public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
//        return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
//    }

    /**
     * 控制滑动速度
     *
     * @param displayMetrics
     * @return
     */
    @Override
    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        return 0.25f;
    }

//    @Override
//    protected int getVerticalSnapPreference() {
//        return SNAP_TO_START;
//    }
//
//    @Override
//    protected int getHorizontalSnapPreference() {
//        return SNAP_TO_START;
//    }
}
