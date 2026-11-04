import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { contasApi } from '../api/endpoints'
import { Campo, ErroFormulario } from '../components/Campo'
import { Confirmacao } from '../components/Confirmacao'
import { Modal } from '../components/Modal'
import { ErroBanner, PageHeader, Selo, Spinner, Vazio } from '../components/ui'
import { chaves, useContas } from '../hooks/queries'
import { useExcluir } from '../hooks/useExcluir'
import type { Conta, TipoConta } from '../types'
import { mensagemDeErro } from '../utils/erros'
import { formatarMoeda, lerValor, ROTULO_TIPO_CONTA, valorParaCampo } from '../utils/format'

const TIPOS = Object.keys(ROTULO_TIPO_CONTA) as TipoConta[]

const schema = z.object({
  nome: z.string().trim().min(1, 'Informe o nome da conta').max(80, 'Máximo de 80 caracteres'),
  tipo: z.enum(['CORRENTE', 'POUPANCA', 'CARTEIRA', 'DINHEIRO', 'CARTEIRA_DIGITAL']),
  saldoInicial: z.string().refine((v) => !Number.isNaN(lerValor(v)), 'Informe um valor válido, ex.: 1.250,00'),
  ativa: z.boolean(),
})
type Form = z.infer<typeof schema>

function FormConta({ conta, aoConcluir }: { conta: Conta | null; aoConcluir: () => void }) {
  const qc = useQueryClient()
  const [erro, setErro] = useState<string | null>(null)
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<Form>({
    resolver: zodResolver(schema),
    defaultValues: {
      nome: conta?.nome ?? '',
      tipo: conta?.tipo ?? 'CORRENTE',
      saldoInicial: valorParaCampo(conta?.saldoInicial ?? 0),
      ativa: conta?.ativa ?? true,
    },
  })

  const salvar = useMutation({
    mutationFn: (v: Form) => {
      const payload = { nome: v.nome, tipo: v.tipo, saldoInicial: lerValor(v.saldoInicial), ativa: v.ativa }
      return conta ? contasApi.atualizar(conta.id, payload) : contasApi.criar(payload)
    },
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: chaves.contas })
      await qc.invalidateQueries({ queryKey: chaves.dashboard })
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
        <input className="input" placeholder="Ex.: Nubank" {...register('nome')} />
      </Campo>
      <Campo rotulo="Tipo" erro={errors.tipo?.message}>
        <select className="input" {...register('tipo')}>
          {TIPOS.map((t) => (
            <option key={t} value={t}>
              {ROTULO_TIPO_CONTA[t]}
            </option>
          ))}
        </select>
      </Campo>
      <Campo rotulo="Saldo inicial (R$)" erro={errors.saldoInicial?.message}>
        <input className="input num" inputMode="decimal" {...register('saldoInicial')} />
      </Campo>
      <label className="flex items-center gap-2">
        <input type="checkbox" className="size-4 accent-primary" {...register('ativa')} />
        Conta ativa
      </label>
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

export function ContasPage() {
  const { data: contas, isLoading, error, refetch } = useContas()
  const [editando, setEditando] = useState<Conta | 'nova' | null>(null)
  const exclusao = useExcluir<Conta>(contasApi.excluir, [chaves.contas, chaves.dashboard])

  return (
    <>
      <PageHeader
        titulo="Contas"
        descricao="Bancos, carteiras e dinheiro em espécie onde você mantém seus recursos."
        acoes={
          <button type="button" className="btn-primary" onClick={() => setEditando('nova')}>
            Nova conta
          </button>
        }
      />

      {isLoading && <Spinner />}
      {error && <ErroBanner mensagem={mensagemDeErro(error)} aoTentar={() => refetch()} />}
      {contas && contas.length === 0 && (
        <Vazio
          titulo="Nenhuma conta cadastrada"
          descricao="Cadastre ao menos uma conta para registrar receitas e despesas."
          acao={
            <button type="button" className="btn-primary" onClick={() => setEditando('nova')}>
              Cadastrar primeira conta
            </button>
          }
        />
      )}

      <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {contas?.map((c) => (
          <li key={c.id} className="card flex flex-col gap-3">
            <div className="flex items-start justify-between gap-2">
              <div className="min-w-0">
                <h2 className="truncate text-lg">{c.nome}</h2>
                <p className="text-muted">{ROTULO_TIPO_CONTA[c.tipo]}</p>
              </div>
              <Selo tom={c.ativa ? 'positivo' : 'neutro'}>{c.ativa ? 'ATIVA' : 'INATIVA'}</Selo>
            </div>
            <div>
              <p className="label">SALDO ATUAL</p>
              <p className={`num text-2xl font-bold ${c.saldoAtual < 0 ? 'text-danger' : ''}`}>{formatarMoeda(c.saldoAtual)}</p>
              <p className="text-xs text-muted">Saldo inicial: {formatarMoeda(c.saldoInicial)}</p>
            </div>
            <div className="mt-auto flex gap-2">
              <button type="button" className="btn-secondary flex-1" onClick={() => setEditando(c)}>
                Editar
              </button>
              <button type="button" className="btn-danger" onClick={() => exclusao.pedir(c)} aria-label={`Excluir conta ${c.nome}`}>
                Excluir
              </button>
            </div>
          </li>
        ))}
      </ul>

      <Modal aberto={editando !== null} titulo={editando === 'nova' ? 'Nova conta' : 'Editar conta'} aoFechar={() => setEditando(null)}>
        {editando !== null && <FormConta conta={editando === 'nova' ? null : editando} aoConcluir={() => setEditando(null)} />}
      </Modal>

      <Confirmacao
        aberto={exclusao.alvo !== null}
        titulo="Excluir conta"
        mensagem={`Excluir a conta "${exclusao.alvo?.nome}"? Contas com movimentações não podem ser excluídas — nesse caso, desative-a.`}
        erro={exclusao.erro}
        carregando={exclusao.pendente}
        aoConfirmar={exclusao.confirmar}
        aoCancelar={exclusao.cancelar}
      />
    </>
  )
}
