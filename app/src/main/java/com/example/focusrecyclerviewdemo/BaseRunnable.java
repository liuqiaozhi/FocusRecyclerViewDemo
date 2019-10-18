package com.example.focusrecyclerviewdemo;

import java.lang.ref.WeakReference;

/**
 *
 * @author liuqz
 * @date 2018/12/15
 */

public abstract class BaseRunnable<T> implements Runnable {

    protected abstract void work();

    private final WeakReference<T> mWeakReference;

    protected T getContent(){
        return mWeakReference.get();
    }

    public boolean isEmptyContent(){
        return mWeakReference.get()==null;
    }

    public BaseRunnable(T weakReference){
        mWeakReference = new WeakReference<T>(weakReference);
    }

    @Override
    public void run() {
        T content = mWeakReference.get();
        if(content!=null){
            this.work();
        }
    }
}
