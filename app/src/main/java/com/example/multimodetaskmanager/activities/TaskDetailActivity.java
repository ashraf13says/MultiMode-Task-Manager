package com.example.multimodetaskmanager.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.multimodetaskmanager.R;
import com.example.multimodetaskmanager.models.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Set up the toolbar to display a back button and a title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
            getSupportActionBar().setTitle("Task Details");
        }

        TextView titleTextView = findViewById(R.id.task_title);
        TextView descriptionTextView = findViewById(R.id.task_description);
        TextView dueDateTextView = findViewById(R.id.task_due_date);
        // Add other TextViews here if you have them for priority, etc., in your activity_task_detail.xml

        // Get the Task object that was passed from the previous activity via the Intent
        Task task = (Task) getIntent().getSerializableExtra("task");

        // Populate the TextViews with the task's data
        if (task != null) {
            titleTextView.setText(task.getTitle());
            // If the description is empty, display a placeholder message
            descriptionTextView.setText(task.getDescription().isEmpty() ? "No description provided." : task.getDescription());

            // Format and display the due date if it exists
            if (task.getDueDate() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                String dateString = sdf.format(new Date(task.getDueDate()));
                // Displays both due date and priority in one line
                dueDateTextView.setText("Due: " + dateString + " | Priority: " + task.getPriority());
            } else {
                // If no due date, display a different message
                dueDateTextView.setText("No due date | Priority: " + task.getPriority());
            }
        } else {
            // Handle cases where no task object was passed (e.g., an error)
            titleTextView.setText("Error: Task not found.");
            descriptionTextView.setText("");
            dueDateTextView.setText("");
        }
    }

    // Handles the back button in the action bar (top left arrow)
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Closes this activity and returns to the previous one
        return true;
    }
}