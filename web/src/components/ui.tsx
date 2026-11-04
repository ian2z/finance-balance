import type { ReactNode } from 'react'
import { formatarMoeda } from '../utils/format'

export function PageHeader({ titulo, descricao, acoes }: { titulo: string; descricao?: string; acoes?: ReactNode }) {
  return (
    <header className="mb-6 flex flex-wrap items-start justify-between gap-4">
      <div>
        <h1 className="text-display-sm max-sm:text-2xl">{titulo}</h1>
        {descricao && <p className="mt-1 text-muted">{descricao}</p>}
      </div>
      {acoes && <div className="flex flex-wrap items-center gap-2">{acoes}</div>}
    </header>
  )
}

export function Spinner({ rotulo = 'Carregando…' }: { rotulo?: string }) {
  return (
    <div role="status" className="flex items-center justify-center gap-3 py-10 text-muted">
      <span className="size-5 animate-spin rounded-full border-2 border-line-strong border-t-primary" />
      <span>{rotulo}</span>
    </div>
  )
}

export function ErroBanner({ mensagem, aoTentar }: { mensagem: string; aoTentar?: () => void }) {
  return (
    <div role="alert" className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-line-strong bg-surface-2 px-4 py-3 text-danger">
      <span>{mensagem}</span>
      {aoTentar && (
        <button type="button" className="btn-secondary" onClick={aoTentar}>
          Tentar novamente
        </button>
      )}
    </div>
  )
}

export function Vazio({ titulo, descricao, acao }: { titulo: string; descricao?: string; acao?: ReactNode }) {
  return (
    <div className="flex flex-col items-center gap-2 rounded-xl border border-dashed border-line-strong px-6 py-12 text-center">
      <p className="text-base font-semibold">{titulo}</p>
      {descricao && <p className="max-w-md text-muted">{descricao}</p>}
      {acao && <div className="mt-3">{acao}</div>}
    </div>
  )
}

type Tom = 'neutro' | 'positivo' | 'alerta' | 'critico'

const TONS: Record<Tom, string> = {
  neutro: 'border-line-strong bg-surface-2 text-cream',
  positivo: 'border-amber bg-surface text-amber',
  alerta: 'border-amber-soft bg-surface text-amber-soft',
  critico: 'border-primary-hover bg-surface-2 text-primary-hover',
}

export function Selo({ tom = 'neutro', children }: { tom?: Tom; children: ReactNode }) {
  return <span className={`badge ${TONS[tom]}`}>{children}</span>
}

/** Valor monetário: receitas em âmbar (+), despesas em tom de erro (−); o sinal garante que a cor não é o único sinal. */
export function Dinheiro({ valor, tipo, className = '' }: { valor: number; tipo?: 'RECEITA' | 'DESPESA' | 'TRANSFERENCIA'; className?: string }) {
  const cor = tipo === 'RECEITA' ? 'text-amber' : tipo === 'DESPESA' ? 'text-danger' : ''
  const sinal = tipo === 'RECEITA' ? '+ ' : tipo === 'DESPESA' ? '− ' : ''
  return (
    <span className={`num ${cor} ${className}`}>
      {sinal}
      {formatarMoeda(Math.abs(valor))}
    </span>
  )
}

export function Progresso({ percentual, rotulo }: { percentual: number; rotulo: string }) {
  const p = Math.max(0, Math.min(100, percentual))
  const cor = percentual >= 100 ? 'bg-primary-hover' : percentual >= 80 ? 'bg-amber-soft' : 'bg-primary'
  return (
    <div
      role="progressbar"
      aria-label={rotulo}
      aria-valuenow={Math.round(percentual)}
      aria-valuemin={0}
      aria-valuemax={100}
      className="h-2 w-full overflow-hidden rounded-full bg-surface-2"
    >
      <div className={`h-full rounded-full ${cor}`} style={{ width: `${p}%` }} />
    </div>
  )
}

export function Paginacao({
  pagina,
  totalPaginas,
  aoMudar,
}: {
  pagina: number
  totalPaginas: number
  aoMudar: (p: number) => void
}) {
  if (totalPaginas <= 1) return null
  return (
    <nav aria-label="Paginação" className="mt-4 flex items-center justify-between gap-3">
      <button type="button" className="btn-secondary" disabled={pagina === 0} onClick={() => aoMudar(pagina - 1)}>
        Anterior
      </button>
      <span className="text-muted">
        Página {pagina + 1} de {totalPaginas}
      </span>
      <button type="button" className="btn-secondary" disabled={pagina + 1 >= totalPaginas} onClick={() => aoMudar(pagina + 1)}>
        Próxima
      </button>
    </nav>
  )
}

export function SeletorMes({ valor, rotulo, aoAnterior, aoProximo }: { valor: string; rotulo: string; aoAnterior: () => void; aoProximo: () => void }) {
  return (
    <div className="flex items-center gap-1 rounded-xl border border-line-strong bg-surface-2 p-1">
      <button type="button" className="btn-ghost min-h-9 px-3" onClick={aoAnterior} aria-label="Mês anterior">
        ‹
      </button>
      <span className="min-w-36 text-center font-semibold" aria-live="polite" data-valor={valor}>
        {rotulo}
      </span>
      <button type="button" className="btn-ghost min-h-9 px-3" onClick={aoProximo} aria-label="Próximo mês">
        ›
      </button>
    </div>
  )
}
