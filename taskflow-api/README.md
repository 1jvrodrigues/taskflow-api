[README.md](https://github.com/user-attachments/files/33846110/README.md)
# TaskFlow API

API REST para gerenciamento de tarefas, construída com **Java 21**, **Spring Boot** e **MySQL**. O projeto foi pensado para praticar (e demonstrar) as boas práticas de uma API de backend: camadas bem separadas, validação, tratamento padronizado de erros, migrações de banco, testes automatizados, documentação interativa e execução via Docker.

> **Demo online:** _adicione aqui o link do deploy (veja a seção [Deploy](#deploy))_
> **Documentação interativa (Swagger):** `<url-do-deploy>/swagger-ui.html`

## Funcionalidades

- CRUD completo de tarefas (título, descrição, status, prioridade, data limite)
- Listagem **paginada** com **filtro por status**
- Atualização parcial do status (`PATCH`)
- **Validação** de entrada com mensagens claras por campo
- Erros padronizados no formato **RFC 7807 (Problem Details)**
- **Migrações versionadas** do banco com Flyway
- Documentação **OpenAPI / Swagger UI** gerada automaticamente
- **Testes de integração** com MockMvc (H2 em modo MySQL)
- **Docker** (multi-stage) + `docker-compose` com MySQL
- **CI** com GitHub Actions (build + testes a cada push)

## Stack

| Camada | Tecnologia |
| --- | --- |
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3 (Web, Data JPA, Validation, Actuator) |
| Banco de dados | MySQL 8.4 |
| Migrações | Flyway |
| Documentação | springdoc-openapi (Swagger UI) |
| Testes | JUnit 5, MockMvc, H2 |
| Build | Maven |
| Infra | Docker, Docker Compose, GitHub Actions |

## Endpoints

| Método | Rota | Descrição |
| --- | --- | --- |
| `GET` | `/api/tasks?status=&page=&size=` | Lista tarefas (paginado; `status` opcional) |
| `GET` | `/api/tasks/{id}` | Busca uma tarefa |
| `POST` | `/api/tasks` | Cria uma tarefa |
| `PUT` | `/api/tasks/{id}` | Atualiza uma tarefa |
| `PATCH` | `/api/tasks/{id}/status` | Atualiza somente o status |
| `DELETE` | `/api/tasks/{id}` | Remove uma tarefa |
| `GET` | `/actuator/health` | Healthcheck |

Valores aceitos: `status` = `TODO`, `IN_PROGRESS`, `DONE` · `priority` = `LOW`, `MEDIUM`, `HIGH`.

### Exemplos

Criar uma tarefa:

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Estudar Spring Boot", "priority": "HIGH", "dueDate": "2026-12-31"}'
```

Listar só as concluídas:

```bash
curl "http://localhost:8080/api/tasks?status=DONE&page=0&size=10"
```

Mudar o status:

```bash
curl -X PATCH http://localhost:8080/api/tasks/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DONE"}'
```

Exemplo de erro de validação (`400`):

```json
{
  "type": "about:blank",
  "title": "Erro de validação",
  "status": 400,
  "detail": "Dados inválidos",
  "errors": { "title": "O título é obrigatório" }
}
```

## Como rodar

### Opção 1 — Docker Compose (mais simples)

Pré-requisito: Docker.

```bash
git clone https://github.com/1jvrodrigues/taskflow-api.git
cd taskflow-api
cp .env.example .env     # opcional: ajuste as senhas
docker compose up --build
```

A API sobe em `http://localhost:8080` e o Swagger em `http://localhost:8080/swagger-ui.html`.

### Opção 2 — Local (Java + MySQL instalados)

Pré-requisitos: JDK 21, Maven e um MySQL 8 rodando.

```sql
CREATE DATABASE taskflow CHARACTER SET utf8mb4;
CREATE USER 'taskflow'@'%' IDENTIFIED BY 'taskflow';
GRANT ALL PRIVILEGES ON taskflow.* TO 'taskflow'@'%';
```

```bash
mvn spring-boot:run
```

As tabelas são criadas automaticamente pelo Flyway na primeira execução.

### Variáveis de ambiente

| Variável | Padrão | Descrição |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/taskflow` | URL JDBC do MySQL |
| `DB_USER` | `taskflow` | Usuário do banco |
| `DB_PASSWORD` | `taskflow` | Senha do banco |
| `PORT` | `8080` | Porta HTTP da aplicação |

## Testes

```bash
mvn verify
```

Os testes de integração usam H2 em modo MySQL, então **não precisam de um MySQL instalado**. Eles cobrem criação com valores padrão, validação (`400`), recurso inexistente (`404`), atualização completa e parcial, remoção e filtro por status.

## Deploy

O projeto tem um `Dockerfile` pronto, então roda em qualquer plataforma que aceite containers. Exemplo com o **Railway** (confira os planos e limites atuais da plataforma antes de começar):

1. Crie uma conta em [railway.com](https://railway.com) e um novo projeto a partir do repositório do GitHub.
2. Adicione um serviço **MySQL** ao mesmo projeto.
3. No serviço da API, em **Variables**, configure (usando as referências às variáveis do serviço MySQL):
   - `DB_URL` = `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}`
   - `DB_USER` = `${{MySQL.MYSQLUSER}}`
   - `DB_PASSWORD` = `${{MySQL.MYSQLPASSWORD}}`
4. Em **Settings → Networking**, gere um domínio público.
5. (Opcional) Configure `/actuator/health` como healthcheck.
6. Acesse `https://<seu-dominio>/swagger-ui.html` e cole o link no topo deste README.

> O nome `MySQL` nas referências deve ser igual ao nome do serviço de banco no seu projeto. A plataforma injeta a variável `PORT`, que a aplicação já respeita.

## Estrutura do projeto

```
src/main/java/com/joaovitor/taskflow
├── controller/   # Endpoints REST
├── service/      # Regras de negócio
├── repository/   # Acesso a dados (Spring Data JPA)
├── model/        # Entidade Task e enums
├── dto/          # Objetos de entrada e saída da API
├── exception/    # Exceções e tratamento global de erros
└── config/       # Configuração do OpenAPI
src/main/resources/db/migration   # Migrações Flyway
src/test                          # Testes de integração
```

## Decisões de projeto

- **DTOs como `record`**: a entidade JPA nunca é exposta diretamente pela API.
- **Flyway em vez de `ddl-auto`**: o schema é versionado e reproduzível em qualquer ambiente.
- **Problem Details (RFC 7807)**: formato padrão para erros, fácil de consumir por qualquer cliente.
- **Paginação com limite máximo**: o tamanho de página é limitado a 100 para evitar consultas pesadas.
- **H2 nos testes**: feedback rápido e CI sem dependência de banco externo.

## Próximos passos

- [ ] Autenticação com JWT (Spring Security) e tarefas por usuário
- [ ] Testes com Testcontainers rodando contra um MySQL real
- [ ] Busca por texto e ordenação configurável
- [ ] Front-end consumindo a API

## Licença

Distribuído sob a licença MIT. Veja o arquivo [LICENSE](LICENSE).

---

Desenvolvido por **João Vitor Rodrigues** · [LinkedIn](https://linkedin.com/in/1jvrodrigues) · [GitHub](https://github.com/1jvrodrigues)
