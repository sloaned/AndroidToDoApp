package com.example.catalyst.androidtodo.activities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.adapters.TaskAdapter;
import com.example.catalyst.androidtodo.data.TaskContract;
import com.example.catalyst.androidtodo.models.Task;
import com.example.catalyst.androidtodo.network.ApiCaller;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Task> mTasks = new ArrayList<Task>();

    public static final String TAG = MainActivity.class.getSimpleName();
   // private static final String API = "http://pc30120.catalystsolves.com:8080/";

    @Bind(R.id.taskRecyclerView)RecyclerView mTaskListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        // SQLiteDatabase taskDatabase = openOrCreateDatabase(TaskContract.DATABASE_NAME, MODE_PRIVATE, null);

        TaskAdapter adapter = new TaskAdapter(this, mTasks);
        mTaskListView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mTaskListView.setLayoutManager(layoutManager);

        mTaskListView.setHasFixedSize(true);


        new ApiCaller(this).loginUserAndGetToken();

    }

    private byte[] getFileDataFromDrawable(Context context, int id){
        Drawable drawable = ContextCompat.getDrawable(context, id);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


}
