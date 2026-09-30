package com.gusutabo.apw;

public enum TaskStatus {
    TODO("To do"),
    DOING("In progress"),
    DONE("Done");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static TaskStatus fromDatabase(String value) {
        if (value != null) {
            for (TaskStatus status : values()) {
                if (status.name().equalsIgnoreCase(value)) {
                    return status;
                }
            }
        }
        return TODO;
    }

    @Override
    public String toString() {
        return label;
    }
}
