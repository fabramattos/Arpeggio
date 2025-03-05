package br.com.arpeggio.api.service

import br.com.arpeggio.api.dto.externalApi.spotify.*
import br.com.arpeggio.api.dto.request.RequestParams
import br.com.arpeggio.api.dto.request.RequestTipo
import br.com.arpeggio.api.dto.response.ItemResponse
import br.com.arpeggio.api.infra.exception.FalhaAoBuscarAlbunsDoArtista
import br.com.arpeggio.api.infra.log.Logs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

private const val VALIDADE_TOKEN = 3600 * 1000L

@Service
class SpotifyService(
    override val NOME_STREAMING: String = "Spotify",
    private val authentication: SpotifyAuthentication,
    private val webClient: WebClient,
) : CommandStreamingAudio {

    @Scheduled(fixedRate = VALIDADE_TOKEN)
    fun atualizaToken() {
        CoroutineScope(Dispatchers.Default)
            .launch {
                authentication.atualizaToken(webClient)
            }
    }

    suspend fun buscaArtista(requestParams: RequestParams): SpotifyApiArtistData {
        var error: Exception? = null
        repeat(2) {
            try {
                Logs.info("ENTRY: SpotifyService/buscaArtista", requestParams.id)
                val uri = UriComponentsBuilder
                    .fromUri(URI("https://api.spotify.com/v1/search"))
                    .queryParam("q", requestParams.busca)
                    .queryParam("type", "artist")
                    .queryParam("market", requestParams.regiao.name)
                    .queryParam("limit", 3)
                    .buildAndExpand()
                    .toUri()

                return webClient
                    .get()
                    .uri { uri }
                    .header("Authorization", authentication.headerValue)
                    .retrieve()
                    .awaitBody<SpotifyApiArtistsResponse>()
                    .artists.items
                    .first()

            } catch (ex: Exception) {
                error = ex
                if (ex.localizedMessage.contains("401")) {
                    Logs.authenticationWarn(NOME_STREAMING)
                    authentication.atualizaToken(webClient)
                } else {
                    Logs.error(NOME_STREAMING, requestParams.id, ex.localizedMessage)
                    return@repeat
                }
            }
        }
        throw Exception(error)
    }


    suspend fun buscaAlbunsDoArtista(requestParams: RequestParams, idArtista: String): SpotifyApiAlbumsResponse {
        repeat(2) {
            try {
                Logs.info("ENTRY: SpotifyService/buscaAlbunsDoArtista", requestParams.id)
                val uri = UriComponentsBuilder
                    .fromUri(URI("https://api.spotify.com/v1/artists/${idArtista}/albums"))
                    .queryParam("include_groups", retornaTipos(requestParams))
                    .queryParam("market", requestParams.regiao.valor)
                    .queryParam("limit", 1)
                    .buildAndExpand()
                    .toUri()

                return webClient
                    .get()
                    .uri { uri }
                    .header("Authorization", authentication.headerValue)
                    .retrieve()
                    .awaitBody<SpotifyApiAlbumsResponse>()

            } catch (ex: Exception) {
                Logs.error(NOME_STREAMING, requestParams.id, ex.localizedMessage)
                Thread.sleep(500)
            }
        }
        throw FalhaAoBuscarAlbunsDoArtista()
    }

    override suspend fun buscaPorArtista(requestParams: RequestParams): ItemResponse {
        Logs.info("ENTRY: SpotifyService/buscaPorArtista", requestParams.id)
        try {
            val artista = buscaArtista(requestParams)
            val totalDeAlbuns = buscaAlbunsDoArtista(requestParams, artista.id).total
            return ItemResponse(
                streaming = NOME_STREAMING,
                consulta = artista.name,
                albuns = totalDeAlbuns
            )
        } catch (ex: Exception) {
            return ItemResponse(
                streaming = NOME_STREAMING,
                consulta = requestParams.busca,
                erro = ex.localizedMessage
            )
        }
    }

    suspend fun buscaPodcasts(requestParams: RequestParams): SpotifyApiPodcastData {
        var error: Exception? = null
        repeat(2) {
            try {
                Logs.info("ENTRY: SpotifyService/buscaPodcasts", requestParams.id)
                val uri = UriComponentsBuilder
                    .fromUri(URI("https://api.spotify.com/v1/search"))
                    .queryParam("q", requestParams.busca)
                    .queryParam("type", "show")
                    .queryParam("market", requestParams.regiao.name)
                    .queryParam("limit", 3)
                    .buildAndExpand()
                    .toUri()

                return webClient
                    .get()
                    .uri { uri }
                    .header("Authorization", authentication.headerValue)
                    .retrieve()
                    .awaitBody<SpotifyApiPodcastsResponse>()
                    .shows.items
                    .first()

            } catch (ex: Exception) {
                error = ex
                if (ex.localizedMessage.contains("401")) {
                    Logs.authenticationWarn(NOME_STREAMING)
                    authentication.atualizaToken(webClient)
                } else {
                    Logs.error(NOME_STREAMING, requestParams.id, ex.localizedMessage)
                    return@repeat
                }
            }
        }
        throw Exception(error)
    }


    override suspend fun buscaPorPodcast(requestParams: RequestParams): ItemResponse {
        Logs.info("ENTRY: SpotifyService/buscaPorPodcast", requestParams.id)
        try {
            val podcast = buscaPodcasts(requestParams)
            return ItemResponse(
                streaming = NOME_STREAMING,
                consulta = podcast.name,
                episodios = podcast.total_episodes
            )

        } catch (ex: Exception) {
            return ItemResponse(
                streaming = NOME_STREAMING,
                consulta = requestParams.busca,
                erro = ex.localizedMessage
            )
        }
    }


    private fun retornaTipos(requestParams: RequestParams): String {
        var texto = ""

        requestParams.tipos
            .filterNot { it == RequestTipo.EP } // -> Spotify não filtra EP! Single = Single + EP
            .forEach { texto = texto.plus(it.name + ",") }

        texto = texto.removeSuffix(",")
        return texto.lowercase()
    }

}





