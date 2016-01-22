package com.example.sree.moviesdb.asyncTasks;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.sree.moviesdb.BuildConfig;
import com.example.sree.moviesdb.data.MoviesDbContract;
import com.example.sree.moviesdb.infos.GridMovieItemInfo;

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
 * Created by Sree on 1/5/16.
 */
public class GetMoviesTask extends AsyncTask<String, Void, GridMovieItemInfo[]> {
    private final String LOG_TAG = GetMoviesTask.class.getSimpleName();
    //private MyImgTxtViewArrayAdapter<GridMovieItemInfo> mMoviesAdapter;
    private final Context mContext;

    /*sP:used when not using loaders
    GetMoviesTask(Context context, MyImgTxtViewArrayAdapter<GridMovieItemInfo> moviesAdapter){
        mContext = context;
        mMoviesAdapter = moviesAdapter;
    }*/
    public GetMoviesTask(Context context){
        mContext = context;
    }

    @Override
    protected GridMovieItemInfo[] doInBackground(String... params) {
        return getMoivesInfo(params[0]);
    }

   /*sP:used when not using loaders
   @Override
   protected void onPostExecute(GridMovieItemInfo[] gridMovieItems) {
        //super.onPostExecute(gridMovieItems);
        mMoviesAdapter.clear();
        for (GridMovieItemInfo gridMovieItemInfo : gridMovieItems) {
            Log.d(LOG_TAG + ".onPostExecute()", "GridMovieItemInfo:: " + gridMovieItemInfo.toString());
            mMoviesAdapter.add(gridMovieItemInfo);
        }
    }*/

    public GridMovieItemInfo[] getMoivesInfo(String sortPref) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String moviesJsonStr = null;
        String sortBy = sortPref;
        GridMovieItemInfo[] movieItems = null;
        try {
            final String MOVIES_DB_BASE_URL = "http://api.themoviedb.org/3/discover/movie";
            final String API_KEY = "api_key";
            final String SORT_BY = "sort_by";
            Uri buildUri = Uri.parse(MOVIES_DB_BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY, BuildConfig.MOVIES_DB_API_KEY)
                    .appendQueryParameter(SORT_BY, sortBy)
                    .build();
            URL url = new URL(buildUri.toString());
            Log.d(LOG_TAG + ".getMoviesInfo()", "Built URI:: " + buildUri.toString());

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

            JSONObject moviesJson = null;
            JSONArray movieResultsArray = null;
            try {
                moviesJson = new JSONObject(moviesJsonStr);
                movieResultsArray = moviesJson.getJSONArray("results");
                if (movieResultsArray != null && movieResultsArray.length() > 0) {
                    movieItems = new GridMovieItemInfo[movieResultsArray.length()];
                    Vector<ContentValues> cVVector = new Vector<ContentValues>(movieResultsArray.length());
                    for (int i = 0; i < movieResultsArray.length(); i++) {
                        JSONObject movieDetails = movieResultsArray.getJSONObject(i);
                        String movieTitle = movieDetails.getString("title");
                        String movieImgPath = movieDetails.getString("poster_path");
                        String movieId = movieDetails.getString("id");
                        GridMovieItemInfo movieItem = new GridMovieItemInfo(movieTitle, movieImgPath, movieId);
                        Log.v(LOG_TAG + ".getMoviesInfo()", "Movie Details:: " + i + "-" + movieItem.toString());
                        movieItems[i] = movieItem;

                        ContentValues movieValues = new ContentValues();
                        movieValues.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
                        movieValues.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_INSERT_DATE, System.currentTimeMillis());
                        movieValues.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_SORT_BY, sortBy);
                        movieValues.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_TITLE, movieTitle);
                        movieValues.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_POSTER_URL, movieImgPath);
                        cVVector.add(movieValues);

                    }
                    // add to database
                    if (cVVector.size() > 0) {
                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        Log.d(LOG_TAG + ".getMoviesInfo()", "total weather data size to be bulk inserted-" + cVVector.size());
                        int insertCount = mContext.getContentResolver().bulkInsert(MoviesDbContract.MovieEntry.CONTENT_URI, cvArray);
                        Log.d(LOG_TAG + ".getMoviesInfo()", "total rows bulk inserted-" + insertCount);

                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            Log.e(LOG_TAG + ".getMoviesInfo()", "Error", e);
        }

        return movieItems;
    }
}