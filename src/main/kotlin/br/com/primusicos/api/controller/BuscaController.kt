package br.com.primusicos.api.controller

import br.com.primusicos.api.domain.resultado.Resultado
import br.com.primusicos.api.service.BuscaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1")
class BuscaController(val service: BuscaService) {

    @GetMapping("/artista")
    fun buscaArtista(@RequestParam nome : String) : ResponseEntity<Resultado> =
        ResponseEntity.ok(service.buscaPorArtista(nome))
}