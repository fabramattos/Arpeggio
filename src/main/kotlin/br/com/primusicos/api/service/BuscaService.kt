package br.com.primusicos.api.service

import br.com.primusicos.api.domain.resultado.ResultadoBuscaErros
import br.com.primusicos.api.domain.resultado.Resultado
import br.com.primusicos.api.domain.resultado.ResultadoBuscaOk
import org.springframework.stereotype.Service

@Service
class BuscaService(
    val spotifyService: SpotifyService,
    var listaResultados: List<ResultadoBuscaOk>,
    var listaErros: List<ResultadoBuscaErros>,
) {

    fun buscaPorArtista(nome: String): Resultado {
        listaErros = mutableListOf()
        listaResultados = mutableListOf()

        val busca = spotifyService.buscaPorArtista(nome)

        if (busca is ResultadoBuscaOk)
            listaResultados = listaResultados.plus(busca)

        if (busca is ResultadoBuscaErros)
            listaErros = listaErros.plus(busca)

        return Resultado(nome, listaResultados, listaErros)

    }
}