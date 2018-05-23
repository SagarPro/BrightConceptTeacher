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
import android.util.Log;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import sagu.supro.BCT.dynamo.LKGVideosDO;
import sagu.supro.BCT.dynamo.NurseryVideosDO;
import sagu.supro.BCT.dynamo.PlaygroupVideosDO;
import sagu.supro.BCT.dynamo.UKGVideosDO;
import sagu.supro.BCT.utils.AWSProvider;
import sagu.supro.BCT.utils.Config;

public class VideoList {

    private List<String> VIDEO_CATEGORY = new ArrayList<>();
    private List<Video> actualVideoList = new ArrayList<>();
    public static List<Video> searchVideosList = new ArrayList<>();

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

    public static List<String> offlineVideos = new ArrayList<>();

    private ArrayObjectAdapter rowsAdapter;

    public VideoList(Context context){
        this.context=context;
    }

    public ArrayObjectAdapter setupMovies() {

        clearAllLists();

        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(context));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        int downloadVideos = 0;
        try {
            downloadVideos = getTotalDownloadedProjects();
        } catch (NullPointerException e){
            Log.d("NullPointerException", e.getMessage());
        }

        try {
            Boolean bgStatus = new FetchVideoDetails().execute().get();
            if (bgStatus) {
                rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
                CardPresenter cardPresenter = new CardPresenter();

                VIDEO_CATEGORY.add("LKG");
                VIDEO_CATEGORY.add("UKG");
                VIDEO_CATEGORY.add("Nursery");
                VIDEO_CATEGORY.add("Playgroup");

                if (downloadVideos != 0){
                    VIDEO_CATEGORY.add("Downloaded");
                }

                int i;
                for (i = 0; i < VIDEO_CATEGORY.size(); i++) {
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
                            NUM_COLS = downloadVideos;
                            updateActualList("downloaded",NUM_COLS);
                            break;
                    }
                    for (int j = 0; j < NUM_COLS; j++) {
                        listRowAdapter.add(actualVideoList.get(j));
                    }
                    HeaderItem header = new HeaderItem(i, VIDEO_CATEGORY.get(i));
                    rowsAdapter.add(new ListRow(header, listRowAdapter));
                }
            } else {
                setDownloadedVideos();
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return rowsAdapter;
    }

    private void setDownloadedVideos(){

        int downloadVideos = 0;
        try {
            downloadVideos = getTotalDownloadedProjects();
        } catch (NullPointerException e){
            Log.d("NullPointerException", e.getMessage());
        }

        rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();

        if (downloadVideos != 0){

            VIDEO_CATEGORY.add("DOWNLOADED");

            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            actualVideoList.clear();
            updateActualList("downloaded",downloadVideos);
            int i=0;
            while (i<actualVideoList.size()) {
                listRowAdapter.add(actualVideoList.get(i));
                i++;
            }
            searchVideosList.addAll(actualVideoList);

            HeaderItem header = new HeaderItem(0, VIDEO_CATEGORY.get(0));
            rowsAdapter.add(new ListRow(header, listRowAdapter));

        } else {

            AlertDialog alertDialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setTitle("No Downloaded Videos");
            alertDialog = builder.create();
            alertDialog.show();

        }
    }

    private int getTotalDownloadedProjects() {
        File directory=new File(Environment.getExternalStorageDirectory()+"/BCT");
        getDownloadedVideoNames(directory);
        return directory.list().length;
    }

    private void getDownloadedVideoNames(File directory) {
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
                downloadedVideoId.add(currentFile.getName());
                getDownloadedVideoNames(currentFile);
            }
        }
    }

    private void clearAllLists() {
        downloadedVideoId.clear();
        downloadedVideoDesc.clear();
        downloadedCardImage.clear();
        downloadedVideoName.clear();

        offlineVideos.clear();
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
                    video.setVideoTopic(lkgVideoDetails.get(i).getVideoTopic());
                    actualVideoList.add(video);
                    searchVideosList.add(video);
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
                    video.setVideoTopic(ukgVideoDetails.get(i).getVideoTopic());
                    actualVideoList.add(video);
                    searchVideosList.add(video);
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
                    video.setVideoTopic(nurseryVideoDetails.get(i).getVideoTopic());
                    actualVideoList.add(video);
                    searchVideosList.add(video);
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
                    video.setVideoTopic(playgroupVideoDetails.get(i).getVideoTopic());
                    actualVideoList.add(video);
                    searchVideosList.add(video);
                }
                break;

            case "downloaded":
                String[] title_desc = new String[2];
                for(int i = 0;i<size;i++){
                    Video video = new Video();
                    video.setId(downloadedVideoId.get(i));
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
                    offlineVideos.add(video.getId());
                }
                break;
        }
    }

    public List<String> getOfflineVideos(){
        return offlineVideos;
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

                Collections.sort(lkgVideoDetails, new LKGVideoIdComparator());

                ScanRequest request2 = new ScanRequest().withTableName(Config.UKGTABLENAME);
                ScanResult response2 = dynamoDBClient.scan(request2);
                List<Map<String, AttributeValue>> userRows2 = response2.getItems();
                for (Map<String, AttributeValue> map : userRows2) {
                    UKGVideosDO ukgVideosDO = dynamoDBMapper.load(UKGVideosDO.class, map.get("video_id").getS(),
                            map.get("video_title").getS());
                    ukgVideoDetails.add(ukgVideosDO);
                }

                Collections.sort(ukgVideoDetails, new UKGVideoIdComparator());

                ScanRequest request3 = new ScanRequest().withTableName(Config.NURSERYTABLENAME);
                ScanResult response3 = dynamoDBClient.scan(request3);
                List<Map<String, AttributeValue>> userRows3 = response3.getItems();
                for (Map<String, AttributeValue> map : userRows3) {
                    NurseryVideosDO nurseryVideosDO = dynamoDBMapper.load(NurseryVideosDO.class, map.get("video_id").getS(),
                            map.get("video_title").getS());
                    nurseryVideoDetails.add(nurseryVideosDO);
                }

                Collections.sort(nurseryVideoDetails, new NurseryVideoIdComparator());


                ScanRequest request4 = new ScanRequest().withTableName(Config.PLAYGROUPTABLENAME);
                ScanResult response4 = dynamoDBClient.scan(request4);
                List<Map<String, AttributeValue>> userRows4 = response4.getItems();
                for (Map<String, AttributeValue> map : userRows4) {
                    PlaygroupVideosDO playgroupVideosDO = dynamoDBMapper.load(PlaygroupVideosDO.class, map.get("video_id").getS(),
                            map.get("video_title").getS());
                    playgroupVideoDetails.add(playgroupVideosDO);
                }

                Collections.sort(playgroupVideoDetails, new PlaygroupVideoIdComparator());

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

    private class LKGVideoIdComparator implements Comparator<LKGVideosDO>{
        @Override
        public int compare(LKGVideosDO v1, LKGVideosDO v2) {
            return v1.getVideoId().compareTo(v2.getVideoId());
        }
    }

    private class UKGVideoIdComparator implements Comparator<UKGVideosDO>{
        @Override
        public int compare(UKGVideosDO v1, UKGVideosDO v2) {
            return v1.getVideoId().compareTo(v2.getVideoId());
        }
    }

    private class NurseryVideoIdComparator implements Comparator<NurseryVideosDO>{
        @Override
        public int compare(NurseryVideosDO v1, NurseryVideosDO v2) {
            return v1.getVideoId().compareTo(v2.getVideoId());
        }
    }

    private class PlaygroupVideoIdComparator implements Comparator<PlaygroupVideosDO>{
        @Override
        public int compare(PlaygroupVideosDO v1, PlaygroupVideosDO v2) {
            return v1.getVideoId().compareTo(v2.getVideoId());
        }
    }

}
