package br.com.arpeggio.api.dto.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ItemResponse(
    val streaming: String,
    val consulta: String,
    val episodios: Int? = null,
    val albuns: Int? = null,
    val erro: String? = null,
)