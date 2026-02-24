# Database Schema – Todo Manager

## Sobre a Persistência

Todo Manager **não utiliza banco de dados relacional**. A persistência é feita em um **arquivo JSON** gerenciado pela classe `FileTaskRepository` com serialização via **Gson**.

- **Localização padrão:** `src/main/resources/data/tasks.json`
- **Formato:** JSON Array de objetos `Task`
- **Criação automática:** arquivo e diretórios são criados na primeira execução se não existirem
- **Encoding:** UTF-8

---

## Estrutura do Arquivo JSON

```json
[
  {
    "id":          "550e8400-e29b-41d4-a716-446655440000",
    "title":       "Estudar JavaFX",
    "description": "Capítulo 5 – Layouts e Controles",
    "status":      "EM_PROGRESSO",
    "createdAt":   "2026-02-24T10:30:00",
    "updatedAt":   "2026-02-24T14:15:00",
    "completedAt": null
  },
  {
    "id":          "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
    "title":       "Configurar Maven",
    "description": "",
    "status":      "CONCLUIDA",
    "createdAt":   "2026-02-23T09:00:00",
    "updatedAt":   "2026-02-23T11:45:00",
    "completedAt": "2026-02-23T11:45:00"
  }
]
```

---

## Schema dos Campos

### Objeto `Task`

| Campo          | Tipo JSON | Formato               | Obrigatório | Regras                                          |
|----------------|-----------|-----------------------|-------------|-------------------------------------------------|
| `id`           | `string`  | UUID v4               | ✅          | Gerado automaticamente. Imutável após criação.  |
| `title`        | `string`  | texto livre           | ✅          | Mín. 3 chars, máx. 100 chars. Não pode ser vazio. |
| `description`  | `string`  | texto livre           | ❌          | Máx. 500 chars. String vazia `""` se omitida.  |
| `status`       | `string`  | enum (ver abaixo)     | ✅          | Um dos três valores válidos de `TaskStatus`.   |
| `createdAt`    | `string`  | ISO-8601 sem timezone | ✅          | Gerado automaticamente. Imutável após criação.  |
| `updatedAt`    | `string`  | ISO-8601 sem timezone | ✅          | Atualizado automaticamente a cada modificação. |
| `completedAt`  | `string` \| `null` | ISO-8601 sem timezone | ❌ | `null` enquanto não concluída. Preenchido ao mudar status para `CONCLUIDA`. Zerado ao reverter. |

---

### Valores válidos para `status`

| Valor JSON      | Significado                          |
|-----------------|--------------------------------------|
| `"PENDENTE"`    | Tarefa criada, ainda não iniciada    |
| `"EM_PROGRESSO"`| Tarefa em execução                   |
| `"CONCLUIDA"`   | Tarefa finalizada                    |

---

### Formato de Datas

Todas as datas seguem o padrão **ISO-8601 local** (sem timezone), serializado pelo `LocalDateTimeAdapter`:

```
yyyy-MM-dd'T'HH:mm:ss
```

Exemplos válidos:
```
"2026-02-24T14:30:00"
"2026-02-24T14:30:45.123"
"2026-01-01T00:00:00"
```

> O Gson **não** inclui timezone. Datas são sempre interpretadas no horário local da máquina.

---

## Regras de Integridade

As seguintes regras são aplicadas pela camada de serviço antes de qualquer escrita:

| Regra                              | Responsável              | Comportamento ao violar       |
|------------------------------------|--------------------------|-------------------------------|
| `id` único no arquivo              | `FileTaskRepository`     | Lança `RepositoryException`   |
| `title` não vazio                  | `ValidationService`      | Lança `ValidationException`   |
| `title` entre 3 e 100 chars        | `ValidationService`      | Lança `ValidationException`   |
| `description` máx. 500 chars       | `ValidationService`      | Lança `ValidationException`   |
| `status` não nulo                  | `Task.setStatus()`       | Lança `IllegalArgumentException` |
| Tarefa existente para update       | `FileTaskRepository`     | Lança `RepositoryException`   |

---

## Comportamento Especial do `completedAt`

A lógica de `completedAt` é controlada pelo setter `Task.setStatus()`:

```
status muda para CONCLUIDA         → completedAt = LocalDateTime.now()
status sai de CONCLUIDA para outro → completedAt = null
status muda entre não-concluídos   → completedAt permanece null
```

---

## Concorrência e Thread-Safety

O arquivo JSON é protegido por um `ReentrantReadWriteLock`:

| Operação                          | Lock utilizado  |
|-----------------------------------|-----------------|
| `findAll`, `findById`, `count`    | `readLock()`    |
| `save`, `update`, `delete`        | `writeLock()`   |
| `deleteAll`                       | `writeLock()`   |

Múltiplas leituras simultâneas são permitidas. Escritas são exclusivas.

---

## Resiliência

| Situação                         | Comportamento                                  |
|----------------------------------|------------------------------------------------|
| Arquivo não existe               | Criado automaticamente com array vazio `[]`    |
| Diretório não existe             | Criado automaticamente via `mkdirs()`          |
| Arquivo JSON corrompido          | Log de aviso no `stderr`, retorna lista vazia  |
| Erro de leitura (`IOException`)  | Log de aviso no `stderr`, retorna lista vazia  |
| Erro de escrita (`IOException`)  | Lança `RepositoryException`                    |

---

## Migração / Backup

Por ser um arquivo JSON simples, backup e migração são diretos:

```powershell
# Backup manual
copy "src\main\resources\data\tasks.json" "tasks_backup_2026-02-24.json"

# Restaurar
copy "tasks_backup_2026-02-24.json" "src\main\resources\data\tasks.json"
```

Para trocar a implementação de persistência (ex: SQLite), basta criar uma nova classe que implemente `ITaskRepository` e injetar no `Main.java`:

```java
// Main.java — trocar apenas esta linha:
ITaskRepository repository = new FileTaskRepository();
// por:
ITaskRepository repository = new SqliteTaskRepository("tasks.db");
```