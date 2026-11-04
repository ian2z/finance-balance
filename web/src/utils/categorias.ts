import type { Categoria } from '../types'

/** Opções "Categoria › Subcategoria" do tipo pedido, em ordem alfabética por grupo. */
export function opcoesDeCategoria(categorias: Categoria[], tipo: 'RECEITA' | 'DESPESA') {
  const doTipo = categorias.filter((c) => c.tipo === tipo)
  const principais = doTipo.filter((c) => c.categoriaPaiId === null)
  return principais.flatMap((p) => [
    { id: p.id, rotulo: p.nome },
    ...doTipo.filter((f) => f.categoriaPaiId === p.id).map((f) => ({ id: f.id, rotulo: `${p.nome} › ${f.nome}` })),
  ])
}
