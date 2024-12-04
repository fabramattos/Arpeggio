package br.com.arpeggio.api.service

import br.com.arpeggio.api.dto.response.SearchResults
import br.com.arpeggio.api.dto.request.RequestParams

interface CommandStreamingAudio {

    val NOME_STREAMING: String
    suspend fun buscaPorArtista(requestParams: RequestParams): SearchResults

    suspend fun buscaPorPodcast(requestParams: RequestParams): SearchResults
}