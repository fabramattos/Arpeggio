package br.com.arpeggio.api.domain.resultado

data class ResultadoBuscaConcluidaPodcast(
    override val streaming: String,
    val podcast: String,
    val episodios: Int,
) : ResultadoBusca