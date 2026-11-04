// Tipos espelhando os DTOs do backend (contrato em /api/v1/swagger-ui.html).

export type TipoCategoria = 'RECEITA' | 'DESPESA'
export type TipoTransacao = 'RECEITA' | 'DESPESA' | 'TRANSFERENCIA'
export type SituacaoTransacao = 'PAGA' | 'PENDENTE'
export type TipoConta = 'CORRENTE' | 'POUPANCA' | 'CARTEIRA' | 'DINHEIRO' | 'CARTEIRA_DIGITAL'
export type TipoInvestimento = 'CDB' | 'TESOURO_DIRETO' | 'FII' | 'ACOES' | 'FUNDOS' | 'POUPANCA' | 'OUTROS'
export type TipoMovimento = 'APORTE' | 'RESGATE' | 'RENDIMENTO'

export interface Ref {
  id: number
  nome: string
}

export interface Usuario {
  id: number
  nome: string
  email: string
}

export interface AuthResponse {
  token: string
  tipo: string
  expiraEm: string
  usuario: Usuario
}

export interface Conta {
  id: number
  nome: string
  tipo: TipoConta
  saldoInicial: number
  saldoAtual: number
  ativa: boolean
}

export interface Categoria {
  id: number
  nome: string
  tipo: TipoCategoria
  categoriaPaiId: number | null
  doSistema: boolean
  semMovimentacoes: boolean
}

export interface Transacao {
  id: number
  descricao: string
  valor: number
  tipo: TipoTransacao
  situacao: SituacaoTransacao
  data: string
  dataVencimento: string | null
  observacao: string | null
  categoria: Ref | null
  conta: Ref
  contaDestino: Ref | null
  formaPagamento: Ref | null
  etiquetas: Ref[]
}

export interface Pagina<T> {
  conteudo: T[]
  pagina: number
  tamanho: number
  totalElementos: number
  totalPaginas: number
}

export interface FiltroTransacoes {
  texto?: string
  categoriaId?: number
  contaId?: number
  tipo?: TipoTransacao
  situacao?: SituacaoTransacao
  etiquetaId?: number
  de?: string
  ate?: string
  page?: number
  size?: number
  sort?: string
}

export interface TransacaoPayload {
  descricao: string
  valor: number
  tipo: 'RECEITA' | 'DESPESA'
  data: string
  dataVencimento?: string | null
  situacao: SituacaoTransacao
  categoriaId: number
  contaId: number
  formaPagamentoId?: number | null
  observacao?: string | null
  etiquetaIds: number[]
}

export interface TransferenciaPayload {
  descricao: string
  valor: number
  data: string
  contaOrigemId: number
  contaDestinoId: number
  observacao?: string | null
}

export type SituacaoOrcamento = 'DENTRO_DO_LIMITE' | 'PROXIMO_DO_LIMITE' | 'ESTOURADO'

export interface Orcamento {
  id: number
  categoria: Ref
  mes: string
  valorLimite: number
  valorUtilizado: number
  valorRestante: number
  percentualConsumido: number
  situacao: SituacaoOrcamento
}

export interface ItemDistribuicao {
  categoria: Ref
  percentual: number
  valorPlanejado: number
  valorRealizado: number
}

export interface Distribuicao {
  rendaBase: number
  totalPercentual: number
  percentualNaoAlocado: number
  itens: ItemDistribuicao[]
}

export interface Investimento {
  id: number
  nome: string
  tipo: TipoInvestimento
  valorAplicado: number
  valorAtual: number
  rendimento: number
  rentabilidadePercentual: number
  dataAplicacao: string
}

export interface MovimentoInvestimento {
  id: number
  tipo: TipoMovimento
  valor: number
  data: string
}

export interface ResumoCarteira {
  totalAplicado: number
  totalAtual: number
  rendimento: number
  rentabilidadePercentual: number
  distribuicao: { tipo: TipoInvestimento; valorAtual: number; percentual: number }[]
}

export interface ResumoDashboard {
  de: string
  ate: string
  saldoTotal: number
  receitas: number
  despesas: number
  resultado: number
  receitasPendentes: number
  despesasPendentes: number
  despesasPorCategoria: { categoriaId: number; categoria: string; valor: number; percentual: number }[]
  evolucao: { mes: string; receitas: number; despesas: number }[]
  maioresGastos: { id: number; descricao: string; valor: number; data: string; categoria: string | null }[]
  orcamento: { totalLimite: number; totalUtilizado: number; percentual: number }
}

/** Formato único de erro da API. */
export interface ErroApi {
  mensagem: string
  campos?: Record<string, string>
}
