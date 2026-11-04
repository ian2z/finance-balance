import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { AuthShell } from '../components/AuthShell'
import { Campo, ErroFormulario } from '../components/Campo'
import { useAuth } from '../hooks/auth'
import { mensagemDeErro } from '../utils/erros'

const schema = z.object({
  email: z.string().trim().min(1, 'Informe seu e-mail').email('E-mail inválido'),
  senha: z.string().min(1, 'Informe sua senha'),
})
type Form = z.infer<typeof schema>

export function LoginPage() {
  const { entrar } = useAuth()
  const navegar = useNavigate()
  const local = useLocation()
  const [erro, setErro] = useState<string | null>(null)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<Form>({ resolver: zodResolver(schema) })

  const enviar = handleSubmit(async ({ email, senha }) => {
    setErro(null)
    try {
      await entrar(email, senha)
      navegar((local.state as { de?: string } | null)?.de ?? '/', { replace: true })
    } catch (e) {
      setErro(mensagemDeErro(e))
    }
  })

  return (
    <AuthShell
      titulo="Entrar"
      subtitulo="Acesse sua conta para ver suas finanças."
      rodape={
        <>
          Ainda não tem conta?{' '}
          <Link to="/cadastro" className="font-semibold text-primary hover:text-primary-soft">
            Cadastre-se
          </Link>
        </>
      }
    >
      <form onSubmit={enviar} noValidate className="space-y-4">
        <ErroFormulario mensagem={erro} />
        <Campo rotulo="E-mail" erro={errors.email?.message}>
          <input type="email" autoComplete="email" className="input" {...register('email')} />
        </Campo>
        <Campo rotulo="Senha" erro={errors.senha?.message}>
          <input type="password" autoComplete="current-password" className="input" {...register('senha')} />
        </Campo>
        <button type="submit" className="btn-primary w-full" disabled={isSubmitting}>
          {isSubmitting ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
    </AuthShell>
  )
}
