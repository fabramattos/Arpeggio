package br.com.arpeggio.api.dto.externalApi.spotify

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyApiAlbumsResponse(val total: Int)