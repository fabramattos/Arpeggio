package br.com.arpeggio.api.domain.resultado

data class ResultadoBuscaConcluidaPodcast(override val streaming: String, val episodios: Int) : ResultadoBusca