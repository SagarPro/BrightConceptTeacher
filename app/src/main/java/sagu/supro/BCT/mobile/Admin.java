package sagu.supro.BCT.mobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.List;

import android.widget.SearchView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.ArrayList;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import sagu.supro.BCT.R;
import sagu.supro.BCT.adapters.UsersAdapter;
import sagu.supro.BCT.dynamo.UserDetailsDO;
import sagu.supro.BCT.utils.AWSProvider;
import sagu.supro.BCT.utils.Config;

public class Admin extends Activity {

    ListView userListView;
    UsersAdapter usersAdapter;
    List<UserDetailsDO> userList = new ArrayList<>();
    List<UserDetailsDO> typedString = new ArrayList<>();

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper dynamoDBMapper;

    SearchView searchView;
    ImageView registerUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        searchView = findViewById(R.id.sv_users);
        userListView = findViewById(R.id.lv_users);
        usersAdapter = new UsersAdapter(userList,this);
        userListView.setAdapter(usersAdapter);
        registerUser = findViewById(R.id.img_reg_user);

        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        new ListUser().execute();

        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Admin.this,Register.class));
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                typedString.clear();
                if(newText!=null && !newText.isEmpty())
                {
                    for (int i = 0; i < userList.size(); i++) {
                        String item = userList.get(i).getUserName();

                        if (item.toLowerCase().contains(newText)) {
                            typedString.add(userList.get(i));
                        }
                    }

                    usersAdapter =  new UsersAdapter(typedString,getApplicationContext());
                    usersAdapter.notifyDataSetChanged();
                    userListView.setAdapter(usersAdapter);
                }
                else
                {
                    usersAdapter =  new UsersAdapter(userList,getApplicationContext());
                    usersAdapter.notifyDataSetChanged();
                    userListView.setAdapter(usersAdapter);
                }
                return false;
            }
        });

    }

    private void displayUsers(){
        usersAdapter.notifyDataSetChanged();
    }


    @SuppressLint("StaticFieldLeak")
    private class ListUser extends AsyncTask<Void, Void, Boolean> {

        AlertDialog dialog;
        UserDetailsDO userDetailsDO = new UserDetailsDO();

        @Override
        protected void onPreExecute() {
            dialog = new SpotsDialog(Admin.this);
            dialog.show();
            userList.clear();
        }

        @SuppressLint("HardwareIds")
        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                ScanRequest request = new ScanRequest().withTableName(Config.USERSTABLENAME);
                ScanResult response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> userRows = response.getItems();
                for (Map<String, AttributeValue> map : userRows) {
                    if (!map.get("email").getS().equals("ss")) {
                        userDetailsDO = dynamoDBMapper.load(UserDetailsDO.class, map.get("email").getS(), map.get("userName").getS());
                        userList.add(userDetailsDO);
                    }
                }
                return true;
            } catch (AmazonClientException e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean loginResult) {
            dialog.dismiss();
            if (loginResult){
                displayUsers();
            } else {
                Toast.makeText(Admin.this, "Exception", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
