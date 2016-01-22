package com.example.sree.moviesdb;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReviewDetailsActivityFragment extends Fragment {

    public ReviewDetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_details, container, false);
        String reviewContent = (String) getActivity().getIntent().getCharSequenceExtra(Intent.EXTRA_TEXT);
        TextView reviewContentTextView = (TextView) view.findViewById(R.id.reviewDetailsContentTextView);
        reviewContentTextView.setText(reviewContent);
        return view;
    }
}
