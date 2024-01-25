package br.com.primusicos.api.service

import br.com.primusicos.api.Infra.exception.FalhaAoRecuperarTokenException
import br.com.primusicos.api.Infra.log.Logs
import br.com.primusicos.api.Infra.security.AuthEncoders
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactor.awaitSingleOrNull
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
    private var token: String? = null,
    private var header: String? = null,
) {
    val headerValue get() = header

    suspend fun atualizaToken(webClient: WebClient) {
        var erros = 0
        while(erros < 3) {
            val chamadaApi = runCatching {
                token = autentica(webClient)
                header = "Bearer $token"
            }

            chamadaApi.onSuccess {
                Logs.autenticacaoConcluida("Tidal")
                return
            }

            chamadaApi.onFailure {
                erros++
                Logs.autenticacaoErro("Tidal", it.localizedMessage, erros)
                Thread.sleep(500)
            }
        }
    }

    private suspend fun autentica(webClient: WebClient): String {
        val json = webClient.post()
            .uri("https://auth.tidal.com/v1/oauth2/token")
            .header("Authorization", B64CREDENTIALS)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
            .retrieve()
            .bodyToMono<String>()
            .awaitSingleOrNull()
            ?: throw FalhaAoRecuperarTokenException()

        return ObjectMapper()
            .readTree(json)
            .path("access_token")
            .asText()
    }
}