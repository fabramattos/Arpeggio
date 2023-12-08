package br.com.primusicos.api.service

import br.com.primusicos.api.domain.RespostaApp
import org.springframework.stereotype.Service

@Service
class BuscaService(val spotifyService: SpotifyService) {

    fun buscaPorArtista(nome: String) =
        RespostaApp(
            listOf(
                spotifyService.buscaPorArtista(nome)
            )
        )
}