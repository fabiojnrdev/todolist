package com.todoapp.model;


/**
 * Enum que representa os possíveis estados de uma tarefa.
 * 
 * Estados do ciclo de vida:
 * PENDENTE → EM_PROGRESSO → CONCLUIDA
 * 
 * @author Todo App Team
 * @version 1.0.0
 */
public enum TaskStatus {
    
    /**
     * Tarefa criada mas ainda não iniciada
     */
    PENDENTE("Pendente", "⏳", "#FFA500"),
    
    /**
     * Tarefa em execução/andamento
     */
    EM_PROGRESSO("Em Progresso", "🚀", "#2196F3"),
    
    /**
     * Tarefa finalizada/completa
     */
    CONCLUIDA("Concluída", "✅", "#4CAF50");
    
    // === ATRIBUTOS ===
    
    private final String displayName;  // Nome legível para UI
    private final String emoji;        // Emoji visual
    private final String hexColor;     // Cor hexadecimal para UI
    
    // === CONSTRUTOR ===
    
    /**
     * Construtor do enum (privado por natureza de enums)
     * 
     * @param displayName nome para exibição
     * @param emoji ícone visual
     * @param hexColor cor em hexadecimal
     */
    TaskStatus(String displayName, String emoji, String hexColor) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.hexColor = hexColor;
    }
    
    // === GETTERS ===
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public String getHexColor() {
        return hexColor;
    }
    
    // === MÉTODOS DE UTILIDADE ===
    
    /**
     * Retorna representação formatada para exibição
     * Exemplo: "⏳ Pendente"
     * 
     * @return emoji + nome
     */
    public String getFormatted() {
        return emoji + " " + displayName;
    }
    
    /**
     * Verifica se a tarefa está completa
     * 
     * @return true se status é CONCLUIDA
     */
    public boolean isCompleted() {
        return this == CONCLUIDA;
    }
    
    /**
     * Verifica se a tarefa está em andamento
     * 
     * @return true se status é EM_PROGRESSO
     */
    public boolean isInProgress() {
        return this == EM_PROGRESSO;
    }
    
    /**
     * Verifica se a tarefa está pendente
     * 
     * @return true se status é PENDENTE
     */
    public boolean isPending() {
        return this == PENDENTE;
    }
    
    /**
     * Retorna o próximo status lógico no ciclo de vida
     * PENDENTE → EM_PROGRESSO
     * EM_PROGRESSO → CONCLUIDA
     * CONCLUIDA → CONCLUIDA (não muda)
     * 
     * @return próximo status ou o mesmo se já estiver concluída
     */
    public TaskStatus next() {
        return switch (this) {
            case PENDENTE -> EM_PROGRESSO;
            case EM_PROGRESSO -> CONCLUIDA;
            case CONCLUIDA -> CONCLUIDA;
        };
    }
    
    /**
     * Retorna o status anterior no ciclo de vida
     * CONCLUIDA → EM_PROGRESSO
     * EM_PROGRESSO → PENDENTE
     * PENDENTE → PENDENTE (não muda)
     * 
     * @return status anterior ou o mesmo se já estiver pendente
     */
    public TaskStatus previous() {
        return switch (this) {
            case CONCLUIDA -> EM_PROGRESSO;
            case EM_PROGRESSO -> PENDENTE;
            case PENDENTE -> PENDENTE;
        };
    }
    
    /**
     * Converte string para TaskStatus (case-insensitive)
     * Útil para deserialização JSON
     * 
     * @param value string representando o status
     * @return TaskStatus correspondente
     * @throws IllegalArgumentException se string inválida
     */
    public static TaskStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Status não pode ser nulo ou vazio");
        }
        
        return switch (value.toUpperCase().trim()) {
            case "PENDENTE", "PENDING" -> PENDENTE;
            case "EM_PROGRESSO", "EM PROGRESSO", "IN_PROGRESS", "IN PROGRESS" -> EM_PROGRESSO;
            case "CONCLUIDA", "CONCLUÍDA", "COMPLETED", "DONE" -> CONCLUIDA;
            default -> throw new IllegalArgumentException(
                "Status inválido: " + value + ". Valores aceitos: PENDENTE, EM_PROGRESSO, CONCLUIDA"
            );
        };
    }
    
    /**
     * Retorna descrição textual do status
     * Usado para serialização JSON e logs
     */
    @Override
    public String toString() {
        return displayName;
    }
}
