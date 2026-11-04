import { cloneElement, isValidElement, useId, type ReactElement, type ReactNode } from 'react'

interface Props {
  rotulo: string
  erro?: string
  dica?: string
  children: ReactNode
}

/** Rótulo + controle + mensagem de erro, ligados por id/aria para leitores de tela. */
export function Campo({ rotulo, erro, dica, children }: Props) {
  const id = useId()
  const msgId = `${id}-msg`
  const controle = isValidElement(children)
    ? cloneElement(children as ReactElement<Record<string, unknown>>, {
        id,
        'aria-invalid': erro ? true : undefined,
        'aria-describedby': erro || dica ? msgId : undefined,
      })
    : children
  return (
    <div>
      <label htmlFor={id} className="label">
        {rotulo}
      </label>
      {controle}
      {(erro || dica) && (
        <p id={msgId} role={erro ? 'alert' : undefined} className={`mt-1 text-xs ${erro ? 'text-danger' : 'text-muted'}`}>
          {erro ?? dica}
        </p>
      )}
    </div>
  )
}

export function ErroFormulario({ mensagem }: { mensagem: string | null }) {
  if (!mensagem) return null
  return (
    <p role="alert" className="rounded-lg border border-line-strong bg-surface-2 px-3 py-2 text-danger">
      {mensagem}
    </p>
  )
}
