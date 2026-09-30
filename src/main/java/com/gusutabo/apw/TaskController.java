package com.gusutabo.apw;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.scene.paint.Color;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.List;

public class TaskController {

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
    private ComboBox<TaskStatus> statusComboBox;

    @FXML
    private Label taskCount;

    @FXML
    private VBox listContainer;

    @FXML
    private HBox boardContainer;

    @FXML
    private ToggleButton listViewButton;

    @FXML
    private ToggleButton boardViewButton;

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

        statusComboBox.setItems(
                FXCollections.observableArrayList(TaskStatus.values())
        );
        statusComboBox.setValue(TaskStatus.TODO);
        statusComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TaskStatus status) {
                return status == null ? "" : status.getLabel();
            }

            @Override
            public TaskStatus fromString(String label) {
                return null;
            }
        });
        // Mantém o checkbox "Mark as completed" coerente com o status
        statusComboBox.setOnAction(e ->
                completedCheckBox.setSelected(
                        statusComboBox.getValue() == TaskStatus.DONE
                )
        );

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
        refreshBoard();

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
                        statusComboBox.setValue(newTask.getStatus());
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
                    refreshBoard();
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
        task.setStatus(selectedStatus());
        task.setCategory(categoryComboBox.getValue());

        try {
            int id = Database.insertTask(task);

            Task savedTask = new Task(id, task.getDescription());
            savedTask.setDetails(task.getDetails());
            savedTask.setStatus(task.getStatus());
            savedTask.setCategory(task.getCategory());

            tasks.add(savedTask);
            taskListView.getSelectionModel().select(savedTask);
            updateTaskCount();
            refreshBoard();

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
            refreshBoard();
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
        selected.setStatus(selectedStatus());
        selected.setCategory(categoryComboBox.getValue());

        try {
            Database.updateTask(selected);
            taskListView.refresh();
            refreshBoard();
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

        TaskStatus oldStatus = selected.getStatus();
        selected.setDone(completedCheckBox.isSelected());

        try {
            Database.updateTask(selected);
            statusComboBox.setValue(selected.getStatus());
            taskListView.refresh();
            refreshBoard();
        } catch (SQLException e) {
            selected.setStatus(oldStatus);
            statusComboBox.setValue(oldStatus);
            completedCheckBox.setSelected(selected.isDone());
            taskListView.refresh();
            refreshBoard();
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
                refreshBoard();
            } catch (SQLException e) {
                showError(
                        "Banco de dados",
                        "Não foi possível criar a subtask.",
                        e
                );
            }
        });
    }

    // ------------------------------------------------------------------
    // KANBAN
    // ------------------------------------------------------------------

    @FXML
    protected void onShowList() {
        showBoard(false);
    }

    @FXML
    protected void onShowBoard() {
        showBoard(true);
    }

    private void showBoard(boolean board) {
        // Evita que o toggle fique "desmarcado" ao clicar no botão já ativo
        listViewButton.setSelected(!board);
        boardViewButton.setSelected(board);

        listContainer.setVisible(!board);
        listContainer.setManaged(!board);
        boardContainer.setVisible(board);
        boardContainer.setManaged(board);

        if (board) {
            refreshBoard();
        }
    }

    /** Status escolhido no painel de detalhes (checkbox já é mantido em sincronia). */
    private TaskStatus selectedStatus() {
        TaskStatus status = statusComboBox.getValue();
        return status == null ? TaskStatus.TODO : status;
    }

    /** Reconstrói as colunas do quadro a partir da lista de tarefas. */
    private void refreshBoard() {
        if (boardContainer == null) {
            return;
        }

        Task selected = taskListView.getSelectionModel().getSelectedItem();
        boardContainer.getChildren().clear();

        for (TaskStatus status : TaskStatus.values()) {
            boardContainer.getChildren().add(buildColumn(status, selected));
        }
    }

    private VBox buildColumn(TaskStatus status, Task selected) {
        VBox cards = new VBox(10);
        cards.getStyleClass().add("board-cards");

        int count = 0;
        for (Task task : tasks) {
            if (task.getStatus() == status) {
                cards.getChildren().add(buildCard(task, task == selected));
                count++;
            }
        }

        if (count == 0) {
            Label empty = new Label("No tasks");
            empty.getStyleClass().add("board-empty");
            cards.getChildren().add(empty);
        }

        Label title = new Label(status.getLabel());
        title.getStyleClass().addAll("board-column-title", "board-title-" + status.name().toLowerCase());
        Label counter = new Label(String.valueOf(count));
        counter.getStyleClass().add("board-column-count");
        HBox header = new HBox(8, title, counter);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 4, 0, 4));

        ScrollPane scroll = new ScrollPane(cards);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("board-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox column = new VBox(12, header, scroll);
        column.getStyleClass().add("board-column");
        column.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(column, Priority.ALWAYS);
        column.setPrefWidth(0); // as 3 colunas dividem o espaço igualmente

        // Soltar um card na coluna muda o status da tarefa
        column.setOnDragOver(event -> {
            if (event.getGestureSource() != column
                    && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                if (!column.getStyleClass().contains("board-column-drop")) {
                    column.getStyleClass().add("board-column-drop");
                }
            }
            event.consume();
        });
        column.setOnDragExited(event -> {
            column.getStyleClass().remove("board-column-drop");
            event.consume();
        });
        column.setOnDragDropped(event -> onCardDropped(event, column, status));

        return column;
    }

    private VBox buildCard(Task task, boolean selected) {
        Label title = new Label(task.getDescription());
        title.setWrapText(true);
        title.getStyleClass().add("board-card-title");
        if (task.isDone()) {
            title.getStyleClass().add("board-card-title-done");
        }

        VBox card = new VBox(8, title);
        card.getStyleClass().add("board-card");
        if (selected) {
            card.getStyleClass().add("board-card-selected");
        }

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);

        if (!"None".equals(task.getCategory())) {
            Label category = new Label(
                    flagFor(task.getCategory()) + " " + task.getCategory()
            );
            category.getStyleClass().add("task-category");
            category.setStyle(
                    "-fx-background-color: " + categoryColor(task.getCategory()) + ";"
            );
            meta.getChildren().add(category);
        }

        int subtasks = task.getSubtasks().size();
        if (subtasks > 0) {
            Label sub = new Label("☰ " + subtasks);
            sub.getStyleClass().add("board-card-meta");
            meta.getChildren().add(sub);
        }

        if (!meta.getChildren().isEmpty()) {
            card.getChildren().add(meta);
        }

        card.setOnMouseClicked(event ->
                taskListView.getSelectionModel().select(task)
        );

        card.setOnDragDetected(event -> {
            Dragboard board = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(task.getId()));
            board.setContent(content);
            board.setDragView(card.snapshot(null, null));
            event.consume();
        });

        return card;
    }

    private void onCardDropped(DragEvent event, VBox column, TaskStatus target) {
        column.getStyleClass().remove("board-column-drop");

        boolean moved = false;
        Dragboard board = event.getDragboard();

        if (board.hasString()) {
            try {
                int id = Integer.parseInt(board.getString());
                moved = moveTask(id, target);
            } catch (NumberFormatException ignored) {
                // conteúdo arrastado não é um card do quadro
            }
        }

        event.setDropCompleted(moved);
        event.consume();

        // Reconstrói depois que o gesto de arrastar termina
        Platform.runLater(this::refreshBoard);
    }

    /** Muda o status da tarefa e persiste. Retorna false se nada mudou. */
    private boolean moveTask(int taskId, TaskStatus target) {
        for (Task task : tasks) {
            if (task.getId() != taskId) {
                continue;
            }

            if (task.getStatus() == target) {
                return false;
            }

            TaskStatus oldStatus = task.getStatus();
            task.setStatus(target);

            try {
                Database.updateTask(task);
            } catch (SQLException e) {
                task.setStatus(oldStatus);
                showError(
                        "Banco de dados",
                        "Não foi possível mover a tarefa.",
                        e
                );
                return false;
            }

            taskListView.refresh();

            // Se o card movido é o selecionado, atualiza o painel de detalhes
            if (task == taskListView.getSelectionModel().getSelectedItem()) {
                statusComboBox.setValue(target);
                completedCheckBox.setSelected(task.isDone());
            }
            return true;
        }
        return false;
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
        statusComboBox.setValue(TaskStatus.TODO);
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
                TaskController.class.getResource("style.css").toExternalForm()
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
