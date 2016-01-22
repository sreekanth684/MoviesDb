package com.example.sree.moviesdb.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.sree.moviesdb.DetailActivityFragment;
import com.example.sree.moviesdb.R;
import com.example.sree.moviesdb.ReviewDetailsActivity;

/**
 * Created by Sree on 1/8/16.
 */
public class MyReviewItemCursorAdapter extends CursorAdapter {
    final String LOG_TAG = MyReviewItemCursorAdapter.class.getSimpleName();

    public MyReviewItemCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_review, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        Log.d(LOG_TAG, "bindView()::review author:" + cursor.getString(DetailActivityFragment.COL_REVIEW_AUTHOR));
        TextView textView = (TextView) view.findViewById(R.id.reviewAuthor);
        if(textView!=null)
            textView.setText(cursor.getString(DetailActivityFragment.COL_REVIEW_AUTHOR));
        view.setTag(cursor.getString(DetailActivityFragment.COL_REVIEW_CONTENT));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cursor reviewCursor = (Cursor) v.getTag();
                String reviewContent = (String) v.getTag();
                //Log.d(LOG_TAG, "bindView()::Invoking review detail intent with content:" + reviewCursor.getString(DetailActivityFragment.COL_REVIEW_CONTENT));
                Log.d(LOG_TAG, "bindView()::Invoking review detail intent with content:" + reviewContent);
                //Toast.makeText(getActivity(), gridMovieItemInfo.getMovieId(), Toast.LENGTH_LONG).show();
                Intent reviewDetailIntent = new Intent(context, ReviewDetailsActivity.class).putExtra(Intent.EXTRA_TEXT, reviewContent);
                context.startActivity(reviewDetailIntent);
            }
        });
    }
}
