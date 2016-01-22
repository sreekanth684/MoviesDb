package com.example.sree.moviesdb.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import android.text.format.Time;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Sree on 1/3/16.
 */
public class MoviesDbContract {

    private static final String LOG_TAG = MoviesDbContract.class.getSimpleName();

    public static final String CONTENT_AUTHORITY = "com.example.sree.moviesdb";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_MOVIE_FAVORITE = "FAVORITES";
    public static final String PATH_MOVIE_TRAILERS = "movieTrailers";
    public static final String PATH_MOVIE_REVIEWS = "movieReviews";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        //Log.d(LOG_TAG+".normalizeDate()", "input Date::" + startDate);
        Calendar c  = Calendar.getInstance();
        c.setTimeInMillis(startDate);
        c.set(Calendar.HOUR_OF_DAY, 0);
        //c.clear(Calendar.HOUR_OF_DAY);
        c.clear(Calendar.AM_PM);
        c.clear(Calendar.MINUTE);
        c.clear(Calendar.SECOND);
        c.clear(Calendar.MILLISECOND);
        //Log.d(LOG_TAG + ".normalizeDate()", "output normalized Date::" + c.getTimeInMillis());
        //Log.d(LOG_TAG + ".normalizeDate()", "output normalized Date::" + c.getTime());
        return c.getTimeInMillis();

    }

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        //this column stamps date when the data is fetched.
        public static final String COLUMN_MOVIE_INSERT_DATE = "insert_date";
        public static final String COLUMN_MOVIE_TITLE = "title";
        public static final String COLUMN_MOVIE_ORG_TITLE = "org_title";
        public static final String COLUMN_MOVIE_POSTER_URL = "poster_url";
        public static final String COLUMN_MOVIE_OVERVIEW = "overview";
        public static final String COLUMN_MOVIE_RATING = "rating";
        public static final String COLUMN_MOVIE_RELEASE_DATE = "release_date";
        public static final String COLUMN_MOVIE_RUNTIME = "runtime";
        public static final String COLUMN_MOVIE_IS_FAV = "is_fav";
        public static final String COLUMN_MOVIE_SORT_BY = "sort_by";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;


        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieSortByUri(String sortPref) {
            return CONTENT_URI.buildUpon().appendPath(sortPref).build();
        }

        public static Uri buildMovieFavoritesUri() {
            return CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_FAVORITE).build();
        }

        public static Uri buildMovieSortByWithMovieIdUri(String sortPref, String movieId) {
            return CONTENT_URI.buildUpon().appendPath(sortPref).appendQueryParameter(COLUMN_MOVIE_ID, movieId).build();
        }

        public static Uri buildMovieSortByWithDateUri(String sortPref, long date) {
            return CONTENT_URI.buildUpon().appendPath(sortPref)
                    .appendPath(Long.toString(normalizeDate(date))).build();
        }

        public static Uri buildMovieSortByWithDateAndMovieIdUri(String sortPref, long date, String movieId) {
            return CONTENT_URI.buildUpon().appendPath(sortPref)
                    .appendPath(Long.toString(normalizeDate(date))).appendQueryParameter(COLUMN_MOVIE_ID, movieId).build();
        }

        public static String getSortByFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getMovieRowIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getDateFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static String getMovieIdFromUri(Uri uri) {
            String movieId = uri.getQueryParameter(COLUMN_MOVIE_ID);
            if (null != movieId && movieId.length() > 0)
                return movieId;
            else
                return null;
        }

    }

    public static final class MovieTrailersEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie_trailers";
        public static final String COLUMN_TRAILER_MOVIE_KEY = "movie_row_id";
        public static final String COLUMN_TRAILER_NAME = "name";
        public static final String COLUMN_TRAILER_SOURCE_URL = "source_url";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_TRAILERS).build();
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_TRAILERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_TRAILERS;

        public static Uri buildTrailersUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrailersForMovieUri(String movieRowId) {
            return CONTENT_URI.buildUpon().appendPath(movieRowId).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class MovieReviewsEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie_reviews";
        public static final String COLUMN_REVIEW_MOVIE_KEY = "movie_row_id";
        public static final String COLUMN_REVIEW_AUTHOR = "author";
        public static final String COLUMN_REVIEW_CONTENT = "content";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_REVIEWS).build();
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_REVIEWS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_REVIEWS;

        public static Uri buildReviewsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildREviewsForMovieUri(String movieRowId) {
            return CONTENT_URI.buildUpon().appendPath(movieRowId).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
