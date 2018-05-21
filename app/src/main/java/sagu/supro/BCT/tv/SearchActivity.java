package sagu.supro.BCT.tv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import sagu.supro.BCT.R;

public class SearchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }
}
