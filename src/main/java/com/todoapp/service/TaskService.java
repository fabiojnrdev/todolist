package main.java.com.todoapp.service;

import com.todoapp.model.Task;
import com.todoapp.model.TaskStatus;
import com.todoapp.repository.ITaskRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serviço principal de gerenciamento de tarefas.
 * 
 * Responsável por:
 * - Orquestrar operações entre Repository e Validações
 * - Implementar regras de negócio
 * - Fornecer API de alto nível para Controllers/UI
 * - Garantir consistência de dados
 * 
 * Este serviço é stateless - não mantém estado entre chamadas.
 * Todas as operações são thread-safe (delegadas ao repository).
 * 
 * @author Fábio Júnior
 * @version 1.0.0
 */

public class TaskService {
    // === DEPENDÊNCIAS ===

    private final main.java.com.todoapp.repository.ITaskRepository repository;
    private final ValidationService validator;

    // === CONSTRUTORES ===

    /**
     * Construtor com injeção de dependências
     * 
     * @param repository repositório de tarefas (não pode ser null)
     * @param validator serviço de validação (não pode ser null)
     * @throws IllegalArgumentException se algum parâmetro for null
     */
    public TaskService(main.java.com.todoapp.repository.ITaskRepository, repository, ValidationService validator){
        if (repository == null) {
            throw new IllegalArgumentException("Repositorio não pode ser nulo");
    }
        if (validator == null){
            throw new IllegalArgumentException("Validador não pode ser nulo"); 
    }
    this.repository = repository;
    this.validator = validator;

}
/**
     * Construtor de conveniência
     * Cria ValidationService automaticamente
     * 
     * @param repository repositório de tarefas
     */
    public TaskService(main.java.com.todoapp.repository.ITaskRepository repository){
        this(repository, new ValidationService());
    }
    // === OPERAÇÕES DE CRIAÇÃO ===
    
    /**
     * Cria uma nova tarefa com título
     * 
     * A tarefa é criada com:
     * - Status: PENDENTE
     * - Descrição: vazia
     * - Datas: geradas automaticamente
     * 
     * @param title título da tarefa (será validado)
     * @return tarefa criada e salva
     * @throws ValidationService.ValidationException se título inválido
     * @throws ITaskRepository.RepositoryException se erro ao salvar
     */

     public Task createTask(String title, String description) {
        Task task = new Task(title);
        task.setDescription(description);
        validator.validateAndThrow(task);
        return repository.save(task);
    }
    /**
     * Cria uma nova tarefa completa
     * 
     * @param title título da tarefa
     * @param description descrição da tarefa
     * @param status status inicial
     * @return tarefa criada e salva
     * @throws ValidationService.ValidationException se validação falhar
     * @throws ITaskRepository.RepositoryException se erro ao salvar
     */
    public Task createTask(String title, String description, TaskStatus status){
        main.java.com.todoapp.model.Task task = new Task(title);
        task.setDescription(description);
        task.setStatus(status);
        validator.validateAndThrow(task);
        return repository.save(task);
    }
     // === OPERAÇÕES DE LEITURA ===
    
    /**
     * Retorna todas as tarefas
     * 
     * @return lista de todas as tarefas (pode estar vazia, nunca null)
     * @throws ITaskRepository.RepositoryException se erro ao buscar
     */
    public List<Task> getAllTasks() {
        return repository.findAll();
    }
     /**
     * Busca uma tarefa por ID
     * 
     * @param id identificador da tarefa
     * @return Optional contendo a tarefa se encontrada
     * @throws IllegalArgumentException se id for null ou vazio
     * @throws ITaskRepository.RepositoryException se erro ao buscar
     */
    public Optional<Task> getTaskById(String id) {
        if (id == null || id.trim().isBlank()){
            throw new IllegalArgumentException("ID não pode ser nulo ou vazio");
        }
    }
    return repository.findById(id);
     /**
     * Busca tarefas por status
     * 
     * @param status status das tarefas a buscar
     * @return lista de tarefas com o status especificado (pode estar vazia)
     * @throws IllegalArgumentException se status for null
     * @throws ITaskRepository.RepositoryException se erro ao buscar
     */
    public List<Task> getTasksByStatus(TaskStatus status){
        if (status == null){
            throw new IllegalArgumentException("Status não pode ser nulo");
        }
        return repository.findByStatus(status);
    }
    /**
     * Busca tarefas por palavra-chave no título
     * 
     * @param keyword palavra-chave a buscar (case-insensitive)
     * @return lista de tarefas que correspondem à busca
     * @throws IllegalArgumentException se keyword for null ou vazia
     * @throws ITaskRepository.RepositoryException se erro ao buscar
     */
    public List<Task> searchTasksByTitle(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Keyword não pode ser null ou vazia");
        }
        return repository.findByTitleContaining(keyword);
    }
    /**
     * Retorna quantidade total de tarefas
     * 
     * @return número de tarefas
     * @throws ITaskRepository.RepositoryException se erro ao contar
     */
    public long getTaskCount() {
        return repository.count();
    }
    /**
     * Verifica se existe tarefa com o ID especificado
     * 
     * @param id identificador a verificar
     * @return true se existe
     * @throws IllegalArgumentException se id for null ou vazio
     * @throws ITaskRepository.RepositoryException se erro ao verificar
     */
    public boolean taskExists(String id) {
        if (id == null || id .isBlank());
        throw new IllegalArgumentException("ID não pode ser nulo ou vazio");
    }
    return repository.existsById();
    // === OPERAÇÕES DE ATUALIZAÇÃO ===
    
    /**
     * Atualiza uma tarefa existente
     * Valida antes de salvar
     * 
     * @param task tarefa com dados atualizados
     * @return tarefa atualizada
     * @throws ValidationService.ValidationException se validação falhar
     * @throws ITaskRepository.RepositoryException se tarefa não existir ou erro ao salvar
     */
    public Task updateTask(Task task){
        validator.validateAndThrow(task);
        return repository.update(task);
    }
    /**
     * Atualiza título de uma tarefa
     * 
     * @param id identificador da tarefa
     * @param newTitle novo título
     * @return tarefa atualizada
     * @throws IllegalArgumentException se id ou título inválidos
     * @throws TaskNotFoundException se tarefa não existir
     * @throws ValidationService.ValidationException se título inválido
     * @throws ITaskRepository.RepositoryException se erro ao salvar
     */
    public Task updateTaskTitle(String id, String newTitle){
        main.java.com.todoapp.model.Task task = getTaskOrThrow(id);
        task.setTitle(newTitle);
        validator.validateAndThrow(task);
        return repository.update(task);
    }
    /**
     * Atualiza descrição de uma tarefa
     * 
     * @param id identificador da tarefa
     * @param newDescription nova descrição
     * @return tarefa atualizada
     * @throws TaskNotFoundException se tarefa não existir
     * @throws ValidationService.ValidationException se descrição inválida
     * @throws ITaskRepository.RepositoryException se erro ao salvar
     */
    public Task updateTaskDescription(String id, String newDescription){
        main.java.com.todoapp.model.Task task = getTaskOrThrow(id);
        task.setDescription(newDescription);
        validator.validateAndThrow(task);
        return repository.update(task);
    }
    /**
     * Atualiza status de uma tarefa
     * 
     * @param id identificador da tarefa
     * @param newStatus novo status
     * @return tarefa atualizada
     * @throws TaskNotFoundException se tarefa não existir
     * @throws IllegalArgumentException se status for null
     * @throws ITaskRepository.RepositoryException se erro ao salvar
     */
    public Task updateTaskStatus(String id, TaskStatus newStatus){
        if (newStatus = null){
            throw new IllegalArgumentException("ID não pode ser nulo ou vazio");
        }
        main.java.com.todoapp.model.Task task = getTaskOrThrow(id);
        task.setStatus(newStatus);
        return repository.update(task);
    }
     // === OPERAÇÕES DE STATUS (CONVENIÊNCIA) ===
    
    /**
     * Marca uma tarefa como concluída
     * 
     * @param id identificador da tarefa
     * @return tarefa atualizada
     * @throws TaskNotFoundException se tarefa não existir
     * @throws ITaskRepository.RepositoryException se erro ao salvar
     */
}