package br.com.arpeggio.api.dto.response

data class ExternalErrorResponse(override val streaming: String, val erro: String) : SearchResults