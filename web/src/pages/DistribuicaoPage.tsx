import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { distribuicaoApi } from '../api/endpoints'
import { ErroFormulario } from '../components/Campo'
import { opcoesDeCategoria } from '../utils/categorias'
import { ErroBanner, PageHeader, Progresso, Spinner } from '../components/ui'
import type { Categoria, Distribuicao } from '../types'
import { chaves, useCategorias, useDistribuicao } from '../hooks/queries'
import { mensagemDeErro } from '../utils/erros'
import { formatarMoeda, formatarPercentual, lerValor, valorParaCampo } from '../utils/format'

interface Linha {
  chave: number
  categoriaId: string
  percentual: string
}

let contador = 0
const novaLinha = (categoriaId = '', percentual = ''): Linha => ({ chave: ++contador, categoriaId, percentual })

export function DistribuicaoPage() {
  const { data, isLoading, error, refetch } = useDistribuicao()
  const { data: categorias } = useCategorias()

  return (
    <>
      <PageHeader
        titulo="Distribuição da renda"
        descricao="Planeje quanto da sua renda vai para cada categoria. É um plano: não movimenta dinheiro."
      />
      {isLoading && <Spinner />}
      {error && <ErroBanner mensagem={mensagemDeErro(error)} aoTentar={() => refetch()} />}
      {data && categorias && <Conteudo data={data} categorias={categorias} />}
    </>
  )
}

/** Montado só depois de carregar: o estado local do formulário nasce dos dados do servidor. */
function Conteudo({ data, categorias }: { data: Distribuicao; categorias: Categoria[] }) {
  const qc = useQueryClient()
  const [renda, setRenda] = useState(data.rendaBase > 0 ? valorParaCampo(data.rendaBase) : '')
  const [linhas, setLinhas] = useState<Linha[]>(() =>
    data.itens.map((i) => novaLinha(String(i.categoria.id), String(i.percentual).replace('.', ','))),
  )
  const [erro, setErro] = useState<string | null>(null)
  const [salvo, setSalvo] = useState(false)

  const opcoes = useMemo(() => (categorias ? opcoesDeCategoria(categorias, 'DESPESA') : []), [categorias])
  const rendaNum = lerValor(renda)
  const total = linhas.reduce((s, l) => s + (Number.isNaN(lerValor(l.percentual)) ? 0 : lerValor(l.percentual)), 0)
  const estoura = total > 100.0001

  const salvar = useMutation({
    mutationFn: () =>
      distribuicaoApi.definir(
        rendaNum,
        linhas.map((l) => ({ categoriaId: Number(l.categoriaId), percentual: lerValor(l.percentual) })),
      ),
    onSuccess: async () => {
      setSalvo(true)
      await qc.invalidateQueries({ queryKey: chaves.distribuicao })
    },
    onError: (e) => setErro(mensagemDeErro(e)),
  })

  function validar(): string | null {
    if (Number.isNaN(rendaNum) || rendaNum <= 0) return 'Informe a renda mensal base, maior que zero.'
    if (linhas.length === 0) return 'Adicione ao menos uma categoria.'
    const usadas = new Set<string>()
    for (const l of linhas) {
      if (!l.categoriaId) return 'Escolha a categoria de todas as linhas.'
      if (usadas.has(l.categoriaId)) return 'Há categorias repetidas.'
      usadas.add(l.categoriaId)
      const p = lerValor(l.percentual)
      if (Number.isNaN(p) || p <= 0 || p > 100) return 'Os percentuais devem estar entre 0,01 e 100.'
    }
    if (estoura) return 'A soma dos percentuais não pode ultrapassar 100%.'
    return null
  }

  const atualizarLinha = (chave: number, campo: 'categoriaId' | 'percentual', valor: string) => {
    setSalvo(false)
    setLinhas((ls) => ls.map((l) => (l.chave === chave ? { ...l, [campo]: valor } : l)))
  }

  return (
    <>
      {(
        <div className="grid gap-6 lg:grid-cols-[1fr_1fr]">
          <form
            className="card space-y-4"
            noValidate
            onSubmit={(e) => {
              e.preventDefault()
              setSalvo(false)
              const msg = validar()
              setErro(msg)
              if (!msg) salvar.mutate()
            }}
          >
            <h2 className="text-lg">Regras</h2>
            <ErroFormulario mensagem={erro} />
            {salvo && (
              <p role="status" className="text-amber">
                Distribuição salva.
              </p>
            )}
            <div>
              <label htmlFor="renda" className="label">
                RENDA MENSAL BASE (R$)
              </label>
              <input id="renda" className="input num" inputMode="decimal" placeholder="2.000,00" value={renda} onChange={(e) => { setRenda(e.target.value); setSalvo(false) }} />
            </div>

            <ul className="space-y-3">
              {linhas.map((l, i) => (
                <li key={l.chave} className="grid grid-cols-[1fr_6rem_auto] items-end gap-2">
                  <div>
                    <label htmlFor={`cat-${l.chave}`} className="label">
                      CATEGORIA {i + 1}
                    </label>
                    <select id={`cat-${l.chave}`} className="input" value={l.categoriaId} onChange={(e) => atualizarLinha(l.chave, 'categoriaId', e.target.value)}>
                      <option value="">Selecione…</option>
                      {opcoes.map((o) => (
                        <option key={o.id} value={o.id}>
                          {o.rotulo}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label htmlFor={`pct-${l.chave}`} className="label">
                      %
                    </label>
                    <input id={`pct-${l.chave}`} className="input num" inputMode="decimal" value={l.percentual} onChange={(e) => atualizarLinha(l.chave, 'percentual', e.target.value)} />
                  </div>
                  <button type="button" className="btn-ghost text-danger" onClick={() => { setSalvo(false); setLinhas((ls) => ls.filter((x) => x.chave !== l.chave)) }} aria-label={`Remover linha ${i + 1}`}>
                    ✕
                  </button>
                </li>
              ))}
            </ul>

            <button type="button" className="btn-secondary" onClick={() => setLinhas((ls) => [...ls, novaLinha()])}>
              Adicionar categoria
            </button>

            <div className="rounded-lg border border-line-strong bg-surface-2 p-3">
              <div className="flex items-center justify-between">
                <span className="font-semibold">Total alocado</span>
                <span className={`num font-bold ${estoura ? 'text-danger' : ''}`}>{formatarPercentual(total)}</span>
              </div>
              <div className="mt-2">
                <Progresso percentual={total} rotulo="Total alocado da renda" />
              </div>
              <p className={`mt-2 text-xs ${estoura ? 'text-danger' : 'text-muted'}`} role={estoura ? 'alert' : undefined}>
                {estoura ? 'A soma ultrapassa 100%.' : `Não alocado: ${formatarPercentual(Math.max(0, 100 - total))}`}
              </p>
            </div>

            <button type="submit" className="btn-primary" disabled={salvar.isPending}>
              {salvar.isPending ? 'Salvando…' : 'Salvar distribuição'}
            </button>
          </form>

          <section className="card" aria-labelledby="h-plano">
            <h2 id="h-plano" className="mb-1 text-lg">
              Planejado x realizado (mês atual)
            </h2>
            <p className="mb-4 text-muted">Renda base: {formatarMoeda(data.rendaBase)}</p>
            {data.itens.length === 0 ? (
              <p className="text-muted">Defina as regras ao lado para ver o comparativo.</p>
            ) : (
              <ul className="space-y-5">
                {data.itens.map((i) => {
                  const pct = i.valorPlanejado > 0 ? (i.valorRealizado / i.valorPlanejado) * 100 : 0
                  return (
                    <li key={i.categoria.id}>
                      <div className="mb-1 flex items-center justify-between gap-2">
                        <span className="font-semibold">
                          {i.categoria.nome} <span className="font-normal text-muted">· {formatarPercentual(i.percentual)}</span>
                        </span>
                        <span className="num text-muted">
                          {formatarMoeda(i.valorRealizado)} / {formatarMoeda(i.valorPlanejado)}
                        </span>
                      </div>
                      <Progresso percentual={pct} rotulo={`Realizado em ${i.categoria.nome}`} />
                    </li>
                  )
                })}
              </ul>
            )}
          </section>
        </div>
      )}
    </>
  )
}
