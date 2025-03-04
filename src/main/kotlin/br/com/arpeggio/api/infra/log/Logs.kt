package br.com.arpeggio.api.infra.log

import br.com.arpeggio.api.dto.request.RequestParams
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Logs {

    private val logger: Logger = LoggerFactory.getLogger("Logs")

    fun error(nomeStreaming: String, idRequest: String, msg: String, erros: Int) {
        logger.error("ERRO EXTERNO: $nomeStreaming | Tentativa: $erros | msg: $msg | requestId: $idRequest")
    }

    fun searchStarted(request: RequestParams) {
        logger.info("INFO: Pesquisando: $request")
    }

    fun searchCompleted(request: RequestParams) {
        logger.info("INFO: Finalizado: $request")
    }

    fun authenticationWarn(nomeStreaming: String, msg: String, erros: Int) {
        logger.warn("WARN: AUTENTICAÇÃO: $nomeStreaming | Tentativa: $erros | msg: $msg")
    }

    fun authenticated(nomeStreaming: String) {
        logger.info("INFO: Autenticação realizada: $nomeStreaming")
    }

    fun debug(message: String) {
        logger.debug("DEBUG: $message")
    }

    fun info(message: String, requestId: Int) {
        logger.info("INFO: $message, requestId: $requestId")
    }

    fun warn(nomeStreaming: String, idRequest: String, msg: String) {
        logger.warn("WARN: $nomeStreaming | msg: $msg | requestId: $idRequest")
    }
}