package br.com.primusicos.api.domain.resultado

data class ResultadoBuscaErros(override val streaming: String, val erro: String) : ResultadoBusca