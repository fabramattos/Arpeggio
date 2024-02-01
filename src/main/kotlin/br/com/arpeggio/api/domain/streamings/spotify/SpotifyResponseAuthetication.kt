package br.com.arpeggio.api.domain.streamings.spotify

data class SpotifyResponseAuthetication(
    val access_token: String,
    val token_type: String,
    val expires_in: String,
)