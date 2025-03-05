package br.com.arpeggio.api.infra.exception

import java.lang.RuntimeException

class FalhaNaAutenticacaoException : RuntimeException("Falha ao recuperar Token")