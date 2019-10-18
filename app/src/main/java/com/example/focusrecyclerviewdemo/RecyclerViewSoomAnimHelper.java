package com.example.focusrecyclerviewdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * RecyclerView移动动画帮助类;
 * 可以对RecyclerView进行指定方向指定像素慢速或者快速的滑动
 * 内部维护正向动画和反向动画两种动画
 *
 * @author liuqz
 *
 */
public class RecyclerViewSoomAnimHelper {

    private String TAG = RecyclerViewSoomAnimHelper.class.getSimpleName();
    private ValueAnimator mPositiveValueAnimator, mNegativeValueAnimtor;
    private int mPositiveSumPx = 0, mNegativeSumPx = 0;
    private int mPositiveLastRate = 0, mNegativeLastRate = 0;
    private long mPositiveLastStartTime = -1, mNegativeLastStartTime = -1;
    /**
     * 一次动画执行的时间
     */
    private long ANIM_TIME = 250;
    /**
     * 两次start Anim 的间隔时间
     */
    private long FAST_INTERVAL = 200;
    private RecyclerView mRecyclerView;
    /**
     * RecyclerView的layout 方向
     */
    private int mOrientation;
    private boolean isRunning = false;
    private AnimSpeedListener mAnimSpeedListener;

    public RecyclerViewSoomAnimHelper(RecyclerView recyclerView, int orientation){
        mRecyclerView = recyclerView;
        mOrientation = orientation;
    }

    public RecyclerViewSoomAnimHelper(RecyclerView recyclerView){
        mRecyclerView = recyclerView;
        mOrientation = LinearLayoutManager.HORIZONTAL;
    }

    public void setAnimSpeedListener(AnimSpeedListener animSpeedListener) {
        mAnimSpeedListener = animSpeedListener;
    }

    /**
     * 取消动画
     */
    public void cancelAnimtor(){
        LogX.d(TAG+" cancelAnimtor start ...");
        if (mPositiveValueAnimator != null){
            if (mPositiveValueAnimator.isRunning()){
                LogX.d(TAG+" cancelAnimtor mPositiveValueAnimator cancel");
                mPositiveValueAnimator.cancel();
            }
        }
        if (mNegativeValueAnimtor != null){
            if (mNegativeValueAnimtor.isRunning()){
                LogX.d(TAG+" cancelAnimtor mNegativeValueAnimtor cancel");
                mNegativeValueAnimtor.cancel();
            }
        }
    }

    public void setDuration(long duration){
        ANIM_TIME = duration;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 开始"正向动画";
     * @param px 需要移动的像素
     */
    public void startPositiveAnim(int px){
        long diff = -1;
        if (mPositiveLastStartTime == -1){
            mPositiveLastStartTime = System.currentTimeMillis();
        }else{
            long currentTime = System.currentTimeMillis();
            diff = currentTime - mPositiveLastStartTime;
            mPositiveLastStartTime = currentTime;
        }
        boolean isChangeAnim = mPositiveSumPx == 0 || mPositiveSumPx != px;
        LogX.e(TAG+" startPositiveAnim px : "+px+" tem : "+diff+" mPositiveSumPx : "+ mPositiveSumPx +" isChangeAnim : "+isChangeAnim);
        ValueAnimator valueAnimator = getPositiveAnimator(isChangeAnim,px);
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        mPositiveSumPx = px;
        if (diff == -1 || diff > FAST_INTERVAL) {
            LogX.d(TAG+" startPositiveAnim >"+FAST_INTERVAL+" isRunning : "+valueAnimator.isRunning());
            start(true);
        }else{
            LogX.d(TAG+" startPositiveAnim <"+FAST_INTERVAL+" isRunning : "+valueAnimator.isRunning());
            scrollRemainPx(true);
            if (mAnimSpeedListener != null){
                mAnimSpeedListener.onAnimEnd();
            }
        }
    }

    /**
     * 开始"反向动画";
     * @param px 需要移动的像素
     */
    public void startNegativeAnim(int px){
        long diff = -1;
        if (mNegativeLastStartTime == -1){
            mNegativeLastStartTime = System.currentTimeMillis();
        }else{
            long currentTime = System.currentTimeMillis();
            diff = currentTime - mNegativeLastStartTime;
            mNegativeLastStartTime = currentTime;
        }
        boolean isChangeAnim = mNegativeSumPx == 0 || mNegativeSumPx != px;
        LogX.e(TAG+" startNegativeAnim px : "+px+" diff : "+diff+" mNegativeSumPx : "+ mNegativeSumPx +" isChangeAnim : "+isChangeAnim);
        ValueAnimator valueAnimator = getNegativeAnimator(isChangeAnim,px);
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        mNegativeSumPx = px;
        mNegativeLastRate = px;
        if (diff == -1 || diff > FAST_INTERVAL) {
            LogX.d(TAG+" startNegativeAnim >"+ANIM_TIME+" isRunning : "+valueAnimator.isRunning());
            start(false);
        }else{
            LogX.d(TAG+" startNegativeAnim <"+ANIM_TIME+" isRunning : "+valueAnimator.isRunning());
            scrollRemainPx(false);
            if (mAnimSpeedListener != null){
                mAnimSpeedListener.onAnimEnd();
            }
        }
    }


    private void start(boolean isRight){
        if (isRight){
            getPositiveAnimator().start();
        }else{
            getNegativeAnimator().start();
        }
    }

    /**
     * 移动RecyclerView
     * @param value 移动的像素
     */
    private void scrollBy(int value){
        if (mRecyclerView != null){
            if (mOrientation == LinearLayoutManager.HORIZONTAL){
                mRecyclerView.scrollBy(value, 0);
            }else{
                mRecyclerView.scrollBy(0, value);
            }
            if (mAnimSpeedListener != null){
                mAnimSpeedListener.onAnimRunning(value);
            }
        }
    }

    private ValueAnimator getPositiveAnimator(){
        return getPositiveAnimator(false,-1);
    }

    /**
     * 获取到本次执行的{@link ValueAnimator}
     * @param isChangeAnim 是否需要重新创建动画对象
     * @param sumPx 需要滚动的px
     * @return {@link ValueAnimator}
     */
    private ValueAnimator getPositiveAnimator(final boolean isChangeAnim, int sumPx){
        boolean isNew = mPositiveValueAnimator == null || isChangeAnim;
        if (isNew) {
            if (mPositiveValueAnimator != null){
                if (mPositiveValueAnimator.isRunning()){
                    LogX.e(TAG+" getPositiveAnimator isRunning cancel");
                    mPositiveValueAnimator.cancel();
                }
            }
            if (sumPx != -1){
                mPositiveSumPx = sumPx;
            }
            LogX.d(TAG+" getPositiveAnimator isNew true mPositiveSumPx : "+ mPositiveSumPx);
            mPositiveValueAnimator = ValueAnimator.ofInt(0, mPositiveSumPx);
            mPositiveValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    int scrollValue = value - mPositiveLastRate;
                    LogX.d(TAG + " getPositiveAnimator onAnimationUpdate"+ " value : " + value+" mPositiveLastRate : "+ mPositiveLastRate +" scrollX : "+scrollValue);
                    mPositiveLastRate = value;
                    if (scrollValue > 0) {
                        scrollBy(scrollValue);
                    }
                }
            });
            mPositiveValueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    LogX.e(TAG + " getPositiveAnimator onAnimationCancel ");
                    scrollRemainPx(true);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    LogX.d(TAG + " getPositiveAnimator onAnimationEnd ");
                    endAnim(true);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    LogX.d(TAG + " getPositiveAnimator onAnimationStart ");
                    isRunning = true;
                }
            });
            mPositiveValueAnimator.setDuration(getAnimDuration(mPositiveSumPx));
        }
        return mPositiveValueAnimator;
    }

    private ValueAnimator getNegativeAnimator(){
        return getNegativeAnimator(false,-1);
    }

    /**
     * 获取到下次执行的{@link ValueAnimator}
     * @param isChangeAnim 是否需要重新创建动画对象
     * @param sumPx 需要滚动的px
     * @return {@link ValueAnimator}
     */
    private ValueAnimator getNegativeAnimator(final boolean isChangeAnim, int sumPx){
        boolean isNew = mNegativeValueAnimtor == null || isChangeAnim;
        if (isNew) {
            if (mNegativeValueAnimtor != null){
                if (mNegativeValueAnimtor.isRunning()){
                    LogX.e(TAG+" getNegativeAnimator isRunning cancel");
                    mNegativeValueAnimtor.cancel();
                }
            }
            if (sumPx != -1){
                mNegativeSumPx = sumPx;
                mNegativeLastRate = sumPx;
            }
            LogX.d(TAG+" getNegativeAnimator isNew true mNegativeSumPx : "+ mNegativeSumPx);
            mNegativeValueAnimtor = ValueAnimator.ofInt(mNegativeSumPx, 0);
            mNegativeValueAnimtor.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    int scrollValue = mNegativeLastRate - value;
                    LogX.d(TAG + " getNegativeAnimator onAnimationUpdate"+ " value : " + value+" mNegativeLastRate : "+ mNegativeLastRate +" scrollX : "+scrollValue);
                    mNegativeLastRate = value;
                    if (scrollValue < 0) {
                        scrollBy(scrollValue);
                    }
                }
            });
            mNegativeValueAnimtor.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    LogX.e(TAG + " getNegativeAnimator onAnimationCancel ");
                    scrollRemainPx(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    LogX.d(TAG + " getNegativeAnimator onAnimationEnd ");
                    endAnim(false);
                }
                @Override
                public void onAnimationStart(Animator animation) {
                    LogX.d(TAG + " getNegativeAnimator onAnimationStart ");
                    isRunning = true;
                }
            });
            mNegativeValueAnimtor.setDuration(getAnimDuration(mNegativeSumPx));
        }
        return mNegativeValueAnimtor;
    }

    private long getAnimDuration(int sumPx){
//        long animDuration = mOrientation == LinearLayoutManager.HORIZONTAL ? ANIM_TIME : (Math.abs(sumPx) * ANIM_TIME) / 225;
        LogX.d(TAG+" getAnimDuration sumPx : "+sumPx+" animDuration : "+ANIM_TIME);
        return ANIM_TIME;
    }

    /**
     * 结束动画
     * @param isPositive
     */
    private void endAnim(boolean isPositive){
        isRunning = false;
        if (isPositive){
            mPositiveLastRate = 0;
        }else{
            mNegativeLastRate = mNegativeSumPx;
        }
        if (mAnimSpeedListener != null){
            mAnimSpeedListener.onAnimEnd();
        }
    }


    /**
     * 滑动剩下的距离
     */
    private void scrollRemainPx(boolean isPositive){
        int scrollValue = 0;
        if (isPositive) {
            scrollValue = mPositiveSumPx - mPositiveLastRate;
            mPositiveLastRate = 0;
        }else{
            scrollValue = mNegativeLastRate;
            mNegativeLastRate = mNegativeSumPx;
        }
        if (isPositive){
            LogX.d(TAG+" scrollRemainPx RIGHT scrollX : "+scrollValue+" mPositiveLastRate : "+ mPositiveLastRate);
            if (scrollValue > 0) {
                scrollBy(scrollValue);
            }
        }else{
            if (scrollValue < 0) {
                scrollBy(scrollValue);
            }
            LogX.d(TAG+" scrollRemainPx LEFT scrollX : "+scrollValue+" mNegativeLastRate : "+ mNegativeLastRate);
        }
    }

    /**
     * 释放资源
     */
    public void destroy(){
        if (mPositiveValueAnimator != null){
            mPositiveValueAnimator.clone();
        }
        if (mNegativeValueAnimtor != null){
            mNegativeValueAnimtor.clone();
        }
        mPositiveValueAnimator = null;
        mNegativeValueAnimtor = null;
        mPositiveSumPx = 0;
        mNegativeSumPx = 0;
        mPositiveLastRate = 0;
        mNegativeLastRate = 0;
        mPositiveLastStartTime = -1;
        mNegativeLastStartTime = -1;
    }

    /**
     * 动画进度监听
     */
    public interface AnimSpeedListener{

        /**
         * 动画执行回调
         * @param px
         */
        public void onAnimRunning(int px);

        /**
         * 动画结束
         */
        public void onAnimEnd();

    }

}
