import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { transacoesApi } from '../api/endpoints'
import { chaves, useCategorias, useContas, useEtiquetas, useFormasPagamento } from '../hooks/queries'
import type { Transacao } from '../types'
import { opcoesDeCategoria } from '../utils/categorias'
import { mensagemDeErro } from '../utils/erros'
import { hojeIso, lerValor, valorParaCampo } from '../utils/format'
import { Campo, ErroFormulario } from './Campo'
import { Spinner } from './ui'

const valorSchema = z
  .string()
  .refine((v) => !Number.isNaN(lerValor(v)), 'Informe um valor válido, ex.: 25,90')
  .refine((v) => lerValor(v) > 0, 'O valor deve ser maior que zero')

const schema = z.object({
  tipo: z.enum(['RECEITA', 'DESPESA']),
  descricao: z.string().trim().min(1, 'Informe a descrição').max(200, 'Máximo de 200 caracteres'),
  valor: valorSchema,
  data: z.string().min(1, 'Informe a data'),
  situacao: z.enum(['PAGA', 'PENDENTE']),
  dataVencimento: z.string(),
  categoriaId: z.string().min(1, 'Escolha uma categoria'),
  contaId: z.string().min(1, 'Escolha uma conta'),
  formaPagamentoId: z.string(),
  observacao: z.string().max(500, 'Máximo de 500 caracteres'),
  etiquetaIds: z.array(z.string()),
})
type Form = z.infer<typeof schema>

export function TransacaoForm({ transacao, aoConcluir }: { transacao: Transacao | null; aoConcluir: () => void }) {
  const qc = useQueryClient()
  const [erro, setErro] = useState<string | null>(null)
  const { data: contas } = useContas()
  const { data: categorias } = useCategorias()
  const { data: formas } = useFormasPagamento()
  const { data: etiquetas } = useEtiquetas()

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<Form>({
    resolver: zodResolver(schema),
    defaultValues: {
      tipo: transacao && transacao.tipo !== 'TRANSFERENCIA' ? transacao.tipo : 'DESPESA',
      descricao: transacao?.descricao ?? '',
      valor: transacao ? valorParaCampo(transacao.valor) : '',
      data: transacao?.data ?? hojeIso(),
      situacao: transacao?.situacao ?? 'PAGA',
      dataVencimento: transacao?.dataVencimento ?? '',
      categoriaId: String(transacao?.categoria?.id ?? ''),
      contaId: String(transacao?.conta.id ?? ''),
      formaPagamentoId: String(transacao?.formaPagamento?.id ?? ''),
      observacao: transacao?.observacao ?? '',
      etiquetaIds: transacao?.etiquetas.map((e) => String(e.id)) ?? [],
    },
  })

  const tipo = watch('tipo')
  const situacao = watch('situacao')
  const opcoes = useMemo(() => (categorias ? opcoesDeCategoria(categorias, tipo) : []), [categorias, tipo])
  const contasDisponiveis = contas?.filter((c) => c.ativa || c.id === transacao?.conta.id) ?? []

  const salvar = useMutation({
    mutationFn: (v: Form) => {
      const payload = {
        descricao: v.descricao,
        valor: lerValor(v.valor),
        tipo: v.tipo,
        data: v.data,
        dataVencimento: v.dataVencimento || null,
        situacao: v.situacao,
        categoriaId: Number(v.categoriaId),
        contaId: Number(v.contaId),
        formaPagamentoId: v.formaPagamentoId ? Number(v.formaPagamentoId) : null,
        observacao: v.observacao || null,
        etiquetaIds: v.etiquetaIds.map(Number),
      }
      return transacao ? transacoesApi.atualizar(transacao.id, payload) : transacoesApi.criar(payload)
    },
    onSuccess: async () => {
      await Promise.all([chaves.transacoes, chaves.contas, chaves.dashboard, chaves.orcamentos, chaves.distribuicao].map((k) => qc.invalidateQueries({ queryKey: k })))
      aoConcluir()
    },
    onError: (e) => setErro(mensagemDeErro(e)),
  })

  if (!contas || !categorias || !formas || !etiquetas) return <Spinner />
  if (contasDisponiveis.length === 0)
    return <p className="text-muted">Cadastre ao menos uma conta ativa na tela “Contas” antes de lançar movimentações.</p>

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

      <fieldset>
        <legend className="label">TIPO</legend>
        <div className="grid grid-cols-2 gap-1 rounded-xl border border-line-strong bg-surface-2 p-1">
          {(['DESPESA', 'RECEITA'] as const).map((t) => (
            <label
              key={t}
              className={`cursor-pointer rounded-lg px-3 py-2 text-center font-bold has-[:focus-visible]:outline-2 has-[:focus-visible]:outline-primary ${
                tipo === t ? 'bg-primary text-on-primary' : 'text-muted hover:text-fg'
              }`}
            >
              <input
                type="radio"
                value={t}
                className="sr-only"
                {...register('tipo', { onChange: () => setValue('categoriaId', '') })}
              />
              {t === 'DESPESA' ? 'Despesa' : 'Receita'}
            </label>
          ))}
        </div>
      </fieldset>

      <Campo rotulo="Descrição" erro={errors.descricao?.message}>
        <input className="input" {...register('descricao')} />
      </Campo>

      <div className="grid gap-4 sm:grid-cols-2">
        <Campo rotulo="Valor (R$)" erro={errors.valor?.message}>
          <input className="input num" inputMode="decimal" placeholder="0,00" {...register('valor')} />
        </Campo>
        <Campo rotulo="Data" erro={errors.data?.message}>
          <input type="date" className="input" {...register('data')} />
        </Campo>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <Campo rotulo="Categoria" erro={errors.categoriaId?.message}>
          <select className="input" {...register('categoriaId')}>
            <option value="">Selecione…</option>
            {opcoes.map((o) => (
              <option key={o.id} value={o.id}>
                {o.rotulo}
              </option>
            ))}
          </select>
        </Campo>
        <Campo rotulo="Conta" erro={errors.contaId?.message}>
          <select className="input" {...register('contaId')}>
            <option value="">Selecione…</option>
            {contasDisponiveis.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nome}
              </option>
            ))}
          </select>
        </Campo>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <Campo rotulo="Forma de pagamento">
          <select className="input" {...register('formaPagamentoId')}>
            <option value="">— Não informada —</option>
            {formas.map((f) => (
              <option key={f.id} value={f.id}>
                {f.nome}
              </option>
            ))}
          </select>
        </Campo>
        <Campo rotulo="Situação">
          <select className="input" {...register('situacao')}>
            <option value="PAGA">{tipo === 'RECEITA' ? 'Recebida' : 'Paga'}</option>
            <option value="PENDENTE">Pendente</option>
          </select>
        </Campo>
      </div>

      {situacao === 'PENDENTE' && (
        <Campo rotulo="Vencimento">
          <input type="date" className="input" {...register('dataVencimento')} />
        </Campo>
      )}

      {etiquetas.length > 0 && (
        <fieldset>
          <legend className="label">ETIQUETAS</legend>
          <div className="flex flex-wrap gap-2">
            {etiquetas.map((e) => (
              <label key={e.id} className="cursor-pointer">
                <input type="checkbox" value={e.id} className="peer sr-only" {...register('etiquetaIds')} />
                <span className="inline-block rounded-full border border-line-strong bg-surface-2 px-3 py-1 text-cream peer-checked:border-primary peer-checked:bg-primary peer-checked:text-on-primary peer-focus-visible:outline-2 peer-focus-visible:outline-primary">
                  {e.nome}
                </span>
              </label>
            ))}
          </div>
        </fieldset>
      )}

      <Campo rotulo="Observação" erro={errors.observacao?.message}>
        <textarea rows={2} className="input" {...register('observacao')} />
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

const schemaTransf = z
  .object({
    descricao: z.string().trim().min(1, 'Informe a descrição').max(200, 'Máximo de 200 caracteres'),
    valor: valorSchema,
    data: z.string().min(1, 'Informe a data'),
    contaOrigemId: z.string().min(1, 'Escolha a conta de origem'),
    contaDestinoId: z.string().min(1, 'Escolha a conta de destino'),
  })
  .refine((v) => v.contaOrigemId !== v.contaDestinoId, { path: ['contaDestinoId'], message: 'Deve ser diferente da origem' })
type FormTransf = z.infer<typeof schemaTransf>

export function TransferenciaForm({ aoConcluir }: { aoConcluir: () => void }) {
  const qc = useQueryClient()
  const [erro, setErro] = useState<string | null>(null)
  const { data: contas } = useContas()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormTransf>({
    resolver: zodResolver(schemaTransf),
    defaultValues: { descricao: 'Transferência entre contas', valor: '', data: hojeIso(), contaOrigemId: '', contaDestinoId: '' },
  })

  const salvar = useMutation({
    mutationFn: (v: FormTransf) =>
      transacoesApi.transferir({
        descricao: v.descricao,
        valor: lerValor(v.valor),
        data: v.data,
        contaOrigemId: Number(v.contaOrigemId),
        contaDestinoId: Number(v.contaDestinoId),
      }),
    onSuccess: async () => {
      await Promise.all([chaves.transacoes, chaves.contas, chaves.dashboard].map((k) => qc.invalidateQueries({ queryKey: k })))
      aoConcluir()
    },
    onError: (e) => setErro(mensagemDeErro(e)),
  })

  if (!contas) return <Spinner />
  const ativas = contas.filter((c) => c.ativa)
  if (ativas.length < 2) return <p className="text-muted">Você precisa de ao menos duas contas ativas para transferir entre elas.</p>

  return (
    <form
      noValidate
      className="space-y-4"
      onSubmit={handleSubmit((v) => {
        setErro(null)
        salvar.mutate(v)
      })}
    >
      <p className="text-muted">Transferências movem dinheiro entre suas contas e não contam como receita nem despesa.</p>
      <ErroFormulario mensagem={erro} />
      <Campo rotulo="Descrição" erro={errors.descricao?.message}>
        <input className="input" {...register('descricao')} />
      </Campo>
      <div className="grid gap-4 sm:grid-cols-2">
        <Campo rotulo="Valor (R$)" erro={errors.valor?.message}>
          <input className="input num" inputMode="decimal" placeholder="0,00" {...register('valor')} />
        </Campo>
        <Campo rotulo="Data" erro={errors.data?.message}>
          <input type="date" className="input" {...register('data')} />
        </Campo>
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <Campo rotulo="De" erro={errors.contaOrigemId?.message}>
          <select className="input" {...register('contaOrigemId')}>
            <option value="">Selecione…</option>
            {ativas.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nome}
              </option>
            ))}
          </select>
        </Campo>
        <Campo rotulo="Para" erro={errors.contaDestinoId?.message}>
          <select className="input" {...register('contaDestinoId')}>
            <option value="">Selecione…</option>
            {ativas.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nome}
              </option>
            ))}
          </select>
        </Campo>
      </div>
      <div className="flex justify-end gap-2 pt-2">
        <button type="button" className="btn-secondary" onClick={aoConcluir}>
          Cancelar
        </button>
        <button type="submit" className="btn-primary" disabled={salvar.isPending}>
          {salvar.isPending ? 'Transferindo…' : 'Transferir'}
        </button>
      </div>
    </form>
  )
}
