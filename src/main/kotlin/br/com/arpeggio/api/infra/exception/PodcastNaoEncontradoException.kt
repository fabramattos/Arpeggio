package br.com.arpeggio.api.infra.exception

import java.lang.RuntimeException

class PodcastNaoEncontradoException : RuntimeException("Podcast não encontrado!")
