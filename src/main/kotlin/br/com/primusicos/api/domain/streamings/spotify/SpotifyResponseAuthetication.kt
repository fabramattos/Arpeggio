package br.com.primusicos.api.domain.spotify

data class SpotifyResponseAuthetication(
    val access_token: String,
    val token_type: String,
    val expires_in: String,
)