package com.gusutabo.apw;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class Database {

    private static final String DB_NAME = "apw";

    private static final String USER =
            System.getenv().getOrDefault("APW_DB_USER", "root");

    private static final String PASSWORD =
            System.getenv().getOrDefault("APW_DB_PASSWORD", "");

    private static final String SERVER_URL =
            System.getenv().getOrDefault(
                    "APW_DB_SERVER_URL",
                    "jdbc:mysql://localhost:3306/?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
            );

    private static final String DB_URL =
            System.getenv().getOrDefault(
                    "APW_DB_URL",
                    "jdbc:mysql://localhost:3306/" + DB_NAME
                            + "?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
            );

    private Database() {
    }

    public static void initialize() throws SQLException {
        try (Connection connection = DriverManager.getConnection(
                SERVER_URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(
                    "CREATE DATABASE IF NOT EXISTS " + DB_NAME
                            + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
            );
        }

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS tasks (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        description VARCHAR(255) NOT NULL,
                        details TEXT,
                        done BOOLEAN NOT NULL DEFAULT FALSE,
                        category VARCHAR(50) NOT NULL DEFAULT 'None',
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS subtasks (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        task_id INT NOT NULL,
                        description VARCHAR(255) NOT NULL,
                        done BOOLEAN NOT NULL DEFAULT FALSE,
                        FOREIGN KEY (task_id) REFERENCES tasks(id)
                            ON DELETE CASCADE
                    )
                    """);
        }

        ensureCategoryColumn();
    }

    private static void ensureCategoryColumn() throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = ?
                  AND table_name = 'tasks'
                  AND column_name = 'category'
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, DB_NAME);

            try (ResultSet result = statement.executeQuery()) {
                result.next();

                if (result.getInt(1) == 0) {
                    try (Statement alter = connection.createStatement()) {
                        alter.executeUpdate(
                                "ALTER TABLE tasks "
                                        + "ADD COLUMN category VARCHAR(50) "
                                        + "NOT NULL DEFAULT 'None'"
                        );
                    }
                }
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    public static List<Task> loadTasks() throws SQLException {
        List<Task> result = new ArrayList<>();

        String taskSql = """
                SELECT id, description, details, done, category
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
                task.setDone(tasks.getBoolean("done"));
                task.setCategory(tasks.getString("category"));

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
                INSERT INTO tasks (description, details, done, category)
                VALUES (?, ?, ?, ?)
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
                SET description = ?, details = ?, done = ?, category = ?
                WHERE id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, task.getDescription());
            statement.setString(2, task.getDetails());
            statement.setBoolean(3, task.isDone());
            statement.setString(4, task.getCategory());
            statement.setInt(5, task.getId());
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
