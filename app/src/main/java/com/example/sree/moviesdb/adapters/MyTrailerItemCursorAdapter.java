package com.example.sree.moviesdb.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.sree.moviesdb.DetailActivityFragment;
import com.example.sree.moviesdb.R;

/**
 * Created by Sree on 1/8/16.
 */
public class MyTrailerItemCursorAdapter extends CursorAdapter {
    final String LOG_TAG = MyTrailerItemCursorAdapter.class.getSimpleName();

    public MyTrailerItemCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_trailer, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        Log.d(LOG_TAG, "bindView()::trailer name:" + cursor.getString(DetailActivityFragment.COL_TRAILER_NAME));
        TextView textView = (TextView) view.findViewById(R.id.trailerName);
        if(textView!=null)
            textView.setText(cursor.getString(DetailActivityFragment.COL_TRAILER_NAME));
        view.setTag(cursor.getString(DetailActivityFragment.COL_TRAILER_SOURCE_URL));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cursor cursor1 = (Cursor) v.getTag();
                String link = (String) v.getTag();
                Log.d(LOG_TAG, "bindView()::Invoking trailer detail intent with source:" + link);
                //Toast.makeText(getActivity(), gridMovieItemInfo.getMovieId(), Toast.LENGTH_LONG).show();
                Intent trailerDetailIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=" + link));
                context.startActivity(trailerDetailIntent);
            }
        });
    }
}
