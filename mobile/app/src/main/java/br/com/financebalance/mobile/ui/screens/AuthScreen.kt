package br.com.financebalance.mobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.financebalance.mobile.ui.components.*
import br.com.financebalance.mobile.ui.theme.*

@Composable
fun AuthScreen(
    carregando: Boolean,
    erro: String?,
    apiUrlAtual: String,
    onLogin: (email: String, senha: String) -> Unit,
    onRegistrar: (nome: String, email: String, senha: String) -> Unit,
    onSalvarApiUrl: (String) -> Unit
) {
    var modoCadastro by remember { mutableStateOf(false) }
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("ana@demo.com") }
    var senha by remember { mutableStateOf("Demo@1234") }
    var mostrarSenha by remember { mutableStateOf(false) }
    var mostrarConfigApi by remember { mutableStateOf(false) }
    var urlInput by remember(apiUrlAtual) { mutableStateOf(apiUrlAtual) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasDark)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Marca idêntica ao AuthShell do Web
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(OrangePrimary, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "F",
                        color = OnOrangePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = "Finance Balance",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
            }

            // Card Principal de Autenticação
            FinanceCard(
                contentPadding = PaddingValues(24.dp)
            ) {
                Text(
                    text = if (modoCadastro) "Criar conta" else "Entrar",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (modoCadastro)
                        "Preencha seus dados para começar a organizar suas finanças."
                    else
                        "Acesse sua conta para ver suas finanças.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(Modifier.height(20.dp))

                if (!erro.isNullOrBlank()) {
                    Surface(
                        color = SurfaceContainerHigh,
                        border = BorderStroke(1.dp, BorderStrong),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = erro,
                            color = ErrorCoral,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (modoCadastro) {
                        CampoFormulario(
                            rotulo = "NOME",
                            valor = nome,
                            onValorChange = { nome = it },
                            placeholder = "Seu nome completo"
                        )
                    }

                    CampoFormulario(
                        rotulo = "E-MAIL",
                        valor = email,
                        onValorChange = { email = it },
                        placeholder = "voce@exemplo.com",
                        teclado = KeyboardType.Email
                    )

                    CampoFormulario(
                        rotulo = "SENHA",
                        valor = senha,
                        onValorChange = { senha = it },
                        placeholder = "Mínimo de 8 caracteres",
                        teclado = KeyboardType.Password,
                        visualTransformation = if (mostrarSenha) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                                Icon(
                                    imageVector = if (mostrarSenha) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (mostrarSenha) "Ocultar senha" else "Mostrar senha",
                                    tint = TextSecondary
                                )
                            }
                        }
                    )

                    BotaoPrimario(
                        texto = if (modoCadastro) "Criar conta" else "Entrar",
                        carregando = carregando,
                        habilitado = email.isNotBlank() && senha.length >= 6 && (!modoCadastro || nome.isNotBlank()),
                        onClick = {
                            if (modoCadastro) {
                                onRegistrar(nome, email, senha)
                            } else {
                                onLogin(email, senha)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Contas de demonstração rápidas
                if (!modoCadastro) {
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(Modifier.height(14.dp))
                    FieldLabel(texto = "ACESSO DE DEMONSTRAÇÃO")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BotaoSecundario(
                            texto = "Ana Souza",
                            onClick = {
                                email = "ana@demo.com"
                                senha = "Demo@1234"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        BotaoSecundario(
                            texto = "Bruno Lima",
                            onClick = {
                                email = "bruno@demo.com"
                                senha = "Demo@1234"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Rodapé de alternância Entrar / Cadastrar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (modoCadastro) "Já possui uma conta?" else "Ainda não tem conta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                BotaoFantasma(
                    texto = if (modoCadastro) "Entrar" else "Cadastre-se",
                    onClick = { modoCadastro = !modoCadastro }
                )
            }

            // Ajuste discreto do servidor para emulador ou dispositivo físico
            BotaoFantasma(
                texto = if (mostrarConfigApi) "Ocultar endereço do servidor" else "Servidor: $apiUrlAtual",
                corTexto = TextMuted,
                onClick = { mostrarConfigApi = !mostrarConfigApi }
            )

            AnimatedVisibility(visible = mostrarConfigApi) {
                FinanceCard(
                    modifier = Modifier.padding(top = 8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    FieldLabel(texto = "ATALHOS RÁPIDOS DE SERVIDOR")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BotaoSecundario(
                            texto = "Render (Nuvem)",
                            onClick = {
                                urlInput = "https://finance-balance-api.onrender.com/api/v1/"
                                onSalvarApiUrl("https://finance-balance-api.onrender.com/api/v1/")
                            },
                            modifier = Modifier.weight(1f)
                        )
                        BotaoSecundario(
                            texto = "Local (Emulador)",
                            onClick = {
                                urlInput = "http://10.0.2.2:8080/api/v1/"
                                onSalvarApiUrl("http://10.0.2.2:8080/api/v1/")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    CampoFormulario(
                        rotulo = "ENDEREÇO DA API PERSONALIZADO",
                        valor = urlInput,
                        onValorChange = { urlInput = it },
                        placeholder = "https://finance-balance-api.onrender.com/api/v1/"
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Alterne rapidamente entre Render em produção ou Localhost no emulador.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    BotaoPrimario(
                        texto = "Salvar endereço",
                        onClick = {
                            onSalvarApiUrl(urlInput)
                            mostrarConfigApi = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
