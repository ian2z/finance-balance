package br.com.financebalance.mobile.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.financebalance.mobile.ui.screens.*
import br.com.financebalance.mobile.ui.theme.*
import br.com.financebalance.mobile.viewmodel.AppViewModel

enum class DestinoPrincipal(
    val rota: String,
    val titulo: String,
    val icone: ImageVector
) {
    DASHBOARD("dashboard", "Dashboard", Icons.Default.Dashboard),
    TRANSACOES("transacoes", "Transações", Icons.Default.ReceiptLong),
    METAS_FIRESTORE("metas_firestore", "Metas", Icons.Default.Flag),
    PERFIL("perfil", "Conta", Icons.Default.AccountBalanceWallet)
}

@Composable
fun FinanceBalanceApp(
    windowSizeClass: WindowSizeClass,
    appViewModel: AppViewModel = viewModel()
) {
    val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensagemFeedback, uiState.erroOperacao) {
        val msg = uiState.mensagemFeedback ?: uiState.erroOperacao
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            appViewModel.limparMensagens()
        }
    }

    if (!uiState.sessaoCarregada) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CanvasDark),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OrangePrimary)
        }
        return
    }

    if (!uiState.sessao.autenticado) {
        AuthScreen(
            carregando = uiState.carregandoAuth,
            erro = uiState.erroAuth,
            apiUrlAtual = uiState.sessao.apiUrl,
            onLogin = appViewModel::login,
            onRegistrar = appViewModel::registrar,
            onSalvarApiUrl = appViewModel::atualizarUrlApi
        )
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = navBackStackEntry?.destination?.route ?: DestinoPrincipal.DASHBOARD.rota
    val layoutExpandido = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    Scaffold(
        containerColor = CanvasDark,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceContainerHigh,
                    contentColor = TextPrimary,
                    actionColor = OrangePrimary
                )
            }
        },
        bottomBar = {
            if (!layoutExpandido) {
                Column {
                    // Hairline estrutural de 1dp separando a barra inferior
                    HorizontalDivider(thickness = 1.dp, color = BorderSubtle)
                    NavigationBar(
                        containerColor = SurfaceContainerLow,
                        tonalElevation = 0.dp
                    ) {
                        DestinoPrincipal.entries.forEach { destino ->
                            val selecionado = rotaAtual == destino.rota
                            NavigationBarItem(
                                selected = selecionado,
                                onClick = { navegarPara(navController, destino.rota) },
                                icon = { Icon(destino.icone, contentDescription = destino.titulo) },
                                label = {
                                    Text(
                                        text = destino.titulo,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = OnOrangePrimary,
                                    selectedTextColor = OrangePrimary,
                                    indicatorColor = OrangePrimary,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (layoutExpandido) {
                NavigationRail(
                    containerColor = SurfaceContainerLow
                ) {
                    Spacer(Modifier.height(16.dp))
                    DestinoPrincipal.entries.forEach { destino ->
                        val selecionado = rotaAtual == destino.rota
                        NavigationRailItem(
                            selected = selecionado,
                            onClick = { navegarPara(navController, destino.rota) },
                            icon = { Icon(destino.icone, contentDescription = destino.titulo) },
                            label = {
                                Text(
                                    text = destino.titulo,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = OnOrangePrimary,
                                selectedTextColor = OrangePrimary,
                                indicatorColor = OrangePrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
                VerticalDivider(thickness = 1.dp, color = BorderSubtle)
            }

            NavHost(
                navController = navController,
                startDestination = DestinoPrincipal.DASHBOARD.rota,
                modifier = Modifier.weight(1f)
            ) {
                composable(DestinoPrincipal.DASHBOARD.rota) {
                    DashboardScreen(
                        mesSelecionado = uiState.mesDashboard,
                        carregando = uiState.carregandoDashboard,
                        resumo = uiState.dashboard,
                        metasFirestore = uiState.metas,
                        layoutExpandido = layoutExpandido,
                        onMesAnterior = { appViewModel.alterarMesDashboard(-1) },
                        onProximoMes = { appViewModel.alterarMesDashboard(1) },
                        onIrParaTransacoes = {
                            navegarPara(navController, DestinoPrincipal.TRANSACOES.rota)
                        },
                        onIrParaMetas = {
                            navegarPara(navController, DestinoPrincipal.METAS_FIRESTORE.rota)
                        }
                    )
                }

                composable(DestinoPrincipal.TRANSACOES.rota) {
                    TransacoesScreen(
                        carregando = uiState.carregandoTransacoes,
                        transacoes = uiState.transacoes,
                        totalElementos = uiState.totalTransacoes,
                        filtro = uiState.filtroTransacoes,
                        contas = uiState.contas,
                        categorias = uiState.categorias,
                        formasPagamento = uiState.formasPagamento,
                        onAtualizarFiltro = appViewModel::atualizarFiltroTransacoes,
                        onRecarregar = appViewModel::carregarTransacoes,
                        onSalvarTransacao = appViewModel::salvarTransacao,
                        onSalvarTransferencia = appViewModel::salvarTransferencia,
                        onExcluirTransacao = appViewModel::excluirTransacao
                    )
                }

                composable(DestinoPrincipal.METAS_FIRESTORE.rota) {
                    MetasFirestoreScreen(
                        carregando = uiState.carregandoMetas,
                        metas = uiState.metasFiltradas,
                        filtro = uiState.filtroMetas,
                        onAtualizarFiltro = appViewModel::atualizarFiltroMetas,
                        onRecarregar = appViewModel::carregarMetasFirestore,
                        onSalvarMeta = appViewModel::salvarMetaFirestore,
                        onAdicionarAporte = appViewModel::adicionarAporteMeta,
                        onExcluirMeta = appViewModel::excluirMetaFirestore
                    )
                }

                composable(DestinoPrincipal.PERFIL.rota) {
                    PerfilContasScreen(
                        sessao = uiState.sessao,
                        contas = uiState.contas,
                        onCriarConta = appViewModel::criarContaRapida,
                        onLogout = appViewModel::logout
                    )
                }
            }
        }
    }
}

private fun navegarPara(navController: NavHostController, rota: String) {
    navController.navigate(rota) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
