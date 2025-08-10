// MultiModeTaskManager/app/src/main/java/com/example/multimodetaskmanager/viewmodel/TaskViewModel.java
package com.example.multimodetaskmanager.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.multimodetaskmanager.models.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskViewModel extends ViewModel {
    // MutableLiveData to hold the list of tasks. MutableLiveData allows us to change its value.
    // Initialized with an empty ArrayList.
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>(new ArrayList<>());
    private SortOrder currentSortOrder = null; // Keeps track of the last applied sort order

    // Enum to define different sorting options for tasks
    public enum SortOrder {
        PRIORITY, DUE_DATE, NAME
    }

    // Provides an immutable LiveData object to the UI, allowing it to observe changes
    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    /**
     * Sets the initial list of tasks. This is typically used when loading tasks from storage
     * at the start of the application.
     * @param initialTasks The list of tasks to set.
     */
    public void setTasks(List<Task> initialTasks) {
        tasks.setValue(initialTasks);
        // If there's a current sort order set, re-apply it after setting initial tasks
        if (currentSortOrder != null) {
            sortTasks(currentSortOrder);
        }
    }

    /**
     * Adds a new task to the list. The list is then re-sorted if a sort order is active.
     * @param task The Task object to add.
     */
    public void addTask(Task task) {
        // Create a new modifiable copy of the current list from LiveData
        List<Task> current = new ArrayList<>(tasks.getValue());
        current.add(task); // Add the new task

        // After adding, apply the current sort order
        if (currentSortOrder != null) {
            List<Task> sortedList = new ArrayList<>(current); // Create a new list for sorting
            Collections.sort(sortedList, getComparatorForOrder(currentSortOrder));
            tasks.setValue(sortedList); // Update LiveData with the sorted list
        } else {
            tasks.setValue(current); // Just update LiveData if no specific sort order is active
        }
    }

    /**
     * Removes a task at a specific position from the list.
     * @param position The index of the task to remove.
     */
    public void removeTask(int position) {
        // Create a new modifiable copy of the current list from LiveData
        List<Task> current = new ArrayList<>(tasks.getValue());
        if (position >= 0 && position < current.size()) {
            current.remove(position); // Remove the task
            tasks.setValue(current); // Update LiveData, which triggers UI update and storage save
        }
    }

    /**
     * Updates an existing task in the list. The list is then re-sorted if a sort order is active.
     * @param updatedTask The updated Task object.
     */
    public void updateTask(Task updatedTask) {
        // Get a mutable copy of the current tasks
        List<Task> currentTasks = new ArrayList<>(tasks.getValue());
        for (int i = 0; i < currentTasks.size(); i++) {
            // Find the task by its unique ID and replace it with the updated version
            if (currentTasks.get(i).getId().equals(updatedTask.getId())) {
                currentTasks.set(i, updatedTask);
                break; // Task found and updated, so exit loop
            }
        }
        // After updating, apply the current sort order
        if (currentSortOrder != null) {
            List<Task> sortedList = new ArrayList<>(currentTasks); // Create a new list for sorting
            Collections.sort(sortedList, getComparatorForOrder(currentSortOrder));
            tasks.setValue(sortedList);
        } else {
            tasks.setValue(currentTasks); // Trigger LiveData update for UI refresh
        }
    }

    // --- Sorting Logic ---
    /**
     * Sorts the current list of tasks based on the specified order.
     * @param order The desired sorting order (PRIORITY, DUE_DATE, or NAME).
     */
    public void sortTasks(SortOrder order) {
        List<Task> currentTasks = tasks.getValue();
        if (currentTasks == null || currentTasks.isEmpty()) {
            return; // Nothing to sort if the list is empty or null
        }

        // CRITICAL FIX: Create a NEW list to ensure LiveData detects a change.
        // Even if the list were sorted in place, setValue() needs a new object reference
        // to properly notify observers, especially when using DiffUtil.
        List<Task> sortedList = new ArrayList<>(currentTasks); // Create a new list from current tasks

        // Sort the new list using the appropriate comparator
        Collections.sort(sortedList, getComparatorForOrder(order));

        currentSortOrder = order; // Update the stored current sort order
        tasks.setValue(sortedList); // Set the NEWLY SORTED LIST to trigger LiveData observer
    }

    // Helper method to get the appropriate Comparator based on the SortOrder enum
    private Comparator<Task> getComparatorForOrder(SortOrder order) {
        return (t1, t2) -> { // Lambda expression for a Comparator
            switch (order) {
                case PRIORITY:
                    // Sort by priority: High (1) > Medium (2) > Low (3). Lower value means higher priority.
                    int p1 = getPriorityValue(t1.getPriority());
                    int p2 = getPriorityValue(t2.getPriority());
                    return Integer.compare(p1, p2);
                case DUE_DATE:
                    // Sort by due date, earliest first. Tasks with no due date (0L) are placed at the end.
                    if (t1.getDueDate() == 0L && t2.getDueDate() == 0L) return 0; // Both no due date, maintain original order
                    if (t1.getDueDate() == 0L) return 1; // t1 has no due date, put it after t2
                    if (t2.getDueDate() == 0L) return -1; // t2 has no due date, put it after t1
                    return Long.compare(t1.getDueDate(), t2.getDueDate());
                case NAME:
                default:
                    // Default sort by task title alphabetically (case-insensitive)
                    return t1.getTitle().compareToIgnoreCase(t2.getTitle());
            }
        };
    }

    // Helper method to assign numerical values to priority strings for easier comparison
    private int getPriorityValue(String priority) {
        if (priority == null) return Integer.MAX_VALUE; // Null priority tasks go last
        switch (priority.toLowerCase()) {
            case "high": return 1;
            case "medium": return 2;
            case "low": return 3;
            default: return Integer.MAX_VALUE; // Unrecognized priorities also go last
        }
    }
}