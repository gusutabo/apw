package com.gusutabo.apw;

import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Database {

    private static final Path DB_PATH = Path.of(
            System.getProperty("user.home"), ".apw", "apw.db");

    private Database() {
    }

    public static void initialize() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS tasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        description TEXT NOT NULL,
                        details TEXT,
                        done INTEGER NOT NULL DEFAULT 0,
                        category TEXT NOT NULL DEFAULT 'None',
                        status TEXT NOT NULL DEFAULT 'TODO',
                        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS subtasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        task_id INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        done INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (task_id) REFERENCES tasks(id)
                            ON DELETE CASCADE
                    )
                    """);
        }

        ensureCategoryColumn();
        ensureStatusColumn();
    }

    private static void ensureCategoryColumn() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            if (!hasColumn(statement, "category")) {
                statement.executeUpdate(
                        "ALTER TABLE tasks ADD COLUMN category TEXT NOT NULL DEFAULT 'None'"
                );
            }
        }
    }

    private static boolean hasColumn(Statement statement, String columnName)
            throws SQLException {
        try (ResultSet columns = statement.executeQuery("PRAGMA table_info(tasks)")) {
            while (columns.next()) {
                if (columnName.equalsIgnoreCase(columns.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Adds the kanban status column to databases created by older versions. */
    private static void ensureStatusColumn() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            if (!hasColumn(statement, "status")) {
                statement.executeUpdate(
                        "ALTER TABLE tasks ADD COLUMN status TEXT NOT NULL DEFAULT 'TODO'"
                );
                statement.executeUpdate(
                        "UPDATE tasks SET status = 'DONE' WHERE done = 1"
                );
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Files.createDirectories(DB_PATH.getParent());
            Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + DB_PATH);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (java.io.IOException e) {
            throw new SQLException(
                    "Não foi possível criar o diretório do banco de dados.", e);
        }
    }

    public static List<Task> loadTasks() throws SQLException {
        List<Task> result = new ArrayList<>();

        String taskSql = """
                SELECT id, description, details, done, category, status
                FROM tasks
                ORDER BY id
                """;

        try (Connection connection = getConnection();
             PreparedStatement taskStatement =
                     connection.prepareStatement(taskSql);
             ResultSet tasks = taskStatement.executeQuery()) {

            while (tasks.next()) {
                Task task = new Task(
                        tasks.getInt("id"),
                        tasks.getString("description")
                );

                task.setDetails(tasks.getString("details"));
                task.setCategory(tasks.getString("category"));
                task.setStatus(TaskStatus.fromDatabase(tasks.getString("status")));

                loadSubtasks(connection, task);
                result.add(task);
            }
        }

        return result;
    }

    private static void loadSubtasks(
            Connection connection,
            Task task
    ) throws SQLException {

        String sql = """
                SELECT description, done
                FROM subtasks
                WHERE task_id = ?
                ORDER BY id
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, task.getId());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    task.addSubtask(
                            result.getString("description"),
                            result.getBoolean("done")
                    );
                }
            }
        }
    }

    public static int insertTask(Task task) throws SQLException {
        String sql = """
                INSERT INTO tasks (description, details, done, category, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(1, task.getDescription());
            statement.setString(2, task.getDetails());
            statement.setBoolean(3, task.isDone());
            statement.setString(4, task.getCategory());
            statement.setString(5, task.getStatus().name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException(
                            "Não foi possível obter o ID da tarefa."
                    );
                }

                return keys.getInt(1);
            }
        }
    }

    public static void updateTask(Task task) throws SQLException {
        String sql = """
                UPDATE tasks
                SET description = ?, details = ?, done = ?, category = ?, status = ?
                WHERE id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, task.getDescription());
            statement.setString(2, task.getDetails());
            statement.setBoolean(3, task.isDone());
            statement.setString(4, task.getCategory());
            statement.setString(5, task.getStatus().name());
            statement.setInt(6, task.getId());
            statement.executeUpdate();
        }
    }

    public static void deleteTask(int taskId) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, taskId);
            statement.executeUpdate();
        }
    }

    public static int insertSubtask(
            int taskId,
            String description
    ) throws SQLException {

        String sql = """
                INSERT INTO subtasks (task_id, description, done)
                VALUES (?, ?, FALSE)
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setInt(1, taskId);
            statement.setString(2, description);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException(
                            "Não foi possível obter o ID da subtask."
                    );
                }

                return keys.getInt(1);
            }
        }
    }
}
