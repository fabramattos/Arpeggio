package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.busca.RequestParams
import br.com.primusicos.api.domain.resultado.ResultadoBusca

interface CommandStreamingAudio {

    val NOME_STREAMING: String
    suspend fun buscaPorArtista(requestParams: RequestParams): ResultadoBusca
}