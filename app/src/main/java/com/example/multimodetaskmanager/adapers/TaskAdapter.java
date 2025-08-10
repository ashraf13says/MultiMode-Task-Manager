package com.example.multimodetaskmanager.adapters;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.multimodetaskmanager.R;
import com.example.multimodetaskmanager.models.Task;
import com.example.multimodetaskmanager.fragments.TaskListFragment; // <--- This import is necessary for context menu setup
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    // Interface to define actions that the hosting fragment/activity can perform on a task
    public interface OnTaskActionListener {
        void onEdit(Task task);
        void onDelete(Task task);
        void onMarkDone(Task task);
        void onTaskClick(Task task); // For regular item click (e.g., to open detail view)
    }

    private List<Task> taskList;
    private final OnTaskActionListener listener; // The listener (usually TaskListFragment)

    public TaskAdapter(List<Task> taskList, OnTaskActionListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    // Method to update the list, typically called after DiffUtil calculations
    public void setTaskList(List<Task> newTaskList) {
        this.taskList = newTaskList;
    }

    // Getter for the current list of tasks, used by DiffUtil
    public List<Task> getTaskList() {
        return taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates the layout for a single task item (item_task.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.title.setText(task.getTitle());
        holder.priority.setText("Priority: " + task.getPriority());

        // Apply strikethrough effect and change color for tasks marked as done
        if (task.isDone()) {
            holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.priority.setPaintFlags(holder.priority.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.title.setTextColor(Color.GRAY);
            holder.priority.setTextColor(Color.GRAY);
        } else {
            // Remove strikethrough and restore original colors for pending tasks
            holder.title.setPaintFlags(holder.title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.priority.setPaintFlags(holder.priority.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.title.setTextColor(holder.initialTitleColor);
            holder.priority.setTextColor(holder.initialPriorityColor);
        }

        // --- Popup Menu on ImageButton Click (for options like edit, delete, mark done) ---
        holder.options.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(v.getContext(), v);
            menu.inflate(R.menu.context_menu); // Inflates the context menu XML for the popup

            // Dynamically change "Mark as Done" to "Mark as Pending" if the task is already done
            MenuItem markDoneItem = menu.getMenu().findItem(R.id.menu_done);
            if (markDoneItem != null) {
                if (task.isDone()) {
                    markDoneItem.setTitle("Mark as Pending");
                } else {
                    markDoneItem.setTitle("Mark as Done");
                }
            }

            // Set a listener for menu item clicks within the popup
            menu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_edit) {
                    listener.onEdit(task);
                    return true;
                } else if (id == R.id.menu_delete) {
                    listener.onDelete(task);
                    return true;
                } else if (id == R.id.menu_done) {
                    listener.onMarkDone(task);
                    return true;
                }
                return false;
            });
            menu.show(); // Display the popup menu
        });

        // --- Handle overall item click (for navigating to detail view) ---
        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));

        // --- Setup for Context Menu (Long Press on entire item) ---
        // This makes the itemView respond to long-press by triggering onCreateContextMenu in the fragment.
        // We cast the listener to TaskListFragment to directly set its fields for context menu handling.
        holder.itemView.setOnLongClickListener(v -> {
            ((TaskListFragment) listener).selectedTaskForContextMenu = task; // Store the clicked task
            ((TaskListFragment) listener).selectedTaskPositionForContextMenu = holder.getAdapterPosition(); // Store its position
            v.showContextMenu(); // Show the context menu
            return true; // Indicate that the long click was consumed
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size(); // Returns the total number of tasks in the list
    }

    // ViewHolder class to hold references to the views for each task item
    static class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView title, priority;
        ImageButton options;
        int initialTitleColor; // To store and restore original text colors
        int initialPriorityColor;

        TaskViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.task_title);
            priority = itemView.findViewById(R.id.task_priority);
            options = itemView.findViewById(R.id.task_options);

            // Store the initial text colors when the ViewHolder is created
            initialTitleColor = title.getCurrentTextColor();
            initialPriorityColor = priority.getCurrentTextColor();

            // Register the entire item view for context menu.
            // This tells Android that long-pressing this view should trigger a context menu.
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            // This method in ViewHolder is less commonly used for RecyclerView item context menus directly
            // because context menu creation often needs data from the adapter.
            // The approach in onBindViewHolder where we set an OnLongClickListener and
            // then set properties on the fragment and call showContextMenu() is more common for RecyclerViews
            // for passing specific item data to the fragment's context menu handler.
        }
    }
}