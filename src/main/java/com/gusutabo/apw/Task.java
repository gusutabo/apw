package com.gusutabo.apw;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Task {

    private final int id;
    private String description;
    private String details;
    private boolean done;
    private String category;
    private TaskStatus status;

    private final ObservableList<String> subtasks =
            FXCollections.observableArrayList();

    public Task(int id, String description) {
        this.id = id;
        this.description = description;
        this.details = "";
        this.done = false;
        this.category = "None";
        this.status = TaskStatus.TODO;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details == null ? "" : details;
    }

    public boolean isDone() {
        return done;
    }

    /**
     * Marks the task as done (moves it to the DONE column) or reopens it
     * (DONE goes back to TODO; TODO and DOING are kept as they are).
     */
    public void setDone(boolean done) {
        this.done = done;

        if (done) {
            this.status = TaskStatus.DONE;
        } else if (this.status == TaskStatus.DONE) {
            this.status = TaskStatus.TODO;
        }
    }

    public TaskStatus getStatus() {
        return status;
    }

    /** Keeps the legacy {@code done} flag in sync with the kanban status. */
    public void setStatus(TaskStatus status) {
        this.status = status == null ? TaskStatus.TODO : status;
        this.done = this.status == TaskStatus.DONE;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category == null || category.isBlank()
                ? "None"
                : category;
    }

    public ObservableList<String> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(String subtask) {
        addSubtask(subtask, false);
    }

    public void addSubtask(String subtask, boolean done) {
        if (subtask != null && !subtask.trim().isEmpty()) {
            subtasks.add(subtask.trim());
        }
    }

    public void removeSubtask(String subtask) {
        subtasks.remove(subtask);
    }

    @Override
    public String toString() {
        return description;
    }
}
