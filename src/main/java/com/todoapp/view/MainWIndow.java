package com.todoapp.view;

//import java.util.List;
import java.util.Optional;

import com.todoapp.controller.TaskController;
import com.todoapp.model.Task;
import com.todoapp.model.TaskStatus;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Janela principal da aplicação de gerenciamento de tarefas.
 *
 * Componentes:
 * - MenuBar com menus Arquivo, Tarefas e Ajuda
 * - Barra de ferramentas (busca, filtros, botões)
 * - Tabela de tarefas com ações inline
 * - Painel de estatísticas com barra de progresso
 * - Barra de status com feedback visual
 *
 * Esta classe é responsável exclusivamente pela UI.
 * Toda a lógica de negócio é delegada ao TaskController.
 *
 * @author Fábio Júnior
 * @version 1.0.0
 */
public class MainWindow{

    // === DEPENDÊNCIAS ===
    private final Stage stage;
    private final TaskController controller;

    // === COMPONENTES DA UI ===
    private TableView<Task> taskTable;
    private TextField searchField;
    private ComboBox<String> filterComboBox;
    private ProgressBar progressBar;
    private Label statsLabel;
    private Label statusLabel;

    // === CONSTANTES ===
    private static final double WINDOW_WIDTH  = 1000;
    private static final double WINDOW_HEIGHT = 700;

    // === CONSTRUTOR ===

    /**
     * Construtor da janela principal.
     *
     * @param stage      stage principal do JavaFX
     * @param controller controller de tarefas
     */
    public MainWindow(Stage stage, TaskController controller) {
        this.stage = stage;
        this.controller = controller;

        setupControllerCallbacks();
        initializeUI();
        controller.loadAllTasks();
    }

    // === INICIALIZAÇÃO ===

    /**
     * Monta todos os componentes da janela.
     */
    private void initializeUI() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(0));

        root.setTop(createMenuAndToolbar());
        root.setCenter(createTaskTable());
        root.setBottom(createBottomPanel());

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Todo Manager – Gerenciador de Tarefas");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setOnCloseRequest(e -> Platform.exit());
    }

    // === MENU E TOOLBAR ===

    /**
     * Cria container com MenuBar e Toolbar empilhados.
     */
    private VBox createMenuAndToolbar() {
        VBox container = new VBox();
        container.getChildren().addAll(createMenuBar(), createToolbar());
        return container;
    }

    /**
     * Cria a barra de menus (Arquivo | Tarefas | Ajuda).
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // — Menu Arquivo —
        Menu fileMenu = new Menu("Arquivo");
        MenuItem exitItem = new MenuItem("Sair");
        exitItem.setOnAction(e -> Platform.exit());
        fileMenu.getItems().add(exitItem);

        // — Menu Tarefas —
        Menu taskMenu = new Menu("Tarefas");

        MenuItem newTaskItem = new MenuItem("Nova Tarefa");
        newTaskItem.setOnAction(e -> showCreateTaskDialog());

        MenuItem clearCompletedItem = new MenuItem("Limpar Concluídas");
        clearCompletedItem.setOnAction(e -> confirmClearCompleted());

        MenuItem deleteAllItem = new MenuItem("Deletar Todas as Tarefas");
        deleteAllItem.setOnAction(e -> confirmDeleteAll());

        taskMenu.getItems().addAll(
                newTaskItem,
                new SeparatorMenuItem(),
                clearCompletedItem,
                deleteAllItem
        );

        // — Menu Ajuda —
        Menu helpMenu = new Menu("Ajuda");
        MenuItem aboutItem = new MenuItem("Sobre");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, taskMenu, helpMenu);
        return menuBar;
    }

    /**
     * Cria a barra de ferramentas com botões e campo de busca.
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(8, 12, 8, 12));
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // Botão Nova Tarefa
        Button newButton = new Button("➕ Nova Tarefa");
        newButton.setStyle("-fx-font-size: 13px; -fx-padding: 6 14;");
        newButton.setOnAction(e -> showCreateTaskDialog());

        // Campo de busca
        searchField = new TextField();
        searchField.setPromptText("🔍 Buscar tarefas...");
        searchField.setPrefWidth(280);
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                controller.searchTasks(newVal));

        // ComboBox de filtro
        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll(
                "Todas",
                "⏳ Pendentes",
                "🚀 Em Progresso",
                "✅ Concluídas",
                "📋 Ativas"
        );
        filterComboBox.setValue("Todas");
        filterComboBox.setOnAction(e -> applyFilter());

        // Spacer para empurrar botão de refresh para a direita
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Botão Atualizar
        Button refreshButton = new Button("🔄");
        refreshButton.setTooltip(new Tooltip("Atualizar lista"));
        refreshButton.setOnAction(e -> controller.loadAllTasks());

        toolbar.getChildren().addAll(
                newButton,
                new Separator(),
                new Label("Buscar:"), searchField,
                new Label("Filtrar:"), filterComboBox,
                spacer,
                refreshButton
        );
        return toolbar;
    }

    // === TABELA DE TAREFAS ===

    /**
     * Cria o painel central com a tabela de tarefas.
     */
    private VBox createTaskTable() {
        VBox container = new VBox(5);
        container.setPadding(new Insets(10, 12, 0, 12));

        taskTable = new TableView<>();
        taskTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        taskTable.setPlaceholder(new Label("Nenhuma tarefa encontrada."));

        // — Coluna Status (emoji) —
        TableColumn<Task, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(80);
        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus().getEmoji()));
        statusCol.setStyle("-fx-alignment: CENTER;");

        // — Coluna Título —
        TableColumn<Task, String> titleCol = new TableColumn<>("Título");
        titleCol.setPrefWidth(280);
        titleCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTitle()));

        // — Coluna Descrição —
        TableColumn<Task, String> descCol = new TableColumn<>("Descrição");
        descCol.setPrefWidth(250);
        descCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getShortDescription()));

        // — Coluna Data de Criação —
        TableColumn<Task, String> createdCol = new TableColumn<>("Criada em");
        createdCol.setPrefWidth(140);
        createdCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFormattedCreatedAt()));

        // — Coluna Ações —
        TableColumn<Task, Void> actionsCol = new TableColumn<>("Ações");
        actionsCol.setPrefWidth(180);
        actionsCol.setCellFactory(col -> new TableCell<>() {

            private final Button editBtn     = new Button("✏️");
            private final Button completeBtn = new Button("✅");
            private final Button deleteBtn   = new Button("🗑️");
            private final HBox   buttons     = new HBox(6, editBtn, completeBtn, deleteBtn);

            {
                editBtn.setTooltip(new Tooltip("Editar tarefa"));
                completeBtn.setTooltip(new Tooltip("Marcar como concluída"));
                deleteBtn.setTooltip(new Tooltip("Deletar tarefa"));

                editBtn.setOnAction(e -> {
                    Task task = getTableRow().getItem();
                    if (task != null) showEditTaskDialog(task);
                });
                completeBtn.setOnAction(e -> {
                    Task task = getTableRow().getItem();
                    if (task != null) controller.completeTask(task.getId());
                });
                deleteBtn.setOnAction(e -> {
                    Task task = getTableRow().getItem();
                    if (task != null) confirmDeleteTask(task);
                });

                buttons.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        /**
         * taskTable.getColumns().addAll(statusCol, titleCol, descCol, createdCol, actionsCol);
*/
        // Menu de contexto (botão direito)
        taskTable.setContextMenu(createContextMenu());

        // Double-click para editar
        taskTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Task selected = taskTable.getSelectionModel().getSelectedItem();
                if (selected != null) showEditTaskDialog(selected);
            }
        });

        VBox.setVgrow(taskTable, Priority.ALWAYS);
        container.getChildren().add(taskTable);
        return container;
    }

    /**
     * Cria o menu de contexto (clique direito na tabela).
     */
    private ContextMenu createContextMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem editItem = new MenuItem("✏️ Editar");
        editItem.setOnAction(e -> {
            Task task = taskTable.getSelectionModel().getSelectedItem();
            if (task != null) showEditTaskDialog(task);
        });

        MenuItem advanceItem = new MenuItem("▶ Avançar Status");
        advanceItem.setOnAction(e -> {
            Task task = taskTable.getSelectionModel().getSelectedItem();
            if (task != null) controller.advanceTaskStatus(task.getId());
        });

        MenuItem revertItem = new MenuItem("◀ Retroceder Status");
        revertItem.setOnAction(e -> {
            Task task = taskTable.getSelectionModel().getSelectedItem();
            if (task != null) controller.revertTaskStatus(task.getId());
        });

        MenuItem completeItem = new MenuItem("✅ Marcar como Concluída");
        completeItem.setOnAction(e -> {
            Task task = taskTable.getSelectionModel().getSelectedItem();
            if (task != null) controller.completeTask(task.getId());
        });

        MenuItem deleteItem = new MenuItem("🗑️ Deletar");
        deleteItem.setOnAction(e -> {
            Task task = taskTable.getSelectionModel().getSelectedItem();
            if (task != null) confirmDeleteTask(task);
        });

        menu.getItems().addAll(
                editItem,
                advanceItem,
                revertItem,
                completeItem,
                new SeparatorMenuItem(),
                deleteItem
        );
        return menu;
    }

    // === PAINEL INFERIOR ===

    /**
     * Cria o painel inferior com estatísticas e barra de status.
     */
    private VBox createBottomPanel() {
        VBox container = new VBox(4);
        container.setPadding(new Insets(8, 12, 8, 12));

        statusLabel = new Label("Pronto");
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setStyle("-fx-padding: 4 8; -fx-background-color: #f0f0f0; -fx-background-radius: 4;");

        container.getChildren().addAll(new Separator(), createStatsPanel(), statusLabel);
        return container;
    }

    /**
     * Cria o painel de estatísticas com counters e barra de progresso.
     */
    private HBox createStatsPanel() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(8));
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 5;");

        statsLabel = new Label("Carregando...");

        VBox progressContainer = new VBox(3);
        Label progressLabel = new Label("Progresso geral:");
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(220);
        progressContainer.getChildren().addAll(progressLabel, progressBar);

        panel.getChildren().addAll(statsLabel, new Separator(), progressContainer);
        return panel;
    }

    // === CALLBACKS DO CONTROLLER ===

    /**
     * Registra os callbacks de comunicação com o Controller.
     * Todos os updates de UI passam pelo Platform.runLater para garantir
     * execução na thread do JavaFX.
     */
    private void setupControllerCallbacks() {

        controller.setOnTasksUpdated(tasks -> Platform.runLater(() -> {
            taskTable.getItems().setAll(tasks);
            updateStats();
        }));

        controller.setOnSuccess(message -> Platform.runLater(() -> {
            statusLabel.setText("✅ " + message);
            statusLabel.setStyle(
                    "-fx-padding: 4 8; -fx-background-color: #d4edda;" +
                    "-fx-text-fill: #155724; -fx-background-radius: 4;");
        }));

        controller.setOnError(message -> Platform.runLater(() -> {
            statusLabel.setText("❌ " + message);
            statusLabel.setStyle(
                    "-fx-padding: 4 8; -fx-background-color: #f8d7da;" +
                    "-fx-text-fill: #721c24; -fx-background-radius: 4;");
            showErrorAlert(message);
        }));
    }

    // === AÇÕES ===

    /**
     * Aplica o filtro selecionado no ComboBox.
     */
    private void applyFilter() {
        String filter = filterComboBox.getValue();
        switch (filter) {
            case "Todas"           -> controller.loadAllTasks();
            case "⏳ Pendentes"    -> controller.loadTasksByStatus(TaskStatus.PENDENTE);
            case "🚀 Em Progresso" -> controller.loadTasksByStatus(TaskStatus.EM_PROGRESSO);
            case "✅ Concluídas"   -> controller.loadCompletedTasks();
            case "📋 Ativas"       -> controller.loadActiveTasks();
            default                -> controller.loadAllTasks();
        }
    }

    /**
     * Atualiza labels e barra de progresso das estatísticas.
     */
    private void updateStats() {
        long total      = controller.getTotalTaskCount();
        long pending    = controller.getTaskCountByStatus(TaskStatus.PENDENTE);
        long inProgress = controller.getTaskCountByStatus(TaskStatus.EM_PROGRESSO);
        long completed  = controller.getTaskCountByStatus(TaskStatus.CONCLUIDA);
        double percent  = controller.getCompletionPercentage();

        statsLabel.setText(String.format(
                "📊 Total: %d  |  ⏳ Pendentes: %d  |  🚀 Em Progresso: %d  |  ✅ Concluídas: %d",
                total, pending, inProgress, completed
        ));
        progressBar.setProgress(percent / 100.0);
    }

    // === DIÁLOGOS ===

    /**
     * Exibe diálogo para criação de nova tarefa.
     */
    private void showCreateTaskDialog() {
        TaskDialog dialog = new TaskDialog(stage);
        Optional<Task> result = dialog.showAndWait();
        result.ifPresent(task ->
                controller.createTask(task.getTitle(), task.getDescription()));
    }

    /**
     * Exibe diálogo para edição de tarefa existente.
     *
     * @param task tarefa a editar
     */
    private void showEditTaskDialog(Task task) {
        TaskDialog dialog = new TaskDialog(stage, task);
        Optional<Task> result = dialog.showAndWait();
        result.ifPresent(controller::updateTask);
    }

    /**
     * Solicita confirmação antes de deletar uma tarefa.
     *
     * @param task tarefa a deletar
     */
    private void confirmDeleteTask(Task task) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Deleção");
        alert.setHeaderText("Deletar tarefa?");
        alert.setContentText("Tem certeza que deseja deletar:\n\"" + task.getTitle() + "\"?");
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> controller.deleteTask(task.getId()));
    }

    /**
     * Solicita confirmação antes de deletar todas as tarefas.
     */
    private void confirmDeleteAll() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Deleção");
        alert.setHeaderText("Deletar TODAS as tarefas?");
        alert.setContentText("Esta ação é irreversível!");
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> controller.deleteAllTasks());
    }

    /**
     * Solicita confirmação antes de limpar tarefas concluídas.
     */
    private void confirmClearCompleted() {
        long count = controller.getTaskCountByStatus(TaskStatus.CONCLUIDA);
        if (count == 0) {
            showInfoAlert("Nenhuma tarefa concluída para remover.");
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Limpeza");
        alert.setHeaderText("Limpar tarefas concluídas?");
        alert.setContentText(count + " tarefa(s) será(ão) removida(s).");
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> controller.deleteCompletedTasks());
    }

    /**
     * Exibe diálogo "Sobre" com informações da aplicação.
     */
    private void showAboutDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Sobre");
        alert.setHeaderText("Todo Manager");
        alert.setContentText("""
    Sistema de Gerenciamento de Tarefas

    Versão: 1.0.0
    Desenvolvido em Java 21 + JavaFX

    © 2026 Fábio Júnior
    """);
        alert.showAndWait();
    }

    /**
     * Exibe alerta de erro.
     *
     * @param message mensagem de erro
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Ocorreu um erro");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Exibe alerta informativo.
     *
     * @param message mensagem a exibir
     */
    private void showInfoAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // === MÉTODOS PÚBLICOS ===

    /**
     * Exibe a janela principal.
     */
    public void show() {
        stage.show();
    }
}