import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { authApi } from '../api/endpoints'
import { Campo, ErroFormulario } from '../components/Campo'
import { PageHeader } from '../components/ui'
import { useAuth } from '../hooks/auth'
import { mensagemDeErro } from '../utils/erros'

const schemaNome = z.object({ nome: z.string().trim().min(1, 'Informe seu nome').max(120, 'Máximo de 120 caracteres') })
const schemaSenha = z
  .object({
    senhaAtual: z.string().min(1, 'Informe a senha atual'),
    novaSenha: z.string().min(8, 'A senha deve ter ao menos 8 caracteres').max(72, 'Máximo de 72 caracteres'),
    confirmacao: z.string(),
  })
  .refine((v) => v.novaSenha === v.confirmacao, { path: ['confirmacao'], message: 'As senhas não coincidem' })

export function PerfilPage() {
  const { usuario, atualizarUsuario, sair } = useAuth()
  const [msgNome, setMsgNome] = useState<{ ok: boolean; texto: string } | null>(null)
  const [msgSenha, setMsgSenha] = useState<{ ok: boolean; texto: string } | null>(null)

  const formNome = useForm<z.infer<typeof schemaNome>>({
    resolver: zodResolver(schemaNome),
    defaultValues: { nome: usuario?.nome ?? '' },
  })
  const formSenha = useForm<z.infer<typeof schemaSenha>>({ resolver: zodResolver(schemaSenha) })

  const salvarNome = useMutation({
    mutationFn: (nome: string) => authApi.atualizarPerfil(nome),
    onSuccess: (u) => {
      atualizarUsuario(u)
      setMsgNome({ ok: true, texto: 'Nome atualizado.' })
    },
    onError: (e) => setMsgNome({ ok: false, texto: mensagemDeErro(e) }),
  })

  const salvarSenha = useMutation({
    mutationFn: (v: { senhaAtual: string; novaSenha: string }) => authApi.alterarSenha(v.senhaAtual, v.novaSenha),
    onSuccess: () => {
      formSenha.reset()
      setMsgSenha({ ok: true, texto: 'Senha alterada com sucesso.' })
    },
    onError: (e) => setMsgSenha({ ok: false, texto: mensagemDeErro(e) }),
  })

  return (
    <>
      <PageHeader titulo="Perfil" descricao="Seus dados de acesso e preferências." />
      <div className="grid gap-6 lg:grid-cols-2">
        <section className="card" aria-labelledby="h-dados">
          <h2 id="h-dados" className="mb-4 text-lg">
            Dados pessoais
          </h2>
          <form
            noValidate
            className="space-y-4"
            onSubmit={formNome.handleSubmit((v) => {
              setMsgNome(null)
              salvarNome.mutate(v.nome)
            })}
          >
            {msgNome && (msgNome.ok ? <p role="status" className="text-amber">{msgNome.texto}</p> : <ErroFormulario mensagem={msgNome.texto} />)}
            <Campo rotulo="E-mail" dica="O e-mail não pode ser alterado.">
              <input className="input" value={usuario?.email ?? ''} disabled readOnly />
            </Campo>
            <Campo rotulo="Nome" erro={formNome.formState.errors.nome?.message}>
              <input className="input" autoComplete="name" {...formNome.register('nome')} />
            </Campo>
            <button type="submit" className="btn-primary" disabled={salvarNome.isPending}>
              Salvar
            </button>
          </form>
        </section>

        <section className="card" aria-labelledby="h-senha">
          <h2 id="h-senha" className="mb-4 text-lg">
            Alterar senha
          </h2>
          <form
            noValidate
            className="space-y-4"
            onSubmit={formSenha.handleSubmit((v) => {
              setMsgSenha(null)
              salvarSenha.mutate(v)
            })}
          >
            {msgSenha && (msgSenha.ok ? <p role="status" className="text-amber">{msgSenha.texto}</p> : <ErroFormulario mensagem={msgSenha.texto} />)}
            <Campo rotulo="Senha atual" erro={formSenha.formState.errors.senhaAtual?.message}>
              <input type="password" className="input" autoComplete="current-password" {...formSenha.register('senhaAtual')} />
            </Campo>
            <Campo rotulo="Nova senha" erro={formSenha.formState.errors.novaSenha?.message}>
              <input type="password" className="input" autoComplete="new-password" {...formSenha.register('novaSenha')} />
            </Campo>
            <Campo rotulo="Confirmar nova senha" erro={formSenha.formState.errors.confirmacao?.message}>
              <input type="password" className="input" autoComplete="new-password" {...formSenha.register('confirmacao')} />
            </Campo>
            <button type="submit" className="btn-primary" disabled={salvarSenha.isPending}>
              Alterar senha
            </button>
          </form>
        </section>
      </div>
      <div className="mt-6">
        <button type="button" className="btn-secondary" onClick={sair}>
          Sair da conta
        </button>
      </div>
    </>
  )
}
