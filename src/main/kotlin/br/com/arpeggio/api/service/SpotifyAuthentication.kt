package br.com.arpeggio.api.service

import br.com.arpeggio.api.infra.exception.FalhaAoRecuperarTokenException
import br.com.arpeggio.api.infra.log.Logs
import br.com.arpeggio.api.domain.spotify.SpotifyResponseAuthetication
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

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

    suspend fun atualizaToken(webClient: WebClient) {
        var erros = 0
        while (erros < 3) {
            val chamadaApi = runCatching {
                token = autentica(webClient).access_token
                header = "Bearer $token"
            }

            chamadaApi.onSuccess {
                Logs.autenticacaoConcluida("Spotify")
                return
            }

            chamadaApi.onFailure {
                erros++
                Logs.autenticacaoErro("Spotify", it.localizedMessage, erros)
                Thread.sleep(500)
            }
        }

    }

    private suspend fun autentica(webClient: WebClient): SpotifyResponseAuthetication =
        webClient.post()
            .uri("https://accounts.spotify.com/api/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=client_credentials&client_id=${SPOTIFY_API_ID}&client_secret=${SPOTIFY_API_SECRET}")
            .retrieve()
            .bodyToMono<SpotifyResponseAuthetication>()
            .awaitSingleOrNull()
            ?: throw FalhaAoRecuperarTokenException()
}