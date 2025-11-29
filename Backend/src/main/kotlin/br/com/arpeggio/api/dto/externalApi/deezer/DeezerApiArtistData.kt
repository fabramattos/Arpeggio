package br.com.arpeggio.api.dto.externalApi.deezer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeezerApiArtistData(
        val id: Int,
        val name: String,
        val link: String?
)