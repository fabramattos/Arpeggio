package br.com.primusicos.api.Infra.log

class Logs {
    companion object {
        fun exception(nomeStreaming: String, idRequest: String, msg: String, erros: Int) {
            println("ERRO: requestId = $idRequest |  Serviço = $nomeStreaming | Tentativa = $erros | $msg")
        }

        fun consultaIniciada(nomeStreaming: String, idRequest: String) {
            println("Iniciando Pesquisa: requestId = $idRequest | Serviço = $nomeStreaming")
        }

        fun consultaFinalizada(nomeStreaming: String, idRequest: String) {
            println("Pesquisa Finalizada: requestId = $idRequest | Serviço = $nomeStreaming")
        }

        fun autenticacaoErro(nomeStreaming: String, msg: String, erros: Int) {
            println("ERRO AUTENTICAÇÃO: Serviço = $nomeStreaming | Tentativa = $erros | $msg")
        }

        fun autenticacaoConcluida(nomeStreaming: String) {
            println("Autenticação realizada: $nomeStreaming")
        }
    }
}