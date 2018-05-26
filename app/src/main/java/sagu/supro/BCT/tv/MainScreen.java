package sagu.supro.BCT.tv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import sagu.supro.BCT.R;
import sagu.supro.BCT.levels.lkg.LkgActivity;
import sagu.supro.BCT.levels.nursery.NurseryActivity;
import sagu.supro.BCT.levels.playgroup.PlaygroupActivity;
import sagu.supro.BCT.levels.ukg.UkgActivity;

public class MainScreen extends Activity {

    ImageButton playgroup,nursery,lkg,ukg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        playgroup = findViewById(R.id.ib_playgroup);
        nursery = findViewById(R.id.ib_nursery);
        lkg = findViewById(R.id.ib_lkg);
        ukg = findViewById(R.id.ib_ukg);

        playgroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), PlaygroupActivity.class));
            }
        });

        nursery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), NurseryActivity.class));
            }
        });

        lkg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LkgActivity.class));
            }
        });

        ukg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), UkgActivity.class));
            }
        });
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }

}
