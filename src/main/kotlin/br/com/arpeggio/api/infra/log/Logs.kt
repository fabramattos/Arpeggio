package br.com.arpeggio.api.infra.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Logs {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Logs::class.java)

        fun error(nomeStreaming: String, idRequest: String, msg: String, erros: Int) {
            logger.error("ERRO EXTERNO: $nomeStreaming | Tentativa: $erros | msg: $msg | requestId: $idRequest")
        }

        fun searchStarted(nome: String, idRequest: String) {
            logger.info("Pesquisando:   '$nome' | requestId: $idRequest")
        }

        fun searchCompleted(nome: String, idRequest: String) {
            logger.info("Finalizado:    '$nome' | requestId: $idRequest")
        }

        fun authenticationError(nomeStreaming: String, msg: String, erros: Int) {
            logger.warn("ERRO AUTENTICAÇÃO: $nomeStreaming | Tentativa: $erros | msg: $msg")
        }

        fun authenticated(nomeStreaming: String) {
            logger.info("Autenticação realizada: $nomeStreaming")
        }
    }
}