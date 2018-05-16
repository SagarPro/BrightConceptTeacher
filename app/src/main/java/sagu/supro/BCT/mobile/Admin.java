package sagu.supro.BCT.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.List;

import sagu.supro.BCT.R;
import sagu.supro.BCT.adapters.UsersAdapter;
import sagu.supro.BCT.dynamo.UserDetailsDO;

public class Admin extends Activity {

    ListView userListView;
    UsersAdapter usersAdapter;
    List<UserDetailsDO> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        userListView = findViewById(R.id.lv_users);
        usersAdapter = new UsersAdapter(userList,this);
        userListView.setAdapter(usersAdapter);
    }
}
