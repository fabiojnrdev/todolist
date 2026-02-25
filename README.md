# Todo Manager

Sistema de gerenciamento de tarefas desktop desenvolvido em **Java 25 + JavaFX 21**.

---

## Funcionalidades

- Criar, editar e deletar tarefas
- Ciclo de vida de status: **Pendente → Em Progresso → Concluída**
- Avançar e retroceder status com um clique
- Busca em tempo real por título
- Filtro por status (Todas, Pendentes, Em Progresso, Concluídas, Ativas)
- Painel de estatísticas com barra de progresso de conclusão
- Persistência automática em arquivo JSON
- Interface com feedback visual de sucesso e erro

---

## Tecnologias

| Tecnologia     | Versão   | Uso                              |
|----------------|----------|----------------------------------|
| Java           | 25       | Linguagem principal              |
| JavaFX         | 21.0.1   | Interface gráfica                |
| Gson           | 2.10.1   | Serialização JSON                |
| JUnit Jupiter  | 5.10.1   | Testes unitários                 |
| Logback        | 1.4.14   | Logging                          |
| Maven          | 3.x      | Build e gerenciamento            |

---

## Pré-requisitos

- **JDK 25** instalado
- **Maven 3.6+** instalado
- Variável `JAVA_HOME` apontando para o JDK 25

---

## Como executar

```bash
# Clonar o repositório
git clone <url-do-repositorio>
cd todo-manager

# Compilar
mvn compile

# Executar a aplicação
mvn javafx:run

# Rodar testes
mvn test
```

---

## Estrutura do Projeto

```
todo-manager/
├── src/
│   ├── main/
│   │   ├── java/com/todoapp/
│   │   │   ├── Main.java
│   │   │   ├── model/
│   │   │   │   ├── Task.java
│   │   │   │   └── TaskStatus.java
│   │   │   ├── repository/
│   │   │   │   ├── ITaskRepository.java
│   │   │   │   └── FileTaskRepository.java
│   │   │   ├── service/
│   │   │   │   ├── TaskService.java
│   │   │   │   └── ValidationService.java
│   │   │   ├── controller/
│   │   │   │   └── TaskController.java
│   │   │   ├── view/
│   │   │   │   ├── MainWindow.java
│   │   │   │   └── TaskDialog.java
│   │   │   └── util/
│   │   │       └── LocalDateTimeAdapter.java
│   │   └── resources/
│   │       └── data/
│   │           └── tasks.json          ← dados persistidos aqui
│   └── test/
│       └── java/com/todoapp/
├── docs/
│   ├── architecture.md
│   ├── database-schema.md
│   └── README.md
└── pom.xml
```

---

## Arquitetura

O projeto segue o padrão **MVC** com camada de serviço:

```
View  ──►  Controller  ──►  Service  ──►  Repository  ──►  JSON
 ◄── callbacks (Consumer<T>)
```

- **View** (`MainWindow`, `TaskDialog`) — JavaFX, sem lógica de negócio
- **Controller** (`TaskController`) — recebe ações da UI, delega ao Service, notifica a View via callbacks
- **Service** (`TaskService`, `ValidationService`) — regras de negócio e validações
- **Repository** (`FileTaskRepository`) — persistência thread-safe em JSON
- **Model** (`Task`, `TaskStatus`) — entidades do domínio

Para mais detalhes, consulte [`docs/architecture.md`](architecture.md).

---

## Persistência

As tarefas são salvas em `src/main/resources/data/tasks.json` automaticamente a cada operação. O arquivo é criado na primeira execução.

Exemplo de entrada no JSON:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Estudar JavaFX",
  "description": "Capítulo 5",
  "status": "EM_PROGRESSO",
  "createdAt": "2026-02-24T10:30:00",
  "updatedAt": "2026-02-24T14:15:00",
  "completedAt": null
}
```

Para mais detalhes, consulte [`docs/database-schema.md`](database-schema.md).

---

## Regras de Negócio

| Regra                        | Detalhe                                      |
|------------------------------|----------------------------------------------|
| Título obrigatório           | Mínimo 3, máximo 100 caracteres              |
| Descrição opcional           | Máximo 500 caracteres                        |
| Status inicial               | Sempre `PENDENTE` ao criar                   |
| `completedAt`                | Preenchido ao concluir, zerado ao reverter   |
| IDs únicos                   | UUID v4 gerado automaticamente               |

---

## Autor

**Fábio Júnior** — v1.0.1