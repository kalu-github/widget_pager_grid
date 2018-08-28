package lib.kalu.pagermanager;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.IntRange;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

import java.util.LinkedList;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.view.View.MeasureSpec.EXACTLY;

/**
 * description: 网格布局, 水平分页和垂直分页
 * created by kalu on 2018/8/27 17:33
 */
public final class PagerGridLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {

    private int mOrientation;                       // 默认水平滚动

    private int mOffsetX = 0;                       // 水平滚动距离(偏移量)
    private int mOffsetY = 0;                       // 垂直滚动距离(偏移量)

    private int mRows;                              // 行数
    private int mColumns;                           // 列数
    private int mOnePageSize;                       // 一页的条目数量

    private int mItemWidth = 0;                     // 条目宽度
    private int mItemHeight = 0;                    // 条目高度

    private int mWidthUsed = 0;                     // 已经使用空间，用于测量View
    private int mHeightUsed = 0;                    // 已经使用空间，用于测量View

    private int mMaxScrollX;                        // 最大允许滑动的宽度
    private int mMaxScrollY;                        // 最大允许滑动的高度
    private int mScrollState = SCROLL_STATE_IDLE;   // 滚动状态

    /**********************************************************************************************/

    public PagerGridLayoutManager(@IntRange(from = 1, to = 100) int rows, @IntRange(from = 1, to = 100) int columns, @IntRange(from = 0, to = 1) int orientation) {
        mOrientation = orientation;
        mRows = rows;
        mColumns = columns;
        mOnePageSize = mRows * mColumns;
    }

    /**********************************************************************************************/

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {

        final boolean isPreLayout = state.isPreLayout();
        final boolean isMeasuring = state.isMeasuring();
        final boolean isDidStructureChange = state.didStructureChange();
        Log.e("kalu", "onLayoutChildren ==> state = " + state + ", isPreLayout = " + isPreLayout + ", isMeasuring = " + isMeasuring + ", isDidStructureChange = " + isDidStructureChange);

        // 如果是 preLayout 则不重新布局
        if (isPreLayout || !isDidStructureChange)
            return;

        final int itemCount = getItemCount();
        final boolean isNull = (itemCount == 0);
        if (isNull) {
            removeAndRecycleAllViews(recycler);
        }

        setPageCount(isNull ? 0 : getTotalPageCount());
        setPageIndex(isNull ? 0 : getPageIndexByOffset(), false);

        if (isNull) {
            return;
        }

        // 计算页面数量
        int mPageCount = itemCount / mOnePageSize;
        if (itemCount % mOnePageSize != 0) {
            mPageCount++;
        }

        // 计算可以滚动的最大数值，并对滚动距离进行修正
        if (canScrollHorizontally()) {
            mMaxScrollX = (mPageCount - 1) * getRealWidth();
            mMaxScrollY = 0;
            if (mOffsetX > mMaxScrollX) {
                mOffsetX = mMaxScrollX;
            }
        } else {
            mMaxScrollX = 0;
            mMaxScrollY = (mPageCount - 1) * getRealHeight();
            if (mOffsetY > mMaxScrollY) {
                mOffsetY = mMaxScrollY;
            }
        }

        // 接口回调
        // setPageCount(mPageCount);
        // setPageIndex(mCurrentPageIndex, false);

        if (mItemWidth <= 0) {
            mItemWidth = getRealWidth() / mColumns;
        }
        if (mItemHeight <= 0) {
            mItemHeight = getRealHeight() / mRows;
        }

        mWidthUsed = getRealWidth() - mItemWidth;
        mHeightUsed = getRealHeight() - mItemHeight;

        if (mOffsetX == 0 && mOffsetY == 0) {
            // 预存储View
            for (int i = 0; i < mOnePageSize; i++) {
                if (i >= getItemCount()) break; // 防止数据过少时导致数组越界异常
                View view = recycler.getViewForPosition(i);
                addView(view);
                measureChildWithMargins(view, mWidthUsed, mHeightUsed);
            }
        }

        // 回收和填充布局
        recycleAndFillItems(recycler, state, true);
    }

    /**
     * 布局结束
     *
     * @param state State
     */
    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        if (state.isPreLayout()) {
            return;
        }
        // 页面状态回调
        setPageCount(getTotalPageCount());
        setPageIndex(getPageIndexByOffset(), false);
    }

    /**********************************************************************************************/

    //--- 页面相关(私有) -----------------------------------------------------------------------------

    /**
     * 获取总页数
     */
    private int getTotalPageCount() {
        if (getItemCount() <= 0) return 0;
        int totalCount = getItemCount() / mOnePageSize;
        if (getItemCount() % mOnePageSize != 0) {
            totalCount++;
        }
        return totalCount;
    }

    /**
     * 根据pos，获取该View所在的页面
     *
     * @param pos position
     * @return 页面的页码
     */
    private int getPageIndexByPos(int pos) {
        return pos / mOnePageSize;
    }

    /**
     * 创建默认布局参数
     *
     * @return 默认布局参数
     */
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * 处理测量逻辑
     *
     * @param recycler          RecyclerView
     * @param state             状态
     * @param widthMeasureSpec  宽度属性
     * @param heightMeasureSpec 高估属性
     */
    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(recycler, state, widthMeasureSpec, heightMeasureSpec);
        int widthsize = View.MeasureSpec.getSize(widthMeasureSpec);      //取出宽度的确切数值
        int widthmode = View.MeasureSpec.getMode(widthMeasureSpec);      //取出宽度的测量模式

        int heightsize = View.MeasureSpec.getSize(heightMeasureSpec);    //取出高度的确切数值
        int heightmode = View.MeasureSpec.getMode(heightMeasureSpec);    //取出高度的测量模式

        // 将 wrap_content 转换为 match_parent
        if (widthmode != EXACTLY && widthsize > 0) {
            widthmode = EXACTLY;
        }
        if (heightmode != EXACTLY && heightsize > 0) {
            heightmode = EXACTLY;
        }
        setMeasuredDimension(View.MeasureSpec.makeMeasureSpec(widthsize, widthmode),
                View.MeasureSpec.makeMeasureSpec(heightsize, heightmode));
    }

    /**
     * 获取当前 X 轴偏移量
     *
     * @return X 轴偏移量
     */
    public int getOffsetX() {
        return mOffsetX;
    }

    /**
     * 获取当前 Y 轴偏移量
     *
     * @return Y 轴偏移量
     */
    public int getOffsetY() {
        return mOffsetY;
    }


    //--- 页面对齐 ----------------------------------------------------------------------------------

    /**
     * 获取偏移量(为PagerGridSnapHelper准备)
     * 用于分页滚动，确定需要滚动的距离。
     * {@link PagerGridSnapHelper}
     *
     * @param targetPosition 条目下标
     */
    int[] getSnapOffset(int targetPosition) {
        int[] offset = new int[2];
        int[] pos = getPageLeftTopByPosition(targetPosition);
        offset[0] = pos[0] - mOffsetX;
        offset[1] = pos[1] - mOffsetY;
        return offset;
    }

    /**
     * 根据条目下标获取该条目所在页面的左上角位置
     *
     * @param pos 条目下标
     * @return 左上角位置
     */
    private int[] getPageLeftTopByPosition(int pos) {
        int[] leftTop = new int[2];
        int page = getPageIndexByPos(pos);
        if (canScrollHorizontally()) {
            leftTop[0] = page * getRealWidth();
            leftTop[1] = 0;
        } else {
            leftTop[0] = 0;
            leftTop[1] = page * getRealHeight();
        }
        return leftTop;
    }

    /**
     * 获取需要对齐的View
     *
     * @return 需要对齐的View
     */
    public View findSnapView() {
        if (null != getFocusedChild()) {
            return getFocusedChild();
        }
        if (getChildCount() <= 0) {
            return null;
        }
        int targetPos = getPageIndexByOffset() * mOnePageSize;   // 目标Pos
        for (int i = 0; i < getChildCount(); i++) {
            int childPos = getPosition(getChildAt(i));
            if (childPos == targetPos) {
                return getChildAt(i);
            }
        }
        return getChildAt(0);
    }


    //--- 处理页码变化 -------------------------------------------------------------------------------

    private boolean mChangeSelectInScrolling = true;    // 是否在滚动过程中对页面变化回调
    private int mLastPageCount = -1;                    // 上次页面总数
    private int mLastPageIndex = -1;                    // 上次页面下标

    /**
     * 设置页面总数
     *
     * @param pageCount 页面总数
     */
    private void setPageCount(int pageCount) {
        if (pageCount >= 0) {
            if (mPageListener != null && pageCount != mLastPageCount) {
                mPageListener.onPageSizeChanged(pageCount);
            }
            mLastPageCount = pageCount;
        }
    }

    /**
     * 设置当前选中页面
     *
     * @param pageIndex   页面下标
     * @param isScrolling 是否处于滚动状态
     */
    private void setPageIndex(int pageIndex, boolean isScrolling) {
        Log.e("kalu", "setPageIndex = " + pageIndex + ":" + isScrolling);
        if (pageIndex == mLastPageIndex) return;
        // TODO: 2018/8/28   如果允许连续滚动，那么在滚动过程中就会更新页码记录
//        if (isAllowContinuousScroll()) {
//            mLastPageIndex = pageIndex;
//        } else {
        // 否则，只有等滚动停下时才会更新页码记录
        if (!isScrolling) {
            mLastPageIndex = pageIndex;
        }
        if (isScrolling && !mChangeSelectInScrolling) return;
        if (pageIndex >= 0) {
            if (null != mPageListener) {
                mPageListener.onPageSelect(pageIndex);
            }
        }
    }

    /**
     * 设置是否在滚动状态更新选中页码
     *
     * @param changeSelectInScrolling true：更新、false：不更新
     */
    public void setChangeSelectInScrolling(boolean changeSelectInScrolling) {
        mChangeSelectInScrolling = changeSelectInScrolling;
    }

    /**
     * 设置滚动方向
     *
     * @param orientation 滚动方向
     * @return 最终的滚动方向
     */
    public int setOrientationType(@IntRange(from = 0, to = 1) int orientation) {
        if (mOrientation == orientation || mScrollState != SCROLL_STATE_IDLE) return mOrientation;
        mOrientation = orientation;
        int x = mOffsetX;
        int y = mOffsetY;
        mOffsetX = y / getRealHeight() * getRealWidth();
        mOffsetY = x / getRealWidth() * getRealHeight();
        int mx = mMaxScrollX;
        int my = mMaxScrollY;
        mMaxScrollX = my / getRealHeight() * getRealWidth();
        mMaxScrollY = mx / getRealWidth() * getRealHeight();
        return mOrientation;
    }

    //--- 对外接口 ----------------------------------------------------------------------------------

    private PageListener mPageListener = null;

    public void setPageListener(PageListener pageListener) {
        mPageListener = pageListener;
    }

    public interface PageListener {
        /**
         * 页面总数量变化
         *
         * @param pageSize 页面总数
         */
        void onPageSizeChanged(int pageSize);

        /**
         * 页面被选中
         *
         * @param pageIndex 选中的页面
         */
        void onPageSelect(int pageIndex);
    }

    /*********************************************************************************************/

    private final int getRealWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private final int getRealHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private final void addOrRemove(RecyclerView.Recycler recycler, Rect displayRect, int position) {

        final View child = recycler.getViewForPosition(position);

        final Rect rect = new Rect();
        // 1. 获取当前View所在页数
        final int pages = position / mOnePageSize;
        // 2. 计算当前页数左上角的总偏移量
        final boolean horizontally = canScrollHorizontally();
        int offsetX = horizontally ? getRealWidth() * pages : 0;
        int offsetY = horizontally ? 0 : getRealHeight() * pages;

        // 3. 根据在当前页面中的位置确定具体偏移量
        int pagePos = position % mOnePageSize;       // 在当前页面中是第几个
        int row = pagePos / mColumns;           // 获取所在行
        int col = pagePos - (row * mColumns);   // 获取所在列

        offsetX += col * mItemWidth;
        offsetY += row * mItemHeight;
        // 状态输出，用于调试
        Log.e("kalu", "addOrRemove ==> pagePos = " + pagePos + ", row = " + row + ", col = " + col + ", offsetX = " + offsetX + ", offsetY = " + offsetY);
        rect.left = offsetX;
        rect.top = offsetY;
        rect.right = offsetX + mItemWidth;
        rect.bottom = offsetY + mItemHeight;

        // 两个矩形是否相交
        if (!Rect.intersects(displayRect, rect)) {
            // 回收入暂存区
            removeAndRecycleView(child, recycler);
        } else {
            addView(child);
            measureChildWithMargins(child, mWidthUsed, mHeightUsed);
            final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int paddingLeft = getPaddingLeft();
            final int paddingTop = getPaddingTop();
            final int left = rect.left - mOffsetX + lp.leftMargin + paddingLeft;
            final int top = rect.top - mOffsetY + lp.topMargin + paddingTop;
            final int right = rect.right - mOffsetX - lp.rightMargin + paddingLeft;
            final int bottom = rect.bottom - mOffsetY - lp.bottomMargin + paddingTop;
            layoutDecorated(child, left, top, right, bottom);
        }
    }

    /**
     * 找到下一页第一个条目的位置
     *
     * @return 第一个搞条目的位置
     */
    protected final int findNextPageFirstPosition() {
        int page = mLastPageIndex;
        page++;
        if (page >= getTotalPageCount()) {
            page = getTotalPageCount() - 1;
        }
        Log.e("kalu", "findNextPageFirstPos ==> page = " + page);
        return page * mOnePageSize;
    }

    /**
     * 找到上一页的第一个条目的位置
     *
     * @return 第一个条目的位置
     */
    protected final int findPrePageFirstPosition() {
        // 在获取时由于前一页的View预加载出来了，所以获取到的直接就是前一页
        int page = mLastPageIndex;
        page--;
        if (page < 0) {
            page = 0;
        }
        Log.e("kalu", "computeScrollVectorForPosition pre = " + page);
        return page * mOnePageSize;
    }

    /**
     * 根据 offset 获取页面Index
     *
     * @return 页面 Index
     */
    private final int getPageIndexByOffset() {
        Log.e("kalu", "getPageIndexByOffset ==> ");

        int pageIndex;
        if (canScrollVertically()) {
            int pageHeight = getRealHeight();
            if (mOffsetY <= 0 || pageHeight <= 0) {
                pageIndex = 0;
            } else {
                pageIndex = mOffsetY / pageHeight;
                if (mOffsetY % pageHeight > pageHeight / 2) {
                    pageIndex++;
                }
            }
        } else {
            int pageWidth = getRealWidth();
            if (mOffsetX <= 0 || pageWidth <= 0) {
                pageIndex = 0;
            } else {
                pageIndex = mOffsetX / pageWidth;
                if (mOffsetX % pageWidth > pageWidth / 2) {
                    pageIndex++;
                }
            }
        }
        return pageIndex;
    }

    /**
     * 回收和填充布局
     *
     * @param recycler Recycler
     * @param state    State
     * @param isStart  是否从头开始，用于控制View遍历方向，true 为从头到尾，false 为从尾到头
     */
    private final void recycleAndFillItems(RecyclerView.Recycler recycler, RecyclerView.State state, boolean isStart) {
        if (state.isPreLayout()) {
            return;
        }

        Log.e("kalu", "mOffsetX = " + mOffsetX);
        Log.e("kalu", "mOffsetY = " + mOffsetY);

        // 计算显示区域区前后多存储一列或则一行
        final Rect displayRect = new Rect(mOffsetX - mItemWidth, mOffsetY - mItemHeight,
                getRealWidth() + mOffsetX + mItemWidth, getRealHeight() + mOffsetY + mItemHeight);
        // 对显显示区域进行修正(计算当前显示区域和最大显示区域对交集)
        displayRect.intersect(0, 0, mMaxScrollX + getRealWidth(), mMaxScrollY + getRealHeight());
        Log.e("kalu", "displayRect = " + displayRect.toString());

        int startPos = 0;                  // 获取第一个条目的Pos
        int pageIndex = getPageIndexByOffset();
        startPos = pageIndex * mOnePageSize;
        Log.e("kalu", "startPos = " + startPos);
        startPos = startPos - mOnePageSize * 2;
        if (startPos < 0) {
            startPos = 0;
        }
        int stopPos = startPos + mOnePageSize * 4;
        if (stopPos > getItemCount()) {
            stopPos = getItemCount();
        }

        Log.e("kalu", "startPos = " + startPos);
        Log.e("kalu", "stopPos = " + stopPos);

        detachAndScrapAttachedViews(recycler); // 移除所有View

        if (isStart) {
            for (int i = startPos; i < stopPos; i++) {
                addOrRemove(recycler, displayRect, i);
            }
        } else {
            for (int i = stopPos - 1; i >= startPos; i--) {
                addOrRemove(recycler, displayRect, i);
            }
        }
        Log.e("kalu", "child count = " + getChildCount());
    }

    /**************************************    滚动    ********************************************/

    @Override
    public void onScrollStateChanged(int state) {
        mScrollState = state;
        super.onScrollStateChanged(state);
        if (state != SCROLL_STATE_IDLE)
            return;
        setPageIndex(getPageIndexByOffset(), false);
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        PointF vector = new PointF();
        int[] pos = getSnapOffset(targetPosition);
        vector.x = pos[0];
        vector.y = pos[1];
        return vector;
    }

    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == LinearLayout.HORIZONTAL;
    }

    @Override
    public boolean canScrollVertically() {
        return mOrientation == LinearLayout.VERTICAL;
    }

    @Override
    public void scrollToPosition(int position) {
        int pageIndex = getPageIndexByPos(position);
        scrollToPage(pageIndex);
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int newX = mOffsetX + dx;
        int result = dx;
        if (newX > mMaxScrollX) {
            result = mMaxScrollX - mOffsetX;
        } else if (newX < 0) {
            result = 0 - mOffsetX;
        }
        mOffsetX += result;
        setPageIndex(getPageIndexByOffset(), true);
        offsetChildrenHorizontal(-result);
        recycleAndFillItems(recycler, state, result > 0);
        return result;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State
            state) {
        int newY = mOffsetY + dy;
        int result = dy;
        if (newY > mMaxScrollY) {
            result = mMaxScrollY - mOffsetY;
        } else if (newY < 0) {
            result = 0 - mOffsetY;
        }
        mOffsetY += result;
        setPageIndex(getPageIndexByOffset(), true);
        offsetChildrenVertical(-result);
        recycleAndFillItems(recycler, state, result > 0);
        return result;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        int targetPageIndex = getPageIndexByPos(position);
        smoothScrollToPage(targetPageIndex);
    }

    public final void smoothPrePage() {
        smoothScrollToPage(getPageIndexByOffset() - 1);
    }

    public final void smoothNextPage() {
        smoothScrollToPage(getPageIndexByOffset() + 1);
    }

    public final void smoothScrollToPage(int pages) {

        final int itemCount = getItemCount();
        if (itemCount <= 0) {
            Log.e("kalu", "RecyclerView Not Found!");
            return;
        }

        if (pages < 0 || pages >= mLastPageCount) {
            Log.e("kalu", "pageIndex is outOfIndex, must in [0, " + mLastPageCount + ").");
            return;
        }

        // 如果滚动到页面之间距离过大，先直接滚动到目标页面到临近页面，在使用 smoothScroll 最终滚动到目标
        // 否则在滚动距离很大时，会导致滚动耗费的时间非常长
        int currentPageIndex = getPageIndexByOffset();
        if (Math.abs(pages - currentPageIndex) > 3) {
            if (pages > currentPageIndex) {
                scrollToPage(pages - 3);
            } else if (pages < currentPageIndex) {
                scrollToPage(pages + 3);
            }
        }

        // 具体执行滚动
        final View snapView = findSnapView();
        final Context applicationContext = snapView.getContext().getApplicationContext();
        final LinearSmoothScroller smoothScroller = new PagerGridSmoothScroller(snapView.getContext());
        int position = pages * mOnePageSize;
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    public final void scrollPrePage() {
        scrollToPage(getPageIndexByOffset() - 1);
    }

    public final void scrollNextPage() {
        scrollToPage(getPageIndexByOffset() + 1);
    }

    public final void scrollToPage(int pages) {

        final int itemCount = getItemCount();
        if (itemCount <= 0) {
            return;
        }

        if (pages < 0 || pages >= mLastPageCount) {
            Log.e("kalu", "scrollToPage ==> pages = " + pages + ", count = " + mLastPageCount);
            return;
        }

        final View child = findSnapView();
        if (null == child) {
            Log.e("kalu", "scrollToPage ==> chile = null");
            return;
        }

        final ViewParent parent = child.getParent();
        if (null == parent || !(parent instanceof RecyclerView)) {
            Log.e("kalu", "scrollToPage ==> parent = null");
            return;
        }

        final boolean vertically = canScrollVertically();
        final int x = vertically ? 0 : pages * getRealWidth() - mOffsetX;
        final int y = vertically ? pages * getRealHeight() - mOffsetY : 0;
        Log.e("kalu", "scrollToPage ==> x = " + x + ", y = " + y);

        ((RecyclerView) parent).scrollBy(x, y);
        setPageIndex(pages, false);
    }

    /**************************************    滚动    ********************************************/
}