package br.com.arpeggio.api.dto.externalApi.tidal

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TidalResult(
    val id: String,
    val name: String,
    var qty: Int,
)
