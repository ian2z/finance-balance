import axios from 'axios'
import type { ErroApi } from '../types'

/** Extrai a mensagem amigável do formato único de erro da API. */
export function mensagemDeErro(erro: unknown): string {
  if (axios.isAxiosError(erro)) {
    const corpo = erro.response?.data as Partial<ErroApi> | undefined
    if (corpo?.campos && Object.keys(corpo.campos).length > 0) {
      const detalhes = Object.entries(corpo.campos)
        .map(([campo, msg]) => `${campo}: ${msg}`)
        .join('; ')
      return `${corpo.mensagem ?? 'Dados inválidos'} (${detalhes})`
    }
    if (corpo?.mensagem) return corpo.mensagem
    if (!erro.response) return 'Não foi possível conectar ao servidor. Tente novamente.'
  }
  return 'Ocorreu um erro inesperado.'
}
