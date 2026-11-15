# Finance Balance — Mobile (Android / Jetpack Compose)

Aplicativo Android nativo desenvolvido em **Kotlin + Jetpack Compose** integrado ao backend **Spring Boot** via **Retrofit** e ao banco em nuvem **Google Cloud Firestore**.

## Atendimento aos Requisitos da Disciplina

1. **Interface intuitiva e responsiva com Jetpack Compose e Material Design 3:**
   - Tema escuro *Modern Financial Precision* em [`ui/theme/`](app/src/main/java/br/com/financebalance/mobile/ui/theme/Theme.kt).
   - Adaptação automática a diferentes tamanhos de tela usando `WindowSizeClass` em [`MainActivity.kt`](app/src/main/java/br/com/financebalance/mobile/MainActivity.kt) e [`FinanceBalanceApp.kt`](app/src/main/java/br/com/financebalance/mobile/ui/navigation/FinanceBalanceApp.kt):
     - Telas compactas (celulares em retrato): `NavigationBar` inferior e cards empilhados.
     - Telas médias/expandidas (tablets ou modo paisagem): `NavigationRail` lateral e grids em 2 colunas.
2. **Busca com filtro e navegação entre telas:**
   - Navegação estruturada com `Navigation Compose` entre **Resumo (Dashboard)**, **Transações**, **Metas Cloud (Firestore)** e **Perfil/Contas**.
   - Busca textual e filtros combinados tanto nas **Transações** (por descrição, tipo, situação e categoria via API) quanto nas **Metas Financeiras** (por texto, status e prioridade no Firestore).
3. **Banco de dados nas nuvens (Cloud Firestore) com CRUD completo:**
   - Implementado no Módulo de **Metas Financeiras** em [`MetasFirestoreRepository.kt`](app/src/main/java/br/com/financebalance/mobile/data/firestore/MetasFirestoreRepository.kt) e [`MetasFirestoreScreen.kt`](app/src/main/java/br/com/financebalance/mobile/ui/screens/MetasFirestoreScreen.kt).
   - Operações de **Create** (`criar`), **Read** (`listarPorUsuario`), **Update** (`atualizar` e `registrarAporte`) e **Delete** (`excluir`) na coleção `metas_financeiras`, vinculadas ao `usuarioId` autenticado.
4. **Consumo de API REST com Retrofit:**
   - Implementado em [`FinanceApi.kt`](app/src/main/java/br/com/financebalance/mobile/data/remote/FinanceApi.kt) e [`ApiClient.kt`](app/src/main/java/br/com/financebalance/mobile/data/remote/ApiClient.kt).
   - Consome o mesmo backend Spring Boot (`/api/v1`) usado pela aplicação Web em Vite, injetando o token JWT via `Interceptor` do OkHttp.

## Como Executar no Android Studio

1. Certifique-se de que o banco PostgreSQL e o backend Spring Boot estejam rodando:
   ```bash
   docker compose up -d db
   cd backend && SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
   ```
2. No **Firebase Console**, certifique-se de que o **Cloud Firestore** esteja criado e com regras permitindo leitura/escrita para testes:
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if true;
       }
     }
   }
   ```
3. Abra a pasta `mobile/` diretamente no **Android Studio** (File > Open > selecione `finance-balance/mobile`).
4. Aguarde o *Gradle Sync* finalizar e execute o app (`Run 'app'`) em um Emulador Android.
   - No emulador, a URL padrão `http://10.0.2.2:8080/api/v1/` já aponta automaticamente para o `localhost:8080` da sua máquina.
   - Em dispositivo físico via Wi-Fi, toque em **"Configurar URL da API"** na tela de login e informe `http://<IP_DA_SUA_MAQUINA>:8080/api/v1/`.
   - Use o botão **"Ana (Demo)"** na tela de login para preencher rapidamente `ana@demo.com` / `Demo@1234`.
