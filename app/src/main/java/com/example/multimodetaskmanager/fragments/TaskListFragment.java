// MultiModeTaskManager/app/src/main/java/com/example/multimodetaskmanager/fragments/TaskListFragment.java
package com.example.multimodetaskmanager.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.example.multimodetaskmanager.R;
import com.example.multimodetaskmanager.activities.TaskDetailActivity;
import com.example.multimodetaskmanager.adapters.TaskAdapter;
import com.example.multimodetaskmanager.models.Task;
import com.example.multimodetaskmanager.utils.TaskDiffUtil;
import com.example.multimodetaskmanager.utils.TaskStorage;
import com.example.multimodetaskmanager.viewmodel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TaskListFragment extends Fragment implements
        TaskAdapter.OnTaskActionListener, // Implements the listener for task actions from the adapter
        TaskDialogFragment.OnTaskDialogListener { // Implements the listener for dialog results

    private boolean isTwoPane; // Flag to indicate if the app is in two-pane mode (e.g., tablet)
    private TaskAdapter adapter;
    private TaskViewModel taskViewModel;
    private RecyclerView recyclerView;

    // These fields are used to pass data to the context menu (long press) handler
    public Task selectedTaskForContextMenu;
    public int selectedTaskPositionForContextMenu;

    // Request codes for dialog results (though setTargetFragment is used, which handles results differently)
    private static final int ADD_TASK_REQUEST = 1;
    private static final int EDIT_TASK_REQUEST = 2;

    // Setter for the two-pane mode, called from MainActivity
    public void setTwoPane(boolean twoPane) {
        isTwoPane = twoPane;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        // Initialize RecyclerView and set its layout manager
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup Floating Action Button for adding new tasks
        FloatingActionButton fab = view.findViewById(R.id.fab_add_task);
        fab.setOnClickListener(v -> showAddTaskDialog());

        // Initialize adapter with an empty list and set it to the RecyclerView
        adapter = new TaskAdapter(new ArrayList<>(), this); // 'this' refers to TaskListFragment implementing OnTaskActionListener
        recyclerView.setAdapter(adapter);

        // Register the RecyclerView for context menu (long press)
        registerForContextMenu(recyclerView);

        // Setup ItemTouchHelper for swipe-to-delete functionality
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't support drag & drop
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (taskViewModel.getTasks().getValue() != null && position < taskViewModel.getTasks().getValue().size()) {
                    Task taskToDelete = taskViewModel.getTasks().getValue().get(position);
                    taskViewModel.removeTask(position); // Remove task from ViewModel
                    Toast.makeText(getContext(), "Task '" + taskToDelete.getTitle() + "' deleted.", Toast.LENGTH_SHORT).show();
                }
            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the TaskViewModel
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // CRITICAL: Observe changes in the task list LiveData from the ViewModel
        // This observer ensures the RecyclerView updates efficiently when tasks are added, updated, or removed.
        taskViewModel.getTasks().observe(getViewLifecycleOwner(), newTasks -> {
            List<Task> oldList = adapter.getTaskList();
            TaskDiffUtil diffCallback = new TaskDiffUtil(oldList, newTasks);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            adapter.setTaskList(newTasks); // Update the adapter's internal list with the new data
            diffResult.dispatchUpdatesTo(adapter); // Apply the calculated differences to the RecyclerView
            TaskStorage.saveTasks(getContext(), newTasks); // Save tasks to storage after any update
        });

        // Load initial tasks only when the fragment is first created (not on rotation)
        if (savedInstanceState == null) {
            List<Task> initialTasks = TaskStorage.loadTasks(getContext());
            // If no tasks are loaded, add some default welcome tasks
            if (initialTasks.isEmpty()) {
                initialTasks.add(new Task("Welcome Task", "This is your first task! Long-press or tap the options icon.", "High", System.currentTimeMillis() + 86400000L * 2)); // Due in 2 days
                initialTasks.add(new Task("Explore App", "Try adding a new task using the plus button.", "Medium", 0L)); // No due date
            }
            taskViewModel.setTasks(initialTasks); // Set initial tasks, which will trigger the observer
        }
    }

    // --- Implementation of TaskAdapter.OnTaskActionListener ---

    @Override
    public void onEdit(Task task) {
        // Create and show the TaskDialogFragment for editing an existing task
        TaskDialogFragment dialog = TaskDialogFragment.newInstance(task);
        dialog.setTargetFragment(TaskListFragment.this, EDIT_TASK_REQUEST); // Set this fragment as the target to receive results
        dialog.show(getParentFragmentManager(), "TaskDialogFragment");
    }

    @Override
    public void onDelete(Task task) {
        List<Task> currentTasks = taskViewModel.getTasks().getValue();
        if (currentTasks != null) {
            // Find the task by ID and remove it from the ViewModel
            for (int i = 0; i < currentTasks.size(); i++) {
                if (currentTasks.get(i).getId().equals(task.getId())) {
                    taskViewModel.removeTask(i);
                    Toast.makeText(getContext(), "Task '" + task.getTitle() + "' deleted.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    @Override
    public void onMarkDone(Task task) {
        List<Task> currentTasks = taskViewModel.getTasks().getValue();
        if (currentTasks != null) {
            // Find the task by ID and toggle its 'done' status
            for (int i = 0; i < currentTasks.size(); i++) {
                if (currentTasks.get(i).getId().equals(task.getId())) {
                    Task updatedTask = currentTasks.get(i);
                    updatedTask.setDone(!updatedTask.isDone()); // Toggle done status
                    taskViewModel.updateTask(updatedTask); // Update task in ViewModel
                    Toast.makeText(getContext(), "'" + task.getTitle() + "' marked as " + (updatedTask.isDone() ? "Done" : "Pending"), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    @Override
    public void onTaskClick(Task task) {
        // Handle clicking on a task item (e.g., open a detail view)
        if (isTwoPane) {
            // In two-pane mode, replace the detail fragment with the selected task's details
            TaskDetailFragment detailFragment = TaskDetailFragment.newInstance(task);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_detail, detailFragment)
                    .commit();
        } else {
            // In single-pane mode, start a new activity to show details
            Intent intent = new Intent(getContext(), TaskDetailActivity.class);
            intent.putExtra("task", task); // Pass the Task object
            startActivity(intent);
        }
    }

    // --- Implementation of TaskDialogFragment.OnTaskDialogListener ---

    @Override
    public void onTaskAdded(Task task) {
        taskViewModel.addTask(task); // Add the new task via ViewModel
        Toast.makeText(getContext(), "Task '" + task.getTitle() + "' added.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskUpdated(Task task) {
        taskViewModel.updateTask(task); // Update the existing task via ViewModel
        Toast.makeText(getContext(), "Task '" + task.getTitle() + "' updated.", Toast.LENGTH_SHORT).show();
    }

    // --- Context Menu (Long Press) Handling ---

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // Inflate the context menu from XML
        getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);

        // Dynamically change "Mark as Done" to "Mark as Pending" if the selected task is already done
        MenuItem markDoneItem = menu.findItem(R.id.menu_done);
        if (markDoneItem != null && selectedTaskForContextMenu != null) {
            if (selectedTaskForContextMenu.isDone()) {
                markDoneItem.setTitle("Mark as Pending");
            } else {
                markDoneItem.setTitle("Mark as Done");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        // Handle clicks on context menu items
        if (selectedTaskForContextMenu != null) {
            int id = item.getItemId();
            if (id == R.id.menu_edit) {
                onEdit(selectedTaskForContextMenu);
                return true;
            } else if (id == R.id.menu_delete) {
                onDelete(selectedTaskForContextMenu);
                return true;
            } else if (id == R.id.menu_done) {
                onMarkDone(selectedTaskForContextMenu);
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    // --- Helper Method to Show Add Task Dialog ---
    private void showAddTaskDialog() {
        TaskDialogFragment dialog = TaskDialogFragment.newInstance(null); // Pass null to indicate adding a new task
        dialog.setTargetFragment(TaskListFragment.this, ADD_TASK_REQUEST); // Set this fragment as the target
        dialog.show(getParentFragmentManager(), "TaskDialogFragment");
    }
}