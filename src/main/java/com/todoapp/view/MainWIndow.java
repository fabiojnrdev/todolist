package com.todoapp.view;

import java.awt.Menu;
import java.awt.MenuBar;

import com.todoapp.controller.TaskController;
import com.todoapp.model.Task;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
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
 * Componentes: - Barra de ferramentas (busca, filtros, botões) - Tabela de
 * tarefas - Painel de estatísticas - Barra de status
 *
 * Esta classe é responsável apenas pela UI. Toda lógica é delegada ao
 * TaskController.
 *
 * @author Fábio Júnior
 * @version 1.0.0
 */
public class MainWIndow {

    // Componentes principais
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

    // Construção 
    /**
     * Construtor da janela principal
     *
     * @param stage stage principal do JavaFX
     * @param controller controller de tarefas
     */
    public MainWIndow(Stage stage, TaskController controller) {
        this.stage = stage;
        this.controller = controller;

        // Configuração de callbacks do controlador
        setupControllerCallbacks();

        // Cria interface
        initializeUI();

        // Carrega dados iniciais
        controller.loadAllTasks();
    }
        // Inicialização da UI

        /**
         * Inicializa todos os componentes
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
    // Cria menu e barra de ferramentas combinados

    private VBox createMenuAndToolbar() {
        VBox container = new VBox();

        // Menu Bar
        MenuBar menuBar = createMenuBar();

        // Toolbar
        ToolBar toolBar = createToolbar();

        container.getChildren().addAll(menuBar, toolBar);
        return container;
        /**
         * Cria barra de menu
         */
    private menuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Menu arquivo
        Menu fileMenu = new Menu("Arquivo");
        MenuItem exitItem = new Menu("Sair");
        exitItem.setOnAction(e -> Platform.exit());
        fileMenu.getItems().addAll(exitItem);

        // Menu tarefas
        Menu taskMenu = new Menu("Tarefas");
        java.awt.MenuItem newTaskItem = new java.awt.MenuItem("Nova tarefa");
        newTaskItem.setOnAction(e -> showCreateTaskDialog());

        MenuItem deleteAllItem = new MenuItem("Deletar todas as tarefas");
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
    private HBox createToolbar(){
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
        // ComboBox de filtro
        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll(
            "Todas",
            "Pendentes",
            "Em Progresso",
            "Concluídas",
            "Ativas"
        );
        filterComboBox.setValue("Todas");
        filterComboBox.setOnAction(e -> applyFilter());

        // Spacer

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Botão atualizar
        java.awt.Button refreshButton = newButton("🔄");
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
}
