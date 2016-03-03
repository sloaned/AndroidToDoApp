package com.example.catalyst.androidtodo.activities;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.adapters.TaskAdapter;
import com.example.catalyst.androidtodo.data.TaskContract;
import com.example.catalyst.androidtodo.models.Task;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private Task[] mTasks;

    public static final String TAG = MainActivity.class.getSimpleName();
   // private static final String API = "http://pc30120.catalystsolves.com:8080/";

    @Bind(R.id.taskRecyclerView)RecyclerView mTaskListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SQLiteDatabase taskDatabase = openOrCreateDatabase(TaskContract.DATABASE_NAME, MODE_PRIVATE, null);

        TaskAdapter adapter = new TaskAdapter(this, mTasks);
        mTaskListView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mTaskListView.setLayoutManager(layoutManager);

        mTaskListView.setHasFixedSize(true);

    }

    private void getTasks() {

    }


}
