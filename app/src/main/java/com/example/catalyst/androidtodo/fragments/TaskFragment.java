package com.example.catalyst.androidtodo.fragments;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.activities.HomeActivity;
import com.example.catalyst.androidtodo.models.Participant;
import com.example.catalyst.androidtodo.models.Task;
import com.example.catalyst.androidtodo.network.RetrofitInterfaces.ITask;
import com.example.catalyst.androidtodo.util.ContactsConstants;
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
import java.util.GregorianCalendar;
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
public class TaskFragment extends DialogFragment implements ContactFragment.ContactClickListener {

    public static final String TAG = TaskFragment.class.getSimpleName();

    private static final String BASE_URL = "http://pc30120.catalystsolves.com:8080/";

    private static Task task;
    private long dateInMilliseconds = 0;
    private long timeInMilliseconds = -1;

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
    private Button addParticipantButton;
    private LinearLayout taskParticipantLayout;

    private boolean editing = false;
    private int participantNumber = 0;

   // private ArrayList<String> participantMatches = new ArrayList<String>();

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
       // taskParticipantView = (EditText) addTaskView.findViewById(R.id.newTaskParticipantsValue);
        taskParticipantLayout = (LinearLayout) addTaskView.findViewById(R.id.newTaskParticipantsLayout);
        addParticipantButton = (Button) addTaskView.findViewById(R.id.newTaskParticipantButton);

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
                timeInMilliseconds = milliseconds % 86400000;
                dateInMilliseconds = milliseconds - timeInMilliseconds;

                String theDate = dateFormat.format(date);
                String theTime = timeFormat.format(date);
                dateView.setText(theDate);
                clearDateButton.setVisibility(View.VISIBLE);
                timeView.setText(theTime);
                clearTimeButton.setVisibility(View.VISIBLE);
            }
            if (task.getParticipants().size() > 0) {

                for (int i = 0; i < task.getParticipants().size(); i++) {
                    String name = task.getParticipants().get(i).getParticipantName();
                    addParticipantView(name);
                }
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

        addParticipantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addParticipantView("");
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

                //TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
                TimeZone timeZone = TimeZone.getDefault();
                calendar.setTimeZone(timeZone);

                int hh = calendar.get(Calendar.HOUR_OF_DAY);
                int mm = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        long milliSeconds = (hour * 3600 * 1000) + (minute * 60 * 1000);
                        timeInMilliseconds = milliSeconds;
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
                timeInMilliseconds = -1;
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

                if (taskTitle != null && !taskTitle.equals((String) null) && !taskTitle.equals("")) {

                    task.setTaskTitle(taskTitle);
                    task.setTaskDetails(taskDetails);
                    task.setLocationName(taskLocation);

                    if (participantNumber > 0) {
                        Log.d(TAG, "participantNumber = " + participantNumber);
                        List<Participant> participants = new ArrayList<Participant>();

                        for (int i = 0; i < participantNumber; i++) {
                            EditText editText = (EditText) addTaskView.findViewById(i);
                            String taskParticipant = editText.getText().toString();
                            Log.d(TAG, "participant name = " + taskParticipant);

                            if (!taskParticipant.equals(null) && !taskParticipant.equals("")) {
                                Participant participant = new Participant();
                                participant.setParticipantName(taskParticipant);
                                participants.add(participant);
                            }
                        }
                        task.setParticipants(participants);
                    }

                    if (dateInMilliseconds != 0 || timeInMilliseconds != -1) {

                        long milliseconds = 0;

                        if (dateInMilliseconds == 0) {
                            Calendar calStart = new GregorianCalendar();
                            calStart.setTime(new Date());
                            calStart.set(Calendar.HOUR_OF_DAY, 0);
                            calStart.set(Calendar.MINUTE, 0);
                            calStart.set(Calendar.SECOND, 0);
                            calStart.set(Calendar.MILLISECOND, 0);
                            Date midnightYesterday = calStart.getTime();
                            Log.d(TAG, "midnight yesterday time = " + midnightYesterday.getTime());
                            //Date date = new Date();
                            milliseconds = midnightYesterday.getTime();
                            Log.d(TAG, "date.getTime() gives us " + milliseconds);
                            milliseconds += timeInMilliseconds;
                        } else if (timeInMilliseconds == -1) {
                            milliseconds = dateInMilliseconds + 86370000;
                        } else {
                            milliseconds = dateInMilliseconds + timeInMilliseconds;
                        }

                        task.setDueDate(String.valueOf(milliseconds));
                    } else {
                        Log.d(TAG, "dateInMilliseconds = " + dateInMilliseconds + ", timeInMilliseconds = " + timeInMilliseconds);
                        task.setDueDate(null);
                    }

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

                    if (context instanceof HomeActivity) {
                        ((HomeActivity) context).getAllTasks();
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

    public void addParticipantView(String name) {
        final LinearLayout layout = new LinearLayout(getActivity());

        final ArrayList<String> participantMatches = getContactList();
        layout.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, participantMatches);
        final AutoCompleteTextView editText = new AutoCompleteTextView(getActivity());
        editText.setAdapter(adapter);
        editText.setLayoutParams(new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT
        ));
        editText.setTextSize(16);
        editText.setHint("Participant name");
        editText.setText(name);
        editText.setId(participantNumber);

        final Button contactButton = new Button(getActivity());
        contactButton.setLayoutParams(new ActionBar.LayoutParams(70, 70));
        contactButton.setGravity(View.TEXT_ALIGNMENT_GRAVITY);
        contactButton.setTextSize(12);
        contactButton.setText("...");
        contactButton.setId(participantNumber + R.string.show_contacts);
        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> names = getContactList();

                DialogFragment dialog = ContactFragment.newInstance(names, editText.getId());

                if (dialog.getDialog() != null) {
                    dialog.getDialog().setCanceledOnTouchOutside(true);
                }
                dialog.setTargetFragment(TaskFragment.this, 0);
                dialog.show(getFragmentManager(), "dialog");
            }
        });

        final Button button = new Button(getActivity());
        button.setLayoutParams(new ActionBar.LayoutParams(45, 45));
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setBackgroundResource(R.drawable.clearx);
        button.setId(participantNumber + R.string.remove_participant);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int id = editText.getId();

                layout.removeView(editText);
                layout.removeView(button);
                taskParticipantLayout.removeView(layout);

                for (int i = id + 1; i < participantNumber; i++) {

                    EditText editText1 = (EditText) addTaskView.findViewById(i);
                    editText1.setId(i - 1);
                    Button button1 = (Button) addTaskView.findViewById(i + R.string.remove_participant);
                    button1.setId((i - 1) + R.string.remove_participant);
                }
                participantNumber--;
            }
        });


        participantNumber++;

        layout.addView(editText);
        layout.addView(contactButton);
        layout.addView(button);

        taskParticipantLayout.addView(layout);
        final ScrollView scrollView = (ScrollView) addTaskView.findViewById(R.id.scrollView);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void assignParticipant(String name, int id) {
        AutoCompleteTextView editText = (AutoCompleteTextView) addTaskView.findViewById(id);
        editText.setText(name);
    }


    public ArrayList<String> getContactList() {

        ArrayList<String> contacts = new ArrayList<String>();

        Cursor phones = null;

        try {
            phones = getActivity().getContentResolver().query(ContactsContract.Data.CONTENT_URI, ContactsConstants.PROJECTION, null, null, null);

            if (phones.moveToFirst()) {
                Log.d(TAG, "got something in phones");
                do {
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                    contacts.add(name);
                } while (phones.moveToNext());
            } else {
                Log.d(TAG, "didn't get nuthin");
            }
            phones.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (phones != null) {
                phones.close();
            }
        }

        return contacts;
    }


    public ArrayList<String> searchContactList(String searchTerm) {
        searchTerm = "%" + searchTerm + "%";
        String[] selectionArgs = {searchTerm};

        ArrayList<String> contacts = new ArrayList<String>();

        Cursor phones = null;

        try {
            phones = getActivity().getContentResolver().query(ContactsContract.Data.CONTENT_URI, ContactsConstants.PROJECTION, ContactsConstants.SEARCH_SELECTION, selectionArgs, null);

            if (phones.moveToFirst()) {
                Log.d(TAG, "got something in phones");
                do {
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                    contacts.add(name);
                } while (phones.moveToNext());
            } else {
                Log.d(TAG, "didn't get nuthin");
            }
            phones.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (phones != null) {
                phones.close();
            }
        }

        return contacts;
    }




}
