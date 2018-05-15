package sagu.supro.BCT.tv;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
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
import sagu.supro.BCT.dynamo.BctLoginCredentialsDO;
import sagu.supro.BCT.utils.Config;

public class Start extends Activity {

    public static final String TAG = Start.class.getName();

    AmazonDynamoDBClient dynamoDBClient;
    DynamoDBMapper dynamoDBMapper;

    public static Start start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        start = this;

        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            Log.d(TAG, "Running on a TV Device");
        } else {
            Log.d(TAG, "Running on a non-TV Device");
        }

        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Log.d(TAG, "AWSMobileClient is instantiated and you are connected to AWS!");
            }
        }).execute();

        // Instantiate a AmazonDynamoDBMapperClient
        this.dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.dynamoDBClient.setRegion(Region.getRegion(Regions.AP_SOUTH_1));
        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        //BctLoginCredentialsDO bctLoginCredentialsDO = new BctLoginCredentialsDO();
    }


    private static class validateUser extends AsyncTask<String, Void, Boolean> {

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
                ScanResult response = start.dynamoDBClient.scan(request);
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
                Toast.makeText(start, "Success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(start, "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
