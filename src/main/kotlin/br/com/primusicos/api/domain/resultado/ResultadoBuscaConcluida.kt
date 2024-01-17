package br.com.primusicos.api.domain.resultado

data class ResultadoBuscaConcluida(override val streaming: String, val albuns: Int) : ResultadoBusca