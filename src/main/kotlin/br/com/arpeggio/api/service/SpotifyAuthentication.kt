package br.com.arpeggio.api.service

import br.com.arpeggio.api.dto.externalApi.spotify.SpotifyApiAutheticationResponse
import br.com.arpeggio.api.infra.exception.FalhaNaAutenticacaoException
import br.com.arpeggio.api.infra.log.Logs
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@Component
class SpotifyAuthentication(
    @Value("\${secrets.spotify_api.id}")
    private val SPOTIFY_API_ID: String,

    @Value("\${secrets.spotify_api.secret}")
    private val SPOTIFY_API_SECRET: String,
    private var header: String? = null,
    private var token: String? = null,
) {

    val headerValue get() = header

    suspend fun atualizaToken(webClient: WebClient, attempt: Int = 1, maxAttempts: Int = 3) {
        if(attempt >= maxAttempts)
            throw FalhaNaAutenticacaoException()

        val chamadaApi = runCatching {
            token = autentica(webClient).access_token
            header = "Bearer $token"
        }

        chamadaApi.onSuccess {
            Logs.authenticated("Spotify")
            return
        }

        chamadaApi.onFailure {
            Logs.error("Spotify", 0, it.localizedMessage)
            Thread.sleep(1000)
            atualizaToken(webClient, attempt + 1)
        }
    }

    private suspend fun autentica(webClient: WebClient): SpotifyApiAutheticationResponse {
        return webClient.post()
            .uri("https://accounts.spotify.com/api/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=client_credentials&client_id=${SPOTIFY_API_ID}&client_secret=${SPOTIFY_API_SECRET}")
            .retrieve()
            .awaitBodyOrNull<SpotifyApiAutheticationResponse>()
            ?: throw FalhaNaAutenticacaoException()
    }
}

