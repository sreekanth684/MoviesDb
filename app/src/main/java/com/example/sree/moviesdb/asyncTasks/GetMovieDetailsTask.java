package com.example.sree.moviesdb.asyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sree.moviesdb.BuildConfig;
import com.example.sree.moviesdb.adapters.MyImgTxtViewArrayAdapter;
import com.example.sree.moviesdb.R;
import com.example.sree.moviesdb.ReviewDetailsActivity;
import com.example.sree.moviesdb.infos.*;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Sree on 1/5/16.
 */
public class GetMovieDetailsTask extends AsyncTask<String, Void, DetailMovieItemInfo> {

    private final String LOG_TAG = GetMoviesTask.class.getSimpleName();
    private View mDetailFrag;
    private Context mContext;
    private DetailMovieItemInfo movieItemInfo;

    public GetMovieDetailsTask(Context context, View detailFragment) {
        mDetailFrag = detailFragment;
        mContext = context;
    }

    @Override
    protected DetailMovieItemInfo doInBackground(String... params) {
        getMovieDetails(params[0]);
        getMovieTrailers(params[0]);
        getMovieReviews(params[0]);
        return movieItemInfo;
    }

    @Override
    protected void onPostExecute(DetailMovieItemInfo movieInfo) {
        TextView titleTextView = (TextView) mDetailFrag.findViewById(R.id.titleTextView);
        if (null == movieInfo) {
            titleTextView.setText("Sorry, no info found!");
        } else {
            titleTextView.setText(movieInfo.getOriginalTitle());
            TextView ratingTextView = (TextView) mDetailFrag.findViewById(R.id.ratingTextView);
            TextView releaseDateTextView = (TextView) mDetailFrag.findViewById(R.id.releaseDateTextView);
            ImageView posterImageView = (ImageView) mDetailFrag.findViewById(R.id.posterImageView);
            if (null == movieInfo.getMovieOverview() || movieInfo.getMovieOverview() == "") {
                TextView overviewHeadingTextView = (TextView) mDetailFrag.findViewById(R.id.overviewHeadingTextView);
                overviewHeadingTextView.setText("Plot not available!");
            } else {
                TextView overviewTextView = (TextView) mDetailFrag.findViewById(R.id.overviewTextView);
                overviewTextView.setText(movieInfo.getMovieOverview());
            }

            ratingTextView.setText(movieInfo.getMovieRating());
            releaseDateTextView.setText(movieInfo.getMovieReleaseDate());
            if (null == movieInfo.getMoviePosterUrl())
                posterImageView.setImageResource(R.mipmap.no_image);
            else
                Picasso.with(mContext).load(movieInfo.getMoviePosterUrl()).into(posterImageView);
            //trailers
            if (null != movieItemInfo.getMovieTrailers()) {
                LinearLayout trailersLinearListView = (LinearLayout) mDetailFrag.findViewById(R.id.trailersListView);
                MyImgTxtViewArrayAdapter<DetailMovieItemInfo.TrailerInfo> trailersAdapter = new MyImgTxtViewArrayAdapter<DetailMovieItemInfo.TrailerInfo>(((Activity)mContext), R.layout.list_item_trailer, movieItemInfo.getMovieTrailers(), 0, R.id.trailerName);
                for (int i = 0; i < trailersAdapter.getCount(); i++) {
                    View reviewItem = trailersAdapter.getView(i, null, trailersLinearListView);
                    DetailMovieItemInfo.TrailerInfo trailerInfo = trailersAdapter.getItem(i);
                    reviewItem.setTag(trailerInfo);
                    reviewItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DetailMovieItemInfo.TrailerInfo trailerInfo1 = (DetailMovieItemInfo.TrailerInfo) v.getTag();
                            Log.d(LOG_TAG, "onPostExecute()::Invoking trailer detail intent with source:" + trailerInfo1.getTrailerSource());
                            //Toast.makeText(getActivity(), gridMovieItemInfo.getMovieId(), Toast.LENGTH_LONG).show();
                            Intent trailerDetailIntent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://www.youtube.com/watch?v=" + trailerInfo1.getTrailerSource()));
                            mContext.startActivity(trailerDetailIntent);
                        }
                    });
                    trailersLinearListView.addView(reviewItem);
                }
                    /*
                    //If using listView use below code. using linearlayout as number items are less and want
                    //it to be scrollable. and listview shall not be used in scrollable.
                    trailersListView.setAdapter(trailersAdapter);
                    setDynamicHeight(trailersListView);
                    trailersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (parent.getItemAtPosition(position) instanceof DetailMovieItemInfo.TrailerInfo) {
                                DetailMovieItemInfo.TrailerInfo trailerInfo = (DetailMovieItemInfo.TrailerInfo) parent.getItemAtPosition(position);
                                Log.d(LOG_TAG, "onPostExecute()::Invoking trailer detail intent with trailerId:" + trailerInfo.getTrailerSource());
                                Intent intent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://www.youtube.com/watch?v=" + trailerInfo.getTrailerSource()));
                                startActivity(intent);
                            }
                        }
                    });*/
            } else {
                TextView trailersTextView = (TextView) mDetailFrag.findViewById(R.id.trailersHeadingTextView);
                trailersTextView.setText("Trailers none available!");
            }
            //reviews
            if (null != movieItemInfo.getMovieReviews()) {
                LinearLayout reviewsLinearListView = (LinearLayout) mDetailFrag.findViewById(R.id.reviewsListView);
                final MyImgTxtViewArrayAdapter<DetailMovieItemInfo.ReviewInfo> reviewsAdapter = new MyImgTxtViewArrayAdapter<DetailMovieItemInfo.ReviewInfo>(((Activity)mContext), R.layout.list_item_review, movieItemInfo.getMovieReviews(), 0, R.id.reviewAuthor);
                for (int i = 0; i < reviewsAdapter.getCount(); i++) {
                    View reviewItem = reviewsAdapter.getView(i, null, reviewsLinearListView);
                    DetailMovieItemInfo.ReviewInfo reviewInfo = reviewsAdapter.getItem(i);
                    reviewItem.setTag(reviewInfo);
                    reviewItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DetailMovieItemInfo.ReviewInfo reviewInfo1 = (DetailMovieItemInfo.ReviewInfo) v.getTag();
                            Log.d(LOG_TAG, "onPostExecute()::Invoking review detail intent with content:" + reviewInfo1.getReviewContent());
                            //Toast.makeText(getActivity(), gridMovieItemInfo.getMovieId(), Toast.LENGTH_LONG).show();
                            Intent reviewDetailIntent = new Intent(((Activity)mContext), ReviewDetailsActivity.class).putExtra(Intent.EXTRA_TEXT, reviewInfo1.getReviewContent());
                            mContext.startActivity(reviewDetailIntent);
                        }
                    });
                    reviewsLinearListView.addView(reviewItem);
                }
                    /*If using listView use below code. using linearlayout as number items are less and want
                    it to be scrollable. and listview shall not be used in scrollable.
                    reviewsListView.setAdapter(reviewsAdapter);
                    setDynamicHeight(reviewsListView);
                    reviewsLinearListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (parent.getItemAtPosition(position) instanceof DetailMovieItemInfo.ReviewInfo) {
                                DetailMovieItemInfo.ReviewInfo reviewInfo = (DetailMovieItemInfo.ReviewInfo) parent.getItemAtPosition(position);
                                Log.d(LOG_TAG, "onPostExecute()::Invoking review detail intent with content:" + reviewInfo.getReviewContent());
                                //Toast.makeText(getActivity(), gridMovieItemInfo.getMovieId(), Toast.LENGTH_LONG).show();
                                Intent reviewDetailIntent = new Intent(getActivity(), ReviewDetailsActivity.class).putExtra(Intent.EXTRA_TEXT, reviewInfo.getReviewContent());
                                startActivity(reviewDetailIntent);
                            }
                        }
                    });*/


            } else {
                TextView reviewsTextView = (TextView) mDetailFrag.findViewById(R.id.reviewsHeadingTextView);
                reviewsTextView.setText("Reviews none available!");

            }
        }
    }


    public DetailMovieItemInfo getMovieDetails(String movieId) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String moviesJsonStr = null;
        if (null == movieId || movieId.isEmpty()) {
            Log.d(LOG_TAG + ".getMovieDetails()", "Movie Id is null!");
            return null;
        }
        try {
            final String MOVIES_DB_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String API_KEY = "api_key";
            Uri buildUri = Uri.parse(MOVIES_DB_BASE_URL).buildUpon()
                    .appendPath(movieId)
                    .appendQueryParameter(API_KEY, BuildConfig.MOVIES_DB_API_KEY)
                    .build();
            URL url = new URL(buildUri.toString());
            Log.d(LOG_TAG + ".getMovieDetails()", "Built URI:: " + buildUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            moviesJsonStr = buffer.toString();
            Log.v(LOG_TAG + ".getMoviesInfo()", "JSON Response:: " + moviesJsonStr);

            JSONObject movieDetailsJson = null;
            try {
                movieDetailsJson = new JSONObject(moviesJsonStr);
                if (null == movieItemInfo)
                    movieItemInfo = new DetailMovieItemInfo();
                movieItemInfo.setOriginalTitle(movieDetailsJson.getString("original_title"));
                movieItemInfo.setMoviePosterUrl(movieDetailsJson.getString("poster_path"));
                movieItemInfo.setMovieOverview(movieDetailsJson.getString("overview"));
                movieItemInfo.setMovieRating(movieDetailsJson.getString("vote_average"));
                movieItemInfo.setMovieReleaseDate(movieDetailsJson.getString("release_date"));
                Log.v(LOG_TAG + ".getMovieDetails()", "Movie Details:: " + movieItemInfo.toString());


            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            Log.e(LOG_TAG + ".getMoviesInfo()", "Error", e);
        }

        return movieItemInfo;
    }

    public DetailMovieItemInfo getMovieTrailers(String movieId) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String moviesJsonStr = null;
        if (null == movieId || movieId.isEmpty()) {
            Log.d(LOG_TAG + ".getMovieTrailers()", "Movie Id is null!");
            return null;
        }
        try {
            final String MOVIES_DB_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String API_KEY = "api_key";
            final String TRAILERS = "trailers";
            Uri buildUri = Uri.parse(MOVIES_DB_BASE_URL).buildUpon()
                    .appendPath(movieId)
                    .appendPath(TRAILERS)
                    .appendQueryParameter(API_KEY, BuildConfig.MOVIES_DB_API_KEY)
                    .build();
            URL url = new URL(buildUri.toString());
            Log.d(LOG_TAG + ".getMovieTrailers()", "Built URI:: " + buildUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            moviesJsonStr = buffer.toString();
            Log.v(LOG_TAG + ".getMovieTrailers()", "JSON Response:: " + moviesJsonStr);

            JSONObject movieTrailersJson = null;
            JSONArray movieTrailersArray = null;
            try {
                movieTrailersJson = new JSONObject(moviesJsonStr);
                movieTrailersArray = movieTrailersJson.getJSONArray("youtube");
                if (null == movieItemInfo)
                    movieItemInfo = new DetailMovieItemInfo();
                if (movieTrailersArray != null && movieTrailersArray.length() > 0) {
                    ArrayList<DetailMovieItemInfo.TrailerInfo> trailersList = new ArrayList<DetailMovieItemInfo.TrailerInfo>();
                    for (int i = 0; i < movieTrailersArray.length(); i++) {
                        JSONObject jsonTrailerInfo = movieTrailersArray.getJSONObject(i);
                        DetailMovieItemInfo.TrailerInfo trailerInfo = movieItemInfo.new TrailerInfo();
                        trailerInfo.setTrailerName(jsonTrailerInfo.getString("name"));
                        trailerInfo.setTrailerSource(jsonTrailerInfo.getString("source"));
                        Log.v(LOG_TAG + ".getMovieTrailers()", "Movie Trailer Info:: " + trailerInfo.toString());
                        trailersList.add(trailerInfo);
                    }
                    movieItemInfo.setMovieTrailers(trailersList);
                }
                Log.v(LOG_TAG + ".getMovieTrailers()", "Movie Trailers:: " + movieItemInfo.toString());


            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            Log.e(LOG_TAG + ".getMovieTrailers()", "Error", e);
        }

        return movieItemInfo;

    }

    public DetailMovieItemInfo getMovieReviews(String movieId) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String moviesJsonStr = null;
        if (null == movieId || movieId.isEmpty()) {
            Log.d(LOG_TAG + ".getMovieReviews()", "Movie Id is null!");
            return null;
        }
        try {
            final String MOVIES_DB_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String API_KEY = "api_key";
            final String REVIEWS = "reviews";
            Uri buildUri = Uri.parse(MOVIES_DB_BASE_URL).buildUpon()
                    .appendPath(movieId)
                    .appendPath(REVIEWS)
                    .appendQueryParameter(API_KEY, BuildConfig.MOVIES_DB_API_KEY)
                    .build();
            URL url = new URL(buildUri.toString());
            Log.d(LOG_TAG + ".getMovieReviews()", "Built URI:: " + buildUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            moviesJsonStr = buffer.toString();
            Log.v(LOG_TAG + ".getMovieReviews()", "JSON Response:: " + moviesJsonStr);

            JSONObject movieReviewsJson = null;
            JSONArray movieReviewsArray = null;
            try {
                movieReviewsJson = new JSONObject(moviesJsonStr);
                movieReviewsArray = movieReviewsJson.getJSONArray("results");
                if (null == movieItemInfo)
                    movieItemInfo = new DetailMovieItemInfo();
                if (movieReviewsArray != null && movieReviewsArray.length() > 0) {
                    ArrayList<DetailMovieItemInfo.ReviewInfo> reviewsList = new ArrayList<DetailMovieItemInfo.ReviewInfo>();
                    for (int i = 0; i < movieReviewsArray.length(); i++) {
                        JSONObject jsonReviewInfo = movieReviewsArray.getJSONObject(i);
                        DetailMovieItemInfo.ReviewInfo reviewInfo = movieItemInfo.new ReviewInfo();
                        reviewInfo.setReviewAuthor(jsonReviewInfo.getString("author"));
                        reviewInfo.setReviewContent(jsonReviewInfo.getString("content"));
                        Log.v(LOG_TAG + ".getMovieReviews()", "Movie Review Info:: " + reviewInfo.toString());
                        reviewsList.add(reviewInfo);
                    }
                    movieItemInfo.setMovieReviews(reviewsList);
                }
                Log.v(LOG_TAG + ".getMovieReviews()", "Movie Reviews:: " + movieItemInfo.toString());


            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            Log.e(LOG_TAG + ".getMovieReviews()", "Error", e);
        }

        return movieItemInfo;

    }

    public static void setDynamicHeight(ListView mListView) {
        ListAdapter mListAdapter = mListView.getAdapter();
        if (mListAdapter == null) {
            // when adapter is null
            return;
        }
        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < mListAdapter.getCount(); i++) {
            View listItem = mListAdapter.getView(i, null, mListView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
        mListView.setLayoutParams(params);
        mListView.requestLayout();
    }
}


