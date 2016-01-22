package com.example.sree.moviesdb.asyncTasks;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.sree.moviesdb.BuildConfig;
import com.example.sree.moviesdb.data.MoviesDbContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by Sree on 1/9/16.
 */
public class GetMovieDetailsTaskFrCursors extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = GetMovieDetailsTaskFrCursors.class.getSimpleName();

    private Context mContext;

    public GetMovieDetailsTaskFrCursors(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (params.length == 0 || params[0] == null || params[1] == null) {
            Log.e(LOG_TAG + ".doInBackground()", "movieId/movieRowId can't be null!");
            return null;
        }
        getMovieDetails(params[0], params[1]);
        getMovieTrailers(params[0], params[1]);
        getMovieReviews(params[0], params[1]);
        return null;
    }

    public void getMovieDetails(String movieRowId, String movieId) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String moviesJsonStr = null;
        if (null == movieId || movieId.isEmpty()) {
            Log.d(LOG_TAG + ".getMovieDetails()", "Movie Id is null!");
            return;
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
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            moviesJsonStr = buffer.toString();
            Log.v(LOG_TAG + ".getMoviesInfo()", "JSON Response:: " + moviesJsonStr);

            JSONObject movieDetailsJson = null;
            try {
                movieDetailsJson = new JSONObject(moviesJsonStr);
                ContentValues movieDetailesValues = new ContentValues();
                movieDetailesValues.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_ORG_TITLE, movieDetailsJson.getString("original_title"));
                movieDetailesValues.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_POSTER_URL, movieDetailsJson.getString("poster_path"));
                movieDetailesValues.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, movieDetailsJson.getString("overview"));
                movieDetailesValues.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_RATING, movieDetailsJson.getString("vote_average"));
                movieDetailesValues.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_RUNTIME, movieDetailsJson.getString("runtime"));
                movieDetailesValues.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE, movieDetailsJson.getString("release_date"));
                mContext.getContentResolver().update(MoviesDbContract.MovieEntry.CONTENT_URI, movieDetailesValues, MoviesDbContract.MovieEntry._ID + " = ?",
                        new String[]{movieRowId});
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            Log.e(LOG_TAG + ".getMoviesInfo()", "Error", e);
        }

        return;
    }

    public void getMovieTrailers(String movieRowId, String movieId) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String moviesJsonStr = null;
        if (null == movieId || movieId.isEmpty()) {
            Log.d(LOG_TAG + ".getMovieTrailers()", "Movie Id is null!");
            return;
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
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            moviesJsonStr = buffer.toString();
            Log.v(LOG_TAG + ".getMovieTrailers()", "JSON Response:: " + moviesJsonStr);

            JSONObject movieTrailersJson = null;
            JSONArray movieTrailersArray = null;
            try {
                movieTrailersJson = new JSONObject(moviesJsonStr);
                movieTrailersArray = movieTrailersJson.getJSONArray("youtube");

                if (movieTrailersArray != null && movieTrailersArray.length() > 0) {
                    Vector<ContentValues> cVVector = new Vector<ContentValues>(movieTrailersArray.length());

                    for (int i = 0; i < movieTrailersArray.length(); i++) {
                        JSONObject jsonTrailerInfo = movieTrailersArray.getJSONObject(i);
                        ContentValues movieTrailerValues = new ContentValues();
                        movieTrailerValues.put(MoviesDbContract.MovieTrailersEntry.COLUMN_TRAILER_MOVIE_KEY, movieRowId);
                        movieTrailerValues.put(MoviesDbContract.MovieTrailersEntry.COLUMN_TRAILER_NAME, jsonTrailerInfo.getString("name"));
                        movieTrailerValues.put(MoviesDbContract.MovieTrailersEntry.COLUMN_TRAILER_SOURCE_URL, jsonTrailerInfo.getString("source"));
                        cVVector.add(movieTrailerValues);
                    }
                    // add to database
                    if (cVVector.size() > 0) {
                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        Log.d(LOG_TAG + ".getMovieTrailers()", "total trailer data size to be bulk inserted-" + cVVector.size());
                        int insertCount = mContext.getContentResolver().bulkInsert(MoviesDbContract.MovieTrailersEntry.CONTENT_URI, cvArray);
                        Log.d(LOG_TAG + ".getMovieTrailers()", "total rows bulk inserted-" + insertCount);

                    }
                }
                // Log.v(LOG_TAG + ".getMovieTrailers()", "Movie Trailers:: " + movieItemInfo.toString());


            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            Log.e(LOG_TAG + ".getMovieTrailers()", "Error", e);
        }

        return;

    }

    public void getMovieReviews(String movieRowId, String movieId) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String moviesJsonStr = null;
        if (null == movieId || movieId.isEmpty()) {
            Log.d(LOG_TAG + ".getMovieReviews()", "Movie Id is null!");
            return;
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
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            moviesJsonStr = buffer.toString();
            Log.v(LOG_TAG + ".getMovieReviews()", "JSON Response:: " + moviesJsonStr);

            JSONObject movieReviewsJson = null;
            JSONArray movieReviewsArray = null;
            try {
                movieReviewsJson = new JSONObject(moviesJsonStr);
                movieReviewsArray = movieReviewsJson.getJSONArray("results");
                if (movieReviewsArray != null && movieReviewsArray.length() > 0) {
                    Vector<ContentValues> cVVector = new Vector<ContentValues>(movieReviewsArray.length());
                    for (int i = 0; i < movieReviewsArray.length(); i++) {
                        JSONObject jsonReviewInfo = movieReviewsArray.getJSONObject(i);

                        ContentValues movieReviewsValues = new ContentValues();
                        movieReviewsValues.put(MoviesDbContract.MovieReviewsEntry.COLUMN_REVIEW_MOVIE_KEY, movieRowId);
                        movieReviewsValues.put(MoviesDbContract.MovieReviewsEntry.COLUMN_REVIEW_AUTHOR, jsonReviewInfo.getString("author"));
                        movieReviewsValues.put(MoviesDbContract.MovieReviewsEntry.COLUMN_REVIEW_CONTENT, jsonReviewInfo.getString("content"));
                        cVVector.add(movieReviewsValues);
                    }
                    // add to database
                    if (cVVector.size() > 0) {
                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        Log.d(LOG_TAG + ".getMovieReviews()", "total reviews data size to be bulk inserted-" + cVVector.size());
                        int insertCount = mContext.getContentResolver().bulkInsert(MoviesDbContract.MovieReviewsEntry.CONTENT_URI, cvArray);
                        Log.d(LOG_TAG + ".getMovieReviews()", "total rows bulk inserted-" + insertCount);

                    }
                }
                //Log.v(LOG_TAG + ".getMovieReviews()", "Movie Reviews:: " + movieItemInfo.toString());


            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            Log.e(LOG_TAG + ".getMovieReviews()", "Error", e);
        }

        return;

    }
}
