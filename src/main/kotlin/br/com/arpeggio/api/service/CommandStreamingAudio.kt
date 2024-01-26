package br.com.arpeggio.api.service

import br.com.arpeggio.api.domain.resultado.ResultadoBusca

interface CommandStreamingAudio {

    val NOME_STREAMING: String
    suspend fun buscaPorArtista(requestParams: br.com.arpeggio.api.infra.busca.RequestParams): ResultadoBusca
}