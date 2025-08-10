package com.example.multimodetaskmanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.multimodetaskmanager.R;
import com.example.multimodetaskmanager.models.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailFragment extends Fragment {

    private static final String ARG_TASK = "task_object"; // Key for passing the Task object
    private Task currentTask;

    public TaskDetailFragment() {
        // Required empty public constructor for Fragment instantiation
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided Task object.
     * This is the recommended way to pass arguments to a Fragment.
     *
     * @param task The Task object to display. Must be Serializable.
     * @return A new instance of fragment TaskDetailFragment.
     */
    public static TaskDetailFragment newInstance(Task task) {
        TaskDetailFragment fragment = new TaskDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TASK, task); // Put the Task object into arguments
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the Task object from arguments
        if (getArguments() != null) {
            currentTask = (Task) getArguments().getSerializable(ARG_TASK);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        // It reuses `activity_task_detail` layout, which works for both Activity and Fragment.
        View view = inflater.inflate(R.layout.activity_task_detail, container, false);

        // Initialize TextViews from the inflated layout
        TextView titleTextView = view.findViewById(R.id.task_title);
        TextView descriptionTextView = view.findViewById(R.id.task_description);
        TextView dueDateTextView = view.findViewById(R.id.task_due_date);

        // Populate views with currentTask data
        if (currentTask != null) {
            titleTextView.setText(currentTask.getTitle());
            descriptionTextView.setText(currentTask.getDescription());

            if (currentTask.getDueDate() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                String dateString = sdf.format(new Date(currentTask.getDueDate()));
                dueDateTextView.setText("Due: " + dateString);
            } else {
                dueDateTextView.setText("No due date");
            }

            // You might want to add edit/delete buttons/logic here as well if required in the detail view
        } else {
            // Handle case where no task is selected or passed (e.g., in an empty detail pane)
            titleTextView.setText("No task selected");
            descriptionTextView.setText("Tap a task on the left to view its details.");
            dueDateTextView.setText("");
        }

        return view;
    }
}