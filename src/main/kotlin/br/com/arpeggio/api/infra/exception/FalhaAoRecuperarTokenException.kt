package br.com.arpeggio.api.infra.exception

import java.lang.RuntimeException

class FalhaAoRecuperarTokenException : RuntimeException("Falha ao recuperar Token")