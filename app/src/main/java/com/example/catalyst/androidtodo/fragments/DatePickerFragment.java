package com.example.catalyst.androidtodo.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import com.example.catalyst.androidtodo.activities.HomeActivity;

import java.util.Calendar;

/**
 * Created by dsloane on 3/10/2016.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public static final String TAG = DatePickerFragment.class.getSimpleName();

    private Context context;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        context = getActivity();
        Log.d(TAG, "context = " + context);

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT, this, year, month, day);

        dpd.setTitle("Set a date");
        return dpd;

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        Log.d(TAG, "just the calendar: " + calendar.toString());
        Log.d(TAG, "calendar time zone: " + calendar.getTimeZone());
        Log.d(TAG, "calendar time = "  + calendar.getTimeInMillis());
        Long timeInMilliseconds = calendar.getTimeInMillis();
        Long timeInSeconds = timeInMilliseconds/1000;
        // 86399000
        Log.d(TAG, "On date set");

    }
}
