package com.gusutabo.apw;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseTest {

    @TempDir
    static Path temporaryHome;

    private static String originalUserHome;

    @BeforeAll
    static void initializeDatabase() throws SQLException {
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", temporaryHome.toString());
        Database.initialize();
        Database.initialize();
    }

    @AfterAll
    static void restoreUserHome() {
        if (originalUserHome == null) {
            System.clearProperty("user.home");
        } else {
            System.setProperty("user.home", originalUserHome);
        }
    }

    @Test
    void persistsTasksAndDeletesSubtasksWithParent() throws SQLException {
        Task task = new Task(0, "Review release");
        task.setDetails("Check the packaged build");
        task.setCategory("Work");
        task.setStatus(TaskStatus.DOING);

        int taskId = Database.insertTask(task);
        Database.insertSubtask(taskId, "Run smoke tests");

        List<Task> loadedTasks = Database.loadTasks();
        assertEquals(1, loadedTasks.size());
        assertEquals("Review release", loadedTasks.getFirst().getDescription());
        assertEquals("Check the packaged build", loadedTasks.getFirst().getDetails());
        assertEquals("Work", loadedTasks.getFirst().getCategory());
        assertEquals(TaskStatus.DOING, loadedTasks.getFirst().getStatus());
        assertEquals(List.of("Run smoke tests"), loadedTasks.getFirst().getSubtasks());

        Database.deleteTask(taskId);

        assertTrue(Database.loadTasks().isEmpty());
    }
}