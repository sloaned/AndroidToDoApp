package com.example.catalyst.androidtodo.fragments;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.activities.DetailActivity;
import com.example.catalyst.androidtodo.activities.HomeActivity;
import com.example.catalyst.androidtodo.activities.LoginActivity;
import com.example.catalyst.androidtodo.adapters.TaskAdapter;
import com.example.catalyst.androidtodo.data.DBHelper;
import com.example.catalyst.androidtodo.data.TaskDBOperations;
import com.example.catalyst.androidtodo.models.Participant;
import com.example.catalyst.androidtodo.models.Task;
import com.example.catalyst.androidtodo.network.ApiCaller;
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
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by dsloane on 3/25/2016.
 */
public class HomeFragment extends Fragment implements AccountManagerCallback<Bundle> {

    private final String TAG = getClass().getSimpleName();

    private static final String BASE_URL = "http://pc30120.catalystsolves.com:8080/";

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
    private OkHttpClient client = new OkHttpClient();
    private ITask apiCaller;
    private Retrofit retrofit;

    private boolean isDualPane;

    private ArrayList<Task> mTasks = new ArrayList<Task>();
    private TaskAdapter adapter;
    private View homeView;
    private SharedPreferences prefs;
    private SharedPreferences.Editor mEditor;

    private int tasksSyncedFromServer;
    private int tasksSyncedToServer;

    private TaskDBOperations mTaskDBOperations;

    @Bind(R.id.taskRecyclerView)RecyclerView mTaskListView;
    @Bind(R.id.new_task_button)Button newTaskButton;
    @Bind(R.id.progressBar)ProgressBar mProgressBar;
    @Bind(R.id.refreshImageView)ImageView mRefreshImageView;
    @Bind(R.id.viewCompletedTasksButton)Button viewCompletedTasksButton;
    @Bind(R.id.viewUncompletedTasksButton)Button viewUncompletedTasksButton;

    OnTaskSelectedListener mCallback;


    public interface OnTaskSelectedListener {
        public void onTaskSelected(Task task);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        homeView = inflater.inflate(R.layout.content_home, null);
        ButterKnife.bind(this, homeView);

        try {
            mCallback = (OnTaskSelectedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement onTaskSelectedListener");
        }

        isDualPane = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mEditor = prefs.edit();
        mTaskDBOperations = new TaskDBOperations(getActivity());

        adapter = new TaskAdapter(getActivity(), mTasks, this);

        mTaskListView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mTaskListView.setLayoutManager(layoutManager);
        mTaskListView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mTaskListView.setHasFixedSize(true);

        viewUncompletedTasksButton.setVisibility(View.GONE);

        newTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTask(new Task());
            }
        });

        viewCompletedTasksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCompletedTasks();
            }
        });

        viewUncompletedTasksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUncompletedTasks();
            }
        });

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncTasks();
            }
        });

        mRefreshImageView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);

        getUncompletedTasks();


        return homeView;
    }

    public static HomeFragment newInstance() {

        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public void updateList() {
        Log.d(TAG, "updating list!");
        getUncompletedTasks();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
        Log.d(TAG, "resumed!");
    }

    @Override
    public void onPause() {
        Log.d(TAG, "paused!");
        super.onPause();
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

    public void deleteTaskLocally(int id) {
        DBHelper dbHelper = new DBHelper(getActivity());
        dbHelper.deleteTask(id);
        dbHelper.close();
    }

    public void deleteTaskFromServer(final int serverId, final int localId) {
        client = new ApiCaller(getActivity()).assignInterceptorWithToken();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        apiCaller = retrofit.create(ITask.class);
        Call<ResponseBody> deleteTask = apiCaller.deleteTask(serverId);
        deleteTask.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.d(TAG, "delete successful");
                int position = getTaskPosition(localId);
                if (position > -1) {
                    mTasks.remove(position);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }

                deleteTaskLocally(localId);
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


        if (isDualPane) {
            mCallback.onTaskSelected(task);
        } else {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("Task", task);
            startActivity(intent);
            updateList();
        }
    }

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

    private void toggleButtons() {
        Log.d(TAG, "viewCompletedTasksButton visibility = " + viewCompletedTasksButton.getVisibility());
        Log.d(TAG, "viewUncompletedTasksbutton visibility = " + viewUncompletedTasksButton.getVisibility());
        if (viewCompletedTasksButton.getVisibility() == View.GONE) {
            viewUncompletedTasksButton.setVisibility(View.GONE);
            viewCompletedTasksButton.setVisibility(View.VISIBLE);
        } else {
            viewUncompletedTasksButton.setVisibility(View.VISIBLE);
            viewCompletedTasksButton.setVisibility(View.GONE);
        }
    }

    private void syncTasks() {

        // get tasks from server first

        client = new ApiCaller(getActivity()).assignInterceptorWithToken();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        apiCaller = retrofit.create(ITask.class);
        Call<ResponseBody> getTasks = apiCaller.getUnsynchedTasks();

        getTasks.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                try {
                    String taskArray = response.body().string();
                    Log.d(TAG, "now getting unsynched tasks from the server");
                    Log.d(TAG, taskArray);

                    try {
                        JSONArray tasks = new JSONArray(taskArray);

                        tasksSyncedFromServer = tasks.length();
                        for (int i = 0; i < tasks.length(); i++) {
                            JSONObject jsonTask = tasks.getJSONObject(i);
                            Task task = new Task();
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_ID)) {
                                task.setServerId(jsonTask.getInt(JSONConstants.JSON_TASK_ID));
                            }
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_SYNC_DATE)) {
                                task.setSyncDate(jsonTask.getLong(JSONConstants.JSON_TASK_SYNC_DATE));
                            }
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_LAST_MODIFIED_DATE)) {
                                task.setLastModifiedDate(jsonTask.getLong(JSONConstants.JSON_TASK_LAST_MODIFIED_DATE));
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
                                Log.d(TAG, "due date not null, due date = " + jsonTask.getString(JSONConstants.JSON_TASK_DUE_DATE));
                                task.setDueDate(jsonTask.getLong(JSONConstants.JSON_TASK_DUE_DATE));
                            }
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_TIMEZONE)) {
                                task.setTimeZone(jsonTask.getString(JSONConstants.JSON_TASK_TIMEZONE));
                            }
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_COMPLETED)) {
                                task.setCompleted(jsonTask.getBoolean(JSONConstants.JSON_TASK_COMPLETED));
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

                            // update tasks locally

                            DBHelper dbHelper = new DBHelper(getActivity());
                            if (dbHelper.doesTaskExist(task.getServerId())) {
                                mTaskDBOperations.updateTaskLocally(task);
                            } else {
                                mTaskDBOperations.addTaskToLocalDatabase(task);
                            }
                            dbHelper.close();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error: " + t.toString());
            }
        });

        // send local unsynched tasks to server

        Log.d(TAG, "now sending unsynched tasks to the server");

        ArrayList<Task> unsyncedTasks = mTaskDBOperations.getLocalUnsynchedTasks();
        tasksSyncedToServer = unsyncedTasks.size();

        Date date = new Date();
        long ms = date.getTime();

        for (Task task : unsyncedTasks) {

            task.setSyncDate(ms);
            Log.d(TAG, task.getTaskTitle());
            if (task.getServerId() < 1) {
                addTaskToServerDatabase(task);
            } else {
                task.setId(task.getServerId());
                updateTaskOnServer(task);
            }
        }

        String message = "Sync successful. " + tasksSyncedToServer + " tasks uploaded to server, " +
                tasksSyncedFromServer + " tasks imported from server";
        Log.d(TAG, "message = " + message);
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

        // call local database to show all tasks
        updateList();

        Log.d(TAG, "Tasks have been synched. now mTasks contains: ");
        for (Task task : mTasks) {
            Log.v(TAG, task.getTaskTitle());
        }
    }

    public void getCompletedTasks() {
        if (viewUncompletedTasksButton.getVisibility() == View.GONE) {
            viewCompletedTasksButton.setVisibility(View.GONE);
            viewUncompletedTasksButton.setVisibility(View.VISIBLE);
        }
        Log.v(TAG, "getCompletedTasks()");
        toggleRefresh();
        mTasks.clear();
        DBHelper dbHelper = new DBHelper(getActivity());
        ArrayList<Task> tasks = dbHelper.getCompletedTasks();
        for (Task task : tasks) {
            Log.d(TAG, task.getTaskTitle());
            mTasks.add(task);
        }
        dbHelper.close();
        toggleRefresh();
        adapter.notifyDataSetChanged();
    }

    public void getUncompletedTasks() {
        if (viewCompletedTasksButton.getVisibility() == View.GONE) {
            viewUncompletedTasksButton.setVisibility(View.GONE);
            viewCompletedTasksButton.setVisibility(View.VISIBLE);
        }
        Log.v(TAG, "getUncompletedTasks()");
        toggleRefresh();
        mTasks.clear();
        DBHelper dbHelper = new DBHelper(getActivity());
        ArrayList<Task> tasks = dbHelper.getUncompletedTasks();
        for (Task task : tasks) {
            Log.d(TAG, task.getTaskTitle());
            mTasks.add(task);
        }
        dbHelper.close();
        toggleRefresh();
        adapter.notifyDataSetChanged();
    }


    public void addTaskToServerDatabase(Task task) {
        client = new ApiCaller(getActivity()).assignInterceptorWithToken();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        apiCaller = retrofit.create(ITask.class);

        Call<ResponseBody> addTask = apiCaller.createTask(task);

        addTask.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.v(TAG, "Success!");
                updateList();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Failure!");
            }
        });
    }


    public void updateTaskOnServer(Task task) {
        client = new ApiCaller(getActivity()).assignInterceptorWithToken();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        apiCaller = retrofit.create(ITask.class);

        Log.d(TAG, "The id of the task = " + task.getId());

        Call<ResponseBody> addTask = apiCaller.editTask(task);

        addTask.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                updateList();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Failure updating!");
            }
        });
    }
}
