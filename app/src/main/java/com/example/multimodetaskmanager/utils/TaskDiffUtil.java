// MultiModeTaskManager/app/src/main/java/com/example/multimodetaskmanager/utils/TaskDiffUtil.java
package com.example.multimodetaskmanager.utils;

import androidx.recyclerview.widget.DiffUtil;
import com.example.multimodetaskmanager.models.Task;
import java.util.List;

public class TaskDiffUtil extends DiffUtil.Callback {
    private final List<Task> oldList; // The list of tasks before changes
    private final List<Task> newList; // The list of tasks after changes

    public TaskDiffUtil(List<Task> oldList, List<Task> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // This method is used by DiffUtil to check if two items represent the same logical entity.
        // CRITICAL FIX: Use the unique ID of the Task object. If IDs are the same, it's the same item.
        // This is essential for DiffUtil to correctly identify moved items across lists.
        return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // This method is used by DiffUtil to check if the content of two *same items* (as determined by areItemsTheSame) has changed.
        // CRITICAL FIX: Use the overridden equals() method in the Task model.
        // This method should compare all fields that, if changed, would require the item's view to be re-bound/updated.
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}