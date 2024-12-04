package br.com.arpeggio.api.dto.response

data class PodcastsResponse(
    override val streaming: String,
    val podcast: String,
    val episodios: Int,
) : SearchResults