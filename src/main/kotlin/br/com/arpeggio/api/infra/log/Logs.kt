package br.com.arpeggio.api.infra.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Logs {

    private val logger: Logger = LoggerFactory.getLogger("Logs")

    fun error(nomeStreaming: String, idRequest: String, msg: String, erros: Int) {
        logger.error("ERRO EXTERNO: $nomeStreaming | Tentativa: $erros | msg: $msg | requestId: $idRequest")
    }

    fun searchStarted(nome: String, idRequest: String) {
        logger.info("INFO: Pesquisando:   '$nome' | requestId: $idRequest")
    }

    fun searchCompleted(nome: String, idRequest: String) {
        logger.info("INFO: Finalizado:    '$nome' | requestId: $idRequest")
    }

    fun authenticationWarn(nomeStreaming: String, msg: String, erros: Int) {
        logger.warn("WARN: AUTENTICAÇÃO: $nomeStreaming | Tentativa: $erros | msg: $msg")
    }

    fun authenticated(nomeStreaming: String) {
        logger.info("INFO: Autenticação realizada: $nomeStreaming")
    }

    fun debug(message: String) {
        logger.debug("DEBUG: MESSAGE: $message")
    }

    fun warn(nomeStreaming: String, idRequest: String, msg: String, erros: Int) {
        logger.warn("WARN: $nomeStreaming | Tentativa: $erros | msg: $msg | requestId: $idRequest")
    }
}