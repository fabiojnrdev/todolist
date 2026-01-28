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
 *   {
 *     "id": "abc-123",
 *     "title": "Estudar Java",
 *     "description": "Capítulo 5",
 *     "status": "PENDENTE",
 *     "createdAt": "2026-01-27T14:30:00",
 *     "updatedAt": "2026-01-27T14:30:00",
 *     "completedAt": null
 *   }
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
    public FileTaskRepository(){
        this(FILE_PATH);
    }

    public FileTaskRepository(String filePATH){
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

    private void initializeFile(){
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
    public Task save(Task task){
        if (task == null) {
            throw new IllegalArgumentException("Task não pode ser nula");
    }
    lock.writeLock().lock();
    try{
        List<Task> tasks = readFromFile();
        if (tasks.stream().anyMatch(t -> t.getId().equals(task.getId()))){
            throw new RepositoryException("Task com esse ID já existe: " + task.getId());
        }
        tasks.add(task);
        writeToFile(tasks);
        return task;
    } finally{
        lock.writeLock().unlock();
    }
    }
    @Override
    public Task update(Task task){
        if (task == null){
            throw new IllegalArgumentException("Task não pode ser nula");
        }
        lock.writeLock().lock();
        
    }
        





















}
