package sagu.supro.BCT.levels.ukg;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import sagu.supro.BCT.R;
import sagu.supro.BCT.leanback_lib.DetailsActivity;
import sagu.supro.BCT.leanback_lib.Video;
import sagu.supro.BCT.tv.SearchActivity;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class UkgFrag extends BrowseFragment {
    private static final String TAG = "UkgFrag";

    private List<String> offlineVideos = new ArrayList<>();
    private UkgVideoList videoList;
    private ArrayObjectAdapter rowsAdapter;

    public static UkgFrag ukgFrag;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ukgFrag = this;

        prepareUI();
    }

    private void prepareUI(){
        setupUIElements();
        loadRows();
        setupEventListeners();
    }

    public void loadRows() {

        offlineVideos.clear();

        final AlertDialog progressDialog = new SpotsDialog(getContext(), "This may take a while...");
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                videoList = new UkgVideoList(getContext());
                rowsAdapter =  videoList.setupUkgVideos();
                offlineVideos.addAll(videoList.getOfflineVideos());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAdapter(rowsAdapter);
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                });

            }
        }).start();

    }

    public void refreshDownloads(){
        videoList.addAllDownloadedVideosToRow(videoList.getTotalDownloadedProjects());
        rowsAdapter.notify();
    }

    private void setupUIElements() {
        setTitle("UKG");
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(ContextCompat.getColor(getContext(), R.color.dark_blue));
        setSearchAffordanceColor(ContextCompat.getColor(getContext(), R.color.search_opaque));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("level", "UKG");
                intent.putStringArrayListExtra("OfflineVideos", (ArrayList<String>) offlineVideos);
                startActivity(intent);
            }
        });

        setOnItemViewClickedListener(new UkgFrag.ItemViewClickedListener());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                Video mvideoDO = (Video) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.VIDEO, mvideoDO);
                intent.putExtra("LEVEL", "UKG");
                intent.putStringArrayListExtra("OfflineVideos", (ArrayList<String>) offlineVideos);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME)
                        .toBundle();
                getActivity().startActivity(intent, bundle);
            } else if (item instanceof String) {
                if (((String) item).contains(getString(R.string.error_fragment))) {
                    /*Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
                    startActivity(intent);*/
                    Toast.makeText(getContext(), "Error Occured", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

