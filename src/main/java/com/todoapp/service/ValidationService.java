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
     * @param title  título a validar
     * @param result objeto para acumular erros
     */
    private void ValidateTitle(String title, ValidationResult result) {
        if (title == null || title.trim().isEmpty()) {
            result.addError("O título da tarefa não pode ser vazio.");
            return;
        }
        String trimmedTitle = title.trim();

        // Verifica comprimento mínimo
        if (trimmedTitle.length() < MIN_TITLE_LENGTH) {
            result.addError(String.format("O título deve possuir pelo menos %d caracteres (atual: %d) ",
                    MIN_TITLE_LENGTH,
                    trimmedTitle.length()));
        }
        // Verifica comprimento máximo
        if (trimmedTitle.length() > MAX_TITLE_LENGTH) {
            result.addError(String.format("O título deve possuir no máximo %d caracteres (atual: %d) ",
                    MAX_TITLE_LENGTH,
                    trimmedTitle.length()));
        }
    }

    /**
     * Valida a descrição da tarefa
     * 
     * Regras:
     * - Pode ser null ou vazia (é opcional)
     * - Se fornecida, não pode exceder 500 caracteres
     * 
     * @param description descrição a validar
     * @param result      objeto para acumular erros
     */
    private void ValidateDescription(String description, ValidationResult result) {
        if (description == null || description.trim().isBlank()) {
            return;
        }
        String trimmedDescription = description.trim();
        // Verifica comprimento máximo
        if (trimmedDescription.length() > MAX_DESCRIPTION_LENGTH) {
            result.addError(String.format("A descrição deve possuir no máximo %d caracteres (atual: %d) ",
                    MAX_DESCRIPTION_LENGTH,
                    trimmedDescription.length()));
        }
    }
    // === VALIDAÇÕES UTILITÁRIAS ===

    /**
     * Verifica se um título é válido (validação rápida)
     * 
     * @param title título a verificar
     * @return true se válido, false caso contrário
     */

    public boolean isTitleValid(String title) {
        if (title == null || title.trim().isBlank()) {
            return false;
        }
        String trimmedTitle = title.trim();
        return trimmedTitle.length() >= MIN_TITLE_LENGTH && trimmedTitle.length() <= MAX_TITLE_LENGTH;
    }

    /**
     * Verifica se uma descrição é válida (validação rápida)
     * 
     * @param description descrição a verificar
     * @return true se válida (pode ser null/vazia)
     */
    public boolean isDescriptionValid(String description) {
        if (description == null || description.trim().isBlank()) {
            return true;
        }
        return description.trim().length() <= MAX_DESCRIPTION_LENGTH;
    }
    // === GETTERS DE CONSTANTES (para UI) ===

    /**
     * Retorna comprimento mínimo permitido para título
     * Útil para exibir na UI (ex: "Mínimo 3 caracteres")
     */
    public int getMinTitleLength() {
        return MIN_TITLE_LENGTH;
    }

    /**
     * Retorna comprimento máximo permitido para título
     * Útil para exibir na UI e limitar TextField
     */
    public int getMaxTitleLength() {
        return MAX_TITLE_LENGTH;
    }

    /**
     * Retorna comprimento máximo permitido para descrição
     * Útil para exibir na UI e limitar TextArea
     */
    public int getDescriptionMaxLength() {
        return MAX_DESCRIPTION_LENGTH;
    }
    // === CLASSE INTERNA: RESULTADO DE VALIDAÇÃO ===

    /**
     * Representa o resultado de uma validação
     * Armazena múltiplos erros e fornece métodos para verificação
     */

    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();

        /**
         * Adiciona um erro à lista de erros
         * 
         * @param error mensagem de erro a ser adicionada
         * @return void
         *         Exemplo de uso:
         *         ValidationResult result = new ValidationResult();
         */
        public void addError(String error) {
            errors.add(error);
        }

        // Verifica se a validação passou (sem erros)
        public boolean isValid() {
            return errors.isEmpty();
        }

        // Verifica se validação falhou (tem erros)
        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        // Retorna lista de erros
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        /**
         * Retorna mensagem única com todos os erros
         * Útil para exibir em diálogos
         */
        public String getErrorMessage() {
            if (errors.isEmpty()) {
                return "Nenhum erro de validação.";
            }
            // Múltiplos erros - formata lista
            StringBuilder sb = new StringBuilder("Erros de validação:\n");
            for (int i = 0; i < errors.size(); i++) {
                sb.append(String.format("%d. %s\n", i + 1, errors.get(i)));
            }
            return sb.toString();
        }

        // Retorna quantidade de erros
        public int getErrorCount() {
            return errors.size();
        }

        @Override
        public String toString() {
            return isValid()
                    ? "ValidationResult{valid}"
                    : "ValidationResult{errors=" + errors + "}";
        }
    }
    // === EXCEÇÃO CUSTOMIZADA ===

    /**
     * Exceção lançada quando validação falha
     */
    public static class ValidationException extends RuntimeException {

        public ValidationException(String message) {
            super(message);
        }

        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
