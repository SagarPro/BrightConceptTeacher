package sagu.supro.BCT.levels.ukg;

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

import sagu.supro.BCT.dynamo.NurseryVideosDO;
import sagu.supro.BCT.dynamo.UKGVideosDO;
import sagu.supro.BCT.leanback_lib.CardPresenter;
import sagu.supro.BCT.leanback_lib.Video;
import sagu.supro.BCT.levels.nursery.NurseryVideoList;
import sagu.supro.BCT.utils.AWSProvider;
import sagu.supro.BCT.utils.Config;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class UkgVideoList {

    private List<String> VIDEO_CATEGORY = new ArrayList<>();
    private List<Video> actualVideoList = new ArrayList<>();
    public static List<Video> u_searchVideosList = new ArrayList<>();

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper dynamoDBMapper;
    private Context context;

    private List<UKGVideosDO> generalKnowledge = new ArrayList<>();
    private List<UKGVideosDO> rhymes = new ArrayList<>();
    private List<UKGVideosDO> phonics = new ArrayList<>();
    private List<UKGVideosDO> alphabetNumberWriting = new ArrayList<>();

    private List<String> downloadedVideoName = new ArrayList<>();
    private List<String> downloadedVideoId = new ArrayList<>();
    private List<String> downloadedCardImage = new ArrayList<>();
    private List<String> downloadedVideoDesc = new ArrayList<>();

    private ArrayObjectAdapter rowsAdapter;

    public static List<String> offlineVideos = new ArrayList<>();

    int downloadVideos = 0;

    public UkgVideoList(Context context){
        this.context=context;
        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(context));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
    }

    public ArrayObjectAdapter setupUkgVideos() {

        clearAllLists();

        try {
            downloadVideos = getTotalDownloadedProjects();
        } catch (NullPointerException e){
            Log.d("NullPointerException", e.getMessage());
        }

        try {
            if (!isOnline()){
                setDownloadedVideos(downloadVideos);
            } else {

                Boolean bgStatus = new FetchVideoDetails().execute().get();
                if (bgStatus) {

                    VIDEO_CATEGORY.add("GENERAL KNOWLEDGE");
                    VIDEO_CATEGORY.add("RHYMES");
                    VIDEO_CATEGORY.add("PHONICS");
                    VIDEO_CATEGORY.add("ALPHABET & NUMBER WRITING");

                    sortVideos();

                    rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
                    CardPresenter cardPresenter = new CardPresenter();

                    if (downloadVideos != 0) {
                        VIDEO_CATEGORY.add("DOWNLOADED");
                    }

                    int i;
                    for (i = 0; i < VIDEO_CATEGORY.size(); i++) {
                        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                        actualVideoList.clear();
                        int NUM_COLS = i;
                        switch (i) {
                            case 0:
                                NUM_COLS = generalKnowledge.size();
                                updateActualList(generalKnowledge);
                                break;
                            case 1:
                                NUM_COLS = rhymes.size();
                                updateActualList(rhymes);
                                break;
                            case 2:
                                NUM_COLS = phonics.size();
                                updateActualList(phonics);
                                break;
                            case 3:
                                NUM_COLS = alphabetNumberWriting.size();
                                updateActualList(alphabetNumberWriting);
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
                    setDownloadedVideos(downloadVideos);
                }
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

    private void sortVideos(){
        Collections.sort(generalKnowledge, new UkgVideoIdComparator());
        Collections.sort(rhymes, new UkgVideoIdComparator());
        Collections.sort(phonics, new UkgVideoIdComparator());
        Collections.sort(alphabetNumberWriting, new UkgVideoIdComparator());
    }

    private void setDownloadedVideos(int downloadVideos){

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
            u_searchVideosList.addAll(actualVideoList);

            HeaderItem header = new HeaderItem(0, VIDEO_CATEGORY.get(0));
            rowsAdapter.add(new ListRow(header, listRowAdapter));

        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
            });
        }
    }

    public int getTotalDownloadedProjects() {
        File directory=new File(Environment.getExternalStorageDirectory()+"/.BCT/UKG");
        getDownloadedVideoNames(directory);
        return downloadedVideoName.size();
    }

    private void getDownloadedVideoNames(File directory) {
        File[] fileList = directory.listFiles();
        if (fileList != null) {
            for (File currentFile : fileList) {
                if (!currentFile.isDirectory() && getMimeType(currentFile.getName()).equals("video/mp4")) {
                    downloadedVideoName.add(currentFile.getPath());
                } else if (!currentFile.isDirectory() && getMimeType(currentFile.getName()).equals("image/jpeg")) {
                    downloadedCardImage.add(currentFile.getPath());
                } else if (!currentFile.isDirectory() && getMimeType(currentFile.getName()).equals("text/plain")) {
                    downloadedVideoDesc.add(currentFile.getPath());
                } else {
                    if (currentFile.length() != 0) {
                        downloadedVideoId.add(currentFile.getName());
                        getDownloadedVideoNames(currentFile);
                    }
                }
            }
        }
    }

    private String getMimeType(String fileUrl) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileUrl);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private void updateActualList(List<UKGVideosDO> topic) {
        for (int i=0; i<topic.size(); i++) {
            Video video = new Video();
            video.setId(topic.get(i).getVideoId());
            video.setTitle(topic.get(i).getVideoTitle());
            video.setDescription(topic.get(i).getVideoDescription());
            video.setCardImageUrl(topic.get(i).getVideoCardImg());
            video.setVideoUrl(topic.get(i).getVideoUrl());
            video.setVideoTopic(topic.get(i).getVideoTopic());
            actualVideoList.add(video);
            u_searchVideosList.add(video);
        }
    }

    public void addAllDownloadedVideosToRow(int size) {
        String[] title_desc = new String[3];
        for(int j = 0;j<size;j++){
            for (int k=0; k<downloadedVideoId.size(); k++){
                if (downloadedVideoName.get(j).contains(downloadedVideoId.get(k))){
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
        }
    }

    private Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            return (returnVal==0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getOfflineVideos(){
        return offlineVideos;
    }

    @SuppressLint("StaticFieldLeak")
    class FetchVideoDetails extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                ScanRequest request = new ScanRequest().withTableName(Config.UKGTABLENAME);
                ScanResult response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> userRows = response.getItems();
                for (Map<String, AttributeValue> map : userRows) {
                    UKGVideosDO ukgVideosDO = dynamoDBMapper.load(UKGVideosDO.class, map.get("video_id").getS(),
                            map.get("video_title").getS());

                    switch (map.get("video_topic").getS()){
                        case "GENERAL KNOWLEDGE":
                            generalKnowledge.add(ukgVideosDO);
                            break;
                        case "RHYMES":
                            rhymes.add(ukgVideosDO);
                            break;
                        case "PHONICS" :
                            phonics.add(ukgVideosDO);
                            break;
                        case "ALPHABET & NUMBER WRITING":
                            alphabetNumberWriting.add(ukgVideosDO);
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

    private class UkgVideoIdComparator implements Comparator<UKGVideosDO> {
        @Override
        public int compare(UKGVideosDO v1, UKGVideosDO v2) {
            return v1.getVideoId().compareTo(v2.getVideoId());
        }
    }

}
