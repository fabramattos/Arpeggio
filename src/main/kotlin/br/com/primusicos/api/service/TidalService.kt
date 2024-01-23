package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.busca.BuscaRequest
import br.com.primusicos.api.Infra.busca.BuscaTipo
import br.com.primusicos.api.Infra.exception.*
import br.com.primusicos.api.Infra.security.AuthEncoders
import br.com.primusicos.api.domain.resultado.ResultadoBusca
import br.com.primusicos.api.domain.resultado.ResultadoBuscaConcluida
import br.com.primusicos.api.domain.resultado.ResultadoBuscaErros
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

private const val DELAY_REQUEST = 800L
private const val DELAY_RETRY = 1000L
private const val LIMIT = 90


@Service
class TidalService(
    private val buscaRequest: BuscaRequest,
    private val NOME_STREAMING: String = "Tidal",
    private val webClient: WebClient,

    @Value("\${secrets.tidal_api.id}")
    private val TIDAL_API_ID: String,

    @Value("\${secrets.tidal_api.secret}")
    private val TIDAL_API_SECRET: String,

    private val B64CREDENTIALS: String = AuthEncoders().encodeToBase64(TIDAL_API_ID, TIDAL_API_SECRET),

    private var HEADER_VALUE: String? = null,
) : CommandStreamingAudio {

    private var TOKEN: String? = null
        private set(value) {
            field = value
            HEADER_VALUE = "Bearer $TOKEN"
        }


    override fun buscaPorArtista(): ResultadoBusca {
        println("Consultando $NOME_STREAMING")
        if (TOKEN.isNullOrEmpty())
            TOKEN = autentica()

        return tentaBuscarPorArtista()
    }


    private fun autentica(): String {
        val json = webClient.post()
            .uri("https://auth.tidal.com/v1/oauth2/token")
            .header("Authorization", B64CREDENTIALS)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
            .retrieve()
            .bodyToMono<String>()
            .block()
            ?: throw FalhaAoRecuperarTokenException()

        return ObjectMapper()
            .readTree(json)
            .path("access_token")
            .asText()
    }

    private fun buscaArtistas(nome: String): JsonNode {
        val uri = uriBuscaArtistas(nome)
        val response = chamadaApiTidal_BuscaArtistas(uri)

        return ObjectMapper()
            .readTree(response)
            .path("artists") // array de artistas. Dentro, RESOURCES contem os dados de cada artista retornado
    }

    private fun chamadaApiTidal_BuscaArtistas(uri: URI) = (webClient
        .get()
        .uri(uri)
        .header("accept", "application/vnd.tidal.v1+json")
        .header("Authorization", HEADER_VALUE)
        .header("Content-Type", "application/vnd.tidal.v1+json")
        .retrieve()
        .bodyToMono<String>()
        .block()
        ?: throw FalhaAoBuscarArtistasException())

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
        var erros = 0
        var offset = 0
        var totalAlbuns = 0
        var maxAlbuns = Int.MAX_VALUE
        while (offset < maxAlbuns && erros <= 3) {
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
                when (it) {
                    is FalhaAoBuscarAlbunsDoArtista,
                    is FalhaInformacoesImprecisasDireitosAutoraisException,
                    -> throw it

                    else -> {
                        val msg = it.localizedMessage
                        println("$NOME_STREAMING: $msg")
                        if (msg.contains("Erro: 429"))
                            Thread.sleep(DELAY_RETRY)
                        else
                            erros++
                    }
                }
            }
        }
        return totalAlbuns
    }

    private fun chamadaApiTidal_AlbunsDoArtista(uri: URI) = (webClient
        .get()
        .uri(uri)
        .header("accept", "application/vnd.tidal.v1+json")
        .header("Authorization", HEADER_VALUE)
        .header("Content-Type", "application/vnd.tidal.v1+json")
        .retrieve()
        .bodyToMono<String>()
        .block()
        ?: throw FalhaAoBuscarAlbunsDoArtista())

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
        var erros = 0
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
                when (it) {
                    is ArtistaNaoEncontradoException,
                    is FalhaInformacoesImprecisasDireitosAutoraisException,
                    ->
                        return ResultadoBuscaErros(NOME_STREAMING, it.localizedMessage)

                    else -> {
                        if (it.localizedMessage.contains("401")) {
                            println("${NOME_STREAMING}: Erro: ${it.localizedMessage} | Tentativa $erros")
                            TOKEN = autentica()
                        }
                        Thread.sleep(500)
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