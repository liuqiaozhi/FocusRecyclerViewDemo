package com.example.focusrecyclerviewdemo;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * @author liuqz
 * @date : 2019/10/15 10:42
 */
public class HRecyclerViewItemView extends ConstraintLayout implements HRecyclerViewItemViewCallback{

    private TextView mTvContent;
    private TextView mTvBg;
    private DisplayMetrics metrics;
    private String mStr;
    private int mParentPosition = -1;
    private int mWidth = -1;
    private int mHeight = -1;
    private boolean isInit = true;

    public HRecyclerViewItemView(Context context) {
        super(context);
    }

    public HRecyclerViewItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        init(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init(getContext());
    }

    private void init(Context context){

//        LayoutInflater.from(context).inflate(R.layout.view_recyclerview_item,this,true);
        setClipChildren(false);
        setClipToPadding(false);
        initView();
        setFocusable(true);
        mTvBg.setBackgroundResource(R.drawable.selector_orange_frame);
     /*   WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);*/

    }

    public void setFocusState(boolean hasFocus){
        mTvContent.setTextColor(hasFocus ? Color.RED : Color.BLACK);
    }

    private void initView(){
        mTvContent = findViewById(R.id.item_tv_content);
        mTvBg = findViewById(R.id.item_tv_bg);
    }

    public void setParentPosition(int parentPosition) {
        mParentPosition = parentPosition;
    }

    public void setLayoutSize(int width, int height){
        setLayoutSize(width,height,Utils.getDimension(getContext(), R.dimen.home_item_recycler_padding_bottom));
    }

    public void setLayoutSize(int width, int height, int paddingBottom){
        if (mWidth == -1){
            mWidth = width;
        }
        if (mHeight == -1){
            mHeight = height;
        }
        ViewGroup.LayoutParams layoutParams2 = getLayoutParams();
        layoutParams2.width = width;
        layoutParams2.height = height + paddingBottom;
        setLayoutParams(layoutParams2);

        ViewGroup.LayoutParams layoutParams = mTvBg.getLayoutParams();
        layoutParams.width = mWidth;
        layoutParams.height = height;
        mTvBg.setLayoutParams(layoutParams);
    }

    public void bind(String str){
        isInit = false;
        setLayoutSize(Utils.getDimension(getContext(),R.dimen.home_item_item_type1_width)
                ,Utils.getDimension(getContext(),R.dimen.home_item_item_type1_height));
        mStr = str;
        mTvContent.setText(str);
    }

    public void bindEmpty(){
        setLayoutSize(Utils.getDimension(getContext(),R.dimen.home_item_item_type1_width)
                ,Utils.getDimension(getContext(),R.dimen.home_item_item_type1_height));
    }

    public void unBind(){
        isInit = true;
        mParentPosition = -1;
        mWidth = -1;
        mHeight = -1;
    }

    @Override
    public HRecyclerViewCallback getParentCallback() {
        HRecyclerViewCallback result = null;
        if (getParentViewGroup(this) instanceof FirstColumnRecyclerView){
            result = (HRecyclerViewCallback)getParentViewGroup(getParentViewGroup(this));
        }
        return result;
    }

    private ViewGroup getParentViewGroup(View view){
        if (view != null && view.getParent() != null && view.getParent() instanceof ViewGroup){
            return (ViewGroup)view.getParent();
        }
        return null;
    }

    @Override
    public int getParentPosition() {
        return mParentPosition;
    }

    @Override
    public boolean isLastPosition() {
        return mStr.endsWith("_99");
    }

    @Override
    public boolean isFirstPosition() {
        return mStr.endsWith("_0");
    }
}
