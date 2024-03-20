package br.com.arpeggio.api.domain.resultado

import com.fasterxml.jackson.annotation.JsonSubTypes

@JsonSubTypes(
    JsonSubTypes.Type(value = ResultadoBuscaErros::class),
    JsonSubTypes.Type(value = ResultadoBuscaConcluidaAlbuns::class)
)

interface ResultadoBusca{val streaming: String}