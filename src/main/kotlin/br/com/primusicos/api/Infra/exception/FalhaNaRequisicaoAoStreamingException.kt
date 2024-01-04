package br.com.primusicos.api.Infra.exception

import java.lang.RuntimeException

class FalhaNaRequisicaoAoStreamingException(nomeStreaming: String)
    : RuntimeException("Falha na comunicação com $nomeStreaming!")