package com.ly.myautonextlinelayoutmanager;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by txw_pc on 2017/4/21.
 */

public class SpaceItemDecoration extends RecyclerView.ItemDecoration{
    private int space;
    public SpaceItemDecoration(int space){
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.top = space;
        outRect.left = space;
        outRect.bottom = space;
        outRect.right = space;
    }
}
