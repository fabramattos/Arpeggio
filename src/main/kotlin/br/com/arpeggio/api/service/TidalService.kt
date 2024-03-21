package br.com.arpeggio.api.service

import br.com.arpeggio.api.domain.resultado.ResultadoBusca
import br.com.arpeggio.api.domain.resultado.ResultadoBuscaConcluidaAlbuns
import br.com.arpeggio.api.domain.resultado.ResultadoBuscaErros
import br.com.arpeggio.api.domain.streamings.tidal.TidalResult
import br.com.arpeggio.api.infra.busca.RequestParams
import br.com.arpeggio.api.infra.busca.RequestTipo
import br.com.arpeggio.api.infra.exception.*
import br.com.arpeggio.api.infra.log.Logs
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

private const val DELAY_REQUEST = 800L
private const val DELAY_RETRY = 1000L
private const val LIMIT = 90
private const val VALIDADE_TOKEN = 86400 * 1000L


@Service
class TidalService(
    override val NOME_STREAMING: String = "Tidal",
    private val authentication: TidalAuthentication,
    private val webClient: WebClient,
) : CommandStreamingAudio {

    @Scheduled(fixedRate = VALIDADE_TOKEN)
    fun atualizaToken() {
        CoroutineScope(Dispatchers.Default)
            .launch {
                authentication.atualizaToken(webClient)
            }
    }


    override suspend fun buscaPorArtista(requestParams: RequestParams): ResultadoBusca {
        var erros = 0
        while (erros < 3) {
            val resultadoBusca = runCatching {
                val artista = buscaArtista(requestParams)
                    .apply { qty = buscaAlbunsDoArtista(requestParams, id)}

                return ResultadoBuscaConcluidaAlbuns(NOME_STREAMING, artista.name, artista.qty)
            }

            resultadoBusca.onFailure {
                erros++
                Logs.exception(NOME_STREAMING, requestParams.id.toString(), it.localizedMessage, erros)
                when (it) {
                    is ArtistaNaoEncontradoException,
                    is FalhaInformacoesImprecisasDireitosAutoraisException,
                    ->
                        return ResultadoBuscaErros(NOME_STREAMING, it.localizedMessage)

                    else -> {
                        if (it.localizedMessage.contains("401"))
                            authentication.atualizaToken(webClient)
                    }
                }
            }
        }
        return ResultadoBuscaErros(
            NOME_STREAMING, FalhaNaRequisicaoAoStreamingException(NOME_STREAMING).localizedMessage
        )
    }

    override suspend fun buscaPorPodcast(requestParams: RequestParams): ResultadoBusca {
        return ResultadoBuscaErros(NOME_STREAMING, "busca por podcast ainda não implementada")
        //TODO("Not yet implemented")
    }

    private suspend fun buscaArtista(requestParams: RequestParams): TidalResult {
        val uri = uriBuscaArtistas(requestParams)
        val response = chamadaApiTidal_BuscaArtistas(uri)

        val artistaDados = ObjectMapper()
            .readTree(response)
            .path("artists") // array de artistas. Dentro, RESOURCES contem os dados de cada artista retornado
            .first()
            .path("resource")
            ?: throw ArtistaNaoEncontradoException()

        return TidalResult(
            artistaDados.path("id").asText(),
            artistaDados.path("name").asText(),
            0
        )


    }

    private suspend fun chamadaApiTidal_BuscaArtistas(uri: URI): String =
        webClient
            .get()
            .uri(uri)
            .header("accept", "application/vnd.tidal.v1+json")
            .header("Authorization", authentication.headerValue)
            .header("Content-Type", "application/vnd.tidal.v1+json")
            .retrieve()
            .bodyToMono<String>()
            .awaitSingleOrNull()
            ?: throw FalhaAoBuscarArtistasException()


    private fun uriBuscaArtistas(requestParams: RequestParams) = UriComponentsBuilder
        .fromUriString("https://openapi.tidal.com/search")
        .queryParam("query", requestParams.busca)
        .queryParam("type", "ARTISTS")
        .queryParam("offset", 0)
        .queryParam("limit", 3)
        .queryParam("countryCode", requestParams.regiao.name)
        .buildAndExpand()
        .toUri()



    private suspend fun buscaAlbunsDoArtista(requestParams: RequestParams, idArtista: String): Int {
        var errosReq = 0
        var offset = 0
        var totalAlbuns = 0
        var maxAlbuns = Int.MAX_VALUE
        while (offset < maxAlbuns && errosReq <= 3) {
            val resultado = runCatching {

                val uri = uriAlbunsDoArtista(requestParams, idArtista, offset)
                val response = chamadaApiTidal_AlbunsDoArtista(uri)

                val albunsNode = ObjectMapper()
                    .readTree(response)
                    .path("data")

                maxAlbuns = ObjectMapper()
                    .readTree(response)
                    .path("metadata")
                    .path("total")
                    .asInt()

                totalAlbuns += contarAlbunsValidos(requestParams, albunsNode)
            }

            resultado.onSuccess {
                offset += LIMIT
                Thread.sleep(DELAY_REQUEST)
            }

            resultado.onFailure {
                if (it.localizedMessage.contains("Erro: 429")) { // erro de muitas requisições ao Tidal
                    errosReq++
                    Thread.sleep(DELAY_RETRY)
                } else
                    throw it
            }
        }
        return totalAlbuns
    }

    private suspend fun chamadaApiTidal_AlbunsDoArtista(uri: URI) = webClient
        .get()
        .uri(uri)
        .header("accept", "application/vnd.tidal.v1+json")
        .header("Authorization", authentication.headerValue)
        .header("Content-Type", "application/vnd.tidal.v1+json")
        .retrieve()
        .bodyToMono<String>()
        .awaitSingleOrNull()
        ?: throw FalhaAoBuscarAlbunsDoArtista()

    private fun uriAlbunsDoArtista(requestParams: RequestParams, idArtista: String, offset: Int) = UriComponentsBuilder
        .fromUriString("https://openapi.tidal.com/artists/${idArtista}/albums")
        .queryParam("countryCode", requestParams.regiao.name)
        .queryParam("offset", offset)
        .queryParam("limit", LIMIT)
        .buildAndExpand()
        .toUri()


    private fun contarAlbunsValidos(requestParams: RequestParams, albunsNode: JsonNode): Int {
        return albunsNode.count { album ->
            if (verificaSePodeIgnorarRestricaoAutoral(requestParams)) {
                true
            } else {
                val type = album.path("resource").path("type").asText()
                val status = album.path("status").asInt()

                if (status == 451) throw FalhaInformacoesImprecisasDireitosAutoraisException()

                requestParams.tipos.any { tipo -> type.equals(tipo.name, true) }
            }
        }
    }

    private fun verificaSePodeIgnorarRestricaoAutoral(requestParams: RequestParams): Boolean {
        return requestParams
            .tipos.containsAll(listOf(RequestTipo.ALBUM, RequestTipo.SINGLE))
    }
}