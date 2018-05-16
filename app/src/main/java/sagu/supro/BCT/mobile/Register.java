package sagu.supro.BCT.mobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import sagu.supro.BCT.R;
import sagu.supro.BCT.dynamo.UserDetailsDO;
import sagu.supro.BCT.utils.AWSProvider;
import sagu.supro.BCT.utils.Config;

public class Register extends Activity {

    EditText userName,userEmail,userPass;
    ImageView addDevice,minusDevice;
    TextView numberOfDevices;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userName = findViewById(R.id.et_name);
        userEmail = findViewById(R.id.et_email);
        userPass = findViewById(R.id.et_password);
        addDevice = findViewById(R.id.img_add);
        minusDevice = findViewById(R.id.img_minus);
        numberOfDevices = findViewById(R.id.tv_dev_added);
        submit = findViewById(R.id.b_submit);


        addDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int noOfDevices = Integer.parseInt(numberOfDevices.getText().toString());
                noOfDevices++;
                numberOfDevices.setText(""+noOfDevices);
            }
        });

        minusDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int noOfDevices = Integer.parseInt(numberOfDevices.getText().toString());
                if (noOfDevices != 1)
                    noOfDevices--;
                numberOfDevices.setText(""+noOfDevices);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validation()){
                    UserDetailsDO userDetailsDO = new UserDetailsDO();
                    userDetailsDO.setEmail(userEmail.getText().toString());
                    userDetailsDO.setPassword(userPass.getText().toString());
                    userDetailsDO.setUserName(userName.getText().toString());

                    Calendar myCalendar = Calendar.getInstance();
                    String myFormat = "yyyy/MM/dd";
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                    userDetailsDO.setCreatedDate(sdf.format(myCalendar.getTime()));
                    userDetailsDO.setStatus("subscribed");
                    userDetailsDO.setNoOfDevices(numberOfDevices.getText().toString());

                    Map<String, String> macAddress = new HashMap<>();
                    for (int i=0; i<Integer.parseInt(numberOfDevices.getText().toString()); i++){
                        macAddress.put(String.valueOf(i+1), "null");
                    }

                    userDetailsDO.setMacAddress(macAddress);

                    new AddUser(userDetailsDO).execute();

                }
            }
        });

    }

    private Boolean validation(){
        return !(TextUtils.isEmpty(userName.getText().toString()) ||
                TextUtils.isEmpty(userEmail.getText().toString()) ||
                TextUtils.isEmpty(userPass.getText().toString()));
    }


    @SuppressLint("StaticFieldLeak")
    private class AddUser extends AsyncTask<Void, Void, Boolean> {

        UserDetailsDO userDetailsDO = new UserDetailsDO();
        AmazonDynamoDBClient dynamoDBClient;
        DynamoDBMapper dynamoDBMapper;

        AddUser(UserDetailsDO userDetailsDO){
            this.userDetailsDO = userDetailsDO;
            AWSProvider awsProvider = new AWSProvider();
            dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                    .build();
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                ScanResult result = null;
                do {
                    ScanRequest req = new ScanRequest();
                    req.setTableName(Config.USERSTABLENAME);
                    if (result != null) {
                        req.setExclusiveStartKey(result.getLastEvaluatedKey());
                    }
                    result = dynamoDBClient.scan(req);
                    List<Map<String, AttributeValue>> rows = result.getItems();
                    for (Map<String, AttributeValue> map : rows) {
                        if (map.get("email").getS().equals(userDetailsDO.getEmail())) {
                            return false;
                        }
                    }
                } while (result.getLastEvaluatedKey() != null);

                dynamoDBMapper.save(userDetailsDO);

                return true;
            } catch (AmazonClientException e){
                //addUserActivity.showSnackBar("Network connection error!!");
                return false;
            }
        }


        @Override
        protected void onPostExecute(Boolean result) {
            /*if(pbAddUser!=null)
                pbAddUser.setVisibility(View.GONE);*/
            if (result){
                Toast.makeText(Register.this, "Added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Register.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
