package br.com.arpeggio.api.service

import br.com.arpeggio.api.infra.log.Logs
import br.com.arpeggio.api.domain.resultado.ResultadoBusca
import br.com.arpeggio.api.domain.resultado.ResultadoBuscaConcluidaAlbuns
import br.com.arpeggio.api.domain.resultado.ResultadoBuscaConcluidaPodcast
import br.com.arpeggio.api.domain.resultado.ResultadoBuscaErros
import br.com.arpeggio.api.domain.streamings.deezer.*
import br.com.arpeggio.api.infra.busca.RequestParams
import br.com.arpeggio.api.infra.exception.*
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder


@Service
class DeezerService(
    override val NOME_STREAMING: String = "Deezer",
    private val webClient: WebClient,
) : CommandStreamingAudio {


    private suspend fun buscaArtista(nome: String): DeezerArtistData {
        val uri = UriComponentsBuilder
            .fromUriString("https://api.deezer.com/search/artist")
            .queryParam("q", nome)
            .buildAndExpand()
            .toUri()

        return webClient
            .get()
            .uri(uri)
            .retrieve()
            .bodyToMono<DeezerSearchArtistResponse>()
            .map { it.data }
            .awaitSingleOrNull()
            ?.firstOrNull()
            ?: throw ArtistaNaoEncontradoException()
    }

    private suspend fun buscaPodcasts(nome: String): DeezerPodcastData {
        val uri = UriComponentsBuilder
            .fromUriString("https://api.deezer.com/search/podcast")
            .queryParam("q", nome)
            .buildAndExpand()
            .toUri()

        return webClient
            .get()
            .uri(uri)
            .retrieve()
            .bodyToMono<DeezerSearchPodcastResponse>()
            .map { it.data }
            .awaitSingleOrNull()
            ?.firstOrNull()
            ?: throw PodcastNaoEncontradoException()
    }


    private suspend fun buscaAlbunsDoArtista(requestParams: RequestParams, idArtista: Int): List<DeezerIdDetail> {
        val uri = UriComponentsBuilder
            .fromUriString("https://api.deezer.com/artist/$idArtista/albums")
            .queryParam("limit", 999)
            .buildAndExpand()
            .toUri()

        return webClient
            .get()
            .uri(uri)
            .retrieve()
            .bodyToMono<DeezerSearchIdDetailResponse>()
            .map { it.data }
            .awaitSingleOrNull()
            ?.filter { album ->
                requestParams.tipos.any { album.record_type.equals(it.name, true) }
            }
            ?: throw FalhaAoBuscarAlbunsDoArtista()
    }

    private suspend fun buscaEpisodiosDoPodcast(idPodcast: Int): Int {
        val uri = UriComponentsBuilder
            .fromUriString("https://api.deezer.com/podcast/$idPodcast/episodes")
            .buildAndExpand()
            .toUri()

        return webClient
            .get()
            .uri(uri)
            .retrieve()
            .bodyToMono<DeezerSearchIdDetailResponse>()
            .map { it.total }
            .awaitSingleOrNull()
            ?: throw FalhaAoBuscarPodcastsException()
    }


    override suspend fun buscaPorArtista(requestParams: RequestParams): ResultadoBusca {
        var erros = 0
        while(erros < 3){
            val response = runCatching {
                val artista = buscaArtista(requestParams.busca)
                val totalDeAlbuns = buscaAlbunsDoArtista(requestParams, artista.id).size
                return ResultadoBuscaConcluidaAlbuns(NOME_STREAMING, artista.name, totalDeAlbuns)
            }

            response.onFailure {
                erros++
                Logs.exception(NOME_STREAMING, requestParams.id.toString(), it.localizedMessage, erros)
                if(it is ArtistaNaoEncontradoException)
                    return ResultadoBuscaErros(NOME_STREAMING, it.localizedMessage)

                Thread.sleep(1000)
            }
        }
        return ResultadoBuscaErros(NOME_STREAMING, FalhaNaRequisicaoAoStreamingException(NOME_STREAMING).localizedMessage)
    }

    override suspend fun buscaPorPodcast(requestParams: RequestParams): ResultadoBusca {
        var erros = 0
        while(erros < 3){
            val response = runCatching {
                val podcast = buscaPodcasts(requestParams.busca)
                val totalDeEpisodios = buscaEpisodiosDoPodcast(podcast.id)
                return ResultadoBuscaConcluidaPodcast(NOME_STREAMING, podcast.title, totalDeEpisodios)
            }

            response.onFailure {
                erros++
                Logs.exception(NOME_STREAMING, requestParams.id.toString(), it.localizedMessage, erros)
                if(it is PodcastNaoEncontradoException)
                    return ResultadoBuscaErros(NOME_STREAMING, it.localizedMessage)

                Thread.sleep(1000)
            }
        }
        return ResultadoBuscaErros(NOME_STREAMING, FalhaNaRequisicaoAoStreamingException(NOME_STREAMING).localizedMessage)
    }

}





