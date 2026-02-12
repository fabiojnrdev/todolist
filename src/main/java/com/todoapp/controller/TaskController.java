package com.todoapp.controller;

import com.todoapp.model.TaskStatus;
import com.todoapp.model.Task;
import com.todoapp.service.TaskService;
import com.todoapp.service.ValidationService;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Controller principal da aplicação de tarefas.
 * 
 * Responsabilidades:
 * - Receber ações da View (UI)
 * - Chamar TaskService para executar operações
 * - Tratar exceções e converter em respostas para UI
 * - Notificar View sobre mudanças (via callbacks)
 * 
 * Padrão MVC:
 * View ← → Controller ← → Service (Model)
 * 
 * Este controller NÃO conhece detalhes da UI (JavaFX, Swing, etc).
 * Usa callbacks genéricos para comunicação com View.
 * 
 * @author Fábio Júnior
 * @version 1.0.0
 */

public class TaskController {
    // Dependências
    private final TaskService taskService;

    // Callbacks para comunicação com a View
    private Consumer<List<Task>> onTasksUpdated; // Callback para atualizar lista de tarefas na UI
    private Consumer<String> onSuccess; // Callback para mensagens de sucesso
    private Consumer<String> onError; // Callback para mensagens de erro

    // === CONSTRUTOR ===

    /**
     * Construtor com injeção de dependência
     * 
     * @param taskService serviço de tarefas (não pode ser null)
     * @throws IllegalArgumentException se taskService for null
     */
    public TaskController(TaskService taskService) {
        if (taskService == null) {
            throw new IllegalArgumentException("TaskService não pode ser nulo ou vazio");
        }
        this.taskService = taskService;
    }

    // === CALLBACKS ===
    /**
     * Define callback para quando lista de tarefas mudar
     * 
     * @param callback função que recebe List<Task>
     */
    public void setOnTasksUpdated(Consumer<List<Task>> callback) {
        this.onTasksUpdated = callback;
    }

    /**
     * Define callback para mensagens de sucesso
     * 
     * @param callback função que recebe String
     */
    public void setOnSuccess(Consumer<String> callback) {
        this.onSuccess = callback;
    }

    /**
     * Define callback para mensagens de erro
     * 
     * @param callback função que recebe String
     */
    public void setOnError(Consumer<String> callback) {
        this.onError = callback;
    }
}
