package br.com.primusicos.api.Infra.exception

import java.lang.RuntimeException

class FalhaAoRecuperarTokenException(msg : String = "Falha ao recuperar Token") : RuntimeException(msg)