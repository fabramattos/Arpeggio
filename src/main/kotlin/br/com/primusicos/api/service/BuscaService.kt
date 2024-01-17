package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.busca.BuscaRegiao
import br.com.primusicos.api.Infra.busca.BuscaRequest
import br.com.primusicos.api.Infra.busca.BuscaTipo
import br.com.primusicos.api.Infra.exception.BuscaEmBrancoException
import br.com.primusicos.api.domain.resultado.Resultado
import br.com.primusicos.api.domain.resultado.ResultadoBusca
import br.com.primusicos.api.utilitario.tratarBuscaArtista
import org.springframework.stereotype.Service

@Service
class BuscaService(
    val buscaRequest: BuscaRequest,
    val spotifyService: SpotifyService,
    val deezerService: DeezerService,
    val youtubeMusicService: YoutubeMusicService,
    val tidalService: TidalService,
    val commandStreamingAudio: List<CommandStreamingAudio> = listOf(
        deezerService,
        spotifyService,
        youtubeMusicService,
        tidalService,
    )
) {

    fun buscaPorArtista(nome: String, regiao: BuscaRegiao, tipo: String): Resultado {
        var nomeBusca = ""

        var listaResultados = emptyList<ResultadoBusca>()
        val operacao = runCatching {
            if (nome.isBlank())
                throw BuscaEmBrancoException()

            nomeBusca = nome.tratarBuscaArtista()
            println("Nome Buscado: $nomeBusca")

            val tipos: List<BuscaTipo> = tipo
                .split(",")
                .map { BuscaTipo.valueOf(it.uppercase()) }

            buscaRequest.busca = nomeBusca
            buscaRequest.regiao = regiao
            buscaRequest.tipos =  tipos
            commandStreamingAudio.forEach { streaming ->
                val busca = streaming.buscaPorArtista()
                listaResultados = listaResultados.plus(busca)
            }
        }

        operacao.onFailure{ return Resultado(nomeBusca, emptyList(), it.localizedMessage)}

        return Resultado(nomeBusca,listaResultados, null)
    }
}