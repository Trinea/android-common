package com.trinea.android.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trinea.android.common.R;

/**
 * 下拉刷新的listView
 * <ul>
 * 替代ListView使用，使用方法如下
 * <li>xml中配置同ListView</li>
 * <li>设置{@link #setOnRefreshListener(OnRefreshListener)}，刷新时执行onRefresh函数</li>
 * <li>刷新结束时调用{@link #onRefreshComplete()}表示刷新结束，恢复View状态</li>
 * </ul>
 * <ul>
 * 其他设置见http://trinea.iteye.com/blog/1560986
 * </ul>
 * <ul>
 * 实现原理见http://trinea.iteye.com/blog/1562281
 * </ul>
 * 
 * @author Trinea 2012-5-20 上午12:36:33
 */
public class DropDownToRefreshListView extends ListView implements OnScrollListener {

    /**
     * 刷新的状态
     */
    public enum RefreshStatusEnum {
        /** 点击刷新状态，为初始状态 **/
        CLICK_TO_REFRESH,
        /** 当刷新layout高度低于一定范围时，下拉再释放即可刷新 **/
        DROP_DOWN_TO_REFRESH,
        /** 当刷新layout高度高于一定范围时，释放即可刷新 **/
        RELEASE_TO_REFRESH,
        /** 刷新中 **/
        REFRESHING
    }

    /** 下拉时下拉距离和header top变化的比例 **/
    private static final float HEADER_PADDING_RATE       = 1.5f;
    /** header height变化的上界 **/
    private static final int   HEADER_HEIGHT_UPPER_LEVEL = 10;

    /** 刷新事件 **/
    private OnRefreshListener  mOnRefreshListener;

    private OnScrollListener   mOnScrollListener;

    /** 需要的View **/
    private RelativeLayout     mRefreshViewLayout;
    private TextView           mRefreshViewTipsText;
    private ImageView          mRefreshViewImage;
    private ProgressBar        mRefreshViewProgress;
    private TextView           mRefreshViewLastUpdatedText;

    /** 当前的滚动状态 **/
    private int                mCurrentScrollState;
    /** 当前的刷新状态 **/
    private RefreshStatusEnum  mCurrentRefreshState;

    /** 正向翻转的animation **/
    private RotateAnimation    mFlipAnimation;
    /** 反向翻转的animation **/
    private RotateAnimation    mReverseFlipAnimation;

    /** header(刷新View layout)的初始高度 **/
    private int                mHeaderOriginalHeight;
    /** header(刷新View layout)的初始top padding **/
    private int                mHeaderOriginalTopPadding;
    /** 用户手指刚接触屏幕时touch的点y坐标 **/
    private float              mActionDownPointY;
    /** 是否反弹，滑动到顶部则标记为true **/
    private boolean            mIsBounceHack;

    public DropDownToRefreshListView(Context context){
        super(context);
        init(context);
    }

    public DropDownToRefreshListView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context);
    }

    public DropDownToRefreshListView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mFlipAnimation = new RotateAnimation(0, 180, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                                             RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(250);
        mFlipAnimation.setFillAfter(true);
        mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                                                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(250);
        mReverseFlipAnimation.setFillAfter(true);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRefreshViewLayout = (RelativeLayout)inflater.inflate(R.layout.drop_down_to_refresh_list_header, this, false);
        mRefreshViewTipsText = (TextView)mRefreshViewLayout.findViewById(R.id.drop_down_to_refresh_list_text);
        mRefreshViewImage = (ImageView)mRefreshViewLayout.findViewById(R.id.drop_down_to_refresh_list_image);
        mRefreshViewProgress = (ProgressBar)mRefreshViewLayout.findViewById(R.id.drop_down_to_refresh_list_progress);
        mRefreshViewLastUpdatedText = (TextView)mRefreshViewLayout.findViewById(R.id.drop_down_to_refresh_list_last_updated_text);
        mRefreshViewImage.setMinimumHeight(50);
        mRefreshViewLayout.setOnClickListener(new OnClickRefreshListener());
        mRefreshViewTipsText.setText(R.string.drop_down_to_refresh_list_refresh_view_tips);
        addHeaderView(mRefreshViewLayout);

        // 设置OnScrollListener为当前的listener
        super.setOnScrollListener(this);

        measureView(mRefreshViewLayout);
        mHeaderOriginalHeight = mRefreshViewLayout.getMeasuredHeight();
        mHeaderOriginalTopPadding = mRefreshViewLayout.getPaddingTop();
        mCurrentRefreshState = RefreshStatusEnum.CLICK_TO_REFRESH;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        setSecondPositionVisible();
    }

    @Override
    public void setOnScrollListener(AbsListView.OnScrollListener listener) {
        mOnScrollListener = listener;
    }

    /**
     * 设置刷新事件器
     * 
     * @param onRefreshListener
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mIsBounceHack = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mActionDownPointY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                adjustHeaderPadding(event);
                break;
            case MotionEvent.ACTION_UP:
                if (!isVerticalScrollBarEnabled()) {
                    setVerticalScrollBarEnabled(true);
                }
                if (getFirstVisiblePosition() == 0 && mCurrentRefreshState != RefreshStatusEnum.REFRESHING) {
                    switch (mCurrentRefreshState) {
                        case CLICK_TO_REFRESH:
                            setStatusClickToRefresh();
                            break;
                        case RELEASE_TO_REFRESH:
                            onRefresh();
                            break;
                        case DROP_DOWN_TO_REFRESH:
                            setStatusClickToRefresh();
                            setSecondPositionVisible();
                            break;
                        default:
                            break;
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        /**
         * ListView为SCROLL_STATE_TOUCH_SCROLL状态(按着不放滚动中)并且刷新状态不为REFRESHING
         * a. 刷新对应的item可见时，若刷新layout高度超出范围，则置刷新状态为RELEASE_TO_REFRESH；
         * 若刷新layout高度低于高度范围，则置刷新状态为DROP_DOWN_TO_REFRESH
         * b. 刷新对应的item不可见，重置header
         * ListView为SCROLL_STATE_FLING状态(松手滚动中)
         * a. 若刷新对应的item可见并且刷新状态不为REFRESHING，设置position为1的(即第二个)item可见
         * b. 若反弹回来，设置position为1的(即第二个)item可见
         */
        if (mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL && mCurrentRefreshState != RefreshStatusEnum.REFRESHING) {
            if (firstVisibleItem == 0) {
                mRefreshViewImage.setVisibility(View.VISIBLE);
                if (mRefreshViewLayout.getBottom() >= mHeaderOriginalHeight + HEADER_HEIGHT_UPPER_LEVEL
                    || mRefreshViewLayout.getTop() >= 0) {
                    setStatusReleaseToRefresh();
                } else if (mRefreshViewLayout.getBottom() < mHeaderOriginalHeight + HEADER_HEIGHT_UPPER_LEVEL) {
                    setStatusDropDownToRefresh();
                }
            } else {
                setStatusClickToRefresh();
            }
        } else if (mCurrentScrollState == SCROLL_STATE_FLING && firstVisibleItem == 0
                   && mCurrentRefreshState != RefreshStatusEnum.REFRESHING) {
            setSecondPositionVisible();
            mIsBounceHack = true;
        } else if (mCurrentScrollState == SCROLL_STATE_FLING && mIsBounceHack) {
            setSecondPositionVisible();
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mCurrentScrollState = scrollState;

        if (mCurrentScrollState == SCROLL_STATE_IDLE) {
            mIsBounceHack = false;
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    /**
     * 准备刷新
     */
    public void onRefreshBegin() {
        setStatusRefreshing();
    }

    /**
     * 刷新
     */
    public void onRefresh() {
        if (mOnRefreshListener != null) {
            onRefreshBegin();
            mOnRefreshListener.onRefresh();
        }
    }

    /**
     * 刷新结束
     * 
     * @param lastUpdatedText 上次更新信息，若为null，不显示
     */
    public void onRefreshComplete(CharSequence lastUpdatedText) {
        setLastUpdatedText(lastUpdatedText);
        onRefreshComplete();
    }

    /**
     * 刷新结束，恢复View状态
     */
    public void onRefreshComplete() {
        setStatusClickToRefresh();

        if (mRefreshViewLayout.getBottom() > 0) {
            invalidateViews();
            setSecondPositionVisible();
        }
    }

    /**
     * 点击刷新View时调用<br/>
     * 主要在list仅有少量items，无法下拉刷新只能手动点击刷新View时调用
     */
    private class OnClickRefreshListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (mCurrentRefreshState != RefreshStatusEnum.REFRESHING) {
                onRefresh();
            }
        }

    }

    /**
     * 在刷新list时调用
     * 
     * @author Trinea 2012-5-31 上午11:15:39
     */
    public interface OnRefreshListener {

        /**
         * 在刷新list时调用
         */
        public void onRefresh();
    }

    /**
     * 如果第一个可见的item position为0(即为刷新View)，设置position为1的(即第二个)item可见
     */
    public void setSecondPositionVisible() {
        if (getAdapter() != null && getAdapter().getCount() > 0 && getFirstVisiblePosition() == 0) {
            setSelection(1);
        }
    }

    /**
     * 设置上次更新信息
     * 
     * @param lastUpdatedText 上次更新信息，若为null，不显示
     */
    public void setLastUpdatedText(CharSequence lastUpdatedText) {
        if (lastUpdatedText == null) {
            mRefreshViewLastUpdatedText.setVisibility(View.GONE);
        } else {
            mRefreshViewLastUpdatedText.setVisibility(View.VISIBLE);
            mRefreshViewLastUpdatedText.setText(lastUpdatedText);
        }
    }

    /**
     * 设置为CLICK_TO_REFRESH状态
     */
    private void setStatusClickToRefresh() {
        if (mCurrentRefreshState != RefreshStatusEnum.CLICK_TO_REFRESH) {
            resetHeaderPadding();

            mRefreshViewImage.clearAnimation();
            mRefreshViewImage.setImageResource(R.drawable.drop_down_to_refresh_list_arrow);
            mRefreshViewImage.setVisibility(View.GONE);
            mRefreshViewProgress.setVisibility(View.GONE);
            mRefreshViewTipsText.setText(R.string.drop_down_to_refresh_list_refresh_view_tips);

            mCurrentRefreshState = RefreshStatusEnum.CLICK_TO_REFRESH;
        }
    }

    /**
     * 设置为DROP_DOWN_TO_REFRESH状态
     */
    private void setStatusDropDownToRefresh() {
        if (mCurrentRefreshState != RefreshStatusEnum.DROP_DOWN_TO_REFRESH) {
            mRefreshViewImage.setVisibility(View.VISIBLE);
            // CLICK_TO_REFRESH不需要启动动画
            if (mCurrentRefreshState != RefreshStatusEnum.CLICK_TO_REFRESH) {
                mRefreshViewImage.clearAnimation();
                mRefreshViewImage.startAnimation(mReverseFlipAnimation);
            }
            mRefreshViewProgress.setVisibility(View.GONE);
            mRefreshViewTipsText.setText(R.string.drop_down_to_refresh_list_pull_tips);

            if (isVerticalFadingEdgeEnabled()) {
                setVerticalScrollBarEnabled(false);
            }

            mCurrentRefreshState = RefreshStatusEnum.DROP_DOWN_TO_REFRESH;
        }
    }

    /**
     * 设置为RELEASE_TO_REFRESH状态
     */
    private void setStatusReleaseToRefresh() {
        if (mCurrentRefreshState != RefreshStatusEnum.RELEASE_TO_REFRESH) {
            mRefreshViewImage.setVisibility(View.VISIBLE);
            mRefreshViewImage.clearAnimation();
            mRefreshViewImage.startAnimation(mFlipAnimation);
            mRefreshViewProgress.setVisibility(View.GONE);
            mRefreshViewTipsText.setText(R.string.drop_down_to_refresh_list_release_tips);

            mCurrentRefreshState = RefreshStatusEnum.RELEASE_TO_REFRESH;
        }
    }

    /**
     * 设置为REFRESHING状态
     */
    private void setStatusRefreshing() {
        if (mCurrentRefreshState != RefreshStatusEnum.REFRESHING) {
            resetHeaderPadding();

            mRefreshViewImage.setVisibility(View.GONE);
            mRefreshViewImage.setImageDrawable(null);
            mRefreshViewProgress.setVisibility(View.VISIBLE);
            mRefreshViewTipsText.setText(R.string.drop_down_to_refresh_list_refreshing_tips);

            mCurrentRefreshState = RefreshStatusEnum.REFRESHING;
            setSelection(0);
        }
    }

    /**
     * 调整header的padding
     * 
     * @param ev
     */
    private void adjustHeaderPadding(MotionEvent ev) {
        /**
         * 通过获取move历史坐标点，不断设置header的padding
         */
        int pointerCount = ev.getHistorySize();
        for (int i = 0; i < pointerCount; i++) {
            if (mCurrentRefreshState == RefreshStatusEnum.RELEASE_TO_REFRESH) {
                mRefreshViewLayout.setPadding(mRefreshViewLayout.getPaddingLeft(),
                                              (int)(((ev.getHistoricalY(i) - mActionDownPointY) - mHeaderOriginalHeight) / HEADER_PADDING_RATE),
                                              mRefreshViewLayout.getPaddingRight(),
                                              mRefreshViewLayout.getPaddingBottom());
            }
        }
    }

    /**
     * 重置header的padding
     */
    private void resetHeaderPadding() {
        mRefreshViewLayout.setPadding(mRefreshViewLayout.getPaddingLeft(), mHeaderOriginalTopPadding,
                                      mRefreshViewLayout.getPaddingRight(), mRefreshViewLayout.getPaddingBottom());
    }

    /**
     * 测量View的宽度和高度
     * 
     * @param child
     */
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }
}
