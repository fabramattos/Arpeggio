package br.com.primusicos.api.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class SpotifyService(
    private val webClient: WebClient,

    @Value("\${secrets.spotify_API.id}")
    private val SPOTIFY_API_ID: String,

    @Value("\${secrets.spotify_API.secret}")
    private val SPOTIFY_API_SECRET: String,

    var token: String? = null
) {

    private fun autentica(): SpotifyResponseToken =
        webClient.post()
            .uri("https://accounts.spotify.com/api/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=client_credentials&client_id=${SPOTIFY_API_ID}&client_secret=${SPOTIFY_API_SECRET}")
            .retrieve()
            .bodyToMono(SpotifyResponseToken::class.java) //PODE CRIAR UMA CLASSE DTO NESTE PONTO PARA TRATA A RESPOSTA
            .block()
            ?: throw RuntimeException("Falha ao recuperar Token do Spotify")

    private fun atualizaToken(){
        token = autentica().access_token
    }

    fun buscaArtista(nome: String) : String {
        if (token.isNullOrEmpty())
            atualizaToken()

        val nomeArtista = nome.replace(" ", "+")

        return webClient.get()
            .uri("https://api.spotify.com/v1/search?q=${nomeArtista}&type=artist&market=BR")
            .header ( "Authorization", "Bearer $token")
            .retrieve()
            .bodyToMono(String::class.java) //PODE CRIAR UMA CLASSE DTO NESTE PONTO PARA TRATA A RESPOSTA
            .block()
            ?: throw RuntimeException("Falha ao recuperar Token do Spotify")
    }
}
