import { Link } from 'react-router-dom'

export function NaoEncontradaPage() {
  return (
    <main className="grid min-h-screen place-items-center px-4 text-center">
      <div>
        <p className="text-display-sm text-primary">404</p>
        <h1 className="mt-2 text-2xl">Página não encontrada</h1>
        <p className="mt-2 text-muted">O endereço acessado não existe.</p>
        <Link to="/" className="btn-primary mt-6">
          Voltar ao início
        </Link>
      </div>
    </main>
  )
}
