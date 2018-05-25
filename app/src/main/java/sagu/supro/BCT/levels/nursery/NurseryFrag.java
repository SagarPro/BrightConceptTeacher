package sagu.supro.BCT.levels.nursery;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
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

import static sagu.supro.BCT.tv.MainScreen.progressDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class NurseryFrag extends BrowseFragment {
    private static final String TAG = "NurseryFrag";

    private List<String> offlineVideos = new ArrayList<>();

    public static NurseryFrag nurseryFrag;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        nurseryFrag = this;

        prepareUI();
    }

    private void prepareUI(){
        setupUIElements();
        loadRows();
        setupEventListeners();
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }


    public void loadRows() {

        offlineVideos.clear();
        NurseryVideoList nurseryVideoList = new NurseryVideoList(getContext());
        ArrayObjectAdapter rowsAdapter =  nurseryVideoList.setupNurseryVideos();
        setAdapter(rowsAdapter);
        offlineVideos.addAll(nurseryVideoList.getOfflineVideos());

    }

    private void setupUIElements() {
        setTitle("Nursery");
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
                intent.putExtra("level", "Nursery");
                intent.putStringArrayListExtra("OfflineVideos", (ArrayList<String>) offlineVideos);
                startActivity(intent);
            }
        });

        setOnItemViewClickedListener(new NurseryFrag.ItemViewClickedListener());
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
                intent.putExtra("LEVEL", "Nursery");
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