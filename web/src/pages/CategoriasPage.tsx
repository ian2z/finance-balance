import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { categoriasApi, etiquetasApi } from '../api/endpoints'
import { Campo, ErroFormulario } from '../components/Campo'
import { Confirmacao } from '../components/Confirmacao'
import { Modal } from '../components/Modal'
import { ErroBanner, PageHeader, Selo, Spinner, Vazio } from '../components/ui'
import { chaves, useCategorias, useEtiquetas } from '../hooks/queries'
import { useExcluir } from '../hooks/useExcluir'
import type { Categoria, Ref, TipoCategoria } from '../types'
import { mensagemDeErro } from '../utils/erros'

const schema = z.object({
  nome: z.string().trim().min(1, 'Informe o nome').max(80, 'Máximo de 80 caracteres'),
  tipo: z.enum(['RECEITA', 'DESPESA']),
  paiId: z.string(),
})
type Form = z.infer<typeof schema>

interface Edicao {
  categoria: Categoria | null
  tipo: TipoCategoria
  paiId: number | null
}

function FormCategoria({ edicao, categorias, aoConcluir }: { edicao: Edicao; categorias: Categoria[]; aoConcluir: () => void }) {
  const qc = useQueryClient()
  const [erro, setErro] = useState<string | null>(null)
  const { categoria } = edicao
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<Form>({
    resolver: zodResolver(schema),
    defaultValues: {
      nome: categoria?.nome ?? '',
      tipo: categoria?.tipo ?? edicao.tipo,
      paiId: String(categoria?.categoriaPaiId ?? edicao.paiId ?? ''),
    },
  })
  const tipo = watch('tipo')
  // Pais possíveis: categorias de nível superior do mesmo tipo (subcategorias têm um único nível).
  const temFilhas = categoria ? categorias.some((c) => c.categoriaPaiId === categoria.id) : false
  const pais = categorias.filter((c) => c.categoriaPaiId === null && c.tipo === tipo && c.id !== categoria?.id)

  const salvar = useMutation({
    mutationFn: (v: Form) => {
      const payload = { nome: v.nome, tipo: v.tipo, categoriaPaiId: v.paiId ? Number(v.paiId) : null }
      return categoria ? categoriasApi.atualizar(categoria.id, payload) : categoriasApi.criar(payload)
    },
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: chaves.categorias })
      aoConcluir()
    },
    onError: (e) => setErro(mensagemDeErro(e)),
  })

  return (
    <form
      noValidate
      className="space-y-4"
      onSubmit={handleSubmit((v) => {
        setErro(null)
        salvar.mutate(v)
      })}
    >
      <ErroFormulario mensagem={erro} />
      <Campo rotulo="Nome" erro={errors.nome?.message}>
        <input className="input" {...register('nome')} />
      </Campo>
      <Campo rotulo="Tipo" dica={categoria ? 'O tipo só pode mudar se a categoria não estiver em uso.' : undefined}>
        <select className="input" {...register('tipo')}>
          <option value="DESPESA">Despesa</option>
          <option value="RECEITA">Receita</option>
        </select>
      </Campo>
      <Campo rotulo="Categoria pai (opcional)" dica={temFilhas ? 'Categorias com subcategorias não podem ser subcategoria.' : undefined}>
        <select className="input" disabled={temFilhas} {...register('paiId')}>
          <option value="">— Nenhuma (categoria principal) —</option>
          {pais.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nome}
            </option>
          ))}
        </select>
      </Campo>
      <div className="flex justify-end gap-2 pt-2">
        <button type="button" className="btn-secondary" onClick={aoConcluir}>
          Cancelar
        </button>
        <button type="submit" className="btn-primary" disabled={salvar.isPending}>
          {salvar.isPending ? 'Salvando…' : 'Salvar'}
        </button>
      </div>
    </form>
  )
}

function ArvoreCategorias({
  titulo,
  tipo,
  categorias,
  aoCriar,
  aoEditar,
  aoExcluir,
}: {
  titulo: string
  tipo: TipoCategoria
  categorias: Categoria[]
  aoCriar: (tipo: TipoCategoria, paiId: number | null) => void
  aoEditar: (c: Categoria) => void
  aoExcluir: (c: Categoria) => void
}) {
  const principais = categorias.filter((c) => c.tipo === tipo && c.categoriaPaiId === null)

  const linha = (c: Categoria, nivel: 0 | 1) => (
    <li key={c.id} className={`flex flex-wrap items-center justify-between gap-2 py-2 ${nivel ? 'pl-6' : ''}`}>
      <span className="flex min-w-0 items-center gap-2">
        {nivel === 1 && <span aria-hidden className="text-subtle">└</span>}
        <span className={`truncate ${nivel === 0 ? 'font-semibold' : ''}`}>{c.nome}</span>
        {c.doSistema && <Selo>PADRÃO</Selo>}
        {!c.doSistema && c.semMovimentacoes && <Selo tom="alerta">SEM MOVIMENTAÇÕES</Selo>}
      </span>
      <span className="flex gap-1">
        {nivel === 0 && (
          <button type="button" className="btn-ghost min-h-8 px-2 text-xs" onClick={() => aoCriar(tipo, c.id)} aria-label={`Adicionar subcategoria em ${c.nome}`}>
            + Sub
          </button>
        )}
        {!c.doSistema && (
          <>
            <button type="button" className="btn-ghost min-h-8 px-2 text-xs" onClick={() => aoEditar(c)} aria-label={`Editar ${c.nome}`}>
              Editar
            </button>
            <button type="button" className="btn-ghost min-h-8 px-2 text-xs text-danger" onClick={() => aoExcluir(c)} aria-label={`Excluir ${c.nome}`}>
              Excluir
            </button>
          </>
        )}
      </span>
    </li>
  )

  return (
    <section className="card" aria-label={titulo}>
      <div className="mb-3 flex items-center justify-between">
        <h2 className="text-lg">{titulo}</h2>
        <button type="button" className="btn-secondary min-h-9" onClick={() => aoCriar(tipo, null)}>
          Nova
        </button>
      </div>
      {principais.length === 0 ? (
        <Vazio titulo="Nenhuma categoria" />
      ) : (
        <ul className="divide-y divide-line">
          {principais.map((p) => (
            <li key={p.id}>
              <ul>
                {linha(p, 0)}
                {categorias.filter((f) => f.categoriaPaiId === p.id).map((f) => linha(f, 1))}
              </ul>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

function Etiquetas() {
  const qc = useQueryClient()
  const { data: etiquetas, isLoading, error } = useEtiquetas()
  const [nome, setNome] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const exclusao = useExcluir<Ref>(etiquetasApi.excluir, [chaves.etiquetas, chaves.transacoes])

  const criar = useMutation({
    mutationFn: (n: string) => etiquetasApi.criar(n),
    onSuccess: async () => {
      setNome('')
      setErro(null)
      await qc.invalidateQueries({ queryKey: chaves.etiquetas })
    },
    onError: (e) => setErro(mensagemDeErro(e)),
  })

  return (
    <section className="card" aria-labelledby="h-etiquetas">
      <h2 id="h-etiquetas" className="mb-1 text-lg">
        Etiquetas
      </h2>
      <p className="mb-4 text-muted">Marque transações com etiquetas livres, como “Viagem”, e filtre depois.</p>
      <form
        className="mb-4 flex flex-wrap gap-2"
        onSubmit={(e) => {
          e.preventDefault()
          if (nome.trim()) criar.mutate(nome.trim())
        }}
      >
        <label htmlFor="nova-etiqueta" className="sr-only">
          Nome da nova etiqueta
        </label>
        <input id="nova-etiqueta" className="input max-w-xs flex-1" value={nome} maxLength={40} placeholder="Nova etiqueta" onChange={(e) => setNome(e.target.value)} />
        <button type="submit" className="btn-primary" disabled={criar.isPending || !nome.trim()}>
          Adicionar
        </button>
      </form>
      {erro && <ErroFormulario mensagem={erro} />}
      {isLoading && <Spinner />}
      {error && <ErroBanner mensagem={mensagemDeErro(error)} />}
      {etiquetas && etiquetas.length === 0 && <p className="text-muted">Nenhuma etiqueta criada.</p>}
      <ul className="mt-3 flex flex-wrap gap-2">
        {etiquetas?.map((e) => (
          <li key={e.id} className="flex items-center gap-1 rounded-full border border-line-strong bg-surface-2 py-1 pr-1 pl-3 text-cream">
            {e.nome}
            <button type="button" className="grid size-6 place-items-center rounded-full text-muted hover:bg-line-strong hover:text-fg" onClick={() => exclusao.pedir(e)} aria-label={`Excluir etiqueta ${e.nome}`}>
              ✕
            </button>
          </li>
        ))}
      </ul>
      <Confirmacao
        aberto={exclusao.alvo !== null}
        titulo="Excluir etiqueta"
        mensagem={`Excluir a etiqueta "${exclusao.alvo?.nome}"? Ela será removida das transações que a usam.`}
        erro={exclusao.erro}
        carregando={exclusao.pendente}
        aoConfirmar={exclusao.confirmar}
        aoCancelar={exclusao.cancelar}
      />
    </section>
  )
}

export function CategoriasPage() {
  const { data: categorias, isLoading, error, refetch } = useCategorias()
  const [edicao, setEdicao] = useState<Edicao | null>(null)
  const exclusao = useExcluir<Categoria>(categoriasApi.excluir, [chaves.categorias])

  return (
    <>
      <PageHeader titulo="Categorias e etiquetas" descricao="Organize receitas e despesas por finalidade." />
      {isLoading && <Spinner />}
      {error && <ErroBanner mensagem={mensagemDeErro(error)} aoTentar={() => refetch()} />}
      {categorias && (
        <div className="grid gap-6 lg:grid-cols-2">
          <ArvoreCategorias titulo="Despesas" tipo="DESPESA" categorias={categorias} aoCriar={(tipo, paiId) => setEdicao({ categoria: null, tipo, paiId })} aoEditar={(c) => setEdicao({ categoria: c, tipo: c.tipo, paiId: c.categoriaPaiId })} aoExcluir={exclusao.pedir} />
          <ArvoreCategorias titulo="Receitas" tipo="RECEITA" categorias={categorias} aoCriar={(tipo, paiId) => setEdicao({ categoria: null, tipo, paiId })} aoEditar={(c) => setEdicao({ categoria: c, tipo: c.tipo, paiId: c.categoriaPaiId })} aoExcluir={exclusao.pedir} />
        </div>
      )}
      <div className="mt-6">
        <Etiquetas />
      </div>

      <Modal aberto={edicao !== null} titulo={edicao?.categoria ? 'Editar categoria' : 'Nova categoria'} aoFechar={() => setEdicao(null)}>
        {edicao && categorias && <FormCategoria edicao={edicao} categorias={categorias} aoConcluir={() => setEdicao(null)} />}
      </Modal>

      <Confirmacao
        aberto={exclusao.alvo !== null}
        titulo="Excluir categoria"
        mensagem={`Excluir "${exclusao.alvo?.nome}"? Categorias com subcategorias ou movimentações não podem ser excluídas.`}
        erro={exclusao.erro}
        carregando={exclusao.pendente}
        aoConfirmar={exclusao.confirmar}
        aoCancelar={exclusao.cancelar}
      />
    </>
  )
}
