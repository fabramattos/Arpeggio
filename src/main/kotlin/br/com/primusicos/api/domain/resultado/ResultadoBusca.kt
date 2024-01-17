package br.com.primusicos.api.domain.resultado

import com.fasterxml.jackson.annotation.JsonSubTypes

@JsonSubTypes(
    JsonSubTypes.Type(value = ResultadoBuscaErros::class),
    JsonSubTypes.Type(value = ResultadoBuscaConcluida::class)
)

interface ResultadoBusca{val streaming: String}