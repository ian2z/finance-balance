const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const numero = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

export const formatarMoeda = (valor: number): string => moeda.format(valor)
export const formatarPercentual = (valor: number): string => `${numero.format(valor)}%`

/** Converte 'AAAA-MM-DD' em 'DD/MM/AAAA' sem passar por Date (evita deslocamento de fuso). */
export function formatarData(iso: string): string {
  const [a, m, d] = iso.split('-')
  return `${d}/${m}/${a}`
}

export function formatarMes(anoMes: string): string {
  const [a, m] = anoMes.split('-')
  const nomes = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez']
  return `${nomes[Number(m) - 1]}/${a.slice(2)}`
}

const pad = (n: number) => String(n).padStart(2, '0')

export const hojeIso = (): string => {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export const mesAtual = (): string => hojeIso().slice(0, 7)

export function deslocarMes(anoMes: string, delta: number): string {
  const [a, m] = anoMes.split('-').map(Number)
  const d = new Date(a, m - 1 + delta, 1)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}`
}

export function limitesDoMes(anoMes: string): { de: string; ate: string } {
  const [a, m] = anoMes.split('-').map(Number)
  const ultimo = new Date(a, m, 0).getDate()
  return { de: `${anoMes}-01`, ate: `${anoMes}-${pad(ultimo)}` }
}

/** Ex.: 'Novembro de 2026' (só a primeira letra maiúscula). */
export function nomeDoMes(anoMes: string): string {
  const [a, m] = anoMes.split('-').map(Number)
  const texto = new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' }).format(new Date(a, m - 1, 1))
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

const compacto = new Intl.NumberFormat('pt-BR', { notation: 'compact', maximumFractionDigits: 1 })
export const formatarCompacto = (valor: number): string => compacto.format(valor)

/** Aceita '1.234,56', '1234.56' ou '12,5' e devolve número; NaN se inválido. */
export function lerValor(texto: string): number {
  const t = texto.trim()
  if (/^-?\d{1,3}(\.\d{3})*(,\d{1,2})?$/.test(t)) return Number(t.replace(/\./g, '').replace(',', '.'))
  if (/^-?\d+([.,]\d{1,2})?$/.test(t)) return Number(t.replace(',', '.'))
  return Number.NaN
}

export const valorParaCampo = (v: number): string => v.toFixed(2).replace('.', ',')

export const ROTULO_TIPO_CONTA: Record<string, string> = {
  CORRENTE: 'Conta corrente',
  POUPANCA: 'Poupança',
  CARTEIRA: 'Carteira',
  DINHEIRO: 'Dinheiro em espécie',
  CARTEIRA_DIGITAL: 'Carteira digital',
}

export const ROTULO_TIPO_INVESTIMENTO: Record<string, string> = {
  CDB: 'CDB',
  TESOURO_DIRETO: 'Tesouro Direto',
  FII: 'Fundos imobiliários',
  ACOES: 'Ações',
  FUNDOS: 'Fundos de investimento',
  POUPANCA: 'Poupança',
  OUTROS: 'Outros',
}
