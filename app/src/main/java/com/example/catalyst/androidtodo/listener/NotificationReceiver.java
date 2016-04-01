package com.example.catalyst.androidtodo.listener;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.activities.MainActivity;
import com.example.catalyst.androidtodo.data.DBHelper;
import com.example.catalyst.androidtodo.data.TaskDBOperations;
import com.example.catalyst.androidtodo.models.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by dsloane on 3/31/2016.
 */
public class NotificationReceiver extends BroadcastReceiver {

    private TaskDBOperations mTaskDBOperations;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        String dueTodayMessage = "";
        String pastDueMessage = "";
        mTaskDBOperations = new TaskDBOperations(context);


        DBHelper dbHelper = new DBHelper(context);
        ArrayList<Task> dueTasks = dbHelper.getTasksDueToday();
        ArrayList<Task> pastDueTasks = dbHelper.getPastDueTasks();
        dbHelper.close();

        Date date = new Date();
        int ms = (int) ((date.getTime())/1000);

        boolean multiple = false;

        if (dueTasks.size() > 1) {
            multiple = true;
            dueTodayMessage = "You have " + dueTasks.size() + " tasks due today.";
        } else if (dueTasks.size() == 1) {
            multiple = false;
            dueTodayMessage = "Due today: " + dueTasks.get(0).getTaskTitle();
        } else {
            dueTodayMessage = "No tasks due today";
        }

        notify(ms+1, context, dueTodayMessage, multiple);

        if (pastDueTasks.size() > 1) {
            multiple = true;
            pastDueMessage = "You have " + pastDueTasks.size() + " tasks that are past due.";
        } else if (pastDueTasks.size() == 1) {
            multiple = false;
            pastDueMessage = "Past Due: " + pastDueTasks.get(0).getTaskTitle();
        } else {
            pastDueMessage = "No tasks past due";
        }

        notify(ms+2, context, pastDueMessage, multiple);

    }

    public void notify (int id, Context context, String message, boolean multiple) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context).setSmallIcon(R.drawable.refresh)
                        .setContentTitle("To Do Alert")
                .setContentText(message);

        //int notificationId = 001;

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(id, builder.build());


    }

    public void testNotification(Context context) {
        Log.d(TAG, "in testNotification()");
        String dueTodayMessage = "";
        String pastDueMessage = "";
        mTaskDBOperations = new TaskDBOperations(context);


        DBHelper dbHelper = new DBHelper(context);
        ArrayList<Task> dueTasks = dbHelper.getTasksDueToday();
        ArrayList<Task> pastDueTasks = dbHelper.getPastDueTasks();
        dbHelper.close();

        Date date = new Date();
        int ms = (int) ((date.getTime())/1000);

        boolean multiple = false;

        if (dueTasks.size() > 1) {
            multiple = true;
            dueTodayMessage = "You have " + dueTasks.size() + " tasks due today.";
        } else if (dueTasks.size() == 1) {
            multiple = false;
            dueTodayMessage = "Due today: " + dueTasks.get(0).getTaskTitle();
        } else {
            dueTodayMessage = "No tasks due today";
        }

        notify(ms+1, context, dueTodayMessage, multiple);

        if (pastDueTasks.size() > 1) {
            multiple = true;
            pastDueMessage = "You have " + pastDueTasks.size() + " tasks that are past due.";
        } else if (pastDueTasks.size() == 1) {
            multiple = false;
            pastDueMessage = "Past Due: " + pastDueTasks.get(0).getTaskTitle();
        } else {
            pastDueMessage = "No tasks past due";
        }

        notify(ms+2, context, pastDueMessage, multiple);

    }

    public void turnOnNotifications (Context context) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.set(Calendar.HOUR_OF_DAY, 8);

        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
}
