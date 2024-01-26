package br.com.arpeggio.api.infra.exception

import java.lang.RuntimeException

class FalhaNaRequisicaoAoStreamingException(nomeStreaming: String)
    : RuntimeException("Falha na comunicação com $nomeStreaming!")