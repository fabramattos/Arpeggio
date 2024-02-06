package br.com.arpeggio.api.infra.exception

import br.com.arpeggio.api.infra.busca.RequestRegiao
import java.lang.RuntimeException

private val regioes = RequestRegiao.entries.toList()

class RequestParamRegiaoException(requestRegiao: String) : RuntimeException("Parametro 'regiao' com valor '$requestRegiao' inválido! Valores válidos: $regioes")