package com.example.sree.moviesdb.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Sree on 1/3/16.
 */
public class MoviesDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;

    static final String DATABASE_NAME = "movies.db";

    private static final String LOG_TAG = MoviesDbHelper.class.getSimpleName();

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Movie Table
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MoviesDbContract.MovieEntry.TABLE_NAME + " (" +

                //need autoincrement to show user in the sorted/inserted order fetched from the call.
                MoviesDbContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                MoviesDbContract.MovieEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                MoviesDbContract.MovieEntry.COLUMN_MOVIE_INSERT_DATE + " INTEGER NOT NULL, " +
                MoviesDbContract.MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                MoviesDbContract.MovieEntry.COLUMN_MOVIE_ORG_TITLE + " TEXT, " +
                MoviesDbContract.MovieEntry.COLUMN_MOVIE_OVERVIEW + " TEXT," +
                MoviesDbContract.MovieEntry.COLUMN_MOVIE_POSTER_URL + " TEXT, " +
                MoviesDbContract.MovieEntry.COLUMN_MOVIE_RATING + " TEST, " +
                MoviesDbContract.MovieEntry.COLUMN_MOVIE_RUNTIME + " TEST, " +
                MoviesDbContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE + " INTEGER, " +
                MoviesDbContract.MovieEntry.COLUMN_MOVIE_IS_FAV + " INTEGER DEFAULT 0, " +
                MoviesDbContract.MovieEntry.COLUMN_MOVIE_SORT_BY + " TEXT NOT NULL, " +

                // To assure the application have just one movie entry per day
                // it's created a UNIQUE constraint
                " UNIQUE (" + MoviesDbContract.MovieEntry.COLUMN_MOVIE_ID + ", " +
                MoviesDbContract.MovieEntry.COLUMN_MOVIE_SORT_BY + ") ON CONFLICT REPLACE);";

        Log.d(LOG_TAG, "Create movie SQL::" + SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);

        //Trailers Table
        final String SQL_CREATE_MOVIE_TRAILERS_TABLE = "CREATE TABLE " + MoviesDbContract.MovieTrailersEntry.TABLE_NAME + " (" +

                MoviesDbContract.MovieTrailersEntry._ID + " INTEGER PRIMARY KEY," +

                MoviesDbContract.MovieTrailersEntry.COLUMN_TRAILER_MOVIE_KEY + " INTEGER NOT NULL, " +
                MoviesDbContract.MovieTrailersEntry.COLUMN_TRAILER_NAME + " TEXT NOT NULL, " +
                MoviesDbContract.MovieTrailersEntry.COLUMN_TRAILER_SOURCE_URL + " TEXT NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + MoviesDbContract.MovieTrailersEntry.COLUMN_TRAILER_MOVIE_KEY + ") REFERENCES " +
                MoviesDbContract.MovieEntry.TABLE_NAME + " (" + MoviesDbContract.MovieEntry._ID + ") ON DELETE CASCADE);";

        Log.d(LOG_TAG, "Create movie trailers SQL::" + SQL_CREATE_MOVIE_TRAILERS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TRAILERS_TABLE);

        //Reviews Table
        final String SQL_CREATE_MOVIE_REVIEWS_TABLE = "CREATE TABLE " + MoviesDbContract.MovieReviewsEntry.TABLE_NAME + " (" +

                MoviesDbContract.MovieReviewsEntry._ID + " INTEGER PRIMARY KEY," +

                MoviesDbContract.MovieReviewsEntry.COLUMN_REVIEW_MOVIE_KEY + " INTEGER NOT NULL, " +
                MoviesDbContract.MovieReviewsEntry.COLUMN_REVIEW_AUTHOR + " TEXT NOT NULL, " +
                MoviesDbContract.MovieReviewsEntry.COLUMN_REVIEW_CONTENT + " TEXT NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + MoviesDbContract.MovieReviewsEntry.COLUMN_REVIEW_MOVIE_KEY + ") REFERENCES " +
                MoviesDbContract.MovieEntry.TABLE_NAME + " (" + MoviesDbContract.MovieEntry._ID + ") ON DELETE CASCADE);";

        Log.d(LOG_TAG, "Create movie reviews SQL::" + SQL_CREATE_MOVIE_REVIEWS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_REVIEWS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //have to alter this table since it stores user's fav movies when ever you update version.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesDbContract.MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesDbContract.MovieTrailersEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesDbContract.MovieReviewsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
