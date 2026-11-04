import { createBrowserRouter } from 'react-router-dom'
import { Layout } from '../components/Layout'
import { RotaProtegida, RotaPublica } from './ProtectedRoute'

export const router = createBrowserRouter([
  {
    element: <RotaPublica />,
    children: [
      { path: '/login', lazy: async () => ({ Component: (await import('../pages/LoginPage')).LoginPage }) },
      { path: '/cadastro', lazy: async () => ({ Component: (await import('../pages/RegisterPage')).RegisterPage }) },
    ],
  },
  {
    element: <RotaProtegida />,
    children: [
      {
        element: <Layout />,
        children: [
          { index: true, lazy: async () => ({ Component: (await import('../pages/DashboardPage')).DashboardPage }) },
          { path: 'transacoes', lazy: async () => ({ Component: (await import('../pages/TransacoesPage')).TransacoesPage }) },
          { path: 'contas', lazy: async () => ({ Component: (await import('../pages/ContasPage')).ContasPage }) },
          { path: 'categorias', lazy: async () => ({ Component: (await import('../pages/CategoriasPage')).CategoriasPage }) },
          { path: 'orcamentos', lazy: async () => ({ Component: (await import('../pages/OrcamentosPage')).OrcamentosPage }) },
          { path: 'distribuicao', lazy: async () => ({ Component: (await import('../pages/DistribuicaoPage')).DistribuicaoPage }) },
          { path: 'investimentos', lazy: async () => ({ Component: (await import('../pages/InvestimentosPage')).InvestimentosPage }) },
          { path: 'perfil', lazy: async () => ({ Component: (await import('../pages/PerfilPage')).PerfilPage }) },
        ],
      },
    ],
  },
  { path: '*', lazy: async () => ({ Component: (await import('../pages/NaoEncontradaPage')).NaoEncontradaPage }) },
])
