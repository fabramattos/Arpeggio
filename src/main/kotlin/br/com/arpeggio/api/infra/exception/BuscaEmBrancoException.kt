package br.com.arpeggio.api.infra.exception

import java.lang.RuntimeException

class BuscaEmBrancoException : RuntimeException("Campo 'busca' em branco!")