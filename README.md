# Agende360 — Documentação Técnica e de Negócios

## Índice

1. [Visão Geral](#visão-geral)
2. [Tecnologias e Dependências](#tecnologias-e-dependências)
3. [Arquitetura e Estrutura de Pacotes](#arquitetura-e-estrutura-de-pacotes)
4. [Banco de Dados e Migrations](#banco-de-dados-e-migrations)
5. [Autenticação e Segurança](#autenticação-e-segurança)
6. [Perfis de Usuário e Controle de Acesso](#perfis-de-usuário-e-controle-de-acesso)
7. [Funcionalidades e Regras de Negócio](#funcionalidades-e-regras-de-negócio)
   - [Empresa (Company)](#empresa-company)
   - [Usuários (Users)](#usuários-users)
   - [Produtos/Serviços (Product)](#produtosserviços-product)
   - [Horários de Funcionamento (Schedule)](#horários-de-funcionamento-schedule)
   - [Agendamentos (Appointment)](#agendamentos-appointment)
   - [Clientes (Customer)](#clientes-customer)
   - [Dashboard e Métricas](#dashboard-e-métricas)
   - [Configurações da Empresa](#configurações-da-empresa)
   - [Assinaturas e Planos (em desenvolvimento)](#assinaturas-e-planos-em-desenvolvimento)
   - [Redefinição de Senha](#redefinição-de-senha)
8. [Sistema de Cache](#sistema-de-cache)
9. [Sistema de Eventos Assíncronos (Outbox Pattern)](#sistema-de-eventos-assíncronos-outbox-pattern)
10. [Integração com WhatsApp](#integração-com-whatsapp)
11. [Sistema de E-mails](#sistema-de-e-mails)
12. [APIs Públicas (sem autenticação)](#apis-públicas-sem-autenticação)
13. [APIs Internas (autenticadas)](#apis-internas-autenticadas)
14. [API de Suporte](#api-de-suporte)
15. [Scheduler Automático](#scheduler-automático)
16. [Bootstrap de Dados](#bootstrap-de-dados)
17. [Modelos de Dados](#modelos-de-dados)
18. [Tratamento de Erros](#tratamento-de-erros)

---

## Visão Geral

O **Agende360** é um backend SaaS (Software as a Service) para gerenciamento de agendamentos. Cada empresa cadastrada na plataforma possui seus próprios serviços, profissionais, horários de funcionamento e agendamentos. O sistema oferece tanto uma interface de gestão interna (para administradores e profissionais da empresa) quanto uma API pública para que clientes finais possam visualizar serviços, consultar disponibilidade e criar agendamentos, sem necessidade de login.

A plataforma é multi-tenant: cada empresa opera de forma isolada, utilizando um identificador único chamado **slug**.

Além do núcleo de agendamento, o backend conta com uma camada de **cache em memória** (Caffeine) para reduzir consultas repetidas ao banco, um **padrão Outbox** para garantir a entrega confiável de notificações assíncronas (e-mail e WhatsApp) e uma **integração com a API oficial do WhatsApp (Meta)** para confirmações e lembretes de agendamento.

---

## Tecnologias e Dependências

| Tecnologia | Versão | Finalidade |
|---|---|---|
| Java | 17 | Linguagem principal |
| Spring Boot | 4.0.3 | Framework principal |
| Spring Security | (via Boot) | Autenticação e autorização |
| Spring Data JPA | (via Boot) | Persistência de dados |
| Flyway | (via Boot) | Migrations de banco de dados |
| PostgreSQL | (via driver) | Banco de dados relacional |
| JJWT | 0.12.5 | Geração e validação de tokens JWT |
| Resend Java SDK | 4.11.0 | Envio de e-mails transacionais |
| Caffeine | (via dependência) | Cache em memória (produtos, usuários, empresas, configurações) |
| Jackson (JSR-310, JDK8, Parameter Names) | (via Boot) | Serialização JSON, incluindo tipos `java.time` e `Optional` |
| Spring Boot Validation | (via Boot) | Validação de DTOs com Bean Validation |
| Spring Boot Mail | (via Boot) | Suporte a e-mail |
| Spring Boot DevTools | (via Boot) | Utilitários de desenvolvimento |
| Meta Graph API (WhatsApp Business) | v25.0 | Integração externa via HTTP para envio de mensagens de template no WhatsApp |

> **Convenção de código:** o projeto utiliza exclusivamente **Java tradicional** — laços `for`/`while`, `if/else` explícitos — sem uso de lambdas, streams, method references ou funções anônimas, inclusive nas camadas de aplicação Spring Boot.

---

## Arquitetura e Estrutura de Pacotes

O projeto segue uma arquitetura em camadas, com pacotes de topo separando domínio de aplicação, infraestrutura transversal (cache, mensageria, eventos assíncronos) e segurança:

```
br.com.corestacks.agende360
├── application
│   ├── controller      → Controllers REST da aplicação
│   ├── dto
│   │   ├── request     → Objetos de entrada das APIs
│   │   └── response    → Objetos de saída das APIs
│   ├── exception       → Exceções customizadas e handler global
│   ├── model            → Entidades JPA
│   ├── repository       → Repositórios Spring Data JPA
│   ├── scheduler         → Jobs agendados (cron) de domínio
│   ├── service            → Regras de negócio
│   ├── type                 → Enums do domínio
│   └── util                  → Utilitários gerais (senha, URLs, templates legados)
├── cache
│   └── config            → Beans dos caches Caffeine e monitor de estatísticas
├── config
│   └── JacksonConfig      → Configuração do ObjectMapper global
├── infrastructure
│   └── http
│       ├── client         → Abstração de cliente HTTP (RestClient) usada por integrações externas
│       ├── config          → Configuração de timeouts do RestClient
│       ├── exception        → Exceções de chamadas HTTP
│       └── model             → Modelos genéricos de requisição HTTP
├── messaging
│   ├── email
│   │   ├── config          → Configuração do cliente Resend
│   │   ├── dto              → Payloads de e-mail
│   │   ├── engine            → Motor de templates HTML de e-mail
│   │   └── service            → Serviço de envio de e-mail
│   └── whatsapp
│       ├── client          → Cliente da API do WhatsApp Business (Meta)
│       ├── dto               → Payloads de mensagens de template
│       ├── enums              → Constantes e nomes de templates
│       └── service             → Orquestração do envio de mensagens
├── outbox
│   ├── enums                → Tipos de evento e de agregado
│   ├── factory                → Fábrica de eventos outbox
│   ├── handler                  → Handlers que processam cada tipo de evento
│   ├── model                      → Entidade OutboxEvent
│   ├── publisher                    → Roteia eventos para o handler correto
│   ├── repository                     → Consulta e limpeza de eventos
│   ├── scheduler                       → Jobs de processamento e limpeza
│   └── service                          → Persistência e transição de status dos eventos
└── security
    ├── config              → Configuração do Spring Security
    ├── controller           → Controller de autenticação
    ├── filter                → Filtros JWT e API Key
    ├── model                  → UserDetails e RefreshToken
    ├── repository              → Repositório de RefreshToken
    ├── service                  → Serviços de JWT, cookies, tokens, reset de senha
    └── util                       → Utilitários de segurança (SecurityUtils, geração de senha)
```

---

## Banco de Dados e Migrations

O banco de dados é gerenciado pelo **Flyway**, com migrations versionadas em `src/main/resources/db/migration/`. A ordem de execução é:

| Versão | Descrição |
|---|---|
| V1 | Criação da tabela `company` |
| V2 | Criação da tabela `users` |
| V3 | Criação da tabela `customer` |
| V4 | Criação da tabela `product` |
| V5 | Criação da tabela `users_products` (associação usuário-produto) |
| V6 | Criação da tabela `appointment` |
| V7 | Criação da tabela `refresh_token` |
| V8 | Remoção da coluna `revoked` da tabela `refresh_token` |
| V9 | Adição da coluna `company_id` na tabela `product` |
| V10 | Constraint unique `(name, company_id)` na tabela `product` |
| V11 | Criação da tabela `schedule` |
| V12 | Alteração das colunas `start_time` e `end_time` da `schedule` para tipo `TIME` |
| V13 | Criação da tabela `password_reset_token` |
| V14 | Drop e recriação da tabela `password_reset_token` (sem primary key inline) |
| V15 | Renomear coluna `password_chaged_at` → `password_changed_at` na `users` |
| V16 | Criação da tabela `company_settings` |
| V17 | Adição de colunas de endereço (`logradouro`, `numero`, `bairro`, `cidade`, `estado`, `CEP`) na `company` |
| V18 | Adição da coluna `complemento` na `company` |
| V19 | Adição da coluna `is_professional` na `users` |
| V20 | Criação da tabela `subscription` (plano, status, dados de integração com Stripe) |
| V21 | Criação dos índices `idx_subscription_company` e `idx_subscription_status` |
| V22 | Renomear coluna `estado` → `uf` na `company` |
| V23 | Criação da tabela `outbox_event` e dos índices de status/data para o padrão Outbox |

---

## Autenticação e Segurança

### Modelo de Autenticação

O sistema utiliza **autenticação stateless** baseada em **JWT (JSON Web Token)**, armazenado em **cookies HttpOnly**. Existem dois tokens:

- **Access Token** (`access_token`): token JWT de curta duração, usado em todas as requisições autenticadas. Transportado via cookie HttpOnly com atributo `SameSite=Strict`.
- **Refresh Token** (`refresh_token`): token de longa duração (7 dias), armazenado no banco e usado exclusivamente nos endpoints `/api/auth/refresh` e `/api/auth/logout`.

### Endpoints de Autenticação (`/api/auth`)

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/auth/login` | Autentica usuário, gera access token e refresh token |
| POST | `/api/auth/refresh` | Renova o access token a partir do refresh token |
| POST | `/api/auth/logout` | Invalida os tokens e limpa os cookies |
| GET | `/api/auth/me` | Retorna dados do usuário autenticado |
| POST | `/api/auth/forgot-password` | Solicita redefinição de senha por e-mail |
| POST | `/api/auth/reset-password` | Redefine senha usando token recebido por e-mail |

### Fluxo de Login

1. O cliente envia `email` e `password` para `/api/auth/login`.
2. O `AuthenticationManager` valida as credenciais contra o banco.
3. É verificado se a empresa do usuário está ativa. Se inativa, a autenticação é bloqueada com erro 403.
4. Um JWT é gerado com as claims: `userId`, `role`, `companyId`, `active`, `email`, `iat`, `exp`.
5. Um Refresh Token é criado no banco e o token anterior do usuário é excluído (garantindo sessão única).
6. Ambos os tokens são enviados como cookies HttpOnly na resposta.

### Filtros de Segurança

A cadeia de filtros do Spring Security é composta por:

1. **`ApiKeyFilter`**: executado antes de tudo. Intercepta requisições destinadas a `/api/agende360/**` e exige o header `X-API-KEY`. Qualquer requisição sem a chave correta retorna 401 imediatamente.
2. **`JwtAuthenticationFilter`**: lê o cookie `access_token`, valida o JWT e popula o `SecurityContextHolder` com um `UserDetailsImpl` contendo `id`, `email`, `companyId` e `role`. Também registra em log o endpoint acessado e o IP de origem do cliente (considerando o header `X-Forwarded-For`, quando presente).

### Regras de Rota

| Padrão de URL | Acesso |
|---|---|
| `/api/auth/**` | Público (sem autenticação) |
| `/api/public/**` | Público (sem autenticação) |
| `/api/agende360/**` | Requer role `SUPPORT` + header `X-API-KEY` |
| `/api/webhook/meta/**` | Público (verificação e recebimento de eventos do WhatsApp/Meta) |
| Qualquer outra rota | Requer autenticação válida |

### Segurança dos Cookies

- `HttpOnly`: sim (não acessível via JavaScript)
- `Secure`: configurável via propriedade `cookie.secure` (deve ser `true` em produção com HTTPS)
- `SameSite=Strict` no access token: proteção contra CSRF
- O refresh token é restrito ao path `/api/auth`, limitando sua transmissão

---

## Perfis de Usuário e Controle de Acesso

O sistema possui três papéis (roles), controlados pelo enum `UserRole`:

### `SUPPORT`
- Papel reservado para o time interno da CoreStacks.
- Acesso exclusivo ao endpoint `/api/agende360/**`.
- Pode criar empresas, desativar empresas e listar todas as empresas cadastradas.
- Autenticação via JWT + API Key no header.
- Criado automaticamente na inicialização via `BootstrapData`.

### `ADMIN`
- Administrador de uma empresa específica.
- Acesso completo ao painel de gestão da sua empresa.
- Pode gerenciar: produtos, horários, usuários, agendamentos, dashboard, configurações.
- Pode se tornar também um profissional (campo `isProfessional = true`).
- Criado automaticamente junto com o cadastro da empresa.

### `PROFESSIONAL`
- Profissional vinculado a uma empresa.
- Acesso restrito: apenas visualiza seus próprios agendamentos do dia, pode marcar como concluído, no-show ou cancelar.
- Pode alterar sua própria senha.
- Criado pelo `ADMIN` da empresa via endpoint `/api/management/user`.

---

## Funcionalidades e Regras de Negócio

### Empresa (Company)

**Cadastro de Empresa** — realizado exclusivamente pelo `SUPPORT` via `/api/agende360/company`.

Regras:
- O CNPJ/documento (`document`) deve ser único na plataforma. Tentativa de duplicata retorna 409 Conflict.
- O e-mail do usuário administrador também deve ser único. Tentativa de duplicata retorna 409 Conflict.
- O **slug** é gerado automaticamente a partir do nome da empresa: letras minúsculas, espaços viram hifens, caracteres especiais são removidos.
- Caso o slug gerado já exista, um sufixo numérico incremental é adicionado (ex: `minha-empresa-2`).
- Junto com a empresa, são criados automaticamente:
  - Um registro em `company_settings` com `schedulingHorizon = 0` (sem limite).
  - Um usuário `ADMIN` com senha temporária padrão.
  - Um evento assíncrono (outbox) de boas-vindas é criado para o administrador, contendo os dados de acesso (ver [Sistema de Eventos Assíncronos](#sistema-de-eventos-assíncronos-outbox-pattern)).
- A empresa possui um endereço completo (`logradouro`, `numero`, `bairro`, `cidade`, `uf`, `cep`, `complemento`).

**Desativação de Empresa** — `/api/agende360/company/{slug}/disable`.

Regras:
- Seta `active = false` na empresa.
- Usuários da empresa ainda existem no banco mas não conseguem fazer login (bloqueio verificado no login).
- O agendamento público também é bloqueado para empresas inativas.

**Listagem de Empresas** — `/api/agende360/company` — retorna todas as empresas com dados do usuário administrador.

---

### Usuários (Users)

**Criação de usuário** — pelo `ADMIN` via `/api/management/user`.

Regras:
- O e-mail deve ser único na plataforma.
- A senha é gerada aleatoriamente com 8 caracteres (letras maiúsculas, minúsculas, números e símbolos) via `PasswordUtil`.
- O usuário é criado sem `passwordChangedAt`, o que indica que é o primeiro acesso.
- O campo `firstAccess` no endpoint `/api/auth/me` retorna `true` quando `passwordChangedAt` é nulo, sinalizando ao frontend que deve solicitar troca de senha.
- O usuário é vinculado à empresa do administrador que o criou.
- A listagem de usuários por empresa é mantida em cache (ver [Sistema de Cache](#sistema-de-cache)) e é invalidada a cada criação/alteração.

**Listagem de usuários** — `/api/management/users` — retorna todos os usuários da empresa do administrador autenticado.

**Alteração de senha** — `/api/management/change-password`.

Regras:
- A senha atual deve bater com a armazenada no banco.
- A nova senha deve ser diferente da atual.
- A confirmação de nova senha deve ser idêntica à nova senha.
- Após a troca, todos os refresh tokens do usuário são invalidados (forçando novo login em outros dispositivos).
- `passwordChangedAt` é atualizado com a data/hora atual.

**Atualização de dados** — `/api/management/user`.

Regras:
- Um `ADMIN` pode atualizar qualquer usuário da empresa.
- Um `PROFESSIONAL` só pode atualizar seus próprios dados.
- O `ADMIN` não pode alterar o próprio e-mail por este endpoint.

**Toggle Profissional** — `/api/management/user/professional`.

Regras:
- Apenas usuários com role `ADMIN` podem ativar/desativar o modo profissional.
- Quando `isProfessional = true`, o ADMIN também aparece na listagem pública de profissionais disponíveis para agendamento.

**Ativação/Desativação de usuário** — `/api/management/user/active` (`ADMIN`).

Regras:
- Apenas usuários da própria empresa podem ser ativados/desativados pelo `ADMIN`.

---

### Produtos/Serviços (Product)

Produtos representam os serviços oferecidos pela empresa (ex: corte de cabelo, consulta, etc.).

**Criação** — `POST /api/product`.

Regras:
- O nome do produto deve ser único dentro da empresa (`name + company_id` é unique).
- Preço mínimo de R$ 0,01, com até 10 dígitos inteiros e 2 casas decimais.
- Duração (`durationMinutes`) deve ser um inteiro positivo.
- Produto é criado com `active = true` por padrão.
- A lista de produtos por empresa é cacheada e invalidada a cada criação/alteração/(des)ativação.

**Atualização** — `PUT /api/product/{productId}`.

Regras:
- Apenas produtos da própria empresa podem ser editados.
- Se o nome for alterado, verifica duplicata antes de salvar.

**Desativação / Ativação** — `PATCH /api/product/{productId}/disable` e `/enable`.

Regras:
- Não é possível desativar um produto já inativo (retorna 400).
- Não é possível ativar um produto já ativo (retorna 400).
- Produtos inativos não aparecem na listagem pública e não podem ser agendados.

---

### Horários de Funcionamento (Schedule)

Define os intervalos de atendimento da empresa por dia da semana.

**Criação** — `POST /api/schedule`.

Regras:
- `startTime` deve ser anterior a `endTime`. Horários iguais também são rejeitados.
- Não é permitido criar um intervalo que se sobreponha a outro já cadastrado para o mesmo dia da semana. A verificação usa a lógica: `existente.start < novo.end AND existente.end > novo.start`.
- Múltiplos intervalos no mesmo dia são permitidos, desde que não se sobreponham (útil para pausa de almoço).

**Atualização** — `PUT /api/schedule/{scheduleId}`.

Regras:
- Mesmas regras de validação da criação.
- O próprio registro é excluído da verificação de sobreposição (para permitir edição sem conflito consigo mesmo).

**Exclusão** — `DELETE /api/schedule/{scheduleId}`.

Regras:
- Apenas horários da própria empresa podem ser excluídos.

**Listagem** — `GET /api/schedule` — retorna os horários da empresa juntamente com o `schedulingHorizon` configurado.

---

### Agendamentos (Appointment)

O ciclo de vida de um agendamento é gerenciado pelo `AppointmentService`.

**Status possíveis** (enum `AppointmentStatus`):
- `SCHEDULED` — agendado (não usado atualmente na criação, mas existe no modelo)
- `CONFIRMED` — confirmado (status padrão na criação)
- `CANCELLED` — cancelado
- `COMPLETED` — concluído
- `NO_SHOW` — cliente não compareceu

**Criação pública** — `POST /api/public/{slug}/appointment`.

Regras:
1. A empresa identificada pelo `slug` deve existir e estar ativa.
2. O produto deve pertencer à empresa e estar ativo.
3. O profissional deve pertencer à empresa e estar ativo.
4. O horário solicitado deve ser no futuro.
5. O horário deve respeitar o **horizonte de agendamento** configurado pela empresa. Se o horizonte for diferente de 0 (sem limite), a data não pode exceder `hoje + horizonte em dias`.
6. A empresa deve ter um horário de funcionamento (`schedule`) cadastrado para o dia da semana solicitado.
7. O slot solicitado (`startTime` até `startTime + durationMinutes`) deve estar completamente dentro de um intervalo de funcionamento cadastrado.
8. Não pode haver conflito com outro agendamento do mesmo profissional no mesmo período (status diferente de `CANCELLED`).
9. O cliente é criado automaticamente se não existir (buscado pelo telefone). Se já existir, seus dados são atualizados.
10. Um token único (UUID) é gerado para permitir cancelamento posterior sem autenticação.
11. O agendamento é criado com status `CONFIRMED`.
12. São agendados, via padrão Outbox, os eventos de **confirmação imediata** (WhatsApp e, se houver e-mail cadastrado, também e-mail) e, quando há pelo menos 30 horas até o início do compromisso, os eventos de **lembrete** (WhatsApp e e-mail) programados para 24 horas antes do horário marcado (ver [Sistema de Eventos Assíncronos](#sistema-de-eventos-assíncronos-outbox-pattern)).

**Consulta de slots disponíveis** — `GET /api/public/{slug}/slots?professionalId=&productId=&date=`.

Regras:
- Valida empresa, produto e dia da semana.
- Para cada intervalo de funcionamento do dia, gera slots com base na duração do produto.
- Slots que já passaram (se a data for hoje) são ignorados.
- Slots com conflito de agendamento existente são ignorados.
- Retorna apenas os slots livres.

**Cancelamento por token (público)** — `GET /api/public/appointment/cancel/{token}`.

Regras:
- O agendamento deve existir.
- Não pode cancelar um agendamento já cancelado.
- Não pode cancelar um agendamento já concluído.

**Cancelamento pelo admin/profissional** — `PATCH /api/appointment/{appointmentId}/cancel`.

Regras:
- O agendamento deve pertencer à empresa do usuário autenticado.
- Mesmas restrições de status da versão pública.

**Confirmação** — `PATCH /api/appointment/{appointmentId}/confirm` (apenas `ADMIN`).

Regras:
- Apenas agendamentos com status `SCHEDULED` podem ser confirmados.

**Conclusão** — `PATCH /api/appointment/{appointmentId}/complete` (`ADMIN` ou `PROFESSIONAL`).

Regras:
- Apenas agendamentos com status `CONFIRMED` podem ser concluídos.

**No-show** — `PATCH /api/appointment/{appointmentId}/no-show` (`ADMIN` ou `PROFESSIONAL`).

Regras:
- Apenas agendamentos com status `CONFIRMED` podem ser marcados como no-show.

**Listagem** — disponíveis para `ADMIN`:
- `GET /api/appointment` — todos os agendamentos não cancelados da empresa.
- `GET /api/appointment/today` — agendamentos do dia atual da empresa.
- `GET /api/appointment/today/me` (`PROFESSIONAL`) — agendamentos do profissional autenticado no dia atual.

**Atualização automática de status** — um job agendado (`UpdateScheduler`) marca como `COMPLETED` os agendamentos `CONFIRMED` cujo horário de término já passou (ver [Scheduler Automático](#scheduler-automático)).

---

### Clientes (Customer)

Clientes são criados automaticamente no momento do agendamento, sem necessidade de cadastro prévio.

**Identificação** — o cliente é identificado pelo número de telefone. Se já existe um cliente com aquele telefone, ele é reutilizado.

**Listagem** — `GET /api/customer` (`ADMIN`) — retorna todos os clientes que possuem ao menos um agendamento com a empresa do administrador autenticado.

**Verificação de cliente por telefone** — `POST /api/public/check/customer` (público).

Regras:
- O telefone informado deve ter ao menos 12 caracteres.
- Se o cliente existe, retorna o telefone e o e-mail mascarado (ex: `jo************hn@email.com`).
- Se não existe, retorna um mapa vazio.
- Útil para o frontend pré-preencher o formulário de agendamento.

---

### Dashboard e Métricas

Acessível apenas pelo `ADMIN` via `POST /api/dashboard/metrics`.

O body da requisição deve conter um `period` com um dos valores do enum `PeriodFilter`:
- `TODAY` — dia atual
- `WEEK` — semana atual (domingo a sábado)
- `MONTH` — mês atual
- `LAST_MONTH` — mês anterior

**Dados retornados** (`DashboardMetricsResponse`):
- `expectedRevenue` — soma dos preços dos serviços agendados no período.
- `totalAppointments` — total de agendamentos criados no período.
- `confirmedAppointments` — agendamentos que não foram cancelados, não são no-show e não foram concluídos.
- `revenueTrend` — variação percentual do faturamento em relação ao período anterior equivalente.
- `appointmentsTrend` — variação percentual do número de agendamentos.
- `chart` — pontos do gráfico: dia da semana vs. faturamento.
- `topServices` — top 5 serviços por faturamento no período.
- `insights` — frases geradas automaticamente descrevendo variações e destaques do período.

**Cálculo de tendência**: `((atual - anterior) / anterior) * 100`. Se o período anterior for zero e o atual também for zero, retorna 0%. Se o anterior for zero e o atual for maior que zero, retorna 100%.

---

### Configurações da Empresa

Acessíveis pelo `ADMIN` via `/api/settings`.

**Consultar configurações** — `GET /api/settings` — retorna nome da empresa, telefone e e-mail do admin.

**Atualizar configurações** — `PUT /api/settings` — atualiza nome da empresa, e-mail e telefone do admin.

**Horizonte de Agendamento** — `PUT /api/settings/scheduling-horizon`.

Define com quantos dias de antecedência um cliente pode criar um agendamento. Os valores válidos são controlados pelo enum `SchedulingHorizon`:
- `7` — até 7 dias no futuro
- `14` — até 14 dias no futuro
- `30` — até 30 dias no futuro
- `0` — sem limite (padrão)

---

### Assinaturas e Planos (em desenvolvimento)

O banco de dados já possui a tabela `subscription`, preparada para suportar cobrança recorrente via **Stripe**:

- `company_id` (único, um plano por empresa), `plan`, `status`.
- `stripe_customer_id` e `stripe_subscription_id` para integração com o Stripe.
- `current_period_start` / `current_period_end` e `cancel_at_period_end` para controle de ciclo de cobrança.
- Índices por `company_id` e `status` para consultas rápidas de plano ativo.

Diversos serviços (`DashboardService`, `ScheduleService`, `ProductService`, `AppointmentService`) já possuem pontos de extensão comentados para um futuro `FeatureGateService`, que deve aplicar limites por plano — por exemplo, dashboard avançado, quantidade de serviços cadastrados e quantidade de intervalos de horário por dia. **Esta funcionalidade ainda não está ativa** na aplicação.

---

### Redefinição de Senha

**Solicitação** — `POST /api/auth/forgot-password`.

Regras:
- O e-mail deve ser válido e pertencer a um usuário ativo.
- Por segurança, mesmo que o e-mail não exista, a resposta é sempre 200 OK (evita enumeração de usuários).
- Um token aleatório (UUID) é gerado, passado por SHA-256 e o hash é armazenado no banco com validade de 5 minutos.
- Um e-mail é enviado com link contendo o token em texto claro.

**Redefinição** — `POST /api/auth/reset-password`.

Regras:
- O token recebido é hasheado e comparado com o banco.
- O token deve existir, não ter sido usado e não ter expirado.
- Após o uso, o token é marcado como `used = true`.
- A senha é atualizada com BCrypt e `passwordChangedAt` é registrado.
- Nova senha deve ter entre 6 e 16 caracteres.

---

## Sistema de Cache

Para reduzir consultas repetidas ao banco em operações multi-tenant (consultadas a todo momento tanto no painel administrativo quanto na API pública), o backend mantém caches em memória usando **Caffeine**, cada um com capacidade máxima de 10.000 entradas e coleta de estatísticas habilitada:

| Cache | Chave | Valor | Uso |
|---|---|---|---|
| `productsCache` | `companyId` | `Map<productId, Product>` | Listagem e busca de produtos por empresa |
| `usersCache` | `companyId` | `Map<userId, User>` | Listagem de usuários, `/auth/me`, listagem pública de profissionais |
| `companiesCache` | `companyId` | `Company` | Lookup de empresa por id |
| `companyIdsBySlugCache` | `slug` | `companyId` | Lookup de empresa por slug (usado nas rotas públicas) |
| `companySettingsCache` | `companyId` | `CompanySettings` | Horizonte de agendamento configurado |

**Invalidação**: cada operação de escrita (criação, atualização, ativação/desativação de produtos e usuários, atualização de configurações da empresa) invalida a entrada correspondente no cache, forçando uma nova leitura do banco na próxima consulta.

**Monitoramento**: o componente `CacheMonitor` roda a cada 5 minutos e registra em log, para os caches de produtos, usuários e empresas, as métricas de `hits`, `misses`, `hitRate` e `evictions`.

---

## Sistema de Eventos Assíncronos (Outbox Pattern)

Efeitos colaterais que não devem bloquear a resposta ao usuário nem correr o risco de ser perdidos em caso de falha — como o envio de e-mails e mensagens de WhatsApp — são tratados com o **padrão Outbox**, implementado no pacote `outbox`.

### Como funciona

1. **Criação do evento**: ao final de uma operação de negócio (ex.: cadastro de empresa, criação de agendamento), um ou mais registros de `OutboxEvent` são persistidos na mesma transação do banco, com status `PENDING`. Cada evento guarda o tipo (`OutboxEventType`), o agregado de origem (`AggregateType`: `APPOINTMENT` ou `COMPANY`), o payload em JSON e, opcionalmente, um `nextAttemptAt` (para eventos agendados para o futuro, como lembretes).
2. **Processamento**: o `OutboxEventScheduler` roda a cada **30 segundos**, busca até 50 eventos pendentes cujo `nextAttemptAt` já tenha passado (ou seja nulo) e delega cada um ao handler correspondente através do `OutboxPublisherService`, que mapeia `OutboxEventType → OutboxEventHandler`.
3. **Sucesso**: o evento é marcado como `PROCESSED` e recebe um `sentAt`.
4. **Falha**: o contador `retryCount` é incrementado e o erro é registrado em `lastError`.
   - Se o número de tentativas ainda estiver abaixo do limite definido para o tipo de evento, o evento permanece `PENDING` e um novo `nextAttemptAt` é calculado com **backoff exponencial** (`min(2^retryCount * 30s, 3600s)`, ou seja, no máximo 1 hora entre tentativas).
   - Caso o limite de tentativas seja atingido, o evento é marcado como `ERROR` e não é mais reprocessado automaticamente.
5. **Limpeza**: o `OutboxEventCleanupScheduler` roda periodicamente e remove os eventos com status `PROCESSED` cujo `created_at` seja anterior a 3 dias, mantendo a tabela enxuta.

### Tipos de evento e limites de tentativa (`OutboxEventType`)

| Tipo | Máx. tentativas | Descrição |
|---|---|---|
| `COMPANY_REGISTRATION_EMAIL` | 3 | E-mail de boas-vindas com dados de acesso do administrador |
| `EMAIL_APPOINTMENT_CONFIRMATION` | 7 | E-mail de confirmação de agendamento |
| `EMAIL_APPOINTMENT_REMINDER` | 5 | E-mail de lembrete (24h antes) |
| `WHATSAPP_APPOINTMENT_CONFIRMATION` | 7 | Mensagem de WhatsApp de confirmação |
| `WHATSAPP_APPOINTMENT_REMINDER` | 5 | Mensagem de WhatsApp de lembrete (24h antes) |
| `APPOINTMENT_CANCELLED` | 10 | Reservado para notificação de cancelamento (tipo definido no enum, handler ainda não implementado) |

### Handlers implementados

- **`CompanyRegistrationEmailHandler`** — envia o e-mail de acesso criado após o cadastro de uma empresa.
- **`EmailAppointmentReminderHandler`** — envia e-mails de confirmação/lembrete de agendamento, verificando antes se o agendamento ainda existe e está com status `CONFIRMED` (evita notificar agendamentos já cancelados).
- **`WhatsAppAppointmentHandler`** — envia mensagens de WhatsApp de confirmação/lembrete, com a mesma verificação de status `CONFIRMED` antes do envio.

---

## Integração com WhatsApp

O envio de mensagens de WhatsApp é feito através da **API oficial da Meta (Graph API, versão v25.0)**, usando templates de mensagem pré-aprovados no WhatsApp Business.

- **`WhatsAppClient`** é a interface de abstração; **`MetaWhatsAppClient`** é a implementação concreta, que chama `https://graph.facebook.com/v25.0/{phoneNumberId}/messages` usando o `HttpClient` interno (baseado em `RestClient`).
- Credenciais configuradas via `system.whatsapp.phone-number-id` e `system.whatsapp.access-token`.
- **`WhatsAppService`** monta o corpo da mensagem de template a partir de um `WhatsAppAppointmentDTO`, incluindo nome do cliente, nome da empresa, data e hora formatadas, endereço, serviço (com descrição, se houver) e nome do profissional, além de um botão com link para cancelamento do agendamento.
- O template `appointment_confirmed` é usado para a confirmação e `appointment_reminder_2` para o lembrete.
- Os números de telefone são enviados com o prefixo internacional `55` (Brasil).

### Webhook do Meta (`/api/webhook/meta`)

Endpoint público exigido pela plataforma da Meta para integração com o WhatsApp Business:

| Método | Descrição |
|---|---|
| GET | Verificação de assinatura do webhook (`hub.mode`, `hub.verify_token`, `hub.challenge`), validada contra o token configurado em `system.token-webhook` |
| POST | Recebimento de eventos enviados pela Meta (atualmente apenas registrado em log; ainda não processado/persistido) |

---

## Sistema de E-mails

Os e-mails são enviados pelo serviço **Resend** (`EmailService`), utilizando templates HTML armazenados em `src/main/resources/emails/template/`.

O motor de templates (`EmailTemplateEngine`) realiza substituição de variáveis no formato `{{NOME_VARIAVEL}}` dentro dos arquivos HTML.

### Templates disponíveis

**`appointment_confirmation.html`** — Confirmação de agendamento (enviado logo após a criação do agendamento).

Variáveis: `COMPANY_NAME`, `CLIENT_NAME`, `SERVICE_NAME`, `PROFESSIONAL_NAME`, `APPOINTMENT_DATE`, `CANCEL_LINK`, `LOGRADOURO`, `NUMERO`, `BAIRRO`, `CIDADE`, `UF`, `CEP`, `COMPLEMENTO`, `YEAR`.

**`appointment_reminder.html`** — Lembrete de agendamento (enviado 24h antes, quando há tempo hábil).

Mesmas variáveis do template de confirmação.

**`company_user_created.html`** — Acesso criado para o administrador de uma nova empresa.

Variáveis: `COMPANY_NAME`, `LINK_PUBLICO`, `COMPANY_DOCUMENT`, `USER_NAME`, `USER_EMAIL`, `TEMP_PASSWORD`, `LOGIN_LINK`, `YEAR`.

**`reset_password.html`** — Redefinição de senha.

Variáveis: `CLIENT_NAME`, `RESET_LINK`, `EXPIRATION_TIME`, `YEAR`.

### Comportamento do serviço de e-mail

- Se o destinatário for nulo ou vazio, o e-mail não é enviado e um log de erro é registrado.
- Cada envio gera um `requestId` (UUID) para rastreamento em logs.
- Erros de envio lançam `RuntimeException` com a causa original — o que faz o `OutboxEventScheduler` tratar o evento como falho e reagendar uma nova tentativa.
- Logs registram início, sucesso e falha de cada envio.
- O envio de e-mails de confirmação e lembrete de agendamento é sempre disparado de forma assíncrona pelo padrão Outbox (ver seção anterior), nunca de forma síncrona na requisição do usuário.

---

## APIs Públicas (sem autenticação)

Base: `/api/public`

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/{slug}` | Informações públicas da empresa (nome, slug, horizonte de agendamento) |
| GET | `/{slug}/products` | Listagem dos produtos ativos da empresa |
| GET | `/{slug}/professionals` | Listagem dos profissionais ativos da empresa |
| GET | `/{slug}/slots` | Slots disponíveis para um profissional, produto e data |
| POST | `/{slug}/appointment` | Criação de agendamento |
| GET | `/appointment/cancel/{token}` | Cancelamento de agendamento via token |
| POST | `/check/customer` | Verifica se cliente existe pelo telefone |

Além disso, `/api/webhook/meta` (GET e POST) é público, conforme descrito na seção [Integração com WhatsApp](#integração-com-whatsapp).

---

## APIs Internas (autenticadas)

### Autenticação — `/api/auth`

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| POST | `/login` | — | Login |
| POST | `/refresh` | — | Renovar access token |
| POST | `/logout` | — | Logout |
| GET | `/me` | Autenticado | Dados do usuário logado |
| POST | `/forgot-password` | — | Solicitar reset de senha |
| POST | `/reset-password` | — | Confirmar reset de senha |

### Produtos — `/api/product`

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| GET | `/` | ADMIN | Listar produtos da empresa |
| GET | `/{productId}` | ADMIN | Buscar produto por ID |
| POST | `/` | ADMIN | Criar produto |
| PUT | `/{productId}` | ADMIN | Atualizar produto |
| PATCH | `/{productId}/disable` | ADMIN | Desativar produto |
| PATCH | `/{productId}/enable` | ADMIN | Ativar produto |

### Horários — `/api/schedule`

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| GET | `/` | ADMIN | Listar horários e horizonte |
| POST | `/` | ADMIN | Criar horário |
| PUT | `/{scheduleId}` | ADMIN | Atualizar horário |
| DELETE | `/{scheduleId}` | ADMIN | Remover horário |

### Agendamentos — `/api/appointment`

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| GET | `/` | ADMIN | Listar agendamentos da empresa |
| GET | `/today` | ADMIN | Listar agendamentos do dia |
| GET | `/today/me` | PROFESSIONAL | Agendamentos do profissional hoje |
| PATCH | `/{id}/confirm` | ADMIN | Confirmar agendamento |
| PATCH | `/{id}/complete` | ADMIN, PROFESSIONAL | Concluir agendamento |
| PATCH | `/{id}/no-show` | ADMIN, PROFESSIONAL | Marcar como não compareceu |
| PATCH | `/{id}/cancel` | ADMIN, PROFESSIONAL | Cancelar agendamento |

### Clientes — `/api/customer`

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| GET | `/` | ADMIN | Listar clientes da empresa |

### Gestão de Usuários — `/api/management`

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| POST | `/user` | ADMIN | Criar usuário |
| GET | `/users` | ADMIN | Listar usuários da empresa |
| PATCH | `/change-password` | ADMIN, PROFESSIONAL | Alterar senha |
| PUT | `/user` | ADMIN | Atualizar dados do usuário |
| PATCH | `/user/professional` | ADMIN | Ativar/desativar modo profissional |
| PATCH | `/user/active` | ADMIN | Ativar/desativar usuário |

### Configurações — `/api/settings`

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| GET | `/` | ADMIN | Consultar configurações |
| PUT | `/` | ADMIN | Atualizar configurações |
| PUT | `/scheduling-horizon` | ADMIN | Atualizar horizonte de agendamento |

### Dashboard — `/api/dashboard`

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| POST | `/metrics` | ADMIN | Métricas por período |

---

## API de Suporte

Base: `/api/agende360` — requer role `SUPPORT` + header `X-API-KEY`.

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/company` | Cadastrar nova empresa |
| PATCH | `/company/{slug}/disable` | Desativar empresa |
| GET | `/company` | Listar todas as empresas |

---

## Scheduler Automático

**`UpdateScheduler`** — executa a cada 10 minutos via cron `0 */10 * * * *`.

Funcionalidade: atualiza automaticamente o status dos agendamentos cujo `endTime` já passou e que ainda estão com status `CONFIRMED`, alterando-os para `COMPLETED`. Isso garante que agendamentos passados não fiquem eternamente como "confirmados" sem intervenção manual.

**`OutboxEventScheduler`** — executa a cada 30 segundos via cron `*/30 * * * * *`.

Funcionalidade: processa até 50 eventos pendentes do padrão Outbox por execução, delegando-os ao handler correspondente e aplicando a lógica de sucesso/retentativa descrita em [Sistema de Eventos Assíncronos](#sistema-de-eventos-assíncronos-outbox-pattern).

**`OutboxEventCleanupScheduler`** — executa periodicamente (cron `0 0 * * */2 *` no código).

Funcionalidade: remove eventos do outbox com status `PROCESSED` e `created_at` anterior a 3 dias, evitando o crescimento indefinido da tabela `outbox_event`.

**`NotificationScheduler`** — executa a cada minuto via cron `* */1 * * * *`.

Job reservado para futura implementação de um pipeline de notificações baseado em tabela de eventos (conforme anotações no próprio código-fonte); atualmente não possui lógica implementada.

---

## Bootstrap de Dados

**`BootstrapData`** — executa na inicialização da aplicação (`CommandLineRunner`).

Verifica se o usuário de suporte já existe (pelo e-mail configurado). Se não existir, cria automaticamente um usuário com role `SUPPORT`, com credenciais definidas pelas variáveis de ambiente:
- `SYSTEM.SUPPORT-EMAIL`
- `SYSTEM.SUPPORT-PASSWORD`
- `SYSTEM.SUPPORT-PHONE`

O `passwordChangedAt` é definido na criação, indicando que não é necessário trocar a senha no primeiro acesso.

---

## Modelos de Dados

### `BaseEntity` (superclasse da maioria das entidades)
- `id` (UUID, gerado automaticamente)
- `createdAt` (TIMESTAMP, preenchido no `@PrePersist`)
- `updatedAt` (TIMESTAMP, atualizado no `@PreUpdate` e via `@UpdateTimestamp`)

### `Company`
- `name`, `slug` (único), `document` (único), `active` (padrão true)
- `endereco` (embeddable: `logradouro`, `numero`, `bairro`, `cidade`, `uf`, `cep`, `complemento`)

### `User`
- `name`, `email` (único), `password` (BCrypt), `phone`
- `active` (padrão true), `role` (enum: ADMIN, PROFESSIONAL, SUPPORT)
- `companyId` (FK para company, pode ser nulo para SUPPORT)
- `passwordChangedAt`, `isProfessional`

### `Customer`
- `name`, `phone` (único, indexado), `email`

### `Product`
- `name`, `description`, `price`, `durationMinutes`
- `active` (padrão true), `companyId` (FK)
- Unique constraint em `(name, companyId)`

### `Schedule`
- `dayOfWeek` (enum), `startTime` (TIME), `endTime` (TIME), `companyId` (FK)

### `Appointment`
- `companyId`, `customerId`, `productId`, `userId` (todos FK)
- `startTime`, `endTime` (TIMESTAMP), `token`, `status` (enum)

### `RefreshToken`
- `token`, `user` (OneToOne FK), `expiresAt` (Instant)

### `PasswordResetToken`
- `userId` (FK), `tokenHash` (SHA-256), `expiresAt`, `used`

### `CompanySettings`
- `companyId` (FK), `schedulingHorizon` (SMALLINT)

### `UserProduct`
- Tabela de associação `users_products` com chave composta (`userId`, `productId`)

### `OutboxEvent`
- `id`, `aggregateId`, `aggregateType` (enum: `APPOINTMENT`, `COMPANY`)
- `eventType` (enum `OutboxEventType`), `payload` (JSONB)
- `eventStatus` (enum: `PENDING`, `PROCESSING`, `PROCESSED`, `ERROR`)
- `createdAt`, `sentAt`, `retryCount`, `nextAttemptAt`, `lastError`

### `subscription` (tabela sem entidade JPA mapeada ainda)
- `companyId` (único), `plan`, `status`
- `stripeCustomerId`, `stripeSubscriptionId`
- `currentPeriodStart`, `currentPeriodEnd`, `cancelAtPeriodEnd`

---

## Tratamento de Erros

O `GlobalExceptionHandler` (`@RestControllerAdvice`) centraliza o tratamento de exceções e mapeia para respostas HTTP padronizadas no formato `ErrorResponse { status, message, timestamp }`.

| Exceção | HTTP Status |
|---|---|
| `IllegalArgumentException` | 400 Bad Request |
| `ConflictException` | 409 Conflict |
| `UnauthorizedException` | 401 Unauthorized |
| `BadCredentialsException` | 401 Unauthorized |
| `ForbiddenException` | 403 Forbidden |
| `Exception` (genérica) | 500 Internal Server Error |

O `AuthEntryPointJwt` trata tentativas de acesso a rotas protegidas sem autenticação, retornando 401 com body JSON contendo `status`, `error`, `message` e `path`.

Validações de Bean Validation (`@Valid`) em DTOs de request são aplicadas automaticamente pelo Spring, retornando 400 para campos inválidos.
