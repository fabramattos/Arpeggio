package br.com.arpeggio.api.service

import br.com.arpeggio.api.dto.response.SearchResults
import br.com.arpeggio.api.dto.response.AlbumsResponse
import br.com.arpeggio.api.dto.response.PodcastsResponse
import br.com.arpeggio.api.dto.response.ExternalErrorResponse
import br.com.arpeggio.api.dto.externalApi.spotify.*
import br.com.arpeggio.api.dto.request.RequestParams
import br.com.arpeggio.api.dto.request.RequestTipo
import br.com.arpeggio.api.infra.exception.*
import br.com.arpeggio.api.infra.log.Logs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder

private const val VALIDADE_TOKEN = 3600*1000L

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


    private suspend fun buscaArtista(requestParams: RequestParams): SpotifyApiArtistData {
        val uri = UriComponentsBuilder
            .fromUriString("https://api.spotify.com/v1/search")
            .queryParam("q", requestParams.busca)
            .queryParam("type", "artist")
            .queryParam("market", requestParams.regiao.name)
            .queryParam("limit", 3)
            .buildAndExpand()
            .toUri()

        return webClient
            .get()
            .uri{uri}
            .header("Authorization", authentication.headerValue)
            .retrieve()
            .bodyToMono<SpotifyApiArtistsResponse>()
            .map { it.artists.items }
            .awaitSingleOrNull()
            ?.first()
            ?: throw ArtistaNaoEncontradoException()
    }

    private suspend fun buscaPodcasts(requestParams: RequestParams): SpotifyApiPodcastData {
        val uri = UriComponentsBuilder
            .fromUriString("https://api.spotify.com/v1/search")
            .queryParam("q", requestParams.busca)
            .queryParam("type", "show")
            .queryParam("market", requestParams.regiao.name)
            .queryParam("limit", 3)
            .buildAndExpand()
            .toUri()

        return webClient
            .get()
            .uri{uri}
            .header("Authorization", authentication.headerValue)
            .retrieve()
            .bodyToMono<SpotifyApiPodcastsResponse>()
            .map { it.shows.items }
            .awaitSingleOrNull()
            ?.first()
            ?: throw PodcastNaoEncontradoException()
    }

    private suspend fun buscaAlbunsDoArtista(requestParams: RequestParams, idArtista: String): SpotifyApiAlbumsResponse {
        val uri = UriComponentsBuilder
            .fromUriString("https://api.spotify.com/v1/artists/${idArtista}/albums")
            .queryParam("include_groups", retornaTipos(requestParams))
            .queryParam("market", requestParams.regiao.valor)
            .queryParam("limit", 1)
            .buildAndExpand()
            .toUri()

        return webClient
            .get()
            .uri{uri}
            .header("Authorization", authentication.headerValue)
            .retrieve()
            .bodyToMono<SpotifyApiAlbumsResponse>()
            .awaitSingleOrNull()
            ?: throw FalhaAoBuscarAlbunsDoArtista()
    }

    override suspend fun buscaPorArtista(requestParams: RequestParams): SearchResults {
        var erros = 0
        while (erros < 3) {
            val resultado = runCatching {
                val artista = buscaArtista(requestParams)
                val totalDeAlbuns = buscaAlbunsDoArtista(requestParams, artista.id).total
                return AlbumsResponse(NOME_STREAMING, artista.name, totalDeAlbuns)
            }

            resultado.onFailure {
                erros++
                Logs.warn(NOME_STREAMING, requestParams.id.toString(), it.localizedMessage, erros)
                if (it is ArtistaNaoEncontradoException)
                    return ExternalErrorResponse(NOME_STREAMING, it.localizedMessage)

                if (it.localizedMessage.contains("401"))
                    authentication.atualizaToken(webClient)
            }
        }
        return ExternalErrorResponse(
            NOME_STREAMING,
            FalhaNaRequisicaoAoStreamingException(NOME_STREAMING).localizedMessage
        )
    }

    override suspend fun buscaPorPodcast(requestParams: RequestParams): SearchResults {
        println("ENTRY: SpotifyService/buscaPorPodcast")
        var erros = 0
        while (erros < 3) {
            val resultado = runCatching {
                val podcast = buscaPodcasts(requestParams)
                return PodcastsResponse(NOME_STREAMING, podcast.name, podcast.total_episodes)
            }

            resultado.onFailure {
                erros++
                Logs.warn(NOME_STREAMING, requestParams.id.toString(), it.localizedMessage, erros)
                if (it is PodcastNaoEncontradoException)
                    return ExternalErrorResponse(NOME_STREAMING, it.localizedMessage)

                if (it.localizedMessage.contains("401"))
                    authentication.atualizaToken(webClient)
            }
        }
        return ExternalErrorResponse(
            NOME_STREAMING,
            FalhaNaRequisicaoAoStreamingException(NOME_STREAMING).localizedMessage
        )
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





