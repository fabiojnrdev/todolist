
package main.java.com.todoapp.repository;

import com.todoapp.model.Task;
import com.todoapp.model.TaskStatus;

import java.util.List;
import java.util.Optional;

/**
 * Interface que define o contrato de persistência de tarefas.
 * 
 * Esta interface segue o padrão Repository, abstraindo a camada de dados
 * e permitindo múltiplas implementações (arquivo, banco de dados, memória, etc).
 * 
 * Benefícios:
 * - Desacoplamento entre lógica de negócio e persistência
 * - Facilita testes (mocks/stubs)
 * - Permite trocar implementação sem afetar outras camadas
 * - Princípio da Inversão de Dependência (SOLID)
 * 
 * @author Todo App Team
 * @version 1.0.0
 */
public interface ITaskRepository {
    
    // === OPERAÇÕES BÁSICAS (CRUD) ===
    
    /**
     * Salva uma nova tarefa no repositório
     * 
     * @param task tarefa a ser salva (não pode ser null)
     * @return tarefa salva (pode incluir campos gerados, como ID)
     * @throws IllegalArgumentException se task for null
     * @throws RepositoryException se houver erro na persistência
     */
    Task save(Task task);
    
    /**
     * Atualiza uma tarefa existente
     * Se a tarefa não existir, pode lançar exceção ou criar nova
     * (comportamento definido pela implementação)
     * 
     * @param task tarefa com dados atualizados (não pode ser null)
     * @return tarefa atualizada
     * @throws IllegalArgumentException se task for null
     * @throws RepositoryException se houver erro na persistência
     */
    Task update(Task task);
    
    /**
     * Remove uma tarefa do repositório pelo ID
     * Se o ID não existir, não faz nada (idempotente)
     * 
     * @param id identificador da tarefa (não pode ser null ou vazio)
     * @return true se removeu, false se não encontrou
     * @throws IllegalArgumentException se id for null ou vazio
     * @throws RepositoryException se houver erro na persistência
     */
    boolean delete(String id);
    
    /**
     * Busca uma tarefa pelo ID
     * 
     * @param id identificador da tarefa (não pode ser null ou vazio)
     * @return Optional contendo a tarefa se encontrada, ou vazio
     * @throws IllegalArgumentException se id for null ou vazio
     * @throws RepositoryException se houver erro ao buscar
     */
    Optional<Task> findById(String id);
    
    /**
     * Retorna todas as tarefas do repositório
     * Se não houver tarefas, retorna lista vazia (nunca null)
     * 
     * @return lista de todas as tarefas (pode estar vazia)
     * @throws RepositoryException se houver erro ao buscar
     */
    List<Task> findAll();
    
    // === OPERAÇÕES DE BUSCA (QUERIES) ===
    
    /**
     * Busca tarefas por status
     * 
     * @param status status das tarefas a buscar (não pode ser null)
     * @return lista de tarefas com o status especificado (pode estar vazia)
     * @throws IllegalArgumentException se status for null
     * @throws RepositoryException se houver erro ao buscar
     */
    List<Task> findByStatus(TaskStatus status);
    
    /**
     * Busca tarefas cujo título contenha a palavra-chave (case-insensitive)
     * 
     * @param keyword palavra-chave a buscar (não pode ser null ou vazia)
     * @return lista de tarefas que correspondem à busca (pode estar vazia)
     * @throws IllegalArgumentException se keyword for null ou vazia
     * @throws RepositoryException se houver erro ao buscar
     */
    List<Task> findByTitleContaining(String keyword);
    
    // === OPERAÇÕES DE UTILIDADE ===
    
    /**
     * Verifica se existe uma tarefa com o ID especificado
     * 
     * @param id identificador a verificar (não pode ser null ou vazio)
     * @return true se existe, false caso contrário
     * @throws IllegalArgumentException se id for null ou vazio
     * @throws RepositoryException se houver erro ao verificar
     */
    boolean existsById(String id);
    
    /**
     * Conta o número total de tarefas no repositório
     * 
     * @return quantidade de tarefas
     * @throws RepositoryException se houver erro ao contar
     */
    long count();
    
    /**
     * Remove todas as tarefas do repositório
     * ATENÇÃO: Operação destrutiva e irreversível!
     * 
     * @throws RepositoryException se houver erro ao limpar
     */
    void deleteAll();
    
    // === EXCEÇÃO CUSTOMIZADA ===
    
    /**
     * Exceção lançada quando há erro nas operações de repositório
     * (leitura, escrita, parsing, etc)
     */
    class RepositoryException extends RuntimeException {
        
        public RepositoryException(String message) {
            super(message);
        }
        
        public RepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}