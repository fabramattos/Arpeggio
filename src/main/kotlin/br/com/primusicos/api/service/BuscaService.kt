package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.busca.RequestParams
import br.com.primusicos.api.Infra.busca.RequestRegiao
import br.com.primusicos.api.Infra.busca.RequestTipo
import br.com.primusicos.api.Infra.exception.BuscaEmBrancoException
import br.com.primusicos.api.Infra.log.Logs
import br.com.primusicos.api.domain.resultado.Resultado
import br.com.primusicos.api.domain.resultado.ResultadoBusca
import br.com.primusicos.api.utilitario.tratarBuscaArtista
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class BuscaService(
    val spotifyService: SpotifyService,
    val deezerService: DeezerService,
    val youtubeMusicService: YoutubeMusicService,
    val tidalService: TidalService,
    val commandStreamingAudio: List<CommandStreamingAudio> = listOf(
        spotifyService,
        deezerService,
        youtubeMusicService,
        tidalService,
    ),
) {

     suspend fun buscaPorArtista(nome: String, regiao: RequestRegiao, tipo: String): Resultado {
        var nomeBusca = ""

        val listaResultados = mutableListOf<ResultadoBusca>()

        val operacao = runCatching {
            if (nome.isBlank())
                throw BuscaEmBrancoException()

            nomeBusca = nome.tratarBuscaArtista()
            println("Nome Buscado: $nomeBusca")

            val tipos: MutableList<RequestTipo> = tipo
                .replace(" ", "")
                .split(",")
                .filter { it.equals("album", true) || it.equals("single", true) }
                .map { RequestTipo.valueOf(it.uppercase()) }
                .toMutableList()
                .also { if (RequestTipo.SINGLE in it) it + RequestTipo.EP }
                .also { if (it.isEmpty()) it.add(RequestTipo.ALBUM) }

            val requestParams = RequestParams(nomeBusca, regiao, tipos)

            coroutineScope {
                commandStreamingAudio.forEach { streaming ->
                    launch {
                        Logs.consultaIniciada(streaming.NOME_STREAMING, requestParams.id.toString())
                        val resultado = streaming.buscaPorArtista(requestParams)
                        listaResultados.add(resultado)
                        Logs.consultaFinalizada(streaming.NOME_STREAMING, requestParams.id.toString())
                    }
                }

            }
        }

        operacao.onFailure { return Resultado(nomeBusca, emptyList(), it.localizedMessage) }

        return Resultado(nomeBusca, listaResultados, null)
    }
}