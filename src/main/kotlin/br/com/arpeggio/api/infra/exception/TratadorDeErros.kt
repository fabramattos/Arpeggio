package br.com.arpeggio.api.infra.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class TratadorDeErros {

    @ExceptionHandler(Exception::class)
    fun tratarErroGenerco(e: Exception) =  ResponseEntity
        .internalServerError()
        .body(ErroNaApiView("Erro na api"))

    @ExceptionHandler(BuscaEmBrancoException::class)
    fun tratarBuscaEmBranco(e: BuscaEmBrancoException) =  ResponseEntity
        .badRequest()
        .body(ErroNaApiView(BuscaEmBrancoException().localizedMessage))


}