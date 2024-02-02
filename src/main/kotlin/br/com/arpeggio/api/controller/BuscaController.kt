package br.com.arpeggio.api.controller

import br.com.arpeggio.api.domain.resultado.ResultadoView
import br.com.arpeggio.api.infra.busca.RequestRegiao
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
        @RequestParam nome: String,
        @RequestParam(required = false, defaultValue = "BR") regiao: RequestRegiao,
        @Parameter(
            name = "tipo",
            description = "Conteúdo desejado de busca, separado por virgulas.\n\nValores aceitos: \"ALBUM\", \"SINGLE\"",
            schema = Schema(
                type = "String",
                defaultValue = "ALBUM",
                example = "ALBUM,SINGLE"
            )
        )
        @RequestParam(required = false, defaultValue = "ALBUM") tipo: String,
    ): ResponseEntity<ResultadoView> =

        ResponseEntity.ok(service.buscaPorArtista(nome, regiao, tipo))


}