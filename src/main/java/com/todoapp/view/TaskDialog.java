package com.todoapp.view;

import java.util.Optional;

import com.todoapp.model.Task;
import com.todoapp.model.TaskStatus;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Diálogo para criação e edição de tarefas.
 *
 * Funciona em dois modos:
 * - Criação: chamado sem tarefa → campos vazios
 * - Edição : chamado com tarefa → campos preenchidos
 *
 * Retorna Optional<Task> com os dados preenchidos, ou Optional.empty()
 * se o usuário cancelar.
 *
 * @author Fábio Júnior
 * @version 1.0.0
 */
public class TaskDialog {

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int DESC_MAX_LENGTH  = 500;

    private final Stage owner;
    private final Task  existingTask; // null = modo criação

    // Campos do formulário
    private TextField  titleField;
    private TextArea   descriptionArea;
    private ComboBox<TaskStatus> statusCombo;
    private Label      charCountLabel;

    /**
     * Construtor para CRIAÇÃO de nova tarefa.
     *
     * @param owner janela pai
     */
    public TaskDialog(Stage owner) {
        this(owner, null);
    }

    /**
     * Construtor para EDIÇÃO de tarefa existente.
     *
     * @param owner        janela pai
     * @param existingTask tarefa a editar
     */
    public TaskDialog(Stage owner, Task existingTask) {
        this.owner        = owner;
        this.existingTask = existingTask;
    }

    /**
     * Exibe o diálogo e aguarda a resposta do usuário.
     *
     * @return Optional com a Task preenchida, ou empty se cancelado
     */
    public Optional<Task> showAndWait() {
        boolean isEditMode = existingTask != null;

        Dialog<Task> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(isEditMode ? "Editar Tarefa" : "Nova Tarefa");
        dialog.setHeaderText(isEditMode
                ? "Edite os dados da tarefa"
                : "Preencha os dados da nova tarefa");

        // Conteúdo
        dialog.getDialogPane().setContent(buildForm(isEditMode));
        dialog.getDialogPane().setPrefWidth(480);

        // Botões
        dialog.getDialogPane().getButtonTypes().addAll(
                javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL
        );

        // Rótulo do botão OK
        Button okButton = (Button) dialog.getDialogPane()
                .lookupButton(javafx.scene.control.ButtonType.OK);
        okButton.setText(isEditMode ? "Salvar" : "Criar");

        // Validação antes de confirmar
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (titleField.getText().trim().length() < 3) {
                titleField.setStyle("-fx-border-color: red;");
                event.consume(); // bloqueia o fechamento
            }
        });

        // Conversão do resultado
        dialog.setResultConverter(buttonType -> {
            if (buttonType == javafx.scene.control.ButtonType.OK) {
                return buildTaskFromForm(isEditMode);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // === CONSTRUÇÃO DO FORMULÁRIO ===

    /**
     * Monta o formulário de criação/edição.
     */
    private VBox buildForm(boolean isEditMode) {
        VBox form = new VBox(12);
        form.setPadding(new Insets(16));

        // Grid de campos
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        // — Título —
        Label titleLabel = new Label("Título *");
        titleField = new TextField();
        titleField.setPromptText("Título da tarefa (mín. 3 caracteres)");
        titleField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(titleField, Priority.ALWAYS);

        // Limitar caracteres e restaurar borda ao digitar
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > TITLE_MAX_LENGTH) {
                titleField.setText(oldVal);
            }
            titleField.setStyle("");
        });

        grid.add(titleLabel, 0, 0);
        grid.add(titleField, 1, 0);

        // — Descrição —
        Label descLabel = new Label("Descrição");
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Descrição opcional (máx. 500 caracteres)");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);
        GridPane.setHgrow(descriptionArea, Priority.ALWAYS);

        charCountLabel = new Label("0 / " + DESC_MAX_LENGTH);
        charCountLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > DESC_MAX_LENGTH) {
                descriptionArea.setText(oldVal);
            } else {
                charCountLabel.setText(newVal.length() + " / " + DESC_MAX_LENGTH);
            }
        });

        grid.add(descLabel, 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(charCountLabel, 1, 2);
        GridPane.setColumnSpan(charCountLabel, 1);

        // — Status (apenas em modo edição) —
        if (isEditMode) {
            Label statusLabel = new Label("Status");
            statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll(TaskStatus.values());
            statusCombo.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(TaskStatus s)   { return s != null ? s.getFormatted() : ""; }
                @Override public TaskStatus fromString(String s) { return null; }
            });
            statusCombo.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(statusCombo, Priority.ALWAYS);

            grid.add(statusLabel, 0, 3);
            grid.add(statusCombo, 1, 3);
        }

        form.getChildren().add(grid);

        // Preencher com dados existentes (modo edição)
        if (isEditMode && existingTask != null) {
            titleField.setText(existingTask.getTitle());
            descriptionArea.setText(existingTask.getDescription());
            charCountLabel.setText(existingTask.getDescription().length() + " / " + DESC_MAX_LENGTH);
            if (statusCombo != null) {
                statusCombo.setValue(existingTask.getStatus());
            }
        }

        return form;
    }

    /**
     * Constrói a Task a partir dos campos do formulário.
     */
    private Task buildTaskFromForm(boolean isEditMode) {
        String title       = titleField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (isEditMode && existingTask != null) {
            // Atualiza objeto existente
            existingTask.setTitle(title);
            existingTask.setDescription(description);
            if (statusCombo != null && statusCombo.getValue() != null) {
                existingTask.setStatus(statusCombo.getValue());
            }
            return existingTask;
        } else {
            // Cria novo objeto
            Task task = new Task(title);
            task.setDescription(description);
            return task;
        }
    }
}