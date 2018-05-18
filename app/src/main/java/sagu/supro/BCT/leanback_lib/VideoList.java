package sagu.supro.BCT.leanback_lib;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.webkit.MimeTypeMap;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sagu.supro.BCT.dynamo.LKGVideosDO;
import sagu.supro.BCT.dynamo.NurseryVideosDO;
import sagu.supro.BCT.dynamo.PlaygroupVideosDO;
import sagu.supro.BCT.dynamo.UKGVideosDO;
import sagu.supro.BCT.leanback_lib.CardPresenter;
import sagu.supro.BCT.leanback_lib.Video;
import sagu.supro.BCT.utils.AWSProvider;
import sagu.supro.BCT.utils.Config;

public class VideoList {

    private final String MOVIE_CATEGORY[] = {"LKG", "UKG", "Nursery", "Playgroup", "Downloaded"};
    private List<Video> actualVideoList = new ArrayList<>();

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper dynamoDBMapper;
    private Context context;

    //Retrieved DOs with Value
    private List<LKGVideosDO> lkgVideoDetails = new ArrayList<>();
    private List<UKGVideosDO> ukgVideoDetails = new ArrayList<>();
    private List<NurseryVideosDO> nurseryVideoDetails = new ArrayList<>();
    private List<PlaygroupVideosDO> playgroupVideoDetails = new ArrayList<>();

    private List<String> downloadedVideoName = new ArrayList<>();
    private List<String> downloadedVideoId = new ArrayList<>();
    private List<String> downloadedCardImage = new ArrayList<>();
    private List<String> downloadedVideoDesc = new ArrayList<>();

    private ArrayObjectAdapter rowsAdapter;


    public VideoList(Context context){
        this.context=context;
    }

    public ArrayObjectAdapter setupMovies() {
        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(context));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        try {
            Boolean bgStatus = new FetchVideoDetails().execute().get();
            if (bgStatus) {
                rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
                CardPresenter cardPresenter = new CardPresenter();

                int i;
                for (i = 0; i < MOVIE_CATEGORY.length; i++) {
                    ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    int NUM_COLS = i;
                    actualVideoList.clear();
                    switch (NUM_COLS) {
                        case 0:
                            NUM_COLS = lkgVideoDetails.size();
                            updateActualList("lkg", lkgVideoDetails.size());
                            break;
                        case 1:
                            NUM_COLS = ukgVideoDetails.size();
                            updateActualList("ukg", ukgVideoDetails.size());
                            break;
                        case 2:
                            NUM_COLS = nurseryVideoDetails.size();
                            updateActualList("nursery", nurseryVideoDetails.size());
                            break;
                        case 3:
                            NUM_COLS = playgroupVideoDetails.size();
                            updateActualList("playgroup", playgroupVideoDetails.size());
                            break;
                        case 4:
                            NUM_COLS = getTotalDownloadedProjects();
                            updateActualList("downloaded",NUM_COLS);
                            break;
                    }
                    for (int j = 0; j < NUM_COLS; j++) {
                        listRowAdapter.add(actualVideoList.get(j));
                    }
                    HeaderItem header = new HeaderItem(i, MOVIE_CATEGORY[i]);
                    rowsAdapter.add(new ListRow(header, listRowAdapter));
                }
            }
        } catch (Exception e) {
            AlertDialog alertDialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setTitle("Please Check Your Network Connection : " +e.getMessage());
            alertDialog = builder.create();
            alertDialog.show();

            e.printStackTrace();
        }
        return rowsAdapter;
    }

    private int getTotalDownloadedProjects() {
        File directory=new File(Environment.getExternalStorageDirectory()+"/BCT");
        getDownloadedVideoNames(directory);
        return directory.list().length;
    }

    private void getDownloadedVideoNames(File directory) {
        clearAllLists();
        File[] fileList = directory.listFiles();
        for(File currentFile : fileList){
            if(!currentFile.isDirectory() && getMimeType(currentFile.getName()).equals("video/mp4")){
                downloadedVideoName.add(currentFile.getPath());
            }
            else if(!currentFile.isDirectory() && getMimeType(currentFile.getName()).equals("image/jpeg")){
                downloadedCardImage.add(currentFile.getPath());
            }
            else if(!currentFile.isDirectory() && getMimeType(currentFile.getName()).equals("text/plain")){
                downloadedVideoDesc.add(currentFile.getPath());
            }
            else{
                getDownloadedVideoNames(currentFile);
                downloadedVideoId.add(currentFile.getName());
            }
        }
    }

    private void clearAllLists() {
        downloadedVideoId.clear();
        downloadedVideoDesc.clear();
        downloadedCardImage.clear();
        downloadedVideoName.clear();
    }

    private String getMimeType(String fileUrl) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileUrl);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private void updateActualList(String type,int size) {
        switch (type){
            case "lkg":
                for(int i = 0;i<size;i++){
                    Video video = new Video();
                    video.setId(lkgVideoDetails.get(i).getVideoId());
                    video.setTitle(lkgVideoDetails.get(i).getVideoTitle());
                    video.setDescription(lkgVideoDetails.get(i).getVideoDescription());
                    video.setCardImageUrl(lkgVideoDetails.get(i).getVideoCardImg());
                    video.setVideoUrl(lkgVideoDetails.get(i).getVideoUrl());
                    actualVideoList.add(video);
                }
                break;
            case "ukg":
                for(int i = 0;i<size;i++){
                    Video video = new Video();
                    video.setId(ukgVideoDetails.get(i).getVideoId());
                    video.setTitle(ukgVideoDetails.get(i).getVideoTitle());
                    video.setDescription(ukgVideoDetails.get(i).getVideoDescription());
                    video.setCardImageUrl(ukgVideoDetails.get(i).getVideoCardImg());
                    video.setVideoUrl(ukgVideoDetails.get(i).getVideoUrl());
                    actualVideoList.add(video);
                }
                break;
            case "nursery":
                for(int i = 0;i<size;i++){
                    Video video = new Video();
                    video.setId(nurseryVideoDetails.get(i).getVideoId());
                    video.setTitle(nurseryVideoDetails.get(i).getVideoTitle());
                    video.setDescription(nurseryVideoDetails.get(i).getVideoDescription());
                    video.setCardImageUrl(nurseryVideoDetails.get(i).getVideoCardImg());
                    video.setVideoUrl(nurseryVideoDetails.get(i).getVideoUrl());
                    actualVideoList.add(video);
                }
                break;
            case "playgroup":
                for(int i = 0;i<size;i++){
                    Video video = new Video();
                    video.setId(playgroupVideoDetails.get(i).getVideoId());
                    video.setTitle(playgroupVideoDetails.get(i).getVideoTitle());
                    video.setDescription(playgroupVideoDetails.get(i).getVideoDescription());
                    video.setCardImageUrl(playgroupVideoDetails.get(i).getVideoCardImg());
                    video.setVideoUrl(playgroupVideoDetails.get(i).getVideoUrl());
                    actualVideoList.add(video);
                }
                break;

            case "downloaded":
                String[] title_desc = new String[2];
                for(int i = 0;i<size;i++){
                    Video video = new Video();
                    video.setId(downloadedVideoId.get(i));
                    //Fetch Title & Desc from downloadedVideoDesc
                    File textFile = new File(downloadedVideoDesc.get(i));
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(textFile));
                        String st;
                        int iterator = 0;
                        while ((st = br.readLine()) != null) {
                            title_desc[iterator] = st;
                            iterator++;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    video.setTitle(title_desc[0]);
                    video.setDescription(title_desc[1]);
                    video.setCardImageUrl(downloadedCardImage.get(i));
                    video.setVideoUrl(downloadedVideoName.get(i));
                    actualVideoList.add(video);
                }
                break;
        }
    }

    @SuppressLint("StaticFieldLeak")
    class FetchVideoDetails extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                ScanRequest request = new ScanRequest().withTableName(Config.LKGTABLENAME);
                ScanResult response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> userRows = response.getItems();
                for (Map<String, AttributeValue> map : userRows) {
                    LKGVideosDO lkgVideosDO = dynamoDBMapper.load(LKGVideosDO.class, map.get("video_id").getS(),
                            map.get("video_title").getS());
                    lkgVideoDetails.add(lkgVideosDO);
                }

                ScanRequest request2 = new ScanRequest().withTableName(Config.UKGTABLENAME);
                ScanResult response2 = dynamoDBClient.scan(request2);
                List<Map<String, AttributeValue>> userRows2 = response2.getItems();
                for (Map<String, AttributeValue> map : userRows2) {
                    UKGVideosDO ukgVideosDO = dynamoDBMapper.load(UKGVideosDO.class, map.get("video_id").getS(),
                            map.get("video_title").getS());
                    ukgVideoDetails.add(ukgVideosDO);
                }

                ScanRequest request3 = new ScanRequest().withTableName(Config.NURSERYTABLENAME);
                ScanResult response3 = dynamoDBClient.scan(request3);
                List<Map<String, AttributeValue>> userRows3 = response3.getItems();
                for (Map<String, AttributeValue> map : userRows3) {
                    NurseryVideosDO nurseryVideosDO = dynamoDBMapper.load(NurseryVideosDO.class, map.get("video_id").getS(),
                            map.get("video_title").getS());
                    nurseryVideoDetails.add(nurseryVideosDO);
                }


                ScanRequest request4 = new ScanRequest().withTableName(Config.PLAYGROUPTABLENAME);
                ScanResult response4 = dynamoDBClient.scan(request4);
                List<Map<String, AttributeValue>> userRows4 = response4.getItems();
                for (Map<String, AttributeValue> map : userRows4) {
                    PlaygroupVideosDO playgroupVideosDO = dynamoDBMapper.load(PlaygroupVideosDO.class, map.get("video_id").getS(),
                            map.get("video_title").getS());
                    playgroupVideoDetails.add(playgroupVideosDO);
                }

                return true;
            } catch (AmazonClientException e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean s) {
            AlertDialog alertDialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            if(!s){
                builder.setTitle("Please Check Your Network Connection");
                alertDialog = builder.create();
                alertDialog.show();
            }
            super.onPostExecute(s);
        }
    }
}
