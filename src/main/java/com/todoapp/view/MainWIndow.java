package com.todoapp.view;

import java.util.Optional;

import com.todoapp.controller.TaskController;
import com.todoapp.model.Task;
import com.todoapp.model.TaskStatus;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
import javafx.scene.control.cell.PropertyValueFactory;
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
 * - Barra de ferramentas (busca, filtros, botões)
 * - Tabela de tarefas
 * - Painel de estatísticas
 * - Barra de status
 * 
 * Esta classe é responsável apenas pela UI.
 * Toda lógica é delegada ao TaskController.
 * 
 * @author Fábio Júnior
 * @version 1.0.1
 */
public class MainWindow {
    
    // === COMPONENTES PRINCIPAIS ===
    
    private final Stage stage;
    private final TaskController controller;
    
    // UI Components
    private TableView<Task> taskTable;
    private TextField searchField;
    private ComboBox<String> filterComboBox;
    private ProgressBar progressBar;
    private Label statsLabel;
    private Label statusLabel;
    
    // Dimensões
    private static final double WINDOW_WIDTH = 1000;
    private static final double WINDOW_HEIGHT = 700;
    
    // === CONSTRUTOR ===
    
    /**
     * Construtor da janela principal
     * 
     * @param stage stage principal do JavaFX
     * @param controller controller de tarefas
     */
    public MainWindow(Stage stage, TaskController controller) {
        this.stage = stage;
        this.controller = controller;
        
        // Configurar callbacks do controller
        setupControllerCallbacks();
        
        // Criar interface
        initializeUI();
        
        // Carregar dados iniciais
        controller.loadAllTasks();
    }
    
    // === INICIALIZAÇÃO DA UI ===
    
    /**
     * Inicializa todos os componentes da interface
     */
    private void initializeUI() {
        // Layout principal
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Componentes
        root.setTop(createToolbar());
        root.setCenter(createTaskTable());
        root.setBottom(createBottomPanel());
        
        // Menu
        root.setTop(createMenuAndToolbar());
        
        // Scene
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Todo Manager - Gerenciador de Tarefas");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        
        // Fechar aplicação ao fechar janela
        stage.setOnCloseRequest(e -> Platform.exit());
    }
    
    /**
     * Cria menu e barra de ferramentas combinados
     */
    private VBox createMenuAndToolbar() {
        VBox container = new VBox();
        
        // Menu Bar
        MenuBar menuBar = createMenuBar();
        
        // Toolbar
        HBox toolbar = createToolbar();
        
        container.getChildren().addAll(menuBar, toolbar);
        return container;
    }
    
    /**
     * Cria barra de menu
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // Menu Arquivo
        Menu fileMenu = new Menu("Arquivo");
        MenuItem exitItem = new MenuItem("Sair");
        exitItem.setOnAction(e -> Platform.exit());
        fileMenu.getItems().addAll(exitItem);
        
        // Menu Tarefas
        Menu taskMenu = new Menu("Tarefas");
        MenuItem newTaskItem = new MenuItem("Nova Tarefa");
        newTaskItem.setOnAction(e -> showCreateTaskDialog());
        
        MenuItem deleteAllItem = new MenuItem("Deletar Todas");
        deleteAllItem.setOnAction(e -> confirmDeleteAll());
        
        MenuItem clearCompletedItem = new MenuItem("Limpar Concluídas");
        clearCompletedItem.setOnAction(e -> confirmClearCompleted());
        
        taskMenu.getItems().addAll(
            newTaskItem,
            new SeparatorMenuItem(),
            clearCompletedItem,
            deleteAllItem
        );
        
        // Menu Ajuda
        Menu helpMenu = new Menu("Ajuda");
        MenuItem aboutItem = new MenuItem("Sobre");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);
        
        menuBar.getMenus().addAll(fileMenu, taskMenu, helpMenu);
        return menuBar;
    }
    
    /**
     * Cria barra de ferramentas
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        // Botão Nova Tarefa
        Button newButton = new Button("➕ Nova Tarefa");
        newButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 16;");
        newButton.setOnAction(e -> showCreateTaskDialog());
        
        // Campo de Busca
        searchField = new TextField();
        searchField.setPromptText("🔍 Buscar tarefas...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, old, keyword) -> {
            controller.searchTasks(keyword);
        });
        
        // ComboBox de Filtro
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
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Botão Atualizar
        Button refreshButton = new Button("🔄");
        refreshButton.setOnAction(e -> controller.loadAllTasks());
        refreshButton.setTooltip(new Tooltip("Atualizar"));
        
        toolbar.getChildren().addAll(
            newButton,
            new Separator(),
            new Label("Buscar:"),
            searchField,
            new Label("Filtrar:"),
            filterComboBox,
            spacer,
            refreshButton
        );
        
        return toolbar;
    }
    
    /**
     * Cria tabela de tarefas
     */
    private VBox createTaskTable() {
        VBox container = new VBox(5);
        container.setPadding(new Insets(10, 0, 10, 0));
        
        // Tabela
        taskTable = new TableView<>();
        taskTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        taskTable.setPlaceholder(new Label("Nenhuma tarefa encontrada"));
        
        // Coluna Status (emoji)
        TableColumn<Task, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(80);
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus().getEmoji()
            )
        );
        statusCol.setStyle("-fx-alignment: CENTER; -fx-font-size: 20px;");
        
        // Coluna Título
        TableColumn<Task, String> titleCol = new TableColumn<>("Título");
        titleCol.setPrefWidth(300);
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        // Coluna Descrição
        TableColumn<Task, String> descCol = new TableColumn<>("Descrição");
        descCol.setPrefWidth(250);
        descCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getShortDescription()
            )
        );
        
        // Coluna Data Criação
        TableColumn<Task, String> createdCol = new TableColumn<>("Criada em");
        createdCol.setPrefWidth(150);
        createdCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getFormattedCreatedAt()
            )
        );
        
        // Coluna Ações
        TableColumn<Task, Void> actionsCol = new TableColumn<>("Ações");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button completeBtn = new Button("✓");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttons = new HBox(5, editBtn, completeBtn, deleteBtn);
            
            {
                editBtn.setTooltip(new Tooltip("Editar"));
                completeBtn.setTooltip(new Tooltip("Concluir"));
                deleteBtn.setTooltip(new Tooltip("Deletar"));
                
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
        
        taskTable.getColumns().add(statusCol);
        taskTable.getColumns().add(titleCol);
        taskTable.getColumns().add(descCol);
        taskTable.getColumns().add(createdCol);
        taskTable.getColumns().add(actionsCol);
        
        // Menu de contexto (clique direito)
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
     * Cria menu de contexto (clique direito)
     */
    private ContextMenu createContextMenu() {
        ContextMenu menu = new ContextMenu();
        
        MenuItem editItem = new MenuItem("Editar");
        editItem.setOnAction(e -> {
            Task task = taskTable.getSelectionModel().getSelectedItem();
            if (task != null) showEditTaskDialog(task);
        });
        
        MenuItem completeItem = new MenuItem("Marcar como Concluída");
        completeItem.setOnAction(e -> {
            Task task = taskTable.getSelectionModel().getSelectedItem();
            if (task != null) controller.completeTask(task.getId());
        });
        
        MenuItem deleteItem = new MenuItem("Deletar");
        deleteItem.setOnAction(e -> {
            Task task = taskTable.getSelectionModel().getSelectedItem();
            if (task != null) confirmDeleteTask(task);
        });
        
        menu.getItems().addAll(editItem, completeItem, new SeparatorMenuItem(), deleteItem);
        return menu;
    }
    
    /**
     * Cria painel inferior (estatísticas + barra de status)
     */
    private VBox createBottomPanel() {
        VBox container = new VBox(5);
        container.setPadding(new Insets(10, 0, 0, 0));
        
        // Painel de estatísticas
        HBox statsPanel = createStatsPanel();
        
        // Barra de status
        statusLabel = new Label("Pronto");
        statusLabel.setStyle("-fx-padding: 5; -fx-background-color: #f0f0f0;");
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        
        container.getChildren().addAll(new Separator(), statsPanel, statusLabel);
        return container;
    }
    
    /**
     * Cria painel de estatísticas
     */
    private HBox createStatsPanel() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 5;");
        
        // Estatísticas (inicializa vazio, será preenchido depois)
        statsLabel = new Label("Total: 0  |  ⏳ Pendentes: 0  |  🚀 Em Progresso: 0  |  ✅ Concluídas: 0");
        
        // Barra de progresso
        VBox progressContainer = new VBox(3);
        Label progressLabel = new Label("Progresso:");
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressContainer.getChildren().addAll(progressLabel, progressBar);
        
        panel.getChildren().addAll(statsLabel, new Separator(), progressContainer);
        return panel;
    }
    
    // === CALLBACKS DO CONTROLLER ===
    
    /**
     * Configura callbacks do controller
     */
    private void setupControllerCallbacks() {
        System.out.println("🔵 MainWindow: Configurando callbacks...");
        
        // Quando lista mudar, atualiza tabela e stats
        controller.setOnTasksUpdated(tasks -> {
            System.out.println("🟢 MainWindow: Callback onTasksUpdated recebido com " + tasks.size() + " tarefa(s)");
            Platform.runLater(() -> {
                System.out.println("🔵 MainWindow: Atualizando TableView...");
                taskTable.getItems().setAll(tasks);
                System.out.println("🟢 MainWindow: TableView atualizada. Itens: " + taskTable.getItems().size());
                updateStats();
            });
        });
        
        // Mensagens de sucesso
        controller.setOnSuccess(message -> {
            System.out.println("🟢 MainWindow: Sucesso - " + message);
            Platform.runLater(() -> {
                statusLabel.setText("✅ " + message);
                statusLabel.setStyle("-fx-padding: 5; -fx-background-color: #d4edda; -fx-text-fill: #155724;");
            });
        });
        
        // Mensagens de erro
        controller.setOnError(message -> {
            System.err.println("🔴 MainWindow: Erro - " + message);
            Platform.runLater(() -> {
                statusLabel.setText("❌ " + message);
                statusLabel.setStyle("-fx-padding: 5; -fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                showErrorAlert(message);
            });
        });
        
        System.out.println("🟢 MainWindow: Callbacks configurados com sucesso");
    }
    
    // === AÇÕES ===
    
    /**
     * Aplica filtro selecionado
     */
    private void applyFilter() {
        String filter = filterComboBox.getValue();
        
        switch (filter) {
            case "Todas" -> controller.loadAllTasks();
            case "⏳ Pendentes" -> controller.loadTasksByStatus(TaskStatus.PENDENTE);
            case "🚀 Em Progresso" -> controller.loadTasksByStatus(TaskStatus.EM_PROGRESSO);
            case "✅ Concluídas" -> controller.loadCompletedTasks();
            case "📋 Ativas" -> controller.loadActiveTasks();
        }
    }
    
    /**
     * Atualiza estatísticas
     */
    private void updateStats() {
        // Verificação de segurança
        if (progressBar == null || statsLabel == null) {
            System.err.println("⚠️ updateStats chamado antes dos componentes serem criados");
            return;
        }
        
        long total = controller.getTotalTaskCount();
        long pending = controller.getTaskCountByStatus(TaskStatus.PENDENTE);
        long inProgress = controller.getTaskCountByStatus(TaskStatus.EM_PROGRESSO);
        long completed = controller.getTaskCountByStatus(TaskStatus.CONCLUIDA);
        double percent = controller.getCompletionPercentage();
        
        statsLabel.setText(String.format(
            "📊 Total: %d  |  ⏳ Pendentes: %d  |  🚀 Em Progresso: %d  |  ✅ Concluídas: %d",
            total, pending, inProgress, completed
        ));
        
        progressBar.setProgress(percent / 100.0);
    }
    
    // === DIÁLOGOS ===
    
    /**
     * Mostra diálogo para criar tarefa
     */
    private void showCreateTaskDialog() {
        System.out.println("🔵 MainWindow: Abrindo diálogo de criação...");
        TaskDialog dialog = new TaskDialog(stage);
        Optional<Task> result = dialog.showAndWait();
        
        result.ifPresent(task -> {
            System.out.println("🟢 MainWindow: Usuário confirmou. Título: " + task.getTitle());
            controller.createTask(task.getTitle(), task.getDescription());
        });
        
        if (result.isEmpty()) {
            System.out.println("⚪ MainWindow: Usuário cancelou o diálogo");
        }
    }
    
    /**
     * Mostra diálogo para editar tarefa
     */
    private void showEditTaskDialog(Task task) {
        TaskDialog dialog = new TaskDialog(stage, task);
        Optional<Task> result = dialog.showAndWait();
        
        result.ifPresent(editedTask -> {
            controller.updateTask(editedTask);
        });
    }
    
    /**
     * Confirma deleção de tarefa
     */
    private void confirmDeleteTask(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Deleção");
        alert.setHeaderText("Deletar tarefa?");
        alert.setContentText("Tem certeza que deseja deletar: \"" + task.getTitle() + "\"?");
        
        alert.showAndWait()
            .filter(response -> response == ButtonType.OK)
            .ifPresent(response -> controller.deleteTask(task.getId()));
    }
    
    /**
     * Confirma deleção de todas as tarefas
     */
    private void confirmDeleteAll() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Deleção");
        alert.setHeaderText("Deletar TODAS as tarefas?");
        alert.setContentText("Esta ação não pode ser desfeita!");
        
        alert.showAndWait()
            .filter(response -> response == ButtonType.OK)
            .ifPresent(response -> controller.deleteAllTasks());
    }
    
    /**
     * Confirma limpeza de tarefas concluídas
     */
    private void confirmClearCompleted() {
        long count = controller.getTaskCountByStatus(TaskStatus.CONCLUIDA);
        
        if (count == 0) {
            showInfoAlert("Nenhuma tarefa concluída para remover.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Limpeza");
        alert.setHeaderText("Limpar tarefas concluídas?");
        alert.setContentText(count + " tarefa(s) será(ão) removida(s).");
        
        alert.showAndWait()
            .filter(response -> response == ButtonType.OK)
            .ifPresent(response -> controller.deleteCompletedTasks());
    }
    
    /**
     * Mostra diálogo sobre
     */
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
     * Mostra alerta de erro
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Ocorreu um erro");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Mostra alerta de informação
     */
    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // === MÉTODOS PÚBLICOS ===
    
    /**
     * Mostra a janela
     */
    public void show() {
        stage.show();
    }
}