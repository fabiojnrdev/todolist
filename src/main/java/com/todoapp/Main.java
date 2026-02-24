package com.todoapp;

import com.todoapp.controller.TaskController;
import com.todoapp.repository.FileTaskRepository;
import com.todoapp.service.TaskService;
import com.todoapp.view.MainWindow;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Ponto de entrada da aplicação Todo Manager.
 *
 * Responsabilidades:
 * - Inicializar o JavaFX
 * - Montar o grafo de dependências (DI manual)
 * - Exibir a janela principal
 *
 * @author Fábio Júnior
 * @version 1.0.0
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // === COMPOSIÇÃO DE DEPENDÊNCIAS (Dependency Injection manual) ===
        FileTaskRepository repository = new FileTaskRepository();
        TaskService taskService = new TaskService(repository);
        TaskController controller = new TaskController(taskService);

        // === JANELA PRINCIPAL ===
        MainWindow mainWindow = new MainWindow(primaryStage, controller);
        mainWindow.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}