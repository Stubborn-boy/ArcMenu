package com.example.arcmenu;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.arcmenu.view.ArcDragMenu;

/**
 * Created by jack on 2017/10/3.
 */

public class ArcDragMenuActivity extends AppCompatActivity {

    private ArcDragMenu arcDragMenu;

    private int[] mItemImgs = new int[] { R.drawable.home_tianjia,
            R.drawable.home_tianjia, R.drawable.home_tianjia,
            R.drawable.home_tianjia, R.drawable.home_tianjia, R.drawable.home_tianjia,
            R.drawable.home_tianjia, R.drawable.home_tianjia,R.drawable.home_tianjia, R.drawable.home_tianjia};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arcdragmenu);
        arcDragMenu = (ArcDragMenu) findViewById(R.id.arcdragmenu);
        arcDragMenu.setMenuItemIcons(mItemImgs);
        arcDragMenu.setOnMenuItemClickListener(new ArcDragMenu.OnMenuItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(ArcDragMenuActivity.this, position+"", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
