// MultiModeTaskManager/app/src/main/java/com/example/multimodetaskmanager/fragments/TaskDialogFragment.java
package com.example.multimodetaskmanager.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment; // Import Fragment

import com.example.multimodetaskmanager.R;
import com.example.multimodetaskmanager.models.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TaskDialogFragment extends DialogFragment {

    // Interface for communicating task additions/updates back to the host
    public interface OnTaskDialogListener {
        void onTaskAdded(Task task);
        void onTaskUpdated(Task task);
    }

    private OnTaskDialogListener listener; // Reference to the listener
    private Task currentTask; // The task being edited (null if adding a new task)

    private EditText etTitle, etDescription;
    private Spinner spPriority;
    private TextView tvDueDate;
    private ImageButton btnSelectDate;
    private Calendar selectedCalendar; // Stores the selected due date

    /**
     * Factory method to create a new instance of the dialog.
     * Use this method to pass arguments (like a Task object for editing).
     *
     * @param task The Task to edit, or null if adding a new task.
     * @return A new instance of TaskDialogFragment.
     */
    public static TaskDialogFragment newInstance(Task task) {
        TaskDialogFragment fragment = new TaskDialogFragment();
        Bundle args = new Bundle();
        if (task != null) {
            args.putSerializable("task", task); // Task must be Serializable to be passed in Bundle
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Attempts to find an OnTaskDialogListener in the following order of priority:
        // 1. Target Fragment (if set by `setTargetFragment()`)
        // 2. Parent Fragment (if this dialog is nested within another fragment)
        // 3. Hosting Activity
        Fragment targetFragment = getTargetFragment();
        if (targetFragment instanceof OnTaskDialogListener) {
            listener = (OnTaskDialogListener) targetFragment;
        }
        else if (getParentFragment() instanceof OnTaskDialogListener) {
            listener = (OnTaskDialogListener) getParentFragment();
        }
        else if (context instanceof OnTaskDialogListener) {
            listener = (OnTaskDialogListener) context;
        }
        // If still no listener found, it will remain null.
        // The saveTask() method will then show a Toast instead of crashing if listener is null.
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the task object if it was passed as an argument (for editing)
        if (getArguments() != null) {
            currentTask = (Task) getArguments().getSerializable("task");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_task_input, null); // Inflate the dialog's layout
        builder.setView(view); // Set the inflated view to the AlertDialog

        // Initialize UI components from the dialog's layout
        etTitle = view.findViewById(R.id.edit_text_task_title);
        etDescription = view.findViewById(R.id.edit_text_task_description);
        spPriority = view.findViewById(R.id.spinner_task_priority);
        tvDueDate = view.findViewById(R.id.text_view_due_date);
        btnSelectDate = view.findViewById(R.id.button_select_date);
        Button btnSave = view.findViewById(R.id.button_save);
        Button btnCancel = view.findViewById(R.id.button_cancel);

        // Set up the spinner with priority options from resources
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.priority_options, // Array of priorities defined in strings.xml
                android.R.layout.simple_spinner_item // Default spinner item layout
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(adapter);

        selectedCalendar = Calendar.getInstance(); // Initialize calendar to current date/time

        // Populate fields if editing an existing task
        if (currentTask != null) {
            builder.setTitle("Edit Task"); // Change dialog title for editing
            etTitle.setText(currentTask.getTitle());
            etDescription.setText(currentTask.getDescription());
            // Set spinner selection based on task's priority
            String[] priorities = getResources().getStringArray(R.array.priority_options);
            for (int i = 0; i < priorities.length; i++) {
                if (priorities[i].equals(currentTask.getPriority())) {
                    spPriority.setSelection(i);
                    break;
                }
            }
            // Set due date if it exists
            if (currentTask.getDueDate() > 0) {
                selectedCalendar.setTimeInMillis(currentTask.getDueDate());
                updateDueDateTextView(); // Update TextView to show the date
            } else {
                tvDueDate.setText("No due date selected");
            }
            btnSave.setText("Update"); // Change button text to "Update"
        } else {
            builder.setTitle("Add New Task"); // Default title for adding
            tvDueDate.setText("No due date selected");
            btnSave.setText("Add"); // Default button text to "Add"
        }

        // Set click listeners for buttons
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveTask());
        btnCancel.setOnClickListener(v -> dismiss()); // Closes the dialog

        return builder.create(); // Create and return the AlertDialog
    }

    // Shows a DatePickerDialog to allow the user to select a due date
    private void showDatePicker() {
        int year = selectedCalendar.get(Calendar.YEAR);
        int month = selectedCalendar.get(Calendar.MONTH);
        int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    // When date is selected, update the calendar and TextView
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth);
                    updateDueDateTextView();
                },
                year, month, day);
        datePickerDialog.show();
    }

    // Updates the TextView that displays the selected due date
    private void updateDueDateTextView() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvDueDate.setText(sdf.format(selectedCalendar.getTime()));
    }

    // Handles saving or updating the task based on input
    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priority = spPriority.getSelectedItem().toString();
        // Determine due date: 0L if no date selected, otherwise the selected time in milliseconds
        long dueDate = (tvDueDate.getText().toString().equals("No due date selected") || TextUtils.isEmpty(tvDueDate.getText())) ? 0L : selectedCalendar.getTimeInMillis();

        // Validate title
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(getContext(), "Task title cannot be empty", Toast.LENGTH_SHORT).show();
            return; // Stop if title is empty
        }

        // Call the appropriate listener method (add or update)
        if (listener != null) {
            if (currentTask == null) {
                // Add new task
                Task newTask = new Task(title, description, priority, dueDate);
                listener.onTaskAdded(newTask);
            } else {
                // Update existing task
                currentTask.setTitle(title);
                currentTask.setDescription(description);
                currentTask.setPriority(priority);
                currentTask.setDueDate(dueDate);
                listener.onTaskUpdated(currentTask);
            }
        } else {
            // Inform user if listener is not set (should ideally not happen with proper setup)
            Toast.makeText(getContext(), "Error: Dialog listener not set. Cannot save task.", Toast.LENGTH_LONG).show();
        }
        dismiss(); // Close the dialog after saving/updating
    }
}