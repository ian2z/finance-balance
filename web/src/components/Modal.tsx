import { useEffect, useRef, type ReactNode } from 'react'

interface Props {
  aberto: boolean
  titulo: string
  aoFechar: () => void
  children: ReactNode
  largura?: 'md' | 'lg'
}

/** Diálogo modal nativo (<dialog>): foco preso, Esc fecha e restaura o foco automaticamente. */
export function Modal({ aberto, titulo, aoFechar, children, largura = 'md' }: Props) {
  const ref = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const dialogo = ref.current
    if (!dialogo) return
    if (aberto && !dialogo.open) dialogo.showModal()
    if (!aberto && dialogo.open) dialogo.close()
  }, [aberto])

  return (
    <dialog
      ref={ref}
      aria-labelledby="modal-titulo"
      onCancel={(e) => {
        e.preventDefault()
        aoFechar()
      }}
      onClick={(e) => {
        if (e.target === ref.current) aoFechar()
      }}
      className={`m-auto w-[calc(100%-2rem)] ${largura === 'lg' ? 'max-w-2xl' : 'max-w-lg'} rounded-xl border border-line-strong bg-surface p-0 text-fg shadow-overlay backdrop:bg-black/70`}
    >
      {aberto && (
        <div className="max-h-[85vh] overflow-y-auto p-6">
          <div className="mb-5 flex items-start justify-between gap-4">
            <h2 id="modal-titulo" className="text-xl">
              {titulo}
            </h2>
            <button type="button" className="btn-ghost min-h-9 px-3" onClick={aoFechar} aria-label="Fechar">
              ✕
            </button>
          </div>
          {children}
        </div>
      )}
    </dialog>
  )
}
