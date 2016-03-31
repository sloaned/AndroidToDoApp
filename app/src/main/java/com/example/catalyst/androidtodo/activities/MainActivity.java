package com.example.catalyst.androidtodo.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.adapters.TaskAdapter;
import com.example.catalyst.androidtodo.data.DBHelper;
import com.example.catalyst.androidtodo.data.TaskContract;
import com.example.catalyst.androidtodo.data.TaskDBOperations;
import com.example.catalyst.androidtodo.listener.NotificationReceiver;
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

import java.io.ByteArrayOutputStream;
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

public class MainActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private SharedPreferences.Editor mEditor;
    private TaskDBOperations mTaskDBOperations;

    private static final String BASE_URL = "http://pc30120.catalystsolves.com:8080/";

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
    private OkHttpClient client = new OkHttpClient();
    private ITask apiCaller;
    private Retrofit retrofit;

    private NotificationReceiver receiver = new NotificationReceiver();

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = prefs.edit();
        mTaskDBOperations = new TaskDBOperations(this);

        deleteDatabase(TaskContract.DATABASE_NAME);
        SQLiteDatabase taskDatabase = openOrCreateDatabase(TaskContract.DATABASE_NAME, MODE_PRIVATE, null);

        receiver.turnOnNotifications(this);

        if (!doesTokenExist()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {

            Log.d(TAG, "Logged in");
            getAllTasksFromServer();
        }

    }

    private boolean doesTokenExist() {
        String token = prefs.getString(SharedPreferencesConstants.PREFS_TOKEN, null);

        if (token == null || token.equals(null)) {
            return false;
        }
        return true;
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
            receiver.testNotification(this);
        }

        return super.onOptionsItemSelected(item);
    }


    public void getAllTasksFromServer() {

        client = new ApiCaller(this).assignInterceptorWithToken();
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
                    Log.d(TAG, taskArray);

                    try {
                        JSONArray tasks = new JSONArray(taskArray);
                        Log.d(TAG, taskArray);
                        for (int i = 0; i < tasks.length(); i++) {
                            JSONObject jsonTask = tasks.getJSONObject(i);
                            Task task = new Task();
                            if (!jsonTask.isNull(JSONConstants.JSON_TASK_ID)) {
                                task.setServerId(jsonTask.getInt(JSONConstants.JSON_TASK_ID));
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
                                Log.d(TAG, "due date back from server, due date = " + jsonTask.getString(JSONConstants.JSON_TASK_DUE_DATE));
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
                        /*
                            if (!task.isCompleted()) {
                                mTasks.add(task);
                            }  */

                            DBHelper dbHelper = new DBHelper(MainActivity.this);
                            if (dbHelper.doesTaskExist(task.getServerId())) {
                                mTaskDBOperations.updateTaskLocally(task);
                            } else {
                                mTaskDBOperations.addTaskToLocalDatabase(task);
                            }
                            dbHelper.close();

                        }

                        startHomeActivity();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        startHomeActivity();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    startHomeActivity();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error: " + t.toString());
                startHomeActivity();
            }
        });
    }

    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }






}
