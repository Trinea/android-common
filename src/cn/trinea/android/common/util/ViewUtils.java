package cn.trinea.android.common.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * ViewUtils
 * <ul>
 * <strong>get view height</strong>
 * <li>{@link ViewUtils#getListViewHeightBasedOnChildren(ListView)}</li>
 * <li>{@link ViewUtils#getAbsListViewHeightBasedOnChildren(AbsListView)}</li>
 * </ul>
 * <ul>
 * <strong>set view height</strong>
 * <li>{@link ViewUtils#setViewHeight(View, int)} set view height</li>
 * <li>{@link ViewUtils#setListViewHeightBasedOnChildren(ListView)}</li>
 * <li>{@link ViewUtils#setAbsListViewHeightBasedOnChildren(AbsListView)}</li>
 * </ul>
 * <ul>
 * <strong>get other info</strong>
 * <li>{@link ViewUtils#getGridViewVerticalSpacing(GridView)} get GridView vertical spacing</li>
 * </ul>
 * <ul>
 * <strong>set other info</strong>
 * <li>{@link ViewUtils#setSearchViewOnClickListener(View, OnClickListener)}</li>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-12-24
 */
public class ViewUtils {

    private ViewUtils() {
        throw new AssertionError();
    }

    /**
     * get ListView height according to every children
     * 
     * @param view
     * @return
     */
    public static int getListViewHeightBasedOnChildren(ListView view) {
        int height = getAbsListViewHeightBasedOnChildren(view);
        ListAdapter adapter;
        int adapterCount;
        if (view != null && (adapter = view.getAdapter()) != null && (adapterCount = adapter.getCount()) > 0) {
            height += view.getDividerHeight() * (adapterCount - 1);
        }
        return height;
    }

    // /**
    // * get GridView height according to every children
    // *
    // * @param view
    // * @return
    // */
    // public static int getGridViewHeightBasedOnChildren(GridView view) {
    // int height = getAbsListViewHeightBasedOnChildren(view);
    // ListAdapter adapter;
    // int adapterCount, numColumns = getGridViewNumColumns(view);
    // if (view != null && (adapter = view.getAdapter()) != null && (adapterCount = adapter.getCount()) > 0
    // && numColumns > 0) {
    // int rowCount = (int)Math.ceil(adapterCount / (double)numColumns);
    // height = rowCount * (height / adapterCount + getGridViewVerticalSpacing(view));
    // }
    // return height;
    // }
    //
    // /**
    // * get GridView columns number
    // *
    // * @param view
    // * @return
    // */
    // public static int getGridViewNumColumns(GridView view) {
    // if (view == null || view.getChildCount() <= 0) {
    // return 0;
    // }
    // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    // return getNumColumnsCompat11(view);
    //
    // } else {
    // int columns = 0;
    // int children = view.getChildCount();
    // if (children > 0) {
    // int width = view.getChildAt(0).getMeasuredWidth();
    // if (width > 0) {
    // columns = view.getWidth() / width;
    // }
    // }
    // return columns;
    // }
    // }
    //
    // @TargetApi(11)
    // private static int getNumColumnsCompat11(GridView view) {
    // return view.getNumColumns();
    // }

    private static final String CLASS_NAME_GRID_VIEW        = "android.widget.GridView";
    private static final String FIELD_NAME_VERTICAL_SPACING = "mVerticalSpacing";

    /**
     * get GridView vertical spacing
     * 
     * @param view
     * @return
     */
    public static int getGridViewVerticalSpacing(GridView view) {
        // get mVerticalSpacing by android.widget.GridView
        Class<?> demo = null;
        int verticalSpacing = 0;
        try {
            demo = Class.forName(CLASS_NAME_GRID_VIEW);
            Field field = demo.getDeclaredField(FIELD_NAME_VERTICAL_SPACING);
            field.setAccessible(true);
            verticalSpacing = (Integer)field.get(view);
            return verticalSpacing;
        } catch (Exception e) {
            /**
             * accept all exception, include ClassNotFoundException, NoSuchFieldException, InstantiationException,
             * IllegalArgumentException, IllegalAccessException, NullPointException
             */
            e.printStackTrace();
        }
        return verticalSpacing;
    }

    /**
     * get AbsListView height according to every children
     * 
     * @param view
     * @return
     */
    public static int getAbsListViewHeightBasedOnChildren(AbsListView view) {
        ListAdapter adapter;
        if (view == null || (adapter = view.getAdapter()) == null) {
            return 0;
        }

        int height = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View item = adapter.getView(i, null, view);
            if (item instanceof ViewGroup) {
                item.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }
            item.measure(0, 0);
            height += item.getMeasuredHeight();
        }
        height += view.getPaddingTop() + view.getPaddingBottom();
        return height;
    }

    /**
     * set view height
     * 
     * @param view
     * @param height
     */
    public static void setViewHeight(View view, int height) {
        if (view == null) {
            return;
        }

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
    }

    // /**
    // * set GistView height which is calculated by {@link # getGridViewHeightBasedOnChildren(GridView)}
    // *
    // * @param view
    // * @return
    // */
    // public static void setGridViewHeightBasedOnChildren(GridView view) {
    // setViewHeight(view, getGridViewHeightBasedOnChildren(view));
    // }

    /**
     * set ListView height which is calculated by {@link # getListViewHeightBasedOnChildren(ListView)}
     * 
     * @param view
     * @return
     */
    public static void setListViewHeightBasedOnChildren(ListView view) {
        setViewHeight(view, getListViewHeightBasedOnChildren(view));
    }

    /**
     * set AbsListView height which is calculated by {@link # getAbsListViewHeightBasedOnChildren(AbsListView)}
     * 
     * @param view
     * @return
     */
    public static void setAbsListViewHeightBasedOnChildren(AbsListView view) {
        setViewHeight(view, getAbsListViewHeightBasedOnChildren(view));
    }

    /**
     * set SearchView OnClickListener
     * 
     * @param v
     * @param listener
     */
    public static void setSearchViewOnClickListener(View v, OnClickListener listener) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)v;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = group.getChildAt(i);
                if (child instanceof LinearLayout || child instanceof RelativeLayout) {
                    setSearchViewOnClickListener(child, listener);
                }

                if (child instanceof TextView) {
                    TextView text = (TextView)child;
                    text.setFocusable(false);
                }
                child.setOnClickListener(listener);
            }
        }
    }

    /**
     * get descended views from parent.
     * 
     * @param parent
     * @param filter Type of views which will be returned.
     * @param includeSubClass Whether returned list will include views which are subclass of filter or not.
     * @return
     */
    public static <T extends View> List<T> getDescendants(ViewGroup parent, Class<T> filter, boolean includeSubClass) {
        List<T> descendedViewList = new ArrayList<T>();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            Class<? extends View> childsClass = child.getClass();
            if ((includeSubClass && filter.isAssignableFrom(childsClass))
                    || (!includeSubClass && childsClass == filter)) {
                descendedViewList.add(filter.cast(child));
            }
            if (child instanceof ViewGroup) {
                descendedViewList.addAll(getDescendants((ViewGroup)child, filter, includeSubClass));
            }
        }
        return descendedViewList;
    }
}
