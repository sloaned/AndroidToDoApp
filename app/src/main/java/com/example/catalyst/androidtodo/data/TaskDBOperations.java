package com.example.catalyst.androidtodo.data;

import android.content.Context;
import android.util.Log;

import com.example.catalyst.androidtodo.activities.HomeActivity;
import com.example.catalyst.androidtodo.models.Task;

import java.util.ArrayList;

/**
 * Created by dsloane on 3/25/2016.
 */
public class TaskDBOperations {

    private Context context;

    public TaskDBOperations(Context context) {
        this.context = context;
    }



    public void deleteTaskLocally(int id) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.deleteTask(id);
        dbHelper.close();
    }


    public void addTaskToLocalDatabase(Task task) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.addTask(task);
        dbHelper.close();
    }

    public void updateTaskLocally(Task task) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.updateTask(task);
        dbHelper.close();

    }

    public ArrayList<Task> getLocalUnsynchedTasks() {
        DBHelper dbHelper = new DBHelper(context);
        ArrayList<Task> tasks = dbHelper.getUnsynchedTasks();
        dbHelper.close();
        return tasks;
    }

    public ArrayList<Task> getTasksDueToday() {
        DBHelper dbHelper = new DBHelper(context);
        ArrayList<Task> tasks = dbHelper.getTasksDueToday();
        dbHelper.close();
        return tasks;
    }

    public ArrayList<Task> getPastDueTasks () {
        DBHelper dbHelper = new DBHelper(context);
        ArrayList<Task> tasks = dbHelper.getPastDueTasks();
        dbHelper.close();
        return tasks;
    }


}
