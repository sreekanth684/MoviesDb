package com.example.sree.moviesdb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sree.moviesdb.adapters.MyGridMovieItemCursorAdapter;
import com.example.sree.moviesdb.asyncTasks.GetMoviesTask;
import com.example.sree.moviesdb.data.MoviesDbContract;
import com.example.sree.moviesdb.infos.GridMovieItemInfo;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    //indices
    public static final int COL_MOVIE_ROW_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_MOVIE_TITLE = 2;
    public static final int COL_MOVIE_POSTER_URL = 3;
    private static final int GRID_MOVIES_LOADER = 666;
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final String[] MOVIE_COLUMNS = {
            MoviesDbContract.MovieEntry.TABLE_NAME + "." + MoviesDbContract.MovieEntry._ID,
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_TITLE,
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_POSTER_URL
    };
    private static final String SELECTED_KEY = "selected_position";
    //private MyImgTxtViewArrayAdapter<GridMovieItemInfo> mMoviesAdapter;
    private MyGridMovieItemCursorAdapter mMoviesAdapter;
    private GridView mGridView;
    private int mScrollPosition = GridView.INVALID_POSITION;

    public MainActivityFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mScrollPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mScrollPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortPref = settings.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_defaultValue));
        Uri queryUri;
        if (sortPref.equalsIgnoreCase(getActivity().getString(R.string.pref_fav_value))) {
            queryUri = MoviesDbContract.MovieEntry.buildMovieFavoritesUri();
        } else {
            queryUri = MoviesDbContract.MovieEntry.buildMovieSortByWithDateUri(sortPref, System.currentTimeMillis());
        }
        Log.d(LOG_TAG, "onCreateView():: queryUri-" + queryUri);
        String sortOrder = MoviesDbContract.MovieEntry._ID + " ASC";
        Cursor cur = getActivity().getContentResolver().query(queryUri, MOVIE_COLUMNS, null, null, sortOrder);
        mMoviesAdapter = new MyGridMovieItemCursorAdapter(getActivity(), cur, 0);
        //mMoviesAdapter = new MyImgTxtViewArrayAdapter<GridMovieItemInfo>(getActivity(), R.layout.grid_item_movie, new ArrayList<GridMovieItemInfo>(), R.id.movie_image, R.id.movie_text);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.moviesGridView);
        mGridView.setAdapter(mMoviesAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object itemType = parent.getItemAtPosition(position);
                if (itemType instanceof GridMovieItemInfo) {
                    GridMovieItemInfo gridMovieItemInfo = (GridMovieItemInfo) itemType;
                    Log.d(LOG_TAG, "onCreateView()::Invoking movie detail intent with movieId (frm info obj):" + gridMovieItemInfo.getMovieId());
                    //Toast.makeText(getActivity(), gridMovieItemInfo.getMovieId(), Toast.LENGTH_LONG).show();
                    Intent movieDetailIntent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, gridMovieItemInfo.getMovieId());
                    startActivity(movieDetailIntent);
                } else if (itemType instanceof Cursor) {
                    Cursor gridMovieItemCursor = (Cursor) itemType;
                    if (gridMovieItemCursor != null) {
                        Log.d(LOG_TAG, "onCreateView()::Invoking movie detail intent with movieId (frm cursor):" + gridMovieItemCursor.getString(COL_MOVIE_ID));
                        Uri movieDetailUri = MoviesDbContract.MovieEntry.buildMovieUri(gridMovieItemCursor.getLong(COL_MOVIE_ROW_ID));
                        //sP: commented to implement callback for tablets
                        // Intent movieDetailIntent = new Intent(getActivity(), DetailActivity.class).setData(movieDetailUri);
                        //startActivity(movieDetailIntent);
                        ((Callback) getActivity()).onItemSelected(movieDetailUri);

                        mScrollPosition = position;
                    }


                }
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mScrollPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Log.v(LOG_TAG + ".onStart()", "invokeGetMoviesTask....api call...");
        invokeGetMoviesTask();
    }

    /**
     * this method checks if the data exists in dB before making the api call. it makes a call once per day and
     * if it finds the same movieId replaces that movie copying over favorite column. if its a new movieId
     * it creates one!
     */
    private void invokeGetMoviesTask() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortPref = settings.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_defaultValue));
        if (!sortPref.equalsIgnoreCase(getActivity().getString(R.string.pref_fav_value))) {
            String sortOrder = MoviesDbContract.MovieEntry._ID + " ASC";
            Uri movieSortByWithDateUri = MoviesDbContract.MovieEntry.buildMovieSortByWithDateUri(sortPref, System.currentTimeMillis());
            Cursor cur = getActivity().getContentResolver().query(movieSortByWithDateUri,
                    MOVIE_COLUMNS, null, null, sortOrder);
            //for now invoking the api call wen there is no data!
            if (cur.moveToFirst())
                return;
            //if netwrk nt available suggest user to enable netwrk.
            if (!isNetworkAvailable()) {
                Toast t = Toast.makeText(getActivity(), R.string.no_network_msg, Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                return;
            }

            //GetMoviesTask moviesTask = new GetMoviesTask(getActivity(), mMoviesAdapter);
            GetMoviesTask moviesTask = new GetMoviesTask(getActivity());
            Log.v(LOG_TAG + ".invokeGetMoviesTask()", "making api call w/ sortPref from SharedPref::" + sortPref);
            moviesTask.execute(sortPref);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // since we read the sortPref when we create the loader, all we need to do is restart things
    void onLocationChanged() {
        Log.v(LOG_TAG + ".onLocationChanged()", "invokeGetMoviesTask....api call...");
        //invokeGetMoviesTask();
        getLoaderManager().restartLoader(GRID_MOVIES_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(GRID_MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortPref = settings.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_defaultValue));
        Log.v(LOG_TAG + ".onCreateLoader()", "sortPref from SharedPref::" + sortPref);
        Uri queryUri;
        if (sortPref.equalsIgnoreCase(getActivity().getString(R.string.pref_fav_value))) {
            queryUri = MoviesDbContract.MovieEntry.buildMovieFavoritesUri();
        } else {
            queryUri = MoviesDbContract.MovieEntry.buildMovieSortByWithDateUri(sortPref, System.currentTimeMillis());
        }
        Log.d(LOG_TAG + ".onCreateLoader()", "queryUri-" + queryUri);
        // Sort order:  Ascending, by insert row id will give in the order we get from the api.
        String sortOrder = MoviesDbContract.MovieEntry._ID + " ASC";
        return new CursorLoader(getActivity(), queryUri, MOVIE_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMoviesAdapter.swapCursor(data);
        if (mScrollPosition != GridView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mGridView.smoothScrollToPosition(mScrollPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesAdapter.swapCursor(null);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }
}


