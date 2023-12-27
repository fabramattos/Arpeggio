package br.com.primusicos.api.service

import br.com.primusicos.api.domain.resultado.Resultado
import br.com.primusicos.api.domain.resultado.ResultadoBuscaErros
import br.com.primusicos.api.domain.resultado.ResultadoBuscaOk
import br.com.primusicos.api.utilitario.tratarBuscaArtista
import org.springframework.stereotype.Service

@Service
class BuscaService(
    val spotifyService: SpotifyService,
    val deezerService: DeezerService,
    val youtubeMusicService: YoutubeMusicService,
    val commandStreamingAudio: List<CommandStreamingAudio> = listOf(deezerService, spotifyService, youtubeMusicService)
) {

    fun buscaPorArtista(nome: String): Resultado {
        val nomeBusca = nome.tratarBuscaArtista()
        var listaResultados = emptyList<ResultadoBuscaOk>()
        var listaErros = emptyList<ResultadoBuscaErros>()


        println("Nome Buscado: $nomeBusca")

        commandStreamingAudio.forEach { streaming ->
            val busca = streaming.buscaPorArtista(nomeBusca)

            if (busca is ResultadoBuscaOk)
                listaResultados = listaResultados.plus(busca)

            if (busca is ResultadoBuscaErros)
                listaErros = listaErros.plus(busca)
        }

        return Resultado(nomeBusca, listaResultados, listaErros)

    }
}