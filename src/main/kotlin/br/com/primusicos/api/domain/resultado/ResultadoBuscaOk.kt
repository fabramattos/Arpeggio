package br.com.primusicos.api.domain.resultado

data class ResultadoBuscaOk(override val streaming: String, val albuns: Int) : ResultadoBusca