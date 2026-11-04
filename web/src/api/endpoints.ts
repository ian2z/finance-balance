import type {
  AuthResponse,
  Categoria,
  Conta,
  Distribuicao,
  FiltroTransacoes,
  Investimento,
  MovimentoInvestimento,
  Orcamento,
  Pagina,
  Ref,
  ResumoCarteira,
  ResumoDashboard,
  TipoCategoria,
  TipoInvestimento,
  TipoMovimento,
  TipoConta,
  Transacao,
  TransacaoPayload,
  TransferenciaPayload,
  Usuario,
} from '../types'
import { api } from './client'

const dados = <T>(p: Promise<{ data: T }>): Promise<T> => p.then((r) => r.data)

/** Remove filtros vazios para não enviar parâmetros em branco. */
const limpar = (f: object) => Object.fromEntries(Object.entries(f).filter(([, v]) => v !== undefined && v !== ''))

export const authApi = {
  login: (email: string, senha: string) => dados<AuthResponse>(api.post('/auth/login', { email, senha })),
  registrar: (nome: string, email: string, senha: string) =>
    dados<AuthResponse>(api.post('/auth/register', { nome, email, senha })),
  perfil: () => dados<Usuario>(api.get('/usuarios/me')),
  atualizarPerfil: (nome: string) => dados<Usuario>(api.put('/usuarios/me', { nome })),
  alterarSenha: (senhaAtual: string, novaSenha: string) =>
    api.put('/usuarios/me/senha', { senhaAtual, novaSenha }).then(() => undefined),
}

export interface ContaPayload {
  nome: string
  tipo: TipoConta
  saldoInicial: number
  ativa: boolean
}

export const contasApi = {
  listar: () => dados<Conta[]>(api.get('/contas')),
  criar: (p: ContaPayload) => dados<Conta>(api.post('/contas', p)),
  atualizar: (id: number, p: ContaPayload) => dados<Conta>(api.put(`/contas/${id}`, p)),
  excluir: (id: number) => api.delete(`/contas/${id}`).then(() => undefined),
}

export interface CategoriaPayload {
  nome: string
  tipo: TipoCategoria
  categoriaPaiId?: number | null
}

export const categoriasApi = {
  listar: () => dados<Categoria[]>(api.get('/categorias')),
  criar: (p: CategoriaPayload) => dados<Categoria>(api.post('/categorias', p)),
  atualizar: (id: number, p: CategoriaPayload) => dados<Categoria>(api.put(`/categorias/${id}`, p)),
  excluir: (id: number) => api.delete(`/categorias/${id}`).then(() => undefined),
}

export const formasPagamentoApi = {
  listar: () => dados<Ref[]>(api.get('/formas-pagamento')),
}

export const etiquetasApi = {
  listar: () => dados<Ref[]>(api.get('/etiquetas')),
  criar: (nome: string) => dados<Ref>(api.post('/etiquetas', { nome })),
  atualizar: (id: number, nome: string) => dados<Ref>(api.put(`/etiquetas/${id}`, { nome })),
  excluir: (id: number) => api.delete(`/etiquetas/${id}`).then(() => undefined),
}

export const transacoesApi = {
  listar: (f: FiltroTransacoes) => dados<Pagina<Transacao>>(api.get('/transacoes', { params: limpar(f) })),
  criar: (p: TransacaoPayload) => dados<Transacao>(api.post('/transacoes', p)),
  atualizar: (id: number, p: TransacaoPayload) => dados<Transacao>(api.put(`/transacoes/${id}`, p)),
  excluir: (id: number) => api.delete(`/transacoes/${id}`).then(() => undefined),
  transferir: (p: TransferenciaPayload) => dados<Transacao>(api.post('/transferencias', p)),
  /** Baixa o CSV respeitando os filtros atuais (o token vai no cabeçalho, por isso não é um link direto). */
  async exportarCsv(f: FiltroTransacoes) {
    const { page: _p, size: _s, ...filtros } = f
    const resposta = await api.get<Blob>('/exportacao/transacoes.csv', {
      params: limpar(filtros),
      responseType: 'blob',
    })
    const url = URL.createObjectURL(resposta.data)
    const a = document.createElement('a')
    a.href = url
    a.download = 'transacoes.csv'
    a.click()
    URL.revokeObjectURL(url)
  },
}

export interface OrcamentoPayload {
  categoriaId: number
  mes: string
  valorLimite: number
}

export const orcamentosApi = {
  /** `mes` no formato AAAA-MM; a API espera uma data (primeiro dia do mês). */
  listar: (mes: string) => dados<Orcamento[]>(api.get('/orcamentos', { params: { mes: `${mes}-01` } })),
  criar: (p: OrcamentoPayload) => dados<Orcamento>(api.post('/orcamentos', p)),
  atualizar: (id: number, p: OrcamentoPayload) => dados<Orcamento>(api.put(`/orcamentos/${id}`, p)),
  excluir: (id: number) => api.delete(`/orcamentos/${id}`).then(() => undefined),
}

export const distribuicaoApi = {
  consultar: (mes?: string) => dados<Distribuicao>(api.get('/distribuicao', { params: limpar({ mes }) })),
  definir: (rendaBase: number, itens: { categoriaId: number; percentual: number }[]) =>
    dados<Distribuicao>(api.put('/distribuicao', { rendaBase, itens })),
}

export interface InvestimentoPayload {
  nome: string
  tipo: TipoInvestimento
  dataAplicacao: string
  valorInicial?: number
  valorAtual?: number
}

export const investimentosApi = {
  listar: () => dados<Investimento[]>(api.get('/investimentos')),
  resumo: () => dados<ResumoCarteira>(api.get('/investimentos/resumo')),
  criar: (p: InvestimentoPayload) => dados<Investimento>(api.post('/investimentos', p)),
  atualizar: (id: number, p: InvestimentoPayload) => dados<Investimento>(api.put(`/investimentos/${id}`, p)),
  excluir: (id: number) => api.delete(`/investimentos/${id}`).then(() => undefined),
  movimentos: (id: number) => dados<MovimentoInvestimento[]>(api.get(`/investimentos/${id}/movimentos`)),
  movimentar: (id: number, p: { tipo: TipoMovimento; valor: number; data: string }) =>
    dados<Investimento>(api.post(`/investimentos/${id}/movimentos`, p)),
}

export const dashboardApi = {
  resumo: (de: string, ate: string) => dados<ResumoDashboard>(api.get('/dashboard/resumo', { params: { de, ate } })),
}
