package br.com.arpeggio.api.service

import br.com.arpeggio.api.infra.exception.BuscaEmBrancoException
import br.com.arpeggio.api.infra.log.Logs
import br.com.arpeggio.api.domain.resultado.Resultado
import br.com.arpeggio.api.domain.resultado.ResultadoBusca
import br.com.arpeggio.api.utilitario.tratarBuscaArtista
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class BuscaService(
    val spotifyService: SpotifyService,
    val deezerService: DeezerService,
    val youtubeMusicService: YoutubeMusicService,
    val tidalService: TidalService,
    val commandStreamingAudio: List<CommandStreamingAudio> = listOf(
        spotifyService,
        deezerService,
        youtubeMusicService,
        tidalService,
    ),
) {

    fun buscaPorArtista(nome: String, regiao: br.com.arpeggio.api.infra.busca.RequestRegiao, tipo: String): Resultado {
        var nomeBusca = ""

        val listaResultados = mutableListOf<ResultadoBusca>()

        val operacao = runCatching {
            if (nome.isBlank())
                throw BuscaEmBrancoException()

            nomeBusca = nome.tratarBuscaArtista()
            println("Nome Buscado: $nomeBusca")

            val tipos: MutableList<br.com.arpeggio.api.infra.busca.RequestTipo> = tipo
                .replace(" ", "")
                .split(",")
                .filter { it.equals("album", true) || it.equals("single", true) }
                .map { br.com.arpeggio.api.infra.busca.RequestTipo.valueOf(it.uppercase()) }
                .toMutableList()
                .also { if (br.com.arpeggio.api.infra.busca.RequestTipo.SINGLE in it) it + br.com.arpeggio.api.infra.busca.RequestTipo.EP }
                .also { if (it.isEmpty()) it.add(br.com.arpeggio.api.infra.busca.RequestTipo.ALBUM) }

            val requestParams = br.com.arpeggio.api.infra.busca.RequestParams(nomeBusca, regiao, tipos)

            runBlocking {
                commandStreamingAudio.forEach { streaming ->
                    launch {
                        Logs.consultaIniciada(streaming.NOME_STREAMING, requestParams.id.toString())
                        val resultado = streaming.buscaPorArtista(requestParams)
                        listaResultados.add(resultado)
                        Logs.consultaFinalizada(streaming.NOME_STREAMING, requestParams.id.toString())
                    }
                }

            }
        }

        operacao.onFailure { return Resultado(nomeBusca, emptyList(), it.localizedMessage) }

        return Resultado(nomeBusca, listaResultados, null)
    }
}