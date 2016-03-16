package com.example.catalyst.androidtodo.activities;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.adapters.TaskAdapter;
import com.example.catalyst.androidtodo.fragments.TaskFragment;
import com.example.catalyst.androidtodo.fragments.DividerItemDecoration;
import com.example.catalyst.androidtodo.models.Participant;
import com.example.catalyst.androidtodo.models.Task;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.ITask;
import com.example.catalyst.androidtodo.util.JSONConstants;
import com.example.catalyst.androidtodo.util.SharedPreferencesConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity implements AccountManagerCallback<Bundle>, TaskFragment.getAllMethods {

    private final String TAG = getClass().getSimpleName();
    private AccountManager accountManager;
    private String type;

    private static final String BASE_URL = "http://pc30120.catalystsolves.com:8080/";

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
    private OkHttpClient client = new OkHttpClient();
    private ITask apiCaller;
    private Retrofit retrofit;

    private ArrayList<Task> mTasks = new ArrayList<Task>();
    private TaskAdapter adapter;
    private SharedPreferences prefs;
    private SharedPreferences.Editor mEditor;

    @Bind(R.id.taskRecyclerView)RecyclerView mTaskListView;
    @Bind(R.id.new_task_button)Button newTaskButton;

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


        adapter = new TaskAdapter(this, mTasks);


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mTaskListView.setLayoutManager(layoutManager);

        mTaskListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        mTaskListView.setHasFixedSize(true);


        newTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = TaskFragment.newInstance(null);
                if (dialog.getDialog() != null) {
                    dialog.getDialog().setCanceledOnTouchOutside(false);
                }
                dialog.show(HomeActivity.this.getSupportFragmentManager(), "dialog");
                getAllTasks();
            }
        });

    }

    @Override
    public void updateList() {
        getAllTasks();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "resumed!");

        mTaskListView.setAdapter(adapter);
        getAllTasks();

        super.onResume();

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

    public void getAllTasks() {
        mTasks.clear();
        adapter.notifyDataSetChanged();
        client = assignInterceptorWithToken();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        apiCaller = retrofit.create(ITask.class);
        Call<ResponseBody> getTasks = apiCaller.getAllTasks();

        getTasks.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                try {

                    String taskArray = response.body().string();

                    try {
                        JSONArray tasks = new JSONArray(taskArray);
                        Log.d(TAG, taskArray);
                        for (int i = 0; i < tasks.length(); i++) {
                            JSONObject jsonTask = tasks.getJSONObject(i);
                            Task task = new Task();
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_ID)) {
                                task.setId(jsonTask.getInt(JSONConstants.JSON_TASK_ID));
                            }
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_TITLE)) {
                                task.setTaskTitle(jsonTask.getString(JSONConstants.JSON_TASK_TITLE));
                            }
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_DETAILS)) {
                                task.setTaskDetails(jsonTask.getString(JSONConstants.JSON_TASK_DETAILS));
                            }
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_LONGITUDE)) {
                                task.setLongitude(jsonTask.getDouble(JSONConstants.JSON_TASK_LONGITUDE));
                            }
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_LATITUDE)) {
                                task.setLatitude(jsonTask.getDouble(JSONConstants.JSON_TASK_LATITUDE));
                            }
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_LOCATION)) {
                                task.setLocationName(jsonTask.getString(JSONConstants.JSON_TASK_LOCATION));
                            }
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_DUE_DATE)) {
                                task.setDueDate(jsonTask.getString(JSONConstants.JSON_TASK_DUE_DATE));
                            }
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_TIMEZONE)) {
                                task.setTimeZone(jsonTask.getString(JSONConstants.JSON_TASK_TIMEZONE));
                            }
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_PARTICIPANTS)) {
                                JSONArray participantsArray = jsonTask.getJSONArray(JSONConstants.JSON_TASK_PARTICIPANTS);
                                List<Participant> participants = new ArrayList<Participant>();
                                for (int j = 0; j < participantsArray.length(); j++) {
                                    JSONObject participantObj = participantsArray.getJSONObject(j);
                                    String name = participantObj.getString(JSONConstants.JSON_TASK_PARTICIPANT_NAME);
                                    Participant participant = new Participant();
                                    participant.setParticipantName(name);
                                    participants.add(participant);
                                }
                                task.setParticipants(participants);
                            }

                            mTasks.add(task);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (String head : response.headers().names()) {
                    Log.v(TAG, head + " " + response.headers().values(head));
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error: " + t.toString());
            }
        });
    }

    private OkHttpClient assignInterceptorWithToken() {

        final String token = prefs.getString(SharedPreferencesConstants.PREFS_TOKEN, (String) null);
        Log.d(TAG, "before adding interceptor, token = " + token);
        return client.newBuilder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .method(original.method(), original.body())
                        .header("X-AUTH-TOKEN", token)
                        .build();
                return chain.proceed(request);
            }
        }).build();
    }

    public void deleteTask(final int id) {
        //mTasks.clear();
        client = assignInterceptorWithToken();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        apiCaller = retrofit.create(ITask.class);
        Call<ResponseBody> deleteTask = apiCaller.deleteTask(id);
        deleteTask.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.d(TAG, "delete successful");
                int position = getTaskPosition(id);
                if (position > -1) {
                    mTasks.remove(position);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "problem with deleting task");
            }
        });
    }

    public int getTaskPosition(int id) {
        for (int i = 0; i < mTasks.size(); i++) {
            if (mTasks.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }


    public void showTask(Task task) {
        DialogFragment dialog = TaskFragment.newInstance(task);
        if (dialog.getDialog() != null) {
            dialog.getDialog().setCanceledOnTouchOutside(false);
        }
        dialog.show(this.getSupportFragmentManager(), "dialog");
        getAllTasks();
    }


}
