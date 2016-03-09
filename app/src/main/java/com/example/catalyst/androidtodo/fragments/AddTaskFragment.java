package com.example.catalyst.androidtodo.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.models.Task;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.ILoginUser;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.ITask;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.IUsers;
import com.example.catalyst.androidtodo.util.SharedPreferencesConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by dsloane on 3/8/2016.
 */
public class AddTaskFragment extends DialogFragment {

    public static final String TAG = AddTaskFragment.class.getSimpleName();

    private static final String BASE_URL = "http://pc30120.catalystsolves.com:8080/";

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
    private OkHttpClient client = new OkHttpClient();
    private SharedPreferences prefs;

    private SharedPreferences.Editor mEditor;
    private Retrofit retrofit;

    private ILoginUser loginUser;
    private ITask apiCaller;
    private IUsers userCall;

    public AddTaskFragment() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        Log.d(TAG, "here we are in the dialogue fragment");


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View addTaskView = inflater.inflate(R.layout.add_new_task, null);

        builder.setView(addTaskView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                EditText taskTitleView = (EditText) getActivity().findViewById(R.id.newTaskTitleValue);
                String taskTitle = taskTitleView.getText().toString();
                EditText taskDetailView = (EditText) getActivity().findViewById(R.id.newTaskDetailsValue);
                String taskDetails = taskDetailView.getText().toString();
                EditText taskDueDateView = (EditText) getActivity().findViewById(R.id.newTaskDueDateValue);
                String taskDueDate = taskDueDateView.getText().toString();
                EditText taskLocationView = (EditText) getActivity().findViewById(R.id.newTaskLocationValue);
                String taskLocation = taskLocationView.getText().toString();

                if (taskTitle != null && !taskTitle.equals((String) null)) {

                    Task task = new Task();
                    task.setTaskTitle(taskTitle);
                    task.setTaskDetails(taskDetails);
                    task.setDueDate(taskDueDate);
                    task.setLocationName(taskLocation);

                    client = assignInterceptorWithToken();
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(client)
                            .build();
                    apiCaller = retrofit.create(ITask.class);



                    Call<Task> addTask = apiCaller.createTask(task);

                    addTask.enqueue(new Callback<Task>() {
                        @Override
                        public void onResponse(Call<Task> call, Response<Task> response) {
                            Log.v(TAG, "Success!");
                        }

                        @Override
                        public void onFailure(Call<Task> call, Throwable t) {
                            Log.e(TAG, "Failure!");
                        }
                    });
                }

            }
        }) .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();

    }

    public static AddTaskFragment newInstance() {
        AddTaskFragment fragment = new AddTaskFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
