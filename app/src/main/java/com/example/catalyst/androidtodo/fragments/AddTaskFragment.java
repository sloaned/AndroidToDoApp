package com.example.catalyst.androidtodo.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.catalyst.androidtodo.BuildConfig;
import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.activities.HomeActivity;
import com.example.catalyst.androidtodo.models.Task;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.ILoginUser;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.ITask;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.IUsers;
import com.example.catalyst.androidtodo.util.SharedPreferencesConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
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

    private Task task;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
    private OkHttpClient client = new OkHttpClient();
    private SharedPreferences prefs;

    private Context context;

    private SharedPreferences.Editor mEditor;
    private Retrofit retrofit;

    private ILoginUser loginUser;
    private ITask apiCaller;
    private IUsers userCall;

    private View addTaskView;

    public AddTaskFragment() {}

    public interface getAllMethods {

        public void updateList();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        context = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();


        addTaskView = inflater.inflate(R.layout.add_new_task, null);

        builder.setView(addTaskView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                EditText taskTitleView = (EditText) addTaskView.findViewById(R.id.newTaskTitleValue);
                String taskTitle = taskTitleView.getText().toString();
                EditText taskDetailView = (EditText)  addTaskView.findViewById(R.id.newTaskDetailsValue);
                String taskDetails = taskDetailView.getText().toString();
                EditText taskDueDateView = (EditText) addTaskView.findViewById(R.id.newTaskDueDateValue);
                String taskDueDate = taskDueDateView.getText().toString();
                EditText taskLocationView = (EditText) addTaskView.findViewById(R.id.newTaskLocationValue);
                String taskLocation = taskLocationView.getText().toString();



                if (taskTitle != null && !taskTitle.equals((String) null)) {

                    task = new Task();
                    task.setTaskTitle(taskTitle);
                    task.setTaskDetails(taskDetails);
                    task.setDueDate(taskDueDate);
                    task.setLocationName(taskLocation);

                    String taskLocationCoordinates = "";
                    if (taskLocation != null && !taskLocation.equals("")) {
                        getLocationCoordinates(taskLocation);
                    }

                    if (task.getLatitude() != 0) {
                        Log.v(TAG, "Latitude = " + task.getLatitude() + ", longitude = " + task.getLongitude());
                    }


                    client = assignInterceptorWithToken();
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
                            if (context instanceof HomeActivity) {
                                ((HomeActivity) context).updateList();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
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

        ButterKnife.bind(getActivity());

        return builder.create();

    }

    public static AddTaskFragment newInstance() {

        Log.d(TAG, "trying to make a new instance here");
        AddTaskFragment fragment = new AddTaskFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    private OkHttpClient assignInterceptorWithToken() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

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

    public void getLocationCoordinates(String location) {
        location = location.replaceAll("\\s+","+");
        final String GOOGLE_MAPS_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=" + location  + "&key=" + BuildConfig.APIKEY;

        if (isNetworkAvailable() ) {
            OkHttpClient okHttpClient = new OkHttpClient();
            okhttp3.Request request = new Request.Builder()
                    .url(GOOGLE_MAPS_URL)
                    .build();
            okhttp3.Call call = okHttpClient.newCall(request);
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e(TAG, e.getMessage());
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            JSONObject data = new JSONObject(jsonData);
                            String status = data.getString("status");
                            if (status.equals("OK")) {
                                JSONArray results = data.getJSONArray("results");
                                if (results.length() > 0) {
                                    JSONObject firstResult = results.getJSONObject(0);
                                    JSONObject geometry = firstResult.getJSONObject("geometry");
                                    JSONObject location = geometry.getJSONObject("location");
                                    task.setLatitude(location.getDouble("lat"));
                                    task.setLongitude(location.getDouble("lng"));
                                }
                            }

                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });

        }


    }



    public void getLocationTimezone(String coordinates) {

    }






    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }
}
