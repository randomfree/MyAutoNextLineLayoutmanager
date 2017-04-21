package com.ly.myautonextlinelayoutmanager;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by txw_pc on 2017/4/21.
 */

public class Myadapter extends RecyclerView.Adapter<Myadapter.SearchViewHolder>{
    String[] data = new String[200];

    public Myadapter(){
        initData();

    }
    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView textView = new TextView(parent.getContext());
        SearchViewHolder searchViewHolder = new SearchViewHolder(textView);
        return searchViewHolder;
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        holder.tv.setText(data[position]);
    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    class SearchViewHolder extends RecyclerView.ViewHolder{
        TextView tv;

        public SearchViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView;
        }
    }
    private void initData(){
        String[] str = new String[]{"adf","gfgfadfaf","gfgfadfafadf","gfgfadfafdfa","gfgfadfafadffad","gfgfadfafadfasfsfd","gfg","gfgfadfafadfadfafadfa"};
        for (int i  = 0;i<data.length;i++){
            data[i] = str[(int) (Math.random()*str.length)];
        }
    }
}
