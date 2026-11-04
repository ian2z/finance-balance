import { Modal } from './Modal'

interface Props {
  aberto: boolean
  titulo: string
  mensagem: string
  rotuloConfirmar?: string
  carregando?: boolean
  erro?: string | null
  aoConfirmar: () => void
  aoCancelar: () => void
}

export function Confirmacao({ aberto, titulo, mensagem, rotuloConfirmar = 'Excluir', carregando, erro, aoConfirmar, aoCancelar }: Props) {
  return (
    <Modal aberto={aberto} titulo={titulo} aoFechar={aoCancelar}>
      <p className="text-muted">{mensagem}</p>
      {erro && (
        <p role="alert" className="mt-3 text-danger">
          {erro}
        </p>
      )}
      <div className="mt-6 flex justify-end gap-2">
        <button type="button" className="btn-secondary" onClick={aoCancelar}>
          Cancelar
        </button>
        <button type="button" className="btn-primary" disabled={carregando} onClick={aoConfirmar}>
          {carregando ? 'Aguarde…' : rotuloConfirmar}
        </button>
      </div>
    </Modal>
  )
}
