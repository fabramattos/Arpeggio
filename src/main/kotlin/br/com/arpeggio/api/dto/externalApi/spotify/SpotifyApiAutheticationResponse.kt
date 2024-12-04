package br.com.arpeggio.api.dto.externalApi.spotify

data class SpotifyApiAutheticationResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: String,
)