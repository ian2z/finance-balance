import type { ReactNode } from 'react'

export function AuthShell({ titulo, subtitulo, children, rodape }: { titulo: string; subtitulo: string; children: ReactNode; rodape: ReactNode }) {
  return (
    <main className="grid min-h-screen place-items-center px-4 py-10">
      <div className="w-full max-w-md">
        <div className="mb-8 flex items-center justify-center gap-2 text-xl font-bold tracking-tight">
          <span aria-hidden className="grid size-9 place-items-center rounded-lg bg-primary text-on-primary">
            F
          </span>
          Finance Balance
        </div>
        <div className="card p-8">
          <h1 className="text-2xl">{titulo}</h1>
          <p className="mt-1 mb-6 text-muted">{subtitulo}</p>
          {children}
        </div>
        <p className="mt-6 text-center text-muted">{rodape}</p>
      </div>
    </main>
  )
}
