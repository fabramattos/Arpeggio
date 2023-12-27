package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.exception.ArtistaNaoEncontradoException
import br.com.primusicos.api.Infra.exception.FalhaAoBuscarAlbunsDoArtista
import br.com.primusicos.api.Infra.exception.FalhaAoBuscarArtistasException
import br.com.primusicos.api.Infra.exception.FalhaAoRecuperarTokenException
import br.com.primusicos.api.domain.resultado.ResultadoBusca
import br.com.primusicos.api.domain.resultado.ResultadoBuscaErros
import br.com.primusicos.api.domain.resultado.ResultadoBuscaOk
import br.com.primusicos.api.domain.spotify.SpotifyArtist
import br.com.primusicos.api.domain.spotify.SpotifyResponseAlbum
import br.com.primusicos.api.domain.spotify.SpotifyResponseAuthetication
import br.com.primusicos.api.domain.spotify.SpotifyResponseBusca
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder

@Service
class SpotifyService(
    private val webClient: WebClient,

    @Value("\${secrets.spotify_API.id}")
    private val SPOTIFY_API_ID: String,

    @Value("\${secrets.spotify_API.secret}")
    private val SPOTIFY_API_SECRET: String,

    private var HEADER_VALUE: String? = null,
    private val NOME_STREAMING: String = "Spotify"
) : CommandStreamingAudio {
    private var TOKEN: String? = null
        private set(value) {
            field = value
            HEADER_VALUE = "Bearer $TOKEN"
        }


    private fun autentica(): SpotifyResponseAuthetication =
        webClient.post()
            .uri("https://accounts.spotify.com/api/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=client_credentials&client_id=${SPOTIFY_API_ID}&client_secret=${SPOTIFY_API_SECRET}")
            .retrieve()
            .bodyToMono<SpotifyResponseAuthetication>()
            .block()
            ?: throw FalhaAoRecuperarTokenException()

    private fun buscaArtistas(nome: String): List<SpotifyArtist> {
        val uri = UriComponentsBuilder
            .fromUriString("https://api.spotify.com/v1/search")
            .queryParam("q", nome)
            .queryParam("type", "artist")
            .queryParam("market", "BR")
            .queryParam("limit", 3)
            .buildAndExpand()
            .toUri()

        return webClient
            .get()
                .uri(uri)
                .header("Authorization", HEADER_VALUE)
                .retrieve()
                .bodyToMono<SpotifyResponseBusca>()
                .map { it.artists.items }
                .block()
            ?: throw FalhaAoBuscarArtistasException()
    }

    private fun encontraIdArtista(nome: String, artistas: List<SpotifyArtist>): String {
        val id = artistas
            .find { it.name.equals(nome, true) }
            ?.id

        if (id.isNullOrEmpty())
            throw ArtistaNaoEncontradoException()

        return id
    }


    private fun buscaAlbunsDoArtista(idArtista: String): SpotifyResponseAlbum {
        val uri = UriComponentsBuilder
            .fromUriString("https://api.spotify.com/v1/artists/${idArtista}/albums")
            .queryParam("include_groups", "album,single")
            .queryParam("limit", 1)
            .buildAndExpand()
            .toUri()

        return webClient
            .get()
                .uri(uri)
                .header("Authorization", HEADER_VALUE)
                .retrieve()
                .bodyToMono<SpotifyResponseAlbum>()
                .block()
            ?: throw FalhaAoBuscarAlbunsDoArtista()
    }

    override fun buscaPorArtista(nome: String): ResultadoBusca {
        println("Consultando Spotify")
        if (TOKEN.isNullOrEmpty())
            TOKEN = autentica().access_token

        var totalDeAlbuns = 0
        val busca = runCatching {
            val artistas: List<SpotifyArtist> = buscaArtistas(nome)
            val idArtista = encontraIdArtista(nome, artistas)
            totalDeAlbuns = buscaAlbunsDoArtista(idArtista).total
        }

        busca.onFailure {return ResultadoBuscaErros(NOME_STREAMING, busca.exceptionOrNull()!!.localizedMessage) }

        return ResultadoBuscaOk(NOME_STREAMING, totalDeAlbuns)
    }

}





