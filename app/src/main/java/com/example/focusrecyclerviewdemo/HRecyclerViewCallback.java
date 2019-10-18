package com.example.focusrecyclerviewdemo;

import android.view.View;

/**
 * @author liuqz
 * @date : 2019/10/14 16:34
 */
public interface HRecyclerViewCallback {

    View getStartView();

    boolean isNoScrolling();

    int getViewHeight();

    int getPosition();

    boolean isAllShow();

    void checkFocus();

}
