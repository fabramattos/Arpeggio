package br.com.arpeggio.api.domain.resultado

data class ResultadoBuscaErros(override val streaming: String, val erro: String) : ResultadoBusca