package com.ly.myautonextlinelayoutmanager;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ly on 2017/4/21.
 * 自动换行，layoutmanager
 * 自定义layoutmanager,需要明白recyclerview有两个缓存，一个是scrap,一个是recycler
 * scrap是将view与视图暂时分离，但是不需要重新绑定view的数据的情况，多数是在layoutmanager改变view的位置时使用(只改变位置
 * 不需要重新绑定数据)
 * recycler则是view滑出屏幕之类的，等待重用时用的
 * 其实自定义layoutmanager以改变recyclerview的排版样式，主要就是自己计算每个view的位置
 * 还有注意重用和滑动
 */

public class AutoNextLineLayoutManger extends RecyclerView.LayoutManager {
    //保存所有item偏移量信息
    private SparseArray<Rect> allItemframs = new SparseArray<>();
    //记录item是否出现过屏幕上，并且还没有回收。true表示出现过屏幕上并且没回收
    private SparseBooleanArray hasAttachedItems = new SparseBooleanArray();

    public AutoNextLineLayoutManger(){
        setAutoMeasureEnabled(true);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);

        Log.e(this.getClass().getSimpleName(),"onlayoutchildren");
        //没有直接返回
        if (getItemCount()<=0) return;
        //preLayout主要支持动画，直接跳过
        if (state.isPreLayout()) return;
        //将视图分离放入scrap缓存中，以准备重新对view进行排版
        detachAndScrapAttachedViews(recycler);
        int offsetY = 0;
        int offsetX = 0;
        int viewH = 0;
        for (int i = 0; i < getItemCount(); i++) {
            //依次取出view
            View view = recycler.getViewForPosition(i);
            //将view 添加只recyclerview
            addView(view);
            //对子view进行测量
            measureChildWithMargins(view, 0, 0);
            //拿到装饰后的宽高
            int w = getDecoratedMeasuredWidth(view);
            int h = getDecoratedMeasuredHeight(view);
            viewH = h;

            Rect fram = allItemframs.get(i);
            if (fram == null){
                fram = new Rect();
            }

            //将view放到想要它出现的位置
            if (offsetX + w > getWidth()) {
                //换行
                offsetY += h;
                offsetX = w;
//                layoutDecorated(view, 0, offsetY, w, offsetY + h);
                fram.set(0, offsetY, w, offsetY + h);
            } else {
                //不换行
//                layoutDecorated(view, offsetX, offsetY, offsetX + w, offsetY + h);
                fram.set(offsetX, offsetY, offsetX + w, offsetY + h);
                offsetX += w;
            }
            //保存每一个item的边界数据
            allItemframs.put(i,fram);
            // 由于已经调用了detachAndScrapAttachedViews，因此需要将当前的Item设置为未出现过
            hasAttachedItems.put(i,false);

        }
        //计算子view的高度和，用于滑动计算
        totalHeight=offsetY+viewH;
        //如果所有子view的高的和,没有填满recyclerview
        //则将高度设置为recyclerview的高度
        totalHeight = Math.max(totalHeight,getVerticalSpace());

        recyclerAndFillItems(recycler,state);
    }

    /**
     * 将view布局到recyclerview上
     * @param recycler
     * @param state
     */
    private void recyclerAndFillItems(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.isPreLayout())return;
        detachAndScrapAttachedViews(recycler);
        //当前scroll offset状态下现实的区域
        Rect displayFram = new Rect(0,verticalScrollOffset,getHorizontalSpace(),verticalScrollOffset+getVerticalSpace());

        //将滑出屏幕的view进行回收
        Rect childRect = new Rect();
        for (int i = 0;i<getChildCount();i++){
            View child = getChildAt(i);
            childRect.left = getDecoratedLeft(child);
            childRect.top = getDecoratedTop(child);
            childRect.right = getDecoratedRight(child);
            childRect.bottom = getDecoratedBottom(child);
            //如果没有现实的区域就回收
            if (!Rect.intersects(displayFram,childRect)){
                removeAndRecycleView(child,recycler);
            }
        }
        //重新现实需要出现在屏幕上的子view
        for (int j = 0;j<getItemCount();j++){
            if (Rect.intersects(displayFram,allItemframs.get(j))){
                View scrap = recycler.getViewForPosition(j);
                measureChildWithMargins(scrap,0,0);
                addView(scrap);

                Rect fram = allItemframs.get(j);
                layoutDecorated(scrap,fram.left,fram.top-verticalScrollOffset,fram.right,fram.bottom-verticalScrollOffset);
            }
        }
    }

    @Override
    public boolean canScrollVertically() {
        //让recyclerview可以在竖直方向上滑动
        return true;
    }

    /**
     * 滑动偏移量
     * 如果是正的就是在向上滑，展现上面的view
     * 如果是负的向下
     */
    private int verticalScrollOffset = 0;
    private int totalHeight = 0;

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);

        //实际滑动距离
        int trval = dy;
        //如果滑动到最顶部
        if (verticalScrollOffset + dy < 0) {
            trval = -verticalScrollOffset;
        }else if (verticalScrollOffset+dy>totalHeight - getVerticalSpace()){//如果滑动到最底部
            trval = totalHeight - getVerticalSpace()-verticalScrollOffset;
        }
        //将竖直方向上的偏移量+trval
        verticalScrollOffset+=trval;
        //平移容器内的item
        offsetChildrenVertical(-trval);

        recyclerAndFillItems(recycler,state);
        Log.e(this.getClass().getSimpleName(),"itemcount = "+getChildCount());
        return trval;
    }

    /**
     * 获取recyclerview减去paddingtop和paddingBottom的高
     * @return
     */
    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }
    /**
     * 获取recyclerview减去paddingleft和paddingright的宽
     * @return
     */
    private int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }


}
