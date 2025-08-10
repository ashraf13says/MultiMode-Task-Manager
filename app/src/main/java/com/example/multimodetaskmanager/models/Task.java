// MultiModeTaskManager/app/src/main/java/com/example/multimodetaskmanager/models/Task.java
package com.example.multimodetaskmanager.models;

import java.io.Serializable;
import java.util.Objects; // Import Objects for utility methods
import java.util.UUID; // For generating unique IDs

public class Task implements Serializable {

    private String id; // Unique identifier for each task
    private String title;
    private String description;
    private String priority; // e.g., "High", "Medium", "Low"
    private boolean isDone; // Status of the task
    private long dueDate; // Due date in milliseconds since epoch

    // PRIMARY CONSTRUCTOR (4 arguments)
    public Task(String title, String description, String priority, long dueDate) {
        this.id = UUID.randomUUID().toString(); // Generates a unique ID for each new task
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.isDone = false; // New tasks are initially not done
        this.dueDate = dueDate;
    }

    // OVERLOADED CONSTRUCTOR (2 arguments - for simple task creation with default values)
    public Task(String title, String priority) {
        this(title, "", priority, 0L); // Calls the 4-argument constructor with empty description and no due date
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public boolean isDone() { return isDone; }
    public long getDueDate() { return dueDate; }

    // --- Setters ---
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; } // Corrected: public void
    public void setPriority(String priority) { this.priority = priority; }
    public void setDone(boolean done) { isDone = done; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }

    // --- CRITICAL: Override equals() and hashCode() for DiffUtil and proper object comparison ---
    // These methods are essential for RecyclerView.Adapter's DiffUtil to correctly
    // identify if items are the same and if their contents have changed.
    @Override
    public boolean equals(Object o) {
        // If the objects are the same instance, they are equal
        if (this == o) return true;
        // If the other object is null or not of the same class, they are not equal
        if (o == null || getClass() != o.getClass()) return false;
        // Cast the object to Task
        Task task = (Task) o;
        // Compare all relevant fields for equality.
        // Use Objects.equals for String fields to handle nulls safely.
        // Use direct comparison for primitive types.
        return isDone == task.isDone &&
                dueDate == task.dueDate &&
                Objects.equals(id, task.id) && // Use ID for primary equality check (two tasks are the same if they have the same ID)
                Objects.equals(title, task.title) &&
                Objects.equals(description, task.description) &&
                Objects.equals(priority, task.priority);
    }

    @Override
    public int hashCode() {
        // Generate a hash code based on all fields used in equals().
        // Objects.hash() is a convenient way to do this.
        return Objects.hash(id, title, description, priority, isDone, dueDate);
    }
}