package br.com.arpeggio.api.service

import br.com.arpeggio.api.domain.resultado.ResultadoBusca
import br.com.arpeggio.api.domain.resultado.ResultadoView
import br.com.arpeggio.api.infra.busca.RequestParams
import br.com.arpeggio.api.infra.busca.RequestRegiao
import br.com.arpeggio.api.infra.busca.RequestTipo
import br.com.arpeggio.api.infra.exception.RequestParamNomeException
import br.com.arpeggio.api.infra.exception.RequestParamRegiaoException
import br.com.arpeggio.api.infra.exception.RequestParamTipoException
import br.com.arpeggio.api.infra.log.Logs
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

    fun buscaPorArtista(nome: String, requestRegiao: String, requestTipo: String): ResultadoView {
        if (nome.isBlank())
            throw RequestParamNomeException()

        val nomeBusca = nome.tratarBuscaArtista()
        println("Nome Buscado: $nomeBusca")

        val tipos = montaListaDeTipos(requestTipo)
        val regiao = verificaRegiao(requestRegiao)
        val requestParams = RequestParams(nomeBusca, regiao, tipos)

        val listaResultados = mutableListOf<ResultadoBusca>()
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

        return ResultadoView(nomeBusca, listaResultados)
    }

    private fun montaListaDeTipos(requestTipo: String): MutableList<RequestTipo> {
        val tiposValidos = RequestTipo.entries.map { it.name }

        val tipos = requestTipo
            .replace(" ", "")
            .split(",")
            .toMutableList()

        val tiposInvalidos = tipos
            .filter { it.uppercase() !in tiposValidos }
            .toMutableList()

        if (tiposInvalidos.isNotEmpty())
            throw RequestParamTipoException(tiposInvalidos)

        return tipos
            .map { RequestTipo.valueOf(it.uppercase()) }
            .toMutableList()
            .also {
                if (RequestTipo.SINGLE in it)
                    it + RequestTipo.EP
                else if (RequestTipo.EP in it)
                    it + RequestTipo.SINGLE
            }
    }

    private fun verificaRegiao(requestRegiao: String): RequestRegiao {
        val tiposValidos = RequestRegiao.entries.map { it.name.uppercase() }
        val regiao = requestRegiao.uppercase()

        if(regiao !in tiposValidos)
            throw RequestParamRegiaoException(requestRegiao)

        return RequestRegiao.valueOf(regiao)

    }
}