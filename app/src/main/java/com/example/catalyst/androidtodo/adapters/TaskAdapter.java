package com.example.catalyst.androidtodo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.models.Task;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context mContext;
    private ArrayList<Task> mTasks = new ArrayList<Task>();

    public static final String TAG = TaskAdapter.class.getSimpleName();

    public TaskAdapter(Context context, ArrayList<Task> tasks) {
        mContext = context;
        mTasks = tasks;
        Log.d(TAG, "Here are the tasks: ");
        for (Task task : tasks) {
            Log.d(TAG, task.getTaskTitle());
        }
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_name_list_item, parent, false);

        TaskViewHolder viewHolder = new TaskViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        holder.bindTask(mTasks.get(position));
    }

    @Override
    public int getItemCount() {

        Log.d(TAG, "size = " + mTasks.size());
        return mTasks.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mTaskNameText;
        public TextView mTaskDetailsText;
        public TextView mTaskDueDateText;
        public TextView mTaskLocation;

        public TaskViewHolder(View itemView) {
            super(itemView);

            mTaskNameText = (TextView) itemView.findViewById(R.id.taskName);
            mTaskDetailsText = (TextView) itemView.findViewById(R.id.taskDetails);
            mTaskDueDateText = (TextView) itemView.findViewById(R.id.taskDueDate);
            mTaskLocation = (TextView) itemView.findViewById(R.id.taskLocation);
        }

        public void bindTask(Task task) {
            mTaskNameText.setText(task.getTaskTitle());
            mTaskDetailsText.setText(task.getTaskDetails());
            mTaskDueDateText.setText(task.getDueDate());
            mTaskLocation.setText(task.getLocationName());
        }

        @Override
        public void onClick(View v) {
            String taskName = mTaskNameText.getText().toString();

            Toast.makeText(mContext, taskName, Toast.LENGTH_LONG).show();
        }
    }

}
