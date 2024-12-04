package br.com.arpeggio.api.dto.response

data class AlbumsResponse(
    override val streaming: String,
    val artista: String,
    val albuns: Int,
) : SearchResults