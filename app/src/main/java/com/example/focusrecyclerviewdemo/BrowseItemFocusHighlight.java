package com.example.focusrecyclerviewdemo;

import android.animation.TimeAnimator;
import android.content.res.Resources;
import android.os.Build;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.RequiresApi;
import androidx.leanback.graphics.ColorOverlayDimmer;
import androidx.leanback.widget.ShadowOverlayContainer;
import androidx.leanback.widget.ShadowOverlayHelper;

/**
 * view的焦点状态,摘自androidx.leanback内部类
 * 增加了焦点状态时,对{@link View#setY(float)}的改变
 * @author liuqz
 */
public class BrowseItemFocusHighlight {
    private static String TAG = BrowseItemFocusHighlight.class.getSimpleName();

    public static BrowseItemFocusHighlight newInstance(){
       return new BrowseItemFocusHighlight(ZOOM_FACTOR_LARGE, false);
    }

    static boolean isValidZoomIndex(int zoomIndex) {
        return zoomIndex == ZOOM_FACTOR_NONE || getResId(zoomIndex) > 0;
    }

    static int getResId(int zoomIndex) {
        switch (zoomIndex) {
            case ZOOM_FACTOR_SMALL:
                return R.fraction.lb_focus_zoom_factor_large;
            case ZOOM_FACTOR_XSMALL:
                return R.fraction.lb_focus_zoom_factor_xsmall;
            case ZOOM_FACTOR_MEDIUM:
                return R.fraction.lb_focus_zoom_factor_medium;
            case ZOOM_FACTOR_LARGE:
                return R.fraction.lb_focus_zoom_factor_large;
            default:
                return 0;
        }
    }

    private static final int DURATION_MS = 150;

    private int mScaleIndex;
    private final boolean mUseDimmer;
    public BrowseItemFocusHighlight(int zoomIndex, boolean useDimmer) {
        if (!isValidZoomIndex(zoomIndex)) {
            throw new IllegalArgumentException("Unhandled zoom index");
        }
        mScaleIndex = zoomIndex;
        mUseDimmer = useDimmer;
    }

    private float getScale(Resources res) {
        return mScaleIndex == ZOOM_FACTOR_NONE ? 1f :
                res.getFraction(getResId(mScaleIndex), 1, 1);
    }

    public void onInitializeView(View view) {
        getOrCreateAnimator(view).animateFocus(false, true);
    }
    public void onItemFocused(View view, boolean hasFocus) {
        onItemFocused(view,hasFocus,true);
    }

    public void onItemFocused(View view, boolean hasFocus,boolean hasSelector) {
        if (hasSelector){
            view.setSelected(hasFocus);
        }
        getOrCreateAnimator(view).animateFocus(hasFocus, false);
    }

    private FocusAnimator getOrCreateAnimator(View view) {
        FocusAnimator animator = (FocusAnimator) view.getTag(R.id.lb_focus_animator);
        if (animator == null) {
            animator = new FocusAnimator(
                    view, getScale(view.getResources()), mUseDimmer, DURATION_MS);
            view.setTag(R.id.lb_focus_animator, animator);
        }
        return animator;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    static class FocusAnimator implements TimeAnimator.TimeListener {
        private final View mView;
        private final int mDuration;
        private final ShadowOverlayContainer mWrapper;
        private final float mScaleDiff;
        private float mFocusLevel = 0f;
        private float mFocusLevelStart;
        private float mFocusLevelDelta;
        private final TimeAnimator mAnimator = new TimeAnimator();
        private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
        private final ColorOverlayDimmer mDimmer;

        void animateFocus(boolean select, boolean immediate) {
            endAnimation();
            final float end = select ? 1 : 0;
            if (immediate) {
                setFocusLevel(end);
            } else if (mFocusLevel != end) {
                mFocusLevelStart = mFocusLevel;
                mFocusLevelDelta = end - mFocusLevelStart;
                mAnimator.start();
            }
        }

        FocusAnimator(View view, float scale, boolean useDimmer, int duration) {
            mView = view;
            mDuration = duration;
            mScaleDiff = scale - 1f;
            if (view instanceof ShadowOverlayContainer) {
                mWrapper = (ShadowOverlayContainer) view;
            } else {
                mWrapper = null;
            }
            mAnimator.setTimeListener(this);
            if (useDimmer) {
                mDimmer = ColorOverlayDimmer.createDefault(view.getContext());
            } else {
                mDimmer = null;
            }
        }

        void setFocusLevel(float level) {
            mFocusLevel = level;
            float scale = 1f + mScaleDiff * level;
//            mView.setPivotX(0);
//            mView.setPivotY(mView.getHeight()/2);
            mView.setScaleX(scale);
            mView.setScaleY(scale);
            mView.setZ(scale);
            if (mWrapper != null) {
                mWrapper.setShadowFocusLevel(level);
            } else {
                ShadowOverlayHelper.setNoneWrapperShadowFocusLevel(mView, level);
            }
            if (mDimmer != null) {
                mDimmer.setActiveLevel(level);
                int color = mDimmer.getPaint().getColor();
                if (mWrapper != null) {
                    mWrapper.setOverlayColor(color);
                } else {
                    ShadowOverlayHelper.setNoneWrapperOverlayColor(mView, color);
                }
            }
        }

        float getFocusLevel() {
            return mFocusLevel;
        }

        void endAnimation() {
            mAnimator.end();
        }

        @Override
        public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
            float fraction;
            if (totalTime >= mDuration) {
                fraction = 1;
                mAnimator.end();
            } else {
                fraction = (float) (totalTime / (double) mDuration);
            }
            if (mInterpolator != null) {
                fraction = mInterpolator.getInterpolation(fraction);
            }
            setFocusLevel(mFocusLevelStart + fraction * mFocusLevelDelta);
        }
    }

    /**
     * No zoom factor.
     */
    public static final int ZOOM_FACTOR_NONE = 0;

    /**
     * A small zoom factor, recommended for large item views.
     */
    public static final int ZOOM_FACTOR_SMALL = 1;

    /**
     * A medium zoom factor, recommended for medium sized item views.
     */
    public static final int ZOOM_FACTOR_MEDIUM = 2;

    /**
     * A large zoom factor, recommended for small item views.
     */
    public static final int ZOOM_FACTOR_LARGE = 3;

    /**
     * An extra small zoom factor.
     */
    public static final int ZOOM_FACTOR_XSMALL = 4;
}
