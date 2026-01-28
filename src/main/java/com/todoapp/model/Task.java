package main.java.com.todoapp.model;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa uma tarefa no sistema de gerenciamento.
 * 
 * Esta classe é o modelo central do domínio, contendo todas as informações
 * necessárias para representar uma tarefa e suas operações básicas.
 * 
 * Imutabilidade parcial:
 * - id e createdAt são imutáveis (final)
 * - demais campos podem ser atualizados via setters
 * 
 * @author Fábio Júnior
 * @version 1.0.0
 */
public class Task {
    // == Campos imutáveis ==
    /**
     * Identificador único da tarefa. (UUID)
     * Gerado automaticamente na criação.
     */
    private final String id;

    /**
     * Data e hora de criação da tarefa.
     * Definido automaticamente na criação.
     */
    private final LocalDateTime createdAt;

    //== Campos mutáveis ==
    /**
     * Titulo ou nome da tarefa (Item obrigatório).
     */
    private String title;
    /**
     * Descrição detalhada da tarefa (opcional)
     */
    private String description;
    
    /**
     * Status atual da tarefa
     */
    private TaskStatus status;
    
    /**
     * Data e hora da última modificação
     * Atualizada automaticamente pelos setters
     */
    private LocalDateTime updatedAt;
    
    /**
     * Data e hora de conclusão da tarefa
     * Null enquanto não estiver concluída
     */
    private LocalDateTime completedAt;

    // == Construtores ==
    /**
     * Construtor padrão - Cria uma nova tarefa com título obrigatório.
     * Valores padrão:
     * - id: Gerado automaticamente (UUID)
     * - createdAt: Data e hora atual
     * - updatedAt: Data e hora atual
     * - completedAt: null
     * - status: PENDING
     * - title: vazio
     * - description: vazia
     */
    public Task(){
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = TaskStatus.PENDENTE;
        this.title = "";
        this.description = "";
        this.completedAt = null;
    }
    /**
     * Construtor com título - cria tarefa já com título definido
     * 
     * @param title título da tarefa (não pode ser null)
     * @throws IllegalArgumentException se título for null ou vazio
     */
    public Task(String title) {
        this();  
        setTitle(title);
    }
    
    /**
     * Construtor completo - cria tarefa com todos os campos definidos
     * @param id identificador único
     * @param title título da tarefa
     * @param description descrição da tarefa
     * @param status status atual
     * @param createdAt data de criação
     * @param updatedAt data de atualização
     * @param completedAt data de conclusão
     */
    public Task(String id, String title, String description,
                TaskStatus status, LocalDateTime createdAt, 
                LocalDateTime updatedAt, LocalDateTime completedAt){
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.completedAt = completedAt; 
        this.status = status != null ? status : TaskStatus.PENDENTE;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();


    }

    // Getters
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    // Setters
    /**
     * Define o titlo com lógica
     * @param title novo título (não pode ser null ou vazio)
     * @throws IllegalArgumentException se título for null ou vazio
     */
    public void setTitle(String title){
        if(title == null || title.trim().isEmpty()){
            throw new IllegalArgumentException("Título não pode ser nulo ou vazio.");
        }
        this.title = title.trim();
        updateTimestamp();
    }
    /**
     * Define a descrição da tarefa
     * @param description (pode ser null ou vazia)
     */
    public void setDescription(String description){
        this.description = description != null ? description : "";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Define o status da tarefa
     * Se for definido como COMPLETED, atualiza completedAt
     * Se mudar de COMPLETED para outro status, zera completedAt
     * @param status novo status 
     * @throws IllegalArgumentException se status for null
     */

    public void setStatus(TaskStatus status){
        if (status == null){
            throw new IllegalArgumentException("status não pode ser nulo.");
        }
        // Se o status for mudado para completado, atualiza completedAt e registra o momento
        if (status == TaskStatus.CONCLUIDA && this.status != TaskStatus.CONCLUIDA){
            this.completedAt = LocalDateTime.now();
        } 
        // Se estiver mudando de COMPLETED para outro status, zera completedAt
        if (this.status != TaskStatus.CONCLUIDA && status != TaskStatus.CONCLUIDA){
            this.completedAt = null;
        }
        this.status = status;
        updateTimestamp();
    }
    // == Métodos auxiliares de utilidade ==

    /**
     * Atualiza o timestamp de atualização para o momento atual
     */
    private void updateTimestamp(){
        this.updatedAt = LocalDateTime.now();
    }
     /**
     * Marca a tarefa como concluída
     * Atalho para setStatus(TaskStatus.CONCLUIDA)
     */
    public void complete() {
        setStatus(TaskStatus.CONCLUIDA);
    }
    
    /**
     * Avança a tarefa para o próximo status no ciclo de vida
     * PENDENTE → EM_PROGRESSO → CONCLUIDA
     */
    public void advanceStatus() {
        setStatus(status.next());
    }
    
    /**
     * Retrocede a tarefa para o status anterior no ciclo de vida
     * CONCLUIDA → EM_PROGRESSO → PENDENTE
     */
    public void revertStatus() {
        setStatus(status.previous());
    }
    
    /**
     * Verifica se a tarefa está concluída
     * 
     * @return true se status é CONCLUIDA
     */
    public boolean isCompleted() {
        return status.isCompleted();
    }
    
    /**
     * Verifica se a tarefa está em progresso
     * 
     * @return true se status é EM_PROGRESSO
     */
    public boolean isInProgress() {
        return status.isInProgress();
    }
    
    /**
     * Verifica se a tarefa está pendente
     * 
     * @return true se status é PENDENTE
     */
    public boolean isPending() {
        return status.isPending();
    }
    
    /**
     * Verifica se a tarefa possui descrição
     * 
     * @return true se description não é vazia
     */
    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }
    /**
     * Retorna titulo formatado para exibição
     * Limita a 60 caracteres, adicionando "..." se truncado
     * @return titulo truncado
     */

    public String getShortTitle(){
        final int MAX_LENGTH = 60;
        if (title.length() <= MAX_LENGTH){
            return title;
        } else{
            return title.substring(0, MAX_LENGTH - 3) + "...";
        }
    }
    /**
     * Retorna descrição formatada para exibição
     * Limita a 100 caracteres, adicionando "..." se truncado
     * @return descrição truncada
     */

    public String getShortDescription(){
        final int MAX_LENGTH = 100;
        if (description.length() <= MAX_LENGTH){
            return description;
        } else{
            return description.substring(0, MAX_LENGTH - 3) + "...";
        }
    }
     /**
     * Formata data de criação para exibição
     * Formato: "dd/MM/yyyy HH:mm"
     * 
     * @return data formatada
     */
    public String getFormattedCreatedAt() {
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    /**
     * Formata data de atualização para exibição
     * Formato: "dd/MM/yyyy HH:mm"
     * 
     * @return data formatada
     */
    public String getFormattedUpdatedAt() {
        return updatedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    /**
     * Formata data de conclusão para exibição
     * Formato: "dd/MM/yyyy HH:mm"
     * 
     * @return data formatada ou "Não concluída"
     */
    public String getFormattedCompletedAt() {
        if (completedAt == null) {
            return "Não concluída";
        }
        return completedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    // === MÉTODOS OBJECT (equals, hashCode, toString) ===
    
    /**
     * Compara duas tarefas
     * Duas tarefas são iguais se têm o mesmo ID
     * 
     * @param o objeto a comparar
     * @return true se for a mesma tarefa (mesmo ID)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }
    
    /**
     * Gera hash code baseado no ID
     * Garante que tarefas iguais tenham mesmo hash
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * Representação textual da tarefa
     * Útil para debug e logs
     * 
     * @return string representando a tarefa
     */
    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", createdAt=" + getFormattedCreatedAt() +
                '}';
    }
    
    /**
     * Representação detalhada da tarefa
     * Inclui todos os campos
     * 
     * @return string completa da tarefa
     */
    public String toDetailedString() {
        return "Task{\n" +
                "  id='" + id + "'\n" +
                "  title='" + title + "'\n" +
                "  description='" + description + "'\n" +
                "  status=" + status.getFormatted() + "\n" +
                "  createdAt=" + getFormattedCreatedAt() + "\n" +
                "  updatedAt=" + getFormattedUpdatedAt() + "\n" +
                "  completedAt=" + getFormattedCompletedAt() + "\n" +
                '}';
    }
}