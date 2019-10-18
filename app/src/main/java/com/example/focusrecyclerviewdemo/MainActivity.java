package com.example.focusrecyclerviewdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FocusConstraintLayout.FocusCallBack,FirstColumnRecyclerView.FocusCallBack{

    private String TAG = MainActivity.class.getSimpleName();
    private List<List<String>> mList;
    private FirstRowRecyclerView mFirstRowRecyclerView;
    private VerticalAdapter mVerticalAdapter;
    private Button mButton1,mButton2;
    private FocusConstraintLayout mFocusConstraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        mVerticalAdapter = new VerticalAdapter();
        mVerticalAdapter.setFocusCallBack(this);
        mVerticalAdapter.setList(mList);
        mFirstRowRecyclerView.setAdapter(mVerticalAdapter);
    }

    private void initView(){
        mFirstRowRecyclerView = findViewById(R.id.recyclerview);
        mButton1 = findViewById(R.id.btn1);
        mButton2 = findViewById(R.id.btn2);
        mFocusConstraintLayout = findViewById(R.id.main);
        mFocusConstraintLayout.setFocusCallBack(this);

        mButton1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mButton1.setTextColor(hasFocus ? Color.RED : Color.BLACK);
            }
        });
        mButton2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mButton2.setTextColor(hasFocus ? Color.RED : Color.BLACK);
            }
        });

        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogX.d(TAG+" mButton1 onClick "+(mFirstRowRecyclerView.getChildAt(0).getHeight()));
                mFirstRowRecyclerView.smoothScrollBy(0,(mFirstRowRecyclerView.getChildAt(0).getHeight()));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirstRowRecyclerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirstRowRecyclerView.onPause();
    }

    private void initData(){
        mList = new ArrayList<>();
        for (int i = 0; i < 100; i++){
            List<String> list = new ArrayList<>();
            for (int j = 0; j < 100; j++){
                list.add(i+"_"+j);
            }
            mList.add(list);
        }
    }

    @Override
    public View getNextDownFocusView() {
        View view = mFirstRowRecyclerView.getChildAt(0);
        if (view instanceof HRecyclerViewCallback){
            HRecyclerViewCallback homeItemLayout = (HRecyclerViewCallback)view;
            View firstView = homeItemLayout.getStartView();
            if (firstView != null){
                mFirstRowRecyclerView.initLastFocusIndex();
                return firstView;
            }
        }
        return null;
    }

    @Override
    public boolean isTopFocusView(View view) {
        return view != null && (view.getId() == R.id.btn1 ||
                view.getId() == R.id.btn2);
    }

    @Override
    public View getFocusView(int position) {
        return mButton2;
    }
}
