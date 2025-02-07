package br.com.arpeggio.api.dto.externalApi.deezer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeezerApiPodcastData (
        val id: Int,
        val title: String,
        val link: String?,
)