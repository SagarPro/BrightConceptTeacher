package sagu.supro.BCT.levels.lkg;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import sagu.supro.BCT.dynamo.LKGVideosDO;
import sagu.supro.BCT.leanback_lib.CardPresenter;
import sagu.supro.BCT.leanback_lib.Video;
import sagu.supro.BCT.utils.AWSProvider;
import sagu.supro.BCT.utils.Config;

public class LkgVideoList {
    private List<String> VIDEO_CATEGORY = new ArrayList<>();
    private List<Video> actualVideoList = new ArrayList<>();
    public static List<Video> searchVideosList = new ArrayList<>();

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper dynamoDBMapper;
    private Context context;

    //Retrieved DOs with Value
    private List<LKGVideosDO> goodHabitsAndSafety = new ArrayList<>();
    private List<LKGVideosDO> generalKnowledge = new ArrayList<>();
    private List<LKGVideosDO> rhymes = new ArrayList<>();
    private List<LKGVideosDO> phonics = new ArrayList<>();
    private List<LKGVideosDO> alphabetAndNumberWriting = new ArrayList<>();

    private List<String> downloadedVideoName = new ArrayList<>();
    private List<String> downloadedVideoId = new ArrayList<>();
    private List<String> downloadedCardImage = new ArrayList<>();
    private List<String> downloadedVideoDesc = new ArrayList<>();

    public static List<String> offlineVideos = new ArrayList<>();

    private ArrayObjectAdapter rowsAdapter;

    LkgVideoList(Context context){
        this.context=context;
        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(context));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
    }

    public ArrayObjectAdapter setupLkgVideos() {
        clearAllLists();  int downloadVideos = 0;

        VIDEO_CATEGORY.add("GOOD HABITS & SAFTEY");
        VIDEO_CATEGORY.add("GENERAL KNOWLEDGE");
        VIDEO_CATEGORY.add("PHONICS");
        VIDEO_CATEGORY.add("RHYMES");
        VIDEO_CATEGORY.add("ALPHABET & NUMBER WRITING");

        try {
            downloadVideos = getTotalDownloadedProjects();
        } catch (NullPointerException e){
            Log.d("NullPointerException", e.getMessage());
        }
        
        try {
            Boolean bgStatus = new FetchVideoDetails().execute().get();
            if (bgStatus) {

                sortVideos();

                rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
                CardPresenter cardPresenter = new CardPresenter();

                if (downloadVideos != 0){VIDEO_CATEGORY.add("DOWNLOADED");}

                int i;
                for (i = 0; i < VIDEO_CATEGORY.size(); i++) {
                    ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    actualVideoList.clear();
                    int NUM_COLS=i;
                    switch (i) {
                        case 0:
                            NUM_COLS = goodHabitsAndSafety.size();
                            updateActualList(goodHabitsAndSafety);
                            break;
                        case 1:
                            NUM_COLS = generalKnowledge.size();
                            updateActualList(generalKnowledge);
                            break;
                        case 2:
                            NUM_COLS = rhymes.size();
                            updateActualList(rhymes);
                            break;
                        case 3:
                            NUM_COLS = phonics.size();
                            updateActualList(phonics);
                            break;
                        case 4:
                            NUM_COLS = alphabetAndNumberWriting.size();
                            updateActualList(alphabetAndNumberWriting);
                            break;
                        case 5:
                            NUM_COLS = downloadVideos;
                            addAllDownloadedVideosToRow(downloadVideos);
                            break;
                    }
                    for (int j = 0; j < NUM_COLS; j++) {
                        listRowAdapter.add(actualVideoList.get(j));
                    }
                    HeaderItem header = new HeaderItem(i, VIDEO_CATEGORY.get(i));
                    rowsAdapter.add(new ListRow(header, listRowAdapter));
                }
            } else {
                systemIsOfflineSoAddDownloaded();
            }
        } catch (Exception e) {

            e.printStackTrace();
        }

        return rowsAdapter;

    }

    private void sortVideos() {
        Collections.sort(goodHabitsAndSafety, new LKGVideoIdComparator());
        Collections.sort(generalKnowledge, new LKGVideoIdComparator());
        Collections.sort(phonics, new LKGVideoIdComparator());
        Collections.sort(rhymes, new LKGVideoIdComparator());
        Collections.sort(alphabetAndNumberWriting, new LKGVideoIdComparator());
    }

    private void addAllDownloadedVideosToRow(int size) {
        String[] title_desc = new String[3];
        for(int j = 0;j<size;j++){
            Video video = new Video();
            video.setId(downloadedVideoId.get(j));
            File textFile = new File(downloadedVideoDesc.get(j));
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
            video.setCardImageUrl(downloadedCardImage.get(j));
            video.setVideoUrl(downloadedVideoName.get(j));
            if(title_desc[2].equals("LKG")){
                actualVideoList.add(video);
                offlineVideos.add(video.getId());
            }
        }
    }

    private void systemIsOfflineSoAddDownloaded() {
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

            addAllDownloadedVideosToRow(downloadVideos);

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

    private void updateActualList(List<LKGVideosDO> topic) {
        Video video = new Video();
        for(int i = 0;i<topic.size();i++){
            video.setId(topic.get(i).getVideoId());
            video.setTitle(topic.get(i).getVideoTitle());
            video.setDescription(topic.get(i).getVideoDescription());
            video.setCardImageUrl(topic.get(i).getVideoCardImg());
            video.setVideoUrl(topic.get(i).getVideoUrl());
            video.setVideoTopic(topic.get(i).getVideoTopic());
            actualVideoList.add(video);
            searchVideosList.add(video);
        }
    }

    public List<String> getOfflineVideos(){
        return offlineVideos;
    }

    private int getTotalDownloadedProjects() {
        File directory=new File(Environment.getExternalStorageDirectory()+"/BCT/LKG");
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

    private String getMimeType(String fileUrl) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileUrl);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private void clearAllLists() {
        downloadedVideoId.clear();
        downloadedVideoDesc.clear();
        downloadedCardImage.clear();
        downloadedVideoName.clear();
        offlineVideos.clear();
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
                    LKGVideosDO nurseryVideosDO = dynamoDBMapper
                            .load(LKGVideosDO.class, map.get("video_id").getS(),
                            map.get("video_title").getS());

                    switch (map.get("video_topic").getS()){
                        case "PHONICS" :
                            phonics.add(nurseryVideosDO);
                            break;
                        case "RHYMES":
                            rhymes.add(nurseryVideosDO);
                            break;
                        case "GENERAL KNOWLEDGE":
                            generalKnowledge.add(nurseryVideosDO);
                            break;
                        case "GOOD HABITS & SAFTEY" :
                            goodHabitsAndSafety.add(nurseryVideosDO);
                            break;
                        case "ALPHABET & NUMBER WRITING":
                            alphabetAndNumberWriting.add(nurseryVideosDO);
                            break;
                    }
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

    private class LKGVideoIdComparator implements Comparator<LKGVideosDO> {
        @Override
        public int compare(LKGVideosDO v1, LKGVideosDO v2) {
            return v1.getVideoId().compareTo(v2.getVideoId());
        }
    }
}