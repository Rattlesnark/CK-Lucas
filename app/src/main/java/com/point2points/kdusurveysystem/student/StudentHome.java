package com.point2points.kdusurveysystem.student;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.point2points.kdusurveysystem.R;

public class StudentHome extends StudentToolbarDrawer {

    private static Context context;

    static ProgressBar progressBar;

    private int option;

    private static final String TAG = "StudentHome";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.student_home);

        super.onCreate(savedInstanceState);
        super.loadUserProfileInfo(savedInstanceState);

        context = getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setTitle("StudentHome");
            }}

        progressBar = (ProgressBar) findViewById(R.id.progressBar_recycler_view);
        progressBar.getIndeterminateDrawable().setColorFilter(0xFF0173B1, android.graphics.PorterDuff.Mode.MULTIPLY);
        onProgressBar();
        offProgressBar();
    }

    public static void onProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    public static void offProgressBar(){
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
