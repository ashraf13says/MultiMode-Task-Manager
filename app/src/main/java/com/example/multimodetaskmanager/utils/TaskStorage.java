package com.example.multimodetaskmanager.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.multimodetaskmanager.models.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken; // Needed for deserializing List<Task>
import java.lang.reflect.Type; // Needed for TypeToken
import java.util.ArrayList;
import java.util.List;

public class TaskStorage {
    private static final String PREF_NAME = "task_prefs"; // Name of the SharedPreferences file
    private static final String KEY_TASKS = "tasks"; // Key for storing the JSON string of tasks

    /**
     * Saves the current list of tasks to SharedPreferences.
     * The list is converted to a JSON string before saving.
     *
     * @param context The application context.
     * @param tasks   The list of Task objects to save.
     */
    public static void saveTasks(Context context, List<Task> tasks) {
        // Get SharedPreferences instance specific to this app
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit(); // Get an editor to put data

        // Convert the List<Task> object to a JSON string
        String json = new Gson().toJson(tasks);
        editor.putString(KEY_TASKS, json); // Store the JSON string with the defined key
        editor.apply(); // Apply the changes asynchronously
    }

    /**
     * Loads the list of tasks from SharedPreferences.
     * The JSON string is converted back into a List<Task> object.
     *
     * @param context The application context.
     * @return A List of Task objects, or an empty ArrayList if no tasks are saved.
     */
    public static List<Task> loadTasks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_TASKS, null); // Retrieve the JSON string, null if not found

        if (json != null) {
            // Define the type of object to deserialize (List<Task>) using TypeToken
            Type type = new TypeToken<ArrayList<Task>>(){}.getType();
            // Convert the JSON string back into a List<Task>
            return new Gson().fromJson(json, type);
        } else {
            // Return an empty list if no tasks were found in SharedPreferences
            return new ArrayList<>();
        }
    }
}