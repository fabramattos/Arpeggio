package br.com.primusicos.api.service

import br.com.primusicos.api.domain.resultado.ResultadoBusca

interface CommandStreamingAudio {
    fun buscaPorArtista(nome: String) : ResultadoBusca
}