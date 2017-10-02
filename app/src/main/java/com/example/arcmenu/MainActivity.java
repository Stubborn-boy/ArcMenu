package com.example.arcmenu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView arcMenu;
    private TextView arcDragMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arcMenu = (TextView) findViewById(R.id.arcmenu);
        arcDragMenu = (TextView) findViewById(R.id.arcdragmenu);
        arcMenu.setOnClickListener(this);
        arcDragMenu.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent ;
        switch (view.getId()){
            case R.id.arcmenu:
                intent = new Intent(this, ArcMenuActivity.class);
                startActivity(intent);
                break;
            case R.id.arcdragmenu:

                break;
        }
    }
}
