package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.exception.FalhaAoRecuperarTokenException
import br.com.primusicos.api.Infra.security.AuthEncoders
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class TidalAuthentication(
    @Value("\${secrets.tidal_api.id}")
    private val TIDAL_API_ID: String,

    @Value("\${secrets.tidal_api.secret}")
    private val TIDAL_API_SECRET: String,
    private val B64CREDENTIALS: String = AuthEncoders().encodeToBase64(TIDAL_API_ID, TIDAL_API_SECRET),
    private var TOKEN: String? = null,
    private var HEADER: String? = null
) {
    val HEADER_VALUE get() = HEADER

    fun atualizaToken(webClient: WebClient) {
        if (TOKEN.isNullOrEmpty()) {
            TOKEN = autentica(webClient)
            HEADER = "Bearer $TOKEN"
        }
    }

    private fun autentica(webClient: WebClient): String {
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
}