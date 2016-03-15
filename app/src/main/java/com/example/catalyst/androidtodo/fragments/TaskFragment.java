package com.example.catalyst.androidtodo.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.activities.HomeActivity;
import com.example.catalyst.androidtodo.models.Participant;
import com.example.catalyst.androidtodo.models.Task;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.ITask;
import com.example.catalyst.androidtodo.util.SharedPreferencesConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
public class TaskFragment extends DialogFragment {

    public static final String TAG = TaskFragment.class.getSimpleName();

    private static final String BASE_URL = "http://pc30120.catalystsolves.com:8080/";

    private static Task task;
    private long dateInMilliseconds = 0;
    private long timeInMilliseconds = 0;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
    private OkHttpClient client = new OkHttpClient();
    private SharedPreferences prefs;

    private Context context;

    private SharedPreferences.Editor mEditor;
    private Retrofit retrofit;

    private ITask apiCaller;

    private View addTaskView;

    private EditText taskTitleView;
    private EditText taskDetailView;
    private EditText taskLocationView;
    private TextView dateView;
    private TextView timeView;
    private Button datePickerButton;
    private Button timePickerButton;
    private Button clearTimeButton;
    private Button clearDateButton;
    private Button clearLocationButton;
    private EditText taskParticipantView;

    private boolean editing = false;

    public TaskFragment() {}

    public interface getAllMethods {

        void updateList();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        context = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();


        addTaskView = inflater.inflate(R.layout.add_new_task, null);


        taskTitleView = (EditText) addTaskView.findViewById(R.id.newTaskTitleValue);
        taskDetailView = (EditText)  addTaskView.findViewById(R.id.newTaskDetailsValue);
        taskLocationView = (EditText) addTaskView.findViewById(R.id.newTaskLocationValue);
        dateView = (TextView) addTaskView.findViewById(R.id.newTaskDateValue);
        timeView = (TextView) addTaskView.findViewById(R.id.newTaskTimeValue);
        datePickerButton = (Button) addTaskView.findViewById(R.id.newTaskDatePickerBtn);
        timePickerButton = (Button) addTaskView.findViewById(R.id.newTaskTimePickerBtn);
        clearDateButton = (Button) addTaskView.findViewById(R.id.clearDateButton);
        clearTimeButton = (Button) addTaskView.findViewById(R.id.clearTimeButton);
        clearLocationButton = (Button) addTaskView.findViewById(R.id.clearLocationButton);
        taskParticipantView = (EditText) addTaskView.findViewById(R.id.newTaskParticipantsValue);

        //ButterKnife.bind(getActivity());

        clearDateButton.setVisibility(View.INVISIBLE);
        clearTimeButton.setVisibility(View.INVISIBLE);
        clearLocationButton.setVisibility(View.INVISIBLE);

        if (task.getTaskTitle() != null && !task.getTaskTitle().equals(null) && !task.getTaskTitle().equals("")) {
            editing = true;
            taskTitleView.setText(task.getTaskTitle());
            if (task.getTaskDetails() != null && !task.getTaskDetails().equals(null) && !task.getTaskDetails().equals("")) {
                taskDetailView.setText(task.getTaskDetails());
            }
            if (task.getLocationName() != null && !task.getLocationName().equals(null) && !task.getLocationName().equals("")) {
                taskLocationView.setText(task.getLocationName());
                clearLocationButton.setVisibility(View.VISIBLE);
            }
            if (task.getDueDate() != null && !task.getDueDate().equals(null) && !task.getDueDate().equals("")) {
                long milliseconds = Long.valueOf(task.getDueDate());
                SimpleDateFormat dt = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy");
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aaa");

                Date date = new Date(milliseconds);

                String dateString = date.toString();
                try {
                    date = dt.parse(dateString);
                } catch (ParseException e) {
                    Log.e(TAG, "date parsing error: " + e.getMessage());
                }
                String theDate = dateFormat.format(date);
                String theTime = timeFormat.format(date);
                dateView.setText(theDate);
                clearDateButton.setVisibility(View.VISIBLE);
                timeView.setText(theTime);
                clearTimeButton.setVisibility(View.VISIBLE);
            }
        }

        taskLocationView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && (!taskLocationView.getText().toString().equals(""))) {
                    clearLocationButton.setVisibility(View.VISIBLE);
                }
            }
        });

       // Button datePickerButton = (Button) addTaskView.findViewById(R.id.newTaskDatePickerBtn);
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int yy = calendar.get(Calendar.YEAR);
                int mm = calendar.get(Calendar.MONTH);
                int dd = calendar .get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        String dateString = String.valueOf(monthOfYear+1) + "/" + String.valueOf(dayOfMonth)
                                + "/" + String.valueOf(year);

                        dateView.setText(dateString);
                        clearDateButton.setVisibility(View.VISIBLE);

                        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                        try {
                            Date date = formatter.parse(dateString);
                            dateInMilliseconds = date.getTime();
                            Log.d(TAG, "date in milliseconds = " + dateInMilliseconds);
                        } catch (ParseException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }, yy, mm, dd);
                datePicker.show();
            }
        });
        //Button timePickerButton = (Button) addTaskView.findViewById(R.id.newTaskTimePickerBtn);
        timePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar calendar = Calendar.getInstance();
                int hh = calendar.get(Calendar.HOUR_OF_DAY);
                int mm = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        long milliSeconds = (hour * 3600 * 1000) + (minute * 60 * 1000);
                        timeInMilliseconds = milliSeconds;
                      /*
                        if (dateInMilliseconds == 0) {
                            Date date = new Date();
                            long milliseconds = date.getTime();
                            milliseconds += timeInMilliseconds;
                            task.setDueDate(String.valueOf(milliseconds));
                        }
                        else {
                            long milliseconds = dateInMilliseconds + timeInMilliseconds;

                            task.setDueDate(String.valueOf(milliseconds));
                        }   */
                        Log.d(TAG, "time in milliseconds = " + timeInMilliseconds);

                        String meridiem = "AM";
                        if (hour > 11) {
                            meridiem = "PM";
                        }
                        if (hour == 0) {
                            hour = 12;
                        }
                        if (hour > 12) {
                            hour -= 12;
                        }
                        String time = String.valueOf(hour) + ":";
                        if (minute < 10) {
                            time += "0";
                        }
                        time += String.valueOf(minute) + " " + meridiem;

                        timeView.setText(time);
                        clearTimeButton.setVisibility(View.VISIBLE);
                    }
                }, hh, mm, false);
                timePicker.show();
            }
        });

        clearDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateInMilliseconds = 0;
                dateView.setText("");
                clearDateButton.setVisibility(View.INVISIBLE);
            }
        });

        clearTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeInMilliseconds = 0;
                timeView.setText("");
                clearTimeButton.setVisibility(View.INVISIBLE);
            }
        });

        clearLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskLocationView.setText("");
                clearLocationButton.setVisibility(View.INVISIBLE);
            }
        });

        builder.setView(addTaskView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String taskTitle = taskTitleView.getText().toString();
                String taskDetails = taskDetailView.getText().toString();
                String taskLocation = taskLocationView.getText().toString();
                String taskParticipant = taskParticipantView.getText().toString();

                if (taskTitle != null && !taskTitle.equals((String) null) && !taskTitle.equals("")) {

                    task.setTaskTitle(taskTitle);
                    task.setTaskDetails(taskDetails);
                    task.setLocationName(taskLocation);
                    if (!taskParticipant.equals(null) && !taskParticipant.equals("")) {
                        Participant participant = new Participant();
                        participant.setParticipantName(taskParticipant);
                        List<Participant> participants = new ArrayList<Participant>();
                        participants.add(participant);
                        task.setParticipants(participants);
                    }


                    if (dateInMilliseconds != 0 || timeInMilliseconds != 0) {

                        long milliseconds = 0;

                        if (dateInMilliseconds == 0) {
                            Date date = new Date();
                            milliseconds = date.getTime();
                            milliseconds += timeInMilliseconds;
                        } else if (timeInMilliseconds == 0) {
                            milliseconds = dateInMilliseconds + 86370000;
                        } else {
                            milliseconds = dateInMilliseconds + timeInMilliseconds;
                        }

                        task.setDueDate(String.valueOf(milliseconds));
                    }

                    String taskLocationCoordinates = "";

                    String tz = TimeZone.getDefault().getID();

                    Log.d(TAG, "The timezone id is " + tz);
                    if (taskLocation == null || taskLocation.equals("") && task.getDueDate() == null) {
                        task.setTimeZone(tz);
                    }

                    if (taskLocation != null && !taskLocation.equals("")) {
                        getLocationCoordinates(taskLocation);
                    } else if (!editing) {
                        addTaskToDatabase();
                    } else {
                        updateTask();
                    }

                }

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        ButterKnife.bind(getActivity());

        return builder.create();

    }

    public static TaskFragment newInstance(Task savedTask) {

        if (savedTask == null) {
            task = new Task();
        } else {
            task = savedTask;
        }

        Log.d(TAG, "trying to make a new instance here");
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    private OkHttpClient assignInterceptorWithToken() {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final String token = prefs.getString(SharedPreferencesConstants.PREFS_TOKEN, (String) null);
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
        final String GOOGLE_MAPS_URL = "https://maps.googleapis.com/maps/api/geocode/json?address="
                + location  + "&key=" + "AIzaSyDTlAi9krTLWAoa8vfYH5fmBF2FsBM-QXg";

        Log.d(TAG, "url = " + GOOGLE_MAPS_URL);

        if (isNetworkAvailable() ) {
            Log.d(TAG, "editing? " + editing);
            OkHttpClient okHttpClient = new OkHttpClient();
            okhttp3.Request request = new Request.Builder()
                    .url(GOOGLE_MAPS_URL)
                    .build();
            okhttp3.Call call = okHttpClient.newCall(request);
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e(TAG, e.getMessage());
                    if (!editing) {
                        addTaskToDatabase();
                    } else {
                        updateTask();
                    }

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
                                    String latitude = location.getString("lat");
                                    String longitude = location.getString("lng");
                                    task.setLatitude(location.getDouble("lat"));
                                    task.setLongitude(location.getDouble("lng"));

                                    if (task.getDueDate() != null && !task.getDueDate().equals("")) {
                                        getLocationTimezone(latitude, longitude);
                                    } else if (!editing) {
                                        addTaskToDatabase();
                                    } else {
                                        Log.d(TAG, "editing the task");
                                        updateTask();
                                    }

                                }


                            } else if (!editing) {
                            addTaskToDatabase();
                            } else {
                                updateTask();
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });

        } else if (!editing) {
            addTaskToDatabase();
        } else {
            updateTask();
        }


    }



    public void getLocationTimezone(String lat, String lng) {
        String coordinates = lat + "," + lng;
        long timeInSeconds = (dateInMilliseconds + timeInMilliseconds)/1000;


        final String GOOGLE_TIMEZONE_API = "https://maps.googleapis.com/maps/api/timezone/json?location=" + coordinates
                + "&timestamp=" + timeInSeconds + "&key=" + "AIzaSyA6qL8z3XqZvFa-dVfCKx3dPMP6efxSad8";

        Log.d(TAG, "timezone url = " + GOOGLE_TIMEZONE_API);

        if (isNetworkAvailable() ) {
            OkHttpClient okHttpClient = new OkHttpClient();
            okhttp3.Request request = new Request.Builder()
                    .url(GOOGLE_TIMEZONE_API)
                    .build();
            okhttp3.Call call = okHttpClient.newCall(request);
            call.enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e(TAG, e.getMessage());
                    addTaskToDatabase();
                }
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            JSONObject results = new JSONObject(jsonData);
                            String timeZoneId = results.getString("timeZoneId");
                            task.setTimeZone(timeZoneId);
                        }
                        if (!editing) {
                            addTaskToDatabase();
                        } else {
                            Log.d(TAG, "updating the task");
                            updateTask();
                        }


                    } catch (IOException e) {
                        Log.e(TAG, "Error getting data: " + e.getMessage());

                        if (!editing) {
                            addTaskToDatabase();
                        } else {
                            Log.d(TAG, "updating the task");
                            updateTask();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        if (!editing) {
                            addTaskToDatabase();
                        } else {
                            Log.d(TAG, "updating the task");
                            updateTask();
                        }
                    }
                }
            });
        }
    }

    public void updateTask() {
        client = assignInterceptorWithToken();
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
                Log.v(TAG, "Success editing!");
                if (context instanceof HomeActivity) {
                    ((HomeActivity) context).updateList();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Failure updating!");
            }
        });
    }


    public void addTaskToDatabase() {
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

    public void onDateButtonClicked(View v) {
        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar .get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String date = String.valueOf(monthOfYear+1) + "/" + String.valueOf(dayOfMonth)
                        + "/" + String.valueOf(year);
                TextView dateView = (TextView) addTaskView.findViewById(R.id.newTaskDateValue);
                dateView.setText(date);
            }
        }, yy, mm, dd);
        datePicker.show();
        //DialogFragment newFragment = new DatePickerFragment();
        //newFragment.show(getFragmentManager(), "Date Picker");
    }

    public void updateDate(String date) {
        TextView dateView = (TextView) addTaskView.findViewById(R.id.newTaskDateValue);
        dateView.setText(date);
    }

    public void updateTime(String time) {
        TextView timeView = (TextView) addTaskView.findViewById(R.id.newTaskTimeValue);
        timeView.setText(time);
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
