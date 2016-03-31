package com.example.catalyst.androidtodo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.fragments.HomeFragment;
import com.example.catalyst.androidtodo.fragments.TaskFragment;
import com.example.catalyst.androidtodo.listener.NotificationReceiver;
import com.example.catalyst.androidtodo.models.Task;
import com.example.catalyst.androidtodo.util.SharedPreferencesConstants;

import butterknife.ButterKnife;


public class HomeActivity extends AppCompatActivity implements HomeFragment.OnTaskSelectedListener, TaskFragment.OnSubmitListener/* implements AccountManagerCallback<Bundle> */{

    private final String TAG = getClass().getSimpleName();

    private SharedPreferences prefs;
    private SharedPreferences.Editor mEditor;

    @Override
    public void onListChanged(final boolean complete) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.listView);

                if (homeFragment != null) {
                    if (complete) {
                        homeFragment.getCompletedTasks();
                    } else {
                        homeFragment.getUncompletedTasks();
                    }
                }
            }
        });
    }

    @Override
    public void onTaskSelected(Task task) {
        TaskFragment taskFragment = (TaskFragment) getFragmentManager().findFragmentById(R.id.detailsView);

        if (taskFragment != null) {
            taskFragment.updateTaskView(task);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = prefs.edit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "resumed!");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "paused!");
        super.onPause();
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
    @Override
    public void run(final AccountManagerFuture<Bundle> future) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();
                    final String authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    if (authToken != null) {
                        Log.v(TAG, "authToken exists");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
*//*


    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

  */

}
