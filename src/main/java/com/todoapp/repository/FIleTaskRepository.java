package com.todoapp.repository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.todoapp.model.Task;
import com.todoapp.model.TaskStatus;
import com.todoapp.util.LocalDateTimeAdapter;

/**
 * Implementação do repositório que persiste tarefas em arquivo JSON.
 *
 * Características:
 * - Arquivo padrão: src/main/resources/data/tasks.json
 * - Formato: JSON Array de objetos Task
 * - Thread-safe: usa ReadWriteLock para leitura concorrente / escrita exclusiva
 * - Auto-criação: cria arquivo e diretórios se não existirem
 *
 * Estrutura do JSON:
 * <pre>
 * [
 *   {
 *     "id":          "abc-123",
 *     "title":       "Estudar Java",
 *     "description": "Capítulo 5",
 *     "status":      "PENDENTE",
 *     "createdAt":   "2026-01-27T14:30:00",
 *     "updatedAt":   "2026-01-27T14:30:00",
 *     "completedAt": null
 *   }
 * ]
 * </pre>
 *
 * @author Fábio Júnior
 * @version 1.0.0
 */
public class FileTaskRepository implements ITaskRepository {

    // === CONSTANTES ===
    private static final String DEFAULT_DATA_DIR = "src/main/resources/data";
    private static final String DEFAULT_FILE_PATH = DEFAULT_DATA_DIR + "/tasks.json";

    // === DEPENDÊNCIAS ===
    private final Gson gson;
    private final File file;
    private final ReadWriteLock lock;

    // === CONSTRUTORES ===

    /**
     * Construtor padrão — usa o caminho padrão do projeto.
     */
    public FileTaskRepository() {
        this(DEFAULT_FILE_PATH);
    }

    /**
     * Construtor com caminho personalizado.
     * Útil para testes (ex: arquivo temporário).
     *
     * @param filePath caminho completo para o arquivo JSON
     */
    public FileTaskRepository(String filePath) {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        this.file = new File(filePath);  // CORREÇÃO: usava FILE_PATH (constante) em vez do parâmetro
        this.lock = new ReentrantReadWriteLock();
        initializeFile();
    }

    // === INICIALIZAÇÃO ===

    /**
     * Garante que o arquivo e o diretório existam.
     * Se o arquivo não existir, cria com lista vazia.
     */
    private void initializeFile() {
        try {
            File dir = file.getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
                writeToFile(new ArrayList<>());
            }
        } catch (IOException e) {
            throw new RepositoryException(
                    "Erro ao inicializar arquivo: " + file.getAbsolutePath(), e);
        }
    }

    // === OPERAÇÕES BÁSICAS (CRUD) ===

    @Override
    public Task save(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task não pode ser nula");
        }
        lock.writeLock().lock();
        try {
            List<Task> tasks = readFromFile();
            boolean alreadyExists = tasks.stream()
                    .anyMatch(t -> t.getId().equals(task.getId()));
            if (alreadyExists) {
                throw new RepositoryException("Task com esse ID já existe: " + task.getId());
            }
            tasks.add(task);
            writeToFile(tasks);
            return task;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Task update(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task não pode ser nula");
        }
        lock.writeLock().lock();
        try {
            List<Task> tasks = readFromFile();
            int index = findIndexById(tasks, task.getId());
            if (index == -1) {
                throw new RepositoryException("Task não encontrada para update: " + task.getId());
            }
            tasks.set(index, task);
            writeToFile(tasks);
            return task;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean delete(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID não pode ser nulo ou vazio");
        }
        lock.writeLock().lock();
        try {
            List<Task> tasks = readFromFile();
            boolean removed = tasks.removeIf(t -> t.getId().equals(id));
            if (removed) {
                writeToFile(tasks);
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Task> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID não pode ser nulo ou vazio");
        }
        lock.readLock().lock();
        try {
            return readFromFile().stream()
                    .filter(t -> t.getId().equals(id))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Task> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(readFromFile());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Task> findByStatus(TaskStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status não pode ser nulo");
        }
        lock.readLock().lock();
        try {
            return readFromFile().stream()
                    .filter(t -> t.getStatus() == status)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Task> findByTitleContaining(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Keyword não pode ser nula ou vazia");
        }
        lock.readLock().lock();
        try {
            String lowerKeyword = keyword.toLowerCase();
            return readFromFile().stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(lowerKeyword))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public long count() {
        lock.readLock().lock();
        try {
            return readFromFile().size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean existsById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID não pode ser nulo ou vazio");
        }
        return findById(id).isPresent();
    }

    @Override
    public void deleteAll() {
        lock.writeLock().lock();
        try {
            writeToFile(new ArrayList<>());
        } finally {
            lock.writeLock().unlock();
        }
    }

    // === I/O PRIVADO ===

    /**
     * Lê a lista de tarefas do arquivo JSON.
     * Em caso de erro ou arquivo corrompido, retorna lista vazia.
     */
    private List<Task> readFromFile() {
        try (FileReader reader = new FileReader(file)) {
            Type taskListType = new TypeToken<ArrayList<Task>>() {}.getType();
            List<Task> tasks = gson.fromJson(reader, taskListType);
            return tasks != null ? tasks : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("AVISO: Erro ao ler arquivo. Iniciando com lista vazia. " + e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("AVISO: Arquivo JSON corrompido. Iniciando com lista vazia. " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Escreve a lista de tarefas no arquivo JSON (sobrescreve).
     */
    private void writeToFile(List<Task> tasks) {
        try (FileWriter writer = new FileWriter(file, false)) {
            gson.toJson(tasks, writer);
            writer.flush();
        } catch (IOException e) {
            throw new RepositoryException(
                    "Erro ao escrever no arquivo: " + file.getAbsolutePath(), e);
        }
    }

    // === UTILITÁRIOS PRIVADOS ===

    /**
     * Retorna o índice de uma task na lista pelo ID, ou -1 se não encontrada.
     */
    private int findIndexById(List<Task> tasks, String id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    // === UTILITÁRIOS PÚBLICOS ===

    /**
     * Retorna o caminho absoluto do arquivo de dados.
     */
    public String getFilePath() {
        return file.getAbsolutePath();
    }

    /**
     * Verifica se o arquivo de dados existe.
     */
    public boolean fileExists() {
        return file.exists();
    }

    // === EXCEÇÃO CUSTOMIZADA ===

    /**
     * Exceção lançada em erros de acesso ao repositório.
     */
    public static class RepositoryException extends RuntimeException {

        public RepositoryException(String message) {
            super(message);
        }

        public RepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}