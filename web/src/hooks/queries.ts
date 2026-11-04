import { keepPreviousData, useQuery } from '@tanstack/react-query'
import {
  categoriasApi,
  contasApi,
  dashboardApi,
  distribuicaoApi,
  etiquetasApi,
  formasPagamentoApi,
  investimentosApi,
  orcamentosApi,
  transacoesApi,
} from '../api/endpoints'
import type { FiltroTransacoes } from '../types'

/** Chaves de cache; invalidar um prefixo atualiza todas as consultas relacionadas. */
export const chaves = {
  contas: ['contas'] as const,
  categorias: ['categorias'] as const,
  formas: ['formas-pagamento'] as const,
  etiquetas: ['etiquetas'] as const,
  transacoes: ['transacoes'] as const,
  dashboard: ['dashboard'] as const,
  orcamentos: ['orcamentos'] as const,
  distribuicao: ['distribuicao'] as const,
  investimentos: ['investimentos'] as const,
}

export const useContas = () => useQuery({ queryKey: chaves.contas, queryFn: contasApi.listar })
export const useCategorias = () => useQuery({ queryKey: chaves.categorias, queryFn: categoriasApi.listar })
export const useFormasPagamento = () =>
  useQuery({ queryKey: chaves.formas, queryFn: formasPagamentoApi.listar, staleTime: Infinity })
export const useEtiquetas = () => useQuery({ queryKey: chaves.etiquetas, queryFn: etiquetasApi.listar })

export const useTransacoes = (filtro: FiltroTransacoes) =>
  useQuery({
    queryKey: [...chaves.transacoes, filtro],
    queryFn: () => transacoesApi.listar(filtro),
    placeholderData: keepPreviousData,
  })

export const useDashboard = (de: string, ate: string) =>
  useQuery({ queryKey: [...chaves.dashboard, de, ate], queryFn: () => dashboardApi.resumo(de, ate) })

export const useOrcamentos = (mes: string) =>
  useQuery({ queryKey: [...chaves.orcamentos, mes], queryFn: () => orcamentosApi.listar(mes) })

export const useDistribuicao = () => useQuery({ queryKey: chaves.distribuicao, queryFn: () => distribuicaoApi.consultar() })

export const useInvestimentos = () => useQuery({ queryKey: chaves.investimentos, queryFn: investimentosApi.listar })
export const useResumoCarteira = () =>
  useQuery({ queryKey: [...chaves.investimentos, 'resumo'], queryFn: investimentosApi.resumo })
export const useMovimentos = (id: number | null) =>
  useQuery({
    queryKey: [...chaves.investimentos, 'movimentos', id],
    queryFn: () => investimentosApi.movimentos(id!),
    enabled: id !== null,
  })
