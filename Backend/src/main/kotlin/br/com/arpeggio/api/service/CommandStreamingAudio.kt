package br.com.arpeggio.api.service

import br.com.arpeggio.api.dto.request.RequestParams
import br.com.arpeggio.api.dto.response.ItemResponse

interface CommandStreamingAudio {

    val NOME_STREAMING: String
    suspend fun buscaPorArtista(requestParams: RequestParams): ItemResponse

    suspend fun buscaPorPodcast(requestParams: RequestParams): ItemResponse
}