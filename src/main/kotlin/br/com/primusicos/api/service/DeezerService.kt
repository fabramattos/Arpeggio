package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.busca.BuscaRequest
import br.com.primusicos.api.Infra.exception.ArtistaNaoEncontradoException
import br.com.primusicos.api.Infra.exception.FalhaAoBuscarAlbunsDoArtista
import br.com.primusicos.api.Infra.exception.FalhaAoBuscarArtistasException
import br.com.primusicos.api.Infra.exception.FalhaNaRequisicaoAoStreamingException
import br.com.primusicos.api.domain.resultado.ResultadoBusca
import br.com.primusicos.api.domain.resultado.ResultadoBuscaConcluida
import br.com.primusicos.api.domain.resultado.ResultadoBuscaErros
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
    private val buscaRequest: BuscaRequest,
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

        return webClient
            .get()
            .uri(uri)
            .retrieve()
            .bodyToMono<DeezerResponseAlbum>()
            .map { it.data }
            .block()
            ?.filter {album ->
                buscaRequest.tipos.any { tipo ->
                    album.record_type.equals(tipo.name, true) }
            }
            ?: throw FalhaAoBuscarAlbunsDoArtista()
    }

    override fun buscaPorArtista(): ResultadoBusca = tentaBuscarPorArtista()

    private fun tentaBuscarPorArtista(): ResultadoBusca {
        repeat(3){
            try {
                val artistas: List<DeezerArtist> = buscaArtistas(buscaRequest.busca)

                val idArtista = encontraIdArtista(buscaRequest.busca, artistas)
                val totalDeAlbuns = buscaAlbunsDoArtista(idArtista).size
                return ResultadoBuscaConcluida(NOME_STREAMING, totalDeAlbuns)
            }catch (e: ArtistaNaoEncontradoException) {
                return ResultadoBuscaErros(NOME_STREAMING, e.localizedMessage)
            }catch (e: Exception){
                println("Erro no ${NOME_STREAMING} | Tentativa $it | Erro: $e.localizedMessage")
                Thread.sleep(1000)
            }
        }
        return ResultadoBuscaErros(NOME_STREAMING, FalhaNaRequisicaoAoStreamingException(NOME_STREAMING).localizedMessage)
    }
}





