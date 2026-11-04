import { useEffect, useMemo, useState } from 'react'
import { transacoesApi } from '../api/endpoints'
import { Confirmacao } from '../components/Confirmacao'
import { Modal } from '../components/Modal'
import { TransacaoForm, TransferenciaForm } from '../components/TransacaoForm'
import { opcoesDeCategoria } from '../utils/categorias'
import { Dinheiro, ErroBanner, PageHeader, Paginacao, Selo, Spinner, Vazio } from '../components/ui'
import { chaves, useCategorias, useContas, useEtiquetas, useTransacoes } from '../hooks/queries'
import { useExcluir } from '../hooks/useExcluir'
import type { FiltroTransacoes, SituacaoTransacao, TipoTransacao, Transacao } from '../types'
import { mensagemDeErro } from '../utils/erros'
import { formatarData } from '../utils/format'

const TAMANHO = 15

function useAtraso<T>(valor: T, ms = 350): T {
  const [atrasado, setAtrasado] = useState(valor)
  useEffect(() => {
    const t = setTimeout(() => setAtrasado(valor), ms)
    return () => clearTimeout(t)
  }, [valor, ms])
  return atrasado
}

interface Filtros {
  texto: string
  tipo: '' | TipoTransacao
  situacao: '' | SituacaoTransacao
  categoriaId: string
  contaId: string
  etiquetaId: string
  de: string
  ate: string
}
const FILTROS_VAZIOS: Filtros = { texto: '', tipo: '', situacao: '', categoriaId: '', contaId: '', etiquetaId: '', de: '', ate: '' }

export function TransacoesPage() {
  const [filtros, setFiltros] = useState<Filtros>(FILTROS_VAZIOS)
  const [pagina, setPagina] = useState(0)
  const [sort, setSort] = useState('data,desc')
  const [edicao, setEdicao] = useState<Transacao | 'nova' | 'transferencia' | null>(null)
  const [erroExport, setErroExport] = useState<string | null>(null)

  const textoAtrasado = useAtraso(filtros.texto)
  const periodoInvalido = filtros.de !== '' && filtros.ate !== '' && filtros.de > filtros.ate

  const filtroApi: FiltroTransacoes = useMemo(
    () => ({
      texto: textoAtrasado || undefined,
      tipo: filtros.tipo || undefined,
      situacao: filtros.situacao || undefined,
      categoriaId: filtros.categoriaId ? Number(filtros.categoriaId) : undefined,
      contaId: filtros.contaId ? Number(filtros.contaId) : undefined,
      etiquetaId: filtros.etiquetaId ? Number(filtros.etiquetaId) : undefined,
      de: periodoInvalido ? undefined : filtros.de || undefined,
      ate: periodoInvalido ? undefined : filtros.ate || undefined,
    }),
    [textoAtrasado, filtros, periodoInvalido],
  )
  const consulta = useMemo<FiltroTransacoes>(() => ({ ...filtroApi, page: pagina, size: TAMANHO, sort }), [filtroApi, pagina, sort])

  const { data, isLoading, isFetching, error, refetch } = useTransacoes(consulta)
  const { data: contas } = useContas()
  const { data: categorias } = useCategorias()
  const { data: etiquetas } = useEtiquetas()
  const exclusao = useExcluir<Transacao>(transacoesApi.excluir, [chaves.transacoes, chaves.contas, chaves.dashboard, chaves.orcamentos])

  const atualizar = <K extends keyof Filtros>(campo: K, valor: Filtros[K]) => {
    setFiltros((f) => ({ ...f, [campo]: valor }))
    setPagina(0)
  }

  const opcoesCategoria = useMemo(
    () => (categorias ? [...opcoesDeCategoria(categorias, 'DESPESA'), ...opcoesDeCategoria(categorias, 'RECEITA')] : []),
    [categorias],
  )
  const filtrando = Object.values(filtros).some((v) => v !== '')

  async function exportar() {
    setErroExport(null)
    try {
      await transacoesApi.exportarCsv(filtroApi)
    } catch (e) {
      setErroExport(mensagemDeErro(e))
    }
  }

  const rotuloConta = (t: Transacao) => (t.contaDestino ? `${t.conta.nome} → ${t.contaDestino.nome}` : t.conta.nome)

  return (
    <>
      <PageHeader
        titulo="Transações"
        descricao="Receitas, despesas e transferências."
        acoes={
          <>
            <button type="button" className="btn-secondary" onClick={exportar}>
              Exportar CSV
            </button>
            <button type="button" className="btn-secondary" onClick={() => setEdicao('transferencia')}>
              Transferir
            </button>
            <button type="button" className="btn-primary" onClick={() => setEdicao('nova')}>
              Nova transação
            </button>
          </>
        }
      />

      {erroExport && <div className="mb-4"><ErroBanner mensagem={erroExport} /></div>}

      <section aria-label="Filtros" className="card mb-4">
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <div className="sm:col-span-2">
            <label htmlFor="f-texto" className="label">BUSCAR</label>
            <input id="f-texto" type="search" className="input" placeholder="Descrição…" value={filtros.texto} onChange={(e) => atualizar('texto', e.target.value)} />
          </div>
          <div>
            <label htmlFor="f-tipo" className="label">TIPO</label>
            <select id="f-tipo" className="input" value={filtros.tipo} onChange={(e) => atualizar('tipo', e.target.value as Filtros['tipo'])}>
              <option value="">Todos</option>
              <option value="RECEITA">Receita</option>
              <option value="DESPESA">Despesa</option>
              <option value="TRANSFERENCIA">Transferência</option>
            </select>
          </div>
          <div>
            <label htmlFor="f-situacao" className="label">SITUAÇÃO</label>
            <select id="f-situacao" className="input" value={filtros.situacao} onChange={(e) => atualizar('situacao', e.target.value as Filtros['situacao'])}>
              <option value="">Todas</option>
              <option value="PAGA">Paga</option>
              <option value="PENDENTE">Pendente</option>
            </select>
          </div>
          <div>
            <label htmlFor="f-cat" className="label">CATEGORIA</label>
            <select id="f-cat" className="input" value={filtros.categoriaId} onChange={(e) => atualizar('categoriaId', e.target.value)}>
              <option value="">Todas</option>
              {opcoesCategoria.map((o) => (
                <option key={o.id} value={o.id}>{o.rotulo}</option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="f-conta" className="label">CONTA</label>
            <select id="f-conta" className="input" value={filtros.contaId} onChange={(e) => atualizar('contaId', e.target.value)}>
              <option value="">Todas</option>
              {contas?.map((c) => (
                <option key={c.id} value={c.id}>{c.nome}</option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="f-etiqueta" className="label">ETIQUETA</label>
            <select id="f-etiqueta" className="input" value={filtros.etiquetaId} onChange={(e) => atualizar('etiquetaId', e.target.value)}>
              <option value="">Todas</option>
              {etiquetas?.map((e) => (
                <option key={e.id} value={e.id}>{e.nome}</option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="f-de" className="label">DE</label>
            <input id="f-de" type="date" className="input" value={filtros.de} max={filtros.ate || undefined} onChange={(e) => atualizar('de', e.target.value)} />
          </div>
          <div>
            <label htmlFor="f-ate" className="label">ATÉ</label>
            <input id="f-ate" type="date" className="input" value={filtros.ate} min={filtros.de || undefined} onChange={(e) => atualizar('ate', e.target.value)} />
          </div>
          <div>
            <label htmlFor="f-ordem" className="label">ORDENAR POR</label>
            <select id="f-ordem" className="input" value={sort} onChange={(e) => { setSort(e.target.value); setPagina(0) }}>
              <option value="data,desc">Data (mais recentes)</option>
              <option value="data,asc">Data (mais antigas)</option>
              <option value="valor,desc">Valor (maior)</option>
              <option value="valor,asc">Valor (menor)</option>
            </select>
          </div>
          <div className="flex items-end">
            <button type="button" className="btn-ghost" disabled={!filtrando} onClick={() => { setFiltros(FILTROS_VAZIOS); setPagina(0) }}>
              Limpar filtros
            </button>
          </div>
        </div>
        {periodoInvalido && <p role="alert" className="mt-3 text-danger">A data inicial deve ser anterior ou igual à final.</p>}
      </section>

      {isLoading && <Spinner />}
      {error && <ErroBanner mensagem={mensagemDeErro(error)} aoTentar={() => refetch()} />}

      {data && data.conteudo.length === 0 && (
        <Vazio
          titulo={filtrando ? 'Nenhuma transação encontrada' : 'Nenhuma transação ainda'}
          descricao={filtrando ? 'Ajuste ou limpe os filtros.' : 'Registre sua primeira receita ou despesa.'}
          acao={!filtrando && <button type="button" className="btn-primary" onClick={() => setEdicao('nova')}>Nova transação</button>}
        />
      )}

      {data && data.conteudo.length > 0 && (
        <div aria-busy={isFetching}>
          <p className="mb-2 text-muted" aria-live="polite">
            {data.totalElementos} {data.totalElementos === 1 ? 'resultado' : 'resultados'}
          </p>

          {/* Tabela (tablet e desktop) */}
          <div className="hidden overflow-hidden rounded-xl border border-line bg-surface md:block">
            <table className="w-full text-left">
              <caption className="sr-only">Lista de transações</caption>
              <thead className="border-b border-line bg-surface-2 text-xs tracking-wide text-cream">
                <tr>
                  <th scope="col" className="px-4 py-3 font-semibold">DATA</th>
                  <th scope="col" className="px-4 py-3 font-semibold">DESCRIÇÃO</th>
                  <th scope="col" className="px-4 py-3 font-semibold">CATEGORIA</th>
                  <th scope="col" className="px-4 py-3 font-semibold">CONTA</th>
                  <th scope="col" className="px-4 py-3 text-right font-semibold">VALOR</th>
                  <th scope="col" className="px-4 py-3"><span className="sr-only">Ações</span></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {data.conteudo.map((t) => (
                  <tr key={t.id} className="hover:bg-surface-2/50">
                    <td className="num px-4 py-3 whitespace-nowrap text-muted">{formatarData(t.data)}</td>
                    <td className="px-4 py-3">
                      <p className="font-semibold">{t.descricao}</p>
                      <p className="mt-0.5 flex flex-wrap gap-1">
                        {t.situacao === 'PENDENTE' && <Selo tom="alerta">PENDENTE</Selo>}
                        {t.etiquetas.map((e) => <Selo key={e.id}>{e.nome}</Selo>)}
                      </p>
                    </td>
                    <td className="px-4 py-3 text-muted">{t.categoria?.nome ?? 'Transferência'}</td>
                    <td className="px-4 py-3 text-muted">{rotuloConta(t)}</td>
                    <td className="px-4 py-3 text-right font-semibold whitespace-nowrap"><Dinheiro valor={t.valor} tipo={t.tipo} /></td>
                    <td className="px-4 py-3 text-right whitespace-nowrap">
                      {t.tipo !== 'TRANSFERENCIA' && (
                        <button type="button" className="btn-ghost min-h-8 px-2 text-xs" onClick={() => setEdicao(t)} aria-label={`Editar ${t.descricao}`}>Editar</button>
                      )}
                      <button type="button" className="btn-ghost min-h-8 px-2 text-xs text-danger" onClick={() => exclusao.pedir(t)} aria-label={`Excluir ${t.descricao}`}>Excluir</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Cartões (mobile) */}
          <ul className="space-y-3 md:hidden">
            {data.conteudo.map((t) => (
              <li key={t.id} className="card p-4">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="truncate font-semibold">{t.descricao}</p>
                    <p className="text-xs text-muted">{formatarData(t.data)} · {t.categoria?.nome ?? 'Transferência'}</p>
                    <p className="text-xs text-muted">{rotuloConta(t)}</p>
                  </div>
                  <Dinheiro valor={t.valor} tipo={t.tipo} className="font-bold whitespace-nowrap" />
                </div>
                <div className="mt-2 flex flex-wrap items-center justify-between gap-2">
                  <span className="flex flex-wrap gap-1">
                    {t.situacao === 'PENDENTE' && <Selo tom="alerta">PENDENTE</Selo>}
                    {t.etiquetas.map((e) => <Selo key={e.id}>{e.nome}</Selo>)}
                  </span>
                  <span className="flex">
                    {t.tipo !== 'TRANSFERENCIA' && (
                      <button type="button" className="btn-ghost min-h-9 px-2 text-xs" onClick={() => setEdicao(t)} aria-label={`Editar ${t.descricao}`}>Editar</button>
                    )}
                    <button type="button" className="btn-ghost min-h-9 px-2 text-xs text-danger" onClick={() => exclusao.pedir(t)} aria-label={`Excluir ${t.descricao}`}>Excluir</button>
                  </span>
                </div>
              </li>
            ))}
          </ul>

          <Paginacao pagina={data.pagina} totalPaginas={data.totalPaginas} aoMudar={setPagina} />
        </div>
      )}

      <Modal
        aberto={edicao !== null}
        largura="lg"
        titulo={edicao === 'transferencia' ? 'Transferir entre contas' : edicao === 'nova' || edicao === null ? 'Nova transação' : 'Editar transação'}
        aoFechar={() => setEdicao(null)}
      >
        {edicao === 'transferencia' && <TransferenciaForm aoConcluir={() => setEdicao(null)} />}
        {edicao !== null && edicao !== 'transferencia' && (
          <TransacaoForm transacao={edicao === 'nova' ? null : edicao} aoConcluir={() => setEdicao(null)} />
        )}
      </Modal>

      <Confirmacao
        aberto={exclusao.alvo !== null}
        titulo="Excluir transação"
        mensagem={`Excluir "${exclusao.alvo?.descricao}"? Esta ação não pode ser desfeita.`}
        erro={exclusao.erro}
        carregando={exclusao.pendente}
        aoConfirmar={exclusao.confirmar}
        aoCancelar={exclusao.cancelar}
      />
    </>
  )
}
