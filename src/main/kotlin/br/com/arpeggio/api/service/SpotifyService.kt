package br.com.arpeggio.api.service

import br.com.arpeggio.api.domain.resultado.ResultadoBusca
import br.com.arpeggio.api.domain.resultado.ResultadoBuscaConcluidaAlbuns
import br.com.arpeggio.api.domain.resultado.ResultadoBuscaConcluidaPodcast
import br.com.arpeggio.api.domain.resultado.ResultadoBuscaErros
import br.com.arpeggio.api.domain.streamings.spotify.*
import br.com.arpeggio.api.infra.busca.RequestParams
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


    private suspend fun buscaArtistas(requestParams: RequestParams): List<SpotifyArtistData> {
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
            .uri(uri)
            .header("Authorization", authentication.headerValue)
            .retrieve()
            .bodyToMono<SpotifySearchArtistsResponse>()
            .map { it.artists.items }
            .awaitSingleOrNull()
            ?: throw FalhaAoBuscarArtistasException()
    }

    private suspend fun buscaPodcasts(requestParams: RequestParams): List<SpotifyShowData> {
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
            .uri(uri)
            .header("Authorization", authentication.headerValue)
            .retrieve()
            .bodyToMono<SpotifySearchShowsResponse>()
            .map { it.shows.items }
            .awaitSingleOrNull()
            ?: throw FalhaAoBuscarArtistasException()
    }


    private fun encontraIdArtista(requestParams: RequestParams, artistas: List<SpotifyArtistData>) =
        artistas
            .firstOrNull { it.name.equals(requestParams.busca, true) }
            ?.id
            ?: throw ArtistaNaoEncontradoException()

    private fun encontraTotalEpisodios(requestParams: RequestParams, podcasts: List<SpotifyShowData>) =
        podcasts
            .firstOrNull { it.name.equals(requestParams.busca, true) }
            ?.total_episodes
            ?: throw PodcastNaoEncontradoException()


    private suspend fun buscaAlbunsDoArtista(requestParams: RequestParams, idArtista: String): SpotifyResponseAlbum {
        val uri = UriComponentsBuilder
            .fromUriString("https://api.spotify.com/v1/artists/${idArtista}/albums")
            .queryParam("include_groups", retornaTipos(requestParams))
            .queryParam("limit", 1)
            .buildAndExpand()
            .toUri()

        return webClient
            .get()
            .uri(uri)
            .header("Authorization", authentication.headerValue)
            .retrieve()
            .bodyToMono<SpotifyResponseAlbum>()
            .awaitSingleOrNull()
            ?: throw FalhaAoBuscarAlbunsDoArtista()
    }

    override suspend fun buscaPorArtista(requestParams: RequestParams): ResultadoBusca {
        var erros = 0
        while (erros < 3) {
            val resultado = runCatching {
                val artistas: List<SpotifyArtistData> = buscaArtistas(requestParams)
                val idArtista = encontraIdArtista(requestParams, artistas)
                val totalDeAlbuns = buscaAlbunsDoArtista(requestParams, idArtista).total
                return ResultadoBuscaConcluidaAlbuns(NOME_STREAMING, totalDeAlbuns)
            }

            resultado.onFailure {
                erros++
                Logs.exception(NOME_STREAMING, requestParams.id.toString(), it.localizedMessage, erros)
                if (it is ArtistaNaoEncontradoException)
                    return ResultadoBuscaErros(NOME_STREAMING, it.localizedMessage)

                if (it.localizedMessage.contains("401"))
                    authentication.atualizaToken(webClient)
            }
        }
        return ResultadoBuscaErros(
            NOME_STREAMING,
            FalhaNaRequisicaoAoStreamingException(NOME_STREAMING).localizedMessage
        )
    }

    override suspend fun buscaPorPodcast(requestParams: RequestParams): ResultadoBusca {
        var erros = 0
        while (erros < 3) {
            val resultado = runCatching {
                val podcasts: List<SpotifyShowData> = buscaPodcasts(requestParams)
                val totalDeEpisodios = encontraTotalEpisodios(requestParams, podcasts)
                return ResultadoBuscaConcluidaPodcast(NOME_STREAMING, totalDeEpisodios)
            }

            resultado.onFailure {
                erros++
                Logs.exception(NOME_STREAMING, requestParams.id.toString(), it.localizedMessage, erros)
                if (it is PodcastNaoEncontradoException)
                    return ResultadoBuscaErros(NOME_STREAMING, it.localizedMessage)

                if (it.localizedMessage.contains("401"))
                    authentication.atualizaToken(webClient)
            }
        }
        return ResultadoBuscaErros(
            NOME_STREAMING,
            FalhaNaRequisicaoAoStreamingException(NOME_STREAMING).localizedMessage
        )
    }


    private fun retornaTipos(requestParams: RequestParams): String {
        var texto = ""
        requestParams.tipos
            .filterNot { it == br.com.arpeggio.api.infra.busca.RequestTipo.EP } // -> Spotify não filtra EP! Single = Single + EP
            .forEach { texto = texto.plus(it.name + ",") }
        texto = texto.removeSuffix(",")
        return texto
    }

}





