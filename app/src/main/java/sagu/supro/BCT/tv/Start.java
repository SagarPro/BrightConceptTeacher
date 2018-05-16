package sagu.supro.BCT.tv;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.List;
import java.util.Map;

import sagu.supro.BCT.R;
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

        //Boolean exception, expired;
        String uEmail;
        //Date currentDate, expiryDate;
        //SimpleDateFormat df;

        @Override
        protected void onPreExecute() {
            /*if (pbLogin !=null && pbLogin.getVisibility()==View.GONE)
                pbLogin.setVisibility(View.VISIBLE);
            expired = false;
            exception = false;
            Date c = Calendar.getInstance().getTime();
            df = new SimpleDateFormat("MMM dd, yyyy");
            String formattedDate = df.format(c.getTime());
            try {
                currentDate = df.parse(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }*/
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
                            return true;
                        }
                    }
                }
            } catch (AmazonClientException e){
                //showSnackBar("Network connection error!!");
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean loginResult) {
            if (loginResult){
                startActivity(new Intent(Start.this, Register.class));
                finish();
            } else {
                Toast.makeText(Start.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
