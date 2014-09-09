package cn.trinea.android.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.trinea.android.common.R;

/**
 * <strong> Listview which can do something when drop down or scroll to bottom. for example, drop down to refresh and
 * load more when scroll to bottom</strong>
 * <ul>
 * <strong>DropDown Listener</strong>
 * <li>define isDropDownStyle="true" at layout xml or call {@link #setDropDownStyle(boolean)} to enable drop down style
 * before</li>
 * <li>{@link #setOnDropDownListener(OnDropDownListener)} set listener which will be excuted when drop down, but you
 * should call {@link #onDropDownComplete()} manual at the end of listener to reinstate status.</li>
 * <li>{@link #setHeaderDefaultText(String)}, {@link #setHeaderLoadingText(String)}, {@link #setHeaderPullText(String)},
 * {@link #setHeaderReleaseText(String)}, {@link #setHeaderSecondText(CharSequence)} to set text</li>
 * </ul>
 * <ul>
 * <strong>OnBottom Listener</strong>
 * <li>define isOnBottomStyle="true" at layout xml or call {@link #setOnBottomStyle(boolean)} to enable on bottom style
 * before</li>
 * <li>{@link #setOnBottomListener(OnClickListener)} set listener which will be excuted when scroll to bottom, but you
 * should call {@link #onBottomComplete()} manual at the end of listener to reinstate status.</li>
 * <li>you can define isAutoLoadOnBottom="true" at layout xml or call {@link #setAutoLoadOnBottom(boolean)} to enable
 * auto call OnBottomListener when scroll to bottom</li>
 * <li>{@link #setFooterDefaultText(String)}, {@link #setFooterLoadingText(String)},
 * {@link #setFooterNoMoreText(String)} to set text</li>
 * </ul>
 * <ul>
 * other setting see <a href="http://www.trinea.cn/?p=523">http://www.trinea.cn/?p=523</a>
 * </ul>
 * <ul>
 * principle see <a href="http://trinea.iteye.com/blog/1562281">http://trinea.iteye.com/blog/1562281</a>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-5-20
 */
public class DropDownListView extends ListView implements OnScrollListener {

    private boolean            isDropDownStyle         = true;
    private boolean            isOnBottomStyle         = true;
    private boolean            isAutoLoadOnBottom      = false;

    private String             headerDefaultText;
    private String             headerPullText;
    private String             headerReleaseText;
    private String             headerLoadingText;
    private String             footerDefaultText;
    private String             footerLoadingText;
    private String             footerNoMoreText;

    private Context            context;

    /** header layout view **/
    private RelativeLayout     headerLayout;
    private ImageView          headerImage;
    private ProgressBar        headerProgressBar;
    private TextView           headerText;
    private TextView           headerSecondText;

    /** footer layout view **/
    private RelativeLayout     footerLayout;
    private ProgressBar        footerProgressBar;
    private Button             footerButton;

    private OnDropDownListener onDropDownListener;
    private OnScrollListener   onScrollListener;

    /** rate about drop down distance and header padding top when drop down **/
    private float              headerPaddingTopRate    = 1.5f;
    /** min distance which header can release to loading **/
    private int                headerReleaseMinDistance;

    /** whether bottom listener has more **/
    private boolean            hasMore                 = true;
    /** whether show footer loading progress bar when loading **/
    private boolean            isShowFooterProgressBar = true;
    /** whether show footer when no more data **/
    private boolean            isShowFooterWhenNoMore  = false;

    private int                currentScrollState;
    private int                currentHeaderStatus;

    /** whether reached top, when has reached top, don't show header layout **/
    private boolean            hasReachedTop           = false;

    /** image flip animation **/
    private RotateAnimation    flipAnimation;
    /** image reverse flip animation **/
    private RotateAnimation    reverseFlipAnimation;

    /** header layout original height **/
    private int                headerOriginalHeight;
    /** header layout original padding top **/
    private int                headerOriginalTopPadding;
    /** y of point which user touch down **/
    private float              actionDownPointY;

    /** whether is on bottom loading **/
    private boolean            isOnBottomLoading       = false;

    public DropDownListView(Context context) {
        super(context);
        init(context);
    }

    public DropDownListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttrs(context, attrs);
        init(context);
    }

    public DropDownListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getAttrs(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        initDropDownStyle();
        initOnBottomStyle();

        // should set, to run onScroll method and so on
        super.setOnScrollListener(this);
    }

    /**
     * init drop down style, only init once
     */
    private void initDropDownStyle() {
        if (headerLayout != null) {
            if (isDropDownStyle) {
                addHeaderView(headerLayout);
            } else {
                removeHeaderView(headerLayout);
            }
            return;
        }
        if (!isDropDownStyle) {
            return;
        }

        headerReleaseMinDistance = context.getResources().getDimensionPixelSize(
                R.dimen.drop_down_list_header_release_min_distance);
        flipAnimation = new RotateAnimation(0, 180, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        flipAnimation.setInterpolator(new LinearInterpolator());
        flipAnimation.setDuration(250);
        flipAnimation.setFillAfter(true);
        reverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseFlipAnimation.setInterpolator(new LinearInterpolator());
        reverseFlipAnimation.setDuration(250);
        reverseFlipAnimation.setFillAfter(true);

        headerDefaultText = context.getString(R.string.drop_down_list_header_default_text);
        headerPullText = context.getString(R.string.drop_down_list_header_pull_text);
        headerReleaseText = context.getString(R.string.drop_down_list_header_release_text);
        headerLoadingText = context.getString(R.string.drop_down_list_header_loading_text);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        headerLayout = (RelativeLayout)inflater.inflate(R.layout.drop_down_list_header, this, false);
        headerText = (TextView)headerLayout.findViewById(R.id.drop_down_list_header_default_text);
        headerImage = (ImageView)headerLayout.findViewById(R.id.drop_down_list_header_image);
        headerProgressBar = (ProgressBar)headerLayout.findViewById(R.id.drop_down_list_header_progress_bar);
        headerSecondText = (TextView)headerLayout.findViewById(R.id.drop_down_list_header_second_text);
        headerLayout.setClickable(true);
        headerLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onDropDown();
            }
        });
        headerText.setText(headerDefaultText);
        addHeaderView(headerLayout);

        measureHeaderLayout(headerLayout);
        headerOriginalHeight = headerLayout.getMeasuredHeight();
        headerOriginalTopPadding = headerLayout.getPaddingTop();
        currentHeaderStatus = HEADER_STATUS_CLICK_TO_LOAD;
    }

    /**
     * init on bottom style, only init once
     */
    private void initOnBottomStyle() {
        if (footerLayout != null) {
            if (isOnBottomStyle) {
                addFooterView(footerLayout);
            } else {
                removeFooterView(footerLayout);
            }
            return;
        }
        if (!isOnBottomStyle) {
            return;
        }

        footerDefaultText = context.getString(R.string.drop_down_list_footer_default_text);
        footerLoadingText = context.getString(R.string.drop_down_list_footer_loading_text);
        footerNoMoreText = context.getString(R.string.drop_down_list_footer_no_more_text);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerLayout = (RelativeLayout)inflater.inflate(R.layout.drop_down_list_footer, this, false);
        footerButton = (Button)footerLayout.findViewById(R.id.drop_down_list_footer_button);
        footerButton.setDrawingCacheBackgroundColor(0);
        footerButton.setEnabled(true);

        footerProgressBar = (ProgressBar)footerLayout.findViewById(R.id.drop_down_list_footer_progress_bar);
        addFooterView(footerLayout);
    }

    /**
     * @return isDropDownStyle
     */
    public boolean isDropDownStyle() {
        return isDropDownStyle;
    }

    /**
     * @param isDropDownStyle
     */
    public void setDropDownStyle(boolean isDropDownStyle) {
        if (this.isDropDownStyle != isDropDownStyle) {
            this.isDropDownStyle = isDropDownStyle;
            initDropDownStyle();
        }
    }

    /**
     * @return isOnBottomStyle
     */
    public boolean isOnBottomStyle() {
        return isOnBottomStyle;
    }

    /**
     * @param isOnBottomStyle
     */
    public void setOnBottomStyle(boolean isOnBottomStyle) {
        if (this.isOnBottomStyle != isOnBottomStyle) {
            this.isOnBottomStyle = isOnBottomStyle;
            initOnBottomStyle();
        }
    }

    /**
     * @return isAutoLoadOnBottom
     */
    public boolean isAutoLoadOnBottom() {
        return isAutoLoadOnBottom;
    }

    /**
     * set whether auto load when on bottom
     * 
     * @param isAutoLoadOnBottom
     */
    public void setAutoLoadOnBottom(boolean isAutoLoadOnBottom) {
        this.isAutoLoadOnBottom = isAutoLoadOnBottom;
    }

    /**
     * get whether show footer loading progress bar when loading
     * 
     * @return the isShowFooterProgressBar
     */
    public boolean isShowFooterProgressBar() {
        return isShowFooterProgressBar;
    }

    /**
     * set whether show footer loading progress bar when loading
     * 
     * @param isShowFooterProgressBar
     */
    public void setShowFooterProgressBar(boolean isShowFooterProgressBar) {
        this.isShowFooterProgressBar = isShowFooterProgressBar;
    }

    /**
     * get isShowFooterWhenNoMore
     * 
     * @return the isShowFooterWhenNoMore
     */
    public boolean isShowFooterWhenNoMore() {
        return isShowFooterWhenNoMore;
    }

    /**
     * set isShowFooterWhenNoMore
     * 
     * @param isShowFooterWhenNoMore the isShowFooterWhenNoMore to set
     */
    public void setShowFooterWhenNoMore(boolean isShowFooterWhenNoMore) {
        this.isShowFooterWhenNoMore = isShowFooterWhenNoMore;
    }

    /**
     * get footer button
     * 
     * @return
     */
    public Button getFooterButton() {
        return footerButton;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        if (isDropDownStyle) {
            setSecondPositionVisible();
        }
    }

    @Override
    public void setOnScrollListener(AbsListView.OnScrollListener listener) {
        onScrollListener = listener;
    }

    /**
     * @param onDropDownListener
     */
    public void setOnDropDownListener(OnDropDownListener onDropDownListener) {
        this.onDropDownListener = onDropDownListener;
    }

    /**
     * @param onBottomListener
     */
    public void setOnBottomListener(OnClickListener onBottomListener) {
        footerButton.setOnClickListener(onBottomListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isDropDownStyle) {
            return super.onTouchEvent(event);
        }

        hasReachedTop = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionDownPointY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                adjustHeaderPadding(event);
                break;
            case MotionEvent.ACTION_UP:
                if (!isVerticalScrollBarEnabled()) {
                    setVerticalScrollBarEnabled(true);
                }
                /**
                 * set status when finger leave screen if first item visible and header status is not
                 * HEADER_STATUS_LOADING
                 * <ul>
                 * <li>if current header status is HEADER_STATUS_RELEASE_TO_LOAD, call onDropDown.</li>
                 * <li>if current header status is HEADER_STATUS_DROP_DOWN_TO_LOAD, then set header status to
                 * HEADER_STATUS_CLICK_TO_LOAD and hide header layout.</li>
                 * </ul>
                 */
                if (getFirstVisiblePosition() == 0 && currentHeaderStatus != HEADER_STATUS_LOADING) {
                    switch (currentHeaderStatus) {
                        case HEADER_STATUS_RELEASE_TO_LOAD:
                            onDropDown();
                            break;
                        case HEADER_STATUS_DROP_DOWN_TO_LOAD:
                            setHeaderStatusClickToLoad();
                            setSecondPositionVisible();
                            break;
                        case HEADER_STATUS_CLICK_TO_LOAD:
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
        if (isDropDownStyle) {
            if (currentScrollState == SCROLL_STATE_TOUCH_SCROLL && currentHeaderStatus != HEADER_STATUS_LOADING) {
                /**
                 * when state of ListView is SCROLL_STATE_TOUCH_SCROLL(ListView is scrolling and finger is on screen)
                 * and header status is not HEADER_STATUS_LOADING
                 * <ul>
                 * if header layout is visiable,
                 * <li>if height of header is higher than a fixed value, then set header status to
                 * HEADER_STATUS_RELEASE_TO_LOAD.</li>
                 * <li>else set header status to HEADER_STATUS_DROP_DOWN_TO_LOAD.</li>
                 * </ul>
                 * <ul>
                 * if header layout is not visiable,
                 * <li>set header status to HEADER_STATUS_CLICK_TO_LOAD.</li>
                 * </ul>
                 */
                if (firstVisibleItem == 0) {
                    headerImage.setVisibility(View.VISIBLE);
                    int pointBottom = headerOriginalHeight + headerReleaseMinDistance;
                    if (headerLayout.getBottom() >= pointBottom) {
                        setHeaderStatusReleaseToLoad();
                    } else if (headerLayout.getBottom() < pointBottom) {
                        setHeaderStatusDropDownToLoad();
                    }
                } else {
                    setHeaderStatusClickToLoad();
                }
            } else if (currentScrollState == SCROLL_STATE_FLING && firstVisibleItem == 0
                    && currentHeaderStatus != HEADER_STATUS_LOADING) {
                /**
                 * when state of ListView is SCROLL_STATE_FLING(ListView is scrolling but finger has leave screen) and
                 * first item(header layout) is visible and header status is not HEADER_STATUS_LOADING, then hide first
                 * item, set second item visible and set hasReachedTop true.
                 */
                setSecondPositionVisible();
                hasReachedTop = true;
            } else if (currentScrollState == SCROLL_STATE_FLING && hasReachedTop) {
                /**
                 * when state of ListView is SCROLL_STATE_FLING(ListView is scrolling but finger has leave screen) and
                 * hasReachedTop is true(it's because flick back), then hide first item, set second item visible.
                 */
                setSecondPositionVisible();
            }
        }

        // if isOnBottomStyle and isAutoLoadOnBottom and hasMore, then call onBottom function auto
        if (isOnBottomStyle && isAutoLoadOnBottom && hasMore) {
            if (firstVisibleItem > 0 && totalItemCount > 0 && (firstVisibleItem + visibleItemCount == totalItemCount)) {
                onBottom();
            }
        }
        if (onScrollListener != null) {
            onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isDropDownStyle) {
            currentScrollState = scrollState;

            if (currentScrollState == SCROLL_STATE_IDLE) {
                hasReachedTop = false;
            }
        }

        if (onScrollListener != null) {
            onScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    /**
     * drop down begin, adjust view status
     */
    private void onDropDownBegin() {
        if (isDropDownStyle) {
            setHeaderStatusLoading();
        }
    }

    /**
     * on drop down loading, you can call it by manual, but you should manual call onBottomComplete at the same time.
     */
    public void onDropDown() {
        if (currentHeaderStatus != HEADER_STATUS_LOADING && isDropDownStyle && onDropDownListener != null) {
            onDropDownBegin();
            onDropDownListener.onDropDown();
        }
    }

    /**
     * drop down complete, restore view status
     * 
     * @param secondText display below header text, if null, not display
     */
    public void onDropDownComplete(CharSequence secondText) {
        if (isDropDownStyle) {
            setHeaderSecondText(secondText);
            onDropDownComplete();
        }
    }

    /**
     * set header second text
     * 
     * @param secondText secondText display below header text, if null, not display
     */
    public void setHeaderSecondText(CharSequence secondText) {
        if (isDropDownStyle) {
            if (secondText == null) {
                headerSecondText.setVisibility(View.GONE);
            } else {
                headerSecondText.setVisibility(View.VISIBLE);
                headerSecondText.setText(secondText);
            }
        }
    }

    /**
     * drop down complete, restore view status
     */
    public void onDropDownComplete() {
        if (isDropDownStyle) {
            setHeaderStatusClickToLoad();

            if (headerLayout.getBottom() > 0) {
                invalidateViews();
                setSecondPositionVisible();
            }
        }
    }

    /**
     * on bottom begin, adjust view status
     */
    private void onBottomBegin() {
        if (isOnBottomStyle) {
            if (isShowFooterProgressBar) {
                footerProgressBar.setVisibility(View.VISIBLE);
            }
            footerButton.setText(footerLoadingText);
            footerButton.setEnabled(false);
        }
    }

    /**
     * on bottom loading, you can call it by manual, but you should manual call onBottomComplete at the same time.
     */
    public void onBottom() {
        if (isOnBottomStyle && !isOnBottomLoading) {
            isOnBottomLoading = true;
            onBottomBegin();
            footerButton.performClick();
        }
    }

    /**
     * on bottom load complete, restore view status
     */
    public void onBottomComplete() {
        if (isOnBottomStyle) {
            if (isShowFooterProgressBar) {
                footerProgressBar.setVisibility(View.GONE);
            }
            if (!hasMore) {
                footerButton.setText(footerNoMoreText);
                footerButton.setEnabled(false);
                if (!isShowFooterWhenNoMore) {
                    removeFooterView(footerLayout);
                }
            } else {
                footerButton.setText(footerDefaultText);
                footerButton.setEnabled(true);
            }
            isOnBottomLoading = false;
        }
    }

    /**
     * OnDropDownListener, called when header released
     * 
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-5-31
     */
    public interface OnDropDownListener {

        /**
         * called when header released
         */
        public void onDropDown();
    }

    /**
     * set second position visible(index is 1), because first position is header layout
     */
    public void setSecondPositionVisible() {
        if (getAdapter() != null && getAdapter().getCount() > 0 && getFirstVisiblePosition() == 0) {
            setSelection(1);
        }
    }

    /**
     * set whether has more. if hasMore is false, onBottm will not be called when listView scroll to bottom
     * 
     * @param hasMore
     */
    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    /**
     * get whether has more
     * 
     * @return
     */
    public boolean isHasMore() {
        return hasMore;
    }

    /**
     * get header layout view
     * 
     * @return
     */
    public RelativeLayout getHeaderLayout() {
        return headerLayout;
    }

    /**
     * get footer layout view
     * 
     * @return
     */
    public RelativeLayout getFooterLayout() {
        return footerLayout;
    }

    /**
     * get rate about drop down distance and header padding top when drop down
     * 
     * @return headerPaddingTopRate
     */
    public float getHeaderPaddingTopRate() {
        return headerPaddingTopRate;
    }

    /**
     * set rate about drop down distance and header padding top when drop down
     * 
     * @param headerPaddingTopRate
     */
    public void setHeaderPaddingTopRate(float headerPaddingTopRate) {
        this.headerPaddingTopRate = headerPaddingTopRate;
    }

    /**
     * get min distance which header can release to loading
     * 
     * @return headerReleaseMinDistance
     */
    public int getHeaderReleaseMinDistance() {
        return headerReleaseMinDistance;
    }

    /**
     * set min distance which header can release to loading
     * 
     * @param headerReleaseMinDistance
     */
    public void setHeaderReleaseMinDistance(int headerReleaseMinDistance) {
        this.headerReleaseMinDistance = headerReleaseMinDistance;
    }

    /***
     * get header default text, default is R.string.drop_down_list_header_default_text
     * 
     * @return
     */
    public String getHeaderDefaultText() {
        return headerDefaultText;
    }

    /**
     * set header default text, default is R.string.drop_down_list_header_default_text
     * 
     * @param headerDefaultText
     */
    public void setHeaderDefaultText(String headerDefaultText) {
        this.headerDefaultText = headerDefaultText;
        if (headerText != null && currentHeaderStatus == HEADER_STATUS_CLICK_TO_LOAD) {
            headerText.setText(headerDefaultText);
        }
    }

    /**
     * get header pull text, default is R.string.drop_down_list_header_pull_text
     * 
     * @return
     */
    public String getHeaderPullText() {
        return headerPullText;
    }

    /**
     * set header pull text, default is R.string.drop_down_list_header_pull_text
     * 
     * @param headerPullText
     */
    public void setHeaderPullText(String headerPullText) {
        this.headerPullText = headerPullText;
    }

    /**
     * get header release text, default is R.string.drop_down_list_header_release_text
     * 
     * @return
     */
    public String getHeaderReleaseText() {
        return headerReleaseText;
    }

    /**
     * set header release text, default is R.string.drop_down_list_header_release_text
     * 
     * @param headerReleaseText
     */
    public void setHeaderReleaseText(String headerReleaseText) {
        this.headerReleaseText = headerReleaseText;
    }

    /**
     * get header loading text, default is R.string.drop_down_list_header_loading_text
     * 
     * @return
     */
    public String getHeaderLoadingText() {
        return headerLoadingText;
    }

    /**
     * set header loading text, default is R.string.drop_down_list_header_loading_text
     * 
     * @param headerLoadingText
     */
    public void setHeaderLoadingText(String headerLoadingText) {
        this.headerLoadingText = headerLoadingText;
    }

    /**
     * get footer default text, default is R.string.drop_down_list_footer_default_text
     * 
     * @return
     */
    public String getFooterDefaultText() {
        return footerDefaultText;
    }

    /**
     * set footer default text, default is R.string.drop_down_list_footer_default_text
     * 
     * @param footerDefaultText
     */
    public void setFooterDefaultText(String footerDefaultText) {
        this.footerDefaultText = footerDefaultText;
        if (footerButton != null && footerButton.isEnabled()) {
            footerButton.setText(footerDefaultText);
        }
    }

    /**
     * get footer loading text, default is R.string.drop_down_list_footer_loading_text
     * 
     * @return
     */
    public String getFooterLoadingText() {
        return footerLoadingText;
    }

    /**
     * set footer loading text, default is R.string.drop_down_list_footer_loading_text
     * 
     * @param footerLoadingText
     */
    public void setFooterLoadingText(String footerLoadingText) {
        this.footerLoadingText = footerLoadingText;
    }

    /**
     * get footer no more text, default is R.string.drop_down_list_footer_no_more_text
     * 
     * @return
     */
    public String getFooterNoMoreText() {
        return footerNoMoreText;
    }

    /**
     * set footer no more text, default is R.string.drop_down_list_footer_no_more_text
     * 
     * @param footerNoMoreText
     */
    public void setFooterNoMoreText(String footerNoMoreText) {
        this.footerNoMoreText = footerNoMoreText;
    }

    /** status which you can click to load, init satus **/
    public static final int HEADER_STATUS_CLICK_TO_LOAD     = 1;
    /**
     * status which you can drop down and then release to excute onDropDownListener, when height of header layout lower
     * than a value
     **/
    public static final int HEADER_STATUS_DROP_DOWN_TO_LOAD = 2;
    /** status which you can release to excute onDropDownListener, when height of header layout higher than a value **/
    public static final int HEADER_STATUS_RELEASE_TO_LOAD   = 3;
    /** status which is loading **/
    public static final int HEADER_STATUS_LOADING           = 4;

    /**
     * set header status to {@link #HEADER_STATUS_CLICK_TO_LOAD}
     */
    private void setHeaderStatusClickToLoad() {
        if (currentHeaderStatus != HEADER_STATUS_CLICK_TO_LOAD) {
            resetHeaderPadding();

            headerImage.clearAnimation();
            headerImage.setVisibility(View.GONE);
            headerProgressBar.setVisibility(View.GONE);
            headerText.setText(headerDefaultText);

            currentHeaderStatus = HEADER_STATUS_CLICK_TO_LOAD;
        }
    }

    /**
     * set header status to {@link #HEADER_STATUS_DROP_DOWN_TO_LOAD}
     */
    private void setHeaderStatusDropDownToLoad() {
        if (currentHeaderStatus != HEADER_STATUS_DROP_DOWN_TO_LOAD) {
            headerImage.setVisibility(View.VISIBLE);
            if (currentHeaderStatus != HEADER_STATUS_CLICK_TO_LOAD) {
                headerImage.clearAnimation();
                headerImage.startAnimation(reverseFlipAnimation);
            }
            headerProgressBar.setVisibility(View.GONE);
            headerText.setText(headerPullText);

            if (isVerticalFadingEdgeEnabled()) {
                setVerticalScrollBarEnabled(false);
            }

            currentHeaderStatus = HEADER_STATUS_DROP_DOWN_TO_LOAD;
        }
    }

    /**
     * set header status to {@link #HEADER_STATUS_RELEASE_TO_LOAD}
     */
    private void setHeaderStatusReleaseToLoad() {
        if (currentHeaderStatus != HEADER_STATUS_RELEASE_TO_LOAD) {
            headerImage.setVisibility(View.VISIBLE);
            headerImage.clearAnimation();
            headerImage.startAnimation(flipAnimation);
            headerProgressBar.setVisibility(View.GONE);
            headerText.setText(headerReleaseText);

            currentHeaderStatus = HEADER_STATUS_RELEASE_TO_LOAD;
        }
    }

    /**
     * set header status to {@link #HEADER_STATUS_LOADING}
     */
    private void setHeaderStatusLoading() {
        if (currentHeaderStatus != HEADER_STATUS_LOADING) {
            resetHeaderPadding();

            headerImage.setVisibility(View.GONE);
            headerImage.clearAnimation();
            headerProgressBar.setVisibility(View.VISIBLE);
            headerText.setText(headerLoadingText);

            currentHeaderStatus = HEADER_STATUS_LOADING;
            setSelection(0);
        }
    }

    /**
     * adjust header padding according to motion event
     * 
     * @param ev
     */
    private void adjustHeaderPadding(MotionEvent ev) {
        // adjust header padding according to motion event history
        int pointerCount = ev.getHistorySize();
        if (isVerticalFadingEdgeEnabled()) {
            setVerticalScrollBarEnabled(false);
        }
        for (int i = 0; i < pointerCount; i++) {
            if (currentHeaderStatus == HEADER_STATUS_DROP_DOWN_TO_LOAD
                    || currentHeaderStatus == HEADER_STATUS_RELEASE_TO_LOAD) {
                headerLayout
                        .setPadding(
                                headerLayout.getPaddingLeft(),
                                (int)(((ev.getHistoricalY(i) - actionDownPointY) - headerOriginalHeight) / headerPaddingTopRate),
                                headerLayout.getPaddingRight(), headerLayout.getPaddingBottom());
            }
        }
    }

    /**
     * reset header padding
     */
    private void resetHeaderPadding() {
        headerLayout.setPadding(headerLayout.getPaddingLeft(), headerOriginalTopPadding,
                headerLayout.getPaddingRight(), headerLayout.getPaddingBottom());
    }

    /**
     * measure header layout
     * 
     * @param child
     */
    private void measureHeaderLayout(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    /**
     * get attrs
     * 
     * @param context
     * @param attrs
     */
    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.drop_down_list_attr);
        isDropDownStyle = ta.getBoolean(R.styleable.drop_down_list_attr_isDropDownStyle, false);
        isOnBottomStyle = ta.getBoolean(R.styleable.drop_down_list_attr_isOnBottomStyle, false);
        isAutoLoadOnBottom = ta.getBoolean(R.styleable.drop_down_list_attr_isAutoLoadOnBottom, false);
        ta.recycle();
    }
}
