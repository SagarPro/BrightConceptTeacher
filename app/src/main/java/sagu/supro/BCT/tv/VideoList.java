package sagu.supro.BCT.tv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
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
import java.util.List;
import java.util.Map;
import sagu.supro.BCT.dynamo.LKGVideosDO;
import sagu.supro.BCT.dynamo.NurseryVideosDO;
import sagu.supro.BCT.dynamo.PlaygroupVideosDO;
import sagu.supro.BCT.dynamo.UKGVideosDO;
import sagu.supro.BCT.tv.DataModel.Video;
import sagu.supro.BCT.utils.AWSProvider;
import sagu.supro.BCT.utils.Config;

public class VideoList {

    public final String MOVIE_CATEGORY[] = {"LKG", "UKG", "Playgroup", "Nursery",};
    public List<Video> mVideoList;

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper dynamoDBMapper;
    private Context context;

    private LKGVideosDO lkgVideosDO = new LKGVideosDO();
    private UKGVideosDO ukgVideosDO = new UKGVideosDO();
    private NurseryVideosDO nurseryVideosDO = new NurseryVideosDO();
    private PlaygroupVideosDO playgroupVideosDO = new PlaygroupVideosDO();

    private List<LKGVideosDO> lkgVideoDetails = new ArrayList<>();
    private List<UKGVideosDO> ukgVideoDetails = new ArrayList<>();
    private List<NurseryVideosDO> nurseryVideoDetails = new ArrayList<>();
    private List<PlaygroupVideosDO> playgroupVideoDetails = new ArrayList<>();

    public VideoList(Context context){
        this.context=context;
    }

    public List<Video> setupMovies() {
        mVideoList = new ArrayList<>();
        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(context));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();



        return mVideoList;
    }


    @SuppressLint("StaticFieldLeak")
    private class ValidateUser extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                ScanRequest request = new ScanRequest().withTableName(Config.LKGTABLENAME);
                ScanResult response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> userRows = response.getItems();
                for (Map<String, AttributeValue> map : userRows) {
                    lkgVideosDO = dynamoDBMapper.load(LKGVideosDO.class,map.get("video_id").getS(),
                            map.get("video_title").getS());
                    lkgVideoDetails.add(lkgVideosDO);
                }
                return true;
            } catch (AmazonClientException e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean s) {
            if(s){
                Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);
        }
    }


}
