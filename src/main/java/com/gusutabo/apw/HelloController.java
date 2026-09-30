package com.gusutabo.apw;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.List;

public class HelloController {

    @FXML
    private ListView<Task> taskListView;

    @FXML
    private ListView<String> subtaskListView;

    @FXML
    private TextField taskInput;

    @FXML
    private TextArea descriptionInput;

    @FXML
    private CheckBox completedCheckBox;

    @FXML
    private javafx.scene.control.ComboBox<String> categoryComboBox;

    @FXML
    private Label taskCount;

    private final ObservableList<Task> tasks =
            FXCollections.observableArrayList();

    private static final String[] CATEGORIES = {
            "None",
            "Personal",
            "Work",
            "Study",
            "Shopping",
            "Urgent"
    };

    @FXML
    public void initialize() {
        categoryComboBox.setItems(
                FXCollections.observableArrayList(CATEGORIES)
        );
        categoryComboBox.setValue("None");
        categoryComboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(flagFor(category) + "  " + category);
            }
        });
        categoryComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(flagFor(category) + "  " + category);
            }
        });

        try {
            Database.initialize();
            List<Task> loadedTasks = Database.loadTasks();
            tasks.setAll(loadedTasks);
        } catch (SQLException e) {
            showError(
                    "Banco de dados",
                    "Não foi possível conectar ao MySQL.",
                    e
            );
        }

        taskListView.setItems(tasks);
        updateTaskCount();

        taskListView.setCellFactory(lv -> new ListCell<Task>() {
            private final Label title = new Label();
            private final Label category = new Label();
            private final HBox content = new HBox(10, title, category);

            {
                HBox.setHgrow(title, Priority.ALWAYS);
                title.setMaxWidth(Double.MAX_VALUE);
                content.setMaxWidth(Double.MAX_VALUE);
                category.getStyleClass().add("task-category");
            }

            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);

                if (empty || task == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                title.setText(task.getDescription());
                title.setStyle("-fx-strikethrough: " + task.isDone() + ";");
                title.setTextFill(Color.web(
                        task.isDone() ? "#999999" : "#444444"
                ));

                category.setText(
                        flagFor(task.getCategory()) + " "
                                + task.getCategory()
                );
                category.setStyle(
                        "-fx-background-color: " + categoryColor(task.getCategory()) + ";"
                );
                category.setVisible(!"None".equals(task.getCategory()));
                category.setManaged(!"None".equals(task.getCategory()));

                setGraphic(content);
                setText(null);
            }
        });

        subtaskListView.setPlaceholder(
                new Label("No subtasks")
        );

        taskListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldTask, newTask) -> {
                    if (newTask != null) {
                        taskInput.setText(newTask.getDescription());
                        descriptionInput.setText(
                                newTask.getDetails() == null
                                        ? ""
                                        : newTask.getDetails()
                        );
                        completedCheckBox.setSelected(newTask.isDone());
                        categoryComboBox.setValue(
                                newTask.getCategory()
                        );
                        subtaskListView.setItems(
                                newTask.getSubtasks()
                        );
                    } else {
                        clearDetailsPanel();
                    }
                });
    }

    @FXML
    protected void onAddTask() {
        String text = taskInput.getText().trim();

        if (text.isEmpty()) {
            showInfo("Nova tarefa", "Digite o nome da tarefa.");
            taskInput.requestFocus();
            return;
        }

        Task task = new Task(0, text);
        task.setDetails(descriptionInput.getText().trim());
        task.setDone(completedCheckBox.isSelected());
        task.setCategory(categoryComboBox.getValue());

        try {
            int id = Database.insertTask(task);

            Task savedTask = new Task(id, task.getDescription());
            savedTask.setDetails(task.getDetails());
            savedTask.setDone(task.isDone());
            savedTask.setCategory(task.getCategory());

            tasks.add(savedTask);
            taskListView.getSelectionModel().select(savedTask);
            updateTaskCount();

        } catch (SQLException e) {
            showError(
                    "Banco de dados",
                    "Não foi possível criar a tarefa.",
                    e
            );
        }
    }

    @FXML
    protected void onRemoveTask() {
        Task selected = taskListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            return;
        }

        try {
            Database.deleteTask(selected.getId());
            tasks.remove(selected);
            clearDetailsPanel();
            updateTaskCount();
        } catch (SQLException e) {
            showError(
                    "Banco de dados",
                    "Não foi possível excluir a tarefa.",
                    e
            );
        }
    }

    @FXML
    protected void onSaveChanges() {
        Task selected = taskListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showInfo(
                    "Salvar alterações",
                    "Selecione uma tarefa primeiro."
            );
            return;
        }

        String title = taskInput.getText().trim();

        if (title.isEmpty()) {
            showInfo(
                    "Salvar alterações",
                    "O título da tarefa não pode ficar vazio."
            );
            return;
        }

        selected.setDescription(title);
        selected.setDetails(descriptionInput.getText().trim());
        selected.setDone(completedCheckBox.isSelected());
        selected.setCategory(categoryComboBox.getValue());

        try {
            Database.updateTask(selected);
            taskListView.refresh();
        } catch (SQLException e) {
            showError(
                    "Banco de dados",
                    "Não foi possível salvar as alterações.",
                    e
            );
        }
    }

    @FXML
    protected void onToggleCompleted() {
        Task selected = taskListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            completedCheckBox.setSelected(false);
            return;
        }

        boolean oldValue = selected.isDone();
        selected.setDone(completedCheckBox.isSelected());

        try {
            Database.updateTask(selected);
            taskListView.refresh();
        } catch (SQLException e) {
            selected.setDone(oldValue);
            completedCheckBox.setSelected(oldValue);
            taskListView.refresh();
            showError(
                    "Banco de dados",
                    "Não foi possível atualizar o estado da tarefa.",
                    e
            );
        }
    }

    @FXML
    protected void onAddSubtask() {
        Task selected = taskListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showInfo("Subtask", "Selecione uma tarefa primeiro.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Subtask");
        dialog.setHeaderText("Add a new subtask");
        dialog.setContentText("Subtask:");
        styleDialog(dialog, "question");

        dialog.showAndWait().ifPresent(text -> {
            String subtask = text.trim();

            if (subtask.isEmpty()) {
                return;
            }

            try {
                Database.insertSubtask(selected.getId(), subtask);
                selected.addSubtask(subtask);
                subtaskListView.refresh();
            } catch (SQLException e) {
                showError(
                        "Banco de dados",
                        "Não foi possível criar a subtask.",
                        e
                );
            }
        });
    }

    private String flagFor(String category) {
        return "None".equals(category) ? "" : "⚑";
    }

    private String categoryColor(String category) {
        return switch (category == null ? "None" : category) {
            case "Personal" -> "#ffe1df";
            case "Work" -> "#dff3e3";
            case "Study" -> "#e1e9ff";
            case "Shopping" -> "#fff0d2";
            case "Urgent" -> "#ffdfe7";
            default -> "#f0f0f0";
        };
    }

    private void clearDetailsPanel() {
        taskInput.clear();
        descriptionInput.clear();
        completedCheckBox.setSelected(false);
        categoryComboBox.setValue("None");
        subtaskListView.setItems(
                FXCollections.observableArrayList()
        );
    }

    private void updateTaskCount() {
        taskCount.setText(String.valueOf(tasks.size()));
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                message,
                ButtonType.OK
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        styleDialog(alert, "info");
        alert.showAndWait();
    }

    private void showError(
            String title,
            String message,
            Exception exception
    ) {
        exception.printStackTrace();

        Alert alert = new Alert(
                Alert.AlertType.ERROR,
                message,
                ButtonType.OK
        );
        alert.setTitle(title);
        alert.setHeaderText(null);

        // Detalhe técnico fica escondido até o usuário pedir
        StringWriter trace = new StringWriter();
        exception.printStackTrace(new PrintWriter(trace));
        String detail = exception.getMessage() == null
                ? trace.toString()
                : exception.getMessage() + "\n\n" + trace;

        TextArea area = new TextArea(detail);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(8);
        area.getStyleClass().add("dialog-details");
        alert.getDialogPane().setExpandableContent(area);

        styleDialog(alert, "error");
        alert.showAndWait();
    }

    /** Aplica o visual do app em qualquer diálogo. kind: info | error | question */
    private void styleDialog(Dialog<?> dialog, String kind) {
        var pane = dialog.getDialogPane();
        pane.getStylesheets().add(
                HelloController.class.getResource("style.css").toExternalForm()
        );
        pane.getStyleClass().addAll("app-dialog", "app-dialog-" + kind);

        Label icon = new Label(switch (kind) {
            case "error" -> "!";
            case "question" -> "?";
            default -> "i";
        });
        icon.setAlignment(Pos.CENTER);
        icon.getStyleClass().addAll("dialog-icon", "dialog-icon-" + kind);
        dialog.setGraphic(icon);

        if (taskListView != null && taskListView.getScene() != null) {
            dialog.initOwner(taskListView.getScene().getWindow());
        }
    }
}
