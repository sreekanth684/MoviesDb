package com.example.sree.moviesdb;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by Sree on 1/5/16.
 */
public class MoviesDbApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
