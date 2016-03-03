package com.example.catalyst.androidtodo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.models.Task;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context mContext;
    private Task[] mTasks;

    public TaskAdapter(Context context, Task[] tasks) {
        mContext = context;
        mTasks = tasks;
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
        holder.bindTask(mTasks[position]);
    }

    @Override
    public int getItemCount() {
        return mTasks.length;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mTaskNameText;

        public TaskViewHolder(View itemView) {
            super(itemView);

            mTaskNameText = (TextView) itemView.findViewById(R.id.taskName);
        }

        public void bindTask(Task task) {
            mTaskNameText.setText(task.getTaskTitle());
        }

        @Override
        public void onClick(View v) {
            String taskName = mTaskNameText.getText().toString();

            Toast.makeText(mContext, taskName, Toast.LENGTH_LONG).show();
        }
    }

}
