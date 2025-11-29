package br.com.arpeggio.api.dto.externalApi.deezer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeezerApiPodcastsResponse(val data: List<DeezerApiPodcastData>)