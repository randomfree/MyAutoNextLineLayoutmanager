package com.ly.myautonextlinelayoutmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new AutoNextLineLayoutManger());
        recyclerView.setAdapter(new Myadapter());
        recyclerView.addItemDecoration(new SpaceItemDecoration(20));
    }
}
