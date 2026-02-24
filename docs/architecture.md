# Arquitetura – Todo Manager

## Visão Geral

Todo Manager é uma aplicação desktop de gerenciamento de tarefas desenvolvida em **Java 21+ com JavaFX**. A arquitetura segue o padrão **MVC (Model-View-Controller)** com uma camada de serviço intermediária e persistência em arquivo JSON.

```
┌─────────────────────────────────────────────────────────┐
│                        VIEW                             │
│         MainWindow.java      TaskDialog.java            │
└────────────────────┬────────────────────────────────────┘
                     │  callbacks (Consumer<T>)
                     ▼
┌─────────────────────────────────────────────────────────┐
│                     CONTROLLER                          │
│                  TaskController.java                    │
└────────────────────┬────────────────────────────────────┘
                     │  chamadas diretas
                     ▼
┌─────────────────────────────────────────────────────────┐
│                      SERVICE                            │
│      TaskService.java      ValidationService.java       │
└────────────────────┬────────────────────────────────────┘
                     │  interface ITaskRepository
                     ▼
┌─────────────────────────────────────────────────────────┐
│                    REPOSITORY                           │
│                FileTaskRepository.java                  │
└────────────────────┬────────────────────────────────────┘
                     │  Gson (serialização)
                     ▼
┌─────────────────────────────────────────────────────────┐
│                    PERSISTÊNCIA                         │
│            src/main/resources/data/tasks.json           │
└─────────────────────────────────────────────────────────┘
```

---

## Camadas

### 1. Model (`com.todoapp.model`)

Contém as entidades e enums do domínio. Não possui dependências externas.

#### `Task`
Entidade central da aplicação. Possui imutabilidade parcial: `id` e `createdAt` são `final` e nunca mudam após a criação.

| Campo         | Tipo            | Mutável | Descrição                                  |
|---------------|-----------------|---------|--------------------------------------------|
| `id`          | `String` (UUID) | ❌      | Identificador único gerado automaticamente |
| `title`       | `String`        | ✅      | Título obrigatório (3–100 chars)           |
| `description` | `String`        | ✅      | Descrição opcional (máx. 500 chars)        |
| `status`      | `TaskStatus`    | ✅      | Estado atual da tarefa                     |
| `createdAt`   | `LocalDateTime` | ❌      | Data de criação (imutável)                 |
| `updatedAt`   | `LocalDateTime` | ✅      | Atualizado automaticamente pelos setters   |
| `completedAt` | `LocalDateTime` | ✅      | Preenchido ao concluir, zerado ao reverter |

**Ciclo de vida do status:**
```
PENDENTE ──► EM_PROGRESSO ──► CONCLUIDA
   ◄──────────────────────────────
```

#### `TaskStatus`
Enum com três estados, cada um com emoji e cor hexadecimal para exibição na UI.

| Valor         | Display Name  | Emoji | Cor       |
|---------------|---------------|-------|-----------|
| `PENDENTE`    | Pendente      | ⏳    | `#FFA500` |
| `EM_PROGRESSO`| Em Progresso  | 🚀    | `#2196F3` |
| `CONCLUIDA`   | Concluída     | ✅    | `#4CAF50` |

---

### 2. Repository (`com.todoapp.repository`)

Responsável exclusivamente pela persistência de dados. Desacoplado via interface.

#### `ITaskRepository` (interface)
Define o contrato de persistência com operações CRUD e consultas. Qualquer implementação (arquivo, banco de dados, memória) pode ser plugada sem alterar as camadas superiores.

Operações definidas:
- `save(Task)` — persiste nova tarefa
- `update(Task)` — atualiza tarefa existente
- `delete(String id)` — remove por ID
- `findById(String id)` — busca por ID
- `findAll()` — retorna todas
- `findByStatus(TaskStatus)` — filtra por status
- `findByTitleContaining(String keyword)` — busca por texto no título
- `count()` — contagem total
- `existsById(String id)` — verificação de existência
- `deleteAll()` — limpeza total

#### `FileTaskRepository`
Implementação concreta que persiste em arquivo JSON usando **Gson**.

- **Thread-safety:** `ReentrantReadWriteLock` — múltiplas leituras simultâneas, escrita exclusiva
- **Serialização:** Gson com `LocalDateTimeAdapter` customizado (formato ISO-8601)
- **Resiliência:** arquivo corrompido ou ausente resulta em lista vazia (não lança exceção)
- **Construtor flexível:** aceita caminho customizado (útil para testes)

---

### 3. Service (`com.todoapp.service`)

Camada de lógica de negócio. Orquestra Repository e Validação.

#### `TaskService`
- Stateless — não guarda estado entre chamadas
- Thread-safe — delega concorrência ao repository
- Dois construtores: injeção completa ou conveniência (cria `ValidationService` automaticamente)
- Método auxiliar privado `getTaskOrThrow(id)` — busca ou lança `TaskNotFoundException`

#### `ValidationService`
Centraliza todas as regras de validação.

| Regra                   | Limite              |
|-------------------------|---------------------|
| Título mínimo           | 3 caracteres        |
| Título máximo           | 100 caracteres      |
| Descrição máxima        | 500 caracteres      |
| Task nula               | Não permitida       |
| Descrição nula/vazia    | Permitida (opcional)|

Retorna `ValidationResult` (lista de erros acumulados) ou lança `ValidationException` via `validateAndThrow()`.

---

### 4. Controller (`com.todoapp.controller`)

Ponte entre View e Service. Não conhece detalhes de UI.

#### `TaskController`
Comunicação com a View via **callbacks funcionais** (`Consumer<T>`):

| Callback           | Tipo                  | Quando é chamado                      |
|--------------------|-----------------------|---------------------------------------|
| `onTasksUpdated`   | `Consumer<List<Task>>`| Após qualquer operação que muda dados |
| `onSuccess`        | `Consumer<String>`    | Operação concluída com sucesso        |
| `onError`          | `Consumer<String>`    | Exceção capturada                     |

Todas as exceções são capturadas internamente — a View nunca recebe exceções brutas.

---

### 5. View (`com.todoapp.view`)

Interface gráfica JavaFX. Não contém lógica de negócio.

#### `MainWindow`
Janela principal com:
- `MenuBar` — Arquivo, Tarefas, Ajuda
- `HBox` toolbar — busca em tempo real, filtro por status, botão nova tarefa
- `TableView<Task>` — colunas: status (emoji), título, descrição, data, ações inline
- `ContextMenu` — editar, avançar/retroceder status, concluir, deletar
- Painel de estatísticas com `ProgressBar` de conclusão
- Barra de status com feedback colorido (verde = sucesso, vermelho = erro)

Todos os updates de UI passam por `Platform.runLater()` garantindo execução na thread do JavaFX.

#### `TaskDialog`
Diálogo reutilizável para criação e edição:
- **Modo criação:** campos vazios, botão "Criar"
- **Modo edição:** campos preenchidos, `ComboBox` de status, botão "Salvar"
- Validação inline: botão OK bloqueado se título < 3 chars
- Contador de caracteres para descrição

---

### 6. Util (`com.todoapp.util`)

#### `LocalDateTimeAdapter`
Adapter Gson para serialização/deserialização de `LocalDateTime`.
- Serializa: `LocalDateTime` → `String` ISO-8601 (`"2026-01-27T14:30:00"`)
- Deserializa: `String` ISO-8601 → `LocalDateTime`
- Trata `null` sem lançar exceção

---

## Fluxo de Dados — Criação de Tarefa

```
Usuário clica "Nova Tarefa"
        │
        ▼
MainWindow.showCreateTaskDialog()
        │  TaskDialog retorna Optional<Task>
        ▼
TaskController.createTask(title, description)
        │
        ▼
TaskService.createTask(title, description)
        │  new Task(title) + setDescription()
        ▼
ValidationService.validateAndThrow(task)
        │  lança ValidationException se inválido
        ▼
FileTaskRepository.save(task)
        │  writeLock → readFromFile → add → writeToFile
        ▼
TaskController.notifySuccess("Tarefa criada!")
TaskController.refreshTaskList()
        │  onTasksUpdated.accept(getAllTasks())
        ▼
MainWindow atualiza TableView + StatusBar + Stats
```

---

## Decisões de Design

**Por que arquivo JSON em vez de banco de dados?**
Aplicação desktop standalone sem necessidade de servidor. JSON é portável, legível e suficiente para o volume de dados esperado.

**Por que callbacks em vez de Observer/EventBus?**
Solução simples e direta para o tamanho do projeto. Evita complexidade desnecessária mantendo o desacoplamento View ↔ Controller.

**Por que ITaskRepository como interface?**
Permite trocar a implementação de persistência (ex: SQLite, H2) sem alterar Service ou Controller. Facilita testes com mocks.

**Por que ReadWriteLock no Repository?**
JavaFX pode disparar eventos de UI em threads diferentes. O lock garante consistência sem bloquear leituras concorrentes.

---

## Estrutura de Pacotes

```
com.todoapp/
├── Main.java                         # Entry point JavaFX
├── model/
│   ├── Task.java                     # Entidade principal
│   └── TaskStatus.java               # Enum de estados
├── repository/
│   ├── ITaskRepository.java          # Contrato de persistência
│   └── FileTaskRepository.java       # Implementação JSON
├── service/
│   ├── TaskService.java              # Lógica de negócio
│   └── ValidationService.java        # Regras de validação
├── controller/
│   └── TaskController.java           # Ponte View ↔ Service
├── view/
│   ├── MainWindow.java               # Janela principal
│   └── TaskDialog.java               # Diálogo criação/edição
└── util/
    └── LocalDateTimeAdapter.java     # Adapter Gson para datas
```