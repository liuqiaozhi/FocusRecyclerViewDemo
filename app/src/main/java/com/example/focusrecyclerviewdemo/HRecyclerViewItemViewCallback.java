package com.example.focusrecyclerviewdemo;

/**
 * @author liuqz
 * @date : 2019/10/14 16:44
 */
public interface HRecyclerViewItemViewCallback {

    HRecyclerViewCallback getParentCallback();

    int getParentPosition();

    boolean isLastPosition();

    boolean isFirstPosition();
}
