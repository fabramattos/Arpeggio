package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.busca.BuscaRequest
import br.com.primusicos.api.Infra.busca.BuscaTipo
import br.com.primusicos.api.Infra.exception.*
import br.com.primusicos.api.domain.resultado.ResultadoBusca
import br.com.primusicos.api.domain.resultado.ResultadoBuscaConcluida
import br.com.primusicos.api.domain.resultado.ResultadoBuscaErros
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

private const val DELAY_REQUEST = 800L
private const val DELAY_RETRY = 1000L
private const val LIMIT = 90


@Service
@RequestScope
class TidalService(
    private val authentication: TidalAuthentication,
    private val buscaRequest: BuscaRequest,
    private val NOME_STREAMING: String = "Tidal",
    private val webClient: WebClient,
    private var erros: Int = 0,
) : CommandStreamingAudio {

    override fun buscaPorArtista(): ResultadoBusca {
        println("Consultando $NOME_STREAMING")
        authentication.atualizaToken(webClient)
        return tentaBuscarPorArtista()
    }

    private fun buscaArtistas(nome: String): JsonNode {
        val uri = uriBuscaArtistas(nome)
        val response = chamadaApiTidal_BuscaArtistas(uri)

        return ObjectMapper()
            .readTree(response)
            .path("artists") // array de artistas. Dentro, RESOURCES contem os dados de cada artista retornado
    }

    private fun chamadaApiTidal_BuscaArtistas(uri: URI): String {
        val response = runCatching {
            webClient
                .get()
                .uri(uri)
                .header("accept", "application/vnd.tidal.v1+json")
                .header("Authorization", authentication.HEADER_VALUE)
                .header("Content-Type", "application/vnd.tidal.v1+json")
                .retrieve()
                .bodyToMono<String>()
                .block()
        }

        response.onFailure {
            if (it.localizedMessage.contains("401"))
                throw FalhaNaoAuthorizadoException()
        }

        return response.getOrNull().toString()
    }

    private fun uriBuscaArtistas(nome: String) = UriComponentsBuilder
        .fromUriString("https://openapi.tidal.com/search")
        .queryParam("query", nome)
        .queryParam("type", "ARTISTS")
        .queryParam("offset", 0)
        .queryParam("limit", 3)
        .queryParam("countryCode", buscaRequest.regiao.name)
        .buildAndExpand()
        .toUri()

    private fun encontraIdArtista(nome: String, artistasNode: JsonNode): String {
        for (artistaNode in artistasNode) {
            val nomeArtista = artistaNode
                .path("resource")
                .path("name").asText()
            if (nomeArtista.equals(nome, true))
                return artistaNode.path("resource").path("id").asText()
        }
        throw ArtistaNaoEncontradoException()
    }

    private fun buscaAlbunsDoArtista(idArtista: String): Int {
        var errosReq = 0
        var offset = 0
        var totalAlbuns = 0
        var maxAlbuns = Int.MAX_VALUE
        while (offset < maxAlbuns && errosReq <= 3) {
            val resultado = runCatching {

                val uri = uriAlbunsDoArtista(idArtista, offset)
                val response = chamadaApiTidal_AlbunsDoArtista(uri)

                val albunsNode = ObjectMapper()
                    .readTree(response)
                    .path("data")

                maxAlbuns = ObjectMapper()
                    .readTree(response)
                    .path("metadata")
                    .path("total")
                    .asInt()

                totalAlbuns += contarAlbunsValidos(albunsNode)
            }

            resultado.onSuccess {
                offset += LIMIT
                Thread.sleep(DELAY_REQUEST)
            }

            resultado.onFailure {
                if (it.localizedMessage.contains("Erro: 429")) { // erro de muitas requisições ao Tidal
                    errosReq++
                    Thread.sleep(DELAY_RETRY)
                }else
                    throw it
            }
        }
        return totalAlbuns
    }

    private fun chamadaApiTidal_AlbunsDoArtista(uri: URI) = webClient
        .get()
        .uri(uri)
        .header("accept", "application/vnd.tidal.v1+json")
        .header("Authorization", authentication.HEADER_VALUE)
        .header("Content-Type", "application/vnd.tidal.v1+json")
        .retrieve()
        .bodyToMono<String>()
        .block()
        ?: throw FalhaAoBuscarAlbunsDoArtista()

    private fun uriAlbunsDoArtista(idArtista: String, offset: Int) = UriComponentsBuilder
        .fromUriString("https://openapi.tidal.com/artists/${idArtista}/albums")
        .queryParam("countryCode", buscaRequest.regiao.name)
        .queryParam("offset", offset)
        .queryParam("limit", LIMIT)
        .buildAndExpand()
        .toUri()


    private fun contarAlbunsValidos(albunsNode: JsonNode): Int {
        return albunsNode.count { album ->
            if (verificaSePodeIgnorarRestricaoAutoral()) {
                true
            } else {
                val type = album.path("resource").path("type").asText()
                val status = album.path("status").asInt()

                if (status == 451) throw FalhaInformacoesImprecisasDireitosAutoraisException()

                buscaRequest.tipos.any { tipo -> type.equals(tipo.name, true) }
            }
        }
    }


    private fun tentaBuscarPorArtista(): ResultadoBusca {
        var totalDeAlbuns = 0
        while (erros < 3) {
            val resultadoBusca = runCatching {
                val artistas = buscaArtistas(buscaRequest.busca)
                val idArtista = encontraIdArtista(buscaRequest.busca, artistas)
                totalDeAlbuns = buscaAlbunsDoArtista(idArtista)
                println("Consulta $NOME_STREAMING concluída")
            }

            resultadoBusca.onSuccess { return ResultadoBuscaConcluida(NOME_STREAMING, totalDeAlbuns) }

            resultadoBusca.onFailure {
                erros++
                println("${NOME_STREAMING}: Erro: ${it.localizedMessage} | Tentativa $erros")
                when (it) {
                    is ArtistaNaoEncontradoException,
                    is FalhaInformacoesImprecisasDireitosAutoraisException,
                    ->
                        return ResultadoBuscaErros(NOME_STREAMING, it.localizedMessage)

                    is FalhaNaoAuthorizadoException -> {
                        Thread.sleep(500)
                        authentication.atualizaToken(webClient)
                        tentaBuscarPorArtista()
                    }
                }
            }
        }
        return ResultadoBuscaErros(
            NOME_STREAMING, FalhaNaRequisicaoAoStreamingException(NOME_STREAMING).localizedMessage
        )
    }

    private fun verificaSePodeIgnorarRestricaoAutoral(): Boolean {
        return buscaRequest.tipos.containsAll(listOf(BuscaTipo.ALBUM, BuscaTipo.SINGLE))
    }
}