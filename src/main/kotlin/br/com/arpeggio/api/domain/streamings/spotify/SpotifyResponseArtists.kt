package br.com.arpeggio.api.domain.spotify

data class SpotifyResponseArtists(
    val next: String?,
    val total: Int,
    val items: List<SpotifyArtist>,
)