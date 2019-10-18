package com.example.focusrecyclerviewdemo;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS;
import static android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS;

/**
 * @author liuqz
 * @date : 2019/10/15 10:18
 */
public class VerticalAdapter extends RecyclerView.Adapter<VerticalAdapter.VerticalViewHolder> implements FirstColumnRecyclerView.FocusCallBack{

    private List<List<String>> mLists;
    private Map<List<String>, Parcelable> mScrollStates = new HashMap<>();
    private FirstColumnRecyclerView.FocusCallBack mFocusCallBack;
    private final static int EMPTY = 3 ;
    private List<ItemAdapter> mItemAdapters;
    /**
     * 创建一个{@link RecyclerView.RecycledViewPool},
     * 根据需求看是否需要为所有小RecyclerView共用一个{@link RecyclerView.RecycledViewPool};
     * 默认开启共用
     */
    private RecyclerView.RecycledViewPool mRecycledViewPool;

    public VerticalAdapter(){
        mRecycledViewPool = new RecyclerView.RecycledViewPool();
        mRecycledViewPool.setMaxRecycledViews(0,50);
        setHasStableIds(true);
    }

    public void setList(List<List<String>> lists){
        mLists = lists;
        mItemAdapters = new ArrayList<>();
        for (int i = 0;i< lists.size();i++){
            ItemAdapter itemAdapter = new ItemAdapter();
            itemAdapter.setList(lists.get(i));
            itemAdapter.setParentPosition(i);
            mItemAdapters.add(itemAdapter);
        }
    }

    public FirstColumnRecyclerView.FocusCallBack getFocusCallBack() {
        return mFocusCallBack;
    }

    public void setFocusCallBack(FirstColumnRecyclerView.FocusCallBack focusCallBack) {
        mFocusCallBack = focusCallBack;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public VerticalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        long t=System.currentTimeMillis();

         View v1=LayoutInflater.from(parent.getContext()).inflate(R.layout.view_home_item,parent,false);


        VerticalViewHolder mVerticalViewHolder= new VerticalAdapter.VerticalViewHolder(v1
                ,mRecycledViewPool,this);
        LogX.d("onCreateViewHolder : "+(System.currentTimeMillis()-t));


        return mVerticalViewHolder;
    }

    public List<String> getItem(int position){
        if (mLists != null && mLists.size() > 0 && position < mLists.size()){
           return mLists.get(position);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull VerticalViewHolder holder, int position) {
        long t=System.currentTimeMillis();
        if (position < mLists.size()){
            List<String> list = mLists.get(position);
            holder.bind(list,position,mItemAdapters.get(position));
        }else{
            holder.bindEmpty();
        }
        LogX.d("onBindViewHolder : "+(System.currentTimeMillis()-t));
    }

    @Override
    public void onViewRecycled(@NonNull VerticalViewHolder holder) {
        super.onViewRecycled(holder);
        holder.unBind();
    }

    @Override
    public int getItemCount() {
        return mLists == null ? 0 : (mLists.size() + EMPTY);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VerticalViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if(getItem(holder.getAdapterPosition()) != null){
            Parcelable parcelable = mScrollStates.get(getItem(holder.getAdapterPosition()));
            if (parcelable != null){
                holder.setRecyclerViewRestoreInstanceState(parcelable);
            }else{
                holder.setRecyclerViewRestoreInstanceState(null);
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VerticalViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        List<String> list = getItem(holder.getAdapterPosition());
        if (list != null){
            Parcelable parcelable = holder.getRecyclerViewParcelable();
            if (parcelable != null){
                mScrollStates.put(list,parcelable);
            }else{
                mScrollStates.remove(list);
            }
        }
    }


    public boolean isShowPosition(int position) {
        boolean result = false;
        if (getItemCount() > 0){
            for (int i=0; i<getItemCount(); i++){
                List<String> list = getItem(i);
                if (list != null){
                    result = i == position;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public View getFocusView(int position) {
        if (isShowPosition(position) && mFocusCallBack != null){
            return mFocusCallBack.getFocusView(-1);
        }
        return null;
    }

    static class VerticalViewHolder extends RecyclerView.ViewHolder{

        private ItemLayout mItemLayout;

        VerticalViewHolder(@NonNull View itemView,RecyclerView.RecycledViewPool recycledViewPool,VerticalAdapter verticalAdapter) {
            super(itemView);
            mItemLayout = (ItemLayout)itemView;
            mItemLayout.setRecycledViewPool(recycledViewPool);
            mItemLayout.setFocusCallBack(verticalAdapter);
        }

        void bind(List<String> list,int position,ItemAdapter itemAdapter){
            toggleVisibility(true);
            mItemLayout.bind(list,position,itemAdapter);
        }

        void bindEmpty(){
            toggleVisibility(false);
            mItemLayout.bindEmpty();
        }

        void unBind(){
            mItemLayout.unBind();
        }

        Parcelable getRecyclerViewParcelable(){
            return mItemLayout.getRecyclerViewParcelable();
        }

        void setRecyclerViewRestoreInstanceState(Parcelable parcelable){
            mItemLayout.setRecyclerViewRestoreInstanceState(parcelable);
        }

        void toggleVisibility(boolean isShow){
            itemView.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
            itemView.setClickable(isShow);
            itemView.setFocusable(isShow);
            itemView.setFocusableInTouchMode(isShow);
            if (itemView instanceof ViewGroup){
                ((ViewGroup)itemView).setDescendantFocusability(isShow ? FOCUS_AFTER_DESCENDANTS : FOCUS_BLOCK_DESCENDANTS);
            }
        }
    }

}
