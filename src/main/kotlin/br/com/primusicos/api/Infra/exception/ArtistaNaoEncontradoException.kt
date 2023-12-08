package br.com.primusicos.api.Infra.exception

import java.lang.RuntimeException

class ArtistaNaoEncontradoException(msg: String = "Artista não encontrado!") : RuntimeException(msg)
