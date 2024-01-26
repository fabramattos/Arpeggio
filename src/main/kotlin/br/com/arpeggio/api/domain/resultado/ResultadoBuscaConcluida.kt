package br.com.arpeggio.api.domain.resultado

data class ResultadoBuscaConcluida(override val streaming: String, val albuns: Int) : ResultadoBusca