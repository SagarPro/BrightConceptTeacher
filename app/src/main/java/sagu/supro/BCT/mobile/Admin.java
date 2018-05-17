package sagu.supro.BCT.mobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.text.method.KeyListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.HashMap;
import java.util.List;

import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
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
import java.util.Set;

import dmax.dialog.SpotsDialog;
import sagu.supro.BCT.R;
import sagu.supro.BCT.adapters.MACAdapter;
import sagu.supro.BCT.adapters.UsersAdapter;
import sagu.supro.BCT.dynamo.UserDetailsDO;
import sagu.supro.BCT.tv.Start;
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
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                typedString.clear();
                if (newText != null && !newText.isEmpty()) {
                    for (int i = 0; i < userList.size(); i++) {
                        String item = userList.get(i).getUserName();

                        if (item.toLowerCase().contains(newText.toLowerCase())) {
                            typedString.add(userList.get(i));
                        }
                    }

                    usersAdapter = new UsersAdapter(typedString, getApplicationContext());
                    usersAdapter.notifyDataSetChanged();
                    userListView.setAdapter(usersAdapter);
                } else {
                    usersAdapter = new UsersAdapter(userList, getApplicationContext());
                    usersAdapter.notifyDataSetChanged();
                    userListView.setAdapter(usersAdapter);
                }
                return false;

            }
        });

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                final Boolean[] updated = {false};

                final Dialog[] dialog = {new Dialog(Admin.this)};
                dialog[0].setContentView(R.layout.user_details_dialog);
                dialog[0].getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                TextView tvCName = dialog[0].findViewById(R.id.tvCName);
                TextView tvCEmail = dialog[0].findViewById(R.id.tvCEmail);
                TextView tvCreatedDate = dialog[0].findViewById(R.id.tvCreatedDate);
                final EditText etPassword = dialog[0].findViewById(R.id.etPassword);
                etPassword.setTag(etPassword.getKeyListener());
                etPassword.setKeyListener(null);
                final ImageView minusDevice = dialog[0].findViewById(R.id.img_minus);
                minusDevice.setEnabled(false);
                final ImageView addDevice = dialog[0].findViewById(R.id.img_add);
                addDevice.setEnabled(false);
                final TextView numberOfDevices = dialog[0].findViewById(R.id.tv_dev_added);
                final Switch sSub = dialog[0].findViewById(R.id.sSub);
                sSub.setEnabled(false);
                TextView tvMACAddress = dialog[0].findViewById(R.id.tvMACAddress);

                Button btnCCancel = dialog[0].findViewById(R.id.btnCCancel);
                final Button btnCEdit = dialog[0].findViewById(R.id.btnCEdit);
                final Button btnCSave = dialog[0].findViewById(R.id.btnCSave);
                btnCSave.setVisibility(View.GONE);

                tvCName.setText(userList.get(i).getUserName());
                tvCEmail.setText(userList.get(i).getEmail());
                tvCreatedDate.setText(userList.get(i).getCreatedDate());
                etPassword.setText(userList.get(i).getPassword());
                numberOfDevices.setText(userList.get(i).getNoOfDevices());
                if (userList.get(i).getStatus().equals("subscribed")){
                    sSub.setChecked(true);
                } else {
                    sSub.setChecked(false);
                }

                final List<String> macList = new ArrayList<>();
                Map<String, String> macMap = userList.get(i).getMacAddress();
                Set macKeys = macMap.keySet();
                for (Object key1 : macKeys) {
                    String key = (String) key1;
                    macList.add(macMap.get(key));
                }

                addDevice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int noOfDevices = Integer.parseInt(numberOfDevices.getText().toString());
                        noOfDevices++;
                        numberOfDevices.setText(""+noOfDevices);
                        macList.add("MAC");
                    }
                });

                minusDevice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int noOfDevices = Integer.parseInt(numberOfDevices.getText().toString());
                        if (noOfDevices != 1) {
                            noOfDevices--;
                            if (macList.contains("MAC")){
                                macList.remove("MAC");
                            } else {
                                macList.remove(macList.size()-1);
                            }
                        }
                        numberOfDevices.setText(""+noOfDevices);
                    }
                });

                final MACAdapter macAdapter = new MACAdapter(macList, Admin.this);

                tvMACAddress.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog innerDialog = new Dialog(Admin.this);
                        innerDialog.setContentView(R.layout.mac_address);
                        innerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                        ListView lvMAC = innerDialog.findViewById(R.id.lvMAC);
                        Button cancel = innerDialog.findViewById(R.id.cancel);

                        lvMAC.setAdapter(macAdapter);

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                innerDialog.dismiss();
                            }
                        });

                        innerDialog.show();

                    }
                });

                btnCCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog[0].dismiss();
                        if (updated[0]){
                            new ListUser().execute();
                        }
                    }
                });

                btnCEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btnCEdit.setVisibility(View.GONE);
                        btnCSave.setVisibility(View.VISIBLE);
                        etPassword.setKeyListener((KeyListener) etPassword.getTag());
                        addDevice.setEnabled(true);
                        minusDevice.setEnabled(true);
                        sSub.setEnabled(true);
                    }
                });

                btnCSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        updated[0] = true;

                        btnCSave.setVisibility(View.GONE);
                        btnCEdit.setVisibility(View.VISIBLE);
                        etPassword.setKeyListener(null);
                        addDevice.setEnabled(false);
                        minusDevice.setEnabled(false);
                        sSub.setEnabled(false);
                        List<String> newMacList = macAdapter.getMacList();
                        final Map<String, String> mMap = new HashMap<>();
                        for (int i=0; i<newMacList.size(); i++){
                            mMap.put(String.valueOf(i+1), newMacList.get(i));
                        }

                        final AlertDialog progressDialog = new SpotsDialog(Admin.this);
                        progressDialog.show();

                        Thread thread = new Thread() {
                            @Override
                            public void run() {

                                UserDetailsDO userDetails = dynamoDBMapper.load(UserDetailsDO.class, userList.get(i).getEmail(), userList.get(i).getUserName());
                                userDetails.setPassword(etPassword.getText().toString());
                                userDetails.setNoOfDevices(numberOfDevices.getText().toString());
                                if (sSub.isChecked())
                                    userDetails.setStatus("subscribed");
                                else
                                    userDetails.setStatus("unsubscribed");
                                userDetails.setMacAddress(mMap);
                                dynamoDBMapper.save(userDetails);

                                progressDialog.dismiss();
                            }
                        };
                        thread.start();

                    }
                });

                dialog[0].show();
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
