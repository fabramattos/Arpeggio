package br.com.arpeggio.api.infra.exception

import java.lang.RuntimeException

class RequestParamNomeException : RuntimeException("Parametro 'nome' não informado!")