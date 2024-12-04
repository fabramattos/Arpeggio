package br.com.arpeggio.api.infra.exception

import br.com.arpeggio.api.dto.request.RequestRegiao
import java.lang.RuntimeException

private val regioes = RequestRegiao.entries.toList()

class RequestParamRegiaoException : RuntimeException("Parametro 'regiao' com valor inválido! Valores válidos: $regioes")