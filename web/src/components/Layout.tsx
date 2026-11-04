import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../hooks/auth'

const ITENS = [
  { para: '/', rotulo: 'Dashboard', fim: true },
  { para: '/transacoes', rotulo: 'Transações' },
  { para: '/contas', rotulo: 'Contas' },
  { para: '/categorias', rotulo: 'Categorias e etiquetas' },
  { para: '/orcamentos', rotulo: 'Orçamentos' },
  { para: '/distribuicao', rotulo: 'Distribuição da renda' },
  { para: '/investimentos', rotulo: 'Investimentos' },
  { para: '/perfil', rotulo: 'Perfil' },
]

function Marca() {
  return (
    <span className="flex items-center gap-2 text-lg font-bold tracking-tight">
      <span aria-hidden className="grid size-8 place-items-center rounded-lg bg-primary text-on-primary">
        F
      </span>
      Finance Balance
    </span>
  )
}

function Navegacao({ aoNavegar }: { aoNavegar?: () => void }) {
  const { usuario, sair } = useAuth()
  return (
    <div className="flex h-full flex-col">
      <div className="px-3 py-2">
        <Marca />
      </div>
      <nav aria-label="Principal" className="mt-6 flex flex-1 flex-col gap-1">
        {ITENS.map((i) => (
          <NavLink
            key={i.para}
            to={i.para}
            end={i.fim}
            onClick={aoNavegar}
            className={({ isActive }) =>
              `rounded-lg border-l-2 px-3 py-2.5 font-semibold transition-colors ${
                isActive
                  ? 'border-primary bg-surface-2 text-primary'
                  : 'border-transparent text-muted hover:bg-surface-2 hover:text-fg'
              }`
            }
          >
            {i.rotulo}
          </NavLink>
        ))}
      </nav>
      <div className="mt-4 border-t border-line pt-4">
        <p className="truncate px-3 text-xs text-muted" title={usuario?.email}>
          {usuario?.nome}
        </p>
        <button type="button" className="btn-ghost mt-1 w-full justify-start" onClick={sair}>
          Sair
        </button>
      </div>
    </div>
  )
}

export function Layout() {
  const [aberto, setAberto] = useState(false)

  return (
    <div className="min-h-screen lg:grid lg:grid-cols-[16rem_1fr]">
      <a href="#conteudo" className="sr-only focus:not-sr-only focus:absolute focus:z-50 focus:m-2 focus:rounded-lg focus:bg-primary focus:px-3 focus:py-2 focus:text-on-primary">
        Ir para o conteúdo
      </a>

      {/* Barra lateral (desktop) */}
      <aside className="sticky top-0 hidden h-screen border-r border-line bg-surface p-4 lg:block">
        <Navegacao />
      </aside>

      {/* Cabeçalho (mobile e tablet) */}
      <div className="sticky top-0 z-30 flex items-center justify-between border-b border-line bg-surface px-4 py-3 lg:hidden">
        <Marca />
        <button
          type="button"
          className="btn-secondary min-h-10"
          aria-expanded={aberto}
          aria-controls="menu-mobile"
          onClick={() => setAberto((v) => !v)}
        >
          {aberto ? 'Fechar' : 'Menu'}
        </button>
      </div>
      {aberto && (
        <div id="menu-mobile" className="fixed inset-x-0 top-[61px] bottom-0 z-20 overflow-y-auto bg-surface p-4 lg:hidden">
          <Navegacao aoNavegar={() => setAberto(false)} />
        </div>
      )}

      <main id="conteudo" className="mx-auto w-full max-w-6xl px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
        <Outlet />
      </main>
    </div>
  )
}
