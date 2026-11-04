import { useMutation, useQueryClient, type QueryKey } from '@tanstack/react-query'
import { useState } from 'react'
import { mensagemDeErro } from '../utils/erros'

/** Fluxo padrão de exclusão com confirmação: guarda o item alvo, o erro e invalida as consultas. */
export function useExcluir<T extends { id: number }>(excluir: (id: number) => Promise<void>, invalidar: QueryKey[]) {
  const qc = useQueryClient()
  const [alvo, setAlvo] = useState<T | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  const mutacao = useMutation({
    mutationFn: (id: number) => excluir(id),
    onSuccess: async () => {
      await Promise.all(invalidar.map((k) => qc.invalidateQueries({ queryKey: k })))
      setAlvo(null)
    },
    onError: (e) => setErro(mensagemDeErro(e)),
  })

  return {
    alvo,
    erro,
    pendente: mutacao.isPending,
    pedir: (item: T) => {
      setErro(null)
      setAlvo(item)
    },
    cancelar: () => setAlvo(null),
    confirmar: () => alvo && mutacao.mutate(alvo.id),
  }
}
