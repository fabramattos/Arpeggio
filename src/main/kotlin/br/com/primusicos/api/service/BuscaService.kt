package br.com.primusicos.api.service

import br.com.primusicos.api.domain.resultado.ResultadoBuscaErros
import br.com.primusicos.api.domain.resultado.Resultado
import br.com.primusicos.api.domain.resultado.ResultadoBuscaOk
import br.com.primusicos.api.utilitario.tratarBuscaArtista
import org.springframework.stereotype.Service

@Service
class BuscaService(
    val spotifyService: SpotifyService,
    val deezerService: DeezerService,
    var listaResultados: List<ResultadoBuscaOk>,
    var listaErros: List<ResultadoBuscaErros>,
) {

    fun buscaPorArtista(nome: String): Resultado {
        val nomeBusca = nome.tratarBuscaArtista()

        listaErros = mutableListOf()
        listaResultados = mutableListOf()

        val busca = spotifyService.buscaPorArtista(nomeBusca)
        val busca2 = deezerService.buscaPorArtista(nomeBusca)

        if (busca is ResultadoBuscaOk)
            listaResultados = listaResultados.plus(busca)

        if (busca is ResultadoBuscaErros)
            listaErros = listaErros.plus(busca)

        if (busca2 is ResultadoBuscaOk)
            listaResultados = listaResultados.plus(busca2)

        if (busca2 is ResultadoBuscaErros)
            listaErros = listaErros.plus(busca2)

        return Resultado(nomeBusca, listaResultados, listaErros)

    }
}