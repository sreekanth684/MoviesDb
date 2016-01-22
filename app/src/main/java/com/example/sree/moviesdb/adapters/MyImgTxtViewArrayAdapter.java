package com.example.sree.moviesdb.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sree.moviesdb.R;
import com.example.sree.moviesdb.infos.*;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Sree on 7/22/15.
 */
public class MyImgTxtViewArrayAdapter<T> extends ArrayAdapter {

    final String LOG_TAG = MyImgTxtViewArrayAdapter.class.getSimpleName();

    private int mImageViewResourceId;
    private int[] mTextViewResourceId;
    private List<T> mObjects;
    private int mResource;
    private LayoutInflater mInflater;
    private Context mContext;

    public MyImgTxtViewArrayAdapter(Activity context, int resource, List<T> objects, int imageViewResourceId, int... textViewResourceId) {
        super(context, resource, objects);

        mContext = context;
        mImageViewResourceId = imageViewResourceId;
        mTextViewResourceId = textViewResourceId;
        mObjects = objects;
        mResource = resource;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public T getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ImageView imageView;
        TextView textView;

        if (convertView == null) {
            view = mInflater.inflate(mResource, parent, false);
        } else {
            view = convertView;
        }

        T item = getItem(position);
        //GridMovieItemInfo
        if(item instanceof GridMovieItemInfo){
            GridMovieItemInfo movie = (GridMovieItemInfo) item;
            imageView = (ImageView) view.findViewById(mImageViewResourceId);
            //Log.d(LOG_TAG + ".getView()", "GridMovieItemInfo:: " + movie.toString() + imageView);
            if(imageView!=null && movie.getMoviePosterUrl() !=null) {
                Picasso.with(mContext).load(movie.getMoviePosterUrl()).into(imageView);
            }else{
                imageView.setImageResource(R.mipmap.no_image);
            }

            textView = (TextView) view.findViewById(mTextViewResourceId[0]);
            if(textView!=null)
            textView.setText(movie.getMovieName());
        }
        //Trailers
        if(item instanceof DetailMovieItemInfo.TrailerInfo){
            DetailMovieItemInfo.TrailerInfo trailerInfo = (DetailMovieItemInfo.TrailerInfo) item;
            textView = (TextView) view.findViewById(mTextViewResourceId[0]);
            if(textView!=null)
                textView.setText(trailerInfo.getTrailerName());
        }
        //Reviews
        if(item instanceof DetailMovieItemInfo.ReviewInfo){
            DetailMovieItemInfo.ReviewInfo reviewInfo = (DetailMovieItemInfo.ReviewInfo) item;
            textView = (TextView) view.findViewById(mTextViewResourceId[0]);
            if(textView!=null)
                textView.setText(reviewInfo.getReviewAuthor());
        }
        return view;
    }
}
