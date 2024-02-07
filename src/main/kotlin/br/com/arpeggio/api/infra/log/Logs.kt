package br.com.arpeggio.api.infra.log

class Logs {
    companion object {
        fun exception(nomeStreaming: String, idRequest: String, msg: String, erros: Int) {
            println("ERRO EXTERNO: $nomeStreaming | Tentativa: $erros | msg: $msg | requestId: $idRequest")
        }

        fun consultaIniciada(nome: String, idRequest: String) {
            println("Pesquisando:   '$nome' | requestId: $idRequest")
        }

        fun consultaFinalizada(nome: String, idRequest: String) {
            println("Finalizado:    '$nome' | requestId: $idRequest")
        }

        fun autenticacaoErro(nomeStreaming: String, msg: String, erros: Int) {
            println("ERRO AUTENTICAÇÃO: $nomeStreaming | Tentativa: $erros | msg: $msg")
        }

        fun autenticacaoConcluida(nomeStreaming: String) {
            println("Autenticação realizada: $nomeStreaming")
        }
    }
}