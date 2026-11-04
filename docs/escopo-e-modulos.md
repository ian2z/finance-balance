# FINANCE BALANCE
## Documento de Visão, Escopo e Especificação dos Módulos

**Tipo de projeto:** Gestor de finanças pessoais (projeto acadêmico) — Web (prioridade) e Mobile Android
**Tecnologias:** Java + Spring Boot, PostgreSQL, React + TypeScript e Jetpack Compose (Kotlin)
**Arquitetura:** Backend único (API REST) consumido por dois clientes independentes
**Objetivo:** Centralização, organização, acompanhamento e planejamento de finanças pessoais.
**Versão:** Rascunho v1.1 — alinhado ao documento "Estrutura e Tecnologias (atualizada)"

> **Como ler este documento:** o escopo completo do projeto está documentado, mas a **ordem de entrega** não segue mais o critério de "dificuldade de implementação". Ela segue três regras:
> 1. **Backend + Web vêm primeiro** (prazo apertado).
> 2. **Mobile e suas integrações são secundários.**
> 3. **Módulos que dependem de APIs externas ou de vínculo com contas bancárias são secundários**, e o sistema nunca deve depender deles para funcionar.

---

# 1. CONTEXTO DO PROJETO

## 1.1. Apresentação

O Finance Balance é um gestor de finanças pessoais no qual o usuário registra e consulta seus gastos, receitas e investimentos. O projeto surge da necessidade de simplificar o controle financeiro, substituindo planilhas manuais por um sistema centralizado, dinâmico e automatizado.

O sistema é composto por **um único backend** que expõe uma API REST, consumida por **dois clientes**: uma aplicação **web em React** e um aplicativo **Android em Jetpack Compose**.

O projeto atende a uma disciplina de Dispositivos Móveis, então o app mobile possui requisitos específicos definidos pelo professor (seção 4.1). Por restrição de prazo, a **versão web tem prioridade** de desenvolvimento, e o mobile é construído em paralelo e de forma enxuta sobre o mesmo contrato de API.

## 1.2. Problema identificado

Muitas pessoas usam planilhas para registrar receitas, despesas, investimentos e compromissos. Isso exige atualização manual, fórmulas complexas e organização constante, o que gera:

- Falta de clareza sobre a quantidade de dinheiro disponível.
- Dificuldade para identificar onde estão os maiores gastos.
- Ausência de acompanhamento organizado de receitas e despesas.
- Dificuldade para estabelecer limites por categoria e planejar despesas futuras.
- Dependência de atualizações manuais e falta de padronização.
- Dificuldade para acompanhar a evolução patrimonial e os rendimentos.

O problema não está somente na ausência de registros, mas na dificuldade de transformá-los em informações úteis para a tomada de decisões.

## 1.3. Solução proposta

Um ambiente integrado em que o usuário registra receitas e despesas, organiza movimentações em contas e categorias, estabelece orçamentos, distribui sua renda por regras percentuais e acompanha os resultados em painéis e relatórios. O sistema evolui depois para planejamento, dívidas, investimentos e automações.

## 1.4. Objetivo geral

Desenvolver uma aplicação de gestão financeira pessoal (web e mobile, sobre um backend único) capaz de automatizar o controle de receitas, despesas, orçamentos e investimentos, oferecendo informações organizadas que auxiliem o planejamento e a tomada de decisões.

## 1.5. Objetivos específicos

- Permitir o cadastro e a autenticação de usuários.
- Registrar, editar, consultar e excluir transações (receitas e despesas) e investimentos.
- Classificar lançamentos por categoria e subcategoria e consultá-los com busca e filtros.
- Gerenciar múltiplas contas e fontes de recursos.
- Estabelecer orçamentos e regras percentuais de distribuição da renda.
- Exibir um resumo financeiro (dashboard) com totais e gráficos.
- Garantir que cada usuário acesse somente os próprios dados.
- Manter a regra de negócio **centralizada no backend**, sem duplicação entre web e mobile.
- Disponibilizar relatórios e exportação de informações.

## 1.6. Público-alvo

- **Estudantes universitários:** controlam bolsas, auxílios e gastos cotidianos.
- **Jovens adultos:** começam a administrar a própria renda.
- **Trabalhadores assalariados:** organizam despesas fixas, variáveis e investimentos.
- **Freelancers e autônomos:** acompanham rendimentos variáveis.
- **Interessados em investimentos:** acompanham patrimônio e rendimentos.

---

# 2. VISÃO GERAL DO PRODUTO

O Finance Balance é organizado em módulos, para que cada funcionalidade seja desenvolvida, testada e expandida sem comprometer o restante.

## 2.1. Critério de priorização

| Prioridade | Plataforma | Dependência externa | Descrição |
|---|---|---|---|
| **P1 — Núcleo** | Backend + Web | Nenhuma | Entrega obrigatória e primeira. Forma o MVP. |
| **P2 — Completo** | Backend + Web | Nenhuma | Restante do produto, 100% funcional sem serviço de terceiros. |
| **P3 — Secundária** | Mobile e/ou externo | APIs externas, bancos, e-mail, push, OCR | Só depois de P1 e P2. Nunca bloqueia o sistema. |

A prioridade **não** indica dificuldade. Um módulo de baixa complexidade (como recuperação de senha por e-mail) pode estar em P3 por depender de serviço externo, e um de alta complexidade interna (como cartões e parcelas) pode estar em P2.

## 2.2. Mapa dos módulos

| Nº | Módulo | Prioridade |
|---|---|---|
| 01 | Autenticação e usuários | P1 (recuperação por e-mail em P3) |
| 02 | Contas financeiras | P1 |
| 03 | Categorias e subcategorias | P1 |
| 04 | Receitas e despesas (transações) | P1 |
| 05 | Formas de pagamento | P1 |
| 06 | Pesquisa e filtros | P1 |
| 07 | Dashboard financeiro | P1 |
| 08 | Orçamentos | P1 |
| 09 | Distribuição percentual da renda | P1 |
| 10 | Exportação (CSV) | P1 |
| 11 | Anotações e etiquetas | P1 |
| 27 | Investimentos (registro manual) | P1 |
| 12 | Recorrências | P2 |
| 13 | Contas a pagar e receber | P2 |
| 14 | Cartões de crédito | P2 |
| 15 | Compras parceladas | P2 |
| 16 | Metas financeiras | P2 |
| 17 | Dívidas e empréstimos | P2 |
| 18 | Assinaturas | P2 |
| 19 | Fluxo de caixa | P2 |
| 20 | Relatórios avançados | P2 |
| 21 | Importação CSV padronizado | P2 |
| 22 | Notificações e lembretes (central interna) | P2 (e-mail/push em P3) |
| 23 | Histórico e fechamento mensal | P2 |
| 24 | Anexos e comprovantes | P2 |
| 25 | Finanças compartilhadas | P2 |
| 26 | Divisão de despesas | P2 |
| 28 | Simulador de investimentos (taxas manuais) | P2 |
| 30 | Patrimônio líquido | P2 |
| 31 | Regras de automação (palavras-chave) | P2 |
| 35 | Planejamento por cenários | P2 |
| 36 | Análises de comportamento | P2 |
| 29 | Integração com APIs financeiras | **P3** |
| 32 | Integração bancária | **P3** |
| 33 | Leitura de comprovantes (OCR) | **P3** |
| 34 | Múltiplas moedas (cotações automáticas) | **P3** |
| — | App mobile Android | **P3** (seção 7.4) |

---

# 3. OBJETIVOS DE ARQUITETURA

- O backend é a **única fonte de verdade** e a **única camada com acesso ao banco**.
- Web e mobile são clientes independentes da mesma API REST.
- Nenhuma regra de negócio é duplicada nos clientes.
- O contrato da API é documentado em OpenAPI/Swagger, permitindo que web e mobile evoluam sem depender um do outro.

---

# 4. REQUISITOS E RESTRIÇÕES

## 4.1. Requisitos do mobile (disciplina — implementação após o Web)

| Requisito | Como será atendido |
|---|---|
| Interface intuitiva e responsiva | Jetpack Compose com Material Design 3 e `WindowSizeClass` para adaptar o layout a diferentes tamanhos de tela |
| Busca com filtro e navegação entre telas | Campo de busca e filtros (tipo, categoria, período) sobre transações, com Navigation Compose |
| Banco de dados em nuvem com CRUD | PostgreSQL hospedado em nuvem, com CRUD completo de transações acessado via API (a confirmar com o professor, ver seção 15) |
| Consumo de API REST | Retrofit (com OkHttp) |

## 4.2. Restrições gerais

- Prioridade para a versão web por causa do prazo.
- Tecnologias já definidas pela equipe: Spring Boot, PostgreSQL, JWT, React e Jetpack Compose.
- Hospedagem preferencialmente em planos gratuitos, suficientes para a demonstração.
- Priorização de tecnologias gratuitas e de código aberto.
- Equipe reduzida e tempo limitado.
- Funcionalidades com dependência externa não podem ser requisito para o núcleo do sistema.

---

# 5. ARQUITETURA E STACK TECNOLÓGICA

## 5.1. Arquitetura geral

```text
[ React (Web) ]  ─┐
                  ├── HTTPS/JSON + JWT ──> [ Spring Boot ] ──> [ PostgreSQL na nuvem ]
[ Compose (App) ] ─┘      (Retrofit no mobile)
```

- Comunicação em JSON sobre HTTPS, com autenticação por token JWT no cabeçalho `Authorization: Bearer <token>`.
- O backend é responsável por regras de negócio, persistência, validações e processamento. Os clientes cuidam apenas da interface.

## 5.2. Backend (P1)

| Item | Tecnologia |
|---|---|
| Linguagem/Framework | Java com Spring Boot |
| Web/API | Spring Web (REST) |
| Segurança | Spring Security com JWT stateless (jjwt ou resource server) |
| Persistência | Spring Data JPA (Hibernate) |
| Banco de dados | PostgreSQL |
| Migrações | Flyway |
| Validação | Bean Validation (`jakarta.validation`) |
| Documentação da API | springdoc-openapi (Swagger UI) |
| Hash de senha | BCrypt |
| Testes | JUnit 5, Mockito e, se possível, Testcontainers |

## 5.3. Web (P1)

| Item | Tecnologia |
|---|---|
| Base | React com TypeScript, empacotado com Vite |
| Rotas | React Router |
| Requisições HTTP | Axios, com interceptor que injeta o JWT |
| Cache e estado de servidor | TanStack Query |
| UI | Tailwind |
| Gráficos | Recharts |
| Formulários e validação | React Hook Form + Zod |

## 5.4. Mobile Android (P3 — secundário)

| Item | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| UI | Jetpack Compose com Material Design 3 |
| Responsividade | `WindowSizeClass` |
| Navegação | Navigation Compose |
| Consumo de API | Retrofit + OkHttp + Gson ou Moshi |
| Arquitetura | MVVM com `ViewModel` e `StateFlow` |
| Armazenamento do token | DataStore ou EncryptedSharedPreferences |
| Injeção de dependência | Hilt (opcional; instanciação manual se o prazo apertar) |

## 5.5. Infraestrutura e ferramentas

| Item | Tecnologia |
|---|---|
| Banco em nuvem | Neon, Supabase ou Railway (PostgreSQL gerenciado) |
| Deploy do backend | Render, Railway ou Fly.io |
| Deploy do web | Vercel ou Netlify |
| Controle de versão | Git com repositório no GitHub |
| Testes manuais da API | Swagger UI e Postman/Insomnia |

---

# 6. ESTRUTURA DO PROJETO

Repositório único (monorepo):

```text
finance-balance/
├── backend/     # Spring Boot
├── web/         # React + Vite
├── mobile/      # Android (Compose)
└── docs/        # Documentação
```

## 6.1. Backend (camadas)

```text
backend/src/main/java/.../financebalance/
├── config/         # Segurança, CORS, OpenAPI
├── controller/     # Endpoints REST
├── service/        # Regras de negócio
├── repository/     # Interfaces Spring Data JPA
├── entity/         # Entidades JPA
├── dto/            # Objetos de entrada e saída
├── security/       # Filtro JWT, serviço de token
├── exception/      # Tratamento global de erros
└── specification/  # Filtros dinâmicos de busca

backend/src/main/resources/db/migration/   # Scripts Flyway (V1__, V2__...)
```

Fluxo: `controller → service → repository`. **Entidades JPA nunca são expostas diretamente**; a API trabalha somente com DTOs.

Com o crescimento dos módulos, dentro de cada camada os arquivos podem ser agrupados por domínio (por exemplo `service/orcamento/`, `service/cartao/`). Não é necessário criar uma estrutura separada por módulo: o ideal é separar responsabilidades, não criar um pacote para cada funcionalidade. Cartões, parcelas e faturas podem compartilhar um mesmo domínio, assim como metas e planejamento.

## 6.2. Web

```text
web/src/
├── api/          # Cliente Axios e funções de chamada
├── components/   # Componentes reutilizáveis
├── pages/        # Login, Cadastro, Dashboard, Transações, Investimentos...
├── hooks/        # Hooks de dados (TanStack Query)
├── routes/       # Rotas e rotas protegidas
├── types/        # Tipos TypeScript espelhando os DTOs
└── utils/        # Formatação de moeda e datas
```

## 6.3. Mobile (P3)

```text
mobile/app/src/main/java/.../financebalance/
├── data/
│   ├── remote/       # Interfaces Retrofit, DTOs, interceptor JWT
│   ├── local/        # DataStore (token)
│   └── repository/   # Repositórios
├── ui/
│   ├── theme/        # Tema Material 3
│   ├── navigation/   # NavHost e rotas
│   └── screens/      # Login, Home, Lista, Formulário, Detalhe
└── viewmodel/        # ViewModels com StateFlow
```

---

# 7. ESPECIFICAÇÃO DOS MÓDULOS

Cada módulo indica **prioridade**, **plataforma** e **dependência externa**. A numeração é mantida estável para rastreabilidade.

---

# 7.1. P1 — NÚCLEO (BACKEND + WEB)

Base do sistema e do MVP. Nenhum módulo desta seção depende de serviços de terceiros.

## Módulo 01 — Autenticação e gerenciamento de usuários

**Prioridade:** P1 · **Plataforma:** Backend + Web · **Dependência externa:** nenhuma (exceto recuperação por e-mail, em P3)

**Objetivo:** Garantir que cada usuário tenha acesso individualizado às suas informações financeiras.

**Funcionalidades (P1):**
- Cadastro de usuários.
- Login com emissão de JWT (access token de expiração curta; refresh token opcional conforme o prazo).
- Logout (descarte do token no cliente).
- Alteração de senha.
- Edição de informações pessoais.
- Configuração de preferências do usuário.
- Proteção das informações pertencentes a cada usuário.

**Funcionalidade secundária (P3):** recuperação de senha por e-mail, que exige serviço de envio de e-mail externo.

**Informações principais:** nome, e-mail (único), senha (hash BCrypt), data de cadastro, preferências.

**Dependências:** nenhuma. É a base dos demais módulos.

## Módulo 02 — Gerenciamento de contas financeiras

**Prioridade:** P1 · **Plataforma:** Backend + Web · **Dependência externa:** nenhuma

**Objetivo:** Organizar os locais ou fontes onde o usuário mantém seu dinheiro.

As contas são **cadastradas manualmente pelo usuário**. Não há vínculo com bancos reais (ver Módulo 32).

**Funcionalidades:**
- Cadastro, edição e exclusão de contas.
- Identificação do tipo de conta (corrente, poupança, carteira, dinheiro em espécie, carteira digital).
- Definição de saldo inicial e consulta do saldo atual.
- Ativação e desativação de contas.
- Visualização das movimentações de cada conta.

**Dependências:** Autenticação.

## Módulo 03 — Categorias e subcategorias

**Prioridade:** P1 · **Plataforma:** Backend + Web · **Dependência externa:** nenhuma

**Objetivo:** Organizar as movimentações de acordo com sua finalidade.

**Funcionalidades:**
- Categorias padrão do sistema e categorias personalizadas do usuário.
- Cadastro, edição e exclusão de categorias e subcategorias.
- Definição do tipo: receita ou despesa.
- Organização hierárquica (categoria → subcategoria).
- Identificação de categorias sem movimentações.

**Exemplos:** Alimentação (Mercado, Restaurante, Delivery); Transporte (Ônibus, Combustível, Aplicativos); Educação (Faculdade, Cursos, Material); Receitas (Salário, Bolsa, Freelance, Rendimentos).

**Dependências:** Autenticação.

## Módulo 04 — Receitas e despesas (transações)

**Prioridade:** P1 · **Plataforma:** Backend + Web · **Dependência externa:** nenhuma

**Objetivo:** Registrar e acompanhar todas as movimentações financeiras do usuário. É o módulo central: alimenta todos os demais.

**Funcionalidades:**
- Cadastro, edição, consulta e exclusão de receitas e despesas.
- Registro de **transferências entre contas** do mesmo usuário (não contam como receita nem despesa).
- Associação a conta, categoria/subcategoria e forma de pagamento.
- Definição de valor, data e descrição.
- Identificação de situação: paga ou pendente.
- Histórico de movimentações paginado.

**Informações principais:** descrição, valor, data de ocorrência, data de vencimento (quando aplicável), tipo, categoria, conta, forma de pagamento, situação.

**Dependências:** Contas e categorias.

## Módulo 05 — Formas de pagamento

**Prioridade:** P1 · **Plataforma:** Backend + Web · **Dependência externa:** nenhuma

**Objetivo:** Identificar como cada movimentação foi realizada.

**Funcionalidades:** cadastro e seleção de formas de pagamento (dinheiro, Pix, débito, crédito, transferência) e associação a receitas e despesas. Uma lista padrão é carregada por migração.

**Dependências:** Transações.

## Módulo 06 — Pesquisa e filtros financeiros

**Prioridade:** P1 · **Plataforma:** Backend + Web · **Dependência externa:** nenhuma

**Objetivo:** Facilitar a localização de movimentações.

**Funcionalidades:** busca por descrição; filtros por período, categoria, conta, tipo e situação; ordenação por data e valor; combinação de filtros; paginação.

Implementada **no backend** para atender web e mobile, com `Pageable` e `JpaSpecificationExecutor` (ou `@Query` com parâmetros opcionais). Ver seção 10.2.

**Dependências:** Transações.

## Módulo 07 — Dashboard financeiro

**Prioridade:** P1 · **Plataforma:** Backend + Web · **Dependência externa:** nenhuma

**Objetivo:** Apresentar um resumo visual da situação financeira.

**Funcionalidades:**
- Saldo total, total de receitas e de despesas do período e resultado do mês.
- Comparação entre receitas e despesas.
- Gráfico de despesas por categoria e de evolução dos gastos (Recharts).
- Maiores gastos.
- Indicador de orçamento consumido.
- Filtro por mês e período.

Os totais são calculados no backend (`GET /dashboard/resumo`) e **não confundem transferências entre contas com receitas ou despesas**.

**Dependências:** Contas, transações e (para o indicador) orçamentos.

## Módulo 08 — Orçamentos

**Prioridade:** P1 · **Plataforma:** Backend + Web · **Dependência externa:** nenhuma

**Objetivo:** Estabelecer limites de gastos por categoria e período.

**Funcionalidades:** cadastro de orçamento mensal e de limites por categoria; consulta de valor planejado, utilizado, restante e percentual consumido; identificação de categorias próximas do limite; comparação entre planejado e realizado.

**Exemplo:** orçamento de Alimentação R$ 500, utilizado R$ 350, restante R$ 150, 70% consumido.

> **Nota de escopo:** o documento de Estrutura lista "metas e orçamentos mensais" como escopo futuro. Este documento os mantém no núcleo por serem parte do projeto completo; se o prazo apertar, orçamentos são o primeiro módulo de P1 a ser adiado, depois do dashboard.

**Dependências:** Categorias e transações.

## Módulo 09 — Regras de distribuição percentual da renda

**Prioridade:** P1 · **Plataforma:** Backend + Web · **Dependência externa:** nenhuma

**Objetivo:** Automatizar a divisão da renda conforme percentuais personalizados. Funcionalidade central da proposta inicial.

**Funcionalidades:** cadastro de regras percentuais por categoria; cálculo automático dos valores; atualização quando a renda mudar; visualização da distribuição planejada; comparação entre planejada e realizada. O sistema valida a soma dos percentuais.

**Exemplo (renda R$ 2.000):** Alimentação 20% (R$ 400); Transporte 10% (R$ 200); Educação 10% (R$ 200); Investimentos 20% (R$ 400); Reserva 20% (R$ 400); Livres 20% (R$ 400).

A distribuição é **planejamento**: ela não significa que o dinheiro já foi movimentado.

**Dependências:** Receitas, categorias e orçamentos.

## Módulo 10 — Exportação de informações

**Prioridade:** P1 · **Plataforma:** Backend + Web · **Dependência externa:** nenhuma

**Funcionalidades:** exportação de movimentações em CSV, por período, por categoria e de relatórios mensais; download do arquivo.

**Dependências:** Transações.

## Módulo 11 — Anotações e etiquetas financeiras

**Prioridade:** P1 · **Plataforma:** Backend + Web · **Dependência externa:** nenhuma

**Funcionalidades:** observações em movimentações; cadastro de etiquetas personalizadas; associação de etiquetas a movimentações; pesquisa por etiqueta.

**Exemplo:** etiqueta "Viagem" agrupa todos os gastos daquela atividade.

**Dependências:** Transações.

## Módulo 27 — Gerenciamento de investimentos (registro manual)

**Prioridade:** P1 · **Plataforma:** Backend + Web · **Dependência externa:** nenhuma

Investimentos fazem parte do núcleo do documento de Estrutura (entidade `Investimento` e página própria no Web). Nesta fase tudo é **informado manualmente**, sem cotações automáticas.

**Funcionalidades:** CRUD de investimentos; modalidade (CDB, Tesouro Direto, FIIs, ações, fundos, poupança); valor aplicado e valor atual; data de aplicação; registro de aportes, resgates e rendimentos; histórico; distribuição da carteira.

Aportes e resgates são separados dos rendimentos para que a evolução do patrimônio seja calculada corretamente.

**Atualização automática de cotações:** secundária (P3, Módulo 29).

**Dependências:** Autenticação (e, opcionalmente, contas).

---

# 7.2. P2 — PRODUTO COMPLETO (BACKEND + WEB, SEM DEPENDÊNCIA EXTERNA)

Implementados depois do núcleo, ainda na frente Backend + Web, sem exigir nenhum serviço de terceiros.

## Módulo 12 — Despesas e receitas recorrentes

**Objetivo:** Automatizar movimentações periódicas.
**Funcionalidades:** periodicidade semanal, mensal ou anual; data inicial e final; geração de movimentações futuras; edição e cancelamento da regra; consulta das próximas. Geração por job agendado do próprio Spring (`@Scheduled`), **sem duplicidade** e com revisão pelo usuário.
**Exemplos:** salário, mensalidade, internet, academia.

## Módulo 13 — Contas a pagar e receber

**Objetivo:** Gerenciar compromissos ainda não pagos ou recebidos.
**Funcionalidades:** cadastro de contas a pagar e valores a receber; vencimentos; situação paga/pendente; pagamentos parciais; histórico de pagamentos; visualização de compromissos futuros.

## Módulo 14 — Cartões de crédito

**Objetivo:** Acompanhar cartões e as despesas feitas por eles.
**Funcionalidades:** cadastro de cartões; limite total e disponível; fechamento e vencimento; registro de compras; fatura atual e anteriores; despesas por cartão.
**Regra crítica:** compras e pagamento da fatura não podem contabilizar a mesma despesa duas vezes.

## Módulo 15 — Compras parceladas

**Objetivo:** Controlar despesas em várias parcelas.
**Funcionalidades:** compra parcelada com quantidade de parcelas e valor de cada uma; parcelas pagas e pendentes; comprometimento futuro; associação a cartões. Cada parcela tem identificação própria e vínculo com a compra original.
**Exemplo:** R$ 1.200 em 6 parcelas de R$ 200.

## Módulo 16 — Metas financeiras

**Objetivo:** Estabelecer objetivos e acompanhar o progresso.
**Funcionalidades:** valor desejado e prazo; aportes; valor acumulado e percentual concluído; histórico de contribuições; metas atingidas.

## Módulo 17 — Dívidas e empréstimos

**Objetivo:** Centralizar compromissos assumidos.
**Funcionalidades:** cadastro de dívidas e empréstimos; credor; valor inicial; parcelas; valores pagos; saldo devedor; juros informados pelo usuário; histórico; estimativa de quitação.

## Módulo 18 — Assinaturas

**Objetivo:** Acompanhar serviços de cobrança periódica.
**Funcionalidades:** fornecedor, valor e periodicidade; próxima cobrança; reajustes; ativas e canceladas; gasto mensal total.

## Módulo 19 — Planejamento de fluxo de caixa

**Objetivo:** Visualizar a movimentação esperada nos próximos períodos.
**Funcionalidades:** projeção de receitas e despesas futuras usando recorrências e contas pendentes; saldo futuro estimado; visão por semana ou mês; períodos de maior comprometimento.

## Módulo 20 — Relatórios financeiros avançados

**Funcionalidades:** relatórios mensal, anual, de receitas, de despesas e por categoria; comparação entre meses; maiores despesas; evolução de renda e gastos; planejado versus realizado; exportação.

## Módulo 21 — Importação de movimentações (CSV padronizado)

**Funcionalidades:** importação de CSV em **formato padronizado do próprio sistema**; mapeamento de colunas; pré-visualização; detecção de duplicidades; confirmação antes de salvar.
Não há integração com instituições financeiras (ver Módulo 32).

## Módulo 22 — Notificações e lembretes (central interna)

**Funcionalidades:** lembretes de vencimento; orçamento próximo do limite ou ultrapassado; contas pendentes; metas; central de notificações com marcação de lida. As notificações são **exibidas dentro do sistema**.
**Secundário (P3):** envio por e-mail e push no celular, que dependem de serviços externos.

## Módulo 23 — Histórico e fechamento mensal

**Funcionalidades:** histórico mensal; saldos de encerramento; orçamentos realizados; comparação entre períodos. O fechamento cria um **retrato (snapshot)** do período, para que mudanças posteriores não alterem a consulta histórica.

## Módulo 24 — Anexos e comprovantes

**Funcionalidades:** upload de comprovantes associados a receitas e despesas; consulta e remoção; limite de formatos e tamanhos.
**Nota:** nesta fase o armazenamento é simples (disco do servidor ou banco). Se for migrado para armazenamento em nuvem (S3 ou equivalente), passa a ser dependência externa.

## Módulo 25 — Finanças compartilhadas

**Funcionalidades:** grupos financeiros; convite de participantes (por vínculo interno entre usuários cadastrados, sem envio de e-mail); associação de despesas ao grupo; responsáveis; permissões; resumo coletivo.
**Atenção:** quebra a regra "um usuário só vê os próprios dados". As permissões de grupo devem ser definidas e testadas explicitamente.

## Módulo 26 — Divisão de despesas

**Funcionalidades:** despesa compartilhada; participantes; divisão igualitária ou por valores personalizados; valores individuais; acompanhamento de pagamentos; pendências.

## Módulo 28 — Simulador de investimentos (taxas manuais)

**Funcionalidades:** valor inicial, aportes periódicos, prazo e taxa informados pelo usuário; juros compostos; comparação de cenários; evolução projetada; rendimento bruto e líquido quando os parâmetros tributários forem informados.
Consulta automática de taxas é secundária (P3, Módulo 29).

## Módulo 30 — Patrimônio líquido

**Funcionalidades:** cadastro de bens e ativos (incluindo investimentos do Módulo 27); obrigações e dívidas (Módulo 17); cálculo do patrimônio líquido (ativos − obrigações); histórico e composição.

## Módulo 31 — Regras de automação financeira (palavras-chave)

**Funcionalidades:** regras personalizadas por palavra-chave ou valor; sugestão automática de categoria; aplicação de etiquetas. Totalmente interna e determinística, **sem IA externa**.
**Exemplo:** descrição "SUPERMERCADO" sugere a categoria Alimentação.

## Módulo 35 — Planejamento financeiro por cenários

**Funcionalidades:** simulação de variação de renda e gastos; comparação de cenários; estimativa de alcance de metas; visualização dos impactos. Usa apenas dados do próprio usuário.

## Módulo 36 — Análises de comportamento financeiro

**Funcionalidades:** categorias com maior crescimento; comparação entre períodos; relação entre renda e despesas; meses de maior economia; sugestões de revisão de orçamento. Baseado nos dados registrados e **sem apresentar previsões como garantia**.

---

# 7.3. P3 — SECUNDÁRIOS: INTEGRAÇÕES EXTERNAS E VÍNCULO BANCÁRIO

Só devem ser iniciados depois de P1 e P2. O sistema **não pode depender** deles: se estiverem indisponíveis, tudo o que usa dados manuais continua funcionando.

## Módulo 29 — Integração com APIs financeiras

**Dependência externa:** APIs de indicadores, taxas e cotações.
**Funcionalidades possíveis:** indicadores e taxas de referência (Selic, CDI, IPCA); cotações e informações de ativos; atualização do simulador (Módulo 28) e dos valores de investimentos (Módulo 27); histórico de consultas com data e origem dos dados.
Usar serviços com documentação e condições compatíveis com o projeto. Em caso de falha da API, aplicar cache e fallback para valores manuais.

## Módulo 32 — Integração bancária

**Dependência externa:** instituições financeiras e agregadores (Open Finance).
**Funcionalidades possíveis:** conexão autorizada; importação automatizada e sincronização periódica; atualização de contas; identificação de duplicidades.
Exige estudo de autorização, segurança, privacidade e disponibilidade. **Complexidade muito alta** e fora do prazo.

## Módulo 33 — Leitura de comprovantes e documentos

**Dependência externa:** serviço de OCR ou IA.
**Funcionalidades possíveis:** extração de valor, data e informações; sugestão de descrição e categoria; pré-preenchimento. O usuário sempre revisa e confirma antes de salvar.

## Módulo 34 — Múltiplas moedas

**Dependência externa:** API de cotações.
**Funcionalidades:** cadastro de moedas; movimentações em moedas diferentes; conversão para moeda de referência; registro das taxas usadas; histórico de conversões.
Uma versão com **taxa digitada manualmente** pode ser feita antes, mas o módulo completo depende da API de cotações.

## Extensões secundárias de módulos anteriores

| Extensão | Módulo de origem | Motivo |
|---|---|---|
| Recuperação de senha por e-mail | 01 | Serviço de envio de e-mail |
| Notificações por e-mail e push | 22 | E-mail e FCM |
| Armazenamento de anexos em nuvem | 24 | Serviço de storage |
| Cotações automáticas em investimentos | 27 | API financeira |

---

# 7.4. P3 — APLICATIVO MOBILE ANDROID

O mobile é **secundário em relação ao Web**, mas é requisito da disciplina (seção 4.1). É construído de forma **enxuta**, sobre o contrato OpenAPI que o backend já terá documentado.

**Escopo mínimo (atende à disciplina):**
- Login (consumindo `/auth/login`) e armazenamento do token em DataStore ou EncryptedSharedPreferences.
- Tela Home com resumo.
- Lista de transações com **busca e filtros** (tipo, categoria, período).
- Formulário de criação e edição, detalhe e exclusão (**CRUD completo**).
- Navegação com Navigation Compose.
- Interface responsiva com `WindowSizeClass` e Material Design 3.

**Escopo ampliado (se houver tempo, na ordem):** categorias e contas, orçamentos, investimentos, metas.

**Escopo futuro do mobile:** push, login biométrico, modo offline com sincronização e leitura de nota fiscal pela câmera (seção 16).

**Regra:** o mobile **não cria endpoints próprios nem duplica regra de negócio**. Se precisar de algo novo, a mudança entra no backend e no contrato OpenAPI.

---

# 8. REQUISITOS NÃO FUNCIONAIS

## 8.1. Segurança

- Autenticação JWT stateless; senhas somente com hash BCrypt.
- Todas as rotas, exceto `/auth/**` e a documentação Swagger, exigem token válido.
- **Controle de propriedade** verificado no service, e não apenas no controller: um usuário nunca acessa dados de outro.
- CORS configurado para a origem do front-end web.
- Validações sempre no backend (Bean Validation).
- Segredos (chave do JWT, credenciais do banco) em variáveis de ambiente, nunca no repositório.
- HTTPS obrigatório em produção.

## 8.2. Privacidade e LGPD

O sistema trata dados financeiros e pessoais e deve considerar a LGPD:

- Transparência sobre o tratamento e coleta apenas do necessário.
- Controle de acesso e proteção contra acesso não autorizado.
- Possibilidade de consulta e exportação dos dados pessoais.
- Procedimento de exclusão de conta e dados.
- Registro de operações relevantes.

## 8.3. Usabilidade e responsividade

Interface intuitiva, formulários com validação (React Hook Form + Zod) e mensagens claras. O Web funciona em computadores, tablets e smartphones, e o mobile se adapta via `WindowSizeClass`.

## 8.4. Compatibilidade

Navegadores modernos (Web) e Android com suporte a Compose (Mobile).

## 8.5. Manutenibilidade

Código em camadas com responsabilidades definidas, DTOs separados de entidades, migrações versionadas por Flyway e contrato documentado em OpenAPI.

## 8.6. Integridade financeira

- Valores monetários em `BigDecimal` no Java e `NUMERIC(15,2)` no banco, **nunca** `float` ou `double`.
- Datas em formato ISO 8601 na API.
- Consistência entre movimentações, contas e saldos preservada (operações transacionais).

## 8.7. Confiabilidade

Cálculos financeiros (saldo, transferências, parcelas, distribuição, rendimentos) devem ter testes unitários próprios (JUnit 5 + Mockito).

---

# 9. MODELO DE DADOS

## 9.1. Núcleo (conforme documento de Estrutura)

| Entidade | Campos principais |
|---|---|
| Usuario | id, nome, email (único), senha (hash), criadoEm |
| Categoria | id, nome, tipo (RECEITA ou DESPESA), usuario (opcional, para categorias personalizadas) |
| Transacao | id, descricao, valor, tipo (RECEITA ou DESPESA), data, categoria, usuario |
| Investimento | id, nome, tipoInvestimento, valorAplicado, valorAtual, dataAplicacao, usuario |

Relações: um Usuario possui muitas Transacoes e Investimentos; uma Categoria classifica muitas Transacoes.

## 9.2. Extensão para o escopo completo

Entidades adicionais necessárias para os módulos de P1 e P2. Vão sendo criadas por migrações Flyway conforme o módulo é implementado.

| Entidade | Campos principais | Módulo | Fase |
|---|---|---|---|
| Conta | id, nome, tipo, saldoInicial, ativa, usuario | 02 | P1 |
| Categoria (ajuste) | + categoriaPai (autorreferência, para subcategorias) | 03 | P1 |
| Transacao (ajuste) | + conta, formaPagamento, situação (PAGA/PENDENTE), dataVencimento, observação; tipo ganha TRANSFERENCIA (+ contaDestino) | 04, 11 | P1 |
| FormaPagamento | id, nome | 05 | P1 |
| Etiqueta / TransacaoEtiqueta | id, nome, usuario / vínculo N:N | 11 | P1 |
| Orcamento | id, categoria, mês/período, valorLimite, usuario | 08 | P1 |
| RegraDistribuicao | id, categoria, percentual, rendaBase, usuario | 09 | P1 |
| MovimentoInvestimento | id, investimento, tipo (APORTE/RESGATE/RENDIMENTO), valor, data | 27 | P1 |
| Recorrencia | id, modelo de transação, periodicidade, início, fim, ativa | 12 | P2 |
| Cartao / Fatura / CompraCartao | limite, fechamento, vencimento / período, total / valor | 14 | P2 |
| CompraParcelada / Parcela | total, nº parcelas / valor, vencimento, situação | 15 | P2 |
| Meta / AporteMeta | valorAlvo, prazo / valor, data | 16 | P2 |
| Divida / PagamentoDivida | credor, valorInicial, juros / valor, data | 17 | P2 |
| Assinatura | fornecedor, valor, periodicidade, próxima cobrança, ativa | 18 | P2 |
| Notificacao | tipo, mensagem, lida, criadoEm | 22 | P2 |
| SnapshotMensal | mês, saldos, totais, orçamentos realizados | 23 | P2 |
| Anexo | transação, nome, tipo, tamanho, caminho | 24 | P2 |
| Grupo / MembroGrupo | nome / usuário, permissão | 25 | P2 |
| DespesaCompartilhada / Participacao | total / participante, valor, pago | 26 | P2 |
| Bem | nome, valor, tipo | 30 | P2 |
| RegraAutomacao | palavraChave, categoria, etiqueta | 31 | P2 |
| Cotacao / Moeda | origem, valor, data | 29, 34 | P3 |
| ConexaoBancaria | instituição, token, status | 32 | P3 |

## 9.3. Convenções

- Valores monetários: `BigDecimal` / `NUMERIC(15,2)`.
- Datas em ISO 8601.
- **Todo registro pertence a um usuário** e toda consulta é filtrada pelo usuário autenticado.
- Entidades JPA nunca são expostas; a API só trabalha com DTOs.

---

# 10. API REST

O prefixo e o versionamento (por exemplo `/api/v1`) serão definidos na implementação. O contrato é documentado em OpenAPI (Swagger UI).

## 10.1. Endpoints

**P1 — Núcleo**

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| POST | `/auth/register` | Cadastro de usuário | Não |
| POST | `/auth/login` | Login, retorna JWT | Não |
| GET/PUT | `/usuarios/me` | Perfil e preferências | Sim |
| PUT | `/usuarios/me/senha` | Alteração de senha | Sim |
| GET | `/transacoes` | Lista paginada com filtros | Sim |
| GET | `/transacoes/{id}` | Detalhe de uma transação | Sim |
| POST | `/transacoes` | Cria transação | Sim |
| PUT | `/transacoes/{id}` | Atualiza transação | Sim |
| DELETE | `/transacoes/{id}` | Remove transação | Sim |
| POST | `/transferencias` | Transferência entre contas | Sim |
| GET/POST/PUT/DELETE | `/contas` | CRUD de contas | Sim |
| GET/POST/PUT/DELETE | `/categorias` | Categorias e subcategorias | Sim |
| GET | `/formas-pagamento` | Lista formas de pagamento | Sim |
| GET/POST/PUT/DELETE | `/etiquetas` | CRUD de etiquetas | Sim |
| GET/POST/PUT/DELETE | `/orcamentos` | CRUD de orçamentos e consumo | Sim |
| GET/POST/PUT/DELETE | `/distribuicao` | Regras de distribuição | Sim |
| GET/POST/PUT/DELETE | `/investimentos` | CRUD de investimentos | Sim |
| POST | `/investimentos/{id}/movimentos` | Aporte, resgate ou rendimento | Sim |
| GET | `/dashboard/resumo` | Totais e dados para gráficos | Sim |
| GET | `/exportacao/transacoes.csv` | Exportação CSV | Sim |

**P2 — Produto completo** (grupos de rotas, detalhadas na implementação de cada módulo)

| Rota base | Módulo |
|---|---|
| `/recorrencias` | 12 |
| `/contas-pagar-receber` | 13 |
| `/cartoes`, `/faturas`, `/compras-parceladas` | 14, 15 |
| `/metas` | 16 |
| `/dividas` | 17 |
| `/assinaturas` | 18 |
| `/fluxo-caixa` | 19 |
| `/relatorios` | 20 |
| `/importacao` | 21 |
| `/notificacoes` | 22 |
| `/fechamentos` | 23 |
| `/anexos` | 24 |
| `/grupos`, `/despesas-compartilhadas` | 25, 26 |
| `/simulador` | 28 |
| `/patrimonio` | 30 |
| `/regras-automacao` | 31 |
| `/cenarios`, `/analises` | 35, 36 |

**P3 — Secundários:** `/integracoes/**` (29, 32, 33, 34).

## 10.2. Busca com filtros

Implementada no backend para atender web e mobile:

```
GET /transacoes?texto=mercado&categoriaId=3&tipo=DESPESA&de=2026-10-01&ate=2026-10-31&page=0&size=20
```

Todos os parâmetros são opcionais. Parâmetros adicionais previstos: `contaId`, `situacao`, `etiquetaId`, `sort`.

## 10.3. Padrão de erro

Todos os erros seguem o mesmo formato, para que web e mobile os tratem de forma idêntica:

```json
{
  "mensagem": "Descrição do erro",
  "campos": { "valor": "deve ser maior que zero" }
}
```

Códigos HTTP usados: 200, 201, 204, 400 (validação), 401 (não autenticado), 403 (sem permissão), 404 (não encontrado) e 409 (conflito, como e-mail já cadastrado).

---

# 11. REGRAS DE NEGÓCIO

### 11.1. Receitas e despesas
Receitas aumentam os recursos e despesas representam sua utilização. O tipo de cada movimentação deve ser identificado corretamente.

### 11.2. Transferências entre contas
Transferências entre contas do mesmo usuário **não** são receita nem despesa no resultado consolidado.

### 11.3. Cálculo de saldo
O saldo considera o saldo inicial e as movimentações efetivamente contabilizadas (pagas).

### 11.4. Orçamentos
O consumo considera as despesas do período e da categoria correspondente.

### 11.5. Distribuição percentual
Os percentuais são validados. Planejamento (orçamento e distribuição) é distinto das movimentações realizadas.

### 11.6. Recorrências
A geração respeita a periodicidade e evita duplicidades.

### 11.7. Histórico
O fechamento preserva informações suficientes para consultas e comparações históricas.

### 11.8. Compras parceladas e cartões
As parcelas têm identificação própria e vínculo com a compra original. A compra no cartão e o pagamento da fatura não podem ser contados duas vezes.

### 11.9. Investimentos
Aportes e resgates são separados dos rendimentos.

### 11.10. Isolamento de dados
Todo registro pertence a um usuário e toda consulta é filtrada pelo usuário autenticado, verificada no service.

### 11.11. Independência de serviços externos
Nenhuma funcionalidade P1 ou P2 pode depender de API externa. Dados de fontes externas são sempre complementares, com data e origem, e com fallback manual.

---

# 12. SEGURANÇA — VARIÁVEIS DE AMBIENTE

Variáveis principais: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`, `CORS_ALLOWED_ORIGINS`.

Nenhum segredo vai para o repositório.

---

# 13. AMBIENTES E DEPLOY

| Ambiente | Descrição |
|---|---|
| Local | Spring em `localhost:8080`, Postgres local ou em nuvem, React com Vite em `localhost:5173` |
| Produção/demonstração | Backend no Render; banco no Supabase; web no Vercel |

**Observações para o mobile:**
- No emulador Android, a API local é acessada por `http://10.0.2.2:8080`. Tráfego HTTP puro exige `usesCleartextTraffic` no manifesto, o que só vale para desenvolvimento.
- Em celular físico, usar o IP da máquina na rede local.
- Com a API em HTTPS e URL pública, web e mobile usam o mesmo endereço. **O deploy do backend deve ocorrer cedo.**

---

# 14. PLANO DE DESENVOLVIMENTO

## 14.1. Cronograma

| Fase | Foco | Entregas |
|---|---|---|
| Semana 1 | Backend (P1) | Modelagem, Flyway, autenticação JWT, CRUD de transações, Swagger e deploy inicial |
| Semana 2 | Web (P1) | Login, cadastro, transações com filtros, dashboard e investimentos |
| Semana 2 em diante (paralelo) | Mobile (P3) | Início do app, aproveitando o contrato já documentado no Swagger |
| Semana 3 | Mobile (P3) | Login, lista com busca e filtro, CRUD, navegação e responsividade |
| Final | Fechamento | Testes, polimento, README com instruções de execução e apresentação |

## 14.2. Ordem de entrega dos módulos

| Etapa | Foco | Módulos | Resultado esperado |
|---|---|---|---|
| 1 | Base — Backend + Web | 01, 02, 03, 04, 05 | O usuário registra e organiza movimentações |
| 2 | Controle básico — Backend + Web | 06, 07, 08, 11, 10, 27 | O usuário consulta gastos e entende sua situação |
| 3 | Planejamento — Backend + Web | 09, 12, 13, 19, 20 | O sistema passa a planejar, não só registrar |
| 4 | Avançado — Backend + Web | 14, 15, 16, 17, 18, 22, 23, 21, 24 | Compromissos, objetivos e histórico |
| 5 | Expansão — Backend + Web | 25, 26, 28, 30, 31, 35, 36 | Plataforma mais completa, ainda sem serviço externo |
| 6 | Secundário | Mobile, 29, 32, 33, 34 e extensões da seção 7.3 | Integrações e mobile ampliado |

O mobile mínimo (seção 7.4) roda **em paralelo** às etapas 2 em diante por ser requisito da disciplina, mas sem tirar tempo de P1.

---

# 15. ESCOPO DO MVP

O MVP é composto apenas por módulos P1, no Backend + Web:

1. Criar conta e acessar o sistema.
2. Cadastrar contas financeiras.
3. Criar categorias e subcategorias.
4. Registrar receitas e despesas (e transferências).
5. Consultar saldo.
6. Filtrar movimentações.
7. Definir orçamentos mensais.
8. Criar regras de distribuição percentual.
9. Registrar investimentos.
10. Visualizar um dashboard financeiro.
11. Exportar movimentações em CSV.

O **fluxo essencial** (autenticação, transações, filtros e dashboard) precisa estar pronto antes de qualquer recurso extra. Se o prazo apertar, a ordem de corte em P1 é: exportação, distribuição percentual, orçamentos, anotações e etiquetas.

---

# 16. RISCOS, DECISÕES EM ABERTO E ESCOPO FUTURO

## 16.1. Decisões em aberto

- **Banco em nuvem exigido pelo professor:** a proposta é usar PostgreSQL hospedado como o banco em nuvem, com CRUD via API. Se for exigido Cloud Firestore especificamente, usá-lo para uma funcionalidade exclusiva do mobile (por exemplo metas ou lembretes), com CRUD completo, mantendo o núcleo financeiro no Spring. **Confirmar com o professor.**
- **Conflito de prazo entre mobile e web:** o mobile é secundário no escopo do produto, mas é entrega obrigatória da disciplina. É preciso garantir o escopo mínimo da seção 7.4 dentro do prazo.

## 16.2. Riscos técnicos

- Inconsistência entre saldos e movimentações.
- Falhas nas regras financeiras (transferências, cartões, parcelas).
- Complexidade crescente da modelagem.
- Vulnerabilidades de segurança e vazamento entre usuários (agravado pelo Módulo 25).
- Dificuldade em garantir a integridade dos dados históricos.

## 16.3. Riscos de projeto

- **Prazo apertado:** priorizar o fluxo essencial antes de qualquer recurso extra.
- **Planos gratuitos de hospedagem** podem hibernar por inatividade, causando a primeira requisição lenta. Acordar o serviço antes da apresentação.
- **Divergência entre clientes:** mitigada pelo contrato OpenAPI e pelo padrão único de erros.
- Ampliação excessiva do escopo, equipe reduzida e falta de testes para regras financeiras.

## 16.4. Estratégias de mitigação

- Entrega incremental por prioridade; módulos P3 nunca bloqueiam P1 e P2.
- Integrações externas não são requisito indispensável.
- Testes unitários dedicados aos cálculos financeiros.
- Contrato OpenAPI como ponto único de acordo entre web e mobile.

## 16.5. Escopo futuro (fora do prazo atual)

- Notificações push (lembrete de contas a pagar).
- Login biométrico no mobile.
- Modo offline com banco local e sincronização.
- Leitura de nota fiscal pela câmera (relacionada ao Módulo 33).
- Integração bancária (Módulo 32) e demais itens de P3.

A importação e exportação de extratos em CSV, listadas como futuras no documento de Estrutura, estão aqui mapeadas nos Módulos 10 (P1) e 21 (P2). Metas e orçamentos mensais estão nos Módulos 16 (P2) e 08 (P1).

---

# 17. RESULTADOS ESPERADOS

Ao final do desenvolvimento, espera-se que o Finance Balance ofereça uma alternativa mais organizada e automatizada às planilhas de controle financeiro, com maior visibilidade de receitas, despesas e compromissos e ferramentas de planejamento. A estrutura modular permite incorporar novas funcionalidades gradualmente, mantendo o sistema organizado.

A estratégia é entregar incrementalmente, com **Backend + Web primeiro**, mantendo o mobile e as integrações externas como expansão. O foco principal é construir um sistema excelente em **registrar, categorizar, consultar e resumir movimentações financeiras**, núcleo sobre o qual os demais módulos funcionam.

Esta documentação pode servir de base para requisitos funcionais, histórias de usuário, diagramas UML e modelagem do banco de dados.

---

# 18. GLOSSÁRIO

- **API REST:** interface de comunicação entre clientes e servidor via HTTP e JSON.
- **JWT:** token assinado usado para autenticar requisições sem manter sessão no servidor.
- **DTO:** objeto usado para trafegar dados na API, separado das entidades do banco.
- **Flyway:** ferramenta que versiona alterações do schema do banco por meio de scripts.
- **MVVM:** padrão de arquitetura que separa interface (View), estado e lógica de apresentação (ViewModel) e dados (Model).
- **OpenAPI/Swagger:** especificação e interface de documentação do contrato da API.
- **Snapshot mensal:** retrato dos dados financeiros de um período fechado.
- **Open Finance:** ecossistema de compartilhamento de dados bancários mediante autorização do cliente.
- **P1 / P2 / P3:** níveis de prioridade de entrega definidos na seção 2.1.
