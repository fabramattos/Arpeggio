package br.com.primusicos.api.controller

import br.com.primusicos.api.Infra.busca.BuscaRegiao
import br.com.primusicos.api.domain.resultado.Resultado
import br.com.primusicos.api.service.BuscaService
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("v1")
@CrossOrigin("https://arpejo.up.railway.app/doc/swagger-ui/index.html")
class BuscaController(val service: BuscaService) {

    @GetMapping("/artista")
    fun buscaArtista(@RequestParam nome: String,
                     @RequestParam(required = false, defaultValue = "BR") regiao: BuscaRegiao,
                     @Parameter(
                         name = "tipo",
                         description = "Conteúdo desejado de busca, separado por virgulas.\n\nValores aceitos: \"ALBUM\", \"SINGLE\"",
                         schema = Schema(
                             type = "String",
                             defaultValue = "ALBUM",
                             example = "ALBUM,SINGLE")
                     )
                     @RequestParam(required = false, defaultValue = "ALBUM") tipo: String,
                     ): ResponseEntity<Resultado> =

        ResponseEntity.ok(service.buscaPorArtista(nome, regiao, tipo))


}