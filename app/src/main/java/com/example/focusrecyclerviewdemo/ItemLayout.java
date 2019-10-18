package com.example.focusrecyclerviewdemo;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * @author liuqz
 * @date : 2019/10/15 10:20
 */
public class ItemLayout extends LinearLayout implements HRecyclerViewCallback{

    private String TAG = ItemLayout.class.getSimpleName();
    private FirstColumnRecyclerView mFirstColumnRecyclerView;
    private ItemAdapter mItemAdapter;
    private int mPosition;
    private boolean mAllShow = false;

    public ItemLayout(Context context) {
        super(context);
    }

    public ItemLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
//        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init(){
//        LayoutInflater.from(getContext()).inflate(R.layout.view_home_item,this,true);
        setClipChildren(false);
        setClipToPadding(false);
        setOrientation(VERTICAL);

        mItemAdapter = new ItemAdapter();

        initView();
        setLayoutSize();
//        mFirstColumnRecyclerView.setAdapter(mItemAdapter);
    }

    private void initView(){
        mFirstColumnRecyclerView = findViewById(R.id.item_recyclerView);
        mFirstColumnRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mFirstColumnRecyclerView.setHasFixedSize(true);
    }

    public void setFocusCallBack(FirstColumnRecyclerView.FocusCallBack callBack){
        mFirstColumnRecyclerView.setFocusCallBack(callBack);
    }

    /**
     * 设置缓存池
     * @param recycledViewPool {@link RecyclerView.RecycledViewPool}
     */
    public void setRecycledViewPool(RecyclerView.RecycledViewPool recycledViewPool){
        mFirstColumnRecyclerView.setRecycledViewPool(recycledViewPool);
    }

    /**
     * 设置此ViewGroup的RecyclerView的使用模式;
     */
    public void setRecyclerModel(boolean isUseNomalRecyclerView,boolean isUseCustomLayoutManager){
        LogX.d( " focusSearch setRecyclerModel isUseNomalRecyclerView : "+isUseNomalRecyclerView+" view : "+this);
        if (isUseCustomLayoutManager){
            mFirstColumnRecyclerView.setCustomMode();
        }else{
            mFirstColumnRecyclerView.setNomalMode(isUseNomalRecyclerView);
        }
    }

    /**
     * bind设置数据
     * @param position layout position
     *
     */
    public void bind(List<String> list, int position,ItemAdapter itemAdapter){
        mAllShow = true;
//        setLayoutSize();
        setRecyclerModel(false,false);
        mPosition = position;
//        mItemAdapter.setParentPosition(position);
//        mItemAdapter.setList(list);
//        mItemAdapter.notifyDataSetChanged();
//        mFirstColumnRecyclerView.setAdapter(mItemAdapter);
        mFirstColumnRecyclerView.swapAdapter(itemAdapter,true);
    }

    public void bindEmpty(){
        setLayoutSize();
    }

    public void unBind(){
        mFirstColumnRecyclerView.setAdapter(null);
        mFirstColumnRecyclerView.removeAllViews();
        mFirstColumnRecyclerView.destroy();
        mPosition = -1;
        mAllShow = true;
    }

    /**
     * 设置ViewGroup的大小;
     */
    private void setLayoutSize(){
        ViewGroup.LayoutParams layoutParams = mFirstColumnRecyclerView.getLayoutParams();
        layoutParams.height = Utils.getDimension(getContext(),R.dimen.home_item_item_type1_height)
                +Utils.getDimension(getContext(),R.dimen.home_item_recycler_padding_bottom);
        mFirstColumnRecyclerView.setLayoutParams(layoutParams);
    }

    /**
     * 获取当前RecyclerView的滑动状态
     * @return
     */
    public Parcelable getRecyclerViewParcelable(){
        if (mFirstColumnRecyclerView != null && mFirstColumnRecyclerView.getLayoutManager() != null &&
                isShouldSetParcelable() ){
            return mFirstColumnRecyclerView.getLayoutManager().onSaveInstanceState();
        }
        return null;
    }

    private boolean isShouldSetParcelable(){
        return mFirstColumnRecyclerView.isShouldSetParcelable();
    }

    /**
     * 设置当前RecyclerView的滑动状态,与{@link #getRecyclerViewParcelable()}对应
     * @param parcelable
     *
     */
    public void setRecyclerViewRestoreInstanceState(Parcelable parcelable){
        mFirstColumnRecyclerView.onCustomRestoreInstanceState(parcelable);
    }

    @Override
    public View getStartView() {
        View view = null;
        boolean isInitState = mFirstColumnRecyclerView.isInitialState();
        LogX.e(TAG+" getFirstView isInitState : "+isInitState+" mFocusHorizontalGridView getChildCount : "+ mFirstColumnRecyclerView.getChildCount());
        view = mFirstColumnRecyclerView.getChildAt(isInitState ? 0 : 1);
        return view;
    }

    @Override
    public boolean isNoScrolling() {
        return mFirstColumnRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE;
    }

    @Override
    public int getViewHeight() {
        return getHeight();
    }

    @Override
    public int getPosition() {
        return mPosition;
    }

    @Override
    public boolean isAllShow() {
        return mAllShow;
    }

    @Override
    public void checkFocus() {
        mFirstColumnRecyclerView.checkFocus();
    }
}
