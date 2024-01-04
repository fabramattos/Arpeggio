package br.com.primusicos.api.domain.resultado

data class ResultadoBuscaStreaming(override val streaming: String, val albuns: Int) : ResultadoBusca