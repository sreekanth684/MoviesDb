package com.example.sree.moviesdb.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Sree on 1/4/16.
 */
public class MoviesDbProvider extends ContentProvider {

    static final int MOVIE = 100;
    static final int MOVIE_WITH_ROW_ID = 101;
    static final int MOVIE_WITH_SORT = 102;
    static final int MOVIE_FAVORITES = 103;
    static final int MOVIE_WITH_SORT_AND_DATE = 104;
    static final int TRAILER = 200;
    static final int TRAILERS_WITH_MOVIE_ID = 201;
    static final int REVIEW = 300;
    static final int REVIEWS_WITH_MOVIE_ID = 301;
    private static final String LOG_TAG = MoviesDbProvider.class.getSimpleName();
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    //sort_by = ? AND insert_date = ?
    private static final String sMovieWithSortByAndDaySelection =
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_SORT_BY + " = ? AND " +
                    MoviesDbContract.MovieEntry.COLUMN_MOVIE_INSERT_DATE + " = ? ";
    //sort_by = ? AND insert_date = ? AND movie_id = ?
    private static final String sMovieWithSortByAndDayWithQuerySelection =
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_SORT_BY + " = ? AND " +
                    MoviesDbContract.MovieEntry.COLUMN_MOVIE_INSERT_DATE + " = ? AND " +
                    MoviesDbContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";
    //sort_by = ? AND movie_id = ?
    private static final String sMovieWithSortByWithMovieIdQuerySelection =
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_SORT_BY + " = ? AND " +
                    MoviesDbContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";
    //sort_by = ?
    private static final String sMovieWithSortBySelection =
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_SORT_BY + " = ? ";

    //is_favorite = ?
    private static final String sMovieFavoritesSelection =
            MoviesDbContract.MovieEntry.COLUMN_MOVIE_IS_FAV + " = ? ";

    //_id = ?
    private static final String sMovieWithMovieId =
            MoviesDbContract.MovieEntry.TABLE_NAME +
                    "." + MoviesDbContract.MovieEntry._ID + " = ? ";

    private static final SQLiteQueryBuilder sTrailersByMovieIdQueryBuilder;
    private static final SQLiteQueryBuilder sReviewsByMovieIdQueryBuilder;

    static {
        sTrailersByMovieIdQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movie INNER JOIN movie_trailers ON movie_trailers.movie_row_id = movie._id
        sTrailersByMovieIdQueryBuilder.setTables(
                MoviesDbContract.MovieEntry.TABLE_NAME + " INNER JOIN " +
                        MoviesDbContract.MovieTrailersEntry.TABLE_NAME +
                        " ON " + MoviesDbContract.MovieTrailersEntry.TABLE_NAME +
                        "." + MoviesDbContract.MovieTrailersEntry.COLUMN_TRAILER_MOVIE_KEY +
                        " = " + MoviesDbContract.MovieEntry.TABLE_NAME +
                        "." + MoviesDbContract.MovieEntry._ID);
    }

    static {
        sReviewsByMovieIdQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movie INNER JOIN movie_reviews ON movie_reviews.movie_row_id = movie._id
        sReviewsByMovieIdQueryBuilder.setTables(
                MoviesDbContract.MovieEntry.TABLE_NAME + " INNER JOIN " +
                        MoviesDbContract.MovieReviewsEntry.TABLE_NAME +
                        " ON " + MoviesDbContract.MovieReviewsEntry.TABLE_NAME +
                        "." + MoviesDbContract.MovieReviewsEntry.COLUMN_REVIEW_MOVIE_KEY +
                        " = " + MoviesDbContract.MovieEntry.TABLE_NAME +
                        "." + MoviesDbContract.MovieEntry._ID);
    }

    private MoviesDbHelper mOpenHelper;

    private Cursor getMovies(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(MoviesDbContract.MovieEntry.TABLE_NAME,
                projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getMoviesBySortPrefAndDate(Uri uri, String[] projection, String sortOrder) {
        String sortPref = MoviesDbContract.MovieEntry.getSortByFromUri(uri);
        long date = MoviesDbContract.MovieEntry.getDateFromUri(uri);
        String movieId = MoviesDbContract.MovieEntry.getMovieIdFromUri(uri);
        String[] selectionArgs;
        String selection;
        if (movieId == null) {
            //Log.d(LOG_TAG + ".withQuery()", "uri-" + uri.toString());
            selection = sMovieWithSortByAndDaySelection;
            selectionArgs = new String[]{sortPref, Long.toString(date)};
        } else {
            //Log.d(LOG_TAG + ".withQuery()", "uri-" + uri.toString());
            //Log.d(LOG_TAG + ".withQuery()", "movieId-" + movieId);
            selection = sMovieWithSortByAndDayWithQuerySelection;
            selectionArgs = new String[]{sortPref, Long.toString(date), movieId};
        }
        return getMovies(uri,
                projection,
                selection,
                selectionArgs,
                sortOrder);
    }

    private Cursor getMoviesBySortPrefAndMovieId(Uri uri, String[] projection, String sortOrder) {
        String sortPref = MoviesDbContract.MovieEntry.getSortByFromUri(uri);
        String movieId = MoviesDbContract.MovieEntry.getMovieIdFromUri(uri);
        String[] selectionArgs;
        String selection;
        //Log.d(LOG_TAG + ".withQuery()", "uri-" + uri.toString());
        //Log.d(LOG_TAG + ".withQuery()", "movieId-" + movieId);
        if(movieId==null){
            selection = sMovieWithSortBySelection;
            selectionArgs = new String[]{sortPref};
        }else {
            selection = sMovieWithSortByWithMovieIdQuerySelection;
            selectionArgs = new String[]{sortPref, movieId};
        }
        return getMovies(uri,
                projection,
                selection,
                selectionArgs,
                sortOrder);
    }

    private Cursor getMovieFavorites(Uri uri, String[] projection, String sortOrder) {
        return getMovies(uri,
                projection,
                sMovieFavoritesSelection,
                new String[]{"1"},
                sortOrder);
    }

    private Cursor getMovieByRowId(Uri uri, String[] projection, String sortOrder) {
        String rowId = MoviesDbContract.MovieEntry.getMovieRowIdFromUri(uri);
        return getMovies(uri,
                projection,
                sMovieWithMovieId,
                new String[]{rowId},
                sortOrder);
    }

    private Cursor getTrailers(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(MoviesDbContract.MovieTrailersEntry.TABLE_NAME,
                projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getMovieTrailers(
            Uri uri, String[] projection, String sortOrder) {
        String movieId = MoviesDbContract.MovieTrailersEntry.getMovieIdFromUri(uri);

        return sTrailersByMovieIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieWithMovieId,
                new String[]{movieId},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getReviews(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(MoviesDbContract.MovieReviewsEntry.TABLE_NAME,
                projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getMovieReviews(
            Uri uri, String[] projection, String sortOrder) {
        String movieId = MoviesDbContract.MovieTrailersEntry.getMovieIdFromUri(uri);

        return sReviewsByMovieIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieWithMovieId,
                new String[]{movieId},
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE_WITH_SORT_AND_DATE:
                return MoviesDbContract.MovieEntry.CONTENT_DIR_TYPE;
            case MOVIE_FAVORITES:
                return MoviesDbContract.MovieEntry.CONTENT_DIR_TYPE;
            case MOVIE_WITH_ROW_ID:
                return MoviesDbContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_WITH_SORT:
                return MoviesDbContract.MovieEntry.CONTENT_DIR_TYPE;
            case MOVIE:
                return MoviesDbContract.MovieEntry.CONTENT_DIR_TYPE;
            case TRAILERS_WITH_MOVIE_ID:
                return MoviesDbContract.MovieTrailersEntry.CONTENT_DIR_TYPE;
            case TRAILER:
                return MoviesDbContract.MovieTrailersEntry.CONTENT_DIR_TYPE;
            case REVIEWS_WITH_MOVIE_ID:
                return MoviesDbContract.MovieReviewsEntry.CONTENT_DIR_TYPE;
            case REVIEW:
                return MoviesDbContract.MovieReviewsEntry.CONTENT_DIR_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // WeatherContract to help define the types to the UriMatcher.
        matcher.addURI(MoviesDbContract.CONTENT_AUTHORITY, MoviesDbContract.PATH_MOVIE, MOVIE);
        matcher.addURI(MoviesDbContract.CONTENT_AUTHORITY, MoviesDbContract.PATH_MOVIE + "/#", MOVIE_WITH_ROW_ID);
        matcher.addURI(MoviesDbContract.CONTENT_AUTHORITY, MoviesDbContract.PATH_MOVIE + "/" + MoviesDbContract.PATH_MOVIE_FAVORITE, MOVIE_FAVORITES);
        matcher.addURI(MoviesDbContract.CONTENT_AUTHORITY, MoviesDbContract.PATH_MOVIE + "/*", MOVIE_WITH_SORT);
        matcher.addURI(MoviesDbContract.CONTENT_AUTHORITY, MoviesDbContract.PATH_MOVIE + "/*/#", MOVIE_WITH_SORT_AND_DATE);



        matcher.addURI(MoviesDbContract.CONTENT_AUTHORITY, MoviesDbContract.PATH_MOVIE_TRAILERS, TRAILER);
        matcher.addURI(MoviesDbContract.CONTENT_AUTHORITY, MoviesDbContract.PATH_MOVIE_TRAILERS + "/*", TRAILERS_WITH_MOVIE_ID);

        matcher.addURI(MoviesDbContract.CONTENT_AUTHORITY, MoviesDbContract.PATH_MOVIE_REVIEWS, REVIEW);
        matcher.addURI(MoviesDbContract.CONTENT_AUTHORITY, MoviesDbContract.PATH_MOVIE_REVIEWS + "/*", REVIEWS_WITH_MOVIE_ID);


        // 3) Return the new matcher!
        return matcher;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        //Log.d(LOG_TAG + ".query()", "matcher uri-" + uri);
        //Log.d(LOG_TAG + ".query()", "matcher Id-" + sUriMatcher.match(uri));
        switch (sUriMatcher.match(uri)) {
            // "movie/#"
            case MOVIE_WITH_ROW_ID: {
                //Log.d(LOG_TAG + ".query()", "MOVIE_WITH_ROW_ID uri-" + uri.toString());
                retCursor = getMovieByRowId(uri, projection, sortOrder);
                break;
            }

            // "movie/FAVORITE"
            case MOVIE_FAVORITES: {
                Log.d(LOG_TAG + ".query()", "MOVIE_FAVORITES uri-" + uri.toString());
                retCursor = getMovieFavorites(uri, projection, sortOrder);
                break;
            }

            // "movie/*/#"
            case MOVIE_WITH_SORT_AND_DATE: {
                //Log.d(LOG_TAG + ".query()", "MOVIE_WITH_SORT_AND_DATE uri-" + uri.toString());
                retCursor = getMoviesBySortPrefAndDate(uri, projection, sortOrder);
                break;
            }


            // "movie/*"
            case MOVIE_WITH_SORT: {
                //Log.d(LOG_TAG + ".query()", "MOVIE_WITH_SORT uri-" + uri.toString());
                retCursor = getMoviesBySortPrefAndMovieId(uri, projection, sortOrder);
                break;
            }


            // "movie"
            case MOVIE: {
                //Log.d(LOG_TAG + ".query()", "MOVIE uri-" + uri.toString());
                retCursor = getMovies(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }
            // "movieTrailers/*"
            case TRAILERS_WITH_MOVIE_ID: {
                retCursor = getMovieTrailers(uri, projection, sortOrder);
                break;
            }
            // "movieTrailers"
            case TRAILER: {
                retCursor = getTrailers(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }
            // "movieReviews/*"
            case REVIEWS_WITH_MOVIE_ID: {
                retCursor = getMovieReviews(uri, projection, sortOrder);
                break;
            }
            // "movieReviews"
            case REVIEW: {
                retCursor = getReviews(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                normalizeDate(values);
                long _id = db.insert(MoviesDbContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesDbContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILER: {
                long _id = db.insert(MoviesDbContract.MovieTrailersEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesDbContract.MovieTrailersEntry.buildTrailersUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEW: {
                long _id = db.insert(MoviesDbContract.MovieReviewsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesDbContract.MovieReviewsEntry.buildReviewsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        //this makes delete all the rows return the number of rows deleted.
        if (null == selection) selection = "1";
        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(MoviesDbContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILER:
                rowsDeleted = db.delete(MoviesDbContract.MovieTrailersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW:
                rowsDeleted = db.delete(MoviesDbContract.MovieReviewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // This is a lot like the delete function.  We return the number of rows impacted
        // by the update.
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case MOVIE:
                normalizeDate(values);
                rowsUpdated = db.update(MoviesDbContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TRAILER:
                rowsUpdated = db.update(MoviesDbContract.MovieTrailersEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REVIEW:
                rowsUpdated = db.update(MoviesDbContract.MovieReviewsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;

        switch (match) {
            case MOVIE:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = -1;
                        //check if movie already exists update else insert.
                        String movieId = value.getAsString(MoviesDbContract.MovieEntry.COLUMN_MOVIE_ID);
                        //Long date = value.getAsLong(MoviesDbContract.MovieEntry.COLUMN_MOVIE_INSERT_DATE);
                        String sortBy = value.getAsString(MoviesDbContract.MovieEntry.COLUMN_MOVIE_SORT_BY);
                        Uri movieUri = MoviesDbContract.MovieEntry.buildMovieSortByWithMovieIdUri(sortBy, movieId);
                        //Log.d(LOG_TAG + ".bulkInsert()", "buildMovieSortByWithMovieIdUri uri-" + movieUri.toString());
                        Cursor cur = getContext().getContentResolver().query(movieUri,
                                null, null, null, null);
                        if (cur.moveToFirst()) {
                            int id = cur.getColumnIndex(MoviesDbContract.MovieEntry._ID);
                            Log.d(LOG_TAG + ".bulkInsert()", "updating movie w/ rowId-" + cur.getLong(id));
                            //copy over favorite data
                            value.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_IS_FAV, cur.getInt(cur.getColumnIndex(MoviesDbContract.MovieEntry.COLUMN_MOVIE_IS_FAV)));
                            _id = update(MoviesDbContract.MovieEntry.CONTENT_URI, value, sMovieWithSortByWithMovieIdQuerySelection, new String[]{sortBy, movieId});
                            if (_id != 0)
                                returnCount++;
                        } else {
                            _id = db.insert(MoviesDbContract.MovieEntry.TABLE_NAME, null, value);
                            if (_id != -1)
                                returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                    break;
                }
            case TRAILER:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(MoviesDbContract.MovieTrailersEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                    break;
                }
            case REVIEW:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(MoviesDbContract.MovieReviewsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                    break;
                }
            default:
                returnCount = super.bulkInsert(uri, values);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(MoviesDbContract.MovieEntry.COLUMN_MOVIE_INSERT_DATE)) {
            long dateValue = values.getAsLong(MoviesDbContract.MovieEntry.COLUMN_MOVIE_INSERT_DATE);
            values.put(MoviesDbContract.MovieEntry.COLUMN_MOVIE_INSERT_DATE, MoviesDbContract.normalizeDate(dateValue));
        }
    }

}
