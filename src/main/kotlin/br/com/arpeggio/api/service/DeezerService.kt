package br.com.arpeggio.api.service

import br.com.arpeggio.api.dto.externalApi.deezer.*
import br.com.arpeggio.api.dto.request.RequestParams
import br.com.arpeggio.api.dto.response.ItemResponse
import br.com.arpeggio.api.infra.exception.FalhaAoBuscarAlbunsDoArtista
import br.com.arpeggio.api.infra.exception.FalhaAoBuscarArtistasException
import br.com.arpeggio.api.infra.exception.FalhaAoBuscarPodcastsException
import br.com.arpeggio.api.infra.exception.PodcastNaoEncontradoException
import br.com.arpeggio.api.infra.log.Logs
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI


@Service
class DeezerService(
    override val NOME_STREAMING: String = "Deezer",
    val webClient: WebClient
) : CommandStreamingAudio {

    private suspend fun buscaArtista(request: RequestParams): DeezerApiArtistData {
        repeat(2) {
            try {
                Logs.info("ENTRY: DeezerService/buscaPorArtista", request.id)
                val uri = UriComponentsBuilder
                    .fromUri(URI("https://api.deezer.com/search/artist"))
                    .queryParam("q", request.busca)
                    .buildAndExpand()
                    .toUri()

                return webClient
                    .get()
                    .uri { uri }
                    .retrieve()
                    .awaitBody<DeezerApiArtistsResponse>()
                    .data
                    .first()

            } catch (ex: Exception) {
                Logs.warn(NOME_STREAMING, request.id, ex.localizedMessage)
                Thread.sleep(500)
            }
        }
        throw FalhaAoBuscarArtistasException()
    }

    private suspend fun buscaPodcasts(requestParams: RequestParams): DeezerApiPodcastData {
        repeat(2) {
            try {
                Logs.info("ENTRY: DeezerService/buscaPodcasts", requestParams.id)

                val uri = UriComponentsBuilder
                    .fromUri(URI("https://api.deezer.com/search/podcast"))
                    .queryParam("q", requestParams.busca)
                    .buildAndExpand()
                    .toUri()

                return webClient
                    .get()
                    .uri { uri }
                    .retrieve()
                    .awaitBody<DeezerApiPodcastsResponse>()
                    .data
                    .first()

            } catch (ex: Exception) {
                Logs.warn(NOME_STREAMING, requestParams.id, ex.localizedMessage)
                Thread.sleep(500)
            }
        }
        throw PodcastNaoEncontradoException()
    }


    private suspend fun buscaAlbunsDoArtista(requestParams: RequestParams, idArtista: Int): List<DeezerApiAlbumData> {
        repeat(2) {
            try {
                Logs.info("ENTRY: DeezerService/buscaPorArtista", requestParams.id)
                val uri = UriComponentsBuilder
                    .fromUri(URI("https://api.deezer.com/artist/$idArtista/albums"))
                    .queryParam("limit", 999)
                    .buildAndExpand()
                    .toUri()

                return webClient
                    .get()
                    .uri { uri }
                    .retrieve()
                    .awaitBody<DeezerApiAlbumsResponse>()
                    .data
                    .filter { album ->
                        requestParams.tipos.any { album.record_type.equals(it.name, true) }
                    }

            } catch (ex: Exception) {
                Logs.error(NOME_STREAMING, requestParams.id, ex.localizedMessage)
                Thread.sleep(500)
            }
        }
        throw FalhaAoBuscarAlbunsDoArtista()
    }


    private suspend fun buscaEpisodiosDoPodcast(requestParam: RequestParams, idPodcast: Int): Int {
        repeat(2) {
            try {
                Logs.info("ENTRY: DeezerService/buscaEpisodiosDoPodcast", requestParam.id)

                val uri = UriComponentsBuilder
                    .fromUri(URI("https://api.deezer.com/podcast/$idPodcast/episodes"))
                    .buildAndExpand()
                    .toUri()

                return webClient
                    .get()
                    .uri { uri }
                    .retrieve()
                    .awaitBody<DeezerApiAlbumsResponse>()
                    .total
            } catch (ex: Exception) {
                Logs.error(NOME_STREAMING, requestParam.id, ex.localizedMessage)
                Thread.sleep(500)
            }
        }
        throw FalhaAoBuscarPodcastsException()
    }


    override suspend fun buscaPorArtista(requestParams: RequestParams): ItemResponse {
        Logs.info("ENTRY: DeezerService/buscaPorArtista", requestParams.id)
        try {
            val artista = buscaArtista(requestParams)
            val totalDeAlbuns = buscaAlbunsDoArtista(requestParams, artista.id).size
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

    override suspend fun buscaPorPodcast(requestParams: RequestParams): ItemResponse {
        Logs.info("ENTRY: DeezerService/buscaPorPodcast", requestParams.id)
        try {
            val podcast = buscaPodcasts(requestParams)
            val totalDeEpisodios = buscaEpisodiosDoPodcast(requestParams, podcast.id)
            return ItemResponse(
                streaming = NOME_STREAMING,
                consulta = podcast.title,
                episodios = totalDeEpisodios
            )
        } catch (ex: Exception) {
            return ItemResponse(
                streaming = NOME_STREAMING,
                consulta = requestParams.busca,
                erro = ex.localizedMessage
            )
        }
    }
}





