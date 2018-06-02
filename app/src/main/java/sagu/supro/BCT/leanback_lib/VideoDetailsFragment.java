/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package sagu.supro.BCT.leanback_lib;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.DetailsFragment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v17.leanback.app.DetailsFragmentBackgroundController;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import sagu.supro.BCT.R;
import sagu.supro.BCT.tv.MainScreen;
import sagu.supro.BCT.utils.Config;

import static sagu.supro.BCT.levels.lkg.LkgFrag.lkgFrag;
import static sagu.supro.BCT.levels.nursery.NurseryFrag.nurseryFrag;
import static sagu.supro.BCT.levels.playgroup.PlaygroupFrag.playgroupFrag;
import static sagu.supro.BCT.levels.ukg.UkgFrag.ukgFrag;

/*
 * LeanbackDetailsFragment extends DetailsFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related videos.
 */
public class VideoDetailsFragment extends DetailsFragment {

    private static final String TAG = "VideoDetailsFragment";

    private static final int REQUEST_STORAGE = 7;

    private static final int ACTION_STREAMONLINE = 1;
    private static final int ACTION_DOWNLOAD = 2;
    private static final int ACTION_PLAY = 3;
    private static final int ACTION_REMOVE = 4;

    private static final int DETAIL_THUMB_WIDTH = 276;
    private static final int DETAIL_THUMB_HEIGHT = 276;

    private Video mSelectedVideo;

    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;

    private DetailsFragmentBackgroundController mDetailsBackground;

    ArrayObjectAdapter actionAdapter = new ArrayObjectAdapter();

    private List<String> offlineVideos = new ArrayList<>();
    private String level;

    ProgressBar progressBar;
    TextView showProg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);

        mDetailsBackground = new DetailsFragmentBackgroundController(this);

        level = getActivity().getIntent().getStringExtra("LEVEL");

        mSelectedVideo =
                (Video) getActivity().getIntent().getSerializableExtra(DetailsActivity.VIDEO);
        if (mSelectedVideo != null) {
            mPresenterSelector = new ClassPresenterSelector();
            mAdapter = new ArrayObjectAdapter(mPresenterSelector);
            setupDetailsOverviewRow();
            setupDetailsOverviewRowPresenter();
            //setupRelatedMovieListRow();
            setAdapter(mAdapter);
            initializeBackground(mSelectedVideo);
            setOnItemViewClickedListener(new ItemViewClickedListener());
        } else {
            Intent intent = new Intent(getActivity(), MainScreen.class);
            startActivity(intent);
        }
    }

    private void initializeBackground(Video data) {
        mDetailsBackground.enableParallax();

        Glide.with(getActivity())
                .load(R.drawable.bct3)
                .asBitmap()
                .asIs()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap,
                                                GlideAnimation<? super Bitmap> glideAnimation) {
                        mDetailsBackground.setCoverBitmap(bitmap);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });
    }

    private void setupDetailsOverviewRow() {
        Log.d(TAG, "doInBackground: " + mSelectedVideo.toString());
        final DetailsOverviewRow row = new DetailsOverviewRow(mSelectedVideo);
        row.setImageDrawable(ContextCompat.getDrawable(getContext(), android.R.color.transparent));
        int width = convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_WIDTH);
        int height = convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_HEIGHT);
        Glide.with(getActivity())
                .load(mSelectedVideo.getCardImageUrl())
                .centerCrop()
                //.error(R.drawable.default_background)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        Log.d(TAG, "details overview card image url ready: " + resource);
                        row.setImageDrawable(resource);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });


        offlineVideos = getActivity().getIntent().getStringArrayListExtra("OfflineVideos");

        if (offlineVideos.contains(mSelectedVideo.getId())){
            actionAdapter.add(new Action(ACTION_PLAY, "Play"));
            actionAdapter.add(new Action(ACTION_REMOVE, "Remove"));
        } else {
            actionAdapter.add(new Action(ACTION_STREAMONLINE, "Stream Online"));
            actionAdapter.add(new Action(ACTION_DOWNLOAD, "Download"));
        }

        row.setActionsAdapter(actionAdapter);

        mAdapter.add(row);
    }

    private void setupDetailsOverviewRowPresenter() {
        // Set detail background.
        FullWidthDetailsOverviewRowPresenter detailsPresenter =
                new FullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
        detailsPresenter.setBackgroundColor(
                ContextCompat.getColor(getContext(), R.color.default_background));

        // Hook up transition element.
        FullWidthDetailsOverviewSharedElementHelper sharedElementHelper =
                new FullWidthDetailsOverviewSharedElementHelper();
        sharedElementHelper.setSharedElementEnterTransition(
                getActivity(), DetailsActivity.SHARED_ELEMENT_NAME);
        detailsPresenter.setListener(sharedElementHelper);
        detailsPresenter.setParticipatingEntranceTransition(true);

        detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                switch ((int) action.getId()){
                    case ACTION_STREAMONLINE:
                        streamOnline();
                        break;
                    case ACTION_DOWNLOAD:
                        storagePermissionCheck();
                        break;
                    case ACTION_PLAY:
                        playDownloaded();
                        break;
                    case ACTION_REMOVE:
                        removeDownloaded();
                        break;
                }
            }
        });
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
    }

    private void streamOnline(){
        Intent intent = new Intent(getActivity(), PlaybackActivity.class);
        intent.putExtra(DetailsActivity.VIDEO, mSelectedVideo);
        intent.putExtra("Type","Online");
        startActivity(intent);
    }

    private void downloadFiles(){

        int totalOfflineVideos = getTotalDownloadedProjects(new File(Environment.getExternalStorageDirectory()+"/.BCT/Nursery"))
                + getTotalDownloadedProjects(new File(Environment.getExternalStorageDirectory()+"/.BCT/LKG"))
                + getTotalDownloadedProjects(new File(Environment.getExternalStorageDirectory()+"/.BCT/UKG"))
                + getTotalDownloadedProjects(new File(Environment.getExternalStorageDirectory()+"/.BCT/Playgroup"));

        if (totalOfflineVideos < 10) {

            final AlertDialog alertDialog;
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.download_status, null);
            progressBar = dialogView.findViewById(R.id.pb_download);
            showProg = dialogView.findViewById(R.id.tv_show_progress);
            builder.setTitle("Downloading, Please Wait");
            builder.setView(dialogView);

            String videoName = mSelectedVideo.getId()+".mp4";
            final File path = Environment.getExternalStorageDirectory();
            File file = new File(path+"/.BCT/"+level+"/"+mSelectedVideo.getId()+"/");
            file.mkdirs();
            final String filePath = path+"/.BCT/"+level+"/"+mSelectedVideo.getId()+"/"+videoName;

            AWSCredentials awsCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
            AmazonS3Client s3 = new AmazonS3Client(awsCredentials);
            s3.setRegion(Region.getRegion(Regions.US_EAST_1));

            final TransferUtility transferUtility = TransferUtility.builder().context(getContext())
                    .s3Client(s3)
                    .build();

            final TransferObserver downloadVideoObserver = transferUtility.download(
                    "bkmhbct/"+level, videoName,
                    new File(filePath));

            builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    File dir = new File(Environment.getExternalStorageDirectory()+"/.BCT/"+level+"/"+mSelectedVideo.getId());
                    //File halfDownloadedFile = new File(downloadVideoObserver.getAbsoluteFilePath());
                    deleteRecursive(dir);
                    refreshAdapter();
                    dialog.dismiss();
                    downloadVideoObserver.cleanTransferListener();
                }
            });
            alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();

            downloadVideoObserver.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        Log.d("Video", "Success");

                        String imageName = mSelectedVideo.getId() + ".jpg";
                        String imagePath = path + "/.BCT/" +level+"/" + mSelectedVideo.getId() + "/"+imageName;

                        TransferObserver downloadImageObserver = transferUtility.download(
                                "bkmhbct/"+level, imageName,
                                new File(imagePath));

                        downloadImageObserver.setTransferListener(new TransferListener() {
                            @Override
                            public void onStateChanged(int id, TransferState state) {
                                if (TransferState.COMPLETED == state) {

                                    String desc = mSelectedVideo.getTitle() + "\n" + mSelectedVideo.getDescription() + "\n" + level;
                                    generateNoteOnSD(mSelectedVideo.getId() + ".txt", desc);

                                    actionAdapter.clear();
                                    actionAdapter.add(new Action(ACTION_PLAY, "Play"));
                                    actionAdapter.add(new Action(ACTION_REMOVE, "Remove"));

                                    alertDialog.dismiss();

                                }
                            }

                            @Override
                            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                            }

                            @Override
                            public void onError(int id, Exception ex) {
                                alertDialog.dismiss();
                                showErrorToUser("Exception : " + ex.getMessage());
                                Log.d("Progress", ex.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    try{
                        progressBar.setMax((int) bytesTotal);
                        progressBar.setProgress((int) bytesCurrent);
                        if(bytesCurrent!=0){
                            showProg.setText(""+((int) bytesCurrent/(1024*1024))+"/"+(int) (bytesTotal/(1024*1024))+"mb");
                        }
                        else{
                            showProg.setText("0/"+(int) (bytesTotal/(1024*1024))+"MB");
                        }
                                    /*if(bytesCurrent==bytesTotal){
                                    }*/
                    }catch (Exception e){
                        alertDialog.dismiss();
                        showErrorToUser("Exception : " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                @Override
                public void onError(int id, Exception ex) {
                    alertDialog.dismiss();
                    showErrorToUser("Exception : "+ex.getMessage());
                    Log.d("Progress", ex.getMessage());
                }
            });

        } else {
            showErrorToUser("Download Limit Exceeded, Only 10 Offline Videos Allowed.\nRemove a downloaded video and try again.");
        }

    }

    private void playDownloaded(){
        Intent playIntent = new Intent(getActivity(), PlaybackActivity.class);
        playIntent.putExtra(DetailsActivity.VIDEO, mSelectedVideo);
        playIntent.putExtra("Type","Downloaded");
        playIntent.putExtra("VideoName",mSelectedVideo.getTitle());
        String videoPath = Environment.getExternalStorageDirectory()+"/.BCT/"+level+"/"+mSelectedVideo.getId()+"/"+mSelectedVideo.getId()+".mp4";
        playIntent.putExtra("VideoPath",videoPath);
        startActivity(playIntent);
    }

    private void removeDownloaded(){
        AlertDialog progressDialog = new SpotsDialog(getActivity(),"Removing Please Wait...");
        progressDialog.show();

        File dir = new File(Environment.getExternalStorageDirectory()+"/.BCT/"+level+"/"+mSelectedVideo.getId());
        deleteRecursive(dir);

        refreshAdapter();

        actionAdapter.clear();
        actionAdapter.add(new Action(ACTION_STREAMONLINE, "Stream Online"));
        actionAdapter.add(new Action(ACTION_DOWNLOAD, "Download"));

        getActivity().finish();
    }

    private void storagePermissionCheck(){
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck == 0) {
            downloadFiles();
        } else {
            permissionDenied();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadFiles();
                }
            }
        }
    }

    private void permissionDenied(){
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Allow Storage Permission to download videos.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
            }
        });
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private int getTotalDownloadedProjects(File directory) {
        if (!directory.exists())
            return 0;
        return directory.list().length;
    }

    private void refreshAdapter(){
        switch (level){
            case "Playgroup":
                playgroupFrag.loadRows();
                break;
            case "Nursery":
                nurseryFrag.loadRows();
                break;
            case "LKG":
                lkgFrag.loadRows();
                break;
            case "UKG":
                ukgFrag.loadRows();
                break;
        }
        //mainFrag.loadRows();
    }

    private void generateNoteOnSD(String sFileName, String sBody) {
        try {
            File path = Environment.getExternalStorageDirectory();
            File file = new File(path+"/.BCT/"+level+"/"+mSelectedVideo.getId()+"/");
            if (!file.exists()) {
                file.mkdirs();
            }
            File gpxfile = new File(file, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            refreshAdapter();
        } catch (IOException e) {
            showErrorToUser("Exception : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    private int convertDpToPixel(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void showErrorToUser(String errMessage){
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(errMessage);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {

            if (item instanceof Video) {
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra("Video", mSelectedVideo);

                Bundle bundle =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                                getActivity(),
                                ((ImageCardView) itemViewHolder.view).getMainImageView(),
                                DetailsActivity.SHARED_ELEMENT_NAME)
                                .toBundle();
                getActivity().startActivity(intent, bundle);
            }
        }
    }


}
