package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.exception.BuscaEmBrancoException
import br.com.primusicos.api.domain.resultado.Resultado
import br.com.primusicos.api.domain.resultado.ResultadoBusca
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
        var nomeBusca = ""
        var listaResultados = emptyList<ResultadoBusca>()

        val operacao = runCatching {
            if (nome.isBlank())
                throw BuscaEmBrancoException()

            nomeBusca = nome.tratarBuscaArtista()
            println("Nome Buscado: $nomeBusca")

            commandStreamingAudio.forEach { streaming ->
                val busca = streaming.buscaPorArtista(nomeBusca)
                listaResultados = listaResultados.plus(busca)
            }
        }

        return Resultado(nomeBusca, listaResultados, operacao.exceptionOrNull()?.localizedMessage)
    }
}