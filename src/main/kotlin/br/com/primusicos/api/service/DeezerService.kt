package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.exception.ArtistaNaoEncontradoException
import br.com.primusicos.api.Infra.exception.FalhaAoBuscarAlbunsDoArtista
import br.com.primusicos.api.Infra.exception.FalhaAoBuscarArtistasException
import br.com.primusicos.api.domain.resultado.ResultadoBusca
import br.com.primusicos.api.domain.resultado.ResultadoBuscaErros
import br.com.primusicos.api.domain.resultado.ResultadoBuscaOk
import br.com.primusicos.api.domain.streamings.deezer.DeezerAlbum
import br.com.primusicos.api.domain.streamings.deezer.DeezerArtist
import br.com.primusicos.api.domain.streamings.deezer.DeezerResponseAlbum
import br.com.primusicos.api.domain.streamings.deezer.DeezerResponseArtists
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder

@Service
class DeezerService(
    private val webClient: WebClient,
    private val NOME_STREAMING: String = "Deezer",
) : CommandStreamingAudio {

    private fun buscaArtistas(nome: String): List<DeezerArtist> {
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
            .block()
            ?: throw FalhaAoBuscarArtistasException()
    }

    private fun encontraIdArtista(nome: String, artistas: List<DeezerArtist>) =
        artistas
            .find { it.name.equals(nome, true) }
            ?.id
            ?: throw ArtistaNaoEncontradoException()

    private fun buscaAlbunsDoArtista(idArtista: Int): List<DeezerAlbum> {
        val uri = UriComponentsBuilder
            .fromUriString("https://api.deezer.com/artist/$idArtista/albums")
            .queryParam("limit", 999)
            .buildAndExpand()
            .toUri()
        println(uri)

        return webClient
            .get()
            .uri(uri)
            .retrieve()
            .bodyToMono<DeezerResponseAlbum>()
            .map { it.data }
            .block()
            ?.filter {
                it.record_type.equals("album", true)
                        || it.record_type.equals("single", true)
            }
            ?: throw FalhaAoBuscarAlbunsDoArtista()
    }

    override fun buscaPorArtista(nome: String): ResultadoBusca {
        var totalDeAlbuns = 0
        val busca = runCatching {
            val artistas: List<DeezerArtist> = buscaArtistas(nome)
            val idArtista = encontraIdArtista(nome, artistas)
            totalDeAlbuns = buscaAlbunsDoArtista(idArtista).size
        }

        busca.onFailure { return ResultadoBuscaErros(NOME_STREAMING, busca.exceptionOrNull()!!.localizedMessage) }

        return ResultadoBuscaOk(NOME_STREAMING, totalDeAlbuns)
    }

}





