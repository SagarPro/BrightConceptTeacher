package sagu.supro.BCT.tv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dmax.dialog.SpotsDialog;
import sagu.supro.BCT.R;
import sagu.supro.BCT.dynamo.UserDetailsDO;
import sagu.supro.BCT.mobile.Admin;
import sagu.supro.BCT.utils.AWSProvider;
import sagu.supro.BCT.utils.Config;

public class Start extends Activity {

    public static final String TAG = Start.class.getName();

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper dynamoDBMapper;

    EditText userEmail,userPass;
    Button login;

    RelativeLayout snackbarView;

    SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        snackbarView = findViewById(R.id.rel_start);
        userEmail = findViewById(R.id.et_email);
        userPass = findViewById(R.id.et_password);
        login = findViewById(R.id.b_submit);

        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            Log.d(TAG, "Running on a TV Device");
        } else {
            Log.d(TAG, "Running on a non-TV Device");
        }

        // Instantiate a AmazonDynamoDBMapperClient
        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        userPref = getSharedPreferences("USER", MODE_PRIVATE);

        String userType = userPref.getString("UserType", "");
        if (!userType.equals("")) {

            if (userType.equals("user")) {
                startActivity(new Intent(Start.this, MainScreen.class));
                finish();
            } else {
                startActivity(new Intent(Start.this,Admin.class));
                finish();
            }

        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(userEmail.getText().toString()) || TextUtils.isEmpty(userPass.getText().toString())){
                    Toast.makeText(Start.this, "Empty Fields Not Allowed", Toast.LENGTH_SHORT).show();
                }
                else{
                    new ValidateUser().execute(userEmail.getText().toString(),userPass.getText().toString());
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class ValidateUser extends AsyncTask<String, Void, String> {

        String uEmail;
        AlertDialog progressDialog;
        Boolean adminCheck = false;
        UserDetailsDO userDetailsDO = new UserDetailsDO();

        @Override
        protected void onPreExecute() {
            progressDialog = new SpotsDialog(Start.this);
            progressDialog.show();
        }

        @SuppressLint("HardwareIds")
        @Override
        protected String doInBackground(String... strings) {

            uEmail = strings[0];

            try {
                ScanRequest request = new ScanRequest().withTableName(Config.USERSTABLENAME);
                ScanResult response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> userRows = response.getItems();
                for (Map<String, AttributeValue> map : userRows) {
                    if (map.get("email").getS().equals(strings[0])) {
                        if (map.get("password").getS().equals(strings[1])) {
                            if(userEmail.getText().toString().equals("ss")) {
                                adminCheck = true;
                                return "admin_success";
                            } else {
                                userDetailsDO = dynamoDBMapper.load(UserDetailsDO.class, strings[0], map.get("userName").getS());
                                if (userDetailsDO.getStatus().equals("subscribed")) {
                                    Map<String, String> macDemo = userDetailsDO.getMacAddress();
                                    Map<String, String> mac = new HashMap<>();
                                    Set macKeys = macDemo.keySet();
                                    for (Object key1 : macKeys) {
                                        String key = (String) key1;
                                        mac.put(key, macDemo.get(key));
                                        if (mac.get(key).equals("MAC")) {
                                            macDemo.put(key, getMacAddr());
                                            userDetailsDO.setMacAddress(macDemo);
                                            dynamoDBMapper.save(userDetailsDO);
                                            return "user_success";
                                        } else {
                                            if (getMacAddr().equals(mac.get(key))){
                                                return "user_success";
                                            }
                                        }
                                    }
                                    return "mac_failed";
                                } else {
                                    return "sub_failed";
                                }
                            }
                        }
                    }
                }
            } catch (AmazonClientException e){
                return "exception";
            }
            return "check_failed";
        }

        @Override
        protected void onPostExecute(String loginResult) {
            progressDialog.dismiss();
            AlertDialog alertDialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(Start.this);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            switch (loginResult){
                case "check_failed":
                    builder.setTitle("Email & Password Don't Match");
                    alertDialog = builder.create();
                    alertDialog.show();
                    break;
                case "exception":
                    builder.setTitle("Please Check Your Network Connection");
                    alertDialog = builder.create();
                    alertDialog.show();
                    break;
                case "sub_failed":
                    builder.setTitle("Your Subscription Has Expired");
                    alertDialog = builder.create();
                    alertDialog.show();
                    break;
                case "mac_failed":
                    builder.setTitle("This Device Is Not Registered");
                    alertDialog = builder.create();
                    alertDialog.show();
                    break;
                case "user_success":
                    SharedPreferences.Editor prefsEditor = userPref.edit();
                    prefsEditor.putString("UserEmail", userDetailsDO.getEmail());
                    prefsEditor.putString("UserName", userDetailsDO.getUserName());
                    prefsEditor.putString("UserType", "user");
                    prefsEditor.apply();
                    startActivity(new Intent(Start.this, MainScreen.class));
                    finish();
                    break;
                case "admin_success":
                    SharedPreferences.Editor aPrefEditor = userPref.edit();
                    aPrefEditor.putString("UserType", "admin");
                    aPrefEditor.apply();
                    startActivity(new Intent(Start.this,Admin.class));
                    finish();
                    break;
            }
        }
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "MAC";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "MAC";
    }
}
