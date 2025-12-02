package br.com.arpeggio.api.infra.log

import br.com.arpeggio.api.dto.request.RequestParams
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Logs {

    private val logger: Logger = LoggerFactory.getLogger("Logs")

    fun error(nomeStreaming: String, idRequest: Int, msg: String) {
        logger.error("ERRO EXTERNO: $nomeStreaming | requestId: $idRequest | msg: $msg")
    }

    fun searchStarted(request: RequestParams) {
        logger.info("INFO: Pesquisando: $request")
    }

    fun searchCompleted(request: RequestParams) {
        logger.info("INFO: Finalizado: $request")
    }

    fun authenticationWarn(nomeStreaming: String) {
        logger.warn("WARN: '$nomeStreaming' NAO AUTENTICADO")
    }

    fun authenticated(nomeStreaming: String) {
        logger.info("INFO: '$nomeStreaming' : Autenticação realizada")
    }

    fun debug(message: String) {
        logger.debug("DEBUG: $message")
    }

    fun info(message: String, requestId: Int) {
        logger.info("INFO: $message | requestId: $requestId")
    }

    fun warn(nomeStreaming: String, idRequest: Int, msg: String) {
        logger.warn("WARN: $nomeStreaming | msg: $msg | requestId: $idRequest")
    }
}