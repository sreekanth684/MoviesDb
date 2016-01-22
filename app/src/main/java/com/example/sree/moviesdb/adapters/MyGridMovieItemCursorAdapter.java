package com.example.sree.moviesdb.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sree.moviesdb.MainActivityFragment;
import com.example.sree.moviesdb.R;
import com.squareup.picasso.Picasso;

/**
 * Created by Sree on 1/7/16.
 */
public class MyGridMovieItemCursorAdapter extends CursorAdapter {
    final String LOG_TAG = MyGridMovieItemCursorAdapter.class.getSimpleName();

    public MyGridMovieItemCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_movie, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view.findViewById(R.id.movie_image);
        String imageUrl = cursor.getString(MainActivityFragment.COL_MOVIE_POSTER_URL);
        //Log.d(LOG_TAG + ".bindView()", "imageUrl is " + imageUrl);
        if(imageUrl==null || imageUrl.equalsIgnoreCase("null") || imageUrl.isEmpty()) {
            imageView.setImageResource(R.mipmap.no_image);
        } else {
            Picasso.with(context).load("http://image.tmdb.org/t/p/w154/"+imageUrl).into(imageView);
        }
        /*TextView textView = (TextView) view.findViewById(R.id.movie_text);
        if(textView != null) {
            textView.setText(cursor.getString(MainActivityFragment.COL_MOVIE_TITLE));
        }*/
    }
}
