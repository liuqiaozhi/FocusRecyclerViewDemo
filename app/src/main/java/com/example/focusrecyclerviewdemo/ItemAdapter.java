package com.example.focusrecyclerviewdemo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS;
import static android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS;

/**
 * @author liuqz
 * @date : 2019/10/15 10:18
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<String> mLists;
    private int mParentPosition = -1;
    private final static int EMPTY_NUM = 5;
    private BrowseItemFocusHighlight mBrowseItemFocusHighlight;

    public ItemAdapter() {
        mBrowseItemFocusHighlight = BrowseItemFocusHighlight.newInstance();
        setHasStableIds(true);
    }

    public void setList(List<String> list){
        mLists = new ArrayList<>(list);
    }

    public void setParentPosition(int parentPosition) {
        mParentPosition = parentPosition;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        long t=System.currentTimeMillis();
        View v1=LayoutInflater.from(parent.getContext()).inflate(R.layout.view_recyclerview_item,parent,false);
//        View v1=LayoutInflater.from(parent.getContext()).inflate(R.layout.view_recyclerview_item,null);
        LogX.d("onCreateViewHolder1 : "+(System.currentTimeMillis()-t));
        ItemViewHolder itemViewHolder =  new ItemViewHolder(v1,mBrowseItemFocusHighlight);
        return itemViewHolder;
    }

    @Override
    public long getItemId(int position) {
        if (position < mLists.size()){
            return (long)mLists.get(position).hashCode();
        }else{
            return (long)(mLists.get(mLists.size() -1 ).hashCode()+position);
        }
//        return super.getItemId(position);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        long t=System.currentTimeMillis();
        if (position < mLists.size()){
            holder.bind(mLists.get(position),mParentPosition);
        }else{
            holder.bindEmpty();
        }
        LogX.d("onBindViewHolder1 : "+(System.currentTimeMillis()-t));
    }

    @Override
    public void onViewRecycled(@NonNull ItemViewHolder holder) {
        super.onViewRecycled(holder);
        holder.unBind();
    }

    @Override
    public int getItemCount() {
        return mLists == null ? 0 : (mLists.size()+EMPTY_NUM);
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder{

        private HRecyclerViewItemView mHRecyclerViewItemView;

        public ItemViewHolder(@NonNull View itemView,final BrowseItemFocusHighlight browseItemFocusHighlight) {
            super(itemView);
         //   browseItemFocusHighlight.onInitializeView(itemView);
            mHRecyclerViewItemView = (HRecyclerViewItemView)itemView;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(),ItemViewHolder.this.getAdapterPosition()+" 被点击啦",Toast.LENGTH_SHORT).show();
                }
            });

            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                //    browseItemFocusHighlight.onItemFocused(v,hasFocus);
                    mHRecyclerViewItemView.setFocusState(hasFocus);
                }
            });
        }

        void bind(String str,int prentPosition){
            toggleVisibility(true);
            mHRecyclerViewItemView.setParentPosition(prentPosition);
            mHRecyclerViewItemView.bind(str);
        }

        void bindEmpty(){
            toggleVisibility(false);
            mHRecyclerViewItemView.bindEmpty();
        }

        void unBind(){
            mHRecyclerViewItemView.unBind();
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
