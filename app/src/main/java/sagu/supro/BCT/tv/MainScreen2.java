package sagu.supro.BCT.tv;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import sagu.supro.BCT.R;
import sagu.supro.BCT.leanback_lib.CardPresenter;

public class MainScreen2 extends Activity {

    ImageButton playgroup,nursery,lkg,ukg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen2);

        playgroup = findViewById(R.id.ib_playgroup);
        nursery = findViewById(R.id.ib_nursery);
        lkg = findViewById(R.id.ib_lkg);
        ukg = findViewById(R.id.ib_ukg);

        playgroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainScreen2.this, "Go to Playgroup browse fragment", Toast.LENGTH_SHORT).show();
            }
        });

        nursery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainScreen2.this, "Go to Nursery browse fragment", Toast.LENGTH_SHORT).show();
            }
        });

        lkg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainScreen2.this, "Go to Lkg browse fragment", Toast.LENGTH_SHORT).show();
            }
        });

        ukg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainScreen2.this, "Go to Ukg browse fragment", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
