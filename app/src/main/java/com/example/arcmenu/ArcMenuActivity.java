package com.example.arcmenu;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arcmenu.view.ArcMenu;

/**
 * Created by jack on 2017/10/2.
 */

public class ArcMenuActivity extends AppCompatActivity{
    private ArcMenu arcMenu;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arcmenu);
        arcMenu = (ArcMenu) findViewById(R.id.id_arcmenu);

        arcMenu.setOnMenuItemClickListener(new ArcMenu.OnMenuItemClickListener() {
            @Override
            public void onClick(View view, int pos) {
                String text = ((TextView)view).getText().toString();
                Toast.makeText(ArcMenuActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
