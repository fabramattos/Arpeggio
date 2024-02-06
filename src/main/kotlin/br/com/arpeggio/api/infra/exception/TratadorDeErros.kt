package br.com.arpeggio.api.infra.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class TratadorDeErros {

    @ExceptionHandler(Exception::class)
    fun tratarErroGenerco(e: Exception): ResponseEntity<ErroNaApiView> {
        println("Exception na api: ${e.printStackTrace()}")
        return ResponseEntity
            .internalServerError()
            .body(ErroNaApiView("Erro na api"))
    }

    @ExceptionHandler(RequestParamNomeException::class)
    fun tratarBuscaEmBranco(e: RequestParamNomeException) =  ResponseEntity
        .badRequest()
        .body(ErroNaApiView(RequestParamNomeException().localizedMessage))


    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun tratarParametrosAusentes(e: MissingServletRequestParameterException): ResponseEntity<ErroNaApiView> {
        val msg = mapeamentoParametros(e.localizedMessage)

        return ResponseEntity
            .internalServerError()
            .body(ErroNaApiView(msg))
    }

    @ExceptionHandler(RequestParamRegiaoException::class)
    fun tratarParametrosRegiaoInvalidos(e: RequestParamRegiaoException): ResponseEntity<ErroNaApiView> {
        return ResponseEntity
            .internalServerError()
            .body(ErroNaApiView(e.localizedMessage))
    }

    @ExceptionHandler(RequestParamTipoException::class)
    fun tratarParametrosTipoInvalidos(e: RequestParamTipoException): ResponseEntity<ErroNaApiView> {
        return ResponseEntity
            .internalServerError()
            .body(ErroNaApiView(e.localizedMessage))
    }


    private fun mapeamentoParametros(msg: String) : String {
        val listaPalavras = msg.split(" ")
        val paramPos =  listaPalavras.indexOf("parameter") + 1
        val paramValue = listaPalavras[paramPos]

        return "Parametro $paramValue não informado!"
    }

}