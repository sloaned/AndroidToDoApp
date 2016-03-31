package com.example.catalyst.androidtodo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.fragments.TaskFragment;
import com.example.catalyst.androidtodo.listener.NotificationReceiver;
import com.example.catalyst.androidtodo.models.Task;
import com.example.catalyst.androidtodo.util.SharedPreferencesConstants;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor mEditor = prefs.edit();
            mEditor.putString(SharedPreferencesConstants.PREFS_TOKEN, null).apply();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_notify) {
            NotificationReceiver receiver = new NotificationReceiver();
            receiver.testNotification(this);
        }

        return super.onOptionsItemSelected(item);
    }

/*
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
   */
}
