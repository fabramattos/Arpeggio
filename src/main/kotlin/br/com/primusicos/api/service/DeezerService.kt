package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.busca.RequestParams
import br.com.primusicos.api.Infra.exception.ArtistaNaoEncontradoException
import br.com.primusicos.api.Infra.exception.FalhaAoBuscarAlbunsDoArtista
import br.com.primusicos.api.Infra.exception.FalhaAoBuscarArtistasException
import br.com.primusicos.api.Infra.exception.FalhaNaRequisicaoAoStreamingException
import br.com.primusicos.api.Infra.log.Logs
import br.com.primusicos.api.domain.resultado.ResultadoBusca
import br.com.primusicos.api.domain.resultado.ResultadoBuscaConcluida
import br.com.primusicos.api.domain.resultado.ResultadoBuscaErros
import br.com.primusicos.api.domain.streamings.deezer.DeezerAlbum
import br.com.primusicos.api.domain.streamings.deezer.DeezerArtist
import br.com.primusicos.api.domain.streamings.deezer.DeezerResponseAlbum
import br.com.primusicos.api.domain.streamings.deezer.DeezerResponseArtists
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


    private suspend fun buscaArtistas(nome: String): List<DeezerArtist> {
        val uri = UriComponentsBuilder
            .fromUriString("https://api.deezer.com/search/artist")
            .queryParam("q", nome)
            .buildAndExpand()
            .toUri()

        return webClient
            .get()
            .uri(uri)
            .retrieve()
            .bodyToMono<DeezerResponseArtists>()
            .map { it.data }
            .awaitSingleOrNull()
            ?: throw FalhaAoBuscarArtistasException()
    }


    private fun encontraIdArtista(nome: String, artistas: List<DeezerArtist>) =
        artistas
            .find { it.name.equals(nome, true) }
            ?.id
            ?: throw ArtistaNaoEncontradoException()


    private suspend fun buscaAlbunsDoArtista(requestParams: RequestParams, idArtista: Int): List<DeezerAlbum> {
        val uri = UriComponentsBuilder
            .fromUriString("https://api.deezer.com/artist/$idArtista/albums")
            .queryParam("limit", 999)
            .buildAndExpand()
            .toUri()

        return webClient
            .get()
            .uri(uri)
            .retrieve()
            .bodyToMono<DeezerResponseAlbum>()
            .map { it.data }
            .awaitSingleOrNull()
            ?.filter {album ->
                requestParams.tipos.any { tipo ->
                    album.record_type.equals(tipo.name, true) }
            }
            ?: throw FalhaAoBuscarAlbunsDoArtista()
    }


    override suspend fun buscaPorArtista(requestParams: RequestParams): ResultadoBusca {
        var erros = 0
        while(erros < 3){
            val response = runCatching {
                val artistas: List<DeezerArtist> = buscaArtistas(requestParams.busca)
                val idArtista = encontraIdArtista(requestParams.busca, artistas)
                val totalDeAlbuns = buscaAlbunsDoArtista(requestParams, idArtista).size
                return ResultadoBuscaConcluida(NOME_STREAMING, totalDeAlbuns)
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

}





