package br.com.arpeggio.api.dto.response

import com.fasterxml.jackson.annotation.JsonSubTypes

@JsonSubTypes(
    JsonSubTypes.Type(value = ExternalErrorResponse::class),
    JsonSubTypes.Type(value = AlbumsResponse::class)
)

interface SearchResults{val streaming: String}