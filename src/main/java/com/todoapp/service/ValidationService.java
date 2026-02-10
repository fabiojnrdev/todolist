package main.java.com.todoapp.service;

import com.todoapp.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável por validar regras de negócio das tarefas.
 * 
 * Centraliza todas as validações em um único lugar, seguindo o princípio
 * Single Responsibility e facilitando manutenção e testes.
 * 
 * Validações implementadas:
 * - Título não vazio
 * - Comprimento mínimo/máximo de título
 * - Comprimento máximo de descrição
 * - Task não nula
 * 
 * @author Fábio Júnior
 * @version 1.0
 */
public class ValidationService {
    private static final int MIN_TITLE_LENGTH = 3;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    /**
     * Valida uma tarefa de acordo com as regras de negócio definidas.
     * Minimo do tamanho de titulo: 3, Maximo do tamanho de titulo: 100, Máximo do
     * tamanho da descrição: 500
     */

    public ValidationResult validade(main.java.com.todoapp.model.Task task) {
        // === VALIDAÇÃO PRINCIPAL ===

        /**
         * Valida uma tarefa completamente
         * Executa todas as validações e retorna resultado agregado
         * 
         * @param task tarefa a validar
         * @return resultado da validação com mensagens de erro
         */
        ValidationResult result = new ValidationResult();

        // Verifica se a tarefa é nula. Se for, adiciona erro e retorna.
        if (task == null) {
            result.addError("A tarefa não pode ser nula.");
            return result;
        }
        ValidateTitle(task.getTitle(), result);
        ValidateDescription(task.getDescription(), result);
        return result;
    }

    /**
     * Valida uma tarefa e lança exceção se inválida
     * Útil para fluxos que precisam de fail-fast
     * 
     * @param task tarefa a validar
     * @throws ValidationException se validação falhar
     */
    public void validateOrThrow(Task task) {
        ValidationResult result = validade(task);
        if (!result.isValid()) {
            throw new ValidationException(result.getErrorMessage());
        }
    }

    // === VALIDAÇÕES ESPECÍFICAS ===
    /**
     * Valida o título da tarefa
     * 
     * Regras:
     * - Não pode ser null ou vazio
     * - Deve ter pelo menos 3 caracteres (após trim)
     * - Não pode exceder 100 caracteres
     * 
     * @param title título a validar
     * @param result objeto para acumular erros
     */
    private void ValidateTitle(String title, ValidationResult result) {
        if (title == null || title.trim().isEmpty()) {
            result.addError("O título da tarefa não pode ser vazio.");
            return;
        }
        String trimmedTitle = title.trim();

        // Verifica comprimento mínimo
        if (trimmedTitle.length() < MIN_TITLE_LENGTH){
            result.addError(String.format("O título deve possuir pelo menos %d caracteres (atual: %d) ", 
            MIN_TITLE_LENGTH,
        trimmedTitle.length()
    ));
}
}
