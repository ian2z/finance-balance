import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import { z } from 'zod'
import { investimentosApi } from '../api/endpoints'
import { Campo, ErroFormulario } from '../components/Campo'
import { Confirmacao } from '../components/Confirmacao'
import { Modal } from '../components/Modal'
import { Dinheiro, ErroBanner, PageHeader, Spinner, Vazio } from '../components/ui'
import { chaves, useInvestimentos, useMovimentos, useResumoCarteira } from '../hooks/queries'
import { useExcluir } from '../hooks/useExcluir'
import type { Investimento, TipoInvestimento, TipoMovimento } from '../types'
import { COR, PALETA_CATEGORIAS } from '../utils/cores'
import { mensagemDeErro } from '../utils/erros'
import {
  formatarData,
  formatarMoeda,
  formatarPercentual,
  hojeIso,
  lerValor,
  ROTULO_TIPO_INVESTIMENTO,
  valorParaCampo,
} from '../utils/format'

const TIPOS = Object.keys(ROTULO_TIPO_INVESTIMENTO) as TipoInvestimento[]
const opcional = z.string().refine((v) => v.trim() === '' || !Number.isNaN(lerValor(v)), 'Informe um valor válido, ex.: 1.000,00')

const schema = z.object({
  nome: z.string().trim().min(1, 'Informe o nome').max(100, 'Máximo de 100 caracteres'),
  tipo: z.enum(['CDB', 'TESOURO_DIRETO', 'FII', 'ACOES', 'FUNDOS', 'POUPANCA', 'OUTROS']),
  dataAplicacao: z.string().min(1, 'Informe a data'),
  valorInicial: opcional,
  valorAtual: opcional,
})
type Form = z.infer<typeof schema>

function FormInvestimento({ investimento, aoConcluir }: { investimento: Investimento | null; aoConcluir: () => void }) {
  const qc = useQueryClient()
  const [erro, setErro] = useState<string | null>(null)
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<Form>({
    resolver: zodResolver(schema),
    defaultValues: {
      nome: investimento?.nome ?? '',
      tipo: investimento?.tipo ?? 'CDB',
      dataAplicacao: investimento?.dataAplicacao ?? hojeIso(),
      valorInicial: '',
      valorAtual: investimento ? valorParaCampo(investimento.valorAtual) : '',
    },
  })

  const salvar = useMutation({
    mutationFn: (v: Form) => {
      const payload = {
        nome: v.nome,
        tipo: v.tipo,
        dataAplicacao: v.dataAplicacao,
        valorInicial: v.valorInicial.trim() ? lerValor(v.valorInicial) : undefined,
        valorAtual: v.valorAtual.trim() ? lerValor(v.valorAtual) : undefined,
      }
      return investimento ? investimentosApi.atualizar(investimento.id, payload) : investimentosApi.criar(payload)
    },
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: chaves.investimentos })
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
        <input className="input" placeholder="Ex.: CDB Banco Inter 110% CDI" {...register('nome')} />
      </Campo>
      <div className="grid gap-4 sm:grid-cols-2">
        <Campo rotulo="Modalidade" erro={errors.tipo?.message}>
          <select className="input" {...register('tipo')}>
            {TIPOS.map((t) => (
              <option key={t} value={t}>
                {ROTULO_TIPO_INVESTIMENTO[t]}
              </option>
            ))}
          </select>
        </Campo>
        <Campo rotulo="Data da aplicação" erro={errors.dataAplicacao?.message}>
          <input type="date" className="input" {...register('dataAplicacao')} />
        </Campo>
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        {!investimento && (
          <Campo rotulo="Valor aplicado (R$)" erro={errors.valorInicial?.message} dica="Gera o aporte inicial. Opcional.">
            <input className="input num" inputMode="decimal" placeholder="0,00" {...register('valorInicial')} />
          </Campo>
        )}
        <Campo rotulo="Valor atual (R$)" erro={errors.valorAtual?.message} dica={investimento ? 'Informe manualmente o valor de mercado.' : 'Se vazio, usa o valor aplicado.'}>
          <input className="input num" inputMode="decimal" placeholder="0,00" {...register('valorAtual')} />
        </Campo>
      </div>
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

const schemaMov = z.object({
  tipo: z.enum(['APORTE', 'RESGATE', 'RENDIMENTO']),
  valor: z
    .string()
    .refine((v) => !Number.isNaN(lerValor(v)), 'Informe um valor válido, ex.: 250,00')
    .refine((v) => lerValor(v) > 0, 'O valor deve ser maior que zero'),
  data: z.string().min(1, 'Informe a data'),
})
type FormMov = z.infer<typeof schemaMov>

const ROTULO_MOV: Record<TipoMovimento, string> = { APORTE: 'Aporte', RESGATE: 'Resgate', RENDIMENTO: 'Rendimento' }

function Movimentos({ investimento, aoFechar }: { investimento: Investimento; aoFechar: () => void }) {
  const qc = useQueryClient()
  const [erro, setErro] = useState<string | null>(null)
  const { data: movimentos, isLoading } = useMovimentos(investimento.id)
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormMov>({ resolver: zodResolver(schemaMov), defaultValues: { tipo: 'APORTE', valor: '', data: hojeIso() } })

  const movimentar = useMutation({
    mutationFn: (v: FormMov) => investimentosApi.movimentar(investimento.id, { tipo: v.tipo, valor: lerValor(v.valor), data: v.data }),
    onSuccess: async () => {
      reset({ tipo: 'APORTE', valor: '', data: hojeIso() })
      await qc.invalidateQueries({ queryKey: chaves.investimentos })
    },
    onError: (e) => setErro(mensagemDeErro(e)),
  })

  return (
    <div className="space-y-5">
      <form
        noValidate
        className="space-y-4"
        onSubmit={handleSubmit((v) => {
          setErro(null)
          movimentar.mutate(v)
        })}
      >
        <ErroFormulario mensagem={erro} />
        <div className="grid gap-3 sm:grid-cols-3">
          <Campo rotulo="Tipo" erro={errors.tipo?.message}>
            <select className="input" {...register('tipo')}>
              {(Object.keys(ROTULO_MOV) as TipoMovimento[]).map((t) => (
                <option key={t} value={t}>
                  {ROTULO_MOV[t]}
                </option>
              ))}
            </select>
          </Campo>
          <Campo rotulo="Valor (R$)" erro={errors.valor?.message}>
            <input className="input num" inputMode="decimal" placeholder="0,00" {...register('valor')} />
          </Campo>
          <Campo rotulo="Data" erro={errors.data?.message}>
            <input type="date" className="input" {...register('data')} />
          </Campo>
        </div>
        <button type="submit" className="btn-primary" disabled={movimentar.isPending}>
          Registrar movimento
        </button>
      </form>

      <div>
        <h3 className="mb-2 text-base">Histórico</h3>
        {isLoading && <Spinner />}
        {movimentos && movimentos.length === 0 && <p className="text-muted">Sem movimentos.</p>}
        <ul className="divide-y divide-line">
          {movimentos?.map((m) => (
            <li key={m.id} className="flex items-center justify-between py-2">
              <span>
                {ROTULO_MOV[m.tipo]} <span className="text-muted">· {formatarData(m.data)}</span>
              </span>
              <Dinheiro valor={m.valor} tipo={m.tipo === 'RESGATE' ? 'DESPESA' : 'RECEITA'} />
            </li>
          ))}
        </ul>
      </div>
      <div className="flex justify-end">
        <button type="button" className="btn-secondary" onClick={aoFechar}>
          Fechar
        </button>
      </div>
    </div>
  )
}

export function InvestimentosPage() {
  const { data: lista, isLoading, error, refetch } = useInvestimentos()
  const { data: resumo } = useResumoCarteira()
  const [edicao, setEdicao] = useState<Investimento | 'novo' | null>(null)
  const [movendo, setMovendo] = useState<Investimento | null>(null)
  const exclusao = useExcluir<Investimento>(investimentosApi.excluir, [chaves.investimentos])

  // O investimento aberto no modal de movimentos deve refletir os valores recém-atualizados.
  const movendoAtual = movendo ? (lista?.find((i) => i.id === movendo.id) ?? movendo) : null

  return (
    <>
      <PageHeader
        titulo="Investimentos"
        descricao="Registro manual da sua carteira: aportes, resgates e rendimentos."
        acoes={
          <button type="button" className="btn-primary" onClick={() => setEdicao('novo')}>
            Novo investimento
          </button>
        }
      />

      {isLoading && <Spinner />}
      {error && <ErroBanner mensagem={mensagemDeErro(error)} aoTentar={() => refetch()} />}

      {resumo && lista && lista.length > 0 && (
        <section aria-label="Resumo da carteira" className="mb-6 grid gap-4 lg:grid-cols-[2fr_1fr]">
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="card">
              <p className="label">VALOR ATUAL DA CARTEIRA</p>
              <p className="num text-display-sm font-bold max-sm:text-3xl">{formatarMoeda(resumo.totalAtual)}</p>
            </div>
            <div className="card">
              <p className="label">TOTAL APLICADO</p>
              <p className="num text-2xl font-bold">{formatarMoeda(resumo.totalAplicado)}</p>
            </div>
            <div className="card sm:col-span-2">
              <p className="label">RENDIMENTO</p>
              <p className={`num text-2xl font-bold ${resumo.rendimento < 0 ? 'text-danger' : 'text-amber'}`}>
                {resumo.rendimento < 0 ? '− ' : '+ '}
                {formatarMoeda(Math.abs(resumo.rendimento))}
                <span className="ml-2 text-base font-semibold">({formatarPercentual(resumo.rentabilidadePercentual)})</span>
              </p>
            </div>
          </div>
          <div className="card">
            <p className="label">DISTRIBUIÇÃO DA CARTEIRA</p>
            <div className="h-36" role="img" aria-label="Gráfico de rosca com a distribuição da carteira por modalidade">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={resumo.distribuicao} dataKey="valorAtual" nameKey="tipo" innerRadius="60%" outerRadius="95%" stroke={COR.superficie} strokeWidth={2}>
                    {resumo.distribuicao.map((f, i) => (
                      <Cell key={f.tipo} fill={PALETA_CATEGORIAS[i % PALETA_CATEGORIAS.length]} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{ background: COR.superficie, border: `1px solid ${COR.linhaForte}`, borderRadius: 12 }}
                    itemStyle={{ color: COR.texto }}
                    formatter={(v, n) => [formatarMoeda(Number(v)), ROTULO_TIPO_INVESTIMENTO[String(n)] ?? String(n)]}
                  />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <ul className="mt-2 space-y-1">
              {resumo.distribuicao.map((f, i) => (
                <li key={f.tipo} className="flex items-center justify-between gap-2 text-xs">
                  <span className="flex items-center gap-2">
                    <span aria-hidden className="size-2.5 rounded-sm" style={{ background: PALETA_CATEGORIAS[i % PALETA_CATEGORIAS.length] }} />
                    {ROTULO_TIPO_INVESTIMENTO[f.tipo]}
                  </span>
                  <span className="num text-muted">{formatarPercentual(f.percentual)}</span>
                </li>
              ))}
            </ul>
          </div>
        </section>
      )}

      {lista && lista.length === 0 && (
        <Vazio
          titulo="Nenhum investimento registrado"
          descricao="Cadastre seus investimentos para acompanhar aportes e rendimentos."
          acao={
            <button type="button" className="btn-primary" onClick={() => setEdicao('novo')}>
              Cadastrar investimento
            </button>
          }
        />
      )}

      <ul className="grid gap-4 md:grid-cols-2">
        {lista?.map((i) => (
          <li key={i.id} className="card flex flex-col gap-3">
            <div>
              <h2 className="truncate text-lg">{i.nome}</h2>
              <p className="text-muted">
                {ROTULO_TIPO_INVESTIMENTO[i.tipo]} · desde {formatarData(i.dataAplicacao)}
              </p>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <p className="label">APLICADO</p>
                <p className="num font-semibold">{formatarMoeda(i.valorAplicado)}</p>
              </div>
              <div>
                <p className="label">ATUAL</p>
                <p className="num font-semibold">{formatarMoeda(i.valorAtual)}</p>
              </div>
            </div>
            <p className={`num font-semibold ${i.rendimento < 0 ? 'text-danger' : 'text-amber'}`}>
              {i.rendimento < 0 ? '− ' : '+ '}
              {formatarMoeda(Math.abs(i.rendimento))} ({formatarPercentual(i.rentabilidadePercentual)})
            </p>
            <div className="mt-auto flex flex-wrap gap-2">
              <button type="button" className="btn-secondary flex-1" onClick={() => setMovendo(i)}>
                Movimentar
              </button>
              <button type="button" className="btn-secondary" onClick={() => setEdicao(i)}>
                Editar
              </button>
              <button type="button" className="btn-danger" onClick={() => exclusao.pedir(i)} aria-label={`Excluir ${i.nome}`}>
                Excluir
              </button>
            </div>
          </li>
        ))}
      </ul>

      <Modal aberto={edicao !== null} titulo={edicao === 'novo' ? 'Novo investimento' : 'Editar investimento'} aoFechar={() => setEdicao(null)}>
        {edicao !== null && <FormInvestimento investimento={edicao === 'novo' ? null : edicao} aoConcluir={() => setEdicao(null)} />}
      </Modal>

      <Modal aberto={movendoAtual !== null} largura="lg" titulo={`Movimentar — ${movendoAtual?.nome ?? ''}`} aoFechar={() => setMovendo(null)}>
        {movendoAtual && <Movimentos investimento={movendoAtual} aoFechar={() => setMovendo(null)} />}
      </Modal>

      <Confirmacao
        aberto={exclusao.alvo !== null}
        titulo="Excluir investimento"
        mensagem={`Excluir "${exclusao.alvo?.nome}" e todo o seu histórico de movimentos?`}
        erro={exclusao.erro}
        carregando={exclusao.pendente}
        aoConfirmar={exclusao.confirmar}
        aoCancelar={exclusao.cancelar}
      />
    </>
  )
}
