package br.com.financebalance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Fluxos ponta a ponta contra PostgreSQL real (Testcontainers). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "app.jwt.secret=segredo-de-teste-com-mais-de-32-caracteres-ok")
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    // ---------- helpers ----------

    private String registrar() throws Exception {
        String email = "u" + UUID.randomUUID() + "@teste.com";
        String body = mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Teste\",\"email\":\"" + email + "\",\"senha\":\"senha12345\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private ResultActions chamar(String metodo, String url, String token, String json) throws Exception {
        var req = switch (metodo) {
            case "POST" -> post(url);
            case "PUT" -> put(url);
            case "DELETE" -> delete(url);
            default -> get(url);
        };
        req.header("Authorization", "Bearer " + token);
        if (json != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mvc.perform(req);
    }

    private long criar(String url, String token, String json) throws Exception {
        String body = chamar("POST", url, token, json).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long conta(String token, String nome, String saldo) throws Exception {
        return criar("/contas", token, "{\"nome\":\"" + nome + "\",\"tipo\":\"CORRENTE\",\"saldoInicial\":" + saldo + "}");
    }

    private long categoriaPadrao(String token, String nome) throws Exception {
        String body = chamar("GET", "/categorias", token, null).andReturn().getResponse().getContentAsString();
        java.util.List<Number> ids = JsonPath.read(body, "$[?(@.nome=='" + nome + "' && @.categoriaPaiId==null)].id");
        return ids.get(0).longValue();
    }

    private String transacao(String tipo, String valor, String data, long categoria, long conta, String descricao) {
        return "{\"descricao\":\"" + descricao + "\",\"valor\":" + valor + ",\"tipo\":\"" + tipo + "\",\"data\":\"" + data
                + "\",\"categoriaId\":" + categoria + ",\"contaId\":" + conta + "}";
    }

    // ---------- autenticacao ----------

    @Test
    void rotasProtegidasExigemTokenEPadraoDeErro() throws Exception {
        mvc.perform(get("/transacoes")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").exists());
        mvc.perform(get("/contas").header("Authorization", "Bearer invalido")).andExpect(status().isUnauthorized());
    }

    @Test
    void cadastroLoginEEmailDuplicado() throws Exception {
        String email = "dup" + UUID.randomUUID() + "@teste.com";
        String json = "{\"nome\":\"Ana\",\"email\":\"" + email + "\",\"senha\":\"senha12345\"}";
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.senha").doesNotExist());
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.replace(email, email.toUpperCase())))
                .andExpect(status().isConflict());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"senha12345\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tipo").value("Bearer"));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"errada123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cadastroValidaCampos() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"x\",\"senha\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    // ---------- isolamento entre usuarios ----------

    @Test
    void usuarioNaoAcessaDadosDeOutro() throws Exception {
        String a = registrar();
        String b = registrar();
        long contaA = conta(a, "Conta A", "100");
        long cat = categoriaPadrao(a, "Alimentação");
        long transA = criar("/transacoes", a, transacao("DESPESA", "10", "2026-10-05", cat, contaA, "Lanche"));

        chamar("GET", "/contas/" + contaA, b, null).andExpect(status().isNotFound());
        chamar("GET", "/transacoes/" + transA, b, null).andExpect(status().isNotFound());
        chamar("DELETE", "/transacoes/" + transA, b, null).andExpect(status().isNotFound());
        chamar("PUT", "/contas/" + contaA, b, "{\"nome\":\"x\",\"tipo\":\"CORRENTE\",\"saldoInicial\":1}")
                .andExpect(status().isNotFound());
        // B nao pode lancar na conta de A
        chamar("POST", "/transacoes", b, transacao("DESPESA", "5", "2026-10-05", cat, contaA, "Invasao"))
                .andExpect(status().isBadRequest());
        chamar("GET", "/transacoes", b, null).andExpect(jsonPath("$.totalElementos").value(0));
    }

    // ---------- contas, saldo e transferencias ----------

    @Test
    void saldoConsideraPagasPendentesETransferencias() throws Exception {
        String t = registrar();
        long c1 = conta(t, "Principal", "1000.00");
        long c2 = conta(t, "Reserva", "0");
        long salario = categoriaPadrao(t, "Salário");
        long mercado = categoriaPadrao(t, "Alimentação");

        criar("/transacoes", t, transacao("RECEITA", "500.00", "2026-10-01", salario, c1, "Salário"));
        criar("/transacoes", t, transacao("DESPESA", "200.00", "2026-10-02", mercado, c1, "Mercado"));
        // pendente nao entra no saldo
        criar("/transacoes", t, transacao("DESPESA", "300.00", "2026-10-03", mercado, c1, "Boleto")
                .replace("}", ",\"situacao\":\"PENDENTE\"}"));
        long transf = criar("/transferencias", t, "{\"descricao\":\"Reserva\",\"valor\":400,\"data\":\"2026-10-04\","
                + "\"contaOrigemId\":" + c1 + ",\"contaDestinoId\":" + c2 + "}");

        chamar("GET", "/contas/" + c1, t, null).andExpect(jsonPath("$.saldoAtual").value(900.00));
        chamar("GET", "/contas/" + c2, t, null).andExpect(jsonPath("$.saldoAtual").value(400.00));

        // transferencia nao e receita nem despesa no dashboard
        chamar("GET", "/dashboard/resumo?de=2026-10-01&ate=2026-10-31", t, null)
                .andExpect(jsonPath("$.receitas").value(500.00))
                .andExpect(jsonPath("$.despesas").value(200.00))
                .andExpect(jsonPath("$.resultado").value(300.00))
                .andExpect(jsonPath("$.despesasPendentes").value(300.00))
                .andExpect(jsonPath("$.saldoTotal").value(1300.00));

        chamar("PUT", "/transacoes/" + transf, t, transacao("DESPESA", "1", "2026-10-04", mercado, c1, "x"))
                .andExpect(status().isBadRequest());
        chamar("DELETE", "/contas/" + c1, t, null).andExpect(status().isConflict());
        chamar("DELETE", "/transacoes/" + transf, t, null).andExpect(status().isNoContent());
        chamar("GET", "/contas/" + c2, t, null).andExpect(jsonPath("$.saldoAtual").value(0.00));
    }

    @Test
    void transferenciaParaMesmaContaERejeitada() throws Exception {
        String t = registrar();
        long c = conta(t, "Unica", "10");
        chamar("POST", "/transferencias", t, "{\"descricao\":\"x\",\"valor\":1,\"data\":\"2026-10-04\","
                + "\"contaOrigemId\":" + c + ",\"contaDestinoId\":" + c + "}").andExpect(status().isBadRequest());
    }

    // ---------- transacoes: validacao e filtros ----------

    @Test
    void validaValorTipoDaCategoriaEPadraoDeErro() throws Exception {
        String t = registrar();
        long c = conta(t, "Conta", "0");
        long salario = categoriaPadrao(t, "Salário");
        long mercado = categoriaPadrao(t, "Alimentação");
        chamar("POST", "/transacoes", t, transacao("DESPESA", "0", "2026-10-01", mercado, c, "Zero"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.valor").exists());
        chamar("POST", "/transacoes", t, transacao("DESPESA", "10", "2026-10-01", salario, c, "Tipo errado"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").exists());
        chamar("POST", "/transacoes", t, transacao("TRANSFERENCIA", "10", "2026-10-01", mercado, c, "Via errada"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filtrosPaginacaoOrdenacaoEEtiquetas() throws Exception {
        String t = registrar();
        long c = conta(t, "Conta", "0");
        long mercado = categoriaPadrao(t, "Alimentação");
        long transporte = categoriaPadrao(t, "Transporte");
        long etiqueta = criar("/etiquetas", t, "{\"nome\":\"Viagem\"}");

        criar("/transacoes", t, transacao("DESPESA", "50", "2026-10-02", mercado, c, "Mercado 100%_ok"));
        criar("/transacoes", t, transacao("DESPESA", "30", "2026-10-15", transporte, c, "Ônibus"));
        criar("/transacoes", t, transacao("DESPESA", "70", "2026-11-02", mercado, c, "Restaurante")
                .replace("}", ",\"etiquetaIds\":[" + etiqueta + "]}"));

        chamar("GET", "/transacoes?texto=mercado", t, null).andExpect(jsonPath("$.totalElementos").value(1));
        // % e _ sao tratados como texto literal, nao como curinga
        mvc.perform(get("/transacoes").param("texto", "%").header("Authorization", "Bearer " + t))
                .andExpect(jsonPath("$.totalElementos").value(1));
        chamar("GET", "/transacoes?categoriaId=" + mercado + "&de=2026-10-01&ate=2026-10-31", t, null)
                .andExpect(jsonPath("$.totalElementos").value(1));
        chamar("GET", "/transacoes?etiquetaId=" + etiqueta, t, null)
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].etiquetas[0].nome").value("Viagem"));
        chamar("GET", "/transacoes?tipo=RECEITA", t, null).andExpect(jsonPath("$.totalElementos").value(0));
        chamar("GET", "/transacoes?sort=valor,desc&size=2", t, null)
                .andExpect(jsonPath("$.conteudo[0].valor").value(70.0))
                .andExpect(jsonPath("$.conteudo.length()").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(2));
        chamar("GET", "/transacoes?sort=usuario.senhaHash,asc", t, null).andExpect(status().isBadRequest());
        chamar("GET", "/transacoes?de=2026-12-01&ate=2026-01-01", t, null).andExpect(status().isBadRequest());
        chamar("GET", "/transacoes?tipo=INVALIDO", t, null).andExpect(status().isBadRequest());
    }

    // ---------- categorias ----------

    @Test
    void categoriasPadraoSaoSomenteLeituraESubcategoriasTemUmNivel() throws Exception {
        String t = registrar();
        long padrao = categoriaPadrao(t, "Alimentação");
        chamar("PUT", "/categorias/" + padrao, t, "{\"nome\":\"x\",\"tipo\":\"DESPESA\"}").andExpect(status().isForbidden());
        chamar("DELETE", "/categorias/" + padrao, t, null).andExpect(status().isForbidden());

        long pet = criar("/categorias", t, "{\"nome\":\"Pet\",\"tipo\":\"DESPESA\"}");
        long racao = criar("/categorias", t, "{\"nome\":\"Ração\",\"tipo\":\"DESPESA\",\"categoriaPaiId\":" + pet + "}");
        chamar("POST", "/categorias", t, "{\"nome\":\"Neto\",\"tipo\":\"DESPESA\",\"categoriaPaiId\":" + racao + "}")
                .andExpect(status().isBadRequest());
        chamar("POST", "/categorias", t, "{\"nome\":\"Pet\",\"tipo\":\"DESPESA\"}").andExpect(status().isConflict());
        chamar("DELETE", "/categorias/" + pet, t, null).andExpect(status().isConflict());
        chamar("DELETE", "/categorias/" + racao, t, null).andExpect(status().isNoContent());
        chamar("DELETE", "/categorias/" + pet, t, null).andExpect(status().isNoContent());
    }

    // ---------- orcamentos e distribuicao ----------

    @Test
    void orcamentoCalculaConsumoComSubcategorias() throws Exception {
        String t = registrar();
        long c = conta(t, "Conta", "0");
        long alim = categoriaPadrao(t, "Alimentação");
        String lista = chamar("GET", "/categorias", t, null).andReturn().getResponse().getContentAsString();
        long mercado = ((Number) ((java.util.List<?>) JsonPath.read(lista,
                "$[?(@.nome=='Mercado' && @.categoriaPaiId==" + alim + ")].id")).get(0)).longValue();

        criar("/transacoes", t, transacao("DESPESA", "350", "2026-10-10", mercado, c, "Compras"));
        criar("/orcamentos", t, "{\"categoriaId\":" + alim + ",\"mes\":\"2026-10-17\",\"valorLimite\":500}");

        chamar("GET", "/orcamentos?mes=2026-10-01", t, null)
                .andExpect(jsonPath("$[0].mes").value("2026-10-01"))
                .andExpect(jsonPath("$[0].valorUtilizado").value(350.0))
                .andExpect(jsonPath("$[0].valorRestante").value(150.0))
                .andExpect(jsonPath("$[0].percentualConsumido").value(70.0))
                .andExpect(jsonPath("$[0].situacao").value("DENTRO_DO_LIMITE"));
        chamar("POST", "/orcamentos", t, "{\"categoriaId\":" + alim + ",\"mes\":\"2026-10-01\",\"valorLimite\":1}")
                .andExpect(status().isConflict());
        chamar("POST", "/orcamentos", t, "{\"categoriaId\":" + categoriaPadrao(t, "Salário")
                + ",\"mes\":\"2026-10-01\",\"valorLimite\":1}").andExpect(status().isBadRequest());
    }

    @Test
    void distribuicaoValidaSomaDosPercentuais() throws Exception {
        String t = registrar();
        long alim = categoriaPadrao(t, "Alimentação");
        long transp = categoriaPadrao(t, "Transporte");
        chamar("PUT", "/distribuicao", t, "{\"rendaBase\":2000,\"itens\":[{\"categoriaId\":" + alim
                + ",\"percentual\":70},{\"categoriaId\":" + transp + ",\"percentual\":40}]}")
                .andExpect(status().isBadRequest());
        chamar("PUT", "/distribuicao", t, "{\"rendaBase\":2000,\"itens\":[{\"categoriaId\":" + alim
                + ",\"percentual\":20},{\"categoriaId\":" + transp + ",\"percentual\":10}]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPercentual").value(30.0))
                .andExpect(jsonPath("$.percentualNaoAlocado").value(70.0))
                .andExpect(jsonPath("$.itens[0].valorPlanejado").value(400.0));
        // substituicao integral
        chamar("PUT", "/distribuicao", t, "{\"rendaBase\":1000,\"itens\":[{\"categoriaId\":" + alim
                + ",\"percentual\":50}]}")
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].valorPlanejado").value(500.0));
        chamar("GET", "/distribuicao", t, null).andExpect(jsonPath("$.rendaBase").value(1000.0));
    }

    // ---------- investimentos ----------

    @Test
    void investimentosAportesResgatesERendimentos() throws Exception {
        String t = registrar();
        long inv = criar("/investimentos", t, "{\"nome\":\"CDB Banco\",\"tipo\":\"CDB\",\"dataAplicacao\":\"2026-10-01\","
                + "\"valorInicial\":1000}");
        chamar("POST", "/investimentos/" + inv + "/movimentos", t, "{\"tipo\":\"RENDIMENTO\",\"valor\":100,\"data\":\"2026-10-20\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorAplicado").value(1000.0))
                .andExpect(jsonPath("$.valorAtual").value(1100.0))
                .andExpect(jsonPath("$.rentabilidadePercentual").value(10.0));
        // resgate de 550 (metade do valor atual) reduz o aplicado pela metade
        chamar("POST", "/investimentos/" + inv + "/movimentos", t, "{\"tipo\":\"RESGATE\",\"valor\":550,\"data\":\"2026-10-21\"}")
                .andExpect(jsonPath("$.valorAplicado").value(500.0))
                .andExpect(jsonPath("$.valorAtual").value(550.0));
        chamar("POST", "/investimentos/" + inv + "/movimentos", t, "{\"tipo\":\"RESGATE\",\"valor\":9999,\"data\":\"2026-10-21\"}")
                .andExpect(status().isBadRequest());
        chamar("GET", "/investimentos/" + inv + "/movimentos", t, null).andExpect(jsonPath("$.length()").value(3));
        chamar("GET", "/investimentos/resumo", t, null)
                .andExpect(jsonPath("$.totalAtual").value(550.0))
                .andExpect(jsonPath("$.distribuicao[0].tipo").value("CDB"))
                .andExpect(jsonPath("$.distribuicao[0].percentual").value(100.0));
    }

    // ---------- exportacao ----------

    @Test
    void exportaCsvNeutralizandoFormulas() throws Exception {
        String t = registrar();
        long c = conta(t, "Conta", "0");
        long cat = categoriaPadrao(t, "Alimentação");
        criar("/transacoes", t, transacao("DESPESA", "12.50", "2026-10-02", cat, c, "=HYPERLINK(1)"));
        String csv = chamar("GET", "/exportacao/transacoes.csv", t, null).andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("transacoes.csv")))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("'=HYPERLINK(1)"), csv);
        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("12.50"), csv);
    }

    // ---------- usuario ----------

    @Test
    void alteraSenhaExigindoSenhaAtual() throws Exception {
        String t = registrar();
        chamar("PUT", "/usuarios/me/senha", t, "{\"senhaAtual\":\"errada\",\"novaSenha\":\"novasenha123\"}")
                .andExpect(status().isBadRequest());
        chamar("PUT", "/usuarios/me/senha", t, "{\"senhaAtual\":\"senha12345\",\"novaSenha\":\"novasenha123\"}")
                .andExpect(status().isNoContent());
    }
}
