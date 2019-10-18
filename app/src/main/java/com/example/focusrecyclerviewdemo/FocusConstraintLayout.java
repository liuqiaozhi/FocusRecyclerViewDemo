package com.example.focusrecyclerviewdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * 首页的{@link ConstraintLayout}
 * 按下选择焦点的时候，会选择RecyclerView的第一行第一个View作为Focus View
 * @author liuqz
 */
public class FocusConstraintLayout extends ConstraintLayout {

    private String TAG = FocusConstraintLayout.class.getSimpleName();
    private FocusCallBack mFocusCallBack;

    public FocusCallBack getFocusCallBack() {
        return mFocusCallBack;
    }

    public void setFocusCallBack(FocusCallBack focusCallBack) {
        mFocusCallBack = focusCallBack;
    }

    public interface FocusCallBack{
        public View getNextDownFocusView();
        public boolean isTopFocusView(View view);
    }

    public FocusConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public View focusSearch(View focused, int direction) {
        LogX.d(TAG+" focusSearch direction : "+direction);
        if (direction == FOCUS_DOWN){
            if (mFocusCallBack != null){
                LogX.d(TAG+" focusSearch FOCUS_DOWN isTopFocusView : "+mFocusCallBack.isTopFocusView(focused));
                if (mFocusCallBack.isTopFocusView(focused)){
                    View nextView = mFocusCallBack.getNextDownFocusView();
                    LogX.d(TAG+" focusSearch FOCUS_DOWN nextView : "+nextView);
                    if (nextView != null){
                        return nextView;
                    }
                }
            }
        }
        return super.focusSearch(focused, direction);
    }

    @Override
    public View focusSearch(int direction) {
        LogX.d(TAG+" focusSearch1 direction : "+direction);
        return super.focusSearch(direction);
    }
}
