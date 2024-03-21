package br.com.arpeggio.api.domain.resultado

data class ResultadoBuscaConcluidaAlbuns(
    override val streaming: String,
    val artista: String,
    val albuns: Int,
) : ResultadoBusca