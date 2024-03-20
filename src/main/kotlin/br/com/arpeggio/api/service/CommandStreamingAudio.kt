package br.com.arpeggio.api.service

import br.com.arpeggio.api.domain.resultado.ResultadoBusca
import br.com.arpeggio.api.infra.busca.RequestParams

interface CommandStreamingAudio {

    val NOME_STREAMING: String
    suspend fun buscaPorArtista(requestParams: RequestParams): ResultadoBusca

    suspend fun buscaPorPodcast(requestParams: RequestParams): ResultadoBusca
}