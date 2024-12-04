package br.com.arpeggio.api.service

import br.com.arpeggio.api.dto.response.SearchResults
import br.com.arpeggio.api.dto.response.ResultsResponse
import br.com.arpeggio.api.dto.request.RequestParams
import br.com.arpeggio.api.dto.request.RequestRegiao
import br.com.arpeggio.api.dto.request.RequestTipo
import br.com.arpeggio.api.infra.exception.RequestParamNomeException
import br.com.arpeggio.api.infra.exception.RequestParamRegiaoException
import br.com.arpeggio.api.infra.exception.RequestParamTipoException
import br.com.arpeggio.api.infra.log.Logs
import br.com.arpeggio.api.utilitario.tratarBusca
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

    fun buscaPorArtista(nome: String, requestRegiao: String, requestTipo: String): ResultsResponse {
        if (nome.isBlank())
            throw RequestParamNomeException()

        val nomeBusca = nome.tratarBusca()
        val tipos = montaListaDeTipos(requestTipo)
        val regiao = verificaRegiao(requestRegiao)
        val requestParams = RequestParams(nomeBusca, regiao, tipos)

        val listaResultados = mutableListOf<SearchResults>()
        runBlocking {
            Logs.searchStarted(nomeBusca, requestParams.id.toString())
            commandStreamingAudio.forEach { streaming ->
                launch {
                    val resultado = streaming.buscaPorArtista(requestParams)
                    listaResultados.add(resultado)
                }
            }
        }
        Logs.searchCompleted(nomeBusca, requestParams.id.toString())

        return ResultsResponse(nomeBusca, listaResultados)
    }

    private fun montaListaDeTipos(requestTipo: String): List<RequestTipo> =
        runCatching {
            requestTipo
                .replace(" ", "")
                .split(",")
                .map { RequestTipo.valueOf(it.uppercase()) }
                .toMutableList()
                .apply {
                    if (contains(RequestTipo.SINGLE))
                        add(RequestTipo.EP)
                    else if (contains(RequestTipo.EP))
                        add(RequestTipo.SINGLE)
                }
        }.getOrElse { throw RequestParamTipoException() }


    private fun verificaRegiao(requestRegiao: String): RequestRegiao =
        runCatching { RequestRegiao.valueOf(requestRegiao.uppercase()) }
            .getOrElse { throw RequestParamRegiaoException() }


    fun buscaPorPodcast(nome: String, requestRegiao: String): ResultsResponse {
        if (nome.isBlank())
            throw RequestParamNomeException()

        val nomeBusca = nome.tratarBusca()
        val regiao = verificaRegiao(requestRegiao)
        val requestParams = RequestParams(nomeBusca, regiao, emptyList())

        val listaResultados = mutableListOf<SearchResults>()
        runBlocking {
            Logs.searchStarted(nomeBusca, requestParams.id.toString())
            commandStreamingAudio.forEach { streaming ->
                launch {
                    val resultado = streaming.buscaPorPodcast(requestParams)
                    listaResultados.add(resultado)
                }
            }
        }
        Logs.searchCompleted(nomeBusca, requestParams.id.toString())

        return ResultsResponse(nomeBusca, listaResultados)
    }
}