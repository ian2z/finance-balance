import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../hooks/auth'

export function RotaProtegida() {
  const { autenticado } = useAuth()
  const local = useLocation()
  return autenticado ? <Outlet /> : <Navigate to="/login" replace state={{ de: local.pathname }} />
}

/** Telas de login/cadastro: usuário já autenticado vai direto ao dashboard. */
export function RotaPublica() {
  const { autenticado } = useAuth()
  return autenticado ? <Navigate to="/" replace /> : <Outlet />
}
