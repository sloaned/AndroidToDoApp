package com.example.catalyst.androidtodo.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.catalyst.androidtodo.fragments.TaskFragment;
import com.example.catalyst.androidtodo.models.Task;

/**
 * Created by dsloane on 3/24/2016.
 */
public class DetailActivity extends AppCompatActivity /*implements ContactFragment.ContactClickListener */{

    public static final String TAG = DetailActivity.class.getSimpleName();

    private static Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish();
            return;
        }

        if (savedInstanceState == null) {

            Intent intent = getIntent();
            task = (Task) intent.getSerializableExtra("Task");

            TaskFragment fragment = TaskFragment.newInstance(task, null);

            getFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
        }
    }

/*
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
   */
}
