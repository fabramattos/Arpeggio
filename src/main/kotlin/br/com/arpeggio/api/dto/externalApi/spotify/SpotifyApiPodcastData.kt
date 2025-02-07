package br.com.arpeggio.api.dto.externalApi.spotify

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyApiPodcastData(
    val name: String,
    val total_episodes: Int,
)