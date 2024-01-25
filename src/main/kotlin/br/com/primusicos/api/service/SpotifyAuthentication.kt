package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.exception.FalhaAoRecuperarTokenException
import br.com.primusicos.api.domain.spotify.SpotifyResponseAuthetication
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

    fun atualizaToken(webClient: WebClient) {
        token = autentica(webClient).access_token
        header = "Bearer $token"
    }

    private fun autentica(webClient: WebClient): SpotifyResponseAuthetication =
        webClient.post()
            .uri("https://accounts.spotify.com/api/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=client_credentials&client_id=${SPOTIFY_API_ID}&client_secret=${SPOTIFY_API_SECRET}")
            .retrieve()
            .bodyToMono<SpotifyResponseAuthetication>()
            .block()
            ?: throw FalhaAoRecuperarTokenException()
}