package br.com.arpeggio.api.infra.exception

import br.com.arpeggio.api.infra.busca.RequestTipo
import java.lang.RuntimeException

private val tipos = RequestTipo.entries.toList()
class RequestParamTipoException(tiposInvalidos: MutableList<String>) : RuntimeException("Parametro 'tipo' com valor $tiposInvalidos inválido! Valores válidos (separados por virgulas): $tipos")