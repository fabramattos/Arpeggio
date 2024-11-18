package br.com.arpeggio.api.infra.exception

import br.com.arpeggio.api.infra.busca.RequestTipo
import java.lang.RuntimeException

private val tipos = RequestTipo.entries.toList()
class RequestParamTipoException : RuntimeException("Parametro 'tipo' com valores inválidos! Valores esperados (separados por virgulas): $tipos")