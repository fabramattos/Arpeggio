package br.com.primusicos.api.service
import org.springframework.stereotype.Service
@Service
class BuscaService(val spotifyService: SpotifyService) {
    fun buscaArtista(nome: String): String {
        val resposta = spotifyService.buscaPorArtista(nome)
        println(resposta)
        return "requisição para API agregadora feita com sucesso para: $nome"
    }
}