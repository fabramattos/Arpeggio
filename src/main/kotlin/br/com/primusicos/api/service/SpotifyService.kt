package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.busca.RequestParams
import br.com.primusicos.api.Infra.busca.RequestTipo
import br.com.primusicos.api.Infra.exception.ArtistaNaoEncontradoException
import br.com.primusicos.api.Infra.exception.FalhaAoBuscarAlbunsDoArtista
import br.com.primusicos.api.Infra.exception.FalhaAoBuscarArtistasException
import br.com.primusicos.api.Infra.exception.FalhaNaRequisicaoAoStreamingException
import br.com.primusicos.api.domain.resultado.ResultadoBusca
import br.com.primusicos.api.domain.resultado.ResultadoBuscaConcluida
import br.com.primusicos.api.domain.resultado.ResultadoBuscaErros
import br.com.primusicos.api.domain.spotify.SpotifyArtist
import br.com.primusicos.api.domain.spotify.SpotifyResponseAlbum
import br.com.primusicos.api.domain.spotify.SpotifyResponseBusca
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder

private const val NOME_STREAMING: String = "Spotify"

@Service
class SpotifyService(
    private val authentication: SpotifyAuthentication,
    private val webClient: WebClient,
) : CommandStreamingAudio {

    @PostConstruct
    fun init() {
        authentication.atualizaToken(webClient)
    }

    private suspend fun buscaArtistas(requestParams: RequestParams): List<SpotifyArtist> {
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
            .bodyToMono<SpotifyResponseBusca>()
            .map { it.artists.items }
            .awaitSingleOrNull()
            ?: throw FalhaAoBuscarArtistasException()
    }


    private fun encontraIdArtista(requestParams: RequestParams, artistas: List<SpotifyArtist>): String {
        val id = artistas
            .find { it.name.equals(requestParams.busca, true) }
            ?.id

        if (id.isNullOrEmpty())
            throw ArtistaNaoEncontradoException()

        return id
    }


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
        println("Consultando Spotify")
        return tentaBuscarPorArtista(requestParams)
    }


    private suspend fun tentaBuscarPorArtista(requestParams: RequestParams): ResultadoBusca {
        var erros = 0
        while (erros < 3) {
            val resultado = runCatching {
                val artistas: List<SpotifyArtist> = buscaArtistas(requestParams)
                val idArtista = encontraIdArtista(requestParams, artistas)
                val totalDeAlbuns = buscaAlbunsDoArtista(requestParams, idArtista).total
                println("Consulta $NOME_STREAMING concluída")
                return ResultadoBuscaConcluida(NOME_STREAMING, totalDeAlbuns)
            }

            resultado.onFailure {
                erros++
                println("${NOME_STREAMING}: Erro: ${it.localizedMessage} | Tentativa $erros")
                if (it is ArtistaNaoEncontradoException)
                    return ResultadoBuscaErros(NOME_STREAMING, it.localizedMessage)

                if (it.localizedMessage.contains("401")) {
                    Thread.sleep(500)
                    authentication.atualizaToken(webClient)
                }
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
            .filterNot { it == RequestTipo.EP } // -> Spotify não filtra EP! Single = Single + EP
            .forEach { texto = texto.plus(it.name + ",") }
        texto = texto.removeSuffix(",")
        return texto
    }

}





