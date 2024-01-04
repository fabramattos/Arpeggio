package br.com.primusicos.api.Infra.exception

import java.lang.RuntimeException

class FalhaAoRecuperarTokenException : RuntimeException("Falha ao recuperar Token")