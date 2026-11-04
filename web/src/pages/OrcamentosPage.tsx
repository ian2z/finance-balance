import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { orcamentosApi } from '../api/endpoints'
import { Campo, ErroFormulario } from '../components/Campo'
import { Confirmacao } from '../components/Confirmacao'
import { Modal } from '../components/Modal'
import { opcoesDeCategoria } from '../utils/categorias'
import { ErroBanner, PageHeader, Progresso, SeletorMes, Selo, Spinner, Vazio } from '../components/ui'
import { chaves, useCategorias, useOrcamentos } from '../hooks/queries'
import { useExcluir } from '../hooks/useExcluir'
import type { Orcamento, SituacaoOrcamento } from '../types'
import { mensagemDeErro } from '../utils/erros'
import { deslocarMes, formatarMoeda, formatarPercentual, lerValor, mesAtual, nomeDoMes, valorParaCampo } from '../utils/format'

const schema = z.object({
  categoriaId: z.string().min(1, 'Escolha uma categoria'),
  valorLimite: z
    .string()
    .refine((v) => !Number.isNaN(lerValor(v)), 'Informe um valor válido, ex.: 500,00')
    .refine((v) => lerValor(v) > 0, 'O limite deve ser maior que zero'),
})
type Form = z.infer<typeof schema>

const SELOS: Record<SituacaoOrcamento, { tom: 'positivo' | 'alerta' | 'critico'; texto: string }> = {
  DENTRO_DO_LIMITE: { tom: 'positivo', texto: 'DENTRO DO LIMITE' },
  PROXIMO_DO_LIMITE: { tom: 'alerta', texto: 'PRÓXIMO DO LIMITE' },
  ESTOURADO: { tom: 'critico', texto: 'LIMITE ULTRAPASSADO' },
}

function FormOrcamento({ orcamento, mes, aoConcluir }: { orcamento: Orcamento | null; mes: string; aoConcluir: () => void }) {
  const qc = useQueryClient()
  const [erro, setErro] = useState<string | null>(null)
  const { data: categorias } = useCategorias()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<Form>({
    resolver: zodResolver(schema),
    defaultValues: {
      categoriaId: String(orcamento?.categoria.id ?? ''),
      valorLimite: orcamento ? valorParaCampo(orcamento.valorLimite) : '',
    },
  })

  const salvar = useMutation({
    mutationFn: (v: Form) => {
      const payload = { categoriaId: Number(v.categoriaId), mes: `${mes}-01`, valorLimite: lerValor(v.valorLimite) }
      return orcamento ? orcamentosApi.atualizar(orcamento.id, payload) : orcamentosApi.criar(payload)
    },
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: chaves.orcamentos })
      await qc.invalidateQueries({ queryKey: chaves.dashboard })
      aoConcluir()
    },
    onError: (e) => setErro(mensagemDeErro(e)),
  })

  if (!categorias) return <Spinner />

  return (
    <form
      noValidate
      className="space-y-4"
      onSubmit={handleSubmit((v) => {
        setErro(null)
        salvar.mutate(v)
      })}
    >
      <p className="text-muted">Orçamento de {nomeDoMes(mes)}. Despesas de subcategorias contam para a categoria principal.</p>
      <ErroFormulario mensagem={erro} />
      <Campo rotulo="Categoria de despesa" erro={errors.categoriaId?.message}>
        <select className="input" {...register('categoriaId')}>
          <option value="">Selecione…</option>
          {opcoesDeCategoria(categorias, 'DESPESA').map((o) => (
            <option key={o.id} value={o.id}>
              {o.rotulo}
            </option>
          ))}
        </select>
      </Campo>
      <Campo rotulo="Limite do mês (R$)" erro={errors.valorLimite?.message}>
        <input className="input num" inputMode="decimal" placeholder="0,00" {...register('valorLimite')} />
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

export function OrcamentosPage() {
  const [mes, setMes] = useState(mesAtual)
  const { data, isLoading, error, refetch } = useOrcamentos(mes)
  const [edicao, setEdicao] = useState<Orcamento | 'novo' | null>(null)
  const exclusao = useExcluir<Orcamento>(orcamentosApi.excluir, [chaves.orcamentos, chaves.dashboard])

  return (
    <>
      <PageHeader
        titulo="Orçamentos"
        descricao="Limites mensais de gastos por categoria."
        acoes={
          <>
            <SeletorMes valor={mes} rotulo={nomeDoMes(mes)} aoAnterior={() => setMes(deslocarMes(mes, -1))} aoProximo={() => setMes(deslocarMes(mes, 1))} />
            <button type="button" className="btn-primary" onClick={() => setEdicao('novo')}>
              Novo orçamento
            </button>
          </>
        }
      />

      {isLoading && <Spinner />}
      {error && <ErroBanner mensagem={mensagemDeErro(error)} aoTentar={() => refetch()} />}
      {data && data.length === 0 && (
        <Vazio
          titulo="Nenhum orçamento neste mês"
          descricao="Defina quanto pretende gastar em cada categoria e acompanhe o consumo."
          acao={
            <button type="button" className="btn-primary" onClick={() => setEdicao('novo')}>
              Criar orçamento
            </button>
          }
        />
      )}

      <ul className="grid gap-4 md:grid-cols-2">
        {data?.map((o) => {
          const selo = SELOS[o.situacao]
          return (
            <li key={o.id} className="card flex flex-col gap-3">
              <div className="flex items-start justify-between gap-2">
                <h2 className="truncate text-lg">{o.categoria.nome}</h2>
                <Selo tom={selo.tom}>{selo.texto}</Selo>
              </div>
              <div className="flex items-end justify-between gap-2">
                <p className="num text-3xl font-bold">{formatarPercentual(o.percentualConsumido)}</p>
                <p className="num text-right text-muted">
                  {formatarMoeda(o.valorUtilizado)} de {formatarMoeda(o.valorLimite)}
                </p>
              </div>
              <Progresso percentual={o.percentualConsumido} rotulo={`Consumo do orçamento de ${o.categoria.nome}`} />
              <p className={`num ${o.valorRestante < 0 ? 'text-danger' : 'text-muted'}`}>
                {o.valorRestante >= 0 ? `Restam ${formatarMoeda(o.valorRestante)}` : `Excedeu em ${formatarMoeda(-o.valorRestante)}`}
              </p>
              <div className="mt-auto flex gap-2">
                <button type="button" className="btn-secondary flex-1" onClick={() => setEdicao(o)}>
                  Editar
                </button>
                <button type="button" className="btn-danger" onClick={() => exclusao.pedir(o)} aria-label={`Excluir orçamento de ${o.categoria.nome}`}>
                  Excluir
                </button>
              </div>
            </li>
          )
        })}
      </ul>

      <Modal aberto={edicao !== null} titulo={edicao === 'novo' ? 'Novo orçamento' : 'Editar orçamento'} aoFechar={() => setEdicao(null)}>
        {edicao !== null && <FormOrcamento orcamento={edicao === 'novo' ? null : edicao} mes={mes} aoConcluir={() => setEdicao(null)} />}
      </Modal>

      <Confirmacao
        aberto={exclusao.alvo !== null}
        titulo="Excluir orçamento"
        mensagem={`Excluir o orçamento de "${exclusao.alvo?.categoria.nome}"?`}
        erro={exclusao.erro}
        carregando={exclusao.pendente}
        aoConfirmar={exclusao.confirmar}
        aoCancelar={exclusao.cancelar}
      />
    </>
  )
}
