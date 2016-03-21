package com.example.catalyst.androidtodo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Telephony;
import android.util.Log;

import com.example.catalyst.androidtodo.models.Participant;
import com.example.catalyst.androidtodo.models.Task;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dsloane on 3/2/2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    private final String TAG = getClass().getSimpleName();

    private Context mContext;

    final String SQL_CREATE_TASK_TABLE = "CREATE TABLE IF NOT EXISTS " + TaskContract.TaskEntry.TABLE_NAME + " (" +
            TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TaskContract.TaskEntry.COLUMN_SERVER_ID + " INTEGER, " +
            TaskContract.TaskEntry.COLUMN_TASK_TITLE + " REAL NOT NULL, " +
            TaskContract.TaskEntry.COLUMN_TASK_DETAILS + " TEXT, " +
            TaskContract.TaskEntry.COLUMN_DUE_DATE + " REAL, " +
            TaskContract.TaskEntry.COLUMN_TIMEZONE + " TEXT, " +
            TaskContract.TaskEntry.COLUMN_LOCATION_NAME + " REAL, " +
            TaskContract.TaskEntry.COLUMN_LATITUDE + " TEXT, " +
            TaskContract.TaskEntry.COLUMN_LONGITUDE + " TEXT," +
            TaskContract.TaskEntry.COLUMN_LAST_MODIFIED_DATE +  " REAL, " +
            TaskContract.TaskEntry.COLUMN_SYNC_DATE + " REAL)";

    final String SQL_CREATE_PARTICIPANT_TABLE = "CREATE TABLE IF NOT EXISTS " + ParticipantContract.ParticipantEntry.TABLE_NAME + " (" +
            ParticipantContract.ParticipantEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ParticipantContract.ParticipantEntry.COLUMN_PARTICIPANT_NAME + " REAL NOT NULL)";

    final String SQL_CREATE_TASK_PARTICIPANT_TABLE = "CREATE TABLE IF NOT EXISTS " + Task_ParticipantContract.Task_ParticipantEntry.TABLE_NAME + " (" +
            Task_ParticipantContract.Task_ParticipantEntry.COLUMN_PARTICIPANT_ID + " INTEGER NOT NULL, " +
            Task_ParticipantContract.Task_ParticipantEntry.COLUMN_TASK_ID + " INTGER NOT NULL)";


    public DBHelper(Context context) {
        super(context, TaskContract.DATABASE_NAME, null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TASK_TABLE);
        db.execSQL(SQL_CREATE_PARTICIPANT_TABLE);
        db.execSQL(SQL_CREATE_TASK_PARTICIPANT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskContract.TaskEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ParticipantContract.ParticipantEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Task_ParticipantContract.Task_ParticipantEntry.TABLE_NAME);
        onCreate(db);
    }


    public boolean updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(TaskContract.TaskEntry.COLUMN_TASK_TITLE, task.getTaskTitle());
        contentValues.put(TaskContract.TaskEntry.COLUMN_TASK_DETAILS, task.getTaskDetails());
        contentValues.put(TaskContract.TaskEntry.COLUMN_DUE_DATE, task.getDueDate());
        contentValues.put(TaskContract.TaskEntry.COLUMN_LOCATION_NAME, task.getLocationName());
        contentValues.put(TaskContract.TaskEntry.COLUMN_LATITUDE, task.getLatitude());
        contentValues.put(TaskContract.TaskEntry.COLUMN_LONGITUDE, task.getLongitude());
        contentValues.put(TaskContract.TaskEntry.COLUMN_LAST_MODIFIED_DATE, task.getLastModifiedDate());
        contentValues.put(TaskContract.TaskEntry.COLUMN_SYNC_DATE, task.getSyncDate());
        contentValues.put(TaskContract.TaskEntry.COLUMN_SERVER_ID, task.getServerId());
        contentValues.put(TaskContract.TaskEntry.COLUMN_TIMEZONE, task.getTimeZone());



        db.update(TaskContract.TaskEntry.TABLE_NAME, contentValues, TaskContract.TaskEntry._ID + " = ? ", new String[]{Integer.toString(task.getId())});

        deleteTaskParticipants(task.getId());

        for (Participant p : task.getParticipants()) {
            if(!doesParticipantExist(p.getParticipantName())) {
                addParticipant(p);
            }
            int id = getParticipantId(p.getParticipantName());
            if (id >= 0) {
                addTaskParticipant(task.getId(), id);
            }
        }
        db.close();
        return true;
    }


    public boolean addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(TaskContract.TaskEntry.COLUMN_TASK_TITLE, task.getTaskTitle());
        contentValues.put(TaskContract.TaskEntry.COLUMN_TASK_DETAILS, task.getTaskDetails());
        contentValues.put(TaskContract.TaskEntry.COLUMN_DUE_DATE, task.getDueDate());
        contentValues.put(TaskContract.TaskEntry.COLUMN_LOCATION_NAME, task.getLocationName());
        contentValues.put(TaskContract.TaskEntry.COLUMN_LATITUDE, task.getLatitude());
        contentValues.put(TaskContract.TaskEntry.COLUMN_LONGITUDE, task.getLongitude());
        contentValues.put(TaskContract.TaskEntry.COLUMN_LAST_MODIFIED_DATE, task.getLastModifiedDate());
        contentValues.put(TaskContract.TaskEntry.COLUMN_SYNC_DATE, task.getSyncDate());

        contentValues.put(TaskContract.TaskEntry.COLUMN_SERVER_ID, task.getServerId());
        contentValues.put(TaskContract.TaskEntry.COLUMN_TIMEZONE, task.getTimeZone());

        db.insert(TaskContract.TaskEntry.TABLE_NAME, null, contentValues);

        for (Participant p : task.getParticipants()) {
            if(!doesParticipantExist(p.getParticipantName())) {
                addParticipant(p);
            }
            int id = getParticipantId(p.getParticipantName());
            if (id >= 0) {
                addTaskParticipant(task.getId(), id);
            }
        }
        db.close();
        return true;
    }

    public void deleteTaskParticipants(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Task_ParticipantContract.Task_ParticipantEntry.TABLE_NAME,
                Task_ParticipantContract.Task_ParticipantEntry.COLUMN_TASK_ID + " = ? ",
                new String[] { Integer.toString(taskId) });
        db.close();
    }

    public int getParticipantId(String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery("SELECT * FROM " + ParticipantContract.ParticipantEntry.TABLE_NAME + " WHERE " + ParticipantContract.ParticipantEntry.COLUMN_PARTICIPANT_NAME
            + " = '" + name + "'", null);
        if (res != null) {
            res.moveToFirst();
            int id = res.getInt(res.getColumnIndex(ParticipantContract.ParticipantEntry._ID));
            return id;
        }
        return -1;

    }

    public boolean addTaskParticipant(int taskId, int participantId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(Task_ParticipantContract.Task_ParticipantEntry.COLUMN_PARTICIPANT_ID, participantId);
        contentValues.put(Task_ParticipantContract.Task_ParticipantEntry.COLUMN_TASK_ID, taskId);

        db.insert(Task_ParticipantContract.Task_ParticipantEntry.TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public boolean addParticipant(Participant participant) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(ParticipantContract.ParticipantEntry.COLUMN_PARTICIPANT_NAME, participant.getParticipantName());

        db.insert(ParticipantContract.ParticipantEntry.TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public boolean doesParticipantExist(String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery("SELECT * FROM " + ParticipantContract.ParticipantEntry.TABLE_NAME + " WHERE " + ParticipantContract.ParticipantEntry.COLUMN_PARTICIPANT_NAME + " = '" + name + "'", null);
        if (res != null && res.moveToFirst()) {

            res.close();
            db.close();
            return true;
        }
        res.close();
        db.close();
        return false;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TaskContract.TaskEntry.TABLE_NAME);
        db.close();
        return numRows;
    }

    public Integer deleteTask(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TaskContract.TaskEntry.TABLE_NAME,
                TaskContract.TaskEntry._ID + " = ? ",
                new String[] { Integer.toString(id) } );
    }

    public ArrayList<Task> getAllTasks() {
        ArrayList<Task> taskList = new ArrayList<Task>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME + "", null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            Task task = new Task();
            task.setTaskTitle(res.getString(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_TITLE)));
            task.setTaskDetails(res.getString(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_DETAILS)));
            task.setDueDate(res.getString(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_DATE)));
            task.setLocationName(res.getString(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_LOCATION_NAME)));

            int id = res.getInt(res.getColumnIndex(TaskContract.TaskEntry._ID));
            List<Participant> participants = getTaskParticipants(id);

            task.setParticipants(participants);

            taskList.add(task);
            res.moveToNext();
        }
        res.close();
        db.close();
        return taskList;
    }

    public ArrayList<Task> getUnsynchedTasks() {
        ArrayList<Task> taskList = new ArrayList<Task>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME + " WHERE " + TaskContract.TaskEntry.COLUMN_LAST_MODIFIED_DATE + " > " + TaskContract.TaskEntry.COLUMN_SYNC_DATE + "", null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            Task task = new Task();

            Log.v(TAG, "unsynched task name = " + res.getString(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_TITLE)));
            task.setTaskTitle(res.getString(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_TITLE)));
            task.setTaskDetails(res.getString(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_DETAILS)));
            task.setDueDate(res.getString(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_DATE)));
            task.setLocationName(res.getString(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_LOCATION_NAME)));
            task.setServerId(res.getInt(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_SERVER_ID)));
            task.setLastModifiedDate(res.getLong(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_LAST_MODIFIED_DATE)));
            task.setLatitude(res.getDouble(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_LATITUDE)));
            task.setLongitude(res.getDouble(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_LONGITUDE)));
            task.setTimeZone(res.getString(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_TIMEZONE)));


            int id = res.getInt(res.getColumnIndex(TaskContract.TaskEntry._ID));
            List<Participant> participants = getTaskParticipants(id);

            task.setParticipants(participants);

            taskList.add(task);
            res.moveToNext();
        }
        res.close();
        db.close();
        return taskList;
    }

    public Participant getParticipantById(int id) {
        Participant participant = new Participant();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + ParticipantContract.ParticipantEntry.TABLE_NAME + " WHERE " + ParticipantContract.ParticipantEntry._ID + " = " + id + "", null);
        if (res != null) {
            res.moveToFirst();

            participant.setParticipantName(res.getString(res.getColumnIndex(ParticipantContract.ParticipantEntry.COLUMN_PARTICIPANT_NAME)));
        }
        res.close();
        db.close();
        return participant;
    }

    public ArrayList<Integer> getParticipantIds(int id) {
        ArrayList<Integer> participantIds = new ArrayList<Integer>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + Task_ParticipantContract.Task_ParticipantEntry.TABLE_NAME + " WHERE " + Task_ParticipantContract.Task_ParticipantEntry.COLUMN_TASK_ID + " = " + id + "", null);

        if (res != null) {
            res.moveToFirst();

            while (res.moveToNext() != false) {
                int idInt = res.getInt(res.getColumnIndex(Task_ParticipantContract.Task_ParticipantEntry.COLUMN_PARTICIPANT_ID));
                participantIds.add(idInt);
            }

        }

        res.close();
        db.close();
        return participantIds;
    }

    public boolean doesTaskExist(int serverId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME + " WHERE " + TaskContract.TaskEntry.COLUMN_SERVER_ID + " = " + serverId + "", null);
        if (res != null && res.moveToFirst()) {
            res.close();
            db.close();
            return true;
        }
        res.close();
        db.close();
        return false;
    }

    public long getLMDByServerId(int serverId) {
       long lmd = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME + " WHERE " + TaskContract.TaskEntry.COLUMN_SERVER_ID + " = " + serverId + "", null);
        if (res != null && res.moveToFirst()) {
            lmd = res.getLong(res.getColumnIndex(TaskContract.TaskEntry.COLUMN_LAST_MODIFIED_DATE));
        }
        res.close();
        db.close();
        return lmd;
    }

    public ArrayList<Participant> getTaskParticipants(int id) {
        ArrayList<Participant> participants = new ArrayList<Participant>();

        ArrayList<Integer> participantIds = getParticipantIds(id);

        for (int i : participantIds) {
            Participant p = getParticipantById(i);
            participants.add(p);
        }

        return participants;
    }
}
