package com.example.focusrecyclerviewdemo;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 固定第一行的RecyclerView;
 * 纵向RecyclerView,"劫持"了上下滑动;
 * @author liuqz
 */
public class FirstRowRecyclerView extends RecyclerView implements RecyclerViewSoomAnimHelper.AnimSpeedListener {

    private String TAG = FirstRowRecyclerView.class.getSimpleName();
    private final int INVALID_INDEX = -1;
    private int mPaddingHeight;
    private RecyclerViewSoomAnimHelper mRecyclerViewSoomAnimHelper;
    private long mCanMoveTime = -1L;
    private Handler mHandler;
    private RequestFocusRunnable mRequestFocusRunnable;
    private int mLastFocusPosition = INVALID_INDEX;
    private boolean isResuming = false;
    private boolean isFirstPosition = false;

    private final ItemAnimator.ItemAnimatorFinishedListener mOnAnimationsFinishedListener = new ItemAnimator.ItemAnimatorFinishedListener() {
        @Override
        public void onAnimationsFinished() {
            if (!isComputingLayout() && isLayoutFrozen()) {
                setLayoutFrozen(false);
                requestLayout();
            }
        }
    };

    public FirstRowRecyclerView(@NonNull Context context) {
        super(context);
    }

    public FirstRowRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mHandler = new Handler();
        mRequestFocusRunnable = new RequestFocusRunnable(this);
        mRecyclerViewSoomAnimHelper = new RecyclerViewSoomAnimHelper(this, LinearLayoutManager.VERTICAL);
        mRecyclerViewSoomAnimHelper.setAnimSpeedListener(this);
        mPaddingHeight = (int)(Utils.getDimension(getContext(), R.dimen.home_recycler_pading_top)/2);
//        mPaddingHeight = 0;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean onRequestChildFocus(
                    RecyclerView parent, State state, View child, View focused) {
                // This disables the default scroll behavior for focus movement.
                return true;
            }
        };
        setLayoutManager(linearLayoutManager);
//        OverFlyingLayoutManager overFlyingLayoutManager = new OverFlyingLayoutManager(OrientationHelper.VERTICAL,true);
//        setLayoutManager(overFlyingLayoutManager);
        setHasFixedSize(true);
        setItemViewCacheSize(10);
        setNestedScrollingEnabled(true);
//        setDrawingCacheEnabled(true);
    }
    @Override
    public void setLayoutManager(@Nullable LayoutManager layout) {
        super.setLayoutManager(layout);
        if (layout instanceof LinearLayoutManager){
            ((LinearLayoutManager)layout).setInitialPrefetchItemCount(6);
        }
    }

    public FirstRowRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void notifyDataSetChanged(){
        LogX.i(TAG+" notifyDataSetChanged start...isRunning : "+(mRecyclerViewSoomAnimHelper.isRunning()));
        long currentTime = System.currentTimeMillis();
        if (mRecyclerViewSoomAnimHelper.isRunning()){
            mRecyclerViewSoomAnimHelper.cancelAnimtor();
        }
        long REQUEST_DETAILY_TIME = 2500L;
        mCanMoveTime = currentTime + REQUEST_DETAILY_TIME;
        mHandler.removeCallbacksAndMessages(null);
        long SCROLL_DETAILY_TIME = 2200L;
        mHandler.postDelayed(mRequestFocusRunnable, SCROLL_DETAILY_TIME);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                LogX.e(TAG+" notifyDataSetChanged start notify...");
                getAdapter().notifyDataSetChanged();
            }
        });
    }

    /**
     * 刷新{@link #mLastFocusPosition}
     */
    public void initLastFocusIndex(){
        mLastFocusPosition = 0;
        isFirstPosition = true;
    }

    /**
     * 刷新{@link #mLastFocusPosition}
     */
    public void setEmptyLastFocusIndex(){
        mLastFocusPosition = INVALID_INDEX;
    }

    public void onPause(){
        isResuming = false;
    }

    public void onResume(){
        isResuming = true;
        checkFocus(true);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        ItemAnimator animator = getItemAnimator();
        if (animator != null && animator.isRunning()) {
            animator.isRunning(this.mOnAnimationsFinishedListener);
            setLayoutFrozen(true);
        } else if (isLayoutFrozen()) {
            setLayoutFrozen(false);
        }
    }

    /**
     * 查找当前应该获取焦点的view,以及它所在{@link RecyclerView#getChildCount()}的位置
     * @return length == 2 的数组;index 0 : 它所在{@link RecyclerView#getChildCount()}的位置;
     *                            index 1 : 当前应该获取焦点的view;
     */
    private Object[] getCheckFocusView(){
        LogX.e(TAG+" checkFocus getCheckFocusView start ...isFirstPosition : "+isFirstPosition+" getChildCount() : "+getChildCount());
        Object[] result = null;
        if (getChildCount() <= 0 || mLastFocusPosition == INVALID_INDEX){
            LogX.e(TAG+" checkFocus getCheckFocusView return ...");
            return result;
        }
        result = new Object[2];
        result[0] = -1;
        int index = isFirstPosition ? 0 : 1;
        for (int i = index; i < getChildCount(); i++){
            View view = getChildAt(i);
            if (view instanceof HRecyclerViewCallback) {
                HRecyclerViewCallback homeItemLayout = (HRecyclerViewCallback) view;
                if (homeItemLayout.isAllShow()){
                    View view1 = homeItemLayout.getStartView();
                    if (view1 != null) {
                        result[0] = index;
                        result[1] = view1;
                        break;
                    }
                }else{
//                    LogX.d(TAG+" checkFocus getCheckFocusView i : "+i+" isAllShow == false type : "
//                            +homeItemLayout.getHomeType()+" title : "+homeItemLayout.getTitle());
                }
            }
        }
        LogX.d(TAG+" checkFocus getCheckFocusView result[0] : "+result[0]+" result[1] : "+(result[1] == null ? "null" : result[1]));
        return result;
    }

    /**
     * 检查焦点
     */
    public void checkFocus(boolean isResume){
        int count = getChildCount();
        LogX.d(TAG+" checkFocus start ... count : "+count+" mLastFocusPosition : "
                +mLastFocusPosition+" isResume : "+isResume+" isResuming : "+isResuming);
        if (!isResuming || count <= 0 || mLastFocusPosition == INVALID_INDEX){
            LogX.e(TAG+" checkFocus forgo check"+" isResume : "+isResume);
            return;
        }
        Object[] objects = getCheckFocusView();
        if (objects != null && objects.length == 2){
            int temIndex = objects[0] != null ? (int)objects[0] : -1;
            View nextView = objects[1] instanceof View ? (View)objects[1] : null;
            if (nextView != null && temIndex != -1) {
                int currentIndex = getFocusedChildIndex();
                LogX.d(TAG + " checkFocus currentIndex : " + currentIndex + " mLastFocusPosition : "
                        + mLastFocusPosition + " isResume : " + isResume + " isResuming : " + isResuming + " temIndex : " + temIndex);
                if (temIndex == currentIndex) {
                    //计算出来的焦点和系统找到的焦点一致，那么检查横向的焦点
                    View view = getChildAt(currentIndex);
                    if (view instanceof HRecyclerViewCallback) {
                        HRecyclerViewCallback homeItemLayout = (HRecyclerViewCallback) view;
                        homeItemLayout.checkFocus();
                    }
                } else {
                    //计算出来的焦点和系统找到的焦点不一致，那么用找到的view获取焦点
                    mLastFocusPosition = temIndex;
                    nextView.requestFocus();
                }
            }
        }
    }

    private int getFocusedChildIndex() {
        for (int i = 0; i < getChildCount(); ++i) {
            View view = getChildAt(i);
            boolean hasFocus = view.hasFocus();
            if (hasFocus) {
                return i;
            }
        }
        return INVALID_INDEX;
    }


    @Override
    public View focusSearch(View focused, int direction) {
        if (mCanMoveTime != -1 && System.currentTimeMillis() < mCanMoveTime){
            LogX.e(TAG+" focusSearch mCanMoveTime : "+ mCanMoveTime);
            return focused;
        }
        if (direction == FOCUS_DOWN){
            if (focused instanceof HRecyclerViewItemViewCallback){
                isFirstPosition = false;
                HRecyclerViewItemViewCallback hRecyclerViewItemViewCallback = (HRecyclerViewItemViewCallback)focused;
                HRecyclerViewCallback parentCallback = hRecyclerViewItemViewCallback.getParentCallback();
                if (parentCallback!= null){
                    View nextView = super.focusSearch(focused,direction);
                    if (parentCallback.isNoScrolling()) {
                        if (nextView instanceof HRecyclerViewItemViewCallback){
                            View nextFocusView = nextView;
                            HRecyclerViewCallback nextHomeItemLayout = ((HRecyclerViewItemViewCallback)nextView).getParentCallback();;
                            if (nextHomeItemLayout != null){
                                View nextFirstView = nextHomeItemLayout.getStartView();
                                if (nextFirstView != null){
                                    nextFocusView = nextFirstView;
                                }
                            }
                            int scrollY = parentCallback.getViewHeight() + (parentCallback.getPosition()==0? mPaddingHeight : 0);
                            LogX.d(TAG + " FOCUS_DOWN scrollY : " + scrollY);
//                               scrollBy(0, scrollY);
//                               smoothScrollBy(0,scrollY);
                            mRecyclerViewSoomAnimHelper.startPositiveAnim(scrollY);
                            return nextFocusView;
                        }
                    }
                }
                return focused;
            }
        }else if(direction == FOCUS_UP){
            View nextView = super.focusSearch(focused,direction);
            View nextFocusView = nextView;
            HRecyclerViewCallback homeItemLayout = null;
            int scrollY = -1;
            if (nextView instanceof HRecyclerViewItemViewCallback) {
                HRecyclerViewItemViewCallback recyclerViewItemLayout = (HRecyclerViewItemViewCallback) nextView;
                homeItemLayout = recyclerViewItemLayout.getParentCallback();
                if (homeItemLayout != null) {
                    isFirstPosition = homeItemLayout.getPosition() == 0;
                    View nextFirstView = homeItemLayout.getStartView();
                    if (nextFirstView != null){
                        nextFocusView = nextFirstView;
                    }
                    scrollY = -homeItemLayout.getViewHeight() - (homeItemLayout.getPosition()==0? (int) (mPaddingHeight * 1.5) : 0);
                }
            }
//            LogX.d(TAG+" FOCUS_UP scrollY : "+scrollY);
            if (scrollY < -1){
//                scrollBy(0, scrollY);
//                smoothScrollBy(0,scrollY);
                mRecyclerViewSoomAnimHelper.startNegativeAnim(scrollY);
            }else{
                if (mRecyclerViewSoomAnimHelper.isRunning()){
                    return focused;
                }
            }
            return nextFocusView;
        }
        return super.focusSearch(focused, direction);
    }

    @Override
    public void onAnimRunning(int px) {

    }

    @Override
    public void onAnimEnd() {
        mLastFocusPosition = getFocusedChildIndex();
        LogX.d(TAG+" onAnimEnd mLastFocusPosition : "+mLastFocusPosition);
    }

    static class RequestFocusRunnable extends BaseRunnable<FirstRowRecyclerView> {

        RequestFocusRunnable(FirstRowRecyclerView weakReference) {
            super(weakReference);
        }

        @Override
        protected void work() {
            LogX.d("CustomRecyclerView RequestFocusRunnable work...");
            getContent().checkFocus(false);
        }
    }

}
