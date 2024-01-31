package br.com.arpeggio.api.service

import br.com.arpeggio.api.domain.resultado.ResultadoBusca
import br.com.arpeggio.api.domain.resultado.ResultadoView
import br.com.arpeggio.api.infra.busca.RequestParams
import br.com.arpeggio.api.infra.busca.RequestRegiao
import br.com.arpeggio.api.infra.busca.RequestTipo
import br.com.arpeggio.api.infra.exception.BuscaEmBrancoException
import br.com.arpeggio.api.infra.log.Logs
import br.com.arpeggio.api.utilitario.tratarBuscaArtista
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    fun buscaPorArtista(nome: String, regiao: RequestRegiao, tipo: String): ResultadoView {
        val listaResultados = mutableListOf<ResultadoBusca>()

        if (nome.isBlank())
            throw BuscaEmBrancoException()

        val nomeBusca = nome.tratarBuscaArtista()
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

        runBlocking {
            commandStreamingAudio.forEach { streaming ->
                launch {
                    Logs.consultaIniciada(streaming.NOME_STREAMING, requestParams.id.toString())
                    val resultado = streaming.buscaPorArtista(requestParams)
                    listaResultados.add(resultado)
                    Logs.consultaFinalizada(streaming.NOME_STREAMING, requestParams.id.toString())
                }
            }
        }

        return ResultadoView(nomeBusca, listaResultados)
    }
}