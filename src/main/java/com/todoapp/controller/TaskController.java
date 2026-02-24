package com.todoapp.controller;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.todoapp.model.Task;
import com.todoapp.model.TaskStatus;
import com.todoapp.service.TaskService;
import com.todoapp.service.ValidationService;

/**
 * Controller principal da aplicação de tarefas.
 *
 * Responsabilidades: - Receber ações da View (UI) - Chamar TaskService para
 * executar operações - Tratar exceções e converter em respostas para UI -
 * Notificar View sobre mudanças (via callbacks)
 *
 * Padrão MVC: View ← → Controller ← → Service (Model)
 *
 * Este controller NÃO conhece detalhes da UI (JavaFX, Swing, etc). Usa
 * callbacks genéricos para comunicação com View.
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

// === MÉTODOS AUXILIARES ===
    /**
     * Notifica a View sobre sucesso
     *
     * @param message mensagem de sucesso
     */
    private void notifySuccess(String message) {
        if (onSuccess != null) {
            onSuccess.accept(message);
        }
    }

    /**
     * Notifica a View sobre erro
     *
     * @param message mensagem de erro
     */
    private void notifyError(String message) {
        if (onError != null) {
            onError.accept(message);
        }
    }

    /**
     * Atualiza a lista de tarefas na View
     */
    private void refreshTaskList() {
        if (onTasksUpdated != null) {
            onTasksUpdated.accept(taskService.getAllTasks());
        }
    }

// === Operadores de criação ===
    /**
     * Cria uma nova tarefa com título e descrição
     *
     * @param title título da tarefa (não pode ser null ou vazio)
     * @param description descrição da tarefa
     */
    public void createTask(String title, String description) {
        try {
            taskService.createTask(title, description);
            notifySuccess("Tarefa criada com sucesso!");
            refreshTaskList();

        } catch (ValidationService.ValidationException e) {
            notifyError("Erro de validação: " + e.getMessage());
        } catch (Exception e) {
            notifyError("Erro ao criar tarefa: " + e.getMessage());
        }
    }

    /**
     * Cria nova tarefa completa
     *
     * @param title título da tarefa
     * @param description descrição da tarefa
     * @param status status inicial
     *
     */
    public void createTask(String title, String description, TaskStatus status) {
        try {
            Task task = taskService.createTask(title, description, status);
            notifySuccess("Tarefa criada com status " + status.getDisplayName());
            refreshTaskList();

        } catch (ValidationService.ValidationException e) {
            notifyError("Erro de validação: " + e.getMessage());
        } catch (Exception e) {
            notifyError("Erro ao criar tarefa: " + e.getMessage());
        }
    }
    // === OPERAÇÕES DE LEITURA ===

    /**
     * Carrega todas as tarefas Notifica View via callback onTasksChanged
     */
    public void loadAllTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            refreshTaskList();

        } catch (Exception e) {
            notifyError("Erro ao carregar tarefas: " + e.getMessage());
        }
    }

    /**
     * Carrega tarefas por status
     *
     * @param status status a filtrar
     */
    public void loadTasksByStatus(TaskStatus status) {
        try {
            List<Task> tasks = taskService.getTasksByStatus(status);
            refreshTaskList();

        } catch (Exception e) {
            notifyError("Erro ao carregar tarefas: " + e.getMessage());
        }
    }

    /**
     * Carrega tarefas por palavra-chave
     *
     * @param keyword palavra-chave a buscar
     */
    public void searchTasks(String keyword) {
        try {
            if (keyword == null || keyword.isBlank()) {
                loadAllTasks();  // se vazio, mostra todas
                return;
            }

            List<Task> tasks = taskService.searchTasksByTitle(keyword);
            refreshTaskList();

        } catch (Exception e) {
            notifyError("Erro ao buscar tarefas: " + e.getMessage());
        }
    }

    /**
     * Carrega tarefas ativas (não concluídas)
     */
    public void loadActiveTasks() {
        try {
            List<Task> tasks = taskService.getActiveTasks();
            notifyTasksChanged(tasks);

        } catch (Exception e) {
            notifyError("Erro ao carregar tarefas ativas: " + e.getMessage());
        }
    }

    /**
     * Carrega tarefas concluídas
     */
    public void loadCompletedTasks() {
        try {
            List<Task> tasks = taskService.getCompletedTasks();
            notifyTasksChanged(tasks);

        } catch (Exception e) {
            notifyError("Erro ao carregar tarefas concluídas: " + e.getMessage());
        }
    }

    /**
     * Busca uma tarefa específica por ID
     *
     * @param id identificador da tarefa
     * @return Optional contendo a tarefa se encontrada
     */
    public Optional<Task> getTaskById(String id) {
        try {
            return taskService.getTaskById(id);
        } catch (Exception e) {
            notifyError("Erro ao buscar tarefa: " + e.getMessage());
            return Optional.empty();
        }
    }
    // === OPERAÇÕES DE ATUALIZAÇÃO ===

    /**
     * Atualiza uma tarefa
     *
     * @param task tarefa com dados atualizados
     */
    public void updateTask(Task task) {
        try {
            taskService.updateTask(task);
            notifySuccess("Tarefa atualizada com êxito!");
            refreshTaskList();
        } catch (ValidationService.ValidationException e) {
            notifyError("Erro de validação: " + e.getMessage());
        } catch (Exception e) {
            notifyError("Erro ao atualizar tarefa: " + e.getMessage());
        }
    }

    /**
     * Atualiza título de uma tarefa
     *
     * @param id identificador da tarefa
     * @param newTitle novo título
     */
    public void updateTaskTitle(String id, String newTitle) {
        try {
            taskService.updateTaskTitle(id, newTitle);
            notifySuccess("Título da tarefa atualizado com êxito!");
            refreshTaskList();
        } catch (TaskService.TaskNotFoundException e) {
            notifyError("Tarefa não encontrada: " + e.getMessage());
        } catch (ValidationService.ValidationException e) {
            notifyError("Erro de validação: " + e.getMessage());
        } catch (Exception e) {
            notifyError("Erro ao atualizar título da tarefa: " + e.getMessage());
        }
    }

    /**
     * Atualiza descrição de uma tarefa
     *
     * @param id identificador da tarefa
     * @param newDescription nova descrição
     */
    public void updateTaskDescription(String id, String newDescription) {
        try {
            taskService.updateTaskDescription(id, newDescription);
            notifySuccess("Descrição da tarefa atualizada com êxito!");
            refreshTaskList();
        } catch (TaskService.TaskNotFoundException e) {
            notifyError("Tarefa não encontrada: " + e.getMessage());
        } catch (ValidationService.ValidationException e) {
            notifyError("Erro de validação: " + e.getMessage());
        } catch (Exception e) {
            notifyError("Erro ao atualizar descrição da tarefa: " + e.getMessage());
        }
    }

    /**
     * Atualiza status de uma tarefa
     *
     * @param id identificador da tarefa
     * @param newStatus novo status
     */
    public void updateTaskStatus(String id, TaskStatus newStatus) {
        try {
            taskService.updateTaskStatus(id, newStatus);
            notifySuccess("Status da tarefa atualizado com êxito!");
            refreshTaskList();
        } catch (TaskService.TaskNotFoundException e) {
            notifyError("Tarefa não encontrada: " + e.getMessage());
        } catch (Exception e) {
            notifyError("Erro ao atualizar status da tarefa: " + e.getMessage());
        }
    }
    // Operações de status

    /**
     * Marca tarefa como concluída
     *
     * @param id identificador da tarefa
     */
    public void completeTask(String id) {
        try {
            taskService.completeTask(id);
            notifySuccess("Tarefa marcada como concluída!");
            refreshTaskList();
        } catch (TaskService.TaskNotFoundException e) {
            notifyError("Tarefa não encontrada: " + e.getMessage());
        } catch (Exception e) {
            notifyError("Erro ao marcar tarefa como concluída: " + e.getMessage());
        }
    }

    /**
     * Avança tarefa para próximo status (ex: Pendente → Em Progresso →
     * Concluída)
     *
     * @param id identificador da tarefa
     */
    public void advanceTaskStatus(String id) {
        try {
            taskService.advanceTaskStatus(id);
            notifySuccess("Status da tarefa avançado com êxito!");
            refreshTaskList();
        } catch (TaskService.TaskNotFoundException e) {
            notifyError("Tarefa não encontrada: " + e.getMessage());
        } catch (Exception e) {
            notifyError("Erro ao avançar status da tarefa: " + e.getMessage());
        }
    }

    /**
     * Retrocede tarefa para status anterior
     *
     * @param id identificador da tarefa
     */
    public void revertTaskStatus(String id) {
        try {
            taskService.revertTaskStatus(id);
            notifySuccess("Status da tarefa retrocedido com êxito!");
            refreshTaskList();
        } catch (TaskService.TaskNotFoundException e) {
            notifyError("Tarefa não encontrada: " + e.getMessage());
        } catch (Exception e) {
            notifyError("Erro ao retroceder status da tarefa: " + e.getMessage());
        }
    }

    // Operações de deleção
    /**
     * Deleta uma tarefa
     *
     * @param id identificador da tarefa
     */
    public void deleteTask(String id) {
        try {
            boolean deleted = taskService.deleteTask(id);
            if (deleted) {
                notifySuccess("Tarefa deletada com êxito!");
                refreshTaskList();
            } else {
                notifyError("Tarefa não encontrada para deleção.");
            }
        } catch (Exception e) {
            notifyError("Erro ao deletar tarefa: " + e.getMessage());
        }
    }

    /**
     * Deleta todas as tarefas Operação perigosa, deve ser confirmada pela UI
     * antes de chamar
     */
    public void deleteAllTasks() {
        try {
            taskService.deleteAllTasks();
            notifySuccess("Todas as tarefas foram deletadas");
            refreshTaskList();

        } catch (Exception e) {
            notifyError("Erro ao deletar tarefas: " + e.getMessage());
        }
    }

    /**
     * Deleta todas as tarefas concluídas
     *
     * @return quantidade de tarefas deletadas
     */
    public int deleteCompletedTasks() {
        try {
            int count = taskService.deleteCompletedTasks();
            if (count > 0) {
                notifySuccess(count + " tarefas concluídas foram deletadas");
                refreshTaskList();
            } else {
                notifySuccess("Nenhuma tarefa concluída para deletar");
            }
            return count;
        } catch (Exception e) {
            notifyError("Erro ao deletar tarefas concluídas: " + e.getMessage());
            return 0;
        }
    }
    // Operações de estatísticas

    /**
     * Retorna porcentagem de tarefas concluídas
     *
     * @return porcentagem (0-100)
     */
    public double getCompletionPercentage() {
        try {
            return taskService.getCompletionPercentage();
        } catch (Exception e) {
            notifyError("Erro ao calcular porcentagem de conclusão: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Retorna quantidade total de tarefas
     *
     * @return número de tarefas
     */
    public long getTotalTaskCount() {
        try {
            return taskService.getTotalTaskCount();
        } catch (Exception e) {
            notifyError("Erro ao contar tarefas: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Retorna quantidade de tarefas por status
     *
     * @param status status a contar
     * @return quantidade de tarefas
     */
    public long getTaskCountByStatus(TaskStatus status) {
        try {
            return taskService.countTasksByStatus(status);
        } catch (Exception e) {
            notifyError("Erro ao contar tarefas: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Verifica se há tarefas pendentes
     *
     * @return true se existem tarefas pendentes
     */
    public boolean hasPendingTasks() {
        try {
            return taskService.hasPendingTasks();
        } catch (Exception e) {
            notifyError("Erro ao verificar tarefas pendentes: " + e.getMessage());
            return false;
        }
        // Metódos auxiliares privados
private void refreshTaskList() {
        loadAllTasks();
    }

    private void notifyTasksChanged(List<Task> tasks) {
        if (onTasksUpdated != null) {
            onTasksUpdated.accept(tasks);
        }
    }
    /**
     * Notifica View sobre sucesso na operação
     * 
     * @param message mensagem de sucesso
     */
    private void notifySuccess(String message) {
        if (onSuccess != null) {
            onSuccess.accept(message);
        }
    }
    /**
     * Notifica View sobre erro na operação
     * 
     * @param message mensagem de erro
     */
    private void notifyError(String message) {
        if (onError != null) {
            onError.accept(message);
        }
    }
    // Acesso ao service
    
    /**
     * Retorna referência ao TaskService
     * Use com cuidado - prefira usar métodos do controller
     * 
     * @return serviço de tarefas
     */
    public TaskService getTaskService() {
        return taskService;
    }
}
