package sagu.supro.BCT.tv;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v17.leanback.app.SearchFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.widget.Toast;
import static sagu.supro.BCT.levels.lkg.LkgVideoList.l_searchVideosList;
import static sagu.supro.BCT.levels.nursery.NurseryVideoList.n_searchVideosList;
import static sagu.supro.BCT.levels.playgroup.PlaygroupVideoList.p_searchVideosList;
import static sagu.supro.BCT.levels.ukg.UkgVideoList.u_searchVideosList;

import java.util.ArrayList;
import java.util.List;

import sagu.supro.BCT.R;
import sagu.supro.BCT.leanback_lib.CardPresenter;
import sagu.supro.BCT.leanback_lib.DetailsActivity;
import sagu.supro.BCT.leanback_lib.Video;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFrag extends SearchFragment implements android.support.v17.leanback.app.SearchFragment.SearchResultProvider{

    private final String TAG = SearchFrag.class.getSimpleName();

    private ArrayObjectAdapter mRowsAdapter;
    private ArrayList<Video> mItems;

    private List<String> offlineVideos = new ArrayList<>();


    public SearchFrag() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setSearchResultProvider(this);
        setOnItemViewClickedListener(new ItemViewClickedListener());

        offlineVideos = getActivity().getIntent().getStringArrayListExtra("OfflineVideos");
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        Log.d(TAG, "getResultsAdapter");
        Log.d(TAG, mRowsAdapter.toString());

        switch (getActivity().getIntent().getStringExtra("level")){
            case "Playgroup":
                mItems = (ArrayList<Video>) p_searchVideosList;
                break;
            case "Nursery":
                mItems = (ArrayList<Video>) n_searchVideosList;
                break;
            case "LKG":
                mItems = (ArrayList<Video>) l_searchVideosList;
                break;
            case "UKG":
                mItems = (ArrayList<Video>) u_searchVideosList;
                break;
        }

        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        listRowAdapter.addAll(0, mItems);
        HeaderItem header = new HeaderItem("Search results");
        mRowsAdapter.add(new ListRow(header, listRowAdapter));

        return mRowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery){
        //Log.i(TAG, String.format("Search Query Text Change %s", newQuery));
        mRowsAdapter.clear();
        ArrayList<Video> searchedVideos = new ArrayList<>();
        for(int i=0;i<mItems.size();i++){
            Video currentVideo = mItems.get(i);
            if(currentVideo.getTitle().toLowerCase().contains(newQuery.toLowerCase())){
                searchedVideos.add(mItems.get(i));
            }
        }

        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        listRowAdapter.addAll(0, searchedVideos);
        HeaderItem header = new HeaderItem("Search results");
        mRowsAdapter.add(new ListRow(header, listRowAdapter));
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, String.format("Search Query Text Submit %s", query));
        return true;
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                Video mvideoDO = (Video) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.VIDEO, mvideoDO);
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
