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

    private final ITaskRepository repository;
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
}
