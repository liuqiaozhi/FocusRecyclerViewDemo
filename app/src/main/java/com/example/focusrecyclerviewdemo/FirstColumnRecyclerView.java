package com.example.focusrecyclerviewdemo;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 固定第一列的RecyclerView;
 * 横向RecyclerView,"劫持"了左右滑动;
 * 左右滑动时,配合{@linkplain RecyclerViewSoomAnimHelper RecyclerView动画类}
 * 进行动画滑动制定距离,达到焦点固定在左端的距离.
 * {@link #mFocusMode}表示了当前RecyclreView的焦点模式;可根据具体需求设置为不同模式.
 *
 * @author liuqz
 */
public class FirstColumnRecyclerView extends RecyclerView implements RecyclerViewSoomAnimHelper.AnimSpeedListener {

    private LinearLayoutManager mLinearLayoutManager;
    private String TAG = FirstColumnRecyclerView.class.getSimpleName();
    private final int INVALID_INDEX = -1;
    /**
     * 普通焦点模式
     */
    private final int FOCUS_NOMAL_MODE = 1;
    /**
     * 焦点固定在左边的焦点模式
     */
    private final int FOCUS_START_MODE = 2;
    /**
     * 自定义焦点模式
     */
    private final int FOCUS_CUSTOM_MODE = 3;
    private RecyclerViewSoomAnimHelper mRecyclerViewSoomAnimHelper;
    private boolean mSetParcelable = false;
    private boolean isStartPosition = true;
    private AtomicInteger mNotifyRemovedIndex = new AtomicInteger(-1);
    private int mFocusMode = FOCUS_NOMAL_MODE;

    /**
     * 设置焦点模式
     * <p>
     *     <li>{@link #FOCUS_NOMAL_MODE} 普通焦点模式
     *     <li>{@link #FOCUS_START_MODE} 焦点固定在左边的焦点模式
     *     <li>{@link #FOCUS_CUSTOM_MODE} 自定义焦点模式
     * <p/>
     * @param focusMode
     */
    private void setFocusMode(int focusMode) {
        mFocusMode = focusMode;
        if (isFocusNomal()) {
            if (mLinearLayoutManager == null) {
                mLinearLayoutManager = new FocusLinearLayoutManager(getContext(), FocusLinearLayoutManager.HORIZONTAL, false);
            }
            setLayoutManager(mLinearLayoutManager);
        } else if (isFocusStart()){
            if (mLinearLayoutManager == null) {
                mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false) {
                    @Override
                    public boolean onRequestChildFocus(@NonNull RecyclerView parent, @NonNull State state, @NonNull View child, @Nullable View focused) {
                        return true;
                    }
                };
            }
            setLayoutManager(mLinearLayoutManager);
        }
    }

    private final ItemAnimator.ItemAnimatorFinishedListener mOnAnimationsFinishedListener = new ItemAnimator.ItemAnimatorFinishedListener() {
        @Override
        public void onAnimationsFinished() {
            if (!isComputingLayout() && isLayoutFrozen()) {
                setLayoutFrozen(false);
                requestLayout();
            }
        }
    };

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
     * 设置{@link #FOCUS_NOMAL_MODE}焦点模式和
     * {@link #FOCUS_START_MODE}焦点模式
     * <p>
     *    <li> isNomal == true 为 {@link #FOCUS_NOMAL_MODE}
     *    <li> isNomal == false 为 {@link #FOCUS_START_MODE}
     * <p/>
     * @param isNomal
     */
    public void setNomalMode(boolean isNomal){
        setFocusMode(isNomal ? FOCUS_NOMAL_MODE : FOCUS_START_MODE);
    }

    /**
     * 设置{@link #FOCUS_CUSTOM_MODE}焦点模式
     */
    public void setCustomMode(){
        setFocusMode(FOCUS_CUSTOM_MODE);
    }

    private boolean isFocusNomal(){
        return mFocusMode == FOCUS_NOMAL_MODE;
    }

    private boolean isFocusStart(){
        return mFocusMode == FOCUS_START_MODE;
    }

    @Override
    public void onAnimRunning(int px) {

    }

    @Override
    public void onAnimEnd() {
        if (mNotifyRemovedIndex.get()>-1){
            getAdapter().notifyItemRemoved(mNotifyRemovedIndex.get());
            mNotifyRemovedIndex.set(-1);
        }
    }

    public interface FocusCallBack{
        /**
         * 获取下一步的焦点View
         * @return focus View
         */
        View getFocusView(int position);
    }

    private FocusCallBack mFocusCallBack;

    public void setFocusCallBack(FocusCallBack focusCallBack) {
        mFocusCallBack = focusCallBack;
    }

    public FirstColumnRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRecyclerViewSoomAnimHelper = new RecyclerViewSoomAnimHelper(this);
        mRecyclerViewSoomAnimHelper.setAnimSpeedListener(this);
        setHasFixedSize(true);
        setItemViewCacheSize(10);
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);
        revertFocusState();
    }

    @Override
    public void swapAdapter(@Nullable Adapter adapter, boolean removeAndRecycleExistingViews) {
        super.swapAdapter(adapter, removeAndRecycleExistingViews);
        revertFocusState();
    }

    public void revertFocusState(){
        mSetParcelable = false;
        isStartPosition = true;
    }

    /**
     * 删除某一项,当时最后一项时,需往左移动一项,让上一项获取焦点,再删除该项
     * @param position
     */
    public void notifyItemRemoved(int position){
        int px = getChildAt(1).getWidth();
        getChildAt(0).requestFocus();
        LogX.d(TAG+" notifyItemRemoved position : "+position+" px : "+px+" getCount : "+getChildCount());
        if (mRecyclerViewSoomAnimHelper.isRunning()){
            mRecyclerViewSoomAnimHelper.cancelAnimtor();
        }
        mRecyclerViewSoomAnimHelper.startNegativeAnim(-px);
        mNotifyRemovedIndex.set(position);
    }

    public void onCustomRestoreInstanceState(Parcelable state){
        LogX.d(TAG+" onCustomRestoreInstanceState state != null : "+(state != null)+" this : "+this);
        if (getLayoutManager() != null){
            getLayoutManager().onRestoreInstanceState(state);
        }
        if (state != null){
            mSetParcelable = true;
        }
    }

    /**
     * 检查焦点
     */
    public void checkFocus(){
        LogX.d(TAG+" checkFocus start ... count : "+getChildCount());
        if (getChildCount() <= 0 || isFocusNomal()){
            return;
        }
        int currentFocusIndex = getFocusedChildIndex();
        if (currentFocusIndex == INVALID_INDEX || currentFocusIndex > 1){
            LogX.d(TAG+" checkFocus overtake error");
            View view = getChildAt(isInitialState() ? 0 : 1);
            if (view != null){
                view.requestFocus();
            }
        }else{
            if (isInitialState() && currentFocusIndex != 0) {
                LogX.d(TAG + " checkFocus position error");
                View view = getChildAt(0);
                if (view != null) {
                    view.requestFocus();
                }
            } else if (!isInitialState() && currentFocusIndex != 1) {
                LogX.d(TAG + " checkFocus position error");
                View view = getChildAt(1);
                if (view != null) {
                    view.requestFocus();
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
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    public boolean isShouldSetParcelable(){
        return !isStartPosition;
    }

    public boolean isInitialState(){
        boolean result = !mSetParcelable && isStartPosition;
        LogX.d(TAG+" isInitialState result : "+result+" this : "+this);
        return result;
    }

    @Override
    public View focusSearch(int direction) {
        LogX.d(TAG+" focusSearch1 direction : "+direction);
        return super.focusSearch(direction);
    }

    @Override
    public View focusSearch(View focused, int direction) {
        LogX.d(TAG+" focusSearch direction : "+direction+" isFocusNomal() : "+isFocusNomal()+" isFocusStart() : "+isFocusStart()
        +" focused : "+focused);
        if (direction == FOCUS_UP ){
            if (focused instanceof HRecyclerViewItemViewCallback) {
                HRecyclerViewItemViewCallback itemLayout = (HRecyclerViewItemViewCallback) focused;
                LogX.d(TAG+" focusSearch mFocusCallBack != null : "+(mFocusCallBack != null)+" itemLayout.getPosition() : "+itemLayout.getParentPosition());
                if (itemLayout.getParentPosition() == 0 && mFocusCallBack != null) {
                    View nextView = mFocusCallBack.getFocusView(itemLayout.getParentPosition());
                    LogX.d(TAG+" focusSearch nextView == null : "+(nextView == null));
                    if (nextView != null) {
                        return nextView;
                    }
                }
            }
        } else if(direction == FOCUS_RIGHT){
            if (isFocusNomal()){
                if (focused instanceof HRecyclerViewItemViewCallback) {
                    HRecyclerViewItemViewCallback focusedItemView = (HRecyclerViewItemViewCallback) focused;
                    if (focusedItemView.isLastPosition()){
                        return focused;
                    }
                }
            }else if (isFocusStart()){
                if (focused instanceof HRecyclerViewItemViewCallback) {
                    HRecyclerViewItemViewCallback focusedItemView = (HRecyclerViewItemViewCallback) focused;
                    if (!focusedItemView.isLastPosition()) {
                        View nextView = super.focusSearch(focused, direction);
                        if (nextView instanceof HRecyclerViewItemViewCallback) {
                            HRecyclerViewItemViewCallback nextItemView = (HRecyclerViewItemViewCallback) nextView;
//                            if (focusedItemView.getHomeType() != null && nextItemView.getHomeType() != null
//                                    && focusedItemView.getHomeType() == nextItemView.getHomeType()) {
                                changeStartPosition(nextItemView.isFirstPosition());
                                mRecyclerViewSoomAnimHelper.startPositiveAnim(focused.getWidth());
                                return nextView;
//                            }
                        }
                    }
                }
                return focused;
            }
        } else if(direction == FOCUS_LEFT){
            if (isFocusNomal()) {
                if (focused instanceof HRecyclerViewItemViewCallback) {
                    HRecyclerViewItemViewCallback focusedItemView = (HRecyclerViewItemViewCallback) focused;
                    if (focusedItemView.isFirstPosition()){
                        return focused;
                    }
                }
            }else if (isFocusStart()){
                if (focused instanceof HRecyclerViewItemViewCallback) {
                    HRecyclerViewItemViewCallback focusedItemView = (HRecyclerViewItemViewCallback) focused;
                    if (focusedItemView.isFirstPosition()) {
                        return focused;
                    }
                    View nextView = super.focusSearch(focused, direction);
                    if (nextView instanceof HRecyclerViewItemViewCallback) {
                        HRecyclerViewItemViewCallback nextItemView = (HRecyclerViewItemViewCallback) nextView;
                        changeStartPosition(nextItemView.isFirstPosition());
                        mRecyclerViewSoomAnimHelper.startNegativeAnim(-nextView.getWidth());
                        return nextView;
                    }
                    return focused;
                }
            }
        }
        return super.focusSearch(focused, direction);
    }

    private void changeStartPosition(boolean isNextStartPosition){
        if (mSetParcelable){
            if (isNextStartPosition){
                mSetParcelable = false;
            }
        }
        isStartPosition = isNextStartPosition;
    }


    public void destroy(){
        mSetParcelable = false;
        isStartPosition = true;
        mLinearLayoutManager = null;
        mFocusMode = FOCUS_NOMAL_MODE;
        if (mRecyclerViewSoomAnimHelper != null){
            mRecyclerViewSoomAnimHelper.destroy();
        }
    }
}
