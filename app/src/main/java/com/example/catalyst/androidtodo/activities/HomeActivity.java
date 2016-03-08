package com.example.catalyst.androidtodo.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.adapters.TaskAdapter;
import com.example.catalyst.androidtodo.models.Task;
import com.example.catalyst.androidtodo.network.ApiCaller;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.ILoginUser;
import com.example.catalyst.androidtodo.network.entities.LoginUser;
import com.example.catalyst.androidtodo.util.SharedPreferencesConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;

public class HomeActivity extends AppCompatActivity implements AccountManagerCallback<Bundle>{

    private final String TAG = getClass().getSimpleName();
    private AccountManager accountManager;
    private String type;

    private static final String BASE_URL = "http://pc30120.catalystsolves.com:8080/";

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
    private OkHttpClient client = new OkHttpClient();

    private ArrayList<Task> mTasks = new ArrayList<Task>();
    private SharedPreferences prefs;
    private SharedPreferences.Editor mEditor;

    @Bind(R.id.taskRecyclerView)RecyclerView mTaskListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = prefs.edit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        type = "com.example.catalyst.androidtodo";
       // accountManager = AccountManager.get(this);
       //Account acc = new Account(type, type);

       // accountManager.getAuthToken(acc, type, null, this, this, null);


        getTasks();

        TaskAdapter adapter = new TaskAdapter(this, mTasks);
        mTaskListView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mTaskListView.setLayoutManager(layoutManager);

        mTaskListView.setHasFixedSize(true);
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

        return super.onOptionsItemSelected(item);
    }

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

    public void getTasks() {
        new ApiCaller(HomeActivity.this).getAllTasks();
    }

}
