package br.com.arpeggio.api.dto.externalApi.spotify

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyApiArtists(
    val items: List<SpotifyApiArtistData>,
)