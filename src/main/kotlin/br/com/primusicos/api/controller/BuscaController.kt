package br.com.primusicos.api.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1")
class BuscaController {

    @GetMapping("/artista")
    fun buscaArtista(@RequestParam nome : String) : String = "requisição para API agregadora feita com sucesso para: $nome"
}