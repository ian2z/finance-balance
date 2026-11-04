import { useQueryClient } from '@tanstack/react-query'
import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { authApi } from '../api/endpoints'
import { EVENTO_SESSAO_EXPIRADA, tokenStorage } from '../api/client'
import type { AuthResponse, Usuario } from '../types'

const CHAVE_USUARIO = 'fb.usuario'

interface AuthContexto {
  usuario: Usuario | null
  autenticado: boolean
  entrar: (email: string, senha: string) => Promise<void>
  cadastrar: (nome: string, email: string, senha: string) => Promise<void>
  sair: () => void
  atualizarUsuario: (u: Usuario) => void
}

const Contexto = createContext<AuthContexto | null>(null)

function lerUsuario(): Usuario | null {
  try {
    const bruto = localStorage.getItem(CHAVE_USUARIO)
    return bruto && tokenStorage.ler() ? (JSON.parse(bruto) as Usuario) : null
  } catch {
    return null
  }
}

function gravarUsuario(u: Usuario | null) {
  try {
    if (u) localStorage.setItem(CHAVE_USUARIO, JSON.stringify(u))
    else localStorage.removeItem(CHAVE_USUARIO)
  } catch {
    /* ignorar */
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [usuario, setUsuario] = useState<Usuario | null>(lerUsuario)

  const sair = useCallback(() => {
    tokenStorage.limpar()
    gravarUsuario(null)
    setUsuario(null)
    queryClient.clear() // nunca manter dados do usuário anterior em cache
  }, [queryClient])

  useEffect(() => {
    window.addEventListener(EVENTO_SESSAO_EXPIRADA, sair)
    return () => window.removeEventListener(EVENTO_SESSAO_EXPIRADA, sair)
  }, [sair])

  const aplicar = useCallback(
    (r: AuthResponse) => {
      queryClient.clear()
      tokenStorage.gravar(r.token)
      gravarUsuario(r.usuario)
      setUsuario(r.usuario)
    },
    [queryClient],
  )

  const valor = useMemo<AuthContexto>(
    () => ({
      usuario,
      autenticado: usuario !== null,
      entrar: async (email, senha) => aplicar(await authApi.login(email, senha)),
      cadastrar: async (nome, email, senha) => aplicar(await authApi.registrar(nome, email, senha)),
      sair,
      atualizarUsuario: (u) => {
        gravarUsuario(u)
        setUsuario(u)
      },
    }),
    [usuario, aplicar, sair],
  )

  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContexto {
  const ctx = useContext(Contexto)
  if (!ctx) throw new Error('useAuth deve ser usado dentro de AuthProvider')
  return ctx
}
