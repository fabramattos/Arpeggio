package br.com.arpeggio.api.controller

import br.com.arpeggio.api.dto.response.ResultsResponse
import br.com.arpeggio.api.service.BuscaService
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1")
class BuscaController(val service: BuscaService) {

    @GetMapping("/artista")
    fun buscaArtista(
        @RequestParam (required = true) nome: String,
        @Parameter(
            name = "regiao",
            description = "País para realizar a consulta.\n\nValores aceitos: \"BR\", \"US\"",
            schema = Schema(type = "string", example = "BR", defaultValue = "BR")
        )
        @RequestParam(required = true) regiao: String,
        @Parameter(
            name = "tipo",
            description = "Conteúdo desejado de busca, separado por virgulas.\n\nValores aceitos: \"ALBUM\", \"SINGLE\"",
            schema = Schema(type = "string", example = "ALBUM,SINGLE", defaultValue = "ALBUM")
        )
        @RequestParam(required = true) tipo: String,
    ): ResponseEntity<ResultsResponse> =

        ResponseEntity.ok(service.buscaPorArtista(nome, regiao, tipo))


    @GetMapping("/podcast")
    suspend fun buscaPodcast(
        @RequestParam (required = true) nome: String,
        @Parameter(
            name = "regiao",
            description = "País para realizar a consulta.\n\nValores aceitos: \"BR\", \"US\"",
            schema = Schema(type = "string", example = "BR", defaultValue = "BR")
        )
        @RequestParam(required = true) regiao: String,
    ): ResponseEntity<ResultsResponse> =

        ResponseEntity.ok(service.buscaPorPodcast(nome, regiao))


}