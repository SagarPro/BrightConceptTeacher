package sagu.supro.BCT.tv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import sagu.supro.BCT.R;

public class MainScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }
}
