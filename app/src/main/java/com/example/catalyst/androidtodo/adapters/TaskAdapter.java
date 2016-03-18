package com.example.catalyst.androidtodo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.catalyst.androidtodo.R;
import com.example.catalyst.androidtodo.activities.HomeActivity;
import com.example.catalyst.androidtodo.models.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SimpleTimeZone;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;


public class TaskAdapter extends RecyclerSwipeAdapter<TaskAdapter.TaskViewHolder> { //RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context mContext;
    private ArrayList<Task> mTasks = new ArrayList<Task>();

    public static final String TAG = TaskAdapter.class.getSimpleName();

    public TaskAdapter(Context context, ArrayList<Task> tasks) {
        mContext = context;
        mTasks = tasks;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_name_list_item, parent, false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TaskViewHolder holder, final int position) {
        holder.bindTask(mTasks.get(position));

        holder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.swipeLayout.findViewById(R.id.bottom_wrapper));

        holder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
            }

            @Override
            public void onOpen(SwipeLayout layout) {
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
            }

            @Override
            public void onClose(SwipeLayout layout) {
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
            }
        });

        holder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = holder.mTaskNameText.getText().toString();

                Toast.makeText(mContext, taskName, Toast.LENGTH_SHORT).show();

                if (mContext instanceof HomeActivity) {
                    ((HomeActivity) mContext).showTask(holder.mTask);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        SwipeLayout swipeLayout;
        public TextView mTaskNameText;
        public TextView mTaskDetailsText;
        public TextView mTaskDueDateText;
        public TextView mTaskLocation;
        public ImageButton mTaskDeleteBtn;
        public Task mTask;

        public TaskViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            mTaskNameText = (TextView) itemView.findViewById(R.id.taskName);
            mTaskDetailsText = (TextView) itemView.findViewById(R.id.taskDetails);
            mTaskDueDateText = (TextView) itemView.findViewById(R.id.taskDueDate);
            mTaskLocation = (TextView) itemView.findViewById(R.id.taskLocation);
            mTaskDeleteBtn = (ImageButton) itemView.findViewById(R.id.deleteTaskBtn);
        }

        public void bindTask(final Task task) {
            mTask = task;
            mTaskNameText.setText(task.getTaskTitle());

            if (task.getDueDate() != null && !task.getDueDate().equals(null) && !task.getDueDate().equals("")) {

                Log.d(TAG, "in bindTask in the adapter, dueDate not null, = " + task.getDueDate());
                long milliseconds = Long.valueOf(task.getDueDate());
                SimpleDateFormat dt = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy");
                SimpleDateFormat dt1 = new SimpleDateFormat("EEE, MM/dd/yyyy 'at' h:mm aaa");

                Date date = new Date(milliseconds);
                String dateString = date.toString();
                try {
                    date = dt.parse(dateString);
                } catch (ParseException e) {
                    Log.e(TAG, "date parsing error: " + e.getMessage());
                }
                Log.d(TAG, "date = " + date);
                dateString = dt1.format(date);
                mTaskDueDateText.setText(dateString);
            } else {
                mTaskDueDateText.setText("");
            }

            if (task.getTaskDetails() != null && !task.getTaskDetails().equals(null) && !task.getTaskDetails().equals("")) {
                mTaskDetailsText.setText(task.getTaskDetails());
            }

            if (task.getLocationName() != null && !task.getLocationName().equals(null) && !task.getLocationName().equals("")) {
                mTaskLocation.setText(task.getLocationName());
            } else {
                mTaskLocation.setText("");
            }

            mTaskDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mContext instanceof HomeActivity) {
                        ((HomeActivity) mContext).deleteTask(task.getId());

                    }

                }
            });


        }
        /*
        @Override
        public void onClick(View v) {
            String taskName = mTaskNameText.getText().toString();

            Toast.makeText(mContext, taskName, Toast.LENGTH_SHORT).show();
        } */
    }




}
