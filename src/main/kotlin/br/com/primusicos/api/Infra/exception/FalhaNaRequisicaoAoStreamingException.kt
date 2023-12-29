package br.com.primusicos.api.Infra.exception

import java.lang.RuntimeException

class FalhaNaRequisicaoAoStreamingException(
    private val nomeStreaming: String,
    msg: String = "Falha na comunicação com $nomeStreaming!",
) : RuntimeException(msg)