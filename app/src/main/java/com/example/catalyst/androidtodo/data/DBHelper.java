package com.example.catalyst.androidtodo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.catalyst.androidtodo.models.Task;

import java.util.ArrayList;

/**
 * Created by dsloane on 3/2/2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    private Context mContext;

    final String SQL_CREATE_TASK_TABLE = "CREATE TABLE IF NOT EXISTS " + TaskContract.TaskEntry.TABLE_NAME + " (" +
            TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TaskContract.TaskEntry.COLUMN_TASK_TITLE + " REAL NOT NULL, " +
            TaskContract.TaskEntry.COLUMN_TASK_DETAILS + " TEXT, " +
            TaskContract.TaskEntry.COLUMN_DUE_DATE + " REAL, " +
            TaskContract.TaskEntry.COLUMN_LOCATION_NAME + " REAL, " +
            TaskContract.TaskEntry.COLUMN_LOCATION_COORDINATES + " TEXT)";

    public DBHelper(Context context) {
        super(context, TaskContract.DATABASE_NAME, null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TASK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskContract.TaskEntry.TABLE_NAME);
        onCreate(db);
    }

    public boolean addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(TaskContract.TaskEntry.COLUMN_TASK_TITLE, task.getTaskTitle());
        contentValues.put(TaskContract.TaskEntry.COLUMN_TASK_DETAILS, task.getTaskDetails());
        contentValues.put(TaskContract.TaskEntry.COLUMN_DUE_DATE, task.getDueDate());
        contentValues.put(TaskContract.TaskEntry.COLUMN_LOCATION_NAME, task.getLocationName());
        String coordinates = task.getLatitude() + " " + task.getLongitude();
        contentValues.put(TaskContract.TaskEntry.COLUMN_LOCATION_COORDINATES, coordinates);

        db.insert(TaskContract.TaskEntry.TABLE_NAME, null, contentValues);
        db.close();
        return true;
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

            taskList.add(task);
            res.moveToNext();
        }
        res.close();
        db.close();
        return taskList;
    }
}
