package main.java.com.todoapp.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.todoapp.model.Task;
import com.todoapp.model.TaskStatus;

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

/**
 * Implementação do repositório que persiste tarefas em arquivo JSON.
 * 
 * Características:
 * - Arquivo: src/main/resources/data/tasks.json
 * - Formato: JSON Array de objetos Task
 * - Thread-safe: usa ReadWriteLock para sincronização
 * - Auto-criação: cria arquivo e diretórios se não existirem
 * 
 * Estrutura do JSON:
 * [
 * {
 * "id": "abc-123",
 * "title": "Estudar Java",
 * "description": "Capítulo 5",
 * "status": "PENDENTE",
 * "createdAt": "2026-01-27T14:30:00",
 * "updatedAt": "2026-01-27T14:30:00",
 * "completedAt": null
 * }
 * ]
 * 
 * @author Fábio Júnior
 * @version 1.0.0
 */
public class FileTaskRepository implements ITaskRepository {
    // Constantes
    private static final String DATA_DIR = "src/main/resources/data";
    private static final String FILE_PATH = DATA_DIR + "/tasks.json";

    // Dependências
    private final Gson gson;
    private final File file;
    private final ReadWriteLock Lock;

    // Construtor
    public FileTaskRepository() {
        this(FILE_PATH);
    }

    public FileTaskRepository(String filePATH) {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        this.file = new File(FILE_PATH);
        this.Lock = new ReentrantReadWriteLock();
        ensureFileExists();

        initializeFile();
    }

    // Inicialização

    /**
     * Cria um arquivo e diretório vazios se não existirem.
     * Inicializa com array vazio.
     */

    private void initializeFile() {
        try {
            // Cria diretório se necessário
            File dir = file.getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }

            // Cria arquivo com array vazio se não existir
            if (!file.exists()) {
                file.createNewFile();
                writeToFile(new ArrayList<>());
            }
        } catch (IOException e) {
            throw new RepositoryException("Erro ao inicializar arquivo: " + file.getAbsolutePath(), e);
        }
    }

    // Operações básicas (CRUD)
    @Override
    public Task save(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task não pode ser nula");
        }
        lock.writeLock().lock();
        try {
            List<Task> tasks = readFromFile();
            if (tasks.stream().anyMatch(t -> t.getId().equals(task.getId()))) {
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
    public Task update(Task task){
        if (task == null){
            throw new IllegalArgumentException("Task não pode ser nula");
        }
        lock.writeLock().lock();
        try{
            List<task> tasks = readFromFile();

            int index = -1;
            for (int i = 0; i < tasks.size(); i++){
                if (tasks.get(i).getId().equals(task.getId())){
                    index = i;
                    break;
                }
            }
            if (index == -1){
                throw new RepositoryException("Task não encontrada: " + task.getId());
            }
            tasks.set(index, task);
            writeToFile(tasks);
            return task;
        } finally {
            lock.writeLock().unlock();
        }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.isEmpty()) {
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
        if (id == null || id.isEmpty()) {
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
    public List<Task> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(readFromFile()); // retorna cópia
        } finally {
            lock.readLock().unlock();
        }
    }
    // operações de busca (queries) 
    @Override
    public List<Task> findByStatus(TaskStatus status){
        if (status == null){
            throw new IllegalArgumentException("Status não pode ser nulo");
        }
        lock.readLock().lock();
        try{
            List<Task> tasks = readFromFile();
            return tasks.stream()
                    .filter(t -> t.getStatus() == status)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    @Override
    public List<Task> findByTitleContaining(String keyword){
        if (keyword == null || keyword.isBlank()){
            throw new IllegalArgumentException("Keyword não pode ser nula ou vazia");
        }
        lock.readLock().lock();
        try{
            List<main.java.com.todoapp.model.Task> tasks = readFromFile();
            String lowerKeyword = keyword.toLowerCase();

            return tasks.stream()
            .filter(t -> t.getTitle().toLowerCase().contains(lowerKeyword))
            .collect(Collectors.toList());
        } finally{
            lock.readLock().unlock();
        }
    }
    // operações de utilidade
    @Override
    public long count(){
        lock.readLock().lock();
        try{
            List<Task> tasks = readFromFile();
            return tasks.size();
        } finally {
            lock.readLock().unlock();
        }
    }
    @Override
    public void  deleteAll(){
        lock.writeLock().lock();
        try{
            writeToFile(new ArrayList<>());
        } finally {
            lock.writeLock().unlock();
        }
    }
      // === MÉTODOS PRIVADOS (I/O) ===
    
    /**
     * Lê tarefas do arquivo JSON
     * Se arquivo estiver vazio ou corrompido, retorna lista vazia
     * 
     * @return lista de tarefas
     * @throws RepositoryException se erro de leitura
     */
    private List<Task> readFromFile() {
        try (FileReader reader = new FileReader(file)){
            // Define tipo para deserialização: List<Task>
            Type taskListType = new TypeToken<ArrayList<Task>>(){}.getType();
            List<Task> tasks = gson.fromJson(reader, taskListType);
            // Se arquivo vazio ou null, retorna lista vazia
            return tasks != null ? tasks : new ArrayList<>();
        } catch (IOException e){
            throw new RepositoryException("Erro ao ler arquivo: " + file.getAbsolutePath(), e);
        } catch (Exception e){
           // JSON corrompido - retorna lista vazia e loga
            System.err.println("AVISO: Arquivo JSON corrompido. Iniciando com lista vazia.");
            return new ArrayList<>();
        }
         /**
     * Escreve tarefas no arquivo JSON
     * Sobrescreve conteúdo anterior
     * 
     * @param tasks lista de tarefas a salvar
     * @throws RepositoryException se erro de escrita
     */
    private void writeToFile(List<Task> tasks){
        try (FileWriter writer = new FileWriter(file, false)){
            gson.toJson(tasks, writer);
            writer.flush();
        } catch (IOException e){
            throw new RepositoryException("Erro ao escrever arquivo: " + file.getAbsolutePath(), e);
        }            
    }
     // === MÉTODOS UTILITÁRIOS ===
    
    /**
     * Retorna caminho absoluto do arquivo
     * Útil para debug
     * 
     * @return path do arquivo
     */
     public String getFilePath() {
        return file.getAbsolutePath();
    }
    
    /**
     * Verifica se arquivo existe
     * 
     * @return true se existe
     */
    public boolean fileExists() {
        return file.exists();
    }
}
}
