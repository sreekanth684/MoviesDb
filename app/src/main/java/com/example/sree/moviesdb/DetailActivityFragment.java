package com.example.sree.moviesdb;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sree.moviesdb.adapters.MyReviewItemCursorAdapter;
import com.example.sree.moviesdb.adapters.MyTrailerItemCursorAdapter;
import com.example.sree.moviesdb.asyncTasks.GetMovieDetailsTaskFrCursors;
import com.example.sree.moviesdb.data.MoviesDbContract;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final String TRAILER_SHARE_TAG = " #MoviesDbApp";
    static final String MOVIE_DETAIL_URI = "URI";

    //movie indices
    public static final int COL_MOVIE_ROW_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_MOVIE_ORG_TITLE = 2;
    public static final int COL_MOVIE_POSTER_URL = 3;
    public static final int COL_MOVIE_RELEASE_DATE = 4;
    public static final int COL_MOVIE_RUNTIME = 5;
    public static final int COL_MOVIE_RATING = 6;
    public static final int COL_MOVIE_OVERVIEW = 7;
    public static final int COL_MOVIE_IS_FAV = 8;
    //trailer indices
    public static final int COL_TRAILER_NAME = 1;
    public static final int COL_TRAILER_SOURCE_URL = 2;
    //review indices
    public static final int COL_REVIEW_AUTHOR = 1;
    public static final int COL_REVIEW_CONTENT = 2;

    //loaders
    private static final int MOVIE_DETAIL_LOADER = 4;
    private static final int MOVIE_TRAILER_LOADER = 6;
    private static final int MOVIE_REVIEW_LOADER = 8;
    //selections
    private static final String[] MOVIE_COLUMNS = {
            MoviesDbContract.MovieEntry.TABLE_NAME + "." + MoviesDbContract.MovieEntry._ID,
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_ORG_TITLE,
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_POSTER_URL,
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_RUNTIME,
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_RATING,
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_OVERVIEW,
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_IS_FAV
    };
    private static final String[] TRAILER_COLUMNS = {
            MoviesDbContract.MovieEntry.TABLE_NAME + "." + MoviesDbContract.MovieEntry._ID,
            MoviesDbContract.MovieTrailersEntry.COLUMN_TRAILER_NAME,
            MoviesDbContract.MovieTrailersEntry.COLUMN_TRAILER_SOURCE_URL
    };
    private static final String[] REVIEW_COLUMNS = {
            MoviesDbContract.MovieEntry.TABLE_NAME + "." + MoviesDbContract.MovieEntry._ID,
            MoviesDbContract.MovieReviewsEntry.COLUMN_REVIEW_AUTHOR,
            MoviesDbContract.MovieReviewsEntry.COLUMN_REVIEW_CONTENT
    };
    //views
    private View mDetailFrag;
    private TextView mTitleTextView;
    private TextView mRatingTextView;
    private TextView mRuntimeTextView;
    private TextView mReleaseDateTextView;
    private ImageView mPosterImageView;
    private ImageButton mFavButton;
    private TextView mOverviewHeadingTextView;
    private TextView mOverviewTextView;
    private LinearLayout mTrailersLinearListView;
    private TextView mTrailersNotAvailTextView;
    private LinearLayout mReviewsLinearListView;
    private TextView mReviewsNotAvailTextView;


    private MyReviewItemCursorAdapter mReviewItemCursorAdapter;
    private MyTrailerItemCursorAdapter mTrailerItemCursorAdapter;

    private ShareActionProvider mShareActionProvider;
    private MenuItem mShareMenuItem;
    private String mShareUrl;

    private Uri mMovieUri;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail_frag, menu);

        // Retrieve the share menu item
        mShareMenuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareMenuItem);

        //default invisible visible only if trailers available.
        mShareMenuItem.setVisible(false);
        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mShareUrl != null) {
            mShareActionProvider.setShareIntent(createShareMovieTrailerIntent());
            mShareMenuItem.setVisible(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*sP: before using loaders/cursors
        View detailFrag = inflater.inflate(R.layout.fragment_detail, container, false);
        String movieId = (String) getActivity().getIntent().getCharSequenceExtra(Intent.EXTRA_TEXT);
        Log.d(LOG_TAG + ".onCreateView()", "Movie Id from intent-" + movieId);
        GetMovieDetailsTask movieDetailsTask = new GetMovieDetailsTask(getContext(), detailFrag);
        movieDetailsTask.execute(movieId);
        return detailFrag;*/


        //sP: after using loaders/cursors
        mDetailFrag = inflater.inflate(R.layout.fragment_detail, container, false);
        //sP:using bundle args to get uri info for supporting 2 pane mode.
        //Intent intent = getActivity().getIntent();
        Bundle arguments = getArguments();

        if (arguments != null && null != arguments.getParcelable(DetailActivityFragment.MOVIE_DETAIL_URI)) {
            //Uri movieWithRowIdUri = intent.getData();
            mMovieUri = arguments.getParcelable(DetailActivityFragment.MOVIE_DETAIL_URI);
            Uri movieWithRowIdUri = mMovieUri;
            String movieRowId = MoviesDbContract.MovieEntry.getMovieRowIdFromUri(movieWithRowIdUri);
            Cursor movieCur = getActivity().getContentResolver().query(movieWithRowIdUri,
                    MOVIE_COLUMNS, null, null, null);
            //check movie to see if it has originalTitle, if it is null that means we dont have data fr movie/trailer/reviews
            //so shall make a api call.
            if (movieCur.moveToFirst() && movieCur.getString(COL_MOVIE_ORG_TITLE) == null) {
                invokeGetMovieDetailsTask(String.valueOf(movieCur.getLong(COL_MOVIE_ROW_ID)), movieCur.getString(COL_MOVIE_ID));
            }

            //movie details ui elements
            mTitleTextView = (TextView) mDetailFrag.findViewById(R.id.titleTextView);
            mRuntimeTextView = (TextView) mDetailFrag.findViewById(R.id.runtimeTextView);
            mRatingTextView = (TextView) mDetailFrag.findViewById(R.id.ratingTextView);
            mReleaseDateTextView = (TextView) mDetailFrag.findViewById(R.id.releaseDateTextView);
            mPosterImageView = (ImageView) mDetailFrag.findViewById(R.id.posterImageView);
            mOverviewHeadingTextView = (TextView) mDetailFrag.findViewById(R.id.overviewHeadingTextView);
            mOverviewTextView = (TextView) mDetailFrag.findViewById(R.id.overviewTextView);
            mFavButton = (ImageButton) mDetailFrag.findViewById(R.id.favImageButton);

            //trailers ui w/ cursor adapter. uses linearLayout instead of listView since we have few trailers/reviews.
            Uri trailersForMovieUri = MoviesDbContract.MovieTrailersEntry.buildTrailersForMovieUri(movieRowId);
            Cursor trailersCur = getActivity().getContentResolver().query(trailersForMovieUri,
                    TRAILER_COLUMNS, null, null, null);
            mTrailerItemCursorAdapter = new MyTrailerItemCursorAdapter(getActivity(), trailersCur, 0);
            mTrailersLinearListView = (LinearLayout) mDetailFrag.findViewById(R.id.trailersListView);
            mTrailersNotAvailTextView = (TextView) mDetailFrag.findViewById(R.id.trailersNotAvailTextView);
            updateTrailersLinearLayout();

            //reviews ui w/ cursor adapter. uses linearLayout instead of listView since we have few trailers/reviews.
            Uri reviewsForMovieUri = MoviesDbContract.MovieReviewsEntry.buildREviewsForMovieUri(movieRowId);
            Cursor reviewsCur = getActivity().getContentResolver().query(reviewsForMovieUri,
                    REVIEW_COLUMNS, null, null, null);
            mReviewItemCursorAdapter = new MyReviewItemCursorAdapter(getActivity(), reviewsCur, 0);
            mReviewsLinearListView = (LinearLayout) mDetailFrag.findViewById(R.id.reviewsListView);
            mReviewsNotAvailTextView = (TextView) mDetailFrag.findViewById(R.id.reviewsNotAvailTextView);
            updateReviewsLinearLayout();

        }


        return mDetailFrag;

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void invokeGetMovieDetailsTask(String movieRowId, String movieId) {
        GetMovieDetailsTaskFrCursors asyncTask = new GetMovieDetailsTaskFrCursors(getActivity());
        Log.v(LOG_TAG + ".invokeAsyncTask()", "movieRowId & movieId::" + movieRowId + "&" + movieId);
        asyncTask.execute(movieRowId, movieId);
    }

    private void updateReviewsLinearLayout() {
        mReviewsLinearListView.removeAllViews();
        mReviewsNotAvailTextView.setVisibility(View.GONE);
        if (mReviewItemCursorAdapter.getCount() > 0) {
            for (int i = 0; i < mReviewItemCursorAdapter.getCount(); i++) {
                View reviewItem = mReviewItemCursorAdapter.getView(i, null, null);
                mReviewsLinearListView.addView(reviewItem);
            }
        } else {
            mReviewsNotAvailTextView.setVisibility(View.VISIBLE);
        }
    }

    private void updateTrailersLinearLayout() {
        mTrailersLinearListView.removeAllViews();
        mTrailersNotAvailTextView.setVisibility(View.GONE);
        if (mTrailerItemCursorAdapter.getCount() > 0) {
            for (int i = 0; i < mTrailerItemCursorAdapter.getCount(); i++) {
                View trailerItem = mTrailerItemCursorAdapter.getView(i, null, null);
                mTrailersLinearListView.addView(trailerItem);
            }
        } else {
            mTrailersNotAvailTextView.setVisibility(View.VISIBLE);
        }
    }

    private Intent createShareMovieTrailerIntent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v=" + mShareUrl + TRAILER_SHARE_TAG);
        shareIntent.setType("text/plain");
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
        getLoaderManager().initLoader(MOVIE_TRAILER_LOADER, null, this);
        getLoaderManager().initLoader(MOVIE_REVIEW_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /*sP: to support 2 pane mode. using bundle args.
        Intent intent = getActivity().getIntent();
        if (intent == null)
            return null;
        Uri movieWithRowIdUri = intent.getData();*/

        Uri movieWithRowIdUri = mMovieUri;
        if(movieWithRowIdUri == null)
            return null;
        String movieRowId = MoviesDbContract.MovieEntry.getMovieRowIdFromUri(movieWithRowIdUri);
        Log.d(LOG_TAG + ".onCreateLoader()", " intent data uri-" + movieWithRowIdUri);
        switch (id) {
            case 4:
                return new CursorLoader(getActivity(), movieWithRowIdUri, MOVIE_COLUMNS, null, null, null);
            case 6:
                Uri trailersFrMovieRowIdUri = MoviesDbContract.MovieTrailersEntry.buildTrailersForMovieUri(movieRowId);
                return new CursorLoader(getActivity(), trailersFrMovieRowIdUri, TRAILER_COLUMNS, null, null, null);
            case 8:
                Uri reviewsFrMovieRowIdUri = MoviesDbContract.MovieReviewsEntry.buildREviewsForMovieUri(movieRowId);
                return new CursorLoader(getActivity(), reviewsFrMovieRowIdUri, REVIEW_COLUMNS, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG + ".onLoadFinished()", "loader.id" + loader.getId() + "has data? -" + data.moveToFirst());
        switch (loader.getId()) {
            case 4:
                if (data.moveToFirst()) {
                    String orgTitle = data.getString(COL_MOVIE_ORG_TITLE);
                    String relDate = data.getString(COL_MOVIE_RELEASE_DATE);
                    String rating = data.getString(COL_MOVIE_RATING);
                    String runtime = data.getString(COL_MOVIE_RUNTIME);
                    String imageUrl = data.getString(COL_MOVIE_POSTER_URL);
                    String plot = data.getString(COL_MOVIE_OVERVIEW);
                    int isFav = data.getInt(COL_MOVIE_IS_FAV);

                    if (orgTitle != null)
                        mTitleTextView.setText(orgTitle);
                    if (relDate != null)
                        mReleaseDateTextView.setText(relDate);
                    if (rating != null)
                        mRatingTextView.setText(rating + getActivity().getString(R.string.movie_rating_append));
                    if (runtime != null)
                        mRuntimeTextView.setText(runtime + getActivity().getString(R.string.movie_runtime_append));
                    if (plot == null || plot.isEmpty()) {
                        mOverviewTextView.setText(getString(R.string.detail_not_available));
                    } else {
                        mOverviewTextView.setText(plot);
                    }
                    if (imageUrl == null || imageUrl.equalsIgnoreCase("null") || imageUrl.isEmpty()) {
                        mPosterImageView.setImageResource(R.mipmap.no_image);
                    } else {
                        Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w92" + imageUrl).into(mPosterImageView);
                    }
                    if (isFav == 1)
                        mFavButton.setImageResource(R.mipmap.fav_yes);
                    else
                        mFavButton.setImageResource(R.mipmap.fav_no);
                    mFavButton.setTag(data);
                    mFavButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Cursor movieCursor = (Cursor) v.getTag();
                            String movieRowId = String.valueOf(movieCursor.getLong(COL_MOVIE_ROW_ID));
                            int isFav = movieCursor.getInt(COL_MOVIE_IS_FAV);
                            Log.d(LOG_TAG + ".onClick()", "fav button clicked fr movieId-" + movieRowId);
                            ContentValues value = new ContentValues();
                            if (isFav == 1) {
                                value.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_IS_FAV, 0);
                                mFavButton.setImageResource(R.mipmap.fav_no);
                            } else {
                                value.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_IS_FAV, 1);
                                mFavButton.setImageResource(R.mipmap.fav_yes);
                            }
                            getActivity().getContentResolver().update(MoviesDbContract.MovieEntry.CONTENT_URI, value, MoviesDbContract.MovieEntry._ID + " = ? ", new String[]{movieRowId});

                        }
                    });

                }
                return;
            case 6:
                mTrailerItemCursorAdapter.swapCursor(data);
                //updating share intent provider
                if (data != null && data.moveToFirst()) {
                    Log.v(LOG_TAG + ".onLoadFinished()", "creating shareIntentProvider w/ trailerUrl -" + data.getString(COL_TRAILER_NAME));
                    mShareUrl = data.getString(COL_TRAILER_SOURCE_URL);
                    // If onCreateOptionsMenu has already happened, we need to update the share intent now.
                    if (mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(createShareMovieTrailerIntent());
                    }
                    if (mShareMenuItem != null)
                        mShareMenuItem.setVisible(true);
                }
                updateTrailersLinearLayout();
                return;
            case 8:
                mReviewItemCursorAdapter.swapCursor(data);
                updateReviewsLinearLayout();
                return;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG + ".onLoaderReset()", "loader.getId() -" + loader.getId());
        switch (loader.getId()) {
            case 4:
                return;
            case 6:
                mTrailerItemCursorAdapter.swapCursor(null);
                if (mShareMenuItem != null)
                    mShareMenuItem.setVisible(false);
                mShareUrl = null;
                if (mShareActionProvider != null)
                    mShareActionProvider.setShareIntent(null);
                return;
            case 8:
                mReviewItemCursorAdapter.swapCursor(null);
                return;

        }
    }
}
