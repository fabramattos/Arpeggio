package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.busca.BuscaRequest
import br.com.primusicos.api.Infra.busca.BuscaTipo
import br.com.primusicos.api.Infra.exception.*
import br.com.primusicos.api.Infra.security.AuthEncoders
import br.com.primusicos.api.domain.resultado.ResultadoBusca
import br.com.primusicos.api.domain.resultado.ResultadoBuscaErros
import br.com.primusicos.api.domain.resultado.ResultadoBuscaConcluida
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder

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
        val uri = UriComponentsBuilder
            .fromUriString("https://openapi.tidal.com/search")
            .queryParam("query", nome)
            .queryParam("type", "ARTISTS")
            .queryParam("offset", 0)
            .queryParam("limit", 3)
            .queryParam("countryCode", buscaRequest.regiao.name)
            .buildAndExpand()
            .toUri()

        val response = webClient
            .get()
            .uri(uri)
            .header("accept", "application/vnd.tidal.v1+json")
            .header("Authorization", HEADER_VALUE)
            .header("Content-Type", "application/vnd.tidal.v1+json")
            .retrieve()
            .bodyToMono<String>()
            .block()
            ?: throw FalhaAoBuscarArtistasException()

        return ObjectMapper()
            .readTree(response)
            .path("artists") // array de artistas. Dentro, RESOURCES contem os dados de cada artista retornado
    }

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
        var offset = 0
        val limit = 10
        var totalAlbuns = 0
        var maxAlbuns = Int.MAX_VALUE
        while (offset < maxAlbuns) {
            val uri = UriComponentsBuilder
                .fromUriString("https://openapi.tidal.com/artists/${idArtista}/albums")
                .queryParam("countryCode", buscaRequest.regiao.name)
                .queryParam("offset", offset)
                .queryParam("limit", limit)
                .buildAndExpand()
                .toUri()

            val response = webClient
                .get()
                .uri(uri)
                .header("accept", "application/vnd.tidal.v1+json")
                .header("Authorization", HEADER_VALUE)
                .header("Content-Type", "application/vnd.tidal.v1+json")
                .retrieve()
                .bodyToMono<String>()
                .block()
                ?: throw FalhaAoBuscarAlbunsDoArtista()

            val albunsNode = ObjectMapper()
                .readTree(response)
                .path("data")

            maxAlbuns = ObjectMapper()
                .readTree(response)
                .path("metadata")
                .path("total")
                .asInt()

            for (album in albunsNode) {
                if (verificaSePodeIgnorarRestricaoAutoral()) {
                    totalAlbuns += 1
                    continue
                }

                val type = album.path("resource").path("type").asText()
                val status = album.path("status").asInt()

                if (status == 451)
                    throw FalhaInformacoesImprecisasDireitosAutoraisException()

                if (buscaRequest.tipos.any { tipo -> type.equals(tipo.name, true) })
                    totalAlbuns += 1
            }
            offset += limit
            Thread.sleep(500)
        }
        return totalAlbuns
    }


    private fun tentaBuscarPorArtista(): ResultadoBusca {
        var totalDeAlbuns: Int
        repeat(3) {
            try {
                val artistas = buscaArtistas(buscaRequest.busca)
                val idArtista = encontraIdArtista(buscaRequest.busca, artistas)
                totalDeAlbuns = buscaAlbunsDoArtista(idArtista)
                println("Consulta $NOME_STREAMING concluída")
                return ResultadoBuscaConcluida(NOME_STREAMING, totalDeAlbuns)
            } catch (e: ArtistaNaoEncontradoException) {
                return ResultadoBuscaErros(NOME_STREAMING, e.localizedMessage)
            } catch (e: FalhaInformacoesImprecisasDireitosAutoraisException) {
                return ResultadoBuscaErros(NOME_STREAMING, e.localizedMessage)
            } catch (e: Exception) {
                if (e.localizedMessage.contains("401")) {
                    println("Erro no ${NOME_STREAMING} | Tentativa $it | Erro: 401 Unauthorized")
                    TOKEN = autentica()
                } else
                    println("Erro no ${NOME_STREAMING} | Tentativa $it | Erro: ${e.localizedMessage}")
                Thread.sleep(1000)
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