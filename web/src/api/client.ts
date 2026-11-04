import axios from 'axios'

const CHAVE_TOKEN = 'fb.token'

export const tokenStorage = {
  ler: (): string | null => {
    try {
      return localStorage.getItem(CHAVE_TOKEN)
    } catch {
      return null
    }
  },
  gravar: (token: string) => {
    try {
      localStorage.setItem(CHAVE_TOKEN, token)
    } catch {
      /* armazenamento indisponível: a sessão vale só até recarregar */
    }
  },
  limpar: () => {
    try {
      localStorage.removeItem(CHAVE_TOKEN)
    } catch {
      /* ignorar */
    }
  },
}

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1',
  headers: { Accept: 'application/json' },
})

// Injeta o JWT em toda requisição.
api.interceptors.request.use((config) => {
  const token = tokenStorage.ler()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Sessão expirada ou token inválido: limpa e avisa o app para voltar ao login.
export const EVENTO_SESSAO_EXPIRADA = 'fb:sessao-expirada'
api.interceptors.response.use(
  (resposta) => resposta,
  (erro) => {
    const url: string = erro.config?.url ?? ''
    if (erro.response?.status === 401 && !url.startsWith('/auth/')) {
      tokenStorage.limpar()
      window.dispatchEvent(new Event(EVENTO_SESSAO_EXPIRADA))
    }
    return Promise.reject(erro)
  },
)
