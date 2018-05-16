package sagu.supro.BCT.tv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import sagu.supro.BCT.R;
import sagu.supro.BCT.mobile.Admin;
import sagu.supro.BCT.mobile.Register;
import sagu.supro.BCT.utils.AWSProvider;
import sagu.supro.BCT.utils.Config;

public class Start extends Activity {

    public static final String TAG = Start.class.getName();

    private AmazonDynamoDBClient dynamoDBClient;

    EditText userEmail,userPass;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

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

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ValidateUser().execute(userEmail.getText().toString(),userPass.getText().toString());
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class ValidateUser extends AsyncTask<String, Void, Boolean> {

        String uEmail;
        AlertDialog dialog;
        Boolean adminCheck = false;

        @Override
        protected void onPreExecute() {
            dialog = new SpotsDialog(Start.this);
            //dialog = new SpotsDialog(Start.this,"Custom Loading Message");
            //dialog = new SpotsDialog(Start.this,"Custom Theme");
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            uEmail = strings[0];

            try {
                ScanRequest request = new ScanRequest().withTableName(Config.USERSTABLENAME);
                ScanResult response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> userRows = response.getItems();
                for (Map<String, AttributeValue> map : userRows) {
                    if (map.get("email").getS().equals(strings[0])) {
                        if (map.get("password").getS().equals(strings[1])) {
                            if(userEmail.getText().toString().equals("supradip@brightkidmont.com"))
                                adminCheck = true;
                            return true;
                        }
                    }
                }
            } catch (AmazonClientException e){
                dialog.dismiss();
                //showSnackBar("Network connection error!!");
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean loginResult) {
            if (loginResult){
                if (adminCheck){
                    startActivity(new Intent(Start.this,Admin.class));
                    finish();
                } else {
                    getMACAddress();
                    startActivity(new Intent(Start.this, Register.class));
                    finish();
                }
                dialog.dismiss();
            } else {
                dialog.dismiss();
                Toast.makeText(Start.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private StringBuilder getMACAddress() {
        StringBuilder userMACAddress = new StringBuilder();
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] mac = network.getHardwareAddress();
            for (int i = 0; i < mac.length; i++) {
                userMACAddress.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
        }catch (Exception e){
            e.printStackTrace();
            return userMACAddress.append("MAC");
        }
        return userMACAddress;
    }
}
