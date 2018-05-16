package sagu.supro.BCT.mobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import java.util.List;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper dynamoDBMapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        userListView = findViewById(R.id.lv_users);
        usersAdapter = new UsersAdapter(userList,this);
        userListView.setAdapter(usersAdapter);

        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        new ListUser().execute();

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
