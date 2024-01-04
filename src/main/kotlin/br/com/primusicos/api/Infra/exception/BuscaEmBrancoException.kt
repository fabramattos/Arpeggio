package br.com.primusicos.api.Infra.exception

import java.lang.RuntimeException

class BuscaEmBrancoException : RuntimeException("Campo 'busca' em branco!")