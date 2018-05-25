package sagu.supro.BCT.levels.playgroup;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import sagu.supro.BCT.dynamo.PlaygroupVideosDO;
import sagu.supro.BCT.leanback_lib.CardPresenter;
import sagu.supro.BCT.leanback_lib.Video;
import sagu.supro.BCT.utils.AWSProvider;
import sagu.supro.BCT.utils.Config;

public class PlaygroupVideoList {
    private List<String> VIDEO_CATEGORY = new ArrayList<>();
    private List<Video> actualVideoList = new ArrayList<>();
    public static List<Video> p_searchVideosList = new ArrayList<>();

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper dynamoDBMapper;
    private Context context;

    //Retrieved DOs with Value
    private List<PlaygroupVideosDO> alphabetAToZ = new ArrayList<>();
    private List<PlaygroupVideosDO> numbers1To20 = new ArrayList<>();
    private List<PlaygroupVideosDO> rhymes = new ArrayList<>();
    private List<PlaygroupVideosDO> stories = new ArrayList<>();
    private List<PlaygroupVideosDO> waterTransportation = new ArrayList<>();
    private List<PlaygroupVideosDO> airTransportation = new ArrayList<>();
    private List<PlaygroupVideosDO> surfaceTransportation = new ArrayList<>();
    private List<PlaygroupVideosDO> goodHabits = new ArrayList<>();
    private List<PlaygroupVideosDO> fruits = new ArrayList<>();
    private List<PlaygroupVideosDO> vegetables = new ArrayList<>();
    private List<PlaygroupVideosDO> healthyFoods = new ArrayList<>();
    private List<PlaygroupVideosDO> farmAnimals = new ArrayList<>();
    private List<PlaygroupVideosDO> jungleAnimals = new ArrayList<>();
    private List<PlaygroupVideosDO> generalKnowledge = new ArrayList<>();

    private List<String> downloadedVideoName = new ArrayList<>();
    private List<String> downloadedVideoId = new ArrayList<>();
    private List<String> downloadedCardImage = new ArrayList<>();
    private List<String> downloadedVideoDesc = new ArrayList<>();

    public static List<String> offlineVideos = new ArrayList<>();
    //public static List<String> searchVideos = new ArrayList<>();

    private ArrayObjectAdapter rowsAdapter;

    PlaygroupVideoList(Context context){
        this.context=context;
        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(context));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
    }

    public ArrayObjectAdapter setupPlaygroupVideos() {
        clearAllLists();  int downloadVideos = 0;

        try {
            downloadVideos = getTotalDownloadedProjects();
        } catch (NullPointerException e){
            Log.d("NullPointerException", e.getMessage());
        }

        try {
            Boolean bgStatus = new FetchVideoDetails().execute().get();
            if (bgStatus) {

                VIDEO_CATEGORY.add("ALPHABET A TO Z");
                VIDEO_CATEGORY.add("NUMBERS 1 TO 20 & COUNTING");
                VIDEO_CATEGORY.add("RHYMES");
                VIDEO_CATEGORY.add("STORIES");
                VIDEO_CATEGORY.add("WATER TRANSPORTATION");
                VIDEO_CATEGORY.add("AIR TRANSPORTATION");
                VIDEO_CATEGORY.add("SURFACE TRANSPORTATION");
                VIDEO_CATEGORY.add("GOOD HABITS");
                VIDEO_CATEGORY.add("FRUITS");
                VIDEO_CATEGORY.add("VEGETABLES");
                VIDEO_CATEGORY.add("HEALTHY FOODS");
                VIDEO_CATEGORY.add("FARM ANIMALS");
                VIDEO_CATEGORY.add("JUNGLE ANIMALS");
                VIDEO_CATEGORY.add("GENERAL KNOWLEDGE");

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
                            NUM_COLS = alphabetAToZ.size();
                            updateActualList(alphabetAToZ);
                            break;
                        case 1:
                            NUM_COLS = numbers1To20.size();
                            updateActualList(numbers1To20);
                            break;
                        case 2:
                            NUM_COLS = rhymes.size();
                            updateActualList(rhymes);
                            break;
                        case 3:
                            NUM_COLS = stories.size();
                            updateActualList(stories);
                            break;
                        case 4:
                            NUM_COLS = waterTransportation.size();
                            updateActualList(waterTransportation);
                            break;
                        case 5:
                            NUM_COLS = airTransportation.size();
                            updateActualList(airTransportation);
                            break;
                        case 6:
                            NUM_COLS = surfaceTransportation.size();
                            updateActualList(surfaceTransportation);
                            break;
                        case 7:
                            NUM_COLS = goodHabits.size();
                            updateActualList(goodHabits);
                            break;
                        case 8:
                            NUM_COLS = fruits.size();
                            updateActualList(fruits);
                            break;
                        case 9:
                            NUM_COLS = vegetables.size();
                            updateActualList(vegetables);
                            break;
                        case 10:
                            NUM_COLS = healthyFoods.size();
                            updateActualList(healthyFoods);
                            break;
                        case 11:
                            NUM_COLS = farmAnimals.size();
                            updateActualList(farmAnimals);
                            break;
                        case 12:
                            NUM_COLS = jungleAnimals.size();
                            updateActualList(jungleAnimals);
                            break;
                        case 13:
                            NUM_COLS = generalKnowledge.size();
                            updateActualList(generalKnowledge);
                            break;
                        case 14:
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
                systemIsOfflineSoAddDownloaded(downloadVideos);
            }
        } catch (Exception e) {

            e.printStackTrace();
        }

        return rowsAdapter;

    }

    private void sortVideos() {
        Collections.sort(alphabetAToZ, new PlaygroupVideoIdComparator());
        Collections.sort(numbers1To20, new PlaygroupVideoIdComparator());
        Collections.sort(rhymes, new PlaygroupVideoIdComparator());
        Collections.sort(stories, new PlaygroupVideoIdComparator());
        Collections.sort(waterTransportation, new PlaygroupVideoIdComparator());
        Collections.sort(airTransportation, new PlaygroupVideoIdComparator());
        Collections.sort(surfaceTransportation, new PlaygroupVideoIdComparator());
        Collections.sort(goodHabits, new PlaygroupVideoIdComparator());
        Collections.sort(fruits, new PlaygroupVideoIdComparator());
        Collections.sort(vegetables, new PlaygroupVideoIdComparator());
        Collections.sort(healthyFoods, new PlaygroupVideoIdComparator());
        Collections.sort(farmAnimals, new PlaygroupVideoIdComparator());
        Collections.sort(jungleAnimals, new PlaygroupVideoIdComparator());
        Collections.sort(generalKnowledge, new PlaygroupVideoIdComparator());
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
            if(title_desc[2].equals("Playgroup")){
                actualVideoList.add(video);
                offlineVideos.add(video.getId());
            }
        }
    }

    private void systemIsOfflineSoAddDownloaded(int downloadVideos) {

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
            p_searchVideosList.addAll(actualVideoList);

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

    private void updateActualList(List<PlaygroupVideosDO> topic) {
        Video video = new Video();
        for(int i = 0;i<topic.size();i++){
            video.setId(topic.get(i).getVideoId());
            video.setTitle(topic.get(i).getVideoTitle());
            video.setDescription(topic.get(i).getVideoDescription());
            video.setCardImageUrl(topic.get(i).getVideoCardImg());
            video.setVideoUrl(topic.get(i).getVideoUrl());
            video.setVideoTopic(topic.get(i).getVideoTopic());
            actualVideoList.add(video);
            p_searchVideosList.add(video);
        }
    }

    public List<String> getOfflineVideos(){
        return offlineVideos;
    }

    private int getTotalDownloadedProjects() {
        File directory=new File(Environment.getExternalStorageDirectory()+"/.BCT/Playgroup");
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
        //searchVideos.clear();
    }

    @SuppressLint("StaticFieldLeak")
    class FetchVideoDetails extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                ScanRequest request = new ScanRequest().withTableName(Config.PLAYGROUPTABLENAME);
                ScanResult response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> userRows = response.getItems();
                for (Map<String, AttributeValue> map : userRows) {
                    PlaygroupVideosDO playgroupVideosDO = dynamoDBMapper
                            .load(PlaygroupVideosDO.class, map.get("video_id").getS(),
                                    map.get("video_title").getS());

                    switch (map.get("video_topic").getS()){
                        case "ALPHABET A TO Z" :
                            alphabetAToZ.add(playgroupVideosDO);
                            break;
                        case "NUMBERS 1 TO 20":
                            numbers1To20.add(playgroupVideosDO);
                            break;
                        case "RHYMES":
                            rhymes.add(playgroupVideosDO);
                            break;
                        case "STORIES":
                            stories.add(playgroupVideosDO);
                            break;
                        case "GENERAL KNOWLEDGE":
                            generalKnowledge.add(playgroupVideosDO);
                            break;
                        case "AIR TRANSPORTATION" :
                            airTransportation.add(playgroupVideosDO);
                            break;
                        case "SURFACE TRANSPORTATION":
                            surfaceTransportation.add(playgroupVideosDO);
                            break;
                        case "GOOD HABITS":
                            goodHabits.add(playgroupVideosDO);
                            break;
                        case "FRUITS":
                            fruits.add(playgroupVideosDO);
                            break;
                        case "VEGETABLES":
                            vegetables.add(playgroupVideosDO);
                            break;
                        case "HEALTHY FOODS" :
                            healthyFoods.add(playgroupVideosDO);
                            break;
                        case "FARM ANIMALS":
                            farmAnimals.add(playgroupVideosDO);
                            break;
                        case "JUNGLE ANIMALS":
                            jungleAnimals.add(playgroupVideosDO);
                            break;
                        case "WATER TRANSPORTATION":
                            waterTransportation.add(playgroupVideosDO);
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

    private class PlaygroupVideoIdComparator implements Comparator<PlaygroupVideosDO> {
        @Override
        public int compare(PlaygroupVideosDO v1, PlaygroupVideosDO v2) {
            return v1.getVideoId().compareTo(v2.getVideoId());
        }
    }
}