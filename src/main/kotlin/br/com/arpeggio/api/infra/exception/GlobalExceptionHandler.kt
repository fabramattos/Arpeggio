package br.com.arpeggio.api.infra.exception

import br.com.arpeggio.api.dto.response.ApiErrorMessageResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun tratarErroGenerco(e: Exception): ResponseEntity<ApiErrorMessageResponse> {
        println("Exception na api: ${e.printStackTrace()}")
        return ResponseEntity
            .internalServerError()
            .body(ApiErrorMessageResponse("Erro na api"))
    }

    @ExceptionHandler(RequestParamNomeException::class)
    fun tratarBuscaEmBranco(e: RequestParamNomeException) =  ResponseEntity
        .badRequest()
        .body(ApiErrorMessageResponse(RequestParamNomeException().localizedMessage))


    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun tratarParametrosAusentes(e: MissingServletRequestParameterException): ResponseEntity<ApiErrorMessageResponse> {
        val msg = mapeamentoParametros(e.localizedMessage)

        return ResponseEntity
            .internalServerError()
            .body(ApiErrorMessageResponse(msg))
    }

    @ExceptionHandler(RequestParamRegiaoException::class)
    fun tratarParametrosRegiaoInvalidos(e: RequestParamRegiaoException): ResponseEntity<ApiErrorMessageResponse> {
        return ResponseEntity
            .internalServerError()
            .body(ApiErrorMessageResponse(e.localizedMessage))
    }

    @ExceptionHandler(RequestParamTipoException::class)
    fun tratarParametrosTipoInvalidos(e: RequestParamTipoException): ResponseEntity<ApiErrorMessageResponse> {
        return ResponseEntity
            .internalServerError()
            .body(ApiErrorMessageResponse(e.localizedMessage))
    }


    private fun mapeamentoParametros(msg: String) : String {
        val listaPalavras = msg.split(" ")
        val paramPos =  listaPalavras.indexOf("parameter") + 1
        val paramValue = listaPalavras[paramPos]

        return "Parametro $paramValue não informado!"
    }

}