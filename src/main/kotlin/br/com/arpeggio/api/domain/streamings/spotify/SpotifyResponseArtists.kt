package br.com.arpeggio.api.domain.streamings.spotify

data class SpotifyResponseArtists(
    val next: String?,
    val total: Int,
    val items: List<SpotifyArtist>,
)