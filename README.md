# Finance Balance

Gestor de finanças pessoais. Backend único (Spring Boot) consumido pelo cliente web (React). O app mobile (Compose) é secundário e ainda não foi iniciado.

```
finance-balance/
├── backend/   # Spring Boot 4 + PostgreSQL + Flyway + JWT
├── web/       # React + TypeScript + Vite + Tailwind
├── mobile/    # (futuro) Android / Jetpack Compose
└── docs/      # escopo e módulos, design system
```

## Escopo implementado (P1 — Backend + Web)

Autenticação, contas, categorias/subcategorias, transações (receita, despesa, transferência), formas de pagamento,
filtros/busca/paginação, etiquetas, dashboard, orçamentos, distribuição percentual da renda, investimentos (manual) e exportação CSV.
Detalhes em [`docs/escopo-e-modulos.md`](docs/escopo-e-modulos.md); identidade visual em [`docs/design-system.md`](docs/design-system.md).

## Como rodar

Requisitos: Java 21, Node 20+, Docker.

```bash
# 1. Banco local (PostgreSQL na porta 5433)
docker compose up -d db

# 2. Backend (http://localhost:8080/api/v1, Swagger em /api/v1/swagger-ui.html)
cd backend
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run

# 3. Web (http://localhost:5173)
cd web
cp .env.example .env   # opcional
npm install
npm run dev
```

O profile `dev` traz um `JWT_SECRET` só para desenvolvimento. Fora dele, defina as variáveis de [`.env.example`](.env.example)
(`JWT_SECRET` é obrigatório e precisa de 32+ caracteres).

## Testes

```bash
cd backend && ./mvnw test      # integração com PostgreSQL via Testcontainers (requer Docker)
cd web && npm run build        # typecheck + build
```

## Decisões

- API sob `/api/v1`; erros sempre em `{ "mensagem": "...", "campos": { ... } }`.
- Valores monetários em `NUMERIC(15,2)` / `BigDecimal`. Transferências não entram em receitas/despesas.
- Dashboard soma só movimentações **pagas**; pendentes aparecem à parte. Orçamentos contam pagas + pendentes.
- Todo dado pertence a um usuário; acesso a registro alheio responde 404.
- O token JWT fica em `localStorage` (simples para o escopo acadêmico; em produção, avalie cookie httpOnly).
