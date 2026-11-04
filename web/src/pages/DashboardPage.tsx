import { useState } from 'react'
import { Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { Link } from 'react-router-dom'
import { Dinheiro, ErroBanner, PageHeader, Progresso, SeletorMes, Spinner, Vazio } from '../components/ui'
import { useDashboard } from '../hooks/queries'
import { COR, PALETA_CATEGORIAS } from '../utils/cores'
import { mensagemDeErro } from '../utils/erros'
import {
  deslocarMes,
  formatarCompacto,
  formatarData,
  formatarMes,
  formatarMoeda,
  formatarPercentual,
  limitesDoMes,
  mesAtual,
  nomeDoMes,
} from '../utils/format'

const estiloTooltip = {
  contentStyle: { background: COR.superficie, border: `1px solid ${COR.linhaForte}`, borderRadius: 12, color: COR.texto },
  itemStyle: { color: COR.texto },
  labelStyle: { color: COR.creme },
}

export function DashboardPage() {
  const [mes, setMes] = useState(mesAtual)
  const { de, ate } = limitesDoMes(mes)
  const { data, isLoading, error, refetch } = useDashboard(de, ate)

  return (
    <>
      <PageHeader
        titulo="Dashboard"
        descricao="Resumo da sua situação financeira."
        acoes={
          <SeletorMes
            valor={mes}
            rotulo={nomeDoMes(mes)}
            aoAnterior={() => setMes(deslocarMes(mes, -1))}
            aoProximo={() => setMes(deslocarMes(mes, 1))}
          />
        }
      />

      {isLoading && <Spinner />}
      {error && <ErroBanner mensagem={mensagemDeErro(error)} aoTentar={() => refetch()} />}

      {data && (
        <div className="space-y-6">
          <section aria-label="Indicadores" className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <div className="card sm:col-span-2 lg:col-span-1">
              <p className="label">SALDO TOTAL</p>
              <p className="num text-display-sm font-bold max-sm:text-3xl">{formatarMoeda(data.saldoTotal)}</p>
              <p className="mt-1 text-xs text-muted">Somatório das contas ativas</p>
            </div>
            <div className="card">
              <p className="label">RECEITAS</p>
              <p className="text-2xl font-bold">
                <Dinheiro valor={data.receitas} tipo="RECEITA" />
              </p>
              {data.receitasPendentes > 0 && (
                <p className="mt-1 text-xs text-muted">+ {formatarMoeda(data.receitasPendentes)} a receber</p>
              )}
            </div>
            <div className="card">
              <p className="label">DESPESAS</p>
              <p className="text-2xl font-bold">
                <Dinheiro valor={data.despesas} tipo="DESPESA" />
              </p>
              {data.despesasPendentes > 0 && (
                <p className="mt-1 text-xs text-muted">+ {formatarMoeda(data.despesasPendentes)} a pagar</p>
              )}
            </div>
            <div className="card">
              <p className="label">RESULTADO DO PERÍODO</p>
              <p className={`num text-2xl font-bold ${data.resultado < 0 ? 'text-danger' : 'text-fg'}`}>
                {data.resultado < 0 ? '− ' : ''}
                {formatarMoeda(Math.abs(data.resultado))}
              </p>
              <p className="mt-1 text-xs text-muted">Receitas − despesas pagas</p>
            </div>
          </section>

          <div className="grid gap-6 lg:grid-cols-2">
            <section className="card" aria-labelledby="h-cat">
              <h2 id="h-cat" className="mb-4 text-lg">
                Despesas por categoria
              </h2>
              {data.despesasPorCategoria.length === 0 ? (
                <Vazio titulo="Sem despesas no período" descricao="As despesas pagas aparecerão aqui." />
              ) : (
                <div className="grid items-center gap-4 sm:grid-cols-2">
                  <div className="h-52" role="img" aria-label="Gráfico de rosca com a distribuição das despesas por categoria">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={data.despesasPorCategoria}
                          dataKey="valor"
                          nameKey="categoria"
                          innerRadius="60%"
                          outerRadius="95%"
                          stroke={COR.superficie}
                          strokeWidth={2}
                        >
                          {data.despesasPorCategoria.map((c, i) => (
                            <Cell key={c.categoriaId} fill={PALETA_CATEGORIAS[i % PALETA_CATEGORIAS.length]} />
                          ))}
                        </Pie>
                        <Tooltip {...estiloTooltip} formatter={(v) => formatarMoeda(Number(v))} />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                  <ul className="space-y-2">
                    {data.despesasPorCategoria.map((c, i) => (
                      <li key={c.categoriaId} className="flex items-center justify-between gap-3">
                        <span className="flex items-center gap-2">
                          <span aria-hidden className="size-3 rounded-sm" style={{ background: PALETA_CATEGORIAS[i % PALETA_CATEGORIAS.length] }} />
                          {c.categoria}
                        </span>
                        <span className="num text-muted">
                          {formatarMoeda(c.valor)} · {formatarPercentual(c.percentual)}
                        </span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </section>

            <section className="card" aria-labelledby="h-evo">
              <h2 id="h-evo" className="mb-4 text-lg">
                Receitas x despesas (6 meses)
              </h2>
              <div className="h-60" role="img" aria-label="Gráfico de barras comparando receitas e despesas pagas nos últimos seis meses">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={data.evolucao.map((p) => ({ ...p, rotulo: formatarMes(p.mes) }))}>
                    <CartesianGrid stroke={COR.linha} vertical={false} />
                    <XAxis dataKey="rotulo" stroke={COR.mudo} tickLine={false} axisLine={false} />
                    <YAxis stroke={COR.mudo} tickLine={false} axisLine={false} width={56} tickFormatter={(v) => formatarCompacto(Number(v))} />
                    <Tooltip {...estiloTooltip} cursor={{ fill: COR.linha }} formatter={(v) => formatarMoeda(Number(v))} />
                    <Bar dataKey="receitas" name="Receitas" fill={COR.ambar} radius={[4, 4, 0, 0]} />
                    <Bar dataKey="despesas" name="Despesas" fill={COR.primario} radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
              <p className="mt-2 flex gap-4 text-xs text-muted">
                <span className="flex items-center gap-1.5">
                  <span aria-hidden className="size-3 rounded-sm bg-amber" /> Receitas
                </span>
                <span className="flex items-center gap-1.5">
                  <span aria-hidden className="size-3 rounded-sm bg-primary" /> Despesas
                </span>
              </p>
            </section>
          </div>

          <div className="grid gap-6 lg:grid-cols-2">
            <section className="card" aria-labelledby="h-maiores">
              <h2 id="h-maiores" className="mb-4 text-lg">
                Maiores gastos
              </h2>
              {data.maioresGastos.length === 0 ? (
                <Vazio titulo="Nada por aqui ainda" />
              ) : (
                <ul className="divide-y divide-line">
                  {data.maioresGastos.map((g) => (
                    <li key={g.id} className="flex items-center justify-between gap-3 py-3">
                      <div className="min-w-0">
                        <p className="truncate font-semibold">{g.descricao}</p>
                        <p className="text-xs text-muted">
                          {formatarData(g.data)}
                          {g.categoria ? ` · ${g.categoria}` : ''}
                        </p>
                      </div>
                      <Dinheiro valor={g.valor} tipo="DESPESA" className="font-semibold" />
                    </li>
                  ))}
                </ul>
              )}
            </section>

            <section className="card" aria-labelledby="h-orc">
              <h2 id="h-orc" className="mb-4 text-lg">
                Orçamento do mês
              </h2>
              {data.orcamento.totalLimite === 0 ? (
                <Vazio
                  titulo="Nenhum orçamento definido"
                  descricao="Defina limites por categoria para acompanhar o consumo."
                  acao={
                    <Link to="/orcamentos" className="btn-primary">
                      Criar orçamento
                    </Link>
                  }
                />
              ) : (
                <div className="space-y-3">
                  <p className="num text-3xl font-bold">{formatarPercentual(data.orcamento.percentual)}</p>
                  <Progresso percentual={data.orcamento.percentual} rotulo="Orçamento consumido" />
                  <p className="text-muted">
                    {formatarMoeda(data.orcamento.totalUtilizado)} de {formatarMoeda(data.orcamento.totalLimite)}
                  </p>
                </div>
              )}
            </section>
          </div>
        </div>
      )}
    </>
  )
}
