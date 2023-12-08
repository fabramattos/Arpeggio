package br.com.primusicos.api.Infra.exception

import java.lang.RuntimeException

class FalhaAoBuscarArtistasException(msg: String = "Falha ao buscar artistas!") : RuntimeException(msg)