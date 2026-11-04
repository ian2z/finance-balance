import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { AuthShell } from '../components/AuthShell'
import { Campo, ErroFormulario } from '../components/Campo'
import { useAuth } from '../hooks/auth'
import { mensagemDeErro } from '../utils/erros'

const schema = z
  .object({
    nome: z.string().trim().min(1, 'Informe seu nome').max(120, 'Máximo de 120 caracteres'),
    email: z.string().trim().min(1, 'Informe seu e-mail').email('E-mail inválido'),
    senha: z.string().min(8, 'A senha deve ter ao menos 8 caracteres').max(72, 'Máximo de 72 caracteres'),
    confirmacao: z.string(),
  })
  .refine((v) => v.senha === v.confirmacao, { path: ['confirmacao'], message: 'As senhas não coincidem' })
type Form = z.infer<typeof schema>

export function RegisterPage() {
  const { cadastrar } = useAuth()
  const navegar = useNavigate()
  const [erro, setErro] = useState<string | null>(null)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<Form>({ resolver: zodResolver(schema) })

  const enviar = handleSubmit(async ({ nome, email, senha }) => {
    setErro(null)
    try {
      await cadastrar(nome, email, senha)
      navegar('/', { replace: true })
    } catch (e) {
      setErro(mensagemDeErro(e))
    }
  })

  return (
    <AuthShell
      titulo="Criar conta"
      subtitulo="Comece a organizar suas finanças em poucos minutos."
      rodape={
        <>
          Já tem conta?{' '}
          <Link to="/login" className="font-semibold text-primary hover:text-primary-soft">
            Entrar
          </Link>
        </>
      }
    >
      <form onSubmit={enviar} noValidate className="space-y-4">
        <ErroFormulario mensagem={erro} />
        <Campo rotulo="Nome" erro={errors.nome?.message}>
          <input type="text" autoComplete="name" className="input" {...register('nome')} />
        </Campo>
        <Campo rotulo="E-mail" erro={errors.email?.message}>
          <input type="email" autoComplete="email" className="input" {...register('email')} />
        </Campo>
        <Campo rotulo="Senha" erro={errors.senha?.message} dica="Mínimo de 8 caracteres.">
          <input type="password" autoComplete="new-password" className="input" {...register('senha')} />
        </Campo>
        <Campo rotulo="Confirmar senha" erro={errors.confirmacao?.message}>
          <input type="password" autoComplete="new-password" className="input" {...register('confirmacao')} />
        </Campo>
        <button type="submit" className="btn-primary w-full" disabled={isSubmitting}>
          {isSubmitting ? 'Criando…' : 'Criar conta'}
        </button>
      </form>
    </AuthShell>
  )
}
