package sagu.supro.BCT.levels.nursery;

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

import sagu.supro.BCT.dynamo.LKGVideosDO;
import sagu.supro.BCT.dynamo.NurseryVideosDO;
import sagu.supro.BCT.dynamo.PlaygroupVideosDO;
import sagu.supro.BCT.dynamo.UKGVideosDO;
import sagu.supro.BCT.leanback_lib.CardPresenter;
import sagu.supro.BCT.leanback_lib.Video;
import sagu.supro.BCT.leanback_lib.VideoList;
import sagu.supro.BCT.utils.AWSProvider;
import sagu.supro.BCT.utils.Config;

public class NurseryVideoList {

    private List<String> VIDEO_CATEGORY = new ArrayList<>();
    private List<Video> actualVideoList = new ArrayList<>();
    public static List<Video> searchVideosList = new ArrayList<>();

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper dynamoDBMapper;
    private Context context;

    private List<NurseryVideosDO> nurseryVideos = new ArrayList<>();

    private List<NurseryVideosDO> phonics = new ArrayList<>();
    private List<NurseryVideosDO> numbersCounting = new ArrayList<>();
    private List<NurseryVideosDO> rhymes = new ArrayList<>();
    private List<NurseryVideosDO> stories = new ArrayList<>();
    private List<NurseryVideosDO> generalKnowledge = new ArrayList<>();
    private List<NurseryVideosDO> airTransportation = new ArrayList<>();
    private List<NurseryVideosDO> surfaceTransportation = new ArrayList<>();
    private List<NurseryVideosDO> goodHabits = new ArrayList<>();
    private List<NurseryVideosDO> fruits = new ArrayList<>();
    private List<NurseryVideosDO> vegetables = new ArrayList<>();
    private List<NurseryVideosDO> healthyFoods = new ArrayList<>();
    private List<NurseryVideosDO> farmAnimals = new ArrayList<>();
    private List<NurseryVideosDO> jungleAnimals = new ArrayList<>();
    private List<NurseryVideosDO> alphabetNumberWriting = new ArrayList<>();

    private List<String> downloadedVideoName = new ArrayList<>();
    private List<String> downloadedVideoId = new ArrayList<>();
    private List<String> downloadedCardImage = new ArrayList<>();
    private List<String> downloadedVideoDesc = new ArrayList<>();

    //private ArrayObjectAdapter listRowAdapter;

    public static List<String> offlineVideos = new ArrayList<>();

    int downloadVideos = 0;

    private ArrayObjectAdapter rowsAdapter;

    public NurseryVideoList(Context context){
        this.context=context;
        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(context));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
    }

    public ArrayObjectAdapter setupNurseryVideos() {

        clearAllLists();

        try {
            downloadVideos = getTotalDownloadedProjects();
        } catch (NullPointerException e){
            Log.d("NullPointerException", e.getMessage());
        }

        try {
            Boolean bgStatus = new FetchVideoDetails().execute().get();
            if (bgStatus) {

                Collections.sort(nurseryVideos, new NurseryVideoIdComparator());

                rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
                CardPresenter cardPresenter = new CardPresenter();

                if (downloadVideos != 0){
                    VIDEO_CATEGORY.add("Downloaded");
                }

                int i;
                for (i = 0; i < VIDEO_CATEGORY.size(); i++) {
                    ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    actualVideoList.clear();
                    switch (i) {
                        case 0:
                            updateActualList(phonics);
                            break;
                        case 1:
                            updateActualList(numbersCounting);
                            break;
                        case 2:
                            updateActualList(rhymes);
                            break;
                        case 3:
                            updateActualList(stories);
                            break;
                        case 4:
                            updateActualList(generalKnowledge);
                            break;
                        case 5:
                            updateActualList(airTransportation);
                            break;
                        case 6:
                            updateActualList(surfaceTransportation);
                            break;
                        case 7:
                            updateActualList(goodHabits);
                            break;
                        case 8:
                            updateActualList(fruits);
                            break;
                        case 9:
                            updateActualList(vegetables);
                            break;
                        case 10:
                            updateActualList(healthyFoods);
                            break;
                        case 11:
                            updateActualList(farmAnimals);
                            break;
                        case 12:
                            updateActualList(jungleAnimals);
                            break;
                        case 13:
                            updateActualList(alphabetNumberWriting);
                            break;
                        case 14:
                            addAllDownloadedVideosToRow(downloadVideos);
                            break;
                    }
                    for (int j = 0; j < i; j++) {
                        listRowAdapter.add(actualVideoList.get(j));
                    }
                    HeaderItem header = new HeaderItem(i, VIDEO_CATEGORY.get(i));
                    rowsAdapter.add(new ListRow(header, listRowAdapter));
                }
            } else {
                //setDownloadedVideos();
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return rowsAdapter;
    }

    private void clearAllLists() {
        downloadedVideoId.clear();
        downloadedVideoDesc.clear();
        downloadedCardImage.clear();
        downloadedVideoName.clear();

        offlineVideos.clear();
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

    private String getMimeType(String fileUrl) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileUrl);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private void updateActualList(List<NurseryVideosDO> topic) {
        Video video = new Video();
        for (int i=0; i<topic.size(); i++) {
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

    private void addAllDownloadedVideosToRow(int size) {
        String[] title_desc = new String[2];
        for (int i = 0; i < size; i++) {
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
            } catch (Exception e) {
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
    }

    /*private ArrayObjectAdapter updateActualList() {

        for (int i=0; i<nurseryVideos.size(); i++){
            switch (nurseryVideos.get(i).getVideoTopic()){
                        case "PHONICS" :
                            phonics.add(nurseryVideos.get(i));
                            //actualVideoList.add(video);
                            break;
                        case "NUMBERS & COUNTING":
                            numbersCounting.add(nurseryVideos.get(i));
                            break;
                        case "RHYMES":
                            rhymes.add(nurseryVideos.get(i));
                            break;
                        case "STORIES":
                            stories.add(nurseryVideos.get(i));
                            break;
                        case "GENERAL KNOWLEDGE":
                            generalKnowledge.add(nurseryVideos.get(i));
                            break;
                        case "AIR TRANSPORTATION" :
                            airTransportation.add(nurseryVideos.get(i));
                            break;
                        case "SURFACE TRANSPORTATION":
                            surfaceTransportation.add(nurseryVideos.get(i));
                            break;
                        case "GOOD HABITS":
                            goodHabits.add(nurseryVideos.get(i));
                            break;
                        case "FRUITS":
                            fruits.add(nurseryVideos.get(i));
                            break;
                        case "VEGETABLES":
                            vegetables.add(nurseryVideos.get(i));
                            break;
                        case "HEALTHY FOODS" :
                            healthyFoods.add(nurseryVideos.get(i));
                            break;
                        case "FARM ANIMALS":
                            farmAnimals.add(nurseryVideos.get(i));
                            break;
                        case "JUNGLE ANIMALS":
                            jungleAnimals.add(nurseryVideos.get(i));
                            break;
                        case "ALPHABET & NUMBER WRITING":
                            alphabetNumberWriting.add(nurseryVideos.get(i));
                            break;
                *//*case "downloaded":
                    String[] title_desc = new String[2];
                    for(int i1 = 0;i<downloadVideos;i++){
                        Video video = new Video();
                        video.setId(downloadedVideoId.get(i1));
                        File textFile = new File(downloadedVideoDesc.get(i1));
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
                        video.setCardImageUrl(downloadedCardImage.get(i1));
                        video.setVideoUrl(downloadedVideoName.get(i1));
                        actualVideoList.add(video);
                        offlineVideos.add(video.getId());
                    }
                    break;*//*
                    }

            Video video = new Video();

            video.setId(nurseryVideos.get(i).getVideoId());
            video.setTitle(nurseryVideos.get(i).getVideoTitle());
            video.setDescription(nurseryVideos.get(i).getVideoDescription());
            video.setCardImageUrl(nurseryVideos.get(i).getVideoCardImg());
            video.setVideoUrl(nurseryVideos.get(i).getVideoUrl());
            video.setVideoTopic(nurseryVideos.get(i).getVideoTopic());

            actualVideoList.add(video);

            for (int j = 0; j < i; j++) {
                listRowAdapter.add(actualVideoList.get(j));
            }
            HeaderItem header = new HeaderItem(i, VIDEO_CATEGORY.get(i));
            rowsAdapter.add(new ListRow(header, listRowAdapter));
        }

        return rowsAdapter;

    }*/

    public List<String> getOfflineVideos(){
        return offlineVideos;
    }

    @SuppressLint("StaticFieldLeak")
    class FetchVideoDetails extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            VIDEO_CATEGORY.clear();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                ScanRequest request = new ScanRequest().withTableName(Config.NURSERYTABLENAME);
                ScanResult response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> userRows = response.getItems();
                for (Map<String, AttributeValue> map : userRows) {
                    NurseryVideosDO nurseryVideosDO = dynamoDBMapper.load(NurseryVideosDO.class, map.get("video_id").getS(),
                            map.get("video_title").getS());
                    //nurseryVideos.add(nurseryVideosDO);
                    if (!VIDEO_CATEGORY.contains(map.get("video_topic").getS()))
                        VIDEO_CATEGORY.add(map.get("video_topic").getS());

                    switch (map.get("video_topic").getS()){
                        case "PHONICS" :
                            phonics.add(nurseryVideosDO);
                            break;
                        case "NUMBERS & COUNTING":
                            numbersCounting.add(nurseryVideosDO);
                            break;
                        case "RHYMES":
                            rhymes.add(nurseryVideosDO);
                            break;
                        case "STORIES":
                            stories.add(nurseryVideosDO);
                            break;
                        case "GENERAL KNOWLEDGE":
                            generalKnowledge.add(nurseryVideosDO);
                            break;
                        case "AIR TRANSPORTATION" :
                            airTransportation.add(nurseryVideosDO);
                            break;
                        case "SURFACE TRANSPORTATION":
                            surfaceTransportation.add(nurseryVideosDO);
                            break;
                        case "GOOD HABITS":
                            goodHabits.add(nurseryVideosDO);
                            break;
                        case "FRUITS":
                            fruits.add(nurseryVideosDO);
                            break;
                        case "VEGETABLES":
                            vegetables.add(nurseryVideosDO);
                            break;
                        case "HEALTHY FOODS" :
                            healthyFoods.add(nurseryVideosDO);
                            break;
                        case "FARM ANIMALS":
                            farmAnimals.add(nurseryVideosDO);
                            break;
                        case "JUNGLE ANIMALS":
                            jungleAnimals.add(nurseryVideosDO);
                            break;
                        case "ALPHABET & NUMBER WRITING":
                            alphabetNumberWriting.add(nurseryVideosDO);
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

    private class NurseryVideoIdComparator implements Comparator<NurseryVideosDO> {
        @Override
        public int compare(NurseryVideosDO v1, NurseryVideosDO v2) {
            return v1.getVideoId().compareTo(v2.getVideoId());
        }
    }

}
